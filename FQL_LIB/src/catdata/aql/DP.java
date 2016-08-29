package catdata.aql;

public interface DP<Ty,En,Sym,Fk,Att,Gen,Sk> {

	public boolean eq(Term<Ty,En,Sym,Fk,Att,Gen,Sk> lhs, Term<Ty,En,Sym,Fk,Att,Gen,Sk> rhs);
	
	public static DP<Void,Void,Void,Void,Void,Void,Void> terminal = new DP<Void, Void, Void,Void,Void,Void,Void>() {
		@Override
		public boolean eq(Term<Void, Void, Void, Void, Void, Void, Void> lhs, Term<Void, Void, Void, Void, Void, Void, Void> rhs) {
			throw new RuntimeException();
		}	
	};
	
}
