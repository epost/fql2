package catdata.aql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;

public class SaturatedInstance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> 
extends Instance<Ty, En, Sym, Fk, Att, X, Y, X, Y>  {

	private final Set<Pair<Term<Ty, En, Sym, Fk, Att, X, Y>, Term<Ty, En, Sym, Fk, Att, X, Y>>> eqs = new HashSet<>();
	private final Ctx<X, En> gens = new Ctx<>();
	private final Ctx<Y, Ty> sks; 
	
	private final DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp;
	private final Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg;
	private final InnerAlgebra inner_alg;
	private final InnerDP inner_dp;

	@Override
	public DP<Ty, En, Sym, Fk, Att, X, Y> dp() {
		return inner_dp;
	}

	@Override
	public Algebra<Ty, En, Sym, Fk, Att, X, Y, X, Y> algebra() {
		return inner_alg;
	}

	public SaturatedInstance(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg, DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp) {
		this.alg = alg;
		this.dp = dp;
		for (En en : schema().ens) {
			for (X x : alg.en(en)) {
				gens.put(x, en);
				for (Att att : schema().attsFrom(en)) {
					Term<Ty, En, Sym, Fk, Att, X, Y> lhs = Term.Att(att, Term.Gen(x));
					Term<Ty, En, Sym, Fk, Att, X, Y> rhs = alg.att(att, x).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
					eqs.add(new Pair<>(lhs, rhs));
				}
				for (Fk fk : schema().fksFrom(en)) {
					Term<Ty, En, Sym, Fk, Att, X, Y> lhs = Term.Fk(fk, Term.Gen(x));
					Term<Ty, En, Sym, Fk, Att, X, Y> rhs = Term.Gen(alg.fk(fk, x));
					eqs.add(new Pair<>(lhs, rhs));
				}
			}
		}
		sks = new Ctx<>(alg.talg().sks.map);
		for (Eq<Ty, Void, Sym, Void, Void, Void, Y> eq : alg.talg().eqs) {
			if (!eq.ctx.isEmpty()) {
				continue;
			}
			eqs.add(new Pair<>(eq.lhs.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity()), eq.rhs.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity())));
		}

		inner_dp = new InnerDP();
		inner_alg = new InnerAlgebra();
	}

	@Override
	public Ctx<X, En> gens() {
		return gens;
	}

	@Override
	public Ctx<Y, Ty> sks() {
		return sks;
	}

	@Override
	public Set<Pair<Term<Ty, En, Sym, Fk, Att, X, Y>, Term<Ty, En, Sym, Fk, Att, X, Y>>> eqs() {
		return eqs;
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return alg.schema();
	}

	private class InnerAlgebra extends Algebra<Ty,En,Sym,Fk,Att,X,Y,X,Y> {
		
		@Override
		public String printX(X x) {
			return alg.printX(x);
		}

		@Override
		public String printY(Y y) {
			return alg.printY(y);
		}
		
		@Override
		public Collection<X> en(En en) {
			return alg.en(en);
		}

		@Override
		public X fk(Fk fk, X x) {
			return alg.fk(fk, x);
		}

		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att att, X x) {
			return alg.att(att, x);
		}

		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Y sk) {
			return Term.Sk(sk);
		}

		@Override
		public X nf(Term<Void, En, Void, Fk, Void, X, Void> term) {
			if (term.gen != null) {
				return term.gen;
			} else if (term.fk != null) {
				return alg.fk(term.fk, nf(term.arg));
			}
			throw new RuntimeException("Anomaly: please report");
		}

		@Override
		public Term<Void, En, Void, Fk, Void, X, Void> repr(X x) {
			return Term.Gen(x); 
		}

		@Override
		public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
			return alg.talg();
		}

		@Override
		public Term<Ty, En, Sym, Fk, Att, X, Y> reprT(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
			return term.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
		}
		
		/*
		private Term<Ty, En, Sym, Fk, Att, X, Y> transR(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
			if (term.obj != null || term.var != null) {
				return term.convert();
			} else if (term.sym != null) {
				return Term.Sym(term.sym, term.args.stream().map(x -> transR(x)).collect(Collectors.toList()));
			} else if (term.att != null) {
				return Term.Att(term.att, transR(term.arg));
			} else if (term.fk != null) {
				return Term.Fk(term.fk, transR(term.arg));
			} else if (term.gen != null) {
				return Term.Gen(outer.nf(term.arg.convert()));
			} else if (term.sk != null) {
				return outer.sk(term.sk).convert();
			}
			throw new RuntimeException("Anomaly: please report");
		}			 */

		@Override
		public Schema<Ty, En, Sym, Fk, Att> schema() {
			return alg.schema();
		}

		@Override
		public String toStringProver() {
			return "Saturated Inner Algebra wrapper of " + alg.toStringProver();
		}

		/*
		@Override
		public DP<Ty, En, Sym, Fk, Att, X, Y> dp() {
			return dp();
		}*/
		
	}
		
	private class InnerDP implements DP<Ty, En, Sym, Fk, Att, X, Y> {
/*
		@Override
		public Collage<Ty, En, Sym, Fk, Att, X, Y> collage() {
			return col;
		}*/

		@Override
		public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, X, Y> lhs, Term<Ty, En, Sym, Fk, Att, X, Y> rhs) {
			return dp.eq(ctx, transL(lhs), transL(rhs));
		}

		@Override
		public boolean hasNFs() {
			return false;
		}

		@Override
		public Term<Ty, En, Sym, Fk, Att, X, Y> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, X, Y> term) {
			throw new RuntimeException("Anomaly: please report");
		}
		
		private Term<Ty, En, Sym, Fk, Att, Gen, Sk> transL(Term<Ty, En, Sym, Fk, Att, X, Y> term) {
			if (term.obj != null) {
				return Term.Obj(term.obj, term.ty); 
			} else if (term.var != null) {
				return Term.Var(term.var);
			} else if (term.sym != null) {
				return Term.Sym(term.sym, term.args.stream().map(x -> transL(x)).collect(Collectors.toList()));
			} else if (term.att != null) {
				return Term.Att(term.att, transL(term.arg));
			} else if (term.fk != null) {
				return Term.Fk(term.fk, transL(term.arg));
			} else if (term.gen != null) {
				return alg.repr(term.gen).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn());
			} else if (term.sk != null) {
				return alg.reprT(Term.Sk(term.sk));
			}
			throw new RuntimeException("Anomaly: please report");
		}
		
	}
		
	
}
