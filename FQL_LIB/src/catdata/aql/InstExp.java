package catdata.aql;

import java.util.Collection;
import java.util.Collections;

import catdata.Util;

public abstract class InstExp<Ty,Sym,En,Att,Fk,Gen,Sk> extends Exp<Instance<Ty,Sym,En,Att,Fk,Gen,Sk>> {
	
	public Kind kind() {
		return Kind.INSTANCE;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public static final class InstExpEmpty<Ty,Sym,En,Att,Fk> extends InstExp<Ty,Sym,En,Att,Fk,Void,Void> {
		
		public final SchExp<Ty,Sym,En,Att,Fk> schema;

		@Override
		public Collection<String> deps() {
			return schema.deps();
		}
		public InstExpEmpty(SchExp<Ty, Sym, En, Att, Fk> schema) {
			if (schema == null) {
				throw new RuntimeException("Attempt to create InstExpEmpty with null schema");
			}
			this.schema = schema;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
			InstExpEmpty<?,?,?,?,?> other = (InstExpEmpty<?,?,?,?,?>) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "empty " + schema;
		}

		@Override
		public Instance<Ty, Sym, En, Att, Fk, Void, Void> eval(AqlEnv env) {
			return Instance.terminal(schema.eval(env));
		}

		@Override
		public String meta() {
			return " : " + schema;
		}
		
	}

	public static final class InstExpVar extends InstExp<Object, Object, Object, Object, Object, Object, Object> {
		public final String var;
		
		@Override
		public Collection<String> deps() {
			return Util.singList(var);
		}
		
		public InstExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create InstExpVar will null var");
			}
			this.var = var;
		}

		@Override
		public Instance<Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.getInstance(var);
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
			InstExpVar other = (InstExpVar) obj;
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
		
		@Override
		public String meta() {
			return "";
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class InstExpLit<Ty,Sym,En,Att,Fk,Gen,Sk> extends InstExp<Ty,Sym,En,Att,Fk,Gen,Sk> {

		public final Instance<Ty,Sym,En,Att,Fk,Gen,Sk> inst;
		
		@Override
		public Collection<String> deps() {
			return Collections.emptyList();
		}
		
		public InstExpLit(Instance<Ty,Sym,En,Att,Fk,Gen,Sk> inst) {
			if (inst == null) {
				throw new RuntimeException("Attempt to create InstExpLit with null schema");
			}
			this.inst = inst;
		}

		@Override
		public Instance<Ty,Sym,En,Att,Fk,Gen,Sk> eval(AqlEnv env) {
			return inst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inst == null) ? 0 : inst.hashCode());
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
			InstExpLit<?,?,?,?,?,?,?> other = (InstExpLit<?,?,?,?,?,?,?>) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InstExpLit [inst=" + inst + "]";
		}
		
		@Override
		public String meta() {
			return "";
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	//TODO: fix types in raws


}