package catdata.aql;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import catdata.BinRelMap;
import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Ref;
import catdata.Triple;
import catdata.Util;
import catdata.graph.UnionFind;

public class Chase<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2, Gen, Sk, X, Y> {

	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	// private final
	// Schema<Ty,Chc<En1,En2>,Sym,Chc<Chc<Fk1,Fk2>,En1>,Chc<Att1,Att2>> S;

	public class Content {
		public final Ctx<En2, BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ens;
		public final Ctx<Fk2, BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> fks;

		public final Ctx<En1, Ctx<X, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> us;

		public final Ctx<Ty, BinRelMap<Lineage<Ty, Void, Sym, Void, Void, Void, Y>, Lineage<Ty, Void, Sym, Void, Void, Void, Y>>> tys;
		public final Ctx<Att2, BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Ty, Void, Sym, Void, Void, Void, Y>>> atts;

		public final Ctx<Ty, Ctx<Y, Lineage<Ty, Void, Sym, Void, Void, Void, Y>>> vs;

		public Content() {
			ens = new Ctx<>();
			fks = new Ctx<>();
			us = new Ctx<>();
			for (En2 en2 : F.dst.ens) {
				ens.put(en2, new BinRelMap<>());
			}
			for (Fk2 fk2 : F.dst.fks.keySet()) {
				fks.put(fk2, new BinRelMap<>());
			}
			for (En1 en1 : F.src.ens) {
				us.put(en1, new Ctx<>());
			}

			tys = new Ctx<>();
			atts = new Ctx<>();
			vs = new Ctx<>();
			for (Ty ty : F.dst.typeSide.tys) {
				tys.put(ty, new BinRelMap<>());
			}
			for (Att2 att : F.dst.atts.keySet()) {
				atts.put(att, new BinRelMap<>());
			}
			for (Ty ty : F.src.typeSide.tys) {
				vs.put(ty, new Ctx<>());
			}
		}

		public void addAll(Content c) {
			for (En2 en : ens.keySet()) {
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x = c.ens
						.get(en);
				ens.get(en).addAll(x);
			}
			for (Fk2 fk : fks.keySet()) {
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x = c.fks
						.get(fk);
				fks.get(fk).addAll(x);
			}
			for (En1 en1 : us.keySet()) {
				us.get(en1).putAll(c.us.get(en1).map);
			}

			for (Ty ty : F.src.typeSide.tys) {
				BinRelMap<Lineage<Ty, Void, Sym, Void, Void, Void, Y>, Lineage<Ty, Void, Sym, Void, Void, Void, Y>> x = c.tys
						.get(ty);
				tys.get(ty).addAll(x);
			}
			for (Att2 att : atts.keySet()) {
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Ty, Void, Sym, Void, Void, Void, Y>> x = c.atts
						.get(att);
				atts.get(att).addAll(x);
			}
			for (En1 en1 : us.keySet()) {
				us.get(en1).putAll(c.us.get(en1).map);
			}
		}

		public Content(Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
			this.ens = new Ctx<>();
			for (En2 en2 : F.dst.ens) {
				ens.put(en2, new BinRelMap<>());
			}
			this.fks = new Ctx<>();
			for (Fk2 fk2 : F.dst.fks.keySet()) {
				fks.put(fk2, new BinRelMap<>());
			}

			this.us = new Ctx<>();

			for (En1 en1 : I.schema().ens) {
				Ctx<X, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> ctx = new Ctx<>();
				for (X x : I.algebra().en(en1)) {
					Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> t = F.trans(I.algebra().repr(x).map(Util.voidFn(),
							Util.voidFn(), q -> q, Util.voidFn(), q -> q, Util.voidFn()));
					ctx.put(x, new Lineage<>(fresh.next(), t.convert()));
				}
				us.put(en1, ctx);
			}

			this.tys = new Ctx<>();
			for (Ty ty : F.dst.typeSide.tys) {
				tys.put(ty, new BinRelMap<>());
			}
			this.atts = new Ctx<>();
			for (Att2 att : F.dst.atts.keySet()) {
				atts.put(att, new BinRelMap<>());
			}

			this.vs = new Ctx<>();

			for (Ty ty : I.schema().typeSide.tys) {
				Ctx<Y, Lineage<Ty, Void, Sym, Void, Void, Void, Y>> ctx = new Ctx<>();
				for (Y y : I.algebra().talg().sks.keySet()) {
					ctx.put(y, new Lineage<>(fresh.next(), Term.Sk(y)));
				}
				vs.put(ty, ctx);
			}
		}

