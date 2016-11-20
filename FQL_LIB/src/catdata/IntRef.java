package catdata;

public class IntRef {
	public int i;
	public IntRef(int i) {
		this.i = i;
	}
	
	@Override
	public String toString() {
		return "?" + i;
	}
	
	public String pp() {
		return Integer.toString(i++);
	}  
}