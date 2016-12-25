package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Schema;

public abstract class SchExp<Ty,En,Sym,Fk,Att> extends Exp<Schema<Ty,En,Sym,Fk,Att>> {	
	
	@Override
	public Kind kind() {
		return Kind.SCHEMA;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpInst<Ty,En,Sym,Fk,Att> extends SchExp<Ty,En,Sym,Fk,Att> {
		public final InstExp<Ty,En,Sym,Fk,Att,?,?,?,?> inst;

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return inst.deps();
		}
			
		public SchExpInst(InstExp<Ty, En, Sym, Fk, Att, ?, ?, ?, ?> inst) {
			if (inst == null) {
				throw new RuntimeException("Attempt to get schema for null instance");
			}
			this.inst = inst;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (inst.hashCode());
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
            return inst.equals(other.inst);
        }

		@Override
		public String toString() {
			return "schemaOf " + inst + "]";
		}

		@Override
		public Schema<Ty, En, Sym, Fk, Att> eval(AqlEnv env) {
			return inst.eval(env).schema();
		}

		@Override
		public long timeout() {
			return inst.timeout();
		}
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpEmpty<Ty,Sym> extends SchExp<Ty,Void,Sym,Void,Void> {
		
		public final TyExp<Ty,Sym> typeSide;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
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
			int prime = 31;
			int result = 1;
			result = prime * result + (typeSide.hashCode());
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
            return typeSide.equals(other.typeSide);
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
		public long timeout() {
			return typeSide.timeout();
		}
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpVar extends SchExp<Object, Object, Object, Object, Object> {
		
		public final String var;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.SCHEMA));
		}
		
		public SchExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create SchExpVar will null var");
			}
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Schema<Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.defs.schs.get(var);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (var.hashCode());
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
            return var.equals(other.var);
        }

		@Override
		public String toString() {
			return var;
		}

		@Override
		public long timeout() {
			return 0;
		}
		
		
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpLit<Ty,Sym,En,Fk,Att> extends SchExp<Ty,Sym,En,Fk,Att> {

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		
		public final Schema<Ty,Sym,En,Fk,Att> schema;
		
		public SchExpLit(Schema<Ty,Sym,En,Fk,Att> schema) {
			if (schema == null) {
				throw new RuntimeException("Attempt to create SchExpLit with null schema");
			}
			this.schema = schema;
		}

		@Override
		public Schema<Ty,Sym,En,Fk,Att> eval(AqlEnv env) {
			return schema;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (schema.hashCode());
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
            return schema.equals(other.schema);
        }

		@Override
		public String toString() {
			return "SchExpLit [schema=" + schema + "]";
		}

		@Override
		public long timeout() {
			return 0;
		}
		
	
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

}