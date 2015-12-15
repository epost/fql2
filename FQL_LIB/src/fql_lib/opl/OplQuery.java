package fql_lib.opl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import catdata.algs.Chc;
import catdata.algs.Pair;
import fql_lib.Util;
import fql_lib.gui.FQLTextPanel;
import fql_lib.opl.OplExp.OplSat;

public class OplQuery<S1, C1, V1, S2, C2, V2> extends OplExp implements OplObject {

	OplSchema<S1, C1, V1> src;
	OplSchema<S2, C2, V2> dst;

	String src_e, dst_e;

	Map<Object, Pair<S2, Block<S1, C1, V1, S2, C2, V2>>> blocks = new HashMap<>();

	public OplQuery(String src_e, String dst_e,
			Map<Object, Pair<S2, Block<S1, C1, V1, S2, C2, V2>>> blocks) {
		this.src_e = src_e;
		this.dst_e = dst_e;
		this.blocks = blocks;
	}

	public void validate(OplSchema<S1, C1, V1> src, OplSchema<S2, C2, V2> dst) {
		this.src = src;
		this.dst = dst;

		if (!src.projT().equals(dst.projT())) {
			throw new RuntimeException("Different type sides");
		}

		for (Object b : blocks.keySet()) {
			Pair<S2, Block<S1, C1, V1, S2, C2, V2>> block0 = blocks.get(b);
			S2 s2 = block0.first;
			Block<S1, C1, V1, S2, C2, V2> block = block0.second;

			if (!dst.projE().sorts.contains(s2)) {
				throw new RuntimeException("In block " + b + ", " + s2 + " is not a target entity.");
			}

			for (V1 v1 : block.from.keySet()) {
				S1 s1 = block.from.get(v1);
				if (!src.projE().sorts.contains(s1)) {
					throw new RuntimeException("In block " + b + ", " + s1
							+ " is not a source entity.");
				}
			}
		}

		for (Object b : blocks.keySet()) {
			Pair<S2, Block<S1, C1, V1, S2, C2, V2>> block0 = blocks.get(b);
			S2 s2 = block0.first;
			Block<S1, C1, V1, S2, C2, V2> block = block0.second;

			OplCtx<S1, V1> ctx = new OplCtx<>(block.from);

			for (Pair<OplTerm<C1, V1>, OplTerm<C1, V1>> eq : block.where) {
				S1 l = eq.first.type(src.sig, ctx);
				S1 r = eq.second.type(src.sig, ctx);
				if (!l.equals(r)) {
					throw new RuntimeException("In checking block " + b + ", different types for "
							+ eq.first + " = " + eq.second + ", " + l + " and " + r);
				}
			}

			for (C2 a : block.attrs.keySet()) {
				OplTerm<C1, V1> e = block.attrs.get(a);
				Pair<List<S2>, S2> t = dst.projA().symbols.get(a);
				if (t == null) {
					throw new RuntimeException("In checking block " + b + ", " + a
							+ " is not an attribute in " + dst_e);
				}
				S1 s1 = e.type(src.sig, ctx);
				if (!s1.equals(t.second)) {
					throw new RuntimeException("In checking block " + b + ", " + e + " has type "
							+ s1 + " but should be " + t.second);
				}
			}
			for (C2 a : dst.projA().symbols.keySet()) {
				Pair<List<S2>, S2> t = dst.projA().symbols.get(a);
				if (t.first.size() != 1) {
					throw new RuntimeException("Internal error, report to Ryan");
				}
				if (!t.first.get(0).equals(s2)) {
					continue;
				}
				if (!block.attrs.containsKey(a)) {
					throw new RuntimeException("In checking block " + b + ", missing attribute: "
							+ a);
				}
			}

			for (C2 a : block.edges.keySet()) {
				Pair<Object, Map<V1, OplTerm<C1, V1>>> e = block.edges.get(a);
				Pair<S2, Block<S1, C1, V1, S2, C2, V2>> tgt = blocks.get(e.first);
				if (tgt == null) {
					throw new RuntimeException("Not a label: " + e.first);
				}
				Pair<List<S2>, S2> t = dst.projE().symbols.get(a);
				if (!t.second.equals(tgt.first)) {
					throw new RuntimeException("In checking edge " + a + " in block " + b
							+ ", the target entity for label " + e.first + " is " + tgt.first
							+ ", not " + t.second + " as expected");
				}

				OplCtx<S1, V1> tgtCtx = new OplCtx<>(tgt.second.from);
				Map<V1, S1> xxx = new HashMap<>();
				for (V1 v1 : e.second.keySet()) {
					xxx.put(v1, e.second.get(v1).type(src.sig, ctx));
				}
				OplCtx<S1, V1> tgtCtx2 = new OplCtx<>(xxx);
				if (!tgtCtx.equals(tgtCtx2)) {
					throw new RuntimeException("In checking edge " + a + " in block " + b
							+ ", the ctx for target block is " + tgtCtx + " but for valuation is "
							+ tgtCtx2);
				}
			}
			for (C2 a : dst.projE().symbols.keySet()) {
				Pair<List<S2>, S2> t = dst.projE().symbols.get(a);
				if (t.first.size() != 1) {
					throw new RuntimeException("Internal error, report to Ryan");
				}
				if (!t.first.get(0).equals(s2)) {
					continue;
				}
				if (!block.edges.containsKey(a)) {
					throw new RuntimeException("Missing edge: " + a);
				}
			}
		}

		// TODO ignores path equality checking
	}

