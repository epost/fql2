package catdata.aql;

import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.aql.exp.GUID;

public class SigmaTransform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> extends Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, GUID, Chc<Sk1, Pair<GUID, Att2>>, GUID, Chc<Sk2, Pair<GUID, Att2>>>  {

	public final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	public final Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h;
	
	public final SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, X1, Y1> src; 
	public final SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2, En2, Fk2, Att2, X2, Y2> dst; 

	private final Ctx<Gen1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> gens = new Ctx<>();
	private final Ctx<Sk1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> sks = new Ctx<>();
	
	//TODO: this recomputes the instances
	public SigmaTransform(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h,  Map<String, String> options1,  Map<String, String> options2) {
		if (!h.src().schema().equals(f.src)) {
			throw new RuntimeException("Source of mapping is " + f.src + " but instances are on " + h.src().schema());
		}
		this.F = f;
		this.h = h;
		src = new SigmaInstance<>(f, h.src(), options1);
		dst = new SigmaInstance<>(f, h.dst(), options2);
		for (Gen1 gen1 : src.gens().keySet()) {
			gens.put(gen1, f.trans(h.gens().get(gen1)));
		}
		for (Sk1 sk1 : src.sks().keySet()) {
			sks.put(sk1, f.trans(h.sks().get(sk1)));
		}
		validate();//TODO aql how to force validation without onerous constructor shenanigans
	}
	
	@Override
	public Ctx<Gen1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> gens() {
		return gens;
	}

	@Override
	public Ctx<Sk1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> sks() {
		return sks;
	}

	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, GUID, Chc<Sk1, Pair<GUID, Att2>>> src() {
		return src;
	}

	@Override
	public Instance<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2, GUID, Chc<Sk2, Pair<GUID, Att2>>> dst() {
		return dst;
	}

	/*public static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, GUID, Chc<Sk1, Pair<GUID, Att2>>, GUID, Chc<Sk2, Pair<GUID, Att2>>> 
	sigma(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h,  List<Pair<String, String>> options1,  List<Pair<String, String>> options2) {
	/*
		Instance<Ty,En2,Sym,Fk2,Att2,Gen1,Sk1,GUID, Chc<Sk1, Pair<GUID, Att2>>> src = sigma(f, h.src, options1);
		Instance<Ty,En2,Sym,Fk2,Att2,Gen2,Sk2,GUID, Chc<Sk2, Pair<GUID, Att2>>> dst = sigma(f, h.dst, options2);
		
		Map<Gen1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> gens = new HashMap<>();
		for (Gen1 gen1 : src.gens().keySet()) {
			gens.put(gen1, f.trans(h.gens.get(gen1)));
		}
		Map<Sk1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> sks = new HashMap<>();
		for (Sk1 sk1 : src.sks().keySet()) {
			sks.put(sk1, f.trans(h.sks.get(sk1)));
		}
		
		return new Transform<>(gens, sks, src, dst);
		
	}*/
}
