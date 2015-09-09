package fql_lib.kb;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Util;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;

public class KB<C, V> {
	
	
	/* void validateRules() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
			if (!r.first.vars().containsAll(r.second.vars())) {
				throw new RuntimeException("bad rule: " + r + " and lhs > rhs " 
			+ gt.apply(new Pair<>(r.first, r.second)) + " and rhs > lhs " + gt.apply(new Pair<>(r.second, r.first)));
			}
		}
	} */
	 
	private boolean isComplete = false;
	private List<Pair<KBExp<C, V>, KBExp<C, V>>> E;
	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> R;
	private Iterator<V> fresh;
	private Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt;
	
	private static <C, V> Pair<KBExp<C, V>, KBExp<C, V>> freshen(Iterator<V> fresh,
			Pair<KBExp<C, V>, KBExp<C, V>> eq) {
		Set<V> vars = new HashSet<>();
		KBExp<C, V> lhs = eq.first;
		KBExp<C, V> rhs = eq.second;
		vars.addAll(lhs.vars());
		vars.addAll(rhs.vars());
		Map<V, KBExp<C, V>> subst = new HashMap<>();
		for (V v : vars) {
			subst.put(v, new KBVar<>(fresh.next()));
		}
		return new Pair<>(lhs.subst(subst), rhs.subst(subst));
	}

	private static <C,V> Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> freshenMap(
			Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> eq) {
		Set<V> vars = new HashSet<>();
		KBExp<C, V> lhs = eq.first;
		KBExp<C, V> rhs = eq.second;
		vars.addAll(lhs.vars());
		vars.addAll(rhs.vars());
		Map<V, KBExp<C, V>> subst = new HashMap<>();
		Map<V, KBExp<C, V>> subst_inv = new HashMap<>();
		for (V v : vars) {
			V fr = fresh.next();
			subst.put(v, new KBVar<>(fr));
			subst_inv.put(fr, new KBVar<>(v));
		}
		return new Pair<>(subst, subst_inv);
	}
	
	public KB(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0,
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		this.R = new HashSet<>();
		this.gt = gt0;
		this.fresh = fresh;
		this.E = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> eq : E0) {
			E.add(eq);
		}
	}



	private void compose() {
		Pair<KBExp<C, V>, KBExp<C, V>> to_remove = null;
		Pair<KBExp<C, V>, KBExp<C, V>> to_add = null;
		do {
			to_remove = null;
			to_add = null;
			for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0 = new HashSet<>(R);
				R0.remove(r);
				KBExp<C, V> new_rhs = red(null, fresh, R0, r.second);
				if (!new_rhs.equals(r.second)) {
					to_remove = r;
					to_add = new Pair<>(r.first, new_rhs);
					break;
				}
			}
			if (to_remove != null) {
				R.remove(to_remove);
				R.add(to_add);
			}
		} while (to_remove != null);
	}
/*
	private void reduceRhsOfRByR() {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newR = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
			newR.add(new Pair<>(r.first, red(fresh, R, r.second)));
		}
		R = newR;
	}
	*/

	private void simplify() {
		Map<KBExp<C,V>, KBExp<C,V>> cache = new HashMap<>();  //helped 2x during xfiles tests

		List<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			KBExp<C, V>	lhs_red = red(cache, fresh, R, e.first);
			KBExp<C, V> rhs_red = red(cache, fresh, R, e.second);
			if (!lhs_red.equals(rhs_red)) {
				add(newE, new Pair<>(lhs_red, rhs_red));
			}
		}
		E = newE;
	}


	/* void deduce() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r1 : R) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> r2 : R) {
				E.addAll(cp(r1, r2));
				//E.addAll(cp(r2, r1));
			}
		}
	} */

	/*void removeSubsumedOfE() {
		for (;;) {
			Pair<KBExp<C, V>, KBExp<C, V>> cand = findOneSubsumed();
			if (cand == null) {
				return;
			}
			//System.out.println("found " + cand);
			remove(E, cand);
		}
	}  */
