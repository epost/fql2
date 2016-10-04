package catdata.aql;

import java.util.Collection;
import java.util.Collections;

import catdata.Util;

public abstract class SchExp<Ty,En,Sym,Att,Fk> extends Exp<Schema<Ty,En,Sym,Att,Fk>> {	
	
	@Override
	public Kind kind() {
		return Kind.SCHEMA;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpInst<Ty,En,Sym,Att,Fk> extends SchExp<Ty,En,Sym,Att,Fk> {
		public final InstExp<Ty,En,Sym,Att,Fk,?,?> inst;

		@Override
		public Collection<String> deps() {
			return inst.deps();
		}
		
		@Override
		public String meta() {
			return "";
		}
		
		public SchExpInst(InstExp<Ty, En, Sym, Att, Fk, ?, ?> inst) {
			if (inst == null) {
				throw new RuntimeException("Attempt to get schema for null instance");
			}
			this.inst = inst;
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
			SchExpInst<?,?,?,?,?> other = (SchExpInst<?,?,?,?,?>) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "schemaOf " + inst + "]";
		}

		@Override
		public Schema<Ty, En, Sym, Att, Fk> eval(AqlEnv env) {
			return inst.eval(env).schema;
		}
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpEmpty<Ty,Sym> extends SchExp<Ty,Void,Sym,Void,Void> {
		
		public final TyExp<Ty,Sym> typeSide;
		
		@Override
		public Collection<String> deps() {
			return typeSide.deps();
		}

		public SchExpEmpty(TyExp<Ty, Sym> typeSide) {
			if (typeSide == null) {
				throw new RuntimeException("Attempt to use null typeSide in SchExpEmpty");
			}
			this.typeSide = typeSide;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
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
			SchExpEmpty<?,?> other = (SchExpEmpty<?,?>) obj;
			if (typeSide == null) {
				if (other.typeSide != null)
					return false;
			} else if (!typeSide.equals(other.typeSide))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "empty " + typeSide;
		}

		@Override
		public Schema<Ty, Void, Sym, Void, Void> eval(AqlEnv env) {
			return Schema.terminal(typeSide.eval(env));
		}
		
		@Override
		public String meta() {
			return " : " + typeSide;
		}
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpVar extends SchExp<Object, Object, Object, Object, Object> {
		
		public final String var;
		
		@Override
		public Collection<String> deps() {
			return Util.singList(var);
		}
		
		public SchExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create SchExpVar will null var");
			}
			this.var = var;
		}

		@Override
		public Schema<Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.getSchema(var);
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
			SchExpVar other = (SchExpVar) obj;
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

	public static final class SchExpLit<Ty,Sym,En,Att,Fk> extends SchExp<Ty,Sym,En,Att,Fk> {

		@Override
		public Collection<String> deps() {
			return Collections.emptyList();
		}
		
		public final Schema<Ty,Sym,En,Att,Fk> schema;
		
		public SchExpLit(Schema<Ty,Sym,En,Att,Fk> schema) {
			if (schema == null) {
				throw new RuntimeException("Attempt to create SchExpLit with null schema");
			}
			this.schema = schema;
		}

		@Override
		public Schema<Ty,Sym,En,Att,Fk> eval(AqlEnv env) {
			return schema;
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
			SchExpLit<?,?,?,?,?> other = (SchExpLit<?,?,?,?,?>) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SchExpLit [schema=" + schema + "]";
		}
		
		@Override
		public String meta() {
			return "";
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

}