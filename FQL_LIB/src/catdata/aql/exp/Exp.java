package catdata.aql.exp;

import java.util.Collection;

import catdata.Pair;

public abstract class Exp<X> {
	
	public abstract Kind kind();
	
	public abstract X eval(AqlEnv env);
		
	@Override
	public abstract String toString();
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);

	public abstract Collection<Pair<String, Kind>> deps();
}