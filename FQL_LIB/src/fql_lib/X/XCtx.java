package fql_lib.X;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Unit;
import fql_lib.Util;
import fql_lib.X.XExp.XInst;
import fql_lib.X.XExp.XSchema;
import fql_lib.cat.Category;
import fql_lib.cat.KB;
import fql_lib.gui.FQLTextPanel;

public class XCtx<C> implements XObject {

	private KB<C> kb;
	Set<C> ids;
	XCtx<C> global;
	XCtx<C> schema;
	Map<C, Pair<C, C>> types;
	Set<Pair<List<C>, List<C>>> eqs;
	private boolean initialized = false;
	
	String kind = "TODO";
	@Override
	public String kind() {
		return kind;
	}

	public Set<C> terms() {
		return types.keySet();
	}
	
	public Set<C> allIds() {
		Set<C> ret = new HashSet<>(ids);
		if (schema != null) {
			ret.addAll(schema.ids);
		}
		if (global != null) {
			ret.addAll(global.ids);
		}
		return ret;
	}
	public Set<C> allTerms() {
		Set<C> ret = new HashSet<>(terms());
		if (schema != null) {
			ret.addAll(schema.terms());
		}
		if (global != null) {
			ret.addAll(global.terms());
		}
		return ret;
	}
	public Set<Pair<List<C>, List<C>>> allEqs() {
		Set<Pair<List<C>, List<C>>> ret = new HashSet<>(eqs);
		if (schema != null) {
			ret.addAll(schema.eqs);
		}
		if (global != null) {
			ret.addAll(global.eqs);
		}
		return ret;
	}
	
	public Pair<C, C> type(C c) {
		Pair<C, C> ret = types.get(c);
		if (ret != null) {
			return ret;
		}
		if (schema != null) {
			return schema.type(c);
		}
		if (global != null) {
			return global.type(c);
		}
		throw new RuntimeException("Cannot type " + c + " in " + this);
	}
	
//	public XCtx<C> copy() {
//		return new XCtx<>(new HashSet<>(ids), new HashSet<>(consts), new HashMap<>(typeOf),
	//			new HashSet<>(eqs), new HashSet<>(local));
	//	throw new RuntimeException();
//	}

	public XCtx(Set<C> ids, Map<C, Pair<C, C>> types,
			Set<Pair<List<C>, List<C>>> eqs, XCtx<C> global, XCtx<C> schema, String kind) {
		this.types = types;
		this.eqs = eqs;
		this.ids = ids;
		this.global = global;
		this.schema = schema;
//		this.local = local;
		init();
		this.kind = kind;
		//validate();
	}

	public KB<C> getKB() {
	//	init();
		return kb;
	}

	private void init() {
		if (initialized) {
			return;
		}

		validate();

		for (C c : ids) {
			types.put((C) ("!_" + c), new Pair<>(c, (C) "1"));
		}
	//	types.put((C) ("!_1"), new Pair<>((C) "1", (C) "1"));
		List lhs = new LinkedList<>();
		lhs.add("1");
		List rhs = new LinkedList<>();
		rhs.add("!_" + 1);
		eqs.add(new Pair<>(lhs, rhs));

		for (C c : terms()) {
			Pair<C, C> t = types.get(c);
			lhs = new LinkedList<>();
			lhs.add(c);
			lhs.add("!_" + t.second);
			rhs = new LinkedList<>();
			rhs.add("!_" + t.first);
			eqs.add(new Pair<>(lhs, rhs));
		}

		validate();
		kb();
		initialized = true;
	}

	private void kb() {
		Set<Pair<List<C>, List<C>>> rules = new HashSet<>(allEqs());
		for (C id : allIds()) {
			List<C> l = new LinkedList<>();
			l.add(id);
			rules.add(new Pair<>(l, new LinkedList<>()));
		}
		kb = new KB<C>(rules, 32); // TODO
	}

	private void validate() {
		if (!types.keySet().containsAll(ids)) {
			throw new RuntimeException("ids not contained in const");
		}
		Set<C> values = types.values().stream().flatMap(x -> { Set<C> ret = new HashSet<>(); ret.add(x.first); ret.add(x.second); return ret.stream(); }).collect(Collectors.toSet());
		if (!allIds().containsAll(values)) {
			values.removeAll(allIds());
			throw new RuntimeException("typeof returns non-ids: " + values);
		}
		for (C c : ids) {
			Pair<C, C> t = types.get(c);
			if (!t.first.equals(t.second)) {
				throw new RuntimeException("Not identity " + c);
			}
		}

		for (Pair<List<C>, List<C>> eq : eqs) {
			if (!allTerms().containsAll(eq.first)) {
				throw new RuntimeException("unknown const: " + eq.first + " in " + this + " (first)");
			}
			if (!allTerms().containsAll(eq.second)) {
				throw new RuntimeException("unknown const: " + eq.second + " in " + this + " (second)");
			}
			if (!type(eq.first).equals(type(eq.second))) {
				throw new RuntimeException("Type mismatch on " + eq);
			}
		} 
	}

	public Pair<C, C> type(List<C> first) {
		if (first.size() == 0) {
			throw new RuntimeException("Empty");
		}
		Iterator<C> it = first.iterator();
		Pair<C, C> ret = type(it.next());
		while (it.hasNext()) {
			Pair<C, C> next = type(it.next());
			if (!ret.second.equals(next.first)) {
				throw new RuntimeException("Ill-typed: " + first);
			}
			ret = new Pair<>(ret.first, next.second);
		}
		return ret;
	}

	@Override
	public String toString() {
		String kb_text = "types:\n  " + Util.sep(allIds(), ",\n  ");
		List<String> tt = allTerms().stream()
				.map(x -> x + " : " + type(x).first + " -> " + type(x).second)
				.collect(Collectors.toList());
		kb_text = kb_text.trim();
		kb_text += "\n\nterms:\n  " + Util.sep(tt, ",\n  ");
		List<String> xx = allEqs().stream()
				.map(x -> Util.sep(x.first, ".") + " = " + Util.sep(x.second, "."))
				.collect(Collectors.toList());
		kb_text = kb_text.trim();
		kb_text += "\n\nequations:\n  " + Util.sep(xx, ",\n  ");
		kb_text = kb_text.trim();
		kb_text += "\n\nlocal: " + terms();

		return kb_text;
	}

