package catdata.provers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Pair;
import catdata.Triple;
import catdata.UnionFind;

//Congruence closure a la Nelson Oppen
public class CongruenceProver<T, C, V> extends DPKB<T, C, V> {

	@Override
	public String toString() {
		return "CongruenceProver [uf=" + uf + ", pred=" + pred + "]";
	}

	private final UnionFind<KBExp<C, V>> uf;

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

	//extends congruence
	private void merge1(KBExp<C, V> u, KBExp<C, V> v) throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		if (uf.connected(u, v)) {
			return;
		}

		Set<KBExp<C, V>> pu = new HashSet<>(pred.get(uf.find(u)));
		Set<KBExp<C, V>> pv = new HashSet<>(pred.get(uf.find(v)));

		uf.union(u, v);
		pred.get(uf.find(u)).addAll(pu);
		pred.get(uf.find(v)).addAll(pv); //one of these will be redundant

		for (KBExp<C, V> x : pu) {
			for (KBExp<C, V> y : pv) {
				if (!uf.connected(x, y) && congruent(x, y)) {
					merge1(x, y);
				}
			}
		}
	}

	//for use when adding new terms - establishes congruence
	private void merge2(KBExp<C, V> u, KBExp<C, V> v) {
		Set<KBExp<C, V>> pu = new HashSet<>(pred.get(uf.find(u)));
		Set<KBExp<C, V>> pv = new HashSet<>(pred.get(uf.find(v)));
		
		uf.union(u, v);
		pred.get(uf.find(u)).addAll(pu);
		pred.get(uf.find(v)).addAll(pv); //one of these will be redundant TODO aql check that find never changes

		for (KBExp<C, V> x : pu) {
			for (KBExp<C, V> y : pv) {
				if (!uf.connected(x, y) && congruent(x, y)) {
					merge2(x, y);
				}
			}
		}
	}

	private final Map<KBExp<C, V>, Set<KBExp<C, V>>> pred;

	public CongruenceProver(Collection<T> sorts, Map<C, Pair<List<T>, T>> sig, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> eqs) throws InterruptedException {
		super(sorts, sig, eqs);

		// Set<KBExp<C,V>> exps = new HashSet<>();
		pred = new HashMap<>();
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
			if (!eq.first.isEmpty()) {
				throw new RuntimeException("Congruence method can not work with universal quantification");
			}
			// eq.second.allSubExps(exps);
			// eq.third.allSubExps(exps);
			eq.second.allSubExps(pred);
			eq.third.allSubExps(pred);
		}
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
			for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
				merge2(eq.second, eq.third);
			}
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
