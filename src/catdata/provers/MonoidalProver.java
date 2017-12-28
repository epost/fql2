package catdata.provers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;

/**
 * works correctly on empty sorts
 */
public class MonoidalProver<T,C,V> extends DPKB<T,C,V> {

	private final Thue<Chc<Chc<Unit,T>,C>> kb;

	public MonoidalProver(KBTheory<T,C,V> th) {
		super(th.tys, th.syms, th.eqs);
		
		//!_1 =  (might be superflous) TODO aql
		List<Pair<List<Chc<Chc<Unit, T>, C>>, List<Chc<Chc<Unit, T>, C>>>> rules = new LinkedList<>();
		rules.add(new Pair<>(Util.singList(Chc.inLeft(Chc.inLeft(new Unit()))) , Collections.emptyList()));
		
		//e : t -> 1 = !_1 - don't have any

		//(e : t -> t').!_t' = !_t
		for (C c : th.syms.keySet()) {
			if (th.syms.get(c).first.size() > 1) {
				throw new RuntimeException(c + " is not unary or zero-ary");
			}
			Chc<Unit,T> t = th.syms.get(c).first.isEmpty() ? Chc.inLeft(new Unit()) : Chc.inRight(th.syms.get(c).first.get(0));
			Chc<Unit,T> t0= Chc.inRight(th.syms.get(c).second);
			List<Chc<Chc<Unit,T>,C>> lhs = Util.list(Chc.inRight(c), Chc.inLeft(t0));
			List<Chc<Chc<Unit,T>,C>> rhs = Util.singList(Chc.inLeft(t));
			rules.add(new Pair<>(lhs, rhs));
		} 
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : th.eqs) {
			rules.add(new Pair<>(trans(eq.first, eq.second), trans(eq.first, eq.third)));
		}
		kb = new Thue<>(rules, -1); 
		kb.complete();
	}
	
	private List<Chc<Chc<Unit,T>,C>> trans(Map<V, T> ctx, KBExp<C, V> term) {
		Chc<Unit,T> t = ctx.isEmpty() ? Chc.inLeft(new Unit()) : Chc.inRight(ctx.values().iterator().next());
		//Chc<Unit,V> v = ctx.isEmpty() ? Chc.inLeft(new Unit()) : Chc.inRight(Ctx.getOnly(ctx).first);
		
		List<Chc<Chc<Unit,T>,C>> ret = new LinkedList<>();

        while (true) {
            if (term.isVar) {
                break;
            } else if (term.getApp().args.isEmpty() && ctx.isEmpty()) {
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
	}

	@Override
	public String toString() {
		return kb.toString();
	}
}
