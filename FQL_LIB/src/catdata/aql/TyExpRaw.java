package catdata.aql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;

//TODO: add shortcuts to editor
//TODO: syntax coloring
//TODO quoting (reuse example maker?)
public final class TyExpRaw extends TyExp<Object, Object> {

		public final Set<String> imports;
		public final Set<Object> types;
		public final Set<Pair<Object, Pair<List<Object>, Object>>> functions;
		public final Set<Triple<List<Pair<String, Object>>, RawTerm, RawTerm>> eqs;

		public final Set<Pair<Object, String>> java_tys_string;
		public final Set<Pair<Object, String>> java_parser_string;
		public final Set<Pair<Object, Triple<List<Object>, Object, String>>> java_fns_string;
		
		public final Set<Pair<String, String>> options;

		//typesafe by covariance of read-only collections
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public TyExpRaw(List<String> imports, List<String> types, List<Pair<String, Pair<List<String>, String>>> functions, List<Triple<List<Pair<String, String>>, RawTerm, RawTerm>> eqs, List<Pair<String, String>> java_tys_string, List<Pair<String, String>> java_parser_string, List<Pair<String, Triple<List<String>, String, String>>> java_fns_string, List<Pair<String, String>> options) {
			this.imports = new HashSet<>(imports);
			this.types = new HashSet<>(types);
			this.functions = new HashSet(functions);
			this.eqs = new HashSet(eqs);
			this.java_tys_string = new HashSet(java_tys_string);
			this.java_parser_string = new HashSet(java_parser_string);
			this.java_fns_string = new HashSet(java_fns_string);
			this.options = new HashSet<>(options);
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
		
		@Override
		public TypeSide<Object, Object> eval(Env env) {
			Collage<Object, Void, Object, Void, Void, Void, Void> col = new Collage<>();
			
			col.tys.addAll(types);
			col.syms.putAll(Util.toMapSafely(functions));
			col.java_tys.putAll(Util.toMapSafely(java_tys_string));
			col.tys.addAll(col.java_tys.keySet());
			col.java_parsers.putAll(Util.toMapSafely(java_parser_string));
			
			for (Entry<Object, Triple<List<Object>, Object, String>> kv : Util.toMapSafely(java_fns_string).entrySet()) {
				Util.putSafely(col.syms, kv.getKey(), new Pair<>(kv.getValue().first, kv.getValue().second));
				Util.putSafely(col.java_fns, kv.getKey(), kv.getValue().third); 
			}

			Set<Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>>> eqs0 = new HashSet<>();

			for (String k : imports) {
				TypeSide<Object, Object> v = env.getTypeSide(k);
				col.tys.addAll(v.tys);
				Util.putAllSafely(col.syms, v.syms);
				eqs0.addAll(v.eqs);
				Util.putAllSafely(col.java_tys, v.java_tys);
				Util.putAllSafely(col.java_fns, v.java_fns);
				Util.putAllSafely(col.java_parsers, v.java_parsers);
			}

			for (Triple<List<Pair<String, Object>>, RawTerm, RawTerm> eq : eqs) {
					Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> tr = inferEq(col, eq);
					eqs0.add(tr);
			}
			
			AqlOptions strat = new AqlOptions(Util.toMapSafely(options), col); 
			
			TypeSide<Object, Object> ret = new TypeSide<>(col.tys, col.syms, eqs0, col.java_tys, col.java_parsers, col.java_fns, strat);
	
			return ret;
		}

		private static Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> inferEq(Collage<Object, Void, Object, Void, Void, Void, Void> col, Triple<List<Pair<String, Object>>, RawTerm, RawTerm> eq) {
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
			Triple<Ctx<String,Chc<Object,Void>>,Term<Object,Void,Object,Void,Void,Void,Void>,Term<Object,Void,Object,Void,Void,Void,Void>>
			eq0 = RawTerm.infer1(ctx, eq.second, eq.third, col);

			LinkedHashMap<Var, Object> map = new LinkedHashMap<>();
			for (String k : ctx.keySet()) {
				Chc<Object, Void> v = eq0.first.get(k);
				if (!v.left) {
					throw new RuntimeException("Anomaly: please report");
				}
				map.put(new Var(k), v.l);
			}
/*			for (String k : eq0.first.keys()) {
				if (!ctx.keySet().contains(k)) {
					throw new RuntimeException("In " + eq.second + " = " + eq.third + ", not a variable or symbol: " + k + " . (Note: java not allowed in typeside equations)");
				}
			}	*/
			Ctx<Var, Object> ctx2 = new Ctx<>(map);
			Term<Object, Void, Object, Void, Void, Void, Void> lhs = eq0.second;
			Term<Object, Void, Object, Void, Void, Void, Void> rhs = eq0.third;
			
			Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>>
			 tr = new Triple<>(ctx2, lhs, rhs);
			return tr;
		}
		
	}