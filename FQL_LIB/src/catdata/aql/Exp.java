package catdata.aql;

public abstract class Exp<X> {
	
	//TODO: dom, cod, id, comp, +, 0
	
	public abstract Kind kind();
	
	public abstract X eval(Env env);
	
	public abstract String meta();
	
	@Override
	public abstract String toString();
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);
}
