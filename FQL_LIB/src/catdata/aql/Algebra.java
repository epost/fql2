package catdata.aql;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;

//TODO: java stuff here
public abstract class Algebra<Ty,En,Sym,Fk,Att,Gen,Sk,X> {
	
	public boolean hasFreeTypeAlgebra() {
		return talg().simplify().first.eqs.isEmpty();
	}
	
	public abstract Schema<Ty,En,Sym,Fk,Att> schema();
	
	//TODO must break into 2 DPs, one for entity side, one for type algebra
	//if java is used forces free+java prover
	public abstract DP<Ty,En,Sym,Fk,Att,Gen,Sk> dp();

	public abstract Collection<X> en(En en);
	
	public abstract X fk(Fk fk, X x);
	
	public abstract X nf(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term); //TODO should specialize to Void here but painful
	
	public abstract Term<Ty, En, Sym, Fk, Att, Gen, Sk> repr(X x);
		
	protected abstract Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> talg_full();
	
	private List<Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>>>> list = new LinkedList<>();
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> simpl(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> term) {
		talg(); //apparently trans can be called before talg()
		for (Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> t : list) {
			term = term.replaceHead(new Head<>(Term.Sk(t.first)), Collections.emptyList(), t.second);
		}
		return term;
	}
	
	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> talg;
	public Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> talg() {
		if (talg != null) {
			return talg;
		}
		
		List<Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>>> eqs = new LinkedList<>(talg_full().eqs);
		List<Chc<Sk, Pair<X, Att>>> sks = new LinkedList<>(talg_full().sks.keySet());
		Iterator<Chc<Sk, Pair<X, Att>>> sks_it = sks.iterator();
		
		while (sks_it.hasNext()) {
			Chc<Sk, Pair<X, Att>> sk = sks_it.next();
			if (sk.left) {
				continue;
			}
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> replacer = null;
			for (Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> eq : eqs) {
				if (eq.second.equals(Term.Sk(sk)) && !eq.third.containsProper(new Head<>(Term.Sk(sk)))) {
					replacer = eq.third;
					break;
				} else if (eq.third.equals(Term.Sk(sk)) && !eq.second.containsProper(new Head<>(Term.Sk(sk))))  {
					replacer = eq.second;
					break;
				}
			}
			if (replacer == null) {
				continue;
			}
			final Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> replacer2 = replacer;
			sks_it.remove();
			eqs = eqs.stream().map(x -> new Triple<>(x.first, x.second.replaceHead(new Head<>(Term.Sk(sk)), Collections.emptyList(), replacer2), x.third.replaceHead(new Head<>(Term.Sk(sk)), Collections.emptyList(), replacer2))).collect(Collectors.toList());
			list.add(new Pair<>(sk, replacer));
		}
				
		Iterator<Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>>> it = eqs.iterator();
		while (it.hasNext()) {
			Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> eq = it.next();
			if (eq.second.equals(eq.third)) {
				it.remove();
			}
		}

		talg = new Collage<>(schema().typeSide);
		for (Chc<Sk, Pair<X, Att>> sk : sks) {
			talg.sks.put(sk, talg_full().sks.get(sk));
		}
		talg.eqs.addAll(eqs);
		//System.out.println(list);
		return talg;
	}
	
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> att(Att att, X x) {
		return trans(Term.Att(att, repr(x)));
	}

	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		System.out.println("Trans on " + term);
		System.out.println("list " + list);
		return simpl(trans0(term));
	}
	
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> trans0(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.var != null) {
			throw new RuntimeException("Anomaly: please report");
		} else if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> trans(x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			return Term.Sk(Chc.inLeft(term.sk));
		} else if (term.att != null) {
			return Term.Sk(Chc.inRight(new Pair<>(trans1(term.arg), term.att)));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private X trans1(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.gen != null) {
			return nf(term);
		} else if (term.fk != null) {
			return fk(term.fk, nf(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	public String toString(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> term) {
		if (term.sk != null) {
			if (term.sk.left) {
				return term.sk.l.toString();
			} else {
				return repr(term.sk.r.first) + "." + term.sk.r.second;
			}
		} else if (term.sym != null) {
			if (term.args.size() == 0) {
				return term.sym.toString();
			} else if (term.args.size() == 1) {
				return toString(term.args.get(0)) + "." + term.sym;
			} else if (term.args.size() == 2) {
				return toString(term.args.get(0)) + " " + term.sym + " " + toString(term.args.get(1));
			} else {
				return term.sym + "(" + Util.sep(term.args.stream().map(x -> toString(x)).collect(Collectors.toList()), ", ") + ")";
			}
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	public String talgToString() {
		String ret = "labelled nulls\n\t";
		List<String> l = talg().sks.entrySet().stream().map(x -> toString(Term.Sk(x.getKey())) + " : " + x.getValue()).collect(Collectors.toList());
		ret += Util.sep(l, "\n\t");
		ret += "\n\nequations\n\t";
		List<String> r = new LinkedList<>();
		for (Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> eq : talg().eqs) {
			if (schema().collage().eqs.contains(eq)) {
				continue;
			}
			if (!eq.first.isEmpty()) {
				throw new RuntimeException("Anomaly: please report");
			}
			r.add(toString(eq.second) + " = " + toString(eq.third));
		}
		ret += Util.sep(r, "\n\t");
		return ret;
	}
	
	
	@Override
	public String toString() {
		String ret = "carriers\n\t";
		ret += Util.sep(schema().ens.stream().map(x -> x + " -> {" + Util.sep(en(x).stream().map(this::repr).collect(Collectors.toList()), ", ") + "}").collect(Collectors.toList()), "\n\t");
		ret += "\n\nforeign keys";
		for (Fk fk : schema().fks.keySet()) {
			ret += "\n\t" + fk + " -> {" + Util.sep(en(schema().fks.get(fk).first).stream().map(x -> "(" + repr(x) + ", " + repr(fk(fk, x)) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		ret += "\n\nattributes";
		for (Att att : schema().atts.keySet()) {
			ret += "\n\t" + att + " -> {" + Util.sep(en(schema().atts.get(att).first).stream().map(x -> "(" + repr(x) + ", " + toString(att(att, x)) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		ret += "\n\ntype algebra\n\n";
		ret += talgToString();

		return ret;
	}
	
}
