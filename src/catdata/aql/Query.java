package catdata.aql;

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
import catdata.Quad;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.It.ID;
import catdata.aql.fdm.ComposeTransform;
import catdata.aql.fdm.IdentityTransform;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralTransform; //TODO aql why depend fdm
import catdata.provers.KBExp;
import catdata.provers.KBExp.KBApp;

public final class Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> implements Semantics {

	@Override
	public int size() {
		return src.size();
	}

	@Override
	public Kind kind() {
		return Kind.QUERY;
	}

	public final Ctx<En2, Frozen<Ty, En1, Sym, Fk1, Att1>> ens = new Ctx<>();
	public final Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts;

	public final Ctx<Fk2, Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>>> fks = new Ctx<>();
	public final Ctx<Fk2, Boolean> doNotValidate = new Ctx<>();

	public final Schema<Ty, En1, Sym, Fk1, Att1> src;
	public final Schema<Ty, En2, Sym, Fk2, Att2> dst;

	public Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> conv1(
			Ctx<En2, Frozen<Ty, En1, Sym, Fk1, Att1>> ens) {
		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ret = new Ctx<>();
		for (En2 en2 : ens.keySet()) {
			ret.put(en2, new Triple<>(ens.get(en2).gens, ens.get(en2).eqs, ens.get(en2).options));
		}
		return ret;
	}

	private Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> conv2()
		{
			Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> ret = new Ctx<>();
		for (Fk2 fk2 : fks.keySet()) {
			ret.put(fk2, new Pair<>(fks.get(fk2).gens(), true)); //TODO aql true is correct here?
		}
		return ret;
	}

	public Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> unnest() {
		Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b = new Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2>(conv1(ens),
				atts, conv2(), src, dst);
		b = unfoldNestedApplications(b);
		Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> p = new Query<>(b.ens, b.atts, b.fks, src, dst, true);
		return p;
	}

	public static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> makeQuery(
			Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens,
			Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts,
			Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks,
			Schema<Ty, En1, Sym, Fk1, Att1> src, Schema<Ty, En2, Sym, Fk2, Att2> dst, boolean doNotCheckPathEqs,
			boolean removeRedundantVars) {
		// do this first to type check
		@SuppressWarnings("unused")
		Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q = new Query<>(ens, atts, fks, src, dst, true);
		
		Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b = new Blob<>(ens, atts, fks, src, dst);
		if (removeRedundantVars) {
			b = removeRedundantVars(b);
		}

		//for testing
		//b = unfoldNestedApplications(b);

		Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> p = new Query<>(b.ens, b.atts, b.fks, src, dst,
				doNotCheckPathEqs);
	
		return p;
	}

	private static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> unfoldNestedApplications(
			Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b) {

		for (;;) {
			Quad<Var, En2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Head<Ty, En1, Sym, Fk1, Att1, Var, Void>> p = findNested(
					Var.it, b);
			if (p == null) {
				return b;
			} else {
				b = elimNested(p.first, p.second, b, p.third, p.fourth);
			}
		}

	}

	private static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> removeRedundantVars(
			Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b) {

		for (;;) {
			Triple<Var, En2, Term<Void, En1, Void, Fk1, Void, Var, Void>> p = findRedundant(b);
			if (p == null) {
				return b;
			} else {
				b = elimRedundant(p.first, p.second, b, p.third);
			}
		}

	}

