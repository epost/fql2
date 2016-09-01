package catdata.aql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Unit;
import catdata.Util;

public final class Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> {
	
	public final Map<En1, En2> ens;
	public final Map<Att1, Term<Ty,En2,Sym2,Fk2,Att2,?,?>> atts;
	public final Map<Fk1,  Fk2[]> fks;
		
	public final Schema<Ty,En1,Sym1,Fk1,Att1> src;
	public final Schema<Ty,En2,Sym2,Fk2,Att2> dst;

	//TODO compose
	
	public static <Ty,En,Sym,Fk,Att> Mapping<Ty,En,Sym,Fk,Att,En,Sym,Fk,Att> id(Schema<Ty,En,Sym,Fk,Att> s) {
		if (s == null) {
			throw new RuntimeException("Attempt to create identity mapping with null schema");
		}
		Map<En, En> ens = Util.id(s.ens);
		Map<Fk,  Fk[]> fks = new HashMap<>();
		for (Fk fk : s.fks.keySet()) {
			fks.put(fk, Util.sing(fk));
		}
		Map<Att, Term<Ty,En,Sym,Fk,Att,?,?>> atts = new HashMap<>();
		for (Att att : s.atts.keySet()) {
			atts.put(att, Term.Att(att, Term.Var(new Var("v"))));
		}
		return new Mapping<>(ens, atts, fks, s, s);
	}
	
	public Mapping(Map<En1, En2> ens, Map<Att1, Term<Ty, En2, Sym2, Fk2, Att2, ?, ?>> atts, Map<Fk1, Fk2[]> fks, Schema<Ty, En1, Sym1, Fk1, Att1> src, Schema<Ty, En2, Sym2, Fk2, Att2> dst) {
		if (ens == null) {
			throw new RuntimeException("Attempt to create mapping with null entity map");
		} else if (atts == null) {
			throw new RuntimeException("Attempt to create mapping with null attribute map");
		} else if (fks == null) {
			throw new RuntimeException("Attempt to create mapping with null foreign key map");
		} else if (src == null) {
			throw new RuntimeException("Attempt to create mapping with null source");
		} else if (dst == null) {
			throw new RuntimeException("Attempt to create mapping with null target");
		}
		this.ens = ens;
		this.atts = atts;
		this.fks = fks;
		this.src = src;
		this.dst = dst;
		validate();
	}

	public void validate() {
		//for each (k,v) in ens/atts/fks, k must be in src and dst must be in target 
		for (En1 en1 : src.ens) {
			En2 en2 = ens.get(en1);
			if (en2 == null) {
				throw new RuntimeException("In mapping " + this + ", source entity " + en1 + " has no mapping");
			}
			if (!dst.ens.contains(en2)) {
				throw new RuntimeException("In mapping " + this + ", source entity " + en1 + " maps to " + en2 + ", which is not in target");
			}	
		}
		for (Att1 att1 : src.atts.keySet()) {
			Term<Ty, En2, Sym2, Fk2, Att2, ?, ?> att2 = atts.get(att1);
			if (att2 == null) {
				throw new RuntimeException("In mapping " + this + ", source attribute " + att1 + " has no mapping");
			}
			Var v = att2.getOnlyVar();
			if (v == null) {
				throw new RuntimeException("In mapping " + this + ", source attribute " + att1 + " goes to target observation " + att2 + ", which has more than one variable");
			}
			En1 en1 = src.atts.get(att1).first;
			Ty ty1 = src.atts.get(att1).second;
			En2 en2 = ens.get(en1);
			Chc<Ty, En2> ty2 = dst.type(new Pair<>(v, en2), att2);
			if (!ty2.equals(Chc.inLeft(ty1))) {
				throw new RuntimeException("In mapping " + this + ", source attribute " + att1 + " goes to target observation " + att2 + ", which has type " + ty2.toStringMash() + ", not " + ty1 + " as expected");
			}
		}
		for (Fk1 fk1 : src.fks.keySet()) {
			Fk2[] l = fks.get(fk1);
			if (l == null) {
				throw new RuntimeException("In mapping " + this + ", source foreign key " + fk1 + " has no mapping");
			}
			En1 en1_s = src.fks.get(fk1).first;
			En1 en1_t = src.fks.get(fk1).second;
			En2 en2_s = ens.get(en1_s);
			En2 en2_t = ens.get(en1_t);			
			Var v = new Var("v");
			Term<Ty, En2, Sym2, Fk2, Att2, ?, ?> fk2 = Term.Fks(l, Term.Var(v));
			Chc<Ty, En2> en2_t_actual = dst.type(new Pair<>(v, en2_s), fk2);
			if (!en2_t_actual.equals(Chc.inRight(en2_t))) {
				throw new RuntimeException("In mapping " + this + ", source foreign key " + fk1 + " goes to target path " + Util.sep(Arrays.asList(l), ".") + ", which has target entity " + en2_t_actual.toStringMash() + ", not " + en2_t + " as expected");
			}
		}
		for (En1 en1 : ens.keySet()) {
			if (!src.ens.contains(en1)) {
				throw new RuntimeException("In mapping " + this + ", there is a mapping for " + en1 + " which is not a source entity");
			}
		}
		for (Att1 att1 : atts.keySet()) {
			if (!src.atts.containsKey(att1)) {
				throw new RuntimeException("In mapping " + this + ", there is a mapping for " + att1 + " which is not a source attribute");
			}
		}
		for (Fk1 fk1 : fks.keySet()) {
			if (!src.fks.containsKey(fk1)) {
				throw new RuntimeException("In mapping " + this + ", there is a mapping for " + fk1 + " which is not a source foreign key");
			}
		}
		
		//TODO proving
	}
	
	public Unit semantics() {
		return new Unit();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
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
		Mapping<?,?,?,?,?,?,?,?,?> other = (Mapping<?,?,?,?,?,?,?,?,?> ) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
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
		return "Mapping [ens=" + ens + ", atts=" + atts + ", fks=" + fks + ", src=" + src + ", dst=" + dst + "]";
	}
	
	
}
