package catdata.fpqlpp;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.opl.OplParser.DoNotIgnore;

public class ACtx<T,E,V> {
	LinkedHashMap<V, Chc<T,E>> vars0;

	public ACtx(Map<V, Chc<T,E>> m) {
		vars0 = new LinkedHashMap<>(m);
	} 

	@Override
	public String toString() {
		List<String> l = new LinkedList<>();
		for (V k : vars0.keySet()) {
			if (vars0.get(k) != null) {
				l.add(k + ":" + vars0.get(k));
			} else {
				l.add(k.toString());
			}
		}
		String ret = Util.sep(l, ", ");
		return ret;
	}

	public int indexOf(V var) {
		int i = -1;
		for (V k : vars0.keySet()) {
			i++;
			if (var.equals(k)) {
				break;
			}
		}
		return i;
	}

	public ACtx(List<Pair<V, Chc<T,E>>> vars) {
		vars0 = new LinkedHashMap<>();
		for (Pair<V, Chc<T, E>> k : vars) {
			if (vars0.containsKey(k.first)) {
				throw new DoNotIgnore("Duplicate variable " + k.first);
			}
			vars0.put(k.first, k.second);
		}
	}

	public ACtx() {
		vars0 = new LinkedHashMap<>();
	}

	public Chc<T,E> get(V s) {
		Chc<T,E> ret = vars0.get(s);
		if (s == null) {
			throw new DoNotIgnore("Unbound var " + s);
		}
		return ret;
	}

/*	public void validate(OplSig<S, ?, V> oplSig) {
		for (V k : vars0.keySet()) {
			S v = get(k);
			if (v == null) {
				throw new RuntimeException("Cannot infer type for " + k);
			}
			if (!oplSig.sorts.contains(v)) {
				throw new DoNotIgnore("Context has bad sort " + v);
			}
		}
	} */

	public List<Chc<T,E>> values() {
		List<Chc<T,E>> ret = new LinkedList<>();
		for (V k : vars0.keySet()) {
			Chc<T,E> v = vars0.get(k);
			ret.add(v);
		}
		return ret;
	}

	public List<Pair<V, Chc<T,E>>> values2() {
		List<Pair<V, Chc<T,E>>> ret = new LinkedList<>();
		for (V k : vars0.keySet()) {
			Chc<T,E> v = vars0.get(k);
			ret.add(new Pair<>(k, v));
		}
		return ret;
	}

	public List<V> names() {
		return new LinkedList<>(vars0.keySet());
	}

	public <T2,E2> ACtx<T2,E2,V> makeEnv(List<Chc<T2,E2>> env) {
		List<Pair<V, Chc<T2,E2>>> in = new LinkedList<>();
		int i = 0;
		for (V k : vars0.keySet()) {
			in.add(new Pair<>(k, env.get(i)));
			i++;
		}
		return new ACtx<>(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vars0 == null) ? 0 : vars0.hashCode());
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
		ACtx<?, ?, ?> other = (ACtx<?, ?, ?>) obj;
		if (vars0 == null) {
			if (other.vars0 != null)
				return false;
		} else if (!vars0.equals(other.vars0))
			return false;
		return true;
	}
	

}