package catdata.aql.fdm;

import java.util.Collection;
import java.util.Collections;
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
		extends Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>>
		implements DP<Ty, En2, Sym, Fk2, Att2, Gen, Sk> {

	

	private final Schema<Ty, En1, Sym, Fk1, Att1> A;
	private final Schema<Ty, En2, Sym, Fk2, Att2> B;
	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> X;
 
	private final Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col;
	private Chase<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2,Gen,Sk,X,Y> chase;
	
	public SigmaChaseAlgebra(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f2,
			Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i2, Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col) {
		A = f2.src;
		B = f2.dst;
		F = f2;
		X = i2;
		this.col = col;
		
		//TODO: move to correct spot
	//	if (!X.algebra().hasFreeTypeAlgebra()) {
	//		throw new RuntimeException("Chase cannot be used: type algebra is not free");
	//	}

	
		chase = new Chase<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2,Gen,Sk,X,Y>(F, X);
		
		for (En2 en2 : B.ens) {
			Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> s = chase.T.ens.get(en2).keySet();
			ens0.put(en2, s);
		}
	
	
	}

	
	private final Ctx<En2, Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>>> ens0 = new Ctx<>();


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
	public Collection<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>> en(En2 en) {
		return ens0.get(en);
	}

	@Override
	public Lineage<Void, En2, Void, Fk2, Void, Gen, Void> gen(Gen gen) {
		X x = X.algebra().gen(gen);
		En1 en1 = X.type(Term.Gen(gen)).r;
		return chase.T.us.get(en1).get(x);
	}

	
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> reprT0(Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>> y) {
		return schema().typeSide.js.java_tys.isEmpty() ? simpl(Term.Sk(y))
				: schema().typeSide.js.reduce(simpl(Term.Sk(y)));
	} 
	
	@Override
	public synchronized Lineage<Void, En2, Void, Fk2, Void, Gen, Void> fk(Fk2 fk2, Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x) {
		return Util.get0(chase.T.fks.get(fk2).get(x));
	}

	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> talg;
	private final List<Pair<Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>, 
	Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>>>> 
	list = new LinkedList<>();
	
	
	@Override
	public synchronized Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> talg() {
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
	public String printX(Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x) {
		// TODO Auto-generated method stub
		return x.toString();
	}

	@Override
	public Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> reprT_protected(
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> y) {
	      return schema().typeSide.js.java_tys.isEmpty() ? unflatten(simpl(y)) : unflatten(schema().typeSide.js.reduce(simpl(y)));
	  	
	}
	
	

	@Override
	public String printY(Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>> y) {
		// TODO Auto-generated method stub
		return y.toString();
	}
	
	//TODO: aql merge with initial algebra
	private Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> unflatten(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(this::unflatten).collect(Collectors.toList()));
		} else if (term.sk != null) {
            return term.sk.left ? Term.Sk(term.sk.l) : Term.Att(term.sk.r.second, repr(term.sk.r.first).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		} 
		throw new RuntimeException("Anomaly: please report");
	}
	

	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> simpl(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> y) {
		 //apparently trans can be called before talg()
		for (Pair<Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>>> t : list) {
			y = y.replaceHead(new Head<>(Term.Sk(t.first)), Collections.emptyList(), t.second);
		}
		return y;
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> att(
			Att2 att, Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x) {
		
		return reprT0(Chc.inRight(new Pair<>(x, att)));
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<Lineage<Void, En2, Void, Fk2, Void, Gen, Void>, Att2>>> sk(
			Sk sk) {
		return Term.Sk(Chc.inLeft(sk));
	}

	@Override
	public Term<Void, En2, Void, Fk2, Void, Gen, Void> repr(
			Lineage<Void, En2, Void, Fk2, Void, Gen, Void> x) {
		return x.t.convert(); //TODO aql convert lineage to use Voids
	}
	
}
