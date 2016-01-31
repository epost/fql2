package fql_lib.fpql;

import java.util.Map;

import fql_lib.fpql.XExp.Apply;
import fql_lib.fpql.XExp.Compose;
import fql_lib.fpql.XExp.FLOWER2;
import fql_lib.fpql.XExp.Flower;
import fql_lib.fpql.XExp.Id;
import fql_lib.fpql.XExp.Iter;
import fql_lib.fpql.XExp.Var;
import fql_lib.fpql.XExp.XCoApply;
import fql_lib.fpql.XExp.XConst;
import fql_lib.fpql.XExp.XCoprod;
import fql_lib.fpql.XExp.XCounit;
import fql_lib.fpql.XExp.XDelta;
import fql_lib.fpql.XExp.XEq;
import fql_lib.fpql.XExp.XExpVisitor;
import fql_lib.fpql.XExp.XFF;
import fql_lib.fpql.XExp.XFn;
import fql_lib.fpql.XExp.XGrothLabels;
import fql_lib.fpql.XExp.XIdPoly;
import fql_lib.fpql.XExp.XInj;
import fql_lib.fpql.XExp.XInst;
import fql_lib.fpql.XExp.XLabel;
import fql_lib.fpql.XExp.XMapConst;
import fql_lib.fpql.XExp.XMatch;
import fql_lib.fpql.XExp.XOne;
import fql_lib.fpql.XExp.XPair;
import fql_lib.fpql.XExp.XPi;
import fql_lib.fpql.XExp.XProj;
import fql_lib.fpql.XExp.XPushout;
import fql_lib.fpql.XExp.XRel;
import fql_lib.fpql.XExp.XSOED;
import fql_lib.fpql.XExp.XSchema;
import fql_lib.fpql.XExp.XSigma;
import fql_lib.fpql.XExp.XSuperED;
import fql_lib.fpql.XExp.XTT;
import fql_lib.fpql.XExp.XTimes;
import fql_lib.fpql.XExp.XToQuery;
import fql_lib.fpql.XExp.XTransConst;
import fql_lib.fpql.XExp.XTy;
import fql_lib.fpql.XExp.XUberPi;
import fql_lib.fpql.XExp.XUnit;
import fql_lib.fpql.XExp.XVoid;

public class XKind implements XExpVisitor<String, 	Map<String, XExp>> {

	@Override
	public String visit(Map<String, XExp> env, @SuppressWarnings("rawtypes") XPoly e) {
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
	
	@Override
	public String visit(Map<String, XExp> env, XSuperED e) {
		return "functor";
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
