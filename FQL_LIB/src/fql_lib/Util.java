package fql_lib;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;

import fql_lib.gui.MyTableRowSorter;

public class Util {
	
	public static <X> Map<X, X> id(Collection<X> X) {
		Map<X, X> ret = new HashMap<>();
		for (X x : X) {
			ret.put(x, x);
		}
		return ret;
	}
	
	public static <X> List<X> singList(X x) {
		List<X> ret = new LinkedList<>();
		ret.add(x);
		return ret;
	}
	
	public static <X> String print(Collection<X[]> c) {
		String ret = "";
		for (X[] x : c) {
			ret += Arrays.toString(x);
		}
		return ret;
	}
	
	public static <X,Y> Map<X,Y> reify(Function<X,Y> f, Set<X> set) {
		Map<X,Y> ret = new HashMap<>();
		for (X x : set) {
			ret.put(x, f.apply(x));
		}
		return ret;
	}
	
	private static JPanel makeRowOrCol(List<JComponent> list, int orientation) {
		if (list.size() == 0) {
			JPanel ret = new JPanel();
			ret.setBorder(BorderFactory.createEmptyBorder());
			return ret;
		}
		JPanel ret = new JPanel(new GridLayout(1,1));
		if (list.size() == 1) {
			ret.add(list.get(0));
			return ret;
		}
		Iterator<JComponent> it = list.iterator();
		JComponent sofar = it.next();
		double n = 2;
		while (it.hasNext()) {
			JSplitPane jsp = new JSplitPane(orientation);
			jsp.setResizeWeight(1.0d / n);
			jsp.add(sofar);
			jsp.add(it.next());
			jsp.setDividerSize(2);
			jsp.setBorder(BorderFactory.createEmptyBorder());
			sofar = jsp;
			n++;
		}
		ret.add(sofar);
		return ret;
	}
	
	public static JPanel makeGrid(List<JComponent> list) {
		int n = (int) Math.ceil(Math.sqrt(list.size()));

		List<JComponent> list2 = new LinkedList<>();
		for (int i = 0; i < list.size(); i += n) {
			int end = Math.min(list.size(), i+n); 
			list2.add(makeRowOrCol(list.subList(i, end), JSplitPane.HORIZONTAL_SPLIT));
		}
		
		JScrollPane jsp = new JScrollPane(makeRowOrCol(list2, JSplitPane.VERTICAL_SPLIT));
		JPanel ret = new JPanel(new GridLayout(1,1));
		ret.add(jsp);
		return ret;
	}
	

	@SuppressWarnings("serial")
	public static JPanel makeTable(Border b, String border,
			Object[][] rowData, Object[] colNames) {
		JTable t = new JTable(rowData, colNames) {
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		JPanel p = new JPanel(new GridLayout(1, 1));
		TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());
		sorter.toggleSortOrder(0);
		t.setRowSorter(sorter);
		sorter.allRowsChanged();
		p.add(new JScrollPane(t));

		// p.setMaximumSize(new Dimension(200,200));
		p.setBorder(BorderFactory.createTitledBorder(b, border));
		return p;
	}

	public static <K, V> FUNCTION<V, K> invget(Map<K, V> m) {
		return v -> {
			for (Entry<K, V> e : m.entrySet()) {
				if (e.getValue().equals(v)) {
					return e.getKey();
				}
			}
			throw new RuntimeException("Cannot inverse lookup " + v + " in "
					+ m);
		};
	}

	public static String nice(String s) { //TODO
		return s;
//		return s.replace("[", "{").replace("]", "}");
	}

	 public static String sep(Collection<?> c, String sep) {
		return sep(c.iterator(), sep);
	}

	public static String sep(Iterator<?> c, String sep) {
		String ret = "";
		boolean b = false;
		while (c.hasNext()) {
			Object o = c.next();
			if (b) {
				ret += sep;
			}
			b = true;

			// if (o instanceof String && ((String)o).contains(" ")) {
			// ret += "\"" + o + "\"";
			// } else {
			ret += o;
			// }
		}
		return ret;
	} 

	public static String q(Object o) {
		if (o == null) {
			return "!!!NULL!!!";
		}
		String s = o.toString();
		if ((s.contains("\t") || s.contains("\n") || s.contains("\r")
				|| s.contains("-") || s.length() == 0)
				&& !s.contains("\"")) {
			return "\"" + s + "\"";
		}
		return s;
	}

	public static <X, Y> Y lookup(Set<Pair<X, Y>> s, X x) {
		for (Pair<X, Y> o : s) {
			if (o.first.equals(x)) {
				return o.second;
			}
		}
		throw new RuntimeException("Cannot find " + nice(x.toString()) + " in "
				+ nice(s.toString()));
	}

	public static <A, B, C> Map<A, C> compose0(Map<A, B> x, Map<B, C> y) {
		Map<A, C> ret = new HashMap<>();

		for (Entry<A,B> a : x.entrySet()) {
			ret.put(a.getKey(), y.get(a.getValue()));
		}
		
		return ret;
	}
	
	public static <X, Y, Z> Set<Pair<X, Z>> compose(Set<Pair<X, Y>> x,
			Set<Pair<Y, Z>> y) {
		Set<Pair<X, Z>> ret = new HashSet<>();

		for (Pair<X, Y> p1 : x) {
			for (Pair<Y, Z> p2 : y) {
				if (p1.second.equals(p2.first)) {
					Pair<X, Z> p = new Pair<>(p1.first, p2.second);
					ret.add(p);
				}
			}
		}
		return ret;
	} 

	public static <X> Set<Pair<X,X>> refl(Set<X> set) {
		Set<Pair<X,X>> ret = new HashSet<>();
		for (X x : set) {
			ret.add(new Pair<>(x,x));
		}
		return ret;
	}

	public static <X,Y> Map<X, Y> convert(Set<Pair<X, Y>> t) {
		Map<X,Y> ret = new HashMap<>();
		
		for (Pair<X,Y> p : t) {
			if (ret.containsKey(p.first)) {
				throw new RuntimeException("Cannot convert to map (not functional): " + t);
			}
			ret.put(p.first, p.second);
		}
		
		return ret;
	}
	
	public static <X,Y> Set<Pair<X, Y>> convert(Map<X, Y> t) {
		Set<Pair<X,Y>> ret = new HashSet<>();
		
		for (Entry<X,Y> p : t.entrySet()) {
			ret.add(new Pair<>(p.getKey(), p.getValue()));
		}
		
		return ret;
	}

	public static <X,Y> Y revLookup(Map<Y,X> m, X x) {
		Y ret = null;
		for (Entry<Y,X> e : m.entrySet()) {
			if (e.getValue().equals(x)) {
				if (ret != null && !ret.equals(e.getKey())) {
					throw new RuntimeException("Inverse is not a function: " + m);
				}
				ret = e.getKey();
			}
		}
		return ret;
	}
	
	public static <X,Y> Function<Y,X> inverse(Function<X,Y> f, Set<X> s) {
		Map<X,Y> m = reify(f, s);
		return y -> revLookup(m, y);
	}

	public static <X,Y> Set<Y> image(Set<X> set, Function<X,Y> f) {
		return set.stream().map(f).collect(Collectors.toSet());
	}

	public static String printForPi(Map<?,?> x) {
		if (x.size() == 0) {
			return "";
		}
		if (x.size() == 1) {
			return x.get(0).toString();
		}
		
		String ret = "(";
		boolean first = true;
		for (Entry<?,?> e : x.entrySet()) {
			if (!first) {
				ret += ", ";
			}
			first = false;
			ret += e.getValue();
		}
		ret += ")";
		return ret;
	}

}
