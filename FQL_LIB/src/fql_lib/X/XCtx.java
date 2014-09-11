package fql_lib.X;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.X.XExp.XInst;
import fql_lib.X.XExp.XSchema;
import fql_lib.cat.Category;
import fql_lib.cat.KB;
import fql_lib.gui.FQLTextPanel;

public class XCtx<C> implements XObject {
	
	private KB<C> kb;
	 Set<C> ids;
	 Set<C> consts;
	 Set<C> local;
	 Map<C, Pair<C, C>> typeOf;
	 Set<Pair<List<C>, List<C>>> eqs;
	private boolean initialized = false;
	
	public XCtx<C> copy() {
		return new XCtx<>(new HashSet<>(ids), new HashSet<>(consts), new HashMap<>(typeOf), new HashSet<>(eqs), new HashSet<>(local));
	}

	public XCtx(Set<C> ids, Set<C> consts, Map<C, Pair<C,C>> typeOf, Set<Pair<List<C>, List<C>>> eqs, Set<C> local) {
		this.consts = consts;
		this.typeOf = typeOf;
		this.eqs = eqs;
		this.ids = ids;
		this.local = local;
		validate();
	}
	
	public KB<C> getKB() {
		init();
		return kb;
	}
	
	public void init() {
		if (initialized) {
			return;
		}
		validate();
		kb();
		initialized = true;
	}
	
	private void kb() {
		Set<Pair<List<C>, List<C>>> rules = new HashSet<>(eqs);
		for (C id : ids) {
			List<C> l = new LinkedList<>();
			l.add(id);
			rules.add(new Pair<>(l, new LinkedList<>()));
		}
		kb = new KB<C>(rules, 32); //TODO
	}

	private void validate() {
		if (!consts.containsAll(local)) {
			throw new RuntimeException("Local definition not included in constants");
		}
		for (C c : consts) {
			if (!typeOf.containsKey(c)) {
				throw new RuntimeException();
			}
		}
		if (!consts.containsAll(ids)) {
			throw new RuntimeException("ids not contained in const");
		}
		for (C c : ids) {
			Pair<C,C> t = typeOf.get(c);
			if (!t.first.equals(t.second)) {
				throw new RuntimeException("Not identity " + c);
			}
		}
		
		for (Pair<List<C>, List<C>> eq : eqs) {
			if (!consts.containsAll(eq.first)) {
				throw new RuntimeException("unknown const in: " + eq.first);
			}
			if (!consts.containsAll(eq.second)) {
				throw new RuntimeException("unknown const in: " + eq.second);
			}
			if (!type(eq.first).equals(type(eq.second))) {
				throw new RuntimeException("Type mismatch on " + eq);
			}
		}		
	}

	public Pair<C,C> type(List<C> first) {
		if (first.size() == 0) {
			throw new RuntimeException("Empty eq");
		}
		Iterator<C> it = first.iterator();
		Pair<C,C> ret = typeOf.get(it.next());
		while (it.hasNext()) {
			Pair<C,C> next = typeOf.get(it.next());
			if (!ret.second.equals(next.first)) {
				throw new RuntimeException("Ill-typed: " + first);
			}
			ret = new Pair<>(ret.first, next.second);
		}
		return ret;
	}

	@Override
	public JComponent display() {
		init();
		String kb_text = "types:\n  " + Util.sep(ids, ",\n  ");
		List<String> terms = consts.stream().map(x -> x + " : " + typeOf.get(x).first + " -> " + typeOf.get(x).second).collect(Collectors.toList());
		kb_text = kb_text.trim();
		kb_text +="\n\nterms:\n  " + Util.sep(terms, ",\n  ");
		List<String> xx = eqs.stream().map(x -> Util.sep(x.first, ".") + " = " + Util.sep(x.second, ".")).collect(Collectors.toList());
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
			//e.printStackTrace();
			cat = "ERROR\n\n" + e.getMessage();
		}
		
		JComponent kbc = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", kb_text);
		JComponent ctp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", cat);
		
		JComponent tables = makeTables();
		
		JTabbedPane ret = new JTabbedPane();
		ret.addTab("Text", kbc);
		ret.addTab("Category", ctp);
		ret.addTab("Tables", tables);
		
