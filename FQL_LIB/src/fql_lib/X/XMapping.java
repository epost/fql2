package fql_lib.X;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import fql_lib.DEBUG;
import fql_lib.FUNCTION;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.X.XExp.XMapConst;
import fql_lib.X.XExp.XTransConst;
import fql_lib.cat.Category;
import fql_lib.cat.Functor;
import fql_lib.cat.categories.FinSet;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.cat.categories.Pi;
import fql_lib.gui.FQLTextPanel;

public class XMapping<C, D> implements XObject {
	public Map<Pair<List<C>, List<C>>, String> unprovable = new HashMap<>();

	@Override
	public String kind() {
		return kind;
	}

	XCtx<C> src;
	XCtx<D> dst;
	Map<C, List<D>> em;
	private String kind = "TODO";

	public XMapping(XCtx<C> src, XCtx<D> dst, Map<C, List<D>> em, String kind) {
		this.src = src;
		this.dst = dst;
		this.em = em;
		this.kind = kind;
		validate();
	}

	// TODO: make sure this is the identity for global and schema
	public void validate() {
		// System.out.println("Validating " + this);
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
			// System.out.println(" on " + c + " is " + x);
			dst.type(x);
		}

		for (C c : src.terms()) {
			Pair<C, C> src_t = src.type(c); // c : t1 -> t2
			Pair<D, D> dst_t = dst.type(em.get(c)); // Fc : tx -> ty
			Pair<List<D>, List<D>> t = new Pair<>(em.get(src_t.first), em.get(src_t.second));
			if (t.first.size() != 1 || t.second.size() != 1) {
				throw new RuntimeException();
			}
			D t1 = t.first.get(0);
			D t2 = t.second.get(0);
			if (!dst_t.first.equals(t1) || !dst_t.second.equals(t2)) {
				throw new RuntimeException("On " + c + ", source type is " + src_t + " mapsto "
						+ em.get(c) + " whose type is " + dst_t + ", which is not " + t
						+ ", as expected.");
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
		// System.out.println(unprovable);
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

	// is identity on non-mapped elements
	public List<D> applyAlsoId(List<C> p) {
		List<D> ret = p.stream().flatMap(x -> {
			List<D> r = em.get(x);
			if (r == null) {
				r = new LinkedList<>();
				r.add((D) x);
			}
			return r.stream();
		}).collect(Collectors.toList());
		// dst.type(ret); can't do type here
		return ret;
	}

	@Override
	public JComponent display() {
		// System.out.println(this);
		String ret = Util.sep(
				src.allTerms().stream().map(x -> x + " -> " + Util.sep(em.get(x), "."))
						.collect(Collectors.toList()), "\n");
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
			List<Object[]> rowData = new LinkedList<>(); // Object[src.cat().arrows().size()][2];
			Object[] colNames = new Object[] { "src", "dst" };

			// int i = 0;
			for (Triple<C, C, List<C>> k : src.cat().arrows()) {
				List<C> a = new LinkedList<>(k.third);
				a.add(0, k.first);
				List<D> applied = apply(a);
				Pair<D, D> t = dst.type(applied);
				if (a.equals(applied)) {
					continue;
				}
				for (Triple<D, D, List<D>> cand : dst.cat().hom(t.first, t.second)) {
					if (dst.getKB().equiv(cand.third, applied)) {
						List<C> z = new LinkedList<>(k.third);
						z.add(0, k.first);
						Object[] row = new Object[2];
						row[0] = Util.sep(z, ".");
						List<D> y = new LinkedList<>(cand.third);
						y.add(0, cand.first);
						row[1] = Util.sep(y, ".");
						rowData.add(row);
					}
				}
				// i++;
			}

			return Util.makeTable(BorderFactory.createEtchedBorder(), "",
					rowData.toArray(new Object[0][0]), colNames);
		} catch (Exception e) {
			e.printStackTrace();
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR:\n\n"
					+ e.getMessage());
		}

	}

	private Component makeTables() {
		try {
			List<JComponent> grid = new LinkedList<>();
			// Map<C, Map<List<C>, List<D>>> m = new HashMap<>();

			for (C id : src.schema.ids) {
				Map<List<C>, List<D>> map = new HashMap<>();
				for (Triple<C, C, List<C>> arr : src.cat().hom((C) "1", id)) { // arrows())
																				// {
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
				JPanel tbl = Util.makeTable(BorderFactory.createEtchedBorder(),
						id + " (" + map.size() + " rows)", rowData, colNames);
				grid.add(tbl);
			}

			return Util.makeGrid(grid);
		} catch (Exception e) {
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR:\n\n"
					+ e.getMessage());
		}
	}

	@Override
	public String toString() {
		return "XMapping [src=" + src + ", dst=" + dst + ", em=" + em + ", unprovable="
				+ unprovable + "]";
	}

