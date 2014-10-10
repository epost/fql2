package fql_lib.X;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fql_lib.Chc;
import fql_lib.Pair;

public class XProd {
	
	public static <X> XCtx<X> zero(XCtx<X> S) {
		return new XCtx<X>(new HashSet<>(), new HashMap<>(), new HashSet<>(), S.global, S, "instance");
	}
	
	public static <X> XMapping<X,X> ff(XCtx<X> I) {
		if (I.schema == null) {
			throw new RuntimeException();
		}
		
		Map em = new HashMap();
		
		for (X x : I.schema.allTerms()) {
			List l = new LinkedList<>();
			l.add(x);
			em.put(x, l);
		}

		return new XMapping<X,X>(zero(I.schema), I, em, "homomorphism");
	}
	
	public static <A,B,C> XMapping<Chc<A,B>, C> match(XMapping<A,C> l, XMapping<B,C> r) {
		if (!l.dst.equals(r.dst)) {
			throw new RuntimeException();
		}
		XCtx<Chc<A,B>> src = coprod(l.src, r.src);
		
		Map em = new HashMap<>();
		for (Chc<A,B> x : src.terms()) {
			if (x.left) {
				em.put(x, l.em.get(x.l));
			} else {
				em.put(x, r.em.get(x.r));
			}
		}
		
		for (Object x : src.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List z = new LinkedList<>();
			z.add(x);
			em.put(x, z); 
		}

		
		return new XMapping<Chc<A,B>, C>(src, l.dst, em, "homomorphism");
	}
	
	public static <X,Y> XMapping<X, Chc<X,Y>> inl(XCtx<X> I, XCtx<Y> J) {
		XCtx<Chc<X,Y>> IJ = coprod(I,J);
		Map<X, List<Chc<X,Y>>> em = new HashMap<>();
		
		for (X x : I.types.keySet()) {
			List<Chc<X,Y>> l = new LinkedList<>();
			l.add(Chc.inLeft(x));
			em.put(x, l);
		}
		
		for (X x : I.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(x);
			em.put(x, l);
		}
		
		return new XMapping<>(I, IJ, em, "homomorphism");
	}
	
	public static <X,Y> XMapping<Y, Chc<X,Y>> inr(XCtx<X> I, XCtx<Y> J) {
		XCtx<Chc<X,Y>> IJ = coprod(I,J);
		Map<Y, List<Chc<X,Y>>> em = new HashMap<>();
		
		for (Y x : J.types.keySet()) {
			List<Chc<X,Y>> l = new LinkedList<>();
			l.add(Chc.inRight(x));
			em.put(x, l);
		}
		
		for (Y x : J.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(x);
			em.put(x, l);
		}
		
		return new XMapping<>(J, IJ, em, "homomorphism");
	}

	public static <X,Y> XCtx<Chc<X,Y>> coprod(XCtx<X> I, XCtx<Y> J) {
		if (I.global == null || J.global == null) {
			throw new RuntimeException();
		}
		if (!I.global.equals(J.global)) {
			throw new RuntimeException();
		}
		if (I.schema == null || J.schema == null) {
			throw new RuntimeException();
		}
		if (!I.schema.equals(J.schema)) {
			throw new RuntimeException();
		}
		
		Set<Chc<X,Y>> ids = new HashSet<>();
		
		Map<Chc<X,Y>, Pair<Chc<X,Y>, Chc<X,Y>>> types = new HashMap<>();
		for (X x : I.types.keySet()) {
			Pair y = I.types.get(x);
			types.put(Chc.inLeft(x), y);
		}
		for (Y x : J.types.keySet()) {
			Pair y = J.types.get(x);
			types.put(Chc.inRight(x), y);
		}
		
		Set<Pair<List<Chc<X,Y>>, List<Chc<X,Y>>>> eqs = new HashSet<>();
		for (Pair<List<X>, List<X>> eq : I.eqs) {
			List lhs = eq.first.stream().map(x -> I.terms().contains(x) ? Chc.inLeft(x) : x).collect(Collectors.toList());
			List rhs = eq.second.stream().map(x -> I.terms().contains(x) ? Chc.inLeft(x) : x).collect(Collectors.toList());
			eqs.add(new Pair(lhs, rhs));
		}
		for (Pair<List<Y>, List<Y>> eq : J.eqs) {
			List lhs = eq.first.stream().map(x -> J.terms().contains(x) ? Chc.inRight(x) : x).collect(Collectors.toList());
			List rhs = eq.second.stream().map(x -> J.terms().contains(x) ? Chc.inRight(x) : x).collect(Collectors.toList());
			eqs.add(new Pair(lhs, rhs));
		}
		
		return new XCtx<Chc<X,Y>>(ids, types, eqs, (XCtx)I.global, (XCtx)I.schema, "instance");
	}
	
	
}
