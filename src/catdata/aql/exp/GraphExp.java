package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.Graph;
import catdata.aql.Kind;
import catdata.graph.DMG;

public abstract class GraphExp<N,E> extends Exp<Graph<N,E>> {

	@Override
	public Kind kind() {
		return Kind.GRAPH;
	}
	
	public abstract Graph<N,E> eval(AqlTyping G);
	
	@Override
	public Graph<N,E> eval(AqlEnv env) { 
		Graph<N,E> ret = eval(env.typing);
		if (ret == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return ret;
	}
	
	////////////////////////////

	public static class GraphExpRaw extends GraphExp<String,String> implements Raw {
		
		private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
		
		@Override
		public Ctx<String, List<InteriorLabel<Object>>> raw() {
			return raw;
		} 
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}

		public final Set<String> nodes;
		public final Map<String,Pair<String,String>> edges;
		
		public final Set<String> imports;

		public GraphExpRaw(List<LocStr> nodes, List<Pair<LocStr, Pair<String, String>>> edges, List<LocStr> imports) {
			this.nodes = LocStr.set1(nodes);
			this.edges = Util.toMapSafely(LocStr.set2(edges));
			this.imports = LocStr.set1(imports);
			
			List<InteriorLabel<Object>> t = InteriorLabel.imports( "imports", imports);
			raw.put("imports", t);
			
			t = InteriorLabel.imports( "nodes", nodes);
			raw.put("nodes", t);
			
			List<InteriorLabel<Object>> f = new LinkedList<>();
			for (Pair<LocStr, Pair<String, String>> p : edges) {
				f.add(new InteriorLabel<>("edges", new Triple<>(p.first.str, p.second.first, p.second.second), p.first.loc,
						x -> x.first + " : " + x.second + " -> " + x.third).conv());
			}
			raw.put("edges", f);
		}

		@Override
		public int hashCode() {
			int prime = 31;
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
		public Graph<String, String> eval(AqlTyping G) {
			for (String s : imports) {
				nodes.addAll((Collection<String>)G.defs.gs.get(s).nodes);
				edges.putAll((Map)G.defs.gs.get(s).edges);
			}
			return new Graph<>(new DMG<>(nodes, edges));
		}

		@Override
		public String toString() {
			List<String> l = new LinkedList<>();
			for (Object e  : edges.keySet()) {
				l.add(e + ": " + edges.get(e).first + " -> " + edges.get(e).second);
			}
			return "literal {\n\tnodes\n\t\t" + Util.sep(nodes, " ") + "\n\tedges\n\t\t" + Util.sep(l, "\n\t\t") + "\n}";
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return imports.stream().map(x -> new Pair<>(x, Kind.GRAPH)).collect(Collectors.toSet());
		}
	}

	
	
	////////////////////////////
	
	public static class GraphExpLiteral<N,E> extends GraphExp<N,E> {

		public final DMG<N, E> graph;
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		public GraphExpLiteral(DMG<N, E> graph) {
			Util.assertNotNull(graph);
			this.graph = graph;
		}

		@Override
		public Graph<N, E> eval(AqlTyping env) {
			return new Graph<>(graph);
		}

		@Override
		public String toString() {
			return graph.toString();
		}

		@Override
		public int hashCode() {
			int prime = 31;
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
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.INSTANCE));
		}
		
		public GraphExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Graph<Object, Object> eval(AqlTyping env) {
			return new Graph(env.defs.gs.get(var));
		}

		@Override
		public int hashCode() {
			int prime = 31;
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
