package catdata.aql;

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
import catdata.Triple;
import catdata.Util;
import catdata.graph.UnionFind;

public class Chase<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2, Gen, Sk, X, Y> {

	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	// private final
	// Schema<Ty,Chc<En1,En2>,Sym,Chc<Chc<Fk1,Fk2>,En1>,Chc<Att1,Att2>> S;

	public class Content {
		public final Ctx<En2, BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> ens;
		public final Ctx<Fk2, BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> fks;

		public final Ctx<En1, Ctx<X, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> us;
		// public Ctx<En,Ctx<Z,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> iso;

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
				Ctx<X, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> ctx = new Ctx<>();
				us.put(en1, ctx);
			}
		}

		@Override
		public String toString() {
			return "Content [ens=" + ens + ", fks=" + fks + ", us=" + us + "]";
		}

		public void addAll(Content c) {
			for (En2 en : ens.keySet()) {
				BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> x = c.ens
						.get(en);
				ens.get(en).addAll(x);
			}
			for (Fk2 fk : fks.keySet()) {
				BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> x = c.fks
						.get(fk);
				fks.get(fk).addAll(x);
			}
			for (En1 en1 : us.keySet()) {
				us.get(en1).putAll(c.us.get(en1).map);
			}
			// iso.putAll(c.iso.map);
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
				Ctx<X, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> ctx = new Ctx<>();
				for (X x : I.algebra().en(en1)) {
					Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> t = F.trans(I.algebra().repr(x).map(Util.voidFn(),
							Util.voidFn(), q -> q, Util.voidFn(), q -> q, Util.voidFn()));
					ctx.put(x, new Lineage<>(fresh.next(), t));
				}
				us.put(en1, ctx);
			}

			// this.ens = ens.map(x->new BinRelMap<>(x));
			// this.fks = fks.map(x->new BinRelMap<>(x));
			// this.iso = iso;
		}

		public Content merge(Ctx<En2, UnionFind<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> ufs) {
			Content ret = new Content();
			for (En2 en : F.dst.ens) {
				for (Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> x : ens
						.get(en)) {
					if (!x.first.equals(x.second)) {
						Util.anomaly();
					}
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n = ufs.get(en).find(x.first);
					ret.ens.get(en).add(n, n);
				}
			}
			// System.out.println("yyy " + ret);
			for (Fk2 fk : F.dst.fks.keySet()) {
				En2 a = F.dst.fks.get(fk).first;
				En2 b = F.dst.fks.get(fk).second;
				for (Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> x : fks
						.get(fk)) {
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n1 = ufs.get(a).find(x.first);
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n2 = ufs.get(b).find(x.second);
					ret.fks.get(fk).add(n1, n2);
					// System.out.println("on " + fk + " doing " + x + " is " + n1 + "," + n2);
				}
			}
			// System.out.println("xxx " + ret);
			for (En1 en : us.keySet()) {
				for (X x : us.get(en).keySet()) {
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n = us.get(en).get(x);
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

	public Chase(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I,
			int max) {
		this.F = F;
		this.I = I;
		T = new Content(I);
		// System.out.println(T.sizes());
		for (int i = 0; i < max; i++) {
			boolean changed = step();
			if (!changed) {
				return;
			}
			// System.out.println(T.sizes());
		}

		throw new RuntimeException("No convergence after " + max + " rounds.");
	}

	private boolean step() {
		Content toAdd = new Content();
		Ctx<En2, UnionFind<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> ufs = new Ctx<>();
		for (En2 en : F.dst.ens) {
			ufs.put(en, new UnionFind<>(new HashSet<>()));
		}
		boolean changed = false;

		// a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_v = T.ens
					.get(v);
			for (Fk2 a : F.dst.fksFrom(v)) {

				// T_v(x) -> Ey. T_a(x,y)
				BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_a = T.fks
						.get(a);
				for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> x : T_v.keySet()) {
					if (T_a.get(x) == null || T_a.get(x).isEmpty()) { // latter should be impossible
						toAdd.fks.get(a).add(x, new Lineage<>(fresh.next(), Term.Fk(a, x.t)));
						changed = true;
					}
				}
			}
		}
		// System.out.println("A " + changed);
		// a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_v = T.ens
					.get(v);
			for (Fk2 a : F.dst.fksFrom(v)) {

				// T_v(x) -> Ey. T_a(x,y)
				BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_a = T.fks
						.get(a);

				// T_a(x,y) -> T_v(x) /\ T_w(y)
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_w = T.ens
						.get(w);
				for (Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> xy : T_a) {
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> x = xy.first;
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> y = xy.second;
					if (!T_v.containsKey(x)) {
						// System.out.println("B " + changed);
						changed = changed | toAdd.ens.get(v).add(x, x);
					}
					if (!T_w.containsKey(y)) {
						// System.out.println("B2 " + y + " and a=" + a + " and v=" + v + " and w=" + w
						// + " and x=" + x + " tw = " + T_w);
						changed = changed | toAdd.ens.get(w).add(y, y);
					}
				}
			}

		}

		// a : a -> F(a)
		for (En1 a : F.src.ens) {
			// T_v(x) -> Ey. T_a(x,y)
			// this is the 'loading' step in the Content constructor

			// T_a(x,y) -> T_v(x) /\ T_w(y)
			En2 w = F.ens.get(a);
			Ctx<X, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_a = T.us.get(a);
			BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_w = T.ens
					.get(w);
			for (X xy : T_a.keySet()) {
				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> x = T_a.get(xy);
				if (!T_w.containsKey(x)) {
					changed = changed | toAdd.ens.get(w).add(x, x);
				}
			}
		}

		// System.out.println("C " + changed);
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : F.dst.eqs) {
			Chc<Ty, En2> t = F.dst.type(eq.first, eq.second);
			if (t.left) {
				continue;
			}
			En2 dst = t.r;
			En2 src = eq.first.second;
			List<Fk2> lhs = eq.second.toFkList();
			List<Fk2> rhs = eq.third.toFkList();

			Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> active = new HashSet<>();
			for (Pair<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> x : T.ens
					.get(src)) {
				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> initial = x.first;
				Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> N = Util.singSet(x.first);
				for (Fk2 fk : lhs) {
					Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> M = new HashSet<>();
					for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n : N) {
						Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> s = T.fks.get(fk).get(n);
						if (s == null) {
							s = new LinkedList<>();
						}
						M.addAll(s);
					}
					N = M;
				}

				Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> N2 = Util.singSet(x.first);
				for (Fk2 fk : rhs) {
					Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> M = new HashSet<>();
					for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n : N2) {
						Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> s = T.fks.get(fk).get(n);
						if (s == null) { // latter should be impossible
							s = new LinkedList<>();
						}
						M.addAll(s);
					}
					N2 = M;
				}

				if (!N.equals(N2)) {
					// System.out.println(N + " and " + N2 + " for initial " + initial);
					active.add(initial);
					changed = true;
				}
			}
			// System.out.println("D active " + active.size() + " and actual " +
			// T.ens.get(src).size());
			for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> x : active) {
				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n = x;
				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> m = x;
				for (Fk2 fk : lhs) {
					m = new Lineage<>(fresh.next(), Term.Fk(fk, n.t));
					toAdd.fks.get(fk).add(n, m);
					n = m;
				}

				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n2 = x;
				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> m2 = x;
				for (Fk2 fk2 : rhs) {
					m2 = new Lineage<>(fresh.next(), Term.Fk(fk2, n2.t));
					toAdd.fks.get(fk2).add(n2, m2);
					n2 = m2;
				}
				// System.out.println("equating " + m + " = " + m2 + " at " + dst);
				ufs.get(dst).union(m, m2);
			}

		}

	
		for (Fk1 a : F.src.fks.keySet()) {
			En1 v = F.src.fks.get(a).first;
			En1 w = F.src.fks.get(a).second;
			// a.m_w = m_v.F(a)

		
			for (X x : I.algebra().en(v)) {
				X a0 = I.algebra().fk(a, x);
				Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> lhs = T.us.get(w).get(a0);

//				for (X y : T.us.get(v).keySet()) {
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> initial = T.us.get(v).get(x);
					Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> N = Util.singSet(initial);
					for (Fk2 fk : F.fks.get(a).second) {
						Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> M = new HashSet<>();
						for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n : N) {
							Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> s = T.fks.get(fk).get(n);
							if (s == null) {
								s = new LinkedList<>();
							}
							M.addAll(s);
						}
						N = M;
					}
					Set<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> active = new HashSet<>();

					if (!N.equals(Util.singSet(lhs))) {
				//		 System.out.println(N + " for initial " + initial + " was not " + lhs);
						active.add(initial);
					//	changed = true;
					}
			//	}
				// System.out.println("D active " + active.size() + " and actual " +
				//		 T.us.get(v).keySet().size());
				 
				for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> y : active) {
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n = y;
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> m = y;
					for (Fk2 fk : F.fks.get(a).second) {
						m = new Lineage<>(fresh.next(), Term.Fk(fk, n.t));
						toAdd.fks.get(fk).add(n, m);
						n = m;
					}

				//	System.out.println("equating " + m + " = " + lhs + " at " + F.ens.get(w));
					ufs.get(F.ens.get(w)).union(m, lhs);
				}
			}
		}
		T.addAll(toAdd);

		for (En2 v : F.dst.ens) {
			for (Fk2 a : F.dst.fksFrom(v)) {

				// T_a(x,y1) /\ T_b(x,y2) -> y2=y1;
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_a = T.fks
						.get(a);

				for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> x : T_a.keySet()) {
					Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> ys = T_a.get(x);
					// System.out.println("collection is " + ys);
					Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> y1 = Util.get0X(ys);
					for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> y2 : ys) {
						if (!y1.equals(y2)) {
							ufs.get(w).union(y1, y2);
							//System.out.println("equating " + y1 + " = " + y2 + " at " + w);

							changed = true;
						}
					}
				}
			}
		}
		// System.out.println("E " + changed);

		if (!changed) {
			return false;
		}

		T = T.merge(ufs);

		return true;
	}

}
