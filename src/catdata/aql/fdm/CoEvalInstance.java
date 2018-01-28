package catdata.aql.fdm;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.Var;

public class CoEvalInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
extends Instance<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y, ID, Chc<Y, Pair<ID, Att1>>> {	
	
	private final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	@SuppressWarnings("unused")
	private final Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y>  J;
	private final InitialAlgebra<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y, ID> init;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y, ID, Chc<Y, Pair<ID, Att1>>> I;
	
	public CoEvalInstance(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q, Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> J, AqlOptions options) {
		if (!Q.dst.equals(J.schema())) {
			throw new RuntimeException("In co-eval instance, target of query is " + Q.dst + ", but instance has type " + J.schema());
		} else if (!Q.consts.keySet().containsAll(Q.params.keySet())) {
			throw new RuntimeException("Missing bindings: " + Util.sep(Util.diff(Q.params.keySet(), Q.consts.keySet()), ",")); //TODO aql
		} else if (!Q.consts.keySet().isEmpty()) {
			Q = Q.deParam();
		}
		

		this.Q = Q;
		this.J = J;

		Set<Pair<Term<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y>, Term<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y>>> eqs = new HashSet<>();

		Collage<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y> col = new Collage<>(Q.src.collage());
		col.sks.putAll(J.algebra().talg().sks.map);
		for (Eq<Ty, Void, Sym, Void, Void, Void, Y> eq : J.algebra().talg().eqs) {
			if (!eq.ctx.isEmpty()) {
				throw new RuntimeException("Anomaly: please report");
			}
			eqs.add(new Pair<>(Term.upTalg(eq.lhs), Term.upTalg(eq.rhs)));		
		}
		
		for (En2 t : J.schema().ens) {
			for (X j : J.algebra().en(t)) {
				for (Var v : Q.ens.get(t).gens.keySet()) {
					En1 s = Q.ens.get(t).gens.get(v);
					col.gens.put(new Pair<>(v, j), s);
				}
				for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Var> eq : Q.ens.get(t).eqs) {
					if (!eq.ctx.isEmpty()) {
						throw new RuntimeException("Anomaly: please report");
					}
					eqs.add(new Pair<>(eq.lhs.mapGenSk(x -> new Pair<Var,X>(x, j), x -> Util.anomaly()), 
									   eq.rhs.mapGenSk(x -> new Pair<>(x, j), x -> Util.anomaly())));
				}
				for (Fk2 fk : J.schema().fksFrom(t)) {
					Transform<Ty, En1, Sym, Fk1, Att1, Var, Var, Var, Var, ID, Chc<Var, Pair<ID, Att1>>, ID, Chc<Var, Pair<ID, Att1>>> 
					fk0 = Q.fks.get(fk);
					for (Var v0 : fk0.src().gens().keySet()) {
						Term<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y> 
						rhs = fk0.gens().get(v0).map(Util.voidFn(),Util.voidFn(),Function.identity(),Util.voidFn(),x->new Pair<>(x, j),Util::abort);
						Term<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y> 
						lhs = Term.Gen(new Pair<>(v0, J.algebra().fk(fk, j))); 
						eqs.add(new Pair<>(lhs, rhs));
					}
				}
				for (Att2 att : J.schema().attsFrom(t)) {
					Term<Ty, En1, Sym, Fk1, Att1, Var, Var> att0 = Q.atts.get(att);
					Term<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y> 
					rhs = att0.mapGenSk(x -> new Pair<>(x, j), x->Util.anomaly());
					Term<Ty, En1, Sym, Fk1, Att1, Pair<Var,X>, Y> 
					lhs = J.algebra().att(att, j).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity()); 
					eqs.add(new Pair<>(lhs, rhs));					
				}
			}
		}
		
		for (Pair<Term<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y>, Term<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y>> eq : eqs) {
			col.eqs.add(new Eq<>(new Ctx<>(), eq.first, eq.second));			
		}
	
		//AqlOptions strat = new AqlOptions(options, col);  
				
		Function<Pair<Var,X>, String> printGen = x -> x.first + " " + J.algebra().printX(x.second);
		Function<Y, String> printSk = J.algebra()::printY; //TODO aql printing coeval
		
		init = new InitialAlgebra<>(options, schema(), col, new It(), printGen, printSk);	
		I = new LiteralInstance<>(schema(), col.gens.map, col.sks.map, eqs, init.dp(), init, (Boolean) options.getOrDefault(AqlOption.require_consistency), (Boolean) options.getOrDefault(AqlOption.allow_java_eqs_unsafe)); 
		validate();
	}

	@Override
	public Schema<Ty, En1, Sym, Fk1, Att1> schema() {
		return Q.src;
	}

	@Override
	public Ctx<Pair<Var, X>, En1> gens() {
		return I.gens();
	}

	@Override
	public Ctx<Y, Ty> sks() {
		return I.sks();
	}

	@Override
	public Set<Pair<Term<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y>, Term<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y>>> eqs() {
		return I.eqs();
	}

	@Override
	public DP<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y> dp() {
		return I.dp();
	}

	@Override
	public Algebra<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y, ID, Chc<Y, Pair<ID, Att1>>> algebra() {
		return I.algebra();
	}

	@Override
	public boolean requireConsistency() {
		return I.requireConsistency();
	}

	@Override
	public boolean allowUnsafeJava() {
		return I.allowUnsafeJava();
	}
	
}	
	