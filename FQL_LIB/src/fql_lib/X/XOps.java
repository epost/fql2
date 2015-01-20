package fql_lib.X;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Util;
import fql_lib.X.XExp.Apply;
import fql_lib.X.XExp.Compose;
import fql_lib.X.XExp.FLOWER2;
import fql_lib.X.XExp.Flower;
import fql_lib.X.XExp.Id;
import fql_lib.X.XExp.Iter;
import fql_lib.X.XExp.Var;
import fql_lib.X.XExp.XConst;
import fql_lib.X.XExp.XCoprod;
import fql_lib.X.XExp.XCounit;
import fql_lib.X.XExp.XDelta;
import fql_lib.X.XExp.XEq;
import fql_lib.X.XExp.XExpVisitor;
import fql_lib.X.XExp.XFF;
import fql_lib.X.XExp.XFn;
import fql_lib.X.XExp.XInj;
import fql_lib.X.XExp.XInst;
import fql_lib.X.XExp.XMapConst;
import fql_lib.X.XExp.XMatch;
import fql_lib.X.XExp.XOne;
import fql_lib.X.XExp.XPair;
import fql_lib.X.XExp.XPi;
import fql_lib.X.XExp.XProj;
import fql_lib.X.XExp.XQueryExp;
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

public class XOps implements XExpVisitor<XObject, XProgram> {
	
	XEnvironment ENV;

	public XOps(XEnvironment env) {
		this.ENV = env;
	}

	@Override
	public XObject visit(XProgram env, XSchema e) {
		return XCtx.make(ENV.global, e);
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
		if (ctx.schema != null || ctx.global == null) {
			throw new RuntimeException("Not a schema: " + e.src);			
		}
		XCtx<String> ctx2 = (XCtx<String>) o2;
		if (ctx2.schema != null || ctx2.global == null) {
			throw new RuntimeException("Not a schema: " + e.dst);			
		}
		return XMapping.make(ENV, ctx, ctx2, e); 
	}

