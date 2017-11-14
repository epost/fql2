package catdata.aql;

import catdata.Chc;
import catdata.Ctx;

//TODO aql all DPs just have to handle java now
public interface DP<Ty,En,Sym,Fk,Att,Gen,Sk> {
	
	public abstract String toStringProver();
		
	boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs);
	
	default boolean hasNFs() {
		return false;
	}
	
	@SuppressWarnings("unused")
	default Term<Ty,En,Sym,Fk,Att,Gen,Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		throw new RuntimeException("Anomaly: please report");
	}
	
	DP<Void,Void,Void,Void,Void,Void,Void> terminal = new DP<Void, Void, Void,Void,Void,Void,Void>() {
		@Override
		public boolean eq(Ctx<Var, Chc<Void, Void>> ctx, Term<Void, Void, Void, Void, Void, Void, Void> lhs, Term<Void, Void, Void, Void, Void, Void, Void> rhs) {
			throw new RuntimeException();
		}

		@Override
		public boolean hasNFs() {
			return true;
		}

		@Override
		public Term<Void, Void, Void, Void, Void, Void, Void> nf(Ctx<Var, Chc<Void, Void>> ctx, Term<Void, Void, Void, Void, Void, Void, Void> term) {
			throw new RuntimeException();
		}

		@Override
		public String toStringProver() {
			return "Theorem prover for empty theory";
		}

	};
	
	
	
}
