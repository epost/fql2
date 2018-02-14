package catdata.provers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.RuntimeInterruptedException;
import catdata.Pair;
import catdata.Triple;
import catdata.provers.KBExp.KBApp;

//TODO aql
/*Loosen requirement for program in MVP that are of form

l -> r

in worst case, must try all combinations of orientations, but maybe can do better.
*/
//TODO aql merge constants and functions in typeside
public class ProgramProver<T, C, V> extends DPKB<T, C, V>  {

	private final Iterator<V> fresh;
	
	public ProgramProver(boolean check, Iterator<V> fresh, KBTheory<T,C,V> th) {
		super(th.tys, th.syms, th.eqs);

		this.fresh = fresh;
		
		if (check) {
			//TODO: aql freshen the rules?
			isProgram(fresh, theory, true);
		}
		
	}

	//TODO: aql find some way to cache the fact that something has been proved to be a program
	public static <T,C,V> boolean isProgram(Iterator<V> fresh, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> theory, boolean throwerror) {
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : theory) {
			List<V> vars = new LinkedList<>();
			eq.second.vars(vars);
			if (vars.size() != eq.second.vars().size()) {
				if (throwerror) {
					throw new RuntimeException("Not left linear (cotains duplicated variable on lhs): " + eq.second + " = " + eq.third);
				} else {
					return false;
				}
			}			
		}
		
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> ab0 : theory) {
			for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> gd0 : theory) {
				Pair<KBExp<C, V>, KBExp<C, V>> ab = LPOUKB.freshen(fresh, new Pair<>(ab0.second, ab0.third));
				Pair<KBExp<C, V>, KBExp<C, V>> gd = LPOUKB.freshen(fresh, new Pair<>(gd0.second, gd0.third));
			
				Set<Triple<KBExp<C, V>, KBExp<C, V>, Map<V, KBExp<C, V>>>> cps = gd.first.cp(new LinkedList<>(), ab.first, ab.second, gd.first, gd.second);
				for (Triple<KBExp<C, V>, KBExp<C, V>, Map<V, KBExp<C, V>>> cp : cps) {
					if (!cp.first.equals(cp.second)) {
						if (throwerror) {
							throw new RuntimeException("Is not a program: equation " + ab0.second + " = " + ab0.third + " overlaps with " + gd0.second + " = " + gd0.third + ", the critical pair is " + cp.first + " and " + cp.second);			
						} else {
							return false;
						}
					}
				}
			}			
		}
		
		return true;
	}
	
	private final Map<KBExp<C,V>, KBExp<C,V>> cache = new HashMap<>();

	private KBExp<C, V> red(KBExp<C, V> e) {
        while (true) {
            KBExp<C, V> e0 = step(e);
            if (e.equals(e0)) {
                return e0;
            }
            e = e0;
        }
	}
	
	private KBExp<C, V> step(KBExp<C, V> ee) {
		if (Thread.currentThread().isInterrupted()) {
			throw new RuntimeInterruptedException(new InterruptedException());
		}
		if (ee.isVar) {
			return step1(ee); 
		} else {
			KBApp<C, V> e = ee.getApp();
			List<KBExp<C, V>> args0 = new LinkedList<>();
			for (KBExp<C, V> arg : e.args) {
				args0.add(step(arg)); //needs to be step for correctness
			}
			KBApp<C, V> ret = new KBApp<>(e.f, args0);
			return step1(ret);
		} 
	}
	
	private KBExp<C, V> step1(KBExp<C, V> e0) {
		KBExp<C, V> e = e0;
		if (cache != null && cache.containsKey(e)) {
			return cache.get(e);
		}
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> r0 : theory) {
			Pair<KBExp<C, V>, KBExp<C, V>> r = new Pair<>(r0.second, r0.third);
			if (!Collections.disjoint(r.first.vars(), e.vars()) || !Collections.disjoint(r.second.vars(), e.vars())) {
				r = LPOUKB.freshen(fresh, r);
			}
			
			KBExp<C, V> lhs = r.first;
			KBExp<C, V> rhs = r.second;
			Map<V, KBExp<C, V>> s = KBUnifier.findSubst(lhs, e);
			if (s == null) {
				continue;
			}
			e = rhs.subst(s);
		}
		if (cache == null || e0 == null || e == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		cache.put(e0, e);

		return e;
	}
	
	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		return nf(ctx, lhs).equals(nf(ctx, rhs));
	}

	@Override
	public boolean hasNFs() {
		return true;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		return red(term);
	}
	
	@Override
	public String toString() {
		return "Program prover";
	}

}
