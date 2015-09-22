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
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;

public class KB<C, V> {
	 
	protected boolean isComplete = false;
	protected boolean isCompleteGround = false;
	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> E;
	protected Set<Pair<KBExp<C, V>, KBExp<C, V>>> R;
	protected Iterator<V> fresh;
	protected Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt;
	protected Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen = new HashSet<>();	
//	protected Map<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>, Set<Pair<KBExp<C, V>, KBExp<C, V>>>> cp_cache = new HashMap<>();
//	protected Function<KBExp<C, V>, KBExp<C, V>> reducer;
	
	protected int count = 0;

	protected static <C, V> Pair<KBExp<C, V>, KBExp<C, V>> freshen(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> eq) {
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

	protected static <C,V> Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> freshenMap(
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
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, 
			Iterator<V> fresh) {
		this.R = new HashSet<>();
		this.gt = gt0;
		this.fresh = fresh;
		this.E = new LinkedList<>();
//		this.reducer = reducer;
		for (Pair<KBExp<C, V>, KBExp<C, V>> eq : E0) {
			E.add(eq);
		}
	}
	
/*	protected KB(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0,
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		this(E0, gt0, fresh);
		for (Pair<KBExp<C, V>, KBExp<C, V>> eq : R0) {
			R.add(eq);
		}
	} */

	protected static <X> void remove(Collection<X> X, X x) {
		while (X.remove(x));
	}
	
	protected static <X> void add(Collection<X> X, X x) {
		if (!X.contains(x)) {
			X.add(x);
		}
	}
	
	protected static <X> void addFront(List<X> X, X x) {
		if (!X.contains(x)) {
			X.add(0, x);
		}
	}
	
	protected static <X> void addAll(Collection<X> X, Collection<X> x) {
		for (X xx : x) {
			add(X, xx);
		}
	}

	protected void sortByStrLen(List<Pair<KBExp<C,V>, KBExp<C,V>>> l) {
		if (!DEBUG.debug.opl_unfailing) {
			l.sort(Util.ToStringComparator);
		} else {
			List<Pair<KBExp<C,V>, KBExp<C,V>>> unorientable = new LinkedList<>();
			List<Pair<KBExp<C,V>, KBExp<C,V>>> orientable = new LinkedList<>();
			for (Pair<KBExp<C, V>, KBExp<C, V>> k : l) {
				if (orientable(k)) {
					orientable.add(k);
				} else {
					unorientable.add(k);
				}
			}
			orientable.sort(Util.ToStringComparator);
			l.clear();
			l.addAll(orientable);
			l.addAll(unorientable);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void complete() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while (!step());
			}
		};				
		Thread t = new Thread(r);
		t.start();
		try {
			t.join(DEBUG.debug.opl_iterations);
			t.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
		if (!isCompleteGround) {
			throw new RuntimeException("Not ground complete after iteration timeout.  Last state:\n\n" + printKB());
		} 
	}
	
	@SuppressWarnings("unchecked")
	protected static <C, V> boolean subsumes(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> cand,
			Pair<KBExp<C, V>, KBExp<C, V>> other) {
		
		Pair<KBExp<C, V>, KBExp<C, V>> candX = cand; 
		
		if (!Collections.disjoint(candX.first.vars(), other.first.vars()) ||
			!Collections.disjoint(candX.first.vars(), other.second.vars()) ||
			!Collections.disjoint(candX.second.vars(), other.first.vars())||
			!Collections.disjoint(candX.second.vars(), other.second.vars())) {	
			//System.out.println("freshening");
			candX = freshen(fresh, cand);
		}
		 
		List<KBExp<C, V>> l = new LinkedList<>(); l.add(candX.first); l.add(candX.second);
		KBApp<C, V> cand0 = new KBApp<C, V>((C) "", l);

		List<KBExp<C, V>> r = new LinkedList<>(); r.add(other.first); r.add(other.second);
		KBApp<C, V> other0 = new KBApp<C, V>((C) "", r);
		
		Map<V, KBExp<C, V>> subst = KBUnifier.findSubst(other0, cand0);
		
		return (subst != null);
	}
	
	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> filterSubsumed(
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

	//TODO: also useful in regular completion?
	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> filterSubsumedBySelf(
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> CPX) {
		List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = new LinkedList<>(CPX);
		
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = CP.iterator();
		while (it.hasNext()) {
			Pair<KBExp<C, V>, KBExp<C, V>> cand = it.next();
			for (Pair<KBExp<C, V>, KBExp<C, V>> e : CP) {
				if (cand.equals(e)) {
					continue;
				}
				if (subsumes(fresh, cand, e)) {
					it.remove();
					break;
				}
				if (subsumes(fresh, cand.reverse(), e)) {
					it.remove();
					break;
				}
				if (subsumes(fresh, cand, e.reverse())) {
					it.remove();
					break;
				}
				//TODO: this one redundant?
				if (subsumes(fresh, cand.reverse(), e.reverse())) {
					it.remove();
					break;
				}
			}
		}
		return CP;
	}
	
	//is also compose2
	protected void compose() {
		Pair<KBExp<C, V>, KBExp<C, V>> to_remove = null;
		Pair<KBExp<C, V>, KBExp<C, V>> to_add = null;
		do {
			to_remove = null;
			to_add = null;
			for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0 = new HashSet<>(R);
				R0.remove(r);
				KBExp<C, V> new_rhs = red(null, E, R0, r.second);
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
	
	// TODO - handle for ground complete using skolemization
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
		if (e.vars().isEmpty()) {
			if (!isCompleteGround) {
				throw new RuntimeException("Cannot find ground normal form for ground incomplete system.");
			}
			return red(null, E, R, e);
		}
		if (!isComplete) {
			throw new RuntimeException("Cannot find normal form for incomplete system.");
		}
		return red(null, E, R, e);
	}
	
	@SuppressWarnings("unchecked")
	public String printKB() {
		KB<String, String> kb = (KB<String, String>) this;
		
		List<String> E0 = new LinkedList<>();
		for (Pair<KBExp<String, String>, KBExp<String, String>> r : kb.E) {
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
			E0.add(stripOuter(r.first.subst(m).toString()) + " = " + stripOuter(r.second.subst(m).toString()));
		}
		E0.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		
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
			R0.add(stripOuter(r.first.subst(m).toString()) + " -> " + stripOuter(r.second.subst(m).toString()));
		}
		R0.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
				
		return (Util.sep(E0, "\n\n") + "\n\n" + Util.sep(R0, "\n\n")).trim();
	}
	
	private static String stripOuter(String s) {
		if (s.startsWith("(") && s.endsWith(")")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	protected KBExp<C, V> red(Map<KBExp<C,V>, KBExp<C,V>> cache, 
			List<Pair<KBExp<C, V>, KBExp<C, V>>> E,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R,
			KBExp<C, V> e) {
		int i = 0;
		KBExp<C, V> orig = e;
		for (;;) {
			i++;
			if (i > DEBUG.debug.opl_red_its) {
				throw new RuntimeException("Reduction taking too long: " + orig + " goes to " + e + " under\n\neqs:" + Util.sep(E,"\n") + "\n\nreds:"+ Util.sep(R,"\n"));
			}
			KBExp<C, V> e0 = step(cache, fresh, E, R, e);
			if (e.equals(e0)) {
				return e;
			}
			e = e0;
		}
	}
	
	protected KBExp<C, V> step(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh,
			List<Pair<KBExp<C, V>, KBExp<C, V>>> E, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> ee) {
		if (ee.isVar) {
			return ee; //variables can't reduce
		} else {
			KBApp<C, V> e = ee.getApp();
			List<KBExp<C, V>> args0 = new LinkedList<>();
			for (KBExp<C, V> arg : e.args) {
				args0.add(step(cache, fresh, E, R, arg));
			}
			KBApp<C, V> ret = new KBApp<>(e.f, args0);
			return step1(cache, fresh, E, R, ret);
		} 
	}
	

	protected void simplify() {
		Map<KBExp<C,V>, KBExp<C,V>> cache = new HashMap<>();  //helped 2x during tests

		List<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			KBExp<C, V>	lhs_red = red(cache, new LinkedList<>(), R, e.first);
			KBExp<C, V> rhs_red = red(cache, new LinkedList<>(), R, e.second);
			if (!lhs_red.equals(rhs_red)) {
				add(newE, new Pair<>(lhs_red, rhs_red));
			}
		}
		E = newE;
	}

	//is not collapse2
	protected void collapseBy(Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> AB = Collections.singleton(ab);
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = R.iterator();
		while (it.hasNext()) {
		Pair<KBExp<C, V>, KBExp<C, V>> r = it.next();
			if (r.equals(ab)) {
				continue;
			}
			KBExp<C, V> lhs = red(null, new LinkedList<>(), AB, r.first);
			if (!r.first.equals(lhs)) {
				addFront(E, new Pair<>(lhs, r.second));	
				it.remove();
			} 
		}
	}

	protected Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps2(
			Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen,
			Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0 = new HashSet<>(E);
		E0.add(ab);
		E0.add(ab.reverse());
		Pair<KBExp<C, V>, KBExp<C, V>> ba = ab.reverse();
		for (Pair<KBExp<C, V>, KBExp<C, V>> gd : E0) {
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> s;
			Pair<KBExp<C, V>, KBExp<C, V>> dg = gd.reverse();

			if (!seen.contains(new Pair<>(ab, gd))) {
				s = cp(ab, gd);
		//		System.out.println(" on " + ab + " and " + gd + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(ab, gd));
			}
			if (!seen.contains(new Pair<>(gd, ab))) {
				s = cp(gd, ab);
		//		System.out.println(" on " + gd + " and " + ab + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(gd, ab));
			}
			if (!seen.contains(new Pair<>(ab, dg))) {
				s = cp(ab, dg);
			//	System.out.println(" on " + ab + " and " + dg + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(ab, dg));
			}
			if (!seen.contains(new Pair<>(dg, ab))) {
				s = cp(dg, ab);
			//	System.out.println(" on " + dg + " and " + ab + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(dg, ab));
			}
			////
			if (!seen.contains(new Pair<>(ba, gd))) {
				s = cp(ba, gd);
			//	System.out.println(" on " + ba + " and " + gd + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(ba, gd));
			}
			if (!seen.contains(new Pair<>(gd, ba))) {
				s = cp(gd, ba);
			//	System.out.println(" on " + gd + " and " + ba + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(gd, ba));
			}
			if (!seen.contains(new Pair<>(ba, dg))) {
				s = cp(ba, dg);
			//	System.out.println(" on " + ba + " and " + dg + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(ba, dg));
			}
			if (!seen.contains(new Pair<>(dg, ba))) {
				s = cp(dg, ba);
			//	System.out.println(" on " + dg + " and " + ba + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(dg, ba));
			}
		}
		
		for (Pair<KBExp<C, V>, KBExp<C, V>> gd : R) {
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> s;

			if (!seen.contains(new Pair<>(ab, gd))) {
				s = cp(ab, gd);
		//		System.out.println(" XXon " + ab + " and " + gd + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(ab, gd));
			}
			if (!seen.contains(new Pair<>(gd, ab))) {
				s = cp(gd, ab);
			//	System.out.println(" XXon " + gd + " and " + ab + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(gd, ab));
			}
			////
			if (!seen.contains(new Pair<>(ba, gd))) {
				s = cp(ba, gd);
			//	System.out.println(" XXon " + ba + " and " + gd + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(ba, gd));
			}
			if (!seen.contains(new Pair<>(gd, ba))) {
				s = cp(gd, ba);
			//	System.out.println(" XXon " + gd + " and " + ba + " adding " + Util.sep(s, "\n"));
				ret.addAll(s);
				seen.add(new Pair<>(gd, ba));
			}
		}
		return ret;
	}

	protected Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps(
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
	//TODO: cp_cache not likely to be useful? indeed, never hits, since always freshening
	protected  Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Pair<KBExp<C, V>, KBExp<C, V>> gd0, Pair<KBExp<C, V>, KBExp<C, V>> ab0) {
	//	Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>> entry = new Pair<>(gd0, ab0);
	//	Set<Pair<KBExp<C, V>, KBExp<C, V>>> value = cp_cache.get(entry);
//		if (value != null) {
//			System.out.println("happened");
//			return value;
//		}		
		
		Pair<KBExp<C, V>, KBExp<C, V>> ab = freshen(fresh, ab0);
		Pair<KBExp<C, V>, KBExp<C, V>> gd = freshen(fresh, gd0);
		
//		Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> abm = freshenMap(fresh, ab0);
//		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(ab0.first.subst(abm.first), ab0.second.subst(abm.first));

		/*Map<V, KBExp<C, V>> inv = new HashMap<>();
		Set<V> vars = new HashSet<>();
		vars.addAll(gd.first.vars());
		vars.addAll(gd.second.vars());
		for (V k : abm.second.keySet()) {
			KBExp<C, V> v = abm.second.get(k);
			if (!Collections.disjoint(vars, v.vars())) {
				inv.put(k, v);
			}
		}*/
		Set<Triple<KBExp<C, V>, KBExp<C, V>, Map<V,KBExp<C,V>>>> retX = gd.first.cp(new LinkedList<>(), ab.first,
				ab.second, gd.first, gd.second, true);

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
		for (Triple<KBExp<C, V>, KBExp<C, V>, Map<V, KBExp<C, V>>> c : retX) {
			//ds !>= gs
			KBExp<C, V> gs = gd.first.subst(c.third);
			KBExp<C, V> ds = gd.second.subst(c.third);
			//System.out.println("checking " + ds + " >= " + gs);
			if ((gt.apply(new Pair<>(ds, gs)) || gs.equals(ds))) {
				continue;
			}
			//bs !>= as
			KBExp<C, V> as = ab.first.subst(c.third);
			KBExp<C, V> bs = ab.second.subst(c.third);
			//System.out.println("checking " + bs + " >= " + as);
			if ((gt.apply(new Pair<>(bs, as)) || as.equals(bs))) {
				continue;
			}
			Pair<KBExp<C, V>, KBExp<C, V>> toAdd = new Pair<>(c.first, c.second);
//			Pair<KBExp<C, V>, KBExp<C, V>> toAdd = new Pair<>(c.first.subst(inv), c.second.subst(inv));
				ret.add(toAdd);
		}
		
	//	cp_cache.put(entry, ret);
		
		return ret;
	}

	protected KBExp<C, V> step1(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh,
			List<Pair<KBExp<C, V>, KBExp<C, V>>> E, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> e0) {
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
			Map<V, KBExp<C, V>> s = KBUnifier.findSubst(lhs, e);
			if (s == null) {
				continue;
			}
			e = rhs.subst(s);
		}
		e = step1Es(E, e);
//		if (reducer != null) {
//			e = reducer.apply(e);
//		}
		if (cache != null) {
			cache.put(e0, e);
		}
		return e;
	}
	
	protected KBExp<C, V> step1Es(List<Pair<KBExp<C, V>, KBExp<C, V>>> E, KBExp<C, V> e) {
		if (DEBUG.debug.opl_unfailing) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> r0 : E) {
				e = step1EsX(r0, e);
				e = step1EsX(new Pair<>(r0.second, r0.first), e);
			}
		}
		return e;
	}

