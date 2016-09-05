package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;

//apparently iteration of a set is not deterministic, use linkedhashset if need deterministic order
public final class Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> {
	
	public final Map<En1, En2> ens;
	public final Map<Att1, Triple<Var,En2,Term<Ty,En2,Sym2,Fk2,Att2,Void,Void>>> atts;
	public final Map<Fk1,  Pair<En2, List<Fk2>>> fks;
		
	public final Schema<Ty,En1,Sym1,Fk1,Att1> src;
	public final Schema<Ty,En2,Sym2,Fk2,Att2> dst;

	//TODO compose
	
	public static <Ty,En,Sym,Fk,Att> Mapping<Ty,En,Sym,Fk,Att,En,Sym,Fk,Att> id(Schema<Ty,En,Sym,Fk,Att> s) {
		if (s == null) {
			throw new RuntimeException("Attempt to create identity mapping with null schema");
		}
		Map<En, En> ens = Util.id(s.ens);
		Map<Fk,  Pair<En, List<Fk>>> fks = new HashMap<>();
		for (Fk fk : s.fks.keySet()) {
			fks.put(fk, new Pair<>(s.fks.get(fk).first, Util.singList(fk)));
		}
		Map<Att, Triple<Var, En, Term<Ty,En,Sym,Fk,Att,Void,Void>>> atts = new HashMap<>();
		for (Att att : s.atts.keySet()) {
			atts.put(att, new Triple<>(new Var("v"), s.atts.get(att).first, Term.Att(att, Term.Var(new Var("v")))));
		}
		return new Mapping<>(ens, atts, fks, s, s);
	}
	
	public Mapping(Map<En1, En2> ens, Map<Att1, Triple<Var,En2,Term<Ty, En2, Sym2, Fk2, Att2, Void, Void>>> atts, Map<Fk1, Pair<En2, List<Fk2>>> fks, Schema<Ty, En1, Sym1, Fk1, Att1> src, Schema<Ty, En2, Sym2, Fk2, Att2> dst) {
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
				throw new RuntimeException("source entity " + en1 + " has no mapping");
			}
			if (!dst.ens.contains(en2)) {
				throw new RuntimeException("source entity " + en1 + " maps to " + en2 + ", which is not in target");
			}	
		}
		for (Att1 att1 : src.atts.keySet()) {
			Triple<Var, En2, Term<Ty, En2, Sym2, Fk2, Att2, Void, Void>> att2 = atts.get(att1);
			if (att2 == null) {
				throw new RuntimeException("source attribute " + att1 + " has no mapping");
			}
			Var v = att2.first;
			En2 proposed_en = att2.second;
			if (proposed_en == null) {
				throw new RuntimeException("in mapping for attribute " + att1 + ", not given a sort for " + v);
			}
			Term<Ty, En2, Sym2, Fk2, Att2, Void, Void> term = att2.third;
			
			En1 en1 = src.atts.get(att1).first;
			Ty ty1 = src.atts.get(att1).second;
			En2 en2 = ens.get(en1);
			if (!proposed_en.equals(en2)) {
				throw new RuntimeException("in mapping for attribute + " + att1 + " the given sort for " + v + " is " + proposed_en + " but it is expected to be " + en2);
			}
			Chc<Ty, En2> ty2 = dst.type(new Pair<>(v, en2), term);
			if (!ty2.equals(Chc.inLeft(ty1))) {
				throw new RuntimeException("source attribute " + att1 + " goes to target observation " + att2 + ", which has type " + ty2.toStringMash() + ", not " + ty1 + " as expected");
			}
		}
		for (Fk1 fk1 : src.fks.keySet()) {
			Pair<En2, List<Fk2>> p = fks.get(fk1);
			if (p == null) {
				throw new RuntimeException("source foreign key " + fk1 + " has no mapping");
			}
			En1 en1_s = src.fks.get(fk1).first;
			En1 en1_t = src.fks.get(fk1).second;
			En2 en2_s = ens.get(en1_s);
			En2 en2_t = ens.get(en1_t);
			if (!p.first.equals(en2_s)) {
				throw new RuntimeException("proposed source of foreign key mapping for " + fk1 + " is " + p.first + " and not " + en2_s + " as expected");
			}
			Var v = new Var("v");
			Term<Ty, En2, Sym2, Fk2, Att2, ?, ?> fk2 = Term.Fks(p.second, Term.Var(v));
			Chc<Ty, En2> en2_t_actual = dst.type(new Pair<>(v, en2_s), fk2);
			if (!en2_t_actual.equals(Chc.inRight(en2_t))) {
				throw new RuntimeException("source foreign key " + fk1 + " maps to target path " + Util.sep(p.second, ".") + ", which has target entity " + en2_t_actual.toStringMash() + ", not " + en2_t + " as expected");
			}
		}
		for (En1 en1 : ens.keySet()) {
			if (!src.ens.contains(en1)) {
				throw new RuntimeException("there is a mapping for " + en1 + " which is not a source entity");
			}
		}
		for (Att1 att1 : atts.keySet()) {
			if (!src.atts.containsKey(att1)) {
				throw new RuntimeException("there is a mapping for " + att1 + " which is not a source attribute");
			}
		}
		for (Fk1 fk1 : fks.keySet()) {
			if (!src.fks.containsKey(fk1)) {
				throw new RuntimeException("there is a mapping for " + fk1 + " which is not a source foreign key");
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

	private String toString = null;
	@Override
	public String toString() {
		if (toString != null) {
			return toString;
		}
		List<String> fks0 = new LinkedList<>();
		for (Fk1 fk : fks.keySet()) {
			fks0.add(fk + " -> " + fks.get(fk).first + (fks.get(fk).second.isEmpty() ? "" : "." + Util.sep(fks.get(fk).second, "."))); 
		}
		List<String> atts0 = new LinkedList<>();
		for (Att1 att : atts.keySet()) {
			atts0.add(att + " -> lambda " + atts.get(att).first + ":" + atts.get(att).second  + ". " + atts.get(att).third); 
		}
		toString = "entities";
		toString += "\n\t" + Util.sep(ens, " -> ", "\n\t");
		
		toString += "\nforeign_keys";
		toString += "\n\t" + Util.sep(fks0, "\n\t");
		
		toString += "\nattributes";
		toString += "\n\t" + Util.sep(atts0, "\n\t");
		
		return toString;
	} 
	//TODO alphabetical?
	
	
}
