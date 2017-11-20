package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.Transform;
import catdata.aql.Var;
import catdata.aql.exp.InstExp.InstExpCoEval;
import catdata.aql.exp.InstExp.InstExpDelta;
import catdata.aql.exp.InstExp.InstExpDistinct;
import catdata.aql.exp.InstExp.InstExpEval;
import catdata.aql.exp.InstExp.InstExpLit;
import catdata.aql.exp.InstExp.InstExpSigma;
import catdata.aql.fdm.CoEvalEvalCoUnitTransform;
import catdata.aql.fdm.CoEvalEvalUnitTransform;
import catdata.aql.fdm.CoEvalTransform;
import catdata.aql.fdm.DeltaTransform;
import catdata.aql.fdm.DistinctTransform;
import catdata.aql.fdm.EvalAlgebra.Row;
import catdata.aql.fdm.EvalTransform;
import catdata.aql.fdm.IdentityTransform;
import catdata.aql.fdm.SigmaDeltaCounitTransform;
import catdata.aql.fdm.SigmaDeltaUnitTransform;
import catdata.aql.fdm.SigmaTransform;


public abstract class TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends Exp<Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2>> {

	@Override
	public Kind kind() {
		return Kind.TRANSFORM;
	}
	
	public abstract Pair<InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1>, InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2>> type(AqlTyping G);

