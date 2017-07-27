package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.AqlOptions.AqlOption;

public class TransExpJdbc<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> 
extends TransExpImport<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2, Connection>  {


	public final String clazz;
	public final String jdbcString;


	public TransExpJdbc(String clazz, String jdbcString, InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst, List<Pair<LocStr, String>> map, List<Pair<String, String>> options) {
		super(src, dst, map, options);
		this.clazz = clazz;
		this.jdbcString = jdbcString;
		Util.checkClass(clazz);
	}

	

	@Override
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = "options" + Util.sep(options, "\n\t\t", " = ");
		}
		return "import_jdbc " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " : " + src + " -> " + dst + " {\n\t" + Util.sep(map, " -> ", "\n\t") + s + "\n}";
	}


	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode(); //TODO aql note
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
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
		return super.equals(obj); //TODO aql note
	}

	@Override
	protected String getHelpStr() {
		return InstExpJdbc.helpStr;
	}

	@Override
	protected void stop(Connection h) throws Exception {
		h.close();
	}

	@Override
	protected Connection start(Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		String toGet = jdbcString;
		if (clazz.trim().isEmpty()) {
			String driver = (String) op.getOrDefault(AqlOption.jdbc_default_class);
			Util.checkClass(driver);
		}
		if (jdbcString.trim().isEmpty()) {
			toGet = (String) op.getOrDefault(AqlOption.jdbc_default_string);
		}
		return DriverManager.getConnection(toGet);	
	}
	
	@Override
	protected void processEn(En en, Schema<Ty, En, Sym, Fk, Att> sch, Connection conn, String q) throws Exception {
		Statement stmt = conn.createStatement();
		stmt.execute(q);
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		if (columnsNumber != 2) {
			stmt.close();
			rs.close();
			throw new RuntimeException("Error in " + en + ": Expected 2 columns but received " + columnsNumber);
		}
		while (rs.next()) {
			 Object gen = rs.getObject(1);
			 if (gen == null) {
				stmt.close();
				rs.close();
				throw new RuntimeException("Error in " + en + ": Encountered a NULL generator in column 1");
			 }
			 Object gen2 = rs.getObject(2);
			 if (gen2 == null) {
				stmt.close();
				rs.close();
				throw new RuntimeException("Error in " + en + ": Encountered a NULL generator in column 2");
			 }						 
			 gens.put((Gen1) gen.toString(), Term.Gen((Gen2) gen2.toString()));
		}
		stmt.close();
		rs.close();		
	}


}
