package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.It.ID;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;


public final class InstExpRaw extends InstExp<Object,Object,Object,Object,Object,Object,Object,ID,Chc<Object,Pair<ID,Object>>> {

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(schema.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.INSTANCE)).collect(Collectors.toList()));
		return ret;
	}
 
	public final SchExp<Object,Object,Object,Object,Object> schema;
	
	public final List<String> imports;

	public final List<Pair<Object, Object>> gens; //TODO aql why is this object and not gens

	public final List<Pair<RawTerm, RawTerm>> eqs;
	
	public final Map<String, String> options;
	
	@Override
	public long timeout() {
		return (Long) AqlOptions.getOrDefault(options, AqlOption.timeout);
	}	

	//typesafe by covariance of read-only collections
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public InstExpRaw(SchExp<?,?,?,?,?> schema, List<String> imports, List<Pair<String, String>> gens, List<Pair<RawTerm, RawTerm>> eqs, Map<String, String> options) {
		this.schema = (SchExp<Object, Object, Object, Object, Object>) schema;
		this.imports = imports;
		this.gens = new LinkedList(gens);
		this.eqs = eqs;
		this.options = options;
		Util.toMapSafely(gens); //do this here rather than wait until 
	}

		@Override
	public String toString() {
		return "InstExpRaw [schema=" + schema + ", imports=" + imports + ", gens=" + gens + ", eqs=" + eqs + ", options=" + options + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
		InstExpRaw other = (InstExpRaw) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
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
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

	@Override
	public Instance<Object, Object, Object, Object, Object, Object, Object, ID, Chc<Object, Pair<ID, Object>>> eval(AqlEnv env) {
		Schema<Object, Object, Object, Object, Object> sch = schema.eval(env);
		Collage<Object, Object, Object, Object, Object, Object, Object> col = new Collage<>(sch.collage());
		
		Set<Pair<Term<Object, Object, Object, Object, Object, Object, Object>, Term<Object, Object, Object, Object, Object, Object, Object>>> eqs0 = new HashSet<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> v = env.defs.insts.get(k);
			col.gens.putAll(v.gens().map);
			col.sks.putAll(v.sks().map);
			eqs0.addAll(v.eqs());
		}
		
		for (Pair<Object, Object> p : gens) {
			Object gen = p.first;
			Object ty = p.second;
			if (col.ens.contains(ty)) {
				col.gens.put(gen, ty);
			} else if (col.tys.contains(ty)) {
				col.sks.put(gen, ty);
			} else {
				throw new RuntimeException("The sort for " + gen + ", namely " + ty + ", is not declared as a type or entity");
			}
		}
	
		for (Pair<RawTerm, RawTerm> eq : eqs) {
				Map<String, Chc<Object, Object>> ctx = Collections.emptyMap();
				
				Triple<Ctx<String,Chc<Object,Object>>,Term<Object,Object,Object,Object,Object,Object,Object>,Term<Object,Object,Object,Object,Object,Object,Object>>
				eq0 = RawTerm.infer1(ctx, eq.first, eq.second, col, sch.typeSide.js);
						
				eqs0.add(new Pair<>(eq0.second, eq0.third));
				col.eqs.add(new Eq<>(new Ctx<>(), eq0.second, eq0.third));
		}

		AqlOptions strat = new AqlOptions(options, col);
		
		InitialAlgebra<Object, Object, Object, Object, Object, Object, Object, ID> 
		initial = new InitialAlgebra<>(strat, sch, col, new It(), x -> x.toString(), x -> x.toString());
				 
		return new LiteralInstance<>(sch, col.gens.map, col.sks.map, eqs0, initial.dp(), initial); 
	}
	
	@Override
	public SchExp<Object, Object, Object, Object, Object> type(AqlTyping G) {
		return schema;
	}
	
}