package catdata.aql;

import java.util.List;

import catdata.Pair;

public abstract class TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> extends Exp<Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2>> {
	
	
	public Kind kind() {
		return Kind.TRANSFORM;
	}

///////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpRaw extends TransExp<String,String,String,String,String,String,String,String,String> {
		
		public final InstExp<?,?,?,?,?,?,?> src, dst;
		
		public final List<String> imports;
		
		public final List<Pair<String, RawTerm>> gens;		
		
		public final List<Pair<String, String>> options;
		
		@Override
		public String meta() {
			return " : " + src + " -> " + dst;
		}

		@Override
		public String toString() {
			return "TransExpRaw [src=" + src + ", dst=" + dst + ", imports=" + imports + ", gens=" + gens + ", options=" + options + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((imports == null) ? 0 : imports.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			TransExpRaw other = (TransExpRaw) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
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
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		public TransExpRaw(InstExp<?, ?, ?, ?, ?, ?, ?> src, InstExp<?, ?, ?, ?, ?, ?, ?> dst, List<String> imports, List<Pair<String, RawTerm>> gens, List<Pair<String, String>> options) {
			this.src = src;
			this.dst = dst;
			this.imports = imports;
			this.gens = gens;
			this.options = options;
		}

		@Override
		public Transform<String, String, String, String, String, String, String, String, String> eval(Env env) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
///////////////////////////////////////////////////////////////////////////////////////
	
public static final class TransExpId<Ty,En,Sym,Fk,Att,Gen,Sk> extends TransExp<Ty,En,Sym,Fk,Att,Gen,Sk,Gen,Sk> {
		
		@Override
		public String meta() {
			return " : " + inst + " -> " + inst;
		}
		
		public final InstExp<Ty,En,Sym,Fk,Att,Gen,Sk> inst;

		public TransExpId(InstExp<Ty, En, Sym, Fk, Att, Gen, Sk> inst) {
			if (inst == null) {
				throw new RuntimeException("Attempt to create TransExpId with null instance");
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
			TransExpId<?,?,?,?,?,?,?> other = (TransExpId<?,?,?,?,?,?,?>) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "id " + inst;
		}

		@Override
		public Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk> eval(Env env) {
			return Transform.id(inst.eval(env));
		}
		
	}
	
///////////////////////////////////////////////////////////////////////////////////////	

	public static final class TransExpVar<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> extends TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> {
		public final String var;
		
		public TransExpVar(String var) {
			this.var = var;
		}

		@Override
		public String meta() {
			return "";
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> eval(Env env) {
			return env.getTransform(var);
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
			TransExpVar<?,?,?,?,?,?,?,?,?> other = (TransExpVar<?,?,?,?,?,?,?,?,?>) obj;
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
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpLit<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> extends TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> {

		public final Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> trans;
		
		@Override
		public String meta() {
			return "";
		}
		
		public TransExpLit(Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> trans) {
			if (trans == null) {
				throw new RuntimeException("Attempt to create TransExpLit with null schema");
			}
			this.trans = trans;
		}

		@Override
		public Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2> eval(Env env) {
			return trans;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((trans == null) ? 0 : trans.hashCode());
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
			TransExpLit<?,?,?,?,?,?,?,?,?> other = (TransExpLit<?,?,?,?,?,?,?,?,?>) obj;
			if (trans == null) {
				if (other.trans != null)
					return false;
			} else if (!trans.equals(other.trans))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TransExpLit [trans=" + trans + "]";
		}
		
		
	}
}