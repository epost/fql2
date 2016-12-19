package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.RuntimeInterruptedException;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.provers.CompletionProver;
import catdata.provers.CongruenceProver;
import catdata.provers.DPKB;
import catdata.provers.FailProver;
import catdata.provers.FreeProver;
import catdata.provers.MonoidalProver;
import catdata.provers.ProgramProver;
import catdata.provers.SaturatedProver;

//TODO: aql cache hashcode for term?

//TODO: aql maybe easier to check queries by translation into mappings?

//TODO: aql add abort functionality

//no java here!
public class AqlProver {

	public static enum ProverName {
		auto, saturated, monoidal, program, completion, congruence, fail, free,
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> create(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col1) {
		ProverName name = (ProverName) ops.getOrDefault(AqlOption.prover);

		if (name.equals(ProverName.auto)) {
			name = auto(ops, col1.simplify().first);
		}

		// col1.simplify();

		try {
			switch (name) {
			case auto:
				throw new RuntimeException("Anomaly: please report");
			case fail:
				return wrap(x -> {
					throw new RuntimeException();
				}, new FailProver<>());
			case free:
				return wrap(col1.simplify().second, new FreeProver<>(col1.simplify().first.toKB()));
			case saturated:
				return wrap(x -> x, saturatedProverHelper(ops, col1));
			case congruence:
				return wrap(col1.simplify().second, new CongruenceProver<>(col1.simplify().first.toKB()));
			case program:
				boolean check   = !(Boolean) ops.getOrDefault(AqlOption.dont_verify_is_appropriate_for_prover_unsafe);
				boolean allowNonTerm = (Boolean) ops.getOrDefault(AqlOption.program_allow_nontermination_unsafe);
				try {
					if (!allowNonTerm) {
						col1 = reorient(col1);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex.getMessage() + "\n\nPossible solution: add options program_allow_nontermination_unsafe=true, or prover=completion");
				}
				return wrap(col1.simplify().second, new ProgramProver<>(check, Var.it, col1.simplify().first.toKB())); // use																																																		
			case completion:
				return wrap(col1.simplify().second, new CompletionProver<>(col1.toKB().syms.keySet(), ops, col1.simplify().first));
			case monoidal:
				return wrap(col1.simplify().second, new MonoidalProver<>(col1.simplify().first.toKB())); // use
																																																// simplified
			default:
				throw new RuntimeException("Anomaly: please report");
			}
 
		} catch (InterruptedException exn) {
			throw new RuntimeInterruptedException(exn);
		}
	}

	private static <Sk, En, Fk, Ty, Att, Sym, Gen> ProverName auto(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		if (col.eqs.isEmpty()) {
			return ProverName.free;
		} else if (col.isGround()) {
			return ProverName.congruence;
		} else if (col.isMonoidal()) {
			return ProverName.monoidal;
		} else if (!(Boolean)ops.getOrDefault(AqlOption.program_allow_nontermination_unsafe) && reorientable(col) && ProgramProver.isProgram(Var.it, reorient(col).toKB().eqs, false)) {
			return ProverName.program; 
		} else if ((Boolean)ops.getOrDefault(AqlOption.program_allow_nontermination_unsafe) && ProgramProver.isProgram(Var.it, col.toKB().eqs, false)) {
			return ProverName.program; 
		} 
		throw new RuntimeException("Cannot automatically chose prover: theory is not free, ground, unary, or program.  Possible solutions include: \n\n1) use the completion prover, possibly with an explicit precedence (see A KB example) \n\n2) Reorient equations from left to right to obtain a size-reducing orthogonal rewrite system \n\n3) Remove all binary function symbols \n\n4) Remove all type side and schema equations \n\n5) disable checking of equations in queries using dont_validate_unsafe=true as an option \n\n6) adding options program_allow_nontermination_unsafe=true \n\n7) emailing support, info@catinf.com ");
	}
	
	private static <Ty, En, Sym, Fk, Att, Gen, Sk> boolean reorientable(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		try {
			reorient(col);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reorient(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> ret = new Collage<>(col);
		ret.eqs.clear();
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : col.eqs) {
			if (size(eq.lhs) < size(eq.rhs)) {
				ret.eqs.add(new Eq<>(eq.ctx, eq.rhs, eq.lhs));
			} else if (size(eq.lhs) > size(eq.rhs)) {
				ret.eqs.add(eq);
			} else {
				throw new RuntimeException("Cannot orient " + eq + " in a size reducing manner.");
			}
				
		}
		return ret;
	}

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> int size(Term<Ty, En, Sym, Fk, Att, Gen, Sk> e) {
		if (e.obj != null || e.gen != null || e.sk != null || e.var != null) {
			return 1;
		} else if (e.att != null || e.fk != null) {
			return 1 + size(e.arg);
		} 
		int ret = 1;
		for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : e.args) {
			ret += size(arg);
		}
		return ret;
	}

	private static <Sk, En, Fk, Ty, Att, Sym, Gen> DP<Ty, En, Sym, Fk, Att, Gen, Sk> wrap(Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp, DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb) {

		return new DP<Ty, En, Sym, Fk, Att, Gen, Sk>() {

			private final Map<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>, Boolean> cache = new HashMap<>();

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

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> SaturatedProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> saturatedProverHelper(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) throws InterruptedException {
		SaturatedProver<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> ret = new SaturatedProver<>(col.toKB());

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

	private static <X> List<List<X>> extend(List<List<X>> ls, List<X> r) { // TODO
																			// aql
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
