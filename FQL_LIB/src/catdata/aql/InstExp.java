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

	public static final class InstExpRaw extends InstExp<String,String,String,String,String,String,String> {
		public final List<Pair<String, String>> options;

		public final SchExp<String, String, String, String, String> schema;

		public final List<Pair<String, String>> gens;
		public final List<Pair<String, String>> sk;

		public final List<Pair<RawTerm, RawTerm>> path_eqs, obs_eqs;

		@Override
		public String toString() {
			return "InstExpRaw [options=" + options + ", schema=" + schema + ", gens=" + gens + ", sk=" + sk + ", path_eqs=" + path_eqs + ", obs_eqs=" + obs_eqs + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((obs_eqs == null) ? 0 : obs_eqs.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((path_eqs == null) ? 0 : path_eqs.hashCode());
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			result = prime * result + ((sk == null) ? 0 : sk.hashCode());
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
			if (gens == null) {
				if (other.gens != null)
					return false;
			} else if (!gens.equals(other.gens))
				return false;
			if (obs_eqs == null) {
				if (other.obs_eqs != null)
					return false;
			} else if (!obs_eqs.equals(other.obs_eqs))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (path_eqs == null) {
				if (other.path_eqs != null)
					return false;
			} else if (!path_eqs.equals(other.path_eqs))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			if (sk == null) {
				if (other.sk != null)
					return false;
			} else if (!sk.equals(other.sk))
				return false;
			return true;
		}

		public InstExpRaw(List<Pair<String, String>> options, SchExp<String, String, String, String, String> schema, List<Pair<String, String>> gens, List<Pair<String, String>> sk, List<Pair<RawTerm, RawTerm>> path_eqs, List<Pair<RawTerm, RawTerm>> obs_eqs) {
			this.options = options;
			this.schema = schema;
			this.gens = gens;
			this.sk = sk;
			this.path_eqs = path_eqs;
			this.obs_eqs = obs_eqs;
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