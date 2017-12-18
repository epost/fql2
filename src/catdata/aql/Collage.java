package catdata.aql;

import java.util.Collection;
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
import catdata.Ctx;
import catdata.Pair;
import catdata.RuntimeInterruptedException;
import catdata.Triple;
import catdata.Util;
import catdata.provers.KBExp;
import catdata.provers.KBTheory;

//TODO: aql validate collage
public class Collage<Ty, En, Sym, Fk, Att, Gen, Sk> {

	public <Ty, En, Sym, Fk, Att, Gen, Sk> Collage<Ty, En, Sym, Fk, Att, Gen, Sk> convert() {
		return (Collage<Ty, En, Sym, Fk, Att, Gen, Sk>) this;
	}
	// TODO: aql eep equations segregated?

	// TODO: aql make this an interface implemented by TypeSide, Schema, Instance?

	// TODO: aql hang onto the typesides, schemas, instances where came from? factor
	// (split into 3 parts) method?
	
	/**
	 * only works on closed terms
	 */
	public Set<Term<Ty, En, Sym, Fk, Att, Gen, Void>> applyAllSymbolsNotSk(Set<Term<Ty, En, Sym, Fk, Att, Gen, Void>> set) {
		Set<Term<Ty, En, Sym, Fk, Att, Gen, Void>> ret = new HashSet<>();
		
		for (Gen gen : gens.keySet()) {
			ret.add(Term.Gen(gen));
		}
//		for (Sk sk : sks.keySet()) {
	//		ret.add(Term.Sk(sk));
//		}
		for (Fk fk : fks.keySet()) {
			for (Term<Ty, En, Sym, Fk, Att, Gen, Void> arg : set) {
				Chc<Ty, En> x = type(new Ctx<>(), arg.mapGenSk(Function.identity(), Util.voidFn()));
				if (x.equals(Chc.inRight(fks.get(fk).first))) {
					ret.add(Term.Fk(fk, arg));
				}
			}
		}
		for (Att att : atts.keySet()) {
			for (Term<Ty, En, Sym, Fk, Att, Gen, Void> arg : set) {
				Chc<Ty, En> x = type(new Ctx<>(), arg.mapGenSk(Function.identity(), Util.voidFn()));
				if (x.equals(Chc.inRight(atts.get(att).first))) {
					ret.add(Term.Att(att, arg));
				}
			}
		}
		for (Sym sym : syms.keySet()) {
			for (List<Term<Ty, En, Sym, Fk, Att, Gen, Void>> args : helper(syms.get(sym), set)) {
				ret.add(Term.Sym(sym, args));
			}
		}
		
		return ret;
	}
	
	private List<Term<Ty, En, Sym, Fk, Att, Gen, Void>> getForTy(Chc<Ty,En> t, Collection<Term<Ty, En, Sym, Fk, Att, Gen, Void>> set) {
		return set.stream().filter(x -> type(new Ctx<>(), x.mapGenSk(Function.identity(), Util.voidFn())).equals(t)).collect(Collectors.toList());
	}

	private List<List<Term<Ty, En, Sym, Fk, Att, Gen, Void>>> helper(Pair<List<Ty>, Ty> ty, Collection<Term<Ty, En, Sym, Fk, Att, Gen, Void>> set) {
		List<List<Term<Ty, En, Sym, Fk, Att, Gen, Void>>> ret = new LinkedList<>();
		ret.add(new LinkedList<>());
		
		for (Ty t : ty.first) {
			List<List<Term<Ty, En, Sym, Fk, Att, Gen, Void>>> ret2 = new LinkedList<>();
			for (Term<Ty, En, Sym, Fk, Att, Gen, Void> l : getForTy(Chc.inLeft(t), set)) {
				for (List<Term<Ty, En, Sym, Fk, Att, Gen, Void>> x : ret) {
					List<Term<Ty, En, Sym, Fk, Att, Gen, Void>> z = new LinkedList<>(x);
					z.add(l);
					ret2.add(z);
				}
			}
			ret = ret2;
		}
		return ret;
	}

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

