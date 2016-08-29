package catdata.aql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;

public class TypeSide<Ty, Sym> {

	public final Set<Ty> tys;
	public final Map<Sym, Pair<Ty[], Ty>> syms;
	public final List<Triple<Ctx<Var, Ty>, Term<Ty, ?, Sym, ?, ?, ?, ?>, Term<Ty, ?, Sym, ?, ?, ?, ?>>> eqs;

	public final Map<Ty, String> java_tys_string;
	public final Map<Ty, String> java_parser_string;
	public final Map<Sym, String> java_fns_string;

	public final Map<Ty, Class<?>> java_tys;
	public final Map<Sym, Function<Object[], Object>> java_fns;

	private final DPStrategy strategy;

	public Ty type(Ctx<Var,Ty> ctx, Term<Ty, ?, Sym, ?, ?, ?, ?> term) {
		if (!term.isTypeSide()) {
			throw new RuntimeException(term + " is not a typeside term");
		} 
		Chc<Ty, ?> t = term.type(ctx, new Ctx<>(), tys, syms, java_tys, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		if (!t.left) {
			throw new RuntimeException(term + " has type " + t.l + " which is not in the typeside.  (This should be impossible, report to Ryan)");
		}
		return t.l;
	}

	public TypeSide(Set<Ty> tys, Map<Sym, Pair<Ty[], Ty>> syms, List<Triple<Ctx<Var, Ty>, Term<Ty, ?, Sym, ?, ?, ?, ?>, Term<Ty, ?, Sym, ?, ?, ?, ?>>> eqs, Map<Ty, String> java_tys_string, Map<Ty, String> java_parser_string, Map<Sym, String> java_fns_string, DPStrategy strategy) {
		if (tys == null) {
			throw new RuntimeException("Null types in typeside");
		} else if (syms == null) {
			throw new RuntimeException("Null symbols in typeside");
		} else if (eqs == null) {
			throw new RuntimeException("Null equations in typeside");
		} else if (java_tys_string == null) {
			throw new RuntimeException("Null java types in typeside");
		} else if (java_parser_string == null) {
			throw new RuntimeException("Null java constants in typeside");
		} else if (java_fns_string == null) {
			throw new RuntimeException("Null java functions in typeside");
		} else if (strategy == null) {
			throw new RuntimeException("Null theorem proving strategy in typeside");
		}
		this.tys = tys;
		this.syms = syms;
		this.eqs = eqs;
		this.java_tys_string = java_tys_string;
		this.java_parser_string = java_parser_string;
		this.java_fns_string = java_fns_string;
		this.strategy = strategy;
		this.java_tys = new HashMap<>(); // TODO 
		this.java_fns = new HashMap<>();
		validate();
	}

	public void validate() {
		//check that each sym is in tys
		for (Sym sym : syms.keySet()) {
			Pair<Ty[], Ty> ty = syms.get(sym);
			if (!tys.contains(ty.second)) {
				throw new RuntimeException("On typeside symbol " + sym + ", the return type " + ty.second + " is not declared.");
			}
			for (Ty t : ty.first) {
				if (!tys.contains(t)) {
					throw new RuntimeException("On typeside symbol " + sym + ", the argument type " + t + " is not declared.");
				}
			}
			
		}
		for (Triple<Ctx<Var, Ty>, Term<Ty, ?, Sym, ?, ?, ?, ?>, Term<Ty, ?, Sym, ?, ?, ?, ?>> eq : eqs) {
			//check that the context is valid for each eq
			Set<Ty> used_tys = new HashSet<>(eq.first.values());
			used_tys.removeAll(tys);
			if (!used_tys.isEmpty()) {
				throw new RuntimeException("In typeside equation " + toString(eq) + ", context uses types " + used_tys + " that are not declared.");
			}
			//check lhs and rhs types match in all eqs
			Ty lhs = type(eq.first, eq.second);
			Ty rhs = type(eq.first, eq.third);
			if (!lhs.equals(rhs)) {
				throw new RuntimeException("In typeside equation " + toString(eq) + ", lhs type is " + lhs + " but rhs type is " + rhs);
			}
		}				
		
		//TODO java validate
	}
	
	public String toString(Triple<Ctx<Var, Ty>, Term<Ty, ?, Sym, ?, ?, ?, ?>, Term<Ty, ?, Sym, ?, ?, ?, ?>> eq) {
		String pre = eq.first.isEmpty() ? "" : "forall ";
		return pre + eq.first + ", " + eq.first + " = " + eq.second;
	}

	public static TypeSide<Void, Void> terminal() {
		return new TypeSide<Void, Void>(new HashSet<>(), new HashMap<>(), new LinkedList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new DPStrategy(DPName.PRECOMPUTED, DP.terminal));
	}

	private DP<Ty, Void, Sym, Void, Void, Void, Void> semantics;

	@SuppressWarnings("unchecked")
	public DP<Ty, Void, Sym, Void, Void, Void, Void> semantics() {
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
			semantics = (DP<Ty, Void, Sym, Void, Void, Void, Void>) strategy.object;
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
		result = prime * result + ((java_fns_string == null) ? 0 : java_fns_string.hashCode());
		result = prime * result + ((java_parser_string == null) ? 0 : java_parser_string.hashCode());
		result = prime * result + ((java_tys_string == null) ? 0 : java_tys_string.hashCode());
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
		TypeSide<?, ?> other = (TypeSide<?, ?>) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (java_fns_string == null) {
			if (other.java_fns_string != null)
				return false;
		} else if (!java_fns_string.equals(other.java_fns_string))
			return false;
		if (java_parser_string == null) {
			if (other.java_parser_string != null)
				return false;
		} else if (!java_parser_string.equals(other.java_parser_string))
			return false;
		if (java_tys_string == null) {
			if (other.java_tys_string != null)
				return false;
		} else if (!java_tys_string.equals(other.java_tys_string))
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
		return "TypeSide [tys=" + tys + ", syms=" + syms + ", eqs=" + eqs + ", java_tys_string=" + java_tys_string + ", java_parser_string=" + java_parser_string + ", java_fns_string=" + java_fns_string + "]";
	} // TODO

}
