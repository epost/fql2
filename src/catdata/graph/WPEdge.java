package catdata.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.fql.decl.Edge;
import catdata.fql.decl.Node;
/**
 * 
 * @author serena
 * 
 *         Class for wted Pairwise Conn. Graph edges in a signature.
 */
public class WPEdge<N> {

	public String toString() {
		return name + " : " + source + " -> " + target;
	}

	public String name;
	public Pair<N,N> source;
	public Pair<N,N> target;
	public double weight;

	public Object morphism;

	public WPEdge(Pair<N,N> nodes1, Pair<N,N> nodes2, String name, double weight) {
		this.source = nodes1;
		this.target = nodes2;
		this.name = name;
		this.weight = weight;
		if (source == null || target == null || name == null) {
			throw new RuntimeException();
		}
	}
	 

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Edge other = (Edge) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
	/*
	public String tojson() {
		String s = "{";
		s += "\"source\" : " + source.tojson();
		s += ", \"target\" : " + target.tojson();
		s += ", \"label\" : \"" + name + "\"}";
		return s;
	}
	*/

}
