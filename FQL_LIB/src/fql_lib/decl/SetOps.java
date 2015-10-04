package fql_lib.decl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import catdata.algs.Pair;
import fql_lib.cat.Functor;
import fql_lib.cat.Transform;
import fql_lib.cat.categories.FinSet;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.decl.FnExp.Case;
import fql_lib.decl.FnExp.Chr;
import fql_lib.decl.FnExp.Comp;
import fql_lib.decl.FnExp.Const;
import fql_lib.decl.FnExp.Curry;
import fql_lib.decl.FnExp.Eval;
import fql_lib.decl.FnExp.FF;
import fql_lib.decl.FnExp.FnExpVisitor;
import fql_lib.decl.FnExp.Fst;
import fql_lib.decl.FnExp.Id;
import fql_lib.decl.FnExp.Inl;
import fql_lib.decl.FnExp.Inr;
import fql_lib.decl.FnExp.Iso;
import fql_lib.decl.FnExp.Krnl;
import fql_lib.decl.FnExp.Prod;
import fql_lib.decl.FnExp.Snd;
import fql_lib.decl.FnExp.TT;
import fql_lib.decl.FnExp.Tru;
import fql_lib.decl.FnExp.Var;
import fql_lib.decl.SetExp.Cod;
import fql_lib.decl.SetExp.Dom;
import fql_lib.decl.SetExp.Exp;
import fql_lib.decl.SetExp.Numeral;
import fql_lib.decl.SetExp.One;
import fql_lib.decl.SetExp.Plus;
import fql_lib.decl.SetExp.Prop;
import fql_lib.decl.SetExp.Range;
import fql_lib.decl.SetExp.SetExpVisitor;
import fql_lib.decl.SetExp.Times;
import fql_lib.decl.SetExp.Zero;

@SuppressWarnings("rawtypes")
public class SetOps implements SetExpVisitor<Set<?>, FQLProgram>, FnExpVisitor<FinSet.Fn, FQLProgram> , Serializable {

	private Environment ENV;
	public SetOps(Environment ENV) {
		this.ENV = ENV;
	}
	
	@SuppressWarnings("unused")
	private SetOps() { }
	
	@Override
	public Fn visit(FQLProgram env, Id e) {
		Set<?> s = e.t.accept(env, this);
		return FinSet.FinSet.identity(s);
	}

	@Override
	public Fn visit(FQLProgram env, Comp e) {
		Fn l = e.l.accept(env, this);
		Fn r = e.r.accept(env, this);
		return FinSet.FinSet.compose(l, r);
	}

	@Override
	public Fn visit(FQLProgram env, Var e) {
		Fn k = ENV.fns.get(e.v);
		if (k == null) {
			throw new RuntimeException("Missing function: " + e);
		}
		return k;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLProgram env, Const e) {
		return new Fn(e.src.accept(env, this), e.dst.accept(env, this), e.f);
	}

	@Override
	public Fn visit(FQLProgram env, TT e) {
		Set<?> s = e.t.accept(env, this);
		return FinSet.FinSet.terminal(s);
	}

	@Override
	public Fn visit(FQLProgram env, FF e) {
		Set<?> s = e.t.accept(env, this);
		return FinSet.FinSet.initial(s);
	}

	@Override
	public Fn visit(FQLProgram env, Fst e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.first(s, t);
	}

	@Override
	public Fn visit(FQLProgram env, Snd e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.second(s, t);
	}

	@Override
	public Fn visit(FQLProgram env, Inl e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.inleft(s, t);

	}

	@Override
	public Fn visit(FQLProgram env, Inr e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.inright(s, t);
	}

	@Override
	public Fn visit(FQLProgram env, Eval e) {
		Set<?> s = e.s.accept(env, this);
		Set<?> t = e.t.accept(env, this);
		return FinSet.FinSet.eval(s, t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLProgram env, Curry e) {
		Fn f = e.f.accept(env, this);
		return FinSet.FinSet.curry(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLProgram env, Case e) {
		Fn s = e.l.accept(env, this);
		Fn t = e.r.accept(env, this);
		return FinSet.FinSet.match(s, t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLProgram env, Prod e) {
		Fn s = e.l.accept(env, this);
		Fn t = e.r.accept(env, this);
		return FinSet.FinSet.pair(s, t);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLProgram env, Iso e) {
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
	public Fn visit(FQLProgram env, Chr e) {
		Fn f = e.f.accept(env, this);
		return FinSet.FinSet.chr(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fn visit(FQLProgram env, Krnl e) {
		Fn f = e.f.accept(env, this);
		return FinSet.FinSet.kernel(f);
	}

	@Override
	public Set<?> visit(FQLProgram env, Zero e) {
		return FinSet.FinSet.initial();
	}

	@Override
	public Set<?> visit(FQLProgram env, One e) {
		return FinSet.FinSet.terminal();
	}

	@Override
	public Set<?> visit(FQLProgram env, Prop e) {
		return FinSet.FinSet.prop();
	}

	@Override
	public Set<?> visit(FQLProgram env, Plus e) {
		Set<?> a = e.a.accept(env, this);
		Set<?> b = e.b.accept(env, this);
		return FinSet.FinSet.coproduct(a, b);
	}

	@Override
	public Set<?> visit(FQLProgram env, Times e) {
		Set<?> a = e.a.accept(env, this);
		Set<?> b = e.b.accept(env, this);
		return FinSet.FinSet.product(a, b);
	}

	@Override
	public Set<?> visit(FQLProgram env, Exp e) {
		Set<?> a = e.a.accept(env, this);
		Set<?> b = e.b.accept(env, this);
		return FinSet.FinSet.exp(a, b);
	}

	@Override
	public Set<?> visit(FQLProgram env, fql_lib.decl.SetExp.Var e) {
		Set x = ENV.sets.get(e.v);
		if (x == null) {
			throw new RuntimeException("Missing set: " + e);
		}
		return x;
	}

	@Override
	public Set<?> visit(FQLProgram env, Dom e) {
		Fn f = e.f.accept(env, this);
		return f.source;
	}
	
	@Override
	public Set<?> visit(FQLProgram env, Cod e) {
		Fn f = e.f.accept(env, this);
		return f.target;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<?> visit(FQLProgram env, Range e) {
		Fn f = e.f.accept(env, this);
		Set<Object> ret = new HashSet<>();
		for (Object o : f.source) {
			ret.add(f.apply(o));
		}
		return ret;
	}

	@Override
	public Set<?> visit(FQLProgram env, fql_lib.decl.SetExp.Const e) {
		return e.s;
	}

	@Override
	public Fn visit(FQLProgram env, Tru e) {
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
	public Set<?> visit(FQLProgram env, Numeral e) {
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
	public Set<?> visit(FQLProgram env, fql_lib.decl.SetExp.Apply e) {
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
	public Fn visit(FQLProgram env, fql_lib.decl.FnExp.Apply e) {
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
	public Fn visit(FQLProgram env, fql_lib.decl.FnExp.ApplyTrans e) {
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
