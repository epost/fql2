package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

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

//TODO: aql add shortcuts to editor

//TODO aql quoting (reuse example maker?)
public final class TyExpRaw extends TyExp<Object, Object> {
	
	@Override
	public void asTree(DefaultMutableTreeNode root, boolean alpha) {
		if (types.size() + java_tys_string.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("types");
			for (Object t : Util.alphaMaybe(alpha, types)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.toString());
				n.add(m);
			}
			for (Pair<Object, String> t : Util.alphaMaybe(alpha, java_tys_string)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first.toString());
				n.add(m);
			}
			root.add(n);
		}
		if (eqs.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("fns");
			for (Triple<List<Pair<String, Object>>, RawTerm, RawTerm> t : Util.alphaMaybe(alpha, eqs)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.second + "=" + t.third);
				n.add(m);
			}
			root.add(n);
		}
		

	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return imports.stream().map(x -> new Pair<>(x, Kind.TYPESIDE)).collect(Collectors.toSet());
	}

	private final Set<String> imports;
	private final Set<Object> types;
	private final Set<Pair<Object, Pair<List<Object>, Object>>> functions;
	private final Set<Triple<List<Pair<String, Object>>, RawTerm, RawTerm>> eqs;

	private final Set<Pair<Object, String>> java_tys_string;
	private final Set<Pair<Object, String>> java_parser_string;
	private final Set<Pair<Object, Triple<List<Object>, Object, String>>> java_fns_string;

	private final Map<String, String> options;
	//private final AqlOptions strat;
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	private final Set<Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>>> eqs0 = new HashSet<>();


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
		
	//	System.out.println(toString());
		
		col.tys.addAll(types);
		col.syms.putAll(Util.toMapSafely(this.functions));
		col.java_tys.putAll(Util.toMapSafely(this.java_tys_string));
		col.tys.addAll(col.java_tys.keySet());
		col.java_parsers.putAll(Util.toMapSafely(this.java_parser_string));
	//	System.out.println(this.java_fns_string);
		for (Entry<Object, Triple<List<Object>, Object, String>> kv : Util.toMapSafely(this.java_fns_string).entrySet()) {
			col.syms.put(kv.getKey(), new Pair<>(kv.getValue().first, kv.getValue().second));
			col.java_fns.put(kv.getKey(), kv.getValue().third);
		//	System.out.println(kv);
		}
	//	System.out.println("syms are " + col.syms.keySet());

	}

	@Override
	public int hashCode() {
		int prime = 31;
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

	private String toString;
	
	@Override
	public String toString() {
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
			toString += "\n\t\t" + Util.sep(types, " ") + "\n";
		}
		
		List<String> temp = new LinkedList<>();
		
		Map<Object, Object> m = new HashMap<>();
		temp = new LinkedList<>();
		for (Pair<Object, Pair<List<Object>, Object>> sym : functions) {
			if (sym.second.first.isEmpty()) {
				m.put(sym.first, sym.second.second);
			}
		}
		Map<Object, Set<Object>> n = Util.revS(m);

		if (!n.isEmpty()) {
			toString += "\tconstants";
	
			for (Object x : n.keySet()) {
				temp.add(Util.sep(n.get(x), " ") + " : " + x);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (functions.size() != n.size()) {
			toString += "\tfunctions";
			temp = new LinkedList<>();
			for (Pair<Object, Pair<List<Object>, Object>> sym : functions) {
				if (!sym.second.first.isEmpty()) {
					temp.add(sym.first + " : " + Util.sep(sym.second.first, ", ") + " -> " + sym.second.second);
				}
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!eqs.isEmpty()) {
			toString += "\tequations";
			temp = new LinkedList<>();
			for (Triple<List<Pair<String, Object>>, RawTerm, RawTerm> sym : eqs) {
				List<String> vars = Util.proj1(sym.first);
				temp.add("forall " + Util.sep(vars, ", ") + ". " + sym.second + " = " + sym.third);
			}
			toString +="\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!java_tys_string.isEmpty()) {
			toString += "\tjava_types";
			temp = new LinkedList<>();
			for (Pair<Object, String> sym : java_tys_string) {
				temp.add(sym.first + " = " + Util.quote(sym.second));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}		
		
		if (!java_parser_string.isEmpty()) {
			toString += "\tjava_constants";
			temp = new LinkedList<>();
			for (Pair<Object, String> sym : java_parser_string) {
				temp.add(sym.first + " = " + Util.quote(sym.second));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!java_fns_string.isEmpty()) {
			toString += "\tjava_functions";
			temp = new LinkedList<>();
			for (Pair<Object, Triple<List<Object>, Object, String>> sym : java_fns_string) {
				temp.add(sym.first + " : " + Util.sep(sym.second.first, ", ") + " -> " + sym.second.second + " = " + Util.quote(sym.second.third));
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!options.isEmpty()) {
			toString += "\toptions";
			temp = new LinkedList<>();
			for (Entry<String, String> sym : options.entrySet()) {
				temp.add(sym.getKey() + " = " + sym.getValue());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		return "literal {\n" + toString + "}";
	} 
	
	
	private final Collage<Object, Void, Object, Void, Void, Void, Void> col = new Collage<>();

	@Override
	public TypeSide<Object, Object> eval(AqlEnv env) {
		//defer equation checking since invokes javascript
		AqlJs<Object,Object> js = new AqlJs<>(col.syms, col.java_tys, col.java_parsers, col.java_fns);
		for (Triple<List<Pair<String, Object>>, RawTerm, RawTerm> eq : eqs) {
			Triple<Ctx<Var, Object>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> 
			tr = inferEq(col, eq, js);
			col.eqs.add(new Eq<>(tr.first.inLeft(), tr.second, tr.third));
			eqs0.add(tr);
		}

		AqlOptions strat = new AqlOptions(options, col, env.defaults);

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
		Triple<Ctx<String, Chc<Object, Void>>, Term<Object, Void, Object, Void, Void, Void, Void>, Term<Object, Void, Object, Void, Void, Void, Void>> 
		eq0 = RawTerm.infer1(ctx, eq.second, eq.third, col, js);

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