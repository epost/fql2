package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Ref;
import catdata.Triple;
import catdata.Util;

public final class MapExpRaw extends MapExp<Object,Object,Object,Object,Object,Object,Object,Object,Object> {
	
	public final SchExp<Object,Object,Object,Object,Object> src, dst;
	
	public final List<String> imports;
	
	public final List<Pair<Object, Object>> ens;
	public final List<Pair<Object, List<Object>>> fks;
	public final List<Pair<Object, Triple<String, Object, RawTerm>>> atts;
	
	public final List<Pair<String, String>> options;
	
	//typesafe by covariance of read only collections
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MapExpRaw(SchExp<?, ?, ?, ?, ?> src, SchExp<?, ?, ?, ?, ?> dst, List<String> imports, List<Pair<String, String>> ens, List<Pair<String, List<String>>> fks, List<Pair<String, Triple<String, String, RawTerm>>> atts, List<Pair<String, String>> options) {
		this.src = (SchExp<Object, Object, Object, Object, Object>) src;
		this.dst = (SchExp<Object, Object, Object, Object, Object>) dst;
		this.imports = imports;
		this.ens = new LinkedList(ens);
		this.fks = new LinkedList(fks);
		this.atts = new LinkedList(atts);
		this.options = options;
	}

	@Override
	public String toString() {
		return "MapExpRaw [src=" + src + ", dst=" + dst + ", imports=" + imports + ", ens=" + ens + ", fks=" + fks + ", atts=" + atts + ", options=" + options + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		MapExpRaw other = (MapExpRaw) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (imports == null) {
			if (other.imports != null)
				return false;
		} else if (!imports.equals(other.imports))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	public String meta() {
		return " : " + src + " -> " + dst;
	}

	//TODO: make collage morphism class?
	
	//TODO: create similar inference functionality that takes into account expected return types?
	
	@Override
	public Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(Env env) {
		Schema<Object, Object, Object, Object, Object> src0 = src.eval(env);
		Schema<Object, Object, Object, Object, Object> dst0 = dst.eval(env);
		//Collage<Object, Object, Object, Object, Object, Void, Void> scol = new Collage<>(src0);
		Collage<Object, Object, Object, Object, Object, Void, Void> dcol = new Collage<>(dst0);
		
		Map<Object, Object> ens0 = new HashMap<>();
		Map<Object, Pair<Object, List<Object>>> fks0 = new HashMap<>();
		Map<Object, Triple<Var, Object, Term<Object, Object, Object, Object, Object, Void, Void>>> atts0 = new HashMap<>();
		for (String k : imports) {
			Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> v = env.getMapping(k);
			Util.putAllSafely(ens0, v.ens);
			Util.putAllSafely(fks0, v.fks);
			Util.putAllSafely(atts0, v.atts);
		}
		
		Util.putAllSafely(ens0, Util.toMapSafely(ens));
		List<Pair<Object, Pair<Object, List<Object>>>> fksX = new LinkedList<>();
		for (Pair<Object, List<Object>> p : fks) {
			Object start_en = null;
			if (dst0.ens.contains(p.second.get(0))) {
				if (fks0.containsKey(p.second.get(0))) {
					throw new RuntimeException("in foreign key mapping " + p.first + " -> " + Util.sep(p.second, ".") + ", " + p.second.get(0) + " is both a target foreign key and a target entity, so the path is ambiguous");
				}
				start_en = p.second.get(0);
			} else {
				Pair<Object, Object> j = dst0.fks.get(p.second.get(0));
				if (j == null) {
					throw new RuntimeException("in foreign key mapping " + p.first + " -> " + Util.sep(p.second, ".") + ", " + p.second.get(0) + " is not a foreign key in the target");
				}
				start_en = j.first;
			}
			//get first object.  if entity and not fk, use entity else error.  if fk, lookup source
			List<Object> r = new LinkedList<>();
			for (Object o : p.second) {
				if (ens0.containsKey(o)) {
					if (fks0.containsKey(o)) {
						throw new RuntimeException("in foreign key mapping " + p.first + " -> " + Util.sep(p.second, ".") + ", " + o + " is both a target foreign key and a target entity, so the path is ambiguous");
					}
				} else {
					r.add(o);
				}
			}
			fksX.add(new Pair<>(p.first, new Pair<>(start_en, r)));
		}
		Util.putAllSafely(fks0, Util.toMapSafely(fksX));
		
		for (Pair<Object, Triple<String, Object, RawTerm>> att : atts) {
			String var = att.second.first;
			Object proposed_en = att.second.second;
			RawTerm term = att.second.third;

			if (proposed_en != null && !dst0.ens.contains(proposed_en)) {
				throw new RuntimeException("in " + term + ", the proposed sort " + proposed_en + " is not a target entity");
			}
			
			Map<String, Ref<Chc<Object, Object>>> ctx = Util.singMap(var, proposed_en == null ? new Ref<>() : new Ref<>(Chc.inRight(proposed_en)));
				
			Ref<Chc<Object,Object>> ref = term.infer(Util.singSet(var), ctx, dcol);
			if (ref.x != null) {
				ref.x.assertNeitherNull();
			}

			String msg = proposed_en == null ? "Possible fixes: add a type annotation to the lambda, or add a type annotation to a java constant" : "";
			Ref<Chc<Object, Object>> actual_en = ctx.get(var);

			if (ref.x == null) {
				throw new RuntimeException("in " + term + ", cannot infer sort for " + term + ".  " + msg);
			} else if (actual_en.x == null) {
				throw new RuntimeException("in " + term + ", cannot infer sort for " + var + ".  " + msg);				
			} else if (actual_en.x.left) {
				throw new RuntimeException("in " + term + ", infered sort for " + var + " is " + actual_en.x.l + " which is not an entity");								
			} else if (proposed_en != null && !actual_en.x.r.equals(proposed_en)) {
				throw new RuntimeException("in " + term + ", infered entity for " + var + " is " + actual_en.x.r + " which is not the proposed entity of " + proposed_en);												
			} else if (proposed_en == null) {
				proposed_en = actual_en.x.r;							
			} 
					
			Term<Object, Object, Object, Object, Object, Void, Void> term0 = term.trans(Util.singSet(var), ctx, dcol);
					
			Util.putSafely(atts0, att.first, new Triple<>(new Var(var), proposed_en, term0));
		}
		
		Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = new Mapping<>(ens0, atts0, fks0, src0, dst0);
		return ret; 
	}
}