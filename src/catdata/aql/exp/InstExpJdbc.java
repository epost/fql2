package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Ctx;
import catdata.IntRef;
import catdata.Null;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.ImportAlgebra;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.fdm.SaturatedInstance;

public class InstExpJdbc<Ty, En, Sym, Fk, Att, Gen> extends InstExp<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>> {

	private final SchExp<Ty, En, Sym, Fk, Att> schema;

	// private final List<String> imports;

	private final Map<String, String> options;

	private final String clazz;
	private final String jdbcString;

	private final Map<String, String> map;

	@Override
	public Map<String, String> options() {
		return options;
	}
	
	public static IntRef counter = new IntRef(0); 
	private final int cur_count;

	public InstExpJdbc(SchExp<Ty, En, Sym, Fk, Att> schema,
			/* List<String> imports, */ List<Pair<String, String>> options, String clazz, String jdbcString, List<Pair<String, String>> map) {
		this.schema = schema;
		// this.imports = imports;
		this.clazz = clazz;
		this.jdbcString = jdbcString;
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.options = Util.toMapSafely(options);
		this.map = Util.toMapSafely(map);
		synchronized (counter) {
			cur_count = counter.i;
			counter.i = counter.i + 1;
		}
	}

	private void totalityCheck(Schema<Ty, En, Sym, Fk, Att> sch, Map<En, String> ens, Map<Ty, String> tys, Map<Att, String> atts, Map<Fk, String> fks) {
		// for (En En : sch.ens) {
		// if (!ens.containsKey(En)) {
		// throw new RuntimeException("no query for " + En);
		// }
		// }
		for (En En : ens.keySet()) {
			if (!sch.ens.contains(En)) {
				throw new RuntimeException("there is a query for " + En + ", which is not an entity in the schema");
			}
		}
		for (Ty ty : tys.keySet()) {
			if (!sch.ens.contains(ty)) {
				throw new RuntimeException("there is a query for " + ty + ", which is not a type in the schema");
			}
		}
		// for (Att Att : sch.atts.keySet()) {
		// if (!atts.containsKey(Att)) {
		// throw new RuntimeException("no query for attribute " + Att);
		// }
		// }
		for (Att Att : atts.keySet()) {
			if (!sch.atts.containsKey(Att)) {
				throw new RuntimeException("there is a query for " + Att + ", which is not an attribute in the schema");
			}
		}
		// for (Fk Fk : sch.fks.keySet()) {
		// if (!fks.containsKey(Fk)) {
		// throw new RuntimeException("no query for foreign key " + Fk);
		// }
		// }
		for (Fk Fk : fks.keySet()) {
			if (!sch.fks.containsKey(Fk)) {
				throw new RuntimeException("there is a query for " + Fk + ", which is not a foreign key in the schema");
			}
		}
	}

	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return schema;
	}

	@SuppressWarnings("unchecked")
	private Gen objectToGen(Object gen) {
		return (Gen) gen.toString();
	}

	// @SuppressWarnings("unchecked")
	public static <Ty,Sym,En,Fk,Att,Gen> Term<Ty, Void, Sym, Void, Void, Void, Null<?>> objectToSk(Schema<Ty, En, Sym, Fk, Att> sch, Object rhs, Gen x, Att att, boolean labelledNulls, Ctx<Ty, Collection<Null<?>>> sks, int cur_count, Ctx<Null<?>, Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>> extraRepr, boolean shouldJS) {
		Ty ty = sch.atts.get(att).second;
		if (rhs == null) {
			Null<?> n = labelledNulls ? new Null<>(Term.Att(att, Term.Gen(x))) : new Null<>(ty.toString() + Integer.toString(cur_count));			
			if (labelledNulls) {
				extraRepr.put(n, Term.Att(att, Term.Gen(x)));
			}
			sks.get(ty).add(n);
			return Term.Sk(n);
		} else if (sch.typeSide.js.java_tys.containsKey(ty)) {
			if (shouldJS) {
				return Term.Obj(sch.typeSide.js.parse(ty, (String) rhs), ty);
			} 
			return Term.Obj(rhs, ty);
		} else if (sch.typeSide.syms.containsKey(objectToSym(rhs)) && sch.typeSide.syms.get(objectToSym(rhs)).first.isEmpty()) {
			return Term.Sym(objectToSym(rhs), Collections.emptyList());
		}
		return Util.anomaly();
	}

	@SuppressWarnings("unchecked")
	private static <Sym2> Sym2 objectToSym(Object s) {
		return (Sym2) s;
	}

	// TODO aql csv import still broken re: quoting

	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> sch = schema.eval(env);
		for (Ty ty : sch.typeSide.tys) {
			if (!sch.typeSide.js.java_tys.containsKey(ty)) {
				throw new RuntimeException("Import is only allowed onto java types");
			}
		}


		Map<En, String> ens = new HashMap<>();
		Map<Ty, String> tys = new HashMap<>();
		Map<Att, String> atts = new HashMap<>();
		Map<Fk, String> fks = new HashMap<>();

		for (String o : map.keySet()) {
			assertUnambig(o, sch);
			String q = map.get(o);

			if (sch.typeSide.tys.contains(stringToTy(o))) {
				tys.put(stringToTy(o), q);
			} else if (sch.ens.contains(o)) {
				ens.put(stringToEn(o), q);
			} else if (sch.atts.containsKey(stringToAtt(o))) {
				atts.put(stringToAtt(o), q);
			} else if (sch.fks.containsKey(stringToFk(o))) {
				fks.put(stringToFk(o), q);
			}
		}
		totalityCheck(sch, ens, tys, atts, fks);

		Ctx<En, Collection<Gen>> ens0 = new Ctx<>(Util.newSetsFor0(sch.ens));
		Ctx<Ty, Collection<Null<?>>> tys0 = new Ctx<>();
		Ctx<Gen, Ctx<Fk, Gen>> fks0 = new Ctx<>();
		Ctx<Gen, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Null<?>>>> atts0 = new Ctx<>();
		AqlOptions op = new AqlOptions(options, null, env.defaults);
		Ctx<Null<?>, Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>> extraRepr = new Ctx<>();

		
		boolean labelledNulls = (boolean) op.getOrDefault(AqlOption.labelled_nulls);

		for (Ty ty : sch.typeSide.tys) {
			if (labelledNulls) {
				tys0.put(ty, new HashSet<>());
			} else {
				tys0.put(ty, Util.singSet(new Null<>(ty.toString() + Integer.toString(cur_count))));
			}
		}

		try (Connection conn = DriverManager.getConnection(jdbcString)) {
			for (En en : ens.keySet()) {
				Statement stmt = conn.createStatement();
				stmt.execute(ens.get(en));
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				if (columnsNumber != 1) {
					stmt.close();
					rs.close();
					throw new RuntimeException("Expected 1 column but received " + columnsNumber);
				}
				while (rs.next()) {
					Object gen = rs.getObject(1);
					if (gen == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Encountered a NULL generator");
					}
					ens0.get(en).add(objectToGen(gen));
				}
				stmt.close();
				rs.close();
			}

			for (Fk fk : fks.keySet()) {
				Statement stmt = conn.createStatement();
				stmt.execute(fks.get(fk));
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				if (columnsNumber != 2) {
					stmt.close();
					rs.close();
					throw new RuntimeException("Error in " + fk + ": Expected 2 columns but received " + columnsNumber);
				}
				while (rs.next()) {
					Object lhs = rs.getObject(1);
					if (lhs == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + fk + ": Encountered a NULL generator");
					}
					Object rhs = rs.getObject(2);
					if (rhs == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + fk + ": Encountered a NULL foreign key");
					}
					if (!fks0.containsKey(objectToGen(lhs))) {
						fks0.put(objectToGen(lhs), new Ctx<>());
					}
					fks0.get(objectToGen(lhs)).put(fk, objectToGen(rhs));
				}
				stmt.close();
				rs.close();
			}
			for (Att att : atts.keySet()) {
				Statement stmt = conn.createStatement();
				stmt.execute(atts.get(att));
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				if (columnsNumber != 2) {
					stmt.close();
					rs.close();
					throw new RuntimeException("Error in " + att + ": Expected 2 columns but received " + columnsNumber);
				}
				//Ty ty = sch.atts.get(att).second;
				while (rs.next()) {
					Object lhs = rs.getObject(1);
					if (lhs == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + att + ": Encountered a NULL generator)");
					}
					Object rhs = rs.getObject(2);
					if (!atts0.containsKey(objectToGen(lhs))) {
						atts0.put(objectToGen(lhs), new Ctx<>());
					}
					atts0.get(objectToGen(lhs)).put(att, objectToSk(sch, rhs, objectToGen(lhs), att, labelledNulls, tys0, cur_count, extraRepr, false));
				}
				stmt.close();
				rs.close();
			}

		} catch (SQLException exn) {
			exn.printStackTrace();
			throw new RuntimeException("JDBC exception: " + exn.getMessage());
		} catch (Throwable thr) {
			thr.printStackTrace();
			throw new RuntimeException(thr.getMessage() + "\n\n" + helpStr);
		}

		ImportAlgebra<Ty, En, Sym, Fk, Att, Gen, Null<?>> alg = new ImportAlgebra<>(sch, ens0, tys0, fks0, atts0, Object::toString, Object::toString);

		// TODO aql validate for collage
		// AqlOptions strat = new AqlOptions(options, col);

		return new SaturatedInstance<>(alg, alg, (Boolean) op.getOrDefault(AqlOption.require_consistency), (Boolean) op.getOrDefault(AqlOption.allow_java_eqs_unsafe), labelledNulls, extraRepr);

	}

	private static final String helpStr = "Possible problem: AQL IDs be unique among all entities and types; it is not possible to have, for example," + "\n" + "\n	0:Employee" + "\n	0:Department" + "\n" + "\nPossible solution: Distinguish the IDs prior to import, or distinguish them during import, for example, " + "\n" + "\n	instance J = import_jdbc ... {"
			+ "\n		Employee -> \"SELECT concat(\"emp\",id) FROM Employee\"" + "\n		Department -> \"SELECT concat(\"dept\",id) FROM Dept\"" + "\n		worksIn -> \"SELECT concat(\"emp\",id), concat(\"dept\",worksIn) FROM Employee\"" + "\n	}" + "\n";

	@SuppressWarnings("unchecked")
	private Fk stringToFk(String o) {
		return (Fk) o;
	}

	@SuppressWarnings("unchecked")
	private Fk objectToFk(Object o) {
		return (Fk) o;
	}

	@SuppressWarnings("unchecked")
	private En objectToEn(Object o) {
		return (En) o;
	}

	@SuppressWarnings("unchecked")
	private Att stringToAtt(String o) {
		return (Att) o;
	}

	@SuppressWarnings("unchecked")
	private Att objectToAtt(Object o) {
		return (Att) o;
	}

	@SuppressWarnings("unchecked")
	private En stringToEn(String o) {
		return (En) o;
	}

	@SuppressWarnings("unchecked")
	private Ty stringToTy(String o) {
		return (Ty) o;
	}

	@SuppressWarnings("unchecked")
	private Ty objectToTy(Object o) {
		return (Ty) o;
	}

	private void assertUnambig(Object o, Schema<Ty, En, Sym, Fk, Att> sch) {
		int i = 0;
		if (sch.typeSide.tys.contains(objectToTy(o))) {
			i++;
		}
		if (sch.ens.contains(objectToEn(o))) {
			i++;
		}
		if (sch.atts.containsKey(objectToAtt(o))) {
			i++;
		}
		if (sch.fks.map.containsKey(objectToFk(o))) {
			i++;
		}
		if (i > 1) {
			throw new RuntimeException(o + " is ambiguously a type/entity/attribute/foreign key");
		} else if (i == 0) {
			throw new RuntimeException(o + " is not a type/entity/attribute/foreign key");
		}
	}

	@Override
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = "options" + Util.sep(options, "\n\t\t", " = ");
		}
		return "import_jdbc " + schema + " " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " {\n\t" + Util.sep(map, " -> ", "\n\t") + s + "\n}";
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(schema.deps());
		// ret.addAll(imports.stream().map(x -> new Pair<>(x,
		// Kind.INSTANCE)).collect(Collectors.toList()));
		return ret;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		// result = prime * result + ((imports == null) ? 0 :
		// imports.hashCode());
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
		InstExpJdbc<?, ?, ?, ?, ?, ?> other = (InstExpJdbc<?, ?, ?, ?, ?, ?>) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
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
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

}