	private KBExp<C, V> step1EsX(Pair<KBExp<C, V>, KBExp<C, V>> r0, KBExp<C, V> e) {
		Pair<KBExp<C, V>, KBExp<C, V>> r = r0;
		if (!Collections.disjoint(r.first.vars(), e.vars())
				|| !Collections.disjoint(r.second.vars(), e.vars())) {
			r = freshen(fresh, r0);
		}

		KBExp<C, V> lhs = r.first;
		KBExp<C, V> rhs = r.second;
		Map<V, KBExp<C, V>> s = KBUnifier.findSubst(lhs, e);
		if (s == null) {
			return e;
		}

		KBExp<C, V> lhs0 = lhs.subst(s);
		KBExp<C, V> rhs0 = rhs.subst(s);
		if (!gt.apply(new Pair<>(lhs0, rhs0))) {
			return e;
		}

		return rhs0;
	}

	
	protected Collection<Pair<KBExp<C, V>, KBExp<C, V>>> reduce(
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> set) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> p = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : set) {
			KBExp<C, V> lhs = red(new HashMap<>(), E, R, e.first);
			KBExp<C, V> rhs = red(new HashMap<>(), E, R, e.second);
			if (lhs.equals(rhs)) {
				continue;
			}
			p.add(new Pair<>(lhs, rhs));
		}
		return p;
	}

	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> removeOrientable(List<Pair<KBExp<C, V>, KBExp<C, V>>> l) {
		List<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new LinkedList<>();
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = l.iterator();
		while (it.hasNext()) {
			Pair<KBExp<C, V>, KBExp<C, V>> p = it.next();
			if (orientable(p)) {
				it.remove();
				ret.add(p);
			}
		}
		return ret;
	}
	
	
	protected boolean step() {
		//System.out.println("\n\niteration " + count);
		//System.out.println(this);
		count++;

		if (checkEmpty()) {
		//	System.out.println("check empty returned true");
			return true;
		}

		Pair<KBExp<C, V>, KBExp<C, V>> st = pick(E);
		
		KBExp<C, V> s0 = st.first;
		KBExp<C, V> t0 = st.second;
		KBExp<C, V> a = null, b = null;
		boolean oriented = false;
		if (gt.apply(new Pair<>(s0, t0))) {
			a = s0; b = t0;
			oriented = true;
		} else if (gt.apply(new Pair<>(t0, s0))) {
			a = t0; b = s0;
			oriented = true;
		} else if (s0.equals(t0)) {
			remove(E, st); return false; //in case x = x coming in
		}  
		else {
			if (DEBUG.debug.opl_unfailing) {
				remove(E, st);
				add(E, st); //for sorting, will add to end of list
				a = s0; b = t0; 
			} else {
				throw new RuntimeException("Unorientable: " + st.first + " = " + st.second);
			}
		}
		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(a, b);
		if (oriented) {
			R.add(ab);
			List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = filterSubsumed(allcps(seen, ab));
			addAll(E, CP);
			remove(E, st); 
			collapseBy(ab);
		} else {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = filterSubsumed(allcps(seen, ab));
			CP.addAll(filterSubsumed(allcps(seen, ab.reverse())));
			CP.addAll(filterSubsumed(allcps2(seen, ab)));
			CP.addAll(filterSubsumed(allcps2(seen, ab.reverse())));		
			addAll(E, CP);
			//collapseBy2(ab);
			//collapseBy2(ba);
		}
		compose();
		
		simplify(); //definitely needed... cuts down on number of iterations
		//simplify2();	
		
		if (DEBUG.debug.opl_sort_cps) {
			sortByStrLen(E);
		}
		
		E = filterSubsumedBySelf(E);
		
//		System.out.println("finish step");
		return false;	
	}
	

	private Pair<KBExp<C, V>, KBExp<C, V>> pick(List<Pair<KBExp<C, V>, KBExp<C, V>>> l) {
		for (int i = 0; i < l.size(); i++) {
			Pair<KBExp<C,V>, KBExp<C,V>> x = l.get(i);
			if (orientable(x)) {
				return l.get(i);
			}
		} 
		return l.get(0);
	}
	
	boolean orientable(Pair<KBExp<C,V>, KBExp<C,V>> e) {
		if (gt.apply(e)) {
			return true;
		}
		if (gt.apply(e.reverse())) {
			return true;
		}
		return false;
	}
	
	
	protected boolean checkEmpty() {
		if (E.isEmpty()) {
			isComplete = true;
			isCompleteGround = true;
			return true;
		}
		if (allUnorientable() && allCpsConfluent()) {
			isComplete = false;
			isCompleteGround = true;
			return true;
		}
		return false;
	}

	private boolean allUnorientable() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			if (orientable(e)) {
				return false;
			}
		}
