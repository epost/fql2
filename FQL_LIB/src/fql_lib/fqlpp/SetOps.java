package fql_lib.fqlpp;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import catdata.algs.Pair;
import fql_lib.fqlpp.FnExp.Case;
import fql_lib.fqlpp.FnExp.Chr;
import fql_lib.fqlpp.FnExp.Comp;
import fql_lib.fqlpp.FnExp.Const;
import fql_lib.fqlpp.FnExp.Curry;
import fql_lib.fqlpp.FnExp.Eval;
import fql_lib.fqlpp.FnExp.FF;
import fql_lib.fqlpp.FnExp.FnExpVisitor;
import fql_lib.fqlpp.FnExp.Fst;
import fql_lib.fqlpp.FnExp.Id;
import fql_lib.fqlpp.FnExp.Inl;
import fql_lib.fqlpp.FnExp.Inr;
import fql_lib.fqlpp.FnExp.Iso;
import fql_lib.fqlpp.FnExp.Krnl;
import fql_lib.fqlpp.FnExp.Prod;
import fql_lib.fqlpp.FnExp.Snd;
import fql_lib.fqlpp.FnExp.TT;
import fql_lib.fqlpp.FnExp.Tru;
import fql_lib.fqlpp.FnExp.Var;
import fql_lib.fqlpp.SetExp.Cod;
import fql_lib.fqlpp.SetExp.Dom;
import fql_lib.fqlpp.SetExp.Exp;
import fql_lib.fqlpp.SetExp.Numeral;
import fql_lib.fqlpp.SetExp.One;
import fql_lib.fqlpp.SetExp.Plus;
import fql_lib.fqlpp.SetExp.Prop;
import fql_lib.fqlpp.SetExp.Range;
import fql_lib.fqlpp.SetExp.SetExpVisitor;
import fql_lib.fqlpp.SetExp.Times;
import fql_lib.fqlpp.SetExp.Zero;
import fql_lib.fqlpp.cat.FinSet;
import fql_lib.fqlpp.cat.Functor;
import fql_lib.fqlpp.cat.Transform;
import fql_lib.fqlpp.cat.FinSet.Fn;

@SuppressWarnings({ "rawtypes", "serial" })
public class SetOps implements SetExpVisitor<Set<?>, FQLPPProgram>, FnExpVisitor<FinSet.Fn, FQLPPProgram> , Serializable {

	private FQLPPEnvironment ENV;
	public SetOps(FQLPPEnvironment ENV) {
		this.ENV = ENV;
	}
	
	@SuppressWarnings("unused")
	private SetOps() { }
	
	@Override
	public Fn visit(FQLPPProgram env, Id e) {
		Set<?> s = e.t.accept(env, this);
		return FinSet.FinSet.identity(s);
	}

	@Override
	public Fn visit(FQLPPProgram env, Comp e) {
		Fn l = e.l.accept(env, this);
		Fn r = e.r.accept(env, this);
		return FinSet.FinSet.compose(l, r);
	}

	@Override
	public Fn visit(FQLPPProgram env, Var e) {
		Fn k = ENV.fns.get(e.v);
		if (k == null) {
			throw new RuntimeException("Missing function: " + e);
		}
		return k;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Const e) {
		return new Fn(e.src.accept(env, this), e.dst.accept(env, this), e.f);
	}

	@Override
	public Fn visit(FQLPPProgram env, TT e) {
		Set<?> s = e.t.accept(env, this);
		return FinSet.FinSet.terminal(s);
	}

	@Override
	public Fn visit(FQLPPProgram env, FF e) {
		Set<?> s = e.t.accept(env, this);
		return FinSet.FinSet.initial(s);
	}

	@Override
	public Fn visit(FQLPPProgram env, Fst e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.first(s, t);
	}

	@Override
	public Fn visit(FQLPPProgram env, Snd e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.second(s, t);
	}

	@Override
	public Fn visit(FQLPPProgram env, Inl e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.inleft(s, t);

	}

	@Override
	public Fn visit(FQLPPProgram env, Inr e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.inright(s, t);
	}

