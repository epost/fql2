package catdata.aql;

import java.util.List;

import catdata.Chc;
import catdata.Pair;
import catdata.aql.exp.It.ID;

//TODO aql check java computation etc through delta
public class AqlFdm { //TODO aql kill this file

	
/*	
	//expose delta as returning aql delta
	//have convenience method for 
	public static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
	Instance<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Y, Pair<En1, X>, Y>
	delta(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I) {
		return null; //delta0(F, I).toInstance();
	}
	
	public static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
	AqlDelta<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>
	delta0(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I) {
		return null; //new AqlDelta<>(F, I);
	}
	*/
	public static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	Transform<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2>
	delta(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h) {
		
		//AqlDelta<Ty,En1,Sym,Fk1,Att1,Gen1,Sk1,En2,Fk2,Att2,X1,Y1> src = delta0(f, h.src);
		//AqlDelta<Ty,En1,Sym,Fk1,Att1,Gen2,Sk2,En2,Fk2,Att2,X2,Y2> dst = delta0(f, h.dst);
		
		//Map<Pair<En1, X1>, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2>> gens = new HashMap<>();
		//Map<Y1, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2>> sks = new HashMap<>();

		/* Function<Term<Void, En1, Void, Fk1, Void, Pair<En1, X1>, Void>, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2>>
		 conv1 = new Function<Term<Void, En1, Void, Fk1, Void, Pair<En1, X1>, Void>, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2>>() {
			@Override
			public Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2> apply(Term<Void, En1, Void, Fk1, Void, Pair<En1, X1>, Void> t) {
				if (term.gen != null) {
					return Term.Gen(src.repr(new Pair<>(term.gen)))
				}
				
			}			
		}; */
		
		//for (Pair<En1, X1> gen1 : src.toInstance().gens().keySet()) {
		//	Term<Void, En1, Void, Fk1, Void, Pair<En1, X1>, Void> t = src.repr(gen1);
			
	//		delta0(f, h.src).re
	//		src.repr(gen1);
//			Term<Void, En2, Void, Fk2, Void, Gen2, Void> t = h.gens.get(gen1).convert(); req Gen1
			
	//		gens.put(gen1, t);//TODO 
	//	}
		//for (Term<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1> sk1 : src.sks.keySet()) {
			//sks.put(sk1, f.trans(h.sks.get(sk1)));
		//}
		
		return null; //new Transform<>(gens, sks, src.toInstance(), dst.toInstance());
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	/*
	public static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2> 
	Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, GUID, Chc<Sk, Pair<GUID, Att2>>> 
	sigma(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, ?, ?> I, List<Pair<String, String>> options) {
		Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col = F.dst.collage();
		
		col.sks.putAll(I.sks().map);
		for (Gen gen : I.gens().keySet()) {
			col.gens.put(gen, F.ens.get(I.gens().get(gen)));
		}
		
		Set<Pair<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> eqs = new HashSet<>();
		for (Pair<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>, Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> eq : I.eqs()) {
			eqs.add(new Pair<>(F.trans(eq.first), F.trans(eq.second)));
		}
		AqlOptions strategy = new AqlOptions(Util.toMapSafely(options), col); 
		
		return null; //Instance.saturate(F.dst, col.gens.map, col.sks.map, eqs, strategy, new GUID.It());
		
	}*/ 

	//TODO: aql this recomputes src and dst
	public static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, ID, Chc<Sk1, Pair<ID, Att2>>, ID, Chc<Sk2, Pair<ID, Att2>>> 
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
		*/
		return null;
	}

}
