package catdata.aql;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.algs.kb.DPKB;
import catdata.algs.kb.FailProver;
import catdata.algs.kb.FreeProver;
import catdata.algs.kb.KBExp;

//TODO: saturated will succeed and return false sometimes when finite will fail to terminate

//TODO knuth bendix not sound with empty sorts because will drop variables from ctx,
//which is not a proof rule with empty sorts

//TODO can't use the simplified theory to do theorem proving, at least, not without remembering
//a way to simplify terms so that can check simplified |- simplify(a) = simplify(b)
//However, is ok to use simplified theory to build term model
//also, don't want to always simplify, for example, program mode shouldn't simplify
//but the others could -- should be an option

//Collage<Ty,En,Sym,Fk,Att,Gen,Sk> simplified = collage.simplify().first;
//collage.assertFreeOnJava(); 

//Collage<Ty,En,Sym,Fk,Att,Gen,Sk> reduced = collage; // .reduce();//must reduce for completeness re java (will do in provers as necessary)

//TODO SQL strategy

//TODO: handle timeout uniformly here 
//TODO: handle verify eqs (program,congruence) uniformly here

/*
 * simplify
 * reduce
 * 
 * create theory with only function symbols, get prover P for it
 * 
 * return (a,b) ->
 *  return p(simplify(a).reduce , simplify(b).reduce)
 * 
 * return a ->
 *  return nf(reduce(simplify(a))) //reduce again is cheating b/c eq doesn't get to reduce again [ie only reduce inputs to dp]
 * 
 * could herbrandize here
 * 
 */
public class AqlProver<Ty, En, Sym, Fk, Att, Gen, Sk> {

	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> create(AqlOptions ops, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col0) {
		ProverName name = (ProverName) ops.get(AqlOption.prover);

		if (name.equals(ProverName.precomputed)) {
			@SuppressWarnings("unchecked")
			DP<Ty, En, Sym, Fk, Att, Gen, Sk> ret = (DP<Ty, En, Sym, Fk, Att, Gen, Sk>) ops.get(AqlOption.precomputed);
			return ret;
		}

		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col1 = col0.reduce();
		
		Pair<List<Triple<Map<Var, Chc<Ty, En>>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>, KBExp<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var>>>, Map<Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Pair<List<Chc<Ty, En>>, Chc<Ty, En>>>> 
		col2 = col1.simplify().first.toKB();

		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col = null;
		Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp0 = null;
		DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb = null; 

		switch (name) {
		case precomputed: throw new RuntimeException();
		case fail: 
			dpkb = new FailProver<>();
			simp0 = x -> { throw new RuntimeException(); };
			col = null;
			break;
		case free: 
			dpkb = new FreeProver<>(col2.second, col2.first);  //use simplified
			simp0 = col1.simplify().second;
			col = col1.simplify().first;
			break;
		case saturated: 
			dpkb = SaturatedProverHelper.create(ops, col1.toKB().second, col1.toKB().first, col1);  
			simp0 = Function.identity();
			col = col0;
			break; //do not use simplified here
		case completion: throw new RuntimeException();
		case congruence: throw new RuntimeException();
		case program:  throw new RuntimeException();
		case monoidal:	 throw new RuntimeException();
		}
		
		return wrap(col, simp0, dpkb);
	
	}

	private static <Sk, En, Fk, Ty, Att, Sym, Gen> DP<Ty, En, Sym, Fk, Att, Gen, Sk> wrap(Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, Function<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> simp, DPKB<Chc<Ty, En>, Head<Ty, En, Sym, Fk, Att, Gen, Sk>, Var> dpkb) {
	
		return new DP<Ty, En, Sym, Fk, Att, Gen, Sk>() {
			@Override
			public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
				return dpkb.eq(ctx.map, AqlJs.reduce(simp.apply(lhs), col).toKB(), AqlJs.reduce(simp.apply(rhs), col).toKB());
			}

			@Override
			public boolean hasNFs() {
				return dpkb.hasNFs();
			}

			@Override
			public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
				return Term.fromKB(dpkb.nf(ctx.map, AqlJs.reduce(simp.apply(term), col).toKB()));
			}
			
			@Override
			public String toString() {
				return "Java-aware wrapping of " + dpkb;
			}
			
		};
	}

	

}
