package catdata.aql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlProver.ProverName;

//TODO: java stuff here
public abstract class Algebra<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> implements DP<Ty,En,Sym,Fk,Att,Gen,Sk> {
	
	public boolean hasFreeTypeAlgebra() {
		return talg().simplify().first.eqs.isEmpty();
	}
	
	public abstract Schema<Ty,En,Sym,Fk,Att> schema();
	
	public abstract Collection<X> en(En en);

	public abstract X fk(Fk fk, X x);
	
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att att, X x);
	
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Sk sk);

	public abstract X nf(Term<Void, En, Void, Fk, Void, Gen, Void> term); 

	public abstract Term<Void, En, Void, Fk, Void, Gen, Void> repr(X x);
	
	public abstract String toStringProver();
		
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> reprT(Y y); //TODO aql
	
	public String printSk(Y y) {
		return y.toString();
	}
	public String printGen(Gen x) {
		return x.toString();
	}
	
	/**
	 * @return only equations for instance part (no typeside, no schema)
	 */
	public abstract Collage<Ty, Void, Sym, Void, Void, Void, Y> talg();
	
	//TODO aql typesafe convert from Void for Term 
	private Instance<Ty,En,Sym,Fk,Att,X,Y> instance;
	public Instance<Ty,En,Sym,Fk,Att,X,Y> toInstance() {
		if (instance != null) {
			return instance;
		}
		Set<Pair<Term<Ty, En, Sym, Fk, Att, X, Y>, Term<Ty, En, Sym, Fk, Att, X, Y>>> eqs = new HashSet<>();
		Map<X, En> gens = new HashMap<>();
		for (En en : schema().ens) {
			for (X x : en(en)) {
				Util.putSafely(gens, x, en);
				for (Att att : schema().attsFrom(en)) {
					Term<Ty, En, Sym, Fk, Att, X, Y> lhs = Term.Att(att, repr(x).convert());
					Term<Ty, En, Sym, Fk, Att, X, Y> rhs = att(att, x).convert();
					eqs.add(new Pair<>(lhs,rhs));
				}
				for (Fk fk : schema().fksFrom(en)) {
					Term<Ty, En, Sym, Fk, Att, X, Y> lhs = Term.Fk(fk, repr(x).convert());
					Term<Ty, En, Sym, Fk, Att, X, Y> rhs = repr(fk(fk, x)).convert();
					eqs.add(new Pair<>(lhs,rhs));		
				}
			}
		}
		Map<Y, Ty> sks = new HashMap<>(talg().sks);
		for (Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Y>, Term<Ty, Void, Sym, Void, Void, Void, Y>> eq : talg().eqs) {
			if (!eq.first.isEmpty()) {
				throw new RuntimeException("Anomaly: please report");
			}
			eqs.add(new Pair<>(eq.second.convert(), eq.third.convert()));
		}
		
		AqlOptions strat = new AqlOptions(ProverName.precomputed, this);
		
		instance = new Instance<Ty,En,Sym,Fk,Att,X,Y>(schema(), gens, sks, eqs, strat);
		
		return instance;
	}
	
	//public abstract Collage<Void, En, Void, Fk, Void, X, Void> ealg();
	
	public static class Tables<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> {
		public Map<En, Collection<Term<Void, En, Void, Fk, Void, Gen, Void>>> carriers = new HashMap<>();
		public Map<Fk, Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Void, En, Void, Fk, Void, Gen, Void>>> fks = new HashMap<>();
		public Map<Att, Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Y>>> atts = new HashMap<>();
	}
	
	
	private Tables<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> tables;
	public Tables<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> getTables() {
		if (tables != null) {
			return tables;
		}
		tables = new Tables<>();
		
		for (En en : schema().ens) {
			tables.carriers.put(en, en(en).stream().map(this::repr).collect(Collectors.toList()));
		}

		for (Fk fk : schema().fks.keySet()) {
			Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Void, En, Void, Fk, Void, Gen, Void>> m = new HashMap<>();
			for (X x : en(schema().fks.get(fk).first)) {
				m.put(repr(x), repr(fk(fk, x)));
			}
			tables.fks.put(fk, m);
		}
		
		for (Att att : schema().atts.keySet()) {
			Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Ty, Void, Sym, Void, Void, Void, Y>> m = new HashMap<>();
			for (X x : en(schema().atts.get(att).first)) {
				m.put(repr(x), att(att, x));
			}
			tables.atts.put(att, m);
		}

		return tables;
	}
	

//	public abstract Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term);
	
//	public String toString(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
//		return term.toString();
//	}
		/*if (term.sk != null) {
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
		} else if (term.obj != null) {
			return term.obj.toString(); 
		}
		throw new RuntimeException("Anomaly: please report: " + term);
	}
	*/
	
	//TODO: have simplified collages also print out their definitions
	
	
	@Override
	public String toString() {
		getTables();

		String ret = "carriers\n\t";
		ret += Util.sep(schema().ens.stream().map(x -> x + " -> {" + Util.sep(tables.carriers.get(x), ", ") + "}").collect(Collectors.toList()), "\n\t");
	
		ret += "\n\nforeign keys";
		for (Fk fk : schema().fks.keySet()) {
			ret += "\n\t" + fk + " -> {" + Util.sep(tables.fks.get(fk).keySet().stream().map(x -> "(" + x + ", " + tables.fks.get(fk).get(x) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		
		ret += "\n\nattributes";
		for (Att att : schema().atts.keySet()) {
			ret += "\n\t" + att + " -> {" + Util.sep(tables.atts.get(att).keySet().stream().map(x -> "(" + x + ", " + tables.atts.get(att).get(x) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		
		ret += "\n\ntype algebra\n\n";
		ret += talg().toString();
		
		ret += "\n\nprover\n\n";
		ret += toStringProver();
		
		return ret;
	}
	
/*	
	public Term<Ty, Void, Sym, Void, Void, Void, Y> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> trans(x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			return sk(term.sk);
		} else if (term.att != null) {
			return att(term.att, trans1(term.arg.convert()));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private X trans1(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (term.gen != null) {
			return nf(term);
		} else if (term.fk != null) {
			return fk(term.fk, nf(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	*/
}
