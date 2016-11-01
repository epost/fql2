package catdata.aql;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;

public final class Ctx<K,V> {
	
	

	public final Map<K,V> map;
	
	public <X> Ctx<K,Chc<V,X>> inLeft() {
		LinkedHashMap<K,Chc<V,X>> ret = new LinkedHashMap<>();
		for (K k : map.keySet()) {
			ret.put(k, Chc.inLeft(map.get(k)));
		}
		return new Ctx<>(ret);
	}
	
	public boolean containsKey(K k) {
		return map.containsKey(k);
	}
	
	public static <K,V> Pair<K,V> getOnly(Map<K,V> map) {
		if (map.size() != 1) {
			throw new RuntimeException("Anomaly: please report");
		}
		Entry<K,V> e = map.entrySet().iterator().next();
		return new Pair<>(e.getKey(), e.getValue());
	}
	
	public Set<K> keySet() { 
		return map.keySet();
	}
	public Collection<V> values() {
		return map.values();
	}
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public Ctx() {
		this(new LinkedHashMap<>());
	}
	
	public Ctx(Pair<K,V> p) {
		this();
		map.put(p.first, p.second);
	}
	
	public Ctx(K k, V v) {
		this();
		map.put(k, v);
	}
	
	public void putAll(Map<K,V> m) {
		Util.putAllSafely(map, m);
	}
	
	public void remove(K k) {
		if (!map.containsKey(k)) {
			throw new RuntimeException("Anomaly: please report");
		}
		map.remove(k);
	}
	
	public Ctx(Map<K,V> map) {
		if (map == null) {
			throw new RuntimeException("Attempt to create a Ctx with null map");
		}
		this.map = map;
	}
	/*
	public Ctx(LinkedHashMap<K,V> map) {
		if (map == null) {
			throw new RuntimeException("Attempt to create a Ctx with null map");
		}
		this.map = map;
	} */
	
	public Ctx(List<Pair<K,V>> list) {
		this(Util.listToMap(list));		
	}
	
	@Override
	public String toString() {
		return Util.sep(map.entrySet().stream().map(z -> z.getKey() + ":" + z.getValue()).collect(Collectors.toList()), ",");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		Ctx<?,?> other = (Ctx<?,?>) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

	public V get(K k) {
		if (k == null) {
			throw new RuntimeException("Attempt to lookup null key in " + map);
		}
		V ret = map.get(k);
		if (ret == null) {
			throw new RuntimeException(k + " cannot be found in context " + map);
		}
		return ret;
	}

	public static <K,V> String toString(List<Pair<K, V>> ctx) {
		return Util.sep(ctx.stream().map(z -> z.first + (z.second != null ? (":" + z.second) : "")).collect(Collectors.toList()), ",");
	}

	public void put(K k, V v) {
		if (map.containsKey(k)) {
			throw new RuntimeException(this + " alredy contains " + k);
		}
		map.put(k, v);
	}

	public String toString(Term<?, ?, ?, ?, ?, ?, ?> l, Term<?, ?, ?, ?, ?, ?, ?> r) {
		String pre = map.isEmpty() ? "" : "forall " + Util.sep(map, ": ", ", ") + " . ";
 		
		return pre + l + " = " + r;
	}

	public int size() {
		return map.size();
	}
	
}
