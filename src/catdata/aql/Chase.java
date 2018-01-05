package catdata.aql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import catdata.BinRelMap;
import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.graph.UnionFind;

public class Chase<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2,Gen,Sk,X,Y> {

	private final Mapping<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
	//private final Schema<Ty,Chc<En1,En2>,Sym,Chc<Chc<Fk1,Fk2>,En1>,Chc<Att1,Att2>> S;
	
	public class Content {
		public final Ctx<En2, BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> ens;
		public final Ctx<Fk2, BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> fks;
		
		public final Ctx<En1, Ctx<X,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> us;
		//public  Ctx<En,Ctx<Z,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> iso;
		
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
				Ctx<X,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> ctx = new Ctx<>();
				us.put(en1, ctx);
			}
		}
		
		@Override
		public String toString() {
			return "Content [ens=" + ens + ", fks=" + fks + ", us=" + us + "]";
		}

		public void addAll(Content c) {
			for (En2 en : ens.keySet()) {
				ens.get(en).addAll(c.ens.get(en));
			}
			for (Fk2 fk : fks.keySet()) {
				fks.get(fk).addAll(c.fks.get(fk));
			}
			for (En1 en1 : us.keySet()) {
				us.get(en1).putAll(c.us.get(en1).map);
			}
			//iso.putAll(c.iso.map);
		}
		
		public Content(Instance<Ty,En1,Sym,Fk1,Att1,Gen,Sk,X,Y> I) {
			this.ens = new Ctx<>();
			for (En2 en2 : F.dst.ens) {
				ens.put(en2, new BinRelMap<>());
			}
			this.fks = new Ctx<>();
			for (Fk2 fk2 : F.dst.fks.keySet()) {
				fks.put(fk2, new BinRelMap<>());
			}

			this.us  = new Ctx<>();
			
			for (En1 en1 : I.schema().ens) {
				Ctx<X,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> ctx = new Ctx<>();
				for (X x : I.algebra().en(en1)) {
					Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk> t = F.trans(I.algebra().repr(x).map(Util.voidFn(), Util.voidFn(), q->q, Util.voidFn(), q->q, Util.voidFn()));
					ctx.put(x, new Lineage<>(fresh.next(), t));
				}
				us.put(en1, ctx);
			}
			
			//	this.ens = ens.map(x->new BinRelMap<>(x));
		//	this.fks = fks.map(x->new BinRelMap<>(x));
		//	this.iso = iso;
		}
		
		public Content merge(Ctx<En2, UnionFind<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> ufs) {
			Content ret = new Content();
			for (En2 en : F.dst.ens) {
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> x : ens.get(en)) {
					if (!x.first.equals(x.second)) {
						Util.anomaly();
					}
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n = ufs.get(en).find(x.first);
					ret.ens.get(en).add(n, n);
				}
			}
			for (Fk2 fk : F.dst.fks.keySet()) {
				En2 a = F.dst.fks.get(fk).first;
				En2 b = F.dst.fks.get(fk).second;
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> x : fks.get(fk)) {
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n1 = ufs.get(a).find(x.first);
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n2 = ufs.get(b).find(x.second);
					ret.fks.get(fk).add(n1, n2);
				}
			}
			for (En1 en : us.keySet()) {
				for (X x : us.get(en).keySet()) {
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n = us.get(en).get(x);
					ret.us.get(en).put(x,ufs.get(F.ens.get(en)).find(n));
				}
			}
			//ret.iso = iso.map((k,x)->new Pair<>(k,x.map(y->ufs.get(k).find(y))));
			return ret;
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
	
	public Chase(Mapping<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F, Instance<Ty,En1,Sym,Fk1,Att1,Gen,Sk,X,Y> I, int max) {
		this.F = F;
		T = new Content(I);
		
		for (int i = 0; i < max; i++) {
			System.out.println(T);
			boolean changed = step();
			if (!changed) {
				return;
			}
		}
			
		throw new RuntimeException("No convergence after " + max + " rounds.");
	}

	private boolean step() {
		Content toAdd = new Content();
		Ctx<En2,UnionFind<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> ufs = new Ctx<>();
		for (En2 en : F.dst.ens) {
			ufs.put(en, new UnionFind<>(new HashSet<>()));
		}
		boolean changed = false;
		
		//a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_v = T.ens.get(v);
			for (Fk2 a : F.dst.fksFrom(v)) {
				
				//T_v(x) -> Ey. T_a(x,y)
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_a = T.fks.get(a);
				for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : T_v.keySet()) {					
					if (T_a.get(x) == null || T_a.get(x).isEmpty()) { //latter should be impossible
						toAdd.fks.get(a).add(x, new Lineage<>(fresh.next(), Term.Fk(a, x.t)));
						changed = true;
					}					
				}
			}
		}
		System.out.println("A " + changed);
		//a : v -> w
		for (En2 v : F.dst.ens) {
			BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_v = T.ens.get(v);
			for (Fk2 a : F.dst.fksFrom(v)) {
				
				//T_v(x) -> Ey. T_a(x,y)
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_a = T.fks.get(a);
		
				//T_a(x,y) -> T_v(x) /\ T_w(y)				
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_w = T.ens.get(w);
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> xy : T_a) {
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x = xy.first;
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y = xy.second;
					if (!T_v.containsKey(x)) {
						System.out.println("B " + changed);
						changed = changed | toAdd.ens.get(v).add(x, x);
					}
					if (!T_w.containsKey(y)) {
						System.out.println("B2 " + y + " and " + T_w);
						changed = changed | toAdd.ens.get(w).add(y, y);
					}
				}
			}
			
		}
			
			//a : a -> F(a)
			for (En1 a : F.src.ens) {
				//T_v(x) -> Ey. T_a(x,y)
				//this is the 'loading' step in the Content constructor
				
				//T_a(x,y) -> T_v(x) /\ T_w(y)				
				En2 w = F.ens.get(a);
				Ctx<X, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> T_a = T.us.get(a);
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_w = T.ens.get(w);
				for (X xy : T_a.keySet()) {
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x = T_a.get(xy);
					if (!T_w.containsKey(x)) {
						changed = changed | toAdd.ens.get(w).add(x, x);
					}
				}
			}
		
		System.out.println("C " + changed);
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : F.dst.eqs) {
			Chc<Ty, En2> t = F.dst.type(eq.first, eq.second);
			if (t.left) {
				continue;
			}
			En2 dst = t.r;
			En2 src = eq.first.second;
			List<Fk2> lhs = eq.second.toFkList();
			List<Fk2> rhs = eq.third.toFkList();

			Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> active = new HashSet<>();
			outer: for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> x : T.ens.get(src)) {
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> initial = x.first;
				Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> N = Util.singSet(x.first);
				for (Fk2 fk : lhs) {
					Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> M = new HashSet<>();
					for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n : N) {
						Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> s = T.fks.get(fk).get(n);
						if (s == null || s.isEmpty()) { //latter should be impossible
							active.add(initial);
							continue outer;
						}
						M.addAll(s);
					}
					N = M;
				}
				
				Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> N2 = Util.singSet(x.first);
				for (Fk2 fk : rhs) {
					Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> M = new HashSet<>();
					for (Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>> n : N2) {
						Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> s = T.fks.get(fk).get(n);
						if (s == null || s.isEmpty()) { //latter should be impossible
							active.add(initial);
							continue outer;
						}
						M.addAll(s);
					}
					N2 = M;
				}
				
				if (!N.equals(N2)) {
					active.add(initial);
					changed = true;
				}
			}
			System.out.println("D " + changed);
			for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> x : T.ens.get(src)) {
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n = x.first;
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> m = x.first;
				for (Fk2 fk : lhs) {
					toAdd.fks.get(fk).add(n, new Lineage<>(fresh.next(), Term.Fk(fk, n.t)));
					n = m;
				}
				
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n2 = x.first;
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> m2 = x.first;
				for (Fk2 fk2 : rhs) {
					toAdd.fks.get(fk2).add(n2, new Lineage<>(fresh.next(), Term.Fk(fk2, n2.t)));
					n2 = m2;
				}
				
				ufs.get(dst).union(m, m2);
			}

		}
		
		//TODO must also do eq / naturality condition for En1
				
		for (En2 v : F.dst.ens) {
			for (Fk2 a : F.dst.fksFrom(v)) {
				
				//T_a(x,y1) /\ T_b(x<Y>> T_a = T.fks.get(a);
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_a= T.fks.get(a);
				
				for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : T_a.keySet()) {	
					Collection<Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> ys = T_a.get(x);
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y1 = Util.get0X(ys);
					for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y2 : ys) {
						if (!y1.equals(y2)) {
							ufs.get(w).union(y1, y2);
							changed = true;
						} 
					}
				}
			}
		}
		System.out.println("E " + changed);
		/*
		for (Fk1 a : src.fks.keySet()) {
			En1 v = src.fks.get(a).first;
			En1 w = src.fks.get(a).second;
			//a.m_w = m_v.F(a)
			Var x = new Var("x");
			Term<Ty, Chc<En1, En2>, Sym, Chc<Chc<Fk1,Fk2>,En1>, Chc<Att1, Att2>, Void, Void> lhs
			= Term.Fk(Chc.inRight(w), Term.Fk(Chc.inLeft(Chc.inLeft(a)), Term.Var(x)));
			
			Term<Ty, Chc<En1, En2>, Sym, Chc<Chc<Fk1,Fk2>,En1>, Chc<Att1, Att2>, Void, Void> rhs
			= Term.Fks(Chc.inLeft(Chc.inRight(fks.get(a).second)), Term.Fk(Chc.inRight(v), Term.Var(x)));
			
			eqs2.add(new Triple<>(new Pair<>(x, Chc.inLeft(v)), lhs, rhs));
		}*/
		
		if (!changed) {
			return false;
		}
		
		T.addAll(toAdd);
		T = T.merge(ufs);
		
		return true;
	}

}