/*
	private Pair<KBExp<C, V>, KBExp<C, V>> findOneSubsumed() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> cand : E) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> other : E) {
				if (cand.equals(other)) {
					continue;
				}
				if (subsumes(fresh, cand, other)) {
					return cand;
				}
				if (subsumes(fresh, new Pair<>(cand.second, cand.first), other)) {
					return cand;
				}
				if (subsumes(fresh, cand, new Pair<>(other.second, other.first))) {
					return cand;
				}
			}
		}
		return null;
	} */
	
	private static <X> void remove(Collection<X> X, X x) {
		while (X.remove(x));
	}
	
	private static <X> void add(Collection<X> X, X x) {
		if (!X.contains(x)) {
			X.add(x);
		}
	}
	
	private static <X> void addFront(List<X> X, X x) {
		if (!X.contains(x)) {
			X.add(0, x);
		}
	}
	
	private static <X> void addAll(Collection<X> X, Collection<X> x) {
		for (X xx : x) {
			add(X, xx);
		}
	}

	static <C, V> boolean subsumes(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> cand,
			Pair<KBExp<C, V>, KBExp<C, V>> other) {
		
		Pair<KBExp<C, V>, KBExp<C, V>> candX = cand; 
		
		if (!Collections.disjoint(candX.first.vars(), other.first.vars()) ||
			!Collections.disjoint(candX.first.vars(), other.second.vars()) ||
			!Collections.disjoint(candX.second.vars(), other.first.vars())||
			!Collections.disjoint(candX.second.vars(), other.second.vars())) {	
			candX = freshen(fresh, cand);
		}
		 
		List<KBExp<C, V>> l = new LinkedList<>(); l.add(candX.first); l.add(candX.second);
		KBApp<C, V> cand0 = new KBApp<C, V>((C) "", l);

		List<KBExp<C, V>> r = new LinkedList<>(); r.add(other.first); l.add(other.second);
		KBApp<C, V> other0 = new KBApp<C, V>((C) "", r);
		
		Map<V, KBExp<C, V>> subst = KBUnifier.findSubst(other0, cand0);
		
		return (subst != null);
	}

	private static <X> void sortByStrLen(List<X> l) {
		l.sort(new Comparator<X>() {
			@Override
			public int compare(X o1, X o2) {
				if (o1.toString().length() > o2.toString().length()) {
					return 1;
				} else if (o1.toString().length() < o2.toString().length()) {
					return -1;
				}
				return 0;
			}
		});
	}
	
	
	private Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen = new HashSet<>();	
	private int count = 0;

	public void complete() {
		while (!step());
	}
		
	private boolean step() {
		if (count > DEBUG.debug.opl_iterations) {
			throw new RuntimeException("Exceeded iteration limit");
		}

		System.out.println("iteration " + count);
		count++;

		if (E.isEmpty()) {
			isComplete = true;
			return true;
		}		

		Pair<KBExp<C, V>, KBExp<C, V>> st = E.get(0);
		
		KBExp<C, V> s0 = st.first;
		KBExp<C, V> t0 = st.second;
		KBExp<C, V> a, b;
		if (gt.apply(new Pair<>(s0, t0))) {
			a = s0; b = t0;
		} else if (gt.apply(new Pair<>(t0, s0))) {
			a = t0; b = s0;
		} else if (s0.equals(t0)) {
			remove(E, st); return false; //in case x = x coming in
		}
		else {
			if (DEBUG.debug.opl_unfailing) {
				remove(E, st); add(E, st); return false; 					
			} else {
				throw new RuntimeException("Cannot orient " + s0 + " and " + t0 + " equal: " + s0.equals(t0));
			}
		}
		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(a, b);
			
		R.add(ab);
		List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = filterSubsumed(allcps(seen, ab));
			
		addAll(E, CP);
		remove(E, st); 
			
		compose();
		collapseBy(ab);

		simplify(); //definitely needed... cuts down on number of iterations
			
		if (DEBUG.debug.opl_sort_cps) {
			sortByStrLen(E);
		}
		
		return false;	
	} 
	

	private List<Pair<KBExp<C, V>, KBExp<C, V>>> filterSubsumed(
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> CPX) {
		List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = new LinkedList<>();
		outer: for (Pair<KBExp<C, V>, KBExp<C, V>> cand : CPX) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
				if (subsumes(fresh, cand, e)) {
					continue outer; 
				}
			}
			CP.add(cand);
		}
		return CP;
	}

	private void collapseBy(Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> AB = Collections.singleton(ab);
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = R.iterator();
		while (it.hasNext()) {
		Pair<KBExp<C, V>, KBExp<C, V>> r = it.next();
			if (r.equals(ab)) {
				continue;
			}
			KBExp<C, V> lhs = red(null, fresh, AB, r.first);
			if (!r.first.equals(lhs)) {
				addFront(E, new Pair<>(lhs, r.second));	
				it.remove();
			} 
		}
	}


	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps(
			Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen,
			Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> gd : R) {
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> s;
			if (!seen.contains(new Pair<>(ab, gd))) {
				s = cp( ab, gd);
				ret.addAll(s);
				seen.add(new Pair<>(ab, gd));
			}

			if (!seen.contains(new Pair<>(gd, ab))) {
				s = cp(gd, ab);
				ret.addAll(s);
				seen.add(new Pair<>(gd, ab));
			}
		}
		return ret;
	}

	// rules in R are gd
	private Map<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>, Set<Pair<KBExp<C, V>, KBExp<C, V>>>> cp_cache = new HashMap<>();
	private  Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Pair<KBExp<C, V>, KBExp<C, V>> gd, Pair<KBExp<C, V>, KBExp<C, V>> ab0) {
		Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>> entry = new Pair<>(gd, ab0);
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> value = cp_cache.get(entry);
		if (value != null) {
			return value;
		}		
		
		Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> abm = freshenMap(fresh, ab0);
		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(ab0.first.subst(abm.first), ab0.second.subst(abm.first));

		Map<V, KBExp<C, V>> inv = new HashMap<>();
		Set<V> vars = new HashSet<>();
		vars.addAll(gd.first.vars());
		vars.addAll(gd.second.vars());
		for (V k : abm.second.keySet()) {
			KBExp<C, V> v = abm.second.get(k);
			if (!Collections.disjoint(vars, v.vars())) {
				inv.put(k, v);
			}
		}

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> retX = gd.first.cp(inv, new LinkedList<>(), ab.first,
				ab.second, gd.first, gd.second, true);

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> c : retX) {
			ret.add(new Pair<>(c.first.subst(inv), c.second.subst(inv)));
		}
		
		cp_cache.put(entry, ret);
		
		return ret;
	}

	public boolean eq(KBExp<C, V> lhs, KBExp<C, V> rhs) {
		KBExp<C, V> lhs0 = nf(lhs);
		KBExp<C, V> rhs0 = nf(rhs);
		if (lhs0.equals(rhs0)) {
			return true;
		}
		
		if (isComplete) {
			return false;
		}
		
		step();
		return eq(lhs, rhs);
	}
	
	public KBExp<C, V> nf(KBExp<C, V> e) {
		if (!isComplete) {
			throw new RuntimeException("Cannot find normal form for incomplete system.");
		}
		return red(null, fresh, R, e);
	}
	private static <C, V> KBExp<C, V> red(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R,
			KBExp<C, V> e) {
		int i = 0;
		KBExp<C, V> orig = e;
		for (;;) {
			i++;
			if (i > 32) {
				throw new RuntimeException("Reduction taking too long: " + orig + " goes to " + e + " under " + R);
			}
			KBExp<C, V> e0 = step(cache, fresh, R, e);
			if (e.equals(e0)) {
				return e;
			}
			e = e0;
		}
	}

	
	private static <C, V> KBExp<C, V> step1(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> e0) {
		KBExp<C, V> e = e0;
		if (cache != null && cache.containsKey(e)) {
			return cache.get(e);
		}
		for (Pair<KBExp<C, V>, KBExp<C, V>> r0 : R) {
			Pair<KBExp<C, V>, KBExp<C, V>> r = r0;
			if (!Collections.disjoint(r.first.vars(), e.vars()) || !Collections.disjoint(r.second.vars(), e.vars())) {
				r = freshen(fresh, r0);
			}
			
			KBExp<C, V> lhs = r.first;
			KBExp<C, V> rhs = r.second;
//			if (!Collections.disjoint(e.vars(), lhs.vars())) { 
	//			throw new RuntimeException("Not disjoint: " + e + " and " + lhs);
		//	}
			Map<V, KBExp<C, V>> s = KBUnifier.findSubst(lhs, e);
			if (s == null) {
				continue;
			}
			/* if (!s.keySet().equals(lhs.vars())) { 
				throw new RuntimeException("Trying to reduce " + e + " by " + lhs + " gives subst "
						+ s);
			} */
			e = rhs.subst(s);
		}
		if (cache != null) {
			cache.put(e0, e);
		}
		return e;
	}

	/*private static class StepVisitor<C,V> implements KBExpVisitor<C, V, KBExp<C, V>, Unit> {
		Map<KBExp<C,V>, KBExp<C,V>> cache;
		Iterator<V> fresh;
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> R;
		
		public StepVisitor(Map<KBExp<C, V>, KBExp<C, V>> cache, Iterator<V> fresh,
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> r) {
			this.cache = cache;
			this.fresh = fresh;
			R = r;
		}

		@Override
		public KBExp<C, V> visit(Unit env, KBVar<C, V> e) {
			return e; //step1(cache, fresh, R, e);
		}

		@Override
		public KBExp<C, V> visit(Unit env, KBApp<C, V> e) {
			List<KBExp<C, V>> args0 = new LinkedList<>();
			for (KBExp<C, V> arg : e.args) {
				args0.add(arg.accept(env, this));
			}
			KBApp<C, V> ret = new KBApp<>(e.f, args0);
			return step1(cache, fresh, R, ret);
		}
	} */
	
	private static <C, V> KBExp<C, V> step(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> ee) {
		if (ee.isVar) {
			return ee; //variables can't reduce
		} else {
			KBApp<C, V> e = ee.getApp();
			List<KBExp<C, V>> args0 = new LinkedList<>();
			for (KBExp<C, V> arg : e.args) {
				args0.add(step(cache, fresh, R, arg));
			}
			KBApp<C, V> ret = new KBApp<>(e.f, args0);
			return step1(cache, fresh, R, ret);
		} 
		
//		return e.accept(new Unit(), new StepVisitor<>(cache, fresh, R));
	}

	private String printEqs() {
		String ret = "Equations:\n\n";
		ret += Util.sep(E.stream().map(x -> {
			return x.first + " = " + x.second;
		}).collect(Collectors.toList()), "\n");
		return ret;
	}

	private String printReds() {
		String ret = "Reductions:\n\n";
		ret += Util.sep(R.stream().map(x -> {
			return x.first + " -> " + x.second;
		}).collect(Collectors.toList()), "\n");
		return ret;
	}

	@Override
	public String toString() {
		return printEqs() + "\n\n" + printReds();
	}
	
	public String printKB() {
		
		KB<String, String> kb = (KB<String, String>) this;
		
		List<String> R0 = new LinkedList<>();
		for (Pair<KBExp<String, String>, KBExp<String, String>> r : kb.R) {
			int i = 0;
			Map<String, KBExp<String, String>> m = new HashMap<>();
			for (String v : r.first.vars()) {
				if (v.startsWith("_v") && !m.containsKey(v)) {
					m.put(v, new KBVar<String, String>("v" + i++));
				}
			}
			for (String v : r.second.vars()) {
				if (v.startsWith("_v") && !m.containsKey(v)) {
					m.put(v, new KBVar<String, String>("v" + i++));
				}
			}
			R0.add(r.first.subst(m) + " -> " + r.second.subst(m));
		}
		R0.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		return Util.sep(R0, "\n\n");
	}
	
}
