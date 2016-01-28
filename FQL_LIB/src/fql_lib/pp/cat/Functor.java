package fql_lib.pp.cat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.algs.Pair;
import fql_lib.FUNCTION;
import fql_lib.core.DEBUG;
import fql_lib.core.Util;
import fql_lib.pp.cat.FinSet.Fn;

public class Functor<O1, A1, O2, A2> implements Serializable {
	public Category<O1, A1> source;
	public Category<O2, A2> target;
	private FUNCTION<O1, O2> o;
	private FUNCTION<A1, A2> a;

	private static Map<Category, Functor> ids = new HashMap<>();
	public static <O, A> Functor<O, A, O, A> identity(Category<O, A> o) {
		if (ids.containsKey(o)) {
			return ids.get(o);
		}
		ids.put(o, new Functor<>(o, o, x -> x, x -> x));
		return ids.get(o);
	}

	static Map<Pair<Functor, Functor>, Functor> cache = new HashMap<>();
	public static <O1, A1, O2, A2, O3, A3> Functor<O1, A1, O3, A3> compose(
			Functor<O1, A1, O2, A2> a1, Functor<O2, A2, O3, A3> a2) {
		Pair p = new Pair<>(a1, a2);
		if (cache.containsKey(p)) {
			return cache.get(p);
		}
		if (!a1.target.equals(a2.source)) {
			throw new RuntimeException("Dom/Cod mismatch on " + a1 + " and " + a2);
		}
		cache.put(p, new Functor<>(a1.source, a2.target, a1.o.andThen(a2.o), a1.a.andThen(a2.a)));
		return cache.get(p);
	}

	public O2 applyO(O1 x) {
		if (!(source.isObject(x))) {
			throw new RuntimeException(x + " is not in " + target);
		}
		O2 y = o.apply(x);
		if (!(target.isObject(y))) {
			throw new RuntimeException(x + " is not in " + target);
		}
		return y;
	}

	public A2 applyA(A1 x) {
		if (!(source.isArrow(x))) {
			throw new RuntimeException(x + " is not in " + source);
		}
		A2 y = a.apply(x);
		if (!(target.isArrow(y))) {
			throw new RuntimeException(x + " is not in " + target);
		}
		Object ys = target.source(y);
		Object yt = target.target(y);
		if (!(ys.equals(applyO(source.source(x))))) {
			throw new RuntimeException(this + " does not preserve sources on " + x + ": source "
					+ source.source(x) + " goes to " + ys + " but should go to "
					+ applyO(source.source(x)));
		}
		if (!(yt.equals(applyO(source.target(x))))) {
			throw new RuntimeException(this + "does not preserve targets on " + x + ": target "
					+ source.target(x) + " goes to " + ys + " but should go to "
					+ applyO(source.target(x)));
		}
		return y;
	}

	public Functor(Category<O1, A1> source, Category<O2, A2> target, FUNCTION<O1, O2> o,
			FUNCTION<A1, A2> a) {
		this.source = source;
		this.target = target;
		this.o = o;
		this.a = a;
		// System.out.println("validating " + this);
		validate();
	}

	@Override
	public String toString() {
		try {
			// String l = source.toString();
			// String r = target.toString();
			String z1 = Util.sep(source.objects().stream().map(x -> x + " -> " + o.apply(x))
					.iterator(), "\n\t");
			String z2 = Util.sep(source.arrows().stream().map(x -> x + " -> " + a.apply(x))
					.iterator(), "\n\t");
			String a1 = "On objects:\n\t " + z1 + "\n\n";
			String a2 = "On arrows:\n\t " + z2 + "\n";
			return a1 + a2; // + "Source    :\n" + l + "\n" +
							// "Target    :\n" + r;
		} catch (Exception e) {
			e.printStackTrace();
			return "(Cannot print functor)";
		}
	}

	public String toStringLong() {
		try {
			String l = source.toString();
			String r = target.toString();
			String z1 = Util.sep(source.objects().stream().map(x -> x + " -> " + o.apply(x))
					.iterator(), ",\n\t");
			String z2 = Util.sep(source.arrows().stream().map(x -> x + " -> " + a.apply(x))
					.iterator(), ",\n\t");
			String a1 = "On objects:\n\t " + z1 + "\n\n";
			String a2 = "On arrows:\n\t " + z2 + "\n";
			return a1 + a2 + "\nSource    :\n" + l + "\n" + "\nTarget    :\n" + r;
		} catch (Exception e) {
			return "(Cannot print transform)";
		}
	}

