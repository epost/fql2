package catdata.aql.fdm;

import java.util.Set;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.aql.Algebra;
import catdata.aql.DP;
import catdata.aql.Instance;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;
import catdata.aql.fdm.EvalAlgebra.Row;

//TODO aql instance DPs only have to extend schema DP to ground terms in instance 
public class EvalInstance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
extends Instance<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y, Row<En2,X>, Y>  
 implements DP<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y>  {	
	
	private final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y>  I;
	private final EvalAlgebra<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> alg;
	private final SaturatedInstance<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y, Row<En2,X>, Y> J;
	
	public EvalInstance(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i) {
		if (!q.src.equals(i.schema())) {
			throw new RuntimeException("In eval instance, source of query is " + q.src + ", but instance has type " + i.schema());
		}

		Q = q;
		I = i;
		alg = new EvalAlgebra<>(Q, I);
		J = new SaturatedInstance<>(alg, dp());
	}

	@Override
	public Schema<Ty, En2, Sym, Fk2, Att2> schema() {
		return Q.dst;
	}

	@Override
	public Ctx<Row<En2,X>, En2> gens() {
		return J.gens();
	}

	@Override
	public Ctx<Y, Ty> sks() {
		return J.sks();
	}

	@Override
	public Set<Pair<Term<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y>, Term<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y>>> eqs() {
		return J.eqs();
	}

	@Override
	public DP<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y> dp() {
		return this;
	}

	@Override
	public Algebra<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y, Row<En2,X>, Y> algebra() {
		return alg;
	}
		
	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En2>> ctx, Term<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y> lhs, Term<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y> rhs) {
		if (!ctx.isEmpty()) {
			throw new RuntimeException("Anomaly: please report.  Note to Ryan: caused by call to instance dp on non-ground eq");
		}
        return atType(lhs) ? I.dp().eq(new Ctx<>(), I.algebra().reprT(alg.intoY(lhs)), I.algebra().reprT(alg.intoY(rhs))) : alg.intoX(lhs).equals(alg.intoX(rhs));
	} 

	private boolean atType(Term<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y> term) {
		if (term.obj != null || term.sk != null) {
			return true;
		} else if (term.gen != null) {
			return false;
		} else if (term.att != null) {
			return true;
		} else if (term.fk != null) {
			return false;
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	
	
}	
	