package catdata.aql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
//TODO restore comment out sections
// cant allow equations involving typeside java symbols [allows inconsistency] also in schemas , but symbols ok
// when an instance creates a collage, check that type algebra is free
// prover factory can't check freeness of type algebra, will have to trust; prover assumes java defs are consistent with eqs
// syms can go back to Chc from Chc3, but att still needs Chc3
import catdata.algs.kb.KBExp;

//TODO: validate?
public class Collage<Ty, En, Sym, Fk, Att, Gen, Sk> {

	// TODO: keep equations segregated?

	// TODO: make this an interface implemented by TypeSide, Schema, Instance?

	// TODO: hang onto the typesides, schemas, instances where came from? factor
	// (split into 3 parts) method?

	public final Set<Ty> tys = new HashSet<>();
	public final Ctx<Sym, Pair<List<Ty>, Ty>> syms = new Ctx<>();
	public final Ctx<Ty, String> java_tys = new Ctx<>();
	public final Ctx<Ty, String> java_parsers = new Ctx<>();
	public final Ctx<Sym, String> java_fns = new Ctx<>();

	public final Set<En> ens = new HashSet<>();
	public final Ctx<Att, Pair<En, Ty>> atts = new Ctx<>();
	public final Ctx<Fk, Pair<En, En>> fks = new Ctx<>();

	public final Ctx<Gen, En> gens = new Ctx<>();
	public final Ctx<Sk, Ty> sks = new Ctx<>();

	public final Set<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>> eqs = new HashSet<>();

	public Collage() {
		// TODO add validator in constructor
	}

	public Collage(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		this.tys.addAll(col.tys);
		this.syms.putAll(col.syms.map);
		this.java_tys.putAll(col.java_tys.map);
		this.java_parsers.putAll(col.java_parsers.map);
		this.java_fns.putAll(col.java_fns.map);
		this.ens.addAll(col.ens);
		this.atts.putAll(col.atts.map);
		this.fks.putAll(col.fks.map);
		this.gens.putAll(col.gens.map);
		this.sks.putAll(col.sks.map);
		this.eqs.addAll(col.eqs);
		// assertFreeOnJava(); causes infinite loop
	}

