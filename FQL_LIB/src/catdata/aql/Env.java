package catdata.aql;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class Env {

	private Map<String, TypeSide> tys = new HashMap<>();
	private Map<String, Schema> schs = new HashMap<>();
	private Map<String, Instance> insts = new HashMap<>();
	private Map<String, Transform> trans = new HashMap<>();
	private Map<String, Mapping> maps = new HashMap<>();
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
	
	public void put(String k, Kind kind, Object o) {
		kind.checkLiteral(k, o);
		switch (kind) {
		case INSTANCE:
			put(k, (Instance)o);
			break;
		case MAPPING:
			put(k, (Mapping)o);
			break;
		case SCHEMA:
			put(k, (Schema)o);
			break;
		case TRANSFORM:
			put(k, (Transform)o);
			break;
		case TYPESIDE:
			put(k, (TypeSide)o);
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
	
	public void put(String k, TypeSide v) {
		if (tys.containsKey(k)) {
			throw new RuntimeException("Already a top-level typeside definition for " + k);
		}
		tys.put(k, v);
	}
	
	public TypeSide<?, ?> getTypeSide(String k) {
		TypeSide ret = tys.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level typeside definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Schema v) {
		if (schs.containsKey(k)) {
			throw new RuntimeException("Already a top-level schema definition for " + k);
		}
		schs.put(k, v);
	}
	
	public Schema getSchema(String k) {
		Schema ret = schs.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level schema definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Instance v) {
		if (insts.containsKey(k)) {
			throw new RuntimeException("Already a top-level instance definition for " + k);
		}
		insts.put(k, v);
	}
	
	public Instance getInstance(String k) {
		Instance ret = insts.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level instance definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Transform v) {
		if (trans.containsKey(k)) {
			throw new RuntimeException("Already a top-level transform definition for " + k);
		}
		trans.put(k, v);
	}
	
	public Transform getTransform(String k) {
		Transform ret = trans.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level transform definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Mapping v) {
		if (maps.containsKey(k)) {
			throw new RuntimeException("Already a top-level mapping definition for " + k);
		}
		maps.put(k, v);
	}
	
	public Mapping getMapping(String k) {
		Mapping ret = maps.get(k);
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
