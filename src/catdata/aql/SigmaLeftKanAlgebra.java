package catdata.aql;

import java.util.Collection;
import java.util.Collections;
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

import catdata.BinRelSet;
import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.fdm.InitialAlgebra;

//TODO analyze || behavior - no apparent difference
public class SigmaLeftKanAlgebra<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2, Gen, Sk, X, Y>
		extends Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>>
		implements DP<Ty, En2, Sym, Fk2, Att2, Gen, Sk> {

	private static class Path<Ty, En, Sym, Fk, Att> {
		public final En src;
		public final En dst;
		public final List<Fk> edges;
		// public final Schema<Ty,En,Sym,Fk,Att> sch;

		public Path(Schema<Ty, En, Sym, Fk, Att> sch, En src, List<Fk> edges) {
			this.src = src;
			this.edges = edges;
			// this.sch = sch;
			if (edges.isEmpty()) {
				dst = src;
			} else {
				dst = sch.fks.get(edges.get(edges.size() - 1)).second;
			}
		}

		public Path(Schema<Ty, En, Sym, Fk, Att> sch, Term<Ty, En, Sym, Fk, Att, Void, Void> t, En src) {
			this(sch, src, t.toFkList());
		}

	}

	private final Schema<Ty, En1, Sym, Fk1, Att1> A;
	private final Schema<Ty, En2, Sym, Fk2, Att2> B;
	public final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> X;
	public int fresh;

	private boolean gamma() {
		boolean ret = false;

		while (true) {
			Pair<En2, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> k = gamma0();
			if (k == null) {
				return ret;
			}
			ret = true;
			gamma1(k.first, k.second);
		}
	}

	private static <Ty,En2,Sym,Fk2,Att2,Gen,Sk> void filter(BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> set,
			Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> d) {
		set.removeIf(p -> p.first.equals(d) || p.second.equals(d));
	}

	private void gamma1(En2 b1, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> xy) {
		if (xy.first.equals(xy.second)) {
			Sb.get(b1).remove(xy.first, xy.second);
			return;
		}
		Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x, y;
		if (xy.first.i > xy.second.i) {
			x = xy.second;
			y = xy.first;
		} else {
			x = xy.first;
			y = xy.second;
		}

		Pb.get(b1).remove(y);

		replace(x, y);

		BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> set0 = new BinRelSet<>(new HashSet<>(Sb.get(b1).R));
		// BinRelSet<Lineage<En2,Fk2,Gen>, Lineage<En2,Fk2,Gen>> set0 = new
		// BinRelSet<>(Sb.get(b1));

		for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> k : Sb.get(b1)) {
			if (k.first.equals(y)) {
				set0.add(x, k.second);
			}
			if (k.second.equals(y)) {
				set0.add(k.first, x);
			}
		}
		filter(set0, y);
		Sb.map.put(b1, set0);

		for (Fk2 g : Pg.keySet()) {
			BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> set = Pg.get(g);
			BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> a = new BinRelSet<>(new HashSet<>());
			En2 gs = schema().fks.get(g).first;
			En2 gt = schema().fks.get(g).second;

			if (gs.equals(b1) && gt.equals(b1)) {
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> k : set) {
					if (k.first.equals(y) && k.second.equals(y)) {
						a.add(x, x);
					}
					if (k.first.equals(y) && !k.second.equals(y)) {
						a.add(x, k.second);
					}
					if (k.second.equals(y) && !k.first.equals(y)) {
						a.add(k.first, x);
					}
				}
			} else if (gs.equals(b1)) {
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> k : set) {
					if (k.first.equals(y) && !k.second.equals(y)) {
						a.add(x, k.second);
					}
				}
			} else if (gt.equals(b1)) {
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> k : set) {
					if (k.second.equals(y) && !k.first.equals(y)) {
						a.add(k.first, x);
					}
				}
			}
			set.addAll(a);
			filter(set, y);
		}
	}

	private void replace(Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y) {

		for (List<Pair<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> a : ua.map.values()) {
			for (Pair<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> s : a) {
				if (s.second.equals(y)) {
					s.second = x;
				}
			}
		}
	}

	private Pair<En2, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> gamma0() {
		for (Entry<En2, BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> c : Sb.entrySet()) {
			if (c.getValue().isEmpty()) {
				continue;
			}
			for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> p0 : c.getValue()) {
				return new Pair<>(c.getKey(), p0);
			}
		}

		return null;
	}

	private boolean bgd() {
		return gamma() || delta() || beta1() || beta2();
	}

	private boolean step() {
		boolean ret = false;
		while (bgd()) {
			ret = true;
		}
		return ret || alpha(); // || alphaT();
	}

	// true = success
	public boolean compute() {
		for (int i = 0; i < max; i++) {
			// System.out.println(i + "AQL: " + toString());
			if (!step()) {
				return true;
			}
		}
		return false;
	}

	// beta, delta, gamma

	private boolean beta2() {
		boolean ret = false;
		for (Fk1 e : A.fks.keySet()) {
			Path<Ty, En2, Sym, Fk2, Att2> g = new Path<>(F.dst, F.fks.get(e).first, F.fks.get(e).second);
			BinRelSet<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> lhs = new BinRelSet<>(X.algebra().fkAsSet(e))
					.compose(new BinRelSet<>(new HashSet<>(ua.get(A.fks.get(e).second))));
			BinRelSet<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> rhs = new BinRelSet<>(new HashSet<>(ua.get(A.fks.get(e).first)))
					.compose(eval2(g));
			// System.out.println(lhs);
			// System.out.println(rhs);

			En2 n = g.dst;
			ret = ret || addCoincidences(lhs, rhs, n);
		}
		return ret;
	}
	/*
	private boolean beta2T() {
		boolean ret = false;
		for (Att1 e : A.atts.keySet()) {
			Path<Ty, En2, Sym, Fk2, Att2> g 
			= new Path<>(F.dst, F.atts.get(e).first, F.atts.get(e).second);
			BinRelSet<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> lhs 
			= new BinRelSet<>(X.algebra().attAsSet(e))
					.compose(new BinRelSet<>(new HashSet<>(tua.get(A.atts.get(e).second))));
			BinRelSet<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> rhs 
			= new BinRelSet<>(new HashSet<>(
					tua.get(A.atts.get(e).first)))
					.compose(eval2(g));
			// System.out.println(lhs);
			// System.out.println(rhs);

			En2 n = g.dst;
			ret = ret || addCoincidences(lhs, rhs, n);
		}
		return ret;
	}*/

	private boolean beta1() {
		boolean ret = false;
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : B.eqs) {
			// for (Lineage<En2,Fk2,Gen> x : Pb.get(eq.first.second)) {
			// Set<Lineage<En2,Fk2,Gen>> lhs = eval(new Path<>(B, eq.second,
			// eq.first.second), x);
			// Set<Lineage<En2,Fk2,Gen>> rhs = eval(new Path<>(B, eq.third,
			// eq.first.second), x);
			Chc<Ty, En2> nn = schema().type(eq.first, eq.second);
			if (nn.left) {
				//defer to consistency check
				continue;
			}

			BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> lhs = eval2(
					new Path<>(B, eq.second, eq.first.second));
			BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> rhs = eval2(
					new Path<>(B, eq.third, eq.first.second));
			
			 
			En2 n = nn.r;
			ret = ret || addCoincidences(lhs, rhs, n);
			// }
		}
		return ret;
	}
	/*
	 * private boolean addCoincidences2( Set<Lineage<En2,Fk2,Gen>> lhs,
	 * Set<Lineage<En2,Fk2,Gen>> rhs, En2 n) { boolean ret = false; for
	 * (Lineage<En2,Fk2,Gen> l : lhs) { for (Lineage<En2,Fk2,Gen> r : rhs) { if
	 * (l.equals(r)) { continue; } ret = Sb.get(n).add(l, r) || ret; ret =
	 * Sb.get(n).add(r, l) || ret; } } return ret; }
	 */

	private <Z> boolean addCoincidences(BinRelSet<Z, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> lhs,
			BinRelSet<Z, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> rhs, En2 n) {
		boolean ret = false;
		for (Pair<Z, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> l : lhs) {
			for (Pair<Z, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> r : rhs) {
				if (!l.first.equals(r.first)) {
					continue;
				}
				if (l.second.equals(r.second)) {
					continue;
				}
				ret = Sb.get(n).add(l.second, r.second) || ret;
				ret = Sb.get(n).add(r.second, l.second) || ret;
			}
		}
		return ret;
	}

	public BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> eval2(Path<Ty, En2, Sym, Fk2, Att2> p) {
		BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> ret = new BinRelSet<>(Util.refl(Pb.get(p.src)));
		for (Fk2 e : p.edges) {
			ret = ret.compose(Pg.get(e));
		}
		return ret;
	}
	/*
	 * public Set<Lineage<En2,Fk2,Gen>> eval(Path<Ty,En2,Sym,Fk2,Att2> p,
	 * Lineage<En2,Fk2,Gen> x) { if (p.edges.isEmpty()) { return
	 * Util.singSet(x); } Fk2 fk2 = p.edges.get(0); Set<Lineage<En2,Fk2,Gen>>
	 * ret = new HashSet<>(); Path<Ty,En2,Sym,Fk2,Att2> q = new Path<>(B,
	 * B.fks.get(fk2).second, p.edges.subList(1, p.edges.size()));
	 * Set<Lineage<En2,Fk2,Gen>> ys = Pg.get(fk2).R.get(x); if (ys != null) {
	 * for (Lineage<En2,Fk2,Gen> y : ys) { ret.addAll(eval(q, y)); } } return
	 * ret; }
	 */

	// private final It fr = new It();
	private int fresh() {
		return fresh++;
	}

	private Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Fk2> smallest() {
		Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Fk2> ret = null;
		for (Fk2 g : Pg.keySet()) {
			BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> pg = Pg.get(g);
			outer: for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : Pb.get(schema().fks.get(g).first)) {
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> p : pg) {
					if (p.first.equals(x)) {
						continue outer;
					}
				}
				if (ret == null || x.i < ret.first.i) {
					ret = new Pair<>(x, g);
				}
			}
		}
		return ret;
	}
	/*
	private Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2> smallestT() {
		Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2> ret = null;
		for (Att2 g : tPg.keySet()) {
			BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> pg = tPg.get(g);
			outer: for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : Pb.get(schema().atts.get(g).first)) {
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> p : pg) {
					if (p.first.equals(x)) {
						continue outer;
					}
				}
				if (ret == null || x.i < ret.first.i) {
					ret = new Pair<>(x, g);
				}
			}
		}
		return ret;
	}*/

	private boolean alpha() {
		Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Fk2> p = smallest();
		if (p == null) {
			return false;
		}
		Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x = p.first;

		Fk2 g = p.second;
		En2 b2 = schema().fks.get(g).second;
		Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y = new Lineage<>(fresh(), Term.Fk(p.second, x.t));

		Pb.get(b2).add(y);
		Pg.get(g).add(x, y);

		return true;
	}
	/*
	private boolean alphaT() {
		Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2> p = smallestT();
		if (p == null) {
			return false;
		}
		Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x = p.first;

		Att2 g = p.second;
		Ty b2 = schema().atts.get(g).second;
		Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y = new Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>
		(fresh(), Term.Att(p.second, x.t));

		tPb.get(b2).add(y);
		tPg.get(g).add(x, y);

		return true;
	}*/

	private boolean delta() {
		boolean ret = false;
		for (Fk2 g : B.fks.keySet()) {
			for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : Pb.get(B.fks.get(g).first)) {
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y = null;
				Iterator<Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> it = Pg.get(g).iterator();
				while (it.hasNext()) {
					Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> z = it.next();
					if (!x.equals(z.first)) {
						continue;
					}
					if (y == null) {
						y = z.second;
						continue;
					}
					ret = true;
					it.remove();
					Sb.get(B.fks.get(g).second).add(y, z.second);
					Sb.get(B.fks.get(g).second).add(z.second, y);
				}
			}
		}
		return ret;
	}

	private final int max;

	private final Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col;
	
	public SigmaLeftKanAlgebra(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f2,
			Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i2, Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col, int max) {
		A = f2.src;
		B = f2.dst;
		F = f2;
		X = i2;
		this.fresh = 0;
		this.max = max;
		this.col = col;
		
		if (!X.algebra().hasFreeTypeAlgebra()) {
			throw new RuntimeException("Chase cannot be used: type algebra is not free");
		}

		Set<X> rank = new HashSet<>();

		for (En1 n : A.ens) {
			List<Pair<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> j = new LinkedList<>();
			Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> i = Pb.get(F.ens.get(n));
			for (X v : X.algebra().en(n)) {
				if (rank.contains(v)) {
					throw new RuntimeException("Contains non-unique " + v);
				}
				Term<Ty, En2, Sym, Fk2, Att2, Gen, Void> tt = F.trans(X.algebra().repr(v).map(Util.voidFn(),
						Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Function.identity()));

				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> ii = new Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>(fresh(), tt.mapGenSk(Function.identity(), Util.voidFn()));
				rank.add(v);
				j.add(new Pair<>(v, ii));
				i.add(ii);
			}
			ua.put(n, j);
		}
		/* for (Ty n : A.typeSide.tys) {
			List<Pair<Y, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> j = new LinkedList<>();
			Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> i = tPb.get(n);
			for (Y v : X.algebra().talg().sks.keySet()) {
				Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> tt = F.trans(X.algebra().reprT(Term.Sk(v))); //
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> ii = new Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>(fresh(), tt);
				j.add(new Pair<>(v, ii));
				i.add(ii);
			}
			tua.put(n, j);
		} */


		if (!compute()) {
			throw new RuntimeException("Fixed point not reached after " + max + " generations.  ");
		}

		for (Fk2 fk : Pg.keySet()) {
			fkMap.put(fk, Util.toMapSafely(Pg.get(fk).R));
		}
		
		//System.out.println(toString());
	}

	@Override
	public String toString() {
		return "LeftKan [Pb=" + Pb + ", Pg=" + Pg + ", ua=" + ua + ", Sb=" + Sb + "]";
	}
