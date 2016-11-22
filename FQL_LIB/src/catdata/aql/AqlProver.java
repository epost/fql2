package catdata.aql;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.InvisibleException;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.provers.CompletionProver;
import catdata.provers.CongruenceProver;
import catdata.provers.DPKB;
import catdata.provers.FailProver;
import catdata.provers.FreeProver;
import catdata.provers.KBExp;
import catdata.provers.MonoidalProver;
import catdata.provers.ProgramProver;
import catdata.provers.SaturatedProver;


//TODO: aql cache hashcode for term?

//TODO: aql maybe easier to check queries by translation into mappings?

//TODO: aql add abort functionality

//no java here!
public class AqlProver<Ty, En, Sym, Fk, Att, Gen, Sk> {
	
	
	public static enum ProverName {
		auto,
		saturated,
		monoidal,
		program,
		completion,
		congruence,
		fail,
		free,
	}
	
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
				 
				//col1.simplify();
		
				switch (name) {
				case auto: 
					throw new RuntimeException("Anomaly: please report");
				case fail: 
					return wrap(col1, x -> { throw new RuntimeException(); }, new FailProver<>());
				case free: 
					return wrap(col1, col1.simplify().second, new FreeProver<>(col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first));
				case saturated: 
					return wrap(col1, x -> x, saturatedProverHelper(ops, col1.toKB().third, col1.toKB().second, col1.toKB().first, col1));
				case congruence: 
					return wrap(col1, col1.simplify().second, new CongruenceProver<>(col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first));
				case program:  
					boolean check = ! (Boolean) ops.getOrDefault(AqlOption.dont_verify_is_appropriate_for_prover_unsafe);
					return wrap(col1, col1.simplify().second, new ProgramProver<>(check, Var.it, col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first)); //use simplified
				case completion: 
					return wrap(col1, col1.simplify().second, new CompletionProver<>(col1.toKB().second.keySet(), ops, col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first, col1.simplify().first)); //use simplified  	
				case monoidal:	
					return wrap(col1, col1.simplify().second, new MonoidalProver<>(col1.simplify().first.toKB().third, col1.simplify().first.toKB().second, col1.simplify().first.toKB().first)); //use simplified
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
	    	   throw new RuntimeException("Timeout (" + timeout + "s) during decision procedure construction.  If using completion, you might try a different completion_precedence.");
	       } catch (InterruptedException e) {
//	    	   e.printStackTrace();
	    	   future.cancel(true);
	    	   throw new InvisibleException("Interruption during decision procedure construction.");
	       } catch (Throwable e) {
	    	   e.printStackTrace();
	    	   throw new RuntimeException(e);
	       }

	}

	//TODO aql add timeout to eq
	
	private static <Sk, En, Fk, Ty, Att, Sym, Gen> ProverName auto(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		if (col.eqs.isEmpty()) {
			return ProverName.free;
		} else if (col.isGround()) {
			return ProverName.congruence;
		} else if (col.isMonoidal()) {
			return ProverName.monoidal;
		} else if (ProgramProver.isProgram(Var.it, col.toKB().first, false)) {
			return ProverName.program; //TODO aql perhaps prover should not be enabled as auto choosable because eq can hang for bad systems
		} 
		throw new RuntimeException("Cannot automatically chose prover: theory is not free, ground, unary, or program.  You must use completion with an explicit precedence.");
	}

	private static <Sk, En, Fk, Ty, Att, Sym, Gen> DP<Ty, En, Sym, Fk, Att, Gen, Sk> wrap(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp, DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb) {
	
		return new DP<Ty, En, Sym, Fk, Att, Gen, Sk>() {
			
			private Map<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>, Boolean> cache = new HashMap<>();
			
			@Override
			public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
				if (lhs.equals(rhs)) {
					return true;
				}
				Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq = new Eq<>(ctx, lhs, rhs);
				Boolean b = cache.get(eq);
				if (b != null) {
					return b;
				}
				b = dpkb.eq(ctx.map, simp.apply(lhs).toKB(), simp.apply(rhs).toKB());
				cache.put(eq, b);
				return b;
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
				return "Caching definitional simplification and reflexivity wrapping of\n\n" + dpkb;
			}

			
		};
	}


	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> SaturatedProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> saturatedProverHelper(AqlOptions ops, Collection<Chc<Ty, En>> sorts, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>> signature, List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>> theory, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) throws InterruptedException {
		SaturatedProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> ret = new SaturatedProver<>(sorts, signature, theory);
		
		if ((Boolean) ops.getOrDefault(AqlOption.dont_verify_is_appropriate_for_prover_unsafe)) {
			return ret;
		}
		for (Fk fk : col.fks.keySet()) {
			for (Gen gen : col.gens.keySet()) {
				if (!col.fks.get(fk).first.equals(col.gens.get(gen))) {
					continue;
				}
				if (!ret.map.containsKey(Head.Fk(fk))) {
					throw new RuntimeException("No equations for " + fk);
				}
				Head<Ty, En, Sym, Fk, Att, Gen, Sk> x = ret.map.get(Head.Fk(fk)).get(Util.singList(Head.Gen(gen)));
				if (x == null) {
					throw new RuntimeException("No equation for " + gen + "." + fk);
				}
			}
		}
		for (Att att : col.atts.keySet()) {
			for (Gen gen : col.gens.keySet()) {
				if (!col.atts.get(att).first.equals(col.gens.get(gen))) {
					continue;
				}
				if (!ret.map.containsKey(Head.Att(att))) {
					throw new RuntimeException("No equations for " + att);
				}
				Head<Ty, En, Sym, Fk, Att, Gen, Sk> x = ret.map.get(Head.Att(att)).get(Util.singList(Head.Gen(gen)));
				if (x == null) {
					throw new RuntimeException("No equation for " + gen + "." + att);
				}
			}
		}
		for (Sym sym : col.syms.keySet()) {
			if (col.syms.get(sym).first.isEmpty() || col.java_fns.containsKey(sym)) {
				continue;
			}
			List<List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>> allArgs = allArgs(sym, col);
			for (List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> arg : allArgs) {
				if (!ret.map.containsKey(Head.Sym(sym))) {
					throw new RuntimeException("No equations for " + sym);
				}
				Head<Ty, En, Sym, Fk, Att, Gen, Sk> x = ret.map.get(Head.Sym(sym)).get(arg);
				if (x == null) {
					List<String> s = arg.stream().map(Head::toString).collect(Collectors.toList());
					throw new RuntimeException("No equation for " + sym + "(" + Util.sep(s, ",") + ")");
				}
			}
		}
		return ret;
	}

	// these Lists will never have dups
	private static <Ty, En, Sym, Fk, Att, Gen, Sk> List<List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>> allArgs(Sym sym, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		List<List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>> ret = new LinkedList<>();
		ret.add(new LinkedList<>());

		for (Ty ty : col.syms.get(sym).first) {
			List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> l = new LinkedList<>();
			for (Sym sym2 : col.syms.keySet()) {
				if (col.syms.get(sym2).first.isEmpty() && col.syms.get(sym2).second.equals(ty)) {
					l.add(Head.Sym(sym2));
				}
			}
			for (Sk sk : col.sks.keySet()) {
				if (col.sks.get(sk).equals(ty)) {
					l.add(Head.Sk(sk));
				}
			}
			ret = extend(ret, l);
		}

		return ret;
	}

	private static <X> List<List<X>> extend(List<List<X>> ls, List<X> r) { //TODO aql
		List<List<X>> ret = new LinkedList<>();
		for (List<X> l : ls) {
			for (X x : r) {
				List<X> y = new LinkedList<>(l);
				y.add(x);
				ret.add(y);
			}
		}
		return ret;
	}

	
}