	@Override
	public JComponent display() {
		init();
		String kb_text = "types:\n  " + Util.sep(allIds(), ",\n  ");
		List<String> tms = allTerms().stream()
				.map(x -> x + " : " + type(x).first + " -> " + type(x).second)
				.collect(Collectors.toList());
		kb_text = kb_text.trim();
		kb_text += "\n\nterms:\n  " + Util.sep(tms, ",\n  ");
		List<String> xx = allEqs().stream()
				.map(x -> Util.sep(x.first, ".") + " = " + Util.sep(x.second, "."))
				.collect(Collectors.toList());
		kb_text = kb_text.trim();
		kb_text += "\n\nequations:\n  " + Util.sep(xx, ",\n  ");
		kb_text = kb_text.trim();

		try {
			kb.complete();
			kb_text += "\n\nKnuth-Bendix Completion:\n" + kb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			kb_text = "\n\nERROR in Knuth-Bendix\n\n" + e.getMessage();
		}

		String cat = "";
		try {
			cat = cat().toString();
		} catch (Exception e) {
			 e.printStackTrace();
			cat = "ERROR\n\n" + e.getMessage();
		}

		String small_cat = "TODO";
/*		try {
			small_cat = small_cat().toString();
		} catch (Exception e) {
			// e.printStackTrace();
			small_cat = "ERROR\n\n" + e.getMessage();
		} */

		JComponent kbc = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", kb_text);
		JComponent ctp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", cat);
		//JComponent small_ctp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", small_cat);

		JComponent tables = makeTables(x -> cat().arrows(), new HashSet<>());
//		JComponent tables2 = makeTables2(small_cat());
		
		JComponent graph = makeGraph();

		JTabbedPane ret = new JTabbedPane();
		ret.addTab("Text", kbc);
		ret.addTab("Graph", graph);
		ret.addTab("Category", ctp);
		if (schema != null) {
			ret.addTab("Full Tables", tables);
	//	ret.addTab("Category 2", small_ctp);
	//	if (schema != null) {
			ret.addTab("Adom Tables", makeTables(z -> foo(), global.ids));
		}
		return ret;
	}

	
	