	public XCtx<D> apply0(XCtx<D> global, XCtx<C> I) {
		// XCtx<D> ret = dst.copy();
		// ret.local = new HashSet<>(); //(Set<D>) I.local;
		Map<D, Pair<D, D>> ret = new HashMap<>();
		Set<Pair<List<D>, List<D>>> eqs = new HashSet<>();
		for (C c : I.terms()) {
			// ret.consts.add((D)c);
			// if (I.local.contains(c)) {
			// ret.local.add((D)c);
			// }
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
			ret.put((D) c, new Pair<>((D) lhs.get(0), (D) rhs.get(0)));
		}

		for (Pair<List<C>, List<C>> eq : I.eqs) {
			if (src.eqs.contains(eq)) {
				continue;
			}
			List<D> lhs = applyAlsoId(eq.first);
			List<D> rhs = applyAlsoId(eq.second);
			eqs.add(new Pair<>(lhs, rhs));
		}

		return new XCtx<D>(new HashSet<>(), ret, eqs, global, dst, "instance");
	}

	public static XMapping<String, String> make(XEnvironment eNV, XCtx<String> src,
			XCtx<String> dst, XMapConst m) {
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
			// if (l != "1") {
			List h = new LinkedList();
			if (ret.get(l) == null) {
				h.add("!_" + l);
			} else {
				h.add("!_" + ret.get(l).get(0));
			}
			ret.put("!_" + l, h);
			// }

			if (!src.terms().contains(l)) {
				continue;
			}
			if (!ret.keySet().contains(l)) {
				throw new RuntimeException("No mapping for node " + l);
			}
			// em.put("1_" + nm.get(l));

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

		return new XMapping<>(src, dst, ret, "mapping");
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
		// System.out.println("returning " + ret);
		return new XMapping<>(src, dst, ret, "homomorphism");
	}

	// on transforms
	public XObject apply(XMapping<C, C> t) {
		XCtx<D> src0 = this.apply0((XCtx<D>) t.src.global, t.src);
		XCtx<D> dst0 = this.apply0((XCtx<D>) t.dst.global, t.dst);

		Map<D, List<D>> ret = new HashMap<>();
		for (D c : src0.allTerms()) {
			// if (src0.parent.consts.contains(c)) {
			// continue;
			// }
			List<C> p = new LinkedList<>();
			p.add((C) c);
			List<C> k = t.applyAlsoId(p);
			ret.put(c, applyAlsoId(k));
		}

		// this = f
		// t : I => J
		// t': sigma_F I => sigma_F J
		// return new XMapping()
		return new XMapping<>(src0, dst0, ret, "homomorphism");
	}

	/*
	 * public XCtx<Triple<D, List<D>, C>> delta(XCtx<D> I) { //XCtx ret =
	 * src.copy(); //ret.local = new HashSet<>(); //ret.parent = src;
	 * Set<Triple<D, List<D>, C>> gens = new HashSet<>(); //
	 * System.out.println(I); // System.out.println("******"); //
	 * System.out.println(I.parent);
	 * 
	 * //TODO variables of type Type must get copied over //TODO copy over
	 * equations that start at type
	 * 
	 * for (C c : src.ids) { // if (!src.local.contains(c)) { // continue; //}
	 * for (D d : I.consts) { // System.out.println("zzzz " + d); // if
	 * (!I.local.contains(d)) { // continue; //} //
	 * System.out.println("yyyyyy "); if (I.parent.consts.contains(d)) {
	 * continue; } if (!I.typeOf.get(d).first.equals("1")) { throw new
	 * RuntimeException(); } D x = I.typeOf.get(d).second; if (em.get(c).size()
	 * != 1) { throw new RuntimeException(); } // System.out.println("XXXX");
	 * 
	 * D y = em.get(c).get(0); Set<Triple<D, D, List<D>>> paths = I.hom(x, y);
	 * for (Triple<D, D, List<D>> path : paths) { Triple tr = new Triple(d,
	 * path.third, c); ret.consts.add(tr); gens.add(tr); ret.local.add(tr);
	 * ret.typeOf.put(tr, new Pair("1", c)); } } }
	 * 
	 * for (Triple<D, List<D>, C> t1 : gens) { for (Triple<D, List<D>, C> t2 :
	 * gens) { if (t1.equals(t2)) { continue; } if (!t1.third.equals(t2.third))
	 * { continue; } List<D> t1x = new LinkedList<>(t1.second); t1x.add(0,
	 * t1.first); List<D> t2x = new LinkedList<>(t2.second); t2x.add(0,
	 * t2.first); if (I.getKB().equiv(t1x, t2x)) { List l1 = new LinkedList<>();
	 * List l2 = new LinkedList<>(); l1.add(t1); l2.add(t2); ret.eqs.add(new
	 * Pair<>(l1, l2)); } } }
	 * 
	 * for (Triple<D, List<D>, C> t1 : gens) { for (C c : src.consts) { //if
	 * (!src.local.contains(c)) { // continue; //} if (src.ids.contains(c)) {
	 * continue; }
	 * 
	 * Pair<C, C> t = src.typeOf.get(c); if (t1.third.equals(t.first)) { List l
	 * = new LinkedList<>(); l.add(t1); l.add(c);
	 * 
	 * List j = new LinkedList<>(t1.second); j.addAll(em.get(c));
	 * 
	 * D dsrc = I.typeOf.get(t1.first).second; D ddst = em.get(t1.third).get(0);
	 * for (Triple<D, D, List<D>> arr : I.cat().arrows()) { if
	 * (!arr.first.equals(dsrc) || !arr.second.equals(ddst)) { continue; } if
	 * (I.getKB().equiv(arr.third, j)) { Triple g = new Triple<>(t1.first,
	 * arr.third, t.second); List z = new LinkedList<>(); z.add(g); if
	 * (!ret.eqs.contains(new Pair<>(l,z)) || !ret.eqs.contains(new
	 * Pair<>(z,l))) { ret.eqs.add(new Pair(l, z)); } break; } }
	 * 
	 * } } }
	 * 
	 * for (Triple<D, List<D>, C> t1 : gens) { List<D> z = new
	 * LinkedList<>(t1.second); z.add(0, t1.first); Pair t = I.type(z);
	 * 
	 * for (D d : dst.consts) { if (!I.typeOf.get(d).equals(t)) { continue; }
	 * 
	 * List<D> l = new LinkedList<>(); l.add(d); List r = new LinkedList<>();
	 * r.add(t1); if (I.getKB().equiv(z, l)) { ret.eqs.add(new Pair<>(l, r)); }
	 * }
	 * 
	 * // if (t1.second.isEmpty() && !I.parent.local.contains(t1.first) &&
	 * !I.local.contains(t1.first) && !I.parent.local.contains(t1.third) &&
	 * !I.local.contains(t1.third)) { // List lhs = new LinkedList<>(); //
	 * lhs.add(t1); // List rhs = new LinkedList<>(); // rhs.add(t1.first); //
	 * ret.eqs.add(new Pair<>(lhs, rhs)); } }
	 * 
	 * //src, dst
	 * 
	 * ret.init();
	 * 
	 * return ret; }
	 */

