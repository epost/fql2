package catdata.aql.fdm;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.Chase;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Head;
import catdata.aql.Instance;
import catdata.aql.Lineage;
import catdata.aql.Mapping;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;

public class SigmaChaseAlgebra<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2, Gen, Sk, X, Y>
		extends Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>>
		implements DP<Ty, En2, Sym, Fk2, Att2, Gen, Sk> {

	

	private final Schema<Ty, En1, Sym, Fk1, Att1> A;
	private final Schema<Ty, En2, Sym, Fk2, Att2> B;
	public final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> X;
	public int fresh;

	

	// private final It fr = new It();
	private int fresh() {
		return fresh++;
	}

	private final int max;

	private final Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col;
	private Chase<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2,Gen,Sk,X,Y> chase;
	
	//private Ctx<En1Ctx<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>,X> iso2 = new Ctx<>();

	public SigmaChaseAlgebra(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f2,
			Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i2, Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col, int max) {
		A = f2.src;
		B = f2.dst;
		F = f2;
		X = i2;
		this.fresh = 0;
		this.max = max;
		this.col = col;
		
		if (!X.algebra().hasFreeTypeAlgebra()) {
			throw new RuntimeException("Chase cannot be used: type algebra is not free");
		}

		Ctx<Chc<En1, En2>, Set<Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> 
		ens = new Ctx<>();
		Ctx<Chc<Chc<Fk1, Fk2>, En1>, Set<Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> 
		fks = new Ctx<>();

		Ctx<Chc<En1, En2>, Ctx<X, Lineage<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>>> iso1 = new Ctx<>();
		int num = 0;
		for (En1 en1 : A.ens) {
			Set<Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> s = new HashSet<>();
			iso1.put(Chc.inLeft(en1), new Ctx<>());
			for (X x : X.algebra().en(en1)) {
				int i = num++;
				Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk> xxx = X.algebra().repr(x).map(Util.voidFn(), Util.voidFn(), xx->xx, Util.voidFn(), xx->xx, Util.voidFn());
				Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> yyy = F.trans(xxx);
			/*	Term<Ty, En2, Sym, Chc<Chc<Fk1, Fk2>, En1>, Att2, Gen, Sk>
				zzz = yyy.mapFk(xx->Chc.inLeft(Chc.inRight(xx)));
				Term<Ty, En2, Sym, Chc<Chc<Fk1, Fk2>, En1>, Chc<Att1,Att2>, Gen, Sk>
				aaa = zzz.mapAtt(xx->Chc.inRight(xx));
				Term<Ty, Chc<En1,En2>, Sym, Chc<Chc<Fk1, Fk2>, En1>, Chc<Att1,Att2>, Gen, Sk>
				bbb = aaa.mapEn(); */
				Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>  
				L = new Lineage<>(i, yyy);
				iso1.get(Chc.inLeft(en1)).put(x, L);
				s.add(new Pair<>(L,L));
			}
			ens.put(Chc.inLeft(en1), s);
			fks.put(Chc.inRight(en1), new HashSet<>());
		}
		for (En2 en2 : B.ens) {
			ens.put(Chc.inRight(en2), new HashSet<>());
			iso1.put(Chc.inRight(en2), new Ctx<>());
		}
		for (Fk1 fk1 : A.fks.keySet()) {
			fks.put(Chc.inLeft(Chc.inLeft(fk1)), new HashSet<>());
		}
		for (Fk2 fk2 : B.fks.keySet()) {
			fks.put(Chc.inLeft(Chc.inRight(fk2)), new HashSet<>());
		}
		
		chase = new Chase<>(F, X, max, num);
		
		/* for (En2 en2 : B.ens) {
			ens0.put(en2, new HashSet<>());
			for (Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> x : chase.T.ens.get(Chc.inRight(en2))) {
				ens0.get(en2).add(x.first);
			}
			//for (String x : chase.T.)
			
		} */
	
	
	}

	
	private final Ctx<En2, Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>> ens0 = new Ctx<>();


	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En2>> ctx, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> lhs,
			Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> rhs) {
		if (!lhs.hasTypeType()) {
			return this.intoX(lhs).equals(intoX(rhs));
		} else {
			return Util.anomaly();
			// return this.intoY(lhs).equals(intoY(rhs));
		}
	}

	@Override
	public Schema<Ty, En2, Sym, Fk2, Att2> schema() {
		return B;
	}

	@Override
	public Set<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>> en(En2 en) {
		return ens0.get(en);
	}

	@Override
	public Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> gen(Gen gen) {
		X x = X.algebra().gen(gen);
		En1 en1 = X.type(Term.Gen(gen)).r;
		return null; //chase.T.iso.get(Chc.inLeft(en1)).get(x);
	}

	
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> reprT0(Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>> y) {
     return null;
		//   return schema().typeSide.js.java_tys.isEmpty() ? simpl(Term.Sk(y)) : schema().typeSide.js.reduce(simpl(Term.Sk(y)));
	} 
	
	@Override
	public synchronized Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> fk(Fk2 fk2, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		return null; //Util.get0(chase.T.fks.get(Chc.inLeft(Chc.inRight(fk2))).R.get(x));
	}

/*	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> att(Att2 att, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		return reprT0(Chc.inRight(new Pair<>(x, att)));
	//	return Util.anomaly();
		// return Util.toMapSafely(theContent.atts.get(att)).get(x);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>>>> sk(Sk sk) {
		return reprT0(Chc.inLeft(sk));
	}

	
	
	@Override
	public Term<Void, En2, Void, Fk2, Void, Gen, Void> repr(LLineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		return Util.anomaly(); //x.t.convert(); //TODO
	}

	
*/
	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> talg;
	private final List<Pair<Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>, 
	Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>>>> 
	list = new LinkedList<>();
	
	
	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> talg() {
		//this is not simplfy from collage - this is how we get 'reduction' to happen, by processing the talg.
			if (talg != null) {
				return talg;
			}
			talg = InitialAlgebra.talg(list, this, col);
			return talg;
	}

	@Override
	public String toStringProver() {
		return "";
	}

	@Override
	public String printX(Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		// TODO Auto-generated method stub
		return x.toString();
	}

	@Override
	public Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> reprT_protected(
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> y) {
	      return schema().typeSide.js.java_tys.isEmpty() ? unflatten(simpl(y)) : unflatten(schema().typeSide.js.reduce(simpl(y)));
	  	
	}
	
	

	@Override
	public String printY(Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>> y) {
		// TODO Auto-generated method stub
		return y.toString();
	}
	
	//TODO: aql merge with initial algebra
	private Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> unflatten(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(this::unflatten).collect(Collectors.toList()));
		} else if (term.sk != null) {
            return term.sk.left ? Term.Sk(term.sk.l) : Term.Att(term.sk.r.second, repr(term.sk.r.first).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		} 
		throw new RuntimeException("Anomaly: please report");
	}
	

	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> simpl(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> y) {
		 //apparently trans can be called before talg()
		for (Pair<Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>>> t : list) {
			y = y.replaceHead(new Head<>(Term.Sk(t.first)), Collections.emptyList(), t.second);
		}
		return y;
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> att(
			Att2 att, Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>>, Att2>>> sk(
			Sk sk) {
		return Term.Sk(Chc.inLeft(sk));
	}

	@Override
	public Term<Void, En2, Void, Fk2, Void, Gen, Void> repr(
			Lineage<Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk>> x) {
		return x.t.convert(); //TODO aql convert lineage to use Voids
	}
	
}
