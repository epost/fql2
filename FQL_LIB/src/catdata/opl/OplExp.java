package catdata.opl;

import java.awt.GridLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import catdata.Chc;
import catdata.Pair;
import catdata.Quad;
import catdata.Triple;
import catdata.Utils;
import catdata.algs.kb.KB;
import catdata.algs.kb.KBExp;
import catdata.fqlpp.cat.FinSet;
import catdata.ide.CodeTextPanel;
import catdata.ide.NEWDEBUG;
import catdata.ide.Util;
import catdata.opl.OplParser.DoNotIgnore;

public abstract class OplExp implements OplObject {

	@Override
	public JComponent display() {
		CodeTextPanel p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
		JTabbedPane ret = new JTabbedPane();
		ret.add(p, "Text");
		return ret;
	}
	
	public abstract <R, E> R accept(E env, OplExpVisitor<R, E> v);

	public static class OplVar extends OplExp {
		String name;

		public OplVar(String c) {
			if (c == null) {
				throw new RuntimeException();
			}
			this.name = c;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class OplFlower<S, C, V, X, Z> extends OplExp {
		Map<Z, OplTerm<C, V>> select;
		Map<V, S> from;
		List<Pair<OplTerm<C, V>, OplTerm<C, V>>> where;
		OplSetInst<S, C, X> I;
		String I0;

		public OplFlower(Map<Z, OplTerm<C, V>> select, Map<V, S> from,
				List<Pair<OplTerm<C, V>, OplTerm<C, V>>> where, String i0) {
			super();
			this.select = select;
			this.from = from;
			this.where = where;
			I0 = i0;
		}

		public void validate() {
			@SuppressWarnings("unchecked")
			OplSig<S, C, V> sig = (OplSig<S, C, V>) I.sig0;

			for (V v : from.keySet()) {
				S s = from.get(v);
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Bad sort: " + s);
				}
			}
			OplCtx<S, V> ctx = new OplCtx<>(from);
			for (Pair<OplTerm<C, V>, OplTerm<C, V>> eq : where) {
				eq.first.type(sig, ctx);
				eq.second.type(sig, ctx);
			}
			for (Z z : select.keySet()) {
				OplTerm<C, V> e = select.get(z);
				e.type(sig, ctx);
			}
		}

		public OplSetTrans<String, String, String> eval(OplSetTrans<S, C, X> h) {
			OplSetInst<S, C, X> I = h.src0;
			OplSetInst<S, C, X> J = h.dst0;

			Pair<Map<Integer, OplCtx<X, Z>>, OplSetInst<String, String, String>> I0X = eval(I);
			Pair<Map<Integer, OplCtx<X, Z>>, OplSetInst<String, String, String>> J0X = eval(J);
			Map<Integer, OplCtx<X, Z>> Im = I0X.first;
			Map<Integer, OplCtx<X, Z>> Jm = J0X.first;
			OplSetInst<String, String, String> QI = I0X.second;
			OplSetInst<String, String, String> QJ = J0X.second;

			@SuppressWarnings("unchecked")
			OplSig<S, C, V> sig = (OplSig<S, C, V>) I.sig0;
			OplCtx<S, V> ctx = new OplCtx<>(from);

			Map<String, Map<String, String>> sorts = new HashMap<>();
			for (S k : I.sig0.sorts) {
				if (k.toString().equals("_Q")) {
					continue;
				}
				Set<X> X = I.sorts.get(k);
				Map<String, String> m = new HashMap<>();
				for (X x : X) {
					m.put(x.toString(), h.sorts.get(k).get(x).toString());
				}
				sorts.put(k.toString(), m);
			}
			Map<String, String> m = new HashMap<>();
			for (Integer i : Im.keySet()) {
				OplCtx<X, Z> t = Im.get(i);
				Map<Z, X> n = new HashMap<>();
				for (Z z : t.vars0.keySet()) {
					X x = t.vars0.get(z);
					S s = select.get(z).type(sig, ctx);
					X x2 = h.sorts.get(s).get(x);
					n.put(z, x2);
				}
				OplCtx<X, Z> t2 = new OplCtx<>(n);
				Integer j = Util.revLookup(Jm, t2);
				if (j == null) {
					throw new RuntimeException("Not found: " + t2 + " in " + Jm);
				}
				m.put("_" + Integer.toString(i), "_" + Integer.toString(j));
			}
			sorts.put("_Q", m);

			OplSetTrans<String, String, String> ret = new OplSetTrans<String, String, String>(
					sorts, "?", "?");
			ret.validate(QI.sig0, QI, QJ);
			return ret;
		}

		public Pair<Map<Integer, OplCtx<X, Z>>, OplSetInst<String, String, String>> eval(
				OplSetInst<S, C, X> I) {
			this.I = I;
			validate();

			@SuppressWarnings("unchecked")
			OplSig<S, C, V> sig = (OplSig<S, C, V>) I.sig0;
			OplCtx<S, V> ctx = new OplCtx<>(from);

			Set<OplCtx<X, V>> tuples = new HashSet<>();
			tuples.add(new OplCtx<>());

			for (V v : from.keySet()) {
				S s = from.get(v);
				Set<X> dom = I.sorts.get(s);
				tuples = extend(tuples, dom, v);
				tuples = filter(tuples, where, I);
			}
			if (from.keySet().isEmpty()) {
				tuples = filter(tuples, where, I);
			}

			Set<String> ret_sorts = new HashSet<>();
			for (S c : sig.sorts) {
				ret_sorts.add(c.toString());
			}
			ret_sorts.add("_Q");
			Map<String, Pair<List<String>, String>> ret_symbols = new HashMap<>();
			for (Z z : select.keySet()) {
				OplTerm<C, V> k = select.get(z);
				ret_symbols.put(z.toString(), new Pair<>(Util.singList("_Q"), k.type(sig, ctx)
						.toString()));
			}
			OplSig<String, String, V> ret_sig = new OplSig<>(sig.fr, new HashMap<>(), ret_sorts,
					ret_symbols, new LinkedList<>());

			Map<String, Set<String>> ret_sorts2 = new HashMap<>();
			Set<String> projected = new HashSet<>();
			Map<String, Map<List<String>, String>> ret_symbols2 = new HashMap<>();
			Map<Integer, OplCtx<X, Z>> m = new HashMap<>();
			int i = 0;
			for (OplCtx<X, V> env : tuples) {
				Map<Z, X> tuple = new HashMap<>();
				for (Z z : select.keySet()) {
					tuple.put(z, select.get(z).eval(sig, I, env));
				}
				projected.add("_" + Integer.toString(i));
				m.put(i, new OplCtx<X, Z>(tuple));
				i++;
			}
			ret_sorts2.put("_Q", projected);
			for (S s : sig.sorts) {
				ret_sorts2.put(s.toString(), I.sorts.get(s).stream().map(x -> {
					return x.toString();
				}).collect(Collectors.toSet()));
			}

			for (Z z : select.keySet()) {
				Map<List<String>, String> n = new HashMap<>();
				for (int j = 0; j < i; j++) {
					OplCtx<X, Z> tuple = m.get(j);
					n.put(Util.singList("_" + j), tuple.get(z).toString());
				}
				ret_symbols2.put(z.toString(), n);
			}

			OplSetInst<String, String, String> ret = new OplSetInst<>(ret_sorts2, ret_symbols2, "?");
			ret.validate(ret_sig);
			return new Pair<>(m, ret);
		}

		@SuppressWarnings("unchecked")
		private static <X, C, S, V> Set<OplCtx<X, V>> filter(Set<OplCtx<X, V>> tuples,
				List<Pair<OplTerm<C, V>, OplTerm<C, V>>> where, OplSetInst<S, C, X> I) {
			Set<OplCtx<X, V>> ret = new HashSet<>();
			outer: for (OplCtx<X, V> tuple : tuples) {
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> eq : where) {
					if (eq.first.isGround(tuple) && eq.second.isGround(tuple)) {
						X l = eq.first.eval((OplSig<S, C, V>) I.sig0, I, tuple);
						X r = eq.second.eval((OplSig<S, C, V>) I.sig0, I, tuple);
						if (!l.equals(r)) {
							continue outer;
						}
					}
				}
				ret.add(tuple);
			}
			return ret;
		}

		private static <X, V> Set<OplCtx<X, V>> extend(Set<OplCtx<X, V>> tuples, Set<X> dom, V v) {
			Set<OplCtx<X, V>> ret = new HashSet<>();

			for (OplCtx<X, V> tuple : tuples) {
				for (X x : dom) {
					Map<V, X> m = new HashMap<>(tuple.vars0);
					m.put(v, x);
					OplCtx<X, V> new_tuple = new OplCtx<>(m);
					ret.add(new_tuple);
				}
			}

			return ret;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}


	

	public static class OplUnSat extends OplExp {

		String I;

		public OplUnSat(String I) {
			this.I = I;
		}

