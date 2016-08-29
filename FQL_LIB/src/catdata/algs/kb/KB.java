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

import catdata.EqProver;
import catdata.Pair;
import catdata.Triple;
import catdata.Utils;
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
 * @param <C> the type of functions/constants
 * @param <V> the type of variables
 */
public class KB<C, V> extends EqProver<C, V> {
	 
	protected boolean isComplete = false;
	protected boolean isCompleteGround = false;
	
	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> R, E, G; //order matters
	
	protected Iterator<V> fresh;
	
	public Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt;
	protected Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen = new HashSet<>();	

	protected Map<C, List<Pair<KBExp<C, V>, KBExp<C, V>>>> AC_symbols;
//	protected List<Pair<KBExp<C, V>, KBExp<C, V>>> AC_R, G;
	
	protected int count = 0;

	protected KBOptions options;
	
	/**
	 * @param E0 initial equations
	 * @param gt0 ordering
	 * @param fresh fresh variable generator
	 */
	public KB(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0, Function<Pair<KBExp<C, V>, 
			KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh,
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0, KBOptions options) {
		this.options = options;
		this.R = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : R0) {
			R.add(freshen(fresh, r));
		}
		this.gt = gt0;
		this.fresh = fresh;
		this.E = new LinkedList<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E0) {
			E.add(freshen(fresh, e));
		}
		this.G = new LinkedList<>();
		initAC();
