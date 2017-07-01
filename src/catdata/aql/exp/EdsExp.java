package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.Collage;
import catdata.aql.Constraints;
import catdata.aql.ED;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;

public abstract class EdsExp<Ty, En, Sym, Fk, Att> extends Exp<Constraints<Ty, En, Sym, Fk, Att>> {

	
	@Override
	public Kind kind() {
		return Kind.CONSTRAINTS;
	}

	public abstract SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G);

	public static class EdExpRaw {
		
		public void asTree(DefaultMutableTreeNode root) {
			if (As.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("forall");
				for (Pair<String, String> t : As) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + " : " + t.second);
					n.add(m);
				}
				root.add(n);
			}
			if (Awh.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("where");
				for (Pair<RawTerm, RawTerm> t : Awh) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + " = " + t.second);
					n.add(m);
				}
				root.add(n);
			}
			if (Es.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("exists");
				for (Pair<String, String> t : Es) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + " : " + t.second);
					n.add(m);
				}
				root.add(n);
			}
			if (Ewh.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("where");
				for (Pair<RawTerm, RawTerm> t : Ewh) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + " = " + t.second);
					n.add(m);
				}
				root.add(n);
			}
		}
		
		private String toString;
		@Override
		public synchronized String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";
		
			if (!As.isEmpty()) {
				toString += "\tforall";
				List<String> temp = new LinkedList<>();		
				for (Pair<String, String> p : As) {
					temp.add(p.first + ":" + p.second);
				}
				
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
			if (!Awh.isEmpty()) {
				toString += "\twhere";
				List<String> temp = new LinkedList<>();
				for (Pair<RawTerm, RawTerm> p : Awh) {
					temp.add(p.first + " = " + p.second);
				}
				
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
			toString += "->\n";
			if (!Es.isEmpty()) {
				toString += "\texists";
				if (isUnique) {
					toString += " unique";
				}
				List<String> temp = new LinkedList<>();		
				for (Pair<String, String> p : Es) {
					temp.add(p.first + ":" + p.second);
				}
				
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
			if (!Ewh.isEmpty()) {
				toString += "\twhere";
				List<String> temp = new LinkedList<>();
				for (Pair<RawTerm, RawTerm> p : Ewh) {
					temp.add(p.first + " = " + p.second);
				}
				
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
			return toString;
		}



		private final List<Pair<String, String>> As; 

		private final List<Pair<String, String>> Es; 
		
		private final List<Pair<RawTerm, RawTerm>> Awh;
		
		private final List<Pair<RawTerm, RawTerm>> Ewh;
		
		public final boolean isUnique;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((As == null) ? 0 : As.hashCode());
			result = prime * result + ((Awh == null) ? 0 : Awh.hashCode());
			result = prime * result + ((Es == null) ? 0 : Es.hashCode());
			result = prime * result + ((Ewh == null) ? 0 : Ewh.hashCode());
			result = prime * result + (isUnique ? 1231 : 1237);
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
			EdExpRaw other = (EdExpRaw) obj;
			if (As == null) {
				if (other.As != null)
					return false;
			} else if (!As.equals(other.As))
				return false;
			if (Awh == null) {
				if (other.Awh != null)
					return false;
			} else if (!Awh.equals(other.Awh))
				return false;
			if (Es == null) {
				if (other.Es != null)
					return false;
			} else if (!Es.equals(other.Es))
				return false;
			if (Ewh == null) {
				if (other.Ewh != null)
					return false;
			} else if (!Ewh.equals(other.Ewh))
				return false;
			if (isUnique != other.isUnique)
				return false;
			return true;
		}

		public EdExpRaw(List<Pair<String, String>> as, List<Pair<RawTerm, RawTerm>> awh, List<Pair<String, String>> es, List<Pair<RawTerm, RawTerm>> ewh, boolean isUnique) {
			As = as;
			Es = es;
			Awh = awh;
			Ewh = ewh;
			this.isUnique = isUnique;
		}

		public ED<Object, Object, Object, Object, Object> eval(Schema<Object, Object, Object, Object, Object> sch, AqlEnv env) {
			Pair<Ctx<Var, Object>, Set<Pair<Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>>> 
			x = eval1(sch, As, Awh);
		
			Pair<Ctx<Var, Object>, Set<Pair<Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>>> 
			y = eval1(sch, Util.append(As, Es), Ewh);
			
			for (Var k : x.first.keySet()) {
				y.first.remove(k);
			}
			return new ED<>(sch, x.first, y.first, x.second, y.second, isUnique, env.defaults);
		}



		private static Pair<Ctx<Var, Object>, Set<Pair<Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>>> eval1(Schema<Object, Object, Object, Object, Object> sch, List<Pair<String, String>> As, List<Pair<RawTerm, RawTerm>> Awh) {
			Ctx<Var, Object> As0 = new Ctx<>();
			Set<Pair<Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>> 
			Awh0 = new HashSet<>();
		
			Collage<Object, Object, Object, Object, Object, Object, Object> 
			col = new Collage<>(sch.collage());
			
			for (Pair<String, String> p : As) {
				String gen = p.first;
				String ty = p.second;
				if (col.ens.contains(ty)) {
					As0.put(new Var(gen), ty);
				} else {
					throw new RuntimeException("The sort for " + gen + ", namely " + ty + ", is not declared as an entity");
				}
			}
		
			for (Pair<RawTerm, RawTerm> eq : Awh) {
					Triple<Ctx<Var,Chc<Object,Object>>,Term<Object,Object,Object,Object,Object,Object,Object>,Term<Object,Object,Object,Object,Object,Object,Object>>
					eq0 =
					RawTerm.infer2(As, eq.first, eq.second, col, sch.typeSide.js);
							
					Awh0.add(new Pair<>(eq0.second.convert(), eq0.third.convert()));
			}

			return new Pair<>(As0, Awh0);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	public static class EdsExpRaw extends EdsExp<Object, Object, Object, Object, Object> {


		@Override
		public void asTree(DefaultMutableTreeNode root) {
			if (eds.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("nodes");
				for (EdExpRaw t : eds) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(" ");
					t.asTree(m);
					n.add(m);
				}
				root.add(n);
			}
		}
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eds == null) ? 0 : eds.hashCode());
			result = prime * result + ((imports == null) ? 0 : imports.hashCode());
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
			EdsExpRaw other = (EdsExpRaw) obj;
			if (eds == null) {
				if (other.eds != null)
					return false;
			} else if (!eds.equals(other.eds))
				return false;
			if (imports == null) {
				if (other.imports != null)
					return false;
			} else if (!imports.equals(other.imports))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			Set<Pair<String, Kind>> ret = new HashSet<>();
			ret.addAll(schema.deps());
			ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.CONSTRAINTS)).collect(Collectors.toList()));
			return ret;
		}

		private final SchExp<Object, Object, Object, Object, Object> schema;

		private final List<String> imports;

		private final List<EdExpRaw> eds;

		
		@SuppressWarnings("unchecked")
		public EdsExpRaw(SchExp<?, ?, ?, ?, ?> schema, List<String> imports, List<EdExpRaw> eds) {
			this.schema = (SchExp<Object, Object, Object, Object, Object>) schema;
			this.imports = imports;
			this.eds = eds;
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
			List<String> l = eds.stream().map(x -> x.toString()).collect(Collectors.toList());
			toString += Util.sep(l, "\n\n");

			return "literal : " + schema + " {\n" + toString + "}";
		}

		@Override
		public Constraints<Object, Object, Object, Object, Object> eval(AqlEnv env) {
			Schema<Object, Object, Object, Object, Object> sch = schema.eval(env);
			Collection<ED<Object, Object, Object, Object, Object>> l = new LinkedList<>();
			for (String k : imports) {
				@SuppressWarnings("unchecked")
				Constraints<Object, Object, Object, Object, Object> v = env.defs.eds.get(k);
				l.addAll(v.eds);
			}
			for (catdata.aql.exp.EdsExp.EdExpRaw e : eds) {
				l.add(e.eval(sch, env));
			}

			return new Constraints<>(sch, l, env.defaults);

		}

		@Override
		public SchExp<Object, Object, Object, Object, Object> type(AqlTyping G) {
			return schema;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	public static final class EdsExpVar extends EdsExp<Object, Object, Object, Object, Object> {

		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		public final String var;

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.CONSTRAINTS));
		}

		public EdsExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Constraints<Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.defs.eds.get(var);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (var.hashCode());
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
			EdsExpVar other = (EdsExpVar) obj;
			return var.equals(other.var);
		}

		@Override
		public String toString() {
			return var;
		}

		

		@SuppressWarnings("unchecked")
		@Override
		public SchExp<Object, Object, Object, Object, Object> type(AqlTyping G) {
			if (!G.defs.eds.containsKey(var)) {
				throw new RuntimeException("Not constraints: " + var);
			}
			return (SchExp<Object, Object, Object, Object, Object>) ((Object) G.defs.eds.get(var));
		}
	}
}
