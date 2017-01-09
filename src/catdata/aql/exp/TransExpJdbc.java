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
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.fdm.LiteralTransform;

public class TransExpJdbc<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {

	private final InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src;
	private final InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst;

	private final List<String> imports;

	private final Map<String, String> options;

	private final String clazz;
	private final String jdbcString;

	private final Map<String, String> map;
	
	@Override
	public long timeout() {
		return (Long) AqlOptions.getOrDefault(options, AqlOption.timeout);
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

	private void totalityCheck(Schema<Ty, En, Sym, Fk, Att> sch, Map<En, String> ens, Map<Ty, String> tys) {
		for (En En : sch.ens) {
			if (!ens.containsKey(En)) {
				throw new RuntimeException("no query for " + En);
			}
		}
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
	}


	@SuppressWarnings("unchecked")
	private Gen1 objectToGen1(Object gen) {
		return (Gen1) gen;
	}

	@SuppressWarnings("unchecked")
	private Gen2 objectToGen2(Object gen) {
		return (Gen2) gen;
	}

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

	@Override
	public Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src0 = src.eval(env);
		Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst0 = dst.eval(env);
		if (!src0.schema().equals(dst0.schema())) {
			throw new RuntimeException("Schema of instance source is " + src0  + " but schema of target instance is " + dst0);
		}
		Schema<Ty, En, Sym, Fk, Att> sch = src0.schema();
		
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
		
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> v = env.defs.trans.get(k);
			Util.putAllSafely(gens, v.gens().map);
			Util.putAllSafely(sks, v.sks().map);
		}
		
		//TODO aql handling of empty fields
		
		try (Connection conn = DriverManager.getConnection(jdbcString)) {
			
			for (En en : sch.ens) {
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
			for (Ty ty : sch.typeSide.tys) {
				if (!tys.containsKey(ty)) {
					continue;
				}
				Statement stmt = conn.createStatement();
				stmt.execute(tys.get(ty));
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				if (columnsNumber != 2) {
					stmt.close();
					rs.close();
					throw new RuntimeException("Error in " + ty + ": Expected 2 columns but received " + columnsNumber);
				}
				 while (rs.next()) {
					 Object sk = rs.getObject(1);
					 if (sk == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + ty + ": Encountered a NULL labelled null (how ironic) (dom)");
					 }
					 Object rhs = rs.getObject(2);
					 if (rhs == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Error in " + ty + ": Encountered a NULL labelled null (how ironic) (cod)");
					 }
					 Term<Ty,En,Sym,Fk,Att,Gen2,Sk2> rhs0;
					 if (sch.typeSide.js.java_tys.containsKey(ty)) {
						rhs0 = Term.Obj(rhs, ty);
					 } else if (dst0.sks().map.containsKey(rhs)) {
						 rhs0 = Term.Sk(objectToSk2(rhs));
					 } else if (dst0.schema().typeSide.syms.map.containsKey(rhs) && dst0.schema().typeSide.syms.map.get(rhs).first.isEmpty()) {
						 rhs0 = Term.Sym(objectToSym(rhs), Collections.emptyList());
					 } else {
						stmt.close();
						rs.close();
						 throw new RuntimeException("Error in " + ty + ": " + rhs + " is not a java primitive, labelled null, or 0-ary constant symbol");
					 }
					 sks.put(objectToSk1(sk), rhs0);
				}
				stmt.close();
				rs.close();		
			}
		
		} catch (SQLException exn) {
			exn.printStackTrace();
			throw new RuntimeException("JDBC error: " + exn.getMessage());
		}
			
		AqlOptions op = new AqlOptions(options, null);
		Boolean dontValidateEqs = (Boolean) op.getOrDefault(AqlOption.dont_validate_unsafe);
		
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
		AqlOptions op = new AqlOptions(options, null);
		Boolean reload = (Boolean) op.getOrDefault(AqlOption.always_reload);
		if (reload) {
			return false;
		}
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
		if (!s.equals(t)) { //TODO aql schema equality
			throw new RuntimeException("Source instance of transform has schema\n" + s + " \n\n but target instance has schema\n" + t);
		}
		return new Pair<>(src, dst);
	}

}
