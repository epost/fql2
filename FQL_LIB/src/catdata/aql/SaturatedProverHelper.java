package catdata.aql;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.algs.kb.KBExp;
import catdata.algs.kb.SaturatedProver;

public class SaturatedProverHelper {

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> SaturatedProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> create(AqlOptions ops, Collection<Chc<Ty, En>> sorts, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>> signature, List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>> theory, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) throws InterruptedException {
		SaturatedProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> ret = new SaturatedProver<>(sorts, signature, theory);
		
		if ((Boolean) ops.getOrDefault(AqlOption.dont_verify_is_appropriate_for_prover_unsafe)) {
			return ret;
		}
	//	System.out.println("col is " + col);
	//	System.out.println("map is " + ret.map);
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

	private static <X> List<List<X>> extend(List<List<X>> ls, List<X> r) {
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
