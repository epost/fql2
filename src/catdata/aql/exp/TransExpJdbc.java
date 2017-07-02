package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.ImportAlgebra;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.fdm.LiteralTransform;
import catdata.aql.fdm.SaturatedInstance;

public class TransExpJdbc<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {

	private final InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src;
	private final InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst;

	private final List<String> imports;

	private final Map<String, String> options;

	private final String clazz;
	private final String jdbcString;

	private final Map<String, String> map;		
	
	@Override
	public Map<String, String> options() {
		return options;
	}

	public TransExpJdbc(InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst, List<String> imports, List<Pair<String, String>> options, String clazz, String jdbcString, List<Pair<String, String>> map) {
		this.src = src;
		this.dst = dst;
		this.imports = imports;
		this.clazz = clazz;
		this.jdbcString = jdbcString;
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.options = Util.toMapSafely(options);
		this.map = Util.toMapSafely(map);
	}

	private void totalityCheck(Schema<Ty, En, Sym, Fk, Att> sch, Map<En, String> ens, @SuppressWarnings("unused") Map<Ty, String> tys) {
//		for (En En : sch.ens) {
//			if (!ens.containsKey(En)) {
//				throw new RuntimeException("no query for " + En);
//			}
//		}
		for (En En : ens.keySet()) {
			if (!sch.ens.contains(En)) {
				throw new RuntimeException("there is a query for " + En + ", which is not an entity in the schema");
			}
		}
//		for (Ty ty : tys.keySet()) {
//			if (!sch.ens.contains(ty)) {
//				throw new RuntimeException("there is a query for " + ty + ", which is not a type in the schema");
//			}
//		}
	}


	@SuppressWarnings("unchecked")
	private Gen1 objectToGen1(Object gen) {
		return (Gen1) gen.toString();
	}

	@SuppressWarnings("unchecked")
	private Gen2 objectToGen2(Object gen) {
		return (Gen2) gen.toString();
	}
/*
	@SuppressWarnings("unchecked")
	private Sk1 objectToSk1(Object s) {
		return (Sk1) s;
	}

	@SuppressWarnings("unchecked")
	private Sk2 objectToSk2(Object s) {
		return (Sk2) s;
	}

	@SuppressWarnings("unchecked")
	private Sym objectToSym(Object s) {
		return (Sym) s;
	}
*/
	
	private static boolean cameFromImport(Instance<?, ?, ?, ?, ?, ?, ?, ?, ?> I) {
		if (!(I instanceof SaturatedInstance)) {
			return false;
		}
		SaturatedInstance<?, ?, ?, ?, ?, ?, ?, ?, ?> J = (SaturatedInstance<?, ?, ?, ?, ?, ?, ?, ?, ?>) I;
		return J.alg instanceof ImportAlgebra;
	}
	
	@Override
	public Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src0 = src.eval(env);
		Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst0 = dst.eval(env);
		if (!src0.schema().equals(dst0.schema())) {
			throw new RuntimeException("Schema of instance source is " + src0  + " but schema of target instance is " + dst0);
		}
		Schema<Ty, En, Sym, Fk, Att> sch = src0.schema();
		if (!(cameFromImport(src0) || !(cameFromImport(dst0)))) {
			throw new RuntimeException("Can only import JDBC transforms between JDBC instances");
		}
		Map<En, String> ens = new HashMap<>();
		Map<Ty, String> tys = new HashMap<>();
		
		for (String o : map.keySet()) {
			assertUnambig(o, sch);
			String q = map.get(o);
			if (sch.typeSide.tys.contains(o)) {
				tys.put(stringToTy(o), q);
			} else if (sch.ens.contains(o)) {
				ens.put(stringToEn(o), q);
			} 
		}
		totalityCheck(sch, ens, tys);
		
		Map<Gen1, Term<Void, En, Void, Fk, Void, Gen2, Void>> gens = new HashMap<>();
		Map<Sk1, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> sks = new HashMap<>();
		
		AqlOptions op = new AqlOptions(options, null, env.defaults);
		Boolean dontValidateEqs = (Boolean) op.getOrDefault(AqlOption.dont_validate_unsafe);
		boolean labelledNulls = (Boolean) op.getOrDefault(AqlOption.labelled_nulls);
		
		if (!labelledNulls) {
			for (Sk1 sk : src0.sks().keySet()) {
				Ty ty = src0.sks().get(sk);
				Sk2 sk2 = Util.get0(Util.revS(dst0.sks().map).get(ty));
				sks.put(sk, Term.Sk(sk2)); //map Null@Ty to Null@Ty
			}
		} 
	
		try (Connection conn = DriverManager.getConnection(jdbcString)) {
			
			for (En en : ens.keySet()) {
				Statement stmt = conn.createStatement();
				stmt.execute(ens.get(en));
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				if (columnsNumber != 2) {
					conn.close();
					stmt.close();
					rs.close();
					throw new RuntimeException("Error in " + en + ": Expected 2 columns but received " + columnsNumber);
				}
				while (rs.next()) {
					 Object gen = rs.getObject(1);
					 if (gen == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + en + ": Encountered a NULL generator (dom)");
					 }
					 Object gen2 = rs.getObject(2);
					 if (gen2 == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + en + ": Encountered a NULL generator (cod)");
					 }						 
					 gens.put(objectToGen1(gen), Term.Gen(objectToGen2(gen2)));
				}
				stmt.close();
				rs.close();
			}
			
		
		} catch (SQLException exn) {
			exn.printStackTrace();
			throw new RuntimeException("JDBC error: " + exn.getMessage());
		}
			
		
		return new LiteralTransform<>(gens, sks, src0, dst0, dontValidateEqs); 
	}

	@SuppressWarnings("unchecked")
	private En stringToEn(String o) {
		return (En) o;
	}

	@SuppressWarnings("unchecked")
	private Ty stringToTy(String o) {
		return (Ty) o;
	}

	private void assertUnambig(Object o, Schema<Ty, En, Sym, Fk, Att> sch) {
		int i = 0;
		if (sch.typeSide.tys.contains(o)) {
			i++;
		}
		if (sch.ens.contains(o)) {
			i++;
		}
		if (sch.atts.map.containsKey(o)) {
			i++;
		}
		if (sch.fks.map.containsKey(o)) {
			i++;
		}
		if (i > 1) {
			throw new RuntimeException(o + " is ambiguously a type/entity");
		} else if (i == 0) {
			throw new RuntimeException(o + " is not a type/entity");
		}
	}

	@Override
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = "options" + Util.sep(options, "\n\t\t", " = ");
		}
		return "import_jdbc " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " {\n\t" + Util.sep(map, " -> ", "\n\t") + s + "\n}";
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.TRANSFORM)).collect(Collectors.toList()));
		return ret;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());

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
		TransExpJdbc<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpJdbc<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
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
		if (imports == null) {
			if (other.imports != null)
				return false;
		} else if (!imports.equals(other.imports))
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
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		return true;
	}
	
	@Override
	public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> type(AqlTyping G) {
		SchExp<Ty, En, Sym, Fk, Att> s = src.type(G);
		SchExp<Ty, En, Sym, Fk, Att> t = dst.type(G);
		if (!G.eq(s, t)) { //TODO aql schema equality
			throw new RuntimeException("Source instance of transform has schema\n" + s + " \n\n but target instance has schema\n" + t);
		}
		return new Pair<>(src, dst);
	}

}
