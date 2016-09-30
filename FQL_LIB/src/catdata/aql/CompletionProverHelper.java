package catdata.aql;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.algs.kb.CompletionProver;
import catdata.algs.kb.KBExp;
import catdata.algs.kb.KBOptions;

public class CompletionProverHelper {
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> CompletionProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> create(Collection<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> init, AqlOptions ops, Collection<Chc<Ty, En>> sorts, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>> signature, List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>> theory, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		boolean sort = (Boolean) ops.getOrDefault(AqlOption.completion_sort);
		boolean filter_subsumed = (Boolean) ops.getOrDefault(AqlOption.completion_filter_subsumed);
		boolean compose = (Boolean) ops.getOrDefault(AqlOption.completion_compose);
		
		@SuppressWarnings("unchecked")
		List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> prec = (List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>) ops.getOrDefault(AqlOption.completion_precedence);
		if (prec == null) {
			throw new RuntimeException("No completion_precedence given");			
		}
		prec = new LinkedList<>(prec);
		for (Head<Ty, En, Sym, Fk, Att, Gen, Sk> c : init) {
			if (!signature.containsKey(c)) {
				prec.remove(c); //simplfied away
			}
		}
		
		
		if (!prec.isEmpty() && !(prec.get(0) instanceof Head)) {
			throw new RuntimeException("Anomaly: please report");
		}
		KBOptions options = new KBOptions(true, sort, false, true, 200000, 256, filter_subsumed, compose);
		
		return new CompletionProver<>(options, prec, Var.it, sorts, signature, theory);	
	}


}