	@Override
	public Fn visit(FQLPPProgram env, Eval e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.eval(s, t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Curry e) {
		Fn f = e.f.accept(env, this);
		return FinSet.FinSet.curry(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Case e) {
		Fn s = e.l.accept(env, this);
		Fn t = e.r.accept(env, this);
		return FinSet.FinSet.match(s, t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Prod e) {
		Fn s = e.l.accept(env, this);
		Fn t = e.r.accept(env, this);
		return FinSet.FinSet.pair(s, t);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Iso e) {
		Set l = e.l.accept(env, this);
		Set r = e.r.accept(env, this);
		Optional<Pair<Fn, Fn>> k = FinSet.FinSet.iso(l, r);
		if (!k.isPresent()) {
			throw new RuntimeException("Not isomorphic: " + l + " and " + r);
		}
		if (e.lToR) {
			return k.get().first;
		} else {
			return k.get().second;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Chr e) {
		Fn f = e.f.accept(env, this);
		return FinSet.FinSet.chr(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, Krnl e) {
		Fn f = e.f.accept(env, this);
		return FinSet.FinSet.kernel(f);
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Zero e) {
		return FinSet.FinSet.initial();
	}

	@Override
	public Set<?> visit(FQLPPProgram env, One e) {
		return FinSet.FinSet.terminal();
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Prop e) {
		return FinSet.FinSet.prop();
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Plus e) {
		Set<?> a = e.a.accept(env, this);
		Set<?> b = e.b.accept(env, this);
		return FinSet.FinSet.coproduct(a, b);
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Times e) {
		Set<?> a = e.a.accept(env, this);
		Set<?> b = e.b.accept(env, this);
		return FinSet.FinSet.product(a, b);
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Exp e) {
		Set<?> a = e.a.accept(env, this);
		Set<?> b = e.b.accept(env, this);
		return FinSet.FinSet.exp(a, b);
	}

	@Override
	public Set<?> visit(FQLPPProgram env, fql_lib.fqlpp.SetExp.Var e) {
		Set x = ENV.sets.get(e.v);
		if (x == null) {
			throw new RuntimeException("Missing set: " + e);
		}
		return x;
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Dom e) {
		Fn f = e.f.accept(env, this);
		return f.source;
	}
	
	@Override
	public Set<?> visit(FQLPPProgram env, Cod e) {
		Fn f = e.f.accept(env, this);
		return f.target;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<?> visit(FQLPPProgram env, Range e) {
		Fn f = e.f.accept(env, this);
		Set<Object> ret = new HashSet<>();
		for (Object o : f.source) {
			ret.add(f.apply(o));
		}
		return ret;
	}

	@Override
	public Set<?> visit(FQLPPProgram env, fql_lib.fqlpp.SetExp.Const e) {
		return e.s;
	}

	@Override
	public Fn visit(FQLPPProgram env, Tru e) {
		if (e.str.equals("true")) {
			return FinSet.FinSet.tru();
		} else if (e.str.equals("false")) {
			return FinSet.FinSet.fals();
		} else if (e.str.equals("and")) {
			return FinSet.FinSet.and();
		} else if (e.str.equals("or")) {
			return FinSet.FinSet.or();
		} else if (e.str.equals("not")) {
			return FinSet.FinSet.not();
		} else if (e.str.equals("implies")) {
			return FinSet.FinSet.implies();
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public Set<?> visit(FQLPPProgram env, Numeral e) {
		Set<Object> ret = new HashSet<>();
		if (e.i < 0) {
			throw new RuntimeException(e + " is negative.");
		}
		for (int i = 0; i < e.i; i++) {
			ret.add(Integer.toString(i));
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<?> visit(FQLPPProgram env, fql_lib.fqlpp.SetExp.Apply e) {
		FunctorExp k = env.ftrs.get(e.f);
		if (k == null) {
			throw new RuntimeException("Missing functor: " + k);
		}
		Set<?> s = e.set.accept(env, this);
		Functor f = k.accept(env, new CatOps(ENV));
		if (!FinSet.FinSet.equals(f.source)) {
			throw new RuntimeException("Domain is not Set in " + e);
		}
		if (!FinSet.FinSet.equals(f.target)) {
			throw new RuntimeException("Codomain is not Set in " + e);
		}
		return (Set<?>) f.applyO(s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, fql_lib.fqlpp.FnExp.Apply e) {
		FunctorExp k = env.ftrs.get(e.f);
		if (k == null) {
			throw new RuntimeException("Missing functor: " + k);
		}
		Fn s = e.set.accept(env, this);
		Functor f = k.accept(env, new CatOps(ENV));
		if (!FinSet.FinSet.equals(f.source)) {
			throw new RuntimeException("Domain is not Set in " + e);
		}
		if (!FinSet.FinSet.equals(f.target)) {
			throw new RuntimeException("Codomain is not Set in " + e);
		}
		return (Fn) f.applyA(s);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLPPProgram env, fql_lib.fqlpp.FnExp.ApplyTrans e) {
		TransExp k = env.trans.get(e.f);
		if (k == null) {
			throw new RuntimeException("Missing transform: " + k);
		}
		Transform f = k.accept(env, new CatOps(ENV));
		if (!FinSet.FinSet.equals(f.source.source)) {
			throw new RuntimeException("Domain is not Set in " + e);
		}
		if (!FinSet.FinSet.equals(f.target.target)) {
			throw new RuntimeException("Codomain is not Set in " + e);
		}
		Set<?> s = e.set.accept(env, this);
		return (Fn) f.apply(s);
	}

}
