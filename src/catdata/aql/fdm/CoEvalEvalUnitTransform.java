package catdata.aql.fdm;

import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Instance;
import catdata.aql.It.ID;
import catdata.aql.Query;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.Var;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.fdm.EvalAlgebra.Row;

public class CoEvalEvalUnitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>  
extends Transform<Ty, En2, Sym, Fk2, Att2, Gen, Sk, Row<En2,ID>, Chc<Y, Pair<ID, Att1>>, X, Y, Row<En2,ID>, Chc<Y, Pair<ID, Att1>>> {
	//TODO aql recomputes
	private final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	private final Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I;
	private final CoEvalInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> J;
	private final EvalInstance<Ty, En1, Sym, Fk1, Att1, Pair<Var, X>, Y, En2, Fk2, Att2, ID, Chc<Y, Pair<ID, Att1>>> K; //TODO aql recomputes
	private final Ctx<Gen, Term<Void, En2, Void, Fk2, Void, Row<En2, ID>, Void>> gens = new Ctx<>();
	private final Ctx<Sk, Term<Ty, En2, Sym, Fk2, Att2, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>>> sks = new Ctx<>();
	
	public CoEvalEvalUnitTransform(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> i, AqlOptions options) {
		if (!q.dst.equals(i.schema())) {
			throw new RuntimeException("Q has dst schema " + q.src + " but instance has schema " + i.schema());
		}
		Q = q;
		I = i;
		J = new CoEvalInstance<>(Q, I, options);
		K = new EvalInstance<>(Q, J, options);
		
		for (Gen gen : src().gens().keySet()) {
			X x = I.algebra().nf(Term.Gen(gen));
			En2 en2 = src().gens().get(gen);			
			Ctx<Var, ID> tuple = new Ctx<>();
			for (Var v : Q.ens.get(en2).gens.keySet()) {
				ID id = J.algebra().gen(new Pair<>(v, x));
				tuple.put(v, id);
			}
			Row<En2, ID> row = Row.mkRow(tuple, en2);		
			Term<Void, En2, Void, Fk2, Void, Row<En2, ID>, Void> term = Term.Gen(row);
			gens.put(gen, term);
		}
		for (Sk sk : src().sks().keySet()) {
			Term<Ty, Void, Sym, Void, Void, Void, Y> y = I.algebra().sk(sk);
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Pair<ID, Att1>>> y2 = trans0(y);
			Term<Ty, En2, Sym, Fk2, Att2, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>> w = y2.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
			sks.put(sk, w);
		}	
		validate((Boolean) options.getOrDefault(AqlOption.dont_validate_unsafe));
	}
	
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Pair<ID, Att1>>> trans0(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
		if (term.sk != null) {
			return Term.Sk(Chc.inLeft(term.sk));
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::trans0).collect(Collectors.toList()));
		} else if (term.obj != null) {
			return term.asObj();
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	@Override
	public Ctx<Gen, Term<Void, En2, Void, Fk2, Void, Row<En2, ID>, Void>> gens() {
		return gens;
	}
	@Override
	public Ctx<Sk, Term<Ty, En2, Sym, Fk2, Att2, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>>> sks() {
		return sks;
	}
	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> src() {
		return I;
	}
	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>> dst() {
		return K;
	}		

}
