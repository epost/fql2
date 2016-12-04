package catdata.provers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.RuntimeInterruptedException;
import catdata.Pair;
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

	private UnionFind<KBExp<C, V>> uf;

	// in paper this doesn't check label - appears to be typo (!)
	private boolean congruent(KBExp<C, V> u, KBExp<C, V> v) {
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

	private void merge1(KBExp<C, V> u, KBExp<C, V> v)  {
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
				if (!uf.connected(x, y) && congruent(x, y)) {
					merge1(x, y);
				}
			}
		}
	}

	private Map<KBExp<C, V>, Set<KBExp<C, V>>> pred;

	public CongruenceProver(Collection<T> sorts, Map<C, Pair<List<T>, T>> sig, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> eqs) {
		super(sorts, sig, eqs);
		pred = new HashMap<>();
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
			if (!eq.first.isEmpty()) {
				throw new RuntimeException("Congruence method can not work with universal quantification");
			}
			eq.second.allSubExps(pred);
			eq.third.allSubExps(pred);
		}
		for (C c : sig.keySet()) {
			(new KBApp<C,V>(c, Collections.emptyList())).allSubExps(pred);
		}
		doCong();
	}
	
	private void doCong() {
		uf = new UnionFind<>(pred.keySet());
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
			merge1(eq.second, eq.third);
		}
	}
	
	

	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		if (!ctx.isEmpty()) {
			throw new RuntimeException("Congruence prover can only be used with ground equations");
		}
		boolean changed = lhs.allSubExps(pred) | rhs.allSubExps(pred);
		if (changed) {
			doCong();
		}
		return uf.connected(lhs, rhs);
	}

	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		throw new RuntimeException("Cannot NF with congruence");
	}

}
