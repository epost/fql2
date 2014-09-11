package fql_lib.X;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import fql_lib.Pair;
import fql_lib.Util;
import fql_lib.X.XExp.XMapConst;
import fql_lib.gui.FQLTextPanel;

public class XMapping<C> implements XObject {

	XCtx<C> src, dst;
	Map<C, List<C>> em;
	public Map<Pair<List<C>, List<C>>, String> unprovable;
	
	public XMapping(XCtx<C> src, XCtx<C> dst, Map<C, List<C>> em) {
		this.src = src;
		this.dst = dst;
		this.em = em;
		validate();
	}
	
	public void validate() {
		unprovable = new HashMap<>();
		for (C c : src.consts) {
			Pair<C, C> src_t = src.typeOf.get(c);  //c : t1 -> t2
			Pair<C, C> dst_t = dst.type(em.get(c)); //Fc : tx -> ty
			Pair<List<C>, List<C>> t = new Pair<>(em.get(src_t.first), em.get(src_t.second));
			if (t.first.size() != 1 || t.second.size() != 1) {
				throw new RuntimeException();
			}
			C t1 = t.first.get(0);
			C t2 = t.second.get(0);
			if (!dst_t.first.equals(t1) || !dst_t.second.equals(t2)) {
				throw new RuntimeException("On " + c + ", source type is " + src_t + " mapsto " + em.get(c) + " whose type is " + dst_t + ", which is not " + t + ", as expected.");
			}
		}
		
		for (Pair<List<C>, List<C>> eq : src.eqs) {
			List<C> lhs = apply(eq.first);
			List<C> rhs = apply(eq.second);
			if (!dst.type(lhs).equals(dst.type(rhs))) {
				throw new RuntimeException();
			}
			try {
				boolean b = dst.getKB().equiv(lhs, rhs);
				unprovable.put(eq, b ? "true" : "false");
			} catch (Exception ex) {
				unprovable.put(eq, "unknown");
			}
		}	
	}

	
	
	public List<C> apply(List<C> p) {
		List<C> ret = p.stream().flatMap(x -> { 
			return em.get(x).stream();
		}).collect(Collectors.toList());
		dst.type(ret);
		return ret;
	}
	
	//is identity on non-mapped elements
	public List<C> applyAlsoId(List<C> p) {
		List<C> ret = p.stream().flatMap(x -> { 
			List<C> r = em.get(x);
			if (r == null) {
				r = new LinkedList<>();
				r.add(x);
			}
			return r.stream();
		}).collect(Collectors.toList());
		//dst.type(ret); can't do type here
		return ret;
	}
	
	
	@Override
	public JComponent display() {
		String ret = Util.sep(src.consts.stream().filter(x -> src.local.contains(x)).map(x -> x + " -> " + Util.sep(em.get(x), ".")).collect(Collectors.toList()), "\n");
		ret += "\n\n";
		for (Pair<List<C>, List<C>> eq : src.eqs) {
			List<C> lhs = apply(eq.first);
			List<C> rhs = apply(eq.second);
			ret += "\nEquation: " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".");
			ret += "\nTransformed: " + Util.sep(lhs, ".") + " = " + Util.sep(rhs, ".");
			ret += "\nProvable: " + unprovable.get(eq);
			ret += "\n";
		}
		
		ret = ret.trim();
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Text", new FQLTextPanel(BorderFactory.createEtchedBorder(), "", ret));
		
		return pane;
	}

	public XCtx<C> apply(XCtx<C> I) {
		XCtx<C> ret = dst.copy();
		for (C c : I.consts) {
			if (src.consts.contains(c)) {
				continue;
			}
			ret.consts.add(c);
			Pair<C, C> t = I.typeOf.get(c);
			List<C> lhs = em.get(t.first);
			List<C> rhs = em.get(t.second);
			if (lhs.size() != 1 || rhs.size() != 1) {
				throw new RuntimeException();
			}
			ret.typeOf.put(c, new Pair<>(lhs.get(0), rhs.get(0)));
		}
		
		for (Pair<List<C>, List<C>> eq : I.eqs) {
			if (src.eqs.contains(eq)) {
				continue;
			}
			List<C> lhs = applyAlsoId(eq.first);
			List<C> rhs = applyAlsoId(eq.second);
			ret.eqs.add(new Pair<>(lhs, rhs));
		}
		
		ret.init();
		
		return ret;
	}

	public static XObject make(XEnvironment eNV, XCtx<String> src, XCtx<String> dst, XMapConst m) {
		Map<String, List<String>> ret = new HashMap<>();
		for (Pair<String, String> k : m.nm) {
			if (!src.ids.contains(k.first)) {
				throw new RuntimeException("Source does not contain node " + k.first);
			}
			if (!dst.ids.contains(k.second)) {
				throw new RuntimeException("Target does not contain node " + k.second);
			}
			if (ret.containsKey(k.first)) {
				throw new RuntimeException("Duplicate node mapping for " + k.first);
			}
			if (!src.local.contains(k.first)) {
				throw new RuntimeException("Not-local: " + k.first);
			}
			List<String> l = new LinkedList<>();
			l.add(k.second);
			ret.put(k.first, l);
		}
		for (String l : src.ids) {
			if (!src.local.contains(l)) {
				continue;
			}
			if (!ret.keySet().contains(l)) {
				throw new RuntimeException("No mapping for node " + l);
			}
		}
		for (Pair<String, List<String>> k : m.em) {
			if (!src.consts.contains(k.first)) {
				throw new RuntimeException("Source does not contain edge " + k.first);
			}
			if (ret.containsKey(k.first)) {
				throw new RuntimeException("Duplicate node mapping for " + k.first);
			}
			if (!src.local.contains(k.first)) {
				throw new RuntimeException("Not-local: " + k.first);
			}
			if (src.ids.contains(k.first)) {
				throw new RuntimeException("Cannot re-map node " + k.first);
			}
			ret.put(k.first, k.second);
		}
		for (String l : src.consts) {
			if (!src.local.contains(l)) {
				continue;
			}
			if (src.ids.contains(l)) {
				continue;
			}
			if (!ret.keySet().contains(l)) {
				throw new RuntimeException("No mapping for edge " + l);
			}
		}
		
		for (String c : src.consts) {
			if (src.local.contains(c)) {
				continue;
			}
			List<String> l = new LinkedList<>();
			l.add(c);
			ret.put(c, l);
		}
		
		return new XMapping<>(src, dst, ret);
	}
		
}
