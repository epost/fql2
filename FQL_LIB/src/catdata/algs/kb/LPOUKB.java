package catdata.algs.kb;

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

import catdata.Chc;
import catdata.DAG;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.algs.kb.KBExp.KBApp;
import catdata.algs.kb.KBExp.KBVar;


/**
 * 
 * @author Ryan Wisnesky
 *
 * Implements "unfailing" aka "ordered" Knuth-Bendix completion.
 * 
 * Note: terminates when the system is complete, not when the system is ground complete.
 * Note: eq is not a true semi-decision procedure for systems that are only ground complete, because eq does not skolemize.
 * Note: printKB assumes <C> and <V> is String
 * Note: will not orient var = const
 * 
 * Update Jan 16: add special support for associative and commutative theories as described in
 * "On Using Ground Joinable Equations in Equational Theorem Proving"
 * 
 * Update Oct 16: change E-reduction to instantiate free variables with a minimal constant,
 *  as described in 'Decision Problems in Ordered Rewriting'.  This is necessary to use only-ground complete
 *  systems as decision procedures.  This means E-reduction is LPO specific.  Also, the AQL wrapper for this 
 *  now herbrandizes, meaning that only-ground complete systems can decide universally quantified equations.
 *
 * @param <C> the type of functions/constants
 * @param <V> the type of variables
 */
public class LPOUKB<T, C, V> extends DPKB<T, C, V> {
	
	private List<C> prec;

	private boolean isComplete = false;
	private boolean isCompleteGround = false;

	//order matters
	private List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> R, E, G; 

	private Iterator<V> fresh;

	private Set<Pair<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>>> seen = new HashSet<>();

	private Map<Chc<V, C>, List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>>> AC_symbols;

	private KBOptions options;

	private Chc<V, C> min;

