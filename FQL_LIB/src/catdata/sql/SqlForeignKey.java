package catdata.sql;

import java.util.HashMap;
import java.util.Map;

public class SqlForeignKey {
	public SqlTable source, target;
	public Map<SqlColumn, SqlColumn> map = new HashMap<>(); //target->source

	public void validate() {
		if (source == null) {
			throw new RuntimeException();
		}
		if (target == null) {
			throw new RuntimeException();
		}
		if (!map.keySet().equals(target.pk)) {
			throw new RuntimeException(map.keySet() + " is not the primary key of " + target);			
		}
		for (SqlColumn tcol : map.keySet()) {
			SqlColumn scol = map.get(tcol);
			if (!source.columns.contains(scol)) {
				throw new RuntimeException(scol + " is not a column in " + source);
			}
			if (!scol.type.equals(tcol.type)) {
				throw new RuntimeException("Types do not match for " + scol + " and " + tcol);
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		SqlForeignKey other = (SqlForeignKey) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
		
	
}