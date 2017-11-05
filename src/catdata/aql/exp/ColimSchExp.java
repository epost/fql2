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

import catdata.Ctx;
import catdata.Pair;
import catdata.Quad;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.ColimitSchema;
import catdata.aql.Kind;
import catdata.aql.Mapping;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.exp.SchExp.SchExpVar;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

//TODO aql E shouldn't really be a type param here
public abstract class ColimSchExp<N> extends Exp<ColimitSchema<N>> {

	@Override
	public Kind kind() {
		return Kind.SCHEMA_COLIMIT;
	}
	
	public abstract SchExp<Ty, En, Sym, Fk, Att> getNode(N n, AqlTyping G);
	
	public abstract ColimSchExp<N> type(AqlTyping G); 

	/////////////////////////////////////////////////////////////////
	
	
	public static class ColimSchExpQuotient<N> 
	extends ColimSchExp<N> implements Raw {

		private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
		
		@Override 
		public Ctx<String, List<InteriorLabel<Object>>> raw() {
			return raw;
		}
		
		public final TyExp<Ty, Sym> ty;

		public final Ctx<N, SchExp<Ty, En, Sym, Fk, Att>> nodes;

		public final Set<Quad<N,En,N,En>> eqEn; 
		
		//TODO add separate path equations
		public final Set<Quad<String,String,RawTerm,RawTerm>> eqTerms; 
		
		public final Set<Pair<List<String>,List<String>>> eqTerms2; 
		
		@Override
		public Map<String, String> options() {
			return options;
		}
		
		public  Map<String, String> options;
		
		

		public ColimSchExpQuotient(TyExp<Ty, Sym> ty, List<LocStr> nodes, List<Pair<Integer, Quad<String, String, String, String>>> eqEn, List<Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> eqTerms, List<Pair<Integer, Pair<List<String>, List<String>>>> eqTerms2, List<Pair<String, String>> options) {
			this.ty = ty;
			this.nodes = new Ctx<>();
			this.eqEn = LocStr.proj2(eqEn).stream().map(x -> new Quad<>((N)x.first,new En(x.second),(N)x.third, new En(x.fourth))).collect(Collectors.toSet());
			this.eqTerms = LocStr.proj2(eqTerms);
			this.eqTerms2 = LocStr.proj2(eqTerms2);
			this.options = Util.toMapSafely(options);
			for (LocStr n : nodes) {
				if (this.nodes.containsKey((N)n.str)) {
					throw new RuntimeException("In schema colimit " + this + " duplicate schema " + n + " - please create new schema variable if necessary.");
				}
				this.nodes.put((N)n.str,  new SchExpVar<>(n.str));
			}
			
			List<InteriorLabel<Object>> f = new LinkedList<>();
			for (Pair<Integer, Quad<String, String, String, String>> p : eqEn) {
				f.add(new InteriorLabel<>("entities", p.second, p.first,
						x -> x.first + "." + x.second + " = " + x.third + "." + x.fourth).conv());
			}
			raw.put("entities", f);
			
			f = new LinkedList<>();
			for (Pair<Integer, Quad<String, String, RawTerm, RawTerm>> p : eqTerms) {
				f.add(new InteriorLabel<>("path eqs", p.second, p.first,
						x -> x.third + " = " + x.fourth).conv());
			}
			raw.put("path eqs", f);
	
			f = new LinkedList<>();
			for (Pair<Integer, Pair<List<String>, List<String>>> p : eqTerms2) {
				f.add(new InteriorLabel<>("obs eqs", p.second, p.first,
						x -> Util.sep(x.first, ".") + " = " + Util.sep(x.second, ".")).conv());
			}
			raw.put("obs eqs", f);
		}

	
		@Override
		public ColimitSchema<N> eval(AqlEnv env) {
			Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes0 = new Ctx<>();
			Set<En> ens = new HashSet<>();
			for (N n : nodes.keySet()) {
				nodes0.put(n, nodes.get(n).eval(env));
				ens.addAll(nodes0.get(n).ens.stream().map(x -> new En( n + "_" + x)).collect(Collectors.toSet()));
			}
			Set<Quad<String,String,RawTerm,RawTerm>> eqs = new HashSet<>(eqTerms);
			for (Pair<List<String>, List<String>> t : eqTerms2) {
				eqs.add(new Quad<>("_v0", null, tr(t.first, ens), tr(t.second, ens)));
			}
			return new ColimitSchema<>(ty.eval(env), nodes0, eqEn, eqs, new AqlOptions(options, null, env.defaults));		
		}
	
		private static RawTerm tr(List<String> l, Set<En> ens) {
			l = l.stream().filter(x -> !ens.contains(new En(x))).collect(Collectors.toList());
			return RawTerm.fold(l, "_v0");  
		}

		private String toString;
		
		@Override
		public synchronized String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";
			
			List<String> temp = new LinkedList<>();
			
			if (!eqEn.isEmpty()) {
				toString += "\tentity_equations";
						
				for (Quad<N, En, N, En> x : eqEn) {
					temp.add(x.first + "." + x.second + " = " + x.third + "." + x.fourth);
				}
				
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
			
			if (!eqTerms2.isEmpty()) {
				toString += "\tpath_equations";
						
				for (Pair<List<String>, List<String>> x : eqTerms2) {
					temp.add(Util.sep(x.first, ".") + " = " + Util.sep(x.second, "."));
				}
				
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
			
			if (!eqTerms.isEmpty()) {
				toString += "\tobservation_equations";
						
				for (Quad<String, String, RawTerm, RawTerm> x : eqTerms) {
					temp.add("forall " + x.first + ". " + x.third + " = " + x.fourth);
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
			if (eqEn.isEmpty() && eqTerms.isEmpty() && eqTerms2.isEmpty()) {
				toString = "coproduct " + Util.sep(nodes.keySet(), " + "); // + " {\n" + toString + "\n}";
				return toString;
			} else {
				toString = "quotient " + Util.sep(nodes.keySet(), " + ") + " {\n" + toString + "\n}";
				return toString;
			}
		} 

		@Override
		public Collection<Pair<String, Kind>> deps() {
			Set<Pair<String, Kind>> ret = new HashSet<>();
			ret.addAll(ty.deps());
			for (SchExp<Ty, En, Sym, Fk, Att> v : nodes.values()) {
				ret.addAll(v.deps());
			}
			return ret;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eqEn == null) ? 0 : eqEn.hashCode());
			result = prime * result + ((eqTerms == null) ? 0 : eqTerms.hashCode());
			result = prime * result + ((eqTerms2 == null) ? 0 : eqTerms2.hashCode());
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((ty == null) ? 0 : ty.hashCode());
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
			ColimSchExpQuotient<?> other = (ColimSchExpQuotient<?>) obj;
			if (eqEn == null) {
				if (other.eqEn != null)
					return false;
			} else if (!eqEn.equals(other.eqEn))
				return false;
			if (eqTerms == null) {
				if (other.eqTerms != null)
					return false;
			} else if (!eqTerms.equals(other.eqTerms))
				return false;
			if (eqTerms2 == null) {
				if (other.eqTerms2 != null)
					return false;
			} else if (!eqTerms2.equals(other.eqTerms2))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (ty == null) {
				if (other.ty != null)
					return false;
			} else if (!ty.equals(other.ty))
				return false;
			return true;
		}

		@Override
		public ColimSchExp<N> type(AqlTyping G) {
			return this;
		}

		@Override
		public SchExp<Ty, En, Sym, Fk, Att> getNode(N n, AqlTyping G) {
			return nodes.get(n);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////
	
	
	
	public static final class ColimSchExpVar<N> extends ColimSchExp<N> {
		public final String var;
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}

		@Override
		public SchExp<Ty, En, Sym, Fk, Att> getNode(N n, AqlTyping G) {
			return type(G).getNode(n, G);
		}
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.SCHEMA_COLIMIT));
		}
		
