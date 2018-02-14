package catdata.aql.fdm;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It.ID;
import catdata.aql.Query;
import catdata.aql.Query.Frozen;
import catdata.aql.Schema;
import catdata.aql.SqlTypeSide;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.Var;
import catdata.aql.fdm.EvalAlgebra.Row;

public class EvalAlgebra<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> extends Algebra<Ty, En2, Sym, Fk2, Att2, Row<En2, X>, Y, Row<En2, X>, Y> {

	@SuppressWarnings("serial")
	// these have to be tagged with the entity to be unique across entities
	public static class Row<En2, X> implements Serializable { // TODO aql
																// removing
																// static causes
																// lots of
																// errors - why?

		public <Z> Row<En2, Z> map(Function<X, Z> f) {
			if (en2 != null) {
				return new Row<>(en2);
			}
			return new Row<>(tail.map(f), v, f.apply(x));
		}

		// public final Ctx<Var,X> ctx;
		private final En2 en2;
		private final Var v;
		private final X x;
		private final Row<En2, X> tail;

		private Map<Var, X> map;
		public synchronized Map<Var, X> asMap() {
			if (map != null) {
				return map;
			}
			Row<En2, X> r = this;
			map = new HashMap<>();
			for (;;) {
				if (r.en2 != null) {
					return map;
				}
				map.put(v, x);
				r = r.tail;
			}
		}

		public final boolean containsKey(Var vv) {
			if (en2 != null) {
				return false;
			} else if (v.equals(vv)) {
				return true;
			}
			return tail.containsKey(vv);
		}

		public final X get(Var vv) {
			if (en2 != null) {
				throw new RuntimeException("Not found: " + vv + ", please report.");
			} else if (v.equals(vv)) {
				return x;
			}
			return tail.get(vv);
		}

		public Row(En2 en2) {
			Util.assertNotNull(en2);
			this.en2 = en2;
			this.v = null;
			this.x = null;
			this.tail = null;
		}

		public Row(Row<En2, X> tail, Var v, X x) {
			this.v = v;
			this.x = x;
			this.tail = tail;
			Util.assertNotNull(v,x,tail);
			this.en2 = null;
			if (tail.equals(this) || tail == this) {
				Util.anomaly();
			}
		}

		public static <X, En2> Row<En2, X> mkRow(Ctx<Var, X> ctx, En2 en2) {
			Row<En2, X> r = new Row<>(en2);
			for (Var v : ctx.keySet()) {
				r = new Row<>(r, v, ctx.get(v));
			}
			return r;
		}

		@Override
		public String toString() {
			if (en2 != null) {
				return ""; 
			}
			return "(" + v + "=" + x + ")" + tail.toString();
		}

		public String toString(Function<X, String> printX) {
			return map(printX).toString();
		}

		
		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((en2 == null) ? 0 : en2.hashCode());
			result = prime * result + ((tail == null) ? 0 : tail.hashCode());
			result = prime * result + ((v == null) ? 0 : v.hashCode());
			result = prime * result + ((x == null) ? 0 : x.hashCode());
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
			Row<?, ?> other = (Row<?, ?>) obj;
			if (en2 == null) {
				if (other.en2 != null)
					return false;
			} else if (!en2.equals(other.en2))
				return false;
			if (tail == null) {
				if (other.tail != null)
					return false;
			} else if (!tail.equals(other.tail))
				return false;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			if (x == null) {
				if (other.x != null)
					return false;
			} else if (!x.equals(other.x))
				return false;
			return true;
		}

