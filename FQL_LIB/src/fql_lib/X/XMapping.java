package fql_lib.X;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.X.XExp.XMapConst;
import fql_lib.X.XExp.XTransConst;
import fql_lib.gui.FQLTextPanel;

public class XMapping<C, D> implements XObject {
	public Map<Pair<List<C>,  List<C>>, String> unprovable = new HashMap<>();

	XCtx<C> src;
	XCtx<D> dst;
	Map<C, List<D>> em;
	
	public XMapping(XCtx<C> src, XCtx<D> dst, Map<C, List<D>> em) {
		this.src = src;
		this.dst = dst;
		this.em = em;
		validate();
	}
	
	//TODO: make sure this is the identity for global and schema
	public void validate() {
	//	System.out.println("Validating " + this);
		unprovable = new HashMap<>();
		for (C c : em.keySet()) {
			if (!src.allTerms().contains(c)) {
				throw new RuntimeException("Extraneous: " + c);
			}
		}
		for (C c : src.allTerms()) {
			if (!em.containsKey(c)) {
				throw new RuntimeException("Missing: " + c);
			}
			List<D> x = em.get(c);
		//	System.out.println(" on " + c + " is " + x);
			dst.type(x);
		}
		
		for (C c : src.terms()) {
			Pair<C, C> src_t = src.type(c);  //c : t1 -> t2
			Pair<D, D> dst_t = dst.type(em.get(c)); //Fc : tx -> ty
			Pair<List<D>, List<D>> t = new Pair<>(em.get(src_t.first), em.get(src_t.second));
			if (t.first.size() != 1 || t.second.size() != 1) {
				throw new RuntimeException();
			}
			D t1 = t.first.get(0);
			D t2 = t.second.get(0);
			if (!dst_t.first.equals(t1) || !dst_t.second.equals(t2)) {
				throw new RuntimeException("On " + c + ", source type is " + src_t + " mapsto " + em.get(c) + " whose type is " + dst_t + ", which is not " + t + ", as expected.");
			}
		}
		
		for (Pair<List<C>, List<C>> eq : src.allEqs()) {
			List<D> lhs = apply(eq.first);
			List<D> rhs = apply(eq.second);
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
	//	System.out.println(unprovable);
	}

		
	public List<D> apply(List<C> p) {
		List<D> ret = p.stream().flatMap(x -> { 
			List<D> xxx = em.get(x);
			if (xxx == null) {
				throw new RuntimeException("Does not map " + x);
			}
			return xxx.stream();
		}).collect(Collectors.toList());
		dst.type(ret);
		return ret;
	}
	
	//is identity on non-mapped elements
	public List<D> applyAlsoId(List<C> p) {
		List<D> ret = p.stream().flatMap(x -> { 
			List<D> r = em.get(x);
			if (r == null) {
				r = new LinkedList<>();
				r.add((D)x);
			}
			return r.stream();
		}).collect(Collectors.toList());
		//dst.type(ret); can't do type here
		return ret;
	}
	
	
	@Override
	public JComponent display() {
//		System.out.println(this);
		String ret = Util.sep(src.allTerms().stream().map(x -> x + " -> " + Util.sep(em.get(x), ".")).collect(Collectors.toList()), "\n");
		ret += "\n";
		for (Pair<List<C>, List<C>> eq : src.allEqs()) {
			List<D> lhs = apply(eq.first);
			List<D> rhs = apply(eq.second);
			ret += "\nEquation: " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".");
			ret += "\nTransformed: " + Util.sep(lhs, ".") + " = " + Util.sep(rhs, ".");
			ret += "\nProvable: " + unprovable.get(eq);
			ret += "\n";
		}
		
		ret = ret.trim();
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Text", new FQLTextPanel(BorderFactory.createEtchedBorder(), "", ret));
		if (src.schema != null) {
			pane.addTab("Tables", makeTables());
		} else {
			pane.addTab("Tables", makeTables2());
		}
		return pane;
	}

