package catdata.aql;

import java.util.Iterator;

public final class Var implements Comparable<Var> {

	
	public final String var;

	public Var(String var) {
		if (var == null) {
			throw new RuntimeException("Attempt to create null variable");
		} 
		this.var = var;
	}
	
	@Override
	public String toString() {
		return var;
	}
	
	private static int index = 0;
	
	private static Var fresh() {
		return new Var("v" + index++);
	}
	
	public static final Iterator<Var> it = new Iterator<Var>() {

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public Var next() {
			return fresh();
		}
		
	};
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + var.hashCode();
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
		Var other = (Var) obj;
        return var.equals(other.var);
    }

	@Override
	public int compareTo(Var o) {
		return this.var.compareTo(o.var);
	}
	
	
	
}
