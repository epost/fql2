package catdata.aql;

import java.util.Map;
import java.util.Set;

import catdata.Pair;
import catdata.Util;

//TODO rename to literal
public class LiteralInstance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> extends Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> {

	private final Schema<Ty, En, Sym, Fk, Att> schema;

	private final Ctx<Gen, En> gens;
	private final Ctx<Sk, Ty> sks;

	private final Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs;

	private final DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp;

	private final Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg;

	public LiteralInstance(Schema<Ty, En, Sym, Fk, Att> schema, Map<Gen, En> gens, Map<Sk, Ty> sks, Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs, DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp, Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg) {
		Util.assertNotNull(schema, gens, sks, eqs, dp);
		this.schema = schema;
		this.gens = new Ctx<>(gens);
		this.sks = new Ctx<>(sks);
		this.eqs = eqs;
		this.dp = dp;
		this.alg = alg;
		validate(); // TODO: aql validate algebra against instance

	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return schema;
	}

	@Override
	public Ctx<Gen, En> gens() {
		return gens;
	}

	@Override
	public Ctx<Sk, Ty> sks() {
		return sks;
	}

	@Override
	public Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs() {
		return eqs;
	}

	@Override
	public DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp() {
		return dp;
	}

	@Override
	public Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> algebra() {
		return alg;
	}

}
