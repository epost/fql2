package catdata.aql.exp;

import java.util.HashSet;
import java.util.Set;

import catdata.Ctx;
import catdata.aql.Kind;

public class KindCtx<V,G,T,S,I,H,F,Q,P,C,SC> {

	public final Ctx<V, G> gs = new Ctx<>();
	public final Ctx<V, T> tys = new Ctx<>();
	public final Ctx<V, S> schs = new Ctx<>();
	public final Ctx<V, I> insts = new Ctx<>();
	public final Ctx<V, H> trans = new Ctx<>();
	public final Ctx<V, F> maps = new Ctx<>();
	public final Ctx<V, Q> qs = new Ctx<>();
	public final Ctx<V, P> ps = new Ctx<>();
	public final Ctx<V, C> cs = new Ctx<>();
	public final Ctx<V, SC> scs = new Ctx<>();
	
	public Object get(V k, Kind kind) {
		switch (kind) {
		case INSTANCE:
			return insts.get(k);
		case MAPPING:
			return maps.get(k);
		case SCHEMA:
			return schs.get(k);
		case TRANSFORM:
			return trans.get(k);
		case TYPESIDE:
			return tys.get(k);
		case QUERY:
			return qs.get(k);
		case PRAGMA:
			return ps.get(k);
		case GRAPH:
			return gs.get(k);
		case COMMENT:
			return cs.get(k);
		case SCHEMA_COLIMIT:
			return scs.get(k);
		default:
			throw new RuntimeException();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void put(V k, Kind kind, Object o) {
		switch (kind) {
		case INSTANCE:
			insts.put(k, (I) o);
			break;
		case MAPPING:
			maps.put(k, (F) o);
			break;
		case SCHEMA:
			schs.put(k, (S) o);
			break;
		case TRANSFORM:
			trans.put(k, (H) o);
			break;
		case TYPESIDE:
			tys.put(k, (T)o);
			break;
		case QUERY:
			qs.put(k, (Q) o);
			break;
		case PRAGMA:
			ps.put(k, (P) o);
			break;
		case GRAPH:
			gs.put(k, (G) o);
			break;
		case COMMENT:
			cs.put(k, (C) o);
			break;	
		case SCHEMA_COLIMIT:
			scs.put(k, (SC) o);
			break;
		default:
			throw new RuntimeException();
		}
	}

	
	public Set<V> keySet() { //TODO AQL do this by case on Kind to prevent forgetting
		Set<V> ret = new HashSet<>();
		ret.addAll(insts.keySet());
		ret.addAll(maps.keySet());
		ret.addAll(ps.keySet());
		ret.addAll(qs.keySet());
		ret.addAll(schs.keySet());
		ret.addAll(trans.keySet());
		ret.addAll(tys.keySet());
		ret.addAll(qs.keySet()); 
		ret.addAll(gs.keySet());
		ret.addAll(cs.keySet());
		return ret;
	}
}
