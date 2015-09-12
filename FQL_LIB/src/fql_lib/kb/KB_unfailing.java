package fql_lib.kb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import fql_lib.Pair;
import fql_lib.Util;

public class KB_unfailing<C, V> extends KB<C, V> {

	public KB_unfailing(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0,
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		super(E0, gt0, fresh);
	}

	@Override
	protected void handleUnorientable(Pair<KBExp<C, V>, KBExp<C, V>> st) {
		remove(E, st);
		add(E, st);
	}

	@Override
	protected KBExp<C, V> step1Es(List<Pair<KBExp<C, V>, KBExp<C, V>>> E, KBExp<C, V> e) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> r0 : E) {
			e = step1EsX(r0, e);
			e = step1EsX(new Pair<>(r0.second, r0.first), e);
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

	@Override
	protected Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps2(
			Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen,
			Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();

		Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0 = new HashSet<>(E);
		E0.add(ab);
		// Set<Pair<KBExp<C, V>, KBExp<C, V>>> E1 = E0.stream().map(x -> new
		// Pair<>(new Pair<>(x.first.second, x.first.first),
		// x.second)).collect(Collectors.toSet());

		// Set<Pair<KBExp<C, V>, KBExp<C, V>>> E1 = E0.stream().map(x -> new
		// Pair<>(x.second, x.first)).collect(Collectors.toSet());

		for (Pair<KBExp<C, V>, KBExp<C, V>> gd : E0) {
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
			Pair<KBExp<C, V>, KBExp<C, V>> dg = gd.reverse();
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

		}
		// ret.removeIf(x -> E.con)
		return ret;
	}

	@Override
	protected void simplify2() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void collapseBy2(Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		// TODO Auto-generated method stub
	}

	@Override
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
			// System.out.println(e + " unorientable ");
		}
		System.out.println("true on allunorientable");
		return true;
	}

	private boolean allCpsConfluent() {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : E) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set = filterSubsumed(reduce(filterSubsumed(allcps2(
					new HashSet<>(), e))));
			/*
			 * System.out.println("considering ");
			 * System.out.println(allcps2(new HashSet<>(), e));
			 * System.out.println("----------- ");
			 * System.out.println(filterSubsumedBySelf(allcps2(new HashSet<>(),
			 * e)));
			 */
			if (!allCpsConfluent(set)) {
				return false;
			}
		}
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : R) {
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set = filterSubsumed(reduce(filterSubsumed(allcps(new HashSet<>(), e))));
			if (!allCpsConfluent(set)) {
				return false;
			}
		}
		System.out.println("true on allcpsconfluent");
		return true;
	}

	private Collection<Pair<KBExp<C, V>, KBExp<C, V>>> reduce(
			List<Pair<KBExp<C, V>, KBExp<C, V>>> set) {
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

	private boolean allCpsConfluent(Collection<Pair<KBExp<C, V>, KBExp<C, V>>> set) {
		for (Pair<KBExp<C, V>, KBExp<C, V>> e : set) {
			KBExp<C, V> lhs = red(new HashMap<>(), E, R, e.first);
			KBExp<C, V> rhs = red(new HashMap<>(), E, R, e.second);
			if (!lhs.equals(rhs)) {
				// TODO - need to skolemize here and check ground confluence?
				System.out.println("not confluent on " + e + " goes to " + lhs + " = " + rhs);
				return false;
			}
		}
		return true;
	}

	@Override
	// TODO
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

}
