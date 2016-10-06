package catdata.aql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.algs.kb.CompletionProver;
import catdata.algs.kb.DPKB;
import catdata.algs.kb.KBExp;
import catdata.algs.kb.KBExp.KBApp;
import catdata.algs.kb.KBOptions;

public class CompletionProverHelper<Ty, En, Sym, Fk, Att, Gen, Sk> extends DPKB<Chc<Ty,En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> {
	
	final private DPKB<Chc<Ty,En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> cp;
	
	public CompletionProverHelper(Collection<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> init, AqlOptions ops, Collection<Chc<Ty, En>> sorts, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>> signature, List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>> theory, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) throws InterruptedException {
		boolean sort = (Boolean) ops.getOrDefault(AqlOption.completion_sort);
		boolean filter_subsumed = (Boolean) ops.getOrDefault(AqlOption.completion_filter_subsumed);
		boolean compose = (Boolean) ops.getOrDefault(AqlOption.completion_compose);
		
		@SuppressWarnings("unchecked")
		List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> prec2 = (List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>) ops.getOrDefault(AqlOption.completion_precedence);
		if (prec2 == null) {
			throw new RuntimeException("No completion_precedence given");			
		}
		List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> prec = new LinkedList<>(prec2);
		for (Head<Ty, En, Sym, Fk, Att, Gen, Sk> c : init) {
			if (!signature.containsKey(c)) {
				prec.remove(c); //simplfied away
			}
		}		
		
		if (!prec.isEmpty() && !(prec.get(0) instanceof Head)) {
			throw new RuntimeException("Anomaly: please report");
		}
		KBOptions options = new KBOptions(true, sort, false, false, Integer.MAX_VALUE, Integer.MAX_VALUE, filter_subsumed, compose); //TODO make relly unbounded
		
		Util.assertNoDups(prec);
		if (!new HashSet<>(prec).equals(signature.keySet())) {
			Set<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> precMinusSig = new HashSet<>(prec);
			precMinusSig.removeAll(signature.keySet());
			Set<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> sigMinusPrec = new HashSet<>(signature.keySet());
			sigMinusPrec.removeAll(prec);
			throw new RuntimeException("Incorrect precedence. Symbols in precedence but not signature: " + precMinusSig + " and symbols in signature but not precedence: " + sigMinusPrec);
		}
		Function<Pair<Head<Ty, En, Sym, Fk, Att, Gen, Sk>,Head<Ty, En, Sym, Fk, Att, Gen, Sk>>, Boolean> gt = x -> {
			if (x.first.obj != null && x.first.ty == null && x.second.obj != null && x.second.ty == null) {
				return ((Var)x.first.obj).toString().compareTo(((Var)x.second.obj).toString()) > 0;
			} else if (x.first.obj != null && x.first.ty == null) {
				return true;
			} else if (x.second.obj != null && x.second.ty == null) {
				return false;
			}
			
			int i = prec.indexOf(x.first);
			int j = prec.indexOf(x.second);
			if (i == -1 || j == -1) {
				throw new RuntimeException("Anomaly: please report");
			}
			return i > j;
		};
		
		cp = new CompletionProver<>(gt, options, Var.it, sorts, signature, theory);	
		
	}

	@Override
	public boolean eq(Map<Var, Chc<Ty, En>> ctx, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> lhs, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> rhs) {
		if (!ctx.isEmpty() && !hasNFs()) {
			System.out.println("1ask eq of " + herb(lhs) + " == " + herb(rhs) + " and nfs are " + cp.nf(Collections.emptyMap(), herb(lhs)) + " and " + cp.nf(Collections.emptyMap(), herb(rhs)));
			return cp.eq(Collections.emptyMap(), herb(lhs), herb(rhs));
		} else {
			System.out.println("2ask eq of " + herb(lhs) + " == " + herb(rhs) + " and nfs are " + cp.nf(Collections.emptyMap(), herb(lhs)) + " and " + cp.nf(Collections.emptyMap(), herb(rhs)));
			return cp.eq(ctx, lhs, rhs);
		}
	}

	private KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> herb(KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> term) {
		if (term.isVar) {
			Head<Ty, En, Sym, Fk, Att, Gen, Sk> h = Head.ObjAqlInternal(term.getVar().var);
			return new KBApp<>(h, Collections.emptyList());
		} else {
			return new KBApp<>(term.getApp().f, term.getApp().args.stream().map(this::herb).collect(Collectors.toList()));
		}
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
		return "Herbrandizing-iff-necessary of\n\n" + cp.toString();
	}


}
