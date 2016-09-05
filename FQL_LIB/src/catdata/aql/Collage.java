package catdata.aql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;

//TODO: validate?
public class Collage<Ty, En, Sym, Fk, Att, Gen, Sk> {

	//TODO: keep equations segregated?
	
	//TODO: make this an interface?
	
	public final Set<Ty> tys = new HashSet<>();
	public final Map<Sym, Pair<List<Ty>, Ty>> syms = new HashMap<>();
	public final Map<Ty, String> java_tys = new HashMap<>();
	public final Map<Ty, String> java_parsers = new HashMap<>();
	public final Map<Sym, String> java_fns = new HashMap<>();
	
	public final Set<En> ens = new HashSet<>();
	public final Map<Att, Pair<En, Ty>> atts = new HashMap<>();
	public final Map<Fk,  Pair<En, En>> fks = new HashMap<>();
		
	public final Map<Gen, En> gens = new HashMap<>();
	public final Map<Sk, Ty> sks = new HashMap<>();

	public final Set<Triple<Ctx<Var, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs = new HashSet<>();

	public Collage() {
		
	}
	
	public Collage(TypeSide<Ty,Sym> typeSide) {
		this.tys.addAll(typeSide.tys);
		this.syms.putAll(typeSide.syms);
		this.java_tys.putAll(typeSide.java_tys);
		this.java_parsers.putAll(typeSide.java_parsers);
		this.java_fns.putAll(typeSide.java_fns);
		this.eqs.addAll(typeSide.eqs.stream().map(x -> new Triple<Ctx<Var, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>(x.first.inLeft(), upgradeTypeSide(x.second), upgradeTypeSide(x.third))).collect(Collectors.toSet()));
	}
	
	public Collage(Schema<Ty,En,Sym,Fk,Att> schema) {
		this(schema.typeSide);
		this.ens.addAll(schema.ens);
		this.atts.putAll(schema.atts);
		this.fks.putAll(schema.fks);
		this.eqs.addAll(schema.eqs.stream().map(x -> new Triple<Ctx<Var, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>(new Ctx<>(new Pair<>(x.first.first, Chc.inRight(x.first.second))), upgradeSchema(x.second), upgradeSchema(x.third))).collect(Collectors.toSet()));
	}
	
	public Collage(Instance<Ty,En,Sym,Fk,Att,Gen,Sk> instance) {
		this(instance.schema);
		this.gens.putAll(instance.gens);
		this.sks.putAll(instance.sks);
		this.eqs.addAll(instance.eqs.stream().map(x -> new Triple<Ctx<Var, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>(new Ctx<>(), x.first, x.second)).collect(Collectors.toSet()));
	}

	@SuppressWarnings("unchecked")
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgradeTypeSide(Term<Ty, Void, Sym, Void, Void, Void, Void> term) {
		return (Term<Ty, En, Sym, Fk, Att, Gen, Sk>) term;
	}
	
	@SuppressWarnings("unchecked")
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgradeSchema(Term<Ty, En, Sym, Fk, Att, Void, Void> term) {
		return (Term<Ty, En, Sym, Fk, Att, Gen, Sk>) term;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
		result = prime * result + ((java_fns == null) ? 0 : java_fns.hashCode());
		result = prime * result + ((java_parsers == null) ? 0 : java_parsers.hashCode());
		result = prime * result + ((java_tys == null) ? 0 : java_tys.hashCode());
		result = prime * result + ((sks == null) ? 0 : sks.hashCode());
		result = prime * result + ((syms == null) ? 0 : syms.hashCode());
		result = prime * result + ((tys == null) ? 0 : tys.hashCode());
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
		Collage<?,?,?,?,?,?,?> other = (Collage<?,?,?,?,?,?,?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (gens == null) {
			if (other.gens != null)
				return false;
		} else if (!gens.equals(other.gens))
			return false;
		if (java_fns == null) {
			if (other.java_fns != null)
				return false;
		} else if (!java_fns.equals(other.java_fns))
			return false;
		if (java_parsers == null) {
			if (other.java_parsers != null)
				return false;
		} else if (!java_parsers.equals(other.java_parsers))
			return false;
		if (java_tys == null) {
			if (other.java_tys != null)
				return false;
		} else if (!java_tys.equals(other.java_tys))
			return false;
		if (sks == null) {
			if (other.sks != null)
				return false;
		} else if (!sks.equals(other.sks))
			return false;
		if (syms == null) {
			if (other.syms != null)
				return false;
		} else if (!syms.equals(other.syms))
			return false;
		if (tys == null) {
			if (other.tys != null)
				return false;
		} else if (!tys.equals(other.tys))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Collage [tys=" + tys + ", syms=" + syms + ", java_tys=" + java_tys + ", java_parsers=" + java_parsers + ", java_fns=" + java_fns + ", ens=" + ens + ", atts=" + atts + ", fks=" + fks + ", gens=" + gens + ", sks=" + sks + ", eqs=" + eqs + "]";
	}

	

}