	public XMapping<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>> deltaT(
			XMapping<D, D> t) {
		XCtx<Pair<Triple<D, D, List<D>>, C>> dsrc = delta(t.src);
		XCtx<Pair<Triple<D, D, List<D>>, C>> ddst = delta(t.dst);

		Map m = new HashMap();
		// Map<Pair<Triple<D, D, List<D>>, C>, List<Pair<Triple<D, D, List<D>>,
		// C>>> m = new HashMap<>();

		for (Pair<Triple<D, D, List<D>>, C> k0 : dsrc.terms()) {
			boolean found = false;
			Pair<Triple<D, D, List<D>>, C> k = new Pair<>(new Triple<>(k0.first.first,
					k0.first.second, t.applyAlsoId(k0.first.third)), k0.second);
			for (Triple<D, D, List<D>> v : t.dst.cat().hom(k.first.first, k.first.second)) {
				// if (!k.second.equals(v.second)) {
				// continue;
				// }
				/*
				 * if (!k.first.first.equals(v.first)) { continue; } if
				 * (!k.first.second.equals(v.second)) { continue; }
				 */
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

		return new XMapping<>(dsrc, ddst, m, "homomorphism");
	}

	public XMapping<C, Pair<Triple<D, D, List<D>>, C>> unit(XCtx<C> I) {
		XCtx<D> FI = apply0(dst.global, I);
		XCtx<Pair<Triple<D, D, List<D>>, C>> FFI = delta(FI);
		Map m = new HashMap<>();

		for (C c : I.terms()) {
			List<D> Fc;
			Fc = new LinkedList<>();
			Fc.add((D) c);
			Pair<D, D> t = FI.type(Fc);
			boolean found = false;
			for (Triple<D, D, List<D>> v : FI.cat().hom(t.first, t.second)) {
				/*
				 * if (!v.first.equals(t.first)) { continue; } if
				 * (!v.second.equals(t.second)) { continue; }
				 */
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

		return new XMapping<>(I, FFI, m, "homomorphism");
	}

	public XMapping<Pair<Triple<D, D, List<D>>, C>, D> counit(XCtx<D> I) {
		XCtx<Pair<Triple<D, D, List<D>>, C>> FI = delta(I);
		XMapping<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>> f = (XMapping<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>>) this;
		XCtx<Pair<Triple<D, D, List<D>>, C>> FFI = f.apply0(
				(XCtx<Pair<Triple<D, D, List<D>>, C>>) I.global, FI);

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

		return new XMapping<>(FFI, I, m, "homomorphism");
	}

	public XCtx<Pair<Triple<D, D, List<D>>, C>> delta(XCtx<D> I) {
		Set<Pair<Triple<D, D, List<D>>, C>> ids = new HashSet<>();
		Map<Pair<Triple<D, D, List<D>>, C>, Pair<Pair<Triple<D, D, List<D>>, C>, Pair<Triple<D, D, List<D>>, C>>> types = new HashMap<>();
		Set<Pair<List<Pair<Triple<D, D, List<D>>, C>>, List<Pair<Triple<D, D, List<D>>, C>>>> eqs = new HashSet<>();

		for (C c : src.allIds()) {
			for (Triple<D, D, List<D>> arr : I.cat().hom((D) "1", em.get(c).get(0))) {
				/*
				 * if (!arr.first.equals("1")) { continue; } if
				 * (!arr.second.equals(em.get(c).get(0))) { continue; }
				 */
				Pair tr = new Pair(arr, c);
				types.put(tr, new Pair("1", c));
			}
		}

		for (Pair<Triple<D, D, List<D>>, C> t1 : types.keySet()) {
			for (C c : src.allTerms()) { // f
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

		return new XCtx<Pair<Triple<D, D, List<D>>, C>>(ids, types, eqs,
				(XCtx<Pair<Triple<D, D, List<D>>, C>>) src.global,
				(XCtx<Pair<Triple<D, D, List<D>>, C>>) src, "instance");
	}

	/*
	 * public XCtx<Map<C, Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>>
	 * pi(XCtx<C> I) { Map<D, List<Map<C, Map<Triple<D, D, List<D>>, Triple<C,
	 * C, List<C>>>>>> thetas_d = new HashMap<>(); Map<Map<C, Map<Triple<D, D,
	 * List<D>>, Triple<C, C, List<C>>>>, Pair<Map<C, Map<Triple<D, D, List<D>>,
	 * Triple<C, C, List<C>>>>, Map<C, Map<Triple<D, D, List<D>>, Triple<C, C,
	 * List<C>>>>>> tys;
	 * 
	 * Map<Map<C, Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>, Pair<D,
	 * D>> types = new HashMap<>(); for (D d : dst.allIds()) {
	 * System.out.println(d); List<Map<C, Map<Triple<D, D, List<D>>, Triple<C,
	 * C, List<C>>>>> thetas = new LinkedList<>(); Map<C,
	 * List<LinkedHashMap<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>> theta
	 * = new HashMap<>(); for (C c : src.allIds()) { Map<Triple<D, D, List<D>>,
	 * List<Triple<C, C, List<C>>>> theta2 = new HashMap<>(); for (Triple<D, D,
	 * List<D>> f : dst.cat().hom(d, em.get(c).get(0))) { List<Triple<C, C,
	 * List<C>>> filtered = I.cat().hom((C) "1", c).stream() .filter(k ->
	 * pifilter(d, c, f, k)).collect(Collectors.toList()); theta2.put(f,
	 * filtered); } List<LinkedHashMap<Triple<D, D, List<D>>, Triple<C, C,
	 * List<C>>>> theta3 = FinSet .homomorphs(theta2); theta.put(c, theta3); }
	 * System.out.println("finished inner"); List<LinkedHashMap<C,
	 * LinkedHashMap<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>> theta4 =
	 * FinSet .homomorphs(theta); System.out.println("size: " + theta4.size());
	 * 
	 * // outer: for (LinkedHashMap<C, LinkedHashMap<Triple<D, D, List<D>>,
	 * Triple<C, C, List<C>>>> cand : theta4) {
	 * 
	 * thetas_d.put(d, thetas); }
	 * 
	 * System.out.println("Finisihed with thetas");
	 * 
	 * Set<Pair<List<Map<C, Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>>,
	 * List<Map<C, Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>>>> eqs =
	 * new HashSet<>(); for (D h : dst.allTerms()) { Pair<D, D> t = dst.type(h);
	 * for (Map<C, Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>>> theta :
	 * thetas_d .get(t.first)) { Map<C, Map<Triple<D, D, List<D>>, Triple<C, C,
	 * List<C>>>> theta0 = new HashMap<>();
	 * 
	 * for (C c : src.allIds()) { Map<Triple<D, D, List<D>>, Triple<C, C,
	 * List<C>>> theta1 = new HashMap<>(); for (Triple<D, D, List<D>> f :
	 * dst.cat().hom(t.first, em.get(c).get(0))) { List<D> f0 = new
	 * LinkedList<>(); f0.add(0, h); Triple<D, D, List<D>> key =
	 * dst.find(dst.getKB(), new Triple<>(t.first, f.second, f0),
	 * dst.cat().hom(t.first, f.second)); theta1.put(f, theta.get(c).get(key));
	 * } theta0.put(c, theta1); }
	 * 
	 * List lhs = new LinkedList<>(); lhs.add(theta); lhs.add(h); List rhs = new
	 * LinkedList<>(); rhs.add(theta0); eqs.add(new Pair<>(lhs, rhs)); } }
	 * 
	 * return new XCtx(new HashSet<>(), types, eqs, src.global, dst,
	 * "instance"); }
	 */

	public XCtx<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> pi(XCtx<C> I) {
		Map<D, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>> thetas_d = makeThetas2(I);
		//System.out.println("` with thetas: " + thetas_d);

		Map types = new HashMap<>();
		for (D d : dst.allIds()) {
			for (Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta : thetas_d.get(d)) {
				types.put(theta, new Pair<>("1", d));				
			}
		}
		

		Set<Pair<List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>>> eqs = new HashSet<>();
		for (D h : dst.allTerms()) {
			Pair<D, D> t = dst.type(h);
			for (Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta : thetas_d
					.get(t.first)) {
				Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta0 = new HashMap<>();

				for (C c : src.allIds()) {
			//		Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>> theta1 = new HashMap<>();
					for (Triple<D, D, List<D>> f : dst.cat().hom(t.second, em.get(c).get(0))) {
						List<D> f0 = new LinkedList<>();
						f0.add(h);
						f0.addAll(f.third);
						Triple<D, D, List<D>> key = dst.find(dst.getKB(), new Triple<>(t.first,
								f.second, f0), dst.cat().arrows()); // dst.cat().hom(t.first,
																	// f.second));
						if (key == null) {
							throw new RuntimeException("Cannot find " + key + " in "
									+ dst.cat().arrows());
						}
						if (theta.get(new Pair<>(c, key)) == null) {
							throw new RuntimeException("Cannot find " + new Pair<>(c, key) + " in " + theta);
						}
						theta0.put(new Pair<>(c,f), theta.get(new Pair<>(c, key)));
					}
				//	theta0.put(c, theta1);
				}

				List lhs = new LinkedList<>();
				lhs.add(theta);
				lhs.add(h);

				List rhs = new LinkedList<>();
				rhs.add(theta0);
				eqs.add(new Pair<>(lhs, rhs));
			}
		}
		return new XCtx(new HashSet<>(), types, eqs, src.global, dst, "instance");
	}

	/*
	 * private boolean pifilter(D d, C c, Triple<D, D, List<D>> f, Triple<C, C,
	 * List<C>> k) { for (C g : src.allTerms()) { Pair<C, C> t = src.type(g); if
	 * (!t.first.equals(c)) { continue; } List<D> fFg = em.get(g); fFg.addAll(0,
	 * f.third); Triple<D, D, List<D>> found = dst.find(dst.getKB(), new
	 * Triple<D, D, List<D>>(f.first, dst.type(fFg).second, fFg),
	 * dst.cat().arrows()); Triple<C, C, List<C>> lhs =
	 * cand.get(t.second).get(found);
	 * 
	 * Triple<C, C, List<C>> rhs1 = cand.get(t.first).get(f); List<C> rhsx = new
	 * LinkedList<>(rhs1.third); rhsx.add(g); Triple<C, C, List<C>> rhs2 = new
	 * Triple<>(rhs1.first, t.second, rhsx); Triple<C, C, List<C>> rhs =
	 * I.find(I.getKB(), rhs2, I.cat().arrows());
	 * 
	 * if (!lhs.first.equals(rhs.first)) { throw new RuntimeException(); } if
	 * (!lhs.second.equals(rhs.second)) { throw new RuntimeException(); } if
	 * (!I.getKB().equiv(lhs.third, rhs.third)) { continue outer; //
	 * thetas.add(cand); } } // thetas.add((Map<C, Map<Triple<D, D, List<D>>,
	 * Triple<C, C, List<C>>>>) ((Object) cand)); // types.put( // (Map<C,
	 * Map<Triple<D, D, List<D>>, Triple<C, C, List<C>>>>) ((Object) cand), //
	 * new Pair<>((D) "1", d));
	 * 
	 * } }
	 */

	private Map<D, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>> makeThetas(
			XCtx<C> I) {

		Category<C, Triple<C, C, List<C>>> scat = src.small_cat();
		Category<D, Triple<D, D, List<D>>> dcat = dst.small_cat();

		FUNCTION<C, Set> o = c -> {
			return I.cat().hom((C) "1", c);
		};

		FUNCTION<Triple<C, C, List<C>>, Fn> a = arr -> {
			FUNCTION f = tr -> {
				Triple<C, C, List<C>> input = (Triple<C, C, List<C>>) tr;
				List<C> l = new LinkedList<>(input.third);
				l.addAll(arr.third);
				Triple<C, C, List<C>> tofind = new Triple<>((C) "1", arr.second, l);
				Object y = I.find(I.getKB(), tofind, I.cat().hom((C) "1", arr.second));
				if (y == null) {
					throw new RuntimeException("On arrow " + arr + ", input ID " + tr + ", tofind "
							+ tofind + ", not in " + I.cat().hom((C) "1", arr.second));
				}
				return y;
			};
			return new Fn(o.apply(arr.first), o.apply(arr.second), f);
		};

		//
		Functor<C, Triple<C, C, List<C>>, Set, Fn> I0 = new Functor<>(scat, FinSet.FinSet, o, a);

		FUNCTION<C, D> o2 = c -> {
			return em.get(c).get(0);
		};
		FUNCTION<Triple<C, C, List<C>>, Triple<D, D, List<D>>> a2 = x -> {
			List<C> l = new LinkedList<>(x.third);
			l.add(0, x.first);
			Triple<D, D, List<D>> t = new Triple<>(em.get(x.first).get(0), em.get(x.second).get(0),
					apply(l));
			return dst.find(dst.getKB(), t, dst.cat().hom(t.first, t.second));
		};

		Functor F = new Functor<>(scat, dcat, o2, a2);
		//

		// maps number -> value, colnames
		System.out.println("Starting pi");
		String old_lineage = DEBUG.debug.piLineage;
		DEBUG.debug.piLineage = "Lineage as ID";
		Triple<Functor<Object, Object, Set, FinSet.Fn>, Map<Object, Set<Map>>, Map<Object, Triple<Object, C, Object>[]>> J0 = Pi
				.pi(F, I0);
		DEBUG.debug.piLineage = old_lineage;
		System.out.println("done");
		Map<D, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>> ret = new HashMap<>();

		for (D d : dst.allIds()) {
			if (d.equals("1")) {
				continue;
			}
			List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> l = new LinkedList<>();
			Triple<Object, C, Object>[] cols = J0.third.get(d);
			Set<Map> maps = J0.second.get(d);
//			System.out.println("Maps: " + maps);
			for (Map m : maps) {
				Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> n = new HashMap<>();
				int i = 0;
				for (Triple<Object, C, Object> col : cols) {
	//				System.out.println("col " + col.third + " " + col.getClass());
		//			System.out.println("mget " + m.get(i) + " " + m.get(i).getClass());
					n.put(new Pair<>(col.second, (Triple<D, D, List<D>>) col.third), (Triple<C, C, List<C>>) ((Map)m.get(0)).get(i));
					i++;
				}
				l.add(n);
			}
			ret.put(d, l);
		}
		ret.put((D)"1", pi_old(I));
		return ret;
	}

	public XMapping<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>, Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> piT(XMapping<C, C> t) {
		XCtx<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> src = pi(t.src);
		XCtx<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> dst = pi(t.dst);
		
		Map em = new HashMap<>();
		
		for (Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta : src.terms()) {
			Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta2 = new HashMap<>();
			for (Pair<C, Triple<D, D, List<D>>> cf : theta.keySet()) {
				Triple<C, C, List<C>> i = theta.get(cf);
				List<C> i2 = new LinkedList<>(i.third);
				i2.add(0, i.first);
				List<C> i2ap = t.apply(i2);
				Triple<C, C, List<C>> i2ap0 = new Triple<>(i.first, i.second, i2ap);
				Triple<C, C, List<C>> found = t.dst.find(t.dst.getKB(), i2ap0, t.dst.cat().hom(i.first, i.second));
				if (found == null) {
					throw new RuntimeException("not found " + i2ap0 + " in " + t.src.cat().hom(i.first, i.second));
				}
				theta2.put(cf, found);
			}
			List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> l = new LinkedList<>();
			l.add(theta2);
			em.put(theta, l);
		}
		
        for (Object o : src.allTerms()) {
			if (em.containsKey(o)) {
				continue;
			}
			List l = new LinkedList();
			l.add(o);
			em.put(o, l);
		} 
		
		return new XMapping<>(src, dst, em, "homomorphism");
	}

	public List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> pi_old(XCtx<C> I) {
	    Map<Pair<C, Triple<D, D, List<D>>>, List<Triple<C, C, List<C>>>> theta = new HashMap<>();
		for (C c : src.allIds()) {
			for (Triple<D, D, List<D>> f : dst.cat().hom((D)"1", em.get(c).get(0))) {
				theta.put(new Pair<>(c, f), new LinkedList<>(I.cat().hom((C) "1", c)));
			}
		}
		List<LinkedHashMap<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> thetas = FinSet.homomorphs(theta);
		List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> ret = new LinkedList<>();
		outer: for (LinkedHashMap<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> cand : thetas) {
			for (C c : src.allIds()) {
				for (Triple<D, D, List<D>> f : dst.cat().hom((D)"1", em.get(c).get(0))) {
					for (C g : src.allTerms()) {
						Pair<C, C> t = src.type(g);
						if (!t.first.equals(c)) {
							continue;
						}
						List<D> fFg = new LinkedList<>(em.get(g));
						fFg.addAll(0, f.third);
						Triple<D, D, List<D>> found = dst.find(dst.getKB(),
								new Triple<D, D, List<D>>(f.first, dst.type(fFg).second, fFg),
								dst.cat().arrows());
						Triple<C, C, List<C>> lhs = cand.get(new Pair<>(t.second, found));

						Triple<C, C, List<C>> rhs1 = cand.get(new Pair<>(t.first, f));
						List<C> rhsx = new LinkedList<>(rhs1.third);
						rhsx.add(g);
						Triple<C, C, List<C>> rhs2 = new Triple<>(rhs1.first, t.second, rhsx);
						Triple<C, C, List<C>> rhs = I.find(I.getKB(), rhs2, I.cat().arrows());

						if (!lhs.first.equals(rhs.first)) {
							throw new RuntimeException();
						}
						if (!lhs.second.equals(rhs.second)) {
							throw new RuntimeException();
						}
						if (!I.getKB().equiv(lhs.third, rhs.third)) {
							continue outer;
						}
					}
					ret.add((Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>) ((Object) cand));
				}
			}
		}
		return ret;
	}
	
	
	boolean fill(XCtx<C> I, Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta,
	        Map<Pair<C, Triple<D, D, List<D>>>, Pair<C, Triple<D, D, List<D>>>> theta2,
   			Pair<C, Triple<D, D, List<D>>> tag, Triple<C, C, List<C>> y) {
		C c = tag.first;
		Triple<D, D, List<D>> f = tag.second;
		for (C c0 : src.allIds()) {
			for (Triple<C, C, List<C>> g : src.cat().hom(c, c0)) {
				List g0 = new LinkedList<>(g.third);
				g0.add(0, g.first);
				List<D> fFg = apply(g0);
				fFg.addAll(0, f.third);
				fFg.add(0, f.first);
				D t = em.get(c0).get(0);
				Triple<D, D, List<D>> found = dst.find(dst.getKB(), new Triple<>(f.first, t, fFg), dst.cat().hom(f.first, t));
				Triple<C, C, List<C>> val = theta.get(new Pair<>(c0, found));
				
				List<C> yIg = new LinkedList<>(y.third);
				yIg.add(0, y.first);
				yIg.addAll(g.third);
				Triple<C, C, List<C>> found2 = I.find(I.getKB(), new Triple<>((C)"1", g.second, yIg), I.cat().hom((C)"1", g.second));
		
				if (val == null) {
					theta.put (new Pair<>(c0, found), found2);
					theta2.put(new Pair<>(c0, found), tag);
				} else if (!I.getKB().equiv(val.third, found2.third)) {
					return false;
				}
			}
		}
		return true;
	}

	void cleanup(Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta,
	        Map<Pair<C, Triple<D, D, List<D>>>, Pair<C, Triple<D, D, List<D>>>> theta2,
   			Pair<C, Triple<D, D, List<D>>> tag) {
		for (Pair<C, Triple<D, D, List<D>>> k : theta.keySet()) {
			Pair<C, Triple<D, D, List<D>>> v = theta2.get(k);
			if (v == null) { 
				continue;
			}
			if (v.equals(tag)) {
				theta.put(k , null);
				theta2.put(k, null);
			}
		}
	}

	
	void try_branch(XCtx<C> I,
			        List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> thetas,
			        Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta,
			        Map<Pair<C, Triple<D, D, List<D>>>, Pair<C, Triple<D, D, List<D>>>> theta2,
           			Pair<C, Triple<D, D, List<D>>> tag, Triple<C, C, List<C>> y) {
		C c = tag.first;
		Triple<D, D, List<D>> f = tag.second;
		
		theta.put(new Pair<>(c, f) , y);
		theta2.put(new Pair<>(c, f), new Pair<>(c, f));
		if (fill(I, theta, theta2, tag, y)) {
			Pair<C, Triple<D, D, List<D>>> c0f0 = next(theta, tag);
			if (c0f0 != null) {
				C c0 = c0f0.first;
				Triple<D, D, List<D>> f0 = c0f0.second;
				for (Triple<C, C, List<C>> x0 : I.cat().hom((C)"1", c0)) {
					try_branch(I, thetas, theta, theta2, c0f0, x0);
				}
			} else {
				for (Pair<C, Triple<D, D, List<D>>> k : theta.keySet()) {
					if (theta.get(k) == null) {
						throw new RuntimeException("Tried to add " + theta);
					}
				}
//				System.out.println("adding " + theta);
				thetas.add(new HashMap<>(theta));
			}
		}
		cleanup(theta, theta2, tag);
	}

	private Pair<C, Triple<D, D, List<D>>> next(
			Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta,
			Pair<C, Triple<D, D, List<D>>> tag) {
		boolean seen = false;
		for (Pair<C, Triple<D, D, List<D>>> k : theta.keySet()) {
			if (k.equals(tag)) {
				seen = true;
			}
			if (seen) {
				if (theta.get(k) == null) {
					return k;
				}
			}
		}
		if (!seen) {
			throw new RuntimeException(tag +  " not seen in " + theta);
		}
		return null;
	}

	private Map<D, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>> makeThetas2(
			XCtx<C> I) {
		
		Map<D, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>> ret = new HashMap<>();
		
		for (D d : dst.allIds()) {
			List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> thetas = new LinkedList<>();
			Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta = new LinkedHashMap<>();
			Map<Pair<C, Triple<D, D, List<D>>>, Pair<C, Triple<D, D, List<D>>>> theta2 = new LinkedHashMap<>();
			for (C c : src.allIds()) {
				for (Triple<D, D, List<D>> f : dst.cat().hom(d, em.get(c).get(0))) {
					theta.put(new Pair<>(c, f), null);
					theta2.put(new Pair<>(c, f), null);
				}
			}
			for (Pair<C, Triple<D, D, List<D>>> cf : theta.keySet()) {
				for (Triple<C, C, List<C>> x : I.cat().hom((C)"1", cf.first)) {
					try_branch(I, thetas, theta, theta2, cf, x);
					break;
				}
				break;
			}
			ret.put(d, thetas);
		}
		
		return ret;
	}

	public XMapping pi_unit(XCtx<D> J) {
		XCtx<Pair<Triple<D,D,List<D>>, C>> deltaI = delta(J); //XCtx<Pair<Triple<D, D, List<D>>, C>>
		XCtx pideltaI = pi((XCtx)deltaI);
		//	public XCtx<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> pi(XCtx<C> I) {

		Map m = new HashMap();
		
		for (D x : J.terms()) {
			D d = J.type(x).second;
			if (!J.type(x).first.equals("1")) {
				throw new RuntimeException();
			}
			Map theta = new HashMap();
			for (C c : src.allIds()) {
				for (Triple<D, D, List<D>> f : dst.cat().hom(d, em.get(c).get(0))) {
					List<D> tofind = new LinkedList<>();
					tofind.add(x);
					tofind.addAll(f.third);
					Triple<D, D, List<D>> found = J.find(J.getKB(), new Triple<>((D)"1", f.second, tofind), J.cat().hom((D)"1", f.second));
					if (found == null) {
						throw new RuntimeException();
					}
					List<Pair<Triple<D,D,List<D>>, C>> g = new LinkedList<>();
					g.add(new Pair<>(found, c));
					//Triple<Pair<Triple<D,D,List<D>>, C>, Pair<Triple<D,D,List<D>>, C>, List<Pair<Triple<D,D,List<D>>, C>>> tr 
					Triple tr = new Triple<>("1", c, g);
					//	Triple<C,C,List<Pair<Triple<D,D,List<D>>, C>>> tr = new Triple<>((C)"1", c, g);
					Object xxx = deltaI.find(deltaI.getKB(), tr, deltaI.cat().arrows());
					theta.put(new Pair<>(c, f), xxx);
				}
			}
			List l = new LinkedList();
			l.add(theta);
			m.put(x, l);
		}
		
		for (Object o : dst.allTerms()) {
			if (m.containsKey(o)) {
				continue;
			}
			List l = new LinkedList();
			l.add(o);
			m.put(o, l);
		} 
		
//		System.out.println(Util.sep(pideltaI.terms(), "\n\n"));
		   
		return new XMapping(J, pideltaI, m, "homomorphism");
	}

	public XMapping pi_counit(XCtx<C> I) {
		XCtx<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> piI = pi(I); //XCtx<Pair<Triple<D, D, List<D>>, C>>
		XCtx deltapiI = delta((XCtx)piI); //<Pair<Triple<D, D, List<D>>, C>>
		
		//System.out.println(deltapiI);
		
		Map m = new HashMap();
		
		for (Object x : deltapiI.terms()) {
			Pair<Triple<?,?,List>, C> x0 = (Pair<Triple<?,?,List>, C>) x;
			if (src.cat().arrows().contains(x0.first)) {
				List l = new LinkedList();
				l.add(x0.first.first);
				l.addAll(x0.first.third);
				m.put(x, l);
				continue;
			}			
			if (x0.first.third.size() != 1) {
				throw new RuntimeException();
			}
			Map theta = (Map) x0.first.third.get(0);
			
			Object o = theta.get(new Pair<>(x0.second, new Triple<>(em.get(x0.second).get(0), em.get(x0.second).get(0), new LinkedList<>())));
			if (o == null) {
				throw new RuntimeException();
			}
			List l = new LinkedList();
			l.add(((Triple)o).first);
			l.addAll((List)((Triple)o).third);
			m.put(x, l);
		}

		for (Object o : src.allTerms()) {
			if (m.containsKey(o)) {
				continue;
			}
			List l = new LinkedList();
			l.add(o);
			m.put(o, l);
		} 

		return new XMapping(deltapiI, I, m, "homomorphism");
	}

}