	private Component makeTables2() {
		try {
			Object[][] rowData = new Object[src.cat().arrows().size()][2];
			Object[] colNames = new Object[] { "src", "dst" };
			
			int i = 0;
			for (Triple<C, C, List<C>> k : src.cat().arrows()) {
				List<C> a = new LinkedList<>(k.third);
				a.add(0, k.first);
				List<D> applied = apply(a);
				Pair<D,D> t = dst.type(applied);
				for (Triple<D, D, List<D>> cand : dst.cat().hom(t.first, t.second)) {
					if (dst.getKB().equiv(cand.third, applied)) {
						List<C> z = new LinkedList<>(k.third);
						z.add(0, k.first);
						rowData[i][0] = Util.sep(z, ".");
						List<D> y = new LinkedList<>(cand.third);
						y.add(0, cand.first);
						rowData[i][1] = Util.sep(y, ".");
					}
				}				
				i++;
			}
			
			return Util.makeTable(BorderFactory.createEtchedBorder(), "", rowData, colNames);			
		} catch (Exception e) {
			e.printStackTrace();
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR:\n\n" + e.getMessage());
		}	

	}
	
	private Component makeTables() {
		try {
			List<JComponent> grid = new LinkedList<>();
			//Map<C, Map<List<C>, List<D>>> m = new HashMap<>();
			
			for (C id : src.schema.ids) {
				Map<List<C>, List<D>> map = new HashMap<>();
				for (Triple<C, C, List<C>> arr : src.cat().hom((C) "1", id)) { //arrows()) {
					List<C> toApply = new LinkedList<>(arr.third);
					toApply.add(0, arr.first);
					List<D> applied = apply(toApply);
					for (Triple<D, D, List<D>> cand : dst.cat().hom((D) "1", (D) id)) {
						if (dst.getKB().equiv(cand.third, applied)) {
							map.put(arr.third, cand.third);						
						}
					}
				}		
				Object[][] rowData = new Object[map.size()][2];
				Object[] colNames = new Object[] { "src", "dst" };
				int i = 0;
				for (Entry<List<C>, List<D>> k : map.entrySet()) {
					rowData[i][0] = Util.sep(k.getKey(), ".");
					rowData[i][1] = Util.sep(k.getValue(), ".");
					i++;
				}
				JPanel tbl = Util.makeTable(BorderFactory.createEtchedBorder(), id + " (" + map.size() + " rows)", rowData, colNames);
				grid.add(tbl);
			}

			return Util.makeGrid(grid);			
		} catch (Exception e) {
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR:\n\n" + e.getMessage());
		}	
	}

	@Override
	public String toString() {
		return "XMapping [src=" + src + ", dst=" + dst + ", em=" + em + ", unprovable="
				+ unprovable + "]";
	}

