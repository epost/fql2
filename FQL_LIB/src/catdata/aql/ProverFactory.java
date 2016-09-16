package catdata.aql;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Chc3;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;

//TODO: saturated will succeed and return false sometimes when finite will fail to terminate

//TODO knuth bendix not sound with empty sorts because will drop variables from ctx,
//which is not a proof rule with empty sorts

//TODO can't use the simplified theory to do theorem proving, at least, not without remembering
//a way to simplify terms so that can check simplified |- simplify(a) = simplify(b)
//However, is ok to use simplified theory to build term model
//also, don't want to always simplify, for example, program mode shouldn't simplify
//but the others could -- should be an option

//Collage<Ty,En,Sym,Fk,Att,Gen,Sk> simplified = collage.simplify().first;
//collage.assertFreeOnJava(); 

//Collage<Ty,En,Sym,Fk,Att,Gen,Sk> reduced = collage; // .reduce();//must reduce for completeness re java (will do in provers as necessary)

//TODO SQL strategy

public class ProverFactory {

	public static <Ty,En,Sym,Fk,Att,Gen,Sk> DP<Ty,En,Sym,Fk,Att,Gen,Sk> 
	  create(AqlOptions ops, Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col) {
		
		//TODO: handle timeout uniformly here
		
		switch ((DPName) ops.get(AqlOption.prover)) {
	
		case ALLJAVA:
			return alljava(ops, col);
		case COMPLETION:
			return completion(ops, col);
		case CONGRUENCE:
			break;
	//		return congruence(ops, col);
		case FAIL:
			return fail(ops, col);
		case FINITE:
			break;
	//		return finite(ops, col);
		case PRECOMPUTED:
			return precomputed(ops, col);
		case PROGRAM:
			break;
	//		return program(ops, col);
		case SATURATED:
			return saturated(ops, col);
		case UNARY:
			break;
	//		return unary(ops, col);
		case FREE:
			break;
		}
	
		throw new RuntimeException("Anomaly: please report");		
		
	}

	
	private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> saturated(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		Map<Fk, Map<Gen, Gen>> fks = Util.newMapsFor(col.fks.keySet());
		Map<Att, Map<Gen,Chc3<Pair<Object,Ty>,Sym,Sk>>> atts = Util.newMapsFor(col.atts.keySet()); //will become Chc3<Sym,Sk,Obj>
		Map<Sym, Map<List<Chc<Sym,Sk>>, Chc3<Pair<Object,Ty>,Sym,Sk>>> syms = Util.newMapsFor(col.syms.keySet()); //only store syms that are non java
				
		for (Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : col.eqs) {
			if (!eq.first.isEmpty()) {
				throw new RuntimeException("Saturated method can not work with universal quantification"); //TODO definitely true
			}
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs = eq.second;
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs = eq.third;
			
			if (lhs.fk != null && lhs.arg.gen != null && rhs.gen != null) {
				Util.putSafely(fks.get(lhs.fk), lhs.arg.gen, rhs.gen);
			} else if (lhs.att != null && lhs.arg.gen != null && rhs.sym != null && rhs.args.isEmpty()) {
				Util.putSafely(atts.get(lhs.att), lhs.arg.gen, Chc3.b(rhs.sym));
			} else if (lhs.att != null && lhs.arg.gen != null && rhs.sk != null) {
				Util.putSafely(atts.get(lhs.att), lhs.arg.gen, Chc3.c(rhs.sk));
			} else if (lhs.att != null && lhs.arg.gen != null && rhs.obj != null) {
				Util.putSafely(atts.get(lhs.att), lhs.arg.gen, Chc3.a(new Pair<>(rhs.obj, rhs.ty)));
			} else if (lhs.sym != null && !col.java_fns.containsKey(lhs.sym)) {
				List<Chc<Sym,Sk>> args = new LinkedList<>();
				for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : lhs.args) {
					if (arg.sym != null && arg.args.isEmpty()) {
						args.add(Chc.inLeft(arg.sym));
					} else if (arg.sk != null) {
						args.add(Chc.inRight(arg.sk));
					} else {
						throw new RuntimeException("In " + eq.first.toString(lhs, rhs) + ", is not of the form f(g1(),...,gN()) = g() where f is an attribute, foreign key, or proper non-java function and g is a null or constant");						
					}
				}
				if (rhs.sym != null && rhs.args.isEmpty()) {
					Util.putSafely(syms.get(lhs.sym), args, Chc3.b(rhs.sym));
				} else if (rhs.sk != null) {
					Util.putSafely(syms.get(lhs.sym), args, Chc3.c(rhs.sk));					
				} else if (rhs.obj != null) {
					Util.putSafely(syms.get(lhs.sym), args, Chc3.a(new Pair<>(rhs.obj, rhs.ty)));
				} else {
					throw new RuntimeException("In " + eq.first.toString(lhs, rhs) + ", is not of the form f(g1(),...,gN()) = g() where f is an attribute, foreign key, or proper non-java function and g is a null, constant, or generator");						
				}
			} else {
				throw new RuntimeException("In " + eq.first.toString(lhs, rhs) + ", is not of the form f(g1(),...,gN()) = g() where f is an attribute, foreign key, or proper non-java function and g is a null or constant, or generator");						
			}
		}
			
