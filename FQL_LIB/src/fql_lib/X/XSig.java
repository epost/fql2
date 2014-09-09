package fql_lib.X;

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
import javax.swing.JTabbedPane;

import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.X.XExp.XSchema;
import fql_lib.cat.Category;
import fql_lib.cat.KB;
import fql_lib.gui.FQLTextPanel;

public class XSig<C> implements XObject {
	
	private KB<C> kb;
	
	private Set<C> ids;
	private Set<C> consts;
	private Map<C, Pair<C, C>> typeOf;
	private Set<Pair<List<C>, List<C>>> eqs;

	public XSig(Set<C> ids, Set<C> consts, Map<C, Pair<C,C>> typeOf, Set<Pair<List<C>, List<C>>> eqs) {
		this.consts = consts;
		this.typeOf = typeOf;
		this.eqs = eqs;
		this.ids = ids;
		validate();
		kb();
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

	private Pair<C,C> type(List<C> first) {
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
		String kb_text = "types:\n  " + Util.sep(ids, ",\n  ");
		List<String> terms = consts.stream().map(x -> x + " : " + typeOf.get(x).first + " -> " + typeOf.get(x).second).collect(Collectors.toList());
		kb_text +="\n\nterms:\n  " + Util.sep(terms, ",\n  ");
		List<String> xx = eqs.stream().map(x -> Util.sep(x.first, ".") + " = " + Util.sep(x.second, ".")).collect(Collectors.toList());
		kb_text += "\n\nequations:\n  " + Util.sep(xx, ",\n  ");		
		
		try {
			kb.complete();
			kb_text += "\n\nKnuth-Bendix Completion:\n\n" + kb.toString();
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
		
		JComponent kbc = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", kb_text);
		JComponent ctp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", cat);
		
		JTabbedPane ret = new JTabbedPane();
		ret.addTab("Text", kbc);
		ret.addTab("Category", ctp);
		
		// TODO build amalgamated category
		return ret; 
	}
	
	@SuppressWarnings("serial")
	public Category<C, Triple<C, C, List<C>>> cat() {
		kb.complete();
		
		Set<Triple<C, C, List<C>>> paths = new HashSet<>();
		for (C c : ids) {
			paths.add(new Triple<>(c, c, new LinkedList<>()));
		}
		
		for (int iter = 0; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			Set<Triple<C, C, List<C>>> newPaths = new HashSet<>();
			for (Triple<C, C, List<C>> p : paths) {
				for (Triple<C, C, C> e : outEdges(p.second)) {
					List<C> p0 = new LinkedList<>(p.third);
					p0.add(e.third);
					newPaths.add(new Triple<>(p.first, e.second, kb.normalize(p0)));
				}
			}
			if (paths.containsAll(newPaths)) {
				break;
			} 
			paths.addAll(newPaths);
		}
		
		Set<Triple<C, C, List<C>>> arrows = new HashSet<>(paths);
		
		return new Category<C, Triple<C, C, List<C>>>() {

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
				return new Triple<>(a1.first, a2.second, kb.normalize(ret));
			}
		};
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

	public static XSig<String> make(XEnvironment env, XSchema s) {
		Set<String> c = new HashSet<>();
		Set<String> i = new HashSet<>();
		Map<String, Pair<String, String>> t = new HashMap<>();
	//	Map<String, String> idf = new HashMap<>();

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
		//	idf.put(k, k);
		}
		
		for (Triple<String, String, String> k : s.arrows) {
			if (env.types.contains(k.first)) {
				throw new RuntimeException("Name of edge is also type " + k.first);
			}
			if (env.fns.containsKey(k.first)) {
				throw new RuntimeException("Name of edge is also function " + k.first);
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
/*		c.addAll(env.types);
		for (String ty : env.types) {
			t.put(ty, new Pair<>(ty, ty));
		} */

		Set<Pair<List<String>, List<String>>> e = new HashSet<>(s.eqs);
		e.addAll(env.eqs);
		
		
		return new XSig<>(i, c, t, e);
	}
	
	

}
