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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Unit;
import fql_lib.Util;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBExpVisitor;
import fql_lib.kb.KBExp.KBVar;

public class KB<C, V> {
	/*
	private static <C, V> State<C,V> simplifyAndDeleteRepeatedly(State<C,V> X) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : X.E) {
			KBExp<C, V> l = red(X.fresh, X.R, e.first);
			KBExp<C, V> r = red(X.fresh, X.R, e.second);
			if (l.equals(r)) {
				continue;
			}
			newE.add(new Pair<>(l, r));
		}
		return new State<>(newE, X.R, X.fresh, X.gt);
	}
	
	private static <C, V> State<C,V> orientOnce(State<C,V> X) {
		if (X.E.isEmpty()) {
			return X;
		}
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>(X.E);
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newR = new HashSet<>(X.R);
		
		Pair<KBExp<C, V>, KBExp<C, V>> e = pick(newE);
		
		KBExp<C, V> s0 = e.first;
		KBExp<C, V> t0 = e.second;
		KBExp<C, V> a, b;
		
		if (X.gt.apply(new Pair<>(s0, t0))) {
			a = s0;
			b = t0;
		} else if (X.gt.apply(new Pair<>(t0, s0))) {
			a = t0;
			b = s0;
		} else {
			throw new RuntimeException("Cannot orient " + s0 + " = " + t0);
		}
		newE.remove(e);
		newR.add(new Pair<>(a, b));
		return new State<>(newE, newR, X.fresh, X.gt);
	}
	
	private static <C, V> State<C,V> compose(State<C,V> X) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : X.R) {
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0 = new HashSet<>(X.R);
			R0.remove(r);
			KBExp<C, V> new_rhs = red(X.fresh, R0, r.second);
			if (new_rhs.equals(r.second)) {
				continue;
			}
			R0.add(new Pair<>(r.first, new_rhs));
			return new State<>(X.E, R0, X.fresh, X.gt);
		}
		return X;
	}
	
	private static <C, V> State<C,V> composeAndCollapseRepeatedly(State<C,V> X) {
		for (;;) {
			State<C, V> old_X = X;
			X = compose(X);
			X = collapse(X);
			if (X.equals(old_X)) {
				return X;
			}
		}
	}
	
	private static <C, V> State<C,V> orientThenComposeAndCollapseRepeatedly(State<C,V> X) {
		return composeAndCollapseRepeatedly(orientOnce(X));
	}
	
	private static <C, V> State<C,V> collapse(State<C,V> X) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> st : X.R) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> lr : X.R) {
				if (st.equals(lr)) {
					continue;
				}
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> lr0 = Collections.singleton(lr);
				KBExp<C, V> new_s = red(X.fresh, lr0, st.first);
				if (!new_s.equals(st.first)) {
					if (gt(X.gt, st, lr)) {
						Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>(X.E);
						Set<Pair<KBExp<C, V>, KBExp<C, V>>> newR = new HashSet<>(X.R);
						newE.add(new Pair<>(new_s, st.second));
						newR.remove(st);
						return new State<>(newE, newR, X.fresh, X.gt);
					}
				}
			}
		}
		return X;
	}

	private static <C, V> State<C,V> allButDeduceRepeatedly(State<C,V> X) {
		for (;;) {
			State<C, V> old_X = X;
			X = simplifyAndDeleteRepeatedly(X);
			X = orientThenComposeAndCollapseRepeatedly(X);
			if (X.equals(old_X)) {
				return X;
			}
		}
	}
	
	public void complete(int limit) {
		State<C,V> X = new State<>(E, R, fresh, gt);
		for (int i = 0; i < 32; i++) {
			State<C, V> old_X = X;
			System.out.println(X);
			X = allButDeduceRepeatedly(X);
			X = deduce(X);
			if (X.equals(old_X)) {
				E = X.E;
				R = X.R;
			}
		}
		throw new RuntimeException("Exceeded iterations");
	}
	
	
	
	
	private static <C, V> State<C,V> deduce_all(State<C,V> X) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> cps = new HashSet<>();

		for (Pair<KBExp<C, V>, KBExp<C, V>> r1 : X.R) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> r2 : X.R) {
				if (r1.equals(r2)) {
					continue;
				}	
				cps.addAll(cp(X.fresh, r1, r2));
			}
		}
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = cps.iterator();
		while (it.hasNext()) {
			Pair<KBExp<C, V>, KBExp<C, V>> p = it.next();
			KBExp<C, V> l = red(X.fresh, X.R, p.first);
			KBExp<C, V> r = red(X.fresh, X.R, p.second);
			if (l.equals(r)) {
				it.remove();
			}
		}
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>(X.E);
		newE.addAll(cps);
		return new State<>(newE, X.R, X.fresh, X.gt);
	}
	
	private static <C, V> State<C,V> deduce(State<C,V> X) {
	
		for (Pair<KBExp<C, V>, KBExp<C, V>> r1 : X.R) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> r2 : X.R) {
				if (r1.equals(r2)) {
					continue;
				}	
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> cps = new HashSet<>();
				cps.addAll(cp(X.fresh, r1, r2));
				Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = cps.iterator();
				while (it.hasNext()) {
					Pair<KBExp<C, V>, KBExp<C, V>> p = it.next();
					KBExp<C, V> l = red(X.fresh, X.R, p.first);
					KBExp<C, V> r = red(X.fresh, X.R, p.second);
					if (l.equals(r)) {
						it.remove();
					}
				}
				if (!cps.isEmpty()) {
					Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>(X.E);
					newE.addAll(cps);
					return new State<>(newE, X.R, X.fresh, X.gt);
				}
			}
		}
		return X;
	}
	
	private static class State<C,V> {
		public Set<Pair<KBExp<C, V>, KBExp<C, V>>> E, R;
		private Iterator<V> fresh;
		private Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt;
		
		public State(Set<Pair<KBExp<C, V>, KBExp<C, V>>> e, Set<Pair<KBExp<C, V>, KBExp<C, V>>> r,
				Iterator<V> fresh, Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt) {
			E = e;
			R = r;
			this.fresh = fresh;
			this.gt = gt;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((E == null) ? 0 : E.hashCode());
			result = prime * result + ((R == null) ? 0 : R.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (E == null) {
				if (other.E != null)
					return false;
			} else if (!E.equals(other.E))
				return false;
			if (R == null) {
				if (other.R != null)
					return false;
			} else if (!R.equals(other.R))
				return false;
			return true;
		}
		
		public String printEqs() {
			String ret = "Equations:\n\n";
			ret += Util.sep(E.stream().map(x -> {
				return x.first + " = " + x.second;
			}).collect(Collectors.toList()), "\n");
			return ret;
		}

		public String printReds() {
			String ret = "Reductions:\n\n";
			ret += Util.sep(R.stream().map(x -> {
				return x.first + " -> " + x.second;
			}).collect(Collectors.toList()), "\n");
			return ret;
		}

		@Override
		public String toString() {
			return "\n****\n" + printEqs() + "\n\n" + printReds() + "\n****";
		}
	}
*/
	
