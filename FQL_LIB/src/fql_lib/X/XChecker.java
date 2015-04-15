package fql_lib.X;

import java.util.Map;

import fql_lib.Pair;
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

public class XChecker implements XExpVisitor<Pair<XExp, XExp>, Map<String, XExp>> {

	static Var SET = new Var("Set");
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, Var e) {
		if (!env.containsKey(e.v)) {
			throw new RuntimeException("Unbound: " + e.v);
		}
		return env.get(e.v).accept(env, this);
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XMapConst e) {
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XTransConst e) {
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XInst e) {
		return new Pair<>(e.schema, SET);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, Id e) {
		return new Pair<>(e.C, e.C);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, Compose e) {
		Pair<XExp, XExp> a = e.f.accept(env, this);
		Pair<XExp, XExp> b = e.g.accept(env, this);
		if (!a.second.equals(b.first)) {
			throw new RuntimeException("In " + e + ", cod " + a.second + " but dom " + b.first);
		}
		return new Pair<>(a.first, b.second);
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XDelta e) {
		Pair<XExp, XExp> FT = e.F.accept(env, this);
		Pair<XExp, XExp> IT = e.I.accept(env, this);
	//	System.out.println("doing " + e);
	//	System.out.println(FT);
	//	System.out.println(IT);
		if (e.I.kind(env).equals("functor")) {
			if (!IT.first.equals(FT.second)) {
				throw new RuntimeException("In " + e + ", instance type " + IT.first + " but functor cod " + FT.second);
			}
			return new Pair<>(FT.first, SET);
		}
		return new Pair<>(new XDelta(e.F, IT.first), new XDelta(e.F, IT.second));
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XSigma e) {
		Pair<XExp, XExp> FT = e.F.accept(env, this);
		Pair<XExp, XExp> IT = e.I.accept(env, this);
			if (e.I.kind(env).equals("functor")) {
			if (!IT.first.equals(FT.first)) {
				throw new RuntimeException("In " + e + ", instance type " + IT.first + " but functor dom " + FT.first);
			}
			return new Pair<>(FT.second, SET);
		}
		return new Pair<>(new XSigma(e.F, IT.first), new XSigma(e.F, IT.second));
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XPi e) {
		Pair<XExp, XExp> FT = e.F.accept(env, this);
		Pair<XExp, XExp> IT = e.I.accept(env, this);
		if (e.I.kind(env).equals("functor")) {
			if (!IT.first.equals(FT.first)) {
				throw new RuntimeException("In " + e + ", instance type " + IT.first + " but functor dom " + FT.first);
			}
			return new Pair<>(FT.second, SET);
		}
		return new Pair<>(new XPi(e.F, IT.first), new XPi(e.F, IT.second));
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XUnit e) {
		if (e.kind.equals("sigma")) {
			return new Pair<>(e.I, new XExp.XDelta(e.F, new XExp.XSigma(e.F, e.I)));
		} else if (e.kind.equals("pi")) {
			return new Pair<>(e.I, new XExp.XPi(e.F, new XExp.XDelta(e.F, e.I)));
		}
		throw null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XCounit e) {
		if (e.kind.equals("sigma")) {
			return new Pair<>(new XExp.XSigma(e.F, new XExp.XDelta(e.F, e.I)), e.I);
		} else if (e.kind.equals("pi")) {
			return new Pair<>(new XExp.XDelta(e.F, new XExp.XPi(e.F, e.I)), e.I);
		}
		throw null;
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XPushout e) {
		Pair<XExp, XExp> l = e.f.accept(env, this);
		Pair<XExp, XExp> r = e.g.accept(env, this);
		if (!l.first.equals(r.first)) {
			throw new RuntimeException("Pushout from different domains, " + l.first + " and " + r.first);
		}
		return l.first.type(env);
	}	

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XRel e) {
		String k = e.I.kind(env);
		Pair<XExp, XExp> IT = e.I.accept(env, this);
		if (k.equals("functor")) {
			return IT;
		} else if (k.equals("transform")) {
			return new Pair<>(new XRel(IT.first), new XRel(IT.second));
		}
		throw new RuntimeException("Bad kind: " + k + " in " + e);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XCoprod e) {
		Pair<XExp, XExp> l = e.l.accept(env, this);
		Pair<XExp, XExp> r = e.r.accept(env, this);
		if (!l.first.equals(r.first)) {
			throw new RuntimeException("Not of same schema on " + e + ", are " + l.first + " and " + r.first);
		}
		return l;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XInj e) {
		Pair<XExp, XExp> l = e.l.accept(env, this);
		Pair<XExp, XExp> r = e.r.accept(env, this);
		if (!l.first.equals(r.first)) {
			throw new RuntimeException("Different schemas in " + e + ", are" + l.first + " and " + r.first);
		}
		if (e.left) {
			return new Pair<>(e.l, new XCoprod(e.l, e.r));
		} else {
			return new Pair<>(e.r, new XCoprod(e.l, e.r));
		}
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XMatch e) {
		Pair<XExp, XExp> l = e.l.accept(env, this);
		Pair<XExp, XExp> r = e.r.accept(env, this);
		if (!l.second.equals(r.second)) {
			throw new RuntimeException("targets of " + e + " not equal: " + l.second + " and " + r.second);
		}
		return new Pair<>(new XCoprod(l.first, r.first), r.second);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XVoid e) {
		return new Pair<>(e.S, SET);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XFF e) {
		return new Pair<>(new XVoid(e.S.type(env).first), e.S);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XTimes e) {
		Pair<XExp, XExp> l = e.l.accept(env, this);
		Pair<XExp, XExp> r = e.r.accept(env, this);
		if (!l.first.equals(r.first)) {
			throw new RuntimeException("Not of same schema on " + e + ", are " + l.first + " and " + r.first);
		}
		return l;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XProj e) {
		Pair<XExp, XExp> l = e.l.accept(env, this);
		Pair<XExp, XExp> r = e.r.accept(env, this);
		if (!l.first.equals(r.first)) {
			throw new RuntimeException("Different schemas in " + e + ", are" + l.first + " and " + r.first);
		}
		if (e.left) {
			return new Pair<>(new XTimes(e.l, e.r), e.l);
		} else {
			return new Pair<>(new XTimes(e.l, e.r), e.r);
		}

	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XPair e) {
		Pair<XExp, XExp> l = e.l.accept(env, this);
		Pair<XExp, XExp> r = e.r.accept(env, this);
		if (!l.first.equals(r.first)) {
			throw new RuntimeException("sources of " + e + " not equal: " + l.second + " and " + r.second);
		}
		return new Pair<>(r.first, new XTimes(l.second, r.second));
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XOne e) {
		return new Pair<>(e.S, SET);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XTT e) {
		return new Pair<>(e.S, new XOne(e.S.type(env).first));
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XPoly e) {
		return new Pair<>(e.src_e, e.dst_e);
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XIdPoly e) {
		return new Pair<>(e.F, e.F);
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, Apply e) {
		String k = e.f.kind(env);
		if (!k.equals("query")) {
			throw new RuntimeException("Can only apply queries in " + e);
		}
		Pair<XExp, XExp> q = e.f.accept(env, this);
		Pair<XExp, XExp> i = e.I.accept(env, this);
		if (e.I.kind(env).equals("functor")) {
			if (!q.first.equals(i.first)) {
				throw new RuntimeException("Query " + e + " expected " + q.first + " but got " + i.first);
			}
			return new Pair<>(q.second, SET);
		} else if (e.I.kind(env).equals("transform")) {
			Object p = i.first.type(env).first;
			if (!p.equals(q.first)) {
				throw new RuntimeException("Schema of transform is " + p + " not " + q.first + " as expected");
			}
			return new Pair<>(new Apply(e.f, i.first), new Apply(e.f, i.second));
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XCoApply e) {
		String k = e.f.kind(env);
		if (!k.equals("query")) {
			throw new RuntimeException("Can only co-apply queries in " + e);
		}
		Pair<XExp, XExp> q = e.f.accept(env, this);
		Pair<XExp, XExp> i = e.I.accept(env, this);
		if (e.I.kind(env).equals("functor")) {
		if (!q.second.equals(i.first)) {
			throw new RuntimeException("Query " + e + " expected " + q.first + " but got " + i.first);
		}
		return new Pair<>(q.first, SET);
		}else if (e.I.kind(env).equals("transform")) {
			Object p = i.first.type(env).first;
			if (!p.equals(q.second)) {
				throw new RuntimeException("Schema of transform is " + p + " not " + q.second + " as expected");
			}
			return new Pair<>(new XCoApply(e.f, i.first), new XCoApply(e.f, i.second));
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, Flower e) {
		return null; //TODO
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, FLOWER2 e) {
		return null; //TODO
	}
	
	//////////////////////////////////////////////
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XToQuery e) {
		throw null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XUberPi e) {
		throw null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XLabel e) {
		throw null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XGrothLabels e) {
		throw null;
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XTy e) {
		return null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XFn e) {
		return null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XConst e) {
		return null;
	}

	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XEq e) {
		return null;
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, XSchema e) {
		return null;
	}
	
	@Override
	public Pair<XExp, XExp> visit(Map<String, XExp> env, Iter e) {
		throw null;
	}
	
}
