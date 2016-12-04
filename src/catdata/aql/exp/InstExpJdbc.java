package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;

public class InstExpJdbc<Ty,En,Sym,Fk,Att,Gen,Sk> 
	extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,ID,Chc<Sk,Pair<ID,Att>>> {

	public final SchExp<Ty,En,Sym,Fk,Att> schema;
	
	public final List<String> imports;

	public final Map<String, String> options;
	
	public final String clazz;
	public final String jdbcString;
	
	public final Map<String, String> map;
	
	@Override
	public long timeout() {
		return (Long) AqlOptions.getOrDefault(options, AqlOption.timeout);
	}	

	public InstExpJdbc(SchExp<Ty, En, Sym, Fk, Att> schema, List<String> imports, List<Pair<String, String>> options, String clazz, String jdbcString, List<Pair<String, String>> map) {
		this.schema = schema;
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
	
	
	public void totalityCheck(Schema<Ty,En,Sym,Fk,Att> sch, Map<En, String> ens, Map<Ty, String> tys, Map<Att, String> atts, Map<Fk, String> fks) {
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
		for (Att Att : sch.atts.keySet()) {
			if (!atts.containsKey(Att)) {
				throw new RuntimeException("no query for attribute " + Att);
			}
		}
		for (Att Att : atts.keySet()) {
			if (!sch.atts.containsKey(Att)) {
				throw new RuntimeException("there is a query for " + Att + ", which is not an attribute in the schema");
			}
		}
		for (Fk Fk : sch.fks.keySet()) {
			if (!fks.containsKey(Fk)) {
				throw new RuntimeException("no query for foreign key " + Fk);
			}
		}
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
		return (Gen) gen;
	}
	@SuppressWarnings("unchecked")
	private Sk objectToSk(Object s) {
		return (Sk) s;
	}
	@SuppressWarnings("unchecked")
	private Sym objectToSym(Object s) {
		return (Sym) s;
	}
	
	//TODO aql csv import still broken re: quoting
	
	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> sch = schema.eval(env);
		
		Map<En, String> ens = new HashMap<>();
		Map<Ty, String> tys = new HashMap<>();
		Map<Att, String> atts = new HashMap<>();
		Map<Fk, String> fks = new HashMap<>();
		
		for (String o : map.keySet()) {
			assertUnambig(o, sch);
			String q = map.get(o);
			
			if (sch.typeSide.tys.contains(o)) {
				tys.put(stringToTy(o), q);
			} else if (sch.ens.contains(o)) {
				ens.put(stringToEn(o), q);
			} else if (sch.atts.map.containsKey(o)) {
				atts.put(stringToAtt(o), q);
			} else if (sch.fks.map.containsKey(o)) {
				fks.put(stringToFk(o), q);
			}
		}
		totalityCheck(sch, ens, tys, atts, fks);
		
		Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col = new Collage<>(sch.collage());
		Set<Pair<Term<Ty,En,Sym,Fk,Att,Gen,Sk>, Term<Ty,En,Sym,Fk,Att,Gen,Sk>>> eqs0 = new HashSet<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Instance<Ty,En,Sym,Fk,Att,Gen,Sk,?,?> v = env.defs.insts.get(k);
			eqs0.addAll(v.eqs());
			col.gens.putAll(v.gens().map);
			col.sks.putAll(v.sks().map);
		}
		
		//TODO aql handling of empty fields
		
		Map<En, ResultSet> ens0 = new HashMap<>();
		Map<Ty, ResultSet> tys0 = new HashMap<>();
		Map<Att, ResultSet> atts0 = new HashMap<>();
		Map<Fk, ResultSet> fks0 = new HashMap<>();
	
		try {
			Connection conn = DriverManager.getConnection(jdbcString);
			
			for (En en : sch.ens) {
				Statement stmt = conn.createStatement();
				stmt.execute(ens.get(en));
				ens0.put(en, stmt.getResultSet()); 
			}
			for (Ty ty : sch.typeSide.tys) {
				if (!tys.containsKey(ty)) {
					continue;
				}
				Statement stmt = conn.createStatement();
				stmt.execute(tys.get(ty));
				tys0.put(ty, stmt.getResultSet()); 
			}
			for (Fk fk : sch.fks.keySet()) {
				Statement stmt = conn.createStatement();
				stmt.execute(fks.get(fk));
				fks0.put(fk, stmt.getResultSet()); 	
			}
			for (Att att : sch.atts.keySet()) {
				Statement stmt = conn.createStatement();
				stmt.execute(atts.get(att));
				atts0.put(att, stmt.getResultSet()); 
			}
		
			for (En en : sch.ens) {
				try {
					ResultSet rs = ens0.get(en);
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnsNumber = rsmd.getColumnCount();
					if (columnsNumber != 1) {
						throw new RuntimeException("Expected 1 column but received " + columnsNumber);
					}
					 while (rs.next()) {
						 Object gen = rs.getObject(1);
						 if (gen == null) {
							 throw new RuntimeException("Encountered a NULL generator");
						 }
						 col.gens.put(objectToGen(gen), en);
					}
				} catch (Throwable thr) {
					throw new RuntimeException("Error in " + en + ": " + thr.getMessage());
				}
			}
			for (Ty ty : sch.typeSide.tys) {
				if (!tys.containsKey(ty)) {
					continue;
				}
				try	{
					ResultSet rs = tys0.get(ty);
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnsNumber = rsmd.getColumnCount();
					if (columnsNumber != 1) {
						throw new RuntimeException("Expected 1 column but received " + columnsNumber);
					}
					 while (rs.next()) {
						 Object sk = rs.getObject(1);
						 if (sk == null) {
							 throw new RuntimeException("Encountered a NULL labelled null (how ironic)");
						 }
						 col.sks.put(objectToSk(sk), ty);
					}
				} catch (Throwable thr) {
					throw new RuntimeException("Error in " + ty + ": " + thr.getMessage());
				}
			}
			for (Fk fk : sch.fks.keySet()) {
				try {
					ResultSet rs = fks0.get(fk);
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnsNumber = rsmd.getColumnCount();
					if (columnsNumber != 2) {
						throw new RuntimeException("Expected 2 columns but received " + columnsNumber);
					}
					 while (rs.next()) {
						 Object lhs = rs.getObject(1);
						 if (lhs == null) {
							 throw new RuntimeException("Encountered a NULL generator)");
						 }
						 Object rhs = rs.getObject(2);
						 if (rhs == null) {
							 continue; 
						 }
						 eqs0.add(new Pair<>(Term.Fk(fk, Term.Gen(objectToGen(lhs))), Term.Gen(objectToGen(rhs))));
					}
				} catch (Throwable thr) {
					throw new RuntimeException("Error in " + fk + ": " + thr.getMessage());
				}
			}
			for (Att att : sch.atts.keySet()) {
				try {
					ResultSet rs = atts0.get(att);
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnsNumber = rsmd.getColumnCount();
					if (columnsNumber != 2) {
						throw new RuntimeException("Expected 2 columns but received " + columnsNumber);
					}
					Ty ty = sch.atts.get(att).second;
					while (rs.next()) {
						 Object lhs = rs.getObject(1);
						 if (lhs == null) {
							 throw new RuntimeException("Encountered a NULL generator)");
						 }
						 Object rhs = rs.getObject(2);
						 if (rhs == null) {
							 continue; 
						 }
						 Term<Ty,En,Sym,Fk,Att,Gen,Sk> rhs0 = null;
						 if (sch.typeSide.js.java_tys.containsKey(ty)) {
							rhs0 = Term.Obj(rhs, ty);
						 } else if (col.sks.map.containsKey(rhs)) {
							 rhs0 = Term.Sk(objectToSk(rhs));
						 } else if (col.syms.map.containsKey(rhs) && col.syms.map.get(rhs).first.isEmpty()) {
							 rhs0 = Term.Sym(objectToSym(rhs), Collections.emptyList());
						 } else {
							 throw new RuntimeException(rhs + " is not a java primitive, labelled null, or 0-ary constant symbol");
						 }
						 eqs0.add(new Pair<>(Term.Att(att, Term.Gen(objectToGen(lhs))), rhs0));
					}
				} catch (Throwable thr) {
					throw new RuntimeException("Error in " + att + ": " + thr.getMessage());
				}
			}
				
			for (Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : eqs0) {
				col.eqs.add(new Eq<>(new Ctx<>(), eq.first, eq.second));
			}
			
		} catch (Throwable exn) {
			exn.printStackTrace();
			throw new RuntimeException("Error in underlying JDBC data: " + exn.getMessage());
		}
		
		AqlOptions strat = new AqlOptions(options, col);
		
		InitialAlgebra<Ty,En,Sym,Fk,Att,Gen,Sk,ID> 
		initial = new InitialAlgebra<>(strat, sch, col, new It(), x -> x.toString(), x -> x.toString());
				 
		return new LiteralInstance<>(sch, col.gens.map, col.sks.map, eqs0, initial.dp(), initial); 
		//TODO aql switch to saturated prover for jdbc
	}
	
	@SuppressWarnings("unchecked")
	private Fk stringToFk(String o) {
		return (Fk) o;
	}

	@SuppressWarnings("unchecked")
	private Att stringToAtt(String o) {
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


	private void assertUnambig(Object o, Schema<Ty,En,Sym,Fk,Att> sch) {
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
			throw new RuntimeException(o + " is ambiguously a type/entity/attribute/foreign key");
		} else if (i == 0) {
			throw new RuntimeException(o + " is not a type/entity/attribute/foreign key");
		}
	}

	@Override
	public String toString() {
		return "import_jdbc " + schema + " " + clazz + " " + jdbcString + "\n\n" + Util.sep(map, " -> ", "\n\t");
	}


	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(schema.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.INSTANCE)).collect(Collectors.toList()));
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
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
		InstExpJdbc<?, ?, ?, ?, ?, ?, ?> other = (InstExpJdbc<?, ?, ?, ?, ?, ?, ?>) obj;
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
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

}

