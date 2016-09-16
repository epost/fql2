package catdata;

public class Chc3<A,B,C> {

	public final A a;
	public final B b;
	public final C c;
	
	private Chc3(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	@Override
	public String toString() {
		return "Chc3 [a=" + a + ", b=" + b + ", c=" + c + "]";
	}

	public static <A,B,C> Chc3<A,B,C> a(A a) {
		if (a == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return new Chc3<>(a, null, null);
	}
	
	public static <A,B,C> Chc3<A,B,C> b(B b) {
		if (b == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return new Chc3<>(null, b, null);
	}
	
	public static <A,B,C> Chc3<A,B,C> c(C c) {
		if (c == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return new Chc3<>(null, null, c);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
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
		Chc3<?,?,?> other = (Chc3<?,?,?>) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		return true;
	}
	
	
	
}
