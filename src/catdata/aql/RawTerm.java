package catdata.aql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Quad;
import catdata.Triple;
import catdata.Util;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.InstExpRaw.Sk;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public final class RawTerm {

	private final String head;
	private final List<RawTerm> args;

	private final String annotation;

	@Override
	public String toString() {
		String str = (annotation == null ? "" : "@" + annotation);
		if (args.isEmpty()) {
			return head + str;
		}
		if (args.size() == 1) {
			return args.get(0) + "." + head;
		}
		return head + "(" + Util.sep(args, ", ") + ")";
	}

	public static Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> infer_good(
			RawTerm e, Chc<Ty, En> expected, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, String pre, AqlJs<Ty, Sym> js,
			Map<Var, Chc<Ty, En>> vars) {
		if (e.annotation != null && !col.tys.contains(new Ty(e.annotation))) {
			throw new RuntimeException(pre + "Annotation " + e.annotation + " is not a type (" + col.tys + ").");
		}

		Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> ret = new HashSet<>();
		if (vars.keySet().contains(new Var((String) e.head)) && e.annotation == null) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Var(new Var((String) e.head));
			if (expected != null) {
				Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>();
				ret2.put(new Var((String) e.head), expected);
				if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
					ret.add(new Triple<>(ret1, ret2, expected));
				}
			} else {
				for (En en : col.ens) {
					Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>();
					ret2.put(new Var((String) e.head), Chc.inRight(en));
					if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
						ret.add(new Triple<>(ret1, ret2, Chc.inRight(en)));
					}
				}
				for (Ty ty : col.tys) {
					Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>();
					if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
						ret2.put(new Var((String) e.head), Chc.inLeft(ty));
					}
					ret.add(new Triple<>(ret1, ret2, Chc.inLeft(ty)));
				}
			}
		}

		if (col.syms.containsKey(new Sym(e.head)) && e.annotation == null) {
			// System.out.println("a " + e);

			List<List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>>> l = new LinkedList<>();
			l.add(new LinkedList<>());
			for (int i = 0; i < e.args.size(); i++) {
				RawTerm arg = e.args.get(i);
				// System.out.println("arg " + arg);

				Ty ty = col.syms.get(new Sym(e.head)).first.get(i);
				Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> z = infer_good(arg,
						Chc.inLeft(ty), col, pre, js, vars);

				List<List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>>> l2 = new LinkedList<>();

				for (List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> old : l) {
					// System.out.println("old " + old);
					for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> y : z) {
						// System.out.println("y " + y);

						if (y.third.equals(Chc.inLeft(ty))) { // takes place of
																// if
																// (argt.equals(outcome.third))
																// {
							// System.out.println("z z");

							l2.add(Util.append(old, Util.singList(y)));
						}
					}
				}
				l = l2;
			}
			// System.out.println("l " + l);

			outer: for (List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> outcome : l) {
				// System.out.println("outcome " + outcome);

				List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> w = outcome.stream().map(x -> x.first)
						.collect(Collectors.toList());
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Sym(new Sym(e.head), w);
				Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>();
				for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> ctx0 : outcome) {
					// System.out.println("ctx0 " + ctx0);

					if (!ctx0.second.agreeOnOverlap(ret2) || !ctx0.second.agreeOnOverlap(Ctx.fromNullable(vars))) {
						// System.out.println("xxx ");
						continue outer;
					}
					// System.out.println("yyy");
					ret2.map.putAll(ctx0.second.map);
				}
				for (int i = 0; i < e.args.size(); i++) {
					RawTerm arg = e.args.get(i);
					// System.out.println("2arx " + arg);
					Chc<Ty, En> ty = Chc.inLeft(col.syms.get(new Sym(e.head)).first.get(i));
					Var v = new Var((String) arg.head);
					if (vars.keySet().contains(v)) {
						// System.out.println("a " + v);
						if (ret2.containsKey(v) && !ret2.get(v).equals(ty)) {
							// System.out.println("b " + v);
							continue;
						} else if (!ret2.containsKey(v)) {
							// System.out.println("c " + v);
							ret2.put(new Var(e.args.get(i).head), ty);
						}
					}
				}

				Chc<Ty, En> ret3 = Chc.inLeft(col.syms.get(new Sym(e.head)).second);
				if (expected != null && !expected.equals(ret3)) {
					// System.out.println("d " );
				} else {
					// System.out.println("e " );
					if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
						ret.add(new Triple<>(ret1, ret2, ret3));
					}
				}
			}

		}
		if (col.fks.containsKey(new Fk(e.head)) && e.args.size() == 1 && e.annotation == null) {
			for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> outcome : infer_good(
					e.args.get(0), Chc.inRight(col.fks.get(new Fk(e.head)).first), col, pre, js, vars)) {
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Fk(new Fk(e.head), outcome.first);
				Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>(outcome.second.map);
				Var v = new Var(e.args.get(0).head);
				Chc<Ty, En> ty = Chc.inRight(col.fks.get(new Fk(e.head)).first);
				if (vars.keySet().contains(v)) {
					if (ret2.containsKey(v) && !ret2.get(v).equals(ty)) {
						continue;
					} else if (!ret2.containsKey(v)) {
						ret2.put(new Var(e.args.get(0).head), ty);
					}
				}
				Chc<Ty, En> ret3 = Chc.inRight(col.fks.get(new Fk(e.head)).second);
				Chc<Ty, En> argt = Chc.inRight(col.fks.get(new Fk(e.head)).first);

				if (expected != null && !expected.equals(ret3)) {
				} else {
					if (argt.equals(outcome.third)) {
						if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
							ret.add(new Triple<>(ret1, ret2, ret3));
						}
					}
				}
			}
		}
		if (col.atts.containsKey(new Att(e.head)) && e.args.size() == 1 && e.annotation == null) {
	//		 System.out.println("x " + e);
			for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> outcome : infer_good(
					e.args.get(0), Chc.inRight(col.atts.get(new Att(e.head)).first), col, pre, js, vars)) {
			//	 System.out.println("y " + outcome);

				Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Att(new Att(e.head), outcome.first);
				Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>(outcome.second.map);
				Var v = new Var(e.args.get(0).head);
				Chc<Ty, En> ty = Chc.inRight(col.atts.get(new Att(e.head)).first);
				if (vars.keySet().contains(v)) {
			//		 System.out.println("z " + v);

					if (ret2.containsKey(v) && !ret2.get(v).equals(ty)) {
				//		 System.out.println("a " + v);

						continue;
					} else if (!ret2.containsKey(v)) {
				//		 System.out.println("b " + v);

						ret2.put(v, ty);
					}
				}

				Chc<Ty, En> ret3 = Chc.inLeft(col.atts.get(new Att(e.head)).second);
				Chc<Ty, En> argt = Chc.inRight(col.atts.get(new Att(e.head)).first);

				if (expected != null && !expected.equals(ret3)) {
				//	 System.out.println("d " + v);
				} else {
				//	 System.out.println("e " + v);
					if (argt.equals(outcome.third)) {
				//		 System.out.println("f " + v);
						if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
							ret.add(new Triple<>(ret1, ret2, ret3));
						}
					}
				}
			}
		}
		if (col.gens.containsKey(new Gen(e.head)) && e.args.isEmpty() && e.annotation == null) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Gen(new Gen(e.head));
			Chc<Ty, En> ret3 = Chc.inRight(col.gens.get(new Gen(e.head)));
			if (expected != null && !expected.equals(ret3)) {
			} else {
				ret.add(new Triple<>(ret1, new Ctx<>(), ret3));
			}
		}
		if (col.sks.containsKey(new Sk(e.head)) && e.args.isEmpty() && e.annotation == null) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Sk(new Sk(e.head));
			Chc<Ty, En> ret3 = Chc.inLeft(col.sks.get(new Sk(e.head)));
			if (expected != null && !expected.equals(ret3)) {
			} else {
				ret.add(new Triple<>(ret1, new Ctx<>(), ret3));
			}
		}
		if (e.args.isEmpty() && e.annotation != null) {
			Ty ty = new Ty(e.annotation);
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Obj(js.parse(ty, e.head), ty);
			Chc<Ty, En> ret3 = Chc.inLeft(ty);
			if (expected != null && !expected.equals(ret3)) {
			} else {
					ret.add(new Triple<>(ret1, new Ctx<>(), ret3));
				
			}
		}
		// as primitive - only if not a variable/generator/etc in scope i.e. none above fired
		if (e.args.isEmpty() && e.annotation == null && ret.isEmpty()) {
			for (Ty ty : col.tys) {
				if (e.annotation != null && !new Ty(e.annotation).equals(ty)) {
					continue;
				}
				try {
					Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Obj(js.parse(ty, e.head), ty);
					Chc<Ty, En> ret3 = Chc.inLeft(ty);
					if (expected != null && !expected.equals(ret3)) {
					} else {
						if (e.annotation != null || !isSymbol(col, e.head)) {
							ret.add(new Triple<>(ret1, new Ctx<>(), ret3));
						}
					}
				} catch (Exception ex) {
				}
			}
		}
		if (ret.isEmpty()) {
			String msg = "Cannot infer a well-sorted term for " + e + ".\n";
			if (!vars.keySet().contains(new Var((String) e.head)) && !isSymbolAll(col, e.head)
					&& e.annotation == null) {
				msg += "Undefined symbol: " + e.head + "\n";
			}
			if (expected != null) {
				String msg2 = expected.left ? "type" : "entity";
				msg += "Expected " + msg2 + ": " + expected.toStringMash();
			}

			throw new RuntimeException(pre + msg);
		}

		return ret;
	}

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> boolean isSymbol(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col,
			String s) {
		return /*
				 * col.syms.map.containsKey(s) || col.fks.map.containsKey(s) ||
				 * col.atts.map.containsKey(s) ||
				 */
		col.gens.map.containsKey(s) || col.sks.map.containsKey(s);
	}

	private static <Gen, Sk> boolean isSymbolAll(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col,
			String s) {
		return col.syms.map.containsKey(s) || col.fks.map.containsKey(s) || col.atts.map.containsKey(s)
				|| col.gens.map.containsKey(s) || col.sks.map.containsKey(s);
	}

	// TODO aql inefficient bitwise operations

	public static Quad<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Chc<Ty, En>> infer1x(
			Map<String, Chc<Ty, En>> ctx0, RawTerm e0, RawTerm f, Chc<Ty, En> expected,
			Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, String pre, AqlJs<Ty, Sym> js) {
		Set<Quad<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Chc<Ty, En>>> ret = new HashSet<>();

		Map<Var, Chc<Ty, En>> vars0 = new HashMap<>();
		for (String s : ctx0.keySet()) {
			vars0.put(new Var(s), ctx0.get(s));

		}
		Set<Var> vars = ctx0.keySet().stream().map(x -> new Var((String) x)).collect(Collectors.toSet());

		Ctx<Var, Chc<Ty, En>> initial = new Ctx<>();
		for (Var v : vars) {
			if (ctx0.get(v.var) != null) {
				initial.put(v, ctx0.get(v.var));
			}
		}
		Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> lhs = infer_good(e0,
				expected, col, pre, js, vars0);
		if (lhs.isEmpty()) {
			Util.anomaly();
		}
		Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> rhs;
		if (f == null) {
			rhs = lhs;
		} else {
			rhs = infer_good(f, expected, col, pre, js, vars0);
		}
		if (rhs.isEmpty()) {
			Util.anomaly();
		}

		for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> outcome : lhs) {
			if (!vars.containsAll(outcome.second.keySet())) {
				Util.anomaly();
			}
		}
		for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> outcome : rhs) {
			if (!vars.containsAll(outcome.second.keySet())) {
				Util.anomaly();
			}
		}

		for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> p : lhs) {
			for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> q : rhs) {
				if (!p.second.agreeOnOverlap(q.second)) {
					continue;
				}
				if (!p.second.agreeOnOverlap(initial)) {
					continue;
				}
				if (!q.second.agreeOnOverlap(initial)) {
					continue;
				}
				if (!p.third.equals(q.third)) {
					continue;
				}
				if (expected != null && !p.third.equals(expected)) {
					continue;
				}
				if (expected != null && !q.third.equals(expected)) {
					continue;
				}
				Ctx<Var, Chc<Ty, En>> u = new Ctx<>(p.second.map);
				u.putAll(q.second.map);
				u.putAll(initial.map);

				if (!u.keySet().equals(vars)) {
					continue;
				}
				ret.add(new Quad<>(u, p.first, q.first, p.third));
			}
		}

		if (ret.size() == 0) {
			String e = (f == null) ? e0.toString() : (e0 + " = " + f);
			String msg = "Cannot infer a well-sorted term for " + e + ".\n";
			if (expected != null) {
				msg += "Expected sort: " + expected.toStringMash() + " (isType=" + expected.left + ")";
			}
			throw new RuntimeException(pre + msg);
		}
		if (pre == null) {
			Util.anomaly();
		}
		if (ret.size() > 1) {
			String e = (f == null) ? (e = e0.toString()) : (e0 + " = " + f);

			String msg = "Cannot infer a unique well-sorted term for " + e + ".\nCandidates: "
					+ Util.sep(ret.stream()
							.map(x -> f == null ? x.second.toStringUnambig()
									: x.second.toStringUnambig() + " = " + x.third.toStringUnambig())
							.collect(Collectors.toList()), "\n");
			if (expected != null) {
				msg += "Expected sort: " + expected.toStringMash() + " (isType=" + expected.left + ")";
			}
			throw new RuntimeException(pre + msg);
		}

		return Util.get0(ret);

	}
	/*
	 * public static <Ty, En, Sym, Fk, Att, Gen, Sk>
	 * Triple<Term<Ty,En,Sym,Fk,Att,Gen,Sk>,Ctx<Var,Chc<Ty,En>>,Chc<Ty,En>>
	 * infer0x( Map<String, Chc<Ty, En>> ctx0, RawTerm e, Chc<Ty, En> expected,
	 * Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, String pre, AqlJs<Ty, Sym>
	 * js) {
	 * 
	 * Map<Var, Chc<Ty, En>> vars0 = new HashMap<>(); for (String s :
	 * ctx0.keySet()) { vars0.put(new Var(s), ctx0.get(s));
	 * 
	 * } Set<Var> vars = ctx0.keySet().stream().map(x -> new
	 * Var((String)x)).collect(Collectors.toSet());
	 * Set<Triple<Term<Ty,En,Sym,Fk,Att,Gen,Sk>,Ctx<Var,Chc<Ty,En>>,Chc<Ty,En>>>
	 * ret = infer_good(e, expected, col, pre, js, vars0); if (ret.isEmpty()) {
	 * Util.anomaly(); } Ctx<Var, Chc<Ty, En>> initial = new Ctx<>(); for (Var v
	 * : vars) { if (ctx0.get(v.var) != null) { initial.put(v, ctx0.get(v.var));
	 * } }
	 * 
	 * for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>,
	 * Chc<Ty, En>> outcome : ret) { if
	 * (!vars.containsAll(outcome.second.keySet())) { Util.anomaly(); } }
	 * //System.out.println("a " + ret.size() + ret); ret.removeIf(x ->
	 * !initial.agreeOnOverlap(x.second)); //updates in place
	 * System.out.println("b " + ret.size() + ret); ret.removeIf(x ->
	 * !Util.union(x.second.keySet(),initial.keySet()).equals(vars));
	 * System.out.println("c " + ret.size() + ret);
	 * 
	 * if (ret.size() == 0) { String msg =
	 * "Cannot infer a well-sorted term for " + e + ".\n"; if (expected != null)
	 * { msg += "Expected sort: " + expected.toStringMash() + " (isType=" +
	 * expected.left + ")"; } throw new RuntimeException(pre + msg); }
	 * 
	 * if (ret.size() > 1) { String msg =
	 * "Cannot infer a unique well-sorted term for " + e + ".\n"; if (expected
	 * != null) { msg += "Expected sort: " + expected.toStringMash() +
	 * " (isType=" + expected.left + ")"; } throw new RuntimeException(pre +
	 * msg); }
	 * 
	 * return Util.get0(ret); }
	 */
	/*
	 * public static <Ty, En, Sym, Fk, Att, Gen, Sk>
	 * Triple<Term<Ty,En,Sym,Fk,Att,Gen,Sk>,Ctx<Var,Chc<Ty,En>>,Chc<Ty,En>>
	 * infer0x(Map<String, Chc<Ty, En>> ctx0, RawTerm lhs, Chc<Ty, En> expected,
	 * Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, String pre, AqlJs<Ty, Sym> js
	 * ) { Quad<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>,
	 * Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Chc<Ty, En>> ret = infer1x(ctx0,
	 * lhs, null, expected, col, pre, js);
	 * 
	 * return new Triple<>(ret.second, ret.first, ret.fourth); }
	 */

	/*
	 * public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att,
	 * Gen, Sk> infer0( Map<String, Chc<Ty, En>> ctx0, RawTerm lhs, Chc<Ty, En>
	 * expected, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, String pre,
	 * AqlJs<Ty, Sym> js) {
	 * 
	 * Map<String, Chc<Ty, En>> ctx = new HashMap<>(ctx0); String fresh =
	 * "(expected sort of " + lhs + ")"; ctx.put(fresh, expected);
	 * 
	 * try {
	 * 
	 * Triple<Ctx<String, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>,
	 * Term<Ty, En, Sym, Fk, Att, Gen, Sk>> ret = infer1( ctx, new
	 * RawTerm(fresh, Collections.emptyList()), lhs, col, js);
	 * 
	 * Chc<Ty, En> actual = ctx.get(fresh); actual.assertNeitherNull();
	 * expected.assertNeitherNull();
	 * 
	 * if (!actual.equals(expected)) { throw new RuntimeException("in " + lhs +
	 * ", infered sort is " + actual.toStringMash() +
	 * " which is not the expected sort " + expected.toStringMash()); }
	 * 
	 * return ret.third;
	 * 
	 * } catch (RuntimeException ex) { ex.printStackTrace(); throw new
	 * RuntimeException(pre + ex.getMessage()); }
	 * 
	 * }
	 */

	/**
	 * 
	 * @return it is misleading to return a context, because strings for
	 *         primitives can come out
	 */
	// TODO: the ctx should assign a Ty or En to each fk/att/gen/sk, not just
	// each prim
	/*
	 * public static <Ty, En, Sym, Fk, Att, Gen, Sk> Triple<Ctx<String, Chc<Ty,
	 * En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att,
	 * Gen, Sk>> infer1( Map<String, Chc<Ty, En>> ctx, RawTerm lhs, RawTerm rhs,
	 * Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlJs<Ty, Sym> js) {
	 * 
	 * Map<String, Ref<Chc<Ty, En>>> ctx0 = new HashMap<>(); Set<String> vars =
	 * ctx.keySet(); for (String p : ctx.keySet()) { Chc<Ty, En> ty =
	 * ctx.get(p); if (ty != null) { ty.assertNeitherNull(); ctx0.put(p, new
	 * Ref<>(ty)); } else { ctx0.put(p, new Ref<>()); } } Ref<Chc<Ty, En>> lhs_t
	 * = lhs.infer(vars, ctx0, col); Ref<Chc<Ty, En>> rhs_t = rhs.infer(vars,
	 * ctx0, col); if (lhs_t.x == null && rhs_t.x == null) { throw new
	 * RuntimeException("In equation " + lhs + " = " + rhs +
	 * "the result type is ambiguous"); } else if (lhs_t.x == null && rhs_t.x !=
	 * null) { lhs_t.set(rhs_t); } else if (rhs_t.x == null && lhs_t.x != null)
	 * { rhs_t.set(lhs_t); } if (!lhs_t.x.equals(rhs_t.x)) { throw new
	 * RuntimeException("For LHS " + lhs + ", the infered sort of RHS " + rhs +
	 * " is " + lhs_t.x.toStringMash() + " but " + rhs + " actually has sort " +
	 * rhs_t.x.toStringMash()); } Ctx<String, Chc<Ty, En>> ret = new Ctx<>();
	 * for (String var : ctx0.keySet()) { Ref<Chc<Ty, En>> ref = ctx0.get(var);
	 * if (ref.x == null) { throw new RuntimeException("In equation " + lhs +
	 * " = " + rhs + ", the variable/primitive " + var +
	 * ", has ambiguous sort"); } ref.x.assertNeitherNull(); ret.put(var,
	 * ref.x); } return new Triple<>(ret, lhs.trans(vars, ctx0, col, js),
	 * rhs.trans(vars, ctx0, col, js)); }
	 */
	// TODO aql statically typesafe coerce
