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

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Pragma;
import catdata.aql.Transform;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public class ToJdbcPragmaTransform<Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> extends Pragma {

	private final String jdbcString;
	private final String prefix;
//	private final String clazz;
	private final String idCol;

	private final Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h;
	
	private final String colTy;
	private final int colTy0;
	
	private final AqlOptions options1;
	private final AqlOptions options2;
	
	//TODO aql column type mapping for jdbc instance export
	public ToJdbcPragmaTransform(String prefix, Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h, String clazz, String jdbcString, AqlOptions options1, AqlOptions options2) {
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.jdbcString = jdbcString;
		this.prefix = prefix;
		this.h = h;
	//s	this.clazz = clazz;
		idCol = (String) options1.getOrDefault(AqlOption.id_column_name);
		colTy = "VARCHAR(" + options1.getOrDefault(AqlOption.varchar_length) + ")";
		colTy0 = Types.VARCHAR;
		assertDisjoint();
		this.options1 = options1;
		this.options2 = options2;
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
	
	private void storeMyRecord(Pair<Map<X1,Integer>, Map<Integer, X1>> I, Pair<Map<X2,Integer>, Map<Integer, X2>> J, Connection conn, X1 x, String table) throws Exception {
		  List<String> hdrQ = new LinkedList<>();
		  List<String> hdr = new LinkedList<>();
		  hdr.add("src" + idCol);
		  hdr.add("dst" + idCol);
		  hdrQ.add("?");
		  hdrQ.add("?");
		  	  
		  String insertSQL = "INSERT INTO " + table + "(" + Util.sep(hdr,"," )+ ") values (" + Util.sep(hdrQ,",") + ")";
		  PreparedStatement ps = conn.prepareStatement(insertSQL);
		
		  ps.setObject(1, I.first.get(x), colTy0);
		  ps.setObject(2, J.first.get(h.repr(x)), colTy0);
		
		  ps.executeUpdate();
	} 

	

	@Override
	public void execute() {
		try {
			Connection conn = DriverManager.getConnection(jdbcString);
			deleteThenCreate(conn);
			int s1 = (int) options1.getOrDefault(AqlOption.start_ids_at);
			int s2 = (int) options2.getOrDefault(AqlOption.start_ids_at);
			Pair<Map<X1, Integer>, Map<Integer, X1>> I = h.src().algebra().intifyX(s1);
			Pair<Map<X2, Integer>, Map<Integer, X2>> J = h.dst().algebra().intifyX(s2);
			for (En en : h.src().schema().ens) {
				for (X1 x : h.src().algebra().en(en)) {
					storeMyRecord(I, J, conn, x, prefix + enToString(en));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void assertDisjoint() {
		Collection<Object> entys = Util.isect(h.src().schema().ens, h.src().schema().typeSide.tys);
		if (!entys.isEmpty()) {
			throw new RuntimeException("Cannot JDBC export: entities and types and idcol share names: " + Util.sep(entys, ","));
		}
		
	}

	private String enToString(En en) {
		return en.str;
	}

	@Override
	public String toString() {
		return "Exported " + h.size() + " rows.";
	}

}
