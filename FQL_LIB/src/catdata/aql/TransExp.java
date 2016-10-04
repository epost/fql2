package catdata.aql;

import java.util.Collection;
import java.util.Collections;

import catdata.Util;

public abstract class TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> extends Exp<Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2>> {

	public Kind kind() {
		return Kind.TRANSFORM;
	}

	///////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpId<Ty, En, Sym, Fk, Att, Gen, Sk> extends TransExp<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk> {

		@Override
		public Collection<String> deps() {
			return inst.deps();
		}

		@Override
		public String meta() {
			return " : " + inst + " -> " + inst;
		}

		public final InstExp<Ty, En, Sym, Fk, Att, Gen, Sk> inst;

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
			TransExpId<?, ?, ?, ?, ?, ?, ?> other = (TransExpId<?, ?, ?, ?, ?, ?, ?>) obj;
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
		public Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk> eval(AqlEnv env) {
			return Transform.id(inst.eval(env));
		}

	}

	///////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpVar extends TransExp<Object, Object, Object, Object, Object, Object, Object, Object, Object> {
		public final String var;

		@Override
		public Collection<String> deps() {
			return Util.singList(var);
		}
		
		public TransExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@Override
		public String meta() {
			return "";
		}

		@Override
		public Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
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
			return var;
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpLit<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> {

		@Override
		public Collection<String> deps() {
			return Collections.emptyList();
		}
		public final Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> trans;

		@Override
		public String meta() {
			return "";
		}

		public TransExpLit(Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> trans) {
			if (trans == null) {
				throw new RuntimeException("Attempt to create TransExpLit with null schema");
			}
			this.trans = trans;
		}

		@Override
		public Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> eval(AqlEnv env) {
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
			TransExpLit<?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpLit<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
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