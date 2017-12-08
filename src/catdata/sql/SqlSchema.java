package catdata.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;

public class SqlSchema {

	private static int fkidx = 0;

	public final Set<SqlType> types = new HashSet<>();
	public final Set<SqlTable> tables = new HashSet<>();
	public final Set<SqlForeignKey> fks = new HashSet<>();

	public boolean isCnf() {
		for (SqlTable table : tables) {
			if (!table.isCnf()) {
				return false;
			}
		}
		return true;
	}

	private void validate() {
		for (SqlTable table : tables) {
			table.validate();
		}
		for (SqlForeignKey fk : fks) {
			fk.validate();
		}
	}

	public SqlSchema(DatabaseMetaData meta) throws SQLException {
		try (ResultSet result0 = meta.getTables(null, null, null, new String[] { "TABLE" })) {
			while (result0.next()) {
				SqlTable table = new SqlTable();
				table.name = result0.getString(3);
				tables.add(table);

				try (ResultSet cols = meta.getColumns(null, null, table.name, null)) {
					while (cols.next()) {
						String columnName = cols.getString(4);
						SqlType resolvedType = SqlType.resolve(cols.getString(5));
						SqlColumn col = new SqlColumn(table, columnName, resolvedType);
						String autoInc = cols.getString(23);
						if (autoInc != null && autoInc.equals("YES")) {
							col.autoInc = true;
						}
						table.columns.add(col);
						types.add(resolvedType);
					}
				}

				try (ResultSet pks = meta.getPrimaryKeys(null, null, table.name)) {
					while (pks.next()) {
						String colName = pks.getString(4);
						table.pk.add(table.getColumn(colName));
					}
					if (table.pk.isEmpty()) {
						table.pk = new HashSet<>(table.columns);
					}
				}
			}

			try (ResultSet result = meta.getTables(null, null, null, new String[] { "TABLE" })) {
				while (result.next()) {
					SqlTable table = getTable(result.getString(3));

					// name, local cols, foreign cols
					List<Triple<List<String>, List<Pair<String, String>>, String>> fks0 = new LinkedList<>();
					try (ResultSet rfks = meta.getImportedKeys(null, null, table.name)) {
						int lastSeen = 0;
						List<String> l = new LinkedList<>();
						List<Pair<String, String>> r = new LinkedList<>();
						String foreignTable = null;
						String foreignColumn = null;
						// String localTable = fks.getString(7);
						String localColumn = null;
						String fkname;
						String lfk = null;
						while (rfks.next()) {

							fkname = rfks.getString(12); // 13 is pkey name,
															// should
															// be irrelevent
							foreignTable = rfks.getString(3);
							foreignColumn = rfks.getString(4);
							// String localTable = fks.getString(7);
							localColumn = rfks.getString(8);
							String seq = rfks.getString(9);
							if (Integer.parseInt(seq) <= lastSeen) {
								fks0.add(new Triple<>(l, r, lfk));
								l = new LinkedList<>();
								r = new LinkedList<>();
							}
							lastSeen = Integer.parseInt(seq);

							l.add(localColumn);
							r.add(new Pair<>(foreignTable, foreignColumn));
							lfk = fkname;
						}
						if (foreignTable != null || foreignColumn != null || localColumn != null) {
							fks0.add(new Triple<>(l, r, lfk));
						}
					}
					
					for (Triple<List<String>, List<Pair<String, String>>, String> xxx : fks0) {
						SqlForeignKey fk = new SqlForeignKey();
						fk.source = getTable(table.name);
						fk.target = getTable(xxx.second.get(0).first);
						fk.name = table.name + "__" + Util.sep(xxx.first, "_") + "__" + Util.sep(xxx.second.stream().map(x->x.first + "_" + x.second).collect(Collectors.toList()), "_");
								//xxx.third == null ? "FK" + (fkidx++) : xxx.third;
						int i = 0;
						for (String lc : xxx.first) {
							Pair<String, String> fc = xxx.second.get(i);
							fk.map.put(fk.target.getColumn(fc.second), fk.source.getColumn(lc));
							i++;
						}
						fks.add(fk);
					}
				}				
			}
		}

		validate();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		String ret = "";
		for (SqlTable table : tables) {
			List<String> all = new LinkedList<>();

			ret += "CREATE TABLE " + table.name;
			ret += "(\n  ";
			List<String> l = table.columns.stream().map(x -> x.name + " " + x.type.name).collect(Collectors.toList());
			all.addAll(l);

			// ret += "(" + Util.sep(l, ", ") + ")";
			all.add("PRIMARY KEY (" + Util.sep(table.pk.stream().map(x -> x.name).collect(Collectors.toList()), ", ") + ")");

			for (SqlForeignKey t : fksFrom(table.name)) {
				List<String> src = new LinkedList<>();
				List<String> dst = new LinkedList<>();
				for (SqlColumn tcol : t.map.keySet()) {
					dst.add(tcol.name);
					src.add(t.map.get(tcol).name);
				}
				all.add("CONSTRAINT " + t.name + " FOREIGN KEY (" + Util.sep(src, ", ") + ") REFERENCES " + t.target.name + " (" + Util.sep(dst, ", ") + ")");
			}

			ret += Util.sep(all, ",\n  ") + "\n);\n\n";
		}
		return ret.trim();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////

	private Set<SqlColumn> allColumns;

	public Set<SqlColumn> cols() {
		if (allColumns != null) {
			return allColumns;
		}
		allColumns = new HashSet<>();
		for (SqlTable t : tables) {
			allColumns.addAll(t.columns);
		}
		return allColumns;
	}

	private final Map<String, SqlTable> tableMap = new HashMap<>();

	private SqlTable getTable0(String name) {
		SqlTable t = tableMap.get(name);
		if (t != null) {
			return t;
		}
		for (SqlTable table : tables) {
			if (table.name.equals(name)) {
				tableMap.put(name, table);
				return table;
			}
		}
		return null;
	}

	public SqlTable getTable(String name) {
		SqlTable ret = getTable0(name);
		if (ret == null) {
			throw new RuntimeException("Not a table: " + name);
		}
		return ret;
	}

	public boolean isTable(String name) {
		SqlTable ret = getTable0(name);
		return (ret != null);
	}

	private Map<String, SqlColumn> colNames;

	private SqlColumn getColumn0(String name) {
		if (colNames != null) {
			return colNames.get(name);
		}
		colNames = new HashMap<>();
		for (SqlColumn c : cols()) {
			colNames.put(c.table.name + c.name, c);
		}
		return colNames.get(name);
	}

	public SqlColumn getColumn(String name) {
		SqlColumn ret = getColumn0(name);
		if (ret == null) {
			throw new RuntimeException("Not a column: " + name);
		}
		return ret;
	}

	public boolean isColumn(String name) {
		return getColumn0(name) != null;
	}

	private Map<String, SqlForeignKey> fkNames;

	private SqlForeignKey getForeignKey0(String name) {
		if (fkNames != null) {
			return fkNames.get(name);
		}
		fkNames = new HashMap<>();
		for (SqlForeignKey c : fks) {
			if (fkNames.containsKey(c.name)) {
				throw new RuntimeException("Report to Ryan: non-unique FK name");
			}
			fkNames.put(c.name, c);
		}
		return fkNames.get(name);
	}

	public SqlForeignKey getForeignKey(String name) {
		SqlForeignKey ret = getForeignKey0(name);
		if (ret == null) {
			throw new RuntimeException("Not a foreign key: " + name);
		}
		return ret;
	}

	public boolean isForeignKey(String name) {
		return getForeignKey0(name) != null;
	}

	private final Map<String, Set<SqlForeignKey>> fksFrom0 = new HashMap<>();

	private Set<SqlForeignKey> fksFrom(String name) {
		Set<SqlForeignKey> t = fksFrom0.get(name);
		if (t != null) {
			return t;
		}
		t = new HashSet<>();
		for (SqlForeignKey fk : fks) {
			if (fk.source.equals(getTable(name))) {
				t.add(fk);
			}
		}
		fksFrom0.put(name, t);
		return t;
	}

	private final Map<String, Set<SqlForeignKey>> fksTo0 = new HashMap<>();

	public Set<SqlForeignKey> fksTo(String name) {

		Set<SqlForeignKey> t = fksTo0.get(name);
		if (t != null) {
			return t;
		}
		t = new HashSet<>();
		for (SqlForeignKey fk : fks) {
			if (fk.target.equals(getTable(name))) {
				t.add(fk);
			}
		}
		fksTo0.put(name, t);
		return t;
	}

	
}