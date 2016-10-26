package catdata.aql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver.ProverName;

public final class TypeSide<Ty, Sym> {
	
	public final Set<Ty> tys;
	public final Map<Sym, Pair<List<Ty>, Ty>> syms;
	public final Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs;

	public final Map<Ty, String> java_tys;
	public final Map<Ty, String> java_parsers;
	public final Map<Sym, String> java_fns;

	private final AqlOptions strategy;
	
	public Ty type(Ctx<Var,Ty> ctx, Term<Ty, ?, Sym, ?, ?, ?, ?> term) {
		if (!term.isTypeSide()) {
			throw new RuntimeException(term + " is not a typeside term");
		} 
		Chc<Ty, ?> t = term.type(ctx, new Ctx<>(), tys, syms, java_tys, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		if (!t.left) {
			throw new RuntimeException(term + " has type " + t.l + " which is not in the typeside.  (This should be impossible, report to Ryan)");
		}
		return t.l;
	}

	public TypeSide(Set<Ty> tys, Map<Sym, Pair<List<Ty>, Ty>> syms, Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs, Map<Ty, String> java_tys_string, Map<Ty, String> java_parser_string, Map<Sym, String> java_fns_string, AqlOptions strategy) {
		if (tys == null) {
			throw new RuntimeException("Null types in typeside");
		} else if (syms == null) {
			throw new RuntimeException("Null symbols in typeside");
		} else if (eqs == null) {
			throw new RuntimeException("Null equations in typeside");
		} else if (java_tys_string == null) {
			throw new RuntimeException("Null java types in typeside");
		} else if (java_parser_string == null) {
			throw new RuntimeException("Null java constants in typeside");
		} else if (java_fns_string == null) {
			throw new RuntimeException("Null java functions in typeside");
		} else if (strategy == null) {
			throw new RuntimeException("Null theorem proving strategy in typeside");
		}
		this.tys = tys;
		this.syms = syms;
		this.eqs = eqs;
		this.java_tys = java_tys_string;
		this.java_parsers = java_parser_string;
		this.java_fns = java_fns_string;
		this.strategy = strategy;
		for (Ty ty : java_tys_string.keySet()) {
			String parser = java_parser_string.get(ty);
			if (parser == null) {
				throw new RuntimeException("No constant parser for " + ty);
			}
			String clazz = java_tys_string.get(ty);
			AqlJs.load(clazz);
			AqlJs.compile(parser);
		}
		validate();
		semantics();
	}

	public void validate() {
		//check that each sym is in tys
		for (Sym sym : syms.keySet()) {
			Pair<List<Ty>, Ty> ty = syms.get(sym);
			if (!tys.contains(ty.second)) {
				throw new RuntimeException("On typeside symbol " + sym + ", the return type " + ty.second + " is not declared.");
			}
			for (Ty t : ty.first) {
				if (!tys.contains(t)) {
					throw new RuntimeException("On typeside symbol " + sym + ", the argument type " + t + " is not declared.");
				}
			}
			
		}
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq : eqs) {
			//check that the context is valid for each eq
			Set<Ty> used_tys = new HashSet<>(eq.first.values());
			used_tys.removeAll(tys);
			if (!used_tys.isEmpty()) {
				throw new RuntimeException("In typeside equation " + toString(eq) + ", context uses types " + used_tys + " that are not declared.");
			}
			//check lhs and rhs types match in all eqs
			Ty lhs = type(eq.first, eq.second);
			Ty rhs = type(eq.first, eq.third);
			if (!lhs.equals(rhs)) {
				throw new RuntimeException("In typeside equation " + toString(eq) + ", lhs type is " + lhs + " but rhs type is " + rhs);
			}
			
		}
		
		validateJava(); 
		
	}

	private void validateJava() {
		if ((Boolean)strategy.getOrDefault(AqlOption.allow_java_eqs_unsafe)) {
			return;
		}
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq : eqs) {
			Ty lhs = type(eq.first, eq.second);
			
			if (java_tys.containsKey(lhs)) {
				throw new RuntimeException("In typeside equation " + toString(eq) + ", the return type is " + lhs + " which is a java type ");
			}
			if (!Collections.disjoint(java_tys.keySet(), eq.first.values())) {
				throw new RuntimeException("In typeside equation " + toString(eq) + ", the context variable(s) bind java type(s)");
			}
			assertNoJava(eq.second);
			assertNoJava(eq.third);
		}
		