	public JComponent makeGraph() {
		Graph<C,C> sgv = buildFromSig();
		
		Layout<C, C> layout = new FRLayout<>(sgv);
	//	Layout<C, C> layout = new ISOMLayout<>(sgv);
//		Layout<C, C> layout = new edu.uci.ics.jung.algorithms.layout.<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<C, C> vv = new VisualizationViewer<>(layout);
		vv.getRenderContext().setLabelOffset(20);
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		
		Transformer<C, Paint> vertexPaint = x -> { 
			if (global.terms().contains(x)) {
				return Color.RED;
			}
			return Color.GREEN;
/*			if (schema == null) {
				return Color.GREEN;
			} else {
				if (schema.terms().contains(x)) {
					return Color.GREEN;
				}
			}
			return Color.BLUE; */
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

	//	Transformer ttt = arg0 -> Util.nice(arg0.toString());
		Transformer<C,String> ttt = arg0 -> {
			String ret = arg0.toString();
			if (ret.length() > 16) {
				return ret.substring(0, 15) + "...";
			}
			return ret;
		};
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(ttt);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}
	
	private Graph<C, C> buildFromSig() {
		Graph<C, C> g2 = new DirectedSparseMultigraph<>();
		for (C n : allIds()) {
			g2.addVertex(n);
		}
		for (C e : allTerms()) {
			if (allIds().contains(e)) {
				continue;
			}
			if (e.toString().startsWith("!")) {
				continue;
			}
			g2.addEdge(e, type(e).first, type(e).second);
		}
		return g2;
	}
	
//	private Set<List<C>> hom(C src, C dst, int n);
	
	
	Map<Integer, Set<List<C>>> pathsUpTo = new HashMap<>();
	public Set<List<C>> pathsUpTo(int n) {
		if (pathsUpTo.containsKey(n)) {
			return pathsUpTo.get(n);
		}
		if (n == 0) {
			Set<List<C>> ret = new HashSet<>();
			for (C c : allIds()) {
				List<C> l = new LinkedList<>();
				l.add(c);
				ret.add(l);
			}
			pathsUpTo.put(0, ret);
			return ret;
		}
		Set<List<C>> set = new HashSet<>(pathsUpTo(n-1));
		Set<List<C>> toAdd = new HashSet<>();
		for (List<C> l : set) {
			for (C e : outEdges(typeAsMap(), type(l).second)) {
				List<C> r = new LinkedList<>(l);
				r.add(e);
				toAdd.add(r);
			}
		}
		set.addAll(toAdd);
		pathsUpTo.put(n, set);
		return set;
	}
	
	private Map<C, Pair<C,C>> typeAsMap() {
		Map<C, Pair<C,C>> ret = new HashMap<>(types);
		if (global != null) {
			ret.putAll(global.types);
		}
		if (schema != null) {
			ret.putAll(schema.types);
		}
		return ret;
	}

	public Set<List<C>> pathsUpTo(int n, C src, C dst) {
		Set<List<C>> ret = new HashSet<>();
		for (List<C> l : pathsUpTo(n)) {
			Pair<C,C> t = type(l);
			if (t.first.equals(src) && t.second.equals(dst)) {
				ret.add(l);
			}
		}
		return ret;
	}
	
	private List<Triple<C, C, List<C>>> foo() {
		//try {
			
			List<Triple<C, C, List<C>>> paths = new LinkedList<>();
			for (C c : allIds()) {
				paths.add(new Triple<>(c, c, new LinkedList<>()));
			}
			
			List<Triple<C, C, List<C>>> consts = new LinkedList<>();
			for (C c : global.terms()) {
				if (!global.ids.contains(c)) {
					if (global.type(c).first.equals("1")) {
						List<C> l = new LinkedList<>();
						l.add(c);
						consts.add(new Triple<>(global.type(c).first, global.type(c).second, l));
					}
				}
			}

			int iter = 0;
			for (; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
				Set<Triple<C, C, List<C>>> newPaths1 = extend2(paths, global.types, consts);
				paths.addAll(newPaths1);
				Set<Triple<C, C, List<C>>> newPaths2 = extend2(paths, schema.types, consts);
				paths.addAll(newPaths2);
				Set<Triple<C, C, List<C>>> newPaths3 = extend2(paths, types, consts);
				if (paths.containsAll(newPaths3)) {
					//need one more iteration for all attributes
					newPaths1 = extend2(paths, global.types, consts);
					paths.addAll(newPaths1);
					newPaths2 = extend2(paths, schema.types, consts);
					paths.addAll(newPaths2);
					break;
				}
				paths.addAll(newPaths3);
			}
			if (iter == DEBUG.debug.MAX_PATH_LENGTH) {
				throw new RuntimeException("Exceeded maximum path length");
			}
			
			return paths; //new FQLTextPanel(BorderFactory.createEtchedBorder(), "", paths.toString());
	//	} catch (Exception e) {
		//	e.printStackTrace();
		//	return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR: " + e.getMessage());			
		//}
	}

	private Set<Triple<C, C, List<C>>> extend2(List<Triple<C, C, List<C>>> paths,
			Map<C, Pair<C, C>> t, List<Triple<C, C, List<C>>> consts) {
		Set<Triple<C, C, List<C>>> newPaths = new HashSet<>();
		for (Triple<C, C, List<C>> p : paths) {
			for (C e : outEdges(t, p.second)) {
				List<C> p0 = new LinkedList<>(p.third);
				p0.add(e);
				Triple<C, C, List<C>> toAdd = new Triple<>(p.first, t.get(e).second, p0);
				Triple<C, C, List<C>> found = find(kb, toAdd, paths); 
				if (found == null) {
					found = find(kb, toAdd, newPaths);
					if (found == null) {
						find(kb, toAdd, consts);
						newPaths.add(toAdd);
					}
				}
			}
		}
		return newPaths;
	}
	
	private JComponent bar() {
		try {
			String text = "";
		//	List<Triple<C, C, List<C>>> consts = new LinkedList<>();

			Map<C, Set<List<C>>> arrs = new HashMap<>();
			Map<C, Set<List<C>>> consts = new HashMap<>();
			for (C c : schema.ids) {
				System.out.println("doing " + c);
				System.out.println("arrs " + arrs);
				Set<List<C>> set = new HashSet<>();
				int i = 1;
				for (; i < DEBUG.debug.MAX_PATH_LENGTH; i++) {
					Set<List<C>> cands = pathsUpTo(i, (C)"1", c);
					System.out.println("set " + set);
					System.out.println("cands at " + i + " are " + cands);
					Set<List<C>> toAdd = new HashSet<>();
					for (List<C> cand : cands) {
						System.out.println("cand " + cand);
						List<C> reduced = find2(getKB(), cand, set);
						System.out.println("reduced to " + reduced);
						if (reduced == null) {
							reduced = find2(getKB(), cand, toAdd);
							System.out.println("2reduced to " + reduced);
							if (reduced == null) {
								System.out.println("added " + cand);
								toAdd.add(cand);
							}
						}
					}
					if (toAdd.isEmpty()) {
						System.out.println("breaking");
						break;
					}
					System.out.println("adding " + toAdd);
					set.addAll(toAdd);
				}
				if (i == DEBUG.debug.MAX_PATH_LENGTH) {
					throw new RuntimeException("Exceeded max path length");
				}
				arrs.put(c,  set);
			}
			
			System.out.println(pathsUpTo);
			System.out.println(arrs);
			
			for (C c : global.ids) {
				consts.put(c, new HashSet<>());
			}
			
			for (C c : global.terms()) {
				if (global.ids.contains(c)) {
					continue;
				}
				if (!type(c).first.equals("1")) {
					continue;
				}
				Set<List<C>> set = consts.get(type(c).second);
				List<C> l = new LinkedList<>();
				l.add(c);
				set.add(l);
			} 
			
			List<JComponent> grid = new LinkedList<>();

			//Map<C, Set<List<C>>> entities = new HashMap<>();
			Map<C, Set<C>> m = new HashMap<>();
			for (C c : allIds()) {
			//	entities.put(c, new HashSet<>());
				m.put(c, new HashSet<>());
			}
			
			//for (Triple<C, C, List<C>> k : cat.arrows()) {
			//	if (k.first.equals("1")) { // uncomment causes exception
				//	Set<List<C>> set = entities.get(k.second);
				//	set.add(k.third);
				//}
			//}

			//does column names
			for (C c : allTerms()) {
				Pair<C, C> t = type(c);
				Set<C> set = m.get(t.first);
				set.add(c);
			}

			List<C> keys = new LinkedList<>(m.keySet());
			keys.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((Comparable) o1).compareTo(o2);
				}
			});

			for (C c : keys) {
				if (global.ids.contains(c)) {
					continue;
				}
				Pair<C, C> t = type(c);
				Set<List<C>> src = arrs.get(t.first);
				if (src == null) {
					throw new RuntimeException("Nothing for " + t.first + " in " + arrs.keySet());
				}
				List<C> cols = new LinkedList<>(m.get(c));
				cols = cols.stream().filter(x -> !x.toString().startsWith("!")).collect(Collectors.toList());

				Object[][] rowData = new Object[src.size()][cols.size()];
				int idx = cols.indexOf(c);
				if (idx != 0) {
					C old = cols.get(0);
					cols.set(0, c);
					cols.set(idx, old);
				}
				List<String> colNames3 = cols
						.stream()
						.map(x -> type(x).second.equals(x) ? x.toString() : x + " ("
								+ type(x).second + ")").collect(Collectors.toList());
				Object[] colNames = colNames3.toArray();

				int row = 0;
				for (List<C> l : src) {
					rowData[row][0] = l;
					int cl = 0;
					for (C col : cols) {
						List<C> r = new LinkedList<>(l);
						r.add(col);
						if (arrs.containsKey(type(col).second)) {
							for (List<C> cand : arrs.get(type(col).second)) {
								if (kb.equiv(cand, r)) {
									rowData[row][cl] = Util.sep(cand, ".");
									break;
								} 
							}
						} else {
							boolean found = false;
							for (List<C> cand : consts.get(type(col).second)) {
								if (kb.equiv(cand, r)) {
									rowData[row][cl] = Util.sep(cand, ".");
									break;
								} 
							}
							if (!found) {
								consts.get(type(col).second).add(r);
								rowData[row][cl] = Util.sep(r, ".");							
							}
						}
						cl++;
					}
					row++;
				}
				JPanel table = Util.makeTable(BorderFactory.createEtchedBorder(),
						c + " (" + src.size() + ") rows", rowData, colNames);
				grid.add(table);
			}

