package catdata.aql.fdm;

import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Instance;
import catdata.aql.It.ID;
import catdata.aql.Mapping;
import catdata.aql.Term;
import catdata.aql.Transform;


public class SigmaTransform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> extends Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, ID, Chc<Sk1, Pair<ID, Att2>>, ID, Chc<Sk2, Pair<ID, Att2>>>  {

	@SuppressWarnings("unused")
	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	@SuppressWarnings("unused")
	private final Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h;
	
	private final SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, X1, Y1> src;
	private final SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2, En2, Fk2, Att2, X2, Y2> dst;

	private final Ctx<Gen1, Term<Void, En2, Void, Fk2, Void, Gen2, Void>> gens = new Ctx<>();
	private final Ctx<Sk1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> sks = new Ctx<>();
	
	//TODO: aql this recomputes the instances
	public SigmaTransform(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h, AqlOptions options1,  AqlOptions options2) {
		if (!h.src().schema().equals(f.src)) {
			throw new RuntimeException("Source of mapping is " + f.src + " but instances are on " + h.src().schema());
		}
        F = f;
		this.h = h;
		src = new SigmaInstance<>(f, h.src(), options1);
		dst = new SigmaInstance<>(f, h.dst(), options2);
		for (Gen1 gen1 : src.gens().keySet()) {
			gens.put(gen1, f.trans(h.gens().get(gen1).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())).convert());
		}
		for (Sk1 sk1 : src.sks().keySet()) {
			sks.put(sk1, f.trans(h.sks().get(sk1)));
		}
		validate(false);//TODO aql how to force validation without onerous constructor shenanigans
	}
	
	@Override
	public Ctx<Gen1, Term<Void, En2, Void, Fk2, Void, Gen2, Void>> gens() {
		return gens;
	}

	@Override
	public Ctx<Sk1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> sks() {
		return sks;
	}

	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, ID, Chc<Sk1, Pair<ID, Att2>>> src() {
		return src;
	}

	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2, ID, Chc<Sk2, Pair<ID, Att2>>> dst() {
		return dst;
	}

	
}