		return ret; 
	}
	
	private JComponent makeTables() {
		try {
			cat();
			List<JComponent> grid = new LinkedList<>();

			Map<C, Set<List<C>>> entities = new HashMap<>();
			Map<C, Set<C>> m = new HashMap<>();
			for (C c : ids) {
				entities.put(c, new HashSet<>());
				m.put(c, new HashSet<>());
			}
			for (Triple<C, C, List<C>> k : cat.arrows()) {
				if (k.first.equals("1") /* && local.contains(k.second) && ids.contains(k.second) */ ) { //uncomment causes exception
					Set<List<C>> set = entities.get(k.second);
					set.add(k.third);
				}
			}
			
			for (C c : consts) {
				Pair<C,C> t = typeOf.get(c);
				//if (t.first.equals("1")) {
				//	continue;
				//}
				//TODO
			//	if (!local.contains(t.first)) {
				//	continue;
				//}
				Set<C> set = m.get(t.first);
			/* 	if (set == null) {
					set = new HashSet<>();
					m.put(t.first, set);
				} */
				set.add(c);
			}
			
			List<C> keys = new LinkedList<>(m.keySet());
			keys.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((Comparable) o1).compareTo(o2);
				}
			});
			
			//System.out.println(entities);
			
			for (C c : keys) {
				//if (!local.contains(c)) {
				//	continue;
				//}
				if (c.equals("1")) {
					continue;
				}
				Pair<C,C> t = typeOf.get(c);
				Set<List<C>> src = entities.get(t.first);
				List<C> cols = new LinkedList<>(m.get(c));
				
				Object[][] rowData = new Object[src.size()][cols.size()];
				//List<C> colNames2 = new LinkedList<>(cols);
				int idx = cols.indexOf(c);
				if (idx != 0) {
					C old = cols.get(0);
					cols.set(0, c);
					cols.set(idx, old);
				}
				List<String> colNames3 = cols.stream().map(x -> typeOf.get(x).second.equals(x) ? x.toString() : x + " (" + typeOf.get(x).second + ")").collect(Collectors.toList());
				Object[] colNames = colNames3.toArray();
				
				
				int row = 0;
				for (List<C> l : src) {
					rowData[row][0] = l;
					int cl = 0;
					for (C col : cols) {
						List<C> r = new LinkedList<>(l);
						r.add(col);
						for (Triple<C, C, List<C>> cand : cat.arrows()) {
							if (kb.equiv(cand.third, r)) {
								rowData[row][cl] = Util.sep(cand.third, ".");
								break;
							}
						}
						
						//List<C> w = kb.normalize(r);
						cl++;
					}
					row++;
				}
				JPanel table = Util.makeTable(BorderFactory.createEtchedBorder(), c + " (" + src.size() + ") rows", rowData, colNames);
				grid.add(table);
			}
			