	public static class Block<S1, C1, V1, S2, C2, V2> {

		Map<V1, S1> from;
		Set<Pair<OplTerm<C1, V1>, OplTerm<C1, V1>>> where;
		Map<C2, OplTerm<C1, V1>> attrs;
		Map<C2, Pair<Object, Map<V1, OplTerm<C1, V1>>>> edges;

		public Block(Map<V1, S1> from, Set<Pair<OplTerm<C1, V1>, OplTerm<C1, V1>>> where,
				Map<C2, OplTerm<C1, V1>> attrs,
				Map<C2, Pair<Object, Map<V1, OplTerm<C1, V1>>>> edges) {
			super();
			this.from = from;
			this.where = where;
			this.attrs = attrs;
			this.edges = edges;
			// TODO
			/*
			 * if (DEBUG.debug.reorder_joins) { this.from = sort(from); } else {
			 * this.from = from; }
			 */
		}

		@Override
		public String toString() {
			String for_str = printFor();
			String where_str = printWhere();
			String attr_str = printAttrs();
			String edges_str = printEdges();

			return "{for " + for_str + "; where " + where_str + "; attributes " + attr_str
					+ "; edges " + edges_str + ";}";
		}

		private String printEdges() {
			boolean first = false;
			String ret = "";
			for (Entry<C2, Pair<Object, Map<V1, OplTerm<C1, V1>>>> k : edges.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + " = {" + printSub(k.getValue().second) + "} : "
						+ k.getValue().first;
			}
			return ret;
		}

		private <C, D> String printSub(Map<V1, OplTerm<C1, V1>> second) {
			boolean first = false;
			String ret = "";
			for (Entry<V1, OplTerm<C1, V1>> k : second.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + " = " + k.getValue();
			}
			return ret;

		}

		private String printAttrs() {
			boolean first = false;
			String ret = "";
			for (Entry<C2, OplTerm<C1, V1>> k : attrs.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + " = " + k.getValue();
			}
			return ret;

		}

		private String printWhere() {
			boolean first = false;
			String ret = "";
			for (Pair<OplTerm<C1, V1>, OplTerm<C1, V1>> k : where) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.first + " = " + k.second;
			}
			return ret;
		}

