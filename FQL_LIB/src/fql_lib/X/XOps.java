package fql_lib.X;

import fql_lib.X.XExp.Var;
import fql_lib.X.XExp.XConst;
import fql_lib.X.XExp.XEq;
import fql_lib.X.XExp.XExpVisitor;
import fql_lib.X.XExp.XFn;
import fql_lib.X.XExp.XInst;
import fql_lib.X.XExp.XMapping;
import fql_lib.X.XExp.XSchema;
import fql_lib.X.XExp.XSigma;
import fql_lib.X.XExp.XTy;

public class XOps implements XExpVisitor<XObject, XProgram> {
	
	XEnvironment ENV;

	public XOps(XEnvironment env) {
		this.ENV = env;
	}

	@Override
	public XObject visit(XProgram env, XSchema e) {
		return XSig.make(ENV, e);
	}

	@Override
	public XObject visit(XProgram env, XMapping e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XObject visit(XProgram env, XSigma e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XObject visit(XProgram env, XInst e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XObject visit(XProgram env, Var e) {
		XObject ret = ENV.objs.get(e.v);
		if (ret == null) {
			throw new RuntimeException("Unbound variable: " + e.v);
		}
		return ret;
	}

	@Override
	public XObject visit(XProgram env, XTy e) {
		return new XString("Type", e.javaName);
	}

	@Override
	public XObject visit(XProgram env, XFn e) {
		return new XString("Function", e.javaFn + " : " + e.src + " -> " + e.dst);
	}

	@Override
	public XObject visit(XProgram env, XConst e) {
		return new XString("Constant", e.javaFn + " : " + e.dst);
	}

	@Override
	public XObject visit(XProgram env, XEq e) {
		return new XString("Assumption", e.lhs + " = " + e.rhs);
	}

}
