package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Query;

public abstract class QueryExp extends Exp<Query> {
	
	public Kind kind() {
		return Kind.QUERY;
	}
	
	
	public static final class QueryExpVar extends QueryExp {
		String var;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.QUERY));
		}
		
		@Override
		public Query eval(AqlEnv env) {
			return env.getQuery(var);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((var == null) ? 0 : var.hashCode());
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
			QueryExpVar other = (QueryExpVar) obj;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return var;
		}
		
		
	}

	
	public static final class QueryExpLit extends QueryExp {

		//TODO: aql add imports

		public final Query q;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		public QueryExpLit(Query q) {
			if (q == null) {
				throw new RuntimeException("Attempt to create MapExpLit with null schema");
			}
			this.q = q;
		}

		@Override
		public Query eval(AqlEnv env) {
			throw new RuntimeException(); 
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((q == null) ? 0 : q.hashCode());
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
			QueryExpLit other = (QueryExpLit) obj;
			if (q == null) {
				if (other.q != null)
					return false;
			} else if (!q.equals(other.q))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "QueryExpLit [q=" + q + "]";
		}
		
		
	}

}