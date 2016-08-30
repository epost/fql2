package catdata.aql;

public abstract class TransExp extends Exp<Transform> {
	
	
	public Kind kind() {
		return Kind.TRANSFORM;
	}
	
	

	public static final class TransExpVar extends TransExp {
		String var;
		
		@Override
		public Transform eval(Env env) {
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
			TransExpVar other = (TransExpVar) obj;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TransExpVar [var=" + var + "]";
		}
		
		
	}
	
	public static final class TransExpLit extends TransExp {

		public final Transform trans;
		
		public TransExpLit(Transform trans) {
			if (trans == null) {
				throw new RuntimeException("Attempt to create TransExpLit with null schema");
			}
			this.trans = trans;
		}

		@Override
		public Transform eval(Env env) {
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
			TransExpLit other = (TransExpLit) obj;
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