	public void addEqs(Collection<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> set) {
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq : set) {
			eqs.add(new Eq<>(eq.first.inLeft(), upgradeTypeSide(eq.second), upgradeTypeSide(eq.third)));
		}
	}
	public Set<Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqsAsTriples() {
		return eqs.stream().map(Eq::toTriple).collect(Collectors.toSet());
	}
	
	public Collage() {
		// TODO aql add validator in constructor
	}

	public Collage(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
        tys.addAll(col.tys);
        syms.putAll(col.syms.map);
        java_tys.putAll(col.java_tys.map);
        java_parsers.putAll(col.java_parsers.map);
        java_fns.putAll(col.java_fns.map);
        ens.addAll(col.ens);
        atts.putAll(col.atts.map);
        fks.putAll(col.fks.map);
        gens.putAll(col.gens.map);
        sks.putAll(col.sks.map);
        eqs.addAll(col.eqs);
		// assertFreeOnJava(); causes infinite loop
	}

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
		assertFreeOnJava(); 
	}

	public <X,Y> Collage(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> instance) {
		this(instance.schema);
		this.gens.putAll(instance.gens);
		this.sks.putAll(instance.sks);
		this.eqs.addAll(instance.eqs.stream().map(x -> new Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>(new Ctx<>(), x.first, x.second)).collect(Collectors.toSet()));
	}
*/
	@SuppressWarnings("unchecked")
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgradeTypeSide(Term<Ty, Void, Sym, Void, Void, Void, Void> term) {
		return (Term<Ty, En, Sym, Fk, Att, Gen, Sk>) term;
	}
/*
	@SuppressWarnings("unchecked")
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgradeSchema(Term<Ty, En, Sym, Fk, Att, Void, Void> term) {
		return (Term<Ty, En, Sym, Fk, Att, Gen, Sk>) term;
	}
	*/
	@Override
	public int hashCode() {
		int prime = 31;
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
		return toString(new Collage<>());
	}

	private <Ty1, En1, Sym1, Fk1, Att1, Gen1, Sk1> String toString(Collage<Ty1, En1, Sym1, Fk1, Att1, Gen1, Sk1> skip) {
		StringBuilder toString = new StringBuilder("");

		toString.append("types\n\t");
		toString.append(Util.sep(Util.diff(tys, skip.tys), "\n\t"));

		toString.append("\nentities\n\t");
		toString.append(Util.sep(Util.diff(ens, skip.ens), "\n\t"));
		
		toString.append("\nfunctions");
		List<String> temp = new LinkedList<>();
		for (Sym sym : Util.diff(syms.keySet(), skip.syms.keySet())) {
			Pair<List<Ty>, Ty> t = syms.get(sym);
			temp.add(sym + " : " + Util.sep(t.first, ", ") + " -> " + t.second);
		}
		toString.append("\n\t" + Util.sep(temp, "\n\t"));

		List<String> fks0 = new LinkedList<>();
		for (Fk fk : Util.diff(fks.keySet(), skip.fks.keySet())) {
			fks0.add(fk + " : " + fks.get(fk).first + " -> " + fks.get(fk).second);
		}
		List<String> atts0 = new LinkedList<>();
		for (Att att : Util.diff(atts.keySet(), skip.atts.keySet())) {
			atts0.add(att + " : " + atts.get(att).first + " -> " + atts.get(att).second);
		}

		toString.append("\nforeign keys");
		toString.append("\n\t");
		toString.append(Util.sep(fks0, "\n\t"));

		toString.append("\nattributes");
		toString.append("\n\t"); 		
		toString.append(Util.sep(atts0, "\n\t"));

		toString.append("\ngenerators for entities");
		toString.append("\n\t");	
		toString.append(Util.sep(Util.diff(gens.map, skip.gens.map), " : ", "\n\t"));

		toString.append("\ngenerators for nulls");
		toString.append("\n\t");
		toString.append(Util.sep(Util.diff(sks.map, skip.sks.map), " : ", "\n\t"));

		List<String> eqs0 = new LinkedList<>();
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : Util.diff(eqs, skip.eqs)) {
			eqs0.add(eq.ctx.toString(eq.lhs, eq.rhs));
		}
		toString.append("\nequations");
		toString.append("\n\t");
		toString.append(Util.sep(eqs0, "\n\t"));

