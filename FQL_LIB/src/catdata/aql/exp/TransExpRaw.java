package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Instance;
import catdata.aql.RawTerm;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.fdm.TransformLiteral;

//TODO aql grobner basis prover
public final class TransExpRaw extends TransExp<Object,Object,Object,Object,Object,Object,Object,Object,Object,Object,Object,Object,Object> {
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.TRANSFORM)).collect(Collectors.toList()));
		return ret;
	}
	public final InstExp<Object,Object,Object,Object,Object,Object,Object,Object,Object> src, dst;
	
	public final List<String> imports;
	
	public final List<Pair<Object, RawTerm>> gens;		
	
	public final Map<String, String> options;
	 
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

	//typeside by covariance of read only collections TODO aql
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TransExpRaw(InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?> src, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?> dst, List<String> imports, List<Pair<String, RawTerm>> gens, List<Pair<String, String>> options) {
		this.src = (InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>) src;
		this.dst = (InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>) dst;
		this.imports = imports;
		this.gens = new LinkedList(gens);
		Util.toMapSafely(this.gens); //do here rather than wait
		this.options = Util.toMapSafely(options);
	}

	@Override
	public TransformLiteral<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
		Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> src0 = src.eval(env), dst0 = dst.eval(env);
		//Collage<Object, Object, Object, Object, Object, Void, Void> scol = new Collage<>(src0);
		Collage<Object, Object, Object, Object, Object, Object, Object> dcol = dst0.collage(); //new Collage<>(dst0);
		
		Map<Object, Term<Void,Object,Void,Object,Void,Object,Void>> gens0 = new HashMap<>();
		Map<Object, Term<Object,Object,Object,Object,Object,Object,Object>> sks0 = new HashMap<>();
		for (String k : imports) {
			Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> v = env.getTransform(k);
			Util.putAllSafely(gens0, v.gens().map);
			Util.putAllSafely(sks0, v.sks().map);
		}
		
		for (Pair<Object, RawTerm> gen : gens) {
			RawTerm term = gen.second;
			Map<String, Chc<Object, Object>> ctx = new HashMap<>();
				
			Chc<Object,Object> required = null;
			if (src0.gens().containsKey(gen.first) && src0.sks().containsKey(gen.first)) {
				throw new RuntimeException("in transform for " + gen.first + ", " + gen.first + " is ambiguously an entity generator and labelled null");
			} else if (src0.gens().containsKey(gen.first)) {
				required = Chc.inRight(src0.gens().get(gen.first));
			} else if (src0.sks().containsKey(gen.first)){
				required = Chc.inLeft(src0.sks().get(gen.first));				
			} else {
				throw new RuntimeException("in transform for " + gen.first + ", " + gen.first + " is not a source generator/labelled null");
			}
			
			Term<Object, Object, Object, Object, Object, Object, Object> term0 = RawTerm.infer0(ctx, term, required, dcol, "in transform for " + gen.first + ", ");
						
			if (required.left) {
				Util.putSafely(sks0, gen.first, term0);				
			} else {
				Util.putSafely(gens0, gen.first, term0.convert());
			}
		}
		
		AqlOptions ops = new AqlOptions(options, null);
		
		
		TransformLiteral<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> ret = new TransformLiteral<>(gens0, sks0, src0, dst0, (Boolean) ops.getOrDefault(AqlOption.dont_validate_unsafe) );
		return ret; 
	}

	@Override
	public Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
			Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1) {
		return new Pair<>(src, dst);
	}
	
}