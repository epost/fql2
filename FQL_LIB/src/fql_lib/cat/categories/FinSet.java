package fql_lib.cat.categories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import fql_lib.Chc;
import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Unit;
import fql_lib.Util;
import fql_lib.cat.Category;
import fql_lib.cat.categories.FinSet.Fn;

@SuppressWarnings({ "rawtypes" })
public class FinSet extends Category<Set, Fn> {
//implements Products<Set, Fn>, Coproducts<Set, Fn>, Exponentials<Set, Fn>, Subobjects<Set, Fn>, Isomorphisms<Set, Fn> {

	@Override 
	public boolean isInfinite() {
		return true;
	}
	
	@Override
	public boolean isObject(Set o) {
		return (o instanceof Set);
	}
	
	@Override
	public boolean isArrow(Fn a) {
		return (a instanceof Fn);
	}

	public static class Fn<X,Y> {
		public Set<X> source;
		public Set<Y> target;
		private Function<X,Y> function;
		
		public Y apply(X o) {
			if (DEBUG.debug.VALIDATE) {
				if (!source.contains(o)) {
					throw new RuntimeException("Cannot apply " + this + " to " + o);
				}
			}
			Y y = function.apply(o);
			return y;
		}
		
		public Fn(Set<X> source, Set<Y> target, Function<X,Y> function) {
			this.source = source;
			this.target = target;
			this.function = function;
			validate();
		}
		
		public void validate() {
			if (!DEBUG.debug.VALIDATE) {
				return;
			}
			if (source == null) {
				throw new RuntimeException("Null source set in " + this);
			}
			if (target == null) {
				throw new RuntimeException("Null target set in " + this);
			}
			if (function == null) {
				throw new RuntimeException("Null function in " + this);
			}
			for (X o : source) {
				if (!target.contains(function.apply(o))) {
					throw new RuntimeException(this + " does not validate: " + o + " maps to " + function.apply(o) + " is not in " + target + ". Source is " + source);
				}
			}
			
		}
		
		@Override 
		public String toString() {
			//String l = source.toString();
			//String r = target.toString();
			String z = Util.sep(source.stream().map(x -> new Pair<>(x, function.apply(x)).toString()).iterator(), ",");
			return "[" + z + "]"; //Source: " + l + "\nTarget: " + r;
		}
		
		public String toStringLong() {
			String l = source.toString();
			String r = target.toString();
			String z = Util.sep(source.stream().map(x -> new Pair<>(x, function.apply(x)).toString()).iterator(), ",");
			return "[" + z + "]\nSource: " + l + "\nTarget: " + r;
		}
		