//		System.out.println("true on allunorientable");
		return true;
	}

	private boolean allCpsConfluent() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set = filterSubsumed(reduce(allcps2(
					new HashSet<>(), e)));
			if (!allCpsConfluent("equation " + e, set)) {
				return false;
			}
		} 
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : R) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set = filterSubsumed(reduce(allcps(new HashSet<>(), e)));
			if (!allCpsConfluent("rule" + e, set)) {
				return false;
			}
		}
		return true;
	}

	private boolean allCpsConfluent(String s, Collection<Pair<KBExp<C, V>, KBExp<C, V>>> set) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : set) {
			KBExp<C, V> lhs = red(new HashMap<>(), E, R, e.first);
			KBExp<C, V> rhs = red(new HashMap<>(), E, R, e.second);
			if (!lhs.equals(rhs)) {
				// TODO morally, should check ground confluence
				//System.out.println("not confluent on " + e + " goes to " + lhs + " = " + rhs + " came from " + s);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		List<String> a = E.stream().map(x -> x.first + " = " + x.second).collect(Collectors.toList());
		List<String> b = R.stream().map(x -> x.first + " -> " + x.second).collect(Collectors.toList());
		
		return (Util.sep(a, "\n") + "\n" + Util.sep(b, "\n")).trim();
	} 
	
	
}
