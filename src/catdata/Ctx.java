package catdata;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.aql.Term;

@SuppressWarnings("serial")
public final class Ctx<K,V> implements Serializable {
	
	public final LinkedHashMap<K,V> map;
	
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
		this.map = new LinkedHashMap<>(map);
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
		return toString(Object::toString);
	}
	
	public String toString(Function<V,String> fn) {
		return Util.sep(map.entrySet().stream().map(z -> z.getKey() + ":" + fn.apply(z.getValue())).collect(Collectors.toList()), ",");
	}

	@Override
	public int hashCode() {
		int prime = 31;
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
			throw new RuntimeException("Encountered " + k + " but was expecting one of " + map.keySet() );
		}
		return ret;
	}

	public static <K,V> String toString(List<Pair<K, V>> ctx) {
		return Util.sep(ctx.stream().map(z -> z.first + (z.second != null ? (":" + z.second) : "")).collect(Collectors.toList()), ",");
	}

	public void put(K k, V v) {
		if (map.containsKey(k)) {
			throw new RuntimeException("already contains " + k + "\n\n" + this);
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

	public <X> Ctx<K,Chc<X,V>> inRight() {
		LinkedHashMap<K,Chc<X,V>> ret = new LinkedHashMap<>();
		for (K k : map.keySet()) {
			ret.put(k, Chc.inRight(map.get(k)));
		}
		return new Ctx<>(ret);
	}

	public <Z> Ctx<K,Z> map(Function<V, Z> f) {
		Ctx<K,Z> ret = new Ctx<>();
		for (K k : map.keySet()) {
			ret.put(k, f.apply(get(k)));			
		}
		return ret;
	}
	
	public <X,Y> Ctx<X,Y> map(BiFunction<K, V, Pair<X,Y>> f) {
		Ctx<X,Y> ret = new Ctx<>();
		for (K k : map.keySet()) {
			Pair<X, Y> x = f.apply(k, get(k));
			ret.put(x.first, x.second);			
		}
		return ret;
	}

	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	private boolean agreeOnOverlap0(Ctx<K, V> ret) {
		for (K k : map.keySet()) {
			if (ret.containsKey(k)) {
				if (!ret.get(k).equals(get(k))) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean agreeOnOverlap(Ctx<K, V> ret) {
		return agreeOnOverlap0(ret) && ret.agreeOnOverlap0(this);
	}
	
	public static <K,V> Ctx<K,V> fromNullable(Map<K,V> m) {
		Ctx<K,V> ret = new Ctx<>();
		for (K k : m.keySet()) {
			if (m.get(k) != null) {
				ret.put(k, m.get(k));
			}
		}
		return ret;
	}
	
}
