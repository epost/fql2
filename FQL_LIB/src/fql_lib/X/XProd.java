package fql_lib.X;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.Chc;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.X.XExp.FLOWER2;
import fql_lib.X.XExp.Flower;

public class XProd {
	
	public static <X> XCtx<X> zero(XCtx<X> S) {
		return new XCtx<X>(new HashSet<>(), new HashMap<>(), new HashSet<>(), S.global, S, "instance");
	}
	
	public static <X> XCtx<X> one(XCtx<X> S) {
		Map<X, Pair<X, X>> tys = new HashMap<>();
		Set<Pair<List<X>, List<X>>> eqs = new HashSet<>();
		
		for (X O : S.allIds()) {
			X x = (X) ("1_" + O);
			tys.put(x, new Pair<>((X)"_1", O));
			for (X f : S.allTerms()) {
				if (!S.type(f).first.equals(O)) {
					continue;
				}			
				X y = (X) ("1_" + S.type(f).second);
				List<X> lhs = new LinkedList<>();
				lhs.add(y);
				List<X> rhs = new LinkedList<>();
				rhs.add(x);
				rhs.add(f);
				eqs.add(new Pair<>(lhs, rhs));
			}
		}
		
		return new XCtx<X>(new HashSet<>(), tys, eqs, S.global, S, "instance");
	}
	
