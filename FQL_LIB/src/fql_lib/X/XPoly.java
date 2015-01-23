package fql_lib.X;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.gui.FQLTextPanel;

public class XPoly<C,D> extends XExp implements XObject {
	
	public static <X> XPoly<X,X> id(XCtx<X> S) {
		Map<Object, Pair<X, Block<X, X>>> bs = new HashMap<>();
		for (X x : S.ids) {
			Map<X, Pair<Object, Map<Object, List<Object>>>> edges = new HashMap<>();
			Map<X, List<Object>> attrs = new HashMap<>();
			Map<Object, X> from = new HashMap<>();
			from.put("q_v", x);
			for (X term : S.terms()) {
				Pair<X, X> t = S.type(term);
				if (!t.first.equals(x) || t.second.equals("_1")) {
					continue;
				}
				List<Object> l = new LinkedList<>();
				l.add("q_v");
				l.add(term);
				if (S.ids.contains(t.second)) {
					Map<Object, List<Object>> m = new HashMap<>();
					m.put("q_v", l);
					edges.put(term, new Pair<>("q" + t.second, m));
				} else {
					attrs.put(term, l);
				}
			}
			Block<X,X> b = new Block<X,X>(from, new HashSet<>(), attrs, edges);
			bs.put("q" + x, new Pair<>(x, b));
		}
		
		XPoly<X,X> ret = new XPoly<X,X>(S, S, bs);
		return ret;
	}

	
	
	public XPoly(XCtx src, XCtx dst, Map<Object, Pair<D, Block<C, D>>> blocks) {
		this.src = src;
		this.dst = dst;
		this.blocks = blocks;
		validate();
	}

	public XPoly(XExp src, XExp dst, Map<Object, Pair<D, Block<C, D>>> blocks) {
		this.src_e = src;
		this.dst_e = dst;
		this.blocks = blocks;
	}

	public static class Block<C, D> {

		//should inherit from parent
		public XCtx<C> frozen(XCtx<C> src) {
			Set<C> ids = new HashSet<>();
			Map<C, Pair<C,C>> types = new HashMap<>();
			Set<Pair<List<C>, List<C>>> eqs = new HashSet<>();
			
			for (Object k0 : from.entrySet()) {
				Entry k = (Entry) k0;
				types.put((C)k.getKey(), new Pair<>((C)"_1", (C) k.getValue()));
			}
			//eqs.addAll(from.where);
			
			XCtx<C> ret = new XCtx<C>(ids, types, eqs, src.global, src, "instance");
			
			return ret;
		}
		
		public Block(Map<Object, C> from, Set<Pair<List<Object>, List<Object>>> where, Map<D, List<Object>> attrs,
				Map<D, Pair<Object, Map<Object, List<Object>>>> edges) {
			super();
			this.from = from;
			this.where = where;
			this.attrs = attrs;
			this.edges = edges;
			if (DEBUG.debug.reorder_joins) {
				this.from = sort(from);
			} else {
				this.from = from;
			}
		}
		
		private void count(List<Object> first, Map counts) {
			for (Object s : first) {
				Integer i = (Integer) counts.get(s);
				if (i == null) {
					continue;
				}
				counts.put(s, i+1);
			}
		}

