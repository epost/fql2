package catdata.aql.fdm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Pragma;
import catdata.aql.Transform;

public class ToJdbcPragmaTransform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> extends Pragma {

	public final String jdbcString;
	public final String prefix;
	public final String clazz;
	public final String idCol;

	public final Map<String, String> options;

	public final Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h;
	
	private final String colTy;
	private final int colTy0;

	//TODO aql column type mapping for jdbc instance export
	public ToJdbcPragmaTransform(String prefix, Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h, String clazz, String jdbcString, Map<String, String> options) {
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.jdbcString = jdbcString;
		this.prefix = prefix;
		this.h = h;
		this.options = options;
		this.clazz = clazz;
		idCol = (String) new AqlOptions(options, null).getOrDefault(AqlOption.id_column_name);
		colTy = "VARCHAR(" + (Integer) new AqlOptions(options, null).getOrDefault(AqlOption.varchar_length) + ")";
		colTy0 = java.sql.Types.VARCHAR;
		assertDisjoint(idCol);
	}

	private void deleteThenCreate(Connection conn) throws SQLException {
		for (En en : h.src().schema().ens) {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS " + prefix + enToString(en));
			String str = "src" + idCol + " " + colTy + " PRIMARY KEY NOT NULL";
			str += ", dst" + idCol + " " + colTy + " NOT NULL";
			stmt.execute("CREATE TABLE " + prefix + enToString(en) + "(" + str + ")");
		}
	}
	
	public void storeMyRecord(Connection conn, X1 x, String table) throws Exception {
		  List<String> hdrQ = new LinkedList<>();
		  List<String> hdr = new LinkedList<>();
		  hdr.add("src" + idCol);
		  hdr.add("dst" + idCol);
		  hdrQ.add("?");
		  hdrQ.add("?");
		  	  
		  String insertSQL = "INSERT INTO " + table + "(" + Util.sep(hdr,"," )+ ") values (" + Util.sep(hdrQ,",") + ")";
		  PreparedStatement ps = conn.prepareStatement(insertSQL);
		
		  ps.setObject(1, x.toString(), colTy0);
		  ps.setObject(2, h.repr(x).toString(), colTy0);
		
		  ps.executeUpdate();
	} 

	

	@Override
	public void execute() {
		try {
			Connection conn = DriverManager.getConnection(jdbcString);
			deleteThenCreate(conn);
			for (En en : h.src().schema().ens) {
				for (X1 x : h.src().algebra().en(en)) {
					storeMyRecord(conn, x, prefix + enToString(en));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void assertDisjoint(String idCol) {
		Collection<Object> entys = Util.isect(h.src().schema().ens, h.src().schema().typeSide.tys);
		if (!entys.isEmpty()) {
			throw new RuntimeException("Cannot JDBC export: entities and types share names: " + Util.sep(entys, ","));
		}
	}

	private String enToString(En en) {
		return (String) en;
	}

	@Override
	public String toString() {
		return "export_jdbc_transform " + clazz + " " + jdbcString + " " + prefix + "\n\n" + h;
	}

}
