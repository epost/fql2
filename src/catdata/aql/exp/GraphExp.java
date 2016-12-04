package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Util;
import catdata.graph.DMG;

public abstract class GraphExp<N,E> extends Exp<DMG<N,E>> {

	@Override
	public Kind kind() {
		return Kind.GRAPH;
	}
	
	public abstract DMG<N,E> eval(AqlTyping G);
	
	@Override
	public DMG<N,E> eval(AqlEnv env) { 
		DMG<N,E> ret = eval(env.typing);
		if (ret == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return ret;
	}
	
	@Override
	public long timeout() {
		return 0;
	}

	////////////////////////////

	public static class GraphExpRaw extends GraphExp<Object,Object> {
		
		

		public Set<Object> nodes;
		public Map<Object,Pair<Object,Object>> edges;
		
		public List<String> imports;

		@SuppressWarnings({ "unchecked" })
		public GraphExpRaw(List<String> nodes, List<Pair<String, Pair<String, String>>> edges, List<String> imports) {
			this.nodes = new HashSet<>(Util.toSetSafely(nodes));
			this.edges = (Map<Object,Pair<Object,Object>>) ((Object)Util.toMapSafely(edges));
			this.imports = imports;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((edges == null) ? 0 : edges.hashCode());
			result = prime * result + ((imports == null) ? 0 : imports.hashCode());
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
			GraphExpRaw other = (GraphExpRaw) obj;
			if (edges == null) {
				if (other.edges != null)
					return false;
			} else if (!edges.equals(other.edges))
				return false;
			if (imports == null) {
				if (other.imports != null)
					return false;
			} else if (!imports.equals(other.imports))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			return true;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public DMG<Object, Object> eval(AqlTyping G) {
			for (String s : imports) {
				nodes.addAll(G.defs.gs.get(s).nodes);
				edges.putAll((Map)G.defs.gs.get(s).edges);
			}
			return new DMG<>(nodes, edges);
		}

		@Override
		public String toString() {
			return "GraphExpRaw [nodes=" + nodes + ", edges=" + edges + "]";
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return imports.stream().map(x -> new Pair<>(x, Kind.GRAPH)).collect(Collectors.toSet());
		}
	}

	
	
	////////////////////////////
	
	public static class GraphExpLiteral<N,E> extends GraphExp<N,E> {

		public final DMG<N, E> graph;
		
		public GraphExpLiteral(DMG<N, E> graph) {
			Util.assertNotNull(graph);
			this.graph = graph;
		}

		@Override
		public DMG<N, E> eval(AqlTyping env) {
			return graph;
		}

		@Override
		public String toString() {
			return graph.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((graph == null) ? 0 : graph.hashCode());
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
			GraphExpLiteral<?,?> other = (GraphExpLiteral<?,?>) obj;
			if (graph == null) {
				if (other.graph != null)
					return false;
			} else if (!graph.equals(other.graph))
				return false;
			return true;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		
	}

	//////////////
	
	public static final class GraphExpVar extends GraphExp<Object, Object> {
		public final String var;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.INSTANCE));
		}
		
		public GraphExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public DMG<Object, Object> eval(AqlTyping env) {
			return (DMG<Object, Object>) env.defs.gs.get(var);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((var == null) ? 0 : var.hashCode());
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
			GraphExpVar other = (GraphExpVar) obj;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return var;
		}
		
	}
	
	
}