	void validateRules() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
			if (!r.first.vars().containsAll(r.second.vars())) {
				throw new RuntimeException("bad rule: " + r + " and lhs > rhs " 
			+ gt.apply(new Pair<>(r.first, r.second)) + " and rhs > lhs " + gt.apply(new Pair<>(r.second, r.first)));
			}
		}
	}
	 

	private Queue<Pair<KBExp<C, V>, KBExp<C, V>>> E;
	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> R;
	// private Function<Pair<C,C>, Boolean> gt;
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

	private static<C,V> Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> freshenMap(
			Iterator<V> fresh,
			Pair<KBExp<C, V>, KBExp<C, V>> eq) {
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
	
	Queue<Pair<KBExp<C, V>, KBExp<C, V>>> newQ() {
		return new LinkedList<>();
/*		return new PriorityQueue(new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				String s1 = o1.toString();
				String s2 = o2.toString();
				if (s1.length() > s2.length()) {
					return 1;
				}
				if (s1.length() < s2.length()) {
					return -1;
				}
				return 0;
			}
			
		}); */
	}

	public KB(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0,
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		this.R = new HashSet<>();
		this.gt = gt0;
		this.fresh = fresh;
		this.E = newQ();
		for (Pair<KBExp<C, V>, KBExp<C, V>> eq : E0) {
			E.add(eq);
		}
	}

	// z = (I(ee) o (z o z))
	private void check(String s) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> toadd : E) {
			if (toadd.first.toString().equals("z")
					&& toadd.second.toString().equals("(I(ee) o (z o z))")) {
				// System.out.println(Util.sep(log, "\n"));
				throw new RuntimeException("$$!! " + ((s != null) ? s : ""));
			}
		}
	}

		public void complete_young(int limit) {
		int count = 0;
		for (;;) {
			count++;
			if (count > limit) {
				throw new RuntimeException("Exceeded iteration limit. \n" + this);
			}

			System.out.println("Before deduce "); //sticks here
			deduce();
			delete();
			//removeSubsumedOfE(); takes 99% of the time
			
			System.out.println("Before simpl: " + this);

			simplify(true);
		//	delete();

			System.out.println("After simpl: " + this);

			if (E.isEmpty()) {
				break;
			}

			Set<Pair<KBExp<C, V>, KBExp<C, V>>> or = orientable();
			R.addAll(or);
			E.removeAll(or);
			//collapse();
			//compose();
						
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
				KBExp<C, V> new_rhs = red(fresh, R0, r.second);
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

	static Map<KBExp, KBExp> red_cache = null;
	private void simplify(boolean print) {
		red_cache = new HashMap<>();
		Queue<Pair<KBExp<C, V>, KBExp<C, V>>> newE = newQ();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			KBExp<C, V>	lhs_red = red(fresh, R, e.first);
			log = new LinkedList<>();
			KBExp<C, V> rhs_red = red(fresh, R, e.second);
			if (!lhs_red.equals(rhs_red)) {
				//System.out.println("Added: " + e.first + " = "+ e.second + " goes to " + lhs_red + " = " + rhs_red);
				add(newE, new Pair<>(lhs_red, rhs_red));
			}
		}
		E = newE;
		red_cache = null;
	}

	private void delete() {
		Queue<Pair<KBExp<C, V>, KBExp<C, V>>> newE = newQ();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			if (e.first.equals(e.second)) {
				continue;
			}
			newE.add(e);
		}
		E = newE;
	}

	//f : r ---> t  rewrites strict subterm of r
 	// 
	private void collapse() {
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = R.iterator();
		while (it.hasNext()) {
			Pair<KBExp<C, V>, KBExp<C, V>> st = it.next();
			for (Pair<KBExp<C, V>, KBExp<C, V>> lr : R) {
				if (st.equals(lr)) {
					continue;
				}
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> lr0 = Collections.singleton(lr);
				KBExp<C, V> new_s = red(fresh, lr0, st.first);
				if (!new_s.equals(st.first)) {
					if (gt(gt, st, lr)) {
						it.remove();
						E.add(new Pair<>(new_s, st.second));
						break;
					}
				}
			}
		}
	}

	private static <C, V> boolean gt(Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt, Pair<KBExp<C, V>, KBExp<C, V>> st, Pair<KBExp<C, V>, KBExp<C, V>> lr) {
		if (gt.apply(new Pair<>(st.first, lr.first))) {
			return true;
		}
		// TODO: morally, should be alpha equivalence
		if (!st.first.equals(lr.first)) {
			return false;
		}
		return gt.apply(new Pair<>(st.second, lr.second));
	}

	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> orientable() {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			KBExp<C, V> s0 = e.first;
			KBExp<C, V> t0 = e.second;
			KBExp<C, V> a, b;
			if (gt.apply(new Pair<>(s0, t0))) {
				a = s0;
				b = t0;
			} else if (gt.apply(new Pair<>(t0, s0))) {
				a = t0;
				b = s0;
			}  else {
				continue;
				//throw new RuntimeException("Cannot orient " + s0 + " = " + t0);
			} 
			ret.add(new Pair<>(a, b));
		}
		return ret;
	}

	void deduce() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r1 : R) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> r2 : R) {
				E.addAll(cp(r1, r2));
				//E.addAll(cp(r2, r1));
			}
		}
	}

	void removeSubsumedOfE() {
		for (;;) {
			Pair<KBExp<C, V>, KBExp<C, V>> cand = findOneSubsumed();
			if (cand == null) {
				return;
			}
			//System.out.println("found " + cand);
			remove(E, cand);
		}
	}

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
	}
	
	public static <X> void remove(Queue<X> X, X x) {
		while (X.remove(x));
	}
	
	public static <X> void add(Queue<X> X, X x) {
		if (!X.contains(x)) {
			X.add(x);
		}
	}
	
	public static <X> void addFront(Queue<X> X, X x) {
		if (!X.contains(x)) {
			((List)X).add(0, x);
		}
	}
	
	public static <X> void addAll(Queue<X> X, Collection<X> x) {
		for (X xx : x) {
			add(X, xx);
		}
	}

	static <C, V> boolean subsumes(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> cand,
			Pair<KBExp<C, V>, KBExp<C, V>> other) {
		boolean shouldPrint = false; //cand.toString().length() == other.toString().length() && cand.toString().length() == 26;
		if (shouldPrint) {
			System.out.println("is " + cand + " subsumed by " + other +"?");
		}
		 Pair<KBExp<C, V>, KBExp<C, V>> candX = freshen(fresh, cand);
		KBApp<C, V> cand0 = new KBApp<C, V>((C) "", Arrays.asList(new KBExp[] {
				candX.first, candX.second }));
		KBApp<C, V> other0 = new KBApp<C, V>((C) "", Arrays.asList(new KBExp[] { other.first,
				other.second }));
		Map<V, KBExp<C, V>> subst = KBUnifier.findSubst(other0, cand0);
		if (shouldPrint) { 
		if (subst == null) {
		 System.out.println("no");
		 } else {
		 System.out.println("yes, by " + subst);
		 }
		}
		return (subst != null);
	}

	/*public void complete_old(int limit) {
		int count = 0;
		limit = 128;
		Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen = new HashSet<>();
		for (;;) {
			count++;
			System.out.println("Starting: " + this);
			validateRules();
			if (count > limit) {
				throw new RuntimeException("Exceeded iteration limit");
			}
			if (E.isEmpty()) {
				break;
			}
			Pair<KBExp<C, V>, KBExp<C, V>> st = pick(E);

			KBExp<C, V> s = st.first;
			KBExp<C, V> t = st.second;
			KBExp<C, V> s0 = red(fresh, R, s);
			KBExp<C, V> t0 = red(fresh, R, t);
			if (s0.equals(t0)) {
				remove(E, st); // st_orig
				continue;
			}
			KBExp<C, V> a, b;
			if (gt.apply(new Pair<>(s0, t0))) {
				a = s0;
				b = t0;
			} else if (gt.apply(new Pair<>(t0, s0))) {
				a = t0;
				b = s0;
			} else {
				//throw new RuntimeException("Cannot orient " + st.first + " and " + st.second);
				remove(E, st);
				add(E, st); 
				continue; //needed
			}
			Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(a, b);
			
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> CP0 = allcps(new HashSet<>(), ab); 
		//	Queue<Pair<KBExp<C, V>, KBExp<C, V>>> CP = newQ();	
		//	addAll(CP, CP0);
			
			validateRules();
			R.add(ab);
			validateRules();
			
			addAll(E, CP0); 
			validateRules();
			remove(E, st); 
	
		//	System.out.println("before simpl " + this);
			simplify(false); //needed
		//	System.out.println("after " + this);
			//	delete();
		//	removeSubsumedOfE();

			//collapse(); //slows it down
		}

	}  */
	
	public void complete_old(int limit) {
		int count = 0;
		limit = 256;
		Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen = new HashSet<>();
		for (;;) {
			count++;
			System.out.println("Starting: " + this);
			validateRules();
			if (count > limit) {
				throw new RuntimeException("Exceeded iteration limit");
			}
			if (E.isEmpty()) {
				break;
			}
			Pair<KBExp<C, V>, KBExp<C, V>> st = pick(E);

			KBExp<C, V> s = st.first;
			KBExp<C, V> t = st.second;
			KBExp<C, V> s0 = red(fresh, R, s);
			KBExp<C, V> t0 = red(fresh, R, t);
			if (s0.equals(t0)) {
				remove(E, st); 
				continue;
			}
			KBExp<C, V> a, b;
			if (gt.apply(new Pair<>(s0, t0))) {
				a = s0;
				b = t0;
			} else if (gt.apply(new Pair<>(t0, s0))) {
				a = t0;
				b = s0;
			} else {
				remove(E, st);
				add(E, st); 
				continue; //needed
			}
			Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(a, b);
			
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> CP0 = allcps(seen, ab); 
			List<Pair<KBExp<C, V>, KBExp<C, V>>> CPX = new LinkedList<>(CP0);
			CPX.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					if (o1.toString().length() > o2.toString().length()) {
						return 1;
					} else if (o1.toString().length() < o2.toString().length()) {
						return -1;
					}
					return 0;
				}
			});
			List<Pair<KBExp<C, V>, KBExp<C, V>>> CP = new LinkedList<>();
			outer: for (Pair<KBExp<C, V>, KBExp<C, V>> cand : CPX) {
				for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
					if (subsumes(fresh, cand, e)) {
						continue outer; //CP.add(cand);
					}
				}
				CP.add(cand);
			}
			//TODO: add subsumedBy cache
			//addAll(CP, CP0);
			
			validateRules();
			R.add(ab);
			validateRules();
			
			addAll(E, CP); 
			validateRules();
			remove(E, st); 
			compose();
			collapseBy(ab);
		//	System.out.println("before simpl " + this);
			simplify(false); //definitely needed... but why?
		//	System.out.println("after " + this);
			//	delete();
		//	removeSubsumedOfE();

			//collapse(); //slows it down
		}

	} 

	private void collapseBy(Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> AB = Collections.singleton(ab);
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = R.iterator();
		while (it.hasNext()) {
		Pair<KBExp<C, V>, KBExp<C, V>> r = it.next();
			if (r.equals(ab)) {
				continue;
			}
			KBExp<C, V> lhs = red(fresh, AB, r.first);
			if (!r.first.equals(lhs)) {
				addFront(E, new Pair<>(lhs, r.second));	
				it.remove();
			} 
		}
	}

	private static <X> X pick(Queue<X> X) {
		if (X.size() == 0) {
			throw new RuntimeException();			
		}
		//List<X> l = new LinkedList<>(X);
		//return l.get(new Random().nextInt(X.size()));
		return X.remove();
	} //TODO

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
	 Map cp_cache = new HashMap();
	private  Set<Pair<KBExp<C, V>, KBExp<C, V>>> 
	cp( Pair<KBExp<C, V>, KBExp<C, V>> gd0,
			Pair<KBExp<C, V>, KBExp<C, V>> ab0) {
		Object entry = new Pair(gd0, ab0);
		Object value = cp_cache.get(entry);
		if (value != null) {
			return (Set<Pair<KBExp<C, V>, KBExp<C, V>>>) value;
		}		
		
		Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> abm = freshenMap(fresh, ab0);

		Pair<KBExp<C, V>, KBExp<C, V>> gd = gd0; 
		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(ab0.first.subst(abm.first),
				ab0.second.subst(abm.first));

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
		
		
//		System.out.println("CPs for " + ab.first + " -> " + ab.second + " and " + gd.first + " -> " + gd.second + " are " + Util.sep(ret, "\n"));

		return ret;
	}

	public static <C, V> KBExp<C, V> red(Iterator<V> fresh, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R,
			KBExp<C, V> e) {
		int i = 0;
		KBExp<C, V> orig = e;
		for (;;) {
			i++;
			if (i > 32) {
				System.out.println("*************");
				System.out.println(Util.sep(R, "\n"));
				throw new RuntimeException("Init " + orig + " goes to " + e + " under " + R);
			}
			KBExp<C, V> e0 = step(fresh, R, e);
			if (e.equals(e0)) {
				return e;
			}
			e = e0;
		}
	}

	
	List<String> log;

	private static <C, V> KBExp<C, V> step1(Iterator<V> fresh,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> e0) {
		KBExp<C, V> e = e0;
		if (red_cache != null && red_cache.containsKey(e)) {
			return red_cache.get(e);
		}
		for (Pair<KBExp<C, V>, KBExp<C, V>> r0 : R) {
			Pair<KBExp<C, V>, KBExp<C, V>> r = freshen(fresh, r0);
			KBExp<C, V> lhs = r.first;
			KBExp<C, V> rhs = r.second;
			if (!Collections.disjoint(e.vars(), lhs.vars())) {
				System.out.println("rule was " + r0);
				System.out.println("freshened to " + r);
				System.out.println("e was " + e);
				throw new RuntimeException("Not disjoint: " + e + " and " + lhs);
			}
			Map<V, KBExp<C, V>> s = KBUnifier.findSubst(lhs, e);
			if (s == null) {
				continue;
			}
			if (!s.keySet().equals(lhs.vars())) {
				throw new RuntimeException("Trying to reduce " + e + " by " + lhs + " gives subst "
						+ s);
			}
			e = rhs.subst(s);
		}
		if (red_cache != null) {
			red_cache.put(e0, e);
		}
		return e;
	}

	private static <C, V> KBExp<C, V> step(Iterator<V> fresh,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> e) {
		KBExpVisitor<C, V, KBExp<C, V>, Unit> v = new KBExpVisitor<C, V, KBExp<C, V>, Unit>() {

			@Override
			public KBExp<C, V> visit(Unit env, KBVar<C, V> e) {
				return step1(fresh, R, e);
			}

			@Override
			public KBExp<C, V> visit(Unit env, KBApp<C, V> e) {
				List<KBExp<C, V>> args0 = new LinkedList<>();
				for (KBExp<C, V> arg : e.args) {
					args0.add(arg.accept(env, this));
				}
				KBApp<C, V> ret = new KBApp<>(e.f, args0);
				return step1(fresh, R, ret);
			}
		};

		return e.accept(new Unit(), v);
	}

	// TODO: check on same rewrite furthermore case
	public String printEqs() {
		String ret = "Equations:\n\n";
		ret += Util.sep(E.stream().map(x -> {
			return x.first + " = " + x.second;
		}).collect(Collectors.toList()), "\n");
		return ret;
	}

	public String printReds() {
		String ret = "Reductions:\n\n";
		ret += Util.sep(R.stream().map(x -> {
			return x.first + " -> " + x.second;
		}).collect(Collectors.toList()), "\n");
		return ret;
	}

	@Override
	public String toString() {
		return "\n****\n" + printEqs() + "\n\n" + printReds() + "\n****";
	}
	
	
	/* public static void main(String[] args) {
		//(v4752516 o I(v4752516)) = ee
		//		(v4752531 o I(v4752531)) = ee

		KBExp<String, String> v = new KBVar<>("v");
		KBExp<String, String> x = new KBVar<>("x");
		KBExp<String, String> Iv = new KBApp<String, String>("I", Arrays.asList(new KBExp[] { v }));
		KBExp<String, String> Ix = new KBApp<String, String>("I", Arrays.asList(new KBExp[] { x }));
		KBExp<String, String> e = new KBApp<String, String>("ee", Arrays.asList(new KBExp[] {  }));
		
		KBExp<String, String> voIv = new KBApp<String, String>("o", Arrays.asList(new KBExp[] { v, Iv }));
		KBExp<String, String> xoIx = new KBApp<String, String>("o", Arrays.asList(new KBExp[] { x, Ix }));
		
		Iterator<String> fresh = new Iterator<String>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public String next() {
				return "v" + (i++);
			}
			
		};
		
		System.out.println(subsumes(fresh, new Pair<>(voIv, e), new Pair<>(xoIx, e)));
		
	} */

}
