package catdata.aql.exp;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Kind;
import catdata.aql.Mapping;
import catdata.aql.Schema;
import catdata.aql.fdm.ColimitSchema;

public abstract class ColimSchExp<N, E, Ty, En, Sym, Fk, Att> extends Exp<ColimitSchema<N, E, Ty, En, Sym, Fk, Att>> {

	@Override
	public Kind kind() {
		return Kind.SCHEMA_COLIMIT;
	}
	
	public abstract ColimSchExpLit<N, E, Ty, En, Sym, Fk, Att> type(AqlTyping G); 

	/////////////////////////////////////////////////////////////////
	
	public static final class ColimSchExpVar extends ColimSchExp<Object, Object, Object, Object, Object, Object, Object> {
		public final String var;
		
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
		
	
		@Override
		public long timeout() {
			return 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ColimSchExpLit<Object, Object, Object, Object, Object, Object, Object> type(AqlTyping G) {
			return (catdata.aql.exp.ColimSchExp.ColimSchExpLit<Object, Object, Object, Object, Object, Object, Object>) 
					G.defs.scs.get(var); 
		}

		@SuppressWarnings("unchecked")
		@Override
		public ColimitSchema<Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.defs.scs.get(var);
		}

		

		
	}
	
	////////////////////////////////////////

	public static class ColimSchExpLit<N, E, Ty, En, Sym, Fk, Att> extends ColimSchExp<N, E, Ty, En, Sym, Fk, Att> {
		public GraphExp<N, E> shape;

		public TyExp<Ty, Sym> ty;

		public Ctx<N, SchExp<Ty, En, Sym, Fk, Att>> nodes;

		public Ctx<E, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att>> edges;
		
		public Map<String, String> options;

		public ColimSchExpLit(GraphExp<N, E> shape, TyExp<Ty, Sym> ty, List<Pair<N, SchExp<Ty, En, Sym, Fk, Att>>> nodes, List<Pair<E, MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att>>> edges, List<Pair<String, String>> options) {
			this.shape = shape;
			this.ty = ty;
			this.nodes = new Ctx<>(Util.toMapSafely(nodes));
			this.edges = new Ctx<>(Util.toMapSafely(edges));
			this.options = Util.toMapSafely(options);
		}

		@Override
		public long timeout() {
			return 0;
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
			ColimSchExpLit<?, ?, ?, ?, ?, ?, ?> other = (ColimSchExpLit<?, ?, ?, ?, ?, ?, ?>) obj;
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
		public ColimitSchema<N, E, Ty, En, Sym, Fk, Att> eval(AqlEnv env) {
			Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes0 = new Ctx<>();
			for (N n : nodes.keySet()) {
				nodes0.put(n, nodes.get(n).eval(env));
			}
			Ctx<E, Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att>> edges0 = new Ctx<>();
			for (E e : edges.keySet()) {
				edges0.put(e, edges.get(e).eval(env));
			}
			return new ColimitSchema<>(shape.eval(env).dmg, ty.eval(env), nodes0, edges0, options);
		}

		@Override
		public ColimSchExpLit<N, E, Ty, En, Sym, Fk, Att> type(AqlTyping G) {
			return this;
		}
	}

}