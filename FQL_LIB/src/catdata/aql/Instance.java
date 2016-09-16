package catdata.aql;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;

public final class Instance<Ty, En, Sym, Fk, Att, Gen, Sk> {

	private final AqlOptions strategy;

	public final Schema<Ty, En, Sym, Fk, Att> schema;

	public final Map<Gen, En> gens;
	public final Map<Sk, Ty> sks;

	public final Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs;

	public static <Ty, En, Sym, Fk, Att> Instance<Ty, En, Sym, Fk, Att, Void, Void> terminal(Schema<Ty, En, Sym, Fk, Att> t) {
		return new Instance<>(t, Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), new AqlOptions(DPName.PRECOMPUTED, t.semantics()));
	}

	public Chc<Ty,En> type(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {		
		return term.type(new Ctx<>(), new Ctx<>(), schema.typeSide.tys, schema.typeSide.syms, schema.typeSide.java_tys, schema.ens, schema.atts, schema.fks, gens, sks);
	}
	
	public Instance(Schema<Ty, En, Sym, Fk, Att> schema, Map<Gen, En> gens, Map<Sk, Ty> sks, Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs, AqlOptions strategy) {
		if (schema == null) {
			throw new RuntimeException("Attempt to construct instance with null schema");
		} else if (gens == null) {
			throw new RuntimeException("Attempt to construct instance with null generators");
		} else if (sks == null) {
			throw new RuntimeException("Attempt to construct instance with null skolem variables");
		} else if (eqs == null) {
			throw new RuntimeException("Attempt to construct instance with null equalities");
		} else if (strategy == null) {
			throw new RuntimeException("Attempt to construct instance with null theorem proving strategy");
		}

		this.schema = schema;
		this.gens = gens;
		this.sks = sks;
		this.eqs = eqs;
		this.strategy = strategy;

		validate();
	}

	public void validate() {
			//check that each gen/sk is in tys/ens
			for (Gen gen : gens.keySet()) {
				En en = gens.get(gen);
				if (!schema.ens.contains(en)) {
					throw new RuntimeException("On generator " + gen + ", the entity " + en + " is not declared.");
				}
			}
			for (Sk sk : sks.keySet()) {
				Ty ty = sks.get(sk);
				if (!schema.typeSide.tys.contains(ty)) {
					throw new RuntimeException("On labelled null " + sk + ", the type " + ty + " is not declared.");
				}
			}
			
			for (Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : eqs) {
				//check lhs and rhs types match in all eqs
				Chc<Ty, En> lhs = type(eq.first);
				Chc<Ty, En> rhs = type(eq.second);
				if (!lhs.equals(rhs)) {
					throw new RuntimeException("In instance equation " + toString(eq) + ", lhs sort is " + lhs.toStringMash() + " but rhs sort is " + rhs.toStringMash());
				}
			}				
			
			//if not 'precomputed', then
			//TODO: entity_theory = restrict to entities ; collage  
			//create type algebra
			//check freeness on java, and freeness if option enabled.  
			//create dp for type algebra by DP in collage
			//
		}	
	
	private String toString(Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq) {
		return eq.first + " = " + eq.second;
	}
	
	//TODO
	private DP<Ty,En,Sym,Fk,Att,Gen,Sk> semantics;
	
	//this could take a while, so make sure two threads don't accidentally do it at the same time
	public synchronized DP<Ty,En,Sym,Fk,Att,Gen,Sk> semantics() {
		if (semantics != null) {
			return semantics;
		} 
		semantics = ProverFactory.create(strategy, collage());
		return semantics;
	}

	private Collage<Ty, En, Sym, Fk, Att, Gen, Sk> collage;
	public Collage<Ty, En, Sym, Fk, Att, Gen, Sk> collage() {
		if (collage != null) {
			return collage;
		}
		collage = new Collage<>(this);
		return collage;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((sks == null) ? 0 : sks.hashCode());
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
		Instance<?,?,?,?,?,?,?> other = (Instance<?,?,?,?,?,?,?>) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (gens == null) {
			if (other.gens != null)
				return false;
		} else if (!gens.equals(other.gens))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		if (sks == null) {
			if (other.sks != null)
				return false;
		} else if (!sks.equals(other.sks))
			return false;
		return true;
	}

	private String toString = null;
	@Override
	public String toString() {
		if (toString != null) {
			return toString;
		}
		List<String> eqs0 = eqs.stream().map(x -> x.first + " = " + x.second).collect(Collectors.toList());
		toString = "generating entities";
		toString += "\n\t" + Util.sep(gens, " : ", "\n\t");
		toString += "\ngenerating labelled nulls";
		toString += "\n\t" + Util.sep(sks, " : " , "\n\t");			
		toString += "\nequations";
		toString += "\n\t" + Util.sep(eqs0, "\n\t");
		
		return toString;
	} 

	
	
}
