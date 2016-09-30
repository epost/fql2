package catdata.aql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;

//TODO: merge constants and functions in typesides
//TODO: need uniform timeout thingy now
public class AqlSaturator<Ty, En, Sym, Fk, Att, Gen, Sk, X> extends Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X> {

	private final DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp;
	
	private final Map<En, Set<X>> ens;
	private final Map<X, Map<Fk, X>> fks = new HashMap<>();
	private final Map<X, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> reprs = new HashMap<>();	
	private final Map<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, X> nfs = new HashMap<>();
	
	private final Instance<Ty, En, Sym, Fk, Att, Gen, Sk> inst;
	private final Iterator<X> fresh;

	// TODO deal with timeout uniformly
	public AqlSaturator(AqlOptions ops, Instance<Ty, En, Sym, Fk, Att, Gen, Sk> inst, Iterator<X> fresh) {
		dp = AqlProver.create(ops, inst.collage());
		ens = Util.newSetsFor(inst.schema.ens);
		this.inst = inst;
		this.fresh = fresh;
		
		int iter = 0;
		while (saturate1()) {
			if (iter++ > 100) {
				throw new RuntimeException("Saturate: DEBUG: emergency stop (report to Ryan)");
			}
			//TODO figure out how to do this only once but without concurrent modification exception
		}
		
		if ((Boolean) ops.getOrDefault(AqlOption.require_consistency) && !hasFreeTypeAlgebra()) {
			throw new RuntimeException("Not necessarily consistent; simplified type algebra is\n\n" + talg().simplify());
		}
	}

	private boolean add(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		X x = nf0(term);
		if (x != null) {
			return false;
		}
			
		x = fresh.next();
		
		nfs.put(term, x);
		ens.get(inst.type(term).r).add(x);
		reprs.put(x, term);
		
		Map<Fk, X> map = new HashMap<>();
		for (Fk fk : schema().fks.keySet()) {
			if (!inst.type(term).r.equals(schema().fks.get(fk).first)) {
				continue;
			}
			add(Term.Fk(fk, term));
			map.put(fk, nf(Term.Fk(fk, term)));
		}
		fks.put(x, map);
		
		return true;
	}
	
	private boolean saturate1() {
		boolean changed = false;
		for (Gen gen : inst.gens.keySet()) {
			if (inst.type(Term.Gen(gen)).left) {
				continue; 
			}
			changed = changed | add(Term.Gen(gen));			
		}
		for (Fk fk : inst.schema.fks.keySet()) {
			List<X> set = new ArrayList<>(ens.get(schema().fks.get(fk).first));
			for (X x : set) { //concurrent modification otherwise
				changed = changed | add(Term.Fk(fk, repr(x)));						
			}
		}
		return changed;
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return inst.schema;
	}

	@Override
	public DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp() {
		return dp;
	}

	@Override
	public Collection<X> en(En en) {
		return ens.get(en);
	}

	@Override
	public X fk(Fk fk, X x) {
		X r = fks.get(x).get(fk);
		if (r == null) {
			throw new RuntimeException("Anomaly, please report: " + fk + "(" + x + ") is not in " + fks.get(x));
		}
		return r;
	}

//	@Override
//	public String toString() {
//		return "AqlSaturator [ens=" + ens + ", fks=" + fks + ", reprs=" + reprs + ", nfs=" + nfs + ", talg=" + talg + "]";
//	}

	@Override 
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> repr(X x) {
		if (x == null) {
			throw new RuntimeException("Anomaly: null given to repr, please report");			
		}
		Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret = reprs.get(x);
		if (ret == null) {
			throw new RuntimeException("Anomaly: please report: " + x + " not in " + reprs);
		}
		return ret;
	}
	
	private X nf0(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (nfs.containsKey(term)) {
			return nfs.get(term);
		}
		En en = inst.type(term).r;
		for (X x : ens.get(en)) {
			if (dp.eq(new Ctx<>(), term, repr(x))) {
				nfs.put(term, x);
				return x;
			}
		}
		return null;
	}

	@Override
	public X nf(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		X x = nf0(term);
		if (x == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return x;
	}

	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg;

	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg_full() {
		if (talg != null) {
			return talg;
		}
		talg = new Collage<>(inst.schema.typeSide);
		for (Sk sk : inst.sks.keySet()) {
			talg.sks.put(Chc.inLeft(sk), inst.sks.get(sk));
		}
		for (En en : inst.schema.ens) {
			for (X x : ens.get(en)) {
				for (Att att : inst.schema.atts.keySet()) {
					Pair<En, Ty> ty = inst.schema.atts.get(att);
					if (!ty.first.equals(en)) {
						continue;
					}
					talg.sks.put(Chc.inRight(new Pair<>(x, att)), ty.second);
				}
			}
		}
		for (Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : inst.eqs) {
			if (!inst.type(eq.first).left) {
				continue;
			}
			talg.eqs.add(new Triple<>(new Ctx<>(), trans(eq.first), trans(eq.second)));
		}
		return talg;
	}

}