	// TODO: free on java should be checked in typeside but this is defensive
	// programming
	/*
	public Collage(TypeSide<Ty, Sym> typeSide) {
		this.tys.addAll(typeSide.tys);
		this.syms.putAll(typeSide.syms.map);
		this.java_tys.putAll(typeSide.java_tys.map);
		this.java_parsers.putAll(typeSide.java_parsers.map);
		this.java_fns.putAll(typeSide.java_fns.map);
		this.eqs.addAll(typeSide.eqs.stream().map(x -> new Eq<>(x.first.inLeft(), upgradeTypeSide(x.second), upgradeTypeSide(x.third))).collect(Collectors.toSet()));
		assertFreeOnJava();
	} 

	public Collage(Schema<Ty, En, Sym, Fk, Att> schema) {
		this(schema.typeSide);
		this.ens.addAll(schema.ens);
		this.atts.putAll(schema.atts);
		this.fks.putAll(schema.fks);
		this.eqs.addAll(schema.eqs.stream().map(x -> new Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>(new Ctx<>(new Pair<>(x.first.first, Chc.inRight(x.first.second))), upgradeSchema(x.second), upgradeSchema(x.third))).collect(Collectors.toSet()));
		assertFreeOnJava(); // TODO should be checked in schema, and also too
							// strong - want free of each representable
							// instances type algebra
	}

	public <X,Y> Collage(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> instance) {
		this(instance.schema);
		this.gens.putAll(instance.gens);
		this.sks.putAll(instance.sks);
		this.eqs.addAll(instance.eqs.stream().map(x -> new Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>(new Ctx<>(), x.first, x.second)).collect(Collectors.toSet()));
	}

	@SuppressWarnings("unchecked")
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgradeTypeSide(Term<Ty, Void, Sym, Void, Void, Void, Void> term) {
		return (Term<Ty, En, Sym, Fk, Att, Gen, Sk>) term;
	}

	@SuppressWarnings("unchecked")
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgradeSchema(Term<Ty, En, Sym, Fk, Att, Void, Void> term) {
		return (Term<Ty, En, Sym, Fk, Att, Gen, Sk>) term;
	}
*/
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
		result = prime * result + ((java_fns == null) ? 0 : java_fns.hashCode());
		result = prime * result + ((java_parsers == null) ? 0 : java_parsers.hashCode());
		result = prime * result + ((java_tys == null) ? 0 : java_tys.hashCode());
		result = prime * result + ((sks == null) ? 0 : sks.hashCode());
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
		Collage<?, ?, ?, ?, ?, ?, ?> other = (Collage<?, ?, ?, ?, ?, ?, ?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (gens == null) {
			if (other.gens != null)
				return false;
		} else if (!gens.equals(other.gens))
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
		if (sks == null) {
			if (other.sks != null)
				return false;
		} else if (!sks.equals(other.sks))
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

	@Override
	public String toString() {
		String toString = "";
		toString += "\nfunctions";
		List<String> temp = new LinkedList<>();
		for (Sym sym : syms.keySet()) {
			Pair<List<Ty>, Ty> t = syms.get(sym);
			temp.add(sym + " : " + Util.sep(t.first, ", ") + " -> " + t.second);
		}
		toString += "\n\t" + Util.sep(temp, "\n\t");

		List<String> fks0 = new LinkedList<>();
		for (Fk fk : fks.keySet()) {
			fks0.add(fk + " : " + fks.get(fk).first + " -> " + fks.get(fk).second);
		}
		List<String> atts0 = new LinkedList<>();
		for (Att att : atts.keySet()) {
			atts0.add(att + " : " + atts.get(att).first + " -> " + atts.get(att).second);
		}

		toString += "\nforeign keys";
		toString += "\n\t" + Util.sep(fks0, "\n\t");

		toString += "\nattributes";
		toString += "\n\t" + Util.sep(atts0, "\n\t");

		toString += "\ngenerating entities";
		toString += "\n\t" + Util.sep(gens.map, " : ", "\n\t");

		toString += "\ngenerating nulls";
		toString += "\n\t" + Util.sep(sks.map, " : ", "\n\t");

		List<String> eqs0 = new LinkedList<>();
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			eqs0.add(eq.ctx.toString(eq.lhs, eq.rhs));
		}
		toString += "\nequations";
		toString += "\n\t" + Util.sep(eqs0, "\n\t");

		return toString;
	}

	
	
	
	private Pair<Collage<Ty, En, Sym, Fk, Att, Gen, Sk>, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> simplified_pair;

	public synchronized Pair<Collage<Ty, En, Sym, Fk, Att, Gen, Sk>, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> simplify() {
		if (simplified_pair != null) {
			return simplified_pair;
		}

		try {
			simplified_pair = simplify0();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Interrupted during simplification " + e.getMessage());
		}
		return simplified_pair;
	}
	
	

	private synchronized Pair<Collage<Ty, En, Sym, Fk, Att, Gen, Sk>, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> simplify0() throws InterruptedException {
	Collage<Ty, En, Sym, Fk, Att, Gen, Sk> simplified = new Collage<>(this);
		List<Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> list = new LinkedList<>();
		while (simplified.simplify1(list));

		Iterator<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>> it = simplified.eqs.iterator();
		while (it.hasNext()) {
			Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq = it.next();
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
			if (eq.lhs.equals(eq.rhs)) {
				it.remove();
			}
		}

		Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> translator = term -> {
			for (Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> t : list) {
				term = term.replaceHead(t.first, t.second, t.third);
			}
			return term;
		};

		simplified.simplified_pair = new Pair<>(simplified, x -> x);

		return new Pair<>(simplified, translator);
	}

	private boolean simplify1(List<Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> list) {
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			if (simplify2(eq.ctx, eq.lhs, eq.rhs, list)) {
				return true;
			} else if (simplify2(eq.ctx, eq.lhs, eq.rhs, list)) {
				return true;
			}
		}
		return false;
	}