	public XCtx<D> apply0(XCtx<D> global, XCtx<C> I) {
		//XCtx<D> ret = dst.copy();
	//	ret.local = new HashSet<>(); //(Set<D>) I.local;
		Map<D, Pair<D,D>> ret = new HashMap<>();
		Set<Pair<List<D>, List<D>>> eqs = new HashSet<>();
		for (C c : I.terms()) {
		//	ret.consts.add((D)c);
			//if (I.local.contains(c)) {
				//ret.local.add((D)c);
			//}
			Pair<C, C> t = I.type(c);
			List<D> lhs = em.get(t.first);
			if (lhs == null) {
				throw new RuntimeException("No edge mapping for " + t.first + " in " + em);
			}
			List<D> rhs = em.get(t.second);
			if (rhs == null) {
				throw new RuntimeException("No edge mapping for " + t.second + " in " + em);
			}
			if (lhs.size() != 1 || rhs.size() != 1) {
				throw new RuntimeException();
			}
			ret.put((D)c, new Pair<>((D)lhs.get(0), (D)rhs.get(0)));
		}
		
		for (Pair<List<C>, List<C>> eq : I.eqs) {
			if (src.eqs.contains(eq)) {
				continue;
			}
			List<D> lhs = applyAlsoId(eq.first);
			List<D> rhs = applyAlsoId(eq.second);
			eqs.add(new Pair<>(lhs, rhs));
		}
	
		return new XCtx<D>(new HashSet<>(), ret, eqs, global, dst);
	}

	
	public static XMapping<String, String> make(XEnvironment eNV, XCtx<String> src, XCtx<String> dst, XMapConst m) { 
		Map<String, List<String>> ret = new HashMap<>();
		List<String> one = new LinkedList<>();
		one.add("1");
		ret.put("1", one);
		List<String> ggg = new LinkedList<>();
		ggg.add("!_1");
		ret.put("!_1", ggg);

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
			if (!src.terms().contains(k.first)) {
				throw new RuntimeException("Not-local: " + k.first);
			}
			List<String> l = new LinkedList<>();
			l.add(k.second);
			ret.put(k.first, l);
		}
		for (String l : src.allIds()) {
//			if (l != "1") {
			List h = new LinkedList();
			if (ret.get(l) == null) {
				h.add("!_" + l); 
			} else {
				h.add("!_" + ret.get(l).get(0));
			}
			ret.put("!_" + l, h);
	//		}

			if (!src.terms().contains(l)) {
				continue;
			}
			if (!ret.keySet().contains(l)) {
				throw new RuntimeException("No mapping for node " + l);
			}
//			em.put("1_" + nm.get(l));
			
		}

