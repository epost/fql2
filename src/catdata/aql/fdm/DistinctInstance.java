package catdata.aql.fdm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Instance;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.graph.UnionFind;

//TODO aql example of distinct
public class DistinctInstance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> extends Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> {

	private final Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I;
	
	private final Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs = new HashSet<>();
	
	private final UnionFind<X> uf;
	
	public DistinctInstance(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> i) {
		I = i;
		eqs.addAll(I.eqs());
		uf = new UnionFind<>(I.algebra().allXs());
		for (En en : schema().ens) {
			for (X x : I.algebra().en(en)) {
				for (X y : I.algebra().en(en)) {
					if (!x.equals(y) && obsEq(en,x,y)) {
						uf.union(x, y);	
						eqs.add(new Pair<>(I.algebra().repr(x).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()), I.algebra().repr(y).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())));
					}
				}
			}
		}
		validate();
	}
	
	private boolean obsEq(En en, X x, X y) {
		for (Fk fk : schema().fksFrom(en)) {
			if (!I.algebra().fk(fk, x).equals(I.algebra().fk(fk, y))) {
				return false;
			}
		}
		for (Att att : schema().attsFrom(en)) {
			if (!I.dp().eq(new Ctx<>(), I.algebra().reprT(I.algebra().att(att, x)), I.algebra().reprT(I.algebra().att(att, y)))) {
				return false;
			}
		}
		return true;
	}
	
	private X conv(X x) {
		return uf.find(x);
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return I.schema();
	}

	@Override
	public Ctx<Gen, En> gens() {
		return I.gens();
	}

	@Override
	public Ctx<Sk, Ty> sks() {
		return I.sks();
	}

	@Override
	public Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs() {
		return eqs;
	}

	@Override
	public DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp() {
		return I.dp();
	}

	@Override
	public Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> algebra() {
		return new InnerAlgebra();
	}

	private final class InnerAlgebra extends Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> {

		InnerAlgebra() {
		}

		@Override
		public Schema<Ty, En, Sym, Fk, Att> schema() {
			return I.schema();
		}

		@Override
		public Collection<X> en(En en) {
			return I.algebra().en(en).stream().map(DistinctInstance.this::conv).collect(Collectors.toSet()); //TODO aql cache
		}

		@Override
		public X fk(Fk fk, X x) {
			return conv(I.algebra().fk(fk, x));
		}

		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att att, X x) {
			return I.algebra().att(att, x);
		}

		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Sk sk) {
			return I.algebra().sk(sk);
		}

		@Override
		public X gen(Gen gen) {
			return conv(I.algebra().gen(gen));
		}

		@Override
		public Term<Void, En, Void, Fk, Void, Gen, Void> repr(X x) {
			return I.algebra().repr(conv(x));
		}

		@Override
		public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
			return I.algebra().talg();
		}

		@Override
		public Term<Ty, En, Sym, Fk, Att, Gen, Sk> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Y> y) {
			return I.algebra().reprT(y);
		}

		@Override
		public String toStringProver() {
			return "Distinct-instance wrapper of " + I.algebra().toStringProver();
		}

		@Override
		public String printX(X x) {
			return "|" + I.algebra().printX(x) + "|";
		}

		@Override
		public String printY(Y y) {
			return "|" + I.algebra().printY(y) + "|";
		}

		@Override
		public boolean hasFreeTypeAlgebra() {
			return I.algebra().hasFreeTypeAlgebra();
		}

		@Override
		public boolean hasFreeTypeAlgebraOnJava() {
			return I.algebra().hasFreeTypeAlgebraOnJava();
		}
		
	}

	@Override
	public boolean requireConsistency() {
		return I.requireConsistency();
	}

	@Override
	public boolean allowUnsafeJava() {
		return I.requireConsistency();
	}
}
