package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlJs;
import catdata.aql.AqlOptions;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Eq;
import catdata.aql.RawTerm;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;

//TODO: aql add shortcuts to editor

//TODO aql quoting (reuse example maker?)
public final class TyExpRaw extends TyExp<Object, Object> {

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return imports.stream().map(x -> new Pair<>(x, Kind.TYPESIDE)).collect(Collectors.toSet());
	}

	public final Set<String> imports;
	public final Set<Object> types;
	public final Set<Pair<Object, Pair<List<Object>, Object>>> functions;
	public final Set<Triple<List<Pair<String, Object>>, RawTerm, RawTerm>> eqs;

	public final Set<Pair<Object, String>> java_tys_string;
	public final Set<Pair<Object, String>> java_parser_string;
	public final Set<Pair<Object, Triple<List<Object>, Object, String>>> java_fns_string;

	public final Map<String, String> options;
	private AqlOptions strat;
	
	private Set<Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>>> eqs0 = new HashSet<>();


	// typesafe by covariance of read-only collections
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TyExpRaw(List<String> imports, List<String> types, List<Pair<String, Pair<List<String>, String>>> functions, List<Triple<List<Pair<String, String>>, RawTerm, RawTerm>> eqs, List<Pair<String, String>> java_tys_string, List<Pair<String, String>> java_parser_string, List<Pair<String, Triple<List<String>, String, String>>> java_fns_string, List<Pair<String, String>> options) {
		this.imports = new HashSet<>(imports);
		this.types = new HashSet<>(types);
		this.functions = new HashSet(functions);
		this.eqs = new HashSet(eqs);
		this.java_tys_string = new HashSet(java_tys_string);
		this.java_parser_string = new HashSet(java_parser_string);
		this.java_fns_string = new HashSet(java_fns_string);
		this.options = Util.toMapSafely(options);
		
		col.tys.addAll(types);
		col.syms.putAll(Util.toMapSafely(this.functions));
		col.java_tys.putAll(Util.toMapSafely(this.java_tys_string));
		col.tys.addAll(col.java_tys.keySet());
		col.java_parsers.putAll(Util.toMapSafely(this.java_parser_string));
		
		for (Entry<Object, Triple<List<Object>, Object, String>> kv : Util.toMapSafely(this.java_fns_string).entrySet()) {
			col.syms.put(kv.getKey(), new Pair<>(kv.getValue().first, kv.getValue().second));
			col.java_fns.put(kv.getKey(), kv.getValue().third);
		}
		AqlJs<Object,Object> js = new AqlJs<>(col.syms, col.java_fns, col.java_tys, col.java_parsers);
		for (Triple<List<Pair<String, Object>>, RawTerm, RawTerm> eq : this.eqs) {
			Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> tr = inferEq(col, eq, js);
			col.eqs.add(new Eq<>(tr.first.inLeft(), tr.second, tr.third));
			eqs0.add(tr);
		}

		strat = new AqlOptions(Util.toMapSafely(options), col);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((functions == null) ? 0 : functions.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((java_fns_string == null) ? 0 : java_fns_string.hashCode());
		result = prime * result + ((java_parser_string == null) ? 0 : java_parser_string.hashCode());
		result = prime * result + ((java_tys_string == null) ? 0 : java_tys_string.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		TyExpRaw other = (TyExpRaw) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (functions == null) {
			if (other.functions != null)
				return false;
		} else if (!functions.equals(other.functions))
			return false;
		if (imports == null) {
			if (other.imports != null)
				return false;
		} else if (!imports.equals(other.imports))
			return false;
		if (java_fns_string == null) {
			if (other.java_fns_string != null)
				return false;
		} else if (!java_fns_string.equals(other.java_fns_string))
			return false;
		if (java_parser_string == null) {
			if (other.java_parser_string != null)
				return false;
		} else if (!java_parser_string.equals(other.java_parser_string))
			return false;
		if (java_tys_string == null) {
			if (other.java_tys_string != null)
				return false;
		} else if (!java_tys_string.equals(other.java_tys_string))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RawTypeSide [imports=" + imports + ", types=" + types + ", functions=" + functions + ", eqs=" + eqs + ", java_tys_string=" + java_tys_string + ", java_parser_string=" + java_parser_string + ", java_fns_string=" + java_fns_string + ", options=" + options + "]";
	}
	
	private final Collage<Object, Void, Object, Void, Void, Void, Void> col = new Collage<>();

	@Override
	public TypeSide<Object, Object> eval(AqlEnv env) {
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			TypeSide<Object, Object> v = env.defs.tys.get(k);
			col.tys.addAll(v.tys);
			col.syms.putAll(v.syms.map);
			col.addEqs(v.eqs);
			col.java_tys.putAll(v.js.java_tys.map);
			col.java_fns.putAll(v.js.java_fns.map);
			col.java_parsers.putAll(v.js.java_parsers.map);
			eqs0.addAll(v.eqs);
		}
		
		TypeSide<Object, Object> ret = new TypeSide<>(col.tys, col.syms.map, eqs0, col.java_tys.map, col.java_parsers.map, col.java_fns.map, strat);

		return ret;
	}

	private static Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> inferEq(Collage<Object, Void, Object, Void, Void, Void, Void> col, Triple<List<Pair<String, Object>>, RawTerm, RawTerm> eq, AqlJs<Object,Object> js) {
		Map<String, Chc<Object, Void>> ctx = new HashMap<>();
		for (Pair<String, Object> p : eq.first) {
			if (ctx.containsKey(p.first)) {
				throw new RuntimeException("Duplicate variable " + p.first + " in context " + Ctx.toString(eq.first));
			}
			if (p.second != null) {
				ctx.put(p.first, Chc.inLeft(p.second));
			} else {
				ctx.put(p.first, null);
			}
		}
		Triple<Ctx<String, Chc<Object, Void>>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> eq0 = RawTerm.infer1(ctx, eq.second, eq.third, col, js);

		LinkedHashMap<Var, Object> map = new LinkedHashMap<>();
		for (String k : ctx.keySet()) {
			Chc<Object, Void> v = eq0.first.get(k);
			if (!v.left) {
				throw new RuntimeException("Anomaly: please report");
			}
			map.put(new Var(k), v.l);
		}
		/*
		 * for (String k : eq0.first.keys()) { if (!ctx.keySet().contains(k)) {
		 * throw new RuntimeException("In " + eq.second + " = " + eq.third +
		 * ", not a variable or symbol: " + k +
		 * " . (Note: java not allowed in typeside equations)"); } }
		 */
		Ctx<Var, Object> ctx2 = new Ctx<>(map);
		Term<Object, Void, Object, Void, Void, Void, Void> lhs = eq0.second;
		Term<Object, Void, Object, Void, Void, Void, Void> rhs = eq0.third;

		Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> tr = new Triple<>(ctx2, lhs, rhs);
		return tr;
	}

}