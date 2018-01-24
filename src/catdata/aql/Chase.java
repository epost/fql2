package catdata.aql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

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
		public final Ctx<En2, BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ens;
		public final Ctx<Fk2, BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> fks;

		public final Ctx<En1, Ctx<X, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> us;

		public final Ctx<Ty, BinRelMap<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> tys;
		public final Ctx<Att2, BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> atts;

		public final Ctx<Ty, Ctx<Y, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> vs;

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
				BinRelMap<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> x = c.tys
						.get(ty);
				tys.get(ty).addAll(x);
			}
			for (Att2 att : atts.keySet()) {
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> x = c.atts
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
				Ctx<Y, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> ctx = new Ctx<>();
				for (Y y : I.algebra().talg().sks.keySet()) {
					ctx.put(y,Term.Sk(Chc.inLeft(y)));
				}
				vs.put(ty, ctx);
			}
		}

		public Content merge(Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs,
				Ctx<Ty, UnionFind<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> ufs2) {
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

			// partitions to their constants, if any
			Ctx<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y,Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> pars = new Ctx<>();
			for (Ty ty : F.dst.typeSide.tys) {
				for (Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> x : tys
						.get(ty)) {
				
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> par = ufs2.get(ty).find(x.first);
					if (pars.containsKey(par)) {
						Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> parConst = pars.get(par);
						if (mergeable(x.first, parConst)) {
							throw new RuntimeException("Collision: " + print(x.first) + " not mergeable with " + print(parConst)
									+ " in input instance.");
						}
					} else {
						pars.put(par, x.first);
					}
				}
			}

			BiFunction<Ty, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> nf = (
					t, zz) -> {
						Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> y = ufs2.get(t).find(zz);
						if (pars.containsKey(y)) {
							return pars.get(y);
						}
						return y;
			};

			for (Ty ty : F.dst.typeSide.tys) {
				for (Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> x : tys
						.get(ty)) {
					if (!x.first.equals(x.second)) {
						Util.anomaly();
					}
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> n = nf.apply(ty, x.first);
					ret.tys.get(ty).add(n, n);
				}
			}
			// System.out.println("yyy " + ret);
			for (Att2 att : F.dst.atts.keySet()) {
				En2 a = F.dst.atts.get(att).first;
				Ty b = F.dst.atts.get(att).second;
				for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> x : atts
						.get(att)) {
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n1 = ufs.get(a).find(x.first);
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> n2 = nf.apply(b, x.second);
					ret.atts.get(att).add(n1, n2);
					// System.out.println("on " + fk + " doing " + x + " is " + n1 + "," + n2);
				}
			}
			// System.out.println("xxx " + ret);
			for (Ty ty : vs.keySet()) {
				for (Y x : vs.get(ty).keySet()) {
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> n = vs.get(ty).get(x);
					ret.vs.get(ty).put(x, nf.apply(ty, n));
				}
			}
			// System.out.println("merged " + ret);
			// ret.iso = iso.map((k,x)->new Pair<>(k,x.map(y->ufs.get(k).find(y))));
			return ret;
		}

		private String print(Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> t) {
			Function<Chc<Y,Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>,String> f = x -> {
				if (x.left) {
					return I.algebra().reprT(Term.Sk(x.l)).toString();
				}
				return x.r.t.toString();
			};
			return t.mapGenSk(Function.identity(), f).toString();
		}

		private boolean mergeable(Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> t,
				Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> r) {
			if (t.sk != null && !t.sk.left) {
				return true;
			} else if (r.sk != null && !r.sk.left) {
				return true;
			} else if (t.obj != null && r.obj != null) {
				return t.obj.equals(r.obj);
			} else if (t.sym != null && r.sym != null && r.sym.equals(t.sym)) {
				Iterator<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> 
				a = t.args.iterator(), b = r.args.iterator();
				for (int i = 0; i < t.args.size(); i++) {
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> 
					c = a.next(), d = b.next();
					if (!mergeable(c, d)) {
						return false;
					}
				}
				return true;
			}
			return false;
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

		@Override
		public String toString() {
			return "Content [ens=" + ens + ", fks=" + fks + ", us=" + us + "]";
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
		Ctx<Ty, UnionFind<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> ufs2 = new Ctx<>();
		for (Ty ty : F.dst.typeSide.tys) {
			ufs2.put(ty, new UnionFind<>(new HashSet<>()));
		}
		Boolean[] changed = new Boolean[] { false };

		makeArrowsTotal(toAdd, changed);

		makeObjectsTotal(toAdd, changed);

		moveObjects(toAdd, changed);

		//todo
		doEqs(toAdd, ufs, changed);

		T.addAll(toAdd);

		makeFunctional(ufs, changed, ufs2);

		if (!changed[0]) {
			return false;
		}

		T = T.merge(ufs, ufs2);

		return true;
	}

	static int x = 0;

	public void makeFunctional(Ctx<En2, UnionFind<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ufs,
			Boolean[] changed, Ctx<Ty, UnionFind<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> ufs2) {
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
			for (Att2 a : F.dst.attsFrom(v)) {

				// T_a(x,y1) /\ T_b(x,y2) -> y2=y1;
				Ty w = F.dst.atts.get(a).second;
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>
				T_a = T.atts.get(a);

				for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x : T_a.keySet()) {
					Collection<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> ys = T_a.get(x);
					// System.out.println("collection is " + ys);
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> y1 = Util.get0X(ys);
					for (Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> y2 : ys) {
						if (!y1.equals(y2)) {
							ufs2.get(w).union(y1, y2);
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

				if (!N.contains(lhs)) {
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

			for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x : T.ens
					.get(src)) {
				Lineage<Void, En2, Void, Fk2, Void, Gen, Void> initial = x.first;
				Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N  = eval(lhs, Util.singSet(x.first));
				Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N2 = eval(rhs, Util.singSet(x.first));
				if (Util.isect(N, N2).isEmpty()) {
					changed[0] = true;
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m  = populate(toAdd, lhs, initial);
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m2 = populate(toAdd, rhs, initial);				
					ufs.get(dst).union(m, m2);
				}
			}
		}
	}
	
	public void targetEqsT(Content toAdd, Ctx<Ty, UnionFind<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> ufs,
			Boolean[] changed) {
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : F.dst.eqs) {
			Chc<Ty, En2> t = F.dst.type(eq.first, eq.second);
			if (!t.left) {
				continue;
			}
			Ty dst = t.l;
			En2 src = eq.first.second;
			
		
			for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> x : T.ens
					.get(src)) {
				Lineage<Void, En2, Void, Fk2, Void, Gen, Void> initial = x.first;
				Set<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> 
				N = evalT(eq.second, Util.singSet(x.first));

				Set<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> 
				N2 = evalT(eq.third, Util.singSet(x.first));

				if (Util.isect(N, N2).isEmpty()) {
					changed[0] = true;
					//TODO aql
			//		Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m = populate(toAdd, eq.second, initial);
			//		Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m2 = populate(toAdd, eq.third, initial);
			//		ufs.get(dst).union(m, m2);
				}
			} 
		}
	}

	public Lineage<Void, En2, Void, Fk2, Void, Gen, Void> populate(Content toAdd, List<Fk2> lhs,
			Lineage<Void, En2, Void, Fk2, Void, Gen, Void> initial) {
		Lineage<Void, En2, Void, Fk2, Void, Gen, Void> n = initial;
		Lineage<Void, En2, Void, Fk2, Void, Gen, Void> m = initial;
		for (Fk2 fk : lhs) {
			m = new Lineage<>(fresh.next(), Term.Fk(fk, n.t));
			toAdd.fks.get(fk).add(n, m);
			n = m;
		}
		return m;
	}
	
	public Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> eval(List<Fk2> lhs,
			Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N) {
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
		return N;
	}

	public Set<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> evalT(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> t,
			Set<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> N) {
		if (t.obj != null) {
			return null; //Util.singSet(Chc.inRight(Term.Obj(t.obj, t.ty)));
		} else if (t.sym != null) {
			List<Set<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> l = new LinkedList<>();
			for (Term<Ty, En2, Sym, Fk2, Att2, Void, Void> x : t.args) {
				//Set<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> r = eval(x, N);
				//l.add(r);
				//TODO aql
			}
			List<List<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>> z = Util.prod(l);
			for (List<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> y : z) {
				
			}
			
		}
		/*
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
		return N; */
		return null;
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
		
		for (Ty a : F.src.typeSide.tys) {
			// T_v(x) -> Ey. T_a(x,y)
			// this is the 'loading' step in the Content constructor

			// T_a(x,y) -> T_v(x) /\ T_w(y)
			Ty w = a;
			Ctx<Y, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> T_a = T.vs.get(a);
			BinRelMap<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> 
			T_w = T.tys.get(w);
			for (Y xy : T_a.keySet()) {
				Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> x = T_a.get(xy);
				if (!T_w.containsKey(x)) {
					changed[0] = changed[0] | toAdd.tys.get(w).add(x, x);
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
			
			for (Att2 a : F.dst.attsFrom(v)) {
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> 
				T_a = T.atts.get(a);

				// T_a(x,y) -> T_v(x) /\ T_w(y)
				Ty w = F.dst.atts.get(a).second;
				BinRelMap<Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>
				T_w = T.tys.get(w);
				for (Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> xy : T_a) {
					Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x = xy.first;
					Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> y = xy.second;
					if (!T_v.containsKey(x)) {
						changed[0] = changed[0] | toAdd.ens.get(v).add(x, x);
					}
					if (!T_w.containsKey(y)) {
						changed[0] = changed[0] | toAdd.tys.get(w).add(y, y);
					}
				}
			}
		}
	}

	public void makeArrowsTotal(Content toAdd, Boolean[] changed) {
		// a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> 
			T_v = T.ens.get(v);
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
		
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> 
			T_v = T.ens.get(v);
			for (Att2 a : F.dst.attsFrom(v)) {

				// T_v(x) -> Ey. T_a(x,y)
				BinRelMap<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Y, Lineage<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>>
				T_a = T.atts.get(a);
				for (Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x : T_v.keySet()) {
					if (T_a.get(x) == null || T_a.get(x).isEmpty()) { // latter should be impossible
						toAdd.atts.get(a).add(x, Term.Sk(Chc.inRight(new Lineage<>(fresh.next(),Term.Att(a, x.t.map(Util.voidFn(),Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()))))));
						changed[0] = true;
					}
				}
			}
		}
		
		
	}

}