/*			for (C c : entities.keySet()) {
				Set<List<C>> set = entities.get(c);
				JList list = new JList(set.toArray());
				JScrollPane jsp = new JScrollPane(list);
				jsp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), c.toString()));
				grid.add(jsp);
			} */
			
			return Util.makeGrid(grid);
		} catch (Exception e) {
			//e.printStackTrace();
			return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "ERROR\n\n" + e.getMessage());
		}
	}

	private Category<C, Triple<C, C, List<C>>> cat;
	
	@SuppressWarnings("serial")
	public Category<C, Triple<C, C, List<C>>> cat() {
		if (cat != null) {
			return cat;
		}
		
		init();
		kb.complete();
		
		Set<Triple<C, C, List<C>>> paths = new HashSet<>();
		for (C c : ids) {
			paths.add(new Triple<>(c, c, new LinkedList<>()));
		}
		
		int iter;
		for (iter = 0; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			Set<Triple<C, C, List<C>>> newPaths = new HashSet<>();
			for (Triple<C, C, List<C>> p : paths) {
				for (Triple<C, C, C> e : outEdges(p.second)) {
					List<C> p0 = new LinkedList<>(p.third);
					p0.add(e.third);
					
					Triple<C,C, List<C>> toAdd = new Triple<>(p.first, e.second, p0);
					
					boolean found = false;
					for (Triple<C,C,List<C>> xxx : paths) {
						if (!xxx.first.equals(toAdd.first) || !xxx.second.equals(toAdd.second)) { //TODO
							continue;
						}
						if (kb.equiv(xxx.third, toAdd.third)) {
							found = true;
							break;
						}
					}
					if (!found) {
						for (Triple<C,C,List<C>> xxx : newPaths) {
							if (!xxx.first.equals(toAdd.first) || !xxx.second.equals(toAdd.second)) { //TODO
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
		//System.out.println("Arrows are " + arrows);
		
		cat = new Category<C, Triple<C, C, List<C>>>() {

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
			public Triple<C, C, List<C>> compose (Triple<C, C, List<C>> a1, Triple<C, C, List<C>> a2) {
				List<C> ret = new LinkedList<>(a1.third);
				ret.addAll(a2.third);
				Triple<C, C, List<C>> xxx = new Triple<>(a1.first, a2.second, ret);
				for (Triple<C, C, List<C>> k : arrows()) {
					if (!k.first.equals(xxx.first) || !k.second.equals(xxx.second)) {
						continue;
					}
					if (kb.equiv(k.third, xxx.third)) { //TODO
						return k;
					}
				}
				throw new RuntimeException("Found nothing equivalent to " + ret + " in " + arrows());
			}
		};
		
		return cat;
	}
	
	//TODO: cache
	private Set<Triple<C, C, C>> outEdges(C p) {
		if (!ids.contains(p)) {
			throw new RuntimeException();
		}
		Set<Triple<C, C, C>> ret = new HashSet<>();
		for (C c : consts) {
			if (c.equals(p)) {
				continue;
			}
			if (typeOf.get(c).first.equals(p)) {
				ret.add(new Triple<C, C, C>(null, typeOf.get(c).second, c));
			}
		}
		return ret;
	}

	public static XCtx<String> make(XEnvironment env, XCtx<String> s_old, XInst i) {
		XCtx<String> s = s_old.copy();
		
		Set<String> seen = new HashSet<>();
		for (Pair<String, String> k : i.nodes) {
			if (!s.local.contains(k.second)) {
				throw new RuntimeException("Not a local definition: " + k.second);
			}
			if (seen.contains(k.first)) {
				throw new RuntimeException("Duplicate name: " + k.first);
			}
			if (env.types.contains(k.first)) {
				throw new RuntimeException("Name of variable is also type " + k);
			}
			if (env.fns.containsKey(k.first)) {
				throw new RuntimeException("Name of variable is also function " + k);
			}
			s.local.add(k.first);
			s.consts.add(k.first);
			s.typeOf.put(k.first, new Pair<>("1", k.second));
			seen.add(k.first);
		}
		
		for (Pair<List<String>, List<String>> k : i.eqs) {
			//TODO: supress this for now
/*			if (!seen.contains(k.first.get(0))) {
				throw new RuntimeException("Does not being with a variable: " + k.first);
			}
			if (!seen.contains(k.second.get(0))) {
				throw new RuntimeException("Does not being with a variable: " + k.second);				
			} */
			s.eqs.add(k);
		}
		
		return s;
	}
	
	public static XCtx<String> make(XEnvironment env, XSchema s) {
		Set<String> c = new HashSet<>();
		Set<String> i = new HashSet<>();
		Set<String> local = new HashSet<>();
		Map<String, Pair<String, String>> t = new HashMap<>();

		for (String k : s.nodes) {
			if (env.types.contains(k)) {
				throw new RuntimeException("Name of node is also type " + k);
			}
			if (env.fns.containsKey(k)) {
				throw new RuntimeException("Name of node is also function " + k);
			}
			if (c.contains(k)) {
				throw new RuntimeException("Duplicate node: " + k);
			}
			c.add(k);
			i.add(k);
			t.put(k, new Pair<>(k, k));
			local.add(k);
		}
		
		for (Triple<String, String, String> k : s.arrows) {
			if (env.types.contains(k.first)) {
				throw new RuntimeException("Name of edge is also type " + k.first);
			}
			if (env.fns.containsKey(k.first)) {
				throw new RuntimeException("Name of edge is also function " + k.first);
			}
			if (i.contains(k.first)) {
				throw new RuntimeException("Name of edge is also node " + k.first);
			}
			if (c.contains(k)) {
				throw new RuntimeException("Duplicate edge: " + k);
			}
			
			String edge1 = null;
			if (i.contains(k.second)) {
				edge1 = "true";
			} 
			if (env.types.contains(k.second)) {
				edge1 = "false";
			}
			if (edge1 == null) {
				throw new RuntimeException("Error in " + k + ": " + k.second + " is not node or type");
			}
			
			String edge2 = null;
			if (i.contains(k.third)) {
				edge2 = "true";
			} 
			if (env.types.contains(k.third)) {
				edge2 = "false";
			}
			if (edge2 == null) {
				throw new RuntimeException("Error in " + k + ": " + k.third + " is not node or type");
			}
			
			if (edge1 == "false" && edge2 == "false") {
				throw new RuntimeException("Error in " + k + ": functions should be declared at top-level");
			}
			
			if (edge1 == "false" && edge2 == "true") {
				throw new RuntimeException("Error in " + k + ": cannot have functions from types");
			}

			local.add(k.first);
			c.add(k.first);
			t.put(k.first, new Pair<>(k.second, k.third));
			
			//edge
/*			if (edge1 == "true" && edge2 == "true") { }
			//attribute
			if (edge1 == "true" && edge2 == "false") { } */			
		}
		
		//TODO: this is active domain semantics
		c.addAll(env.fns.keySet());
		t.putAll(env.fns);
		i.addAll(env.types);

		Set<Pair<List<String>, List<String>>> e = new HashSet<>(s.eqs);
		e.addAll(env.eqs);
		
		return new XCtx<>(i, c, t, e, local);
	}
	
	

}
