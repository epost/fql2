package catdata.aql;

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
		//public  Ctx<En,Ctx<Z,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> iso;
		
		public Content() {
			ens = new Ctx<>();
			fks = new Ctx<>();
	//		iso = new Ctx<>();
		}
		
		public void addAll(Content c) {
			for (En2 en : ens.keySet()) {
				ens.get(en).addAll(c.ens.get(en));
			}
			for (Fk2 fk : fks.keySet()) {
				fks.get(fk).addAll(c.fks.get(fk));
			}
			//iso.putAll(c.iso.map);
		}
		
		public Content(Ctx<En2, Set<Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> ens, Ctx<Fk2, Set<Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> fks, Ctx<En2,Ctx<?,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> iso) {
			this.ens = ens.map(x->new BinRelMap<>(x));
			this.fks = fks.map(x->new BinRelMap<>(x));
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
			//ret.iso = iso.map((k,x)->new Pair<>(k,x.map(y->ufs.get(k).find(y))));
			return ret;
		}
	}
	
	public Content T;
	
	private int fresh;
	public Chase(Mapping<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F, Instance<Ty,En1,Sym,Fk1,Att1,Gen,Sk,X,Y> I, int max
			, /*Ctx<En,Ctx<Z,Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> iso, */ int fresh) {
		this.F = F;
		T = null; //new Content(ens, fks, null);
		this.fresh = fresh;
		
		for (int i = 0; i < max; i++) {
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
				for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : T_v.R.keySet()) {					
					if (!T_a.R.get(x).contains(x)) {
				//		toAdd.fks.get(a).add(x, fr.next());
						changed = true;
					}					
				}
				
				//T_a(x,y) -> T_v(x) /\ T_w(y)				
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_w = T.ens.get(w);
				for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> xy : T_a) {
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x = xy.first;
					Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> y = xy.second;
					if (!T_v.R.containsKey(x)) {
						changed = changed | toAdd.ens.get(v).add(x, x);
					}
					if (!T_w.R.containsKey(y)) {
						changed = changed | toAdd.ens.get(w).add(y, y);
					}
				}
			}
			
			//TODO must do for each En1
		}
		
		//TODO seperate function to compose them and check
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : F.dst.eqs) {
			Chc<Ty, En2> t = F.dst.type(eq.first, eq.second);
			if (t.left) {
				continue;
			}
			En2 dst = t.r;
			En2 src = eq.first.second;
			List<Fk2> lhs = eq.second.toFkList();
			List<Fk2> rhs = eq.third.toFkList();
			for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> x : T.ens.get(src)) {
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n = x.first;
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> m = x.first;
				/*for (Fk2 fk : lhs2) {
					m = fr.next();
					toAdd.fks.get(fk).add(n, m);
					n = m;
				}
				
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> n2 = x.first;
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> m2 = x.first;
				for (Fk2 fk2 : rhs) {
					m2 = fr.next();
					toAdd.fks.get(fk2).add(n2, m2);
					n2 = m2;
				}*/
				
//				ufs.get(dst).union(m, m2);
			}

		}
		
		//TODO must also do eqs for En1
				
		for (En2 v : F.dst.ens) {
			for (Fk2 a : F.dst.fksFrom(v)) {
				
				//T_a(x,y1) /\ T_b(x<Y>> T_a = T.fks.get(a);
				En2 w = F.dst.fks.get(a).second;
				BinRelMap<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> T_a= T.fks.get(a);
				
				for (Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x : T_a.R.keySet()) {	
					LinkedHashSet<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> ys = T_a.R.get(x);
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
		
		if (!changed) {
			return false;
		}
		
		T.addAll(toAdd);
		T = T.merge(ufs);
		
		return true;
	}

}