	public static <X> XMapping<X,X> tt(XCtx<X> I) {
		if (I.schema == null) {
			throw new RuntimeException();
		}
		
		Map em = new HashMap();
		
		for (X x : I.terms()) {
			Pair<X,X> t = I.type(x);
			if (!t.first.equals("_1")) {
				throw new RuntimeException();
			}
			X y = (X) ("1_" + t.second);
			List<X> l = new LinkedList<>();
			l.add(y);
			em.put(x, l);
		}
		
		for (X x : I.schema.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(x);
			em.put(x, l);
		}

		return new XMapping<X,X>(I, one(I.schema), em, "homomorphism");
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
	
	public static <X> XCtx<Pair<Triple<X, X, List<X>>,Triple<X, X, List<X>>>> prod(XCtx<X> I, XCtx<X> J) {
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
		
		Set ids = new HashSet<>();
		Set<Pair<List<Pair<Triple<X, X, List<X>>, Triple<X, X, List<X>>>>, List<Pair<Triple<X, X, List<X>>, Triple<X, X, List<X>>>>>> eqs = new HashSet<>();
		Map types = new HashMap<>();
		/* for each pair (i,j) of type X and each generating
edge f:X->Y in S (including edges in type, like length or succ),
(i,j);f = (i;f, j;f). */
		for (X x :I.schema.allIds()) {
			Set<Pair<Triple<X,X,List<X>>, Triple<X,X,List<X>>>> s = new HashSet<>();
			for (Triple<X, X, List<X>> i : I.cat().hom((X)"_1", x)) {
				for (Triple<X, X, List<X>> j : J.cat().hom((X)"_1", x)) {
					types.put(new Pair<>(i,j), new Pair<>("_1", x));
					s.add(new Pair<>(i,j));
				}
			}
			
			for (X f : I.schema.allTerms()) {
				Pair<X,X> t = I.type(f);
				if (!t.first.equals(x)) {
					continue;
				}
				for (Pair<Triple<X,X,List<X>>, Triple<X,X,List<X>>> xy : s) {
					List lhs = new LinkedList();
					lhs.add(xy);
					lhs.add(f);

					List<X> l1 = new LinkedList<>();
					l1.add(xy.first.first);
					l1.addAll(xy.first.third);
					l1.add(f);
					Triple<X,X,List<X>> tofind1 = new Triple<>((X)"_1", t.second, l1);
					Triple<X,X,List<X>> found1 = I.find(I.getKB(), tofind1 , I.cat().hom((X)"_1", t.second));
					if (found1 == null) {
						throw new RuntimeException("foudn1");
					}
					
					List<X> l2 = new LinkedList<>();
					l2.add(xy.second.first);
					l2.addAll(xy.second.third);
					l2.add(f);
					Triple<X,X,List<X>> tofind2 = new Triple<>((X)"_1", t.second, l2);
					Triple<X,X,List<X>> found2 = J.find(J.getKB(), tofind2 , J.cat().hom((X)"_1", t.second));
					if (found2 == null) {
						throw new RuntimeException("ouns 2");
					}
					
					Pair rhs0 = new Pair<>(found1, found2);
					List rhs = new LinkedList();
					rhs.add(rhs0);
					
					eqs.add(new Pair<>(lhs, rhs));
				}
			}
		}
		
		return new XCtx<Pair<Triple<X, X, List<X>>,Triple<X, X, List<X>>>>(ids, types, eqs, (XCtx)I.global, (XCtx)I.schema, "instance");
		
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
	
	public static <X,Y> XMapping fst(XCtx<X> I, XCtx<X> J) {
		XCtx<Pair<Triple<X,X,List<X>>,Triple<X,X,List<X>>>> IJ = prod(I,J);
		Map em = new HashMap<>();
		
		for (Pair<Triple<X, X, List<X>>, Triple<X, X, List<X>>> x : IJ.types.keySet()) {
			List l = new LinkedList<>();
			l.add(x.first.first);
			l.addAll(x.first.third);
			em.put(x, l);
		}
		
		for (Object x : IJ.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(x);
			em.put(x, l);
		}
		
		return new XMapping<>(IJ, I, em, "homomorphism");
	}
	
	public static <X,Y> XMapping snd(XCtx<X> I, XCtx<X> J) {
		XCtx<Pair<Triple<X,X,List<X>>,Triple<X,X,List<X>>>> IJ = prod(I,J);
		Map em = new HashMap<>();
		
		for (Pair<Triple<X, X, List<X>>, Triple<X, X, List<X>>> x : IJ.types.keySet()) {
			List l = new LinkedList<>();
			l.add(x.second.first);
			l.addAll(x.second.third);
			em.put(x, l);
		}
		
		for (Object x : IJ.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(x);
			em.put(x, l);
		}
		
		return new XMapping<>(IJ, J, em, "homomorphism");
	}
	
	public static <A,B,C> XMapping<A, ?> pair(XMapping<A, A> l, XMapping<A, A> r) {
		if (!l.src.equals(r.src)) {
			throw new RuntimeException();
		}
		XCtx<Pair<Triple<A,A,List<A>>,Triple<A,A,List<A>>>> dst = prod(l.dst, r.dst);
		
		Map em = new HashMap<>();
		for (A x : l.src.terms()) {
			List<A> x1 = l.em.get(x);
			Triple t1 = l.dst.find(l.dst.getKB(), new Triple<A,A,List<A>>((A)"_1", l.dst.type(x1).second, x1), l.dst.cat().hom((A)"_1", l.dst.type(x1).second));

			List<A> x2 = r.em.get(x);
			Triple t2 = r.dst.find(r.dst.getKB(), new Triple<A,A,List<A>>((A)"_1", r.dst.type(x2).second, x2), r.dst.cat().hom((A)"_1", r.dst.type(x2).second));

			List xl = new LinkedList();
			xl.add(new Pair<>(t1, t2));
			em.put(x, xl);
		}
		
		for (Object x : l.src.allTerms()) {
			if (em.containsKey(x)) {
				continue;
			}
			List z = new LinkedList<>();
			z.add(x);
			em.put(x, z); 
		}

		
		return new XMapping(l.src, dst, em, "homomorphism");
	}

	//TODO: constants
	public static XCtx flower(Flower e, XCtx I) {
		Set ids = new HashSet<>(I.schema.ids);
		Map types = new HashMap<>(I.schema.types);
		Set eqs = new HashSet<>(I.schema.eqs);
		
		ids.add("_Q");
		types.put("_Q", new Pair<>("_Q", "_Q"));
//		types.put("!__Q", new Pair<>("_Q", "_1"));  needed?
		
		for (Entry<String, String> k : e.from.entrySet()) {
			types.put(k.getKey(), new Pair<>("_Q", k.getValue()));
		}
		eqs.addAll(e.where);

		XCtx c = new XCtx(ids, types, eqs, I.global, null, "schema");
		Map em = new HashMap<>();
		for (Object o : I.schema.allTerms()) {
			List l = new LinkedList<>();
			l.add(o);
			em.put(o, l);
		}
		XMapping m = new XMapping(I.schema, c, em, "mapping");
		
		XCtx J = m.pi(I);
		
		Set ids2 = new HashSet<>();
		Map types2 = new HashMap<>();
		Set eqs2 = new HashSet<>();
		Map em2 = new HashMap<>();
		ids2.add("_Q");
		types2.put("_Q", new Pair<>("_Q", "_Q"));
		List ll = new LinkedList<>();
		ll.add("_Q");
		em2.put("_Q", ll);
//		System.out.println("C is " + c);
		for (Entry o : e.select.entrySet()) {
			//System.out.println("trying " + o.getValue());
			Object tgt = c.type((List)o.getValue()).second;
			types2.put(o.getKey(), new Pair<>("_Q", tgt));
			if (!I.global.allTerms().contains(tgt)) {
				ids2.add(tgt);
				types2.put(tgt, new Pair<>(tgt, tgt));
			}
			em2.put(o.getKey(), o.getValue());
		}
		XCtx c2 = new XCtx(ids2, types2, eqs2, I.global, null, "schema");
		for (Object o : c2.allTerms()) {
			if (em2.containsKey(o)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(o);
			em2.put(o, l);
		}
		XMapping m2 = new XMapping(c2, c, em2, "mapping"); 
		
		return m2.delta(J);
	}
	
	private static Set<Flower> normalize(FLOWER2 e) {
		Set<Flower> ret = new HashSet<>();
		
		if (e.where == null) {
			Flower f = new Flower(e.select, e.from, new LinkedList<>(), e.src);
			f.ty = e.ty;
			ret.add(f);
			return ret;
		}
		
		if (e.where.lhs != null && e.where.rhs != null) {
			List<Pair<List<String>, List<String>>> l = new LinkedList<>();
			l.add(new Pair<>(e.where.lhs, e.where.rhs));
			Flower f = new Flower(e.select, e.from, l, e.src);
			f.ty = e.ty;
			ret.add(f);
			return ret;
		}
		
		e.where.normalize();
		
		List<List<Pair<List<String>, List<String>>>> ll = e.where.fromOr();
		for (List<Pair<List<String>, List<String>>> l : ll) {
			Flower f = new Flower(e.select, e.from, l, e.src);
			f.ty = e.ty;
			ret.add(f);
		}
		return ret;
	}
	
	public static XCtx FLOWER(FLOWER2 e0, XCtx I) {
		Set ids = new HashSet<>(I.schema.ids);
		Map types = new HashMap<>(I.schema.types);
		Set eqs = new HashSet<>(I.schema.eqs);
		
		Set<Flower> flowers = normalize(e0);
		
		int i = 0;
		for (Flower e : flowers) {
			String qi = "_Q" + i;
			ids.add(qi);
			types.put(qi, new Pair<>(qi, qi));

			for (Entry<String, String> k : e.from.entrySet()) {
				types.put(qi + k.getKey(), new Pair<>(qi, k.getValue()));
			}
			//!__Q -> !__Q1
			for (Pair<List<String>, List<String>> eq : e.where) {
				Function<String, String> f = x -> e.from.keySet().contains(x) ? qi + x : x;
				List<String> lhs = eq.first.stream().map(f).collect(Collectors.toList());
				List<String> rhs = eq.second.stream().map(f).collect(Collectors.toList());
				Function<String, String> g = x -> x.equals("!__Q") ? "!_" + qi : x;
				lhs = lhs.stream().map(g).collect(Collectors.toList());
				rhs = rhs.stream().map(g).collect(Collectors.toList());
				eqs.add(new Pair<>(lhs, rhs));
			}
			i++;
		}
		
		XCtx c = new XCtx(ids, types, eqs, I.global, null, "schema");
		Map em = new HashMap<>();
		for (Object o : I.schema.allTerms()) {
			List l = new LinkedList<>();
			l.add(o);
			em.put(o, l);
		}
		XMapping m = new XMapping(I.schema, c, em, "mapping");
		
		XCtx J = m.pi(I);
		
		Set ids2 = new HashSet<>();
		Map types2 = new HashMap<>();
		Set eqs2 = new HashSet<>();
		Map em2 = new HashMap<>();
		
		i = 0;
		for (Flower e : flowers) {
			String qi = "_Q" + i;
			ids2.add(qi);
			types2.put(qi, new Pair<>(qi, qi));
			List ll = new LinkedList<>();
			ll.add(qi);
			em2.put(qi, ll);
			for (Entry<String, List<String>> o : e.select.entrySet()) { 
				Function<String, String> f = x -> e.from.keySet().contains(x) ? qi + x : x;
				List<String> lll = o.getValue().stream().map(f).collect(Collectors.toList());

				Object tgt = c.type(lll).second;
				types2.put(qi + o.getKey(), new Pair<>(qi, tgt));
				if (!I.global.allTerms().contains(tgt)) {
					ids2.add(tgt);
					types2.put(tgt, new Pair<>(tgt, tgt));
				}
				em2.put(qi + o.getKey(), lll);
			}
			i++;
		}
		XCtx c2 = new XCtx(ids2, types2, eqs2, I.global, null, "schema");
		for (Object o : c2.allTerms()) {
			if (em2.containsKey(o)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(o);
			em2.put(o, l);
		}
		XMapping m2 = new XMapping(c2, c, em2, "mapping"); 
		
		XCtx K = m2.delta(J);
		
		Set ids3 = new HashSet<>();
		Map types3 = new HashMap<>();
		Set eqs3 = new HashSet<>();
		Map em3 = new HashMap<>();
		
		ids3.add("_Q");
		types3.put("_Q", new Pair<>("_Q", "_Q"));

		i = 0;
		for (Flower e : flowers) {
			String qi = "_Q" + i;
			List ll = new LinkedList<>();
			ll.add("_Q");
			em3.put(qi, ll);
			List jj = new LinkedList<>();
			jj.add("!__Q");
			em3.put("!_" + qi, jj);

			for (Entry<String, List<String>> o : e.select.entrySet()) { 
				Function<String, String> f = x -> e.from.keySet().contains(x) ? qi + x : x;
				List<String> lll = o.getValue().stream().map(f).collect(Collectors.toList());

				Object tgt = c.type(lll).second;
				types3.put(o.getKey(), new Pair<>("_Q", tgt));
				if (!I.global.allTerms().contains(tgt)) {
					ids3.add(tgt);
					types3.put(tgt, new Pair<>(tgt, tgt));
				}
				List u = new LinkedList();
				u.add(o.getKey());
				em3.put(qi + o.getKey(), u);
			}
			i++;
		}
		XCtx c3 = new XCtx(ids3, types3, eqs3, I.global, null, "schema");
		for (Object o : c2.allTerms()) {
			if (em3.containsKey(o)) {
				continue;
			}
			List l = new LinkedList<>();
			l.add(o);
			em3.put(o, l);
		}

		XMapping m3 = new XMapping(c2, c3, em3, "mapping"); 
		
		XCtx L = m3.apply0(K);
		
		return L;
	}
	
	
	
	
	
}
