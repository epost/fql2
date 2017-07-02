package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;


public final class InstExpRaw extends InstExp<Object,Object,Object,Object,Object,Object,Object,ID,Chc<Object,Pair<ID,Object>>> {

	@Override
	public void asTree(DefaultMutableTreeNode root, boolean alpha) {
		if (imports.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("imports");
			for (Object t : Util.alphaMaybe(alpha, imports)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.toString());
				n.add(m);
			}
		}
		if (gens.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("gens");
			for (Pair<Object, Object> t : Util.alphaMaybe(alpha, gens)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " : " + t.second);
				n.add(m);
			}
			root.add(n);
		}
		if (eqs.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("eqs");
			for (Pair<RawTerm, RawTerm> t : Util.alphaMaybe(alpha, eqs)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + "=" + t.second);
				n.add(m);
			}
			root.add(n);
		}
		
	}
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(schema.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.INSTANCE)).collect(Collectors.toList()));
		return ret;
	}
 
	private final SchExp<Object,Object,Object,Object,Object> schema;
	
	private final List<String> imports;

	private final List<Pair<Object, Object>> gens; //TODO aql why is this object and not gens

	private final List<Pair<RawTerm, RawTerm>> eqs;
	
	private final Map<String, String> options;
	
	@Override
	public Map<String, String> options() {
		return options;
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
			
		List<String> temp = new LinkedList<>();
		
		if (!gens.isEmpty()) {
			toString += "\tgenerators";
					
			Map<Object, Set<Object>> n = Util.revS(Util.toMapSafely(gens));
			
			temp = new LinkedList<>();
			for (Object x : n.keySet()) {
				temp.add(Util.sep(n.get(x), " ") + " : " + x);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!eqs.isEmpty()) {
			toString += "\tequations";
			temp = new LinkedList<>();
			for (Pair<RawTerm, RawTerm> sym : eqs) {
				temp.add(sym.first + " = " + sym.second);
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
		
		return "literal : " + schema + " {\n" + toString + "}";
	} 


	@Override
	public int hashCode() {
		int prime = 31;
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
			col.eqs.addAll(v.eqs().stream().map(x -> new Eq<>(new Ctx<>(), x.first, x.second)).collect(Collectors.toList()));
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

		AqlOptions strat = new AqlOptions(options, col, env.defaults);
		InitialAlgebra<Object, Object, Object, Object, Object, Object, Object, ID> 
		initial = new InitialAlgebra<>(strat, sch, col, new It(), Object::toString, Object::toString);
				 
		return new LiteralInstance<>(sch, col.gens.map, col.sks.map, eqs0, initial.dp(), initial, (Boolean) strat.getOrDefault(AqlOption.require_consistency), (Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)); 
	}
	
	//TODO aql: schema eval should happen first, so can typecheck before running
	
	@Override
	public SchExp<Object, Object, Object, Object, Object> type(AqlTyping G) {
		return schema;
	}
	
}