		// TODO aql update
		public Content merge(Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs) {
			Content ret = new Content();
			for (En2 en : F.dst.ens) {
				for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x : ens
						.get(en)) {
					if (!x.first.equals(x.second)) {
						Util.anomaly();
					}
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n = ufs.get(en).find(x.first);
					ret.ens.get(en).add(n, n);
				}
			}
			// System.out.println("yyy " + ret);
			for (Fk2 fk : F.dst.fks.keySet()) {
				En2 a = F.dst.fks.get(fk).first;
				En2 b = F.dst.fks.get(fk).second;
				for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x : fks
						.get(fk)) {
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n1 = ufs.get(a).find(x.first);
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n2 = ufs.get(b).find(x.second);
					ret.fks.get(fk).add(n1, n2);
					// System.out.println("on " + fk + " doing " + x + " is " + n1 + "," + n2);
				}
			}
			// System.out.println("xxx " + ret);
			for (En1 en : us.keySet()) {
				for (X x : us.get(en).keySet()) {
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n = us.get(en).get(x);
					ret.us.get(en).put(x, ufs.get(F.ens.get(en)).find(n));
				}
			}
			// System.out.println("merged " + ret);
			// ret.iso = iso.map((k,x)->new Pair<>(k,x.map(y->ufs.get(k).find(y))));
			return ret;
		}

		public String sizes() {
			String s = "";
			for (En2 en2 : ens.keySet()) {
				s += (en2 + ": " + ens.get(en2).size() + ", ");
			}
			for (Ty ty : tys.keySet()) {
				s += (ty + ": " + tys.get(ty).size() + ", ");
			}
			return s;
		}
	}

	public Content T;

	private Iterator<Integer> fresh = new Iterator<Integer>() {
		private int i = 0;

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public Integer next() {
			return i++;
		}
	};

	private Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;

	public Chase(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F,
			Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
		this.F = F;
		this.I = I;
		T = new Content(I);
		// System.out.println(T.sizes());
		for (;;) {
			boolean changed = step();
			if (!changed) {
				return;
			}
			// System.out.println(T.sizes());
		}

	}

	private boolean step() {
		Content toAdd = new Content();
		Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs = new Ctx<>();
		for (En2 en : F.dst.ens) {
			ufs.put(en, new UnionFind<>(new HashSet<>()));
		}
		Boolean[] changed = new Boolean[] { false }; 
		
		makeArrowsTotal(toAdd, changed);

		makeObjectsTotal(toAdd, changed);

		moveObjects(toAdd, changed);

		doEqs(toAdd, ufs, changed);

		T.addAll(toAdd);

		makeFunctional(ufs, changed);
		// System.out.println("E " + changed);

		if (!changed[0]) {
			return false;
		}

		T = T.merge(ufs);

		return true;
	}

	public void makeFunctional(Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs,
			Boolean[] changed) {
		for (En2 v : F.dst.ens) {
			for (Fk2 a : F.dst.fksFrom(v)) {

				// T_a(x,y1) /\ T_b(x,y2) -> y2=y1;
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_a = T.fks
						.get(a);

				for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x : T_a.keySet()) {
					Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> ys = T_a.get(x);
					// System.out.println("collection is " + ys);
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> y1 = Util.get0X(ys);
					for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> y2 : ys) {
						if (!y1.equals(y2)) {
							ufs.get(w).union(y1, y2);
							// System.out.println("equating " + y1 + " = " + y2 + " at " + w);

							changed[0] = true;
						}
					}
				}
			}
		}
	}

	public void doEqs(Content toAdd, Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs,
			Boolean[] changed) {

		targetEqs(toAdd, ufs, changed);

		collageEqs(toAdd, ufs, changed);
	}

	public void collageEqs(Content toAdd, Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs,
			Boolean[] ref) {
		for (Fk1 a : F.src.fks.keySet()) {
			En1 v = F.src.fks.get(a).first;
			En1 w = F.src.fks.get(a).second;
			// a.m_w = m_v.F(a)

			for (X x : I.algebra().en(v)) {
				X a0 = I.algebra().fk(a, x);
				Lineage<Void, En2, Void, Fk2, Void, Gen, Void> lhs = T.us.get(w).get(a0);

				Lineage<Void, En2, Void, Fk2, Void, Gen, Void> initial = T.us.get(v).get(x);
				Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N = Util.singSet(initial);
				for (Fk2 fk : F.fks.get(a).second) {
					Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> M = new HashSet<>();
					for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n : N) {
						Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> s = T.fks.get(fk).get(n);
						if (s == null) {
							s = new LinkedList<>();
						}
						M.addAll(s);
					}
					N = M;
				}

				if (!N.equals(Util.singSet(lhs))) {
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n = initial;
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m = initial;
					for (Fk2 fk : F.fks.get(a).second) {
						m = new Lineage<>(fresh.next(), Term.Fk(fk, n.t));
						toAdd.fks.get(fk).add(n, m);
						n = m;
					}

					ufs.get(F.ens.get(w)).union(m, lhs);
					ref[0] = true;
				}
			}
		}
	}

	public void targetEqs(Content toAdd, Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs,
			Boolean[] changed) {
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : F.dst.eqs) {
			Chc<Ty, En2> t = F.dst.type(eq.first, eq.second);
			if (t.left) {
				continue;
			}
			En2 dst = t.r;
			En2 src = eq.first.second;
			List<Fk2> lhs = eq.second.toFkList();
			List<Fk2> rhs = eq.third.toFkList();

			Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> active = new HashSet<>();
			for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x : T.ens
					.get(src)) {
				Lineage<Void, En2, Void, Fk2, Void, Gen, Void> initial = x.first;
				Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N = Util.singSet(x.first);
				for (Fk2 fk : lhs) {
					Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> M = new HashSet<>();
					for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n : N) {
						Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> s = T.fks.get(fk).get(n);
						if (s == null) {
							s = new LinkedList<>();
						}
						M.addAll(s);
					}
					N = M;
				}

				Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N2 = Util.singSet(x.first);
				for (Fk2 fk : rhs) {
					Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> M = new HashSet<>();
					for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n : N2) {
						Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> s = T.fks.get(fk).get(n);
						if (s == null) { // latter should be impossible
							s = new LinkedList<>();
						}
						M.addAll(s);
					}
					N2 = M;
				}

				if (!N.equals(N2)) {
					changed[0] = true;
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n = initial;
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m = initial;
					for (Fk2 fk : lhs) {
						m = new Lineage<>(fresh.next(), Term.Fk(fk, n.t));
						toAdd.fks.get(fk).add(n, m);
						n = m;
					}

					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n2 = initial;
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m2 = initial;
					for (Fk2 fk2 : rhs) {
						m2 = new Lineage<>(fresh.next(), Term.Fk(fk2, n2.t));
						toAdd.fks.get(fk2).add(n2, m2);
						n2 = m2;
					} // System.out.println("equating " + m + " = " + m2 + " at " + dst);
					ufs.get(dst).union(m, m2);
				}
			}
		}
	}

	public void moveObjects(Content toAdd, Boolean[] changed) {
		// a : a -> F(a)

		for (En1 a : F.src.ens) {
			// T_v(x) -> Ey. T_a(x,y)
			// this is the 'loading' step in the Content constructor

			// T_a(x,y) -> T_v(x) /\ T_w(y)
			En2 w = F.ens.get(a);
			Ctx<X, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_a = T.us.get(a);
			BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_w = T.ens
					.get(w);
			for (X xy : T_a.keySet()) {
				Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x = T_a.get(xy);
				if (!T_w.containsKey(x)) {
					changed[0] = changed[0] | toAdd.ens.get(w).add(x, x);
				}
			}
		}
	}

	public void makeObjectsTotal(Content toAdd, Boolean[] changed) {
		// a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_v = T.ens
					.get(v);
			for (Fk2 a : F.dst.fksFrom(v)) {
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_a = T.fks
						.get(a);

				// T_a(x,y) -> T_v(x) /\ T_w(y)
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_w = T.ens
						.get(w);
				for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> xy : T_a) {
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x = xy.first;
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> y = xy.second;
					if (!T_v.containsKey(x)) {
						changed[0] = changed[0] | toAdd.ens.get(v).add(x, x);
					}
					if (!T_w.containsKey(y)) {
						changed[0] = changed[0] | toAdd.ens.get(w).add(y, y);
					}
				}
			}

		}
	}

	public void makeArrowsTotal(Content toAdd, Boolean[] changed) {
		// a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_v = T.ens
					.get(v);
			for (Fk2 a : F.dst.fksFrom(v)) {

				// T_v(x) -> Ey. T_a(x,y)
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> T_a = T.fks
						.get(a);
				for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x : T_v.keySet()) {
					if (T_a.get(x) == null || T_a.get(x).isEmpty()) { // latter should be impossible
						toAdd.fks.get(a).add(x, new Lineage<>(fresh.next(), Term.Fk(a, x.t)));
						changed[0] = true;
					}
				}
			}

		}
	}

}
