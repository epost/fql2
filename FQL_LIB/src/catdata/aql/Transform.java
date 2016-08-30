package catdata.aql;

import java.util.HashMap;
import java.util.Map;

import catdata.Unit;

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



	@Override
	public String toString() {
		return "Transform [gens=" + gens + ", sks=" + sks + ", src=" + src + ", dst=" + dst + "]";
	}

	public Unit semantics() {
		return new Unit();
	}

}