		for (Sym sym : java_fns.keySet()) {
			if (!syms.containsKey(sym)) {
				throw new RuntimeException("The java function " + sym + " is not a declared function");
			}
		}
		for (Sym sym : syms.keySet()) {
			Pair<List<Ty>, Ty> t = syms.get(sym);
			if (allJava(t) || noJava(t)) {
				
			} else {
				throw new RuntimeException("In symbol " + sym + ", functions must not mix java and non-java types");
			}
		}
	}
	
	private boolean noJava(Pair<List<Ty>, Ty> t) {
		List<Ty> l = new LinkedList<>(t.first);
		l.add(t.second);
		
		for (Ty ty : l) {
			if (java_tys.containsKey(ty)) {
				return false;
			}
		}
		
		return true;
	}

	//TODO move to collage?
	private boolean allJava(Pair<List<Ty>, Ty> t) {
		List<Ty> l = new LinkedList<>(t.first);
		l.add(t.second);
		
		for (Ty ty : l) {
			if (!java_tys.containsKey(ty)) {
				return false;
			}
		}
		
		return true;
	}

	
	public void assertNoJava(Term<Ty, ?, Sym, ?, ?, Void, Void> t) {
		if (t.var != null) {
			return;
		} else if (t.fk != null) {
			assertNoJava(t.arg);
			return;
		} else if (t.att != null) {
			assertNoJava(t.arg);
			//does not test for attribute ending at java type, but that should ruled out previously
			return;
		} else if (t.sym != null) {
			Pair<List<Ty>, Ty> x = syms.get(t.sym);
			if (!Collections.disjoint(x.first, java_tys.keySet())) {
				throw new RuntimeException("In " + t + ", functions with java types are not allowed ");
			} else if (java_tys.keySet().contains(x.second)) {
				throw new RuntimeException("In " + t + ", functions with java types are not allowed");
			} 
			for (Term<Ty, ?, Sym, ?, ?, Void, Void> arg : t.args) {
				assertNoJava(arg);
			}
			return;
		} else if (t.obj != null) {
			throw new RuntimeException("In " + t + ", java constants are not allowed ");
		}
		
		throw new RuntimeException("Anomaly: please report."); //else if (t.g)
	} 
	
	public String toString(Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq) {
		String pre = eq.first.isEmpty() ? "" : "forall ";
		return pre + eq.first + ", " + eq.first + " = " + eq.second;
	}

	public static TypeSide<Void,Void> terminal() {
		return new TypeSide<>(new HashSet<>(), new HashMap<>(), new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new AqlOptions(ProverName.precomputed, DP.terminal));
	}

	private DP<Ty, Void, Sym, Void, Void, Void, Void> semantics;

	//this could take a while, so make sure two threads don't accidentally do it at the same time
	public synchronized DP<Ty, Void, Sym, Void, Void, Void, Void> semantics() {
		if (semantics != null) {
			return semantics;
		}
		semantics = AqlProver.create(strategy, collage());
		return semantics;
	}
	
	private Collage<Ty, Void, Sym, Void, Void, Void, Void> collage;
	public Collage<Ty, Void, Sym, Void, Void, Void, Void> collage() {
		if (collage != null) {
			return collage;
		}
		collage = new Collage<>(this);
		return collage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((java_fns == null) ? 0 : java_fns.hashCode());
		result = prime * result + ((java_parsers == null) ? 0 : java_parsers.hashCode());
		result = prime * result + ((java_tys == null) ? 0 : java_tys.hashCode());
		result = prime * result + ((syms == null) ? 0 : syms.hashCode());
		result = prime * result + ((tys == null) ? 0 : tys.hashCode());
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
		TypeSide<?,?> other = (TypeSide<?,?>) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (java_fns == null) {
			if (other.java_fns != null)
				return false;
		} else if (!java_fns.equals(other.java_fns))
			return false;
		if (java_parsers == null) {
			if (other.java_parsers != null)
				return false;
		} else if (!java_parsers.equals(other.java_parsers))
			return false;
		if (java_tys == null) {
			if (other.java_tys != null)
				return false;
		} else if (!java_tys.equals(other.java_tys))
			return false;
		if (syms == null) {
			if (other.syms != null)
				return false;
		} else if (!syms.equals(other.syms))
			return false;
		if (tys == null) {
			if (other.tys != null)
				return false;
		} else if (!tys.equals(other.tys))
			return false;
		return true;
	}

	private String toString;
	
	@Override
	public String toString() {
		if (toString != null) {
			return toString;
		}
		List<Ty> tys0 = Util.alphabetical(tys);
		List<Sym> syms0 = Util.alphabetical(syms.keySet());
		List<String> eqs1 = eqs.stream().map(x -> x.first.toString(x.second, x.third)).collect(Collectors.toList());
		List<String> eqs0 = Util.shortest(eqs1);
		
		toString = "types";
		toString += "\n\t" + Util.sep(tys0, " ");
		toString += "\nfunctions";
		List<String> temp = new LinkedList<>();
		for (Sym sym : syms0) {
			Pair<List<Ty>, Ty> t = syms.get(sym);
			temp.add(sym + " : " + Util.sep(t.first, ", ") + " -> " + t.second);
		}
		toString += "\n\t" + Util.sep(temp, "\n\t");
		
		toString += "\nequations";
		toString += "\n\t" + Util.sep(eqs0, "\n\t");
		
		toString += "\njava_types";
		toString += "\n\t" + Util.sep(tys0, java_tys, " = " , "\n\t", true);
		
		toString += "\njava_constants";
		toString += "\n\t" + Util.sep(tys0, java_parsers, " = " , "\n\t", true);

		toString += "\njava_functions";
		toString += "\n\t" + Util.sep(syms0, java_fns, " = " , "\n\t", true);
		return toString;
	} 

}
