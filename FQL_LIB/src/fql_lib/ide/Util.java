package fql_lib.ide;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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

import catdata.algs.Chc;
import catdata.algs.Pair;
import catdata.algs.Utils;
import fql_lib.fqlpp.FUNCTION;
import fql_lib.opl.OplTerm;

public class Util {
	
	public static Comparator<Object> ToStringComparator = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			if (o1.toString().length() > o2.toString().length()) {
				return 1;
			} else if (o1.toString().length() < o2.toString().length()) {
				return -1;
			}
			return o1.toString().compareTo(o2.toString());
		}
	};
	
	

	
	public static <X,Y> Map<Y,X> rev0(Map<X, Y> m) {
		return Utils.rev(m, new HashSet<>(m.values()));
	}
	
	public static <X,Y> Map<Y,Set<X>> revS(Map<X, Y> m) {
		Map<Y,Set<X>> ret = new HashMap<>();
		for (X x : m.keySet()) {
			Y y = m.get(x);
			Set<X> s = ret.get(y);
			if (s == null) {
				s = new HashSet<>();
				ret.put(y, s);
			}
			s.add(x);
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
		if (colNames.length > 0) {
			sorter.toggleSortOrder(0);
		}
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

			ret += o;
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

	public static <X,Y> X anyKey(Map<X,Y> m) {
		for (X x : m.keySet()) {
			return x;
		}
		throw new RuntimeException();
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Pair<Function, Object> stripChcs(Object o) {
		 if (o instanceof Chc) {
			 Chc c = (Chc) o;
			 if (c.left) {
				 Pair<Function, Object> p = stripChcs(c.l);
				 return new Pair<Function, Object>(x -> { return Chc.inLeft(p.first.apply(x)); }, p.second);
			 } else {
				 Pair<Function, Object> p = stripChcs(c.r);
				 return new Pair<Function, Object>(x -> { return Chc.inRight(p.first.apply(x)); }, p.second);
			 }
		 }
		 return new Pair<Function, Object>(x -> { return x; }, o);
	 }

	public static <V> double termToDouble(OplTerm<?,V> t) {
		return termToNat(t);
	}
	
	public static <V> Integer termToNat(OplTerm<?,V> t) {
		if (t.var != null) {
			return null;
		}
		if (stripChcs(t.head).second.equals("zero")) { 
			return 0;
		} else if (stripChcs(t.head).second.equals("succ")) { 
			if (t.args.size() != 1) {
				return null;
			}
			Integer j = termToNat(t.args.get(0));
			if (j == null) {
				return null;
			}
			return 1 + j;
		} 
		return null;
	}
		
	public static <V> OplTerm<String, V> natToTerm(int i) {
		if (i < 0) {
			throw new RuntimeException("Cannot convert negative number to natural");
		}
		if (i == 0) {
			return new OplTerm<>("zero", new LinkedList<>());
		}
		return new OplTerm<>("succ", Util.singList(natToTerm(i-1)));
	}

	public static <X> List<List<X>> prod(List<Set<X>> in1) {
		List<List<X>> y = new LinkedList<>();
		List<X> z = new LinkedList<>();
		y.add(z);
	
		for (Set<X> X : in1) {
			List<List<X>> y0 = new LinkedList<>();
			for (List<X> a : y) {
				for (X x : X) {
					List<X> toadd = new LinkedList<>(a);
					toadd.add(x);
					y0.add(toadd);
				}
			}
			y = y0;
		}
	
		return y;
	}
}
