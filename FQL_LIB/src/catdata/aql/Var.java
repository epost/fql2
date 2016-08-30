package catdata.aql;

public final class Var {

	public final String var;

	public Var(String var) {
		this.var = var;
	}
	
	@Override
	public String toString() {
		return var;
	}
	
	private static int index = 0;
	
	public static Var fresh() {
		return new Var("" + index++);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}
	
	
	
}
