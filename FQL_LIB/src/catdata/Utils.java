package catdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Utils {

	public static String sep(Collection<?> c, String sep) {
		return sep(c.iterator(), sep);
	}

	public static String sep(Iterator<?> c, String sep) {
		String ret = "";
		boolean b = false;
		while (c.hasNext()) {
			Object o = c.next();
			if (b) {
				ret += sep;
			}
			b = true;

			ret += o;
		}
		return ret;
	} 
	
	public static <X,Y> boolean isBijection(Map<X, Y> m, Set<X> X, Set<Y> Y) {
		if (!m.keySet().equals(X)) {
			return false;
		}
		if (!new HashSet<>(m.values()).equals(Y)) {
			return false;
		}
		Map<Y,X> n = rev(m, Y);
		if (n == null) {
			return false;
		}
		
		Map<X,X> a = compose0(m, n);
		Map<Y,Y> b = compose0(n, m);
		
		if (!a.equals(id(X))) {
			return false;
		}
		if (!b.equals(id(Y))) {
			return false;
		}
		
		return true;
	}
	
	
	
	public static <X> Map<X, X> id(Collection<X> X) {
		Map<X, X> ret = new HashMap<>();
		for (X x : X) {
			ret.put(x, x);
		}
		return ret;
	}
	
	public static <X,Y> X rev(Map<X, Y> m, Y y) {
		X x = null;
		for (X x0 : m.keySet()) {
			Y y0 = m.get(x0);
			if (y0.equals(y)) {
				if (x != null) {
					return null;
				}
				x = x0;
			}
		}
		return x;
	}

	public static <A, B, C> Map<A, C> compose0(Map<A, B> x, Map<B, C> y) {
		Map<A, C> ret = new HashMap<>();

		for (Entry<A,B> a : x.entrySet()) {
			ret.put(a.getKey(), y.get(a.getValue()));
		}
		
		return ret;
	}
	
	public static <X,Y> Map<Y,X> rev(Map<X, Y> m, Set<Y> Y) {
		Map<Y,X> ret = new HashMap<>();
		
		for (Y y : Y) {
			X x = rev(m, y);
			if (x == null) {
				return null;
			}
			ret.put(y, x);
		}
		
		return ret;
	}
	
	public static <X> List<X> append(List<X> x, List<X> y) {
		List<X> ret = new LinkedList<>(x);
		ret.addAll(y);
		return ret;
	}
}
