package catdata.aql;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Ref;
import catdata.Util;

public final class TransExpRaw extends TransExp<Object,Object,Object,Object,Object,Object,Object,Object,Object> {
	
	public final InstExp<Object,Object,Object,Object,Object,Object,Object> src, dst;
	
	public final List<String> imports;
	
	public final List<Pair<Object, RawTerm>> gens;		
	
	public final List<Pair<String, String>> options;
	
	@Override
	public String meta() {
		return " : " + src + " -> " + dst;
	}

	@Override
	public String toString() {
		return "TransExpRaw [src=" + src + ", dst=" + dst + ", imports=" + imports + ", gens=" + gens + ", options=" + options + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
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
		TransExpRaw other = (TransExpRaw) obj;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (gens == null) {
			if (other.gens != null)
				return false;
		} else if (!gens.equals(other.gens))
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

	//typeside by covariance of read only collections
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TransExpRaw(InstExp<?, ?, ?, ?, ?, ?, ?> src, InstExp<?, ?, ?, ?, ?, ?, ?> dst, List<String> imports, List<Pair<String, RawTerm>> gens, List<Pair<String, String>> options) {
		this.src = (InstExp<Object, Object, Object, Object, Object, Object, Object>) src;
		this.dst = (InstExp<Object, Object, Object, Object, Object, Object, Object>) dst;
		this.imports = imports;
		this.gens = new LinkedList(gens);
		this.options = options;
	}

	//TODO: more inference for primitives based on expected type, also use for mapping.  set up similarly to one for =
	@Override
	public Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(Env env) {
		Instance<Object, Object, Object, Object, Object, Object, Object> src0 = src.eval(env), dst0 = dst.eval(env);
		//Collage<Object, Object, Object, Object, Object, Void, Void> scol = new Collage<>(src0);
		Collage<Object, Object, Object, Object, Object, Object, Object> dcol = new Collage<>(dst0);
		
		Map<Object, Term<Object,Object,Object,Object,Object,Object,Object>> gens0 = new HashMap<>(), sks0 = new HashMap<>();
		for (String k : imports) {
			Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> v = env.getTransform(k);
			Util.putAllSafely(gens0, v.gens);
			Util.putAllSafely(sks0, v.sks);
		}
		
		for (Pair<Object, RawTerm> gen : gens) {
			RawTerm term = gen.second;
			Map<String, Ref<Chc<Object, Object>>> ctx = new HashMap<>();
				
			Ref<Chc<Object,Object>> ref = term.infer(Collections.emptySet(), ctx, dcol);
			if (ref.x != null) {
				ref.x.assertNeitherNull();
			}
			
			if (ref.x == null) {
				throw new RuntimeException("in " + term + ", cannot infer sort for " + term);
			} 
			
			Chc<Object,Object> infered = ref.x;
			Chc<Object,Object> required = null;
			if (src0.gens.containsKey(gen.first) && src0.sks.containsKey(gen.first)) {
				throw new RuntimeException("in transform for " + gen.first + ", " + gen.first + " is ambiguously an entity generator and labelled null");
			} else if (src0.gens.containsKey(gen.first)) {
				required = Chc.inRight(src0.gens.get(gen.first));
			} else if (src0.sks.containsKey(gen.first)){
				required = Chc.inLeft(src0.sks.get(gen.first));				
			} else {
				throw new RuntimeException("in transform for " + gen.first + ", " + gen.first + " is not a source generator/labelled null");
			}
			
		
			if (!infered.equals(required)) {
				throw new RuntimeException("in transform for " + gen.first + ", inferred sort is " + infered.toStringMash() + " which is not the required sort " + required.toStringMash());												
			} 
					
			Term<Object, Object, Object, Object, Object, Object, Object> term0 = term.trans(Collections.emptySet(), ctx, dcol);
					
			if (required.left) {
				Util.putSafely(sks0, gen.first, term0);				
			} else {
				Util.putSafely(gens0, gen.first, term0);
			}
		}
		
		Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = new Transform<>(gens0, sks0, src0, dst0);
		return ret; 
	}
	
}