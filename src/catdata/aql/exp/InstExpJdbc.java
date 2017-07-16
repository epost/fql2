package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Schema;

//TODO this type is actually a lie bc of import_as_theory option
public class InstExpJdbc<Ty, En, Sym, Fk, Att, Gen> extends InstExpImport<Ty, En, Sym, Fk, Att, Gen, Connection> {

	public final String clazz;
	public final String jdbcString;

	public InstExpJdbc(SchExp<Ty, En, Sym, Fk, Att> schema,
			List<Pair<String, String>> options, String clazz, String jdbcString,
			List<Pair<LocStr, String>> map) {
		super(schema, map, options);

		this.clazz = clazz;
		this.jdbcString = jdbcString;
		try {
			Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Connection start(Schema<Ty, En, Sym, Fk, Att> sch) throws SQLException {
		return DriverManager.getConnection(jdbcString);	
	}
	
	@Override
	protected void end(Connection conn) throws SQLException {
		conn.close();
	}
	
	public final static String helpStr = "Possible problem: AQL IDs be unique among all entities and types; it is not possible to have, for example,"
			+ "\n" + "\n	0:Employee" + "\n	0:Department" + "\n"
			+ "\nPossible solution: Distinguish the IDs prior to import, or distinguish them during import, for example, "
			+ "\n" + "\n	instance J = import_jdbc ... {"
			+ "\n		Employee -> \"SELECT concat(\"emp\",id) FROM Employee\""
			+ "\n		Department -> \"SELECT concat(\"dept\",id) FROM Dept\""
			+ "\n		worksIn -> \"SELECT concat(\"emp\",id), concat(\"dept\",worksIn) FROM Employee\"" + "\n	}"
			+ "\nRemember also that by default imports are of entire sets of tables with no missing data; see the import_as_theory option in the manual to change this behavior";
	
	@Override
	protected String getHelpStr() {
		return helpStr;
	}
	
	@Override
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = "options" + Util.sep(options, "\n\t\t", " = ");
		}
		return "import_jdbc " + schema + " " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " {\n\t"
				+ Util.sep(map, " -> ", "\n\t") + s + "\n}";
	}

	@Override
	protected void shreddedAtt(Connection conn, Att att, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Statement stmt = conn.createStatement();
		stmt.execute(s);
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		if (columnsNumber != 2) {
			stmt.close();
			rs.close();
			conn.close();
			throw new RuntimeException(
					"Error in " + att + ": Expected 2 columns but received " + columnsNumber);
		}
		while (rs.next()) {
			Object lhs = rs.getObject(1);
			if (lhs == null) {
				stmt.close();
				rs.close();
				conn.close();
				throw new RuntimeException("Error in " + att + ": Encountered a NULL column 1");
			}
			Object rhs = rs.getObject(2);
			if (!atts0.map.containsKey(lhs.toString())) {
				atts0.put((Gen) lhs.toString(), new Ctx<>());
			}
			atts0.get((Gen) lhs.toString()).put(att, objectToSk(sch, rhs, lhs.toString(), att,
					tys0, extraRepr, false));
		}
		stmt.close();
		rs.close();
	}

	@Override
	protected void shreddedFk(Connection conn, Fk fk, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Statement stmt = conn.createStatement();
		stmt.execute(s);
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		if (columnsNumber != 2) {
			stmt.close();
			rs.close();
			conn.close();
			throw new RuntimeException("Error in " + fk + ": Expected 2 columns but received " + columnsNumber);
		}
		while (rs.next()) {
			Object lhs = rs.getObject(1);
			if (lhs == null) {
				stmt.close();
				rs.close();
				conn.close();
				throw new RuntimeException("Error in " + fk + ": Encountered a NULL in column 1");
			}
			Object rhs = rs.getObject(2);
			if (rhs == null) {
				stmt.close();
				rs.close();
				conn.close();
				throw new RuntimeException("Error in " + fk + ": Encountered a NULL in column 2");
			}
			Gen g1 = (Gen) lhs.toString();
			Gen g2 = (Gen) rhs.toString(); //store strings
			if (!fks0.containsKey(g1)) {
				fks0.put(g1, new Ctx<>());
			}
			fks0.get(g1).put(fk, g2);
		}
		stmt.close();
		rs.close();
	}