		// TODO to use with saturate_easy, should turn back into theory
		// TODO this type signature is totally wrong, is not inverse of
		// saturate_easy
		// TODO not sure what this is, but can only desaturate things that were
		// first saturted, not arbitrary models
		public static <S, C, V, X> OplPres<S, C, V, X> desaturate(String S, OplSetInst<S, C, X> I) {
			@SuppressWarnings("unchecked")
			OplSig<S, C, V> sig = (OplSig<S, C, V>) I.sig0; // TODO

			Map<X, S> gens = new HashMap<>();
			for (S s : sig.sorts) {
				for (X c : I.sorts.get(s)) {
					gens.put(c, s);
				}
			}

			List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations = new LinkedList<>();
			for (C f : sig.symbols.keySet()) {
				List<S> arg_ts = sig.symbols.get(f).first;
				if (arg_ts.isEmpty()) {
					continue;
				}
				Map<Integer, List<X>> l = new HashMap<>();
				int i = 0;
				for (S t : arg_ts) {
					l.put(i, new LinkedList<>(I.sorts.get(t)));
					i++;
				}
				List<LinkedHashMap<Integer, X>> m = FinSet.homomorphs(l);
				for (LinkedHashMap<Integer, X> a : m) {
					List<X> arg1 = new LinkedList<>();
					List<OplTerm<Chc<C, X>, V>> arg2 = new LinkedList<>();
					for (int j = 0; j < i; j++) {
						arg1.add(a.get(j));
						arg2.add(new OplTerm<>(Chc.inRight(a.get(j)), new LinkedList<>()));
					}
					OplTerm<Chc<C, X>, V> termA = new OplTerm<>(Chc.inLeft(f), arg2);
					OplTerm<Chc<C, X>, V> termB = new OplTerm<>(Chc.inRight(I.symbols.get(f).get(
							arg1)), new LinkedList<>());
					equations.add(new Pair<>(termA, termB));
				}
			}

			OplPres<S, C, V, X> ret = new OplPres<S, C, V, X>(new HashMap<>(), S, sig, gens,
					equations);
			return ret;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class OplUberSat extends OplExp {

		String I, P;

		public OplUberSat(String I, String P) {
			this.I = I;
			this.P = P;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		private static <S, C, V, X, Y> OplSetInst<S, C, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> inject(
				OplSetInst<S, C, OplTerm<Chc<C, X>, V>> I, OplJavaInst I0) {
			Map<S, Set<KBExp<Chc<Chc<C, X>, JSWrapper>, V>>> sorts = new HashMap<>();
			for (S s : I.sorts.keySet()) {
				Set<KBExp<Chc<Chc<C, X>, JSWrapper>, V>> set = new HashSet<>();
				for (OplTerm<Chc<C, X>, V> e : I.sorts.get(s)) {
					OplTerm<Chc<Chc<C, X>, JSWrapper>, V> kkk = OplSig.inject(e);
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> z = OplToKB.redBy(I0, jjj);
					set.add(z);
				}
				sorts.put(s, set);
			}

			Map<C, Map<List<KBExp<Chc<Chc<C, X>, JSWrapper>, V>>, KBExp<Chc<Chc<C, X>, JSWrapper>, V>>> symbols = new HashMap<>();
			for (C c : I.symbols.keySet()) {
				Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>> m = I.symbols.get(c);
				Map<List<KBExp<Chc<Chc<C, X>, JSWrapper>, V>>, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> n = new HashMap<>();
				for (List<OplTerm<Chc<C, X>, V>> a : m.keySet()) {
					List<KBExp<Chc<Chc<C, X>, JSWrapper>, V>> l = new LinkedList<>();
					for (OplTerm<Chc<C, X>, V> e : a) {
						OplTerm<Chc<Chc<C, X>, JSWrapper>, V> kkk = OplSig.inject(e);
						KBExp<Chc<Chc<C, X>, JSWrapper>, V> jjj = OplToKB.convert(kkk);
						KBExp<Chc<Chc<C, X>, JSWrapper>, V> z = OplToKB.redBy(I0, jjj);
						l.add(z);
					}
					OplTerm<Chc<Chc<C, X>, JSWrapper>, V> kkk = OplSig.inject(m.get(a));
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> z = OplToKB.redBy(I0, jjj);
					n.put(l, z);
				}
				symbols.put(c, n);
			}

			OplSetInst<S, C, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> ret = new OplSetInst<>(sorts,
					symbols, I.sig);
			ret.validate(I.sig0);
			return ret;
		}

		public static <S, C, V, X> OplSetInst<S, C, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> saturate(
				OplJavaInst I0, OplPres<S, C, V, X> P0) {
			OplSetInst<S, C, OplTerm<Chc<C, X>, V>> I = OplSat.saturate(P0);
			OplSetInst<S, C, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> J = inject(I, I0);

			return J;
		}

	}

	public static class OplSat extends OplExp {

		String I;

		public OplSat(String I) {
			this.I = I;
		}

		// works fine
		public static <S, C, V, X> OplSetInst<S, Chc<C, X>, OplTerm<Chc<C, X>, V>> saturateEasy(
				OplPres<S, C, V, X> P) {
			OplSig<S, Chc<C, X>, V> sig = P.toSig();
			return sig.saturate(P.S);
		}

		public static <S, C, V, X> OplSetInst<S, C, OplTerm<Chc<C, X>, V>> saturate(
				OplPres<S, C, V, X> P) {
			OplSig<S, Chc<C, X>, V> sig = P.toSig();
			OplToKB<S, Chc<C, X>, V> kb = sig.getKB();

			Map<S, Set<OplTerm<Chc<C, X>, V>>> sorts = kb.doHoms();

			Map<C, Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>>> symbols = new HashMap<>();
			for (C f : P.sig.symbols.keySet()) {
				Pair<List<S>, S> ty = P.sig.symbols.get(f);
				Map<Integer, List<OplTerm<Chc<C, X>, V>>> args = new HashMap<>();
				int i = 0;
				for (S t : ty.first) {
					args.put(i++, new LinkedList<>(sorts.get(t)));
				}
				List<LinkedHashMap<Integer, OplTerm<Chc<C, X>, V>>> cands = FinSet.homomorphs(args);
				Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>> out = new HashMap<>();
				for (LinkedHashMap<Integer, OplTerm<Chc<C, X>, V>> cand : cands) {
					List<OplTerm<Chc<C, X>, V>> actual = new LinkedList<>();
					for (int j = 0; j < i; j++) {
						actual.add(cand.get(j));
					}
					OplTerm<Chc<C, X>, V> to_red = new OplTerm<>(Chc.inLeft(f), actual);
					OplTerm<Chc<C, X>, V> red = OplToKB.convert(kb.nf(OplToKB.convert(to_red)));
					out.put(actual, red);
				}
				symbols.put(f, out);
			}

			OplSetInst<S, C, OplTerm<Chc<C, X>, V>> ret = new OplSetInst<>(sorts, symbols, P.S);
			ret.validate(P.sig);
			return ret;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplSCHEMA0<S, C, V> extends OplExp {
		public Map<C, Integer> prec;
		public Set<S> entities;
		public Map<C, Pair<List<S>, S>> edges, attrs;
		public List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> pathEqs, obsEqs;
		public String typeSide;
		
		public OplSCHEMA0(Map<C, Integer> prec, Set<S> entities, Map<C, Pair<List<S>, S>> edges,
				Map<C, Pair<List<S>, S>> attrs,
				List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> pathEqs,
				List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> obsEqs, String typeSide) {
			super();
			this.prec = prec;
			this.entities = entities;
			this.edges = edges;
			this.attrs = attrs;
			this.pathEqs = pathEqs;
			this.obsEqs = obsEqs;
			this.typeSide = typeSide;
		}

		public void validate(OplSig<S, C, V> sig) {
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : pathEqs) {
					if (eq.first.vars0.keySet().size() != 1) {
						throw new RuntimeException("Non-1 context size for " + eq);
					}
					if (!entities.contains(eq.first.values().get(0))) {
						throw new RuntimeException("Non-entity in context for " + eq);		
					}
			}
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : obsEqs) {
					if (eq.first.vars0.keySet().size() != 1) {
						throw new RuntimeException("Non-1 context size for " + eq);
					}
					if (!entities.contains(eq.first.values().get(0))) {
						throw new RuntimeException("Non-entity in context for " + eq);		
					}
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
			result = prime * result + ((edges == null) ? 0 : edges.hashCode());
			result = prime * result + ((entities == null) ? 0 : entities.hashCode());
			result = prime * result + ((obsEqs == null) ? 0 : obsEqs.hashCode());
			result = prime * result + ((pathEqs == null) ? 0 : pathEqs.hashCode());
			result = prime * result + ((prec == null) ? 0 : prec.hashCode());
			result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
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
			OplSCHEMA0<?,?,?> other = (OplSCHEMA0<?,?,?>) obj;
			if (attrs == null) {
				if (other.attrs != null)
					return false;
			} else if (!attrs.equals(other.attrs))
				return false;
			if (edges == null) {
				if (other.edges != null)
					return false;
			} else if (!edges.equals(other.edges))
				return false;
			if (entities == null) {
				if (other.entities != null)
					return false;
			} else if (!entities.equals(other.entities))
				return false;
			if (obsEqs == null) {
				if (other.obsEqs != null)
					return false;
			} else if (!obsEqs.equals(other.obsEqs))
				return false;
			if (pathEqs == null) {
				if (other.pathEqs != null)
					return false;
			} else if (!pathEqs.equals(other.pathEqs))
				return false;
			if (prec == null) {
				if (other.prec != null)
					return false;
			} else if (!prec.equals(other.prec))
				return false;
			if (typeSide == null) {
				if (other.typeSide != null)
					return false;
			} else if (!typeSide.equals(other.typeSide))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}

	public static class OplSig<S, C, V> extends OplExp {

		private OplToKB<S, C, V> kb;

		public OplToKB<S, C, V> getKB() {
			if (kb != null) {
				return kb;
			}
			kb = new OplToKB<S, C, V>(fr, this);
			return kb;
		} 

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((implications == null) ? 0 : implications.hashCode());
			result = prime * result + ((equations == null) ? 0 : equations.hashCode());
			result = prime * result + ((sorts == null) ? 0 : sorts.hashCode());
			result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
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
			OplSig<?, ?, ?> other = (OplSig<?, ?, ?>) obj;
			if (equations == null) {
				if (other.equations != null)
					return false;
			} else if (!equations.equals(other.equations))
				return false;
			if (implications == null) {
				if (other.implications != null)
					return false;
			} else if (!implications.equals(other.implications))
				return false;
			if (sorts == null) {
				if (other.sorts != null)
					return false;
			} else if (!sorts.equals(other.sorts))
				return false;
			if (symbols == null) {
				if (other.symbols != null)
					return false;
			} else if (!symbols.equals(other.symbols))
				return false;
			return true;
		}

		public static <X, C, V> OplTerm<Chc<C, X>, V> inject(OplTerm<C, V> e) {
			if (e.var != null) {
				return new OplTerm<>(e.var);
			}
			List<OplTerm<Chc<C, X>, V>> args0 = new LinkedList<>();
			for (OplTerm<C, V> a : e.args) {
				args0.add(inject(a));
			}
			return new OplTerm<Chc<C, X>, V>(Chc.inLeft(e.head), args0);
		}

		public <X> OplSig<S, Chc<C, X>, V> inject() {
			Map<Chc<C, X>, Pair<List<S>, S>> symbols0 = new HashMap<>();
			for (C f : symbols.keySet()) {
				Pair<List<S>, S> s = symbols.get(f);
				symbols0.put(Chc.inLeft(f), s);
			}
			List<Triple<OplCtx<S, V>, OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations0 = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : equations) {
				equations0.add(new Triple<>(eq.first, inject(eq.second), inject(eq.third)));
			}
			List<Triple<OplCtx<S, V>, List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>>, List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>>>> implications0 = new LinkedList<>();
			for (Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>> impl : implications) {
				List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> lhs = new LinkedList<>();
				List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> rhs = new LinkedList<>();
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> l : impl.second) {
					lhs.add(new Pair<>(inject(l.first), inject(l.second)));
				}
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> r : impl.third) {
					rhs.add(new Pair<>(inject(r.first), inject(r.second)));
				}
				implications0.add(new Triple<>(impl.first, lhs, rhs));
			}
			return new OplSig<S, Chc<C, X>, V>(fr, new HashMap<>(), sorts, symbols0, equations0,
					implications0);
		}

		@Override
		public JComponent display() {
			return display(true);
		}

		public JComponent display(Boolean b) {
			JTabbedPane ret = new JTabbedPane();

			CodeTextPanel p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			ret.add(p, "Text");

			try {
				JPanel pp = makeReducer(b);
				ret.add(pp, "KB");
				try {
					JPanel qq = makeHomSet();
					ret.add(qq, "Hom");
				} catch (Exception ex) {
					ex.printStackTrace();
					p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", "exception: "
							+ ex.getMessage());
					ret.add(p, "Hom");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", "exception: "
						+ ex.getMessage());
				ret.add(p, "KB");
			}

			return ret;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		JPanel makeReducer(boolean b) {
			OplToKB kb = getKB();
			JPanel ret = new JPanel(new GridLayout(1, 1));
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			ret.add(pane);

			JPanel top = new JPanel();

			JTextField src = new JTextField(16);
			JTextField dst = new JTextField(16);

			JButton go = new JButton("Reduce");
			go.addActionListener(x -> {
				try {
					OplTerm t = OplParser.parse_term(symbols, src.getText());
					dst.setText(kb.nf(OplToKB.convert(t)).toString());
				} catch (Exception ex) {
					dst.setText(ex.getMessage());
				}
			});

			top.add(new JLabel("Input term:"));
			top.add(src);
			top.add(go);
			top.add(new JLabel("Result:"));
			top.add(dst);

			CodeTextPanel bot = new CodeTextPanel(BorderFactory.createEtchedBorder(),
					"Re-write rules", strip(kb.printKB()));

			pane.add(top);
			pane.add(bot);

			if (b) {
				return ret;
			} else {
				return bot;
			}

		}

		@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
		JPanel makeHomSet() {
			OplToKB kb = getKB();
			JPanel ret = new JPanel(new GridLayout(1, 1));
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			ret.add(pane);

			JPanel top = new JPanel(new GridLayout(3, 1));
			JPanel p1 = new JPanel();
			JPanel p2 = new JPanel();
			JPanel p3 = new JPanel();

			JTextField src = new JTextField(32);
			JTextField dst = new JTextField(32);
			p1.add(new JLabel("source (sep by ,):"));
			p1.add(src);
			p2.add(new JLabel("target:"));
			p2.add(dst);

			CodeTextPanel bot = new CodeTextPanel(BorderFactory.createEtchedBorder(), "Result", "");

			JButton go = new JButton("Compute hom set");
			go.addActionListener(x -> {
				String[] l = src.getText().split(",");
				String r = dst.getText();
				List<String> l0 = new LinkedList<>();
				for (String j : l) {
					String j2 = j.trim();
					if (j2.length() > 0) {
						l0.add(j2);
					}
				}
				Runnable runnable = new Runnable() {
					public void run() {
						try {

							Collection<Pair<OplCtx<S, V>, OplTerm<C, V>>> z = kb.hom0(l0, r);
							List<String> u = z
									.stream()
									.map(o -> {
										return strip(o.first + " |- "
												+ OplToKB.convert(o.second).toString());
									}).collect(Collectors.toList());
							if (u.isEmpty()) {
								bot.setText("empty");
							} else {
								bot.setText(Util.sep(u, "\n\n"));
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							bot.setText(ex.getMessage());
						}
						// finished = true;
					}
				};
				Thread t = new Thread(runnable);
				try {
					t.start();
					t.join(NEWDEBUG.debug.opl.opl_hom_its);

					t.stop();
					if (bot.getText().equals("")) {
						bot.setText("Timeout");
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					throw new RuntimeException("Timout");
				}

			});
			p3.add(go);
			top.add(p1);
			top.add(p2);
			top.add(p3);

			pane.add(top);
			pane.add(bot);

			return ret;
		}

		public Map<C, Integer> prec;
		public Set<S> sorts;
		public Map<C, Pair<List<S>, S>> symbols;
		public List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations;
		public List<Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>>> implications;

		@Override
		public String toString() {
			String ret = "\tsorts\n";
			ret += "\t\t" + Util.sep(sorts, ", ") + ";\n";

			ret += "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (C k : symbols.keySet()) {
				Pair<List<S>, S> v = symbols.get(k);
				String s;
				if (v.first.isEmpty()) {
					s = strip(k.toString()) + " : " + v.second;

				} else {
					s = strip(k.toString()) + " : " + Util.sep(v.first, ", ") + " -> " + v.second;
				}
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist, ",\n\t\t") + ";\n";

			ret += "\tequations\n";
			List<String> elist = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> k : equations) {
				String z = k.first.vars0.size() == 0 ? "" : "forall ";
				String y = k.first.vars0.size() == 0 ? "" : ". ";
				String s = z + k.first + y + strip(k.second.toString()) + " = "
						+ strip(k.third.toString());
				elist.add(s);
			}
			ret += "\t\t" + Util.sep(elist, ",\n\t\t") + ";\n";

			if (!implications.isEmpty()) {
				ret += "\timplications\n";
				elist = new LinkedList<>();
				for (Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>> k : implications) {
					String z = k.first.vars0.size() == 0 ? "" : "forall ";
					String y = k.first.vars0.size() == 0 ? "" : ". ";
					List<String> xxx = new LinkedList<>();
					for (Pair<OplTerm<C, V>, OplTerm<C, V>> xx : k.second) {
						xxx.add(xx.first + " = " + xx.second);
					}
					List<String> yyy = new LinkedList<>();
					for (Pair<OplTerm<C, V>, OplTerm<C, V>> xx : k.third) {
						yyy.add(xx.first + " = " + xx.second);
					}

					String s = z + k.first + y + strip(Util.sep(xxx, ", ").toString()) + " -> "
							+ strip(Util.sep(yyy, ","));
					elist.add(s);
				}
				ret += "\t\t" + Util.sep(elist, ",\n\t\t") + ";\n";
			}

			return "theory {\n" + ret + "}" + "\n\nprec: " + prec;
		}

		public OplSig(Iterator<V> fr, Map<C, Integer> prec, Set<S> sorts,
				Map<C, Pair<List<S>, S>> symbols,
				List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations) {
			this(fr, prec, sorts, symbols, equations, new LinkedList<>());
		}

		public OplSig(
				Iterator<V> fr,
				Map<C, Integer> prec,
				Set<S> sorts,
				Map<C, Pair<List<S>, S>> symbols,
				List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations,
				List<Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>>> implications) {
			this.sorts = sorts;
			this.symbols = symbols;
			this.equations = equations;
			this.prec = prec;
			this.fr = fr;
			this.implications = implications;
			validate();
		}

		public Iterator<V> fr;

		void validate() {
			if (!NEWDEBUG.debug.opl.opl_horn && !implications.isEmpty()) {
				throw new DoNotIgnore("Implications in theories disabled in options menu.");
			}
			for (C k : symbols.keySet()) {
				Pair<List<S>, S> v = symbols.get(k);
				if (!sorts.contains(v.second)) {
					throw new DoNotIgnore("Bad codomain " + v.second + " for " + k + " in " + this);
				}
				for (S a : v.first) {
					if (!sorts.contains(a)) {
						throw new DoNotIgnore("Bad argument sort " + a + " for " + k);
					}
				}
			}
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq0 : equations) {
				Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq = new Triple<>(inf(eq0),
						eq0.second, eq0.third);
				eq0.first = eq.first;
				eq.first.validate(this);
				S t1 = eq.second.type(this, eq.first);
				S t2 = eq.third.type(this, eq.first);
				if (!t1.equals(t2)) {
					throw new DoNotIgnore("Domains do not agree in " + eq.second + " = " + eq.third
							+ ", are " + t1 + " and " + t2);
				}
			}
			for (Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>> eq0 : implications) {
				Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>> eq = new Triple<>(
						inf2(eq0), eq0.second, eq0.third);
				eq0.first = eq.first;
				eq.first.validate(this);
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> conjunct : eq.second) {
					S t1 = conjunct.first.type(this, eq.first);
					S t2 = conjunct.second.type(this, eq.first);
					if (!t1.equals(t2)) {
						throw new DoNotIgnore("Domains do not agree in " + conjunct.first + " = "
								+ conjunct.second + ", are " + t1 + " and " + t2);
					}
				}
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> conjunct : eq.third) {
					S t1 = conjunct.first.type(this, eq.first);
					S t2 = conjunct.second.type(this, eq.first);
					if (!t1.equals(t2)) {
						throw new DoNotIgnore("Domains do not agree in " + conjunct.first + " = "
								+ conjunct.second + ", are " + t1 + " and " + t2);
					}
				}
			}
		}

