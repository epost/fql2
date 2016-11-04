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

public final class TypeSide<Ty, Sym> {
	
	public final Set<Ty> tys;
	public final Ctx<Sym, Pair<List<Ty>, Ty>> syms;
	public final Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs;

	public final Ctx<Ty, String> java_tys;
	public final Ctx<Ty, String> java_parsers;
	public final Ctx<Sym, String> java_fns;

	public <En,Fk,Att,Gen,Sk> Ty type(Ctx<Var,Ty> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (!term.isTypeSide()) {
			throw new RuntimeException(term + " is not a typeside term");
		} 
		Chc<Ty, En> t = term.type(ctx, new Ctx<>(), tys, syms.map, java_tys.map, new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
		if (!t.left) {
			throw new RuntimeException(term + " has type " + t.l + " which is not in the typeside.  (This should be impossible, report to Ryan)");
		}
		return t.l;
	}

	
	private static <Ty, Sym> Collage<Ty, Void, Sym, Void, Void, Void, Void> col(Set<Ty> tys, Map<Sym, Pair<List<Ty>, Ty>> syms, Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs, Map<Ty, String> java_tys, Map<Ty, String> java_parsers, Map<Sym, String> java_fns) {
		Collage<Ty, Void, Sym, Void, Void, Void, Void> col = new Collage<>();
		col.tys.addAll(tys);
		col.syms.putAll(syms);
		col.java_tys.putAll(java_tys);
		col.java_parsers.putAll(java_parsers);
		col.java_fns.putAll(java_fns);
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq : eqs) {
			col.eqs.add(new Eq<>(eq.first.inLeft(), eq.second, eq.third));
		}
		return col;
	}
	
	public TypeSide(Set<Ty> tys, Map<Sym, Pair<List<Ty>, Ty>> syms, Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs, Map<Ty, String> java_tys_string, Map<Ty, String> java_parser_string, Map<Sym, String> java_fns_string, AqlOptions strategy) {
		this(tys, syms, eqs, java_tys_string, java_parser_string, java_fns_string, AqlProver.create(strategy, col(tys, syms, eqs, java_tys_string, java_parser_string, java_fns_string)));
	}

	
	public TypeSide(Set<Ty> tys, Map<Sym, Pair<List<Ty>, Ty>> syms, Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs, Map<Ty, String> java_tys_string, Map<Ty, String> java_parser_string, Map<Sym, String> java_fns_string, DP<Ty, Void, Sym, Void, Void, Void, Void> semantics) {
		Util.assertNotNull(tys, syms, eqs, java_tys_string, java_fns_string, semantics);
		this.tys = tys;
		this.syms = new Ctx<>(syms);
		this.eqs = eqs;
		this.java_tys = new Ctx<>(java_tys_string);
		this.java_parsers = new Ctx<>(java_parser_string);
		this.java_fns = new Ctx<>(java_fns_string);
		this.semantics = semantics;		
		validate();
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
		
		
//		if ((Boolean)strategy.getOrDefault(AqlOption.allow_java_eqs_unsafe)) {
			validateJava(); 
//		}
	}

	private void validateJava() {
		for (Ty ty : java_tys.keySet()) {
			String parser = java_parsers.get(ty);
			if (parser == null) {
				throw new RuntimeException("No constant parser for " + ty);
			}
			String clazz = java_tys.get(ty);
			AqlJs.load(clazz);
			AqlJs.compile(parser);
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

	//TODO aql move to collage?
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

	
	public <En,Fk,Att,Gen,Sk> void assertNoJava(Term<Ty, En, Sym, Fk, Att, Gen, Sk> t) {
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
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : t.args) {
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
		return new TypeSide<>(new HashSet<>(), new HashMap<>(), new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), DP.terminal);
	}

	public final DP<Ty, Void, Sym, Void, Void, Void, Void> semantics;

	/*
	public DP<Ty, Void, Sym, Void, Void, Void, Void> semantics() {
		if (semantics != null) {
			return semantics;
		}
		semantics = AqlProver.create(strategy, collage());
		return semantics;
	} */
	
	private Collage<Ty, Void, Sym, Void, Void, Void, Void> collage;
	@SuppressWarnings("unchecked")
	public <En,Fk,Att,Gen,Sk> Collage<Ty, En, Sym, Fk, Att, Gen, Sk> collage() {
		if (collage != null) {
			if (!collage.atts.isEmpty() || !collage.fks.isEmpty() || !collage.gens.isEmpty()|| !collage.sks.isEmpty()) {
				throw new RuntimeException("Anomaly: please report"); 
			}
			return (Collage<Ty, En, Sym, Fk, Att, Gen, Sk>) collage;
		}
		collage = col(tys, syms.map, eqs, java_tys.map, java_parsers.map, java_fns.map);
		return (Collage<Ty, En, Sym, Fk, Att, Gen, Sk>) collage;
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
		toString += "\n\t" + Util.sep(tys0, java_tys.map, " = " , "\n\t", true);
		
		toString += "\njava_constants";
		toString += "\n\t" + Util.sep(tys0, java_parsers.map, " = " , "\n\t", true);

		toString += "\njava_functions";
		toString += "\n\t" + Util.sep(syms0, java_fns.map, " = " , "\n\t", true);
		return toString;
	} 

}
