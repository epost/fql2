package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlJs;
import catdata.aql.AqlOptions;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

//TODO aql quoting (reuse example maker?)
public final class TyExpRaw extends TyExp<Ty, Sym> implements Raw {
	
	public static class Ty implements Comparable<Ty> {
		public final String str;

		public Ty(String str) {
			Util.assertNotNull(str);

			this.str = str;
		}

		@Override
		public int hashCode() {
			return str.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Ty)) {
				Util.anomaly();
			}
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Ty))
				return false;
			Ty other = (Ty) obj;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public int compareTo(Ty o) {
			if (!(o instanceof Ty)) {
				Util.anomaly();
			}
			return str.compareTo(o.str);
		}
		
	}
	
	public static class Sym implements Comparable<Sym> {
		public final String str;

		public Sym(String str) {
			Util.assertNotNull(str);
			this.str = str;
		}

		@Override
		public int hashCode() {
			return str.hashCode(); //must work with compareTo - cant use auto gen one
		} 

		@Override
		public int compareTo(Sym o) {
			if (!(o instanceof Sym)) {
				Util.anomaly();
			}
			return str.compareTo(o.str);
		}
		
		@Override
		public boolean equals(Object obj) {
			//if (!(obj instanceof Sym)) {
			//	Util.anomaly();
		//	}
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Sym))
				return false;
			Sym other = (Sym) obj;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		} 

		@Override
		public String toString() {
			return str;
		}

	}

	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return imports.stream().map(x -> new Pair<>(x, Kind.TYPESIDE)).collect(Collectors.toSet());
	}

	public final Set<String> imports;
	public final Set<String> types;
	public final Set<Pair<String, Pair<List<String>, String>>> functions;
	public final Set<Triple<List<Pair<String, String>>, RawTerm, RawTerm>> eqs;

	public final Set<Pair<String, String>> java_tys;
	public final Set<Pair<String, String>> java_parser;
	public final Set<Pair<String, Triple<List<String>, String, String>>> java_fns;

	public final Map<String, String> options;
	
	private final Collage<Ty, Void, Sym, Void, Void, Void, Void> col = new Collage<>();


	@Override
	public Map<String, String> options() {
		return options;
	}