			return Util.makeGrid(grid);
		
		
	//		return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", text);
		} catch (Exception e) {
			e.printStackTrace();
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR: " + e.getMessage());			
		}
	}

	private List<C> find2(KB<C> kb2, List<C> cand, Set<List<C>> set) {
		for (List<C> l : set) {
			if (kb2.equiv(cand, l)) {
				return l;
			}
		}
		return null;
	}

	private JComponent makeTables(Function<Unit, Collection<Triple<C, C, List<C>>>> fn, Set<C> ignore) {
		try {
//			Category<C, Triple<C, C, List<C>>> cat = cat();
			List<JComponent> grid = new LinkedList<>();
			Collection<Triple<C, C, List<C>>> cat = fn.apply(new Unit());
			
			Map<C, Set<List<C>>> entities = new HashMap<>();
			Map<C, Set<C>> m = new HashMap<>();
			for (C c : allIds()) {
				entities.put(c, new HashSet<>());
				m.put(c, new HashSet<>());
			}
			for (Triple<C, C, List<C>> k : cat) {
				if (k.first.equals("1")) { // uncomment causes exception
					Set<List<C>> set = entities.get(k.second);
					set.add(k.third);
				}
			}

			for (C c : allTerms()) {
				Pair<C, C> t = type(c);
				Set<C> set = m.get(t.first);
				set.add(c);
			}

			List<C> keys = new LinkedList<>(m.keySet());
			keys.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((Comparable) o1).compareTo(o2);
				}
			});

			for (C c : keys) {
				if (c.equals("1")) {
					continue;
				}
				if (ignore.contains(c)) {
					continue;
				}
				Pair<C, C> t = type(c);
				Set<List<C>> src = entities.get(t.first);
				List<C> cols = new LinkedList<>(m.get(c));
				cols = cols.stream().filter(x -> !x.toString().startsWith("!")).collect(Collectors.toList());

				Object[][] rowData = new Object[src.size()][cols.size()];
				int idx = cols.indexOf(c);
				if (idx != 0) {
					C old = cols.get(0);
					cols.set(0, c);
					cols.set(idx, old);
				}
				List<String> colNames3 = cols
						.stream()
						.map(x -> type(x).second.equals(x) ? x.toString() : x + " ("
								+ type(x).second + ")").collect(Collectors.toList());
				Object[] colNames = colNames3.toArray();

				int row = 0;
				for (List<C> l : src) {
					rowData[row][0] = l;
					int cl = 0;
					for (C col : cols) {
						List<C> r = new LinkedList<>(l);
						r.add(col);
						for (Triple<C, C, List<C>> cand : cat) { //TODO sort by hom
							if (!cand.first.equals("1")) {
								continue;
							}
							if (!cand.second.equals(type(col).second)) {
								continue;
							}
							if (kb.equiv(cand.third, r)) {
								rowData[row][cl] = Util.sep(cand.third, ".");
								break;
							}
						}
						cl++;
					}
					row++;
				}
				JPanel table = Util.makeTable(BorderFactory.createEtchedBorder(),
						c + " (" + src.size() + ") rows", rowData, colNames);
				grid.add(table);
			}

			return Util.makeGrid(grid);
		} catch (Exception e) {
			 e.printStackTrace();
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR\n\n"
					+ e.getMessage());
		} 
	}

