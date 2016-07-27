package catdata;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Wisnesky
 */
public class Chc<X,Y> {
	public Boolean left;
	
	public X l;
	public Y r;
	
	public static <X,Y> Set<X> projIfAllLeft(Set<Chc<X,Y>> set) {
		Set<X> ret = new HashSet<>();
		
		for (Chc<X, Y> x : set) {
			if (x.left) {
				ret.add(x.l);
			} else {
				throw new RuntimeException("Cannot projLeft " + x);
			}
		}
		
		return ret;
	}
	
	public static <X,Y> Set<Y> projIfAllRight(Set<Chc<X,Y>> set) {
		Set<Y> ret = new HashSet<>();
		
		for (Chc<X, Y> x : set) {
			if (!x.left) {
				ret.add(x.r);
			} else {
				throw new RuntimeException("Cannot projRight " + x);
			}
		}
		
		return ret;
	}
	
	public static <X,Y> List<Chc<X,Y>> inLeft(List<X> l) {
		List<Chc<X,Y>> ret = new LinkedList<>();
		
		for (X x : l) {
			ret.add(inLeft(x));
		}
		
		return ret;
	}
	
	public static <X,Y> List<Chc<Y,X>> inRight(List<X> l) {
		List<Chc<Y,X>> ret = new LinkedList<>();
		
		for (X x : l) {
			ret.add(inRight(x));
		}
		
		return ret;
	}

	
	public static <X,Y> Chc<X,Y> inLeft(X l) {
		Chc<X,Y> ret = new Chc<>();
		ret.left = true;
		ret.l = l;
		return ret;
	}
	
	public static <X,Y> Chc<X,Y> inRight(Y r) {
		Chc<X,Y> ret = new Chc<>();
		ret.left = false;
		ret.r = r;
		return ret;
	}

	public String toString() {
		if (left) {
			return "inl " + l;
		} else {
			return "inr " + r;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((r == null) ? 0 : r.hashCode());
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
		Chc<?,?> other = (Chc<?,?>) obj;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		return true;
	}
	
}