/*	private boolean oriented(Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
		return !hasJava(lhs);
	}

	private boolean hasJava(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.var != null || term.gen != null || term.sk != null) {
			return false;
		}
		for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : term.args()) {
			if (hasJava(arg)) {
				return true;
			}
		}
		return term.obj != null || java_fns.containsKey(term.sym);
	} */

	private boolean simplify2(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs, List<Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> list) {
		if (lhs.var != null || lhs.obj != null) {
			return false;
		}
		Head<Ty, En, Sym, Fk, Att, Gen, Sk> head = new Head<>(lhs);
		List<Var> vars = getVarArgsUnique(lhs); 
		if (vars == null) {
			return false; //f(x,x) kind of thing
		}
		if (!new HashSet<>(vars).equals(ctx.keySet())) {
			return false; //forall x, y. f(y) = ... kind of thing
		}

		Set<Eq<Ty, En, Sym, Fk, Att, Gen, Sk>> neweqs = new HashSet<>();

		if (!rhs.contains(head)) {
			remove(head);
			for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
				list.add(new Triple<>(head, vars, rhs));
				neweqs.add(new Eq<>(eq.ctx, eq.lhs.replaceHead(head, vars, rhs), eq.rhs.replaceHead(head, vars, rhs)));
			}
			eqs.clear();
			eqs.addAll(neweqs);
			return true;
		}
		return false;
	}

	private void remove(Head<Ty, En, Sym, Fk, Att, Gen, Sk> head) {
		if (head.att != null) {
			atts.remove(head.att);
		} else if (head.fk != null) {
			fks.remove(head.fk);
		} else if (head.sym != null) {
			syms.remove(head.sym);
		} else if (head.gen != null) {
			gens.remove(head.gen);
		} else if (head.sk != null) {
			sks.remove(head.sk);
		} else {
			throw new RuntimeException("Anomaly: please report");
		}
	}

	private List<Var> getVarArgsUnique(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		List<Var> ret = new LinkedList<>();
		for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : term.args()) {
			if (arg.var == null) {
				return null;
			} else if (ret.contains(arg.var)) {
				return null;
			}
			ret.add(arg.var);
		}
		return ret;
	}

	public void assertFreeOnJava() {
		// each symbol must be of the form
		// f : s_1 , ... , s_n -> s
		// where isJava(s) implies all( isJava(s_i)) or all(!isJava(s_i))
		// !isJava(s) implies all(!isJava(s_i))
		for (Sym sym : syms.keySet()) {
			Pair<List<Ty>, Ty> t = syms.get(sym);
			if (java_tys.containsKey(t.second)) {
				int numJava = 0;
				for (Ty ty : t.first) {
					if (java_tys.containsKey(ty)) {
						numJava++;
					}
				}
				if (!(numJava == 0 || numJava == t.first.size())) {
					throw new RuntimeException("In symbol " + sym + ", target sort is java but source sorts mix java and non-java sorts");
				}
			} else {
				for (Ty ty : t.first) {
					if (java_tys.containsKey(ty)) {
						throw new RuntimeException("In symbol " + sym + ", target sort is java but source sort " + ty + "is not");
					}
				}
			}
		}

	}

	public Chc<Ty, En> type(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		Pair<LinkedHashMap<Var, Ty>, LinkedHashMap<Var, En>> m = Util.split(ctx.map);
		Ctx<Var, Ty> ctxt = new Ctx<>(m.first);
		Ctx<Var, En> ctxe = new Ctx<>(m.second);
		return term.type(ctxt, ctxe, tys, syms.map, java_tys.map, ens, atts.map, fks.map, gens.map, sks.map);
	}

	/*
	 * private List<Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att,
	 * Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>>
	 * removeNonJavaTypedEqs(Set<Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym,
	 * Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs) {
	 * List<Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>,
	 * Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> ret = new LinkedList<>(); for
	 * (Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>,
	 * Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : eqs) { Chc<Ty, En> type =
	 * type(eq.first, eq.second); if (type.left && java_tys.containsKey(type.l))
	 * { ret.add(eq); } } return ret; }
	 */

	// conservative
	// public boolean isFree() {
	// return simplify().first.eqs.isEmpty();
	// }

	// TODO consistent extension

	public Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduce() {
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> ret = new Collage<>(this);
		ret.eqs.clear();
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs = AqlJs.reduce(eq.lhs, this);
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs = AqlJs.reduce(eq.rhs, this);
			ret.eqs.add(new Eq<>(eq.ctx, lhs, rhs));
		}
		return ret;
	}

	private Triple<List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>>, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>>, Set<Chc<Ty, En>>> toKB = null;

	public Triple<List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>>, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>>, Set<Chc<Ty, En>>> toKB() {
		if (toKB != null) {
			return toKB;
		}
		List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>> theory = new LinkedList<>();
		Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>> signature = new HashMap<>();

		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			theory.add(new Triple<>(eq.ctx.map, eq.lhs.toKB(), eq.rhs.toKB()));
			Set<Pair<Object, Ty>> objs = new HashSet<>();
			eq.lhs.objs(objs);
			eq.rhs.objs(objs);
			for (Pair<Object, Ty> p : objs) {
				signature.put(Head.Obj(p.first, p.second), new Pair<>(Collections.emptyList(), Chc.inLeft(p.second)));
			}
		}

		for (Sym sym : syms.keySet()) {
			List<Chc<Ty, En>> l = Chc.inLeft(syms.get(sym).first);
			signature.put(Head.Sym(sym), new Pair<>(l, Chc.inLeft(syms.get(sym).second)));
		}
		for (Fk fk : fks.keySet()) {
			List<Chc<Ty, En>> l = Util.singList(Chc.inRight(fks.get(fk).first));
			signature.put(Head.Fk(fk), new Pair<>(l, Chc.inRight(fks.get(fk).second)));
		}
		for (Att att : atts.keySet()) {
			List<Chc<Ty, En>> l = Util.singList(Chc.inRight(atts.get(att).first));
			signature.put(Head.Att(att), new Pair<>(l, Chc.inLeft(atts.get(att).second)));
		}
		for (Gen gen : gens.keySet()) {
			List<Chc<Ty, En>> l = Collections.emptyList();
			signature.put(Head.Gen(gen), new Pair<>(l, Chc.inRight(gens.get(gen))));
		}
		for (Sk sk : sks.keySet()) {
			List<Chc<Ty, En>> l = Collections.emptyList();
			signature.put(Head.Sk(sk), new Pair<>(l, Chc.inLeft(sks.get(sk))));
		}
		Set<Chc<Ty, En>> sorts = new HashSet<>();
		sorts.addAll(Chc.inLeft(new LinkedList<>(tys)));
		sorts.addAll(Chc.inRight(new LinkedList<>(ens))); // TODO

		toKB = new Triple<>(theory, signature, sorts);
		return toKB;
	}

	public boolean isGround() {
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			if (!eq.ctx.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isMonoidal() {
		for (Sym sym : syms.keySet()) {
			if (syms.get(sym).first.size() > 1) {
				return false;
			}
		}
		return true;
	}

	public void assertNoEmptySorts(boolean emptySortsOk) {
		if (emptySortsOk) {
			return;
		}

		HashSet<Chc<Ty, En>> m = new HashSet<>(toKB().third);
		for (Head<Ty, En, Sym, Fk, Att, Gen, Sk> f : toKB().second.keySet()) {
			Pair<List<Chc<Ty, En>>, Chc<Ty, En>> a = toKB().second.get(f);
			if (a.first.isEmpty()) {
				m.remove(a.second);
			}
		}
		
		 //TODO: weaker condition: ok if for all sorts s and s', that s' is non-empty in context with s

		//TODO: are empty sorts ok for program?
		if (!m.isEmpty()) {
			List<String> l = m.stream().map(Chc::toStringMash).collect(Collectors.toList());
			throw new RuntimeException("Sorts " + Util.sep(l, ", ") + " have no 0-ary constants.  (Perhaps you meant to set allow_empty_sorts_unsafe=true?)");
		}
	}

	public Collage<Ty, En, Sym, Fk, Att, Gen, Sk> entities_only() {
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> ret = new Collage<>();
		ret.ens.addAll(ens);
		ret.fks.putAll(fks.map);
		ret.gens.putAll(gens.map);
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			if (!type(eq.ctx, eq.lhs).left) {
				ret.eqs.add(eq);
			}
		}
		return ret;
	}

}
