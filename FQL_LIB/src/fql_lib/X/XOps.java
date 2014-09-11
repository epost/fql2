package fql_lib.X;

import fql_lib.X.XExp.Var;
import fql_lib.X.XExp.XConst;
import fql_lib.X.XExp.XEq;
import fql_lib.X.XExp.XExpVisitor;
import fql_lib.X.XExp.XFn;
import fql_lib.X.XExp.XInst;
import fql_lib.X.XExp.XMapConst;
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
		return XCtx.make(ENV, e);
	}

	@Override
	public XObject visit(XProgram env, XMapConst e) {
		XObject o = e.src.accept(env, this);
		if (!(o instanceof XCtx<?>)) {
			throw new RuntimeException("Not a schema: " + e.src);
		}
		XObject o2 = e.dst.accept(env, this);
		if (!(o2 instanceof XCtx<?>)) {
			throw new RuntimeException("Not a schema: " + e.dst);
		}
		XCtx<String> ctx = (XCtx<String>) o;
		XCtx<String> ctx2 = (XCtx<String>) o2;
		return XMapping.make(ENV, ctx, ctx2, e);
	}

	@Override
	public XObject visit(XProgram env, XSigma e) {
		XObject o = e.F.accept(env, this);
		if (!(o instanceof XMapping<?>)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		XObject o2 = e.I.accept(env, this);
		if (!(o2 instanceof XCtx<?>)) {
			throw new RuntimeException("Not an instance: " + e.I);
		}
		XMapping<String> ctx = (XMapping<String>) o;
		XCtx<String> ctx2 = (XCtx<String>) o2;
		return ctx.apply(ctx2);
	}

	@Override
	public XObject visit(XProgram env, XInst e) {
		XObject o = e.schema.accept(env, this);
		if (!(o instanceof XCtx<?>)) {
			throw new RuntimeException("Not a schema: " + e.schema);
		}
		XCtx<String> ctx = (XCtx<String>) o;
		return XCtx.make(ENV, ctx, e);		
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
