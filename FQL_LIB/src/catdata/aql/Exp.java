package catdata.aql;

import java.util.Collection;

public abstract class Exp<X> {
	
	//TODO: dom, cod, id, comp, +, 0
	
	public abstract Kind kind();
	
	public abstract X eval(AqlEnv env);
	
	public abstract String meta();
	
	@Override
	public abstract String toString();
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);

	public abstract Collection<String> deps();
}
