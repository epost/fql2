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
				for (Triple<C, C, List<C>> arr : src.cat().hom((C) "_1", id)) { // arrows())
																				// {
					List<C> toApply = new LinkedList<>(arr.third);
					toApply.add(0, arr.first);
					List<D> applied = apply(toApply);
					for (Triple<D, D, List<D>> cand : dst.cat().hom((D) "_1", (D) id)) {
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

	public XCtx<D> apply0(XCtx<C> I) {
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

		return new XCtx(new HashSet<>(), ret, eqs, I.global, dst, "instance");
	}

	public static XMapping<String, String> make(XEnvironment eNV, XCtx<String> src,
			XCtx<String> dst, XMapConst m) {
		Map<String, List<String>> ret = new HashMap<>();
		List<String> one = new LinkedList<>();
		one.add("_1");
		ret.put("_1", one);
		List<String> ggg = new LinkedList<>();
		ggg.add("!__1");
		ret.put("!__1", ggg);

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
		//	 if (l.equals("_1")) {
		//		 continue;
		//	 }
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
			if (l.startsWith("!") || l.startsWith("_1")) {
				continue;
			}
			List ls = new LinkedList<>();
			ls.add(l);
			ret.put(l, ls);
		}

		return new XMapping<>(src, dst, ret, "mapping");
	}

	//TODO
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

		Map ret = new HashMap<>(); //TODO
		for (String c : src.schema.allTerms()) {
			List<String> l = new LinkedList<>();
			l.add(c);
			ret.put(c, l);
		}

		//will mutate in place
		for (Pair<Pair<String,String>, List<String>> k : e.vm) {
			boolean leftok = false;
			boolean rightok = false;
			Pair lhs = new Pair<>(k.first.first, k.first.second);
			if (k.first.second == null) {
				Set cands = new HashSet();
				for (Object o : src.terms()) {
					if (((Pair)o).first.equals(lhs.first)) {
						cands.add(o);
					}
				}
				if (cands.size() == 1) {
					lhs = (Pair) new LinkedList<>(cands).get(0);
					leftok = true;
				}
			}
			List rhs = new LinkedList<>(k.second);
			Set sofar = new HashSet();
			List l = new LinkedList();
			sofar.add(l);
			Set<List> rhs0 = XCtx.expand(sofar, rhs, dst.schema, dst);
			if (rhs0.size() == 1) {
				rhs = (List) new LinkedList(rhs0).get(0);
				rightok = true;
			}
			//v:t -> u:?
			if (!rightok && leftok && rhs.size() == 1 && dst.terms().contains(rhs.get(0))) {
				List rhsX = new LinkedList<>();
				rhsX.add(new Pair<>(((Pair)rhs.get(0)).first, lhs.second));
				rightok = true;
			}
			//v:? -> p:t
			if (!leftok && rightok) {
				Pair p = dst.type(rhs);
				lhs.second = p.second;
				leftok = true;
			}
			
			if (!src.terms().contains(lhs)) {
				throw new RuntimeException("Source does not contain variable " + k.first);
			}
			if (src.schema.allTerms().contains(lhs)) {
				throw new RuntimeException("Not a variable: " + k.first);
			}
			if (ret.containsKey(lhs)) {
				throw new RuntimeException("Duplicate node mapping for " + k.first);
			}
			ret.put(lhs, rhs);
		}
		for (Object c : src.allTerms()) {
			if (!ret.containsKey(c)) {
				throw new RuntimeException("Does not map " + c);
			}
		}
		// System.out.println("returning " + ret);
		return new XMapping<>(src, dst, ret, "homomorphism");
	}

	// on transforms
	public XObject apply(XMapping<C, C> t) {
		XCtx<D> src0 = this.apply0(t.src);
		XCtx<D> dst0 = this.apply0(t.dst);

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
		XCtx<D> FI = apply0(I);
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
		XCtx<Pair<Triple<D, D, List<D>>, C>> FFI = f.apply0(FI);

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
			for (Triple<D, D, List<D>> arr : I.cat().hom((D) "_1", em.get(c).get(0))) {
				if (I.global.cat().objects().contains(c)) {
					if (I.global.cat().hom((D) "_1", (D)c).contains(arr)) {
						continue;
					}
				}
				Pair tr = new Pair(arr, c);
				types.put(tr, new Pair("_1", c));
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
				//	System.out.println("looking for " + rhs + " in\n\n" + Util.sep(I.cat().hom(dsrc, ddst), "\n\n"));
					Triple rhsX = XCtx.find(I.getKB(), rhs, I.cat().hom(dsrc, ddst));
					List g = new LinkedList<>();
					g.add(new Pair<>(rhsX, t.second));

					if (I.global.allIds().contains(t.second)) { //a' in G
						Triple ooo = XCtx.find(I.global.getKB(), rhsX,
							I.global.cat().arrows());
						if (ooo != null) {
							List lll = new LinkedList();
							if (((List)ooo.third).isEmpty()) {
								lll.add(ooo.first);
							}
							lll.addAll((List)ooo.third);
							
							eqs.add(new Pair(lhs, lll));
						} else {
							eqs.add(new Pair<>(lhs, g));
						}
					} else {
						eqs.add(new Pair<>(lhs, g));
					}
				}
			}
		}


		XCtx ret = new XCtx<Pair<Triple<D, D, List<D>>, C>>(ids, types, eqs,
				(XCtx<Pair<Triple<D, D, List<D>>, C>>) src.global,
				(XCtx<Pair<Triple<D, D, List<D>>, C>>) src, "instance");
		ret.saturated = I.saturated;
		return ret;
	}



	public XCtx<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>> pi(XCtx<C> I) {
		Map<D, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>> thetas_d = makeThetas2(I);
		//System.out.println("` with thetas: " + thetas_d);

		Map types = new HashMap<>();
		for (D d : dst.allIds()) {
			for (Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta : thetas_d.get(d)) {
				types.put(theta, new Pair<>("_1", d));				
			}
		}
		
		Set<Pair<List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>, List<Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>>>>> eqs = new HashSet<>();
		for (D h : dst.allTerms()) {
			Pair<D, D> t = dst.type(h);
			for (Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta : thetas_d
					.get(t.first)) {
				Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta0 = new HashMap<>();

				for (C c : src.allIds()) {
			//		if (c.equals("_1")) {
				//		continue;
				//	}
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
				
				boolean found = false;
				for (Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> thetaX : thetas_d.get(t.second)) {
					if (theta0.equals(thetaX)) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("At h=" + h + ": " + t + ", Constructed theta^prime " + theta0 + " not found in\n\n" + Util.sep(thetas_d.get(t.second), "\n"));
				}

				List lhs = new LinkedList<>();
				lhs.add(theta);
				lhs.add(h);

				List rhs = new LinkedList<>();
				rhs.add(theta0);
				eqs.add(new Pair<>(lhs, rhs));
			}
		}
		XCtx ret = new XCtx(new HashSet<>(), types, eqs, src.global, dst, "instance");
		//ret.saturated = true; //TODO
		return ret;
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
				if (found == null) {
					throw new RuntimeException();
				}
				Triple<C, C, List<C>> val = theta.get(new Pair<>(c0, found));
				List<C> yIg = new LinkedList<>(y.third);
				yIg.add(0, y.first);
				yIg.addAll(g.third);
				Triple<C, C, List<C>> found2 = I.find(I.getKB(), new Triple<>((C)"_1", g.second, yIg), I.cat().hom((C)"_1", g.second));
				if (found2 == null) {
					throw new RuntimeException();
				}
				if (val == null) {
					if (!theta.containsKey(new Pair<>(c0, found))) {
						throw new RuntimeException();
					}
					theta.put (new Pair<>(c0, found), found2);
					theta2.put(new Pair<>(c0, found), tag);
			//		System.out.println(theta.keySet());
				} else if (!I.getKB().equiv(val.third, found2.third)) {
			//		System.out.println("contra");
					return false;
				}
			}
		}
		return true;
	}

	//TODO: check on cleanup
	void cleanup(Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta,
	        Map<Pair<C, Triple<D, D, List<D>>>, Pair<C, Triple<D, D, List<D>>>> theta2,
   			Pair<C, Triple<D, D, List<D>>> tag, Triple<C, C, List<C>> y) {
		//System.out.println(theta.keySet());
		for (Pair<C, Triple<D, D, List<D>>> k : theta.keySet()) {
			Pair<C, Triple<D, D, List<D>>> v = theta2.get(k);
			if (v == null) { 
				continue;
			}
			if (v.equals(tag) /*&& theta.get(k).equals(y)*/ ) {
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
	//	System.out.println("try_branch at " + theta + " and " + theta2 + " and tag= " + tag + " y= " + y);
		C c = tag.first;
		Triple<D, D, List<D>> f = tag.second;
		if (!theta.keySet().contains(new Pair<>(c, f))) {
			throw new RuntimeException();
		}
		theta.put(new Pair<>(c, f) , y);
		theta2.put(new Pair<>(c, f), new Pair<>(c, f));
		boolean contra = fill(I, theta, theta2, tag, y);
	//	System.out.println("filled " + theta);
		if (contra) {
			Pair<C, Triple<D, D, List<D>>> c0f0 = next(theta, tag);
			if (c0f0 != null) {
				C c0 = c0f0.first;
				Triple<D, D, List<D>> f0 = c0f0.second;
				for (Triple<C, C, List<C>> x0 : I.cat().hom((C)"_1", c0)) {
					try_branch(I, thetas, theta, theta2, c0f0, x0);
				}
			} else {
			//	System.out.println(theta.keySet());
				for (Pair<C, Triple<D, D, List<D>>> k : theta.keySet()) {
					if (theta.get(k) == null) {
						throw new RuntimeException("Tried to add " + theta);
					}
				}
			//	System.out.println("adding " + theta);
			//	System.out.println(theta.keySet());
			//	System.out.println("Added");
				thetas.add(new LinkedHashMap<>(theta)); //ok, irrelevent
			}
		} 
		cleanup(theta, theta2, tag, y);
		//System.out.println("cleans up to " + theta);
	}

	
	private Pair<C, Triple<D, D, List<D>>> next(
			Map<Pair<C, Triple<D, D, List<D>>>, Triple<C, C, List<C>>> theta,
			Pair<C, Triple<D, D, List<D>>> tag) {
		boolean seen = false;
		//System.out.println(theta.keySet());
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
			Map<Pair<C, Triple<D, D, List<D>>>, Pair<C, Triple<D, D, List<D>>>> theta2 = new LinkedHashMap<>(); //ok (irrelevent)
			for (C c : src.allIds()) {
				for (Triple<D, D, List<D>> f : dst.cat().hom(d, em.get(c).get(0))) {
					theta.put(new Pair<>(c, f), null);
					theta2.put(new Pair<>(c, f), null);
				}
			}
//			System.out.println("at " + d + " ordering is " + theta.keySet());
			for (Pair<C, Triple<D, D, List<D>>> cf : theta.keySet()) {
				for (Triple<C, C, List<C>> x : I.cat().hom((C)"_1", cf.first)) {
					try_branch(I, thetas, theta, theta2, cf, x);
					
					//break;
				}
				break;
			}
	//		System.out.println("at " + d + ", " + thetas.size());
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
			if (!J.type(x).first.equals("_1")) {
				throw new RuntimeException();
			}
	
			Map theta = new HashMap();
			for (C c : src.allIds()) {
				for (Triple<D, D, List<D>> f : dst.cat().hom(d, em.get(c).get(0))) {

					List<D> tofind = new LinkedList<>();
					tofind.add(x);
					tofind.addAll(f.third);
					Triple<D, D, List<D>> found = J.find(J.getKB(), new Triple<>((D)"_1", f.second, tofind), J.cat().hom((D)"_1", f.second));
					if (found == null) {
						throw new RuntimeException();
					}
					List<Pair<Triple<D,D,List<D>>, C>> g = new LinkedList<>();
					g.add(new Pair<>(found, c));
					//Triple<Pair<Triple<D,D,List<D>>, C>, Pair<Triple<D,D,List<D>>, C>, List<Pair<Triple<D,D,List<D>>, C>>> tr 
					Triple tr = new Triple<>("_1", c, g);
					//	Triple<C,C,List<Pair<Triple<D,D,List<D>>, C>>> tr = new Triple<>((C)"1", c, g);
					Object xxx = deltaI.find(deltaI.getKB(), tr, deltaI.cat().arrows());
					
					if (xxx == null) {
						throw new RuntimeException("cannot find " + tr);
					}
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

	//TODO: maybe should not have extra constants in delta?
	//TODO: or, not need map all vars? only need map eqcs?
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

	public XMapping<C, D> rel() {
		return new XMapping<>(src.rel(), dst.rel(), new HashMap<>(em), "homomorphism");
	}

}