	@Override
	protected void shreddedEn(Connection conn, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Statement stmt = conn.createStatement();
		stmt.execute(s);
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		if (columnsNumber != 1) {
			rs.close();
			stmt.close();
			conn.close();
			throw new RuntimeException("Expected 1 column but received " + columnsNumber);
		}
		while (rs.next()) {
			Object gen = rs.getObject(1);
			if (gen == null) {
				stmt.close();
				rs.close();
				conn.close();
				throw new RuntimeException("Encountered a NULL generator");
			}
			ens0.get(en).add((Gen) gen.toString()); //store strings 
		}
		rs.close();
		stmt.close();
	}

	@Override
	protected void joinedEn(Connection conn, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Statement stmt = conn.createStatement();
		stmt.execute(s);
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		checkColumns(en, s, sch, rsmd);
		
		while (rs.next()) {
			Object gen = rs.getObject(idCol);
			if (gen == null) {
				stmt.close();
				rs.close();
				conn.close();
				throw new RuntimeException("Encountered a NULL generator in ID column " + idCol);
			}
			Gen g1 = (Gen) gen.toString();
			ens0.get(en).add(g1); //store strings 
			
			for (Fk fk : sch.fksFrom(en)) {
				Object rhs = rs.getObject((String) fk);
				if (rhs == null) {
					stmt.close();
					rs.close();
					conn.close();
					throw new RuntimeException("ID " + gen + " has a NULL foreign key value on " + fk);
				}	
				Gen g2 = (Gen) rhs.toString(); //store strings
				if (!fks0.containsKey(g1)) {
					fks0.put(g1, new Ctx<>());
				}
				fks0.get(g1).put(fk, g2);
			}
			for (Att att : sch.attsFrom(en)) {
				Object rhs = rs.getObject((String) att);
				if (!atts0.map.containsKey(g1)) {
					atts0.put(g1, new Ctx<>());
				}
				atts0.get(g1).put(att, objectToSk(sch, rhs, gen.toString(), att,
						tys0, extraRepr, false));
			}
			
		}
	
	}

	private void checkColumns(En en, String s, Schema<Ty, En, Sym, Fk, Att> sch, ResultSetMetaData rsmd)
			throws SQLException {
		Set<String> colNames = new HashSet<>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String colName = rsmd.getColumnLabel(i);
			if (!(colName.equalsIgnoreCase(idCol) || Util.containsUpToCase(sch.attsFrom(en), colName) || Util.containsUpToCase(sch.fksFrom(en), colName))) {
				throw new RuntimeException("Column name " + colName + " does not refer to a foreign key or attribute in \n\n" + s);
			}
			colNames.add(colName);
		}
		for (Att att : sch.attsFrom(en)) {
			if (! Util.containsUpToCase(colNames, att)) {
				throw new RuntimeException("Attribute " + att + " has no column in \n\n" + s);
			}
		}
		for (Fk fk : sch.fksFrom(en)) {
			if (! Util.containsUpToCase(colNames, fk)) {
				throw new RuntimeException("Foreign key " + fk + " has no column in \n\n" + s);
			}
		}
		if (! Util.containsUpToCase(colNames, idCol)) {
			throw new RuntimeException("No ID column " + idCol + " in \n\n" + s);
		}
	}

	//TODO AQL *******************************************************************
	// must invoke super


	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
		result = prime * result + super.hashCode();
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
		if (jdbcString == null) {
			if (other.jdbcString != null)
				return false;
		} else if (!jdbcString.equals(other.jdbcString))
			return false;
		return super.equals(obj);
	}

	

}
