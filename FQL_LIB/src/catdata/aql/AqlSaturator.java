package catdata.aql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;

//TODO: merge constants and functions in typesides
public class AqlSaturator<Ty, En, Sym, Fk, Att, Gen, Sk, X> extends Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Chc<Sk,Pair<X,Att>>> {

	private final DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp;
	
	private final Map<En, Set<X>> ens;
	private final Map<X, Map<Fk, X>> fks = new HashMap<>();
	private final Map<X, Term<Void, En, Void, Fk, Void, Gen, Void>> reprs = new HashMap<>();	
	private final Map<Term<Void, En, Void, Fk, Void, Gen, Void>, X> nfs = new HashMap<>();
	
	private final Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col;
	private final Schema<Ty, En, Sym, Fk, Att> schema;
	private final Iterator<X> fresh;
	
	public AqlSaturator(AqlOptions ops, Schema<Ty, En, Sym, Fk, Att> schema, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, Iterator<X> fresh) {
		ens = Util.newSetsFor(schema.ens);
		this.col = col;
		this.schema = schema;
		this.fresh = fresh;
		
		if (schema.typeSide.java_tys.isEmpty()) {
			dp = AqlProver.create(ops, col);
		} else {
			dp = AqlProver.create(ops, col.entities_only());
		}
	
		Integer timeout = (Integer) ops.getOrDefault(AqlOption.timeout);
		ExecutorService executor = Executors.newSingleThreadExecutor();
	    Future<Unit> future = executor.submit(new Callable<Unit>() {
	    	@Override
			public Unit call() throws Exception {
				while (saturate1()); 
				return new Unit();
			}
	    });
	    		
	       try {
	    	   	if (timeout < 0) {
	    	   		future.get();
	    	   	} else {
	    	   		future.get(timeout, TimeUnit.SECONDS);
	    	   	}
	       } catch (TimeoutException e) {
	    	   e.printStackTrace();
	    	   future.cancel(true);
	    	   throw new RuntimeException("Timeout (" + timeout + "s) during saturation");
	       } catch (InterruptedException e) {
	    	   return;
	       } catch (ExecutionException e) {
	    	   e.printStackTrace();
	    	   throw new RuntimeException("Error during saturation: " + e.getMessage());
	       }
			//TODO figure out how to do this only once but without concurrent modification exception
			
		if ((Boolean) ops.getOrDefault(AqlOption.require_consistency) && !hasFreeTypeAlgebra()) {
			throw new RuntimeException("Not necessarily consistent; simplified type algebra is\n\n" + talg().simplify());
		}
	}

	//TODO is it really safe to do depth first saturation?
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
		ens.get(col.type(new Ctx<>(), term.convert()).r).add(x);
		reprs.put(x, term);
		
		Map<Fk, X> map = new HashMap<>();
		for (Fk fk : schema().fks.keySet()) {
			if (!col.type(new Ctx<>(), term.convert()).r.equals(schema().fks.get(fk).first)) {
				continue;
			}
			add(Term.Fk(fk, term));
			map.put(fk, nf(Term.Fk(fk, term)));
		}
		fks.put(x, map);
		
