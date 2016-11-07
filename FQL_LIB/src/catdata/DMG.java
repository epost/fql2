package catdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DMG<N,E> {
	public final Collection<N> nodes;
	public final Map<E, Pair<N,N>> edges;
	
	public DMG(Collection<N> nodes, Map<E, Pair<N,N>> edges) {
		this.nodes = nodes;
		this.edges = edges;
		for (E e : edges.keySet()) {
			if (!this.nodes.contains(edges.get(e).first)) {
				throw new RuntimeException("Not a node: " + edges.get(e).first);
			}
			if (!this.nodes.contains(edges.get(e).second)) {
				throw new RuntimeException("Not a node: " + edges.get(e).second);
			}
		}
	}
	
	public DMG(Collection<N> nodes, Set<Triple<E, N, N>> edges) {
		this.nodes = new HashSet<>(nodes);	
		this.edges = new HashMap<>();
		for (Triple<E, N, N> e : edges) {
			if (this.edges.containsKey(e.first)) {
				throw new RuntimeException("Duplicate element: " + e.first);
			}
			if (!this.nodes.contains(e.second)) {
				throw new RuntimeException("Not a node: " + e.second);
			}
			if (!this.nodes.contains(e.third)) {
				throw new RuntimeException("Not a node: " + e.third);
			}
			this.edges.put(e.first, new Pair<>(e.second, e.third));
		}
	}
	
	@Override
	public String toString() {
		List<String> l = new LinkedList<>();
		for (E e  : edges.keySet()) {
			l.add(e + ": " + edges.get(e).first + " -> " + edges.get(e).second);
		}
		return "nodes\n\t" + Util.sep(nodes, " ") + "\nedges\n\t" + Util.sep(l, "\n\t");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
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
		DMG<?,?> other = (DMG<?,?>) obj;
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
		return true;
	}
	
}