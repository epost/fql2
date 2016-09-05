package catdata.aql;

import java.util.HashMap;
import java.util.Map;

public final class Env {

	private Map<String, TypeSide<Object, Object>> tys = new HashMap<>();
	private Map<String, Schema<Object, Object, Object, Object, Object>> schs = new HashMap<>();
	private Map<String, Instance<Object, Object, Object, Object, Object, Object, Object>> insts = new HashMap<>();
	private Map<String, Transform<Object, Object, Object, Object, Object,Object, Object, Object, Object>> trans = new HashMap<>();
	private Map<String, Mapping<Object, Object, Object, Object, Object,Object, Object, Object, Object>> maps = new HashMap<>();
	private Map<String, Query> qs = new HashMap<>();
	private Map<String, Pragma> ps = new HashMap<>();
	
	public Object get(String k, Kind kind) {
		switch (kind) {
		case INSTANCE:
			return getInstance(k);
		case MAPPING:
			return getMapping(k);
		case SCHEMA:
			return getSchema(k);
		case TRANSFORM:
			return getTransform(k);
		case TYPESIDE:
			return getTypeSide(k);
		case QUERY:
			return getQuery(k);
		case PRAGMA:
			return getPragma(k);
		default:
			throw new RuntimeException();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void put(String k, Kind kind, Object o) {
		kind.checkLiteral(k, o);
		switch (kind) {
		case INSTANCE:
			put(k, (Instance<Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case MAPPING:
			put(k, (Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case SCHEMA:
			put(k, (Schema<Object, Object, Object, Object, Object>)o);
			break;
		case TRANSFORM:
			put(k, (Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case TYPESIDE:
			put(k, (TypeSide<Object, Object>)o);
			break;
		case QUERY:
			put(k, (Query)o);
			break;
		case PRAGMA:
			put(k, (Pragma)o);
		default:
			throw new RuntimeException();
		}
	}
	
	public void put(String k, TypeSide<Object, Object> v) {
		if (tys.containsKey(k)) {
			throw new RuntimeException("Already a top-level typeside definition for " + k);
		}
		tys.put(k, v);
	}
	
	public TypeSide<Object,Object> getTypeSide(String k) {
		TypeSide<Object,Object> ret = tys.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level typeside definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Schema<Object, Object, Object, Object, Object> v) {
		if (schs.containsKey(k)) {
			throw new RuntimeException("Already a top-level schema definition for " + k);
		}
		schs.put(k, v);
	}
	
	public Schema<Object, Object, Object, Object, Object> getSchema(String k) {
		Schema<Object, Object, Object, Object, Object> ret = schs.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level schema definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Instance<Object, Object, Object, Object, Object, Object, Object> v) {
		if (insts.containsKey(k)) {
			throw new RuntimeException("Already a top-level instance definition for " + k);
		}
		insts.put(k, v);
	}
	
	public Instance<Object, Object, Object, Object, Object, Object, Object> getInstance(String k) {
		Instance<Object, Object, Object, Object, Object, Object, Object> ret = insts.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level instance definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> v) {
		if (trans.containsKey(k)) {
			throw new RuntimeException("Already a top-level transform definition for " + k);
		}
		trans.put(k, v);
	}
	
	public Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> getTransform(String k) {
		Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = trans.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level transform definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> v) {
		if (maps.containsKey(k)) {
			throw new RuntimeException("Already a top-level mapping definition for " + k);
		}
		maps.put(k, v);
	}
	
	public Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> getMapping(String k) {
		Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = maps.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level mapping definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Query v) {
		if (qs.containsKey(k)) {
			throw new RuntimeException("Already a top-level query definition for " + k);
		}
		qs.put(k, v);
	}
	
	public Query getQuery(String k) {
		Query ret = qs.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level query definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Pragma v) {
		if (ps.containsKey(k)) {
			throw new RuntimeException("Already a top-level pragma definition for " + k);
		}
		ps.put(k, v);
	}
	
	public Pragma getPragma(String k) {
		Pragma ret = ps.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level pragma definition for " + k);
		}
		return ret;
	}
	
}
