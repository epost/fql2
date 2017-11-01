package catdata.aql;

import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;

public abstract class Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> implements Semantics {

	@Override
	public Kind kind() {
		return Kind.TRANSFORM;
	}
	
	@Override
	public int size() {
		return src().size();
	}
	
	public abstract Ctx<Gen1, Term<Void,En,Void,Fk,Void,Gen2,Void>> gens();
	public abstract Ctx<Sk1, Term<Ty,En,Sym,Fk,Att,Gen2,Sk2>> sks();
			
	public abstract Instance<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src();
	public abstract Instance<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst();
	
	//TODO aql transform initial

	protected void validate(boolean dontValidateEqs) {
		if (!src().schema().equals(dst().schema())) {
			throw new RuntimeException("Differing instance schemas\n\nsrc " + src().schema() + "\n\ndst " + dst().schema());
		}
		//for each (k,v) in gens/fks, k must be in src and dst must be in target 
			for (Gen1 gen1 : src().gens().keySet()) {
				En en1 = src().gens().get(gen1);
				if (!gens().containsKey(gen1)) {
					throw new RuntimeException("source generator " + gen1 + " has no transform");
				}
				Term<Void, En, Void, Fk, Void, Gen2, Void> gen2 = gens().map.get(gen1).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn());
				Chc<Ty, En> en2 = dst().type(gen2.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
				if (!en2.equals(Chc.inRight(en1))) {
					throw new RuntimeException("source generator " + gen1 + " transforms to " + gen2 + ", which has sort " + en2.toStringMash() + ", not " + en1 + " as expected");
				}	
			}
			for (Sk1 sk1 : src().sks().keySet()) {
				Ty ty1 = src().sks().get(sk1);
				Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> sk2 = sks().map.get(sk1);
				if (sk2 == null) {
					throw new RuntimeException("source labelled null " + sk1 + " has no transform");
				}
				Chc<Ty, En> ty2 = dst().type(sk2);
				if (!ty2.equals(Chc.inLeft(ty1))) {
					throw new RuntimeException("source labelled null " + sk1 + " transforms to " + sk2 + ", which has sort " + ty2.toStringMash() + ", not " + ty1 + " as expected");
				}	
			}
			for (Gen1 gen1 : gens().keySet()) {
				if (!src().gens().containsKey(gen1)) {
					throw new RuntimeException("there is a transform for " + gen1 + " which is not a source generator");
				}
			}
			for (Sk1 sk1 : sks().keySet()) {
				if (!src().sks().containsKey(sk1)) {
					throw new RuntimeException("there is a transform for " + sk1 + " which is not a source labelled null");
				}
			}
			
			if (!dontValidateEqs) { 
				for (Pair<Term<Ty, En, Sym, Fk, Att, Gen1, Sk1>, Term<Ty, En, Sym, Fk, Att, Gen1, Sk1>> eq : src().eqs()) {
					Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> lhs = trans(eq.first), rhs = trans(eq.second);
					dst().type(lhs);
					dst().type(rhs);
					boolean ok = dst().dp().eq(new Ctx<>(), lhs, rhs);
					if (!ok) {
						String xxx = ""; //", (and further, " + dst().collage().simplify().second.apply(lhs) + " = " + dst().collage().simplify().second.apply(rhs) + ")";
						throw new RuntimeException("Equation " + eq.first + " = " + eq.second + " translates to " + lhs + " = " + rhs + xxx + ", which is not provable in \n\n" + dst());
					}
				}
			}
				
	}