	private static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> elimNested(
			Var v, En2 second, Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b,
			Term<Ty, En1, Sym, Fk1, Att1, Var, Void> third, Head<Ty, En1, Sym, Fk1, Att1, Var, Void> head) {

		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> xens = new Ctx<>();
		Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> xatts = new Ctx<>();
		Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> xfks = new Ctx<>();

		En1 srcEn = null;
		if (head.fk != null) {
			srcEn = b.src.fks.get(head.fk).first;
		} else if (head.att != null) {
			srcEn = b.src.atts.get(head.att).first;
		}

		for (Att2 att2 : b.atts.keySet()) {
			En2 atts_en2 = b.dst.atts.get(att2).first;
			if (second.equals(atts_en2)) {
				xatts.put(att2, b.atts.get(att2).replace(third, Term.Gen(v)));
			} else {
				xatts.put(att2, b.atts.get(att2));
			}
		}

		for (Fk2 fk2 : b.fks.keySet()) {
			En2 src = b.dst.fks.get(fk2).first;
			En2 dst = b.dst.fks.get(fk2).second;
			Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>> g = new Ctx<>(b.fks.get(fk2).first.map);
			if (second.equals(dst)) {
				g.put(v, third.replace(g.map((s,t) -> new Pair<Term<Ty, En1, Sym, Fk1, Att1, Var, Void>,Term<Ty, En1, Sym, Fk1, Att1, Var, Void>>(Term.Gen(s), t.convert())).map).convert());
			}
			if (second.equals(src)) {
				g = g.map(t -> t.replace(third.convert(), Term.Gen(v)));
			}

			xfks.put(fk2,
					new Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>(g, b.fks.get(fk2).second));
		}

		for (En2 en : b.ens.keySet()) {
			if (en.equals(second)) {
				Ctx<Var, En1> ctx = new Ctx<>(b.ens.get(en).first.map);
				ctx.put(v, srcEn);
				Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = new LinkedList<>();
				for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : b.ens.get(en).second) {
					Term<Ty, En1, Sym, Fk1, Att1, Var, Void> l = eq.lhs.replace(third, Term.Gen(v)),
							r = eq.rhs.replace(third, Term.Gen(v));
					if (!l.equals(r)) {
						eqs.add(new Eq<>(new Ctx<>(), l, r));
					}
				}
				eqs.add(new Eq<>(new Ctx<>(), Term.Gen(v), third));
				xens.put(en, new Triple<>(ctx, eqs, b.ens.get(en).third));
			} else {
				xens.put(en, b.ens.get(en));
			}
		}

