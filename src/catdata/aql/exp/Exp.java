package catdata.aql.exp;

import java.util.Collection;
import java.util.Map;

import catdata.Pair;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Kind;

public abstract class Exp<X> {
	
	public Object getOrDefault(AqlEnv env, AqlOption option) {
		return env.defaults.getOrDefault(options(), option);
	}
	
	protected abstract Map<String, String> options();
	
	public abstract Kind kind();
	
	public abstract X eval(AqlEnv env);
		
	@Override
	public abstract String toString();
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);
	
	/**
	 * This will not capture global order constraints; for example,
	 * that pragmas form barriers.
	 */
	public abstract Collection<Pair<String, Kind>> deps();
}
