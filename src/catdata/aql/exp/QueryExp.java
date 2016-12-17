package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Query;
import catdata.aql.exp.SchExp.SchExpLit;

public abstract class QueryExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> extends Exp<Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2>> {
	
	@Override
	public Kind kind() {
		return Kind.QUERY;
	}
	
	public abstract Pair<SchExp<Ty,En1,Sym,Fk1,Att1>, SchExp<Ty,En2,Sym,Fk2,Att2>> type(AqlTyping G);
	
	
	public static final class QueryExpVar extends QueryExp<Object,Object,Object,Object,Object,Object,Object,Object> {
		public final String var;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.QUERY));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Query<Object,Object,Object,Object,Object,Object,Object,Object> eval(AqlEnv env) {
			return env.defs.qs.get(var);
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
	
		@SuppressWarnings("unchecked")
		@Override
		public Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>> type(AqlTyping G) {
			return (Pair<SchExp<Object, Object, Object, Object, Object>,SchExp<Object, Object, Object, Object, Object>>) ((Object)G.defs.qs.get(var));
		}

		public QueryExpVar(String var) {
			this.var = var;
		}

		@Override
		public long timeout() {
			return 0;
		}	
		
	}

	
	public static final class QueryExpLit<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> extends QueryExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> {

		//TODO: aql add imports

		public final Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> q;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		public QueryExpLit(Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> q) {
			if (q == null) {
				throw new RuntimeException("Attempt to create MapExpLit with null schema");
			}
			this.q = q;
		}

		@Override
		public Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> eval(AqlEnv env) {
			throw new RuntimeException("todo"); 
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
			QueryExpLit<?,?,?,?,?,?,?,?> other = (QueryExpLit<?,?,?,?,?,?,?,?>) obj;
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
		
		@Override
		public Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> type(AqlTyping G) {
			return new Pair<>(new SchExpLit<>(q.src), new SchExpLit<>(q.dst));
		}
		@Override
		public long timeout() {
			return 0;
		}
	}

}