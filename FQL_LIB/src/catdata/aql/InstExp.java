package catdata.aql;

import java.util.List;

import catdata.Pair;

public abstract class InstExp<Ty,Sym,En,Att,Fk,Gen,Sk> extends Exp<Instance<Ty,Sym,En,Att,Fk,Gen,Sk>> {
	
	public Kind kind() {
		return Kind.INSTANCE;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public static final class InstExpEmpty<Ty,Sym,En,Att,Fk> extends InstExp<Ty,Sym,En,Att,Fk,Void,Void> {
		
		public final SchExp<Ty,Sym,En,Att,Fk> schema;

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
		public Instance<Ty, Sym, En, Att, Fk, Void, Void> eval(Env env) {
			return Instance.terminal(schema.eval(env));
		}

		@Override
		public String meta() {
			return " : " + schema;
		}
		
	}

	public static final class InstExpVar<Ty,Sym,En,Att,Fk,Gen,Sk> extends InstExp<Ty,Sym,En,Att,Fk,Gen,Sk> {
		public final String var;
		
		public InstExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create InstExpVar will null var");
			}
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Instance<Ty,Sym,En,Att,Fk,Gen,Sk> eval(Env env) {
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
			InstExpVar<?,?,?,?,?,?,?> other = (InstExpVar<?,?,?,?,?,?,?>) obj;
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
		
		public InstExpLit(Instance<Ty,Sym,En,Att,Fk,Gen,Sk> inst) {
			if (inst == null) {
				throw new RuntimeException("Attempt to create InstExpLit with null schema");
			}
			this.inst = inst;
		}

		@Override
		public Instance<Ty,Sym,En,Att,Fk,Gen,Sk> eval(Env env) {
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
	
	public static final class InstExpRaw extends InstExp<String,String,String,String,String,String,String> {

		public final SchExp<?, ?, ?, ?, ?> schema;
		
		public final List<String> imports;

		public final List<Pair<String, String>> gens;

		public final List<Pair<RawTerm, RawTerm>> eqs;
		
		public final List<Pair<String, String>> options;

		public InstExpRaw(SchExp<?, ?, ?, ?, ?> schema, List<String> imports, List<Pair<String, String>> gens, List<Pair<RawTerm, RawTerm>> eqs, List<Pair<String, String>> options) {
			this.schema = schema;
			this.imports = imports;
			this.gens = gens;
			this.eqs = eqs;
			this.options = options;
		}

			@Override
		public String toString() {
			return "InstExpRaw [schema=" + schema + ", imports=" + imports + ", gens=" + gens + ", eqs=" + eqs + ", options=" + options + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((imports == null) ? 0 : imports.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			InstExpRaw other = (InstExpRaw) obj;
			if (eqs == null) {
				if (other.eqs != null)
					return false;
			} else if (!eqs.equals(other.eqs))
				return false;
			if (gens == null) {
				if (other.gens != null)
					return false;
			} else if (!gens.equals(other.gens))
				return false;
			if (imports == null) {
				if (other.imports != null)
					return false;
			} else if (!imports.equals(other.imports))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public Instance<String, String, String, String, String, String, String> eval(Env env) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String meta() {
			return " : " + schema;
		}

		
		
		
	}

}