package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlFdm;
import catdata.aql.Ctx;
import catdata.aql.Mapping;
import catdata.aql.Transform;
import catdata.aql.exp.InstExp.InstExpLit;
import catdata.aql.exp.InstExp.InstExpSigma;

public abstract class TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> extends Exp<Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2>> {

	public Kind kind() {
		return Kind.TRANSFORM;
	}
	
	public abstract Pair<InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1>, InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2>> 
	type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> ctx0,
	     Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx, 
	     Ctx<String, Pair<InstExp<Object,Object,Object,Object,Object,Object,Object>,  InstExp<Object,Object,Object,Object,Object,Object,Object>>> ctx1);


	///////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpSigma<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2> extends TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2> { 
		
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		public final TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2> t;

		public final List<Pair<String, String>> options1, options2;
		
		
		public TransExpSigma(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2> t, List<Pair<String, String>> options1, List<Pair<String, String>> options2) {
			this.F = F;
			this.t = t;
			this.options1 = options1;
			this.options2 = options2;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((options1 == null) ? 0 : options1.hashCode());
			result = prime * result + ((options2 == null) ? 0 : options2.hashCode());
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			TransExpSigma<?,?,?,?,?,?,?,?,?,?,?,?> other = (TransExpSigma<?,?,?,?,?,?,?,?,?,?,?,?>) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (options1 == null) {
				if (other.options1 != null)
					return false;
			} else if (!options1.equals(other.options1))
				return false;
			if (options2 == null) {
				if (other.options2 != null)
					return false;
			} else if (!options2.equals(other.options2))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}



		@Override
		public Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1>, InstExp<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object>>> ctx1) {
			Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1>, InstExp<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2>> x = t.type(ctx0, ctx, ctx1);
			//TODO schema equality
			if (!x.first.type(ctx0, ctx).equals(F.type(ctx0).first)) {
				throw new RuntimeException("In " + this + ", mapping domain is " + F.type(ctx0).first + " but transform domain schema is " + x.first.type(ctx0, ctx));
			}
			InstExp<Ty,En2,Sym,Fk2,Att2,Gen1,Sk1> a = new InstExpSigma<>(F, x.first, options1);
			InstExp<Ty,En2,Sym,Fk2,Att2,Gen2,Sk2> b = new InstExpSigma<>(F, x.second, options2);
			return new Pair<>(a,b);
		}

		@Override
		public Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2> eval(AqlEnv env) {
			Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f = F.eval(env);
			Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2> h = t.eval(env);		
			return AqlFdm.sigma(f, h, options1, options2);
	
		}

		@Override
		public String toString() {
			return "sigma " + F + " " + t;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(F.deps(), t.deps());
		}
				
	}

	public static final class TransExpId<Ty, En, Sym, Fk, Att, Gen, Sk> extends TransExp<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk> {

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return inst.deps();
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

		@Override
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen, Sk>, InstExp<Ty, En, Sym, Fk, Att, Gen, Sk>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object>>> ctx1) {
			return new Pair<>(inst, inst);
		}

	}

	///////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpVar extends TransExp<Object, Object, Object, Object, Object, Object, Object, Object, Object> {
		public final String var;

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.TRANSFORM));
		}
		
		public TransExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
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

		@Override
		public Pair<InstExp<Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object>>> ctx1) {
			return ctx1.get(var);
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpLit<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> {

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		public final Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2> trans;

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

		@Override
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object>>> ctx1) {
			return new Pair<>(new InstExpLit<>(trans.src), new InstExpLit<>(trans.dst));
		}

	}
}