		@SuppressWarnings({ "unchecked" })
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Fn)) {
				return false;
			}
			Fn other = (Fn) o;
			if (!source.equals(other.source)) {
				return false;
			}
			if (!target.equals(other.target)) {
				return false;
			}
			for (X k : source) {
				if (!function.apply(k).equals(other.function.apply(k))) {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public int hashCode() {
			return 0;
		}

		public Map<X,Y> toMap() {
			Map<X,Y> ret = new HashMap<>();
			for (X o : source) {
				ret.put(o, function.apply(o));
			}
			return ret;
		}
		
		public static <X> Fn<X,X> id(Set<X> set) {
			return new Fn<>(set, set, x -> x);
		}
		
		public static <X,Y,Z> Fn<X,Z> compose(Fn<X,Y> f, Fn<Y,Z> g) {
			if (!f.target.equals(g.source)) {
				throw new RuntimeException("Cannot compose " + f + " and " + g + ": cod/dom do not match: " + f.target + " and " + g.source);
			}
			return new Fn<>(f.source, g.target, x -> g.function.apply(f.function.apply(x)));

		}
	}
	
	public static <X> Category<Set<X>, Fn<X,X>> FinSet() {
		return (Category<Set<X>, Fn<X,X>>) ((Object)FinSet);
	}
	
	public static FinSet FinSet = new FinSet();
	

	@Override
	public Set<Set> objects() {
		throw new RuntimeException("Cannot enumerate objects of Set");
	}

	@Override
	public Set<Fn> arrows() {
		throw new RuntimeException("Cannot enumerate arrows of Set");
	}

	@Override
	public Set source(Fn a) {
		return a.source;
	}

	@Override
	public Set target(Fn a) {
		return a.target;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Fn identity(Set o) {
		return new Fn<>(o, o, x -> x);
	}

	@SuppressWarnings({ "unchecked",  })
	@Override
	public Fn compose(Fn a1, Fn a2) {
		return Fn.compose(a1, a2);
	}

	@Override
	public String toString() {
		return "Set";
	}

	@Override
	public void validate() {
		
	}

	public static <X, Y> List<LinkedHashMap<X, Y>> homomorphs(List<X> A,
			List<Y> B) {
		List<LinkedHashMap<X, Y>> ret = new LinkedList<>();

		if (A.size() == 0) {
			ret.add(new LinkedHashMap<X, Y>());
			return ret;
		}
		if (B.size() == 0) {
			return ret;
		}

		int[] counters = new int[A.size() + 1];

		for (;;) {
			if (counters[A.size()] == 1) {
				break;
			}
			ret.add(make2(counters, A, B));
			inc(counters, B.size());
		}

		return ret;
	}

	public static <X,Y> List<LinkedHashMap<X, Y>> bijections(List<X> A, List<Y> B) {
		List<LinkedHashMap<X, Y>> ret = new LinkedList<>();

		if (A.size() != B.size()) {
			throw new RuntimeException();
		}

		if (A.size() == 0) {
			ret.add(new LinkedHashMap<X, Y>());
			return ret;
		}

		List<Integer> seq = new LinkedList<>();
		for (int i = 0; i < A.size(); i++) {
			seq.add(i);
		}

		Collection<List<Integer>> xxx = permute(seq);

		for (List<Integer> l : xxx) {
			LinkedHashMap<X, Y> m = new LinkedHashMap<>();
			int j = 0;
			for (Integer i : l) {
				m.put(A.get(j), B.get(i));
				j++;
			}
			ret.add(m);
		}

		return ret;
	}
	
	public static <X, Y> LinkedHashMap<X, Y> fast_iso(List<X> A, List<Y> B) {
	
		if (A.size() != B.size()) {
			return null;
		}

		if (A.size() == 0) {
			return new LinkedHashMap<X, Y>();
		}

		List<Integer> seq = new LinkedList<>();
		for (int i = 0; i < A.size(); i++) {
			seq.add(i);
		}

		Collection<List<Integer>> xxx = permute(seq);

		for (List<Integer> l : xxx) {
			LinkedHashMap<X, Y> m = new LinkedHashMap<>();
			int j = 0;
			for (Integer i : l) {
				m.put(A.get(j), B.get(i));
				j++;
			}
			return m;
		}

		throw new RuntimeException();
	}

	private static <X, Y> LinkedHashMap<X, Y> make2(int[] counters, List<X> A,
			List<Y> B) {
		LinkedHashMap<X, Y> ret = new LinkedHashMap<>();
		int i = 0;
		for (X x : A) {
			ret.put(x, B.get(counters[i++]));
		}
		return ret;
	}

	private static void inc(int[] counters, int size) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == size) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	public static <X, Y> List<LinkedHashMap<X, Y>> homomorphs(Map<X, List<Y>> L) {
		List<LinkedHashMap<X, Y>> ret = new LinkedList<>();

		if (L.isEmpty()) {
			ret.add(new LinkedHashMap<X, Y>());
			return ret;
		}
		for (Entry<X, List<Y>> k : L.entrySet()) {
			if (k.getValue().isEmpty()) {
				return ret;
			}
		}

		int[] counters = new int[L.keySet().size() + 1];
		int[] lengths = new int[L.keySet().size()];
		int i = 0;
		for (Entry<X, List<Y>> x : L.entrySet()) {
			lengths[i++] = x.getValue().size();
		}

		for (;;) {

			if (counters[L.keySet().size()] == 1) {
				break;
			}
			ret.add(make3(counters, L));
			inc3(counters, lengths);

		}

		return ret;
	}

	private static <X, Y> LinkedHashMap<X, Y> make3(int[] counters,
			Map<X, List<Y>> L) {
		LinkedHashMap<X, Y> ret = new LinkedHashMap<>();
		int i = 0;
		for (X x : L.keySet()) {
			ret.put(x, L.get(x).get(counters[i++]));
		}
		return ret;
	}

	private static <X, Y> void inc3(int[] counters, int[] lengths) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == lengths[i]) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	public static <T> Collection<List<T>> permute(Collection<T> input) {
		Collection<List<T>> output = new ArrayList<List<T>>();
		if (input.isEmpty()) {
			output.add(new ArrayList<T>());
			return output;
		}
		List<T> list = new ArrayList<T>(input);
		T head = list.get(0);
		List<T> rest = list.subList(1, list.size());
		for (List<T> permutations : permute(rest)) {
			List<List<T>> subLists = new ArrayList<List<T>>();
			for (int i = 0; i <= permutations.size(); i++) {
				List<T> subList = new ArrayList<T>();
				subList.addAll(permutations);
				subList.add(i, head);
				subLists.add(subList);
			}
			output.addAll(subLists);
		}
		return output;
	}

	public Set<Unit> terminal() {
		Set<Unit> ret = new HashSet<>();
		ret.add(new Unit());
		return ret;
	}

	public <X> Fn<X,Unit> terminal(Set<X> o) {
		return new Fn<>(o, terminal(), x -> new Unit());
	}

	public <X,Y> Set<Pair<X,Y>> product(Set<X> o1, Set<Y> o2) {
		Set<Pair<X,Y>> ret = new HashSet<>();
		for (X x : o1) {
			for (Y y : o2) {
				ret.add(new Pair<>(x, y));
			}
		}
		return ret;
	}

	public <X,Y> Fn<Pair<X,Y>, X> first(Set<X> o1, Set<Y> o2) {
		return new Fn<>(product(o1, o2), o1, x -> x.first);
	}

	public<X,Y>  Fn<Pair<X,Y>, Y> second(Set<X> o1, Set<Y> o2) {
		return new Fn<>(product(o1, o2), o2, x -> x.second);
	}

	public <A,B,C,D> Fn<Pair<A,C>,Pair<B,D>> pairF(Fn<A,B> a1, Fn<C,D> a2) {
		return pair(Fn.compose(first (a1.source, a2.source), a1), Fn.compose(second(a1.source, a2.source), a2));
	}
	
	public <A,B,C,D> Fn<Chc<A,C>,Chc<B,D>> matchF(Fn<A,B> a1, Fn<C,D> a2) {
		return match(Fn.compose(a1, inleft (a1.target, a2.target)), Fn.compose(a2, inright(a1.target, a2.target)));
	}

	public <X,Y,Z> Fn<X,Pair<Y,Z>> pair(Fn<X,Y> a1, Fn<X,Z> a2) {
		if (!a1.source.equals(a2.source)) {
			throw new RuntimeException();
		}
		return new Fn<>(a1.source, product(a1.target, a2.target),
				x -> new Pair<>(a1.function.apply(x), a2.function.apply(x)));
	}

	public Set<Void> initial() {
		return new HashSet<>();
	}

	public <X> Fn<Void, X> initial(Set<X> o) {
		return new Fn<Void, X>(initial(), o, x -> { throw new RuntimeException(); });
	}

	public <X,Y> Set<Chc<X,Y>> coproduct(Set<X> o1, Set<Y> o2) {
		Set<Chc<X,Y>> ret = new HashSet<>();
		for (X o : o1) {
			ret.add(Chc.inLeft(o));
		}
		for (Y o : o2) {
			ret.add(Chc.inRight(o));
		}
		return ret;
	}

	public <X,Y> Fn<X,Chc<X,Y>> inleft(Set<X> o1, Set<Y> o2) {
		return new Fn<>(o1, coproduct(o1, o2), x -> Chc.inLeft(x));
	}

	public <X,Y> Fn<Y,Chc<X,Y>> inright(Set<X> o1, Set<Y> o2) {
		return new Fn<>(o2, coproduct(o1, o2), x -> Chc.inRight(x));
	}

	public <X,Y,Z> Fn<Chc<X,Y>,Z> match(Fn<X,Z> a1, Fn<Y,Z> a2) {
		if (!a1.target.equals(a2.target)) {
			throw new RuntimeException();
		}
		return new Fn<>(coproduct(a1.source, a2.source), a1.target, x -> {
			if (x.left) {
				return a1.function.apply(x.l);
			} else {
				return a2.function.apply(x.r);
			}
		});
	}
	
	Map<Pair<Set,Set>, Set<Fn>> cached = new HashMap<>();
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public Set hom(Set A, Set B) {
		Pair<Set, Set> p = new Pair<>(A,B);
		Set retX = cached.get(p);
		if (retX != null) {
			return retX;
		}
		Set<Fn> ret = new HashSet<>();

		List<LinkedHashMap<Set, Set>> k = homomorphs(new LinkedList<>(A), new LinkedList<>(B));
		for (LinkedHashMap<Set, Set> v : k) {
			ret.add(new Fn(A, B, x -> v.get(x)));
		}
		
		cached.put(p, ret);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <X,Y> Set<Fn<Y,X>> exp(Set<X> o1, Set<Y> o2) {
		Set<Fn<Y,X>> o = hom(o2, o1);
		return o;
	}

	 //A^B * B -> A
	public <A,B> Fn<Pair<Fn<B,A>,B>, A> eval(Set<A> a, Set<B> b) {
		return new Fn<>(product(exp(a,b),b), a, 
				x -> { return x.first.function.apply(x.second); });
	}

	// a*b -> c ~~> a -> c^b
	public <A,B,C> Fn<A, Fn<B,C>> curry(Fn<Pair<A,B>, C> f) {
		Set<A> as = new HashSet<>();
		Set<B> bs = new HashSet<>();
		for (Pair<A,B> k : f.source) {
			as.add(k.first);
			bs.add(k.second);
		}
		return new Fn<>(as, exp(f.target, bs), x -> { 
			Fn<B,C> ret = new Fn<>(bs, f.target, y -> f.function.apply(new Pair<>(x,y)));
			return ret;
		});
	}

	public Set<Boolean> prop() {
		Set<Boolean> ret = new HashSet<>();
		ret.add(true);
		ret.add(false);
		return ret;
	}

	public Fn<Unit, Boolean> tru() {
		return new Fn<>(terminal(), prop(), x -> true);
	}

	public <A,B> Fn<B,Boolean> chr(Fn<A,B> f) {
		Set<B> s = new HashSet<>();
		for (A a : f.source) {
			s.add(f.function.apply(a));
		}
		return new Fn<>(f.target, prop(), x -> s.contains(x));
	}

	public <B> Fn<B,B> kernel(Fn<B,Boolean> f) {
		Set<B> s = new HashSet<>();
		for (B k : f.source) {
			Boolean b = (Boolean) f.function.apply(k);
			if (b) {
				s.add(k);
			}
		}
		return new Fn<>(s, f.source, x -> x);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof FinSet);
	}
	
	private FinSet() { }

	public Fn<Unit,Boolean> fals() {
		return new Fn<>(terminal(), prop(), x -> false);
	}
	
	public Fn<Boolean,Boolean> not() {
		return new Fn<>(prop(), prop(), x -> !x);
	}
	
	public Fn<Pair<Boolean,Boolean>,Boolean> and() {
		return new Fn<>(product(prop(),prop()), prop(), x -> x.first && x.second);
	}
	
	public Fn<Pair<Boolean,Boolean>,Boolean> or() {
		return new Fn<>(product(prop(),prop()), prop(), x -> x.first || x.second);
	}
	
	public Fn<Pair<Boolean,Boolean>,Boolean> implies() {
		return new Fn<>(product(prop(),prop()), prop(), x -> !x.first || x.second);
	}

	public <X,Y> Optional<Pair<Fn<X,Y>, Fn<Y,X>>> iso(Set<X> o1, Set<Y> o2) {
		LinkedHashMap<X,Y> k = fast_iso(new LinkedList<>(o1), new LinkedList<>(o2));
		if (k == null) {
			return Optional.empty();
		}
		Fn<X,Y> f1 = new Fn<>(o1, o2, k::get);
		Fn<Y,X> f2 = new Fn<>(o2, o1, x -> {
			for (Entry<X,Y> v : k.entrySet()) {
				if (v.getValue().equals(x)) {
					return v.getKey();
				}
			}
			throw new RuntimeException();
		});
		return Optional.of(new Pair<>(f1, f2));
	}

}