//	private final Set<Triple<Ctx<Var, String>, Term<String, Void, String, Void, Void, Void, Void>, Term<String, Void, String, Void, Void, Void, Void>>> eqs0 = new HashSet<>();

	public TyExpRaw(List<LocStr> imports, List<LocStr> types, List<Pair<LocStr, Pair<List<String>, String>>> functions,
			List<Pair<Integer, Triple<List<Pair<String, String>>, RawTerm, RawTerm>>> eqsX,
			List<Pair<LocStr, String>> java_tys_string, List<Pair<LocStr, String>> java_parser_string,
			List<Pair<LocStr, Triple<List<String>, String, String>>> java_fns_string,
			List<Pair<String, String>> options) {
		this.imports = LocStr.set1(imports);
		this.types = LocStr.set1(types);
		this.functions = LocStr.functions1(functions);
		this.eqs = LocStr.eqs1(eqsX);
		this.java_tys = LocStr.set2(java_tys_string);
		this.java_parser = LocStr.set2(java_parser_string);
		this.java_fns = LocStr.functions2(java_fns_string);
		this.options = Util.toMapSafely(options);

		col.tys.addAll(this.types.stream().map(x -> new Ty(x)).collect(Collectors.toList()));
		col.syms.putAll(conv1(Util.toMapSafely(this.functions)));
		col.java_tys.putAll(conv3(Util.toMapSafely(this.java_tys)));
		col.tys.addAll(col.java_tys.keySet());
		col.java_parsers.putAll(conv3(Util.toMapSafely(this.java_parser)));
		for (Entry<String, Triple<List<String>, String, String>> kv : Util.toMapSafely(this.java_fns)
				.entrySet()) {
			List<Ty> l1 = kv.getValue().first.stream().map(x -> new Ty(x)).collect(Collectors.toList());
			col.syms.put(new Sym(kv.getKey()), new Pair<>(l1, new Ty(kv.getValue().second)));
			col.java_fns.put(new Sym(kv.getKey()), kv.getValue().third);
		}
		
		// changed my mind: do not defer equation checking since invokes javascript
		//col.
		AqlJs<Ty, Sym> js = new AqlJs<>(col.syms, col.java_tys, col.java_parsers, col.java_fns);
		
		for (Triple<List<Pair<String, String>>, RawTerm, RawTerm> eq : eqs) {
			try {
				Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> 
				tr = infer1x(yyy(eq.first), eq.second, eq.third, null, col, "", js);
				col.eqs.add(new Eq<>(tr.first, tr.second, tr.third));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("equations", eq), "In equation " + eq.second + " = " + eq.third + ", " + ex.getMessage());
			}

		}

		doGuiIndex(imports, types, functions, eqsX, java_tys_string, java_parser_string, java_fns_string);

	}

	private Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> infer1x(
		Map<String, Chc<Ty, En>> ctx0, RawTerm e0, RawTerm f, Chc<Ty, Void> expected,
		Collage col, String pre, AqlJs<Ty, Sym> js) {
	return RawTerm.infer1x(ctx0, e0, f, (Chc<Ty,En>)((Object)expected), col, pre, js).first3();
}

	private Map<Ty, String> conv3(Map<String, String> m) {
		return Util.map(m, (x,y) -> new Pair<>(new Ty(x), y));
	}

	
	private Pair<List<Ty>, Ty> conv2(List<String> k, String v) {
		List<Ty> l1 = k.stream().map(x -> new Ty(x)).collect(Collectors.toList());
		return new Pair<>(l1, new Ty(v));
	}
	
	private Map<Sym, Pair<List<Ty>, Ty>> conv1(Map<String, Pair<List<String>, String>> m) {
		return Util.map(m, (k,v) ->  new Pair<>(new Sym(k), conv2(v.first, v.second)));		
	}

	public void doGuiIndex(List<LocStr> imports, List<LocStr> types,
			List<Pair<LocStr, Pair<List<String>, String>>> functions,
			List<Pair<Integer, Triple<List<Pair<String, String>>, RawTerm, RawTerm>>> eqs,
			List<Pair<LocStr, String>> java_tys_string, List<Pair<LocStr, String>> java_parser_string,
			List<Pair<LocStr, Triple<List<String>, String, String>>> java_fns_string) {
		List<InteriorLabel<Object>> i = InteriorLabel.imports("imports", imports);
		raw.put("imports", i);
		List<InteriorLabel<Object>> t = InteriorLabel.imports("types", types);
		raw.put("types", t);

		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, Pair<List<String>, String>> p : functions) {
			f.add(new InteriorLabel<>("functions", new Triple<>(p.first.str, p.second.first, p.second.second),
					p.first.loc,
					x -> x.first + " : " + Util.sep(x.second, ",") + (x.second.isEmpty() ? "" : " -> ") + x.third)
							.conv());
		}
		raw.put("functions", f);

		List<InteriorLabel<Object>> e = new LinkedList<>();
		for (Pair<Integer, Triple<List<Pair<String, String>>, RawTerm, RawTerm>> p : eqs) {
			e.add(new InteriorLabel<>("equations", p.second, p.first, x -> x.second + " = " + x.third).conv());
		}
		raw.put("equations", e);

		List<InteriorLabel<Object>> jt = new LinkedList<>();
		raw.put("java_types", jt);
		for (Pair<LocStr, String> p : java_tys_string) {
			jt.add(new InteriorLabel<>("java_types", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " = " + x.second).conv());
		}

		List<InteriorLabel<Object>> jc = new LinkedList<>();
		for (Pair<LocStr, String> p : java_parser_string) {
			jc.add(new InteriorLabel<>("java_constants", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " = " + x.second).conv());
		}
		raw.put("java_constants", jc);

		List<InteriorLabel<Object>> jf = new LinkedList<>();
		raw.put("java_functions", jf);
		for (Pair<LocStr, Triple<List<String>, String, String>> p : java_fns_string) {
			jf.add(new InteriorLabel<>("java_functions", new Triple<>(p.first.str, p.second.first, p.second.second),
					p.first.loc, x -> x.first + " : " + Util.sep(x.second, ",") + " -> " + x.third).conv());
		}
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((functions == null) ? 0 : functions.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((java_fns == null) ? 0 : java_fns.hashCode());
		result = prime * result + ((java_parser == null) ? 0 : java_parser.hashCode());
		result = prime * result + ((java_tys == null) ? 0 : java_tys.hashCode());
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
		if (java_fns == null) {
			if (other.java_fns != null)
				return false;
		} else if (!java_fns.equals(other.java_fns))
			return false;
		if (java_parser == null) {
			if (other.java_parser != null)
				return false;
		} else if (!java_parser.equals(other.java_parser))
			return false;
		if (java_tys == null) {
			if (other.java_tys != null)
				return false;
		} else if (!java_tys.equals(other.java_tys))
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

	private String toString;

	@Override
	public synchronized String toString() {
		if (toString != null) {
			return toString;
		}
		toString = "";

		if (!imports.isEmpty()) {
			toString += "\timports";
			toString += "\n\t\t" + Util.sep(imports, " ") + "\n";
		}

		if (!types.isEmpty()) {
			toString += "\ttypes";
			toString += "\n\t\t" + Util.sep(Util.alphabetical(types), " ") + "\n";
		}

		List<String> temp = new LinkedList<>();

		Map<Object, Object> m = new HashMap<>();
		temp = new LinkedList<>();
		for (Pair<String, Pair<List<String>, String>> sym : Util.alphabetical(functions)) {
			if (sym.second.first.isEmpty()) {
				m.put(sym.first, sym.second.second);
			}
		}
		Map<Object, Set<Object>> n = Util.revS(m);

		if (!n.isEmpty()) {
			toString += "\tconstants";

			for (Object x : Util.alphabetical(n.keySet())) {
				temp.add(Util.sep(n.get(x), " ") + " : " + x);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (functions.size() != n.size()) {
			toString += "\tfunctions";
			temp = new LinkedList<>();
			for (Pair<String, Pair<List<String>, String>> sym : Util.alphabetical(functions)) {
				if (!sym.second.first.isEmpty()) {
					temp.add(sym.first + " : " + Util.sep(sym.second.first, ", ") + " -> " + sym.second.second);
				}
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!eqs.isEmpty()) {
			toString += "\tequations";
			temp = new LinkedList<>();
			for (Triple<List<Pair<String, String>>, RawTerm, RawTerm> sym : Util.alphabetical(eqs)) {
				List<String> vars = Util.proj1(sym.first);
				temp.add("forall " + Util.sep(vars, ", ") + ". " + sym.second + " = " + sym.third);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!java_tys.isEmpty()) {
			toString += "\tjava_types";
			temp = new LinkedList<>();
			for (Pair<String, String> sym : Util.alphabetical(java_tys)) {
				temp.add(sym.first + " = " + Util.quote(sym.second));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!java_parser.isEmpty()) {
			toString += "\tjava_constants";
			temp = new LinkedList<>();
			for (Pair<String, String> sym : Util.alphabetical(java_parser)) {
				temp.add(sym.first + " = " + Util.quote(sym.second));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		Function<List<String>, String> fff = x -> x.isEmpty() ? "" : (Util.sep(x, ", ") + " -> ");
		if (!java_fns.isEmpty()) {
			toString += "\tjava_functions";
			temp = new LinkedList<>();
			for (Pair<String, Triple<List<String>, String, String>> sym : Util.alphabetical(java_fns)) {
				temp.add(sym.first + " : " + fff.apply(sym.second.first) + sym.second.second + " = "
						+ Util.quote(sym.second.third));
			}

			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!options.isEmpty()) {
			toString += "\toptions";
			temp = new LinkedList<>();
			for (Entry<String, String> sym : Util.alphabetical(options.entrySet())) {
				temp.add(sym.getKey() + " = " + sym.getValue());
			}

			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		return "literal {\n" + toString + "}";
	}


	@Override
	public synchronized TypeSide<Ty, Sym> eval(AqlEnv env) {
		AqlOptions ops = new AqlOptions(options, col, env.defaults);

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			TypeSide<Ty, Sym> v = env.defs.tys.get(k);
			col.addAll(v.collage());
		}
		
		Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> 
		eqs0 = col.eqsAsTriples().stream().map(x -> new Triple<>(xxx(x.first),x.second,x.third)).collect(Collectors.toSet());
	
		TypeSide<Ty, Sym> ret = new TypeSide<Ty, Sym>(col.tys, col.syms.map, eqs0, col.java_tys.map,
					col.java_parsers.map, col.java_fns.map, ops);

			return ret;
	}

	private static Ctx<Var, Ty> xxx(Ctx<Var, Chc<Ty,Void>> x) {
		return x.map((k,v)->new Pair<>(k,v.l));
	}
	
	private static Map<String, Chc<Ty,En>> yyy(List<Pair<String, String>> l) {
		Map<String, Chc<Ty,En>> ret = new HashMap<>();
		for (Pair<String, String> p : l) {
			if (ret.containsKey(p.first)) {
				throw new RuntimeException("Duplicate bound variable: " + p.first);
			}
			Chc<Ty, En> x = p.second == null ? null : Chc.inLeft(new Ty(p.second));
			ret.put(p.first, x);
		}
		return ret; 
	}

	
	
}