	@Override
	public boolean equals(Object ooo) {
		if (this == ooo) {
			return true;
		}
		if (!(ooo instanceof Functor)) {
			return false;
		}
		Functor<?, ?, ?, ?> otherX = (Functor<?, ?, ?, ?>) ooo;
		if (source.isInfinite()) {
			return this == ooo;
		}
		if (!source.equals(otherX.source)) {
			return false;
		}
		if (!target.equals(otherX.target)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Functor<O1, A1, O2, A2> other = (Functor<O1, A1, O2, A2>) otherX;

		for (O1 k : source.objects()) {
			if (!o.apply(k).equals(other.o.apply(k))) {
				return false;
			}
		}
		for (A1 k : source.arrows()) {
			if (!a.apply(k).equals(other.a.apply(k))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public void validate() {
		if (!DEBUG.debug.VALIDATE) {
			return;
		}
		if (source.isInfinite()) {
			return;
		}
		//System.out.println("Validating ---------------- ");
		for (O1 x : source.objects()) {
			if (!target.isObject(o.apply(x))) {
				throw new RuntimeException(x + " mapped to " + o.apply(x) + " not in " + target);
			}
		//	System.out.println("checking " + x);
		//	System.out.println(source.identity(x));
		//	System.out.println(o.apply(x));
		//	System.out.println(a.apply(source.identity(x)));
		//	System.out.println(target.identity(o.apply(x)));
			if (!a.apply(source.identity(x)).equals(target.identity(o.apply(x)))) {
				throw new RuntimeException("Does not preserve identity on " + x + ": lhs is "
						+ a.apply(source.identity(x)) + " rhs is " + target.identity(o.apply(x)));
			}
		}
		for (A1 x : source.arrows()) {
			if (!target.isArrow(a.apply(x))) {
				throw new RuntimeException(x + " mapped to " + a.apply(x) + " not in " + target);
			}
			//System.out.println("On arrow: " + x + ", source is " + source.source(x) + " (transforms to " + o.apply(source.source(x)) + 
				//	") and target is " + source.target(x) +
				//	" (transforms to " + o.apply(source.target(x)) + "). The arrow itself transforms to " + a.apply(x) + " with source " +
				//	target.source(a.apply(x)) + " and target " + target.target(a.apply(x)));
			if (!target.source(a.apply(x)).equals(o.apply(source.source(x)))) {
				throw new RuntimeException(x + " mapped to " + a.apply(x)
						+ " does not preserve source.");
			}
			if (!target.target(a.apply(x)).equals(o.apply(source.target(x)))) {
				throw new RuntimeException(x + " mapped to " + a.apply(x)
						+ " does not preserve target.");
			}
			for (A1 y : source.arrows()) {
				if (!source.source(y).equals(source.target(x))) {
					continue;
				}
				if (!a.apply(source.compose(x, y)).equals(target.compose(a.apply(x), a.apply(y)))) {
					throw new RuntimeException("Composition not preserved on" + x + " and " + y
							+ ":\nLHS =\n" + a.apply(source.compose(x, y)) + "\nand RHS=\n"
							+ target.compose(a.apply(x), a.apply(y)));
				}
			}
		}
		//System.out.println("done");
	}
	
	private Instance<O1,A1> instance;
	public  Instance<O1,A1> toInstance() {
		if (instance != null) {
			return instance;
		}
		instance = toInstanceX(source.toSig());
		return instance;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Instance<O1,A1> toInstanceX(Signature<O1,A1> src) {
		if (source.isInfinite() || !target.equals(FinSet.FinSet)) {
			throw new RuntimeException("Cannot create mapping from " + this);
		}

		//Signature<O1,A1> src = source.toSig();

		Map<Signature<O1,A1>.Node, Set<Object>> nm = new HashMap<>();
		Map<Signature<O1,A1>.Edge, Map<Object,Object>> em = new HashMap<>();
		
		for (O1 o : source.objects()) {
			nm.put(src.new Node(o), (Set) applyO(o));
		}
		
		for (Signature<O1, A1>.Edge a : src.edges) {
				Fn f = (Fn) applyA(a.name);
				em.put(a, Util.reify(f::apply, f.source));
		}
		//for (A1 a : source.arrows()) {
		//	Fn f = (Fn) applyA(a);
		//	em.put(src.getEdge(a), Util.reify(f::apply, f.source));
		//}
		
		return new Instance<>(nm, em, src);
	}
	
	
	
	private Mapping<O1,A1,O2,A2> mapping;
	public  Mapping<O1,A1,O2,A2> toMapping() {
		if (mapping != null) {
			return mapping;
		}
		mapping = toMappingX();
		return mapping;
	}
	public Mapping<O1,A1,O2,A2> toMappingX() {
		if (source.isInfinite() || target.isInfinite()) {
			throw new RuntimeException("Cannot create mapping from " + this);
		}

		Signature<O1,A1> src = source.toSig();
		Signature<O2,A2> dst = target.toSig();

		Map<Signature<O1,A1>.Node, Signature<O2,A2>.Node> nm = new HashMap<>();
		Map<Signature<O1,A1>.Edge, Signature<O2,A2>.Path> em = new HashMap<>();
		
		for (O1 o : source.objects()) {
			nm.put(src.new Node(o), dst.new Node(applyO(o)));
		}
		
		for (Signature<O1, A1>.Edge a : src.edges) {
			A2 b = applyA(a.name);
			O2 s = target.source(b);
			if (target.isId(b)) {
				em.put(a, dst.path(s, new LinkedList<>())); //dst.new Node(s); //(path(s)); //dst.getEdge(applyA(a.name))));				
			} else {
				em.put(a, dst.path(dst.getEdge(applyA(a.name))));				
			}
		}

		return new Mapping<>(nm, em, src, dst);
	}
	
	public Mapping<O1,A1,O2,A2> toMappingZ(Signature<O1,A1> src, Signature<O2,A2> dst) {
		if (source.isInfinite() || target.isInfinite()) {
			throw new RuntimeException("Cannot create mapping from " + this);
		}

//		Signature<O1,A1> src = source.toSig();
	//	Signature<O2,A2> dst = target.toSig();

		Map<Signature<O1,A1>.Node, Signature<O2,A2>.Node> nm = new HashMap<>();
		Map<Signature<O1,A1>.Edge, Signature<O2,A2>.Path> em = new HashMap<>();
		
		for (O1 o : source.objects()) {
			nm.put(src.new Node(o), dst.new Node(applyO(o)));
		}
		
		for (Signature<O1, A1>.Edge a : src.edges) {
			A2 b = applyA(a.name);
			O2 s = target.source(b);
		//	if (target.isId(b)) {
		//		em.put(a, dst.path(s, new LinkedList<>())); //dst.new Node(s); //(path(s)); //dst.getEdge(applyA(a.name))));				
		//	} else {
				
			//	List<Signature<O1, A1>.Edge> aaa = (List<Signature<O1, A1>.Edge>) a.name;
				
			//	aaa.stream().map(z -> dst.getEdge(applyA(z.name))).collect(Collectors.toList());
				
			Signature<O2, A2>.Path p = (Signature<O2, A2>.Path) b;
			p.source = p.sig().new Node((O2) p.source);
			p.target = p.sig().new Node((O2) p.target);
			
			em.put(a, p); // dst.path(dst.getEdge(applyA(a.name))));			/	
			//}
		}
		
/*
		for (A1 a : source.arrows()) {
			boolean found = false;
			for (Signature<O1, A1>.Edge a2 : src.edges) {
				if (a2.name.equals(a) && !source.isId(a)) {
					found = true;
				}
			}
			if (found) {
				em.put(src.getEdge(a), dst.path(dst.getEdge(applyA(a))));
			}
		} */

		
		return new Mapping<>(nm, em, src, dst);
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Functor<O1,A1,Set,Fn>> subInstances() {
		if (source.isInfinite() || !target.equals(FinSet.FinSet)) {
			throw new RuntimeException("Cannot find subinstances of " + this);
		}
		return SubInstances.subInstances((Functor<O1,A1,Set,Fn>)this);
	}
	
	public Instance instance0;
	public Mapping mapping0;

}