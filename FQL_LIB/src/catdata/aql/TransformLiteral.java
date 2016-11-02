package catdata.aql;

import java.util.Map;

import catdata.Util;

public class TransformLiteral<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> extends Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> {
	
	private final Ctx<Gen1, Term<Void, En, Void, Fk, Void, Gen2, Void>> gens;
	private final Ctx<Sk1, Term<Ty,En,Sym,Fk,Att,Gen2,Sk2>> sks;
			
	private final Instance<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src;
	private final Instance<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst;

	
	/*
	public static Transform<Ty,En,Sym,Fk,Att,Gen,Sk,Gen,Sk,X,Y,X,Y> id(Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> i) {
		if (i == null) {
			throw new RuntimeException("Attempt to create identity transform with null instance");
		}
		Map<Gen, Term<Ty,En,Sym,Fk,Att,Gen,Sk>> gens = new HashMap<>();
		Map<Sk,  Term<Ty,En,Sym,Fk,Att,Gen,Sk>> sks  = new HashMap<>();

		for (Gen gen : i.gens().keySet()) {
			gens.put(gen, Term.Gen(gen));
		}
		for (Sk sk : i.sks().keySet()) {
			sks.put(sk, Term.Sk(sk));
		}
		return new Transform<>(gens, sks, i, i);
	}
	*/
	public TransformLiteral(Map<Gen1, Term<Void, En, Void, Fk, Void, Gen2, Void>> gens, Map<Sk1, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> sks, Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src, Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst) {
		Util.assertNotNull(gens, sks, src, dst);
		this.gens = new Ctx<>(gens);
		this.sks = new Ctx<>(sks);
		this.src = src;
		this.dst = dst;
		validate();
	}


	@Override
	public Ctx<Gen1, Term<Void, En, Void, Fk, Void, Gen2, Void>> gens() {
		return gens;
	}


	@Override
	public Ctx<Sk1, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> sks() {
		return sks;
	}


	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src() {
		return src;
	}


	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst() {
		return dst;
	}
}
