package catdata.provers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;

public class MonoidalProver<T,C,V> extends DPKB<T,C,V> {

	private SemiThue<Chc<Chc<Unit,T>,C>> kb;
	private Set<Pair<List<Chc<Chc<Unit,T>,C>>, List<Chc<Chc<Unit,T>,C>>>> rules = new HashSet<>();
	
	public MonoidalProver(Collection<T> sorts, Map<C, Pair<List<T>, T>> sig, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> eqs) {
		super(sorts, sig, eqs);
		
		//!_1 =  (might be superflous) TODO aql
		rules.add(new Pair<>(Util.singList(Chc.inLeft(Chc.inLeft(new Unit()))) , Collections.emptyList()));
		
		//e : t -> 1 = !_1 - don't have any

		//(e : t -> t').!_t' = !_t
		for (C c : sig.keySet()) {
			if (sig.get(c).first.size() > 1) {
				throw new RuntimeException(c + " is not unary or zero-ary");
			}
			Chc<Unit,T> t = sig.get(c).first.size() == 0 ? Chc.inLeft(new Unit()) : Chc.inRight(sig.get(c).first.get(0));
			Chc<Unit,T> t0= Chc.inRight(sig.get(c).second);
			List<Chc<Chc<Unit,T>,C>> lhs = Util.list(Chc.inRight(c), Chc.inLeft(t0));
			List<Chc<Chc<Unit,T>,C>> rhs = Util.singList(Chc.inLeft(t));
			rules.add(new Pair<>(lhs, rhs));
		} 
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : eqs) {
			rules.add(new Pair<>(trans(eq.first, eq.second), trans(eq.first, eq.third)));
		}
		kb = new SemiThue<>(rules, -1); 
		kb.complete();
	}
	
	private List<Chc<Chc<Unit,T>,C>> trans(Map<V, T> ctx, KBExp<C, V> term) {
		Chc<Unit,T> t = ctx.isEmpty() ? Chc.inLeft(new Unit()) : Chc.inRight(ctx.values().iterator().next());
		//Chc<Unit,V> v = ctx.isEmpty() ? Chc.inLeft(new Unit()) : Chc.inRight(Ctx.getOnly(ctx).first);
		
		List<Chc<Chc<Unit,T>,C>> ret = new LinkedList<>();
		
		for (;;) {
			if (term.isVar) {
				break;
			} else if (term.getApp().args.isEmpty() &&  ctx.isEmpty()) {
				ret.add(Chc.inRight(term.getApp().f));
				break;
			} else if (term.getApp().args.isEmpty() && !ctx.isEmpty()) {
				ret.add(Chc.inRight(term.getApp().f));
				ret.add(Chc.inLeft(t));
				break;
			} else {
				ret.add(Chc.inRight(term.getApp().f));
				term = term.getApp().args.get(0);
			}
		}
		
		return Util.reverse(ret);
	}
	/*
	 * the reverse is tricky, and not needed
	 *
	private KBExp<C, V> trans(Map<V, T> ctx, List<Chc<Chc<Unit,T>,C>> list) {
		
		KBExp<C, V> ret;
		
		if (ctx.isEmpty() && list.isEmpty()) {
			throw new RuntimeException("Anomaly: please report");
		} else if (ctx.isEmpty() && !list.isEmpty()) {
			Chc<Chc<Unit,T>,C>
		}
		
		for (Chc<Chc<Unit,T>,C> c : list) {
			if (c.left) {
				Chc<Unit,T> b = c.l;
				if (b.left) {
					
				} else {
					
				}
			} else {
				C b = c.r;
				ret = new KBApp<>(b, Util.singList(ret));
			}
		}
		
	}
	*/
	
	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		return kb.equiv(trans(ctx, lhs), trans(ctx, rhs));
	}

	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		throw new RuntimeException("Cannot NF in monoidal prover");
		//return trans(ctx, kb.normalize("", trans(ctx, term))); hard to do
	}

	@Override
	public String toString() {
		return kb.toString();
	}
}
