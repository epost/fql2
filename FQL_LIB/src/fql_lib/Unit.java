package fql_lib;

public class Unit {

	@Override
	public String toString() {
		return "()";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Unit) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}

}