	@Override
	public XObject visit(XProgram env, XSigma e) {
		XObject o = e.F.accept(env, this);
		if (!(o instanceof XMapping<?,?>)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		XMapping<String,String> ctx = (XMapping<String,String>) o;
		XObject o2 = e.I.accept(env, this);
		if (o2 instanceof XCtx<?>) {
			XCtx<String> ctx2 = (XCtx<String>) o2;
			return ctx.apply0(ctx2);
		} else if (o2 instanceof XMapping<?,?>) {
			XMapping<String,String> ctx2 = (XMapping<String,String>) o2;
			return ctx.apply(ctx2);			
		} 
		else {
			throw new RuntimeException("Not an instance or transform: " + e.I);
		} 
	}

	
	public XObject visit(XProgram env, XDelta e) {
		XObject o = e.F.accept(env, this);
		if (!(o instanceof XMapping<?,?>)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		XMapping<String,String> ctx = (XMapping<String,String>) o;
		XObject o2 = e.I.accept(env, this);
		if (o2 instanceof XCtx<?>) {
			XCtx<String> ctx2 = (XCtx<String>) o2;
			return ctx.delta(ctx2);
		} else if (o2 instanceof XMapping<?,?>) {
			XMapping<String,String> ctx2 = (XMapping<String,String>) o2;
			return ctx.deltaT(ctx2);			
		}
		else {
			throw new RuntimeException("Not an instance or transform: " + e.I + " (class " + o2.getClass() + ")");
		} 
//		throw new RuntimeException();
	}
	@Override
	public XObject visit(XProgram env, XInst e) {
		XObject o = e.schema.accept(env, this);
		if (!(o instanceof XCtx<?>)) {
			throw new RuntimeException("Not a schema: " + e.schema);
		}
		XCtx<String> ctx = (XCtx<String>) o;
		if (ctx.schema != null) {
			throw new RuntimeException("Not a schema: " + e.schema);			
		}
		return XCtx.make(ctx, e);		
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
		return new XString("Assumption", Util.sep(e.lhs, ".") + " = " + Util.sep(e.rhs, "."));
	}

	@Override
	public XObject visit(XProgram env, XTransConst e) {
		XObject o = e.src.accept(env, this);
		if (!(o instanceof XCtx<?>)) {
			throw new RuntimeException("Not an instance: " + e.src);
		}
		XObject o2 = e.dst.accept(env, this);
		if (!(o2 instanceof XCtx<?>)) {
			throw new RuntimeException("Not an instance: " + e.dst);
		}
		XCtx<String> ctx = (XCtx<String>) o;
		XCtx<String> ctx2 = (XCtx<String>) o2;
		return XMapping.make(ctx, ctx2, e); 
	}

	@Override
	public XObject visit(XProgram env, XUnit e) {
		XObject o = e.F.accept(env, this);
		if (!(o instanceof XMapping<?, ?>)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		XObject o2 = e.I.accept(env, this);
		if (!(o2 instanceof XCtx<?>)) {
			throw new RuntimeException("Not an instance: " + e.I);
		}
		XMapping<String, String> ctx = (XMapping<String, String>) o;
		XCtx<String> ctx2 = (XCtx<String>) o2;
		if (e.kind.equals("sigma")) {
			return ctx.unit(ctx2); 
		}
		return ctx.pi_unit(ctx2);
	}

	@Override
	public XObject visit(XProgram env, XCounit e) {
		XObject o = e.F.accept(env, this);
		if (!(o instanceof XMapping<?, ?>)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		XObject o2 = e.I.accept(env, this);
		if (!(o2 instanceof XCtx<?>)) {
			throw new RuntimeException("Not an instance: " + e.I);
		}
		XMapping<String, String> ctx = (XMapping<String, String>) o;
		XCtx<String> ctx2 = (XCtx<String>) o2;
		if (e.kind.equals("sigma")) {
			return ctx.counit(ctx2); 
		}
		return ctx.pi_counit(ctx2);
	}

	@Override
	public XObject visit(XProgram env, XPi e) {
		XObject o = e.F.accept(env, this);
		if (!(o instanceof XMapping<?,?>)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		XMapping<String,String> ctx = (XMapping<String,String>) o;
		XObject o2 = e.I.accept(env, this);
		if (o2 instanceof XCtx<?>) {
			XCtx<String> ctx2 = (XCtx<String>) o2;
			return ctx.pi(ctx2);
		} else if (o2 instanceof XMapping<?,?>) {
			XMapping<String,String> ctx2 = (XMapping<String,String>) o2;
			return ctx.piT(ctx2);			
		}
		throw new RuntimeException("Not an instance or transform: " + e.I + " (class " + o2.getClass() + ")"); 
	}

	@Override
	public XObject visit(XProgram env, XRel e) {
		XObject o = e.I.accept(env, this);
		if (o instanceof XCtx<?>) {
			XCtx<?> x = (XCtx<?>) o;
			return x.rel();			
		}
		if (o instanceof XMapping<?,?>) {
			XMapping<?,?> x = (XMapping<?,?>) o;
			return x.rel();
		}
		throw new RuntimeException("Not a instance or homomorphism: " + o);
	}

	@Override
	public XObject visit(XProgram env, XCoprod e) {
		XObject l = e.l.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XObject r = e.r.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XCtx ll = (XCtx) l;
		XCtx rr = (XCtx) r;
		return XProd.coprod(ll, rr);
	}

	@Override
	public XObject visit(XProgram env, XInj e) {
		XObject l = e.l.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XObject r = e.r.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XCtx ll = (XCtx) l;
		XCtx rr = (XCtx) r;
		if (e.left) {
			return XProd.inl(ll, rr);
		} else {
			return XProd.inr(ll, rr);
		}
	}

	@Override
	public XObject visit(XProgram env, XMatch e) {
		XObject l = e.l.accept(env, this);
		if (!(l instanceof XMapping)) {
			throw new RuntimeException("LHS not a homomorphism");
		}
		XObject r = e.r.accept(env, this);
		if (!(l instanceof XMapping)) {
			throw new RuntimeException("RHS not a homomorphism");
		}
		XMapping ll = (XMapping) l;
		XMapping rr = (XMapping) r;
		return XProd.match(ll, rr);
	}

	@Override
	public XObject visit(XProgram env, XVoid e) {
		XObject x = e.S.accept(env, this);
		if (!(x instanceof XCtx)) {
			throw new RuntimeException("Not a schema");
		}
		XCtx c = (XCtx) x;
		return XProd.zero(c);
	}

	@Override
	public XObject visit(XProgram env, XFF e) {
		XObject x = e.S.accept(env, this);
		if (!(x instanceof XCtx)) {
			throw new RuntimeException("Not an instance in " + e);
		}
		XCtx c = (XCtx) x;
		return XProd.ff(c);
	}
	
	@Override
	public XObject visit(XProgram env, XTimes e) {
		XObject l = e.l.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XObject r = e.r.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XCtx<String> ll = (XCtx<String>) l;
		XCtx<String> rr = (XCtx<String>) r;
		return XProd.prod(ll, rr);

	}

	@Override
	public XObject visit(XProgram env, XProj e) {
		XObject l = e.l.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XObject r = e.r.accept(env, this);
		if (!(l instanceof XCtx)) {
			throw new RuntimeException("LHS not an instance in " + e);
		}
		XCtx ll = (XCtx) l;
		XCtx rr = (XCtx) r;
		if (e.left) {
			return XProd.fst(ll, rr);
		} else {
			return XProd.snd(ll, rr);
		}
	}

	@Override
	public XObject visit(XProgram env, XPair e) {
		XObject l = e.l.accept(env, this);
		if (!(l instanceof XMapping)) {
			throw new RuntimeException("LHS not a homomorphism");
		}
		XObject r = e.r.accept(env, this);
		if (!(l instanceof XMapping)) {
			throw new RuntimeException("RHS not a homomorphism");
		}
		XMapping ll = (XMapping) l;
		XMapping rr = (XMapping) r;
		return XProd.pair(ll, rr);
	}

	@Override
	public XObject visit(XProgram env, XOne e) {
		XObject x = e.S.accept(env, this);
		if (!(x instanceof XCtx)) {
			throw new RuntimeException("Not a schema");
		}
		XCtx c = (XCtx) x;
		return XProd.one(c);
	}

	@Override
	public XObject visit(XProgram env, XTT e) {
		XObject x = e.S.accept(env, this);
		if (!(x instanceof XCtx)) {
			throw new RuntimeException("Not an instance in " + e);
		}
		XCtx c = (XCtx) x;
		return XProd.tt(c);
	}

	@Override
	public XObject visit(XProgram env, Flower e) {
		XObject src0 = e.src.accept(env, this);
		if (!(src0 instanceof XCtx)) {
			throw new RuntimeException("Not an instance in " + e);			
		}
		XCtx src = (XCtx) src0;
		return XProd.flower(e, src);		
	}

	@Override
	public XObject visit(XProgram env, FLOWER2 e) {
		XObject src0 = e.src.accept(env, this);
		if (!(src0 instanceof XCtx)) {
			throw new RuntimeException("Not an instance in " + e);			
		}
		XCtx src = (XCtx) src0;
		return XProd.flower2(e, src);		
	}

	@Override
	public XObject visit(XProgram env, XQueryExp e) {
		Object delta0 = e.delta.accept(env, this);
		if (!(delta0 instanceof XMapping)) {
			throw new RuntimeException("Delta not a mapping in " + e);
		}
		Object pi0 = e.pi.accept(env, this);
		if (!(pi0 instanceof XMapping)) {
			throw new RuntimeException("Pi not a mapping in " + e);
		}
		Object sigma0 = e.sigma.accept(env, this);
		if (!(sigma0 instanceof XMapping)) {
			throw new RuntimeException("Sigma not a mapping in " + e);
		}
		return new XQuery((XMapping)pi0, (XMapping)delta0, (XMapping)sigma0);
	}

	@Override
	public XObject visit(XProgram env, Apply e) {
		XObject f = e.f.accept(env, this);
		if (!(f instanceof XPoly)) {
			throw new RuntimeException("Not a query in " + e);
		}
		XObject i = e.I.accept(env, this);
		if (!(i instanceof XCtx)) {
			throw new RuntimeException("Not an instance in " + e);
		}
		return XProd.uberflower((XPoly)f, (XCtx)i);
	}

	@Override
	public XObject visit(XProgram env, Iter e) {
		XObject f = e.f.accept(env, this);
		if (!(f instanceof XQuery)) {
			throw new RuntimeException("Not a query in " + e);
		}
		XObject i = e.initial.accept(env, this);
		if (!(f instanceof XCtx)) {
			throw new RuntimeException("Not an instance in " + e);
		}
		XQuery F = (XQuery) f;
		XCtx I = (XCtx) i;
		
		for (int j = 0; j < e.num; j++) {
			I = F.apply(I);
		}
		
		return I;
	}

	@Override
	public XObject visit(XProgram env, Id e) {
		Object o = e.C.accept(env, this);
		if (!(o instanceof XCtx)) {
			throw new RuntimeException("Not schema/instance in " + e);
		}
		XCtx C = (XCtx) o;
		String str = null;
		if (C.kind().equals("schema")) {
			str = "mapping";
		}
		if (C.kind().equals("instance")) {
			str = "homomorphism";
		}
		if (str == null) {
			throw new RuntimeException();
		}
		XMapping F = new XMapping(C, str);
		if (e.isQuery) {
			return new XQuery(F, F, F);
		} 
		return F;
	}

	@Override
	public XObject visit(XProgram env, Compose e) {
		Object f0 = e.f.accept(env, this);
		Object g0 = e.g.accept(env, this);
		if (f0 instanceof XMapping && g0 instanceof XMapping) {
			XMapping F = (XMapping) f0;
			XMapping G = (XMapping) g0;
			return new XMapping(F, G);
		} else if (f0 instanceof XQuery && g0 instanceof XQuery) {
			throw new RuntimeException("Cannot compose queries yet.");			
		}
		throw new RuntimeException("Cannot compose in " + e);
	}

	@Override
	public XObject visit(XProgram env, XPoly e) {
		Object a = e.src_e.accept(env, this);
		if (!(a instanceof XCtx)) {
			throw new RuntimeException("Not a schema: " + a);
		}
		Object b = e.dst_e.accept(env, this);
		if (!(a instanceof XCtx)) {
			throw new RuntimeException("Not a schema: " + a);
		}
		e.src = (XCtx) a;
		e.dst = (XCtx) b;
		return e;
	}

	@Override
	public XObject visit(XProgram env, XToQuery e) {
		Object o = e.inst.accept(env, this);
		if (!(o instanceof XCtx)) {
			throw new RuntimeException("Not instance: " + o);
		}
		XCtx c = (XCtx) o;

		int i = 0;
		Map m1 = new HashMap();
		Map m2 = new HashMap();
	//	Map mty = new HashMap();
		
		Map from = new HashMap<>();
		for (Object t : c.terms()) {
			m1.put("v_v"+i, t);
			m2.put(t, "v_v"+i);
			from.put("v_v"+i, c.type(t).second);
//			from.put("v"+i, c.type(((Pair)t).second);
			i++;
		}
		List where = new LinkedList<>();

		System.out.println("m1 : " + m1);
		
		Function f = x -> {
			Object j = m2.get(x);
			if (j == null) {
				return x;
			}
			return j;
		};
		for (Object k : c.eqs) {
			List l = (List) ((Pair)k).first;
			List r = (List) ((Pair)k).second;
			//lookup m2
			where.add(new Pair<>(l.stream().map(f).collect(Collectors.toList()),
					             r.stream().map(f).collect(Collectors.toList())));
		}
		
		Flower iii= new Flower(new HashMap<>(), from, where, e.applyTo);
	//	System.out.println(iii);
		return iii.accept(env, this);		
	}

	@Override
	public XObject visit(XProgram env, XUberPi e) {
		XObject eF = e.F.accept(env, this);
		
		if (!(eF instanceof XMapping)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		
		XMapping m = (XMapping) eF;
		return m.uber();
	}
	

	
}
