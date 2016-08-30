package catdata.aql;

import java.util.List;

import catdata.Pair;
import catdata.Triple;

public abstract class SchExp<Ty,En,Sym,Att,Fk> extends Exp<Schema<Ty,En,Sym,Att,Fk>> {	
	
	@Override
	public Kind kind() {
		return Kind.SCHEMA;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpInst<Ty,En,Sym,Att,Fk> extends SchExp<Ty,En,Sym,Att,Fk> {
		public final InstExp<Ty,En,Sym,Att,Fk,?,?> inst;

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
		public Schema<Ty, En, Sym, Att, Fk> eval(Env env) {
			return inst.eval(env).schema;
		}
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class SchExpEmpty<Ty,Sym> extends SchExp<Ty,Void,Sym,Void,Void> {
		
		public final TyExp<Ty,Sym> typeSide;

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
		public Schema<Ty, Void, Sym, Void, Void> eval(Env env) {
			return Schema.terminal(typeSide.eval(env));
		}
		
		@Override
		public String meta() {
			return " : " + typeSide;
		}
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	public static final class SchExpVar<Ty,Sym,En,Att,Fk> extends SchExp<Ty,Sym,En,Att,Fk> {
		
		public final String var;
		
		public SchExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create SchExpVar will null var");
			}
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Schema<Ty,Sym,En,Att,Fk> eval(Env env) {
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

		public final Schema<Ty,Sym,En,Att,Fk> schema;
		
		public SchExpLit(Schema<Ty,Sym,En,Att,Fk> schema) {
			if (schema == null) {
				throw new RuntimeException("Attempt to create SchExpLit with null schema");
			}
			this.schema = schema;
		}

		@Override
		public Schema<Ty,Sym,En,Att,Fk> eval(Env env) {
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

	public static final class SchExpRaw extends SchExp<String,String,String,String,String>  {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
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
		SchExpRaw other = (SchExpRaw) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (typeSide == null) {
			if (other.typeSide != null)
				return false;
		} else if (!typeSide.equals(other.typeSide))
			return false;
		return true;
	}
	
	

	@Override
	public String toString() {
		return "SchExpRaw [typeSide=" + typeSide + ", ens=" + ens + ", atts=" + atts + ", fks=" + fks + ", eqs=" + eqs + ", options=" + options + "]";
	}

	public SchExpRaw(TyExp<String, String> typeSide, List<String> ens, List<Triple<String, String, String>> atts, List<Triple<String, String, String>> fks, List<Triple<Pair<String, String>, RawTerm, RawTerm>> eqs, List<Pair<String, String>> options) {
		this.typeSide = typeSide;
		this.ens = ens;
		this.atts = atts;
		this.fks = fks;
		this.eqs = eqs;
		this.options = options;
	}

	@Override
	public String meta() {
		return " : " + typeSide;
	}
	
	public final TyExp<String,String> typeSide;
	
	public final List<String> ens;
	public final List<Triple<String, String, String>> atts, fks;		
	public final List<Triple<Pair<String, String>, RawTerm, RawTerm>> eqs;
	
	public final List<Pair<String, String>> options;

	@Override
	public Schema<String, String, String, String, String> eval(Env env) {
		// TODO Auto-generated method stub
		return null;
	} 
	
	
	

}
}