		return true; 
	}
	
	private boolean saturate1() throws InterruptedException {
		boolean changed = false;
		for (Gen gen : col.gens.keySet()) {
			if (col.type(new Ctx<>(), Term.Gen(gen)).left) {
				continue; 
			}
			changed = changed | add(Term.Gen(gen));			
		}
		for (Fk fk : col.fks.keySet()) {
			List<X> set = new ArrayList<>(ens.get(schema().fks.get(fk).first));
			for (X x : set) { //concurrent modification otherwise
				changed = changed | add(Term.Fk(fk, repr(x)));						
			}
		}
		return changed;
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return schema;
	}

	@Override
	public Collection<X> en(En en) {
		return ens.get(en);
	}

	@Override
	public X fk(Fk fk, X x) {
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
			throw new RuntimeException("Anomaly: please report: " + x + " not in " + reprs);
		}
		return ret;
	}
	
	private X nf0(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (nfs.containsKey(term)) {
			return nfs.get(term);
		}
		En en = col.type(new Ctx<>(), term.convert()).r;
		for (X x : ens.get(en)) {
			if (dp.eq(new Ctx<>(), term.convert(), repr(x).convert())) {
				nfs.put(term, x);
				return x;
			}
		}
		return null;
	}

	@Override
	public X nf(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		X x = nf0(term);
		if (x == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		return x;
	}

	private Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg_full() {
		Collage<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> talg = new Collage<>(schema.typeSide);
		for (Sk sk : col.sks.keySet()) {
			talg.sks.put(Chc.inLeft(sk), col.sks.get(sk));
		}
		for (En en : col.ens) {
			for (X x : ens.get(en)) {
				for (Att att : col.atts.keySet()) {
					Pair<En, Ty> ty = col.atts.get(att);
					if (!ty.first.equals(en)) {
						continue;
					}
					talg.sks.put(Chc.inRight(new Pair<>(x, att)), ty.second);
				}
			}
		}
		for (Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : col.eqs) {
			if (!col.type(eq.first, eq.second).left) {
				continue; //entity
			}
			if (!eq.first.isEmpty()) {
				continue; //in type side or schema
			}
			if (schema.typeSide.eqs.contains(new Triple<>(new Ctx<>(), eq.second, eq.third))) {
				continue; //in type side
			}
			talg.eqs.add(new Triple<>(new Ctx<>(), transX(eq.second), transX(eq.third)));
		}
		
		for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq : schema().eqs) {
			if (schema().type(eq.first, eq.second).left) { //type
				for (X x : ens.get(eq.first.second)) {
					Map<Var, Term<Ty, En, Sym, Fk, Att, Void, Void>> map = new HashMap<>();
					map.put(eq.first.first, repr(x).convert());
					talg.eqs.add(new Triple<>(new Ctx<>(), transX(eq.second.subst(map).convert()), transX(eq.third.subst(map).convert())));
				}
			} 
		}
		
		
		return talg;
	}

	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs, Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs) {
		if (schema.typeSide.java_tys.isEmpty()) {
			return dp.eq(ctx, lhs, rhs);
		} else {
			if (col.type(ctx, lhs).left) { //type
				return AqlJs.reduce(lhs, col).equals(AqlJs.reduce(rhs, col));
			} else {
				return dp.eq(ctx, lhs, rhs);
			}
		}
	}

	@Override
	public boolean hasNFs() {
		return true;
	}

	@Override
	public Term<Ty, En, Sym, Fk, Att, Gen, Sk> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (col.type(ctx, term).left) { //type
			if (schema.typeSide.java_tys.isEmpty()) {
				return dp.nf(ctx, term);
			} else {
				return AqlJs.reduce(term, col); 
			}
		} 
		if (schema.typeSide.java_tys.isEmpty()) {
			return dp.nf(ctx, term);
		} else if (ctx.isEmpty()){
			return repr(nf(term.convert())).convert();
		}
		throw new RuntimeException("Anomaly: please report");
	}

	
	
	private List<Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>>>> list = new LinkedList<>();
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> simpl(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X,Att>>> term) {
		talg(); //apparently trans can be called before talg()
		for (Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> t : list) {
			term = term.replaceHead(new Head<>(Term.Sk(t.first)), Collections.emptyList(), t.second);
		}
		return term;
	}
	
	//this is not simplfy from collage - this is how we get 'reduction' to happen, by processing the talg.
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
				if (!eq.first.isEmpty()) {
					continue; //TODO or unsafe
				}
				if (eq.second.equals(eq.third)) {
					continue;
				}
				if (eq.second.equals(Term.Sk(sk)) && !eq.third.containsProper(new Head<>(Term.Sk(sk)))) {
					replacer = eq.third;
					break;
				} else if (eq.third.equals(Term.Sk(sk)) && !eq.second.containsProper(new Head<>(Term.Sk(sk))))  { //TODO - use provable eq here?
					replacer = eq.second;
					break;
				}
			}
			if (replacer == null) {
				continue;
			}
			final Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> replacer2 = replacer;
			sks_it.remove();
			eqs = eqs.stream().map(x -> {
				return new Triple<>(x.first, x.second.replaceHead(new Head<>(Term.Sk(sk)), Collections.emptyList(), replacer2), x.third.replaceHead(new Head<>(Term.Sk(sk)), Collections.emptyList(), replacer2));
			}).collect(Collectors.toList());
			
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
	
	private Term<Ty, En, Sym, Fk, Att, Gen, Sk> unflatten(Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> unflatten(x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			if (term.sk.left) {
				return Term.Sk(term.sk.l);
			} else {
				return Term.Att(term.sk.r.second, repr(term.sk.r.first).convert());
			}
		} 
		throw new RuntimeException("Anomaly: please report");
	}
	
	/*
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		return simpl(trans0(term));
	}
	
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> trans0(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> trans0(x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			return Term.Sk(Chc.inLeft(term.sk));
		} else if (term.att != null) {
			return Term.Sk(Chc.inRight(new Pair<>(trans1(term.arg.convert()), term.att)));
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
		} else if (term.obj != null) {
			return term.obj.toString(); 
		}
		throw new RuntimeException("Anomaly: please report: " + term);
	} */
	
	//TODO: move definitions functionality into Collage
	/*
	public String talgToString() {
		String ret = "generating labelled nulls\n\t";
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
		ret += "\n\ndefinitions\n\t";
		List<String> z = new LinkedList<>();
		for (Pair<Chc<Sk, Pair<X, Att>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>>> eq : list) {
			z.add(toString(Term.Sk(eq.first)) + " := " + toString(eq.second));
		}
		ret += Util.sep(z, "\n\t");
		return ret;
	}
		*/
	
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> att(Att att, X x) {
		return reprT(Chc.inRight(new Pair<>(x, att)));
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> reprT(Chc<Sk, Pair<X, Att>> y) {
		if (schema().typeSide.java_tys.isEmpty()) {
			return simpl(Term.Sk(y));
		} else {
			return AqlJs.reduce(simpl(Term.Sk(y)), col);
		}
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> sk(Sk sk) {
		return reprT(Chc.inLeft(sk));
	}
	
	@Override
	public String printSk(Chc<Sk, Pair<X, Att>> y) {
		return unflatten(Term.Sk(y)).toString();
	}
	
	/*
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> att(Att att, X x) {
		if (schema().typeSide.java_tys.isEmpty()) {
			return trans(Term.Att(att, repr(x)));
		} 
		return AqlJs.reduce(trans(Term.Att(att, repr(x))), schema().collage());
	}
	*/
	
	//slightly different than algebra versions
	private Term<Ty, Void, Sym, Void, Void, Void, Chc<Sk, Pair<X, Att>>> transX(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> transX(x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			return Term.Sk(Chc.inLeft(term.sk));
		} else if (term.att != null) {
			return Term.Sk(Chc.inRight(new Pair<>(trans1X(term.arg.convert()), term.att)));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private X trans1X(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (term.gen != null) {
			return nf(term);
		} else if (term.fk != null) {
			return fk(term.fk, nf(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	@Override
	public String toStringProver() {
		return dp.toString();
	}

}
