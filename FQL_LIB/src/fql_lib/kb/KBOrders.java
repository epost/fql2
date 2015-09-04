package fql_lib.kb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import fql_lib.Pair;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;

public class KBOrders {
	
	

	public static <C, V> Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> pogt(
			Function<Pair<C, C>, Boolean> gt, String which) {
		return new Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean>() {
			@Override
			public Boolean apply(Pair<KBExp<C, V>, KBExp<C, V>> xxx) {
				KBExp<C, V> s = xxx.first;
				KBExp<C, V> t = xxx.second;

				if (s.hasAsSubterm(t) && !t.equals(s)) {
					return true;
				}
				if (t.isVar) {
					return !t.equals(s) && s.vars().contains(t.getVar().var);
				}
				if (s.isVar) {
					return false;
				} 

				KBApp<C, V> s0 = s.getApp();
				KBApp<C, V> t0 = t.getApp();
				C f = s0.f;
				C g = t0.f;
				if (gt.apply(new Pair<>(f, g))) { 
					for (KBExp<C, V> ti : t0.args) {
						if (!apply(new Pair<>(s, ti))) {
							return false;
						}
					}
					return true;
				} else if (f.equals(g)) {
					if (which == "rpo") {
						return baglt(this).apply(new Pair<>(tobag(s0.args), tobag(t0.args)));
					} else if (which == "lpo") {
						return lexlt(this).apply(new Pair<>(t0.args, s0.args));
					} else {
						throw new RuntimeException();
					}
				} else {
					for (KBExp<C, V> si : s0.args) {
						if (apply(new Pair<>(si, t)) || si.equals(t)) {
							return true;
						}
					}
					return false;
				}
			}
		};
	}

	private static <E> Map<E, Integer> tobag(List<E> l) {
		Map<E, Integer> ret = new HashMap<>();
		for (E e : l) {
			Integer i = ret.get(e);
			if (i == null) {
				i = 0;
			}
			ret.put(e, i + 1);
		}
		return ret;
	}

	private static <E> Function<Pair<List<E>, List<E>>, Boolean> lexlt(
			Function<Pair<E, E>, Boolean> gt) {
		return new Function<Pair<List<E>, List<E>>, Boolean>()  {
				@Override
			public Boolean apply(Pair<List<E>, List<E>> xxx) {
					List<E> A = xxx.first; 
					List<E> B = xxx.second;
					if (A.size() != B.size()) {
						throw new RuntimeException();
					}
					if (A.isEmpty()) {
						return false; //heads were equal, so are args
					}
					E a = A.get(0);
					E b = B.get(0);
					if (gt.apply(new Pair<>(b,a))) {
						return true;
					}
					else if (gt.apply(new Pair<>(a,b))) {
						return false;
					}
					else if (a.equals(b)) {
						return apply(new Pair<>(A.subList(1, A.size()), B.subList(1, B.size())));
					}
					return false;
			}
		};
	}
	private static <E> Function<Pair<Map<E, Integer>, Map<E, Integer>>, Boolean> baglt(
			Function<Pair<E, E>, Boolean> gt) {
		return xxx -> {
			Map<E, Integer> M = new HashMap<>(xxx.first);
			Map<E, Integer> N = new HashMap<>(xxx.second);
			Set<E> E = new HashSet<>();
			E.addAll(M.keySet());
			E.addAll(N.keySet());
			for (E z : E) {
				if (!M.containsKey(z)) {
					M.put(z, 0);
				}
				if (!N.containsKey(z)) {
					N.put(z, 0);
				}
			}
			boolean b = true;
			for (E y : E) {
				if (!(M.get(y) > N.get(y))) {
					continue;
				}
				for (E x : E) {
					if (gt.apply(new Pair<>(x, y)) && M.get(x) < N.get(y)) {
						continue;
					} else {
						b = false;
						break;
					}
				}
			}
			return !M.equals(N) && b;
		};
	}
	
	
	
	
	
}
