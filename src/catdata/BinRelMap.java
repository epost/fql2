package catdata;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import catdata.aql.Lineage;
import catdata.aql.Term;

public class BinRelMap<X, Y> implements Iterable<Pair<X, Y>> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((R == null) ? 0 : R.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BinRelMap))
			return false;
		BinRelMap other = (BinRelMap) obj;
		if (R == null) {
			if (other.R != null)
				return false;
		} else if (!R.equals(other.R))
			return false;
		return true;
	}

	// does not allow empty sets
	public class It implements Iterator<Pair<X, Y>> {

		Iterator<X> xIt;
		X x;
		Iterator<Y> yIt;
		Set<Y> ys;

		public It() {
			xIt = R.keySet().iterator();
		}

		@Override
		public void remove() {
			yIt.remove();
			if (ys.isEmpty()) {
				xIt.remove();
			}
		}

		@Override
		public boolean hasNext() {
			if (yIt != null && yIt.hasNext()) {
				return true;
			}
			if (!xIt.hasNext()) {
				return false;
			}
			x = xIt.next();
			yIt = R.get(x).iterator();
			ys = R.get(x);

			return true;
		}

		@Override
		public Pair<X, Y> next() {
			Y y = yIt.next();
			return new Pair<X, Y>(x, y);
		}

	}

	private LinkedHashMap<X, LinkedHashSet<Y>> R;

	public BinRelMap(Set<Pair<X, Y>> r) {
		R = Util.toRel(r);
	}

	public BinRelMap(BinRelMap<X, Y> r) {
		R = new LinkedHashMap<>();
		for (X x : r.R.keySet()) {
			R.put(x, new LinkedHashSet<>(r.R.get(x)));
		}
	}

	public BinRelMap() {
		R = new LinkedHashMap<>();
	}

	public boolean add(X x, Y y) {
		LinkedHashSet<Y> ys = R.get(x);
		if (ys == null) {
			ys = new LinkedHashSet<>();
			R.put(x, ys);
		}
		return ys.add(y);
	}

	public void remove(X x, Y y) {
		Set<Y> ys = R.get(x);
		if (ys == null) {
			return;
		}
		ys.remove(y);
		if (ys.isEmpty()) {
			R.remove(x);
		}
	}

	public <Z> BinRelMap<X, Z> compose(BinRelMap<Y, Z> rhs) {
		LinkedHashMap<X, LinkedHashSet<Z>> ret = new LinkedHashMap<>();
		for (X x : R.keySet()) {
			LinkedHashSet<Z> set = new LinkedHashSet<>();
			for (Y y : R.get(x)) {
				LinkedHashSet<Z> w = rhs.R.get(y);
				if (w != null) {
					set.addAll(w);
				}
			}
			if (!set.isEmpty()) {
				ret.put(x, set);
			}
		}
		BinRelMap<X, Z> b = new BinRelMap<>();
		b.R = ret;
		return b;
	}

	public void addAll(BinRelMap<X, Y> a) {
		for (X x : a.R.keySet()) {
			for (Y y : a.R.get(x)) {
				add(x, y);
			}
		}
	}

	public void removeIf(Predicate<Pair<X, Y>> p) {
		Iterator<X> xIt = R.keySet().iterator();
		while (xIt.hasNext()) {
			X x = xIt.next();
			Iterator<Y> yIt = R.get(x).iterator();
			while (yIt.hasNext()) {
				Y y = yIt.next();
				if (p.test(new Pair<>(x, y))) {
					yIt.remove();
				}
			}
			if (R.get(x).isEmpty()) {
				xIt.remove();
			}
		}
	}

	public boolean isEmpty() {
		return R.isEmpty();
	}

	@Override
	public Iterator<Pair<X, Y>> iterator() {
		return new It();
	}

	@Override
	public String toString() {
		return "BinRelMap [R=" + R + "]";
	}

	public Collection<X> keySet() {
		return R.keySet();
	}

	public Collection<Y> get(
			X x) {
		return R.get(x);
	}

	public boolean containsKey(X x) {
		return R.containsKey(x);
	}

	public int size() {
		int i = 0;
		for (Pair<X, Y> x : this) {
			i++;
		}
		return i;
	}

}