	public LPOUKB(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0, Iterator<V> fresh, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0, KBOptions options, List<C> prec) throws InterruptedException {
		this.options = options;
		this.prec = prec;
		this.R = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : R0) {
			R.add(freshen(fresh, new Pair<>(r.first.inject(), r.second.inject())));
		}
		this.fresh = fresh;
		this.E = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E0) {
			E.add(freshen(fresh, new Pair<>(e.first.inject(), e.second.inject())));
		}
		this.G = new LinkedList<>();
		initAC();
		min = Chc.inLeft(fresh.next());
		complete();
	}

	private void initAC() throws InterruptedException {
		if (!options.semantic_ac) {
			return;
		}
		Map<Chc<V, C>, Integer> symbols = new HashMap<>();
		AC_symbols = new HashMap<>();
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : E) {
			e.first.symbols(symbols);
			e.second.symbols(symbols);
		}
		outer: for (Chc<V, C> f : symbols.keySet()) {
			Integer i = symbols.get(f);
			if (i.intValue() != 2) {
				continue;
			}
			boolean cand1_found = false;
			boolean cand2_found = false;
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> cands = AC_E(f);
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cand1 = cands.get(0);
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cand2 = cands.get(1);
			for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> other : E) {
				if (subsumes(fresh, cand1, other)) {
					cand1_found = true;
				} else if (subsumes(fresh, cand1, other.reverse())) {
					cand1_found = true;
				}
				if (subsumes(fresh, cand2, other)) {
					cand2_found = true;
				} else if (subsumes(fresh, cand2, other.reverse())) {
					cand2_found = true;
				}
				if (cand1_found && cand2_found) {
					List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> l = new LinkedList<>();
					l.add(AC_E(f).get(1)); // assoc rewrite rule
					l.add(AC_E(f).get(0)); // comm eq
					l.addAll(AC_E0(f)); // perm eqs
					AC_symbols.put(f, l);
					continue outer;
				}
			}
		}
	}

	private KBExp<Chc<V, C>, V> achelper(Chc<V, C> f, V xx, V yy, V zz) {
		KBExp<Chc<V, C>, V> x = new KBVar<>(xx);
		KBExp<Chc<V, C>, V> y = new KBVar<>(yy);
		KBExp<Chc<V, C>, V> z = new KBVar<>(zz);
		List<KBExp<Chc<V, C>, V>> yz = new LinkedList<>();
		yz.add(y);
		yz.add(z);
		List<KBExp<Chc<V, C>, V>> xfyz = new LinkedList<>();
		xfyz.add(x);
		xfyz.add(new KBApp<>(f, yz));
		return new KBApp<>(f, xfyz);
	}

	private List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> AC_E0(Chc<V, C> f) {
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> ret = new LinkedList<>();
		V x = fresh.next();
		V y = fresh.next();
		V z = fresh.next();

		ret.add(freshen(fresh, new Pair<>(achelper(f, x, y, z), achelper(f, y, x, z))));
		ret.add(freshen(fresh, new Pair<>(achelper(f, x, y, z), achelper(f, z, y, x))));
		ret.add(freshen(fresh, new Pair<>(achelper(f, x, y, z), achelper(f, y, z, x))));

		return ret;
	}

	private List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> AC_E(Chc<V, C> f) {
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> ret = new LinkedList<>();
		KBExp<Chc<V, C>, V> x = new KBVar<>(fresh.next());
		KBExp<Chc<V, C>, V> y = new KBVar<>(fresh.next());
		List<KBExp<Chc<V, C>, V>> xy = new LinkedList<>();
		xy.add(x);
		xy.add(y);
		List<KBExp<Chc<V, C>, V>> yx = new LinkedList<>();
		yx.add(y);
		yx.add(x);
		ret.add(new Pair<>(new KBApp<>(f, xy), new KBApp<>(f, yx)));

		x = new KBVar<>(fresh.next());
		y = new KBVar<>(fresh.next());
		KBExp<Chc<V, C>, V> z = new KBVar<>(fresh.next());
		List<KBExp<Chc<V, C>, V>> yz = new LinkedList<>();
		yz.add(y);
		yz.add(z);
		xy = new LinkedList<>();
		xy.add(x);
		xy.add(y);
		List<KBExp<Chc<V, C>, V>> xfyz = new LinkedList<>();
		xfyz.add(x);
		xfyz.add(new KBApp<>(f, yz));
		List<KBExp<Chc<V, C>, V>> fxyz = new LinkedList<>();
		fxyz.add(new KBApp<>(f, xy));
		fxyz.add(z);
		ret.add(new Pair<>(new KBApp<>(f, fxyz), new KBApp<>(f, xfyz)));

		return ret;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static <C, V> Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> freshen(Iterator<V> fresh, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> eq) {
		Map<V, KBExp<Chc<V, C>, V>> subst = freshenMap(fresh, eq).first;
		return new Pair<>(eq.first.subst(subst), eq.second.subst(subst));
	}

	private static <C, V> Pair<Map<V, KBExp<Chc<V, C>, V>>, Map<V, KBExp<Chc<V, C>, V>>> freshenMap(Iterator<V> fresh, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> eq) {
		Set<V> vars = new HashSet<>();
		KBExp<Chc<V, C>, V> lhs = eq.first;
		KBExp<Chc<V, C>, V> rhs = eq.second;
		vars.addAll(lhs.vars());
		vars.addAll(rhs.vars());
		Map<V, KBExp<Chc<V, C>, V>> subst = new HashMap<>();
		Map<V, KBExp<Chc<V, C>, V>> subst_inv = new HashMap<>();
		for (V v : vars) {
			V fr = fresh.next();
			subst.put(v, new KBVar<>(fr));
			subst_inv.put(fr, new KBVar<>(v));
		}
		return new Pair<>(subst, subst_inv);
	}

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

	private void sortByStrLen(List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> l) {
		if (!options.unfailing) {
			l.sort(ToStringComparator);
		} else {
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> unorientable = new LinkedList<>();
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> orientable = new LinkedList<>();
			for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> k : l) {
				if (orientable(k)) {
					orientable.add(k);
				} else {
					unorientable.add(k);
				}
			}
			orientable.sort(ToStringComparator);
			l.clear();
			l.addAll(orientable);
			l.addAll(unorientable);
		}
	}

	private void checkParentDead() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Precedence tried: " + prec);
		}
	}

	private void complete() throws InterruptedException {
		while (!step());
		
		if (!isCompleteGround) {
			throw new RuntimeException("Not ground complete after iteration timeout.  Last state:\n\n" + toString());
		}
	}

	private static <C, V> boolean subsumes(Iterator<V> fresh, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cand, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> other) throws InterruptedException {
		return (subsumes0(fresh, cand, other) != null);
	}

	private static <C, V> Map<V, KBExp<Chc<V, C>, V>> subsumes0(Iterator<V> fresh, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cand, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> other) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> candX = cand;

		if (!Collections.disjoint(candX.first.vars(), other.first.vars()) || !Collections.disjoint(candX.first.vars(), other.second.vars()) || !Collections.disjoint(candX.second.vars(), other.first.vars()) || !Collections.disjoint(candX.second.vars(), other.second.vars())) {
			candX = freshen(fresh, cand);
		}

		Chc<V,C> xxx = Chc.inLeft(fresh.next());
		List<KBExp<Chc<V, C>, V>> l = new LinkedList<>();
		l.add(candX.first);
		l.add(candX.second);
		KBApp<Chc<V, C>, V> cand0 = new KBApp<>(xxx, l);

		List<KBExp<Chc<V, C>, V>> r = new LinkedList<>();
		r.add(other.first);
		r.add(other.second);
		KBApp<Chc<V, C>, V> other0 = new KBApp<>(xxx, r);

		Map<V, KBExp<Chc<V, C>, V>> subst = KBUnifier.findSubst(other0, cand0);
		return subst;
	}

	private List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> filterSubsumed(Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> CPX) throws InterruptedException {
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> CP = new LinkedList<>();
		outer: for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cand : CPX) {
			for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : E) {
				if (subsumes(fresh, cand, e)) {
					continue outer;
				}
			}
			CP.add(cand);
		}
		return CP;
	}

	private List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> filterSubsumedBySelf(Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> CPX) throws InterruptedException {
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> CP = new LinkedList<>(CPX);

		Iterator<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> it = CP.iterator();
		while (it.hasNext()) {
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cand = it.next();
			for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : CP) {
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
				// TODO: this one redundant?
				if (subsumes(fresh, cand.reverse(), e.reverse())) {
					it.remove();
					break;
				}
			}
		}
		return CP;
	}

	// is also compose2
	// simplify RHS of a rule
	private void compose() throws InterruptedException {
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> to_remove = null;
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> to_add = null;
		do {
			to_remove = null;
			to_add = null;
			for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r : R) {
				Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> R0 = new HashSet<>(R);
				R0.remove(r);
				KBExp<Chc<V, C>, V> new_rhs = red(null, Util.append(E, G), R0, r.second);
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

	private KBExp<Chc<V, C>, V> red(Map<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cache, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> Ex, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> Ry, KBExp<Chc<V, C>, V> e) throws InterruptedException {
		for (;;) {
			KBExp<Chc<V, C>, V> e0 = step(cache, fresh, Ex, Ry, e);
			if (e.equals(e0)) {
				return e0;
			}
			e = e0;
		}
	}

	private KBExp<Chc<V, C>, V> step(Map<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cache, Iterator<V> fresh, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> E, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> R, KBExp<Chc<V, C>, V> ee) throws InterruptedException {
		if (ee.isVar) {
			return step1(cache, fresh, E, R, ee);
		} else {
			KBApp<Chc<V, C>, V> e = ee.getApp();
			List<KBExp<Chc<V, C>, V>> args0 = new LinkedList<>();
			for (KBExp<Chc<V, C>, V> arg : e.args) {
				args0.add(step(cache, fresh, E, R, arg)); //must be step
			}
			KBApp<Chc<V, C>, V> ret = new KBApp<>(e.f, args0);
			return step1(cache, fresh, E, R, ret);
		}
	}

	// simplifies equations
	// can also use E U G with extra checking
	private void simplify() throws InterruptedException {
		Map<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cache = new HashMap<>(); 

		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> newE = new LinkedList<>();
		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> newE2 = new HashSet<>(); 
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : E) {
			KBExp<Chc<V, C>, V> lhs_red = red(cache, new LinkedList<>(), R, e.first);
			KBExp<Chc<V, C>, V> rhs_red = red(cache, new LinkedList<>(), R, e.second);
			if (!lhs_red.equals(rhs_red)) {
				Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> p = new Pair<>(lhs_red, rhs_red);
				if (!newE2.contains(p)) {
					newE.add(p);
					newE2.add(p);
				}
			}
		}
		E = newE;
	}

	// is not collapse2
	// can also use E U G here
	private void collapseBy(Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ab) throws InterruptedException {
		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> AB = Collections.singleton(ab);
		Iterator<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> it = R.iterator();
		while (it.hasNext()) {
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r = it.next();
			if (r.equals(ab)) {
				continue;
			}
			KBExp<Chc<V, C>, V> lhs = red(null, new LinkedList<>(), AB, r.first);
			if (!r.first.equals(lhs)) {
				addFront(E, new Pair<>(lhs, r.second));
				it.remove();
			}
		}
	}

	private Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> allcps2(Set<Pair<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>>> seen, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ab) throws InterruptedException {
		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> ret = new HashSet<>();

		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> E0 = new HashSet<>(E);
		E0.add(ab);
		E0.add(ab.reverse());
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ba = ab.reverse();
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> gd : E0) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
			Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> s;
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> dg = gd.reverse();

			if (!seen.contains(new Pair<>(ab, gd))) {
				s = cp(ab, gd);
				ret.addAll(s);
				seen.add(new Pair<>(ab, gd));
			}
			if (!seen.contains(new Pair<>(gd, ab))) {
				s = cp(gd, ab);
				ret.addAll(s);
				seen.add(new Pair<>(gd, ab));
			}
			if (!seen.contains(new Pair<>(ab, dg))) {
				s = cp(ab, dg);
				ret.addAll(s);
				seen.add(new Pair<>(ab, dg));
			}
			if (!seen.contains(new Pair<>(dg, ab))) {
				s = cp(dg, ab);
				ret.addAll(s);
				seen.add(new Pair<>(dg, ab));
			}
			////
			if (!seen.contains(new Pair<>(ba, gd))) {
				s = cp(ba, gd);
				ret.addAll(s);
				seen.add(new Pair<>(ba, gd));
			}
			if (!seen.contains(new Pair<>(gd, ba))) {
				s = cp(gd, ba);
				ret.addAll(s);
				seen.add(new Pair<>(gd, ba));
			}
			if (!seen.contains(new Pair<>(ba, dg))) {
				s = cp(ba, dg);
				ret.addAll(s);
				seen.add(new Pair<>(ba, dg));
			}
			if (!seen.contains(new Pair<>(dg, ba))) {
				s = cp(dg, ba);
				ret.addAll(s);
				seen.add(new Pair<>(dg, ba));
			}
		}

		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> gd : R) {
			Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> s;

			if (!seen.contains(new Pair<>(ab, gd))) {
				s = cp(ab, gd);
				ret.addAll(s);
				seen.add(new Pair<>(ab, gd));
			}
			if (!seen.contains(new Pair<>(gd, ab))) {
				s = cp(gd, ab);
				ret.addAll(s);
				seen.add(new Pair<>(gd, ab));
			}
			////
			if (!seen.contains(new Pair<>(ba, gd))) {
				s = cp(ba, gd);
				ret.addAll(s);
				seen.add(new Pair<>(ba, gd));
			}
			if (!seen.contains(new Pair<>(gd, ba))) {
				s = cp(gd, ba);
				ret.addAll(s);
				seen.add(new Pair<>(gd, ba));
			}
		}
		return ret;
	}

	private Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> allcps(Set<Pair<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>>> seen, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ab) throws InterruptedException {
		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> ret = new HashSet<>();
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> gd : R) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
			Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> s;
			if (!seen.contains(new Pair<>(ab, gd))) {
				s = cp(ab, gd);
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

	private Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> cp(Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> gd0, Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ab0) throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ab = freshen(fresh, ab0);
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> gd = freshen(fresh, gd0);

		Set<Triple<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>, Map<V, KBExp<Chc<V, C>, V>>>> retX = gd.first.cp(new LinkedList<>(), ab.first, ab.second, gd.first, gd.second);

		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> ret = new HashSet<>();
		for (Triple<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>, Map<V, KBExp<Chc<V, C>, V>>> c : retX) {
			// ds !>= gs
			KBExp<Chc<V, C>, V> gs = gd.first.subst(c.third);
			KBExp<Chc<V, C>, V> ds = gd.second.subst(c.third);
			if ((gt_lpo(ds, gs)) || gs.equals(ds)) {
				continue;
			}
			// bs !>= as
			KBExp<Chc<V, C>, V> as = ab.first.subst(c.third);
			KBExp<Chc<V, C>, V> bs = ab.second.subst(c.third);
			if ((gt_lpo(bs, as)) || as.equals(bs)) {
				continue;
			}
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> toAdd = new Pair<>(c.first, c.second);
			ret.add(toAdd);
		}

		return ret;
	}

	private KBExp<Chc<V, C>, V> step1(Map<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> cache, Iterator<V> fresh, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> E, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> R, KBExp<Chc<V, C>, V> e0) throws InterruptedException {
		KBExp<Chc<V, C>, V> e = e0;
		if (cache != null && cache.containsKey(e)) {
			return cache.get(e);
		}
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r0 : R) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r = r0;
			if (!Collections.disjoint(r.first.vars(), e.vars()) || !Collections.disjoint(r.second.vars(), e.vars())) {
				r = freshen(fresh, r0);
			}

			KBExp<Chc<V, C>, V> lhs = r.first;
			KBExp<Chc<V, C>, V> rhs = r.second;
			Map<V, KBExp<Chc<V, C>, V>> s = null;

			s = KBUnifier.findSubst(lhs, e);

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

	private KBExp<Chc<V, C>, V> step1Es(Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> E, KBExp<Chc<V, C>, V> e) throws InterruptedException {
		if (options.unfailing && e.vars().isEmpty()) {
			for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r0 : E) {
				KBExp<Chc<V, C>, V> a = step1EsX(r0, e);
				if (a != null) {
					e = a;
				}
				KBExp<Chc<V, C>, V> b = step1EsX(new Pair<>(r0.second, r0.first), e);
				if (b != null) {
					e = b;
				}
			}
		}
		return e;
	}

	private KBExp<Chc<V, C>, V> step1EsX(Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r0, KBExp<Chc<V, C>, V> e) throws InterruptedException {
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> r = r0;
		if (!Collections.disjoint(r.first.vars(), e.vars()) || !Collections.disjoint(r.second.vars(), e.vars())) {
			r = freshen(fresh, r0);
		}

		KBExp<Chc<V, C>, V> lhs = r.first;
		KBExp<Chc<V, C>, V> rhs = r.second;
		Map<V, KBExp<Chc<V, C>, V>> s0 = KBUnifier.findSubst(lhs, e);
		if (s0 == null) {
			return null;
		}
		Map<V, KBExp<Chc<V, C>, V>> s = new HashMap<>(s0);

		KBExp<Chc<V, C>, V> lhs0 = lhs.subst(s);
		KBExp<Chc<V, C>, V> rhs0 = rhs.subst(s);

		Set<V> newvars = new HashSet<>();
		newvars.addAll(lhs0.vars());
		newvars.addAll(rhs0.vars());
		Map<V, KBExp<Chc<V, C>, V>> t = new HashMap<>();
		for (V v : newvars) {
			t.put(v, new KBApp<>(min, Collections.emptyList()));
		}
		lhs0 = lhs0.subst(t);
		rhs0 = rhs0.subst(t);

		if (gt_lpo(lhs0, rhs0)) {
			return rhs0;
		}
		return null;
	}

	private Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> reduce(Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> set) throws InterruptedException {
		Set<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> p = new HashSet<>();
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : set) {
			KBExp<Chc<V, C>, V> lhs = red(new HashMap<>(), Util.append(E, G), R, e.first);
			KBExp<Chc<V, C>, V> rhs = red(new HashMap<>(), Util.append(E, G), R, e.second);
			if (lhs.equals(rhs)) {
				continue;
			}
			p.add(new Pair<>(lhs, rhs));
		}
		return p;
	}

	private boolean strongGroundJoinable(KBExp<Chc<V, C>, V> s, KBExp<Chc<V, C>, V> t) throws InterruptedException {
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> R0 = new LinkedList<>();
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> E0 = new LinkedList<>();
		for (Chc<V, C> f : AC_symbols.keySet()) {
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> lx = AC_symbols.get(f);
			R0.add(lx.get(0));
			E0.addAll(lx.subList(1, 5));
		}

		if (!s.equals(red(null, new LinkedList<>(), R0, s))) {
			return false;
		}
		if (!t.equals(red(null, new LinkedList<>(), R0, t))) {
			return false;
		}
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : E0) {
			Map<V, KBExp<Chc<V, C>, V>> m = subsumes0(fresh, new Pair<>(s, t), e);
			if (m == null) {
				m = subsumes0(fresh, new Pair<>(t, s), e);
			}
			if (m == null) {
				m = subsumes0(fresh, new Pair<>(s, t), e.reverse());
			}
			if (m == null) {
				m = subsumes0(fresh, new Pair<>(s, t), e.reverse());
			}
			if (m == null) {
				continue;
			}
			return false;
		}

		KBExp<Chc<V, C>, V> s0 = s.sort(AC_symbols.keySet());
		KBExp<Chc<V, C>, V> t0 = t.sort(AC_symbols.keySet());

		return s0.equals(t0);

	}

	// TODO: when filtering for subsumed, can also take G into account
	private boolean step() throws InterruptedException {
		checkParentDead();

		if (checkEmpty()) {
			return true;
		}

		if (options.semantic_ac) {
			filterStrongGroundJoinable();
		}

		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> st = pick(E);

		KBExp<Chc<V, C>, V> s0 = st.first;
		KBExp<Chc<V, C>, V> t0 = st.second;
		KBExp<Chc<V, C>, V> a = null, b = null;
		boolean oriented = false;
		if (gt_lpo(s0, t0)) {
			a = s0;
			b = t0;
			oriented = true;
		} else if (gt_lpo(t0, s0)) {
			a = t0;
			b = s0;
			oriented = true;
		} else if (s0.equals(t0)) {
			remove(E, st);
			return false; // in case x = x coming in
		} else {
			if (options.unfailing) {
				remove(E, st);
				add(E, st); // for sorting, will add to end of list
				a = s0;
				b = t0;
			} else {
				throw new RuntimeException("Unorientable: " + st.first + " = " + st.second);
			}
		}
		Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ab = new Pair<>(a, b);
		if (oriented) {
			R.add(ab);
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> CP = filterSubsumed(allcps(seen, ab));
			addAll(E, CP);
			remove(E, st);
			collapseBy(ab);
		} else {
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> CP = filterSubsumed(allcps(seen, ab));
			CP.addAll(filterSubsumed(allcps(seen, ab.reverse())));
			CP.addAll(filterSubsumed(allcps2(seen, ab)));
			CP.addAll(filterSubsumed(allcps2(seen, ab.reverse())));
			addAll(E, CP);
		}

		checkParentDead();

		if (options.compose) {
			compose();
			checkParentDead();
		}

		simplify(); //needed for correctness
		checkParentDead();

		if (options.sort_cps) {
			sortByStrLen(E);
			checkParentDead();
		}

		if (options.filter_subsumed_by_self) {
			E = filterSubsumedBySelf(E);
			checkParentDead();
		}

//		System.out.println(this);
		
		return false;
	}

	void filterStrongGroundJoinable() throws InterruptedException {
		List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> newE = new LinkedList<>(E);
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> st : newE) {
			if (strongGroundJoinable(st.first, st.second)) {
				remove(E, st);
				add(G, st);
			}
		}
		G = filterSubsumedBySelf(G);
	}

	private Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> pick(List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> l) {
		for (int i = 0; i < l.size(); i++) {
			Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> x = l.get(i);
			if (orientable(x)) {
				return l.get(i);
			}
		}
		return l.get(0);
	}

	boolean orientable(Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e) {
		return (gt_lpo(e.first, e.second) || gt_lpo(e.second, e.first));
	}

	// TODO: add ground-completeness check sometime
	private boolean checkEmpty() throws InterruptedException {
		if (E.isEmpty()) {
			isComplete = true;
			isCompleteGround = true;
			return true;
		}
		if (!allUnorientable()) {
			return false;
		}
		if (allCpsConfluent(false, false) || (options.semantic_ac && allCpsConfluent(false, true))) {
			isComplete = false;
			isCompleteGround = true;
			return true;
		}

		return false;
	}

	private boolean allUnorientable() {
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : E) {
			if (orientable(e)) {
				return false;
			}
		}
		return true;
	}

	private boolean allCpsConfluent(boolean print, boolean ground) throws InterruptedException {
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : E) {
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> set = filterSubsumed(reduce(allcps2(new HashSet<>(), e)));
			if (!allCpsConfluent(print, ground, "equation " + e, set)) {
				return false;
			}
		}
		for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : R) {
			List<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> set = filterSubsumed(reduce(allcps(new HashSet<>(), e)));
			if (!allCpsConfluent(print, ground, "rule" + e, set)) {
				return false;
			}
		}
		return true;
	}

	private boolean allCpsConfluent(boolean print, boolean ground, String s, Collection<Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>>> set) throws InterruptedException {
		outer: for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> e : set) {
			KBExp<Chc<V, C>, V> lhs = red(new HashMap<>(), Util.append(E, G), R, e.first);
			KBExp<Chc<V, C>, V> rhs = red(new HashMap<>(), Util.append(E, G), R, e.second);
			if (!lhs.equals(rhs)) {
				if (!ground) {
					return false;
				} else {
					for (Pair<KBExp<Chc<V, C>, V>, KBExp<Chc<V, C>, V>> ex : G) {
						if (subsumes(fresh, new Pair<>(lhs, rhs), ex) || subsumes(fresh, new Pair<>(rhs, lhs), ex)) {
							continue outer;
						}
					}
					if (options.semantic_ac) {
						if (!lhs.sort(AC_symbols.keySet()).equals(rhs.sort(AC_symbols.keySet()))) {
							return false;
						}
					}
				}
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

	private static Comparator<Object> ToStringComparator = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			if (o1.toString().length() > o2.toString().length()) {
				return 1;
			} else if (o1.toString().length() < o2.toString().length()) {
				return -1;
			}
			return o1.toString().compareTo(o2.toString());
		}
	};
	

	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		return qnf(lhs).equals(qnf(rhs));
	}

	@Override
	public boolean hasNFs() {
		return isComplete;
	}

	private KBExp<Chc<V, C>, V> qnf(KBExp<C, V> e) {
		try {
			if (isComplete) {
				return red(null, Util.append(E, G), R, e.inject());
			} else if (isCompleteGround) {
				return red(null, Util.append(E, G), R, e.skolemize());
			}
			throw new RuntimeException("Anomaly: please report");
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> e) {
		return deject(qnf(e));
	}

	private KBExp<C, V> deject(KBExp<Chc<V, C>, V> e) {
		if (e.isVar) {
			return new KBVar<>(e.getVar().var);
		}
		if (e.getApp().f.left) {
			throw new RuntimeException("Anomaly: skolem term " + e.getApp().f.l + " is escaping in " + e);
		}
		return new KBApp<>(e.getApp().f.r, e.getApp().args.stream().map(this::deject).collect(Collectors.toList()));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean gt(Chc<V, C> lhs, Chc<V, C> rhs) {
		if (lhs.equals(rhs)) {
			return false;
		} else if (min.equals(lhs)) {
			return false;
		} else if (min.equals(rhs)) {
			return true;
		} else if (lhs.left && rhs.left) {
			return lhs.l.toString().compareTo(rhs.l.toString()) > 0;
		} else if (lhs.left) {
			return true;
		} else if (rhs.left) {
			return false;
		}

		int i = prec.indexOf(lhs.r);
		int j = prec.indexOf(rhs.r);
		if (i == -1 || j == -1) {
			throw new RuntimeException("Anomaly: please report");
		}
		return i > j;
	};
	
	public boolean gt_lpo(KBExp<Chc<V, C>, V> s, KBExp<Chc<V, C>, V> t) {
		return gt_lpo1(s, t) || gt_lpo2(s, t);
	}

	public boolean gt_lpo1(KBExp<Chc<V, C>, V> s, KBExp<Chc<V, C>, V> t) {
		if (s.isVar) {
			return false;
		} else {
			for (KBExp<Chc<V, C>, V> si : s.getApp().args) {
				if (si.equals(t) || gt_lpo(si, t)) {
					return true;
				}
			}
			return false;
		}
	}

	public boolean gt_lpo2(KBExp<Chc<V, C>, V> s, KBExp<Chc<V, C>, V> t) {
		if (s.isVar || t.isVar) {
			return false;
		}
		KBApp<Chc<V, C>, V> S = s.getApp();
		KBApp<Chc<V, C>, V> T = t.getApp();
		for (KBExp<Chc<V, C>, V> ti : T.args) {
			if (!gt_lpo(S, ti)) {
				return false;
			}
		}
		if (S.f.equals(T.f)) {
			return gt_lpo_lex(S.args, T.args);
		} else {
			return gt(S.f, T.f);
		}
	}

	public boolean gt_lpo_lex(List<KBExp<Chc<V, C>, V>> ss, List<KBExp<Chc<V, C>, V>> tt) {
		if (ss.size() != tt.size()) {
			throw new RuntimeException("Anomaly: please report");
		}
		if (ss.isEmpty()) {
			return false;
		}
		KBExp<Chc<V, C>, V> s0 = ss.get(0), t0 = tt.get(0);
		if (gt_lpo(s0, t0)) {
			return true;
		}
		if (!s0.equals(t0)) {
			return false;
		}
		return gt_lpo_lex(ss.subList(1, ss.size()), tt.subList(1, tt.size()));
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Constraint satisfaction problem for LPO orientability, used to infer precedences
	// http://www.jaist.ac.jp/~hirokawa/publications/03rta.pdf
	// "Tsukuba Termination Tool" Nao Hirokawa and Aart Middeldorp

	private static <X> Set<DAG<X>> tru() {
		return Util.singSet(new DAG<>());
	}
	
	private static <X> Set<DAG<X>> fals() {
		return Collections.emptySet();
	}
	
	private static <X> Set<DAG<X>> and(Set<DAG<X>> a, Set<DAG<X>> b) {
		Set<DAG<X>> ret = new HashSet<>();
		for (DAG<X> x : a) {
			for (DAG<X> y : b) {
				DAG<X> xy = union(x, y);
				if (xy != null) {
					ret.add(xy);
				}
			}
		}
//		System.out.println("and " + a + " and " + b + " ret " + ret + "");

		return min(ret);
	}
	

	private static <X> DAG<X> union(DAG<X> a, DAG<X> b) {
		DAG<X> ret = new DAG<>();
		Set<X> xs = Util.union(a.vertices(), b.vertices());
		for (X x1 : xs) {
			for (X x2 : xs) {
				if (x1.equals(x2)) {
					continue;
				}
				if (a.hasPath(x1, x2) || b.hasPath(x1, x2)) {
					boolean added = ret.addEdge(x1, x2);
					if (!added) {
						return null;
					}
				}
			}
		}
//		System.out.println("union " + a + " and " + b + " ret " + ret + "");

		return ret;
	}
	
	private static <X> Set<DAG<X>> or(Set<DAG<X>> a, Set<DAG<X>> b) {
		return min(Util.union(a, b));
	}
	
	private static <X> Set<DAG<X>> min(Set<DAG<X>> a) { //TODO: add this in
		Set<DAG<X>> ret = new HashSet<>();
		for (DAG<X> x : a) {
			if (minimal(x, a)) {
				ret.add(x);
			}
		}
		return ret;
	}
	
	
	private static <X> boolean minimal(DAG<X> x, Set<DAG<X>> a) {
		//System.out.println("is " + x + " minimal against " + a + "?");
		for (DAG<X> aa : a) {
			if (lessThanOrEqual(aa, x) && !x.equals(aa)) {
				//System.out.println("no");
				return false;
			}
		}
		//System.out.println("yes");
		return true;
	}

	private static <X> boolean lessThanOrEqual(DAG<X> a, DAG<X> b) {
		//System.out.println("is " + a + " lessThanOrEqual " + b + "?");
		Set<X> xs = Util.union(a.vertices(), b.vertices());
		for (X x1 : xs) {
			for (X x2 : xs) {
				if (a.hasPath(x1, x2) && !b.hasPath(x1, x2)) {
					//System.out.println("no");
					return false;
				}
			}
		}
		//System.out.println("yes");
		return true;
	}

	 private static <X,V> Set<DAG<X>> eq(KBExp<X, V> s, KBExp<X, V> t) {
		if (s.equals(t)) {
//			System.out.println("eq " + s + " = " + t + " ret " + tru() + "");
			return tru();
		}
	//	System.out.println("eq " + s + " = " + t + " ret " + fals() + "");
		return fals();
	} 
	
	private static <X> Set<DAG<X>> gtInfer(X lhs, X rhs) {
		if (lhs.equals(rhs)) {
			throw new RuntimeException("Anomaly: please report");
		}
		DAG<X> d = new DAG<>();
		d.addEdge(lhs, rhs);
//		System.out.println(" " + lhs + " > " + rhs + " ret " + d + "");
		return Util.singSet(d);
	} 

	public static <X,V> Set<DAG<X>> gt_lpoInfer(KBExp<X, V> s, KBExp<X, V> t) throws InterruptedException {
		return or(gt_lpo1Infer(s, t), gt_lpo2Infer(s, t)); //TODO: remove non-minimal elements everywhere
	} 

	public static <X,V> Set<DAG<X>> gt_lpo1Infer(KBExp<X, V> s, KBExp<X, V> t) throws InterruptedException {
	//	System.out.println("1 start on " + s + " -> " + t);
		if (s.isVar) {
	//		System.out.println("1 end on " + s + " -> " + t + " ret " + fals() + "");
			return fals();
		} else {
			Set<DAG<X>> ret = fals();
			for (KBExp<X, V> si : s.getApp().args) {
				ret = or(ret, eq(si, t));
				ret = or(ret, gt_lpoInfer(si, t));
			}
	//		System.out.println("1 end on " + s + " -> " + t + " ret " + ret + "");

			return ret;
		}
	} 

	public static <X,V> Set<DAG<X>> gt_lpo2Infer(KBExp<X, V> s, KBExp<X, V> t) throws InterruptedException {
	//	System.out.println("2 start on " + s + " -> " + t);

		if (s.isVar || t.isVar) {
//			System.out.println("2 end on " + s + " -> " + t + " ret " + fals() + "");			
			return fals();
		}
		KBApp<X, V> S = s.getApp();
		KBApp<X, V> T = t.getApp();
	
		Set<DAG<X>> ret = tru();
		for (KBExp<X, V> ti : T.args) {
			ret = and(ret, gt_lpoInfer(S, ti));
		}
		
		Set<DAG<X>> zz = null;
		if (S.f.equals(T.f)) {
			zz = and(ret, gt_lpo_lexInfer(S.args, T.args));
	//		System.out.println("2 end on " + s + " -> " + t + " ret " + zz);
		} else {
			zz = and(ret, gtInfer(S.f, T.f));
	//		System.out.println("2 end on " + s + " -> " + t + " ret " + zz);
		}
		return zz;
	}

	public static <X,V> Set<DAG<X>> gt_lpo_lexInfer(List<KBExp<X, V>> ss, List<KBExp<X, V>> tt) throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		if (ss.size() != tt.size()) {
			throw new RuntimeException("Anomaly: please report");
		}
		if (ss.isEmpty()) {
			return fals();
		}
		KBExp<X, V> s0 = ss.get(0), t0 = tt.get(0);	
		return or(gt_lpoInfer(s0, t0), and(eq(s0,t0), gt_lpo_lexInfer(ss.subList(1, ss.size()), tt.subList(1, tt.size()))));	
	}
	
	public static <C,V> List<C> inferPrec(Map<C, Integer> symbols, Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0) throws InterruptedException {
		Set<DAG<C>> ret = tru();
		for (Pair<KBExp<C, V>, KBExp<C, V>> R : R0) {
			ret = and(ret, gt_lpoInfer(R.first, R.second));
		}
		if (ret.isEmpty()) {
			throw new RuntimeException("There is no LPO precedence that can orient all rules from left to right.  (Unfailing) completion can still be used, but you will have to specify a precedence manually.");
		}
//		System.out.println("zz " + ret.stream().map(x -> toPrec(symbols, x)).collect(Collectors.toList()));
		DAG<C> g = Util.get0X(ret);
		return toPrec(symbols, g); //TODO: just pick one randomly and make it total randomly.   

	}

	//arity-0 < arity-2 < arity-1 < arity-3 < arity-4
	private static <C> List<C> toPrec(Map<C, Integer> cs, DAG<C> g) {
//		System.out.println("init " + g.topologicalSort());
		List<C> ret = new LinkedList<>(g.topologicalSort()); //biggest first
		
		List<C> extra = new LinkedList<>(cs.keySet()); //bigest first
		extra.removeAll(g.vertices());
		Function<Integer, Integer> f = x -> {
			if (x == 2) {
				return 1;
			} else if (x == 1) {
				return 2;
			}
			return x;
		};
		extra.sort((x,y) -> {
			int x0 = cs.get(x);
			int y0 = cs.get(y);
			return Integer.compare(f.apply(x0), f.apply(y0));
		});
		ret.addAll(0, extra);
//		System.out.println("final " + ret);
		return Util.reverse(ret);
	}


	
	
}
