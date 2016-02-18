package catdata.mpl;

import catdata.Unit;
import catdata.ide.Environment;
import catdata.mpl.Mpl.MplExp.MplEval;
import catdata.mpl.Mpl.MplExp.MplSch;
import catdata.mpl.Mpl.MplExp.MplVar;
import catdata.mpl.Mpl.MplExpVisitor;
import catdata.mpl.Mpl.MplTerm.MplAlpha;
import catdata.mpl.Mpl.MplTerm.MplComp;
import catdata.mpl.Mpl.MplTerm.MplConst;
import catdata.mpl.Mpl.MplTerm.MplId;
import catdata.mpl.Mpl.MplTerm.MplLambda;
import catdata.mpl.Mpl.MplTerm.MplPair;
import catdata.mpl.Mpl.MplTerm.MplRho;
import catdata.mpl.Mpl.MplTerm.MplTr;
import catdata.mpl.Mpl.MplTermVisitor;
import catdata.mpl.Mpl.MplType.MplBase;
import catdata.mpl.Mpl.MplType.MplProd;
import catdata.mpl.Mpl.MplType.MplUnit;
import catdata.mpl.Mpl.MplTypeVisitor;

public class MplOps<O,A> implements MplTypeVisitor<O,MplObject,Unit>,
                                    MplTermVisitor<O,A,MplObject,Unit>,
                                    MplExpVisitor<O,A,MplObject,Unit> {

	Environment<MplObject> ENV;

	public MplOps(Environment<MplObject> env) {
		this.ENV = env;
	}
	
	@Override
	public MplObject visit(Unit env, MplVar<O, A> e) {
		return ENV.get(e.s);
	}

	@Override
	public MplObject visit(Unit env, MplSch<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplEval<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplConst<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplId<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplComp<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplPair<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplAlpha<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplRho<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplLambda<O, A> e) {
		return e;
	}
	
	@Override
	public MplObject visit(Unit env, MplTr<O, A> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplBase<O> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplProd<O> e) {
		return e;
	}

	@Override
	public MplObject visit(Unit env, MplUnit<O> e) {
		return e;
	}
	

}
