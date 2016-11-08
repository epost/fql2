package catdata.aql.exp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import catdata.DMG;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Ctx;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Pragma;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.Transform;
import catdata.aql.TypeSide;
import catdata.ide.Program;

public final class AqlEnv {

	public Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> mtys = new Ctx<>();
	public Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qtys = new Ctx<>();
	public Ctx<String, SchExp<Object,Object,Object,Object,Object>> itys = new Ctx<>();
	public Ctx<String, Pair<InstExp<Object,Object,Object,Object,Object,Object,Object,Object,Object>,  InstExp<Object,Object,Object,Object,Object,Object,Object,Object,Object>>> ttys = new Ctx<>();

	//TODO AqlEnv move to ctx
	private Map<String, DMG<Object, Object>> gs = new HashMap<>();
	private Map<String, TypeSide<Object, Object>> tys = new HashMap<>();
	private Map<String, Schema<Object, Object, Object, Object, Object>> schs = new HashMap<>();
	private Map<String, Instance<Object,Object,Object, Object, Object, Object, Object, Object, Object>> insts = new HashMap<>();
	private Map<String, Transform<Object,Object,Object,Object,Object, Object, Object, Object, Object,Object, Object, Object, Object>> trans = new HashMap<>();
	private Map<String, Mapping<Object, Object, Object, Object,Object, Object, Object, Object>> maps = new HashMap<>();
	private Map<String, Query<Object, Object, Object, Object, Object, Object, Object, Object>> qs = new HashMap<>();
	private Map<String, Pragma> ps = new HashMap<>();
	
	public Throwable exn = null;
	
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
		case GRAPH:
			return getGraph(k);
		}
		throw new RuntimeException();
	}
	
	@SuppressWarnings("unchecked")
	public void put(String k, Kind kind, Object o) {
		kind.checkLiteral(k, o);
		switch (kind) {
		case INSTANCE:
			put(k, (Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case MAPPING:
			put(k, (Mapping<Object, Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case SCHEMA:
			put(k, (Schema<Object, Object, Object, Object, Object>)o);
			break;
		case TRANSFORM:
			put(k, (Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case TYPESIDE:
			put(k, (TypeSide<Object, Object>)o);
			break;
		case QUERY:
			put(k, (Query<Object, Object, Object, Object, Object, Object, Object, Object>)o);
			break;
		case PRAGMA:
			put(k, (Pragma)o);
			break;
		case GRAPH:
			put(k, (DMG<Object,Object>) o);
			break;
		default:
			throw new RuntimeException();
		}
	}
	
	public void put(String k, TypeSide<Object, Object> v) {
		Util.assertNotNull(v);
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
		Util.assertNotNull(v);
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
	
	public void put(String k, Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> v) {
		Util.assertNotNull(v);
		if (insts.containsKey(k)) {
			throw new RuntimeException("Already a top-level instance definition for " + k);
		}
		insts.put(k, v);
	}
	
	public Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> getInstance(String k) {
		Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = insts.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level instance definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> v) {
		Util.assertNotNull(v);
		if (trans.containsKey(k)) {
			throw new RuntimeException("Already a top-level transform definition for " + k);
		}
		trans.put(k, v);
	}
	
	public Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> getTransform(String k) {
		Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = trans.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level transform definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Mapping<Object, Object, Object, Object, Object, Object, Object, Object> v) {
		Util.assertNotNull(v);
		if (maps.containsKey(k)) {
			throw new RuntimeException("Already a top-level mapping definition for " + k);
		}
		maps.put(k, v);
	}
	
	public Mapping<Object, Object, Object, Object, Object, Object, Object, Object> getMapping(String k) {
		Mapping<Object, Object, Object, Object, Object, Object, Object, Object> ret = maps.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level mapping definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, Query<Object, Object, Object, Object, Object, Object, Object, Object> v) {
		Util.assertNotNull(v);
		if (qs.containsKey(k)) {
			throw new RuntimeException("Already a top-level query definition for " + k);
		}
		qs.put(k, v);
	}
	
	public Query<Object, Object, Object, Object, Object, Object, Object, Object> getQuery(String k) {
		Query<Object, Object, Object, Object, Object, Object, Object, Object> ret = qs.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level query definition for " + k);
		}
		return ret;
	}
	
	public void put(String k, DMG<Object, Object> v) {
		if (gs.containsKey(k)) {
			throw new RuntimeException("Already a top-level graph definition for " + k);
		}
		gs.put(k, v);
	}
	
	public DMG<Object, Object> getGraph(String k) {
		DMG<Object, Object> ret = gs.get(k);
		if (ret == null) {
			throw new RuntimeException("No top-level graph definition for " + k);
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

	

	public Set<String> keySet() {
		Set<String> ret = new HashSet<>();
		ret.addAll(insts.keySet());
		ret.addAll(maps.keySet());
		ret.addAll(ps.keySet());
		ret.addAll(qs.keySet());
		ret.addAll(schs.keySet());
		ret.addAll(trans.keySet());
		ret.addAll(tys.keySet());
		ret.addAll(qs.keySet()); 
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void type(Program<Exp<?>> prog) {
		for (String s : prog.order) {
			Exp<?> e = prog.exps.get(s);
			switch (e.kind()) {
			case INSTANCE:
				itys.put(s, ((InstExp)e).type(mtys, itys, qtys));
				continue;
			case MAPPING:
				mtys.put(s, ((MapExp)e).type(mtys));
				continue;
			case PRAGMA:
				continue;
			case QUERY:
				qtys.put(s, ((QueryExp)e).type(qtys));
				continue;
			case SCHEMA:
				continue;
			case TRANSFORM:
				ttys.put(s, ((TransExp)e).type(mtys, itys, ttys, qtys));
				continue;
			case TYPESIDE:
				continue;
			case GRAPH:
				continue;
			}
			throw new RuntimeException("Anomaly: please report");
		}
	}
	
}