	////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	public static class TransExpCoEvalEvalCoUnit<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>  
	extends TransExp<Ty, En1, Sym, Fk1, Att1, Pair<Var,Row<En2,X>>, Y, Gen, Sk, ID, Chc<Y, Pair<ID, Att1>>, X, Y> {

		public final QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q; 
		public final InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;
		public final Map<String,String> options;
			
		@Override
		public Map<String, String> options() {
			return options;
		}
		
		public TransExpCoEvalEvalCoUnit(QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, Map<String, String> options) {
			Q = q;
			I = i;
			this.options = options;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((Q == null) ? 0 : Q.hashCode());
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
			TransExpCoEvalEvalCoUnit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpCoEvalEvalCoUnit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			if (Q == null) {
				if (other.Q != null)
					return false;
			} else if (!Q.equals(other.Q))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "counit_query " + Q + " " + I;
		}

		@Override
		public Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Pair<Var, Row<En2, X>>, Y, ID, Chc<Y, Pair<ID, Att1>>>, InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y>> type(AqlTyping G) {
			if (!Q.type(G).first.equals(I.type(G))) {
				throw new RuntimeException("Q has src schema " + Q.type(G).first + " but instance has schema " + I.type(G));
			}
			return new Pair<>(new InstExpCoEval<>(Q,new InstExpEval<>(Q, I, Util.toList(options)), Util.toList(options)),I);
		}

		@Override
		public Transform<Ty, En1, Sym, Fk1, Att1, Pair<Var, Row<En2, X>>, Y, Gen, Sk, ID, Chc<Y, Pair<ID, Att1>>, X, Y> eval(AqlEnv env) {
			return new CoEvalEvalCoUnitTransform<>(Q.eval(env), I.eval(env), new AqlOptions(options, null, env.defaults));
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(Q.deps(), I.deps());
		}
		
		
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	
	public static class TransExpCoEvalEvalUnit<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>  
	extends TransExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, Row<En2,ID>, Chc<Y, Pair<ID, Att1>>, X, Y, Row<En2,ID>, Chc<Y, Pair<ID, Att1>>> {
		//TODO aql recomputes
		public final QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q; 
		public final InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I;
		public final Map<String, String> options;
		
		@Override
		public Map<String, String> options() {
			return options;
		}
		
		public TransExpCoEvalEvalUnit(QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> i, Map<String, String> options) {
			Q = q;
			I = i;
			this.options = options;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((Q == null) ? 0 : Q.hashCode());
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
			TransExpCoEvalEvalUnit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpCoEvalEvalUnit<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			if (Q == null) {
				if (other.Q != null)
					return false;
			} else if (!Q.equals(other.Q))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "unit_query " + Q + " " + I;
		}

		@Override
		public Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y>, InstExp<Ty, En2, Sym, Fk2, Att2, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>>> type(AqlTyping G) {
			if (!Q.type(G).second.equals(I.type(G))) {
				throw new RuntimeException("Q has dst schema " + Q.type(G).second + " but instance has schema " + I.type(G));
			}
			return new Pair<>(I, new InstExpEval<>(Q,new InstExpCoEval<>(Q, I, Util.toList(options)), Util.toList(options)));
		}

		@Override
		public Transform<Ty, En2, Sym, Fk2, Att2, Gen, Sk, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>, X, Y, Row<En2, ID>, Chc<Y, Pair<ID, Att1>>> eval(AqlEnv env) {
			return new CoEvalEvalUnitTransform<>(Q.eval(env), I.eval(env), new AqlOptions(options, null, env.defaults));
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(Q.deps(), I.deps());
		}
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	//TODO aql options weirdness
	public static class TransExpCoEval<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	extends TransExp<Ty, En1, Sym, Fk1, Att1, Pair<Var,X1>, Y1, Pair<Var,X2>, Y2, ID, Chc<Y1, Pair<ID, Att1>>, ID, Chc<Y2, Pair<ID, Att1>>> {

		public final QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
		public final TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;
		private final Map<String,String> options1, options2;
		private final List<Pair<String, String>> o1, o2;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public TransExpCoEval(QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t, List<Pair<String, String>> o1, List<Pair<String, String>> o2) {
			this.t = t;
			Q = q;
			options1 = Util.toMapSafely(o1);
			options2 = Util.toMapSafely(o2);
			this.o1 = o1;
			this.o2 = o2;
		}

		@Override
		public Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Pair<Var, X1>, Y1, ID, Chc<Y1, Pair<ID, Att1>>>, InstExp<Ty, En1, Sym, Fk1, Att1, Pair<Var, X2>, Y2, ID, Chc<Y2, Pair<ID, Att1>>>> type(AqlTyping G) {
			if (!t.type(G).first.type(G).equals(Q.type(G).second)) {
				throw new RuntimeException("Target of query is " + t.type(G).second.type(G) + " but transform is on " + t.type(G).first);
			}
			return new Pair<>(new InstExpCoEval<>(Q, t.type(G).first, o1), new InstExpCoEval<>(Q, t.type(G).second, o2));
		}

		@Override
		public Transform<Ty, En1, Sym, Fk1, Att1, Pair<Var, X1>, Y1, Pair<Var, X2>, Y2, ID, Chc<Y1, Pair<ID, Att1>>, ID, Chc<Y2, Pair<ID, Att1>>> eval(AqlEnv env) {
			return new CoEvalTransform<>(Q.eval(env), t.eval(env), new AqlOptions(options1, null, env.defaults), new AqlOptions(options2, null, env.defaults));
		}

		@Override
		public String toString() {
			return "coeval " + Q + " " + t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((Q == null) ? 0 : Q.hashCode());
			result = prime * result + ((o1 == null) ? 0 : o1.hashCode());
			result = prime * result + ((o2 == null) ? 0 : o2.hashCode());
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
			TransExpCoEval<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpCoEval<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (Q == null) {
				if (other.Q != null)
					return false;
			} else if (!Q.equals(other.Q))
				return false;
			if (o1 == null) {
				if (other.o1 != null)
					return false;
			} else if (!o1.equals(other.o1))
				return false;
			if (o2 == null) {
				if (other.o2 != null)
					return false;
			} else if (!o2.equals(other.o2))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(Q.deps(), t.deps());
		}

	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public static class TransExpEval<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> 
	extends TransExp<Ty, En2, Sym, Fk2, Att2, Row<En2,X1>, Y1, Row<En2,X2>, Y2, Row<En2,X1>, Y1, Row<En2,X2>, Y2>  {

		public final QueryExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> Q;
		public final TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public TransExpEval(QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q,TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			this.t = t;
			Q = q;
		}

		@Override
		public Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Row<En2, X1>, Y1, Row<En2, X1>, Y1>, InstExp<Ty, En2, Sym, Fk2, Att2, Row<En2, X2>, Y2, Row<En2, X2>, Y2>> type(AqlTyping G) {
			if (!t.type(G).first.type(G).equals(Q.type(G).first)) {
				throw new RuntimeException("Source of query is " + t.type(G).first.type(G) + " but transform is on " + t.type(G).first);
			}
			return new Pair<>(new InstExpEval<>(Q, t.type(G).first, Collections.emptyList()), new InstExpEval<>(Q, t.type(G).second, Collections.emptyList()));
		}

		@Override
		public Transform<Ty, En2, Sym, Fk2, Att2, Row<En2, X1>, Y1, Row<En2, X2>, Y2, Row<En2, X1>, Y1, Row<En2, X2>, Y2> eval(AqlEnv env) {
			return new EvalTransform<>(Q.eval(env), t.eval(env), env.defaults);
		}

		@Override
		public String toString() {
			return "eval " + Q + " " + t;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((Q == null) ? 0 : Q.hashCode());
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
			TransExpEval<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpEval<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (Q == null) {
				if (other.Q != null)
					return false;
			} else if (!Q.equals(other.Q))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(Q.deps(), t.deps());
		}

	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public static class TransExpSigmaDeltaCounit<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
	extends TransExp<Ty, En2, Sym, Fk2, Att2, Pair<En1, X>, Y, Gen, Sk, ID, Chc<Y, Pair<ID, Att2>>, X, Y> {

		public final MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F; 
		public final InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> I;
		public final Map<String, String> options;
		
		@Override
		public Map<String, String> options() {
			return options;
		}
		
		@Override
		public int hashCode() {
			int prime = 31;
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
			return new SigmaDeltaCounitTransform<>(F.eval(env), I.eval(env), new AqlOptions(options, null, env.defaults));
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
		            InstExp<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y>> type(AqlTyping G) {
			SchExp<Ty, En2, Sym, Fk2, Att2> x = I.type(G);
			//TODO aql schema equality
			if (!G.eq(x, F.type(G).second)) {
				throw new RuntimeException("In " + this + ", mapping codomain is " + F.type(G).second + " but instance schema is " + x);
			}
			return new Pair<>(new InstExpSigma<>(F, new InstExpDelta<>(F, I), options), I);
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpSigmaDeltaUnit<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2, Gen, Sk, X, Y> 
	extends TransExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, Pair<En1, ID>, Chc<Sk,Pair<ID,Att2>>, X, Y, Pair<En1, ID>, Chc<Sk,Pair<ID,Att2>>> { 
		
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> F;
		public final InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;

		public final Map<String, String> options;
		
		@Override
		public Map<String, String> options() {
			return options;
		}
		
		public TransExpSigmaDeltaUnit(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, InstExp<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, Map<String, String> options) {
			F = f;
			I = i;
			this.options = options;
		}
		

		@Override
		public int hashCode() {
			int prime = 31;
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
			TransExpSigmaDeltaUnit<?,?,?,?,?,?,?,?,?,?,?,?> other = (TransExpSigmaDeltaUnit<?,?,?,?,?,?,?,?,?,?,?,?>) obj;
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
		InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, ID>, Chc<Sk, Pair<ID, Att2>>, Pair<En1, ID>, Chc<Sk, Pair<ID, Att2>>>> type(AqlTyping G) {
			SchExp<Ty, En1, Sym, Fk1, Att1> x = I.type(G);
			//TODO aql schema equality
			if (!G.eq(x, F.type(G).first)) {
				throw new RuntimeException("In " + this + ", mapping domain is " + F.type(G).first + " but instance schema is " + x);
			}
			return new Pair<>(I,new InstExpDelta<>(F, new InstExpSigma<>(F, I, options)));
		}

		@Override
		public SigmaDeltaUnitTransform<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> eval(AqlEnv env) {
			return new SigmaDeltaUnitTransform<>(F.eval(env), I.eval(env), new AqlOptions(options, null, env.defaults));
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

		public final Map<String, String> options1, options2;	 //TODO aql options weirdness
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		public TransExpSigma(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, TransExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t, Map<String, String> options1, Map<String, String> options2) {
			this.F = F;
			this.t = t;
			this.options1 = options1;
			this.options2 = options2;
		}
		
		@Override
		public int hashCode() {
			int prime = 31;
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
		            InstExp<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2, ID,Chc<Sk2,Pair<ID,Att2>>>> type(AqlTyping G) {
			Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, X1, Y1>, InstExp<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2, X2, Y2>> x = t.type(G);
			//TODO aql schema equality
			if (!G.eq(x.first.type(G), F.type(G).first)) {
				throw new RuntimeException("In " + this + ", mapping domain is " + F.type(G).first + " but transform domain schema is " + x.first.type(G));
			}
			InstExp<Ty,En2,Sym,Fk2,Att2,Gen1,Sk1,ID,Chc<Sk1,Pair<ID,Att2>>> a = new InstExpSigma<>(F, x.first, options1);
			InstExp<Ty,En2,Sym,Fk2,Att2,Gen2,Sk2,ID,Chc<Sk2,Pair<ID,Att2>>> b = new InstExpSigma<>(F, x.second, options2);
			return new Pair<>(a,b);
		} 

		@Override
		public SigmaTransform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2> eval(AqlEnv env) {
			return new SigmaTransform<>(F.eval(env), t.eval(env), new AqlOptions(options1, null, env.defaults), new AqlOptions(options2, null, env.defaults));
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
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public TransExpDelta(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, TransExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			this.F = F;
			this.t = t;
		}
		
		@Override
		public int hashCode() {
			int prime = 31;
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
		public Pair<InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X1>, Y1>, InstExp<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2, Pair<En1, X2>, Y2>> type(AqlTyping G) {
			Pair<InstExp<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, X1, Y1>, InstExp<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2, X2, Y2>> x = t.type(G);
			//TODO aql schema equality
			if (!G.eq(x.first.type(G), F.type(G).second)) {
				throw new RuntimeException("In " + this + ", mapping codomain is " + F.type(G).second + " but transform domain schema is " + x.first.type(G));
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
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
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
			int prime = 31;
			int result = 1;
			result = prime * result + inst.hashCode();
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
            return inst.equals(other.inst);
        }

		@Override
		public String toString() {
			return "identity " + inst;
		}

		@Override
		public Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk, X, Y, X, Y> eval(AqlEnv env) {
			return new IdentityTransform<>(inst.eval(env));
		}

		@Override
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>, InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>> type(AqlTyping G) {
			return new Pair<>(inst, inst);
		}
		
		

	}

	///////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpVar extends TransExp<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> {
		public final String var;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.TRANSFORM));
		}
		
		public TransExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.defs.trans.get(var);
		}

		@Override
		public int hashCode() {
			int prime = 31;
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

		@SuppressWarnings("unchecked")
		@Override
		public Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>, InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>> type(AqlTyping G) {
			if (!G.defs.trans.containsKey(var)) {
				throw new RuntimeException("Not a transform: " + var);
			}
			return (Pair<InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>,InstExp<Object, Object, Object, Object, Object, Object, Object, Object, Object>>) ((Object)G.defs.trans.get(var));
		}
		
	

	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TransExpLit<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
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
			int prime = 31;
			int result = 1;
			result = prime * result + (trans.hashCode());
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
            return trans.equals(other.trans);
        }

		@Override
		public String toString() {
			return trans.toString();
		} 

		@Override
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> type(AqlTyping G) {
			return new Pair<>(new InstExpLit<>(trans.src()), new InstExpLit<>(trans.dst()));
		}

		
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static final class TransExpDistinct<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2>
	extends TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {
		
		public final TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public TransExpDistinct(TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			this.t = t;
		}

		@Override
		public int hashCode() {
			int prime = 31;
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
		public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> type(AqlTyping G) {
			return new Pair<>(new InstExpDistinct<>(t.type(G).first), new InstExpDistinct<>(t.type(G).second));
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