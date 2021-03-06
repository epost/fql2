package catdata.aql.fdm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import catdata.aql.Algebra;
import catdata.aql.AqlOptions;
import catdata.aql.AqlProver;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Eq;
import catdata.aql.Head;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;

//TODO: aql merge constants and functions in typesides
//TODO: aql add example that illustrates consistency
//TODO: aql add example that illustrates novalidate

//TODO aql check java computation etc through delta

//TODO: aql special saturator - rather than use java for typeside, use another prover such as grobner basis
//Ty = (Nat,0,1,+,*) S = (EqP, EqO) I + EqP decidable implies I decidable 
//works for any  commutative ring. problem: Eq0 not decidable by grobner
public class InitialAlgebra<Ty, En, Sym, Fk, Att, Gen, Sk, X> 
extends Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Chc<Sk, Pair<X, Att>>>
 implements DP<Ty, En, Sym, Fk, Att, Gen, Sk>  { //is DP for entire instance

	public boolean hasFreeTypeAlgebra() {
	//	return talg().eqs.isEmpty();
		Set<Eq<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> l = new HashSet<>();		
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq : schema().typeSide.eqs) {
			l.add(new Eq<>(eq.first.inLeft(), transX(this, eq.second.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Util.voidFn())), transX(this, eq.third.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Util.voidFn()))));
		}
		return Util.diff(talg().eqs, l).isEmpty(); 
	}
	
	public boolean hasFreeTypeAlgebraOnJava() {
		return talg().eqs.stream().filter(x -> talg().java_tys.containsKey(talg().type(x.ctx, x.lhs).l)).collect(Collectors.toList()).isEmpty();
	}
	
