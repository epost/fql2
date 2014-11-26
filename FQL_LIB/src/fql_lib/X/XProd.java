package fql_lib.X;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.Chc;
import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Unit;
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

	//going to need bigger schema here, for typing
/*	public static <C> Triple<C, C, List<C>> eval(List<String> eq, Map<String, Triple<C, C, List<C>>> tuple, XCtx S, XCtx<C> I) {
		Triple<C, C, List<C>> sofar = tuple.get(eq.get(0));
		if (sofar == null) {
			sofar = I.type(c)
		}

		///if (sofar == null) {
		//	return null;
		//}

		
	//	String s = eq.get(0);
		
		for (String edge : eq.subList(1, eq.size())) {
			Triple<C, C, List<C>> t = tuple.get(edge);
			if (t != null) {
				if (sofar == null) {
					sofar = t;
					continue;
				}
				sofar = I.cat().compose(sofar, t);
				continue;
			} 
			
			
		}
		
		return sofar;
	}
	*/
	
	 public static <C> List subst2(List<String> eq, Map<String, Triple<C, C, List<C>>> tuple, Set<String> keys, Set xxx) {
		List ret = eq.stream().flatMap(x -> { 
			List l = new LinkedList<>();
			if (tuple.containsKey(x)) {
				l.add("!__Q");
				l.add(tuple.get(x).first);
				l.addAll(tuple.get(x).third);
				return l.stream();
			} else if (keys.contains(x)) { 
				l.add(x);
				xxx.add(new Unit());
				return l.stream();
			}	else {
				l.add(x);
				return l.stream();
			}
		}).collect(Collectors.toList());
		
		return ret;
	} 
	
	 public static <C> List subst(List<String> eq, Map<String, Triple<C, C, List<C>>> tuple) {
		List ret = eq.stream().flatMap(x -> { 
			List l = new LinkedList<>();
			if (tuple.containsKey(x)) {
				l.add("!__Q");
				l.add(tuple.get(x).first);
				l.addAll(tuple.get(x).third);
				return l.stream();
			} else {
				l.add(x);
				return l.stream();
			}
		}).collect(Collectors.toList());
		
		return ret;
	} 

	 //FROM schema, SELECT schema, 
	public static <C> XCtx fast_flower(Flower flower, XCtx<C> I, XCtx<C> S, XCtx<C> Z) {
		System.out.println("happening");
		Set<C> ids = new HashSet<>();
		ids.addAll(S.ids);
		ids.addAll(I.ids);
		ids.addAll(I.global.ids);
		Map types = new HashMap<>();
		types.putAll(S.types);
		types.putAll(I.types);
		types.putAll(I.global.types);
		Set<Pair<List<C>, List<C>>> eqs = new HashSet<>();
		eqs.addAll(S.eqs);
		eqs.addAll(I.eqs);
		eqs.addAll(I.global.eqs);
		
		XCtx<C> IS = new XCtx<C>(ids, types, eqs, I.global, S, "instance");
		
		
		Set<Map<String, Triple<C, C, List<C>>>> ret = new HashSet<>();
		Map<String, Triple<C, C, List<C>>> m = new HashMap<>();
		ret.add(m);
		
		for (String var : flower.from.keySet()) {
			String node = flower.from.get(var);
			Set<Map<String, Triple<C, C, List<C>>>> ret2 = new HashSet<>();
			for (Map<String, Triple<C, C, List<C>>> tuple : ret) {
				outer: for (Triple<C, C, List<C>> t : I.cat().hom((C)"_1", (C)node)) {
//					System.out.println("t " + t);
					Map<String, Triple<C, C, List<C>>> merged = new HashMap<>(tuple);
					merged.put(var, t);
					
					
					for (Pair<List<String>, List<String>> eq : flower.where) {
					//	System.out.println("eq " + eq);
						Set xxx = new HashSet();
						List lhs = subst2(eq.first, merged, flower.from.keySet(), xxx);
						Set yyy = new HashSet();
						List rhs = subst2(eq.second, merged, flower.from.keySet(), yyy);
				//		System.out.println("lhs " + lhs);
				//		System.out.println("rhs " + rhs);

						if (xxx.isEmpty() && yyy.isEmpty() && !IS.getKB().equiv(lhs, rhs)) {
				//			System.out.println("not equiv");
							continue outer;
						}
					}
					ret2.add(merged);
			//		System.out.println("equiv");
				}
			}
			ret = ret2;
		}
		/*
		System.out.println("pre-ret is " + ret);
		Iterator<Map<String, Triple<C, C, List<C>>>> it = ret.iterator();
		while (it.hasNext()) {
			Map<String, Triple<C, C, List<C>>> merged = it.next();
			for (Pair<List<String>, List<String>> eq : flower.where) {
				System.out.println("eq " + eq);
				List lhs = subst(eq.first, merged);
				List rhs = subst(eq.second, merged);
				System.out.println("lhs " + lhs);
				System.out.println("rhs " + rhs);
				if (!IS.getKB().equiv(lhs, rhs)) {
					System.out.println("not equiv");
					it.remove();
					break;
				}
				System.out.println("equiv");
			}
		}
		System.out.println("ret is " + ret); */
		
		ids = new HashSet<>();
		ids.addAll(Z.ids);
		ids.addAll(Z.global.ids);
		types = new HashMap<>();
		types.putAll(Z.types);
		types.putAll(Z.global.types);
		eqs = new HashSet<>();
		eqs.addAll(Z.eqs);
		eqs.addAll(Z.global.eqs);
		//k : 1 -> Q
		//k ; f : 1 -> adom
		for (Map<String, Triple<C, C, List<C>>> k : ret) {
			types.put(k, new Pair("_1", "_Q"));
			for (String edge : flower.select.keySet()) {
				List lhs = new LinkedList();
				lhs.add(k);
				lhs.add(edge);
				if (!I.global.ids.contains(IS.type((List)flower.select.get(edge)).second)) {
					throw new RuntimeException("Cannot fast_flower with selection paths into non-types");
				}
				List rhs0 = subst(flower.select.get(edge), k);
				//this should give a constant
				List rhsX = IS.getKB().normalize("", rhs0);
				//Triple<C,C,List<C>> rhs2 = IS.find(IS.getKB(), new Triple("_1", IS.type(rhs0).second, rhsX), I.global.cat().hom((C)"_1", IS.type(rhs0).second));
				if (rhsX.size() != 2) {
					throw new RuntimeException();
				}
				//System.out.println("Cannot find " + new Triple("_1", IS.type(rhs0).second, rhsX) + "\n\nin\n\n" + I.global.cat().hom((C)"_1", IS.type(rhs0).second));
				List rh3 = new LinkedList<>();
				rh3.add(rhsX.get(1));
				//rh3.addAll(rhs2.third);
			eqs.add(new Pair(lhs, rh3));
			}
		} //TODO: must end on type
		
		XCtx<C> J = new XCtx<C>(ids, types, eqs, I.global, Z, "instance");
		//J.saturated = true; TODO
		return J;
	}
	
