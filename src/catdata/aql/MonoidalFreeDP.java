package catdata.aql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.provers.DPKB;
import catdata.provers.KBTheory;
import catdata.provers.MonoidalProver;

public class MonoidalFreeDP<Ty, En, Sym, Fk, Att, Gen, Sk> implements DP<Ty, En, Sym, Fk, Att, Gen, Sk> {

	private final Map<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>, Boolean> cache = new HashMap<>();

	private final Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp;
	
	private final Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col;
	
	private final DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb;
	
	private final AqlJs<Ty, Sym> js;
	
	public MonoidalFreeDP(AqlJs<Ty, Sym> js, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp,
			Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		this.simp = simp;
		this.col = col;
		this.js = js;
		//TODO aql dont verify unsafe
		if (!ok(col)) {
			throw new RuntimeException("Not monoidal: " + col);
		}
		KBTheory<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> th = col.toKB();
		Iterator<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> it = th.syms.keySet().iterator();
		while (it.hasNext()) {
			Head<Ty, En, Sym, Fk, Att, Gen, Sk> h = it.next();
			if (th.syms.get(h).first.size() > 1) {
				it.remove();
			}
		}
		dpkb = new MonoidalProver<>(th);
	}
	
	//no equations involving symbols with arity > 2
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> boolean ok(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : col.eqs) {
			if (!ok(eq.lhs) || !ok(eq.rhs)) {
				return false;
			} 
		}
		return true;
	}
	
	public boolean eq0(Map<Var,Chc<Ty,En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
		if (lhs.args().size() > 1 && rhs.args().size() > 1) {
			if (!lhs.sym.equals(rhs.sym)) {
				return false; 
			}
			for (int i = 0; i < col.syms.get(lhs.sym).first.size(); i++) {
				if (!eq0(ctx, lhs.args.get(i), rhs.args.get(i))) {
					return false;
				}
			}
			return true;
		} else if (lhs.args().size() <= 1 && rhs.args().size() <= 1) {
			return dpkb.eq(ctx, lhs.toKB(), rhs.toKB());
		}
		return false;
	}
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> boolean ok(Term<Ty, En, Sym, Fk, Att, Gen, Sk> e) {
		if (e.var != null || e.gen != null || e.sk != null || e.obj != null || (e.sym != null && e.args().size() == 0)) {
			return true;
		} else if (e.args().size() > 1) {
			return false;
		} 
		return ok(e.args().get(0));
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
		b = eq0(ctx.map, js.reduce(simp.apply(lhs)), js.reduce(simp.apply(rhs)));
		cache.put(eq, b);
		return b;
	}

	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		return Term.fromKB(dpkb.nf(ctx.map, js.reduce(simp.apply(term)).toKB()));
	}

	@Override
	public String toStringProver() {
		return "Caching definitional simplification and reflexivity wrapping of free and monoidal plus java\n " + dpkb;
	}

};