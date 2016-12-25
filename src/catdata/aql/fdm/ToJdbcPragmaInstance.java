package catdata.aql.fdm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Instance;
import catdata.aql.Pragma;
import catdata.aql.Term;

public class ToJdbcPragmaInstance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> extends Pragma {

	private final String jdbcString;
	private final String prefix;
	private final String clazz;
	private final String idCol;

	@SuppressWarnings("unused")
	private final Map<String, String> options;

	private final Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I;
	
	private final String colTy;
	private final int colTy0;

	//TODO aql have pragma for tojdbc inst print queries
	//TODO aql multi-line quoting doesn't colorize correctly
	//TODO aql column type mapping for jdbc instance export
	public ToJdbcPragmaInstance(String prefix, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I, String clazz, String jdbcString, Map<String, String> options) {
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.jdbcString = jdbcString;
		this.prefix = prefix;
		this.I = I;
		this.options = options;
		this.clazz = clazz;
		idCol = (String) new AqlOptions(options, null).getOrDefault(AqlOption.id_column_name);
		colTy = "VARCHAR(" + new AqlOptions(options, null).getOrDefault(AqlOption.varchar_length) + ")";
		colTy0 = Types.VARCHAR;
		assertDisjoint(idCol);
	}

	private void deleteThenCreate(Connection conn) throws SQLException {
		for (En en : I.schema().ens) {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS " + prefix + enToString(en));
			String str = idCol + " " + colTy + " PRIMARY KEY NOT NULL";
			for (Att att : I.schema().attsFrom(en)) {
				str += ", " + attToString(att) + " " + colTy; //JAVA_OBJECT
			}
			for (Fk fk : I.schema().fksFrom(en)) {
				str += ", " + fkToString(fk) + " " + colTy + " NOT NULL";
			}
			stmt.execute("CREATE TABLE " + prefix + enToString(en) + "(" + str + ")");
		}
		/*for (Ty en : I.schema().typeSide.tys) {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS " + prefix + tyToString(en));
			stmt.execute("CREATE TABLE " + prefix + tyToString(en) + "(" + idCol + " " + colTy + " PRIMARY KEY)");
		}*/
		//TODO aql emit foreign keys
	}
	
	private void storeMyRecord(Connection conn, X x, List<Chc<Fk, Att>> header, String table) throws Exception {
		  List<String> hdrQ = new LinkedList<>();
		  List<String> hdr = new LinkedList<>();
		  hdr.add(idCol);
		  hdrQ.add("?");
        for (Chc<Fk, Att> aHeader : header) {
            hdrQ.add("?");
            Chc<Fk, Att> chc = aHeader;
            if (chc.left) {
                hdr.add(fkToString(chc.l));
            } else {
                hdr.add(attToString(chc.r));
            }
        }
		  
		  String insertSQL = "INSERT INTO " + table + "(" + Util.sep(hdr,"," )+ ") values (" + Util.sep(hdrQ,",") + ")";
		  PreparedStatement ps = conn.prepareStatement(insertSQL);
		
		  ps.setObject(1, x.toString(), colTy0);
		
		  for (int i = 0; i < header.size(); i++) {
			  Chc<Fk,Att> chc = header.get(i);
			  if (chc.left) {
				  ps.setObject(i+1+1, I.algebra().fk(chc.l, x).toString(), colTy0);			   
			  } else {
				  Object o = fromTerm(I.algebra().att(chc.r, x));
				  ps.setObject(i+1+1, o == null ? null : o.toString(), colTy0);			   			  
			  }
		  }
		
		  ps.executeUpdate();
	} 

	/*public void storeMyRecord(Connection conn, Y y, String table) throws Exception {
		  String insertSQL = "INSERT INTO " + table + "(" + idCol + ") values (?)";
		  PreparedStatement ps = conn.prepareStatement(insertSQL);
		  ps.setObject(1, y.toString(), colTy0);
		  ps.executeUpdate();
	} */

	@Override
	public void execute() {
		try {
			Connection conn = DriverManager.getConnection(jdbcString);
			deleteThenCreate(conn);
			for (En en : I.schema().ens) {
				List<Chc<Fk, Att>> header = headerFor(en);
				for (X x : I.algebra().en(en)) {
					storeMyRecord(conn, x, header, prefix + enToString(en));
				}
			}
			//kind of pointless to store labelled nulls since they are de-labelled in the entity part
			/*for (Ty ty : I.schema().typeSide.tys) {
				for (Y y : I.algebra().talg().sks.keySet()) {
					storeMyRecord(conn, y, prefix + tyToString(ty));
				}
			}*/
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<Chc<Fk,Att>> headerFor(En en) {
		List<Chc<Fk,Att>> ret = new LinkedList<>();
		for (Fk fk : I.schema().fksFrom(en)) {
			ret.add(Chc.inLeft(fk));
		}
		for (Att att : I.schema().attsFrom(en)) {
			ret.add(Chc.inRight(att));
		}
		return ret;
	}

	private void assertDisjoint(String idCol) {
		Collection<Object> entys = Util.isect(I.schema().ens, I.schema().typeSide.tys);
		if (!entys.isEmpty()) {
			throw new RuntimeException("Cannot JDBC export: entities and types share names: " + Util.sep(entys, ","));
		}
		Collection<Object> attfks = Util.isect(I.schema().atts.keySet(), I.schema().fks.keySet());
		if (!attfks.isEmpty()) {
			throw new RuntimeException("Cannot JDBC export: attributes and foreign keys share names: " + Util.sep(attfks, ","));
		}
		if (I.schema().atts.keySet().contains(idCol)) {
			throw new RuntimeException("Cannot JDBC export: id column (" + idCol + ") is also an attribute" );
		}
		if (I.schema().fks.keySet().contains(idCol)) {
			throw new RuntimeException("Cannot JDBC export: id column (" + idCol + ") is also a foreign key" );
		}	
	}

	private Object fromTerm(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
		if (term.obj != null) {
			return term.obj;
		} else if (term.sym != null && term.args.isEmpty()) {
			return term.sym;
		} else if (term.sym != null && !term.args.isEmpty() || term.sk != null) {
			return null;
		}
        throw new RuntimeException("anomaly: please report");
	}

	private String fkToString(Fk fk) {
		return (String) fk;
	}

	private String attToString(Att att) {
		return (String) att;
	}

	private String enToString(En en) {
		return (String) en;
	}

	/*private String tyToString(Ty ty) {
		return (String) ty;
	}*/

	@Override
	public String toString() {
		return "export_jdbc_instance " + clazz + " " + jdbcString + " " + prefix + "\n\n" + I;
	}

}
