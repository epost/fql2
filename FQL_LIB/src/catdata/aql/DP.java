package catdata.aql;

import catdata.Chc;

public interface DP<Ty,En,Sym,Fk,Att,Gen,Sk> {

	public boolean eq(Ctx<Var, Chc<Ty,En>> ctx, Term<Ty,En,Sym,Fk,Att,Gen,Sk> lhs, Term<Ty,En,Sym,Fk,Att,Gen,Sk> rhs);
	
	public boolean hasNFs();
	
	public Term<Ty,En,Sym,Fk,Att,Gen,Sk> nf(Ctx<Var, Chc<Ty,En>> ctx, Term<Ty,En,Sym,Fk,Att,Gen,Sk> term);
	
	public static DP<Void,Void,Void,Void,Void,Void,Void> terminal = new DP<Void, Void, Void,Void,Void,Void,Void>() {
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
	};
	
	
	
}