/*
	private <Y> Collage<Ty, Void, Sym, Void, Void, Void, Y> addTy(TypeSide<Ty, Sym> ty, Collage<Ty, Void, Sym, Void, Void, Void, Y> talg) {
		Collage<Ty, Void, Sym, Void, Void, Void, Y> ret = new Collage<>(talg);
		ret.tys.addAll(ty.tys);
		ret.syms.putAll(ty.syms.map);
		ret.addEqs(ty.eqs);
		return ret;
	} */
	
	
	@Override
	public String printX(X x) {
		return "[" + repr(x).toString(Util.voidFn(), printGen) + "]";
	}

	@Override
	public String printY(Chc<Sk, Pair<X, Att>> y) {
	       return y.left ? "[" + printSk.apply(y.l) + "]" : "[" + printX(y.r.first) + "." + y.r.second + "]";
	}
	
	private final DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp; //may just be on entity side, if java
	
	private final Map<En, Set<X>> ens;
	private final Map<X, Map<Fk, X>> fks = new HashMap<>();
	private final Map<X, Term<Void, En, Void, Fk, Void, Gen, Void>> reprs = new HashMap<>();	
	private final Map<Term<Void, En, Void, Fk, Void, Gen, Void>, X> nfs = new HashMap<>();
	
	private final Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col;
	private final Schema<Ty, En, Sym, Fk, Att> schema;
	private final Iterator<X> fresh;
	
	private final Function<Gen, String> printGen;
	private final Function<Sk, String> printSk;
	
	public InitialAlgebra(AqlOptions ops, Schema<Ty, En, Sym, Fk, Att> schema, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, Iterator<X> fresh, Function<Gen, String> printGen, Function<Sk, String> printSk) {
		this(AqlProver.create(ops, col, schema.typeSide.js), schema, col, fresh, printGen, printSk);
	}
	
	public InitialAlgebra(DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp, Schema<Ty, En, Sym, Fk, Att> schema, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, Iterator<X> fresh, Function<Gen, String> printGen, Function<Sk, String> printSk) {
		ens = Util.newSetsFor(schema.ens);
		this.col = col;
		this.schema = schema;
		this.fresh = fresh;
		this.printGen = printGen;
		this.printSk = printSk;

        this.dp = dp; 
        
		try {
			while (saturate1());
		} catch (InterruptedException exn) {
			throw new RuntimeInterruptedException(exn);
		}
		talg();
				
		//TODO aql figure out how to do this only once but without concurrent modification exception
			
		
		
		//new SaturatedInstance<>(this, this); // sanity check remove 
	}

	//TODO aql is it really safe to do depth first saturation?
	private boolean add(Term<Void, En, Void, Fk, Void, Gen, Void> term) throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		X x = nf0(term);
		if (x != null) {
			return false;
		}
			
		x = fresh.next();
		
		nfs.put(term, x);
		ens.get(col.type(new Ctx<>(), term.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())).r).add(x);
		reprs.put(x, term);
		
		Map<Fk, X> map = new HashMap<>();
		for (Fk fk : schema().fks.keySet()) {
			if (!col.type(new Ctx<>(), term.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())).r.equals(schema().fks.get(fk).first)) {
				continue;
			}
			add(Term.Fk(fk, term));
			map.put(fk, nf0(Term.Fk(fk, term)));
		}
		fks.put(x, map);
		
		return true; 
	}
	
	private boolean saturate1() throws InterruptedException {
		//schema.typeSide.collage(); sanity check

		boolean changed = false;
		for (Gen gen : col.gens.keySet()) {
			if (col.type(new Ctx<>(), Term.Gen(gen)).left) {
				continue; 
			}
			changed |= add(Term.Gen(gen));
		}
		for (Fk fk : col.fks.keySet()) {
			List<X> set = new ArrayList<>(ens.get(schema().fks.get(fk).first));
			for (X x : set) { //concurrent modification otherwise
				changed |= add(Term.Fk(fk, repr(x)));
			}
		}
		return changed;
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		//schema.typeSide.collage(); //sanity check
		return schema;
	}

	@Override
	public Collection<X> en(En en) {
		return ens.get(en);
	}

	@Override
	public X fk(Fk fk, X x) {
		Util.assertNotNull(x);
		X r = fks.get(x).get(fk);
		if (r == null) {
			throw new RuntimeException("Anomaly, please report: " + fk + "(" + x + ") is not in " + fks.get(x));
		}
		return r;
	}

	@Override 
	public Term<Void, En, Void, Fk, Void, Gen, Void> repr(X x) {
		if (x == null) {
			throw new RuntimeException("Anomaly: null given to repr, please report");			
		}
		Term<Void, En, Void, Fk, Void, Gen, Void> ret = reprs.get(x);
		if (ret == null) {
			throw new RuntimeException("Anomaly: please report: " + x + " (" + x.getClass() + ")" + " not in " + reprs);
		}
		return ret;
	}
	
	private X nf0(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (nfs.containsKey(term)) {
			return nfs.get(term);
		}
		En en = col.type(new Ctx<>(), term.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())).r;
		for (X x : ens.get(en)) {
			if (dp.eq(new Ctx<>(), term.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()), repr(x).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()))) {
				nfs.put(term, x);
				return x;
			}
		}
		return null;
	}