/*	private static <K, V> K find(Map<K, V> m, String k) {
		for (K k0 : m.keySet()) {
			if (k.equals(k0)) {
				return k0;
			}
		}
		throw new RuntimeException("Anomaly, please report");
	}*/

	
	 public static void assertUnambig(String head,
				Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
			int n = boolToInt(col.syms.containsKey(new Sym(head))) + boolToInt(col.atts.containsKey(new Att(head)))
					+ boolToInt(col.fks.containsKey(new Fk(head))) + boolToInt(col.gens.containsKey(new Gen(head)))
					+ boolToInt(col.sks.containsKey(new Sk(head)));
			if (n == 0) {
				throw new RuntimeException(head + " is not a symbol");
			} else if (n > 1) {
				throw new RuntimeException(head + " is ambiguous");
			}
		
	 }
	
	//@SuppressWarnings("unchecked")
	//only used for precedences with aql options
	 public static  Head<Ty, En, Sym, Fk, Att, Gen, Sk> toHeadNoPrim(String head,
			Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		
		 assertUnambig(head, col);
		 
		if (col.syms.containsKey(new Sym(head))) {
			return Head.Sym(new Sym(head));
		} else if (col.atts.containsKey(new Att(head))) {
			return Head.Att(new Att(head));
		} else if (col.fks.containsKey(new Fk(head))) {
			return Head.Fk(new Fk(head));
		} else if (col.gens.containsKey(new Gen(head))) {
			return Head.Gen(new Gen(head));
		} else if (col.sks.containsKey(new Sk(head))) {
			return Head.Sk(new Sk(head));
		}
		throw new RuntimeException("Anomaly: please report");
	} 

	/*
	 * private <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen,
	 * Sk> trans(Set<String> vars, Map<String, Ref<Chc<Ty, En>>> ctx0,
	 * Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlJs<Ty, Sym> js) {
	 * List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args0 = args.stream().map(x ->
	 * x.trans(vars, ctx0, col, js)) .collect(Collectors.toList());
	 * 
	 * int n = boolToInt(vars.contains(head)) +
	 * boolToInt(col.syms.containsKey(resolve(head))) +
	 * boolToInt(col.atts.containsKey(resolve(head))) +
	 * boolToInt(col.fks.containsKey(resolve(head))) +
	 * boolToInt(col.gens.containsKey(resolve(head))) +
	 * boolToInt(col.sks.containsKey(resolve(head))); if (n > 1) { throw new
	 * RuntimeException( head +
	 * " is ambiguously a variable/function/attribute/foreign key/generator/lablled null"
	 * ); }
	 * 
	 * if (annotation != null) {
	 * 
	 * @SuppressWarnings("unchecked") Ty ty = (Ty) annotation; Object o =
	 * js.parse(ty, head); // compile(code); return Term.Obj(o, ty); } else if
	 * (vars.contains(head)) { return Term.Var(new Var(head)); } else if
	 * (col.syms.containsKey(resolve(head))) { return
	 * Term.Sym(find(col.syms.map, resolve(head)), args0); } else if
	 * (col.atts.containsKey(resolve(head))) { return
	 * Term.Att(find(col.atts.map, resolve(head)), args0.get(0)); } else if
	 * (col.fks.containsKey(resolve(head))) { return Term.Fk(find(col.fks.map,
	 * resolve(head)), args0.get(0)); } else if
	 * (col.gens.containsKey(resolve(head))) { return
	 * Term.Gen(find(col.gens.map, resolve(head))); } else if
	 * (col.sks.containsKey(resolve(head))) { return Term.Sk(find(col.sks.map,
	 * head)); } else if (ctx0.containsKey(head) && !ctx0.get(head).x.left) { //
	 * this must be a generator - but why isn't it in gens? throw new
	 * RuntimeException("In term " + this +
	 * ", inference implies that that term must be a generator, but available generators at this location are: "
	 * + col.gens.keySet()); } else if (ctx0.containsKey(head) &&
	 * ctx0.get(head).x.left) { Ty ty = ctx0.get(head).x.l; if (ty == null) {
	 * throw new RuntimeException("Anomaly: please report"); } if
	 * (!col.java_tys.containsKey(ty) && col.java_parsers.containsKey(ty)) {
	 * throw new RuntimeException( "In term " + this + ", " + ty +
	 * " has a java parser but is not declared as a java type"); } if
	 * (col.java_tys.containsKey(ty) && !col.java_parsers.containsKey(ty)) {
	 * throw new RuntimeException( "In term " + this + ", " + ty +
	 * " is a java type but does not hava a java parser"); } if
	 * (!col.java_tys.containsKey(ty) && !col.java_parsers.containsKey(ty)) {
	 * throw new RuntimeException("In term " + this + ", symbol not defined"); }
	 * Object o = js.parse(ty, head); return Term.Obj(o, ty); } throw new
	 * RuntimeException("Anomaly, please report: " + this); }
	 */
	private static int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	/*
	 * @SuppressWarnings({ "ConstantConditions" }) private <Ty, En, Sym, Fk,
	 * Att, Gen, Sk> Ref<Chc<Ty, En>> infer(Set<String> vars, Map<String,
	 * Ref<Chc<Ty, En>>> ctx, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
	 * boolean isSym, isAtt, isFk, isGen, isSk, isVar, isObj;
	 * 
	 * isObj = (annotation != null);
	 * 
	 * Pair<List<Ty>, Ty> syms_t = col.syms.map.get(resolve(head)); // actually
	 * // want // possible // null // here isSym = syms_t != null && !isObj;
	 * Pair<En, Ty> atts_t = col.atts.map.get(resolve(head)); isAtt = atts_t !=
	 * null && !isObj; Pair<En, En> fks_t = col.fks.map.get(resolve(head)); isFk
	 * = fks_t != null && !isObj; En gens_t = col.gens.map.get(resolve(head));
	 * isGen = gens_t != null && !isObj; Ty sks_t =
	 * col.sks.map.get(resolve(head)); isSk = sks_t != null && !isObj;
	 * 
	 * isVar = vars.contains(new Var(resolve(head))) && !isObj;
	 * 
	 * int sum = Util.list(isSym, isAtt, isFk, isGen, isSk, isVar,
	 * isObj).stream().filter(x -> x.equals(Boolean.TRUE))
	 * .collect(Collectors.toList()).size(); if (sum > 2) { throw new
	 * RuntimeException( "In " + this + ", the head " + head + " is ambiguous: "
	 * + (isSym ? "is a typeside symbol " : " ") + (isVar ? "is a variable " :
	 * " ") + (isFk ? "is a foreign key " : " ") + (isAtt ? "is an attribute " :
	 * " ") + (isGen ? "is a generator " : " ") + (isSk ? "is a labelled null "
	 * : " ") + (isObj ? "is a primitive" : "")); }
	 * 
	 * if (isGen) { return new Ref<>(Chc.inRight(gens_t)); } else if (isSk) {
	 * return new Ref<>(Chc.inLeft(sks_t)); } else if (isObj) { for (Ty ty :
	 * col.tys) { if (ty.equals(annotation)) { // avoids cast return new
	 * Ref<>(Chc.inLeft(ty)); } } throw new RuntimeException("In " + this +
	 * ", the annotation " + annotation + " is not a type"); } else if (isAtt ||
	 * isFk) { if (args.size() != 1) { throw new RuntimeException("In " + this +
	 * ", the head " + head +
	 * " is an attribute/foreign key but it is given more than one or less than one argument, namely "
	 * + args.size()); } Ref<Chc<Ty, En>> arg_t = args.get(0).infer(vars, ctx,
	 * col); En ty = atts_t == null ? fks_t.first : atts_t.first; if (arg_t.x !=
	 * null && !Chc.inRight(ty).equals(arg_t.x)) { throw new RuntimeException(
	 * "In " + this + ", the head " + head +
	 * " is an attribute/foreign key expecting argument type " + ty +
	 * " but its argument has actual type " + arg_t.x.toStringMash()); }
	 * arg_t.set(Chc.inRight(ty)); // redundant sometimes
	 * 
	 * return isAtt ? new Ref<>(Chc.inLeft(atts_t.second)) : new
	 * Ref<>(Chc.inRight(fks_t.second)); } else if (isSym) { if (args.size() !=
	 * syms_t.first.size()) { throw new RuntimeException("In " + this +
	 * ", the head " + head + " is a typeside symbol of arity " +
	 * syms_t.first.size() + " but it is given " + args.size() + " arguments");
	 * } int i = 0; for (RawTerm arg : args) { Ref<Chc<Ty, En>> arg_t =
	 * arg.infer(vars, ctx, col); if (arg_t.x != null &&
	 * !Chc.inLeft(syms_t.first.get(i)).equals(arg_t.x)) { throw new
	 * RuntimeException("In " + this + ", the head " + head + " at position " +
	 * i + " is expecting argument type " + syms_t.first.get(i) +
	 * " but its argument has actual type " + arg_t.x.toStringMash()); }
	 * arg_t.set(Chc.inLeft(syms_t.first.get(i))); // redundant // sometimes
	 * i++; } return new Ref<>(Chc.inLeft(syms_t.second)); } else if
	 * (ctx.containsKey(head)) { return ctx.get(head); } else if (head != null
	 * && args.isEmpty()) { Ref<Chc<Ty, En>> ref = new Ref<>(); ctx.put(head,
	 * ref); return ref; } else if (head != null && !args.isEmpty()) { throw new
	 * RuntimeException("In " + this + ", the head " + head +
	 * " is not a function symbol"); } else { throw new
	 * RuntimeException("Anomaly: please report"); }
	 * 
	 * }
	 */
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RawTerm other = (RawTerm) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		return true;
	}

	private RawTerm(String head, List<RawTerm> args, String annotation) {
		if (head == null) {
			throw new RuntimeException("Attempt to create raw term with null head");
		} else if (args == null) {
			throw new RuntimeException("Attempt to create raw term with null args");
		} else if (annotation != null && !args.isEmpty()) {
			throw new RuntimeException("Attempt to annotate raw term with arguments");
		}
		this.head = head;
		this.args = args;
		this.annotation = annotation;
	}

	public RawTerm(String head, String annotation) {
		this(head, new LinkedList<>(), annotation);
	}

	public RawTerm(String head, List<RawTerm> args) {
		this(head, args, null);
	}

	public RawTerm(String head) {
		this(head, Collections.emptyList(), null);
	}

	// TODO: aql use of toString here is ugly
	public static  RawTerm fold(Set<Fk> fks, Set<En> entities, List<String> l, String v) {
		RawTerm ret = new RawTerm(v, (String) null);
		for (String o : l) {
			if (entities.contains(new En(o))) {
				if (fks.contains(new Fk(o))) {
					// throw new RuntimeException("In " + Util.sep(l, ".") + ",
					// " + o
					// + "is ambiguous: it can refer to an entity (0-length
					// path) or a foreign key (1-length path)");
				} else {
					continue;
				}
			}
			ret = new RawTerm((String) o, Util.singList(ret));
		}
		return ret;
	}

	public static RawTerm fold(List<String> l, String v) {
		RawTerm ret = new RawTerm(v, (String) null);
		for (Object o : l) {
			ret = new RawTerm(o.toString(), Util.singList(ret));
		}
		return ret;
	}

	public static Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> infer2(
			List<Pair<String, String>> l, RawTerm a, RawTerm b, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col,
			AqlJs<Ty, Sym> js) {
		Map<String, Chc<Ty, En>> ctx = new HashMap<>();
		for (Pair<String, String> p : l) {
			if (ctx.containsKey(p.first)) {
				throw new RuntimeException("Duplicate variable " + p.first + " in context " + Ctx.toString(l));
			}
			if (p.second != null) {
				if (col.tys.contains(p.second) && col.ens.contains(p.second)) {
					throw new RuntimeException("Ambiguous: " + p.second + " is an entity and a type");
				} else if (col.tys.contains(p.second)) {
					Ty tt = new Ty(p.second);
					ctx.put(p.first, Chc.inLeft(tt)); // TODO aql remove for
														// loops for other ones
				} else if (col.ens.contains(p.second)) {
					En tt = new En(p.second);
					ctx.put(p.first, Chc.inRight(tt));
				} else {
					throw new RuntimeException(p.second + " is neither a type nor entity");
				}
			} else {
				ctx.put(p.first, null);
			}
		}
		Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq0 = infer1x(
				ctx, a, b, null, col, "", js).first3();

		LinkedHashMap<Var, Chc<Ty, En>> map = new LinkedHashMap<>();
		for (String k : ctx.keySet()) {
			Chc<Ty, En> v = eq0.first.get(new Var(k));
			map.put(new Var(k), v);
		}

		Ctx<Var, Chc<Ty, En>> ctx2 = new Ctx<>(map);

		Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> tr = new Triple<>(
				ctx2, eq0.second, eq0.third);
		return tr;
	}

}
