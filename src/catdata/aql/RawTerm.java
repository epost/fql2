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

	private static Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> infer_good(
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
			// //System.out.println("a " + e);

			List<List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>>> l = new LinkedList<>();
			l.add(new LinkedList<>());
			for (int i = 0; i < e.args.size(); i++) {
				RawTerm arg = e.args.get(i);
				// //System.out.println("arg " + arg);

				Ty ty = col.syms.get(new Sym(e.head)).first.get(i);
				Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> z = infer_good(arg,
						Chc.inLeft(ty), col, pre, js, vars);

				List<List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>>> l2 = new LinkedList<>();

				for (List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> old : l) {
					// //System.out.println("old " + old);
					for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> y : z) {
						// //System.out.println("y " + y);

						if (y.third.equals(Chc.inLeft(ty))) {
							// //System.out.println("z z");

							l2.add(Util.append(old, Util.singList(y)));
						}
					}
				}
				l = l2;
			}
			// //System.out.println("l " + l);

			outer: for (List<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> outcome : l) {
				// //System.out.println("outcome " + outcome);

				List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> w = outcome.stream().map(x -> x.first)
						.collect(Collectors.toList());
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Sym(new Sym(e.head), w);
				Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>();
				for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> ctx0 : outcome) {
					// //System.out.println("ctx0 " + ctx0);

					if (!ctx0.second.agreeOnOverlap(ret2) || !ctx0.second.agreeOnOverlap(Ctx.fromNullable(vars))) {
						// //System.out.println("xxx ");
						continue outer;
					}
					// //System.out.println("yyy");
					ret2.map.putAll(ctx0.second.map);
				}
				for (int i = 0; i < e.args.size(); i++) {
					RawTerm arg = e.args.get(i);
					// //System.out.println("2arx " + arg);
					Chc<Ty, En> ty = Chc.inLeft(col.syms.get(new Sym(e.head)).first.get(i));
					Var v = new Var((String) arg.head);
					if (vars.keySet().contains(v)) {
						// //System.out.println("a " + v);
						if (ret2.containsKey(v) && !ret2.get(v).equals(ty)) {
							// //System.out.println("b " + v);
							continue;
						} else if (!ret2.containsKey(v)) {
							// //System.out.println("c " + v);
							ret2.put(new Var(e.args.get(i).head), ty);
						}
					}
				}

				Chc<Ty, En> ret3 = Chc.inLeft(col.syms.get(new Sym(e.head)).second);
				if (expected != null && !expected.equals(ret3)) {
					// //System.out.println("d " );
				} else {
					// //System.out.println("e " );
					if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
						ret.add(new Triple<>(ret1, ret2, ret3));
					}
				}
			}
		}
		
		for (En en : col.ens) {
			if (col.fks.containsKey(new Fk(en, e.head)) && e.args.size() == 1 && e.annotation == null) {
				for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> outcome : infer_good(
						e.args.get(0), Chc.inRight(col.fks.get(new Fk(en, e.head)).first), col, pre, js, vars)) {
					Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Fk(new Fk(en, e.head), outcome.first);
					//System.out.println("trying " + en + " and " + e.head);
					Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>(outcome.second.map);
					Var v = new Var(e.args.get(0).head);
					Chc<Ty, En> ty = Chc.inRight(col.fks.get(new Fk(en, e.head)).first);
					if (vars.keySet().contains(v)) {
						if (ret2.containsKey(v) && !ret2.get(v).equals(ty)) {
							//System.out.println("no1");
							continue;
						} else if (!ret2.containsKey(v)) {
							ret2.put(new Var(e.args.get(0).head), ty);
						}
					}
					Chc<Ty, En> ret3 = Chc.inRight(col.fks.get(new Fk(en, e.head)).second);
					Chc<Ty, En> argt = Chc.inRight(col.fks.get(new Fk(en, e.head)).first);
	
					if (expected != null && !expected.equals(ret3)) {
					} else {
						if (argt.equals(outcome.third)) {
							if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
								ret.add(new Triple<>(ret1, ret2, ret3));
							} else {
								//System.out.println("b3");
							}
						} else {
							//System.out.println("c3");
						}
					}
				}
			}
			
			if (col.atts.containsKey(new Att(en, e.head)) && e.args.size() == 1 && e.annotation == null) {
						 //System.out.println("x " + e);
						for (Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>> outcome : infer_good(
								e.args.get(0), Chc.inRight(col.atts.get(new Att(en, e.head)).first), col, pre, js, vars)) {
							 //System.out.println("y " + outcome);

							Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Att(new Att(en, e.head), outcome.first);
							Ctx<Var, Chc<Ty, En>> ret2 = new Ctx<>(outcome.second.map);
							Var v = new Var(e.args.get(0).head);
							Chc<Ty, En> ty = Chc.inRight(col.atts.get(new Att(en, e.head)).first);
							if (vars.keySet().contains(v)) {
								 //System.out.println("z " + v);

								if (ret2.containsKey(v) && !ret2.get(v).equals(ty)) {
									 //System.out.println("a " + v);

									continue;
								} else if (!ret2.containsKey(v)) {
									 //System.out.println("b " + v);

									ret2.put(v, ty);
								}
							}

							Chc<Ty, En> ret3 = Chc.inLeft(col.atts.get(new Att(en, e.head)).second);
							Chc<Ty, En> argt = Chc.inRight(col.atts.get(new Att(en, e.head)).first);

							if (expected != null && !expected.equals(ret3)) {
								 //System.out.println("d " + v);
							} else {
								 //System.out.println("e " + v);
								if (argt.equals(outcome.third)) {
									 //System.out.println("f " + v);
									if (ret2.agreeOnOverlap(Ctx.fromNullable(vars))) {
										ret.add(new Triple<>(ret1, ret2, ret3));
									}
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
				if (expected != null && !expected.equals(Chc.inLeft(ty))) {
					continue;
				}
				try {
					Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret1 = Term.Obj(js.parse(ty, e.head), ty);
					Chc<Ty, En> ret3 = Chc.inLeft(ty);
					if (expected != null && !expected.equals(ret3)) {
						//System.out.println("zzz");
					} else {
						ret.add(new Triple<>(ret1, new Ctx<>(), ret3));
						//System.out.println("added " + ret + " and " + ret3);
					}
				} catch (Exception ex) {
					if (expected != null) {
						ex.printStackTrace();
						//throw ex;
					} 
//					//ex.printStackTrace();
				}
			}
		}
		

		return ret;
	}

	private static  boolean isSymbolAll(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col,
			String s) {
		return col.syms.containsKey(new Sym(s)) || 
				col.fks.keySet().stream().map(x -> x.str).collect(Collectors.toSet()).contains(s) ||
				col.atts.keySet().stream().map(x -> x.str).collect(Collectors.toSet()).contains(s) ||
				col.gens.map.containsKey(new Gen(s)) ||
				col.sks.map.containsKey(new Sk(s));
	}
 
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
			String msg = "Cannot infer a well-sorted term for " + e0 + ".\n";
			if (!vars.contains(new Var(e0.head)) && !isSymbolAll(col, e0.head)
					&& e0.annotation == null) {
				msg += "Undefined (or not java-parseable) symbol: " + e0.head + "\n";
				msg += "\nAvailable symbols:\n\t" + Util.sep(Util.alphabetical(col.allSymbolsAsStrings()), "\n\t");
				
			}
			if (expected != null) {
				String msg2 = expected.left ? "type" : "entity";
				msg += "Expected " + msg2 + ": " + expected.toStringMash();
			}

			throw new RuntimeException(pre + msg);
		}
		
		Set<Triple<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Ctx<Var, Chc<Ty, En>>, Chc<Ty, En>>> rhs;
		if (f == null) {
			rhs = lhs;
		} else {
			rhs = infer_good(f, expected, col, pre, js, vars0);
		}
		
		if (rhs.isEmpty()) {
			String msg = "Cannot infer a well-sorted term for " + f + ".\n"; //if f were null, above exn would have fired
			if (!vars.contains(new Var(f.head)) && !isSymbolAll(col, f.head)
					&& f.annotation == null) {
				msg += "Undefined (or not java-parseable) symbol: " + f.head + "\n";
				msg += "\nAvailable symbols:\n\t" + Util.sep(Util.alphabetical(col.allSymbolsAsStrings()), "\n\t");
				//TODO aql merge this error message with the one above it
			}
			if (expected != null) {
				String msg2 = expected.left ? "type" : "entity";
				msg += "Expected " + msg2 + ": " + expected.toStringMash();
			}
			throw new RuntimeException(pre + msg);
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

		List<String> misses = new LinkedList<>();
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
					misses.add(p.third + " and " + q.third);
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
			if (!misses.isEmpty()) {
				msg += "\nAttempted LHS and RHS types: " + Util.sep(misses, "\n");
			}
			
			throw new RuntimeException((pre + msg).trim());
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

	
	 public static void assertUnambig(String head,
				Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
			int n = boolToInt(col.syms.containsKey(new Sym(head))) 
					+ boolToInt(col.atts.keySet().stream().map(x -> x.str).collect(Collectors.toSet()).contains(head)) 
					+ boolToInt(col.fks.keySet().stream().map(x -> x.str).collect(Collectors.toSet()).contains(head)) 
					+ boolToInt(col.gens.containsKey(new Gen(head)))
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
		Util.assertNotNull(head);
		Util.assertNotNull(col);
		 assertUnambig(head, col);
		 
		if (col.syms.containsKey(new Sym(head))) {
			return Head.Sym(new Sym(head));
		} else if (col.gens.containsKey(new Gen(head))) {
			return Head.Gen(new Gen(head));
		} else if (col.sks.containsKey(new Sk(head))) {
			return Head.Sk(new Sk(head));
		}
		for (En en : col.ens) { //TODO aql won't work with ambig
			if (col.fks.containsKey(new Fk(en, head))) {
				return Head.Fk(new Fk(en, head));
			}
			if (col.atts.containsKey(new Att(en, head))) {
				return Head.Att(new Att(en, head));
			}
		}
		throw new RuntimeException("Anomaly: please report");
	} 


	private static int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	
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
		outer: for (String o : l) {
			if (entities.contains(new En(o))) {
				for (En en : entities) {
					if (fks.contains(new Fk(en, o))) {
						break;
					} else {
						continue outer;
					}
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