		// TODO AQL slowness hurts chase
		public static <En2, X, Y, Ty, En1, Sym, Fk1, Att1, Gen, Sk> Collection<Row<En2, X>> extend(En2 entity, Collection<Row<En2, X>> tuples, Var v, Frozen<Ty, En1, Sym, Fk1, Att1> q, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I, int max, boolean useIndices) {
			List<Row<En2, X>> ret = new LinkedList<>(); // tuples.size() *
														// dom.size());
			for (Row<En2, X> tuple : tuples) {
				Collection<X> dom;
				if (useIndices) {
					List<Pair<Fk1, X>> l1 = getAccessPath(v, tuple, q, I);
					List<Pair<Att1, Object>> l2 = getAccessPath2(v, tuple, q, I);
					dom = I.algebra().en_indexed(q.gens.get(v), l1, l2);	
				} else {
					dom = I.algebra().en(q.gens.get(v));
				}
				outer: for (X x : dom) {
					if (ret.size() > max) {
						throw new RuntimeException("On entity " + entity + ", query evaluation maximum intermediate result size (" + max + ") exceeded.  Try options eval_max_temp_size = " + tuples.size() * dom.size() + " (the largest possible size of the temporary table that triggered this error).  Or, try "
								+ ", options eval_reorder_joins=false and choose a nested loops join order that results in smaller intermediate results.");
					}
					Row<En2, X> row = new Row<>(tuple, v, x);
					for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : q.eqsNoSks()) {
						Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> lhs = trans1(row, eq.lhs, I);
						Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> rhs = trans1(row, eq.rhs, I);
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
			}
			return ret;
		}
	}

	private final Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;
	// private final Algebra<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> alg;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;

	private final Ctx<En2, Pair<List<Var>,Collection<Row<En2, X>>>> ens = new Ctx<>();

	@Override
	public Row<En2, X> fk(Fk2 fk, Row<En2, X> row) {
		Transform<Ty, En1, Sym, Fk1, Att1, Var, Var, Var, Var, ID, Chc<Var, Pair<ID, Att1>>, ID, Chc<Var, Pair<ID, Att1>>> 
		t = Q.fks.get(fk);

		Ctx<Var, X> ret = new Ctx<>();

		for (Var v : ens.get(Q.dst.fks.get(fk).second).first) {
			ret.put(v, trans2(row, t.gens().get(v)));
		}

		return Row.mkRow(ret, Q.dst.fks.get(fk).second);
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
	public Row<En2, X> gen(Row<En2, X> gen) {
		return gen;
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att2 att, Row<En2, X> x) {
		Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> l = trans1(x, Q.atts.get(att).convert(), I);
		if (!l.isPresent()) {
			System.out.flush();
			throw new RuntimeException("Anomly: please report: cannot translate " + att + " on " + x + " alg is " + this.ens);
		}
		return I.algebra().intoY(l.get());
	}

	@Override
	public Collection<Row<En2, X>> en(En2 en) {
		return ens.get(en).second;
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Y sk) {
		return Term.Sk(sk);
	}

	@Override
	public Term<Void, En2, Void, Fk2, Void, Row<En2, X>, Void> repr(Row<En2, X> x) {
		return Term.Gen(x);
	}

	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
		return I.algebra().talg();
	}