/*
	public final Ctx<Ty, Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> tPb = new Ctx<>();
	public final Ctx<Att2, BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> tPg = new Ctx<>();
	public final Ctx<Ty, List<Pair<Y, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> tua = new Ctx<>();
	public final Ctx<En2, BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> tSb = new Ctx<>();
*/
	public final Ctx<En2, Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> Pb = new Ctx<>();
	public final Ctx<Fk2, BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> Pg = new Ctx<>();
	public final Ctx<En1, List<Pair<X, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> ua = new Ctx<>();
	private final Ctx<En2, BinRelSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> Sb = new Ctx<>();

	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En2>> ctx, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> lhs,
			Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> rhs) {
		if (!lhs.hasTypeType()) {
			return this.intoX(lhs).equals(intoX(rhs));
		} else {
			return Util.anomaly();
			// return this.intoY(lhs).equals(intoY(rhs));
		}
	}

	@Override
	public Schema<Ty, En2, Sym, Fk2, Att2> schema() {
		return B;
	}

	@Override
	public Collection<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> en(En2 en) {
		return Pb.get(en);
	}

	@Override
	public Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> gen(Gen x) {
		return Util.lookup(ua.get(X.gens().get(x)), X.algebra().gen(x));
	}

	Map<Fk2, Map<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> fkMap = new HashMap<>();

	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> reprT0(Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>> y) {
        return schema().typeSide.js.java_tys.isEmpty() ? simpl(Term.Sk(y)) : schema().typeSide.js.reduce(simpl(Term.Sk(y)));
	} 
	
	@Override
	public synchronized Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> fk(Fk2 fk, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		// return Util.get0(Pg.get(fk).R.get(x));

		return fkMap.get(fk).get(x);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> att(Att2 att, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		return reprT0(Chc.inRight(new Pair<>(x, att)));
	//	return Util.anomaly();
		// return Util.toMapSafely(theContent.atts.get(att)).get(x);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> sk(Sk sk) {
		return reprT0(Chc.inLeft(sk));
	}

	
	
	@Override
	public Term<Void, En2, Void, Fk2, Void, Gen, Void> repr(Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		return x.t.convert();
	}

	

	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> talg;
	private final List<Pair<Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>, 
	Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>>>> 
	list = new LinkedList<>();
	
	
	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> talg() {
		//this is not simplfy from collage - this is how we get 'reduction' to happen, by processing the talg.
			if (talg != null) {
				return talg;
			}
			talg = InitialAlgebra.talg(list, this, col);
			return talg;
	}

	@Override
	public String toStringProver() {
		return "";
	}

	@Override
	public String printX(Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		// TODO Auto-generated method stub
		return x.toString();
	}

	@Override
	public Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> reprT_protected(
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> y) {
	      return schema().typeSide.js.java_tys.isEmpty() ? unflatten(simpl(y)) : unflatten(schema().typeSide.js.reduce(simpl(y)));
	  	
	}
	
	

	@Override
	public String printY(Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>> y) {
		// TODO Auto-generated method stub
		return y.toString();
	}
	
	//TODO: aql merge with initial algebra
	private Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> unflatten(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(this::unflatten).collect(Collectors.toList()));
		} else if (term.sk != null) {
            return term.sk.left ? Term.Sk(term.sk.l) : Term.Att(term.sk.r.second, repr(term.sk.r.first).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		} 
		throw new RuntimeException("Anomaly: please report");
	}
	

	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> simpl(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>> y) {
		 //apparently trans can be called before talg()
		for (Pair<Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Att2>>>> t : list) {
			y = y.replaceHead(new Head<>(Term.Sk(t.first)), Collections.emptyList(), t.second);
		}
		return y;
	}
	
}
