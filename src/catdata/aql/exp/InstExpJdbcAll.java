package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import catdata.Chc;
import catdata.Ctx;
import catdata.Null;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Eq;
import catdata.aql.ImportAlgebra;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Schema;
import catdata.aql.SqlTypeSide;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;
import catdata.aql.fdm.SaturatedInstance;
import catdata.sql.SqlColumn;
import catdata.sql.SqlForeignKey;
import catdata.sql.SqlInstance;
import catdata.sql.SqlSchema;
import catdata.sql.SqlTable;

public class InstExpJdbcAll extends InstExp<String, String, String, String, String, String, Null<?>, String, Null<?>> {

	private final Map<String, String> options;

	private final String clazz;
	private final String jdbcString;

	@Override
	public Map<String, String> options() {
		return options;
	}

	public InstExpJdbcAll(String clazz, String jdbcString, List<Pair<String, String>> options) {
		this.clazz = clazz;
		this.jdbcString = jdbcString;
		Util.load(clazz);
		this.options = Util.toMapSafely(options);
	}

	public static String sqlTypeToAqlType(String s) {
		String x = s.toLowerCase();
		return x.substring(0, 1).toUpperCase() + x.substring(1, x.length());
	}
	
	private Instance<String, String, String, String, String, String, Null<?>, String, Null<?>> toInstance(AqlEnv env, SqlInstance inst, SqlSchema info) {
		boolean checkJava = !(Boolean) env.defaults.getOrDefault(AqlOption.allow_java_eqs_unsafe);

		TypeSide<String, String> typeSide = new SqlTypeSide(new AqlOptions(options, null, env.defaults));
		//typeSide.validate(true);
		Collage<String, String, String, String, String, Void, Void> col0 = new Collage<>(typeSide.collage());
		Set<Triple<Pair<Var, String>, Term<String, String, String, String, String, Void, Void>, Term<String, String, String, String, String, Void, Void>>> eqs = new HashSet<>();

		for (SqlTable table : info.tables) {
			col0.ens.add(table.name);
			for (SqlColumn c : table.columns) {
				col0.atts.put(c.toString(), new Pair<>(table.name, sqlTypeToAqlType(c.type.name)));
			}
		}

		for (SqlForeignKey fk : info.fks) {
			col0.fks.put(fk.toString(), new Pair<>(fk.source.name, fk.target.name));

			Var v = new Var("x");

			for (SqlColumn tcol : fk.map.keySet()) {
				SqlColumn scol = fk.map.get(tcol);
				String l = scol.toString();
				String r = tcol.toString();
				Term<String, String, String, String, String, Void, Void> lhs = Term.Att(l, Term.Var(v));
				Term<String, String, String, String, String, Void, Void> rhs = Term.Att(r, Term.Fk(fk.toString(), Term.Var(v)));
				eqs.add(new Triple<>(new Pair<>(v, fk.source.name), lhs, rhs));
				col0.eqs.add(new Eq<>(new Ctx<>(new Pair<>(v, Chc.inRight(fk.source.name))), lhs, rhs));
			}
		}

		DP<String, String, String, String, String, Void, Void> dp = AqlProver.create(new AqlOptions(options, col0, env.defaults), col0, typeSide.js);

		Schema<String, String, String, String, String> sch = new Schema<>(typeSide, col0.ens, col0.atts.map, col0.fks.map, eqs, dp, checkJava);

		Ctx<String, Collection<String>> ens0 = new Ctx<>(Util.newSetsFor0(sch.ens));
		Ctx<String, Collection<Null<?>>> tys0 = new Ctx<>();
		Ctx<String, Ctx<String, String>> fks0 = new Ctx<>();
		Ctx<String, Ctx<String, Term<String, Void, String, Void, Void, Void, Null<?>>>> atts0 = new Ctx<>();
		AqlOptions op = new AqlOptions(options, null, env.defaults);
		boolean labelledNulls = (Boolean) op.getOrDefault(AqlOption.labelled_nulls);
		Ctx<Null<?>, Term<String, String, String, String, String, String, Null<?>>> extraRepr = new Ctx<>();

		int cur_count;
		synchronized (InstExpJdbc.counter) {
			cur_count = InstExpJdbc.counter.i;
			InstExpJdbc.counter.i = InstExpJdbc.counter.i + 1;
		}
		for (String ty : sch.typeSide.tys) {
			if (labelledNulls) {
				tys0.put(ty, new HashSet<>());
			} else {
				tys0.put(ty, Util.singSet(new Null<>(ty.toString() + Integer.toString(cur_count))));
			}
		}

		int fr = 0;
		Map<SqlTable, Map<Map<SqlColumn, Optional<Object>>, String>> iso1 = new HashMap<>();
	
		for (SqlTable table : info.tables) {
			Set<Map<SqlColumn, Optional<Object>>> tuples = inst.get(table);

			Map<Map<SqlColumn, Optional<Object>>, String> i1 = new HashMap<>();
			for (Map<SqlColumn, Optional<Object>> tuple : tuples) {
				String i = "v" + (fr++);
				i1.put(tuple, i);
				// i2.put(i, tuple);
				ens0.get(table.name).add(i);
				for (SqlColumn c : table.columns) {
					if (!atts0.containsKey(i)) {
						atts0.put(i, new Ctx<>());
					}
					Optional<Object> val = tuple.get(c);
					Term<String, Void, String, Void, Void, Void, Null<?>> xxx
					 = InstExpJdbc.objectToSk(sch, val.orElse(null), i, c.toString(), labelledNulls, tys0, cur_count, extraRepr, false);
					atts0.get(i).put(c.toString(), xxx);
				}
			}
			iso1.put(table, i1);
			// iso2.put(table, i2);
		}

		for (SqlForeignKey fk : info.fks) {
			for (Map<SqlColumn, Optional<Object>> in : inst.get(fk.source)) {
				Map<SqlColumn, Optional<Object>> out = inst.follow(in, fk);
				String tgen = iso1.get(fk.target).get(out);
				String sgen = iso1.get(fk.source).get(in);
				if (!fks0.containsKey(sgen)) {
					fks0.put(sgen, new Ctx<>());
				}
				fks0.get(sgen).put(fk.toString(), tgen);
			}
		}

		ImportAlgebra<String, String, String, String, String, String, Null<?>> alg = new ImportAlgebra<String,String,String,String,String,String,Null<?>>(sch, ens0, tys0, fks0, atts0, Object::toString, Object::toString);

		return new SaturatedInstance<>(alg, alg, (Boolean) op.getOrDefault(AqlOption.require_consistency), (Boolean) op.getOrDefault(AqlOption.allow_java_eqs_unsafe), labelledNulls, extraRepr);
	}

	

	@Override
	public Instance<String, String, String, String, String, String, Null<?>, String, Null<?>> eval(AqlEnv env) {

		try (Connection conn = DriverManager.getConnection(jdbcString)) {
			SqlSchema sch = new SqlSchema(conn.getMetaData());
			SqlInstance inst = new SqlInstance(sch, conn);
			return toInstance(env, inst, sch);
		} catch (SQLException exn) {
			exn.printStackTrace();
			throw new RuntimeException("JDBC exception: " + exn.getMessage());
		}

	}

	@Override
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = "options" + Util.sep(options, "\n\t\t", " = ");
		}
		return "import_jdbc_all " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " {\n\t" + s + "\n}";
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		return ret;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
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
		InstExpJdbcAll other = (InstExpJdbcAll) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (jdbcString == null) {
			if (other.jdbcString != null)
				return false;
		} else if (!jdbcString.equals(other.jdbcString))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}

	@Override
	public SchExp<String, String, String, String, String> type(AqlTyping G) {
		return new SchExp.SchExpInst<>(this);
	}

}
