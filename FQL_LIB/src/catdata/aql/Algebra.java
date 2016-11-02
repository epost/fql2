package catdata.aql;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Util;


public abstract class Algebra<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> /* implements DP<Ty,En,Sym,Fk,Att,Gen,Sk> */ {
	//TODO aql generic map method like printX
	
	//TODO aql add final eq method here
	
	public abstract Schema<Ty,En,Sym,Fk,Att> schema();
	
	public abstract Collection<X> en(En en);

	public abstract X fk(Fk fk, X x);
	
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att att, X x);
	
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Sk sk);

	public abstract X nf(Term<Void, En, Void, Fk, Void, Gen, Void> term); 

	public abstract Term<Void, En, Void, Fk, Void, Gen, Void> repr(X x);
	
	//public abstract DP<Ty,En,Sym,Fk,Att,Gen,Sk> dp();
	
	/**
	 * @return only equations for instance part (no typeside, no schema)
	 */
	public abstract Collage<Ty, Void, Sym, Void, Void, Void, Y> talg();

	/**
	 * @param y obtained from a call to att or sk only! TODO is this really needed anymore?
	 * @return not a true normal form, but a 'simplified' term for e.g., display purposes
	 */
	public abstract Term<Ty,En,Sym,Fk,Att,Gen,Sk> reprT(Term<Ty, Void, Sym, Void, Void, Void, Y> y);
	
	/**
	 * @param term of type sort
	 */
	public Term<Ty, Void, Sym, Void, Void, Void, Y> intoY(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
			if (term.obj != null) {
				return Term.Obj(term.obj, term.ty);
			} else if (term.sym != null) {
				return Term.Sym(term.sym, term.args().stream().map(x -> intoY(x)).collect(Collectors.toList()));
			} else if (term.sk != null) {
				return sk(term.sk);
			} else if (term.att != null) {
				return att(term.att, intoX(term.arg.convert()));
			}
			throw new RuntimeException("Anomaly: please report");
		}

	/**
	 * @param term term of type entity
	 */
		public X intoX(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
			if (term.gen != null) {
				return nf(term.convert());
			} else if (term.fk != null) {
				return fk(term.fk, nf(term.arg.convert()));
			}
			throw new RuntimeException("Anomaly: please report");
		}

	
	
	public abstract String toStringProver();
		/*
	public abstract String printSk(Sk y);
	public abstract String printGen(Gen x);
	*/
	public abstract String printX(X x);
	public abstract String printY(Y y);
	
	/*
	 * 	
	public String printSk(Sk y) { //TODO aql
		return y.toString();
	} 
	public String printGen(Gen x) {
		return x.toString();
	} 
	public String printX(X x) { 
		return x.toString();
	} 
	public String printY(Y y) {
		return y.toString();
	}
	 */
	public boolean hasFreeTypeAlgebra() {
		return talg().simplify().first.eqs.isEmpty();
	}
	

	//TODO aql visitor cleanup
	public Term<Ty, En, Sym, Fk, Att, X, Y> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.var != null) {
			return Term.Var(term.var);
		} else if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::trans).collect(Collectors.toList()));
		} else if (term.att != null) {
			return Term.Att(term.att, trans(term.arg));
		} else if (term.fk != null) {
			return Term.Fk(term.fk, trans(term.arg));
		} else if (term.gen != null) {
			return Term.Gen(nf(Term.Gen(term.gen)));
		} else if (term.sk != null) {
			return sk(term.sk).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	
	//TODO: have simplified collages also print out their definitions
	
	
	@Override
	public String toString() {
		String ret = "----- entity algebra\n\n";

		ret = "carriers\n\t";
		ret += Util.sep(schema().ens.stream().map(x -> x + " -> {" + Util.sep(en(x).stream().map(this::printX).collect(Collectors.toList()), ", ") + "}").collect(Collectors.toList()), "\n\t");
	
		ret += "\n\nforeign keys";
		for (Fk fk : schema().fks.keySet()) {
			ret += "\n\t" + fk + " -> {" + Util.sep(en(schema().fks.get(fk).first).stream().map(x -> "(" + x + ", " + printX(fk(fk, x)) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		
		ret += "\n\nattributes";
		for (Att att : schema().atts.keySet()) {
			ret += "\n\t" + att + " -> {" + Util.sep(en(schema().atts.get(att).first).stream().map(x -> "(" + x + ", " + att(att, x).toString(this::printY, Util.voidFn()) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		
		ret += "\n\n----- type algebra\n\n";
		ret += talg().toString();
		
		ret += "\n\n----- prover\n\n";
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
