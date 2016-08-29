package catdata.aql;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.ide.Util;

public class Ctx<K,V> {

	private final Map<K,V> map;
	
	public boolean containsKey(K k) {
		return map.containsKey(k);
	}
	
	public Set<K> keys() {
		return map.keySet();
	}
	public Set<V> values() {
		return new HashSet<>(map.values());
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
	
	public Ctx(LinkedHashMap<K,V> map) {
		if (map == null) {
			throw new RuntimeException("Attempt to create a Ctx with null map");
		}
		this.map = map;
	}
	
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
	
}
