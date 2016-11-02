package catdata.aql;

import java.util.Map;
import java.util.function.Function;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.exp.GUID;

public class SigmaDeltaUnitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> extends Transform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>, X, Y, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>> {
	
	public final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F; 
	public final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;
	public final SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> J;
	public final DeltaInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, GUID, Chc<Sk, Pair<GUID, Att2>>> K; //TODO aql recomputes
	private final Ctx<Gen, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>>> gens = new Ctx<>();
	private final Ctx<Sk, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>>> sks = new Ctx<>();
	
	public SigmaDeltaUnitTransform(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, Map<String, String> options) {
		F = f;
		I = i;
		J = new SigmaInstance<>(F, I, options);
		K = new DeltaInstance<>(F, J);
		
		for (Gen gen : src().gens().keySet()) {
			GUID guid = J.algebra().intoX(F.trans(Term.Gen(gen)));
			En1 en1 = src().gens().get(gen);			
			gens.put(gen, Term.Gen(new Pair<>(en1, guid)));
		}
		for (Sk sk : src().sks().keySet()) {
			Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>> term = J.algebra().intoY(F.trans(Term.Sk(sk))).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
			sks.put(sk, term);
		}
	}

	@Override
	public Ctx<Gen, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>>> gens() {
		return gens;
	}

	@Override
	public Ctx<Sk, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>>> sks() {
		return sks;
	}

	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> src() {
		return I;
	}

	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>, Pair<En1, GUID>, Chc<Sk, Pair<GUID, Att2>>> dst() {
		return K;
	}

}
