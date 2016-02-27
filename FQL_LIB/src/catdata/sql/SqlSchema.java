package catdata.sql;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;

import catdata.Chc;
import catdata.Pair;
import catdata.ide.Util;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class SqlSchema {
	
	public Set<SqlType> types = new HashSet<>();
	public Set<SqlTable> tables = new HashSet<>();
	public Set<SqlForeignKey> fks = new HashSet<>();
	
	public void validate() {
		for (SqlTable table : tables) {
			table.validate();
		}
		for (SqlForeignKey fk : fks) {
			fk.validate();
		}
	}
	
	public SqlSchema(DatabaseMetaData meta) throws SQLException {
		ResultSet result = meta.getTables(null, null, null, new String[] { "TABLE" });
		while (result.next()) {
			SqlTable table = new SqlTable();
			table.name = result.getString(3).toUpperCase();
			tables.add(table);
			
			ResultSet cols = meta.getColumns(null, null, table.name, null);
			while (cols.next()) {
				String columnName = cols.getString(4).toUpperCase();
				SqlType resolvedType = SqlType.resolve(cols.getString(5));
				table.columns.add(new SqlColumn(table, columnName, resolvedType));
				types.add(resolvedType);
			}
			
			ResultSet pks = meta.getPrimaryKeys(null, null, table.name);
			while (pks.next()) {
				String colName = pks.getString(4).toUpperCase();
				table.pk.add(table.getColumn(colName));
			}
			if (table.pk.isEmpty()) {
				table.pk = new HashSet<>(table.columns);
			}	
		}
		
		result = meta.getTables(null, null, null, new String[] { "TABLE" });
		while (result.next()) {
			SqlTable table = getTable(result.getString(3).toUpperCase());
			
			//name, local cols, foreign cols
			List<Pair<List<String>, List<Pair<String, String>>>> fks0 = new LinkedList<>(); 
			ResultSet rfks = meta.getImportedKeys(null, null, table.name);
			int lastSeen = 0;
			List<String> l = new LinkedList<>();
			List<Pair<String, String>> r = new LinkedList<>();
			String foreignTable = null;
			String foreignColumn = null; 
			//String localTable = fks.getString(7);
			String localColumn = null;
	//		String fkname = null;
			while (rfks.next()) {
				
			//	fkname = fks.getString(12); //13 is pkey name, should be irrelevent
				foreignTable = rfks.getString(3);
				foreignColumn = rfks.getString(4); 
				//String localTable = fks.getString(7);
				localColumn = rfks.getString(8);
				String seq = rfks.getString(9);
				if (Integer.parseInt(seq) <= lastSeen) {
					fks0.add(new Pair<>(l, r));
					l = new LinkedList<>();
					r = new LinkedList<>();
				}
				lastSeen = Integer.parseInt(seq);
				
				l.add(localColumn);
				r.add(new Pair<>(foreignTable, foreignColumn));
			}
			if (foreignTable != null || foreignColumn != null || localColumn != null) {
				fks0.add(new Pair<>(l, r));
			}
			
			for (Pair<List<String>, List<Pair<String, String>>> xxx : fks0) {
				SqlForeignKey fk = new SqlForeignKey();
				fk.source = getTable(table.name);
				fk.target = getTable(xxx.second.get(0).first);
				int i = 0;
				for (String lc : xxx.first) {
					Pair<String, String> fc = xxx.second.get(i);
					fk.map.put(fk.target.getColumn(fc.second), fk.source.getColumn(lc));
					i++;
				}
				fks.add(fk);
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

			ret += "CREATE TABLE "+ table.name;
			ret += "(\n  ";
			List<String> l = table.columns.stream().map(x -> { return x.name + " " + x.type.name; }).collect(Collectors.toList());
			all.addAll(l);
			
//			ret += "(" + Util.sep(l, ", ") + ")";
			all.add("PRIMARY KEY (" + Util.sep(table.pk.stream().map(x -> { return x.name; }).collect(Collectors.toList()) , ", ") + ")");
			
			for (SqlForeignKey t : fksFrom(table.name)) {
				List<String> src = new LinkedList<>();
				List<String> dst = new LinkedList<>();
				for (SqlColumn tcol : t.map.keySet()) {
					dst.add(tcol.name);
					src.add(t.map.get(tcol).name);
				}
				all.add("FOREIGN KEY (" + Util.sep(src, ", ") + ") REFERENCES " + t.target.name + " (" + Util.sep(dst, ", ") + ")"); 
			}
			
			
			ret += Util.sep(all, ",\n  ") + "\n);\n\n";
		}
		return ret.trim();
	} 
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	private Map<String, SqlTable> tableMap = new HashMap<>();
	public SqlTable getTable(String name) {
		SqlTable t = tableMap.get(name.toUpperCase());
		if (t != null) {
			return t;
		}
		for (SqlTable table : tables) {
			if (table.name.toUpperCase().equals(name.toUpperCase())) {
				tableMap.put(name.toUpperCase(), table);
				return table;
			}
		}
		throw new RuntimeException("Not a table: " + name);
	}

	private Map<String, Set<SqlForeignKey>> fksFrom0 = new HashMap<>();
	public Set<SqlForeignKey> fksFrom(String name) {
		Set<SqlForeignKey> t = fksFrom0.get(name.toUpperCase());
		if (t != null) {
			return t;
		}
		t = new HashSet<>();
		for (SqlForeignKey fk : fks) {
			if (fk.source.equals(getTable(name))) {
				t.add(fk);
			}
		}
		fksFrom0.put(name.toUpperCase(), t);
		return t;
	}
	
	private Map<String, Set<SqlForeignKey>> fksTo0 = new HashMap<>();
	public Set<SqlForeignKey> fksTo(String name) {

		Set<SqlForeignKey> t = fksTo0.get(name.toUpperCase());
		if (t != null) {
			return t;
		}
		t = new HashSet<>();
		for (SqlForeignKey fk : fks) {
			if (fk.target.equals(getTable(name))) {
				t.add(fk);
			}
		}
		fksTo0.put(name.toUpperCase(), t);
		return t;
	} 
}