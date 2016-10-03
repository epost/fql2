package catdata.aql;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;

public final class Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> {

	public final Map<Gen1, Term<Ty,En,Sym,Fk,Att,Gen2,Sk2>> gens;
	public final Map<Sk1, Term<Ty,En,Sym,Fk,Att,Gen2,Sk2>> sks;
			
	public final Instance<Ty,En,Sym,Fk,Att,Gen1,Sk1> src;
	public final Instance<Ty,En,Sym,Fk,Att,Gen2,Sk2> dst;
	
	//TODO compose
	
	public static <Ty,En,Sym,Fk,Att,Gen,Sk> Transform<Ty,En,Sym,Fk,Att,Gen,Sk,Gen,Sk> id(Instance<Ty,En,Sym,Fk,Att,Gen,Sk> i) {
		if (i == null) {
			throw new RuntimeException("Attempt to create identity transform with null instance");
		}
		Map<Gen, Term<Ty,En,Sym,Fk,Att,Gen,Sk>> gens = new HashMap<>();
		Map<Sk,  Term<Ty,En,Sym,Fk,Att,Gen,Sk>> sks  = new HashMap<>();

		for (Gen gen : i.gens.keySet()) {
			gens.put(gen, Term.Gen(gen));
		}
		for (Sk sk : i.sks.keySet()) {
			sks.put(sk, Term.Sk(sk));
		}
		return new Transform<>(gens, sks, i, i);
	}
	
	public Transform(Map<Gen1, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> gens, Map<Sk1, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> sks, Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1> src, Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2> dst) {
		if (gens == null) {
			throw new RuntimeException("Attempt to create a transform with null generator mappings");
		} else if (sks == null) {
			throw new RuntimeException("Attempt to create a transform with null labelled null mappings");
		} else if (src == null) {
			throw new RuntimeException("Attempt to create a transform with null source instance");
		} else if (dst == null) {
			throw new RuntimeException("Attempt to create a transform with null target instance");
		}
		this.gens = gens;
		this.sks = sks;
		this.src = src;
		this.dst = dst;
		validate();
	}

	public void validate() {
		//for each (k,v) in gens/fks, k must be in src and dst must be in target 
			for (Gen1 gen1 : src.gens.keySet()) {
				En en1 = src.gens.get(gen1);
				Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> gen2 = gens.get(gen1);
				if (gen2 == null) {
					throw new RuntimeException("source generator " + gen1 + " has no transform");
				}
				Chc<Ty, En> en2 = dst.type(gen2);
				if (!en2.equals(Chc.inRight(en1))) {
					throw new RuntimeException("source generator " + gen1 + " transforms to " + gen2 + ", which has sort " + en2.toStringMash() + ", not " + en1 + " as expected");
				}	
			}
			for (Sk1 sk1 : src.sks.keySet()) {
				Ty ty1 = src.sks.get(sk1);
				Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> sk2 = sks.get(sk1);
				if (sk2 == null) {
					throw new RuntimeException("source labelled null " + sk1 + " has no transform");
				}
				Chc<Ty, En> ty2 = dst.type(sk2);
				if (!ty2.equals(Chc.inLeft(ty1))) {
					throw new RuntimeException("source labelled null " + sk1 + " transforms to " + sk2 + ", which has sort " + ty2.toStringMash() + ", not " + ty1 + " as expected");
				}	
			}
			for (Gen1 gen1 : gens.keySet()) {
				if (!src.gens.containsKey(gen1)) {
					throw new RuntimeException("there is a transform for " + gen1 + " which is not a source generator");
				}
			}
			for (Sk1 sk1 : sks.keySet()) {
				if (!src.sks.containsKey(sk1)) {
					throw new RuntimeException("there is a transform for " + sk1 + " which is not a source labelled null");
				}
			}
				
				//TODO: proving
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
		result = prime * result + ((sks == null) ? 0 : sks.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transform<?,?,?,?,?,?,?,?,?> other = (Transform<?,?,?,?,?,?,?,?,?>) obj;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (gens == null) {
			if (other.gens != null)
				return false;
		} else if (!gens.equals(other.gens))
			return false;
		if (sks == null) {
			if (other.sks != null)
				return false;
		} else if (!sks.equals(other.sks))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}



	private String toString = null;
	@Override
	public String toString() {
		if (toString != null) {
			return toString;
		}
	
		toString = "generators";
		toString += "\n\t" + Util.sep(gens, " -> ", "\n\t");

		toString += "\nlabelled nulls";
		toString += "\n\t" + Util.sep(sks, " -> ", "\n\t");
		
		return toString;
	}
	
	//TODO push into Morphism?
	private Morphism<Ty,En,Sym,Fk,Att,Gen1,Sk1,En,Sym,Fk,Att,Gen2,Sk2> semantics;
	public Morphism<Ty,En,Sym,Fk,Att,Gen1,Sk1,En,Sym,Fk,Att,Gen2,Sk2> semantics() {
		if (semantics != null) {
			return semantics;
		}
		for (Pair<Term<Ty, En, Sym, Fk, Att, Gen1, Sk1>, Term<Ty, En, Sym, Fk, Att, Gen1, Sk1>> eq : src.eqs) {
			Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> lhs = trans(eq.first), rhs = trans(eq.second);
			boolean ok = dst.semantics().eq(new Ctx<>(), lhs, rhs);
			if (!ok) {
				throw new RuntimeException("Equation " + eq.first + " = " + eq.second + " translates to " + lhs + " = " + rhs + ", which is not provable");
			}
		}
		semantics = new Morphism<Ty,En,Sym,Fk,Att,Gen1,Sk1,En,Sym,Fk,Att,Gen2,Sk2>() {

			@Override
			public Pair<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> translate(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> term) {
				return new Pair<>(ctx, trans(term));
			}

			@Override
			public Collage<Ty, En, Sym, Fk, Att, Gen1, Sk1> src() {
				return src.collage();
			}

			@Override
			public Collage<Ty, En, Sym, Fk, Att, Gen2, Sk2> dst() {
				return dst.collage();
			}
			
		};
		return semantics;
	}

	public Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> trans(Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> term) {
		if (term.var != null || term.obj != null) {
			return term.convert();
		} else if (term.fk != null) {
			return Term.Fk(term.fk, trans(term.arg));
		} else if (term.att != null) {
			return Term.Att(term.att, trans(term.arg));
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::trans).collect(Collectors.toList()));
		} else if (term.gen != null) {
			return gens.get(term.gen);
		} else if (term.sk != null) {
			return sks.get(term.sk);
		}
		throw new RuntimeException("Anomaly: please report");
	}
}
