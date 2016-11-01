package catdata.aql.exp; //TODO change package

import java.util.Iterator;

//intention: unique per table
public class GUID { //TODO: rename to ID
	
	public final String str;
	
	@Override
	public int hashCode() {
		return str.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GUID other = (GUID) obj;
		return str.equals(other.str);
	}

	private GUID(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}

	public static class It implements Iterator<GUID> { 

		int next = 0;
		
		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public GUID next() {
			return new GUID("_id" + next++);
		}

	}
	
}
