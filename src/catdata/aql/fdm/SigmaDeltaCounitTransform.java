package catdata.aql.fdm;

import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.It.ID;


public class SigmaDeltaCounitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
extends Transform<Ty, En2, Sym, Fk2, Att2, Pair<En1, X>, Y, Gen, Sk, ID, Chc<Y, Pair<ID, Att2>>, X, Y> {
	
	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I;

	private final DeltaInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> J;
	private final SigmaInstance<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Y, En2, Fk2, Att2, Pair<En1, X>, Y> K; //TODO aql recomputes

	private final Ctx<Pair<En1, X>, Term<Void, En2, Void, Fk2, Void, Gen, Void>> gens = new Ctx<>();
	private final Ctx<Y, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> sks = new Ctx<>();
	
	public SigmaDeltaCounitTransform(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> i, AqlOptions options) {
		F = f;
		I = i;
		J = new DeltaInstance<>(F, I);
		K = new SigmaInstance<>(F, J, options); //TODO aql are these even necessary?
		
		for (Pair<En1, X> gen : src().gens().keySet()) {
			gens.put(gen, I.algebra().repr(gen.second).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		}
		for (Y sk : src().sks().keySet()) {
			sks.put(sk, I.algebra().reprT(Term.Sk(sk)));
		}
		validate((Boolean) options.getOrDefault(AqlOption.dont_validate_unsafe));

	}

	@Override
	public Ctx<Pair<En1, X>, Term<Void, En2, Void, Fk2, Void, Gen, Void>> gens() {
		return gens;
	}

	@Override
	public Ctx<Y, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> sks() {
		return sks;
	}

	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Pair<En1, X>, Y, ID, Chc<Y, Pair<ID, Att2>>> src() {
		return K;
	}

	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> dst() {
		return I;
	}

}
