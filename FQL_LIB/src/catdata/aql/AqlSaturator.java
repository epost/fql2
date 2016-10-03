package catdata.aql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;

//TODO: merge constants and functions in typesides
//TODO: need uniform timeout thingy now
public class AqlSaturator<Ty, En, Sym, Fk, Att, Gen, Sk, X> extends Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X> {

	private final DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp;
	
	private final Map<En, Set<X>> ens;
	private final Map<X, Map<Fk, X>> fks = new HashMap<>();
	private final Map<X, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> reprs = new HashMap<>();	
	private final Map<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, X> nfs = new HashMap<>();
	
	private final Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col;
	private final Schema<Ty, En, Sym, Fk, Att> schema;
	//private final Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> inst;
	private final Iterator<X> fresh;

	//private int size = 0;
	
	public AqlSaturator(AqlOptions ops, Schema<Ty, En, Sym, Fk, Att> schema,  Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, Iterator<X> fresh) {
		ens = Util.newSetsFor(schema.ens);
		this.col = col;
		this.schema = schema;
		this.fresh = fresh;
		
		if (schema.typeSide.java_tys.isEmpty()) {
			dp = AqlProver.create(ops, col);
		} else {
			dp = AqlProver.create(ops, col.entities_only());
		}
	
		Integer timeout = (Integer) ops.getOrDefault(AqlOption.timeout);
		ExecutorService executor = Executors.newSingleThreadExecutor();
	    Future<Unit> future = executor.submit(new Callable<Unit>() {

			@Override
			public Unit call() throws Exception {
				while (saturate1()); 
				return new Unit();
			}
	    	
	    });
	    		
	       try {
	    	   	if (timeout < 0) {
	    	   		future.get();
	    	   	} else {
	    	   		future.get(timeout, TimeUnit.SECONDS);
	    	   	}
	       } catch (TimeoutException e) {
	    	   e.printStackTrace();
	    	   future.cancel(true);
	    	   throw new RuntimeException("Timeout (" + timeout + "s) during saturation");
	       } catch (InterruptedException e) {
	    	   return;
	       } catch (ExecutionException e) {
	    	   e.printStackTrace();
	    	   throw new RuntimeException("Error during saturation: " + e.getMessage());
	       }
			//TODO figure out how to do this only once but without concurrent modification exception
			
		if ((Boolean) ops.getOrDefault(AqlOption.require_consistency) && !hasFreeTypeAlgebra()) {
			throw new RuntimeException("Not necessarily consistent; simplified type algebra is\n\n" + talg().simplify());
		}
	}

	//TODO is it really safe to do depth first saturation?
	private boolean add(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		
		X x = nf0(term);
		if (x != null) {
			return false;
		}
			
		x = fresh.next();
		
		nfs.put(term, x);
		ens.get(col.type(new Ctx<>(), term).r).add(x);
		reprs.put(x, term);
		
		Map<Fk, X> map = new HashMap<>();
		for (Fk fk : schema().fks.keySet()) {
			if (!col.type(new Ctx<>(), term).r.equals(schema().fks.get(fk).first)) {
				continue;
			}
			add(Term.Fk(fk, term));
			map.put(fk, nf(Term.Fk(fk, term)));
		}
		fks.put(x, map);
		
		return true; 
	}
	
	private boolean saturate1() throws InterruptedException {
		boolean changed = false;
		for (Gen gen : col.gens.keySet()) {
			if (col.type(new Ctx<>(), Term.Gen(gen)).left) {
				continue; 
			}
			changed = changed | add(Term.Gen(gen));			
		}
		for (Fk fk : col.fks.keySet()) {
			List<X> set = new ArrayList<>(ens.get(schema().fks.get(fk).first));
			for (X x : set) { //concurrent modification otherwise
				changed = changed | add(Term.Fk(fk, repr(x)));						
			}
		}
		return changed;
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return schema;
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
		En en = col.type(new Ctx<>(), term).r;
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
		talg = new Collage<>(schema.typeSide);
		for (Sk sk : col.sks.keySet()) {
			talg.sks.put(Chc.inLeft(sk), col.sks.get(sk));
		}
		for (En en : col.ens) {
			for (X x : ens.get(en)) {
				for (Att att : col.atts.keySet()) {
					Pair<En, Ty> ty = col.atts.get(att);
					if (!ty.first.equals(en)) {
						continue;
					}
					talg.sks.put(Chc.inRight(new Pair<>(x, att)), ty.second);
				}
			}
		}
		for (Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : col.eqs) {
			if (!col.type(eq.first, eq.second).left) {
				continue; //entity
			}
			if (!eq.first.isEmpty()) {
				continue; //in type side or schema
			}
			if (schema.typeSide.eqs.contains(new Triple<>(new Ctx<>(), eq.second, eq.third))) {
				continue; //in type side
			}
			talg.eqs.add(new Triple<>(new Ctx<>(), trans(eq.second), trans(eq.third)));
		}
		
		for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq : schema().eqs) {
			if (schema().type(eq.first, eq.second).left) { //type
				for (X x : ens.get(eq.first.second)) {
					Map<Var, Term<Ty, En, Sym, Fk, Att, Void, Void>> map = new HashMap<>();
					map.put(eq.first.first, repr(x).convert());
					talg.eqs.add(new Triple<>(new Ctx<>(), trans(eq.second.subst(map).convert()), trans(eq.third.subst(map).convert())));
				}
			} 
		}
		
		
		return talg;
	}

	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
		if (schema.typeSide.java_tys.isEmpty()) {
			return dp.eq(ctx, lhs, rhs);
		} else {
			if (col.type(ctx, lhs).left) { //type
				return AqlJs.reduce(lhs, col).equals(AqlJs.reduce(rhs, col));
			} else {
				return dp.eq(ctx, lhs, rhs);
			}
		}
	}

	@Override
	public boolean hasNFs() {
		return true;
	}

	@Override
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (col.type(ctx, term).left) { //type
			if (schema.typeSide.java_tys.isEmpty()) {
				return dp.nf(ctx, term);
			} else {
				return AqlJs.reduce(term, col); 
			}
		} 
		if (schema.typeSide.java_tys.isEmpty()) {
			return dp.nf(ctx, term);
		} else if (ctx.isEmpty()){
			return repr(nf(term));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	@Override
	public String toString() {
		String ret = super.toString();
		
		ret += "\n\nprover\n\n";
		ret += dp.toString();
		return ret;
	}
	
	

}
