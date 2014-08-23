package fql_lib;

import java.io.Serializable;

public class Unit implements Serializable{

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