		for (Pair<String, List<String>> k : m.em) {
			if (!src.terms().contains(k.first)) {
				throw new RuntimeException("Source does not contain edge " + k.first);
			}
			if (ret.containsKey(k.first)) {
				throw new RuntimeException("Duplicate node mapping for " + k.first);
			}
			if (src.ids.contains(k.first)) {
				throw new RuntimeException("Cannot re-map node " + k.first);
			}
			ret.put(k.first, k.second);
		}
		for (String l : src.terms()) {
			if (!ret.keySet().contains(l)) {
				throw new RuntimeException("No mapping for edge " + l);
			}
		}
		for (String l : src.global.terms()) {
			if (l.startsWith("!") || l.startsWith("1")) {
				continue;
			}
			List ls = new LinkedList<>();
			ls.add(l);
			ret.put(l, ls);
		}

		
		return new XMapping<>(src, dst, ret);
	}
 
	public static XMapping<String, String> make(XCtx<String> src, XCtx<String> dst, XTransConst e) {
		if (src.schema == null) {
			throw new RuntimeException("source is not an instance");
		}
		if (dst.schema == null) {
			throw new RuntimeException("target is not an instance");
		}
		if (!src.schema.equals(dst.schema)) {
			throw new RuntimeException("not on same schema");
		}
		
		Map<String, List<String>> ret = new HashMap<>();
		for (String c : src.schema.allTerms()) {
			List<String> l = new LinkedList<>();
			l.add(c);
			ret.put(c, l);
		}
		
		for (Pair<String, List<String>> k : e.vm) {
			if (!src.terms().contains(k.first)) {
				throw new RuntimeException("Source does not contain variable " + k.first);
			}
			if (src.schema.allTerms().contains(k.first)) {
				throw new RuntimeException("Not a variable: " + k.first);
			}
			if (ret.containsKey(k.first)) {
				throw new RuntimeException("Duplicate node mapping for " + k.first);
			}
			ret.put(k.first, k.second);
		}
		for (String c : src.allTerms()) {
			if (!ret.containsKey(c)) {
				throw new RuntimeException("Does not map " + c);
			}
		}
		//System.out.println("returning " + ret);
		return new XMapping<>(src, dst, ret);
	}
 
	//on transforms
	public XObject apply(XMapping<C, C> t) {
		XCtx<D> src0 = this.apply0((XCtx<D>) t.src.global, t.src);
		XCtx<D> dst0 = this.apply0((XCtx<D>) t.dst.global, t.dst);
		
		Map<D, List<D>> ret = new HashMap<>();
		for (D c : src0.allTerms()) {
//			if (src0.parent.consts.contains(c)) {
	//			continue;
		//	}
			List<C> p = new LinkedList<>();
			p.add((C)c);
			List<C> k = t.applyAlsoId(p);
			ret.put(c, applyAlsoId(k));
		}
		
		//this = f
		//t : I => J
		//t': sigma_F I => sigma_F J
//		return new XMapping()
		return new XMapping<>(src0, dst0, ret);	
	}
	
	/*
	public XCtx<Triple<D, List<D>, C>> delta(XCtx<D> I) {
		//XCtx ret = src.copy();
		//ret.local = new HashSet<>();
		//ret.parent = src;
		Set<Triple<D, List<D>, C>> gens = new HashSet<>();
//		System.out.println(I);
//		System.out.println("******");
//		System.out.println(I.parent);
		
		//TODO variables of type Type must get copied over
		//TODO copy over equations that start at type
		
		for (C c : src.ids) {
		//	if (!src.local.contains(c)) {
			//	continue;
			//}
			for (D d : I.consts) {
//				System.out.println("zzzz " + d);
			//	if (!I.local.contains(d)) {
				//	continue;
				//}
		//		System.out.println("yyyyyy ");
				if (I.parent.consts.contains(d)) {
					continue;
				}
				if (!I.typeOf.get(d).first.equals("1")) {
					throw new RuntimeException();
				}
				D x = I.typeOf.get(d).second;
				if (em.get(c).size() != 1) {
					throw new RuntimeException();
				}
			//	System.out.println("XXXX");
				
				D y = em.get(c).get(0);
				Set<Triple<D, D, List<D>>> paths = I.hom(x, y);
				for (Triple<D, D, List<D>> path : paths) {
					Triple tr = new Triple(d, path.third, c);
					ret.consts.add(tr);
					gens.add(tr);
					ret.local.add(tr);
					ret.typeOf.put(tr, new Pair("1", c));
				}
			}
		}
		
		for (Triple<D, List<D>, C> t1 : gens) {
			for (Triple<D, List<D>, C> t2 : gens) {
				if (t1.equals(t2)) {
					continue;
				}
				if (!t1.third.equals(t2.third)) {
					continue;
				}
				List<D> t1x = new LinkedList<>(t1.second);
				t1x.add(0, t1.first);
				List<D> t2x = new LinkedList<>(t2.second);
				t2x.add(0, t2.first);
				if (I.getKB().equiv(t1x, t2x)) {
					List l1 = new LinkedList<>();
					List l2 = new LinkedList<>();
					l1.add(t1);
					l2.add(t2);
					ret.eqs.add(new Pair<>(l1, l2));
				}
			}
		}
		
		for (Triple<D, List<D>, C> t1 : gens) {
			for (C c : src.consts) {
				//if (!src.local.contains(c)) {
				//	continue;
				//}
				if (src.ids.contains(c)) {
					continue;
				}
				
				Pair<C, C> t = src.typeOf.get(c);
				if (t1.third.equals(t.first)) {
					List l = new LinkedList<>();
					l.add(t1);
					l.add(c);
				
					List j = new LinkedList<>(t1.second);
					j.addAll(em.get(c));
				
					D dsrc = I.typeOf.get(t1.first).second;
					D ddst = em.get(t1.third).get(0);
					for (Triple<D, D, List<D>> arr : I.cat().arrows()) {
						if (!arr.first.equals(dsrc) || !arr.second.equals(ddst)) {
							continue;
						}
						if (I.getKB().equiv(arr.third, j)) {
							Triple g = new Triple<>(t1.first, arr.third, t.second);
							List z = new LinkedList<>();
							z.add(g);
							if (!ret.eqs.contains(new Pair<>(l,z)) || !ret.eqs.contains(new Pair<>(z,l))) {
								ret.eqs.add(new Pair(l, z));
							}
							break;
						}
					}
				
				}
			}
		} 
		
		for (Triple<D, List<D>, C> t1 : gens) {
			List<D> z = new LinkedList<>(t1.second);
			z.add(0, t1.first);
			Pair t = I.type(z);

			for (D d : dst.consts) {
				if (!I.typeOf.get(d).equals(t)) {
					continue;
				}
				
				List<D> l = new LinkedList<>();
				l.add(d);
				List r = new LinkedList<>();
				r.add(t1);
				if (I.getKB().equiv(z, l)) {
					ret.eqs.add(new Pair<>(l, r));
				}
			}
			
//			if (t1.second.isEmpty() && !I.parent.local.contains(t1.first) && !I.local.contains(t1.first) && !I.parent.local.contains(t1.third) && !I.local.contains(t1.third)) {
	//			List lhs = new LinkedList<>();
		//		lhs.add(t1);
		//		List rhs = new LinkedList<>();
			//	rhs.add(t1.first);
			//	ret.eqs.add(new Pair<>(lhs, rhs));
			}  
		}
		
		//src, dst
		
		ret.init();

		return ret;
	}
	*/


	public XMapping<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>> deltaT(XMapping<D,D> t) {
		XCtx<Pair<Triple<D, D, List<D>>, C>> dsrc = delta(t.src);
		XCtx<Pair<Triple<D, D, List<D>>, C>> ddst = delta(t.dst);
		
		Map m = new HashMap();
		//Map<Pair<Triple<D, D, List<D>>, C>, List<Pair<Triple<D, D, List<D>>, C>>> m = new HashMap<>();
		
		for (Pair<Triple<D, D, List<D>>, C> k0 : dsrc.terms()) {
			boolean found = false;
			Pair<Triple<D, D, List<D>>, C> k = new Pair<>(new Triple<>(k0.first.first, k0.first.second, t.applyAlsoId(k0.first.third)), k0.second);
			for (Triple<D, D, List<D>> v : t.dst.cat().hom(k.first.first, k.first.second)) {
			//	if (!k.second.equals(v.second)) {
				//	continue;
			//	}
			/* 	if (!k.first.first.equals(v.first)) {
					continue;
				}
				if (!k.first.second.equals(v.second)) {
					continue;
				} */
				if (t.dst.getKB().equiv(k.first.third, v.third)) {
					List<Pair<Triple<D, D, List<D>>, C>> l = new LinkedList<>();
					l.add(new Pair<>(v, k.second));
					m.put(k0, l);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("No equiv for " + k.first.third + " in " + t.dst);
			}
		}
		
		for (Object o : dsrc.allTerms()) {
			if (m.containsKey(o)) {
				continue;
			}
			List l = new LinkedList();
			l.add(o);
			m.put(o, l);
		}
		
		return new XMapping<>(dsrc, ddst, m);
	}

	
	public XMapping<C, Pair<Triple<D, D, List<D>>, C>> unit(XCtx<C> I) {
		XCtx<D> FI = apply0(dst.global, I); 
		XCtx<Pair<Triple<D, D, List<D>>, C>> FFI = delta(FI);
		Map m = new HashMap<>();
		
		for (C c : I.terms()) {
			List<D> Fc;
			Fc = new LinkedList<>(); 
			Fc.add((D)c);
			Pair<D, D> t = FI.type(Fc);
			boolean found = false;
			for (Triple<D, D, List<D>> v : FI.cat().hom(t.first, t.second)) {
/*				if (!v.first.equals(t.first)) {
					continue;
				}
				if (!v.second.equals(t.second)) {
					continue;
				} */
				if (FI.getKB().equiv(v.third, Fc)) {
					List l = new LinkedList();
					l.add(new Pair<>(v, I.type(c).second));
					m.put(c, l);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("No equiv for " + Fc + " in " + FI);
			}
		}
		
		for (Object o : I.allTerms()) {
			if (m.containsKey(o)) {
				continue;
			}
			List l = new LinkedList();
			l.add(o);
			m.put(o, l);
		} 

		return new XMapping<>(I, FFI, m);
	}
	
	public XMapping<Pair<Triple<D, D, List<D>>, C>, D> counit(XCtx<D> I) {
		XCtx<Pair<Triple<D, D, List<D>>, C>> FI = delta(I);
		XMapping<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>> f = (XMapping<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>>) this;
		XCtx<Pair<Triple<D, D, List<D>>, C>> FFI = f.apply0((XCtx<Pair<Triple<D, D, List<D>>, C>>) I.global, FI); 
		
		Map m = new HashMap<>();
		
		for (Pair<Triple<D, D, List<D>>, C> c : FFI.terms()) {
			List<D> l = new LinkedList<>(c.first.third);
			l.add(0, c.first.first);
			m.put(c, l);
		}
		
		for (Object o : FFI.allTerms()) {
			if (m.containsKey(o)) {
				continue;
			}
			List l = new LinkedList();
			l.add(o);
			m.put(o, l);
		} 

		return new XMapping<>(FFI, I, m);
	}

		
	public XCtx<Pair<Triple<D, D, List<D>>, C>> delta(XCtx<D> I) {
		Set<Pair<Triple<D, D, List<D>>, C>> ids = new HashSet<>();
		Map<Pair<Triple<D, D, List<D>>, C>, Pair<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>>> types = new HashMap<>();
		Set<Pair<List<Pair<Triple<D, D, List<D>>, C>>, List<Pair<Triple<D, D, List<D>>, C>>>> eqs = new HashSet<>();

		for (C c : src.allIds()) {
			for (Triple<D, D, List<D>> arr : I.cat().hom((D)"1", em.get(c).get(0))) {
	/*			if (!arr.first.equals("1")) {
					continue;
				}
				if (!arr.second.equals(em.get(c).get(0))) {
					continue;
				} */
				Pair tr = new Pair(arr, c);
				types.put(tr, new Pair("1", c));
			}
		}
		
		for (Pair<Triple<D, D, List<D>>, C> t1 : types.keySet()) {
			for (C c : src.allTerms()) { //f
				if (src.ids.contains(c)) {
					continue;
				}
				
				Pair<C, C> t = src.type(c);
				D dsrc = t1.first.first;
				D ddst = em.get(t.second).get(0);

				if (t1.second.equals(t.first)) {
					List lhs = new LinkedList<>();
					lhs.add(t1);
					lhs.add(c);
				
					List j = new LinkedList<>(t1.first.third);
					j.addAll(em.get(c));
					Triple rhs = new Triple(dsrc, ddst, j);
					Triple rhsX = XCtx.find(I.getKB(), rhs, I.cat().hom(dsrc, ddst));
					List g = new LinkedList<>();
					g.add(new Pair<>(rhsX, t.second));
					
					eqs.add(new Pair<>(lhs, g));
				}
			}
		} 
		
		for (Pair<Triple<D, D, List<D>>, C> t1 : types.keySet()) {
			Pair t = new Pair<>(t1.first.first, t1.first.second);

			for (D d : dst.allTerms()) {
				if (!I.type(d).equals(t)) {
					continue;
				}
				
				List<D> l = new LinkedList<>();
				l.add(d);
				List r = new LinkedList<>();
				r.add(t1);
				if (I.getKB().equiv(t1.first.third, l)) {
					eqs.add(new Pair(l, r));
				}
			}
		} 
		
		return new XCtx<Pair<Triple<D, D, List<D>>, C>>(ids, types, eqs, (XCtx<Pair<Triple<D, D, List<D>>, C>>)src.global, (XCtx<Pair<Triple<D, D, List<D>>, C>>)src);
	}

}
