package catdata.aql;

import catdata.Chc;
import catdata.Ctx;
import catdata.Triple;
import catdata.Util;

public class Eq<Ty, En, Sym, Fk, Att, Gen, Sk> {

	public final Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, rhs;
	public final Ctx<Var, Chc<Ty, En>> ctx;
	
	public Eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
		Util.assertNotNull(lhs, rhs, ctx);
		this.lhs = lhs;
		this.rhs = rhs;
		this.ctx = ctx;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((ctx == null) ? 0 : ctx.hashCode());
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Eq<?,?,?,?,?,?,?> other = (Eq<?,?,?,?,?,?,?>) obj;
		if (ctx == null) {
			if (other.ctx != null)
				return false;
		} else if (!ctx.equals(other.ctx))
			return false;
		if (lhs == null) {
			if (other.lhs != null)
				return false;
		} else if (!lhs.equals(other.lhs))
			return false;
		if (rhs == null) {
			if (other.rhs != null)
				return false;
		} else if (!rhs.equals(other.rhs))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ctx.toString(lhs, rhs);
	}
	
	public final Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> toTriple() {
		return new Triple<>(ctx, lhs, rhs);
	}
	
}