		public ColimSchExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
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
			ColimSchExpVar other = (ColimSchExpVar) obj;
			return var.equals(other.var);
		}

		@Override
		public String toString() {
			return var;
		}
		
	
			@SuppressWarnings("unchecked")
		@Override
		public ColimSchExp<N> type(AqlTyping G) {
			return 
					(ColimSchExp<N>) G.defs.scs.get(var); 
		}

		@SuppressWarnings("unchecked")
		@Override
		public ColimitSchema<N> eval(AqlEnv env) {
			return env.defs.scs.get(var);
		}

		

		
	}
	
	////////////////////////////////////////
	
	public static class ColimSchExpWrap<N> extends ColimSchExp<N> {

		
		
		public final ColimSchExp<N> colim;
		
		public final MapExp<Ty,En,Sym,Fk,Att,En,Fk,Att> toUser;
		
		public final MapExp<Ty,En,Sym,Fk,Att,En,Fk,Att> fromUser;
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		@Override
		public SchExp<Ty, En, Sym, Fk, Att> getNode(N n, AqlTyping G) {
			return colim.getNode(n, G);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((colim == null) ? 0 : colim.hashCode());
			result = prime * result + ((fromUser == null) ? 0 : fromUser.hashCode());
			result = prime * result + ((toUser == null) ? 0 : toUser.hashCode());
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
			ColimSchExpWrap<?> other = (ColimSchExpWrap<?>) obj;
			if (colim == null) {
				if (other.colim != null)
					return false;
			} else if (!colim.equals(other.colim))
				return false;
			if (fromUser == null) {
				if (other.fromUser != null)
					return false;
			} else if (!fromUser.equals(other.fromUser))
				return false;
			if (toUser == null) {
				if (other.toUser != null)
					return false;
			} else if (!toUser.equals(other.toUser))
				return false;
			return true;
		}

		

		public ColimSchExpWrap(ColimSchExp<N> colim, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att> toUser, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att> fromUser) {
			this.colim = colim;
			this.toUser = toUser;
			this.fromUser = fromUser;
		}

		@Override
		public ColimSchExp<N> type(AqlTyping G) {
			return colim.type(G);
		}

	

		@Override
		public ColimitSchema<N> eval(AqlEnv env) {
			return colim.eval(env).wrap(toUser.eval(env), fromUser.eval(env));
		}

		@Override
		public String toString() {
			return "wrap " + colim + " " + toUser + " " + fromUser;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(colim.deps(), Util.union(toUser.deps(), fromUser.deps()));
		}
		
		
	}
	
	////////////////////////////////////////

	public static class ColimSchExpRaw<N, E> extends ColimSchExp<N> implements Raw {
		
		
		private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
		
		@Override 
		public Ctx<String, List<InteriorLabel<Object>>> raw() {
			return raw;
		}
		
		public final GraphExp<N, E> shape;

		public final TyExp<Ty, Sym> ty;

		public final Ctx<N, SchExp<Ty, En, Sym, Fk, Att>> nodes;

		public final Ctx<E, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att>> edges;
		
	
		
		public final Map<String, String> options;
		
		@Override
		public Map<String, String> options() {
			return options;
		}
		
		@Override
		public SchExp<Ty, En, Sym, Fk, Att> getNode(N n, AqlTyping G) {
			return nodes.get(n);
		}

		public ColimSchExpRaw(GraphExp<N, E> shape, TyExp<Ty, Sym> ty, List<Pair<LocStr, SchExp<Ty, En, Sym, Fk, Att>>> nodes, List<Pair<LocStr, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att>>> edges, List<Pair<String, String>> options) {
			this.shape = shape;
			this.ty = ty;
			this.nodes = new Ctx<>(LocStr.list2(nodes, x -> (N) x));
			this.edges = new Ctx<>(LocStr.list2(edges, x -> (E) x));
			this.options = Util.toMapSafely(options);
			
			List<InteriorLabel<Object>> f = new LinkedList<>();
			for (Pair<LocStr, SchExp<Ty, En, Sym, Fk, Att>> p : nodes) {
				f.add(new InteriorLabel<>("nodes", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " -> " + x.second).conv());
			}
			raw.put("nodes", f);
			
			f = new LinkedList<>();
			for (Pair<LocStr, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att>> p : edges) {
				f.add(new InteriorLabel<>("edges", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " -> " + x.second).conv());
			}
			raw.put("edges", f);
			
		}

	

		@Override
		public String toString() {
			String ret = "literal " + shape + " : " + ty + " {";
			ret += "\n\tnodes\n\t\t";
			ret += Util.sep(nodes.map, " -> ", "\n\t\t");
			ret += "\n\tedges\n\t\t";
			ret += Util.sep(edges.map, " -> ", "\n\t\t");
			return ret + "\n}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((edges == null) ? 0 : edges.hashCode());
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((shape == null) ? 0 : shape.hashCode());
			result = prime * result + ((ty == null) ? 0 : ty.hashCode());
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
			ColimSchExpRaw<?, ?> other = (ColimSchExpRaw<?, ?>) obj;
			if (edges == null) {
				if (other.edges != null)
					return false;
			} else if (!edges.equals(other.edges))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (shape == null) {
				if (other.shape != null)
					return false;
			} else if (!shape.equals(other.shape))
				return false;
			if (ty == null) {
				if (other.ty != null)
					return false;
			} else if (!ty.equals(other.ty))
				return false;
			return true;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			Collection<Pair<String, Kind>> ret = new HashSet<>();
			for (SchExp<Ty, En, Sym, Fk, Att> k : nodes.values()) {
				ret.addAll(k.deps());
			}
			for (MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att> k : edges.values()) {
				ret.addAll(k.deps());
			}
			ret.addAll(shape.deps());
			ret.addAll(ty.deps());
			return ret;
		}

		@Override
		public ColimitSchema<N> eval(AqlEnv env) {
			Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes0 = new Ctx<>();
			for (N n : nodes.keySet()) {
				nodes0.put(n, nodes.get(n).eval(env));
			}
			Ctx<E, Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att>> edges0 = new Ctx<>();
			for (E e : edges.keySet()) {
				edges0.put(e, edges.get(e).eval(env));
			}
			return new ColimitSchema<>(shape.eval(env).dmg, ty.eval(env), nodes0, edges0, new AqlOptions(options, null, env.defaults));
		}

		@Override
		public ColimSchExpRaw<N,E> type(AqlTyping G) {
			return this;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
}