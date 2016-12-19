package catdata.provers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Pair;
import catdata.Triple;

public class KBTheory<T, C, V> {
	
	public void validate() {
		for (C sym : syms.keySet()) {
			Pair<List<T>, T> T = syms.get(sym);
			if (!tys.contains(T.second)) {
				throw new RuntimeException("On Typeside symbol " + sym + ", the return Type " + T.second + " is not declared.");
			}
			for (T t : T.first) {
				if (!tys.contains(t)) {
					throw new RuntimeException("On Typeside symbol " + sym + ", the argument Type " + t + " is not declared.");
				}
			}
			
		}
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : eqs) {
			//check that the context is valid for each eq
			Set<T> used_Ts = new HashSet<>(eq.first.values());
			used_Ts.removeAll(tys);
			if (!used_Ts.isEmpty()) {
				throw new RuntimeException("In equation " + eq + ", context uses types " + used_Ts + " that are not declared.");
			}
			//check lhs and rhs Types match in all eqs
			T lhs = eq.second.type(syms, eq.first);
			T rhs = eq.third.type(syms, eq.first);
			if (!lhs.equals(rhs)) {
				throw new RuntimeException("In equation " + eq + ", lhs type is " + lhs + " but rhs type is " + rhs);
			}	
		}
		
	}

	public final Collection<T> tys;
	public final Map<C, Pair<List<T>, T>> syms;
	public final Collection<Triple<Map<V, T>, KBExp<C,V>, KBExp<C,V>>> eqs;
	
	@Override
	public String toString() {
		return "KBTheory [tys=" + tys + ", syms=" + syms + ", eqs=" + eqs + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
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
		KBTheory<?,?,?> other = (KBTheory<?,?,?>) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
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
	
	public KBTheory(Collection<T> tys, Map<C, Pair<List<T>, T>> syms, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> eqs) {
		this.tys = tys;
		this.syms = syms;
		this.eqs = eqs;
		validate(); //TODO aql disable for production
	}
	
	
		
	
}
