package catdata.provers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Head;
import catdata.aql.Var;

public class CompletionProver<Ty, En, Sym, Fk, Att, Gen, Sk> extends DPKB<Chc<Ty,En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> {
	
	private final LPOUKB<Chc<Ty,En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> cp;
	
	public CompletionProver(Collection<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> init, AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) throws InterruptedException {
		super(col.toKB().tys, col.toKB().syms, col.toKB().eqs);
		boolean sort = (Boolean) ops.getOrDefault(AqlOption.completion_sort);
		boolean filter_subsumed = (Boolean) ops.getOrDefault(AqlOption.completion_filter_subsumed);
		boolean compose = (Boolean) ops.getOrDefault(AqlOption.completion_compose);
		boolean syntactic_ac = (Boolean) ops.getOrDefault(AqlOption.completion_syntactic_ac);
		
		Set<Triple<KBExp<Head<Ty,En,Sym,Fk,Att,Gen,Sk>,Var>, KBExp<Head<Ty,En,Sym,Fk,Att,Gen,Sk>,Var>, Map<Var, Chc<Ty,En>>>>
		E0 = theory.stream().map(x -> new Triple<>(x.second, x.third, x.first)).collect(Collectors.toSet());
		@SuppressWarnings("unchecked")
		List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> prec2 = (List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>) ops.getOrDefault(AqlOption.completion_precedence);
		if (prec2 == null) {
			Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Integer> m = new HashMap<>();
			for (Head<Ty, En, Sym, Fk, Att, Gen, Sk> c : signature.keySet()) {
				m.put(c, signature.get(c).first.size());
			}
			prec2 = LPOUKB.inferPrec(m, E0); 
		//	System.out.println("prec2 " + prec2);
		}
		List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> prec = new LinkedList<>(prec2);
		for (Head<Ty, En, Sym, Fk, Att, Gen, Sk> c : init) {
			if (!signature.containsKey(c)) {
				prec.remove(c); //simplfied away TODO aql kind of weird
			}
		}		
		//&& !(prec.get(0) instanceof Head)
		if (!prec.isEmpty() && prec.get(0) == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		KBOptions options = new KBOptions(true, sort, false, true, Integer.MAX_VALUE, Integer.MAX_VALUE, filter_subsumed, compose, syntactic_ac); //this ignores all but 4 options, see LPOUKB
		
		Util.assertNoDups(prec);
		if (!new HashSet<>(prec).equals(signature.keySet())) {
			Set<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> precMinusSig = new HashSet<>(prec);
			precMinusSig.removeAll(signature.keySet());
			Set<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> sigMinusPrec = new HashSet<>(signature.keySet());
			sigMinusPrec.removeAll(prec);
			throw new RuntimeException("Incorrect precedence. Symbols in precedence but not signature: " + precMinusSig + " and symbols in signature but not precedence: " + sigMinusPrec);
		}		
	//	System.out.println("prec: " + prec);
		cp = new LPOUKB<>(E0, Var.it, Collections.emptySet(), options, prec, col.toKB().syms, new HashSet<>(col.toKB().tys));	
		
	}

	@Override
	public boolean eq(Map<Var, Chc<Ty, En>> ctx, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> lhs, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> rhs) {
		return cp.eq(ctx, lhs, rhs);
	}

	@Override
	public boolean hasNFs() {
		return cp.hasNFs();
	}

	@Override
	public KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> nf(Map<Var, Chc<Ty, En>> ctx, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> term) {
		return cp.nf(ctx, term);
	}
	
	@Override
	public String toString() {
		return cp.toString();
	}


}
