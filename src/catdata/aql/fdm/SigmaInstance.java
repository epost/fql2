package catdata.aql.fdm;

import java.io.File;
import java.io.IOException;
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
import catdata.aql.Mapping;
import catdata.aql.Schema;
import catdata.aql.Term;

public class SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
 extends Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, ID, Chc<Sk, Pair<ID, Att2>>> {
	
	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;
	private final LiteralInstance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, ID, Chc<Sk, Pair<ID, Att2>>> J;

	//options has to come in as a list, because conversion to AqlOptions requires the sigma'd collage
	public SigmaInstance(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, AqlOptions strat) {
		if (!f.src.equals(i.schema())) {
			throw new RuntimeException("In sigma instance, source of mapping is " + f.src + ", but instance has type " + i.schema());
		}
		F = f;
		I = i;
		
		Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col = new Collage<>(F.dst.collage());
		
		col.sks.putAll(I.sks().map);
		for (Gen gen : I.gens().keySet()) {
			col.gens.put(gen, F.ens.get(I.gens().get(gen)));
		}
		
		Set<Pair<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> eqs = new HashSet<>();
		for (Pair<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>, Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> eq : I.eqs()) {
			eqs.add(new Pair<>(F.trans(eq.first), F.trans(eq.second)));
			col.eqs.add(new Eq<>(new Ctx<>(), F.trans(eq.first), F.trans(eq.second)));
		}
		/* do not delete - in use by ryan for prover experiments with sarah
		try {
			Util.writeFile(col.tptp(), "/Users/ryan/Desktop/sigma" + System.currentTimeMillis() + ".tptp");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Util.anomaly();
		*/
		Function<Gen,String> printGen = x -> I.algebra().printX(I.algebra().nf(Term.Gen(x)));
		Function<Sk, String> printSk = x -> I.algebra().sk(x).toString(I.algebra()::printY, Util.voidFn());
		InitialAlgebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, ID> initial 
		= new InitialAlgebra<>(strat, schema(), col, new It(), printGen, printSk);
				
		J = new LiteralInstance<>(schema(), col.gens.map, col.sks.map, eqs, initial.dp(), initial, (Boolean) strat.getOrDefault(AqlOption.require_consistency), (Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)); 
		validate();
	}

	@Override
	public Schema<Ty, En2, Sym, Fk2, Att2> schema() {
		return F.dst;
	}
	
	@Override
	public Ctx<Gen, En2> gens() {
		return J.gens();
	}
	
	@Override
	public Ctx<Sk, Ty> sks() {
		return J.sks();
	}
	
	@Override
	public Set<Pair<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> eqs() {
		return J.eqs();
	}
	
	@Override
	public DP<Ty, En2, Sym, Fk2, Att2, Gen, Sk> dp() {
		return J.dp();
	}
	
	@Override
	public Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, ID, Chc<Sk, Pair<ID, Att2>>> algebra() {
		return J.algebra();
	}

	@Override
	public boolean requireConsistency() {
		return J.requireConsistency();
	}

	@Override
	public boolean allowUnsafeJava() {
		return J.allowUnsafeJava();
	}
	
	

}