		System.out.println(syms);
			
		//TODO slow
		if (!(Boolean)ops.getOrDefault(AqlOption.dont_verify_is_appropriate_for_prover_unsafe)) {
		 	for (Fk fk : col.fks.keySet()) {
				for (Gen gen : col.gens.keySet()) {
					if (!col.fks.get(fk).first.equals(col.gens.get(gen))) {
						continue;
					}
					Gen x = fks.get(fk).get(gen);
					if (x == null) {
						throw new RuntimeException("No equation for " + gen + "." + fk);
					}
				}
			}
			for (Att att : col.atts.keySet()) {
				for (Gen gen : col.gens.keySet()) {
					if (!col.atts.get(att).first.equals(col.gens.get(gen))) {
						continue;
					}
					Chc3<Pair<Object, Ty>, Sym, Sk> x = atts.get(att).get(gen);
					if (x == null) {
						throw new RuntimeException("No equation for " + gen + "." + att);
					}
				}
			}
			for (Sym sym : col.syms.keySet()) {
				if (col.syms.get(sym).first.isEmpty() || col.java_fns.containsKey(sym)) {
					continue;
				}
				List<List<Chc<Sym,Sk>>> allArgs = allArgs(sym, col);
				for (List<Chc<Sym,Sk>> arg : allArgs) {
					Chc3<Pair<Object, Ty>, Sym, Sk> x = syms.get(sym).get(arg);
					if (x == null) {
						List<String> s = arg.stream().map(Chc::toStringMash).collect(Collectors.toList());
						throw new RuntimeException("No equation for " + sym + "(" + Util.sep(s, ",") + ")");
					}
				}
			}			
		}
		
		return new DP<Ty, En, Sym, Fk, Att, Gen, Sk>() {

			@Override
			public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
				return nf(ctx, lhs).equals(nf(ctx, rhs)); 
			}

			@Override
			public boolean hasNFs() {
				return true;
			}