		public Map<Object, C> sort(Map m) {
			Map count = new HashMap<>();
			for (Object s : m.keySet()) {
				count.put(s, 0);
			}
			for (Pair<List<Object>, List<Object>> k : where) {
				count(k.first, count);
				count(k.first, count);
			}
			List l = new LinkedList<>(m.keySet());
			l.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((Integer)count.get(o2)) - ((Integer)count.get(o1));
				}
			});
			Map ret = new LinkedHashMap<>();
			for (Object s : l) {
				ret.put(s, m.get(s));
			}
			return ret;
		}
		

		Map<Object, C> from = new HashMap<>(); 
		Set<Pair<List<Object>, List<Object>>> where = new HashSet<>();
		Map<D, List<Object>> attrs = new HashMap<>();
		Map<D, Pair<Object, Map<Object, List<Object>>>> edges = new HashMap<>();
		/*{ for a:A;
           where a.attA=1;
           attributes attA = a.attA;
           edges f = {b=a.f} : qB;
           } */
		@Override
		public String toString() {
			String for_str = printFor();
			String where_str = printWhere();
			String attr_str = printAttrs();
			String edges_str = printEdges();
			
			return "{for " + for_str + "; where " + where_str + "; attributes " 
			+ attr_str + "; edges " + edges_str + ";}" ;
		}

		private String printEdges() {
			boolean first = false;
			String ret = "";
			for (Entry<D, Pair<Object, Map<Object, List<Object>>>> k : edges.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + " = {" + printSub(k.getValue().second) + "} : " + k.getValue().first;
			}			
			return ret;
		}

		private static <C,D> String printSub(Map<D, List<C>> map) {
			boolean first = false;
			String ret = "";
			for (Entry<D, List<C>> k : map.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + " = " + Util.sep(k.getValue(), ".");
			}			
			return ret;

		}

		private String printAttrs() {
			boolean first = false;
			String ret = "";
			for (Entry<D, List<Object>> k : attrs.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + " = " + Util.sep(k.getValue(), ".");
			}			
			return ret;

		}

		private String printWhere() {
			boolean first = false;
			String ret = "";
			for (Pair<List<Object>, List<Object>> k : where) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += Util.sep(k.first, ".") + " = " + Util.sep(k.second, ".");
			}			
			return ret;
		}

		private String printFor() {
			boolean first = false;
			String ret = "";
			for (Entry<Object, C> k : from.entrySet()) {
				if (first) {
					ret += ", ";
				}
				first = true;
				ret += k.getKey() + ":" + k.getValue();
			}			
			return ret;
		}		
	}
	
	XExp src_e, dst_e;
	
	XCtx<C> src;
	XCtx<D> dst;
	Map<Object, Pair<D, Block<C,D>>> blocks = new HashMap<>();
	
	@Override
	public String kind() {
		return "polynomial";
	}

	@Override
	public String toString() {
		return "polynomial {" + printBlocks() + "\n} : " + src_e + " -> " + dst_e;
	}

	private String printBlocks() {
		boolean first = false;
		String ret = "";
		for (Entry<Object, Pair<D, Block<C, D>>> k : blocks.entrySet()) {
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
		return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XPoly other = (XPoly) obj;
		if (blocks == null) {
			if (other.blocks != null)
				return false;
		} else if (!blocks.equals(other.blocks))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (dst_e == null) {
			if (other.dst_e != null)
				return false;
		} else if (!dst_e.equals(other.dst_e))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (src_e == null) {
			if (other.src_e != null)
				return false;
		} else if (!src_e.equals(other.src_e))
			return false;
		return true;
	}

	@Override
	public <R, E> R accept(E env, XExpVisitor<R, E> v) {
		return v.visit(env, this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((dst_e == null) ? 0 : dst_e.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((src_e == null) ? 0 : src_e.hashCode());
		return result;
	}
	
	private XCtx<D> o_cache = null;
	public XCtx<D> o() {
		if (o_cache != null) {
			return o_cache;
		}
		Map<D, Pair<D, D>> types = new HashMap<>();
		Set<Pair<List<D>, List<D>>> eqs = new HashSet<>();
		for (Object l : blocks.keySet()) {
			D d = blocks.get(l).first;
			types.put((D)l, new Pair<>((D)"_1", d));
			Block<C, D> b = blocks.get(l).second;
			for (D e : b.edges.keySet()) {
				Pair<Object, Map<Object, List<Object>>> k = b.edges.get(e);
				Object l0 = k.first;
				List<D> lhs = new LinkedList<>();
				lhs.add((D)l);
				lhs.add(e);
				List<D> rhs = new LinkedList<>();
				rhs.add((D)l0);
				eqs.add(new Pair<>(lhs, rhs));
			}
		}
		
		o_cache = new XCtx<D>(new HashSet<>(), types, eqs, XCtx.empty_global(), dst.hat(), "instance");
		return o_cache;
	}

	private Function<Pair<Object, List<D>>, XMapping<C,C>> frozen = null;
	public Function<Pair<Object, List<D>>, XMapping<C,C>> freeze() {
		if (frozen != null) {
			return frozen;
		}
		
		Map<Object, XCtx<C>> frozens = new HashMap<>();
		for (Object l : blocks.keySet()) {
			frozens.put(l, blocks.get(l).second.frozen(src));
		}
		for (C t : src.global.ids) {
			frozens.put(t, src.y((C)"y_a", t));
		}
		
		Map<D, XMapping<C,C>> transforms2 = new HashMap<>();
		for (D e : dst.global.terms()) {
			Pair<D,D> t = dst.global.type(e);
			XCtx<C> srcX = frozens.get(t.first);
			XCtx<C> dstX = frozens.get(t.second);
			Map em = new HashMap<>();
			List v2 = new LinkedList<>();
			v2.add("y_a");
			v2.add(e);
			em.put("y_a", v2);
			for (Object o : dstX.allTerms()) {
				if (em.containsKey(o)) {
					continue;
				}
				em.put(o, Collections.singletonList(o));
			}
			XMapping<C,C> m = new XMapping(dstX, srcX, em, "homomorphism");
			transforms2.put(e, m);
		}
		
		Map<Pair<Object, D>, XMapping<C,C>> transforms = new HashMap<>();
		
		for (Object k : blocks.keySet()) {
			Pair<D, Block<C, D>> b = blocks.get(k);
			XCtx<C> srcX = frozens.get(k);
			
			//just validates
			for (D term : dst.terms()) {
				Pair<D, D> t = dst.type(term);
				if (!t.first.equals(b.first)) {
					continue;
				}
				if (!dst.ids.contains(term) && dst.ids.contains(t.second) && !b.second.edges.containsKey(term)) {
					throw new RuntimeException("Missing mapping for edge " + term + " in " + k + " in " + this);
				} else if (!t.second.equals("_1") && dst.global.ids.contains(t.second) && !b.second.attrs.containsKey(term)){
					throw new RuntimeException("Missing mapping for attr " + term + " in " + k + " in " + this);
				}
			}

			for (D k2 : b.second.edges.keySet()) {
				Pair<Object, Map<Object, List<Object>>> v2 = b.second.edges.get(k2);
				XCtx<C> dstX = frozens.get(v2.first);
				Map em = new HashMap<>(v2.second);
				for (Object o : dstX.schema.allTerms()) {
					if (em.containsKey(o)) {
						continue;
					}
					em.put(o, Collections.singletonList(o));
				}
				XMapping<C,C> m = new XMapping(dstX, srcX, em, "homomorphism");
				transforms.put(new Pair<>(k,k2),m);
			}			
			
			for (D k2 : b.second.attrs.keySet()) {
				List v2 = b.second.attrs.get(k2);
				XCtx<C> dstX = frozens.get(dst.type(k2).second);
				Map em = new HashMap<>();
				em.put("y_a", v2);
				for (Object o : dstX.allTerms()) {
					if (em.containsKey(o)) {
						continue;
					}
					em.put(o, Collections.singletonList(o));
				}
				XMapping<C,C> m = new XMapping(dstX, srcX, em, "homomorphism");
				transforms.put(new Pair<>(k,k2), m);
			}	
			
			D k2 = (D) ("!_" + b.first);
			//List v2 = Collections.singletonList(k2); //b.second.attrs.get(k2);
			List v2 = Collections.singletonList("_1"); //b.second.attrs.get(k2);
			XCtx<C> dstX = frozens.get(dst.type(k2).second);
			Map em = new HashMap<>();
			em.put("y_a", v2);
			for (Object o : dstX.allTerms()) {
				if (em.containsKey(o)) {
					continue;
				}
				em.put(o, Collections.singletonList(o));
			}
			XMapping<C,C> m = new XMapping(dstX, srcX, em, "homomorphism");
			transforms.put(new Pair<>(k, k2), m);
	
			transforms.put(new Pair<>(k, b.first), new XMapping<>(srcX, "homomorphism"));			
			
		}

		Map<Pair<Object, List<D>>, XMapping<C,C>> frozen_cache = new HashMap<>();
		frozen = p -> {
			if (frozen_cache.containsKey(p)) {
				return frozen_cache.get(p);
			}
			Object l = p.first;
			D e = p.second.get(0);
			XMapping<C,C> h = transforms.get(new Pair<>(l, e));
			if (h == null) {
				throw new RuntimeException("(" + l + "," + e + ") not in " + transforms.keySet());
			}

			//need filter b/c don't have identity edges in the blocks
	//		for (D eX : p.second.subList(1, p.second.size()).stream().filter(z -> !dst.allIds().contains(z)).collect(Collectors.toList())) {
			for (D eX : p.second.subList(1, p.second.size()) /*.stream().filter(z -> dst.ids.contains(z)).collect(Collectors.toList()) */ ) {
				XMapping<C, C> hX = null;
				if (transforms2.containsKey(eX)) {
					l = null;
					hX = transforms2.get(eX);
					if (hX == null) {
						throw new RuntimeException();
					}
				} else {
					if (l == null) {
						throw new RuntimeException();
					}
				/*	if (!blocks.containsKey(l)) {
						throw new RuntimeException();
					}
					if (!blocks.get(l).second.edges.containsKey(e)) {
						throw new RuntimeException("Missing " + e + " in " + blocks.get(l).second.edges.keySet() + " at label " + l);
					} */
					Object lX = null;
					if (dst.ids.contains(e)) {
						lX = l;
					} else {
						lX = blocks.get(l).second.edges.get(e).first;
					}
					hX = transforms.get(new Pair<>(lX, eX));
					if (hX == null) {
						throw new RuntimeException();
					}
					l = lX;
				}
				e = eX;
				h = new XMapping<>(hX, h);
			}
			frozen_cache.put(p, h);
			return h;
		};
		return frozen;
	}
	
	public void validate() {
		Function<Pair<Object, List<D>>, XMapping<C,C>> f = freeze();
		/* for (Object l : blocks.keySet()) {
			D d = blocks.get(l).first;
			for (D d0 : dst.allIds()) {
				for (Triple<D, D, List<D>> p : dst.cat().hom(d, d0)) {
					List<D> p0 = new LinkedList<>();
					p0.add(p.first);
					p0.addAll(p.third);
					f.apply(new Pair<>(l, p0));
					System.out.println(p);
				}			
			}
		}  */
		for (Pair<List<D>, List<D>> eq : dst.allEqs()) {
			List<D> p = eq.first;
			List<D> q = eq.second;
			Pair<D,D> t = dst.type(p);
			D d = t.first;
			D d0= t.second;
		//	if (dst.global.allIds().contains(d0)) {
				
		//	} else {
			for (Object l : blocks.keySet()) {
				Pair<D, Block<C, D>> block = blocks.get(l);
				if (!block.first.equals(d)) {
					continue;
				}
				List p2 = new LinkedList<>(p);
				p2.add(0, l);
				List q2 = new LinkedList<>(q);
				q2.add(0, l);
				//are not paths in o
				if (!dst.global.allIds().contains(d0) && !o().getKB().equiv(p2, q2)) {
					throw new RuntimeException("Not respected on " + eq + " in " + o());
				}
//				System.out.println("checking " + p + " = " + q);
				XMapping<C, C> p3 = freeze().apply(new Pair<>(l, p));
				XMapping<C, C> q3 = freeze().apply(new Pair<>(l, q));
				if (!XMapping.transform_eq(p3, q3)) {
					throw new RuntimeException("FR Not respected on " + eq);
				}
			}
		//	}
		} 
	}
	
	
}


/*			Set<D> atts = new HashSet();
for (D arr : dst.allTerms()) {
	Pair<D, D> ty = dst.type(arr);
	if (ty.second.equals("_1") || dst.allIds().contains(arr) || !ty.first.equals(b.first)) {
		continue;
	}
	if (!dst.ids.contains(ty.second)) {
		atts.add(arr);
	}
}
if (!atts.equals(b.second.attrs.keySet())) {
	throw new RuntimeException("Bad attributes in " + k + ": " + atts + " vs " + b.second.attrs.keySet());
} */