	@Override
	public final int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((dst() == null) ? 0 : dst().hashCode());
		result = prime * result + ((gens() == null) ? 0 : gens().hashCode());
		result = prime * result + ((sks() == null) ? 0 : sks().hashCode());
		result = prime * result + ((src() == null) ? 0 : src().hashCode());
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
		Transform<?,?,?,?,?,?,?,?,?,?,?,?,?> other = (Transform<?,?,?,?,?,?,?,?,?,?,?,?,?>) obj;
		if (dst() == null) {
			if (other.dst() != null)
				return false;
		} else if (!dst().equals(other.dst()))
			return false;
		if (gens() == null) {
			if (other.gens() != null)
				return false;
		} else if (!gens().equals(other.gens()))
			return false;
		if (sks() == null) {
			if (other.sks() != null)
				return false;
		} else if (!sks().equals(other.sks()))
			return false;
		if (src() == null) {
			if (other.src() != null)
				return false;
		} else if (!src().equals(other.src()))
			return false;
		return true;
	}


	//TODO aql alphabetical
	private String toString = null;
	@Override
	public final String toString() {
		if (toString != null) {
			return toString;
		}
	
		toString = toString("generators" , "");
		
		return toString;
	}
	
	public final String toString(String s, String t) {
		toString = s;
		toString += "\n\t" + Util.sep(gens().map, " -> ", "\n\t");
		toString += "\n" + t;
		toString += "\n\t" + Util.sep(sks().map, " -> ", "\n\t");
		return toString;
	}
	
	//private Morphism<Ty,En,Sym,Fk,Att,Gen1,Sk1,En,Sym,Fk,Att,Gen2,Sk2> semantics;
	/* public final Morphism<Ty,En,Sym,Fk,Att,Gen1,Sk1,En,Sym,Fk,Att,Gen2,Sk2> semantics() {
		if (semantics != null) {
			return semantics;
		}
		for (Pair<Term<Ty, En, Sym, Fk, Att, Gen1, Sk1>, Term<Ty, En, Sym, Fk, Att, Gen1, Sk1>> eq : src().eqs()) {
			Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> lhs = trans(eq.first), rhs = trans(eq.second);
			boolean ok = dst().dp().eq(new Ctx<>(), lhs, rhs);
			if (!ok) {
				throw new RuntimeException("Equation " + eq.first + " = " + eq.second + " translates to " + lhs + " = " + rhs + ", which is not provable");
			}
		}
		semantics = new Morphism<Ty,En,Sym,Fk,Att,Gen1,Sk1,En,Sym,Fk,Att,Gen2,Sk2>() {

			@Override
			public Pair<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen2, Sk2>> translate(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> term) {
				return new Pair<>(ctx, trans(term));
			}

			@Override
			public Collage<Ty, En, Sym, Fk, Att, Gen1, Sk1> src() {
				return Transform.this.src().collage();
			}

			@Override
			public Collage<Ty, En, Sym, Fk, Att, Gen2, Sk2> dst() {
				return Transform.this.dst().collage();
			}
			
		};
		return semantics;
	} */

	public final Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> trans(Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> term) {
		if (term.var != null) {
			return Term.Var(term.var); 
		} else if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.fk != null) {
			return Term.Fk(term.fk, trans(term.arg));
		} else if (term.att != null) {
			return Term.Att(term.att, trans(term.arg));
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::trans).collect(Collectors.toList()));
		} else if (term.gen != null) {
			return gens().get(term.gen).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn());
		} else if (term.sk != null) {
			return sks().get(term.sk);
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	public X2 repr(X1 x1){
		Term<Void, En, Void, Fk, Void, Gen1, Void> a = src().algebra().repr(x1);
		Term<Void, En, Void, Fk, Void, Gen2, Void> b = trans0(a); //
		return dst().algebra().nf(b);
	}
	
	private Term<Void, En, Void, Fk, Void, Gen2, Void> trans0(Term<Void, En, Void, Fk, Void, Gen1, Void> term) {
		if (term.fk != null) {
			return Term.Fk(term.fk, trans0(term.arg));
		} else if (term.gen != null) {
			return gens().get(term.gen).convert(); //TODO: aql gens should have diff type
		} 
		throw new RuntimeException("Anomaly: please report");
	}
	
	public Term<Ty,En,Sym,Fk,Att,Gen2,Sk2> reprT(Y1 y1){
		Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> a = src().algebra().reprT(Term.Sk(y1));
		return trans(a);
	}
	
	
}