	@Override
	public Term<Ty, En2, Sym, Fk2, Att2, Row<En2, X>, Y> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
		Term<Ty, Void, Sym, Void, Void, Void, Y> t = I.algebra().intoY(I.algebra().reprT(term));
		Term<Ty, Void, Sym, Void, Void, Void, Y> u = I.schema().typeSide.js.reduce(t);
		return u.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
	}

	@Override
	public String printX(Row<En2, X> x) {
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

	//TODO aql: associate the loaded and indexed data with the instance, so can be re-used
	public final AqlOptions options;
	public EvalAlgebra(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I, AqlOptions options) {
		this.I = I;
		this.options = options;
		if (!I.schema().equals(q.src)) {
			throw new RuntimeException("Anomaly: please report");
		} else if (!q.consts.keySet().containsAll(q.params.keySet())) {
			throw new RuntimeException("Missing bindings: " + Util.sep(Util.diff(q.params.keySet(), q.consts.keySet()), ",")); //TODO aql
		} else if (!q.consts.keySet().isEmpty()) {
			q = q.deParam();
		}
		Connection conn = null;
		boolean safe = (I.algebra().talg().sks.isEmpty() && I.algebra().talg().eqs.isEmpty()) || allowUnsafeSql();
		boolean useSql = I.size() >= minSizeForSql() && safe && (SqlTypeSide.tys().containsAll(I.schema().typeSide.tys));
		int vlen = (int) options.getOrDefault(AqlOption.varchar_length);
		if (useSql) {
			this.Q = q.unnest();
			Map<En1, List<String>> xx = Collections.emptyMap();
			if (useIndices()) {
				Pair<Collection<Fk1>, Collection<Att1>> l = this.Q.fksAndAttsOfWhere();
				xx = I.schema().toSQL_srcIdxs(l);
			} 
			int startId = (Integer) options.getOrDefault(AqlOption.start_ids_at);
			Pair<Map<X,Integer>, Map<Integer, X>> J = I.algebra().intifyX(startId);
			if (persistentIndices()) {
				conn = I.algebra().addIndices(J, xx, vlen);
			} else {
				conn = I.algebra().createAndLoad(xx, J, vlen);
			}
		} else {
			this.Q = q;
		}
		for (En2 en2 : Q.ens.keySet()) {
			Pair<List<Var>, Collection<Row<En2, X>>> x = eval(en2, this.Q.ens.get(en2), conn, useSql);
			ens.put(en2, x);
		}
		if (conn != null) {
			if (!persistentIndices()) {
				try {
					conn.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}		
		
	}
	

	private boolean allowUnsafeSql() {
		return (boolean) options.getOrDefault(AqlOption.eval_approx_sql_unsafe);
	}
	
	private int maxTempSize() {
		return (int) options.getOrDefault(AqlOption.eval_max_temp_size);
	}
	
	private boolean useIndices() {
		return (boolean) options.getOrDefault(AqlOption.eval_use_indices);
	}
	
	private boolean persistentIndices() {
		return (boolean) options.getOrDefault(AqlOption.eval_sql_persistent_indices);
	}
	
	private int minSizeForSql() {
		return (int) options.getOrDefault(AqlOption.eval_use_sql_above);
	}
	
	//TODO must convert back to original unfolded vars
	private Pair<List<Var>,Collection<Row<En2, X>>> eval(En2 en2, Frozen<Ty, En1, Sym, Fk1, Att1> q, Connection conn, boolean useSql) {
		Collection<Row<En2, X>> ret = null; 
		Integer k = maxTempSize();
		if (useSql) {
			Pair<List<Var>, Collection<Row<En2, X>>> ret2 = evalSql(en2, q, I.algebra().intifyX((int)options.getOrDefault(AqlOption.start_ids_at)), conn);
			//TODO aql should also stop on max temp size?
			return ret2;
		} else {
			ret = new LinkedList<>();
			List<Var> plan = q.order(options, I);
			boolean useIndices = useIndices() && q.gens.size() > 1 && I.algebra().hasFreeTypeAlgebra();
			ret.add(new Row<>(en2));
			for (Var v : plan) {
				ret = Row.extend(en2, ret, v, q, I, k, useIndices);
			}
			return new Pair<>(plan,ret);
		}
	}
	
	
	private Pair<List<Var>, Collection<Row<En2, X>>> evalSql(En2 en2, Frozen<Ty, En1, Sym, Fk1, Att1> q,
			Pair<Map<X,Integer>, Map<Integer, X>> I, Connection conn) {
		
			if (q.gens.isEmpty()) {
				Collection<Row<En2, X>> ret = new LinkedList<>();
				ret.add(new Row<>(en2));
				return new Pair<>(Collections.emptyList(), ret);
			}
		List<Var> order = new LinkedList<>(q.gens().keySet());
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(Q.toSQL().get(en2));
			Collection<Row<En2, X>> ret = new LinkedList<>();
			//ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				Row<En2, X> r = new Row<>(en2);
				for (Var v : order) {
					X x = I.second.get(rs.getInt(v.var)); 
					if (x == null) {
						stmt.close();
						rs.close();
						throw new RuntimeException("Encountered a NULL generator");
					}
					r = new Row<>(r, v, x); 
				}
				ret.add(r);
			}
			rs.close();
			stmt.close();
			return new Pair<>(order, ret);
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
	}
	
	
	
	
	private static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y, En2> List<Pair<Fk1, X>> getAccessPath(Var v, Row<En2, X> tuple, Frozen<Ty, En1, Sym, Fk1, Att1> q2, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
		List<Pair<Fk1, X>> ret = new LinkedList<>();
		for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : q2.eqsNoSks()) {
			if (eq.lhs.fk != null && eq.lhs.arg.equals(Term.Gen(v))) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> rhs = trans1(tuple, eq.rhs, I);
				if (!rhs.isPresent()) {
					continue;
				}
				X x = I.algebra().nf(rhs.get().convert());
				ret.add(new Pair<>(eq.lhs.fk, x));
			} else if (eq.rhs.fk != null && eq.rhs.arg.equals(Term.Gen(v))) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> lhs = trans1(tuple, eq.lhs, I);
				if (!lhs.isPresent()) {
					continue;
				}
				X x = I.algebra().nf(lhs.get().convert());
				ret.add(new Pair<>(eq.rhs.fk, x));
			}
		}
		return ret;
	}

	private static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y, En2> List<Pair<Att1, Object>> getAccessPath2(Var v, Row<En2, X> tuple, Frozen<Ty, En1, Sym, Fk1, Att1> q2, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
		List<Pair<Att1, Object>> ret = new LinkedList<>();
		for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : q2.eqsNoSks()) {
			if (eq.lhs.att != null && eq.lhs.arg.equals(Term.Gen(v))) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> rhs = trans1(tuple, eq.rhs, I);
				if (!rhs.isPresent()) {
					continue;
				}
				Term<Ty, Void, Sym, Void, Void, Void, Y> x = I.algebra().intoY(rhs.get().convert());
				if (x.obj != null) {
					ret.add(new Pair<>(eq.lhs.att, x.obj));
				}
			} else if (eq.rhs.att != null && eq.rhs.arg.equals(Term.Gen(v))) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> lhs = trans1(tuple, eq.lhs, I);
				if (!lhs.isPresent()) {
					continue;
				}
				Term<Ty, Void, Sym, Void, Void, Void, Y> x = I.algebra().intoY(lhs.get().convert());
				if (x.obj != null) {
					ret.add(new Pair<>(eq.rhs.att, x.obj));
				}
			}
		}
		return ret;
	}
	
	private static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, X, Y> Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> trans1(Row<En2, X> row, Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
		if (term.gen != null) {
			return row.containsKey(term.gen) ? Optional.of(I.algebra().repr(row.get(term.gen)).map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())) : Optional.empty();
		} else if (term.obj != null) {
			return Optional.of(term.asObj());
		} else if (term.fk != null) {
			Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> arg = trans1(row, term.arg, I);
			if (!arg.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(Term.Fk(term.fk, arg.get()));
		} else if (term.att != null) {
			Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> arg = trans1(row, term.arg, I);
			if (!arg.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(Term.Att(term.att, arg.get()));
		} else if (term.sym != null) {
			List<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> args = new LinkedList<>();
			for (Term<Ty, En1, Sym, Fk1, Att1, Var, Void> arg : term.args) {
				Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> arg2 = trans1(row, arg, I);
				if (!arg2.isPresent()) {
					return Optional.empty();
				}
				args.add(arg2.get());
			}
			return Optional.of(Term.Sym(term.sym, args));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	/*
	 * private Collection<Row<En2, X>> filter(Collection<Row<En2, X>> rows,
	 * Frozen<Ty, En1, Sym, Fk1, Att1> q) { Collection<Row<En2, X>> ret = new
	 * LinkedList<>();
	 * 
	 * outer: for (Row<En2, X> row : rows) { for (Eq<Ty, En1, Sym, Fk1, Att1,
	 * Var, Void> eq : q.eqs) { Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>>
	 * lhs = trans1(row, eq.lhs, I); Optional<Term<Ty, En1, Sym, Fk1, Att1, Gen,
	 * Sk>> rhs = trans1(row, eq.rhs, I); if (!lhs.isPresent() ||
	 * !rhs.isPresent()) { ret.add(row); continue outer; } if (!I.dp().eq(new
	 * Ctx<>(), lhs.get(), rhs.get())) { continue outer; } } ret.add(row); }
	 * 
	 * return ret; }
	 */
	/*
	 * public Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> translate(Term<Ty, En1,
	 * Sym, Fk1, Att1, Pair<En1, X>, Y> e) { if (e.var != null) { return
	 * Term.Var(e.var); } else if (e.obj != null) { return Term.Obj(e.obj,
	 * e.ty); } else if (e.gen != null) { return
	 * alg.repr(e.gen.second).map(Util.voidFn(), Util.voidFn(),
	 * Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn());
	 * } else if (e.fk != null) { return Schema.fold(F.fks.get(e.fk).second,
	 * translate(e.arg)); } else if (e.att != null) { Term<Ty, En2, Sym, Fk2,
	 * Att2, Gen, Sk> t = F.atts.get(e.att).third.map(Function.identity(),
	 * Function.identity(), Function.identity(), Function.identity(),
	 * Util.voidFn(), Util.voidFn()); return
	 * t.subst(Util.singMapM(F.atts.get(e.att).first, translate(e.arg))); } else
	 * if (e.sym != null) { return Term.Sym(e.sym,
	 * e.args.stream().map(this::translate).collect(Collectors.toList())); }
	 * else if (e.sk != null) { return alg.reprT(Term.Sk(e.sk)); } throw new
	 * RuntimeException("Anomaly: please report: " + e); }
	 * 
	 * private Term<Void,En2,Void,Fk2,Void,Gen,Void> translateE(Term<Void, En1,
	 * Void, Fk1, Void, Pair<En1, X>, Void> e) { return
	 * translate(e.map(Util.voidFn(), Util.voidFn(), Function.identity(),
	 * Util.voidFn(), Function.identity(), Util.voidFn())).convert(); }
	 * 
	 * @Override public Pair<En1, X> nf(Term<Void, En1, Void, Fk1, Void,
	 * Pair<En1, X>, Void> term) { Term<Void,En2,Void,Fk2,Void,Gen,Void> term2 =
	 * translateE(term); X x = alg.nf(term2); En1 en1 =
	 * type(term.map(Util.voidFn(), Util.voidFn(), Function.identity(),
	 * Util.voidFn(), Function.identity(), Util.voidFn())); return new
	 * Pair<>(en1, x); }
	 * 
	 * 
	 * private Map<En1, Collection<Pair<En1, X>>> en_cache = new HashMap<>();
	 * 
	 * @Override public Collection<Pair<En1, X>> en(En1 en) { if
	 * (en_cache.containsKey(en)) { return en_cache.get(en); } Collection<X> in
	 * = alg.en(F.ens.get(en)); Collection<Pair<En1, X>> ret = new
	 * ArrayList<>(in.size()); for (X x : in) { ret.add(new Pair<>(en, x)); }
	 * en_cache.put(en, ret); return ret; }
	 * 
	 * @Override public Pair<En1, X> fk(Fk1 fk1, Pair<En1, X> e) { X x =
	 * e.second; for (Fk2 fk2 : F.trans(Util.singList(fk1))) { x = alg.fk(fk2,
	 * x); } En1 en1 = F.src.fks.get(fk1).second; return new Pair<>(en1, x); }
	 * 
	 * private En1 type(Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Sk> e) { if
	 * (e.gen != null) { return e.gen.first; } else if (e.fk != null) { return
	 * F.src.fks.get(e.fk).second; //no need to recurse, only use outer one }
	 * throw new RuntimeException("Anomaly: please report"); }
	 * 
	 * @Override public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att1 att,
	 * Pair<En1, X> e) { return attY(F.atts.get(att).third, e); }
	 * 
	 * private Term<Ty, Void, Sym, Void, Void, Void, Y> attY(Term<Ty, En2, Sym,
	 * Fk2, Att2, Void, Void> term, Pair<En1, X> x) { if (term.att != null) {
	 * return alg.att(term.att, attX(term.arg.asArgForAtt().map(Util.voidFn(),
	 * Util.voidFn(), Function.identity(), Util.voidFn(), Util.voidFn(),
	 * Util.voidFn()), x.second)); } else if (term.sym != null) { return
	 * Term.Sym(term.sym, term.args.stream().map(q -> attY(q,
	 * x)).collect(Collectors.toList())); } throw new
	 * RuntimeException("Anomaly: please report"); }
	 * 
	 * private X attX(Term<Void, En2, Void, Fk2, Void, Gen, Void> term, X x) {
	 * if (term.var != null) { return x; } else if (term.gen != null) { return
	 * alg.nf(term); } else if (term.fk != null) { return alg.fk(term.fk,
	 * attX(term.arg, x)); } throw new
	 * RuntimeException("Anomaly: please report"); }
	 * 
	 * 
	 * 
	 */

	@Override
	public boolean hasFreeTypeAlgebra() {
		return I.algebra().hasFreeTypeAlgebra();
	}

	@Override
	public boolean hasFreeTypeAlgebraOnJava() {
		return I.algebra().hasFreeTypeAlgebraOnJava();
	}

}