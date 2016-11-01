package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Ctx;
import catdata.aql.DeltaInstance;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.SigmaInstance;
import catdata.aql.TerminalInstance;
import catdata.aql.exp.SchExp.SchExpLit;

public abstract class InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> extends Exp<Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y>> {
	
	public Kind kind() {
		return Kind.INSTANCE;
	}
	
	public abstract SchExp<Ty,En,Sym,Fk,Att>  
	type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> ctx0,
	     Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx);
	
////////////////////////////////////////////////////////////////////////////////////////////////////	

	public static final class InstExpSigma<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>
	extends InstExp<Ty,En2,Sym,Fk2,Att2,Gen,Sk,GUID,Chc<Sk,Pair<GUID, Att2>>> {

		public final InstExp<Ty,En1,Sym,Fk1,Att1,Gen,Sk,X,Y> I;
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		public final List<Pair<String, String>> options;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(I.deps(), F.deps());
		}

		public InstExpSigma(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, List<Pair<String, String>> options) {
			I = i;
			F = f;
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
			InstExpSigma<?,?,?,?,?,?,?,?,?,?,?,?> other = (InstExpSigma<?,?,?,?,?,?,?,?,?,?,?,?>) obj;
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
		public SchExp<Ty, En2, Sym, Fk2, Att2> type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> ctx0, Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx) {		
			SchExp<Ty, En1, Sym, Fk1, Att1> t0 = I.type(ctx0, ctx);
			Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> t1 = F.type(ctx0);
			
			if (!t1.first.equals(t0)) { //TODO type equality
				throw new RuntimeException("Type error: In " + this + " domain of mapping is " + t1.first + " but instance has schema " + t0);
			} 
			
			return t1.second;
		}

		@Override
		public String toString() {
			return "sigma " + F + " " + I;
		}

		@Override
		public SigmaInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> eval(AqlEnv env) {
			Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f = F.eval(env);
			Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i = I.eval(env);		
			return new SigmaInstance<>(f, i, options);
		}
		
	}
	
	public static final class InstExpDelta<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2,Gen,Sk,X,Y>
	extends InstExp<Ty,En1,Sym,Fk1,Att1,Pair<En1,X>,Y,Pair<En1,X>,Y> {

		public final InstExp<Ty,En2,Sym,Fk2,Att2,Gen,Sk,X,Y> I;
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(I.deps(), F.deps());
		}

		public InstExpDelta(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> i) {
			I = i;
			F = f; 
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			InstExpDelta<?,?,?,?,?,?,?,?,?,?,?,?> other = (InstExpDelta<?,?,?,?,?,?,?,?,?,?,?,?>) obj;
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
			return true;
		}

		@Override
		public SchExp<Ty, En1, Sym, Fk1, Att1> type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> ctx0, Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx) {		
			SchExp<Ty, En2, Sym, Fk2, Att2> t0 = I.type(ctx0, ctx);
			Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> t1 = F.type(ctx0);
			
			if (!t1.second.equals(t0)) { //TODO type equality
				throw new RuntimeException("Type error: In " + this + " codomain of mapping is " + t1.first + " but instance has schema " + t0);
			} 
			
			return t1.first;
		}

		@Override
		public String toString() {
			return "delta " + F + " " + I;
		}

		@Override
		public Instance<Ty,En1,Sym,Fk1,Att1,Pair<En1,X>,Y,Pair<En1,X>,Y> eval(AqlEnv env) {
			Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f = F.eval(env);
			Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> i = I.eval(env);		
			return new DeltaInstance<>(f, i);
		}
		
	}
	
	public static final class InstExpEmpty<Ty,Sym,En,Fk,Att> extends InstExp<Ty,En,Sym,Fk,Att,Void,Void,Void,Void> {
		
		public final SchExp<Ty,En,Sym,Fk,Att> schema;

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return schema.deps();
		}
		
		public InstExpEmpty(SchExp<Ty, En, Sym, Fk, Att> schema) {
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
		public TerminalInstance<Ty, En, Sym, Fk, Att> eval(AqlEnv env) {
			return new TerminalInstance<>(schema.eval(env)); 
		}

		@Override
		public SchExp<Ty, En, Sym, Fk, Att> type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>, SchExp<Object,Object,Object,Object,Object>>> ctx0, Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx) {
			return schema;
		}
		
	}

	public static final class InstExpVar extends InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object> {
		public final String var;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.INSTANCE));
		}
		
		public InstExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create InstExpVar will null var");
			}
			this.var = var;
		}

		@Override
		public Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
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
			InstExpVar other = (InstExpVar) obj;
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
		public SchExp<Object, Object, Object, Object, Object> type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>,  SchExp<Object,Object,Object,Object,Object>>> ctx0, Ctx<String, SchExp<Object, Object, Object, Object, Object>> ctx) {
			return ctx.get(var);
		}

		
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class InstExpLit<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> {

		public final Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> inst;
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		
		public InstExpLit(Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> inst) {
			if (inst == null) {
				throw new RuntimeException("Attempt to create InstExpLit with null schema");
			}
			this.inst = inst;
		}

		@Override
		public Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> eval(AqlEnv env) {
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
			InstExpLit<?,?,?,?,?,?,?,?,?> other = (InstExpLit<?,?,?,?,?,?,?,?,?>) obj;
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
		public SchExp<Ty, En, Sym, Fk, Att> type(Ctx<String, Pair<SchExp<Object,Object,Object,Object,Object>, SchExp<Object,Object,Object,Object,Object>>> ctx0, Ctx<String, SchExp<Object,Object,Object,Object,Object>> ctx) {
			return new SchExpLit<>(inst.schema());
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	//TODO: fix types in raws


}