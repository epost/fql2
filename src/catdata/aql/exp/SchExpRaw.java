package catdata.aql.exp;

import java.util.Collection;
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
import catdata.Program;
import catdata.Quad;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver;
import catdata.aql.AqlProver.ProverName;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;

public final class SchExpRaw extends SchExp<Object,Object,Object,Object,Object>  {
	
	
	@Override
	public void asTree(DefaultMutableTreeNode root) {
		if (imports.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("imports");
			for (Object t : imports) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.toString());
				n.add(m);
			}
		}
		if (ens.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("entities");
			for (Object t : ens) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.toString());
				n.add(m);
			}
			root.add(n);
		}
		if (fks.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("fks");
			for (Pair<Object, Pair<Object, Object>> t : fks) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " : " + t.second.first + "->" + t.second.second);
				n.add(m);
			}
			root.add(n);
		}
		if (atts.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("fks");
			for (Pair<Object, Pair<Object, Object>> t : atts) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " : " + t.second.first + "->" + t.second.second);
				n.add(m);
			}
			root.add(n);
		}
		if (p_eqs.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("path_eqs");
			for (Pair<List<Object>, List<Object>> t : p_eqs) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(Util.sep(t.first, ".") + "=" + Util.sep(t.second, "."));
				n.add(m);
			}
			root.add(n);
		}
		if (t_eqs.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("obs_eqs");
			for (Quad<String, Object, RawTerm, RawTerm> t : t_eqs) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.third + "=" + t.fourth);
				n.add(m);
			}
			root.add(n);
		}
	}
	
	public SchExp<Object,Object,Object,Object,Object> resolve(AqlTyping G, Program<Exp<?>> prog) {
	return this;
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.SCHEMA)).collect(Collectors.toList()));
		ret.addAll(typeSide.deps());
		return ret;
	}
	
	@Override
	public Map<String, String> options() {
		return options;
	}

	//TODO: aql printing of contexts broken when conitain choices
	
	@SuppressWarnings("unused")
	@Override
	public Schema<Object, Object, Object, Object, Object> eval(AqlEnv env) {
		TypeSide<Object, Object> ts = typeSide.eval(env);
		Collage<Object, Object, Object, Object, Object, Void, Void> col = new Collage<>(ts.collage());
		
		Set<Triple<Pair<Var, Object>, Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>> eqs0 = new HashSet<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Schema<Object, Object, Object, Object, Object> v = env.defs.schs.get(k);
			col.ens.addAll(v.ens);
			col.fks.putAll(v.fks.map);
			col.atts.putAll(v.atts.map);
			eqs0.addAll(v.eqs);
		}
		
		col.ens.addAll(ens);
		
		col.fks.putAll(Util.toMapSafely(fks));
		col.atts.putAll(Util.toMapSafely(atts));
		
		for (Quad<String, Object, RawTerm, RawTerm> eq : t_eqs) {
				Map<String, Chc<Object, Object>> ctx = Util.singMap(eq.first, eq.second == null ? null : Chc.inRight(eq.second));
				
				Triple<Ctx<String,Chc<Object,Object>>,Term<Object,Object,Object,Object,Object,Void,Void>,Term<Object,Object,Object,Object,Object,Void,Void>>
				eq0 = RawTerm.infer1(ctx, eq.third, eq.fourth, col, ts.js);
				
				//if (eq0.first.size() != 1) {
					//throw new RuntimeException("In " + eq.third + " = " + eq.fourth + ", there are either unbound variables or java primitives (without annotations), neither of which are not allowed"); 
				//}

				Chc<Object, Object> v = eq0.first.get(eq.first);
				if (v.left) {
					throw new RuntimeException("In " + eq.third + " = " + eq.fourth + ", variable " + eq.first + " has type " + v.l + " which is not an entity");
				}
				Object t = v.r;
			
				eqs0.add(new Triple<>(new Pair<>(new Var(eq.first), t), eq0.second, eq0.third));
		}
		
		for (Pair<List<Object>, List<Object>> eq : p_eqs) {
			String vv = "v";
			Var var = new Var(vv);
			
			Map<String, Chc<Object, Object>> ctx = Util.singMap(vv, null);
			
			RawTerm lhs = RawTerm.fold(col.fks.keySet(), col.ens, eq.first, vv);
			RawTerm rhs = RawTerm.fold(col.fks.keySet(), col.ens, eq.second,vv);
			
			Triple<Ctx<String,Chc<Object,Object>>,Term<Object,Object,Object,Object,Object,Void,Void>,Term<Object,Object,Object,Object,Object,Void,Void>>
			eq0 = RawTerm.infer1(ctx, lhs, rhs, col, ts.js);
		
			Chc<Object, Object> v = eq0.first.get(vv);
			if (v.left) {
				throw new RuntimeException("In " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".") + ", the equations source " + eq.first + " is type " + v.l + " which is not an entity");
			}
			Object t = v.r;
		
			if (eq0.first.size() != 1) {
				throw new RuntimeException("In " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".") + ", java constants cannot be used ");
			}

			eqs0.add(new Triple<>(new Pair<>(var, t), eq0.second, eq0.third));
		}
		for (Triple<Pair<Var, Object>, Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>> eq : eqs0) {
			col.eqs.add(new Eq<>(new Ctx<>(eq.first).inRight(), eq.second, eq.third));
		}
		
		AqlOptions strat = new AqlOptions(options, col, env.defaults);
		
		AqlOptions s = new AqlOptions(Util.singMap(AqlOption.prover.toString(), ProverName.fail.toString()), col, env.defaults);
		
		//forces type checking before prover construction
		new Schema<>(ts, col.ens, col.atts.map, col.fks.map, eqs0, AqlProver.create(s, col, ts.js), false);
		
		Schema<Object, Object, Object, Object, Object> ret = new Schema<>(ts, col.ens, col.atts.map, col.fks.map, eqs0, AqlProver.create(strat, col, ts.js), !((Boolean)strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)));
		return ret; 
		
	}

	
	
	//could be ? - don't think so
	private final TyExp<Object,Object> typeSide;
	
	private final List<String> imports;
	
	private final List<Object> ens;

	private final List<Pair<Object, Pair<Object, Object>>> fks;
	private final List<Pair<List<Object>, List<Object>>> p_eqs;

	private final List<Pair<Object, Pair<Object, Object>>> atts;
	private final List<Quad<String, Object, RawTerm, RawTerm>> t_eqs;
	
	private final Map<String, String> options;
	
		

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
		
		if (!ens.isEmpty()) {
			toString += "\tentities";
			toString += "\n\t\t" + Util.sep(ens, " ") + "\n";
		}
		
		List<String> temp = new LinkedList<>();
		
		if (!fks.isEmpty()) {
			toString += "\tforeign_keys";
			temp = new LinkedList<>();
			for (Pair<Object, Pair<Object, Object>> sym : fks) {
				temp.add(sym.first + " : " + sym.second.first + " -> " + sym.second.second);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!p_eqs.isEmpty()) {
			toString += "\tpath_equations";
			temp = new LinkedList<>();
			for (Pair<List<Object>, List<Object>> sym : p_eqs) {
				temp.add(Util.sep(sym.first, ".") + " = " + Util.sep(sym.second, ".") );
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!atts.isEmpty()) {
			toString += "\tattributes";
			temp = new LinkedList<>();
			for (Pair<Object, Pair<Object, Object>> sym : atts) {
				temp.add(sym.first + " : " + sym.second.first + " -> " + sym.second.second);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!t_eqs.isEmpty()) {
			toString += "\tobservation_equations";
			temp = new LinkedList<>();
			for (Quad<String, Object, RawTerm, RawTerm> sym : t_eqs) {
				temp.add("forall " + sym.first + ". " + sym.third + " = " + sym.fourth);
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
		
		return "literal : " + typeSide + " {\n" + toString + "}";
	} 

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((p_eqs == null) ? 0 : p_eqs.hashCode());
		result = prime * result + ((t_eqs == null) ? 0 : t_eqs.hashCode());
		result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
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
		SchExpRaw other = (SchExpRaw) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
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
		if (p_eqs == null) {
			if (other.p_eqs != null)
				return false;
		} else if (!p_eqs.equals(other.p_eqs))
			return false;
		if (t_eqs == null) {
			if (other.t_eqs != null)
				return false;
		} else if (!t_eqs.equals(other.t_eqs))
			return false;
		if (typeSide == null) {
			if (other.typeSide != null)
				return false;
		} else if (!typeSide.equals(other.typeSide))
			return false;
		return true;
	}

	//type safe by covariance of read only collections
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SchExpRaw(TyExp<?, ?> typeSide, List<String> imports, List<String> ens, List<Pair<String, Pair<String, String>>> fks, List<Pair<List<String>, List<String>>> p_eqs, List<Pair<String, Pair<String, String>>> atts, List<Quad<String, String, RawTerm, RawTerm>> t_eqs, List<Pair<String, String>> options) {
		this.typeSide = (TyExp<Object, Object>) typeSide;
		this.imports = imports;
		this.ens = new LinkedList<>(ens);
		this.fks = new LinkedList(fks);
		this.p_eqs = new LinkedList(p_eqs);
		this.atts = new LinkedList(atts);
		this.t_eqs = new LinkedList(t_eqs);
		this.options = Util.toMapSafely(options);
		Util.toMapSafely(fks); //check no dups here rather than wait until eval
		Util.toMapSafely(atts);	
	} 
	
	
	

}