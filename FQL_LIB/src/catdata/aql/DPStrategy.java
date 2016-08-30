package catdata.aql;

public final class DPStrategy {

	public final DPName name;
	public final Object object;

	public DPStrategy(DPName name, Object object) {
		if (name == null) {
			throw new RuntimeException("Attempt to create theorem proving strategy with null name");
		} else if (name.requiresParam() && object == null) {
			throw new RuntimeException(name + " requires a parameter");
		}
		this.name = name;
		this.object = object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		DPStrategy other = (DPStrategy) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "DPStrategy [name=" + name + ", object=" + object + "]";
	}
	
	
	
}