/*
	@Override
	public X nf(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		X x = nf0(term);
		if (x == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return x;
	}
*/
	@Override
	public X gen(Gen gen) {
		X x = nf0(Term.Gen(gen));
		if (x == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return x;
	}
	
	
	private static <Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> 
	talg_full(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg = new Collage<>();
		for (Sk sk : col.sks.keySet()) {
			talg.sks.put(Chc.inLeft(sk), col.sks.get(sk));
		}
		for (En en : col.ens) {
			for (X x : alg.en(en)) {
				for (Att att : col.atts.keySet()) {
					Pair<En, Ty> ty = col.atts.get(att);
					if (!ty.first.equals(en)) {
						continue;
					}
					talg.sks.put(Chc.inRight(new Pair<>(x, att)), ty.second);
				}
			}
		}
		
		
		for (Eq<Ty, En, Sym, Fk, Att, Gen, Sk> eq : col.eqs) {
			if (!col.type(eq.ctx, eq.lhs).left) {
				continue; //entity
			}
			if (!eq.ctx.isEmpty()) {
				continue; //in type side or schema
			}
                        //TODO aql need convert
			if (alg.schema().typeSide.eqs.contains(new Triple<>(new Ctx<>(), eq.lhs, eq.rhs))) {
				continue; //in type side
			}
			talg.eqs.add(new Eq<>(new Ctx<>(), transX(alg, eq.lhs), transX(alg, eq.rhs)));
		}
		
		for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq : alg.schema().eqs) {
			if (alg.schema().type(eq.first, eq.second).left) { //type
				for (X x : alg.en(eq.first.second)) {
					Map<Var, Term<Ty, En, Sym, Fk, Att, Void, Void>> map = new HashMap<>();
					Term<Ty, En, Sym, Fk, Att, Void, Void> q = alg.repr(x).convert(); map.put(eq.first.first, q);
					talg.eqs.add(new Eq<>(new Ctx<>(), transX(alg, eq.second.subst(map).convert()), transX(alg, eq.third.subst(map).convert())));
				}
			} 
		}
		
		
		return talg;
	}

	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
		if (!ctx.isEmpty()) {
			Util.anomaly();
		}
		//	if (schema.typeSide.js.java_tys.isEmpty()) {
	//		return dp.eq(ctx, lhs, rhs);
	//	} else {
    //        return col.type(ctx, lhs).left ? intoY(schema.typeSide.js.reduce(lhs)).equals(intoY(schema.typeSide.js.reduce(rhs))) : dp.eq(ctx, lhs, rhs);
	//	}
   //     return dp.eq(ctx, schema.typeSide.js.reduce(lhs), schema.typeSide.js.reduce(rhs)) ; //|| (col.type(ctx, lhs).left ? intoY().equals(intoY(schema.typeSide.js.reduce(rhs))) : false);

		//for typeside terms, must inject into type algebra bc type algebra does things like replace a.age with 45
		return dp.eq(ctx, lhs, rhs) || (col.type(ctx, lhs).left ? intoY(schema.typeSide.js.reduce(lhs)).equals(intoY(schema.typeSide.js.reduce(rhs))) : false);
	}

	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	//TODO aql nf for initial algebra
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		/*if (col.type(ctx, term).left) { //type
			if (schema.typeSide.java_tys.isEmpty()) {
				return dp.nf(ctx, term);
			} else {
				return AqlJs.reduce(dp.nf(ctx, term), col); 
			}
		} 
		if (schema.typeSide.java_tys.isEmpty()) {
			return dp.nf(ctx, term);
		} else if (ctx.isEmpty()){
			return repr(nf(term.convert())).convert();
		}*/
		throw new RuntimeException("Not implemented");
	}
	
	private final List<Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>>>> list = new LinkedList<>();
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> simpl(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> term) {
		 //apparently trans can be called before talg()
		for (Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> t : list) {
			term = term.replaceHead(new Head<>(Term.Sk(t.first)), Collections.emptyList(), t.second);
		}
		return term;
	}
	
	//this is not simplfy from collage - this is how we get 'reduction' to happen, by processing the talg.
	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg;
	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg() {
		if (talg != null) {
			return talg;
		}
		talg = talg(list, this, col);
		return talg;
	}
	
	public static <En,Ty,Sym,Fk,Att,Gen,Sk,X,Y> Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> 
	talg(List<Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>>>> list, Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, ?> alg, Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col) {

		List<Eq<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> eqs = new LinkedList<>(talg_full(alg, col).eqs);
	//	System.out.println(eqs);
		List<Chc<Sk, Pair<X, Att>>> sks = new LinkedList<>(talg_full(alg, col).sks.keySet());
		Iterator<Chc<Sk, Pair<X, Att>>> sks_it = sks.iterator();
		
		while (sks_it.hasNext()) {
			Chc<Sk, Pair<X, Att>> sk = sks_it.next();
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> replacer = null;
			for (Eq<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> eq : eqs) {
				if (!eq.ctx.isEmpty()) {
					continue; //TODO aql or unsafe
				}
				if (eq.lhs.equals(eq.rhs)) {
					continue;
				}
				if (eq.lhs.equals(Term.Sk(sk)) && !eq.rhs.containsProper(new Head<>(Term.Sk(sk)))) {
					replacer = eq.rhs;
					break;
				} else if (eq.rhs.equals(Term.Sk(sk)) && !eq.lhs.containsProper(new Head<>(Term.Sk(sk))))  { //TODO aql use provable eq here?
					replacer = eq.lhs;
					break;
				}
			}
			if (replacer == null) {
				continue;
			}
			Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> replacer2 = replacer;
			sks_it.remove();
			
			List<Eq<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> eqs2 = new LinkedList<>();
			
			for (Eq<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> x : eqs) {
				eqs2.add(new Eq<>(x.ctx, x.lhs.replaceHead(new Head<>(Term.Sk(sk)), Collections.emptyList(), replacer2), x.rhs.replaceHead(new Head<>(Term.Sk(sk)), Collections.emptyList(), replacer2)));
			}
			eqs = eqs2;
			
			list.add(new Pair<>(sk, replacer));
		}

		eqs.removeIf(eq -> eq.lhs.equals(eq.rhs));

		Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>  talg = new Collage<>();
		talg.syms.putAll(alg.schema().typeSide.syms.map);
		talg.tys.addAll(alg.schema().typeSide.tys);
		talg.java_fns.putAll(alg.schema().typeSide.js.java_fns.map);
		talg.java_parsers.putAll(alg.schema().typeSide.js.java_parsers.map);
		talg.java_tys.putAll(alg.schema().typeSide.js.java_tys.map);
		for (Chc<Sk, Pair<X, Att>> sk : sks) {
			talg.sks.put(sk, talg_full(alg, col).sks.get(sk));
		}
		talg.eqs.addAll(eqs);
		
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> eq : alg.schema().typeSide.eqs) {
			talg.eqs.add(new Eq<>(eq.first.inLeft(), transX(alg, eq.second.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Util.voidFn())), transX(alg, eq.third.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Util.voidFn()))));
		}
		
		return talg;
	}
	
	
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> unflatten(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(this::unflatten).collect(Collectors.toList()));
		} else if (term.sk != null) {
            return term.sk.left ? Term.Sk(term.sk.l) : Term.Att(term.sk.r.second, repr(term.sk.r.first).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		} 
		throw new RuntimeException("Anomaly: please report");
	}
	

	//TODO: aql move definitions functionality into Collage
	
	
	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> att(Att att, X x) {
		return reprT0(Chc.inRight(new Pair<>(x, att)));
	}

	
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> reprT0(Chc<Sk, Pair<X, Att>> y) {
        return schema().typeSide.js.java_tys.isEmpty() ? simpl(Term.Sk(y)) : schema.typeSide.js.reduce(simpl(Term.Sk(y)));
	} 
	
	
	@Override
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> y) {
        return schema().typeSide.js.java_tys.isEmpty() ? unflatten(simpl(y)) : unflatten(schema.typeSide.js.reduce(simpl(y)));
	} 

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> sk(Sk sk) {
		return reprT0(Chc.inLeft(sk));
	}
	
	
	//TODO aql why does using algebra's version cause infinite loop?
	private static <Ty, Sym, Att, Sk, En, Fk, Gen, X, Y> Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> transX(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> transX(alg, x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			return Term.Sk(Chc.inLeft(term.sk));
		} else if (term.att != null) {
			return Term.Sk(Chc.inRight(new Pair<>(trans1X(alg, term.arg.asArgForAtt()), term.att)));
		} else if (term.var != null) {
			return Term.Var(term.var);
		}
		throw new RuntimeException("Anomaly: please report: " + term + ", gen " + term.gen + " fk " + term.fk + ", var " + term.var);
	}

	private static <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> X trans1X(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg, Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (term.gen != null) {
			return alg.nf(term);
		} else if (term.fk != null) {
			return alg.fk(term.fk, alg.nf(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	
	@Override
	public String toStringProver() {
		return dp.toStringProver();
	}
	

//	@Override
	public DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp() {
		//return dp;
		return this; //definitely this - not dp bc dp may be for entity side only
	}


}
