package fql_lib.X;

import java.util.Map;

import fql_lib.X.XExp.Apply;
import fql_lib.X.XExp.Compose;
import fql_lib.X.XExp.FLOWER2;
import fql_lib.X.XExp.Flower;
import fql_lib.X.XExp.Id;
import fql_lib.X.XExp.Iter;
import fql_lib.X.XExp.Var;
import fql_lib.X.XExp.XCoApply;
import fql_lib.X.XExp.XConst;
import fql_lib.X.XExp.XCoprod;
import fql_lib.X.XExp.XCounit;
import fql_lib.X.XExp.XDelta;
import fql_lib.X.XExp.XEq;
import fql_lib.X.XExp.XExpVisitor;
import fql_lib.X.XExp.XFF;
import fql_lib.X.XExp.XFn;
import fql_lib.X.XExp.XGrothLabels;
import fql_lib.X.XExp.XIdPoly;
import fql_lib.X.XExp.XInj;
import fql_lib.X.XExp.XInst;
import fql_lib.X.XExp.XLabel;
import fql_lib.X.XExp.XMapConst;
import fql_lib.X.XExp.XMatch;
import fql_lib.X.XExp.XOne;
import fql_lib.X.XExp.XPair;
import fql_lib.X.XExp.XPi;
import fql_lib.X.XExp.XProj;
import fql_lib.X.XExp.XPushout;
import fql_lib.X.XExp.XRel;
import fql_lib.X.XExp.XSOED;
import fql_lib.X.XExp.XSchema;
import fql_lib.X.XExp.XSigma;
import fql_lib.X.XExp.XTT;
import fql_lib.X.XExp.XTimes;
import fql_lib.X.XExp.XToQuery;
import fql_lib.X.XExp.XTransConst;
import fql_lib.X.XExp.XTy;
import fql_lib.X.XExp.XUberPi;
import fql_lib.X.XExp.XUnit;
import fql_lib.X.XExp.XVoid;

public class XKind implements XExpVisitor<String, 	Map<String, XExp>> {

	@Override
	public String visit(Map<String, XExp> env, XPoly e) {
		return "query";
	}
	
	@Override
	public String visit(Map<String, XExp> env, Var e) {
		if (!env.containsKey(e.v)) {
			throw new RuntimeException("Missing: " + e.v);
		}
		return env.get(e.v).accept(env, this);
	}
	
	@Override
	public String visit(Map<String, XExp> env, XSchema e) {
		return "category";
	}

	@Override
	public String visit(Map<String, XExp> env, XMapConst e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, XTransConst e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, XInst e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, Id e) {
		String s = e.C.accept(env, this);
		if (s.equals("category")) {
			return "functor";
		}
		if (s.equals("functor")) {
			return "transform";
		}
		return "Cannot id " + e;
	}

	@Override
	public String visit(Map<String, XExp> env, Compose e) {
		String a = e.f.accept(env, this);
		String b = e.g.accept(env, this);
		if (!a.equals(b)) {
			throw new RuntimeException("In " + e + ", cod " + a + " but dom " + b);
		}
		return a;
	}
	
	@Override
	public String visit(Map<String, XExp> env, XDelta e) {
		return e.I.accept(env, this);
	}

	@Override
	public String visit(Map<String, XExp> env, XSigma e) {
		return e.I.accept(env, this);
	}

	@Override
	public String visit(Map<String, XExp> env, XPi e) {
		return e.I.accept(env, this);
	}

	@Override
	public String visit(Map<String, XExp> env, XUnit e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, XCounit e) {
		return "transform";
	}


	/////////////////////////////////////////////////////////////////////
	
	
	@Override
	public String visit(Map<String, XExp> env, XPushout e) {
		return "functor";
	}	

	@Override
	public String visit(Map<String, XExp> env, XRel e) {
		return e.I.kind(env);
	}

	@Override
	public String visit(Map<String, XExp> env, XCoprod e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, XInj e) {
		return "transfrom";
	}

	@Override
	public String visit(Map<String, XExp> env, XMatch e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, XVoid e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, XFF e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, XTimes e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, XProj e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, XPair e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, XOne e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, XTT e) {
		return "transform";
	}

	@Override
	public String visit(Map<String, XExp> env, Apply e) {
		return e.I.kind(env);
	}
	
	@Override
	public String visit(Map<String, XExp> env, XCoApply e) {
		return e.I.kind(env);
	}

	@Override
	public String visit(Map<String, XExp> env, Flower e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, FLOWER2 e) {
		return "functor";
	}

	@Override
	public String visit(Map<String, XExp> env, XIdPoly e) {
		return "query";
	}
	
	@Override
	public String visit(Map<String, XExp> env, XSOED e) {
		return "query";
	}
	

	/////////////////////////////////////////////////////////
	

	@Override
	public String visit(Map<String, XExp> env, Iter e) {
		return null;
	}

	
	@Override
	public String visit(Map<String, XExp> env, XToQuery e) {
		return null;

	}

	@Override
	public String visit(Map<String, XExp> env, XUberPi e) {
		return null;

	}

	@Override
	public String visit(Map<String, XExp> env, XLabel e) {
		return null;

	}

	@Override
	public String visit(Map<String, XExp> env, XGrothLabels e) {
		return null;

	}
	
	@Override
	public String visit(Map<String, XExp> env, XTy e) {
		return null;
	}

	@Override
	public String visit(Map<String, XExp> env, XFn e) {
		return null;
	}

	@Override
	public String visit(Map<String, XExp> env, XConst e) {
		return null;
	}

	@Override
	public String visit(Map<String, XExp> env, XEq e) {
		return null;
	}

	
	
}
