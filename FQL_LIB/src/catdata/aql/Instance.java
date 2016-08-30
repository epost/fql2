package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;

public final class Instance<Ty, En, Sym, Fk, Att, Gen, Sk> {

	private final DPStrategy strategy;

	public final Schema<Ty, En, Sym, Fk, Att> schema;

	public final Map<Gen, En> gens;
	public final Map<Sk, Ty> sks;

	public final List<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs;

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Instance<Ty, En, Sym, Fk, Att, Void, Void> terminal(Schema<Ty, En, Sym, Fk, Att> t) {
		return new Instance<>(t, new HashMap<>(), new HashMap<>(), new LinkedList<>(), new DPStrategy(DPName.PRECOMPUTED, t.semantics()));
	}

	public Chc<Ty,En> type(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {		
		return term.type(new Ctx<>(), new Ctx<>(), schema.typeSide.tys, schema.typeSide.syms, schema.typeSide.java_tys, schema.ens, schema.atts, schema.fks, gens, sks);
	}
	
	public Instance(Schema<Ty, En, Sym, Fk, Att> schema, Map<Gen, En> gens, Map<Sk, Ty> sks, List<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs, DPStrategy strategy) {
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
		}	
	
	private String toString(Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq) {
		return eq.first + " = " + eq.second;
	}
	
	private Algebra<Ty, En, Sym, Fk, Att, Gen, Sk> semantics;
	
	@SuppressWarnings("unchecked")
	public Algebra<Ty, En, Sym, Fk, Att, Gen, Sk> semantics() {
		if (semantics != null) {
			return semantics;
		} 
		switch (strategy.name) {
		case ALLJAVA:
			break;
		case COMPLETION:
			break;
		case CONGRUENCE:
			break;
		case FAIL:
			throw new RuntimeException("semantics called for typeside " + this + ", but theorem proving strategy is to fail");
		case FINITE:
			break;
		case PRECOMPUTED:
			semantics = (Algebra<Ty, En, Sym, Fk, Att, Gen, Sk>) strategy.object;
			return semantics;
		case PROGRAM:
			break;
		case UNARY:
			break;
		default:
			throw new RuntimeException();
		}
		
		throw new RuntimeException();
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

	@Override
	public String toString() {
		return "Instance [schema=" + schema + ", gens=" + gens + ", sks=" + sks + ", eqs=" + eqs + "]";
	} 

	
	
}
