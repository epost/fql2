package catdata.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import catdata.Util;

//TODO: aql can store these not as sets, but as maps from primary key
public class SqlInstance {

	private final Map<SqlTable, Set<Map<SqlColumn, Optional<Object>>>> db = new HashMap<>();
	private final SqlSchema schema;
	//private final Connection conn;
	
	public Map<SqlColumn, Optional<Object>> follow(Map<SqlColumn, Optional<Object>> row, SqlForeignKey fk) {
		Map<SqlColumn, Optional<Object>> cand = new HashMap<>();
		
		for (SqlColumn tcol : fk.target.pk) {
			SqlColumn scol = fk.map.get(tcol);
			cand.put(tcol, row.get(scol));
		}
		
		Map<SqlColumn, Optional<Object>> ret = null;
		for (Map<SqlColumn, Optional<Object>> tuple : get(fk.target)) {
			for (SqlColumn col : fk.target.pk) {
				if (cand.get(col).equals(tuple.get(col))) {
					if (ret != null) {
						throw new RuntimeException();
					}
					ret = tuple;
				}
			}
		}
		if (ret == null) {
			throw new RuntimeException();
		}
		return ret;
	}
	
	public Set<Map<SqlColumn, Optional<Object>>> get(SqlTable t) {
		if (!db.containsKey(t)) {
			throw new RuntimeException("Not a table: " + t.name);
		}
		return db.get(t);
	}
	
	public SqlInstance(SqlSchema schema, Connection conn, boolean errMeansNull, boolean useDistinct) throws SQLException {
		if (schema == null || conn == null) {
			throw new RuntimeException();
		}
		this.schema = schema;
		//this.conn = conn;
		String d = useDistinct ? "DISTINCT" : "";
		for (SqlTable table : schema.tables) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("SELECT " + d + " * FROM " + table.name);
				try (ResultSet resultSet = stmt.getResultSet()) {
					Set<Map<SqlColumn, Optional<Object>>> rows = new HashSet<>();
					while (resultSet.next()) {
						Map<SqlColumn, Optional<Object>> row = new HashMap<>();
						for (SqlColumn col : table.columns) {
							try {
								Object o = resultSet.getObject(col.name);
								if (o != null) {
									row.put(col, Optional.of(o));
								} else {
									row.put(col, Optional.empty());
								}
							} catch (Exception ex) {
								if (errMeansNull) {
									row.put(col, Optional.empty());
								} else {
									throw ex;
								}
							}
					    }
						rows.add(row);
					}
					db.put(table, rows);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		List<String> all = new LinkedList<>();
		
		for (SqlTable table : schema.tables) {
			if (db.get(table).isEmpty()) {
				continue;
			}
			List<String> x = new LinkedList<>();
			for (Map<SqlColumn, Optional<Object>> row : db.get(table)) {
				String y = "(";
				List<String> q = new LinkedList<>();
				for (SqlColumn col : table.columns) {
					Optional<Object> o = row.get(col);
					String z = o.isPresent() ? "'" + o.get() + "'" : "NULL";
					q.add(z);
				}
				y += Util.sep(q, ", ") + ")";
				x.add(y);
			}
			all.add("INSERT INTO " + table.name + " VALUES\n  " + Util.sep(x, ",\n  ") + ";");
		}
		
		return Util.sep(all, "\n\n"); 
	}
	
	
}
