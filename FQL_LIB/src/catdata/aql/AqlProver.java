package catdata.aql;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import catdata.Chc;
import catdata.algs.kb.CongruenceProver;
import catdata.algs.kb.DPKB;
import catdata.algs.kb.FailProver;
import catdata.algs.kb.FreeProver;
import catdata.algs.kb.MonoidalProver;
import catdata.algs.kb.ProgramProver;


//TODO SQL strategy

//TODO: set infinite defaults for underlying provers

//TODO: cache hashcode for term?

//TODO: could herbrandize here

//TODO: redo program/monoidal/completion to work with empty sorts

//TODO: better guessing of precedences (fk, att, etc)



//no java here!
public class AqlProver<Ty, En, Sym, Fk, Att, Gen, Sk> {
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> 
	create(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col1) {
	
		Integer timeout = (Integer) ops.getOrDefault(AqlOption.timeout);
		ExecutorService executor = Executors.newSingleThreadExecutor();
	  
		Future<DP<Ty, En, Sym, Fk, Att, Gen, Sk>> future = executor.submit(new Callable<DP<Ty, En, Sym, Fk, Att, Gen, Sk>>() {

			@Override
			public DP<Ty, En, Sym, Fk, Att, Gen, Sk> call() throws Exception {
				ProverName name = (ProverName) ops.getOrDefault(AqlOption.prover);

				if (name.equals(ProverName.auto)) {
					name = auto(ops, col1.simplify().first);
				}
				
				col1.simplify();
		
				switch (name) {
				case auto: 
					throw new RuntimeException("Anomaly: please report");
				case precomputed:
					@SuppressWarnings("unchecked")
					DP<Ty, En, Sym, Fk, Att, Gen, Sk> ret = (DP<Ty, En, Sym, Fk, Att, Gen, Sk>) ops.get(AqlOption.precomputed);
					return ret;
				case fail: 
					return wrap(x -> { throw new RuntimeException(); }, new FailProver<>());
				case free: 
					return wrap(col1.simplify().second, new FreeProver<>(col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first));
				case saturated: 
					return wrap(x -> x, SaturatedProverHelper.create(ops, col1.toKB().third, col1.toKB().second, col1.toKB().first, col1));
				case congruence: 
					return wrap(col1.simplify().second, new CongruenceProver<>(col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first));
				case program:  
					boolean emptySortsOk = (Boolean) ops.getOrDefault(AqlOption.allow_empty_sorts_unsafe);
					boolean check = ! (Boolean) ops.getOrDefault(AqlOption.dont_verify_is_appropriate_for_prover_unsafe);
					col1.assertNoEmptySorts(emptySortsOk);
					return wrap(col1.simplify().second, new ProgramProver<>(check, Var.it, col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first)); //use simplified
				case completion: 
					emptySortsOk = (Boolean) ops.getOrDefault(AqlOption.allow_empty_sorts_unsafe);
					col1.assertNoEmptySorts(emptySortsOk);
					return wrap(col1.simplify().second, CompletionProverHelper.create(col1.toKB().second.keySet(), ops, col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first, col1.simplify().first)); //use simplified  	
				case monoidal:	
					emptySortsOk = (Boolean) ops.getOrDefault(AqlOption.allow_empty_sorts_unsafe);
					col1.assertNoEmptySorts(emptySortsOk);
					emptySortsOk = (Boolean) ops.getOrDefault(AqlOption.allow_empty_sorts_unsafe);
					return wrap(col1.simplify().second, new MonoidalProver<>(col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first)); //use simplified
				}
				
				throw new RuntimeException("Anomaly: please report");
			}
	    	
	    });
	    		
		try {
	     	if (timeout < 0) {
				return future.get();
		   	} else {
		   		return future.get(timeout, TimeUnit.SECONDS);
		   	} 
	    } catch (TimeoutException e) {
//	    	   e.printStackTrace();
	    	   future.cancel(true);
	    	   throw new RuntimeException("Timeout (" + timeout + "s) during decision procedure construction");
	       } catch (InterruptedException e) {
//	    	   e.printStackTrace();
	    	   future.cancel(true);
	    	   throw new RuntimeException("Interruption (" + timeout + "s) during decision procedure construction");
	       } catch (Throwable e) {
	//    	   e.printStackTrace();
	    	   throw new RuntimeException("Error during during decision procedure construction: " + e.getMessage());
	       }

	}

	private static <Sk, En, Fk, Ty, Att, Sym, Gen> ProverName auto(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		if (col.eqs.isEmpty()) {
			return ProverName.free;
		} else if (col.isGround()) {
			return ProverName.congruence;
		} else if (ProgramProver.isProgram(Var.it, col.toKB().first)) {
/*			if (!ops.options.containsKey(AqlOption.allow_empty_sorts_unsafe)) {
				ops.options.put(AqlOption.allow_empty_sorts_unsafe, true);
			} */
			return ProverName.program;
		} else if (col.isMonoidal()) {
			/*			if (!ops.options.containsKey(AqlOption.allow_empty_sorts_unsafe)) {
			ops.options.put(AqlOption.allow_empty_sorts_unsafe, true);
		} */
		return ProverName.monoidal;
	}
		throw new RuntimeException("Cannot automatically chose prover: theory is not free, ground, unary, or program.  You must use completion with an explicit precedence.");
	}

	private static <Sk, En, Fk, Ty, Att, Sym, Gen> DP<Ty, En, Sym, Fk, Att, Gen, Sk> wrap(Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp, DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb) {
	
		return new DP<Ty, En, Sym, Fk, Att, Gen, Sk>() {
			@Override
			public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
				if (lhs.equals(rhs)) {
					return true;
				}
				return dpkb.eq(ctx.map, simp.apply(lhs).toKB(), simp.apply(rhs).toKB());
			}

			@Override
			public boolean hasNFs() {
				return dpkb.hasNFs();
			}

			@Override
			public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
				return Term.fromKB(dpkb.nf(ctx.map, simp.apply(term).toKB()));
			}
			
			@Override
			public String toString() {
				return "Definitional simplification and reflexivity wrapping of\n\n" + dpkb;
			}
			
		};
	}

	

}
