package catdata.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.fql.decl.*;

/**
 * Directed labelled multi-graphs with nodes as pairs of vertices.  
 * 
 * @author serena
 *
 * @param <N> type of nodes
 * @param <E> type of edges
 */
public class PCGDMG<N,E> {
	
	public final Collection<Pair<N,N>> nodes;
	
	public final Map<WPEdge, Pair<Pair<N,N>, Pair<N,N>>> edges;
	//public final Collection<WPEdge> edges;
	
	public PCGDMG(Collection<Pair<N, N> > nodes, Map<WPEdge, Pair<Pair<N,N>, Pair<N,N>>> edges) {
		this.nodes = nodes;
		this.edges = edges;
		//validate();
	}
	
	/*
	public void validate() {
		for (PCGEdge e : edges) {
			if (!this.nodes.contains(e.name)) {
				throw new RuntimeException("Not a node: " + edges.get(e).first);
			}
			if (!this.nodes.contains(edges.get(e).second)) {
				throw new RuntimeException("Not a node: " + edges.get(e).second);
			}
		}
	}
	*/
	 
	/*
	public List<Pair<WPEdge, WPEdge>> edges(WPEdge src, WPEdge dst) {
		 // initial edge 
		List<WPEdge> ret = new LinkedList<>();
		Iterator<WPEdge> g = edges.iterator();
		//Pair<WPEdge, WPEdge> wpair = new Pair<WPEdge, WPEdge>(src, dst);
		//ret.add(wpair);
		while (g.hasNext()) {
			WPEdge nxt = g.next();
			Pair n1 = nxt.source;
			Pair n2 = nxt.target;
			N nn1 = (N) n1.first;
			N nn2 = (N) n1.second;
		}
		
		return ret;
	}
	*/
	
	/*
	public Pair<N,N> type(N src, List<E> path) {
		Util.assertNotNull(src, path);
		N dst = src;
		for (E e : path) {
			if (!edges.containsKey(e)) {
				throw new RuntimeException("Not an edge: " + e);
			}
			if (!dst.equals(edges.get(e).first)) {
				throw new RuntimeException("Ill-typed: " + path + ", edge " + e + " has source " + edges.get(e).first + " but is applied to " + src);
			}
			dst = edges.get(e).second;
		}		
		return new Pair<>(src, dst);
	}
	public final Collection<Pair<N,N>> nodes;
	public final Collection<Pair<WPEdge,WPEdge>> edges;
	*/
	
	public PCGDMG(Collection<N> nodes, Collection<WPEdge> edges, Set<Pair<N, N>> nodesset) {
		this.nodes = new HashSet<>();	
		this.edges = new HashMap<>();
		for (WPEdge e : edges) {
			//if (this.edges.containsKey(e.first)) {
				//throw new RuntimeException("Duplicate element: " + e.first);
			//}
			// make new pair of nodes 
			Pair<N,N> n1 = this.edges.get(e).first;
			Pair<N,N> n2 = this.edges.get(e).second;
			

			this.edges.put(e, (Pair<Pair<N, N>, Pair<N, N>>) new Pair<N,N>(n1.first, n1.second));
			this.edges.put(e, (Pair<Pair<N, N>, Pair<N, N>>) new Pair<N,N>(n2.first, n2.second));

		}
		//validate();
	}
	
	@Override
	public String toString() {
		List<String> l = new LinkedList<>();
		/*
		Iterator<Pair<WPEdge, WPEdge>> g = edges.iterator();
		while (g.hasNext()) {
			Pair<WPEdge, WPEdge> nxt = g.next();
			WPEdge e1 = nxt.first;
			WPEdge e2 = nxt.second;
			Pair n1 = e1.source;
			Pair n2 = e1.target;
			N nn1 = (N) n1.first;
			N nn2 = (N) n1.second;
			
		} */
		
		
		return "nodes\n\t" + Util.sep(nodes, "\n\t") + "\nedges\n\n" + Util.sep(l, "\n");
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
		PCGDMG<?,?> other = (PCGDMG<?,?>) obj;
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