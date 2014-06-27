package fql_lib.decl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import fql_lib.Chc;
import fql_lib.Pair;
import fql_lib.cat.Category;
import fql_lib.cat.CoMonad;
import fql_lib.cat.FDM;
import fql_lib.cat.Functor;
import fql_lib.cat.Monad;
import fql_lib.cat.Transform;
import fql_lib.cat.categories.FinCat;
import fql_lib.cat.categories.FinSet;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.cat.categories.FunCat;
import fql_lib.cat.categories.Groth;
import fql_lib.cat.categories.Inst;
import fql_lib.cat.presentation.Instance;
import fql_lib.cat.presentation.Mapping;
import fql_lib.cat.presentation.Signature;
import fql_lib.cat.presentation.Signature.Edge;
import fql_lib.cat.presentation.Signature.Node;
import fql_lib.cat.presentation.Signature.Path;
import fql_lib.decl.CatExp.CatExpVisitor;
import fql_lib.decl.CatExp.Cod;
import fql_lib.decl.CatExp.Const;
import fql_lib.decl.CatExp.Dom;
import fql_lib.decl.CatExp.Exp;
import fql_lib.decl.CatExp.Kleisli;
import fql_lib.decl.CatExp.Named;
import fql_lib.decl.CatExp.One;
import fql_lib.decl.CatExp.Pivot;
import fql_lib.decl.CatExp.Plus;
import fql_lib.decl.CatExp.Times;
import fql_lib.decl.CatExp.Zero;
import fql_lib.decl.FunctorExp.Apply;
import fql_lib.decl.FunctorExp.Case;
import fql_lib.decl.FunctorExp.CatConst;
import fql_lib.decl.FunctorExp.Comp;
import fql_lib.decl.FunctorExp.Curry;
import fql_lib.decl.FunctorExp.Eval;
import fql_lib.decl.FunctorExp.FF;
import fql_lib.decl.FunctorExp.FinalConst;
import fql_lib.decl.FunctorExp.Fst;
import fql_lib.decl.FunctorExp.FunctorExpVisitor;
import fql_lib.decl.FunctorExp.Id;
import fql_lib.decl.FunctorExp.Inl;
import fql_lib.decl.FunctorExp.Inr;
import fql_lib.decl.FunctorExp.InstConst;
import fql_lib.decl.FunctorExp.Iso;
import fql_lib.decl.FunctorExp.MapConst;
import fql_lib.decl.FunctorExp.Migrate;
import fql_lib.decl.FunctorExp.Prod;
import fql_lib.decl.FunctorExp.Prop;
import fql_lib.decl.FunctorExp.SetSetConst;
import fql_lib.decl.FunctorExp.Snd;
import fql_lib.decl.FunctorExp.TT;
import fql_lib.decl.FunctorExp.Uncurry;
import fql_lib.decl.FunctorExp.Var;
import fql_lib.decl.TransExp.Adj;
import fql_lib.decl.TransExp.AndOrNotImplies;
import fql_lib.decl.TransExp.ApplyPath;
import fql_lib.decl.TransExp.ApplyTrans;
import fql_lib.decl.TransExp.Bool;
import fql_lib.decl.TransExp.Chr;
import fql_lib.decl.TransExp.Inj;
import fql_lib.decl.TransExp.Ker;
import fql_lib.decl.TransExp.PeterApply;
import fql_lib.decl.TransExp.Proj;
import fql_lib.decl.TransExp.SetSet;
import fql_lib.decl.TransExp.ToCat;
import fql_lib.decl.TransExp.ToInst;
import fql_lib.decl.TransExp.ToMap;
import fql_lib.decl.TransExp.ToSet;
import fql_lib.decl.TransExp.TransExpVisitor;
import fql_lib.decl.TransExp.Whisker;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CatOps implements CatExpVisitor<Category, FQLProgram>,
		FunctorExpVisitor<Functor, FQLProgram>, TransExpVisitor<Transform, FQLProgram> {

	private Environment ENV;
	public CatOps(Environment ENV) {
		this.ENV = ENV;
	}

	@SuppressWarnings("unused")
	private CatOps() { }
	
	@Override
	public Category visit(FQLProgram env, Zero e) {
		return FinCat.FinCat.initial();
	}

	@Override
	public Category visit(FQLProgram env, One e) {
		return FinCat.FinCat.terminal();
	}

	@Override
	public Category visit(FQLProgram env, Plus e) {
		Category<?, ?> a = e.a.accept(env, this);
		Category<?, ?> b = e.b.accept(env, this);
		return FinCat.FinCat.coproduct(a, b);
	}

	@Override
	public Category visit(FQLProgram env, Times e) {
		Category<?, ?> a = e.a.accept(env, this);
		Category<?, ?> b = e.b.accept(env, this);
		return FinCat.FinCat.product(a, b);
	}

	@Override
	public Category visit(FQLProgram env, Exp e) {
		Category<?, ?> a = e.a.accept(env, this);
		Category<?, ?> b = e.b.accept(env, this);
		if (!a.isInfinite() && !b.isInfinite()) {
			return FinCat.FinCat.exp(a, b);
		} else if (!b.isInfinite() && a.equals(FinSet.FinSet)) {
			return Inst.get(b);
		} else if (!b.isInfinite() && a.equals(FinCat.FinCat)) {
			return FunCat.get(b);
		} else {
			throw new RuntimeException("Cannot compute category " + a + "^" + b);
		}
	}

	@Override
	public Category visit(FQLProgram env, fql_lib.decl.CatExp.Var e) {
		Category<?, ?> x = ENV.cats.get(e.v);
		if (x == null) {
			throw new RuntimeException("Missing category: " + e);
		}
		return x;
	}

	@Override
	public Category visit(FQLProgram env, fql_lib.decl.CatExp.Const e) {
		Signature<String, String> s = new Signature<>(e.nodes, e.arrows, e.eqs);
		return s.toCat();
	}

	@Override
	public Category visit(FQLProgram env, Dom e) {
		return e.f.accept(env, this).source;
	}

	@Override
	public Category visit(FQLProgram env, Cod e) {
		return e.f.accept(env, this).target;
	}

	@Override
	public Functor visit(FQLProgram env, Id e) {
		Category c = e.t.accept(env, this);
		return FinCat.FinCat.identity(c);
	}

	@Override
	public Functor visit(FQLProgram env, Comp e) {
		Functor f = e.l.accept(env, this);
		Functor g = e.r.accept(env, this);
		return FinCat.FinCat.compose(f, g);
	}

	@Override
	public Functor visit(FQLProgram env, Var e) {
		Functor x = ENV.ftrs.get(e.v);
		if (x == null) {
			throw new RuntimeException("Missing functor: " + e);
		}
		return x;
	}

	public static CatExp resolve(FQLProgram env, CatExp c) {
		if (c instanceof CatExp.Var) {
			return resolve(env, env.cats.get(((CatExp.Var) c).v));
		}
		return c;
	}

	@Override
	public Functor visit(FQLProgram env, InstConst ic) {
		CatExp e = resolve(env, ic.sig);
		if (!(e instanceof Const)) {
			throw new RuntimeException(
					"Can only create instances for finitely-presented categories.");
		}
		Const c = (Const) e;

		Category src = c.accept(env, this);
		Signature<String, String> sig = new Signature<>(c.nodes, c.arrows, c.eqs);

		Map<Node, Set> nm = new HashMap<>();
		for (Node n : sig.nodes) {
			SetExp kkk = ic.nm.get(n.name);
			if (kkk == null) {
				throw new RuntimeException("Missing node mapping from " + n);
			}
			nm.put(n, kkk.accept(env, new SetOps(ENV)));
		}
		Map<Edge, Map> em = new HashMap<>();
		for (Edge n : sig.edges) {
			Chc<FnExp, SetExp> chc = ic.em.get(n.name);
			if (chc == null) {
				throw new RuntimeException("Missing edge mapping from " + n);
			}
			if (chc.left) {
				FnExp kkk = chc.l;
				em.put(n, kkk.accept(env, new SetOps(ENV)).toMap());
			} else {
				SetExp sss = chc.r;
				Set vvv = sss.accept(env, new SetOps(ENV));
				Map<Object, Object> uuu = new HashMap<>();
				for (Object o : vvv) {
					if (!(o instanceof Pair)) {
						throw new RuntimeException("Not a pair: " + o);
					}
					Pair oo = (Pair) o;
					if (uuu.containsKey(oo.first)) {
						throw new RuntimeException("Duplicate domain entry: " + o + " in " + ic);
					}
					uuu.put(oo.first, oo.second);
				}
				FnExp kkk = new FnExp.Const(uuu::get, ic.nm.get(n.source.name),
						ic.nm.get(n.target.name));
				em.put(n, kkk.accept(env, new SetOps(ENV)).toMap());
			}
		}

		Instance<String, String> I = new Instance(nm, em, sig);

		Function f = p0 -> {
			Path p = (Path) p0;
			return new Fn(I.nm.get(p.source), I.nm.get(p.target), x -> I.evaluate(p).get(x));
		};

		return new Functor(src, FinSet.FinSet, I.nm::get, f);
	}
	

	@Override
	public Functor visit(FQLProgram env, MapConst ic) {
		CatExp src0 = resolve(env, ic.src);
		if (src0 == null) {
			throw new RuntimeException("Missing category: " + ic.src);
		}
		if (!(src0 instanceof Const)) {
			throw new RuntimeException(
					"Can only create mappings for finitely-presented categories.");
		}
		Const src = (Const) src0;

		CatExp dst0 = resolve(env, ic.dst);
		if (!(dst0 instanceof Const)) {
			throw new RuntimeException(
					"Can only create mappings for finitely-presented categories.");
		}
		Const dst = (Const) dst0;

		Category srcX = src.accept(env, this);
		Category dstX = dst.accept(env, this);

		Signature<String, String> srcY = new Signature<>(src.nodes, src.arrows, src.eqs);
		Signature<String, String> dstY = new Signature<>(dst.nodes, dst.arrows, dst.eqs);

		Map<Node, Node> nm = new HashMap<>();
		for (Node n : srcY.nodes) {
			nm.put(n, dstY.getNode(ic.nm.get(n.name)));
		}
		Map<Edge, Path> em = new HashMap<>();
		for (Edge n : srcY.edges) {
			Pair<String, List<String>> k = ic.em.get(n.name);
			em.put(n, dstY.path(k.first, k.second));
		}

		Mapping<String, String, String, String> I = new Mapping(nm, em, srcY, dstY);
		Function f = p0 -> {
			Path p = (Path) p0;
			return I.apply(p);
		};

		return new Functor(srcX, dstX, I.nm::get, f);
	}

	@Override
	public Functor visit(FQLProgram env, CatConst ic) {
		CatExp e = resolve(env, ic.sig);
		if (!(e instanceof Const)) {
			throw new RuntimeException(
					"Can only create functors to cat from finitely-presented categories.");
		}
		Const c = (Const) e;

		Category cat = c.accept(env, this);
		Signature<String, String> sig = new Signature<>(c.nodes, c.arrows, c.eqs);

		Map<Node, Category> nm = new HashMap<>();
		for (Node n : sig.nodes) {
			CatExp kkk = ic.nm.get(n.name);
			if (kkk == null) {
				throw new RuntimeException("Missing node mapping from " + n);
			}
			nm.put(n, kkk.accept(env, this));
		}
		Map<Edge, Functor> em = new HashMap<>();
		for (Edge n : sig.edges) {
			FunctorExp chc = ic.em.get(n.name);
			if (chc == null) {
				throw new RuntimeException("Missing edge mapping from " + n);
			}
			em.put(n, chc.accept(env, this));
		}

		Function fff = p0 -> {
			Path p = (Path) p0;
			Functor fn = FinCat.FinCat.identity(nm.get(p.source));
			for (Object nnn : p.path) {
				Edge n = (Edge) nnn;
				fn = FinCat.FinCat.compose(fn, em.get(n));
			}
			return fn;
		};

		return new Functor<>(cat, FinCat.FinCat, nm::get, fff);
	}

	@Override
	public Functor visit(FQLProgram env, FinalConst ic) {
		CatExp e = resolve(env, ic.src);
		if (!(e instanceof Const)) {
			throw new RuntimeException(
					"Can only create functors from finitely-presented categories.");
		}
		Const c = (Const) e;

		Category cat = c.accept(env, this);
		Signature<String, String> sig = new Signature<>(c.nodes, c.arrows, c.eqs);

		Category target = ic.C.accept(env, this);

		Map<Node, Functor> nm = new HashMap<>();
		for (Node n : sig.nodes) {
			FunctorExp kkk = ic.nm.get(n.name);
			if (kkk == null) {
				throw new RuntimeException("Missing node mapping from " + n);
			}
			Functor F = kkk.accept(env, this);
			nm.put(n, F);
		}
		Map<Edge, Transform> em = new HashMap<>();
		for (Edge n : sig.edges) {
			TransExp chc = ic.em.get(n.name);
			if (chc == null) {
				throw new RuntimeException("Missing edge mapping from " + n);
			}
			em.put(n, chc.accept(env, this));
		}

		Function fff = p0 -> {
			Path p = (Path) p0;
			Object fn = target.identity(nm.get(p.source));
			for (Object nnn : p.path) {
				Edge n = (Edge) nnn;
				fn = target.compose(fn, em.get(n));
			}
			return fn;
		};

		return new Functor<>(cat, target, nm::get, fff);
	}

	@Override
	public Functor visit(FQLProgram env, TT e) {
		Category<?, ?> c = e.t.accept(env, this);
		return FinCat.FinCat.terminal(c);
	}

	@Override
	public Functor visit(FQLProgram env, FF e) {
		Category<?, ?> c = e.t.accept(env, this);
		return FinCat.FinCat.initial(c);
	}

	@Override
	public Functor visit(FQLProgram env, Fst e) {
		Category<?, ?> s = e.s.accept(env, this);
		Category<?, ?> t = e.t.accept(env, this);
		return FinCat.FinCat.first(s, t);
	}

	@Override
	public Functor visit(FQLProgram env, Snd e) {
		Category<?, ?> s = e.s.accept(env, this);
		Category<?, ?> t = e.t.accept(env, this);
		return FinCat.FinCat.second(s, t);
	}

	@Override
	public Functor visit(FQLProgram env, Inl e) {
		Category<?, ?> s = e.s.accept(env, this);
		Category<?, ?> t = e.t.accept(env, this);
		return FinCat.FinCat.inleft(s, t);
	}

	@Override
	public Functor visit(FQLProgram env, Inr e) {
		Category<?, ?> s = e.s.accept(env, this);
		Category<?, ?> t = e.t.accept(env, this);
		return FinCat.FinCat.inright(s, t);
	}

	@Override
	public Functor visit(FQLProgram env, Eval e) {
		Category<?, ?> s = e.s.accept(env, this);
		Category<?, ?> t = e.t.accept(env, this);
		return FinCat.FinCat.eval(s, t);
	}

	@Override
	public Functor visit(FQLProgram env, Curry e) {
		Functor f = e.f.accept(env, this);
		if (!f.source.isInfinite() && f.target.equals(FinSet.FinSet)) {
			return Inst.CURRY(f); 
		}
		return FinCat.FinCat.curry(f);
	}

	@Override
	public Functor visit(FQLProgram env, Case e) {
		Functor l = e.l.accept(env, this);
		Functor r = e.r.accept(env, this);
		if (FinSet.FinSet.equals(l.target) && FinSet.FinSet.equals(r.target)) {
			if (!l.source.equals(r.source)) {
				throw new RuntimeException("Source categories do not match");
			}
			return Inst.get(l.source).coproduct(l, r);
		}
		if (FinCat.FinCat.equals(l.target) && FinCat.FinCat.equals(r.target)) {
			if (!l.source.equals(r.source)) {
				throw new RuntimeException("Source categories do not match");
			}
			return FunCat.get(l.source).coproduct(l, r);
		}

		return FinCat.FinCat.match(l, r);
	}

	@Override
	public Functor visit(FQLProgram env, Prod e) {
		Functor l = e.l.accept(env, this);
		Functor r = e.r.accept(env, this);
		if (FinSet.FinSet.equals(l.target) && FinSet.FinSet.equals(r.target)) {
			if (!l.source.equals(r.source)) {
				throw new RuntimeException("Source categories do not match");
			}
			return Inst.get(l.source).product(l, r);
		}
		if (FinCat.FinCat.equals(l.target) && FinCat.FinCat.equals(r.target)) {
			if (!l.source.equals(r.source)) {
				throw new RuntimeException("Source categories do not match");
			}
			return FunCat.get(l.source).product(l, r);
		}
		return FinCat.FinCat.pair(l, r);
	}
	
	@Override
	public Functor visit(FQLProgram env, FunctorExp.Exp e) {
		Functor l = e.l.accept(env, this);
		Functor r = e.r.accept(env, this);
		if (FinSet.FinSet.equals(l.target) && FinSet.FinSet.equals(r.target)) {
			if (!l.source.equals(r.source)) {
				throw new RuntimeException("Source categories do not match");
			}
			return Inst.get(l.source).exp(l, r);
		}
		if (FinCat.FinCat.equals(l.target) && FinCat.FinCat.equals(r.target)) {
			if (!l.source.equals(r.source)) {
				throw new RuntimeException("Source categories do not match");
			}
			throw new RuntimeException("Not implemented yet"); //TODO
//			return FunCat.get(l.source).product(l, r);
		}
		throw new RuntimeException("Cannot exponentiate " + l + " and " + r);
	}

	public Functor visit(FQLProgram env, Iso e) {
		Category l = e.l.accept(env, this);
		Category r = e.r.accept(env, this);
		Optional<Pair<Functor, Functor>> k = FinCat.FinCat.iso(l, r);
		if (!k.isPresent()) {
			throw new RuntimeException("Not isomorphic: " + e.l + " and " + e.r);
		}
		if (e.lToR) {
			return k.get().first;
		} else {
			return k.get().second;
		}
	}

	@Override
	public Functor visit(FQLProgram env, SetSetConst e) {
		Function o = x -> {
			Set s = (Set) x;
			Environment env2 = new Environment(ENV);
//			FQLProgram env2 = new FQLProgram(env);
	//		SetExp c = new SetExp.Const(s);
			env2.sets.put(e.ob, s);
			return e.set.accept(env, new SetOps(env2));
		};

		Function a = x -> {
			Fn s = (Fn) x;
			Set src = s.source;
			Set dst = s.target;
		//	SetExp srcE = new SetExp.Const(src);
		//	SetExp dstE = new SetExp.Const(dst);
			Environment env2 = new Environment(ENV);
		//	FnExp c = new FnExp.Const(s::apply, srcE, dstE);
			env2.sets.put(e.src, src);
			env2.sets.put(e.dst, dst);
			env2.fns.put(e.f,s);
			return e.fun.accept(env, new SetOps(env2));
		};

		return new Functor(FinSet.FinSet, FinSet.FinSet, o, a);
	}

	@Override
	public Category visit(FQLProgram env, Named e) {
		if (e.name.equals("Set")) {
			return FinSet.FinSet;
		} else if (e.name.equals("Cat")) {
			return FinCat.FinCat;
		} else {
			throw new RuntimeException(e.name + " is not a category.");
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Id e) {
		Functor ff = e.t.accept(env, this);
		return Transform.id(ff);
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Comp e) {
		Transform l = e.l.accept(env, this);
		Transform r = e.r.accept(env, this);
		return Transform.compose(l, r);
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Var e) {
		Transform<?, ?, ?, ?> x = ENV.trans.get(e.v);
		if (x == null) {
			throw new RuntimeException("Missing transform: " + e);
		}
		return x;
	}

	@Override
	public Transform visit(FQLProgram env, SetSet e) {
		Functor s = e.src.accept(env, this);
		Functor t = e.dst.accept(env, this);
		Function o = x -> {
			Set set = (Set) x;
			Environment env2 = new Environment(ENV);
		//	SetExp c = new SetExp.Const(set);
			env2.sets.put(e.ob, set);
			return e.fun.accept(env, new SetOps(env2));
		};

		return new Transform(s, t, o);
	}

	@Override
	public Transform visit(FQLProgram env, ToMap e) {
		Functor s = e.src.accept(env, this);
		Functor t = e.dst.accept(env, this);
		CatExp scat = resolve(env, e.s);
		if (!(scat instanceof CatExp.Const)) {
			throw new RuntimeException("Source category of " + e + " is not a constant.");
		}
		// CatExp.Const scon = (CatExp.Const) scat;
		CatExp tcat = resolve(env, e.t);
		if (!(tcat instanceof CatExp.Const)) {
			throw new RuntimeException("Target category of " + e + " is not a constant.");
		}
		CatExp.Const tcon = (CatExp.Const) tcat;

		// Signature ssig = new Signature(scon.nodes, scon.arrows, scon.eqs);
		Signature<String, String> tsig = new Signature<String, String>(tcon.nodes, tcon.arrows,
				tcon.eqs);

		Function o = x -> {
			Node n = (Node) x;
			// Set src = (Set) s.applyO(n);
			// Set dst = (Set) t.applyO(n);
			Pair<String, List<String>> k = e.fun.get(n.name);
			Signature<String, String>.Path fun = tsig.path(k.first, k.second);
			return fun; // new Fn(src, dst, fun);
		};

		return new Transform(s, t, o);
	}

	@Override
	public Transform visit(FQLProgram env, ToSet e) {
		Functor s = e.src.accept(env, this);
		Functor t = e.dst.accept(env, this);
		Function o = x -> {
			Node n = (Node) x;
			Chc<FnExp, SetExp> chc = e.fun.get(n.name);
			if (chc == null) {
				throw new RuntimeException("Missing object mapping for: " + n.name);
			}
			if (chc.left) {
				return chc.l.accept(env, new SetOps(ENV));
			} else {
				Set src = (Set) s.applyO(n);
				Set dst = (Set) t.applyO(n);
				Set<Pair> p = (Set<Pair>) chc.r.accept(env, new SetOps(ENV));
				Map<Object, Object> map = new HashMap<>();
				for (Pair h : p) {
					if (map.containsKey(h.first)) {
						throw new RuntimeException("Duplicate arg: " + e);
					}
					map.put(h.first, h.second);
				}
				return new Fn(src, dst, map::get);
			}
		};

		return new Transform(s, t, o);
	}

	@Override
	public Transform visit(FQLProgram env, ToCat e) {
		Functor s = e.src.accept(env, this);
		Functor t = e.dst.accept(env, this);
		Function o = x -> {
			Node n = (Node) x;
			// Set src = (Set) s.applyO(n);
			// Set dst = (Set) t.applyO(n);
			Functor fun = e.fun.get(n.name).accept(env, this);
			// if (!src.equals(fun.source)) {
			return fun; // new Fn(src, dst, fun);
		};

		return new Transform(s, t, o);
	}

	@Override
	public Transform visit(FQLProgram env, ToInst e) {
		Functor s = e.src.accept(env, this);
		Functor t = e.dst.accept(env, this);
		Function o = x -> {
			Node n = (Node) x;
			// Set src = (Set) s.applyO(n);
			// Set dst = (Set) t.applyO(n);
			Transform fun = e.fun.get(n.name).accept(env, this);
			// if (!src.equals(fun.source)) {
			return fun; // new Fn(src, dst, fun);
		};

		return new Transform(s, t, o);
	}

	@Override
	public Category visit(FQLProgram env, Kleisli e) {
		FunctorExp f0 = env.ftrs.get(e.F);
		if (f0 == null) {
			throw new RuntimeException("Missing functor: " + f0);
		}
		TransExp ret0 = env.trans.get(e.unit);
		if (ret0 == null) {
			throw new RuntimeException("Missing transform: " + ret0);
		}
		TransExp join0 = env.trans.get(e.join);
		if (join0 == null) {
			throw new RuntimeException("Missing transform: " + join0);
		}

		Functor F = f0.accept(env, this);
		Transform ret = ret0.accept(env, this);
		Transform join = join0.accept(env, this);
		if (e.isCo) {
			CoMonad m = new CoMonad(F, ret, join);
			return m.cokleisli();
		} else {
			Monad m = new Monad(F, ret, join);
			return m.kleisli();
		}
	}

	// TODO for these, if target is Cat, use ToCat instead of Inst.

	@Override
	public Transform visit(FQLProgram env, Proj e) {
		Functor l = e.l.accept(env, this);
		Functor r = e.r.accept(env, this);
		if (!l.source.equals(r.source)) {
			throw new RuntimeException("Source category does not match");
		}
		if (!l.target.equals(r.target)) {
			throw new RuntimeException("Target category does not match");
		}
		if (l.target.equals(FinSet.FinSet)) {
			if (e.proj1) {
				return Inst.get(l.source).first(l, r);
			} else {
				return Inst.get(l.source).second(l, r);
			}
		} else if (l.target.equals(FinCat.FinCat)) {
			if (e.proj1) {
				return FunCat.get(l.source).first(l, r);
			} else {
				return FunCat.get(l.source).second(l, r);
			}
		} else {
			throw new RuntimeException("report this error to ryan");
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Prod e) {
		Transform l = e.l.accept(env, this);
		Transform r = e.r.accept(env, this);
		if (!l.source.equals(r.source)) {
			throw new RuntimeException("Source functors do not match");
		}
		if (!l.target.source.equals(r.target.source)) {
			throw new RuntimeException("Categories do not match");
		}
		if (l.source.target.equals(FinSet.FinSet)) {
			return Inst.get(l.target.source).pair(l, r);
		} else if (l.source.target.equals(FinCat.FinCat)) {
			return FunCat.get(l.target.source).pair(l, r);
		} else {
			throw new RuntimeException("Report this error to ryan.");
		}
	}

	@Override
	public Functor visit(FQLProgram env, fql_lib.decl.FunctorExp.One e) {
		Category<?, ?> cat = e.cat.accept(env, this);
		Category<?, ?> amb = e.ambient.accept(env, this);
		if (amb.equals(FinSet.FinSet)) {
			return Inst.get(cat).terminal();
		} else if (amb.equals(FinCat.FinCat)) {
			return FunCat.get(cat).terminal();
		} else {
			throw new RuntimeException("Report this error to Ryan. Error: ambient category is "
					+ amb);
		}
	}

	@Override
	public Functor visit(FQLProgram env, fql_lib.decl.FunctorExp.Zero e) {
		Category<?, ?> cat = e.cat.accept(env, this);
		Category<?, ?> amb = e.ambient.accept(env, this);
		if (amb.equals(FinSet.FinSet)) {
			return Inst.get(cat).initial();
		} else if (amb.equals(FinCat.FinCat)) {
			return FunCat.get(cat).initial();
		} else {
			throw new RuntimeException("Report this error to Ryan. Error: ambient category is "
					+ amb);
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.One e) {
		Functor F = e.f.accept(env, this);
		if (F.target.equals(FinSet.FinSet)) {
			return Inst.get(F.source).terminal(F);
		} else if (F.target.equals(FinCat.FinCat)) {
			return FunCat.get(F.source).terminal(F);
		} else {
			throw new RuntimeException("Error: Please send your file to Ryan.");
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Zero e) {
		Functor F = e.f.accept(env, this);
		if (F.target.equals(FinSet.FinSet)) {
			return Inst.get(F.source).initial(F);
		} else if (F.target.equals(FinCat.FinCat)) {
			return FunCat.get(F.source).initial(F);
		} else {
			throw new RuntimeException("Error: Please send your file to Ryan.");
		}
	}

	@Override
	public Transform visit(FQLProgram env, Inj e) {
		Functor l = e.l.accept(env, this);
		Functor r = e.r.accept(env, this);
		if (!l.source.equals(r.source)) {
			throw new RuntimeException("Source category does not match");
		}
		if (l.target.equals(FinSet.FinSet)) {
			if (e.inj1) {
				return Inst.get(l.source).inleft(l, r);
			} else {
				return Inst.get(l.source).inright(l, r);
			}
		} else if (l.target.equals(FinCat.FinCat)) {
			if (e.inj1) {
				return FunCat.get(l.source).inleft(l, r);
			} else {
				return FunCat.get(l.source).inright(l, r);
			}
		} else {
			throw new RuntimeException("Report this error to ryan");
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.CoProd e) {
		Transform l = e.l.accept(env, this);
		Transform r = e.r.accept(env, this);
		if (!l.target.equals(r.target)) {
			throw new RuntimeException("Target functors do not match");
		}
		if (!l.target.source.equals(r.target.source)) {
			throw new RuntimeException("Categories do not match");
		}
		if (l.source.target.equals(FinSet.FinSet)) {
			return Inst.get(l.target.source).match(l, r);
		} else if (l.source.target.equals(FinCat.FinCat)) {
			return FunCat.get(l.target.source).match(l, r);
		} else {
			throw new RuntimeException("Report this error to Ryan");
		}
	}

	@Override
	public Functor visit(FQLProgram env, Uncurry e) {
		Functor f = e.F.accept(env, this);
		if (!f.source.isInfinite() && f.target instanceof Inst) {
			return Inst.UNCURRY(f);
		}
		throw new RuntimeException("Cannot uncurry " + f);
	}

	@Override
	public Functor visit(FQLProgram env, Migrate e) {
		Functor F = e.F.accept(env, this);
		if (e.which.equals("delta")) {
			return FDM.deltaF(F);
		} else if (e.which.equals("sigma")) {
			return FDM.sigmaF(F);
		} else if (e.which.equals("pi")) {
			return FDM.piF(F);
		}
		throw new RuntimeException("Report this error to Ryan.");
	}

	@Override
	public Functor visit(FQLProgram env, Apply e) {
		Functor ret1 = null;
		Exception ret1_e = null;
		
		Functor ret2 = null;
		Exception ret2_e = null;

		Functor F = e.F.accept(env, this);
		
		try {
			Functor I = e.I.accept(env, this);
			ret1 = (Functor) F.applyO(I);
		} catch (Exception ex) {
			ret1_e = ex;
		}
		
		try {
			FunctorExp.Var I = (FunctorExp.Var) e.I;
			Signature.Node n = F.source.toSig().new Node(I.v);
			ret2 = (Functor) F.applyO(n);
		} catch (Exception ex) {
			ret2_e = ex;
		}
		
		if (ret1 != null && ret2 != null) {
			throw new RuntimeException("Ambiguous: " + e.I + " is an object in two different categories.");
		}
		
		if (ret1 != null) {
			return ret1;
		}
		if (ret2 != null) {
			return ret2;
		}
		
		ret1_e.printStackTrace();
		ret2_e.printStackTrace();
		throw new RuntimeException("Cannot apply:\n\nmost probable cause: " + ret1_e.getMessage() + "\n\nless probable cause: " + ret2_e.getMessage());
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Apply e) {
		Functor F = e.F.accept(env, this);
		Transform I = e.I.accept(env, this);
		return (Transform) F.applyA(I);
	}

	@Override
	public Transform visit(FQLProgram env, ApplyPath e) {
		Functor F = e.F.accept(env, this);
		CatExp c = resolve(env, e.cat);
		if (!(c instanceof CatExp.Const)) {
			throw new RuntimeException("Can only take paths in constant categories.");
		}
		CatExp.Const C = (CatExp.Const) c;
		Signature s = new Signature(C.nodes, C.arrows, C.eqs);
		Signature.Path n = s.path(e.node, e.edges);
		return (Transform) F.applyA(n);
	}

	@Override
	public Transform visit(FQLProgram env, Adj e) {
		Functor F = e.F.accept(env, this);
		if (e.which.equals("return") && e.L.equals("sigma") && e.R.equals("delta")) {
			return FDM.sigmaDelta(F).unit;
		} else if (e.which.equals("coreturn") && e.L.equals("sigma") && e.R.equals("delta")) {
			return FDM.sigmaDelta(F).counit;
		} else if (e.which.equals("return") && e.L.equals("delta") && e.R.equals("pi")) {
			return FDM.deltaPi(F).unit;
		} else if (e.which.equals("coreturn") && e.L.equals("delta") && e.R.equals("pi")) {
			return FDM.deltaPi(F).counit;
		} else {
			throw new RuntimeException("Report this error to Ryan.");
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Iso e) {
		Functor l = e.l.accept(env, this);
		Functor r = e.r.accept(env, this);
		if (!l.source.equals(r.source)) {
			throw new RuntimeException("Source categories do not match: " + l.source + "\nand\n" + r.source);
		}
		if (l.source.isInfinite()) {
			throw new RuntimeException("Source category must be finite.");
		}
		if (!l.target.equals(FinSet.FinSet)) {
			throw new RuntimeException("Target category must be Set.");
		}
		Optional<Pair<Transform, Transform>> k = Inst.get(l.source).iso(l, r);
		if (!k.isPresent()) {
			throw new RuntimeException("Not isomorphic: " + e.l + " and " + e.r);
		}
		if (e.lToR) {
			return k.get().first;
		} else {
			return k.get().second;
		}
	}

	@Override
	public Transform visit(FQLProgram env, ApplyTrans e) {
		Transform F = e.F.accept(env, this);
		Functor I = e.I.accept(env, this);
		return (Transform) F.apply(I);
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Curry e) {
		Transform t = e.f.accept(env, this);
		if (t.source.source.isInfinite() || !t.source.target.equals(FinSet.FinSet)) {
			throw new RuntimeException("Cannot curry " + t);
		}
		if (e.useInst) {
			return Inst.get(t.source.source).curry(t);
		} else {
			return Inst.CURRY(t);
		}
	}

	@Override
	public Transform visit(FQLProgram env, fql_lib.decl.TransExp.Eval e) {
		Functor a = e.a.accept(env, this);
		Functor b = e.b.accept(env, this);
		if (!a.source.equals(b.source) || a.source.isInfinite() || !a.target.equals(FinSet.FinSet)) {
			throw new RuntimeException("Cannot eval " + a + " and " + b);
		}
		return Inst.get(a.source).eval(a, b);
	}

	@Override
	public Transform visit(FQLProgram env, Whisker e) {
		Functor F  = e.func .accept(env, this);
		Transform T=e.trans.accept(env, this);
		
		if (e.left) {
			return Transform.leftWhisker(F, T);
		} else {
			return Transform.rightWhisker(F, T);
		}
	}

	@Override
	public Functor visit(FQLProgram env, Prop e) {
		Category c = e.cat.accept(env, this);
		return Inst.get(c).prop();
	}

	@Override
	public Transform visit(FQLProgram env, Bool e) {
		Category c = e.cat.accept(env, this);
		if (e.b) {
			return Inst.get(c).tru();
		} else {
			return Inst.get(c).fals();
		}
	}

	@Override
	public Transform visit(FQLProgram env, Chr e) {
		Transform t = e.t.accept(env, this);
		return Inst.get(t.source.source).chr(t);
	}

	@Override
	public Transform visit(FQLProgram env, Ker e) {
		Transform t = e.t.accept(env, this);
		return Inst.get(t.source.source).kernel(t);
	}

	@Override
	public Functor visit(FQLProgram env, fql_lib.decl.FunctorExp.Dom e) {
		Transform t = new TransExp.Var(e.t).accept(env, this);
		if (e.dom) {
			return t.source;
		} else {
			return t.target;
		}
	}

	@Override
	public Transform visit(FQLProgram env, AndOrNotImplies e) {
		Category c = e.cat.accept(env, this);
		if (e.which.equals("not")) {
			return Inst.get(c).not();
		} else {
			return Inst.get(c).andOrImplies(e.which);
		}
	}

	@Override
	public Transform visit(FQLProgram env, PeterApply e) {
		Transform t = e.t.accept(env, this);
		Signature.Node n = t.source.source.toSig().new Node(e.node);
		return (Transform) t.apply(n);
	}

	@Override
	public Category visit(FQLProgram env, Pivot e) {
		Functor F = new FunctorExp.Var(e.F).accept(env, this);
		Category ret = Groth.pivot(F);
//			System.out.println(ret.objects().size());
	//		System.out.println(ret.arrows().size());
		//	System.out.println(ret);
		return ret;
	}
}
