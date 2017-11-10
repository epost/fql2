package catdata.aql.exp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.Term;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;

//TODO AQL CSV version of this
//TODO merge this with coproduct sigma
public final class InstExpCsvQuotient<Gen, Sk, X, Y>
		extends InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> {

	public final InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I;

	public final Map<String, String> options;

	public final List<String> queries;

	@Override
	public Map<String, String> options() {
		return options;
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return I.deps();
	}

	public InstExpCsvQuotient(InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> i,
			List<String> queries, List<Pair<String, String>> options) {
		I = i;
		this.options = Util.toMapSafely(options);
		this.queries = queries;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((I == null) ? 0 : I.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((queries == null) ? 0 : queries.hashCode());
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
		InstExpCsvQuotient<?, ?, ?, ?> other = (InstExpCsvQuotient<?, ?, ?, ?>) obj;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (queries == null) {
			if (other.queries != null)
				return false;
		} else if (!queries.equals(other.queries))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "quotient_csv " + I + " {\n" + Util.sep(queries, "\n") + "\n}";
	}

	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return I.type(G);
	}

	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> J = I.eval(env);
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col = new Collage<>(J.collage());
		AqlOptions strat = new AqlOptions(options, col, env.defaults);

		Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs0 = new HashSet<>(
				J.eqs());

		Map<String, String> map = new HashMap<>();
		for (String q : queries) {
			map.put(q, q);
		}
		try {
			Map<En, List<String[]>> ret = InstExpCsv.start2(map, strat, J.schema(), true);
		
		for (En q : ret.keySet()) {
			for (String[] row : ret.get(q)) {

				if (row.length != 2) {
					throw new RuntimeException("On " + q + ", encountered a row of length != 2: " + Arrays.toString(row));
				}
				Gen gen1 = (Gen) row[0];
				Gen gen2 = (Gen) row[1];
				if (gen1 == null) {
					throw new RuntimeException("Encountered a NULL generator in column 1 of " + q);
				}
				if (gen2 == null) {
					throw new RuntimeException("Encountered a NULL generator in column 2 of " + q);
				}
				if (!J.gens().containsKey(gen1)) {
					throw new RuntimeException(
							"Cannot import record linkage: " + gen1 + " is not a generator in the input instance");
				} else if (!J.gens().containsKey(gen2)) {
					throw new RuntimeException(
							"Cannot import record linkage: " + gen2 + " is not a generator in the input instance");
				}
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> l = Term.Gen(gen1);
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> r = Term.Gen(gen2);
				eqs0.add(new Pair<>(l, r));
				col.eqs.add(new Eq<>(new Ctx<>(), l, r));
			}
		}

		InitialAlgebra<Ty, En, Sym, Fk, Att, Gen, Sk, ID> initial0 = new InitialAlgebra<>(strat, J.schema(), col,
				new It(), Object::toString, Object::toString);

		return new LiteralInstance<>(J.schema(), col.gens.map, col.sks.map, eqs0, initial0.dp(), initial0,
				(Boolean) strat.getOrDefault(AqlOption.require_consistency),
				(Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe));
		
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}

	}

}