package catdata;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Wisnesky
 */
@SuppressWarnings("serial")
public class Chc<X,Y> implements Serializable {
	public final boolean left;
	
	public final X l;
	public final Y r;
	
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
			if (x.left) {
				throw new RuntimeException("Cannot projRight " + x);
			} else {
				ret.add(x.r);
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

	
	
	private Chc(Boolean left, X l, Y r) {
		this.left = left;
		this.l = l;
		this.r = r;
	}

	public static <X,Y> Chc<X,Y> inLeft(X l) {
		 if (l == null) {
			throw new RuntimeException();
		} 
		Chc<X,Y> ret = new Chc<>(true, l, null);
		return ret;
	}
	
	public static <X,Y> Chc<X,Y> inRight(Y r) {
		 if (r == null) {
			throw new RuntimeException();
		} 
		Chc<X,Y> ret = new Chc<>(false, null, r);
		return ret;
	}

	@Override
	public String toString() {
        return left ? "inl " + l : "inr " + r;
	}
	
	public String toStringMash() {
        return left ? l.toString() : r.toString();
	}
	
	

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + (left ? 1231 : 1237);
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
        if (left != other.left)
            return false;
        if (left) {
            return l.equals(other.l);
        }
        return r.equals(other.r);
    }

	public void assertNeitherNull() {
		if (l == null && r == null) {
			throw new RuntimeException("Assertion failed: Chc containing both null");
		}
	}
	
	
	
}