/*	private JComponent makeTables2(Category<C, Triple<C, C, List<C>>> cat) {
		try {
			// cat();
			List<JComponent> grid = new LinkedList<>();

			Map<C, Set<List<C>>> entities = new HashMap<>();
			Map<C, Set<C>> m = new HashMap<>();
			for (C c : ids) {
				entities.put(c, new HashSet<>());
				m.put(c, new HashSet<>());
			}
			for (Triple<C, C, List<C>> k : cat.arrows()) {
				if (k.first.equals("1") ) { // uncomment causes exception
					Set<List<C>> set = entities.get(k.second);
					set.add(k.third);
				}
			}

			for (C c : consts) {
				Pair<C, C> t = typeOf.get(c);
				// if (t.first.equals("1")) {
				// continue;
				// }
				// 
				// if (!local.contains(t.first)) {
				// continue;
				// }
				Set<C> set = m.get(t.first);
				
				set.add(c);
			}

			List<C> keys = new LinkedList<>(m.keySet());
			keys.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((Comparable) o1).compareTo(o2);
				}
			});

			// System.out.println(entities);

			Set<Triple<C, C, List<C>>> adom = new HashSet<>();
			Set<C> local0 = new HashSet<>(local);
			if (parent != null) {
				local0.addAll(parent.local);
			}
			for (C c : keys) {
				if (!local0.contains(c)) {
					continue;
				}
				if (c.equals("1")) {
					continue;
				}
				Pair<C, C> t = typeOf.get(c);
				Set<List<C>> src = entities.get(t.first);
				List<C> cols = new LinkedList<>(m.get(c));

				Object[][] rowData = new Object[src.size()][cols.size()];
				// List<C> colNames2 = new LinkedList<>(cols);
				int idx = cols.indexOf(c);
				if (idx != 0) {
					C old = cols.get(0);
					cols.set(0, c);
					cols.set(idx, old);
				}
				List<String> colNames3 = cols
						.stream()
						.map(x -> typeOf.get(x).second.equals(x) ? x.toString() : x + " ("
								+ typeOf.get(x).second + ")").collect(Collectors.toList());
				Object[] colNames = colNames3.toArray();

				int row = 0;
				for (List<C> l : src) {
					rowData[row][0] = l;
					int cl = 0;
					for (C col : cols) {
						List<C> r = new LinkedList<>(l);
						r.add(col);

						boolean found = false;
						for (Triple<C, C, List<C>> cand : adom) {
							if (!cand.first.equals("1")
									|| !cand.second.equals(typeOf.get(col).second)) {
								continue;
							} // 

							if (kb.equiv(cand.third, r)) {
								rowData[row][cl] = Util.sep(cand.third, ".");
								found = true;
								break;
							}
						}

						if (!found) {
							List<C> w = kb.normalize("", r);
							rowData[row][cl] = Util.sep(w, ".");
							adom.add(new Triple("1", typeOf.get(col).second, w));
						}

						// List<C> w = kb.normalize(r);
						cl++;
					}
					row++;
				}
				JPanel table = Util.makeTable(BorderFactory.createEtchedBorder(),
						c + " (" + src.size() + ") rows", rowData, colNames);
				grid.add(table);
			}

			

			return Util.makeGrid(grid);
		} catch (Exception e) {
			// e.printStackTrace();
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR\n\n"
					+ e.getMessage());
		}
	} */

	private Category<C, Triple<C, C, List<C>>> xcat;
	
	private Category<C, Triple<C, C, List<C>>> small_cat;
	
	private static <C> Set<C> outEdges(Map<C, Pair<C,C>> t, C p) {
		Set<C> ret = new HashSet<>();
		for (C c : t.keySet()) {
			if (c.equals(p)) {
				continue;
			}
			if (t.get(c).first.equals(p)) {
				ret.add(c);
			}
		} 
		return ret;
	}
	
	//TODO: test for inconsistency here?
	public static <D> Triple<D, D, List<D>> find(KB<D> kb, Triple<D, D, List<D>> tofind, Collection<Triple<D, D, List<D>>> cat) {
		Set<Triple<D, D, List<D>>> ret = new HashSet<>();
		for (Triple<D, D, List<D>> arr : cat) {
			if (arr.first.equals(tofind.first) && arr.second.equals(tofind.second)) {
				if (kb.equiv(arr.third, tofind.third)) {
					ret.add(arr);
				}
			}
		}
		if (ret.isEmpty()) {
			return null;
		}
		if (ret.size() == 1) {
			for (Triple<D, D, List<D>> k : ret) {
				return k;
			}
		}

		ret.add(tofind);
		List<String> xxx = ret.stream().map(x -> {
			List<D> h = new LinkedList<>(x.third);
			h.add(0, x.first);
			return Util.sep(h, ".");
		}).collect(Collectors.toList());
		throw new RuntimeException("Inconsistent: " + Util.sep(xxx, " = "));
	
	}

	public Category<C, Triple<C, C, List<C>>> cat() {
		if (xcat != null) {
			return xcat;
		}
		
		List<Triple<C, C, List<C>>> paths = new LinkedList<>();
		LinkedHashMap<C, Pair<C,C>> t = new LinkedHashMap<>();

		List<Triple<C, C, List<C>>> consts = new LinkedList<>();
		
		if (global != null) {
			for (C c : global.ids) {
				paths.add(new Triple<>(c, c, new LinkedList<>()));
			}
			for (C c : global.terms()) {
				if (!global.ids.contains(c)) {
					if (global.type(c).first.equals("1")) {
						List<C> l = new LinkedList<>();
						l.add(c);
						consts.add(new Triple<>(global.type(c).first, global.type(c).second, l));
					}
				}
			}
			t.putAll(global.types);
			extend(global.getKB(), paths, t, consts);
		}	
		
		if (schema != null) {
			for (C c : schema.ids) {
				paths.add(new Triple<>(c, c, new LinkedList<>()));
			}
			t.putAll(schema.types);
			extend(schema.getKB(), paths, t, consts);
		}	
		
		for (C c : ids) {
			paths.add(new Triple<>(c, c, new LinkedList<>()));
		}
		t.putAll(types);
		extend(getKB(), paths, t, consts);
		
		Set<Triple<C, C, List<C>>> arrows = new HashSet<>(paths);

		Category<C, Triple<C, C, List<C>>> xcat2 = new Category<C, Triple<C, C, List<C>>>() {

			@Override
			public Set<C> objects() {
				return allIds();
			}

			@Override
			public Set<Triple<C, C, List<C>>> arrows() {
				return arrows;
			}

			@Override
			public C source(Triple<C, C, List<C>> a) {
				return a.first;
			}

			@Override
			public C target(Triple<C, C, List<C>> a) {
				return a.second;
			}

			@Override
			public Triple<C, C, List<C>> identity(C o) {
				return new Triple<>(o, o, new LinkedList<>());
			}

			@Override
			public Triple<C, C, List<C>> compose(Triple<C, C, List<C>> a1, Triple<C, C, List<C>> a2) {
				Triple<C, C, List<C>> r = cache.get(new Pair<>(a1, a2));
				if (r != null) {
					return r;
				}
				List<C> ret = new LinkedList<>(a1.third);
				ret.addAll(a2.third);
				Triple<C, C, List<C>> xxx = new Triple<>(a1.first, a2.second, ret);
				Triple<C, C, List<C>> yyy = find(getKB(), xxx, hom(a1.first, a2.second));
				if (yyy == null) {
					throw new RuntimeException("Found nothing equivalent to " + ret + " in " + arrows());
				}
				cache.put(new Pair<>(a1, a2), yyy);
				return yyy;
			}
			
			Map<Pair<Triple<C, C, List<C>>, Triple<C, C, List<C>>>, Triple<C, C, List<C>>> cache = new HashMap<>();
		};

		//TODO: too hard to validate here
//		xcat2.validate();
		
		xcat = xcat2;
		
		return xcat; 
	}
	
	//TODO: cache these?
	public Category<C, Triple<C, C, List<C>>> small_cat() {
		Set<C> localIds = new HashSet<>(ids);
		List<Triple<C, C, List<C>>> paths = new LinkedList<>();
		LinkedHashMap<C, Pair<C,C>> t = new LinkedHashMap<>();

		List<Triple<C, C, List<C>>> consts = new LinkedList<>();
		
		if (global != null) {
			for (C c : global.ids) {
				if (c.equals("1")) {
					continue;
				}
				paths.add(new Triple<>(c, c, new LinkedList<>()));
				localIds.add(c);
			}
//			for (C c : global.terms()) {
	//			if (!global.ids.contains(c)) {
/*					if (global.type(c).first.equals("1")) {
						List<C> l = new LinkedList<>();
						l.add(c);
						consts.add(new Triple<>(global.type(c).first, global.type(c).second, l));
					} */
			//	}
			//}
			for (Entry<C, Pair<C, C>> k : global.types.entrySet()) {
				if (k.getValue().first.equals("1") || k.getValue().second.equals("1") || k.getKey().equals("1")) {
					continue;
				}
				t.put(k.getKey(), k.getValue());
			}

//			t.putAll(global.types); //TODO
			extend(global.getKB(), paths, t, consts);
		}	
		
		if (schema != null) {
			throw new RuntimeException();
		}	
		
		for (C c : ids) {
			paths.add(new Triple<>(c, c, new LinkedList<>()));
		}
		for (Entry<C, Pair<C, C>> k : types.entrySet()) {
			if (k.getValue().first.equals("1") || k.getValue().second.equals("1") || k.getKey().equals("1")) {
				continue;
			}
			t.put(k.getKey(), k.getValue());
		}
//		t.putAll(types);
		extend(getKB(), paths, t, consts);
		
		Set<Triple<C, C, List<C>>> arrows = new HashSet<>(paths);

		Category<C, Triple<C, C, List<C>>> xcat2 = new Category<C, Triple<C, C, List<C>>>() {

			@Override
			public Set<C> objects() {
				return localIds;
			}

			@Override
			public Set<Triple<C, C, List<C>>> arrows() {
				return arrows;
			}

			@Override
			public C source(Triple<C, C, List<C>> a) {
				return a.first;
			}

			@Override
			public C target(Triple<C, C, List<C>> a) {
				return a.second;
			}

			@Override
			public Triple<C, C, List<C>> identity(C o) {
				return new Triple<>(o, o, new LinkedList<>());
			}

			@Override
			public Triple<C, C, List<C>> compose(Triple<C, C, List<C>> a1, Triple<C, C, List<C>> a2) {
				List<C> ret = new LinkedList<>(a1.third);
				ret.addAll(a2.third);
				Triple<C, C, List<C>> xxx = new Triple<>(a1.first, a2.second, ret);
				Triple<C, C, List<C>> yyy = find(getKB(), xxx, hom(a1.first, a2.second));
				if (yyy == null) {
					throw new RuntimeException("Found nothing equivalent to " + ret + " in " + arrows());
				}
				return yyy;
			}
		};

		xcat2.validate();
		
	//	xcat = xcat2;
		
		return xcat2; 
	}

	
	//mutate paths in place
	public static <C> void extend(KB<C> kb, Collection<Triple<C, C, List<C>>> paths, Map<C, Pair<C,C>> t, Collection<Triple<C, C, List<C>>> consts) {
		int iter = 0;
		for (; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			Set<Triple<C, C, List<C>>> newPaths = new HashSet<>();
			for (Triple<C, C, List<C>> p : paths) {
				for (C e : outEdges(t, p.second)) {
					List<C> p0 = new LinkedList<>(p.third);
					p0.add(e);

					Triple<C, C, List<C>> toAdd = new Triple<>(p.first, t.get(e).second, p0);
					Triple<C, C, List<C>> found = find(kb, toAdd, paths); //TODO: sort into hom
					
					if (found == null) {
						found = find(kb, toAdd, newPaths);
						if (found == null) {
							find(kb, toAdd, consts);
							newPaths.add(toAdd);
						}
					}
				}
			}
			if (paths.containsAll(newPaths)) {
				break;
			}
			paths.addAll(newPaths);
		}
		if (iter == DEBUG.debug.MAX_PATH_LENGTH) {
			throw new RuntimeException("Exceeded maximum path length");
		}		
	}

