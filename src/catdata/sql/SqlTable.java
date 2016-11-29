package catdata.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Util;

public class SqlTable {

	public String name;
	public List<SqlColumn> columns = new LinkedList<>();
	public Set<SqlColumn> pk = new HashSet<>();
	
	public SqlColumn getCnfId() {
		return Util.get0(pk);		
	}
		
	public boolean isCnf() {
		if (pk.size() != 1) {
			return false;
		}
		return getCnfId().autoInc;
	}
	
	public void validate() {
		if (name == null) {
			throw new RuntimeException();
		}
		for (SqlColumn col : columns) {
			if (!col.table.equals(this)) {
				throw new RuntimeException();
			}
		}
		if (!columns.containsAll(pk)) {
			throw new RuntimeException();
		}
		if (columns.size() != new HashSet<>(columns).size()) {
			throw new RuntimeException();
		}
	}
	
	private Map<String, SqlColumn> colMap = new HashMap<>();
	public SqlColumn getColumn(String name) {
		SqlColumn t = colMap.get(name.toUpperCase());
		if (t != null) {
			return t;
		}
		for (SqlColumn col : columns) {
			if (col.name.toUpperCase().equals(name.toUpperCase())) {
				colMap.put(name.toUpperCase(), col);
				return col;
			}
		}
		throw new RuntimeException("Not a column in " + this + ": " + name);
	}
	
	private Map<String, String> typeMap = null;
	public Map<String, String> typeMap() {
		if (typeMap != null) {
			return typeMap;
		}
		typeMap = new HashMap<>();
		for (SqlColumn col : columns) {
			typeMap.put(col.name, col.type.name);
		}
		return typeMap;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SqlTable other = (SqlTable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}