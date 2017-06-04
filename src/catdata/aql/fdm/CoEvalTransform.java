package catdata.aql.fdm;

import java.util.function.Function;

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

public class CoEvalTransform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
extends Transform<Ty, En1, Sym, Fk1, Att1, Pair<Var,X1>, Y1, Pair<Var,X2>, Y2, ID, Chc<Y1, Pair<ID, Att1>>, ID, Chc<Y2, Pair<ID, Att1>>> {

	private final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	@SuppressWarnings("unused")
	private final Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h;
	
	private final CoEvalInstance<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, X1, Y1> src;
	private final CoEvalInstance<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2, En2, Fk2, Att2, X2, Y2> dst;

	private final Ctx<Pair<Var, X1>, Term<Void, En1, Void, Fk1, Void, Pair<Var, X2>, Void>> gens = new Ctx<>();
	private final Ctx<Y1, Term<Ty, En1, Sym, Fk1, Att1, Pair<Var, X2>, Y2>> sks = new Ctx<>();
	
	
	//TODO aql recomputes
	public CoEvalTransform(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h, AqlOptions options1, AqlOptions options2) {
		if (!h.src().schema().equals(q.dst)) {
			throw new RuntimeException("Target of query is " + q.dst + " but transform is on " + h.src().schema());
		}
	
		Q = q;
		this.h = h;
		
		src = new CoEvalInstance<>(Q, h.src(), options1);
		dst = new CoEvalInstance<>(Q, h.dst(), options2);
		
		for (Pair<Var, X1> gen1 : src.gens().keySet()) {
			gens.put(gen1, Term.Gen(new Pair<>(gen1.first, h.repr(gen1.second)))); 
		}
		for (Y1 sk1 : src.sks().keySet()) {
			sks.put(sk1, h.dst().algebra().intoY(h.reprT(sk1)).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity()));
		}
		
		validate(false);
	}

	@Override
	public Ctx<Pair<Var, X1>, Term<Void, En1, Void, Fk1, Void, Pair<Var, X2>, Void>> gens() {
		return gens;
	}

	@Override
	public Ctx<Y1, Term<Ty, En1, Sym, Fk1, Att1, Pair<Var, X2>, Y2>> sks() {
		return sks;
	}
	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Pair<Var, X1>, Y1, ID, Chc<Y1, Pair<ID, Att1>>> src() {
		return src;
	}

	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Pair<Var, X2>, Y2, ID, Chc<Y2, Pair<ID, Att1>>> dst() {
		return dst;
	}
	
}