		private String printFor() {
			boolean first = false;
			String ret = "";
			for (Entry<V1, S1> k : from.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + ":" + k.getValue();
			}
			return ret;
		}
	}

	@Override
	public String toString() {
		return "query {" + printBlocks() + "\n} : " + src_e + " -> " + dst_e;
	}

	private String printBlocks() {
		boolean first = false;
		String ret = "";
		for (Entry<Object, Pair<S2, Block<S1, C1, V1, S2, C2, V2>>> k : blocks.entrySet()) {
			if (first) {
				ret += ", ";
			}
			first = true;
			ret += "\n  " + k.getKey() + " = " + k.getValue().second + " : " + k.getValue().first;
		}

		return ret;
	}

	@Override
	public JComponent display() {
		JTabbedPane ret = new JTabbedPane();

		ret.addTab("Text", new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString()));

		// ret.addTab("Hat", new
		// FQLTextPanel(BorderFactory.createEtchedBorder(), "",
		// hat().toString()));

		// ret.addTab("GrothO", new
		// FQLTextPanel(BorderFactory.createEtchedBorder(), "",
		// grotho().toString()));

		// ret.addTab("Tilde", new
		// FQLTextPanel(BorderFactory.createEtchedBorder(), "",
		// tilde().toString()));

		return ret;
	}

	@Override
	public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
		return v.visit(env, this);
	}

	public static <S, C, V> OplQuery<S, C, V, S, C, V> id(String str, OplSchema<S, C, V> S) {
		Map<Object, Pair<S, Block<S, C, V, S, C, V>>> bs = new HashMap<>();
		for (S x : S.projE().sorts) {
			Map<V, S> from = new HashMap<>();
			Map<C, OplTerm<C, V>> attrs = new HashMap<>();
			Map<C, Pair<Object, Map<V, OplTerm<C, V>>>> edges = new HashMap<>();
			from.put((V) "q_v", x);
			for (C term : S.projEA().symbols.keySet()) {
				Pair<List<S>, S> t0 = S.projEA().symbols.get(term);
				Pair<S, S> t = new Pair<>(t0.first.get(0), t0.second);
				if (!t.first.equals(x)) {
					continue;
				}
				OplTerm<C, V> l = new OplTerm<C, V>(term,
						Util.singList(new OplTerm<C, V>((V) "q_v")));
				if (S.projE().symbols.containsKey(term)) {
					Map<V, OplTerm<C, V>> m = new HashMap<>();
					m.put((V) "q_v", l);
					edges.put(term, new Pair<>((V) ("q" + t.second), m));
				} else {
					attrs.put(term, l);
				}
			}
			Block<S, C, V, S, C, V> b = new Block<S, C, V, S, C, V>(from, new HashSet<>(), attrs,
					edges);
			bs.put("q" + x, new Pair<>(x, b));
		}

		OplQuery<S, C, V, S, C, V> ret = new OplQuery<S, C, V, S, C, V>(str, str, bs);
		ret.validate(S, S);
		return ret;
	}
	
	private <X> Set<Map<V1, OplTerm<Chc<C1, X>, V1>>> filter(Set<Map<V1, OplTerm<Chc<C1, X>, V1>>> tuples,
			Set<Pair<OplTerm<Chc<C1, X>, V1>, OplTerm<Chc<C1, X>, V1>>> where, OplInst<S1,C1,V1,X> I0) {
		Set<Map<V1,OplTerm<Chc<C1, X>, V1>>> ret = new HashSet<>();
		outer: for (Map<V1, OplTerm<Chc<C1, X>, V1>> tuple0 : tuples) {
			OplCtx<OplTerm<Chc<C1, X>, V1>, V1> tuple = new OplCtx<>(tuple0);			
			for (Pair<OplTerm<Chc<C1, X>, V1>, OplTerm<Chc<C1, X>, V1>> eq : where) {
				if (eq.first.isGround(tuple) && eq.second.isGround(tuple)) {
					OplTerm<Chc<C1, X>, V1> l = eq.first.subst(tuple0);
					OplTerm<Chc<C1, X>, V1> r = eq.second.subst(tuple0);
					OplTerm<Chc<C1, X>, V1> l0 = I0.P.toSig().getKB().nf(l);
					OplTerm<Chc<C1, X>, V1> r0 = I0.P.toSig().getKB().nf(r);
					
					if (!l0.equals(r0)) {
						continue outer;
					}
				}					
			}
			ret.add(tuple0);				
		}
		return ret;
	}

	private static <X,V> Set<Map<V, X>> extend(Set<Map<V,X>> tuples, Set<X> dom, V v) {
		Set<Map<V, X>> ret = new HashSet<>();
		
		for (Map<V, X> tuple : tuples) {
			for (X x : dom) {
				Map<V,X> m = new HashMap<>(tuple);
				m.put(v, x);
				ret.add(m);
			}
		}
		
		return ret;
	}
	
	//TODO - not right. input here should be instance. do saturation here, emit instance as well
	public <X> OplInst<S2,C2,V2,Chc<X, Chc<Pair<Object,Map<V1,OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> eval(OplInst<S1,C1,V1,X> I0) {
		//if (!I.sig0.equals(src.projEA())) {
		//	throw new RuntimeException("Instance not on correct schema");
		//}

		OplSetInst<S1,C1,OplTerm<Chc<C1,X>,V1>> I = OplSat.saturate(I0.projE());
		
		Map<S2, Set<Chc<X, Chc<Pair<Object,Map<V1,OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>>> sorts = new HashMap<>();
		for (S2 s2 : dst.sig.sorts) {
			sorts.put(s2, new HashSet<>());
		}
		for (X x : I0.P.gens.keySet()) {
			S1 s1 = I0.P.gens.get(x);
			if (dst.projT().sorts.contains(s1)) {
				sorts.get(s1).add(Chc.inLeft(x));
			}
		}
		Map<C2, Map<List<Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>>, Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>>> symbols = new HashMap<>();
		for (C2 c2 : dst.projEA().symbols.keySet()) {
			symbols.put(c2, new HashMap<>());
		}
		
		//TODO: don't forget to copy over skolem generators
		for (Object label : blocks.keySet()) {			
			Pair<S2, Block<S1, C1, V1, S2, C2, V2>> xxx = blocks.get(label);
			S2 tgt = xxx.first;
			Block<S1, C1, V1, S2, C2, V2> block = xxx.second;
			
			Set<Map<V1,OplTerm<Chc<C1, X>, V1>>> tuples = new HashSet<>();
			tuples.add(new HashMap<>());

			Set<Pair<OplTerm<Chc<C1, X>, V1>, OplTerm<Chc<C1, X>, V1>>> where = new HashSet<>();
			for (Pair<OplTerm<C1, V1>, OplTerm<C1, V1>> eq : block.where) {
				where.add(new Pair<>(OplSig.inject(eq.first), OplSig.inject(eq.second)));
			}
			
			for (V1 v : block.from.keySet()) {
				S1 s = block.from.get(v);
				Set<OplTerm<Chc<C1, X>, V1>> dom = I.sorts.get(s);
				tuples = extend(tuples, dom, v);
				tuples = filter(tuples, where, I0);
			}
			
			Set<Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> set = sorts.get(tgt);
			Set<Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> toadd = new HashSet<>();
			for (Map<V1, OplTerm<Chc<C1, X>, V1>> tuple : tuples) {
				Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>> temp1 = Chc.inLeft(new Pair<>(label, tuple));
				toadd.add(Chc.inRight(temp1));
			}
			set.addAll(toadd);
			
			for (C2 c2 : block.attrs.keySet()) {
				Map<List<Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>>, Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> m = symbols.get(c2);
				OplTerm<Chc<C1, X>, V1> e = OplSig.inject(block.attrs.get(c2));
				for (Map<V1, OplTerm<Chc<C1, X>, V1>> tuple : tuples) {
					Chc<X, Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>> tuple0 = Chc.inRight(new Pair<>(label, tuple));
					Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>> temp1 = Chc.inLeft(new Pair<>(label, tuple));
					OplTerm<Chc<C1, X>, V1> a = I0.P.toSig().getKB().nf(e.subst(tuple));
					if (a.var == null && a.args.isEmpty() && !a.head.left) {
						m.put(Util.singList(Chc.inRight(temp1)), Chc.inLeft(a.head.r));
						sorts.get(dst.sig.symbols.get(c2).second).add(Chc.inLeft(a.head.r));
					} else {
						m.put(Util.singList(Chc.inRight(temp1)), Chc.inRight(Chc.inRight(a)));
						sorts.get(dst.sig.symbols.get(c2).second).add(Chc.inRight(Chc.inRight(a)));
					}
				}
			} 
			
			for (C2 c2 : block.edges.keySet()) {
				Map<List<Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>>, Chc<X, Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> m = symbols.get(c2);
				Pair<Object, Map<V1, OplTerm<C1, V1>>> ctx0 = block.edges.get(c2);
				Object tgt_label = ctx0.first;
				Map<V1, OplTerm<C1, V1>> tgt_ctx = ctx0.second;
				for (Map<V1, OplTerm<Chc<C1, X>, V1>> tuple : tuples) {
					Map<V1, OplTerm<Chc<C1, X>, V1>> substed = new HashMap<>();
					for (V1 v1 : tgt_ctx.keySet()) {
						OplTerm<Chc<C1, X>, V1> uuu = OplSig.inject(tgt_ctx.get(v1));
						OplTerm<Chc<C1, X>, V1> vvv = uuu.subst(tuple);
						substed.put(v1, I0.P.toSig().getKB().nf(vvv));
					}
					Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>> temp1 = Chc.inLeft(new Pair<>(tgt_label, substed));
					Chc<Pair<Object, Map<V1, OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>> temp2 = Chc.inLeft(new Pair<>(label, tuple));
					m.put(Util.singList(Chc.inRight(temp2)), Chc.inRight(temp1));
				}
			}
		}
		
		OplSetInst<S2,C2,Chc<X, Chc<Pair<Object,Map<V1,OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> ret = new OplSetInst<S2,C2,Chc<X, Chc<Pair<Object,Map<V1,OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>>(sorts, symbols , "?");
		System.out.println(ret);
		ret.validate(dst.projEA());
		OplExp.OplPres<S2,C2,V2,Chc<X, Chc<Pair<Object,Map<V1,OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> P = OplUnSat.desaturate("?", ret);
		P.toSig = null;
		P.sig = dst.sig;
		
		OplInst<S2,C2,V2,Chc<X, Chc<Pair<Object,Map<V1,OplTerm<Chc<C1, X>, V1>>>, OplTerm<Chc<C1, X>, V1>>>> retX = new OplInst<>("?", "?", "?");
		retX.validate(dst, P, I0.J);
		return retX;
	}
	
	//TODO knuth bendix precedence should favor rewriting into type side rather than entity side

	//TODO using a separate type for generators was sound.  However, in a typed setting, there should be two kinds of
	//generators, so that the types for generators at type can be preserved across queries, and let the types at
	//entities change (.e.g, to hashmaps).  As it stands now, the type for attribute generators must change along with entities.
	
}
