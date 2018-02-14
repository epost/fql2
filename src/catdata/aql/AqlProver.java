package catdata.aql;

import catdata.RuntimeInterruptedException;
import catdata.aql.AqlOptions.AqlOption;
import catdata.provers.CompletionProver;
import catdata.provers.CongruenceProver;
import catdata.provers.FailProver;
import catdata.provers.FreeProver;
import catdata.provers.MaedmaxProver;
import catdata.provers.ProgramProver;

//TODO: aql cache hashcode for term?

//TODO: aql maybe easier to check queries by translation into mappings?

//TODO: aql add abort functionality

//no java here!
public class AqlProver {

	public enum ProverName {
		auto, monoidal, program, completion, congruence, fail, free, maedmax
	}

	//these provers say that x = y when that is true when all java symbols are treated as free,
	//or if x and y reduce to the same java normal form.  as such, the provers won't actually
	//decide, for example, that e = 2 -> e + 1 = 3.  So the DP is not necessarily a decision
	//procedure for the input theory + java - or even any theory at all, because you may not have
	//x = y and y = z -> x = z when java is involved.  
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> create(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col1, AqlJs<Ty, Sym> js) {
		ProverName name = (ProverName) ops.getOrDefault(AqlOption.prover);
		long timeout = (Long) ops.getOrDefault(AqlOption.timeout);
		
		if (name.equals(ProverName.auto)) {
			name = auto(ops, col1.simplify().first);
		}

		// col1.simplify();

		try {
			switch (name) {
			case auto:
				throw new RuntimeException("Anomaly: please report");
			case fail:
				return new KBtoDP<>(js, x -> {
					throw new RuntimeException();
				}, new FailProver<>());
			case free:
				return new KBtoDP<>(js, col1.simplify().second, new FreeProver<>(col1.simplify().first.toKB()));
			case congruence:
				return new KBtoDP<>(js, col1.simplify().second, new CongruenceProver<>(col1.simplify().first.toKB()));
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
				return new KBtoDP<>(js, col1.simplify().second, new ProgramProver<>(check, Var.it, col1.simplify().first.toKB())); // use																																																		
			case completion:
				return new KBtoDP<>(js, col1.simplify().second, new CompletionProver<>(col1.toKB().syms.keySet(), ops, col1.simplify().first));
			case monoidal:
				return new MonoidalFreeDP<>(js, col1.simplify().second, col1.simplify().first); // use																																					// simplified
			case maedmax:
				String exePath = (String) ops.getOrDefault(AqlOption.maedmax_path);
				Boolean b = (Boolean) ops.getOrDefault(AqlOption.maedmax_allow_empty_sorts_unsafe);
				return new KBtoDP<>(js, col1.simplify().second, new MaedmaxProver<>(exePath, col1.simplify().first.toKB(), b, timeout)); // use																																																		
				
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
		} else if (MonoidalFreeDP.ok(col)) {
			return ProverName.monoidal;
		} else if (!(Boolean) ops.getOrDefault(AqlOption.program_allow_nontermination_unsafe) && reorientable(col) && ProgramProver.isProgram(Var.it, reorient(col).toKB().eqs, false) || (Boolean) ops.getOrDefault(AqlOption.program_allow_nontermination_unsafe) && ProgramProver.isProgram(Var.it, col.toKB().eqs, false)) {
			return ProverName.program; 
		}
		throw new RuntimeException("Cannot automatically chose prover: theory is not free, ground, monoidal, or program.  Possible solutions include: \n\n1) use the completion prover, possibly with an explicit precedence (see KB example) \n\n2) Reorient equations from left to right to obtain a size-reducing orthogonal rewrite system \n\n3) Remove all equations involving function symbols of arity > 1 \n\n4) Remove all type side and schema equations \n\n5) disable checking of equations in queries using dont_validate_unsafe=true as an option \n\n6) adding options program_allow_nontermination_unsafe=true \n\n7) emailing support, info@catinf.com ");
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

	
	

	// these Lists will never have dups
	/* private static <Ty, En, Sym, Fk, Att, Gen, Sk> List<List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>>> allArgs(Sym sym, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
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
/*
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
	} */

}
