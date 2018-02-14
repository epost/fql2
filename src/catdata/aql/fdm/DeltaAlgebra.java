package catdata.aql.fdm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.Collage;
import catdata.aql.Mapping;
import catdata.aql.Schema;
import catdata.aql.Term;

public class DeltaAlgebra<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
extends Algebra<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Y, Pair<En1, X>, Y> {
 
	
	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> alg;

	@Override
	public String toStringProver() {
		return alg.toStringProver();
	}
	
	public DeltaAlgebra(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> alg) {
		this.F = F;
		this.alg = alg;
	}

	@Override
	public Schema<Ty, En1, Sym, Fk1, Att1> schema() {
		return F.src;
	}

	public Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> translate(Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Y> e) {
		if (e.var != null) {
			return Term.Var(e.var);
		} else if (e.obj != null) {
			return Term.Obj(e.obj, e.ty);
		} else if (e.gen != null) {
			return alg.repr(e.gen.second).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn());
		} else if (e.fk != null) {
			return Schema.fold(F.fks.get(e.fk).second, translate(e.arg));
		} else if (e.att != null) {
			Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> t = F.atts.get(e.att).third.map(Function.identity(), Function.identity(), Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn());
			return t.subst(Util.singMapM(F.atts.get(e.att).first, translate(e.arg)));
		} else if (e.sym != null) {
			return Term.Sym(e.sym, e.args.stream().map(this::translate).collect(Collectors.toList()));
		} else if (e.sk != null) {
			return alg.reprT(Term.Sk(e.sk));
		}
		throw new RuntimeException("Anomaly: please report: " + e);	
	}
	/*
	 private Term<Void,En2,Void,Fk2,Void,Gen,Void> translateE(Term<Void, En1, Void, Fk1, Void, Pair<En1, X>, Void> e) {
		return translate(e.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())).convert();		
	} 
	 */
	@Override
	public Pair<En1, X> gen(Pair<En1, X> gen) {
		return gen;
		 /* Term<Void,En2,Void,Fk2,Void,Gen,Void> term2 = translateE(term);
		 X x = alg.nf(term2);
		 En1 en1 = type(term.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		 return new Pair<>(en1, x);
		 */
		  	//TODO aql 
		/*if (term.gen != null) {
			return term.gen;
		} else if (term.fk != null) {
			return fk(term.fk, nf(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
		*/
	}


	private final Map<En1, Collection<Pair<En1, X>>> en_cache = new HashMap<>();
	@Override
	public Collection<Pair<En1, X>> en(En1 en) {
		if (en_cache.containsKey(en)) {
			return en_cache.get(en);
		}
		Collection<X> in = alg.en(F.ens.get(en));
		Collection<Pair<En1, X>> ret = new ArrayList<>(in.size());
		for (X x : in) {
			ret.add(new Pair<>(en, x));
		}
		en_cache.put(en, ret);
		return ret;
	}

	@Override
	public Pair<En1, X> fk(Fk1 fk1, Pair<En1, X> e) {
		X x = e.second;
		for (Fk2 fk2 : F.trans(Util.singList(fk1))) {
			x = alg.fk(fk2, x);
		}
		En1 en1 = F.src.fks.get(fk1).second;
		return new Pair<>(en1, x);
	}

	@Override 
	public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
		return alg.talg();
	}
/*
	private En1 type(Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Sk> e) {
		if (e.gen != null) {
			return e.gen.first;
		} else if (e.fk != null) {
			return F.src.fks.get(e.fk).second; //no need to recurse, only use outer one
		}
		throw new RuntimeException("Anomaly: please report");
	}*/
	
	@Override
	public Term<Void, En1, Void, Fk1, Void, Pair<En1, X>, Void> repr(Pair<En1, X> x) {
		return Term.Gen(x);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att1 att, Pair<En1, X> e) {
		Term<Ty, Void, Sym, Void, Void, Void, Y> ret =  attY(F.atts.get(att).third, e);
		return alg.intoY(alg.reprT(ret));
	}

	private Term<Ty, Void, Sym, Void, Void, Void, Y> attY(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> term, Pair<En1, X> x) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.att != null) {
			return alg.att(term.att, attX(term.arg.asArgForAtt().map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn()), x.second)); 
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(q -> attY(q, x)).collect(Collectors.toList()));
		} 
		throw new RuntimeException("Anomaly: please report: " + term + " and " + x);
	}

	private X attX(Term<Void, En2, Void, Fk2, Void, Gen, Void> term, X x) {
		if (term.var != null) {
			return x;
		} else if (term.gen != null) {
			return alg.nf(term);
		} else if (term.fk != null) {
			return alg.fk(term.fk, attX(term.arg, x));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Y sk) {
		return Term.Sk(sk);
	}

	
	
	@Override
	public String printY(Y y) {
		return "<" + alg.printY(y) + ">";
	}
	
	@Override
	public String printX(Pair<En1,X> p) {
		return "<" + p.first + " " + alg.printX(p.second) + ">"; // "<" + alg.repr(p.second) + ">";
	} 

	@Override
	public Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Y> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
		Term<Ty, Void, Sym, Void, Void, Void, Y> t = alg.intoY(alg.reprT(term));
		return t.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
	}

	@Override
	public boolean hasFreeTypeAlgebra() {
		return alg.hasFreeTypeAlgebra();
	}

	@Override
	public boolean hasFreeTypeAlgebraOnJava() {
		return alg.hasFreeTypeAlgebraOnJava();
	}

	

}	