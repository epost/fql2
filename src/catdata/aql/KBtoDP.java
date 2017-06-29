package catdata.aql;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.provers.DPKB;

/*	private static <Sk, En, Fk, Ty, Att, Sym, Gen> DP<Ty, En, Sym, Fk, Att, Gen, Sk> 
 * wrap(Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp, 
 * DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb) {

		return
	}*/
public class KBtoDP<Ty, En, Sym, Fk, Att, Gen, Sk>
 implements DP<Ty, En, Sym, Fk, Att, Gen, Sk> {

	private final Map<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>, Boolean> cache = new HashMap<>();

	private final Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp;
	
	private final DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb;
	
	private final AqlJs<Ty, Sym> js;
	
	public KBtoDP(AqlJs<Ty, Sym> js,
			Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp,
			  DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb) {
		this.simp = simp;
		this.dpkb = dpkb;
		this.js = js;
	}
	
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
		b = dpkb.eq(ctx.map, js.reduce(simp.apply(lhs)).toKB(), js.reduce(simp.apply(rhs)).toKB());
		cache.put(eq, b);
		return b;
	}

	@Override
	public boolean hasNFs() {
		return dpkb.hasNFs();
	}

	@Override
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		return Term.fromKB(dpkb.nf(ctx.map, js.reduce(simp.apply(term)).toKB()));
	}

	@Override
	public String toStringProver() {
		return "Caching definitional simplification and reflexivity wrapping plus java of\n\n" + dpkb;
	}

};