/*	@SuppressWarnings("serial")
	public Category<C, Triple<C, C, List<C>>> cat() {
		if (xcat != null) {
			return xcat;
		}

		init();
		kb.complete();

		Set<Triple<C, C, List<C>>> paths = new TreeSet<>(new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1.toString().length() != o2.toString().length()) {
					return new Integer(o1.toString().length()).compareTo(o2.toString().length());
				}
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (C c : ids) {
			paths.add(new Triple<>(c, c, new LinkedList<>()));
		}

		int iter;
		for (iter = 0; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			Set<Triple<C, C, List<C>>> newPaths = new TreeSet<>(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					if (o1.toString().length() != o2.toString().length()) {
						return new Integer(o1.toString().length())
								.compareTo(o2.toString().length());
					}
					return o1.toString().compareTo(o2.toString());
				}
			});
			for (Triple<C, C, List<C>> p : paths) {
				for (Triple<C, C, C> e : outEdges(p.second)) {
					List<C> p0 = new LinkedList<>(p.third);
					p0.add(e.third);

					Triple<C, C, List<C>> toAdd = new Triple<>(p.first, e.second, p0);

					boolean found = false;
					for (Triple<C, C, List<C>> xxx : paths) {
						if (!xxx.first.equals(toAdd.first) || !xxx.second.equals(toAdd.second)) { // 
							continue;
						}
						if (kb.equiv(xxx.third, toAdd.third)) {
							found = true;
							break;
						}
					}
					if (!found) {
						for (Triple<C, C, List<C>> xxx : newPaths) {
							if (!xxx.first.equals(toAdd.first) || !xxx.second.equals(toAdd.second)) { // 
								continue;
							}
							if (kb.equiv(xxx.third, toAdd.third)) {
								found = true;
								break;
							}
						}
						if (!found) {
							newPaths.add(toAdd);
						}
					}
				}
			}
			if (paths.containsAll(newPaths)) {
				break;
			}
			paths.addAll(newPaths);
		}
		if (iter == DEBUG.debug.MAX_PATH_LENGTH) {
			throw new RuntimeException("Exceeded maximum path length in " + this);
		}

		Set<Triple<C, C, List<C>>> arrows = new HashSet<>(paths);
		System.out.println("ZZZZZZZZZZ");
		System.out.println("Arrows are " + arrows);

		xcat = new Category<C, Triple<C, C, List<C>>>() {

			@Override
			public Set<C> objects() {
				return ids;
			}

			@Override
			public Set<Triple<C, C, List<C>>> arrows() {
				return arrows;
			}

			@Override
			public C source(Triple<C, C, List<C>> a) {
				return a.first;
			}

			@Override
			public C target(Triple<C, C, List<C>> a) {
				return a.second;
			}

			@Override
			public Triple<C, C, List<C>> identity(C o) {
				return new Triple<>(o, o, new LinkedList<>());
			}

			@Override
			public Triple<C, C, List<C>> compose(Triple<C, C, List<C>> a1, Triple<C, C, List<C>> a2) {
				List<C> ret = new LinkedList<>(a1.third);
				ret.addAll(a2.third);
				Triple<C, C, List<C>> xxx = new Triple<>(a1.first, a2.second, ret);
				for (Triple<C, C, List<C>> k : arrows()) {
					if (!k.first.equals(xxx.first) || !k.second.equals(xxx.second)) {
						continue;
					}
					if (kb.equiv(k.third, xxx.third)) { // 
						return k;
					}
				}
				throw new RuntimeException("Found nothing equivalent to " + ret + " in " + arrows());
			}
		};

		return xcat; 
	} */