			@Override
			public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
				if (term.var != null) {
					return term;
				}
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> ret = null;
				if (term.obj != null || term.gen != null || term.sk != null || (!col.java_fns.containsKey(term.sym) && term.sym != null && term.args.isEmpty())) {
					ret = term;
				} else {
					Head<Ty, En, Sym, Fk, Att, Gen, Sk> head = new Head<>(term);
					List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args = new LinkedList<>(); 
					for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : term.args()) {
						args.add(nf(ctx, arg));
					}
					ret = Term.Head(head, args);
					if (ret.isGround()) {
						ret = nf2(ret);
					} 
				}				
				return AqlJs.reduce(ret, col);
			}
			
			private Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf2(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
				if (term.obj != null || term.var != null || term.gen != null || term.sk != null || (!col.java_fns.containsKey(term.sym) && term.sym != null && term.args.isEmpty())) {
					return term;
				} 
				//Head<Ty, En, Sym, Fk, Att, Gen, Sk> head = new Head<>(term);
				List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args = new LinkedList<>(); 
				for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : term.args()) {
					args.add(nf2(arg));
				}
				
				if (term.fk != null) {
					return Term.Gen(fks.get(term.fk).get(args.get(0).gen));
				} else if (term.att != null) {
					Chc3<Pair<Object,Ty>,Sym,Sk> x = atts.get(term.att).get(args.get(0).gen);
					if (x.b != null) {
						return Term.Sym(x.b, Collections.emptyList());											
					} else if (x.c != null) {
						return Term.Sk(x.c);
					} else {
						return Term.Obj(x.a.first, x.a.second);
					}
				} else if (term.sym != null) {
					List<Chc<Sym,Sk>> l = new LinkedList<>();
					for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : args) {
						if (arg.sym != null && arg.args.isEmpty()) {
							l.add(Chc.inLeft(arg.sym));
						} else if (arg.sk != null) {
							l.add(Chc.inRight(arg.sk));
						} else {
							throw new RuntimeException("Anomaly: please report");
						}
					}
					Chc3<Pair<Object,Ty>,Sym,Sk> x = syms.get(term.sym).get(l);
					if (x.b != null) {
						return Term.Sym(x.b, Collections.emptyList());											
					} else if (x.c != null){
						return Term.Sk(x.c);
					} else {
						return Term.Obj(x.a.first, x.a.second);
					}
				} else {
					throw new RuntimeException("Anomaly: please report");
				}
			}
			
			@Override
			public String toString() {
				return "Decision procedure by saturation";
			}
			
		};
			
		
		
		
	}

	//these Lists will never have dups
	private static <Ty, En, Sym, Fk, Att, Gen, Sk> List<List<Chc<Sym, Sk>>> allArgs(Sym sym, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		List<List<Chc<Sym, Sk>>> ret = new LinkedList<>();
		ret.add(new LinkedList<>());

		for (Ty ty : col.syms.get(sym).first) {
			List<Chc<Sym, Sk>> l = new LinkedList<>();
			for (Sym sym2 : col.syms.keySet()) {
				if (col.syms.get(sym2).first.isEmpty() && col.syms.get(sym2).second.equals(ty)) {
					l.add(Chc.inLeft(sym2));
				} 
			}
			for (Sk sk : col.sks.keySet()) {
				if (col.sks.get(sk).equals(ty)) {
					l.add(Chc.inRight(sk));
				} 
			}
			ret = extend(ret, l);
		}
		
		return ret;
	}
	
	private static <X> List<List<X>> extend(List<List<X>> ls, List<X> r) {
		List<List<X>> ret = new LinkedList<>();
		for (List<X> l : ls) {
			for (X x : r) {
				List<X> y = new LinkedList<>(l);
				y.add(x);
				ret.add(y);
			}
		}
		return ret;
	}

	/* private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> program(Object options, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduced) {
		// TODO Auto-generated method stub
		throw new RuntimeException("todo");
	} */

	@SuppressWarnings("unchecked")
	private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> precomputed(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduced) {
		return (DP<Ty, En, Sym, Fk, Att, Gen, Sk>) ops.get(AqlOption.precomputed);
	}

	/* private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> finite(Object options, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduced) {
		if (!reduced.java_tys.isEmpty()) {
			throw new RuntimeException("Cannot use finite proving method with java types");
		}
		// TODO Auto-generated method stub
		throw new RuntimeException("todo");
	} */

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> fail(Object options, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduced) {
		throw new RuntimeException("Proving strategy is to fail");
	}
	

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> completion(Object options0, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
	/*
		if (options0 == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		@SuppressWarnings("unchecked")
		Map<String, String> options = (Map<String, String>) options0;
		
		Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> fun = Function.identity();
		if (options.containsKey("simplify") && Boolean.parseBoolean(options.get("simplify"))) {
			col = col.simplify().first;
			fun = col.simplify().second;
		}
		
		for (Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : col.eqs) {
			Ctx<Var, Chc<Ty, En>> ctx = eq.first;
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs = fun.apply(eq.second);
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs = fun.apply(eq.third);
		}
		*/
		// TODO Auto-generated method stub
		throw new RuntimeException("todo");
	}

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> alljava(Object options, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduced) {
		if (!reduced.eqs.isEmpty()) {
			throw new RuntimeException("not an empty theory, as required by alljava proving strategy");
		}
		return new DP<Ty, En, Sym, Fk, Att, Gen, Sk>() {

			@Override
			public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
				return AqlJs.reduce(lhs, reduced).equals(AqlJs.reduce(rhs, reduced));
			}

			@Override
			public boolean hasNFs() {
				return true;
			}

			@Override
			public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
				return AqlJs.reduce(term, reduced);
			}
			
			@Override
			public String toString() {
				return "Decision procedure by java evaluation";
			}
			
		};
	}
	
}
