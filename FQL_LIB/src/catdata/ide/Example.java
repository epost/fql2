package catdata.ide;

public abstract class Example implements Comparable<Example> {
	
	public abstract String getName(); 
	
	public abstract String getText();
	
	public abstract Language lang();

	@Override 
	public String toString() {
		return lang().prefix() +" " + getName();
	}
	
	@Override 
	public int compareTo(Example e) {
		return toString().compareTo(e.toString());
	}
	
}
