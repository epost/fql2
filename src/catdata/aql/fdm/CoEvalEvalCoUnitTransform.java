package catdata.aql.fdm;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Instance;
import catdata.aql.Query;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.Var;
import catdata.aql.It.ID;
import catdata.aql.fdm.EvalAlgebra.Row;

public class CoEvalEvalCoUnitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>  
extends Transform<Ty, En1, Sym, Fk1, Att1, Pair<Var,Row<En2,X>>, Y, Gen, Sk, ID, Chc<Y, Pair<ID, Att1>>, X, Y> {
	
	private final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;
	private final EvalInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> J;
	private final CoEvalInstance<Ty, En1, Sym, Fk1, Att1, Row<En2, X>, Y, En2, Fk2, Att2, Row<En2, X>, Y> K; //TODO aql recomputes
	private final Ctx<Pair<Var, Row<En2, X>>, Term<Void, En1, Void, Fk1, Void, Gen, Void>> gens = new Ctx<>();
	private final Ctx<Y, Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> sks = new Ctx<>();
	
	public CoEvalEvalCoUnitTransform(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, AqlOptions options) {
		if (!q.src.equals(i.schema())) { 
			throw new RuntimeException("Q has src schema " + q.src + " but instance has schema " + i.schema());
		}
		Q = q;
		I = i;
		J = new EvalInstance<>(Q, I, options);
		K = new CoEvalInstance<>(Q, J, options);
		
		for (Pair<Var, Row<En2, X>> gen : src().gens().keySet()) {
			gens.put(gen, I.algebra().repr(gen.second.get(gen.first)));
		}
		for (Y y : src().sks().keySet()) {
			sks.put(y, I.algebra().reprT(Term.Sk(y)));
		}		
		validate((Boolean) options.getOrDefault(AqlOption.dont_validate_unsafe));
	}
	
	@Override
	public Ctx<Pair<Var, Row<En2, X>>, Term<Void, En1, Void, Fk1, Void, Gen, Void>> gens() {
		return gens;
	}
	
	@Override
	public Ctx<Y, Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> sks() {
		return sks;
	}
	
	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Pair<Var, Row<En2, X>>, Y, ID, Chc<Y, Pair<ID, Att1>>> src() {
		return K;
	}
	
	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> dst() {
		return I;
	}

}
