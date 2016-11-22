package catdata;

public class Ref<X> {

	public X x;

	public int var;
	
	public static int count = 0;
	
	public Ref() {
		var = count++;
	}

	public Ref(X x) {
		if (x == null) {
			throw new RuntimeException("Anomaly, please report");
		}
//		if (x instanceof Chc) {
//			((Chc) x).assertNeitherNull();
//		}
		this.x = x;
	}
	
	public void set(X x) {
		if (x == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		if (this.x == null) {
			this.x = x;
			var = -1;
		//	if (x instanceof Chc) {
			//	((Chc) x).assertNeitherNull();
			//}
		} else if (this.x.equals(x)) {
			return;
		} else {
			throw new RuntimeException("Reference already set to " + this.x + ", cannot set to " + x);
		}
	}
	
	public void set(Ref<X> ref) {
		if (ref == null) {
			throw new RuntimeException("Anomaly, please report");
		}
		if (ref.x != null) {
			set(ref.x);
		} else {
			count = ref.var;
		}
	}

	@Override
	public String toString() {
		return x == null ? "?" + var : x.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + var;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
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
		Ref<?> other = (Ref<?>) obj;
		if (var != other.var)
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		return true;
	}

	
	

}