/*		if (list != null) {
			toString += "\ndefinitions";
			toString += "\n\t" + Util.sep(list, ",");	
		} */
		
		return toString.toString();
	}
	
	public String tptp() {
		List<String> l = new LinkedList<>();
		int i = 0;
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			l.add("cnf(eq" + i + ",axiom,(" + eq.lhs.tptp() + " = " + eq.rhs.tptp() + ")).");
			i++;
		}
		return Util.sep(l, "\n\n");
	}
	
	private Pair<Collage<Ty, En, Sym, Fk, Att, Gen, Sk>, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> simplified_pair;

	public synchronized Pair<Collage<Ty, En, Sym, Fk, Att, Gen, Sk>, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> simplify() {
	//	return new Pair<>(this, x -> x);
	if (simplified_pair != null) {
			return simplified_pair;
		}

		try {
			simplified_pair = simplify0();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeInterruptedException(e);
		}
		return simplified_pair; 
		
	}


	private List<Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> list;
	
	private synchronized Pair<Collage<Ty, En, Sym, Fk, Att, Gen, Sk>, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> simplify0() throws InterruptedException {
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> simplified = new Collage<>(this);
		list = new LinkedList<>();
		while (simplify1(simplified, list));

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

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> boolean simplify1(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> simplified, List<Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> list) {
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : simplified.eqs) {
			if (simplify2(simplified, eq.ctx, eq.lhs, eq.rhs, list) || simplify2(simplified, eq.ctx, eq.rhs, eq.lhs, list)) {
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

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> boolean simplify2(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> simplified, Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs, List<Triple<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, List<Var>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> list) {
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
			simplified.remove(head);
			list.add(new Triple<>(head, vars, rhs));
			for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : simplified.eqs) {
				neweqs.add(new Eq<>(eq.ctx, eq.lhs.replaceHead(head, vars, rhs), eq.rhs.replaceHead(head, vars, rhs)));
			}
			simplified.eqs.clear();
			simplified.eqs.addAll(neweqs);
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

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> List<Var> getVarArgsUnique(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		List<Var> ret = new LinkedList<>();
		for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : term.args()) {
			if (arg.var == null || ret.contains(arg.var)) {
				return null;
			}
			ret.add(arg.var);
		}
		return ret;
	}

	//TODO aql move this into schema
	/*public void assertFreeOnJava() {
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

	}*/

	public Chc<Ty, En> type(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		Pair<LinkedHashMap<Var, Ty>, LinkedHashMap<Var, En>> m = Util.split(ctx.map);
		Ctx<Var, Ty> ctxt = new Ctx<>(m.first);
		Ctx<Var, En> ctxe = new Ctx<>(m.second);
		return term.type(ctxt, ctxe, tys, syms.map, java_tys.map, ens, atts.map, fks.map, gens.map, sks.map);
	}

	
	
	public Collage<Ty, En, Sym, Fk, Att, Gen, Sk> reduce(AqlJs<Ty,Sym> js) {
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> ret = new Collage<>(this);
		ret.eqs.clear();
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : eqs) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs = js.reduce(eq.lhs);
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs = js.reduce(eq.rhs);
			ret.eqs.add(new Eq<>(eq.ctx, lhs, rhs));
		}
		return ret;
	} 

	private KBTheory<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> toKB = null;

	public KBTheory<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> toKB() {
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
		sorts.addAll(Chc.inRight(new LinkedList<>(ens))); 

		toKB = new KBTheory<>(sorts, signature, theory);
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

		Collection<Chc<Ty, En>> m = new HashSet<>(toKB().tys);
		for (Head<Ty, En, Sym, Fk, Att, Gen, Sk> f : toKB().syms.keySet()) {
			Pair<List<Chc<Ty, En>>, Chc<Ty, En>> a = toKB().syms.get(f);
			if (a.first.isEmpty()) {
				m.remove(a.second);
			}
		}
		
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

	public void addAll(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> v) {
		tys.addAll(v.tys);
		ens.addAll(v.ens);
		syms.putAll(v.syms.map);
		atts.putAll(v.atts.map);
		fks.putAll(v.fks.map);
		gens.putAll(v.gens.map);
		sks.putAll(v.sks.map);	
		eqs.addAll(v.eqs);
		java_tys.putAll(v.java_tys.map);
		java_fns.putAll(v.java_fns.map);
		java_parsers.putAll(v.java_parsers.map);

	}

	public Collection<String> allSymbolsAsStrings() {
		 Collection<String> syms_ret = syms.keySet().stream().map(x->x.toString()).collect(Collectors.toList());
		 Collection<String> atts_ret = atts.keySet().stream().map(x->x.toString()).collect(Collectors.toList());
		 Collection<String> fks_ret = fks.keySet().stream().map(x->x.toString()).collect(Collectors.toList());
		 Collection<String> gens_ret = gens.keySet().stream().map(x->x.toString()).collect(Collectors.toList());
		 Collection<String> sks_ret = sks.keySet().stream().map(x->x.toString()).collect(Collectors.toList());
		 return Util.union(syms_ret, Util.union(atts_ret, Util.union(fks_ret, Util.union(gens_ret, sks_ret))));
	}

}