//		System.out.println("AC symbols: " + AC_symbols);
		initHorn();
	}
	
	
	
	private void initAC() {
		if (!options.semantic_ac) {
			return;
		}
		Map<C, Integer> symbols = new HashMap<>();
		AC_symbols = new HashMap<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			e.first.symbols(symbols);
			e.second.symbols(symbols);
		}
		outer: for (C f : symbols.keySet()) {
			Integer i = symbols.get(f);
			if (i.intValue() != 2) {
				continue;
			}
			boolean cand1_found = false;
			boolean cand2_found = false;
			List<Pair<KBExp<C, V>, KBExp<C, V>>> cands = AC_E(f);
			Pair<KBExp<C, V>, KBExp<C, V>> cand1 = cands.get(0);
			Pair<KBExp<C, V>, KBExp<C, V>> cand2 = cands.get(1);
			for (Pair<KBExp<C, V>, KBExp<C, V>> other : E) {
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
					List<Pair<KBExp<C, V>, KBExp<C, V>>> l = new LinkedList<>();
					l.add(AC_E(f).get(1)); //assoc rewrite rule
					l.add(AC_E(f).get(0)); //comm eq
					l.addAll(AC_E0(f)); //perm eqs
					AC_symbols.put(f, l);
					continue outer;
				}
			}
		}
	}
	
	private KBExp<C, V> achelper(C f, V xx, V yy, V zz) {
		KBExp<C,V> x = new KBVar<>(xx);
		KBExp<C,V> y = new KBVar<>(yy);
		KBExp<C,V> z = new KBVar<>(zz);
		List<KBExp<C,V>> yz = new LinkedList<>();
		yz.add(y);
		yz.add(z);
		List<KBExp<C,V>> xfyz = new LinkedList<>();
		xfyz.add(x);
		xfyz.add(new KBApp<>(f, yz));
		return new KBApp<>(f, xfyz);
	}
	
	private List<Pair<KBExp<C, V>, KBExp<C, V>>> AC_E0(C f) {
		List<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new LinkedList<>(); 
		V x = fresh.next();
		V y = fresh.next();
		V z = fresh.next();
		
		ret.add(freshen(fresh, new Pair<>(achelper(f, x, y, z), achelper(f, y, x, z))));
		ret.add(freshen(fresh, new Pair<>(achelper(f, x, y, z), achelper(f, z, y, x))));
		ret.add(freshen(fresh, new Pair<>(achelper(f, x, y, z), achelper(f, y, z, x))));
		
		return ret;
	}
	
	
	
	private List<Pair<KBExp<C, V>, KBExp<C, V>>> AC_E(C f) {
		List<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new LinkedList<>(); 
		KBExp<C,V> x = new KBVar<>(fresh.next());
		KBExp<C,V> y = new KBVar<>(fresh.next());
		List<KBExp<C,V>> xy = new LinkedList<>();
		xy.add(x);
		xy.add(y);
		List<KBExp<C,V>> yx = new LinkedList<>();
		yx.add(y);
		yx.add(x);
		ret.add(new Pair<>(new KBApp<>(f, xy), new KBApp<>(f, yx)));
		
		x = new KBVar<>(fresh.next());
		y = new KBVar<>(fresh.next());
		KBExp<C,V> z = new KBVar<>(fresh.next());
		List<KBExp<C,V>> yz = new LinkedList<>();
		yz.add(y);
		yz.add(z);
		xy = new LinkedList<>();
		xy.add(x);
		xy.add(y);
		List<KBExp<C,V>> xfyz = new LinkedList<>();
		xfyz.add(x);
		xfyz.add(new KBApp<>(f, yz));
		List<KBExp<C,V>> fxyz = new LinkedList<>();
		fxyz.add(new KBApp<>(f, xy));
		fxyz.add(z);
		ret.add(new Pair<>(new KBApp<>(f, fxyz), new KBApp<>(f, xfyz)));
		
		return ret;
	}

	private void initHorn() {
		if (!options.horn) {
			return;
		}
		R.add(new Pair<>(KBHorn.not(KBHorn.tru()), KBHorn.fals()));
		R.add(new Pair<>(KBHorn.not(KBHorn.fals()), KBHorn.tru()));
		KBExp<C,V> x = new KBVar<>(fresh.next());
		R.add(new Pair<>(KBHorn.or(x, KBHorn.tru()), KBHorn.tru()));
		x = new KBVar<>(fresh.next());
		R.add(new Pair<>(KBHorn.or(x, KBHorn.fals()), x));
		x = new KBVar<>(fresh.next());
		R.add(new Pair<>(KBHorn.or(KBHorn.tru(), x), KBHorn.tru()));
		x = new KBVar<>(fresh.next());
		R.add(new Pair<>(KBHorn.or(KBHorn.fals(), x), x));
		x = new KBVar<>(fresh.next());
		R.add(new Pair<>(KBHorn.eq(x, x), KBHorn.tru()));
	//	x = new KBVar<>(fresh.next());
	//	R.add(new Pair<>(KBHorn.or(x, KBHorn.not(x)), KBHorn.tru()));
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected static <C, V> Pair<KBExp<C, V>, KBExp<C, V>> freshen(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> eq) {
		Map<V, KBExp<C, V>> subst = freshenMap(fresh, eq).first;
		return new Pair<>(eq.first.subst(subst), eq.second.subst(subst));
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
		if (!options.unfailing) {
			l.sort(ToStringComparator);
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
			orientable.sort(ToStringComparator);
			l.clear();
			l.addAll(orientable);
			l.addAll(unorientable);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void checkParentDead(Thread cur) {
		if (!cur.isAlive()) {
			Thread.currentThread().stop();
		} 
	}
	
	@SuppressWarnings("deprecation")
	//if the parent dies, the current thread will too
	public void complete(Thread parent) {
		final String[] arr = new String[] { null };
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					while (!step(parent));
				} catch (Exception ex) {
					ex.printStackTrace();
					arr[0] = ex.getMessage();
				}
			}
		};				
		Thread t = new Thread(r);
		t.start();
		try {
			t.join(options.iterations);
			t.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
		if (arr[0] != null) {
			throw new RuntimeException(arr[0] + "\n\nLast state:\n\n" + printKB());			
		}
		if (!isCompleteGround) {
			System.out.println("---------------");
			//allCpsConfluent(true, true);
			throw new RuntimeException("Not ground complete after iteration timeout.  Last state:\n\n" + printKB());
		} 
	}
	
	protected static <C, V> boolean subsumes(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> cand,
			Pair<KBExp<C, V>, KBExp<C, V>> other) {
		return (subsumes0(fresh, cand, other) != null);
	}
	
/*	private static <C,V> boolean bijection(Map<V, KBExp<C, V>> m) {
		
		Map<V, V> ret = new HashMap<>();
		for (V v : m.keySet()) {
			KBExp<C, V> e = m.get(v);
			if (!e.isVar) {
				return false;
			}
			KBVar<C, V> ee = e.getVar();
			ret.put(v, ee.var);
		}
		
		return Utils.isBijection(ret, ret.keySet(), new HashSet<>(ret.values()));
		
	} */
	
	@SuppressWarnings("unchecked")
	protected static <C, V> Map<V, KBExp<C, V>> subsumes0(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> cand,
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

		List<KBExp<C, V>> r = new LinkedList<>(); r.add(other.first); r.add(other.second);
		KBApp<C, V> other0 = new KBApp<C, V>((C) "", r);
		
		Map<V, KBExp<C, V>> subst = KBUnifier.findSubst(other0, cand0);
		return subst;
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
	//simplify RHS of a rule
	protected void compose() {
		Pair<KBExp<C, V>, KBExp<C, V>> to_remove = null;
		Pair<KBExp<C, V>, KBExp<C, V>> to_add = null;
		do {
			to_remove = null;
			to_add = null;
			for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
				Set<Pair<KBExp<C, V>, KBExp<C, V>>> R0 = new HashSet<>(R);
				R0.remove(r);
				KBExp<C, V> new_rhs = red(null, Utils.append(E,G), R0, r.second);
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
	
	// TODO For this to be a true semi-decision procedure, open terms should first be skolemized
	@Override
	public boolean eq(KBExp<C, V> lhs, KBExp<C, V> rhs) {
		KBExp<C, V> lhs0 = nf(lhs);
		KBExp<C, V> rhs0 = nf(rhs);
		if (lhs0.equals(rhs0)) {
			return true;
		}

		if (isComplete) {
			return false;
		}

		step(Thread.currentThread());
		return eq(lhs, rhs);
	} 
	
	@Override
	public KBExp<C, V> nf(KBExp<C, V> e) {
		if (e.vars().isEmpty()) {
			if (!isCompleteGround) {
				throw new RuntimeException("Cannot find ground normal form for ground incomplete system.");
			}
			return red(null, Utils.append(E,G), R, e);
		}
		if (!isComplete) {
			throw new RuntimeException("Cannot find normal form for incomplete system.\n\n" + this);
		}
		return red(null, Utils.append(E,G), R, e);
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Requires <C> and <V> to be String.  Renames _v345487 to _v0, for example.
	 * 
	 * @return A nicer printout of the rules
	 */
	@Override
	public String printKB() {
		KB<String, String> kb = (KB<String, String>) this; //dangerous
		
	//	List<Pair<KBExp<String, String>, KBExp<String, String>>> EE = new LinkedList<>(kb.E);
	//	EE.addAll(kb.G);
		
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
		
		List<String> G0 = new LinkedList<>();
		for (Pair<KBExp<String, String>, KBExp<String, String>> r : kb.G) {
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
			G0.add(stripOuter(r.first.subst(m).toString()) + " = " + stripOuter(r.second.subst(m).toString()));
		}
		G0.sort(new Comparator<String>() {
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
				
		return (Utils.sep(R0, "\n\n") + "\n\nE--\n\n" + Utils.sep(E0, "\n\n") + "\n\nG--\n\n" + Utils.sep(G0, "\n\n")).trim();
	}
	
	protected static String stripOuter(String s) {
		if (s.startsWith("(") && s.endsWith(")")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	protected KBExp<C, V> red(Map<KBExp<C,V>, KBExp<C,V>> cache, 
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> Ex,
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> Ry,
			KBExp<C, V> e) {
		int i = 0;
		KBExp<C, V> orig = e;
//		Collection<Pair<KBExp<C, V>, KBExp<C, V>>> Ey = new LinkedList<>(Ex);
	//	Ey.addAll(G);
	//	List<String> errs = new LinkedList<>();
		for (;;) {
			i++;			
	//		errs.add("iteration " + i + ", pre, e=" + e);

			KBExp<C, V> e0 = step(cache, fresh, Ex, Ry, e);
			if (e.equals(e0)) {
				return e0;  
			}
		//	errs.add("iteration " + i + ", post, e=" + e + " and e0= " + e0);
			if (i > options.red_its) {
				throw new RuntimeException(
						"Reduction taking too long (>" + options.red_its + "):" + orig + " goes to " + e0 + " under\n\neqs:" + Utils.sep(E,"\n") + "\n\nreds:"+ Utils.sep(R,"\n"));
					//	+ "\n\n and \ne=" + e + " and \ne0=" + e0 + " and \ne=e0=" + e.equals(e0) + " and \ne0=e=" + e0.equals(e) + " e=e0asptr=" + (e == e0) + 
						//"\nehash=" + e.hashCode() + "\ne0hash=" + e0.hashCode() + "\n" + Util.sep(errs, "\n"));
			}
			e = e0;
		}
	}
	
	protected KBExp<C, V> step(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh,
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> E, Collection<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> ee) {
		if (ee.isVar) {
			return step1(cache, fresh, E, R, ee); 
		} else {
			KBApp<C, V> e = ee.getApp();
			List<KBExp<C, V>> args0 = new LinkedList<>();
			for (KBExp<C, V> arg : e.args) {
				args0.add(step(cache, fresh, E, R, arg)); //needs to be step for correctness
			}
			KBApp<C, V> ret = new KBApp<>(e.f, args0);
			return step1(cache, fresh, E, R, ret);
		} 
	}
	
	//simplifies equations
	//can also use E U G with extra checking
	protected void simplify() {
		Map<KBExp<C,V>, KBExp<C,V>> cache = new HashMap<>();  //helped 2x during tests

		List<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new LinkedList<>();
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE2 = new HashSet<>(); //also helpful for performance
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			KBExp<C, V>	lhs_red = red(cache, new LinkedList<>(), R, e.first);
			KBExp<C, V> rhs_red = red(cache, new LinkedList<>(), R, e.second);
			if (!lhs_red.equals(rhs_red)) {
				Pair<KBExp<C, V>, KBExp<C, V>> p = new Pair<>(lhs_red, rhs_red);
				if (!newE2.contains(p)) {
					newE.add(p);
					newE2.add(p);
				}
//				add(newE, p);
			}
		}
		E = newE;
	}

	//is not collapse2
	//can also use E U G here
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
		
		for (Pair<KBExp<C, V>, KBExp<C, V>> gd : R) {
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> s;

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

	protected  Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Pair<KBExp<C, V>, KBExp<C, V>> gd0, Pair<KBExp<C, V>, KBExp<C, V>> ab0) {
		Pair<KBExp<C, V>, KBExp<C, V>> ab = freshen(fresh, ab0);
		Pair<KBExp<C, V>, KBExp<C, V>> gd = freshen(fresh, gd0);
		
		Set<Triple<KBExp<C, V>, KBExp<C, V>, Map<V,KBExp<C,V>>>> retX = gd.first.cp(new LinkedList<>(), ab.first,
				ab.second, gd.first, gd.second);

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
				ret.add(toAdd);
		}
		
		return ret;
	}

	protected KBExp<C, V> step1(Map<KBExp<C,V>, KBExp<C,V>> cache, Iterator<V> fresh,
			Collection<Pair<KBExp<C, V>, KBExp<C, V>>> E, Collection<Pair<KBExp<C, V>, KBExp<C, V>>> R, KBExp<C, V> e0) {
		KBExp<C, V> e = e0;
		if (cache != null && cache.containsKey(e)) {
			return cache.get(e);
		}
		//does not improve performance
		//Map<Pair<KBExp<C, V>, KBExp<C, V>>, Map<V, KBExp<C, V>>> findSubstCache = new HashMap<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> r0 : R) {
			Pair<KBExp<C, V>, KBExp<C, V>> r = r0;
			if (!Collections.disjoint(r.first.vars(), e.vars()) || !Collections.disjoint(r.second.vars(), e.vars())) {
				r = freshen(fresh, r0);
			}
			
			KBExp<C, V> lhs = r.first;
			KBExp<C, V> rhs = r.second;
			Map<V, KBExp<C, V>> s = null;
		//	if (lhs.equals(e)) { doesn't seem to help
			//	e = rhs;
		//		continue;
		//	} 
		//	if (findSubstCache.containsKey(new Pair<>(lhs, e))) {
				s = KBUnifier.findSubst(lhs, e);
			//	if (s != null) {
		//			findSubstCache.put(new Pair<>(lhs, e), s);
		//		}
		//	} else {
			//	s = KBUnifier.findSubst(lhs, e);				
		//	}
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
	
	protected KBExp<C, V> step1Es(Collection<Pair<KBExp<C, V>, KBExp<C, V>>> E, KBExp<C, V> e) {
		if (options.unfailing) {
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
			KBExp<C, V> lhs = red(new HashMap<>(), Utils.append(E,G), R, e.first);
			KBExp<C, V> rhs = red(new HashMap<>(), Utils.append(E,G), R, e.second);
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
	
	protected boolean strongGroundJoinable(KBExp<C, V> s, KBExp<C, V> t) {
		//System.out.println("-----------");
		//System.out.println(s + " = " + t);
		List<Pair<KBExp<C, V>, KBExp<C, V>>> R0 = new LinkedList<>();
		List<Pair<KBExp<C, V>, KBExp<C, V>>> E0 = new LinkedList<>();
		for (C f : AC_symbols.keySet()) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> lx = AC_symbols.get(f);
			R0.add(lx.get(0));
			E0.addAll(lx.subList(1, 5));
		}
			
		if (!s.equals(red(null, new LinkedList<>(), R0, s))) {
	//		System.out.println("1");
			return false;
		}
		if (!t.equals(red(null, new LinkedList<>(), R0, t))) {
		//	System.out.println("2");
			return false;
		}
		//TODO
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E0) {
			Map<V, KBExp<C,V>> m = subsumes0(fresh, new Pair<>(s,t), e);
			if (m == null) {
				m = subsumes0(fresh, new Pair<>(t,s), e);
			}
			if (m == null) {
				m = subsumes0(fresh, new Pair<>(s,t), e.reverse());
			}
			if (m == null) {
				m = subsumes0(fresh, new Pair<>(s, t), e.reverse());
			}
			if (m == null) {
				continue;
			}
			//return false;
			//if (bijection(m)) {
				//System.out.println("bij with " + e);
				//continue;
			//} 
			return false;
		}
			
		KBExp<C, V> s0 = s.sort(AC_symbols.keySet());
		KBExp<C, V> t0 = t.sort(AC_symbols.keySet());
	//	System.out.println(s + " sorts to " + s0);
	//	System.out.println(t + " sorts to " + t0);
		
		return s0.equals(t0);
		
//		return false;
	}
	
	//TODO: when filtering for subsumed, can also take G into account
	protected boolean step(Thread parent) {
//		System.out.println("\n\niteration " + count);
	//	System.out.println(this);
		count++;

		checkParentDead(parent); 
		
		if (options.horn) {
			handleHorn();
		}
		
		if (checkEmpty()) {
			return true;
		}

		if (options.semantic_ac) {
			filterStrongGroundJoinable();
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
			if (options.unfailing) {
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
		}
		
		checkParentDead(parent); 
		
		if (options.compose) {
			compose();
			checkParentDead(parent); 
		}
		
		//TODO: appear to need simplify for correctness.  checked again: definitely need
//		if (options.simplify) {
			simplify(); //definitely needed... cuts down on number of iterations
			//simplify2();	//TODO: add this in for efficiency sometime 
			checkParentDead(parent); 
	//	}
		
		if (options.sort_cps) {
			sortByStrLen(E);
			checkParentDead(parent); 
		}
			
		if (options.filter_subsumed_by_self) {
			E = filterSubsumedBySelf(E);
			checkParentDead(parent); 
		}
		
		return false;	
	}
	
	void filterStrongGroundJoinable() {
		List<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new LinkedList<>(E);
		for (Pair<KBExp<C, V>, KBExp<C, V>> st : newE) {
			if (strongGroundJoinable(st.first, st.second)) {
				//System.out.println("removing " + st);
				remove(E, st);
				add(G, st);
			} else {
				//System.out.println("not removing " + st);
			}
		}
		G = filterSubsumedBySelf(G);
	}


	private void handleHorn() {
		Iterator<Pair<KBExp<C, V>, KBExp<C, V>>> it = R.iterator();
		while (it.hasNext()) {
			Pair<KBExp<C, V>, KBExp<C, V>> r = it.next();
			if (!r.second.equals(KBHorn.tru())) {
				continue;
			}
			if (r.first.isVar) {
				continue;
			}
			KBApp<C,V> app = r.first.getApp();
			if (app.f.equals(KBHorn._eq)) {
				if (app.args.get(0).equals(app.args.get(1))) {
					continue;
				}
				E.add(new Pair<>(app.args.get(0), app.args.get(1)));
				it.remove();
			}
		}
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
	
	//TODO: add ground-completeness check sometime
	protected boolean checkEmpty() {
		if (E.isEmpty()) {
			isComplete = true;
			isCompleteGround = true;
			return true;
		}
		if (!noEqsBetweenAtoms() || !noNonReflRewrites() || !allUnorientable()) {
		//	System.out.println("!!!");
			return false;
		}
		if (allCpsConfluent(false, false) || (options.semantic_ac && allCpsConfluent(false, true))) {
			isComplete = false;
			isCompleteGround = true;
			return true;
		}
		
		return false;
	}
	
	protected boolean noEqsBetweenAtoms() {
		if (!options.horn) {
			return true;
		}
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			if (KBHorn.isAtom(e.first) || KBHorn.isAtom(e.second)) {
				//System.out.println("busted1 " + e);
				return false;
			}
		}
		return true;
	}
	
	protected boolean noNonReflRewrites() {
		if (!options.horn) {
			return true;
		}
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : R) {
			if (!e.first.isVar && e.second.equals(KBHorn.tru())) {
				KBApp<C,V> a = e.first.getApp();
				if (a.f.equals(KBHorn._eq)) {
					if (!a.args.get(0).equals(a.args.get(1))) {
						//System.out.println("busted2 " + e);
						return false;
					}
				}
			}
		}
		return true;
	}

	protected boolean allUnorientable() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			if (orientable(e)) {
				return false;
			}
		}
		return true;
	}

	protected boolean allCpsConfluent(boolean print, boolean ground) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set = filterSubsumed(reduce(allcps2(
					new HashSet<>(), e)));
			if (!allCpsConfluent(print, ground, "equation " + e, set)) {
				return false;
			}
		} 
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : R) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set = filterSubsumed(reduce(allcps(new HashSet<>(), e)));
			if (!allCpsConfluent(print, ground, "rule" + e, set)) {
				return false;
			}
		}
		return true;
	}
	
	

	protected boolean allCpsConfluent(boolean print, boolean ground, String s, Collection<Pair<KBExp<C, V>, KBExp<C, V>>> set) {
		outer: for (Pair<KBExp<C, V>, KBExp<C, V>> e : set) {
			KBExp<C, V> lhs = red(new HashMap<>(), Utils.append(E,G), R, e.first);
			KBExp<C, V> rhs = red(new HashMap<>(), Utils.append(E,G), R, e.second);
			if (!lhs.equals(rhs)) {
				if (!ground) {
					System.out.println("regular badness on " + s + " e= " + e + " | lhs=" + lhs + " | rhs=" + rhs);
					return false;
				} else {
					for (Pair<KBExp<C, V>, KBExp<C, V>> ex : G) {
						if (subsumes(fresh, new Pair<>(lhs, rhs), ex) ||
							subsumes(fresh, new Pair<>(rhs, lhs), ex) ) {
							continue outer;
						}
					}
					//TODO
					if (options.semantic_ac) {
						if (!lhs.sort(AC_symbols.keySet()).equals(rhs.sort(AC_symbols.keySet()))) {
							System.out.println("ground badness on " + s + "e= " + e + " | lhs=" + lhs + " | rhs=" + rhs + " | sorts to " 
						+ lhs.sort(AC_symbols.keySet()) + " and " + rhs.sort(AC_symbols.keySet()));
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
		
		return (Utils.sep(a, "\n") + "\n" + Utils.sep(b, "\n")).trim();
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
	
	

}
