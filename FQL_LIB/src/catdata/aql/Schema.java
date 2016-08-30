package catdata.aql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;

public final class Schema<Ty,En,Sym,Fk,Att> {
	
	public final TypeSide<Ty,Sym> typeSide;
	
	public final Set<En> ens;
	public final Map<Att, Pair<En, Ty>> atts;
	public final Map<Fk,  Pair<En, En>> fks;
		
	public final List<Triple<Pair<Var, En>, Term<Ty,En,Sym,Fk,Att,?,?>, Term<Ty,En,Sym,Fk,Att,?,?>>> eqs;
	
	private final DPStrategy strategy; 
	
	public void validate() {
		//check that each att/fk is in tys/ens
		for (Att att : atts.keySet()) {
			Pair<En, Ty> ty = atts.get(att);
			if (!typeSide.tys.contains(ty.second)) {
				throw new RuntimeException("On attribute " + att + ", the target type " + ty.second + " is not declared.");
			} else if (!ens.contains(ty.first)) {
				throw new RuntimeException("On attribute " + att + ", the source entity " + ty.first + " is not declared.");
			}
		}
		for (Fk fk : fks.keySet()) {
			Pair<En, En> ty = fks.get(fk);
			if (!ens.contains(ty.second)) {
				throw new RuntimeException("On foreign key " + fk + ", the target entity " + ty.second + " is not declared.");
			} else if (!ens.contains(ty.first)) {
				throw new RuntimeException("On foreign key " + fk + ", the source entity " + ty.first + " is not declared.");
			}
		}
		for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, ?, ?>, Term<Ty, En, Sym, Fk, Att, ?, ?>> eq : eqs) {
			//check that the context is valid for each eq
			if (!ens.contains(eq.first.second)) {
				throw new RuntimeException("In schema equation " + toString(eq) + ", context sort " + eq.first.second + " is not a declared entity.");
			}
			//check lhs and rhs types match in all eqs
			Chc<Ty, En> lhs = type(eq.first, eq.second);
			Chc<Ty, En> rhs = type(eq.first, eq.third);
			if (!lhs.equals(rhs)) {
				throw new RuntimeException("In schema equation " + toString(eq) + ", lhs sort is " + lhs.toStringMash() + " but rhs sort is " + rhs.toStringMash());
			}
		}				
	}
	
	private String toString(Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, ?, ?>, Term<Ty, En, Sym, Fk, Att, ?, ?>> eq) {
		return "forall " + eq.first.first + ":" + eq.first.second + ", " + eq.first + " = " + eq.second;
	}
	
	
	public Chc<Ty,En> type(Pair<Var, En> p, Term<Ty, En, Sym, Fk, Att, ?, ?> term) {
		if (!term.isSchema()) {
			throw new RuntimeException(term + " is not a schema term");
		} 
		return term.type(new Ctx<>(), new Ctx<>(p), typeSide.tys, typeSide.syms, typeSide.java_tys, ens, atts, fks, new HashMap<>(), new HashMap<>());
	}
	
	public static <Ty,En,Sym,Fk,Att> Schema<Ty,Void,Sym,Void,Void> terminal(TypeSide<Ty,Sym> t) {
		return new Schema<>(t, new HashSet<>(), new HashMap<>(), new HashMap<>(), new LinkedList<>(), new DPStrategy(DPName.PRECOMPUTED, t.semantics()));
	}
	
	public Schema(TypeSide<Ty,Sym> typeSide, Set<En> ens,
			Map<Att, Pair<En, Ty>> atts, Map<Fk, Pair<En, En>> fks,
			List<Triple<Pair<Var, En>, Term<Ty,En, Sym, Fk, Att, ?, ?>, Term<Ty, En, Sym, Fk, Att, ?, ?>>> eqs,
			DPStrategy strategy) {
		if (typeSide == null) {
			throw new RuntimeException("Attempt to construct schema with null type side");
		} else if (ens == null) {
			throw new RuntimeException("Attemp to construct schema with null entities");
		} else if (fks == null) {
			throw new RuntimeException("Attempt to construct schema with null foreign keys");
		} else if (atts == null) {
			throw new RuntimeException("Attempt to construct schema with null attributes");			
		} else if (eqs == null) {
			throw new RuntimeException("Attempt to construct schema with null equalities");
		} else if (strategy == null) {
			throw new RuntimeException("Attempt to construct schema with null theorem proving strategy");
		}

		this.typeSide = typeSide;
		this.atts = atts;
		this.fks = fks;
		this.eqs = eqs;
		this.ens = ens;
		this.strategy = strategy;
		validate();
	}

	private DP<Ty,En,Sym,Fk,Att,Void,Void> semantics;
	
	@SuppressWarnings("unchecked")
	public DP<Ty,En,Sym,Fk,Att,Void,Void> semantics() {
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
			semantics = (DP<Ty,En,Sym,Fk,Att,Void,Void>) strategy.object;
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
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
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
		Schema<?,?,?,?,?> other = (Schema<?,?,?,?,?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
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
		if (typeSide == null) {
			if (other.typeSide != null)
				return false;
		} else if (!typeSide.equals(other.typeSide))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Schema [ens=" + ens + ", atts=" + atts + ", fks=" + fks + ", eqs=" + eqs + "]";
	} //TODO
	
	
	
}
