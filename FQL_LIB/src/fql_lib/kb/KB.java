package fql_lib.kb;

import java.util.Arrays;
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
import fql_lib.opl.OplParser.VIt;

public abstract class KB<C, V> {
	 
	protected boolean isComplete = false;
	protected boolean isCompleteGround = false;
	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> E;
	protected Set<Pair<KBExp<C, V>, KBExp<C, V>>> R;
	protected Iterator<V> fresh;
	protected Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt;
	protected Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen = new HashSet<>();	
	protected Map<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>, Set<Pair<KBExp<C, V>, KBExp<C, V>>>> cp_cache = new HashMap<>();

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
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		this.R = new HashSet<>();
		this.gt = gt0;
		this.fresh = fresh;
		this.E = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> eq : E0) {
			E.add(eq);
		}
	}


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

	protected static <X> void sortByStrLen(List<X> l) {
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
	

	public void complete() {
		while (!step());
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
				if (subsumes(fresh, cand.reverse(), e.reverse())) {
					it.remove();
					break;
				}
				
				//System.out.println("no");
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
	
	public abstract boolean eq(KBExp<C, V> lhs, KBExp<C, V> rhs);
	
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
			E0.add(r.first.subst(m) + " = " + r.second.subst(m));
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
			R0.add(r.first.subst(m) + " -> " + r.second.subst(m));
		}
		R0.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		
		
		return (Util.sep(E0, "\n\n") + "\n\n" + Util.sep(R0, "\n\n")).trim();
	}

	protected KBExp<C, V> red(Map<KBExp<C,V>, KBExp<C,V>> cache, 
			List<Pair<KBExp<C, V>, KBExp<C, V>>> E,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R,
			KBExp<C, V> e) {
		int i = 0;
		KBExp<C, V> orig = e;
		for (;;) {
			i++;
			if (i > 32) {
				throw new RuntimeException("Reduction taking too long: " + orig + " goes to " + e + " under " + R);
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

	//is not collapse 2
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

	protected abstract Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps2(
			Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen,
			Pair<KBExp<C, V>, KBExp<C, V>> ab);
	
	

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
	//TODO: cp_cache not likely to be useful?
	protected  Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Pair<KBExp<C, V>, KBExp<C, V>> gd0, Pair<KBExp<C, V>, KBExp<C, V>> ab0) {
		Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>> entry = new Pair<>(gd0, ab0);
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> value = cp_cache.get(entry);
		if (value != null) {
			return value;
		}		
		
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
			if ((gt.apply(new Pair<>(ds, gs)) || gs.equals(ds))) {
				continue;
			}
			//bs !>= as
			KBExp<C, V> as = ab.first.subst(c.third);
			KBExp<C, V> bs = ab.second.subst(c.third);
			if ((gt.apply(new Pair<>(bs, as)) || as.equals(bs))) {
				continue;
			}
			Pair<KBExp<C, V>, KBExp<C, V>> toAdd = new Pair<>(c.first, c.second);
//			Pair<KBExp<C, V>, KBExp<C, V>> toAdd = new Pair<>(c.first.subst(inv), c.second.subst(inv));
			//if (toAdd.equals(gd.reverse())) {
				//TODO
				//throw new RuntimeException();
			//} else {
				ret.add(toAdd);
			//}
		}
		
		cp_cache.put(entry, ret);
		
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
		if (cache != null) {
			cache.put(e0, e);
		}
		return e;
	}

	protected boolean step() {
		if (count > DEBUG.debug.opl_iterations) {
			throw new RuntimeException("Exceeded iteration limit");
		}

		System.out.println("\n\niteration " + count);
		System.out.println(this);
		count++;

		if (checkEmpty()) {
			System.out.println("check empty returned true");
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
			handleUnorientable(st);
			a = s0; b = t0; //TODO
		}
		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(a, b);
		if (oriented) {
		//	System.out.println("oriented " + ab);
			R.add(ab);
			List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = filterSubsumed(allcps(seen, ab));
			//CP.addAll(filterSubsumed(allcps2(seen, ab)));	
			addAll(E, CP);
			remove(E, st); 
			collapseBy(ab);

		} else {
		//	System.out.println("unoriented " + ab);
			List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = filterSubsumed(allcps(seen, ab));
			CP.addAll(filterSubsumed(allcps2(seen, ab)));
			CP.addAll(filterSubsumed(allcps2(seen, new Pair<>(ab.second, ab.first))));		
		//	System.out.println("---------------------");
		//	System.out.println(CP.size());
		//	System.out.println(Util.sep(CP, "\n"));
		//	System.out.println("+++++++++++++++++++++");
			CP = filterSubsumedBySelf(CP); //TODO add above?
		//	System.out.println(CP.size());
		//	System.out.println(Util.sep(CP, "\n"));
			addAll(E, CP);
			collapseBy2(ab);

		}
		//System.out.println("pre compose");
		compose();
		
	//	System.out.println("pre simplify");
		simplify(); //definitely needed... cuts down on number of iterations
		simplify2();	
		
		if (DEBUG.debug.opl_sort_cps) {
			sortByStrLen(E);
		}
		
		E = filterSubsumedBySelf(E);
		
	//	System.out.println("finish step");
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

	protected abstract boolean checkEmpty();

	protected abstract KBExp<C, V> step1Es(List<Pair<KBExp<C, V>, KBExp<C, V>>> E, KBExp<C, V> e);

	protected abstract void simplify2();

	protected abstract void collapseBy2(Pair<KBExp<C, V>, KBExp<C, V>> ab); 

	protected abstract void handleUnorientable(Pair<KBExp<C, V>, KBExp<C, V>> st);

	@Override
	public String toString() {
		List<String> a = E.stream().map(x -> x.first + " = " + x.second).collect(Collectors.toList());
		List<String> b = R.stream().map(x -> x.first + " -> " + x.second).collect(Collectors.toList());
		
		return (Util.sep(a, "\n") + "\n" + Util.sep(b, "\n")).trim();
	} 

/*	public static void main(String[] args) {
		KBExp<String, String> x= new KBVar<>("x");
		KBExp<String, String> y= new KBVar<>("y");

		KBExp<String, String> fx = new KBApp<String, String>("f", Arrays.asList(new KBExp[]{ x }));

		KBExp<String, String> gy = new KBApp<String, String>("g", Arrays.asList(new KBExp[]{ y }));

		Function<Pair<String, String>, Boolean> gt = xx -> {
				return xx.first.toString().compareTo(xx.second.toString()) > 0;
			};
		
		Pair<KBExp<String, String>,KBExp<String, String>> eq = new Pair<>(fx, gy);
			
		KB_unfailing<String,String> z = new KB_unfailing<String,String>(Collections.singleton(eq), KBOrders.pogt(gt), VIt.vit);
	
		System.out.println(Util.sep(z.allcps2(new HashSet<>(), eq), "\n"));
	} */
	/*             
((_v22613 o _v22618) o _v22617) = ((_v22613 o _v22614) o _v22617)

not confluent on (((_v22705 o _v22710) o _v22707), ((_v22705 o _v22704) o _v22707))
 goes to          ((_v22705 o _v22710) o _v22707) = ((_v22705 o _v22704) o _v22707)
 
*/
	
	
	public static void main(String[] args) {
		KBExp<String, String> x= new KBVar<>("x");
		KBExp<String, String> y= new KBVar<>("y");
		KBExp<String, String> z= new KBVar<>("z");
		KBExp<String, String> w= new KBVar<>("w");

		KBExp<String, String> x1= new KBVar<>("x1");
		KBExp<String, String> y1= new KBVar<>("y1");
		KBExp<String, String> z1= new KBVar<>("z1");
		KBExp<String, String> w1= new KBVar<>("w1");
		
		KBExp<String, String> xy = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ x, y }));
		KBExp<String, String> xyz = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ xy, z }));
		
		KBExp<String, String> xw = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ x, w }));
		KBExp<String, String> xwz = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ xw, z }));
		
		KBExp<String, String> x1y1 = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ x1, y1 }));
		KBExp<String, String> x1y1z1 = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ x1y1, z1 }));
		
		KBExp<String, String> x1w1 = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ x1, w1 }));
		KBExp<String, String> x1w1z1 = new KBApp<String, String>("o", Arrays.asList(new KBExp[]{ x1w1, z1 }));
		
		Pair<KBExp<String, String>, KBExp<String, String>> a = new Pair<>(xyz, xwz);
		Pair<KBExp<String, String>, KBExp<String, String>> b = new Pair<>(x1y1z1, x1w1z1);

		System.out.println(a + " and " + b + subsumes(VIt.vit, a, b));
		System.out.println(subsumes(VIt.vit, b, a));
	}
	
	//((x o y) o _v38910) = ((x o z) o _v38910)
	//((x o y) o _v38922) = ((x o z) o _v38922)

	
	
}
