package catdata.provers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import catdata.RuntimeInterruptedException;
import catdata.Triple;
import catdata.graph.UnionFind;
import catdata.provers.KBExp.KBApp;

//Congruence closure a la Nelson Oppen
//TODO aql better incremental computation for congruence closure
public class CongruenceProver<T, C, V> extends DPKB<T, C, V> {

	@Override
	public String toString() {
		return "CongruenceProver [uf=" + uf + ", pred=" + pred + "]";
	}

	private UnionFind<KBExp<C, V>> uf; //, uf2;

	// in paper this doesn't check label - appears to be typo (!)
	private static <C,V> boolean congruent(UnionFind<KBExp<C, V>> uf, KBExp<C, V> u, KBExp<C, V> v) {
		if (!u.getApp().f.equals(v.getApp().f)) { 
			return false;
		}
		for (int i = 0; i < u.getApp().args.size(); i++) {
			if (!uf.connected(u.getApp().args.get(i), v.getApp().args.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static <C,V >void merge1(Map<KBExp<C, V>, Set<KBExp<C, V>>> pred, UnionFind<KBExp<C, V>> uf, KBExp<C, V> u, KBExp<C, V> v)  {
		if (Thread.currentThread().isInterrupted()) {
			try {
				throw new InterruptedException();
			} catch (InterruptedException ex) {
				throw new RuntimeInterruptedException(ex);
			}
		}
		if (uf.connected(u, v)) {
			return;
		} 

		Set<KBExp<C, V>> pu = new HashSet<>(); 
		for (KBExp<C, V> exp : pred.keySet()) {
			if (uf.connected(u, exp)) {
				pu.addAll(pred.get(exp));
			}
		}
		
		Set<KBExp<C, V>> pv = new HashSet<>(); 
		for (KBExp<C, V> exp : pred.keySet()) {
			if (uf.connected(v, exp)) {
				pv.addAll(pred.get(exp));
			}
		}
		
		uf.union(u, v);
		
		for (KBExp<C, V> x : pu) {
			for (KBExp<C, V> y : pv) {
				if (!uf.connected(x, y) && congruent(uf, x, y)) {
					merge1(pred, uf, x, y);
				}
			}
		}
	}

	private final Map<KBExp<C, V>, Set<KBExp<C, V>>> pred; //, pred2;

	public CongruenceProver(KBTheory<T,C,V> th) {
		super(th.tys, th.syms, th.eqs);
		pred = new HashMap<>();
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
			if (!eq.first.isEmpty()) {
				throw new RuntimeException("Congruence method can not work with universal quantification");
			}
			eq.second.allSubExps(pred);
			eq.third.allSubExps(pred);
		}
		for (C c : th.syms.keySet()) {
			(new KBApp<C,V>(c, Collections.emptyList())).allSubExps(pred);
		}
		//pred2 = new HashMap<>();
		//for (KBExp<C, V> l : pred.keySet()) {
		//	pred2.put(l, new HashSet<>(pred.get(l)));
		//}
		doCong();
		//uf2 = new UnionFind<>(pred2.keySet());
		//for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
		//	merge1(pred2, uf2, eq.second, eq.third);
		//}
	}
	
	private void doCong() {
		uf = new UnionFind<>(pred.keySet());
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
			merge1(pred, uf, eq.second, eq.third);
		}
	}
	
	
	//synchronized bc eq can trigger redo
	@Override
	public synchronized boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		if (!ctx.isEmpty()) {
			throw new RuntimeException("Congruence prover can only be used with ground equations");
		}
		boolean changed = lhs.allSubExps(pred) | rhs.allSubExps(pred);
		if (changed) {
			doCong();
		}
		return uf.connected(lhs, rhs);
	}
	/*
	@Override
	public synchronized boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		if (!ctx.isEmpty()) {
			throw new RuntimeException("Congruence prover can only be used with ground equations");
		}
		boolean b1 = eq_old(ctx, lhs, rhs);
		boolean b2 = eq_new(ctx, lhs, rhs);
		if (b1 != b2) {
			throw new RuntimeException("XX\n" + super.theory + "\n\n" + lhs + " and " + rhs + " old " + b1 + "\n\n" + uf2);
		}
		return b1;
	}*/
	
	//@Override
	/*
	public synchronized boolean eq_new(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		if (!ctx.isEmpty()) {
			throw new RuntimeException("Congruence prover can only be used with ground equations");
		}
		if (pred2.containsKey(lhs) && pred2.containsKey(rhs)) {
			return uf2.connected(lhs, rhs);			
		}
		if (lhs.getApp().f.equals(rhs.getApp().f)) {
			for (int i = 0; i < lhs.getApp().args.size(); i++) {
				if (!eq_new(ctx, lhs.getApp().args.get(i), rhs.getApp().args.get(i))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
*/
	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		throw new RuntimeException("Cannot NF with congruence");
	}

}
