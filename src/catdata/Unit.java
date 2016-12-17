package catdata;

/**
 * @author Ryan Wisnesky
 */
public class Unit {

	@Override
	public String toString() {
            return "()";
	}
	
	@Override
	public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            return (o instanceof Unit);
	}
	
	@Override
	public int hashCode() {
            return 0;
	}

}
