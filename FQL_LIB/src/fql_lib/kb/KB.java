package fql_lib.kb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

	/*void validateRule(Pair<KBExp<C, V>, KBExp<C, V>> eq, KBExp<C, V> orig, String str) {
		if (!eq.first.vars().containsAll(eq.second.vars())) {
			log = new LinkedList<>();
			red(orig);
			String s = Util.sep(log, "\n");
			log = null;
			throw new RuntimeException("Bad rule: " + eq.first + " -> " + eq.second + "\n\nmsg is "
					+ s + "\n\n" + str);
		}
	}*/

	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> E, R;
	// private Function<Pair<C,C>, Boolean> gt;
	private Iterator<V> fresh;
	private Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt;

	private static <C,V> Pair<KBExp<C, V>, KBExp<C, V>> freshen(Iterator<V> fresh, Pair<KBExp<C, V>, KBExp<C, V>> eq) {
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

	private Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> freshenMap(
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

	public KB(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0,
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		this.R = new HashSet<>();
		this.gt = gt0;
		this.fresh = fresh;
		this.E = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> eq : E0) {
			// E.add(freshen(eq));
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

	/*
	 * reduce RHS of R by R reduce lhs and rhs of E by R
	 * 
	 * remove tautologies of E remove subsumed rules of E
	 * 
	 * if LHS of R is reducible, remove from R, add to E orient E, add to R add
	 * critical pairs of R to E
	 */
	public void complete(int limit) {
		int count = 0;
		for (;;) {
			count++;
			System.out.println("Starting: " + this);
			if (count > limit) {
				throw new RuntimeException("Exceeded iteration limit. \n" + this);
			}
			if (E.isEmpty()) {
				break;
			}
		
			reduceRhsOfRByR();
		//	System.out.println("After reducing rhs of R by R: " + this);
			reduceLhsAndRhsOfEByR();
			System.out.println("After reducing rhs or R by R and lhs and rhs of E by R: " + this);
		
			 moveLhsReducibleOfRToE(); //TODO: check on technical condition
			 System.out.println("After moving Rs whose lhs reduce to E: " +  this);

			removeTautologiesOfE();
		//	System.out.println("After removing tautologies of E: " + this);
			removeSubsumedOfE();
			System.out.println("After simplifying: " + this);
			check("");

			Set<Pair<KBExp<C, V>, KBExp<C, V>>> or = orientable();
			E.clear();

			findAllCPsOfR(or);
			System.out.println("After orienting and adding critical pairs: " + this);
//			check("");

			System.out.println("-----------------------------------------------");
		}

		// Set<Pair<KBExp<C,V>, KBExp<C,V>>> cps = findAllCPsOfR();
		// TODO: false, all CPs must be confluent
		// if (!cps.isEmpty()) {
		// throw new
		// RuntimeException("Termination but re-write system still has critical pairs "
		// + this + " and " + cps);
		// }
	}

	private void reduceRhsOfRByR() {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newR = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> r : R) {
			newR.add(new Pair<>(r.first, red(fresh, R, r.second)));
		}
		R = newR;
	}

	private void reduceLhsAndRhsOfEByR() {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			KBExp<C, V> lhs_red = red(fresh, R, e.first);
			log = new LinkedList<>();
			KBExp<C, V> rhs_red = red(fresh, R, e.second);
			newE.add(new Pair<>(lhs_red, rhs_red));
			if (lhs_red.toString().equals("z") && rhs_red.toString().equals("(z o z)")) {
				System.out.println("equation was " + e.first + " = " + e.second);
				System.out.println(Util.sep(log, "\n"));
				System.out.println("rules were " + Util.sep(R, "\n"));
				throw new RuntimeException("$$!!");
			}
		}
		E = newE;
	}

	private void removeTautologiesOfE() {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> newE = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			if (e.first.equals(e.second)) {
				continue;
			}
			newE.add(e);
		}
		E = newE;
	}

	private void moveLhsReducibleOfRToE() {
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
					if (gt(st, lr)) {
						it.remove();
						E.add(new Pair<>(new_s, st.second));
						break;						
					}
				}
			}
		}
	}


	private boolean gt(Pair<KBExp<C, V>, KBExp<C, V>> st, Pair<KBExp<C, V>, KBExp<C, V>> lr) {
		if (gt.apply(new Pair<>(st.first, lr.first))) {
			return true;
		}
		//TODO: morally, should be alpha equivalence
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
			} else {
				throw new RuntimeException("Cannot orient " + s0 + " = " + t0);
			}
			ret.add(new Pair<>(a, b));
		}
		return ret;
	}

	void findAllCPsOfR(Set<Pair<KBExp<C, V>, KBExp<C, V>>> newR) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r1 : newR) {
			// Set<Pair<KBExp<C,V>, KBExp<C,V>>> ret = new HashSet<>();
			for (Pair<KBExp<C, V>, KBExp<C, V>> r2 : R) {
				check("before");
				E.addAll(cp(r1, r2));
				check(" a critical pair of " + r1.first + " -> " + r1.second + " and " + r2.first
						+ " -> " + r2.second + " is " + "z = (I(ee) o (z o z))");
				E.addAll(cp(r2, r1));
				check(r2 + " and " + r1);
			}
			R.add(r1);
		}
		// return ret;
	}

	void removeSubsumedOfE() {
		for (;;) {
			Pair<KBExp<C, V>, KBExp<C, V>> cand = findOneSubsumed();
			if (cand == null) {
				return;
			}
			E.remove(cand);
		}
	}

	private Pair<KBExp<C, V>, KBExp<C, V>> findOneSubsumed() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> cand : E) {
			for (Pair<KBExp<C, V>, KBExp<C, V>> other : E) {
				if (cand.equals(other)) {
					continue;
				}
				if (subsumes(cand, other)) {
					return cand;
				}
				if (subsumes(new Pair<>(cand.second, cand.first), other)) {
					return cand;
				}
				if (subsumes(cand, new Pair<>(other.second, other.first))) {
					return cand;
				}
				// TODO: last is superflous?
				// if (subsumes(new Pair<>(cand.second, cand.first), new
				// Pair<>(other.second, other.first)) {
				// return cand;
				// }
			}
		}
		return null;
	}

	static <C, V> boolean subsumes(Pair<KBExp<C, V>, KBExp<C, V>> cand,
			Pair<KBExp<C, V>, KBExp<C, V>> other) {
		// System.out.println("is " + cand + " subsumed by " + other +"?");
		KBApp<C, V> cand0 = new KBApp<C, V>((C) "", Arrays.asList(new KBExp[] {
				cand.first.freeze(), cand.second.freeze() }));
		KBApp<C, V> other0 = new KBApp<C, V>((C) "", Arrays.asList(new KBExp[] { other.first,
				other.second }));
		Map<V, KBExp<C, V>> subst = KBUnifier.findSubst(other0, cand0);
		// if (subst == null) {
		// System.out.println("no");
		// } else {
		// System.out.println("yes, by " + subst);
		// }
		return (subst != null);
	}

	public void complete_old(int limit) {
		int count = 0;
		for (;;) {
			count++;
			System.out.println("Starting: " + this);
			if (count > limit) {
				throw new RuntimeException("Exceeded iteration limit\neqs " + printEqs()
						+ "\n\nreds " + printReds());
			}
			if (E.isEmpty()) {
				break;
			}
			// Pair<KBExp<C,V>, KBExp<C,V>> st_orig = pick(E);
			// Pair<KBExp<C,V>, KBExp<C,V>> st = freshen(st_orig);
			Pair<KBExp<C, V>, KBExp<C, V>> st = pick(E);

			// System.out.println("bbb");
			System.out.println("picked " + st);
			KBExp<C, V> s = st.first;
			KBExp<C, V> t = st.second;
			KBExp<C, V> s0 = red(fresh, R, s);
			// System.out.println("bbb222");
			KBExp<C, V> t0 = red(fresh, R, t);
			// System.out.println("bbb333");
			System.out.println("reduced " + s0 + " and " + t0);
			if (s.equals(t) && !s0.equals(t0)) {
				throw new RuntimeException("Confluence failure");
			}
			if (s0.equals(t0)) {
				E.remove(st); // st_orig
				continue;
			}
			// System.out.println("ccc");
			KBExp<C, V> a, b;
			if (gt.apply(new Pair<>(s0, t0))) {
				a = s0;
				b = t0;
				System.out.println(s0 + " > " + t0);
			} else if (gt.apply(new Pair<>(t0, s0))) {
				a = t0;
				b = s0;
				System.out.println(t0 + " > " + s0);
			} else {
				throw new RuntimeException("Cannot orient " + s0 + " = " + t0);
			}
			Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(a, b);
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> CP = allcps(ab); // .stream().map(x
																	// ->
																	// freshen(x)).collect(Collectors.toSet());
			System.out.println("critical pairs are " + CP);
			// System.out.println()
			//validateRule(ab, st.first, "original was " + st.first + " -> " + st.second + " on "
				//	+ this);
			R.add(ab);
			E.addAll(CP);
			E.remove(st); // st_orig
			// System.out.println("****** " + this);
		}

	}

	private static <X> X pick(Set<X> X) {
		for (X x : X) {
			return x;
		}
		throw new RuntimeException();
	}

	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps(Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> gd : R) {
			System.out.println("Finding critical pairs for " + gd.first + " -> " + gd.second
					+ " and " + ab.first + " -> " + ab.second);
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> s = cp(ab, gd);
			ret.addAll(s);
			System.out.println("they are " + Util.sep(s, "\n"));

			System.out.println("now the opposite");
			s = cp(gd, ab);
			System.out.println("they are " + Util.sep(s, "\n"));
			ret.addAll(s);

		}
		// System.out.println();
		return ret;
	}

	// rules in R are gd
	private Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Pair<KBExp<C, V>, KBExp<C, V>> gd0,
			Pair<KBExp<C, V>, KBExp<C, V>> ab0) {
		// Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> gdm = freshenMap(gd0);
		Pair<Map<V, KBExp<C, V>>, Map<V, KBExp<C, V>>> abm = freshenMap(ab0);

		Pair<KBExp<C, V>, KBExp<C, V>> gd = gd0; // new
													// Pair<>(gd0.first.subst(gdm.first),
													// gd0.second.subst(gdm.first));
		Pair<KBExp<C, V>, KBExp<C, V>> ab = new Pair<>(ab0.first.subst(abm.first),
				ab0.second.subst(abm.first));

		Map<V, KBExp<C, V>> inv = new HashMap<>();
		// inv.putAll(gdm.second);
		Set<V> vars = new HashSet<>();
		vars.addAll(gd.first.vars());
		vars.addAll(gd.second.vars());
		for (V k : abm.second.keySet()) {
			KBExp<C, V> v = abm.second.get(k);
			if (!Collections.disjoint(vars, v.vars())) {
				inv.put(k, v);
			}
		}
		// inv.putAll(abm.second); //TODO: creates conficts? do not need to
		// freshen on of the rules

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> retX = gd.first.cp(inv, new LinkedList<>(), ab.first,
				ab.second, gd.first, gd.second, true);

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
		for (Pair<KBExp<C, V>, KBExp<C, V>> c : retX) {
			ret.add(new Pair<>(c.first.subst(inv), c.second.subst(inv)));
		}

		return ret;
	}

	public static <C,V> KBExp<C, V> red(Iterator<V> fresh, Set<Pair<KBExp<C,V>, KBExp<C,V>>> R, KBExp<C, V> e) {
		int i = 0;
		KBExp<C, V> orig = e;
		for (;;) {
			i++;
			if (i > 16) {
				throw new RuntimeException("Init " + orig + " goes to " + e + " under " + R);
			}
			KBExp<C, V> e0 = step(fresh, R, e);
			if (e.equals(e0)) {
				return e;
			}
			e = e0;
		}
	}

	// TODO: reduction is wrong
	List<String> log;

	private static <C,V> KBExp<C, V> step1(Iterator<V> fresh, Set<Pair<KBExp<C,V>, KBExp<C,V>>> R, KBExp<C, V> e) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r0 : R) {
			Pair<KBExp<C, V>, KBExp<C, V>> r = freshen(fresh, r0);
			KBExp<C, V> lhs = r.first;
			KBExp<C, V> rhs = r.second;
			if (!Collections.disjoint(e.vars(), lhs.vars())) {
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
		return e;
	}

	private static <C,V> KBExp<C, V> step(Iterator<V> fresh, Set<Pair<KBExp<C,V>, KBExp<C,V>>> R, KBExp<C, V> e) {
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

}