		return new Blob<>(xens, xatts, xfks, b.src, b.dst);
	}

	private static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> elimRedundant(
			Var v, En2 en2, Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b,
			Term<Void, En1, Void, Fk1, Void, Var, Void> term) {

		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> xens = new Ctx<>();
		Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> xatts = new Ctx<>();
		Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> xfks = new Ctx<>();

		for (Att2 att2 : b.atts.keySet()) {
			En2 atts_en2 = b.dst.atts.get(att2).first;
			if (en2.equals(atts_en2)) {
				xatts.put(att2, b.atts.get(att2).replaceHead(Head.Gen(v), Collections.emptyList(), term.map(Util::abort,
						Util::abort, Function.identity(), Util::abort, Function.identity(), Function.identity())));
			} else {
				xatts.put(att2, b.atts.get(att2));
			}
		}

		for (Fk2 fk2 : b.fks.keySet()) {
			En2 src = b.dst.fks.get(fk2).first;
			En2 dst = b.dst.fks.get(fk2).second;
			Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>> g = new Ctx<>(b.fks.get(fk2).first.map);
			if (en2.equals(dst)) {
				g.remove(v);
			}
			if (en2.equals(src)) {
				g = g.map(t -> t.replaceHead(Head.Gen(v), Collections.emptyList(), term));
			}

			xfks.put(fk2,
					new Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>(g, b.fks.get(fk2).second));
		}

		for (En2 en : b.ens.keySet()) {
			if (en.equals(en2)) {
				Ctx<Var, En1> ctx = new Ctx<>(b.ens.get(en).first.map);
				ctx.remove(v);
				Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = new LinkedList<>();
				for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : b.ens.get(en).second) {
					Term<Ty, En1, Sym, Fk1, Att1, Var, Void> l = eq.lhs.replaceHead(Head.Gen(v),
							Collections.emptyList(), term.convert()),
							r = eq.rhs.replaceHead(Head.Gen(v), Collections.emptyList(), term.convert());
					if (!l.equals(r)) {
						eqs.add(new Eq<>(new Ctx<>(), l, r));
					}
				}
				xens.put(en, new Triple<>(ctx, eqs, b.ens.get(en).third));
			} else {
				xens.put(en, b.ens.get(en));
			}
		}

		return new Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2>(xens, xatts, xfks, b.src, b.dst);
	}

	private static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Quad<Var, En2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Head<Ty, En1, Sym, Fk1, Att1, Var, Void>> findNested(
			Iterator<Var> it, Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b) {
		for (En2 en2 : b.ens.keySet()) {
			HashSet<KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var>> s = new HashSet<>();

			for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : b.ens.get(en2).second) { 
				Map<KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var>, Set<KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var>>> m1 = new HashMap<>();
				eq.lhs.toKB().allSubExps(m1);
				Map<KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var>, Set<KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var>>> m2 = new HashMap<>();
				eq.rhs.toKB().allSubExps(m2);
				s.addAll(m1.keySet());
				s.addAll(m2.keySet());

				for (KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var> e : s) {
					Quad<Var, En2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Head<Ty, En1, Sym, Fk1, Att1, Var, Void>> q = findNested(
							it, en2, e);
					if (q != null) {
						return q;
					}
				}
			}

			for (Att2 att2 : b.dst.attsFrom(en2)) {
				Quad<Var, En2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Head<Ty, En1, Sym, Fk1, Att1, Var, Void>> q = findNested(
						it, en2, b.atts.get(att2).toKB());
				if (q != null) {
					return q;
				}
			}

			for (Fk2 fk2 : b.dst.fksFrom(en2)) {
				for (Term<Void, En1, Void, Fk1, Void, Var, Void> cand : b.fks.get(fk2).first.values()) {
					Term<Ty, En1, Sym, Fk1, Att1, Var, Void> cand2 = cand.convert();
					Quad<Var, En2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Head<Ty, En1, Sym, Fk1, Att1, Var, Void>> q = findNested(
							it, en2, cand2.toKB());
					if (q != null) {
						return q;
					}
				}
			}

		}
		return null;
	}

	private static <Fk1, En2, Ty, En1, Att1, Sym> Quad<Var, En2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Head<Ty, En1, Sym, Fk1, Att1, Var, Void>> findNested(
			Iterator<Var> it, En2 en2, KBExp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var> e) {
		KBApp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var> f = e.getApp();
		if (f.args.size() == 1 && f.f.fk != null || f.f.att != null) {
			KBApp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var> g = f.args.get(0).getApp();
			if (g.args.size() == 1 && g.f.fk != null) {
				KBApp<Head<Ty, En1, Sym, Fk1, Att1, Var, Void>, Var> x = g.args.get(0).getApp();
				if (x.args.size() == 0 && x.f.gen != null) {
					return new Quad<>(it.next(), en2, Term.fromKB(g), f.f);
				}
			}
		}
		return null;
	}

	private static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Triple<Var, En2, Term<Void, En1, Void, Fk1, Void, Var, Void>> findRedundant(
			Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> b) {
		for (En2 en2 : b.ens.keySet()) {
			for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : b.ens.get(en2).second) { 
				if (eq.lhs.gen != null && !eq.rhs.gens().contains(eq.lhs.gen)) {
					return new Triple<>(eq.lhs.gen, en2, eq.rhs.convert());
				} else if (eq.rhs.gen != null && !eq.lhs.vars().contains(eq.rhs.gen)) {
					return new Triple<>(eq.rhs.gen, en2, eq.lhs.convert());
				}
			}
		}
		return null;
	}

	private static class Blob<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> {
		public final Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens;
		public final Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts;
		public final Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks;
		public final Schema<Ty, En1, Sym, Fk1, Att1> src;
		public final Schema<Ty, En2, Sym, Fk2, Att2> dst;

		
		@Override
		public String toString() {
			return "Blob [ens=" + ens + ", atts=" + atts + ", fks=" + fks + "]";
		}

		public Blob(Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens,
				Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts,
				Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks,
				Schema<Ty, En1, Sym, Fk1, Att1> src, Schema<Ty, En2, Sym, Fk2, Att2> dst) {
			this.ens = ens;
			this.atts = atts;
			this.fks = fks;
			this.src = src;
			this.dst = dst;
		}

	}

	// doNotCheckPathEqs will stop construction of dps
	public Query(Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens,
			Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts,
			Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks,
			Schema<Ty, En1, Sym, Fk1, Att1> src, Schema<Ty, En2, Sym, Fk2, Att2> dst, boolean doNotCheckPathEqs) {
		Util.assertNotNull(ens, atts, fks, src, dst);
		this.src = src;
		this.dst = dst;
		totalityCheck(ens, atts, fks);

		for (En2 en2 : ens.keySet()) {
			try {
				this.ens.put(en2, new Frozen<>(ens.get(en2).first, ens.get(en2).second, src, ens.get(en2).third,
						doNotCheckPathEqs));
			} catch (Throwable thr) {
				thr.printStackTrace();
				throw new RuntimeException("In block for entity " + en2 + ", " + thr.getMessage());
			}
		}

		for (Fk2 fk2 : fks.keySet()) {
			try {
				Boolean b = fks.get(fk2).second || doNotCheckPathEqs;
				this.fks.put(fk2, new LiteralTransform<>(fks.get(fk2).first.map, new HashMap<>(),
						this.ens.get(dst.fks.get(fk2).second), this.ens.get(dst.fks.get(fk2).first), b));
				doNotValidate.put(fk2, b);
			} catch (Throwable thr) {
				thr.printStackTrace();
				throw new RuntimeException("In transform for foreign key " + fk2 + ", " + thr.getMessage());
			}
		}
		this.atts = new Ctx<>(atts.map);
		if (!doNotCheckPathEqs) {
			validate();
		}
	}

	private void totalityCheck(Ctx<En2, ?> ens2, Ctx<Att2, ?> atts, Ctx<Fk2, ?> fks2) {
		for (En2 en2 : dst.ens) {
			if (!ens2.containsKey(en2)) {
				throw new RuntimeException("no query for " + en2);
			}
		}
		for (En2 en2 : ens2.keySet()) {
			if (!dst.ens.contains(en2)) {
				throw new RuntimeException("there is a query for " + en2 + ", which is not an entity in the target");
			}
		}
		for (Att2 att2 : dst.atts.keySet()) {
			if (!atts.containsKey(att2)) {
				throw new RuntimeException("no return clause for attribute " + att2);
			}
		}
		for (Att2 att2 : atts.keySet()) {
			if (!dst.atts.containsKey(att2)) {
				throw new RuntimeException(
						"there is a return clause for " + att2 + ", which is not an attribute in the target");
			}
		}
		for (Fk2 fk2 : dst.fks.keySet()) {
			if (!fks2.containsKey(fk2)) {
				throw new RuntimeException("no transform for foreign key " + fk2);
			}
		}
		for (Fk2 fk2 : fks2.keySet()) {
			if (!dst.fks.containsKey(fk2)) {
				throw new RuntimeException(
						"there is a transform for " + fk2 + ", which is not a foreign key in the target");
			}
		}
	}

	private void validate() {
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : dst.eqs) {
			Chc<Ty, En2> ty = dst.type(eq.first, eq.second);
			Frozen<Ty, En1, Sym, Fk1, Att1> I = ens.get(eq.first.second);
			if (ty.left) {
				Term<Ty, En1, Sym, Fk1, Att1, Var, Void> lhs = transT(eq.second);
				Term<Ty, En1, Sym, Fk1, Att1, Var, Void> rhs = transT(eq.third);
				if (!I.dp.eq(new Ctx<>(), lhs, rhs)) {
					throw new RuntimeException(
							"Target equation " + eq.second + " = " + eq.third + " not respected: transforms to " + lhs
									+ " = " + rhs + ", which is not provable in the sub-query for " + eq.first.second);
				}
			} else { // entity
				for (Var u : ens.get(ty.r).gens.keySet()) {
					Term<Ty, En1, Sym, Fk1, Att1, Var, Void> lhs = transP(eq.second, Term.Gen(u), ty.r);
					Term<Ty, En1, Sym, Fk1, Att1, Var, Void> rhs = transP(eq.third, Term.Gen(u), ty.r);
					if (!I.dp.eq(new Ctx<>(), lhs, rhs)) {
						throw new RuntimeException("Target equation " + eq.second + " = " + eq.third
								+ " not respected: transforms to " + lhs + " = " + rhs
								+ ", which is not provable in the sub-query for " + eq.first.second);
					}
				}
			}
		}
	}

	public static class Frozen<Ty, En1, Sym, Fk1, Att1>
			extends Instance<Ty, En1, Sym, Fk1, Att1, Var, Void, ID, Chc<Void, Pair<ID, Att1>>> {

		public <Gen, Sk, X, Y> List<Var> order(AqlOptions options, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I) {
			if (!(Boolean) options.getOrDefault(AqlOption.eval_reorder_joins)
					|| gens().size() > (Integer) options.getOrDefault(AqlOption.eval_max_plan_depth)) { 
				return new LinkedList<>(gens.map.keySet());
			}
			Map<Pair<Var, Var>, Float> selectivities = estimateSelectivities();
			if (gens().isEmpty()) {
				return new LinkedList<>();
			}
			List<Var> lowest_plan = null;
			float lowest_cost = -1;
			for (List<Var> plan : generatePlans()) {
				float cost = estimateCost(plan, I, selectivities);
				if (lowest_plan == null || cost < lowest_cost) {
					lowest_plan = plan;
					lowest_cost = cost;
				}
			}
			return lowest_plan;
		}

		private float estimateSelectivity(List<Var> l, Var v, Map<Pair<Var, Var>, Float> sel) {
			if (l.isEmpty()) {
				return sel.get(new Pair<>(v, v));
			}
			float ret = sel.get(new Pair<>(v, v));
			for (Var u : l) {
				ret *= sel.get(new Pair<>(u, v));
			}
			return ret;
		}

		private  Map<Pair<Var, Var>, Float> estimateSelectivities() {
			Map<Pair<Var, Var>, Float> ret = new HashMap<>();
			for (Var v1 : gens().keySet()) {
				for (Var v2 : gens().keySet()) {
					ret.put(new Pair<>(v1, v2), 1f);
				}
			}
			for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : eqs) {
				Set<Var> l = new HashSet<>();
				Set<Var> r = new HashSet<>();
				eq.lhs.gens(l);
				eq.rhs.gens(r);
				for (Var v : l) {
					for (Var u : r) {
						ret.put(new Pair<>(v, u), ret.get(new Pair<>(v, u)) * .5f); // TODO
																					// aql
																					// magic
																					// number
						ret.put(new Pair<>(u, v), ret.get(new Pair<>(u, v)) * .5f); // TODO
																					// aql
																					// magic
																					// number

					}
				}
			}
			return ret;
		}

		private <Gen, Sk, X, Y> float estimateCost(List<Var> plan, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I,
				Map<Pair<Var, Var>, Float> selectivities) {
			if (plan.isEmpty()) {
				return 0;
			} else if (plan.size() == 1) {
				return I.algebra().en(gens.get(plan.get(0))).size();
			}
			float cost = I.algebra().en(gens.get(plan.get(0))).size();
			List<Var> vl = Util.singList(plan.get(0));
			for (int i = 1; i < plan.size() - 1; i++) {
				Var vr = plan.get(i);
				float sel = estimateSelectivity(vl, vr, selectivities);
				float cost2 = I.algebra().en(gens.get(vr)).size();
				cost *= (sel * cost2);
				vl.add(vr);
			}
			return cost;
		}

		private  Iterable<List<Var>> generatePlans() {
			return Util.permutationsOf(new LinkedList<>(gens.keySet()));
		}
		/*
		 * private void inc(Term<Ty, En1, Sym, Fk1, Att1, Var, Void> t, Map<Var,
		 * Integer> counts) { if (t.gen != null) { counts.put(t.gen,
		 * counts.get(t.gen) + 1); return; } for (Term<Ty, En1, Sym, Fk1, Att1,
		 * Var, Void> arg : t.args()) { inc(arg, counts); } }
		 */

		public final Ctx<Var, En1> gens;
		public final Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs;
		public final Schema<Ty, En1, Sym, Fk1, Att1> schema;
		private final DP<Ty, En1, Sym, Fk1, Att1, Var, Void> dp;
		public final AqlOptions options;

		public Frozen(Ctx<Var, En1> gens, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs,
				Schema<Ty, En1, Sym, Fk1, Att1> schema, AqlOptions options, boolean dont_validate_unsafe) {
			this.gens = gens;
			this.eqs = eqs;
			this.schema = schema;
			if (!dont_validate_unsafe) {
				dp = AqlProver.create(options, collage(), schema.typeSide.js);
				validateNoTalg();
			} else {
				dp = null;
			}
			this.options = options;
		}

		@Override
		public Schema<Ty, En1, Sym, Fk1, Att1> schema() {
			return schema;
		}

		@Override
		public Ctx<Var, En1> gens() {
			return gens;
		}

		@Override
		public Ctx<Void, Ty> sks() {
			return new Ctx<>();
		}

		@Override
		public Set<Pair<Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>>> eqs() {
			return eqs.stream().map(x -> new Pair<>(x.lhs, x.rhs)).collect(Collectors.toSet());
		}

		@Override
		public DP<Ty, En1, Sym, Fk1, Att1, Var, Void> dp() {
			return dp;
		}

		private InitialAlgebra<Ty, En1, Sym, Fk1, Att1, Var, Void, ID> hidden;

		@Override
		public Algebra<Ty, En1, Sym, Fk1, Att1, Var, Void, ID, Chc<Void, Pair<ID, Att1>>> algebra() {
			if (hidden != null) {
				return hidden;
			}
			hidden = new InitialAlgebra<>(dp, schema, collage(), new It(), x -> x.toString(), x -> x.toString());
			return hidden;
		}

		@Override
		public boolean requireConsistency() {
			return false;
		}

		@Override
		public boolean allowUnsafeJava() {
			return true;
		}

	}

	@Override
	public final String toString() {
		String ret = "";

		ret += "---- entities ---------------------------\n\n";
		ret += Util.sep(ens.map, "\n\n", "\n----\n\n");
		ret += "\n\n---- foreign keys------------------------\n\n";
		ret += Util.sep(fks.map, "\n\n", "\n----\n\n");
		ret += "\n\n---- attributes---------------------\n\n";
		ret += Util.sep(atts.map, "\n\n", "\n----\n\n");

		return ret;
	}

	private Term<Ty, En1, Sym, Fk1, Att1, Var, Void> transT(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> term) {
		if (term.obj != null) {
			return term.asObj();
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::transT).collect(Collectors.toList()));
		} else if (term.att != null) {
			return transP(term.arg, atts.get(term.att), dst.atts.get(term.att).first);
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private List<Fk2> transP(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> term) {
		if (term.var != null) {
			return Collections.emptyList();
		} else if (term.fk != null) {
			return Util.append(Util.singList(term.fk), transP(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> compose(
			List<Fk2> l, En2 en2) {
		if (l.isEmpty()) {
			return new IdentityTransform<>(ens.get(en2));
		} else {
			Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> t = fks
					.get(l.get(0));
			Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> u = compose(
					l.subList(1, l.size()), dst.fks.get(l.get(0)).first);
			return new ComposeTransform<>(t, u);
		}
	}

	private Term<Ty, En1, Sym, Fk1, Att1, Var, Void> transP(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> term,
			Term<Ty, En1, Sym, Fk1, Att1, Var, Void> u, En2 en2) {
		List<Fk2> l = transP(term);
		Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> t = compose(
				l, en2);
		return t.trans(u);
	}

	////////////////

	
	
	public Pair<Collection<Fk1>, Collection<Att1>> fksAndAttsOfWhere() {
		Set<Fk1> fks = new HashSet<>();
		Set<Att1> atts = new HashSet<>();
		for (Frozen<Ty, En1, Sym, Fk1, Att1> I : ens.values()) {
			for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : I.eqs) {
				eq.lhs.fks(fks);
				eq.rhs.fks(fks);
				eq.rhs.atts(atts);
				eq.lhs.atts(atts);
			}
		}
		return new Pair<>(fks, atts);
	}

	
		
	private Map<En2,String> ret;

	public synchronized Map<En2,String> toSQL() {
		if (ret != null) {
			return ret;
		}
		String idCol = "id"; //used internally, so don't honor options
		ret = new HashMap<>();
		for (En2 en2 : ens.keySet()) {
			Frozen<Ty, En1, Sym, Fk1, Att1> b = ens.get(en2);
			Ctx<Var, En1> gens = b.gens;
			Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = b.eqs;
		
			if (ens.get(en2).gens.isEmpty()) {
				ret.put(en2, "Empty from clause doesn't work with sql"); 
				return ret;
			}
			// TODO aql check name collision
			String toString2 = " from ";

			List<String> temp = new LinkedList<>();
			List<String> tempL = new LinkedList<>();
			
			for (Var v : gens.keySet()) {
				temp.add(gens.get(v) + " as " + v);
				tempL.add(v.toString() + "." + idCol + " as " + v); 
			}

			toString2 += Util.sep(temp, ", ");
			
			if (!eqs.isEmpty()) {
				toString2 += whereToString(eqs, idCol);
			}

			
			String toString3 = " select ";
			toString3 += Util.sep(tempL, ", ");
			String y = toString3 + toString2;	
			ret.put(en2, y);
		}


		return ret;
	}
	
	public List<String> toSQLViews(String pre, String post, String idCol) {
		if (!(src.typeSide instanceof SqlTypeSide)) {
			throw new RuntimeException("Not on SQL typeside");
		}
		
		List<String> ret = new LinkedList<>();
		
	/*	Map<En2, Triple<List<Chc<Fk2, Att2>>, List<String>, List<String>>> ss = dst.toSQL_srcSchemas(post, "Varchar");
		for (En2 en2 : dst.ens) {
			ret.addAll(ss.get(en2).second);
		} */
		
  		for (En2 en2 : ens.keySet()) {
			Frozen<Ty, En1, Sym, Fk1, Att1> b = ens.get(en2);
			Ctx<Var, En1> gens = b.gens;
			Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = b.eqs;
		
			if (ens.get(en2).gens.isEmpty()) {
				throw new RuntimeException("Empty from clause doesn't work with sql"); 
			}

			List<String> from = new LinkedList<>();
			List<String> select = new LinkedList<>();
			
			select.add(sk(gens.keySet(), idCol) + " as " + idCol); //add id column
			for (Var v : gens.keySet()) {
				from.add(pre + gens.get(v) + " as " + v);
			}
			for (Att2 att2 : dst.attsFrom(en2)) {
				select.add(atts.get(att2) + " as " + att2); 
			}
			for (Fk2 fk2 : dst.fksFrom(en2)) {
				select.add(sk(fks.get(fk2), idCol) + " as " + fk2); 
			}
			//TODO ADD FOREIGN KEYS aql

			ret.add("drop view if exists " + post + en2);
			
			ret.add("create view " + post + en2 + " as select " + Util.sep(select, ", ") + "\nfrom " + Util.sep(from, ", ") + "\n " + whereToString(eqs, idCol));
		}

		return ret;
	}
	
	private String convert(String x) {
		return "convert(" + x + ", varchar)";
	}
	
	private String qdirty(Term<?,?,?,?,?,?,?> t, String idCol) {
		if (t.gen != null) {
			return t.gen + "." + idCol;
		} else if (t.fk != null) {
			return t.toString();
		}
		return Util.anomaly();
	}
	
	private String sk(Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> h, String idCol) {
		List<Pair<String, String>> l = h.src().gens().keySet().stream().map(v -> new Pair<>(v.var, convert(qdirty(h.gens().get(v), idCol)))).collect(Collectors.toList());
		return sk(l);
	}
	
	private String sk(Collection<Pair<String,String>> vs) {
		if (vs.isEmpty()) {
			Util.anomaly();
		}
		List<String> l = vs.stream().map(x -> "concat('(" + x.first + "=', concat(" + convert(x.second) + ", ')'))").collect(Collectors.toList());
		
		String s = l.get(0);
		for (int i = 1; i < l.size(); i++) {
			s = "concat(" + s + ", " + l.get(i) + ")";
		}
		return s;
	}

	private String sk(Set<Var> vs, String idCol) {
		return sk(vs.stream().map(x -> new Pair<>(x.var, x.var + "." + idCol)).collect(Collectors.toList()));
		/*
		if (vs.isEmpty()) {
			Util.anomaly();
		}
		List<String> l = vs.stream().map(x -> "concat('(" + x + "=', concat(" + convert(x.var + "." + idCol) + ", ')'))").collect(Collectors.toList());
		return Util.sep(l, " + "); */
	}

	private String whereToString(Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs, String idCol) {
		if (eqs.isEmpty()) {
			return "";
		}
		List<String> temp;
		String toString2 = " where ";
		temp = new LinkedList<>();
		for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : eqs) {
			String newLhs;
			if (eq.lhs.gen != null) {
				newLhs = eq.lhs.gen + "." + idCol;
			} else {
				newLhs = quotePrim(eq.lhs).toString();
			}
			String newRhs;
			if (eq.rhs.gen != null) {
				newRhs = eq.rhs.gen + "." + idCol;
			} else {
				newRhs = quotePrim(eq.rhs).toString();
			}
			temp.add(newLhs + " = " + newRhs);
		}
		toString2 += Util.sep(temp, " and ");
		return toString2;
	}

	private Term<Ty, En1, Sym, Fk1, Att1, Var, Void> quotePrim(Term<Ty, En1, Sym, Fk1, Att1, Var, Void> t) {
		if (t.var != null || t.gen != null) {
			return t;
		} else if (t.sym != null && t.args.size() == 0) {
			return t;
		} else if (t.fk != null) {
			return Term.Fk(t.fk, quotePrim(t.arg));
		} else if (t.att != null) {
			return Term.Att(t.att, quotePrim(t.arg));
		} else if (t.obj != null) {
			return Term.Obj("'" + t.obj + "'", t.ty);
		}
		return Util.anomaly();
	}
	
	
	public static <Ty, En, Sym, Fk, Att> Query<Ty, En, Sym, Fk, Att, En, Fk, Att> id(AqlOptions options, Schema<Ty, En, Sym, Fk, Att> S) {
		Var v = new Var("v");
		
		Ctx<En, Triple<Ctx<Var, En>, Collection<Eq<Ty, En, Sym, Fk, Att, Var, Void>>, AqlOptions>> ens0 = new Ctx<>();
		Ctx<Att, Term<Ty, En, Sym, Fk, Att, Var, Void>> atts0 = new Ctx<>();
		Ctx<Fk, Pair<Ctx<Var, Term<Void, En, Void, Fk, Void, Var, Void>>, Boolean>> fks0 = new Ctx<>();
		
		for (En en : S.ens) {
			Ctx<Var, En> from = new Ctx<>();
			from.put(v, en);
			ens0.put(en, new Triple<>(from, Collections.emptyList(), options));
			for (Att att : S.attsFrom(en)) {
				atts0.put(att, Term.Att(att, Term.Gen(v)));
			}
			for (Fk fk : S.fksFrom(en)) {
				Ctx<Var, Term<Void, En, Void, Fk, Void, Var, Void>> h = new Ctx<>();
				h.put(v, Term.Fk(fk, Term.Gen(v)));
				fks0.put(fk, new Pair<>(h, true));
			}
		}
		
		return new Query<>(ens0, atts0, fks0, S, S, true);
	}
	

}
