package catdata.aql.fdm;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.Query;
import catdata.aql.Query.Frozen;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.Var;
import catdata.aql.fdm.EvalAlgebra.Row;

public class EvalAlgebra<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> 
extends Algebra<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y, Row<En2,X>, Y> {
 
	@SuppressWarnings("serial")
	public static class Row<En2,X> implements Serializable {
		
		public <Z> Row<En2,Z> map(Function<X,Z> f) {
			return new Row<>(ctx.map(f), en2);
		}
		
		public final Ctx<Var,X> ctx;
		public final En2 en2; 
		
		public final X get(Var v) {
			return ctx.get(v);
		}
		
		public Row(Ctx<Var, X> ctx, En2 en2) {
			this.ctx = ctx;
			this.en2 = en2;
		}

		@Override
		public String toString() {
			return en2 + " " + ctx.toString(x -> x.toString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ctx == null) ? 0 : ctx.hashCode());
			result = prime * result + ((en2 == null) ? 0 : en2.hashCode());
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
			Row<?,?> other = (Row<?,?>) obj;
			if (ctx == null) {
				if (other.ctx != null)
					return false;
			} else if (!ctx.equals(other.ctx))
				return false;
			if (en2 == null) {
				if (other.en2 != null)
					return false;
			} else if (!en2.equals(other.en2))
				return false;
			return true;
		}

		public String toString(Function<X,String> printX) {
			return en2 + " " + ctx.toString(printX);
		}
		
		public static <En2,X> Set<Row<En2, X>> extend(Collection<Row<En2, X>> tuples, Collection<X> dom, Var v, En2 en2) {
			Set<Row<En2, X>> ret = new HashSet<>();
			for (Row<En2, X> tuple : tuples) {
				for (X x : dom) {
					Ctx<Var, X> m = new Ctx<>(tuple.ctx.map);
					m.put(v, x);
					ret.add(new Row<>(m, en2));
				}
			}
			return ret;
		}
	}
	
	public final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	//private final Algebra<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> alg;
	public final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y>  I;
	
	private final Ctx<En2, Collection<Row<En2,X>>> ens = new Ctx<>();
	
	@Override
	public Row<En2,X> fk(Fk2 fk, Row<En2,X> row) {
		Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, Void, Void, Void, Void> 
		t = Q.fks.get(fk);
		
		Ctx<Var,X> ret = new Ctx<>();
		
		for (Var v : t.src().gens().keySet()) {
			ret.put(v, trans2(row, t.gens().get(v)));
		}

		return new Row<>(ret, Q.dst.fks.get(fk).second);
	}
		
	private X trans2(Row<En2, X> row, Term<Void, En1, Void, Fk1, Void, Var, Void> term) {
		if (term.gen != null) {
			return row.get(term.gen);
		} else if (term.fk != null) {
			return I.algebra().fk(term.fk, trans2(row, term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}
			

	@Override
	public Row<En2,X> gen(Row<En2,X> gen) {
		return gen;
	}
	
	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att2 att, Row<En2,X> x) {
		return I.algebra().intoY(trans1(x, Q.atts.get(att)).get());
	}

	@Override
	public Collection<Row<En2,X>> en(En2 en) {
		return ens.get(en);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Y sk) {
		return Term.Sk(sk);
	}

	@Override
	public Term<Void, En2, Void, Fk2, Void, Row<En2,X>, Void> repr(Row<En2,X> x) {
		return Term.Gen(x);
	}

	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
		return I.algebra().talg();
	}

	@Override
	protected Term<Ty, En2, Sym, Fk2, Att2, Row<En2,X>, Y> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
		Term<Ty, Void, Sym, Void, Void, Void, Y> t = I.algebra().intoY(I.algebra().reprT(term));
		Term<Ty, Void, Sym, Void, Void, Void, Y> u = I.schema().typeSide.js.reduce(t);
		return u.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
	}

	@Override
	public String printX(Row<En2,X> x) {
		return "<" + x.toString(I.algebra()::printX) + ">"; 
	}

	@Override
	public String printY(Y y) {
		return "<" + I.algebra().printY(y) + ">";
	}
	
	@Override
	public String toStringProver() {
		return I.algebra().toStringProver();
	}
	
	@Override
	public Schema<Ty, En2, Sym, Fk2, Att2> schema() {
		return Q.dst;
	}

	public EvalAlgebra(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
		Q = q;
		this.I = I;
		if (!I.schema().equals(Q.src)) {
			throw new RuntimeException("Anomaly: please report");
		}
		for (En2 en2 : Q.ens.keySet()) {
			ens.put(en2, eval(en2, Q.ens.get(en2)));
		}
		
	}

	private Collection<Row<En2, X>> eval(En2 en2, Frozen<Ty, En1, Sym, Fk1, Att1> q) {
		Collection<Row<En2, X>> ret = new LinkedList<>();
		ret.add(new Row<>(new Ctx<>(), en2));
		for (Var v : q.order()) { 
			Collection<X> dom = I.algebra().en(q.gens.get(v));
			ret = Row.extend(ret, dom, v, en2);
			ret = filter(ret, q);
		}
		return ret;
	}
	
	private Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> trans1(Row<En2, X> row, Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term) {
		if (term.gen != null) {
			if (row.ctx.containsKey(term.gen)) {
				return Optional.of(I.algebra().repr(row.get(term.gen)).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
			} else {
				return Optional.empty();
			}
		} else if (term.obj != null) {
			return Optional.of(term.asObj());
		} else if (term.fk != null) {
			Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> arg = trans1(row, term.arg);
			if (!arg.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(Term.Fk(term.fk, arg.get()));
		} else if (term.att != null) {
			Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> arg = trans1(row, term.arg);
			if (!arg.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(Term.Att(term.att, arg.get()));
		} else if (term.sym != null) {
			List<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> args = new LinkedList<>();
			for (Term<Ty, En1, Sym, Fk1, Att1, Var, Void> arg : term.args) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> arg2 = trans1(row, arg);
				if (!arg2.isPresent()) {
					return Optional.empty();
				}
				args.add(arg2.get());
			}
			return Optional.of(Term.Sym(term.sym, args));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private Collection<Row<En2, X>> filter(Collection<Row<En2, X>> rows, Frozen<Ty, En1, Sym, Fk1, Att1> q) {
		Collection<Row<En2, X>> ret = new LinkedList<>();
		
		outer: for (Row<En2, X> row : rows) {
			for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : q.eqs) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> lhs = trans1(row, eq.lhs);
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> rhs = trans1(row, eq.rhs);
				if (!lhs.isPresent() || !rhs.isPresent()) {
					ret.add(row);
					continue outer;
				}
				if (!I.dp().eq(new Ctx<>(), lhs.get(), rhs.get())) {
					continue outer;
				}
			}
			ret.add(row);
		}
		
		return ret;
	}
	
/*
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
	
	 private Term<Void,En2,Void,Fk2,Void,Gen,Void> translateE(Term<Void, En1, Void, Fk1, Void, Pair<En1, X>, Void> e) {
		return translate(e.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())).convert();		
	} 
	 
	@Override
	public Pair<En1, X> nf(Term<Void, En1, Void, Fk1, Void, Pair<En1, X>, Void> term) {
		 Term<Void,En2,Void,Fk2,Void,Gen,Void> term2 = translateE(term);
		 X x = alg.nf(term2);
		 En1 en1 = type(term.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
		 return new Pair<>(en1, x);	
	}


	private Map<En1, Collection<Pair<En1, X>>> en_cache = new HashMap<>();
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

	private En1 type(Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Sk> e) {
		if (e.gen != null) {
			return e.gen.first;
		} else if (e.fk != null) {
			return F.src.fks.get(e.fk).second; //no need to recurse, only use outer one
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att1 att, Pair<En1, X> e) {
		return attY(F.atts.get(att).third, e);
	}

	private Term<Ty, Void, Sym, Void, Void, Void, Y> attY(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> term, Pair<En1, X> x) {
		if (term.att != null) {
			return alg.att(term.att, attX(term.arg.asArgForAtt().map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn()), x.second)); 
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(q -> attY(q, x)).collect(Collectors.toList()));
		} 
		throw new RuntimeException("Anomaly: please report");
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

	

*/
	
	

}