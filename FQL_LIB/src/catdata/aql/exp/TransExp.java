package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Ctx;
import catdata.aql.It.ID;
import catdata.aql.Transform;
import catdata.aql.exp.InstExp.InstExpDelta;
import catdata.aql.exp.InstExp.InstExpDistinct;
import catdata.aql.exp.InstExp.InstExpLit;
import catdata.aql.exp.InstExp.InstExpSigma;
import catdata.aql.fdm.DeltaTransform;
import catdata.aql.fdm.DistinctTransform;
import catdata.aql.fdm.IdentityTransform;
import catdata.aql.fdm.SigmaDeltaCounitTransform;
import catdata.aql.fdm.SigmaDeltaUnitTransform;
import catdata.aql.fdm.SigmaTransform;


public abstract class TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends Exp<Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2>> {

	public Kind kind() {
		return Kind.TRANSFORM;
	}
	
	public abstract Pair<InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1>, InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2>> 
	type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> ctx0,
	     Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx, 
	     Ctx<String, Pair<InstExp<Object,Object,Object,Object,Object,Object,Object,Object,Object>,  InstExp<Object,Object,Object,Object,Object,Object,Object,Object,Object>>> ctx1,
	     Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs);

	///////////////////////////////////////////////////////////////////////////////////////
	
	public static class TransExpSigmaDeltaCounit<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
	extends TransExp<Ty, En2, Sym, Fk2, Att2, Pair<En1, X>, Y, Gen, Sk, ID, Chc<Y, Pair<ID, Att2>>, X, Y> {

		public final MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F; 
		public final InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I;
		public final Map<String, String> options;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			TransExpSigmaDeltaCounit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpSigmaDeltaCounit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		public TransExpSigmaDeltaCounit(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> i, Map<String, String> options) {
			F = f;
			I = i;
			this.options = options;
		}

		@Override
		public SigmaDeltaCounitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> eval(AqlEnv env) {
			return new SigmaDeltaCounitTransform<>(F.eval(env), I.eval(env), options);
		}

		@Override
		public String toString() {
			return "counit " + F + " " + I;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(F.deps(), I.deps());
		}

		@Override
		public Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Pair<En1, X>, Y, ID, Chc<Y, Pair<ID, Att2>>>, 
		            InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			SchExp<Ty, En2, Sym, Fk2, Att2> x = I.type(ctx0, ctx, qs);
			//TODO aql schema equality
			if (!x.equals(F.type(ctx0).second)) {
				throw new RuntimeException("In " + this + ", mapping codomain is " + F.type(ctx0).second + " but instance schema is " + x);
			}
			return new Pair<>(new InstExpSigma<>(F, new InstExpDelta<>(F, I), options), I);
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpSigmaDeltaUnit<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen, Sk, X, Y> 
	extends TransExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, Pair<En1, ID>, Chc<Sk,Pair<ID,Att2>>, X, Y, Pair<En1, ID>, Chc<Sk,Pair<ID,Att2>>> { 
		
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		public final InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;

		public final Map<String, String> options;

		public TransExpSigmaDeltaUnit(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, Map<String, String> options) {
			F = f;
			I = i;
			this.options = options;
		}
		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			TransExpSigmaDeltaUnit<?,?,?,?,?,?,?,?,?,?,?,?,?,?> other = (TransExpSigmaDeltaUnit<?,?,?,?,?,?,?,?,?,?,?,?,?,?>) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}	
		
		@Override
		public Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y>, 
		InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, ID>, Chc<Sk, Pair<ID, Att2>>, Pair<En1, ID>, Chc<Sk, Pair<ID, Att2>>>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			SchExp<Ty, En1, Sym, Fk1, Att1> x = I.type(ctx0, ctx, qs);
			//TODO aql schema equality
			if (!x.equals(F.type(ctx0).first)) {
				throw new RuntimeException("In " + this + ", mapping domain is " + F.type(ctx0).first + " but instance schema is " + x);
			}
			return new Pair<>(I,new InstExpDelta<>(F, new InstExpSigma<>(F, I, options)));
		}

		@Override
		public SigmaDeltaUnitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> eval(AqlEnv env) {
			return new SigmaDeltaUnitTransform<>(F.eval(env), I.eval(env), options);
		}

		@Override
		public String toString() {
			return "unit " + F + " " + I;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(F.deps(), I.deps());
		}
			
	}


	///////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpSigma<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	extends TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, ID, Chc<Sk1,Pair<ID,Att2>>, ID, Chc<Sk2,Pair<ID,Att2>>> { 
		
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		public final TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;

		public final Map<String, String> options1, options2;		
		
		public TransExpSigma(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t, Map<String, String> options1, Map<String, String> options2) {
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
			TransExpSigma<?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?> other = (TransExpSigma<?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?>) obj;
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
		public Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, ID,Chc<Sk1,Pair<ID,Att2>>>, 
		            InstExp<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2, ID,Chc<Sk2,Pair<ID,Att2>>>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, X1, Y1>, InstExp<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2, X2, Y2>> x = t.type(ctx0, ctx, ctx1, qs);
			//TODO aql schema equality
			if (!x.first.type(ctx0, ctx, qs).equals(F.type(ctx0).first)) {
				throw new RuntimeException("In " + this + ", mapping domain is " + F.type(ctx0).first + " but transform domain schema is " + x.first.type(ctx0, ctx, qs));
			}
			InstExp<Ty,En2,Sym,Fk2,Att2,Gen1,Sk1,ID,Chc<Sk1,Pair<ID,Att2>>> a = new InstExpSigma<>(F, x.first, options1);
			InstExp<Ty,En2,Sym,Fk2,Att2,Gen2,Sk2,ID,Chc<Sk2,Pair<ID,Att2>>> b = new InstExpSigma<>(F, x.second, options2);
			return new Pair<>(a,b);
		} 

		@Override
		public SigmaTransform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> eval(AqlEnv env) {
			return new SigmaTransform<>(F.eval(env), t.eval(env), options1, options2);
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
	
	public static final class TransExpDelta<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	extends TransExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2>   { 
		
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		public final TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;
		
		public TransExpDelta(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			this.F = F;
			this.t = t;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
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
			TransExpDelta<?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?> other = (TransExpDelta<?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?>) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}


		@Override
		public Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X1>, Y1>, InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2, Pair<En1, X2>, Y2>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, X1, Y1>, InstExp<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2, X2, Y2>> x = t.type(ctx0, ctx, ctx1, qs);
			//TODO aql schema equality
			if (!x.first.type(ctx0, ctx, qs).equals(F.type(ctx0).second)) {
				throw new RuntimeException("In " + this + ", mapping codomain is " + F.type(ctx0).second + " but transform domain schema is " + x.first.type(ctx0, ctx, qs));
			}
			InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X1>, Y1> a = new InstExpDelta<>(F, x.first);
			InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2, Pair<En1, X2>, Y2> b = new InstExpDelta<>(F, x.second);
			return new Pair<>(a,b);
		}

		@Override
		public Transform<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2> eval(AqlEnv env) {
			return new DeltaTransform<>(F.eval(env), t.eval(env));
		}

		@Override
		public String toString() {
			return "delta " + F + " " + t;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(F.deps(), t.deps());
		}
				
	}

	public static final class TransExpId<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> extends TransExp<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk, X, Y, X, Y> {

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return inst.deps();
		}

		public final InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> inst;

		public TransExpId(InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> inst) {
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
			TransExpId<?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpId<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
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
		public Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk, X, Y, X, Y> eval(AqlEnv env) {
			return new IdentityTransform<>(inst.eval(env));
		}

		@Override
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>, InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			return new Pair<>(inst, inst);
		}

	}

	///////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpVar extends TransExp<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> {
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
		public Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
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
		public Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			return ctx1.get(var);
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpLit<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		public final Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> trans;

		public TransExpLit(Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> trans) {
			if (trans == null) {
				throw new RuntimeException("Attempt to create TransExpLit with null schema");
			}
			this.trans = trans;
		}

		@Override
		public Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> eval(AqlEnv env) {
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
			TransExpLit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpLit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
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
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			return new Pair<>(new InstExpLit<>(trans.src()), new InstExpLit<>(trans.dst()));
		}

	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpDistinct<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2>
	extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {
		
		public final TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;

		public TransExpDistinct(TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			TransExpDistinct<?,?,?,?,?,?,?,?,?,?,?,?,?> other = (TransExpDistinct<?,?,?,?,?,?,?,?,?,?,?,?,?>) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx,
				Ctx<String, Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>> ctx1, Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> qs) {
			return new Pair<>(new InstExpDistinct<>(t.type(ctx0, ctx, ctx1, qs).first), new InstExpDistinct<>(t.type(ctx0, ctx, ctx1, qs).second));
		}

		@Override
		public Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> eval(AqlEnv env) {
			return new DistinctTransform<>(t.eval(env));
		}

		@Override
		public String toString() {
			return "distinct " + t;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return t.deps();
		}
		
		
		
		
	}
}