		private OplCtx<S, V> inf(Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq0) {
			try {
				KBExp<C, V> lhs = OplToKB.convert(eq0.second);
				KBExp<C, V> rhs = OplToKB.convert(eq0.third);
				Map<V, S> m = new HashMap<>(eq0.first.vars0);
				S l = lhs.typeInf(symbols, m);
				S r = rhs.typeInf(symbols, m);
				if (l == null && r == null) {
					throw new DoNotIgnore("Cannot infer sorts for " + lhs + " and " + rhs);
				}
				if (l == null && lhs.isVar && rhs != null) {
					m.put(lhs.getVar().var, r);
				}
				if (r == null && rhs.isVar && lhs != null) {
					m.put(rhs.getVar().var, l);
				}
				return new OplCtx<>(m);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new DoNotIgnore(ex.getMessage() + "\n\n in " + this);
			}
		}

		private OplCtx<S, V> inf2(
				Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>> eq0) {
			try {
				Map<V, S> m = new HashMap<>(eq0.first.vars0);
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> eq : eq0.second) {
					KBExp<C, V> lhs = OplToKB.convert(eq.first);
					KBExp<C, V> rhs = OplToKB.convert(eq.second);
					S l = lhs.typeInf(symbols, m);
					S r = rhs.typeInf(symbols, m);
					if (l == null && lhs.isVar && rhs != null) {
						m.put(lhs.getVar().var, r);
					}
					if (r == null && rhs.isVar && lhs != null) {
						m.put(rhs.getVar().var, l);
					}
				}
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> eq : eq0.third) {
					KBExp<C, V> lhs = OplToKB.convert(eq.first);
					KBExp<C, V> rhs = OplToKB.convert(eq.second);
					S l = lhs.typeInf(symbols, m);
					S r = rhs.typeInf(symbols, m);
					if (l == null && lhs.isVar && rhs != null) {
						m.put(lhs.getVar().var, r);
					}
					if (r == null && rhs.isVar && lhs != null) {
						m.put(rhs.getVar().var, l);
					}
				}
				for (V v : eq0.first.names()) {
					S c = m.get(v);
					if (c == null) {
						throw new DoNotIgnore("Cannot infer sort for " + v);
					}
				}
				return new OplCtx<>(m);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new DoNotIgnore(ex.getMessage() + "\n\n in " + this);
			}
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public Pair<List<S>, S> getSymbol(C var) {
			Pair<List<S>, S> ret = symbols.get(var);
			if (ret == null) {
				throw new DoNotIgnore("Unknown symbol " + var);
			}
			return ret;
		}

		public OplSetInst<S, C, OplTerm<C, V>> saturate(String name) {

			Map<S, Set<OplTerm<C, V>>> sorts0 = getKB().doHoms();

			Map<C, Map<List<OplTerm<C, V>>, OplTerm<C, V>>> symbols0 = new HashMap<>();
			for (C f : symbols.keySet()) {
				Pair<List<S>, S> ty = symbols.get(f);
				Map<Integer, List<OplTerm<C, V>>> args = new HashMap<>();
				int i = 0;
				for (S t : ty.first) {
					args.put(i++, new LinkedList<>(sorts0.get(t)));
				}
				List<LinkedHashMap<Integer, OplTerm<C, V>>> cands = FinSet.homomorphs(args);
				Map<List<OplTerm<C, V>>, OplTerm<C, V>> out = new HashMap<>();
				for (LinkedHashMap<Integer, OplTerm<C, V>> cand : cands) {
					List<OplTerm<C, V>> actual = new LinkedList<>();
					for (int j = 0; j < i; j++) {
						actual.add(cand.get(j));
					}
					OplTerm<C, V> to_red = new OplTerm<>(f, actual);
					OplTerm<C, V> red = OplToKB.convert(kb.nf(OplToKB.convert(to_red)));
					out.put(actual, red);
				}
				symbols0.put(f, out);
			}

			OplSetInst<S, C, OplTerm<C, V>> ret = new OplSetInst<>(sorts0, symbols0, name);
			ret.validate(this);
			return ret;
		}

		public int largestPrec() {
			int ret = 0;
			for (Integer k : prec.values()) {
				if (k.intValue() > ret) {
					ret = k.intValue();
				}
			}
			return ret;
		}

	}

	public static class OplPres<S, C, V, X> extends OplExp {

		public String S;
		public Map<X, Integer> prec;
		public Map<X, S> gens;
		public List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations;
		public OplSig<S, C, V> sig;
		public OplSig<S, Chc<C, X>, V> toSig;

		@SuppressWarnings("unchecked")
		private static <S, C, X, V> OplTerm<Chc<C, X>, V> conv(Map<X, S> gens, OplTerm<Object, V> e) {
			if (e.var != null) {
				return new OplTerm<>(e.var);
			}
			List<OplTerm<Chc<C, X>, V>> args0 = new LinkedList<>();
			for (OplTerm<Object, V> arg : e.args) {
				args0.add(conv(gens, arg));
			}
			if (gens.get(e.head) != null) {
				return new OplTerm<>(Chc.inRight((X) e.head), args0);
			} else {
				return new OplTerm<>(Chc.inLeft((C) e.head), args0);
			}
		}

		public static <S, C, V, X> OplPres<S, C, V, X> OplPres0(Map<X, Integer> prec, String S,
				OplSig<S, C, V> sig, Map<X, S> gens,
				List<Pair<OplTerm<Object, V>, OplTerm<Object, V>>> equations) {

			List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> eqs = new LinkedList<>();
			for (Pair<OplTerm<Object, V>, OplTerm<Object, V>> eq : equations) {
				eqs.add(new Pair<>(conv(gens, eq.first), conv(gens, eq.second)));
			}

			return new OplPres<>(prec, S, sig, gens, eqs);
		}

		public OplSig<S, Chc<C, X>, V> toSig() {
			if (toSig != null) {
				return toSig;
			}
			OplSig<S, Chc<C, X>, V> sig0 = sig.inject();

			Map<Chc<C, X>, Pair<List<S>, S>> symbols0 = new HashMap<>(sig0.symbols);
			for (X k : gens.keySet()) {
				symbols0.put(Chc.inRight(k), new Pair<>(new LinkedList<>(), gens.get(k)));
			}

			List<Triple<OplCtx<S, V>, OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations0 = new LinkedList<>(
					sig0.equations);
			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> eq : equations) {
				equations0.add(new Triple<>(new OplCtx<>(), eq.first, eq.second));
			}

			Map<Chc<C, X>, Integer> m = new HashMap<>();
			for (C c : sig.symbols.keySet()) {
				Integer i = sig.prec.get(c);
				if (i != null) {
					m.put(Chc.inLeft(c), i);
				}
			}
			for (X x : gens.keySet()) {
				Integer i = prec.get(x);
				if (i != null) {
					m.put(Chc.inRight(x), i);
				}
			}

			toSig = new OplSig<S, Chc<C, X>, V>(sig.fr, m, sig.sorts, symbols0, equations0,
					sig0.implications);
			return toSig;
		}

		@Override
		public JComponent display() {
			JTabbedPane ret = new JTabbedPane();

			CodeTextPanel p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			ret.add(p, "Text");

			CodeTextPanel q = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toSig()
					.toString());
			ret.add(q, "Full Theory");

			return ret;
		}

		public OplPres(Map<X, Integer> prec, String S, OplSig<S, C, V> sig, Map<X, S> gens,
				List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations) {
			this.S = S;
			this.gens = gens;
			this.equations = equations;
			this.sig = sig;
			this.prec = prec;
			//toSig();
		}

		@Override
		public String toString() {
			String ret = "";
			ret += "\tgenerators\n";
			List<String> slist = new LinkedList<>();
			for (X k : gens.keySet()) {
				String v = gens.get(k).toString();
				String s = strip(k.toString()) + " : " + v;
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist, ",\n\t\t") + ";\n";

			ret += "\tequations\n";
			List<String> elist = new LinkedList<>();
			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> k : equations) {
				String s = strip(k.first + " = " + k.second);
				elist.add(s);
			}

			ret += "\t\t" + Util.sep(elist, ",\n\t\t") + ";\n";

			return "presentation {\n" + ret + "} : " + S; // + "\n\nprec: " +
															// prec + "\n\n" +
															// sig.prec;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	

	public static class OplMapping<S1, C1, V, S2, C2> extends OplExp {
		Map<S1, S2> sorts;
		Map<C1, Pair<OplCtx<S2, V>, OplTerm<C2, V>>> symbols;

		String src0, dst0;
		OplSig<S1, C1, V> src;
		OplSig<S2, C2, V> dst;

		public JComponent display() {
			JTabbedPane jtp = new JTabbedPane();

			JComponent text = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			jtp.addTab("Text", text);

			JComponent tables = makeTables();
			jtp.addTab("Tables", tables);

			return jtp;
		}

		private JComponent makeTables() {
			List<JComponent> list = new LinkedList<>();

			List<Object[]> rs = new LinkedList<>();
			for (S1 n : sorts.keySet()) {
				S2 f = sorts.get(n);
				rs.add(new Object[] { n, f });
			}
			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), "Sorts",
					rs.toArray(new Object[][] {}), new Object[] { src0 + " (in)", dst0 + " (out)" }));

			List<Object[]> rows = new LinkedList<>();
			for (C1 n : symbols.keySet()) {
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> f = symbols.get(n);
				Object[] row = new Object[] { strip(n.toString()),
						strip("forall " + f.first + ". " + f.second) };
				rows.add(row);
			}
			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), "Symbols",
					rows.toArray(new Object[][] {}),
					new Object[] { src0 + " (in)", dst0 + " (out)" }));

			return Util.makeGrid(list);
		}

		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (S1 k : sorts.keySet()) {
				S2 v = sorts.get(k);
				sortsX.add(k + " -> " + v);
			}
			ret += "\t\t" + Util.sep(sortsX, ",\n\t\t") + ";\n";

			ret += "\tsymbols\n";
			List<String> symbolsX = new LinkedList<>();
			for (C1 k : symbols.keySet()) {
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> v = symbols.get(k);
				String z = v.first.vars0.size() == 0 ? "" : "forall ";
				String y = v.first.vars0.size() == 0 ? "" : " . ";
				symbolsX.add(strip(k.toString()) + " -> " + z + v.first + y + v.second.toString());
			}
			ret += "\t\t" + Util.sep(symbolsX, ",\n\t\t") + ";\n";

			return "mapping {\n" + ret + "} : " + src0 + " -> " + dst0;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public void validate(OplSig<S1, C1, V> src, OplSig<S2, C2, V> dst) {
			this.src = src;
			this.dst = dst;
			for (S1 k : sorts.keySet()) {
				if (!src.sorts.contains(k)) {
					throw new RuntimeException("Extra sort: " + k);
				}
				S2 v = sorts.get(k);
				if (!dst.sorts.contains(v)) {
					throw new RuntimeException("Bad target sort " + v + " from " + k);
				}
			}
			for (S1 k : src.sorts) {
				if (!sorts.keySet().contains(k)) {
					throw new RuntimeException("Missing sort: " + k);
				}
			}
			for (C1 k : src.symbols.keySet()) {
				if (!symbols.keySet().contains(k)) {
					throw new RuntimeException("missing symbol: " + k);
				}
			}
			for (C1 k : symbols.keySet()) {
				if (!src.symbols.containsKey(k)) {
					throw new RuntimeException("Extra symbol: " + k);
				}
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> v = symbols.get(k);
				v.second = replaceVarsByConsts(v.first, v.second);
				v.first = inf(v);
				S2 t = v.second.type(dst, v.first);
				if (t == null) {
					throw new RuntimeException("Cannot type " + v.second + " in context ["
							+ v.first + "]. ");
				}

				if (!t.equals(sorts.get(src.symbols.get(k).second))) {
					throw new RuntimeException("Symbol " + k + " returns a "
							+ src.symbols.get(k).second + " but transforms to " + t);
				}
				List<S2> trans_t = src.symbols.get(k).first.stream().map(x -> sorts.get(x))
						.collect(Collectors.toList());
				if (!v.first.values().equals(trans_t)) {
					throw new RuntimeException("Symbol " + k + " inputs a " + v.first.values()
							+ " but transforms to " + trans_t + " in \n\n " + this + "\n\nvalidate ctx " + v.first + "\nvalidate values " + v.first.values());
				}
			}

			for (Triple<OplCtx<S1, V>, OplTerm<C1, V>, OplTerm<C1, V>> eq : src.equations) {
				OplTerm<C2, V> l = subst(eq.second);
				OplTerm<C2, V> r = subst(eq.second);
				if (NEWDEBUG.debug.opl.opl_validate) {
					KBExp<C2, V> l0 = dst.getKB().nf(OplToKB.convert(l));
					KBExp<C2, V> r0 = dst.getKB().nf(OplToKB.convert(r));
					if (!l0.equals(r0)) {
						throw new RuntimeException("Eq not preserved: " + l + " = " + r
								+ " transforms to " + l0 + " = " + r0);
					}
				}
			}

			// TODO: cannot actually check mapping validity for horn clauses

		}

		private OplCtx<S2, V> inf(Pair<OplCtx<S2, V>, OplTerm<C2, V>> eq0) {
			KBExp<C2, V> lhs = OplToKB.convert(eq0.second);
			Map<V, S2> m = new LinkedHashMap<>(eq0.first.vars0);
			lhs.typeInf(dst.symbols, m);
			return new OplCtx<>(m);
		}

		private OplTerm<C2, V> replaceVarsByConsts(OplCtx<S2, V> g, OplTerm<C2, V> e) {
			if (e.var != null) {
				if (dst.symbols.containsKey(e.var)) {
					if (g.vars0.containsKey(e.var)) {
						throw new RuntimeException("Attempt to shadow " + e.var);
					}
					@SuppressWarnings("unchecked")
					C2 c2 = (C2) e.var;
					return new OplTerm<C2, V>(c2, new LinkedList<>());
				}
				return e;
			}
			return new OplTerm<C2, V>(e.head, e.args.stream().map(x -> replaceVarsByConsts(g, x))
					.collect(Collectors.toList()));
		}

		public OplMapping(Map<S1, S2> sorts, Map<C1, Pair<OplCtx<S2, V>, OplTerm<C2, V>>> symbols,
				String src0, String dst0) {
			this.sorts = sorts;
			this.symbols = symbols;
			this.src0 = src0;
			this.dst0 = dst0;
		}

		public <X, Y> OplPresTrans<S2, C2, V, X, Y> sigma(OplPresTrans<S1, C1, V, X, Y> h) {
			OplPres<S1, C1, V, X> I = h.src;

			OplPres<S2, C2, V, X> I0 = sigma(h.src);
			OplPres<S2, C2, V, Y> J0 = sigma(h.dst);

			Map<S2, Map<X, OplTerm<Chc<C2, Y>, V>>> map = new HashMap<>();
			for (S2 s : I0.sig.sorts) {
				map.put(s, new HashMap<>());
			}
			for (S1 s : I.sig.sorts) {
				for (X x : I.gens.keySet()) {
					S1 t = I.gens.get(x);
					if (!s.equals(t)) {
						continue;
					}
					map.get(sorts.get(s)).put(x, sigma(h.map.get(s).get(x)));
				}
			}

			// validates
			OplPresTrans<S2, C2, V, X, Y> ret = new OplPresTrans<S2, C2, V, X, Y>(map, "?", "?",
					I0, J0);
			return ret;
		}

		public <X> OplSetTrans<S1, C1, X> delta(OplSetTrans<S2, C2, X> h) {
			if (!h.src0.sig.equals(dst0)) {
				throw new RuntimeException("Source of transform, " + h.src0
						+ " does not have theory " + dst0);
			}
			if (!h.dst0.sig.equals(dst0)) {
				throw new RuntimeException("Target of transform, " + h.src0
						+ " does not have theory " + dst0);
			}

			OplSetInst<S1, C1, X> srcX = delta(h.src0);
			OplSetInst<S1, C1, X> dstX = delta(h.dst0);

			Map<S1, Map<X, X>> sortsX = new HashMap<>();
			for (S1 s : src.sorts) {
				Map<X, X> m = new HashMap<>();
				for (X v : srcX.sorts.get(s)) {
					m.put(v, h.sorts.get(sorts.get(s)).get(v));
				}
				sortsX.put(s, m);
			}

			OplSetTrans<S1, C1, X> ret = new OplSetTrans<>(sortsX, "?", "?");
			ret.validate(src, srcX, dstX);
			return ret;
		}

		public <X> OplPres<S2, C2, V, X> sigma(OplPres<S1, C1, V, X> I) {
			if (!src.equals(I.sig)) {
				throw new RuntimeException("Source of mapping " + src0 + " does not match " + I.S);
			}

			Map<X, S2> sym = new HashMap<>();
			for (X c : I.gens.keySet()) {
				S1 t = I.gens.get(c);
				sym.put(c, sorts.get(t));
			}

			List<Pair<OplTerm<Chc<C2, X>, V>, OplTerm<Chc<C2, X>, V>>> eqs = new LinkedList<>();
			for (Pair<OplTerm<Chc<C1, X>, V>, OplTerm<Chc<C1, X>, V>> eq : I.equations) {
				eqs.add(new Pair<>(sigma(eq.first), sigma(eq.second)));
			}

			OplPres<S2, C2, V, X> ret = new OplPres<S2, C2, V, X>(I.prec, dst0, dst, sym, eqs);
			return ret;
		}

		/*
		 * private OplCtx<S2, V> sigma(OplCtx<S1, V> t) { List<Pair<V, S2>> l =
		 * t.values2().stream().map(x -> { return new Pair<>(x.first,
		 * sorts.get(x.second)); }).collect(Collectors.toList()); return new
		 * OplCtx<>(l); }
		 */

		private OplTerm<C2, V> subst(OplTerm<C1, V> t) {
			if (t.var != null) {
				return new OplTerm<>(t.var);
			} else {
				List<OplTerm<C2, V>> l = new LinkedList<>();
				for (OplTerm<C1, V> a : t.args) {
					l.add(subst(a));
				}

				Pair<OplCtx<S2, V>, OplTerm<C2, V>> h = symbols.get(t.head);
				if (h == null) {
					throw new RuntimeException();
				}

				Map<V, OplTerm<C2, V>> s = new HashMap<>();
				List<Pair<V, S2>> r = h.first.values2();
				int i = 0;
				for (Pair<V, S2> p : r) {
					s.put(p.first, l.get(i++));
				}

				OplTerm<C2, V> ret = h.second;
				return ret.subst(s);
			}
		}

		private <X> OplTerm<Chc<C2, X>, V> sigma(OplTerm<Chc<C1, X>, V> t) {
			if (t.var != null) {
				return new OplTerm<>(t.var);
			} else {
				List<OplTerm<Chc<C2, X>, V>> l = new LinkedList<>();
				for (OplTerm<Chc<C1, X>, V> a : t.args) {
					l.add(sigma(a));
				}

				if (!t.head.left) {
					return new OplTerm<>(Chc.inRight(t.head.r), l);
				}

				Pair<OplCtx<S2, V>, OplTerm<C2, V>> h = symbols.get(t.head.l);

				Map<V, OplTerm<Chc<C2, X>, V>> s = new HashMap<>();
				List<Pair<V, S2>> r = h.first.values2();
				int i = 0;
				for (Pair<V, S2> p : r) {
					s.put(p.first, l.get(i++));
				}

				OplTerm<Chc<C2, X>, V> ret = OplSig.inject(h.second);
				return ret.subst(s);
			}
		}

		public <X> OplSetInst<S1, C1, X> delta(OplSetInst<S2, C2, X> J) {
			if (!dst0.equals(J.sig)) {
				throw new RuntimeException("Target of mapping " + dst + " does not match " + J.sig);
			}
			Map<S1, Set<X>> sortsX = new HashMap<>();
			for (S1 s : src.sorts) {
				S2 s0 = sorts.get(s);
				sortsX.put(s, J.sorts.get(s0));
			}

			Map<C1, Map<List<X>, X>> symbolsX = new HashMap<>();
			for (C1 n : src.symbols.keySet()) {
				Pair<List<S1>, S1> f_t = src.symbols.get(n);
				List<Set<X>> f_a = f_t.first.stream().map(x -> sortsX.get(x))
						.collect(Collectors.toList());
				List<List<X>> inputs = Util.prod(f_a);
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> f = symbols.get(n);
				Map<List<X>, X> map = new HashMap<>();
				for (List<X> input : inputs) {
					X output = f.second.eval(dst, J, f.first.makeEnv(input));
					if (map.containsKey(input)) {
						throw new RuntimeException();
					}
					map.put(input, output);
				}
				symbolsX.put(n, map);
			}

			OplSetInst<S1, C1, X> I = new OplSetInst<>(sortsX, symbolsX, src0);
			I.validate(src);
			return I;
		}

	}

	public static class OplDelta extends OplExp {
		String F, I;

		public OplDelta(String f, String i) {
			F = f;
			I = i;
		}

		@Override
		public String toString() {
			return "delta " + F + " " + I;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class OplSigma extends OplExp {
		String F, I;

		public OplSigma(String f, String i) {
			F = f;
			I = i;
		}

		@Override
		public String toString() {
			return "sigma " + F + " " + I;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class OplEval extends OplExp {
		String I;
		OplTerm e;

		public OplEval(String i, OplTerm e) {
			I = i;
			this.e = e;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class OplSetInst<S, C, X> extends OplExp {

		public int size() {
			int ret = 0;
			for (S s : sorts.keySet()) {
				ret += sorts.get(s).size();
			}
			return ret;
		}

		public Map<S, Set<X>> sorts;
		String sig;
		public Map<C, Map<List<X>, X>> symbols;
		public OplSig<S, C, ?> sig0;

		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (S k : sorts.keySet()) {
				sortsX.add(k
						+ " -> {"
						+ Util.sep(
								sorts.get(k).stream().map(x -> strip(x.toString()))
										.collect(Collectors.toList()), ", ") + "}");
			}
			ret += "\t\t" + Util.sep(sortsX, ", ") + ";\n";

			ret += "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (C k : symbols.keySet()) {
				Map<List<X>, X> v = symbols.get(k);
				List<String> u = new LinkedList<>();
				for (List<X> i : v.keySet()) {
					X j = v.get(i);
					u.add("(("
							+ Util.sep(
									i.stream().map(x -> strip(x.toString()))
											.collect(Collectors.toList()), ",") + "), "
							+ strip(j.toString()) + ")");
				}
				String s = k + " -> {" + Util.sep(u, ", ") + "}";
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist, ",\n\t\t") + ";\n";

			return "model {\n" + ret + "} : " + sig;
		}

		public JComponent display() {
			JTabbedPane jtp = new JTabbedPane();

			JComponent text = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			jtp.addTab("Text", text);

			JComponent tables = makeTables(new HashSet<>());
			jtp.addTab("Tables", tables);

			return jtp;
		}

		public JComponent makeTables(Set<S> skip) {
			List<JComponent> list = new LinkedList<>();

			Map<String, JComponent> all = new HashMap<>();

			List<C> keys2 = new LinkedList<>(symbols.keySet());

			Comparator<Object> comp = new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					return o1.toString().compareTo(o2.toString());
				}
			};

			for (S n : sorts.keySet()) {
				List<S> t0 = Collections.singletonList(n);
				List<C> set = new LinkedList<>();
				for (C f : symbols.keySet()) {
					Pair<List<S>, S> t = sig0.symbols.get(f);
					if (t.first.equals(t0)) {
						set.add(f);
						keys2.remove(f);
					}
				}
				set.sort(comp);

				List<Object[]> rows = new LinkedList<>();
				List<String> cols = new LinkedList<>();

				for (X arg : sorts.get(n)) {
					List<Object> row = new LinkedList<>();
					cols = new LinkedList<>();
					cols.add(n.toString());
					row.add(arg);
					for (C f : set) {
						row.add(symbols.get(f).get(Collections.singletonList(arg)));
						cols.add(f.toString());
					}
					rows.add(row.toArray(new Object[] {}));
				}
				all.put(n.toString(),
						JSWrapper.makePrettyTables(BorderFactory.createEmptyBorder(), n + " (" + rows.size() + ")",
								rows.toArray(new Object[][] {}), cols.toArray(new String[] {})));
			}

			for (C n : keys2) {
				if (sig0.symbols.get(n).first.size() == 0) {
					continue;
				}
				Map<List<X>, X> f = symbols.get(n);
				List<Object[]> rows = new LinkedList<>();
				for (List<X> arg : f.keySet()) {
					@SuppressWarnings("unchecked")
					List<Object> argX = (List<Object>) arg;
					argX.add(f.get(arg));
					Object[] row = argX.toArray(new Object[] {});
					rows.add(row);
				}
				@SuppressWarnings("unchecked")
				List<String> l = new LinkedList<String>((List<String>) sig0.symbols.get(n).first);
				l.add(sig0.symbols.get(n).second.toString());
				all.put(n.toString(),
						JSWrapper.makePrettyTables(BorderFactory.createEmptyBorder(), n + " (" + rows.size() + ")",
								rows.toArray(new Object[][] {}), l.toArray(new String[] {})));
			}
			List<String> xxx = new LinkedList<>(all.keySet());
			xxx.sort(comp);
			for (String n : xxx) {
				if (skip.contains(n) && NEWDEBUG.debug.opl.opl_suppress_dom) {
					continue;
				}
				list.add(all.get(n));
			}
			return Util.makeGrid(list);
		}

		public <V> void validate(OplSig<S, C, V> sig) {
			for (S s : sig.sorts) {
				if (!sorts.containsKey(s)) {
					throw new RuntimeException("No data for " + s + " in " + this);
				}
			}
			for (S s : sorts.keySet()) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Extra data for " + s);
				}
			}
			for (C f : sig.symbols.keySet()) {
				if (!symbols.containsKey(f)) {
					throw new RuntimeException("No data for " + f);
				}
				List<S> arg_ts = sig.symbols.get(f).first;
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> sorts.get(x))
						.collect(Collectors.toList());
				List<List<X>> args = Util.prod(arg_ds);
				for (List<X> arg : args) {
					X at = symbols.get(f).get(arg);
					if (at == null) {
						throw new RuntimeException("Missing on argument " + arg + " in " + f);
					}
					if (!sorts.get(sig.symbols.get(f).second).contains(at)) {
						throw new RuntimeException("In " + f + ", return value " + at
								+ " not in correct sort " + sorts.get(sig.symbols.get(f).second));
					}
				}
				for (List<X> gt : symbols.get(f).keySet()) {
					if (!args.contains(gt)) {
						throw new RuntimeException("Superfluous arg " + gt + " in " + f + " notin "
								+ args);
					}
				}
			}
			for (C f : symbols.keySet()) {
				if (!sig.symbols.keySet().contains(f)) {
					throw new RuntimeException("Extra data for " + f);
				}
			}
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				List<S> arg_ts = eq.first.values();
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> sorts.get(x))
						.collect(Collectors.toList());
				List<List<X>> args = Util.prod(arg_ds);
				for (List<X> env : args) {
					OplCtx<X, V> env2 = eq.first.makeEnv(env);
					X x = eq.second.eval(sig, this, env2);
					X y = eq.third.eval(sig, this, env2);
					if (!x.equals(y)) {
						throw new RuntimeException("Equation " + eq.second + " = " + eq.third
								+ " not respected on " + env + ", lhs=" + x + " and rhs=" + y);
					}
				}
			}
			outer: for (Triple<OplCtx<S, V>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>, List<Pair<OplTerm<C, V>, OplTerm<C, V>>>> eq : sig.implications) {
				List<S> arg_ts = eq.first.values();
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> sorts.get(x))
						.collect(Collectors.toList());
				List<List<X>> args = Util.prod(arg_ds);
				for (List<X> env : args) {
					OplCtx<X, V> env2 = eq.first.makeEnv(env);
					for (Pair<OplTerm<C, V>, OplTerm<C, V>> conjunct : eq.second) {
						X x = conjunct.first.eval(sig, this, env2);
						X y = conjunct.second.eval(sig, this, env2);
						if (!x.equals(y)) {
							continue outer;
						}
					}
					for (Pair<OplTerm<C, V>, OplTerm<C, V>> conjunct : eq.third) {
						X x = conjunct.first.eval(sig, this, env2);
						X y = conjunct.second.eval(sig, this, env2);
						if (!x.equals(y)) {
							throw new RuntimeException("Implication " + Util.sep(eq.second, ",")
									+ " -> " + conjunct.first + "=" + conjunct.second
									+ " not respected on " + env + ", lhs=" + x + " and rhs=" + y);
						}
					}
				}
			}

			sig0 = sig;
		}

		public OplSetInst(Map<S, Set<X>> sorts, Map<C, Map<List<X>, X>> symbols, String sig) {
			this.sorts = sorts;
			this.sig = sig;
			this.symbols = symbols;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public Map<List<X>, X> getSymbol(C head) {
			Map<List<X>, X> ret = symbols.get(head);
			if (ret == null) {
				throw new RuntimeException("Unknown symbol " + head);
			}
			return ret;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sorts == null) ? 0 : sorts.hashCode());
			result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
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
			OplSetInst<?, ?, ?> other = (OplSetInst<?, ?, ?>) obj;
			if (sorts == null) {
				if (other.sorts != null)
					return false;
			} else if (!sorts.equals(other.sorts))
				return false;
			if (symbols == null) {
				if (other.symbols != null)
					return false;
			} else if (!symbols.equals(other.symbols))
				return false;
			return true;
		}

	}

	public static class OplJavaInst extends OplExp {

		public Map<String, String> defs;

		public ScriptEngine engine;

		String sig;

		@SuppressWarnings("rawtypes")
		OplSig sig0;

		OplEnvironment ENV;

		public OplJavaInst(Map<String, String> defs, String sig) {
			this.defs = defs;
			this.sig = sig;
		}

		public void validate(@SuppressWarnings("rawtypes") OplSig sig, OplEnvironment ENV) {
			this.ENV = ENV;
			sig0 = sig;
			for (String k : defs.keySet()) {
				if (k.equals("_preamble") || k.equals("_compose")) {
					continue;
				}
				if (!sig.symbols.containsKey(k)) {
					throw new RuntimeException("Extra symbol " + k);
				}
			}
			for (Object k : sig.symbols.keySet()) {
				if (!defs.keySet().contains(k)) {
					throw new RuntimeException("Missing symbol " + k);
				}
			}

			engine = new ScriptEngineManager().getEngineByName("nashorn");
			for (String key : ENV.keys()) {
				engine.put(key, ENV.get(key));
			}
			String ret = "";
			if (defs.containsKey("_preamble")) {
				ret += defs.get("_preamble") + "\n\n";
			}
			for (String k : defs.keySet()) {
				if (k.equals("_preamble")) {
					continue;
				}
				String v = defs.get(k);
				ret += "function " + k + "(input) { " + v + " }\n\n";
			}

			try {
				engine.eval(ret);
			} catch (ScriptException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}

		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			String ret = "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (String k : defs.keySet()) {
				String s = k + " -> \"" + defs.get(k) + "\"";
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist, ",\n\t\t") + ";\n";

			return "javascript {\n" + ret + "} : " + sig;
		}

	}

	// TODO: pretty print
	public static class OplPresTrans<S, C, V, X, Y> extends OplExp {
		Map<S, Map<X, OplTerm<Chc<C, Y>, V>>> map = new HashMap<>();
		Map<S, Map<X, OplTerm<Object, V>>> pre_map;
		String src0, dst0;

		OplPres<S, C, V, X> src;
		OplPres<S, C, V, Y> dst;

		OplInst<S, C, V, X> src1;
		OplInst<S, C, V, Y> dst1;
		
		public OplTerm<Chc<C, Y>, V> apply(OplTerm<Chc<C, X>, V> e) {
			if (e.var != null) {
				throw new RuntimeException();
			}
			List<OplTerm<Chc<C, Y>, V>> args0 = new LinkedList<>();
			for (OplTerm<Chc<C, X>, V> arg : e.args) {
				args0.add(apply(arg));
			}
			if (e.head.left) {
				return new OplTerm<Chc<C, Y>, V>(Chc.inLeft(e.head.l), args0);
			} else {
				if (!e.args.isEmpty()) {
					throw new RuntimeException();
				}
				OplTerm<Chc<C, Y>, V> e0 = map.get(e.type(src.toSig(), new OplCtx<>())).get(e.head.r);
				if (e0 == null) {
					throw new RuntimeException();
				}
				return e0;
			}
		}
		
		@Override
		public String toString() {
			return "OplPresTranGens [map=" + map + ", pre_map=" + pre_map + ", src0=" + src0
					+ ", dst0=" + dst0 + ", src=" + src + ", dst=" + dst + ", mapping=" + mapping
					+ "]";
		}

		@Override
		public JComponent display() {
			return toMapping().display();
		}

		private OplTerm<Chc<C, Y>, V> conv(OplTerm<Object, V> e) {
			if (e.var != null) {
				return new OplTerm<>(e.var);
			}
			List<OplTerm<Chc<C, Y>, V>> args0 = new LinkedList<>();
			for (OplTerm<Object, V> arg : e.args) {
				args0.add(conv(arg));
			}
			if (dst.gens.get(e.head) != null) {
				@SuppressWarnings("unchecked")
				Y y = (Y) e.head;
				return new OplTerm<>(Chc.inRight(y), args0);
			} else {
				@SuppressWarnings("unchecked")
				C c = (C) e.head;
				return new OplTerm<>(Chc.inLeft(c), args0);
			}
		}

		OplMapping<S, Chc<C, X>, V, S, Chc<C, Y>> mapping;

		public OplMapping<S, Chc<C, X>, V, S, Chc<C, Y>> toMapping() {
			if (mapping != null) {
				return mapping;
			}
			Map<S, S> sorts = Utils.id(src.sig.sorts);

			Map<Chc<C, X>, Pair<OplCtx<S, V>, OplTerm<Chc<C, Y>, V>>> symbols = new HashMap<>();
			for (C c : src.sig.symbols.keySet()) {
				Pair<List<S>, S> t = src.sig.symbols.get(c);

				List<Pair<V, S>> l = new LinkedList<>();
				List<OplTerm<Chc<C, Y>, V>> r = new LinkedList<>();
				for (S s : t.first) {
					V v = src.sig.fr.next();
					l.add(new Pair<>(v, s));
					r.add(new OplTerm<>(v));
				}

				OplCtx<S, V> g = new OplCtx<>(l);
				OplTerm<Chc<C, Y>, V> e = new OplTerm<>(Chc.inLeft(c), r);

				Pair<OplCtx<S, V>, OplTerm<Chc<C, Y>, V>> p = new Pair<>(g, e);
				symbols.put(Chc.inLeft(c), p);
			}

			for (X x : src.gens.keySet()) {
				S s = src.gens.get(x);
				if (!map.containsKey(s)) {
					throw new RuntimeException(s + " not a key in " + map);
				}
				if (!map.get(s).containsKey(x)) {
					throw new RuntimeException(x + " not a key in " + map.get(s));
				}
				OplTerm<Chc<C, Y>, V> y = map.get(s).get(x);
				symbols.put(Chc.inRight(x), new Pair<>(new OplCtx<>(), y));
			}

			mapping = new OplMapping<S, Chc<C, X>, V, S, Chc<C, Y>>(sorts, symbols, src0, dst0);
			mapping.validate(src.toSig(), dst.toSig());
			return mapping;
		}

		public void validateNotReally(OplInst<S, C, V, X> src, OplInst<S, C, V, Y> dst) {
			if (pre_map == null) {
				return; //throw exn?
			}
			this.src1 = src;
			this.dst1 = dst;
			for (S s : src.P.sig.sorts) {
				if (!pre_map.containsKey(s)) {
					pre_map.put(s, new HashMap<>());
				}
			}
			/*for (X c : src.P.gens.keySet()) {
				S s = src.P.gens.get(c);
				
				if (pre_map.get(j.second).containsKey(c)) {
					throw new RuntimeException("should not map " + c);
				}
				pre_map.get(j.second).put((X)c, new OplTerm(c, new LinkedList()));
			}
				*/		
			validateNotReally(src.P, dst.P); 			
		}
		
		public void validateNotReally(OplPres<S, C, V, X> src, OplPres<S, C, V, Y> dst) {
			if (pre_map == null) {
				return;
			}
			this.src = src;
			this.dst = dst;

			if (!src.sig.equals(dst.sig)) {
				throw new RuntimeException("Signatures do not match");
			}

			for (S s : src.sig.sorts) {
				if (!pre_map.containsKey(s)) {
					throw new RuntimeException("Missing sort: " + s);
				}
			}
			for (S s : pre_map.keySet()) {
				if (!src.sig.sorts.contains(s)) {
					throw new RuntimeException("Extra sort: " + s);
				}
			}

			for (S s : src.sig.sorts) {
				Map<X, OplTerm<Chc<C, Y>, V>> m = new HashMap<>();
				for (X x : pre_map.get(s).keySet()) {
					OplTerm<Object, V> n = pre_map.get(s).get(x);
					OplTerm<Chc<C, Y>, V> u = conv(n);
					if (!src.gens.keySet().contains(x)) {
						throw new RuntimeException("Not a generator: " + x);
					}
					u.type(dst.toSig(), new OplCtx<>());
					m.put(x, u);
				}
				map.put(s, m);
			}

			toMapping();

			

		}

		public OplPresTrans(Map<S, Map<X, OplTerm<Object, V>>> m, String src0, String dst0) {
			this.pre_map = m;
			this.src0 = src0;
			this.dst0 = dst0;
		}

		// validates
		public OplPresTrans(Map<S, Map<X, OplTerm<Chc<C, Y>, V>>> map, String src0, String dst0,
				OplPres<S, C, V, X> src, OplPres<S, C, V, Y> dst) {
			this.map = map;
			this.src0 = src0;
			this.dst0 = dst0;
			this.src = src;
			this.dst = dst;
			toMapping();
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class OplSetTrans<S, C, X> extends OplExp {
		Map<S, Map<X, X>> sorts;
		String src, dst;

		OplSig<S, C, ?> sig;
		OplSetInst<S, C, X> src0, dst0;

		public JComponent display() {
			JTabbedPane jtp = new JTabbedPane();

			JComponent text = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			jtp.addTab("Text", text);

			JComponent tables = makeTables();
			jtp.addTab("Tables", tables);

			return jtp;
		}

		private JComponent makeTables() {
			List<JComponent> list = new LinkedList<>();
			for (S n : sorts.keySet()) {
				Map<X, X> f = sorts.get(n);
				List<Object[]> rows = new LinkedList<>();
				for (X arg : f.keySet()) {
					Object[] row = new Object[2];
					row[0] = arg;
					row[1] = f.get(arg);
					rows.add(row);
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), n.toString() + " ("
						+ rows.size() + ")", rows.toArray(new Object[][] {}), new Object[] {
						src + " (in)", dst + " (out)" }));
			}

			return Util.makeGrid(list);
		}

		public OplSetTrans(Map<S, Map<X, X>> sorts, String src, String dst) {
			this.sorts = sorts;
			this.src = src;
			this.dst = dst;
		}

		public void validate(OplSig<S, C, ?> sig, OplSetInst<S, C, X> src0, OplSetInst<S, C, X> dst0) {
			this.sig = sig;
			this.src0 = src0;
			this.dst0 = dst0;

			for (Object s : sig.sorts) {
				if (!sorts.containsKey(s)) {
					throw new RuntimeException("Missing sort: " + s);
				}
			}
			for (S s : sorts.keySet()) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Extra sort: " + s);
				}
			}
			for (Object s : sig.sorts) {
				Map<X, X> h = sorts.get(s);
				for (X x : h.keySet()) {
					X y = h.get(x);
					if (!src0.sorts.get(s).contains(x)) {
						throw new RuntimeException("Value " + x + " is not in source for sort " + s);
					}
					if (!dst0.sorts.get(s).contains(y)) {
						throw new RuntimeException("Value " + y + " is not in target for sort " + s);
					}
				}
			}
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> a = sig.symbols.get(f);
				List<S> arg_ts = a.first;
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> src0.sorts.get(x))
						.collect(Collectors.toList());
				List<List<X>> args = Util.prod(arg_ds);

				for (List<X> arg : args) {
					X r = src0.getSymbol(f).get(arg);
					X lhs = sorts.get(a.second).get(r);

					List<X> l = new LinkedList<>();
					int i = 0;
					for (S at : arg_ts) {
						X v = arg.get(i);
						X u = sorts.get(at).get(v);
						l.add(u);
						i++;
					}
					X rhs = dst0.getSymbol(f).get(l);
					if (!lhs.equals(rhs)) {
						throw new RuntimeException("Compatibility condition failure for " + f
								+ " on " + arg + ", lhs=" + lhs + " but rhs=" + rhs);
					}
				}

			}
		}

		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (S k : sorts.keySet()) {
				Map<X, X> v = sorts.get(k);
				List<String> l = new LinkedList<>();
				for (X x : v.keySet()) {
					X y = v.get(x);
					l.add("(" + x + ", " + y + ")");
				}
				sortsX.add(k + " -> {" + Util.sep(l, ", ") + "}");
			}
			ret += "\t\t" + Util.sep(sortsX, ", ") + ";\n";

			return "transform {\n" + ret + "} : " + src + " -> " + dst;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class OplJavaTrans {

	}

	public static class OplSchema<S, C, V> extends OplExp {
		String sig0;
		OplSig<S, C, V> sig;
		Set<S> entities;

		public OplSchema(String sig0, Set<S> entities) {
			this.sig0 = sig0;
			this.entities = entities;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		/*
		 * @Override public JComponent display() { return sig.display(); }
		 */

		public void validate(OplSig<S, C, V> sig) {
			this.sig = sig;
			for (S s : entities) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Not a sort: " + s);
				}
			}
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);

				if (t.first.size() == 1 && entities.contains(t.first.get(0))
						&& entities.contains(t.second)) {
					continue; // is foreign key
				}
				if (t.first.size() == 1 && entities.contains(t.first.get(0))
						&& !entities.contains(t.second)) {
					continue; // is attribute
				}
				boolean hitEntity = false;
				for (S k : t.first) {
					if (entities.contains(k)) {
						hitEntity = true;
						break;
					}
				}
				if (!hitEntity && !entities.contains(t.second)) {
					continue; // is typeside function
				}
				throw new RuntimeException("Does not pass entity/typeside check: " + f);
			}
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				for (S k : eq.first.values()) {
					if (entities.contains(k)) {
						if (eq.first.values().size() != 1) {
							throw new RuntimeException(eq + " has ctx with > 1 variable");
						}
						break;
					}
				}
			}
			
		}

		OplSig<S, C, V> cache_E, cache_A, cache_T;

		public OplSig<S, C, V> projE() {
			if (cache_E != null) {
				return cache_E;
			}
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);

				if (entities.containsAll(t.first)
						&& entities.contains(t.second)) {
					symbols.put(f, t);
				}
			}

			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				if (eq.first.vars0.size() != 1) {
					continue;
				}
				if (!entities.contains(new LinkedList<>(eq.first.vars0.values()).get(0))) {
					continue;
				}
				if (!symbols.keySet().containsAll(eq.second.symbols())
						|| !symbols.keySet().containsAll(eq.third.symbols())) {
					continue;
				}
				equations.add(eq);
			}

			cache_E = new OplSig<S, C, V>(sig.fr, sig.prec, new HashSet<>(entities), symbols,
					equations);
			
			return cache_E;
		}

		public OplSig<S, C, V> projT() {
			if (cache_T != null) {
				return cache_T;
			}
			Set<S> types = new HashSet<>(sig.sorts);
			types.removeAll(entities);

			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			outer: for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);

				if (entities.contains(t.second)) {
					continue;
				}
				for (S s : t.first) {
					if (entities.contains(s)) {
						continue outer;
					}
				}

				symbols.put(f, t);
			}

			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				if (!types.containsAll(eq.first.vars0.values())) {
					continue;
				}
				if (!symbols.keySet().containsAll(eq.second.symbols())
						|| !symbols.keySet().containsAll(eq.third.symbols())) {
					continue;
				}
				equations.add(eq);
			}

			cache_T = new OplSig<S, C, V>(sig.fr, sig.prec, types, symbols, equations);
			return cache_T;
		}

		OplSig<S, C, V> cache_EA;

		public OplSig<S, C, V> projEA() {
			if (cache_EA != null) {
				return cache_EA;
			}

			Set<S> types = new HashSet<>();
			types.addAll(sig.sorts);
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			symbols.putAll(projA().symbols);
			symbols.putAll(projE().symbols);
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			equations.addAll(projA().equations);
			equations.addAll(projE().equations);
			cache_EA = new OplSig<S, C, V>(sig.fr, sig.prec, types, symbols, equations);
			return cache_EA;
		}

		

		public OplSig<S, C, V> projA() {
			if (cache_A != null) {
				return cache_A;
			}
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);

				if (entities.contains(t.second)) {
					continue;
				}
				if (t.first.size() != 1) {
					continue;
				}
				if (!entities.contains(t.first.get(0))) {
					continue;
				}

				symbols.put(f, t);
			}

			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				if (!symbols.keySet().containsAll(eq.second.symbols())
						|| !symbols.keySet().containsAll(eq.third.symbols())) {
					continue;
				}
				equations.add(eq);
			}

			cache_A = new OplSig<S, C, V>(sig.fr, sig.prec, new HashSet<>(sig.sorts), symbols,
					equations);
			return cache_A;
		}

		@Override
		public String toString() {
			return "schema {\n entities\n  " + Util.sep(entities, ", ") + ";\n}" + "\n\n + " + sig;
		}
	}

	public static class OplSchemaProj<S, C, V> extends OplExp {
		String which;
		String sch0;

		public OplSchemaProj(String sch0, String which) {
			this.sch0 = sch0;
			this.which = which;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public OplSig<S, C, V> proj(OplSchema<S, C, V> sig) {
			if (which.equals("E")) {
				return sig.projE();
			} else if (which.equals("A")) {
				return sig.projA();
			} else if (which.equals("T")) {
				return sig.projT();
			} else if (which.equals("EA")) {
				return sig.projEA();
			}
			throw new RuntimeException();
		}
	}

	public static class OplInst0<S, C, V, X> extends OplExp {
		
		OplPres<S, C, V, X> P;
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public OplInst0(OplPres<S, C, V, X> p) {
			super();
			P = p;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((P == null) ? 0 : P.hashCode());
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
			OplInst0<?,?,?,?> other = (OplInst0<?,?,?,?>) obj;
			if (P == null) {
				if (other.P != null)
					return false;
			} else if (!P.equals(other.P))
				return false;
			return true;
		}
		
		
	}
	
	public static class OplInst<S, C, V, X> extends OplExp {
		String P0, J0, S0;
		OplPres<S, C, V, X> P;
		OplJavaInst J;
		OplSchema<S, C, V> S;

		public OplPres<S, C, V, X> projE() {
			return proj(S.projE());
		}

		public OplPres<S, C, V, X> projEA() {
			return proj(S.projEA());
		}

		// public OplPres<S, C, V, X> projEdiscreteT() {
		// return proj(S.projEdiscreteT());
		// }

		// public OplPres<S, C, V, X> projEAdiscreteT() {
		// return proj(S.projEAdiscreteT());
		// }

		public OplPres<S, C, V, X> proj(OplSig<S, C, V> sig) {
			// OplSig<S, C, V> sig = S.projE();

			Map<X, S> gens = new HashMap<>();
			for (X x : P.gens.keySet()) {
				S s = P.gens.get(x);
				// if (S.entities.contains(s)) {
				gens.put(x, s);
				// }
			}

			List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations = new LinkedList<>();
			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> eq : P.equations) {
				Set<Chc<C, X>> set = new HashSet<>();
				set.addAll(eq.first.symbols());
				set.addAll(eq.second.symbols());

				Set<C> set1 = new HashSet<>();
				Set<X> set2 = new HashSet<>();
				for (Chc<C, X> cx : set) {
					if (cx.left) {
						set1.add(cx.l);
					} else {
						set2.add(cx.r);
					}
				}

				if (!gens.keySet().containsAll(set2)) {
					continue;
				}
				if (!sig.symbols.keySet().containsAll(set1)) {
					continue;
				}
				equations.add(eq);
			}

			OplPres<S, C, V, X> ret = new OplPres<>(P.prec, "?", sig, gens, equations);

			return ret;
		}

		public OplInst(String S0, String P0, String J0) {
			this.P0 = P0;
			this.J0 = J0;
			this.S0 = S0;
		}

		void validate(OplSchema<S, C, V> S, OplPres<S, C, V, X> P, OplJavaInst J) {
			if (!S.sig.equals(P.sig)) {
				throw new RuntimeException("Presentation not on expected theory: \n\nschema sig " + S.sig + "\n\npres sig " + P.sig);
			}
			if (J != null && !J.sig0.equals(S.projT())) {
				throw new RuntimeException("JS model not on expected theory");
			}

			this.P = P;
			this.J = J;
			this.S = S;
			P.toSig();
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			return P.toString();
		}

		public JComponent display() {
			JTabbedPane ret = new JTabbedPane();

			CodeTextPanel p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			ret.add(p, "Presentation");

			// FQLTextPanel q = new
			// FQLTex/tPanel(BorderFactory.createEtchedBorder(),
			// "",projEA().display();
			// ret.add(OplSat.saturate(projEA()).display(), "Attribute Graph");

			try {
				Quad<OplSetInst<S, C, OplTerm<Chc<C, X>, V>>, OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplPres<S, C, V, OplTerm<Chc<C, X>, V>>, OplSetInst<S, C, OplTerm<Chc<C, X>, V>>> xxx = saturate();
				ret.add(new CodeTextPanel(BorderFactory.createEtchedBorder(), "", xxx.third
						.toString()), "Type Algebra");

				ret.add(xxx.first.makeTables(S.projT().sorts), "Saturation");
				ret.add(xxx.fourth.makeTables(S.projT().sorts), "Normalized");
				if (xxx.second != null) {
					ret.add(xxx.second.makeTables(S.projT().sorts), "Image");
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				ret.add(new CodeTextPanel(BorderFactory.createEtchedBorder(), "Exception", ex
						.getMessage()), "Error");
			}
			return ret;

		}

		public Quad<OplSetInst<S, C, OplTerm<Chc<C, X>, V>>, OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplPres<S, C, V, OplTerm<Chc<C, X>, V>>, OplSetInst<S, C, OplTerm<Chc<C, X>, V>>> saturate() {
			return saturate(J, projEA(), S, P);
		}

		public static <S, C, V, X> Quad<OplSetInst<S, C, OplTerm<Chc<C, X>, V>>, OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplPres<S, C, V, OplTerm<Chc<C, X>, V>>, OplSetInst<S, C, OplTerm<Chc<C, X>, V>>> saturate(
				OplJavaInst I0, OplPres<S, C, V, X> P0, OplSchema<S, C, V> S, OplPres<S, C, V, X> P) {
			OplSetInst<S, C, OplTerm<Chc<C, X>, V>> I = OplSat.saturate(P0);

			OplPres<S, C, V, OplTerm<Chc<C, X>, V>> T = typeAlg(I, S, P, P0);

			OplSetInst<S, C, OplTerm<Chc<C, X>, V>> X = reduce(I, P);

			OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> J = null;
			if (I0 != null) {
				J = inject(X, I0);
			}

			return new Quad<>(I, J, T, X);
		}

		public static <S, C, V, X> OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> inject(
				OplSetInst<S, C, OplTerm<Chc<C, X>, V>> I, OplJavaInst I0) {
			if (I0 == null) {
				throw new RuntimeException();
			}
			Map<S, Set<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>> sorts = new HashMap<>();
			for (S s : I.sorts.keySet()) {
				Set<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> set = new HashSet<>();
				for (OplTerm<Chc<C, X>, V> e : I.sorts.get(s)) {
					OplTerm<Chc<Chc<C, X>, JSWrapper>, V> kkk = OplSig.inject(e);
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> z = OplToKB.redBy(I0, jjj);
					OplTerm<Chc<Chc<C, X>, JSWrapper>, V> ee = OplToKB.convert(z);
					set.add(ee);
				}
				sorts.put(s, set);
			}

			Map<C, Map<List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>> symbols = new HashMap<>();
			for (C c : I.symbols.keySet()) {
				Map<List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> map = new HashMap<>();
				for (List<OplTerm<Chc<C, X>, V>> args : I.symbols.get(c).keySet()) {
					List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> args0 = new LinkedList<>();
					for (OplTerm<Chc<C, X>, V> arg : args) {
						OplTerm<Chc<Chc<C, X>, JSWrapper>, V> kkk = OplSig.inject(arg);
						KBExp<Chc<Chc<C, X>, JSWrapper>, V> jjj = OplToKB.convert(kkk);
						KBExp<Chc<Chc<C, X>, JSWrapper>, V> z = OplToKB.redBy(I0, jjj);
						OplTerm<Chc<Chc<C, X>, JSWrapper>, V> arg0 = OplToKB.convert(z);
						args0.add(arg0);
					}
					OplTerm<Chc<Chc<C, X>, JSWrapper>, V> kkk = OplSig.inject(I.symbols.get(c).get(
							args));
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C, X>, JSWrapper>, V> z = OplToKB.redBy(I0, jjj);
					OplTerm<Chc<Chc<C, X>, JSWrapper>, V> ee = OplToKB.convert(z);
					map.put(args0, ee);
				}
				symbols.put(c, map);
			}

			OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> ret = new OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>(
					sorts, symbols, I.sig);
			ret.validate(I.sig0);
			return ret;

		}

		// morally, should reduce by type algebra, but this should be equivalent
		public static <S, C, V, X> OplSetInst<S, C, OplTerm<Chc<C, X>, V>> reduce(
				OplSetInst<S, C, OplTerm<Chc<C, X>, V>> I, OplPres<S, C, V, X> P) {
			Map<S, Set<OplTerm<Chc<C, X>, V>>> sorts = new HashMap<>();
			for (S s : I.sorts.keySet()) {
				Set<OplTerm<Chc<C, X>, V>> set = new HashSet<>();
				for (OplTerm<Chc<C, X>, V> e : I.sorts.get(s)) {
					set.add(P.toSig().getKB().nf(e));
				}
				sorts.put(s, set);
			}

			Map<C, Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>>> symbols = new HashMap<>();
			for (C c : I.symbols.keySet()) {
				Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>> map = new HashMap<>();
				for (List<OplTerm<Chc<C, X>, V>> args : I.symbols.get(c).keySet()) {
					List<OplTerm<Chc<C, X>, V>> args0 = new LinkedList<>();
					for (OplTerm<Chc<C, X>, V> arg : args) {
						OplTerm<Chc<C, X>, V> arg0 = P.toSig().getKB().nf(arg);
						args0.add(arg0);
					}
					map.put(args0, P.toSig().getKB().nf(I.symbols.get(c).get(args)));
				}
				symbols.put(c, map);
			}

			OplSetInst<S, C, OplTerm<Chc<C, X>, V>> ret = new OplSetInst<S, C, OplTerm<Chc<C, X>, V>>(
					sorts, symbols, I.sig);
			ret.validate(I.sig0);
			return ret;
		}

		public static <S, C, V, X> OplPres<S, C, V, OplTerm<Chc<C, X>, V>> typeAlg(
				OplSetInst<S, C, OplTerm<Chc<C, X>, V>> I, OplSchema<S, C, V> S,
				OplPres<S, C, V, X> P, OplPres<S, C, V, X> P0) {
			Map<OplTerm<Chc<C, X>, V>, S> gens = new HashMap<>();
			List<Pair<OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V>, OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V>>> eqs = new LinkedList<>();

			List<OplTerm<Chc<C, X>, V>> allgens = new LinkedList<>();
			for (S s : S.projT().sorts) {
				for (OplTerm<Chc<C, X>, V> e : I.sorts.get(s)) {
					gens.put(e, s);
					allgens.add(e);
				}
			}

			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> eq : P.equations) {
				P.toSig();
				S.projT();
				if (!S.projT().sorts.contains(eq.first.type(P.toSig(), new OplCtx<>()))) {
					continue; // only process equations at types
				}

				eqs.add(new Pair<>(conv(S, eq.first, P0), conv(S, eq.second, P0)));
			}

			if (P0.toSig.getKB().KB instanceof KB) {
				allgens.sort(new Comparator<OplTerm<Chc<C, X>, V>>() {
					public int compare(OplTerm<Chc<C, X>, V> o1, OplTerm<Chc<C, X>, V> o2) {
						if (o1.equals(o2)) {
							return 0;
						}
						KB<Chc<C,X>,V> kb = (KB<Chc<C,X>,V>) P0.toSig.getKB().KB;
					//	KB.gt.apply(new Pair<>(convert(e1), convert(e2)));
						if (kb.gt.apply(new Pair<>(OplToKB.convert(o1), OplToKB.convert(o2)))) {
						//if (((KB<C,V>)P0.toSig().getKB()).gt(o1, o2)) {
							return 1;
						}
						return 0;
					}
				});
			}
			int j = S.projT().largestPrec();
			Map<OplTerm<Chc<C, X>, V>, Integer> prec = new HashMap<>();
			for (int i = 0; i < allgens.size(); i++) {
				prec.put(allgens.get(i), j + 1 + i);
			}
			OplPres<S, C, V, OplTerm<Chc<C, X>, V>> ret = new OplPres<S, C, V, OplTerm<Chc<C, X>, V>>(
					prec, "?", S.projT(), gens, eqs);
			ret.toSig();
			return ret;
		}

		//TODO this is important and should be called out somehow
		public static <S, C, V, X> OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V> conv(
				OplSchema<S, C, V> S, OplTerm<Chc<C, X>, V> e0, OplPres<S, C, V, X> P0) {
			OplTerm<Chc<C, X>, V> e = P0.toSig().getKB().nf(e0);
			if (e.var != null) {
				throw new RuntimeException();
			}

			Chc<C, X> h = e.head;
			// base case, generator in instance, skolem term
			if (!h.left) {
				if (!e.args.isEmpty()) {
					throw new RuntimeException();
				}
				return new OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V>(Chc.inRight(e),
						new LinkedList<>());
			}

			C c = h.l;
			if (S.projT().symbols.containsKey(c)) {
				List<OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V>> l = new LinkedList<>();
				for (OplTerm<Chc<C, X>, V> arg : e.args) {
					OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V> arg0 = conv(S, arg, P0);
					l.add(arg0);
				}
				return new OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V>(Chc.inLeft(c), l);
			} else if (S.projA().symbols.containsKey(c)) {
				return new OplTerm<Chc<C, OplTerm<Chc<C, X>, V>>, V>(Chc.inRight(e),
						new LinkedList<>());
			} else {
				throw new RuntimeException();
			}

		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static class OplApply extends OplExp {
		String Q0, I0;
		OplQuery Q;
//		OplInst I;

		public OplApply(String Q0, String I0) {
			this.Q0 = Q0;
			this.I0 = I0;
		}
/*
		void validate(OplQuery Q, OplInst I) {
			this.Q = Q;
			this.I = I;
			// TODO
		} */

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class OplId extends OplExp {
		String s;

		public OplId(String s) {
			this.s = s;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class OplTyMapping<S1, C1, V, S2, C2> extends OplExp {
		String src0, dst0;
		OplSchema<S1, C1, V> src;
		OplSchema<S2, C2, V> dst;
		private OplMapping<S1, C1, V, S2, C2> m;

		public OplTyMapping(String src0, String dst0, OplSchema<S1, C1, V> src,
				OplSchema<S2, C2, V> dst, OplMapping<S1, C1, V, S2, C2> m) {
			this.src0 = src0;
			this.dst0 = dst0;
			this.src = src;
			this.dst = dst;
			this.m = m;
			validate();
		}

		private void validate() {
			if (!src.projT().equals(dst.projT())) {
				throw new RuntimeException("Differing type sides");
			}
			extend().validate(src.sig, dst.sig);
		}

		private OplMapping<S1, C1, V, S2, C2> cache;

		public OplMapping<S1, C1, V, S2, C2> extend() {
			if (cache != null) {
				return cache;
			}

			Map<S1, S2> sorts = new HashMap<>(m.sorts);
			Map<C1, Pair<OplCtx<S2, V>, OplTerm<C2, V>>> symbols = new HashMap<>(m.symbols);

			for (S1 s1 : src.projT().sorts) {
				@SuppressWarnings("unchecked")
				S2 s2 = (S2) s1;
				sorts.put(s1, s2);
			}

			for (C1 c1 : src.projT().symbols.keySet()) {
				Pair<List<S1>, S1> t = src.projT().symbols.get(c1);
				List<Pair<V, S2>> l = new LinkedList<>();
				List<OplTerm<C2, V>> vs = new LinkedList<>();
				for (S1 s1 : t.first) {
					V v = src.sig.fr.next();
					vs.add(new OplTerm<>(v));
					@SuppressWarnings("unchecked")
					S2 s2 = (S2) s1;
					l.add(new Pair<>(v, s2));
				}
				OplCtx<S2, V> ctx = new OplCtx<>(l);
				@SuppressWarnings("unchecked")
				C2 c2 = (C2) c1;
				OplTerm<C2, V> value = new OplTerm<>(c2, vs);
				symbols.put(c1, new Pair<>(ctx, value));
			}
			cache = new OplMapping<S1, C1, V, S2, C2>(sorts, symbols, "?", "?");
			
			return cache;
		}

		@Override
		public JComponent display() {
			return extend().display();
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	@SuppressWarnings("rawtypes")
	public interface OplExpVisitor<R, E> {
		public R visit(E env, OplTyMapping e);

		public R visit(E env, OplId e);

		public R visit(E env, OplApply e);

		public R visit(E env, OplSig e);

		public R visit(E env, OplPres e);

		public R visit(E env, OplSetInst e);

		public R visit(E env, OplEval e);

		public R visit(E env, OplVar e);

		public R visit(E env, OplSetTrans e);

		public R visit(E env, OplJavaInst e);

		public R visit(E env, OplMapping e);

		public R visit(E env, OplDelta e);

		public R visit(E env, OplSigma e);

		public R visit(E env, OplSat e);

		public R visit(E env, OplUnSat e);

		public R visit(E env, OplPresTrans e);

		public R visit(E env, OplUberSat e);

		public R visit(E env, OplFlower e);

		public R visit(E env, OplSchema e);

		public R visit(E env, OplSchemaProj e);

		public R visit(E env, OplInst e);

		public R visit(E env, OplQuery e);
		
		public R visit(E env, OplSCHEMA0 e);
		
		public R visit(E env, OplInst0 e);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("serial")
	static class NonEditableModel extends DefaultTableModel {

		NonEditableModel(Object[][] data, String[] columnNames) {
			super(data, columnNames);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return true;
		}
	} 



	static String strip(String s) {
		if (!NEWDEBUG.debug.opl.opl_pretty) {
			return s;
		}
		String ret = s.replace("inl ", "").replace("inr ", "").replace("()", "")
				.replace("forall . ", "").trim();
		if (ret.startsWith("|- ")) {
			ret = ret.substring(3);
		}
		return ret;
	}

}