/*	@SuppressWarnings("serial")
	public Category<C, Triple<C, C, List<C>>> small_cat() {
		HashSet<C> local0 = new HashSet<>(local);
		if (parent != null) {
			local0.addAll(parent.local);
		}

		if (small_cat != null) {
			return small_cat;
		}

		init();
		kb.complete();

		Set<Triple<C, C, List<C>>> paths = new HashSet<>();
		Set<C> local_ids = new HashSet<>();
		for (C c : ids) {
			if (!local0.contains(c) && !c.equals("1")) {
				continue;
			}
			paths.add(new Triple<>(c, c, new LinkedList<>()));
			local_ids.add(c);
		}

		// System.out.println("locals are " + local);

		int iter;
		for (iter = 0; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			Set<Triple<C, C, List<C>>> newPaths = new HashSet<>();
			for (Triple<C, C, List<C>> p : paths) {
				for (Triple<C, C, C> e : outEdges_local(local0, p.second)) {
					List<C> p0 = new LinkedList<>(p.third);
					p0.add(e.third);

					Triple<C, C, List<C>> toAdd = new Triple<>(p.first, e.second, p0);

					boolean found = false;
					for (Triple<C, C, List<C>> xxx : paths) {
						if (!xxx.first.equals(toAdd.first) || !xxx.second.equals(toAdd.second)) { // TODO
							continue;
						}
						if (kb.equiv(xxx.third, toAdd.third)) {
							found = true;
							break;
						}
					}
					if (!found) {
						for (Triple<C, C, List<C>> xxx : newPaths) {
							if (!xxx.first.equals(toAdd.first) || !xxx.second.equals(toAdd.second)) { // TODO
								continue;
							}
							if (kb.equiv(xxx.third, toAdd.third)) {
								found = true;
								break;
							}
						}
						if (!found) {
							newPaths.add(toAdd);
						}
					}
				}
			}
			if (paths.containsAll(newPaths)) {
				break;
			}
			paths.addAll(newPaths);
		}
		if (iter == DEBUG.debug.MAX_PATH_LENGTH) {
			throw new RuntimeException("Exceeded maximum path length");
		}

		Set<Triple<C, C, List<C>>> arrows = new HashSet<>(paths);
		// System.out.println("Arrows are " + arrows);

		small_cat = new Category<C, Triple<C, C, List<C>>>() {

			@Override
			public Set<C> objects() {
				return local_ids;
			}

			@Override
			public Set<Triple<C, C, List<C>>> arrows() {
				return arrows;
			}

			@Override
			public C source(Triple<C, C, List<C>> a) {
				return a.first;
			}

			@Override
			public C target(Triple<C, C, List<C>> a) {
				return a.second;
			}

			@Override
			public Triple<C, C, List<C>> identity(C o) {
				return new Triple<>(o, o, new LinkedList<>());
			}

			@Override
			public Triple<C, C, List<C>> compose(Triple<C, C, List<C>> a1, Triple<C, C, List<C>> a2) {
				List<C> ret = new LinkedList<>(a1.third);
				ret.addAll(a2.third);
				Triple<C, C, List<C>> xxx = new Triple<>(a1.first, a2.second, ret);
				for (Triple<C, C, List<C>> k : arrows()) {
					if (!k.first.equals(xxx.first) || !k.second.equals(xxx.second)) {
						continue;
					}
					if (kb.equiv(k.third, xxx.third)) { // TODO
						return k;
					}
				}
				throw new RuntimeException("Found nothing equivalent to " + ret + " in " + arrows());
			}
		};

		small_cat.validate();
		return small_cat;
	}
*/
/*	private Set<Triple<C, C, C>> outEdges(C p) {
		if (!ids.contains(p)) {
			throw new RuntimeException();
		}
		Set<Triple<C, C, C>> ret = new HashSet<>();
		List<C> consts2 = new LinkedList<>(consts);
		consts2.sort(new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1.toString().length() != o2.toString().length()) {
					return new Integer(o1.toString().length()).compareTo(o2.toString().length());
				}
				return o1.toString().compareTo(o2.toString());
			}
		});
		System.out.println("^^^^^");
		for (C c : consts2) {
			System.out.println(c);
			if (c.equals(p)) {
				continue;
			}
			if (typeOf.get(c).first.equals(p)) {
				ret.add(new Triple<C, C, C>(null, typeOf.get(c).second, c));
			}
		}
		return ret;
	}

	private Set<Triple<C, C, C>> outEdges_local(Set<C> local, C p) {
		if (!ids.contains(p)) {
			throw new RuntimeException();
		}
		Set<Triple<C, C, C>> ret = new HashSet<>();
		for (C c : consts) {
			if (c.equals(p)) {
				continue;
			}
			if (!local.contains(c)) {
				continue;
			}
			Pair<C, C> t = typeOf.get(c);
			if (!local.contains(t.second)) {
				continue;
			}
			if (typeOf.get(c).first.equals(p)) {
				ret.add(new Triple<C, C, C>(null, typeOf.get(c).second, c));
			}
		}
		return ret;
	} */

	public static XCtx<String> make(XCtx<String> S, XInst I) {
		//Set<String> seen = new HashSet<>();
		Map<String, Pair<String, String>> t = new HashMap<>();
		Set<Pair<List<String>, List<String>>> e = new HashSet<>();

		for (Pair<String, String> k : I.nodes) {
			if (!S.types.containsKey(k.second) && !S.global.types.containsKey(k.second)) {
				throw new RuntimeException("Unknown node/type: " + k.second);
			}
			if (t.containsKey(k.first)) {
				throw new RuntimeException("Duplicate name: " + k.first);
			}
			if (S.types.containsKey(k.first)) {
				throw new RuntimeException("Name of variable is also in schema " + k);
			}
			if (S.global.types.containsKey(k.first)) {
				throw new RuntimeException("Name of variable is also global " + k);
			}
			t.put(k.first, new Pair<>("1", k.second));
		}

		for (Pair<List<String>, List<String>> k : I.eqs) {
			// TODO: supress variable check for now			
			e.add(k);
		}

		//	s.parent = s_old;
		//s.validate();
		// s.init();
		return new XCtx<String>(new HashSet<>(), t, e , S.global, S, "instance");
	} 


	public static XCtx<String> make(XCtx<String> env, XSchema s) {
		Set<String> i = new HashSet<>();
		Map<String, Pair<String, String>> t = new HashMap<>();

		for (String k : s.nodes) {
			if (env.types.containsKey(k)) {
				throw new RuntimeException("Name of node is also global " + k);
			}
			if (t.containsKey(k)) {
				throw new RuntimeException("Duplicate node: " + k);
			}
			i.add(k);
			t.put(k, new Pair<>(k, k));
		}

		for (Triple<String, String, String> k : s.arrows) {
			if (env.types.containsKey(k.first)) {
				throw new RuntimeException("Name of edge is also global " + k.first);
			}
			if (i.contains(k.first)) {
				throw new RuntimeException("Name of edge is also node " + k.first);
			}
			if (t.containsKey(k)) {
				throw new RuntimeException("Duplicate edge: " + k);
			}

			String edge1 = null;
			if (i.contains(k.second)) {
				edge1 = "true";
			}
			if (env.types.containsKey(k.second)) {
				edge1 = "false";
			}
			if (edge1 == null) {
				throw new RuntimeException("Error in " + k + ": " + k.second
						+ " is not node or type");
			}

			String edge2 = null;
			if (i.contains(k.third)) {
				edge2 = "true";
			}
			if (env.types.containsKey(k.third)) {
				edge2 = "false";
			}
			if (edge2 == null) {
				throw new RuntimeException("Error in " + k + ": " + k.third
						+ " is not node or type");
			}

			if (edge1 == "false" && edge2 == "false") {
				throw new RuntimeException("Error in " + k
						+ ": functions should be declared at top-level");
			}

			if (edge1 == "false" && edge2 == "true") {
				throw new RuntimeException("Error in " + k + ": cannot have functions from types");
			}

			t.put(k.first, new Pair<>(k.second, k.third));
		}

		Set<Pair<List<String>, List<String>>> e = new HashSet<>(s.eqs);

		return new XCtx<>(i, t, e, env, null, "schema");
	}

	/* public Set<Triple<C, C, List<C>>> hom(C src, C dst) {
		Category<C, Triple<C, C, List<C>>> cat = cat();

		return cat.hom(src, dst);
	} */
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((global == null) ? 0 : global.hashCode());
		result = prime * result + ((ids == null) ? 0 : ids.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		XCtx other = (XCtx) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (global == null) {
			if (other.global != null)
				return false;
		} else if (!global.equals(other.global))
			return false;
		if (ids == null) {
			if (other.ids != null)
				return false;
		} else if (!ids.equals(other.ids))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	public XCtx<C> rel() {
		Set<Pair<List<C>, List<C>>> new_eqs = new HashSet<>(eqs);
		if (schema == null) {
			throw new RuntimeException("Problem with relationalize");
		}
		for (C x : terms()) {
			if (!type(x).first.equals("1")) {
				continue;
			}
			loop: for (C y : terms()) {
				if (!type(y).first.equals("1")) {
					continue;
				}
				if (!type(x).second.equals(type(y).second)) {
					continue;
				}
				if (x.equals(y)) {
					continue;
				}
				for (C b : allIds()) {
					for (Triple<C, C, List<C>> p : cat().hom(type(x).second, b)) {
						for (C t : global.allIds()) {
							for (Triple<C, C, List<C>> att : cat().hom(b, t)) {
								List<C> lhs = new LinkedList<>();
								lhs.add(x);
								lhs.addAll(p.third);
								lhs.addAll(att.third);								
								List<C> rhs = new LinkedList<>();
								rhs.add(y);
								rhs.addAll(p.third);
								rhs.addAll(att.third);
								if (!getKB().equiv(lhs, rhs)) {
									continue loop;
								}
							}
						}
					}					
				}
				List<C> lhs = new LinkedList<>();
				List<C> rhs = new LinkedList<>();
				lhs.add(x);
				rhs.add(y);
				new_eqs.add(new Pair<>(lhs, rhs));
			}
		}
		
		return new XCtx<>(ids, types, new_eqs, global, schema, kind);
	}
	
	

}