/*	public static <C> XCtx fast_flower(Flower flower, XCtx<C> I) {
		Set<Map<String, Triple<C, C, List<C>>>> ret = new HashSet<>();
		Map<String, Triple<C, C, List<C>>> m = new HashMap<>();
		ret.add(m);
		
		
		for (String var : flower.from.keySet()) {
			String node = flower.from.get(var);
			Set<Map<String, Triple<C, C, List<C>>>> ret2 = new HashSet<>();
			for (Map<String, Triple<C, C, List<C>>> tuple : ret) {
				for (Triple<C, C, List<C>> t : I.cat().hom((C)"_1", (C)node)) {
					Map<String, Triple<C, C, List<C>>> merged = new HashMap<>(tuple);
					merged.put(var, t);
					boolean add = true;
					for (Pair<List<String>, List<String>> eq : flower.where) {
						Object lhs = eval(eq.first, merged, I);
						Object rhs = eval(eq.second, merged, I);
						if (lhs != null && rhs != null && !lhs.equals(rhs)) {
							add = false;
							break;
						}
					}
					if (add) {
						ret2.add(merged);
					}
				}
			}
			ret = ret2;
		}
		
		
		throw new RuntimeException();
	} */
	
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
		
		if (DEBUG.debug.direct_flower) {
			return fast_flower(e, I, c, c2);
		}
		
		XCtx J = m.pi(I);
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
		
		XCtx J = m.pi(I);
		XCtx K = m2.delta(J);
		XCtx L = m3.apply0(K);
		
		return L.rel();
	}
	
	
	
	
	
}
