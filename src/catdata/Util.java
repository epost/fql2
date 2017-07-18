package catdata;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.commons.collections15.CollectionUtils;

public class Util {

	public static String quote(String s) {
		s = s.replace("\\", "\\" + "\\"); // \ --> \\
		s = s.replace("\"", "\\\""); // " --> \"
		return "\"" + s + "\"";
	}

	@SuppressWarnings("deprecation")
	public static <X> X timeout(Callable<X> c, long timeout) {
		// final Object lock = new Object();
		Ref<X> ret = new Ref<>();
		Ref<Exception> thr = new Ref<>();
		Thread t = new Thread(() -> {
			try {
				X x = c.call();
				synchronized (ret) {
					ret.set(x);
				}
			} catch (Exception exn) {
				exn.printStackTrace();
				synchronized (thr) {
					thr.set(exn);
				}
			} catch (OutOfMemoryError exn) {
				exn.printStackTrace();
				synchronized (thr) {
					thr.set(new RuntimeException("Out of memory.  Try increasing the java heap space (see manual)."));
				}
			} 
			catch (ThreadDeath d) {
				synchronized (thr) {
					thr.set(new RuntimeInterruptedException(d));
				}
			}
		});
		t.start();

		try {
			t.join(timeout);
		} catch (InterruptedException e) {
			t.stop();
			throw new RuntimeInterruptedException(e);
		}

		synchronized (thr) {
			synchronized (ret) {
				if (!ret.isSet() && !thr.isSet()) {
					t.stop();
					throw new RuntimeException("Timout after " + (timeout / 1000) + " seconds. \n\nPossible solution: add options timeout=X where X > " + (timeout / 1000) + " is how many seconds to wait.");
				} else if (ret.isSet() && !thr.isSet()) {
					// t should be dying
					return ret.x;
				} else if (!ret.isSet() && thr.isSet()) {
					// t should be dying
					if (thr.x instanceof RuntimeException) {
						throw (RuntimeException) thr.x;
					} else {
						throw new RuntimeException(thr.x);
					}
				} else {
					throw new RuntimeException("Anomaly: please report");
				}
			}
		}

	}

	public static Class<?> load(String clazz) {
		if (clazz == null) {
			return Util.anomaly();
		}
		try {
			return Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(clazz + " is not on the java classpath");
		}
	}

	public static <X, Y> List<Pair<X, Y>> toList(Map<X, Y> map) {
		List<Pair<X, Y>> ret = new LinkedList<>();
		for (Entry<X, Y> e : map.entrySet()) {
			ret.add(new Pair<>(e.getKey(), e.getValue()));
		}
		return ret;
	}

	public static <X> Set<X> toSetSafely(List<X> set) {
		Set<X> ret = new HashSet<>();
		for (X x : set) {
			if (ret.contains(x)) {
				throw new RuntimeException("Duplicate elmement " + x);
			}
			ret.add(x);
		}
		return ret;
	}

	public static <X> List<X> concat(List<List<X>> l) {
		List<X> ret = new LinkedList<>();
		for (List<X> x : l) {
			ret.addAll(x);
		}
		return ret;
	}

	public static <X> List<X> diff(Collection<X> l, Collection<?> r) {
		List<X> ret = new LinkedList<>(l);
		ret.removeAll(r);
		return ret;
	}

	public static <X, Y> Map<X, Y> diff(Map<X, Y> l, Map<?, ?> r) {
		Map<X, Y> ret = new HashMap<>(l);
		ret.keySet().removeAll(r.keySet());
		return ret;
	}

	public static <X> Function<Void, X> voidFn() {
		return v -> {
			throw new RuntimeException("Anomaly: please report");
		};
	}

	/*
	 * private static class VoidIter implements Iterator<Void> {
	 * 
	 * @Override public boolean hasNext() { return false; }
	 * 
	 * @Override public Void next() { throw new
	 * RuntimeException("Anomaly: please report"); }
	 * 
	 * }
	 */
	public static class MyTableRowSorter extends TableRowSorter<TableModel> {

		public MyTableRowSorter(TableModel model) {
			super(model);
		}

		@Override
		protected boolean useToString(int c) {
			return false;
		}

		@Override
		public Comparator<?> getComparator(int c) {
			return (o1, o2) -> {
				if (o1 instanceof Integer && o2 instanceof Integer) {
					return ((Integer) o1).compareTo((Integer) o2);
				}
				return o1.toString().compareTo(o2.toString());
			};
		}
	}

	@SuppressWarnings("serial")
	public static class BoldifyingColumnHeaderRenderer extends JLabel implements TableCellRenderer {

		private final Collection<?> boldify;
		private final Font normal = UIManager.getFont("TableHeader.font");
		private final Font bold = normal.deriveFont(Font.BOLD);
		private final TableCellRenderer r; // = new DefaultTableCellRenderer();

		public BoldifyingColumnHeaderRenderer(Collection<?> boldify, TableCellRenderer r) {
			this.boldify = boldify;
			this.r = r;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel ret = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (boldify.contains(value)) {
				ret.setFont(bold);
			} else {
				ret.setFont(normal);
			}
			return ret;
		}

	}

	public static void assertNotNull(Object... O) {
		for (Object o : O) {
			if (o == null) {
				throw new RuntimeException("Anomaly: please report ");
			}
		}
	}

	public static <X, Y, Z> Pair<LinkedHashMap<X, Y>, LinkedHashMap<X, Z>> split(Map<X, Chc<Y, Z>> map) {
		LinkedHashMap<X, Y> m1 = new LinkedHashMap<>();
		LinkedHashMap<X, Z> m2 = new LinkedHashMap<>();
		for (X x : map.keySet()) {
			Chc<Y, Z> e = map.get(x);
			if (e.left) {
				m1.put(x, e.l);
			} else {
				m2.put(x, e.r);
			}
		}
		return new Pair<>(m1, m2);
	}

	public static <X> X abort(Void v) {
		if (v == null) {
			throw new RuntimeException("Anomaly: please report");
		}
		throw new RuntimeException("Called on non-null void");
	}

	public static <X> List<X> newIfNull(List<X> l) {
		return l == null ? new LinkedList<>() : l;
	}

	public static String sep(Collection<?> c, String sep) {
		return sep(c.iterator(), sep);
	}

	public static String sep(Iterator<?> c, String sep) {
		return sep(c, sep, Object::toString);
	}
	
	public static <X> String sep(Iterator<X> c, String sep, Function<X, String> fun) {
		String ret = "";
		boolean b = false;
		while (c.hasNext()) {
			X o = c.next();
			if (b) {
				ret += sep;
			}
			b = true;

			ret += fun.apply(o);
		}
		return ret;
	}

	public static <X, Y> boolean isBijection(Map<X, Y> m, Set<X> X, Set<Y> Y) {
		if (!m.keySet().equals(X)) {
			return false;
		}
		if (!new HashSet<>(m.values()).equals(Y)) {
			return false;
		}
		Map<Y, X> n = rev(m, Y);
		if (n == null) {
			return false;
		}

		Map<X, X> a = compose0(m, n);
		Map<Y, Y> b = compose0(n, m);

		return a.equals(id(X)) && (!b.equals(id(Y)));
	}

	public static <X> Map<X, X> id(Collection<X> X) {
		Map<X, X> ret = new LinkedHashMap<>();
		for (X x : X) {
			ret.put(x, x);
		}
		return ret;
	}

	private static <X, Y> X rev(Map<X, Y> m, Y y) {
		X x = null;
		for (X x0 : m.keySet()) {
			Y y0 = m.get(x0);
			if (y0.equals(y)) {
				if (x != null) {
					return null;
				}
				x = x0;
			}
		}
		return x;
	}

	public static <A, B, C> Map<A, C> compose0(Map<A, B> x, Map<B, C> y) {
		Map<A, C> ret = new HashMap<>();

		for (Entry<A, B> a : x.entrySet()) {
			ret.put(a.getKey(), y.get(a.getValue()));
		}

		return ret;
	}

	private static <X, Y> Map<Y, X> rev(Map<X, Y> m, Set<Y> Y) {
		Map<Y, X> ret = new HashMap<>();

		for (Y y : Y) {
			X x = rev(m, y);
			if (x == null) {
				return null;
			}
			ret.put(y, x);
		}

		return ret;
	}

	public static <X> List<X> append(List<X> x, List<X> y) {
		List<X> ret = new LinkedList<>(x);
		ret.addAll(y);
		return ret;
	}

	@SuppressWarnings({ "unchecked", "SuspiciousArrayCast" })
	public static <X> X[] sing(X x) {
		return (X[]) new Object[] { x };
	}

	@SuppressWarnings("unchecked")
	public static <X, Y> Y[] map(X[] xs, Function<X, Y> f) {
		// noinspection SuspiciousArrayCast
		return (Y[]) Arrays.stream(xs).map(f).collect(Collectors.toList()).toArray();
	}

	public static <X, Y> Y fold(X[] xs, Y y, Function<Pair<X, Y>, Y> f) {
		for (X x : xs) {
			y = f.apply(new Pair<>(x, y));
		}
		return y;
	}

	@SafeVarargs
	public static <X> List<X> list(X... xs) {
		List<X> ret = new LinkedList<>();
		ret.addAll(Arrays.asList(xs));
		return ret;
	}

	public static <K, V> LinkedHashMap<K, V> listToMap(List<Pair<K, V>> list) {
		if (list == null) {
			throw new RuntimeException("Attempt to create a map with a null list");
		}
		LinkedHashMap<K, V> map = new LinkedHashMap<>();
		for (Pair<K, V> p : list) {
			if (p == null) {
				throw new RuntimeException("null pair in " + list);
			}
			if (p.first == null) {
				throw new RuntimeException("null first of pair in " + list);
			}
			if (p.second == null) {
				throw new RuntimeException("null second of pair in " + list);
			}
			if (map.containsKey(p.first)) {
				throw new RuntimeException("Duplicate entry for " + p.first + " in " + list);
			}
			map.put(p.first, p.second);
		}
		return map;
	}
	
	

	/**
	 * 
	 * @param m
	 *            target
	 * @param m2
	 *            source
	 */
	public static <K, V> void putAllSafely(Map<K, V> m, Map<K, V> m2) {
		for (K k : m2.keySet()) {
			V v2 = m2.get(k);
			if (!m.containsKey(k)) {
				m.put(k, v2);
				continue;
			}
			V v = m.get(k);
			if (!v.equals(v2)) {
				throw new RuntimeException("Collision on " + k + " was " + v + " becomes " + v2);
			}
		}

	}

	public static <X> X get0X(Collection<X> c) {
		for (X x : c) {
			return x;
		}
		throw new RuntimeException();
	}

	public static <X> X get0(Collection<X> c) {
		if (c.size() != 1) {
			throw new RuntimeException();
		}
		for (X x : c) {
			return x;
		}
		throw new RuntimeException();
	}

	static <X extends Comparable<X>, Y> String printNicely(Set<Map<X, Y>> map) {
		List<String> l = map.stream().map(Util::printNicely).collect(Collectors.toList());
		Collections.sort(l);
		return sep(l, "\n");
	}

	private static <X extends Comparable<X>, Y> String printNicely(Map<X, Y> map) {
		List<X> l = new LinkedList<>(map.keySet());
		Collections.sort(l);
		boolean first = true;
		String ret = "";
		for (X key : l) {
			if (!first) {
				ret += ", ";
			}
			ret += key + "=" + map.get(key);
			first = false;
		}
		return ret;
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public static String maybeQuote(String s) {
		if (s.trim().isEmpty()) {
			return "\"" + s + "\"";
		}
		Character x = s.charAt(0);
		if (!Character.isLetter(x) && !x.equals('_')) {
			return "\"" + s + "\"";
		}
		for (Character c : s.toCharArray()) {
			if (!Character.isLetterOrDigit(c) && !c.equals('_')) {
				return "\"" + s + "\"";
			}
		}
		return s;
	}

	public static void show(JComponent p, int w, int h, String title) {
		JFrame f = new JFrame(title);
		f.setContentPane(p);
		f.pack();
		if (w > 0 && h > 0) {
			f.setSize(new Dimension(w, h));
		}
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public static List<List<Integer>> multiply_many(List<List<Integer>> l, List<List<List<Integer>>> r) {
		List<List<Integer>> ret = l;
		for (List<List<Integer>> x : r) {
			ret = mat_conv2(mult(mat_conv1(x), mat_conv1(l)));
		}
		return ret;
	}

	public static List<List<Integer>> multiply(List<List<Integer>> l, List<List<Integer>> r) {
		return mat_conv2(mult(mat_conv1(l), mat_conv1(r)));
	}

	private static int[][] mat_conv1(List<List<Integer>> l) {
		int[][] ret = new int[l.size()][];
		int w = 0;
		for (List<Integer> r : l) {
			int[] q = new int[r.size()];
			int cnt = 0;
			for (int x : r) {
				q[cnt++] = x;
			}
			ret[w++] = q;
		}
		return ret;
	}

	// public for OPL example
	public static List<List<Integer>> mat_conv2(int[][] l) {
		List<List<Integer>> ret = new LinkedList<>();
		for (int[] r : l) {
			List<Integer> q = new LinkedList<>();
			for (int x : r) {
				q.add(x);
			}
			ret.add(q);
		}
		return ret;
	}

	private static int[][] mult(int[][] A, int[][] B) {
		int mA = A.length;
		int nA = A[0].length;
		int mB = B.length;
		int nB = B[0].length;
		if (nA != mB)
			throw new RuntimeException("Illegal matrix dimensions: " + mat_conv2(A) + " and " + mat_conv2(B));
		int[][] C = new int[mA][nB];
		for (int i = 0; i < mA; i++)
			for (int j = 0; j < nB; j++)
				for (int k = 0; k < nA; k++)
					C[i][j] += A[i][k] * B[k][j];
		return C;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Comparator<Object> LengthComparator = (Object o1, Object o2) -> {
		if (o1.toString().length() > o2.toString().length()) {
			return 1;
		} else if (o1.toString().length() < o2.toString().length()) {
			return -1;
		}
		return o1.toString().compareTo(o2.toString());
	};

	public static final Comparator<Object> AlphabeticalComparator = Comparator.comparing(Object::toString);

	public static <X, Y> Map<Y, X> rev0(Map<X, Y> m) {
		return rev(m, new HashSet<>(m.values()));
	}

	public static <X, Y> Map<Y, Set<X>> revS(Map<X, Y> m) {
		Map<Y, Set<X>> ret = new HashMap<>();
		for (X x : m.keySet()) {
			Y y = m.get(x);
			Set<X> s = ret.computeIfAbsent(y, k -> new HashSet<>());
			s.add(x);
		}
		return ret;
	}

	public static <X, Y> Map<X, Y> singMapM(X x, Y y) {
		Map<X, Y> ret = new HashMap<>();
		ret.put(x, y);
		return ret;
	}

	public static <X, Y> LinkedHashMap<X, Y> singMap(X x, Y y) {
		LinkedHashMap<X, Y> ret = new LinkedHashMap<>();
		ret.put(x, y);
		return ret;
	}

	public static <X, Y> Map<X, Y> singMap0(X x, Y y) {
		return singMap(x, y);
	}

	public static <X> List<X> singList(X x) {
		List<X> ret = new LinkedList<>();
		ret.add(x);
		return ret;
	}

	public static <X> Set<X> singSet(X x) {
		Set<X> ret = new HashSet<>();
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

	public static <X, Y> Map<X, Y> reify(Function<X, Y> f, Set<X> set) {
		Map<X, Y> ret = new HashMap<>();
		for (X x : set) {
			ret.put(x, f.apply(x));
		}
		return ret;
	}

	private static JPanel makeRowOrCol(List<JComponent> list, int orientation) {
		if (list.isEmpty()) {
			JPanel ret = new JPanel();
			ret.setBorder(BorderFactory.createEmptyBorder());
			return ret;
		}
		JPanel ret = new JPanel(new GridLayout(1, 1));
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
			int end = Math.min(list.size(), i + n);
			list2.add(makeRowOrCol(list.subList(i, end), JSplitPane.HORIZONTAL_SPLIT));
		}

		JScrollPane jsp = new JScrollPane(makeRowOrCol(list2, JSplitPane.VERTICAL_SPLIT));
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(jsp);
		return ret;
	}

	/*public static JPanel makeTable(Border b, String border, Object[][] rowData, Object... colNames) {
		return makeTable(null, b, border, rowData, colNames);
	}*/

	@SuppressWarnings("serial")
	public static JPanel makeTable(Border b, String border, Object[][] rowData, Object... colNames) {
		JTable t = new JTable(rowData, colNames) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		/*if (f != null) {
			t.setFont(f);
		}*/
		JPanel p = new JPanel(new GridLayout(1, 1));
		TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());
		if (colNames.length > 0) {
			sorter.toggleSortOrder(0);
		}
		t.setRowSorter(sorter);
		sorter.allRowsChanged();
		p.add(new JScrollPane(t));
		
		for (int row = 0; row < t.getRowCount(); row++) {
			int rowHeight = t.getRowHeight();

			for (int column = 0; column < t.getColumnCount(); column++) {
				Component comp = t.prepareRenderer(t.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			t.setRowHeight(row, rowHeight);
		}

		Font font = UIManager.getFont("TableHeader.font");
		p.setBorder(BorderFactory.createTitledBorder(b, border, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
		return p;

	}

	@SuppressWarnings("serial")
	//TODO aql merge with other makeTable method
	public static JPanel makeBoldHeaderTable(Collection<String> atts, Border b, String border, Object[][] rowData, String... colNames) {
		JTable t = new JTable(rowData, colNames) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		/*if (f != null) {
			t.setFont(f);
		}*/
		// PlusMinusCellRenderer r = new PlusMinusCellRenderer();
		// t.setDefaultRenderer(Object.class, r);
		// t.setDefaultEditor(Object.class, r);
		// t.setModel(new NonEditableModel(rowData, colNames));
		// t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel p = new JPanel(new GridLayout(1, 1));
		TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());
		if (colNames.length > 0) {
			sorter.toggleSortOrder(0);
		}
		t.setRowSorter(sorter);
		sorter.allRowsChanged();
		p.add(new JScrollPane(t));

		for (int row = 0; row < t.getRowCount(); row++) {
			int rowHeight = t.getRowHeight();

			for (int column = 0; column < t.getColumnCount(); column++) {
				Component comp = t.prepareRenderer(t.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			t.setRowHeight(row, rowHeight);
		}

		p.setBorder(BorderFactory.createTitledBorder(b, border));
		// t.getTableHeader().set
		for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
			TableColumn col = t.getColumnModel().getColumn(i);

			col.setHeaderRenderer(new BoldifyingColumnHeaderRenderer(atts, t.getTableHeader().getDefaultRenderer()));
		}
		
		Font font = UIManager.getFont("TableHeader.font");
		p.setBorder(BorderFactory.createTitledBorder(b, border, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));


		return p;

	}

	public static String nice(String s) {
		return s;
		// return s.replace("[", "{").replace("]", "}");
	}

	public static <X, Y> List<Y> proj2(Collection<Pair<X, Y>> l) {
		return l.stream().map(x -> x.second).collect(Collectors.toList());
	}

	public static <X, Y> List<X> proj1(Collection<Pair<X, Y>> l) {
		return l.stream().map(x -> x.first).collect(Collectors.toList());
	}

	public static String sep(Collection<?> order, Map<?, ?> m, String sep1, String sep2, boolean skip) {
		String ret = "";
		boolean b = false;
		for (Object o : order) {
			Object z = m.get(o);
			if (z == null && skip) {
				continue;
			}
			if (b) {
				ret += sep2;
			}
			b = true;
			ret += o + sep1 + m.get(o);
		}
		return ret;
	}

	public static String sep(Map<?, ?> m, String sep1, String sep2) {
		return sep(m.keySet(), m, sep1, sep2, false);
	}

	public static String q(Object o) {
		if (o == null) {
			return "!!!NULL!!!";
		}
		String s = o.toString();
		if ((s.contains("\t") || s.contains("\n") || s.contains("\r") || s.contains("-") || s.isEmpty()) && !s.contains("\"")) {
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
		throw new RuntimeException("Cannot find " + nice(x.toString()) + " in " + nice(s.toString()));
	}

	public static <X, Y, Z> Set<Pair<X, Z>> compose(Set<Pair<X, Y>> x, Set<Pair<Y, Z>> y) {
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

	public static <X> Set<Pair<X, X>> refl(Set<X> set) {
		Set<Pair<X, X>> ret = new HashSet<>();
		for (X x : set) {
			ret.add(new Pair<>(x, x));
		}
		return ret;
	}

	public static <X, Y> Map<X, Y> convert(Set<Pair<X, Y>> t) {
		Map<X, Y> ret = new HashMap<>();

		for (Pair<X, Y> p : t) {
			if (ret.containsKey(p.first)) {
				throw new RuntimeException("Cannot convert to map (not functional): " + t);
			}
			ret.put(p.first, p.second);
		}

		return ret;
	}

	public static <X, Y> void putSafely(Map<X, Y> ret, X k, Y v) {
		if (ret.containsKey(k) && !ret.get(k).equals(v)) {
			throw new RuntimeException("Two distinct bindings for " + k + ": " + v + " and " + ret.get(k));
		}
		ret.put(k, v);
	}

	public static <X, Y> Map<X, Y> toMapSafely(Collection<Pair<X, Y>> t) {
		Map<X, Y> ret = new HashMap<>();

		for (Pair<X, Y> p : t) {
			putSafely(ret, p.first, p.second);
		}

		return ret;
	}

	public static <X, Y> Set<Pair<X, Y>> convert(Map<X, Y> t) {
		Set<Pair<X, Y>> ret = new HashSet<>();

		for (Entry<X, Y> p : t.entrySet()) {
			ret.add(new Pair<>(p.getKey(), p.getValue()));
		}

		return ret;
	}

	public static <X, Y> Map<Y, X> invMap(Map<X, Y> m) {
		Map<Y, X> ret = new HashMap<>();

		for (Entry<X, Y> e : m.entrySet()) {
			if (ret.containsKey(e.getValue())) {
				throw new RuntimeException("Not injective");
			}
			ret.put(e.getValue(), e.getKey());
		}

		return ret;
	}

	public static <X, Y> Y revLookup(Map<Y, X> m, X x) {
		Y ret = null;
		for (Entry<Y, X> e : m.entrySet()) {
			if (e.getValue().equals(x)) {
				if (ret != null && !ret.equals(e.getKey())) {
					throw new RuntimeException("Inverse is not a function: " + m);
				}
				ret = e.getKey();
			}
		}
		return ret;
	}

	public static <X, Y> Function<Y, X> inverse(Function<X, Y> f, Set<X> s) {
		Map<X, Y> m = reify(f, s);
		return y -> revLookup(m, y);
	}

	public static <X, Y> Set<Y> image(Set<X> set, Function<X, Y> f) {
		return set.stream().map(f).collect(Collectors.toSet());
	}

	public static <X, Y> X anyKey(Map<X, Y> m) {
		for (X x : m.keySet()) {
			return x;
		}
		throw new RuntimeException();
	}

	public static String printForPi(Map<?, ?> x) {
		if (x.isEmpty()) {
			return "";
		}
		if (x.size() == 1) {
			return x.get(0).toString();
		}

		String ret = "(";
		boolean first = true;
		for (Entry<?, ?> e : x.entrySet()) {
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
				return new Pair<>(x -> Chc.inLeft(p.first.apply(x)), p.second);
			} else {
				Pair<Function, Object> p = stripChcs(c.r);
				return new Pair<>(x -> Chc.inRight(p.first.apply(x)), p.second);
			}
		}
		return new Pair<>(x -> x, o);
	}

	public static <X> List<List<X>> prod(List<Set<X>> in1) {
		List<List<X>> y = new LinkedList<>();
		y.add(new LinkedList<>());

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

	public static <X> List<X> reverse(List<X> l) {
		List<X> ret = new LinkedList<>(l);
		Collections.reverse(ret);
		return ret;
	}

	public static <T> void assertNoDups(Collection<T> list) {

		Set<T> duplicates = new HashSet<>();
		Set<T> uniques = new HashSet<>();

		for (T t : list) {
			if (!uniques.add(t)) {
				duplicates.add(t);
			}
		}

		if (!duplicates.isEmpty()) {
			throw new RuntimeException("List contains duplicates, namely " + duplicates);
		}
	}

	/**
	 * @return
	 */
	public static <Ty> List<Ty> alphabetical(Collection<Ty> tys) {
		List<Ty> ret = new LinkedList<>(tys);
		ret.sort(AlphabeticalComparator);
		return ret;
	}
	
	public static <Ty> Collection<Ty> alphaMaybe(boolean b, Collection<Ty> tys) {
		if (b) {
			return alphabetical(tys);
		}
		return tys;
	}

	public static <X> List<String> shortest(Collection<X> set) {
		List<String> ret = set.stream().map(Object::toString).collect(Collectors.toList());
		ret.sort(LengthComparator);
		return ret;
	}

	public static <X, Y, Z> Map<X, Map<Y, Z>> newMapsFor(Collection<X> xs) {
		Map<X, Map<Y, Z>> ret = new HashMap<>();
		for (X x : xs) {
			ret.put(x, new HashMap<>());
		}
		return ret;
	}

	public static <X, Y> Map<X, Set<Y>> newSetsFor(Collection<X> xs) {
		Map<X, Set<Y>> ret = new HashMap<>();
		for (X x : xs) {
			ret.put(x, new HashSet<>());
		}
		return ret;
	}

	public static <X, Y> Map<X, Collection<Y>> newSetsFor0(Collection<X> xs) {
		Map<X, Collection<Y>> ret = new HashMap<>();
		for (X x : xs) {
			ret.put(x, new HashSet<>());
		}
		return ret;
	}

	public static <X, Y> Map<X, List<Y>> newListsFor(Collection<X> xs) {
		Map<X, List<Y>> ret = new HashMap<>();
		for (X x : xs) {
			ret.put(x, new LinkedList<>());
		}
		return ret;
	}

	public static <X, Y> Map<X, Y> constMap(Collection<X> xs, Y y) {
		Map<X, Y> ret = new HashMap<>();
		for (X x : xs) {
			ret.put(x, y);
		}
		return ret;
	}

	public static <X extends Z, Y extends Z, Z> Set<Z> union(Collection<X> x, Collection<Y> y) {
		Set<Z> ret = new HashSet<>(x);
		ret.addAll(y);
		return ret;
	}

	public static <X> List<String> toString(Collection<X> list) {
		return list.stream().map(Object::toString).collect(Collectors.toList());
	}

	public static <T> Set<Set<T>> powerSet(Collection<T> originalSet) {
		Set<Set<T>> sets = new HashSet<>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<>());
			return sets;
		}
		List<T> list = new ArrayList<>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	public static final Comparator<Object> ToStringComparator = (Object o1, Object o2) -> {
		if (o1.toString().length() > o2.toString().length()) {
			return 1;
		} else if (o1.toString().length() < o2.toString().length()) {
			return -1;
		}
		return o1.toString().compareTo(o2.toString());
	};

	public static <X> X anomaly() {
		throw new RuntimeException("Anomaly: please report");
	}

	public static void centerLineInScrollPane(JTextComponent component)
	{
		Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

		if (container == null) return;

		try
		{
			Rectangle r = component.modelToView(component.getCaretPosition());
			JViewport viewport = (JViewport)container;
			int extentHeight = viewport.getExtentSize().height;
			int viewHeight = viewport.getViewSize().height;
			if (r == null || viewport == null) {
				return;
			}
			int y = Math.max(0, r.y - ((extentHeight - r.height) / 2));
			y = Math.min(y, viewHeight - extentHeight);

			viewport.setViewPosition(new Point(0, y));
		}
		catch(BadLocationException ble) {}
	}
	
	public static <X, Y> Collection<Object> isect(Collection<X> xs, Collection<Y> ys) {
		return CollectionUtils.intersection(xs, ys);
	}

	/**
	 * Levenshtein Edit Distance
	 * http://rosettacode.org/wiki/Levenshtein_distance#Java
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	/**
	 * Calculates a similarity (a number within 0 and 1) between two strings as
	 * 1 / 1 + editDistance
	 * 
	 */
	public static double similarity(String s1, String s2) { // TODO aql
		return (1) / ((double) 1 + editDistance(s1, s2));
	}

	public static String readFile(Reader r) throws IOException {
		try (BufferedReader reader = new BufferedReader(r)) {
			String line;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			reader.close();
			return stringBuilder.toString();
		}
	}

	public static String readFile(InputStream file) {
		try (InputStreamReader r = new InputStreamReader(file)) {

			return readFile(r);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	}

	public static String readFile(String file) {
		try (FileReader r = new FileReader(file)) {
			return readFile(r);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	}

	public static String readFile(File file) {
		try (FileReader r = new FileReader(file)) {
			return readFile(r);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	} 
	
	public static <T> Iterable<List<T>> permutationsOf(List<T> l) {
		return new Iterable<List<T>>() {
			@Override
			public Iterator<List<T>> iterator() {
				return new PermutationIterator<>(l);
			}
		};
	}
	
	//seems like heap's algorithm
	private static final class PermutationIterator<T> 
    implements Iterator<List<T>> {

        private List<T> nextPermutation;
        private final List<T> allElements = new ArrayList<>();
        private int[] indices;

        PermutationIterator(List<T> allElements) {
            if (allElements.isEmpty()) {
                nextPermutation = null;
                return;
            }

            this.allElements.addAll(allElements);
            this.indices = new int[allElements.size()];

            for (int i = 0; i < indices.length; ++i) {
                indices[i] = i;
            }

            nextPermutation = new ArrayList<>(this.allElements);
        }

        @Override
        public boolean hasNext() {
            return nextPermutation != null;
        }

        @Override
        public List<T> next() {
            if (nextPermutation == null) {
                throw new NoSuchElementException("No permutations left.");
            }

            List<T> ret = nextPermutation;
            generateNextPermutation();
            return ret;
        }

        private void generateNextPermutation() {
            int i = indices.length - 2;

            while (i >= 0 && indices[i] > indices[i + 1]) {
                --i;
            }

            if (i == -1) {
                // No more new permutations.
                nextPermutation = null;
                return;
            }

            int j = i + 1;
            int min = indices[j];
            int minIndex = j;

            while (j < indices.length) {
                if (indices[i] < indices[j] && indices[j] < min) {
                    min = indices[j];
                    minIndex = j;
                }

                ++j;
            }

            swap(indices, i, minIndex);

            ++i;
            j = indices.length - 1;

            while (i < j) {
                swap(indices, i++, j--);
            }

            loadPermutation();
        }

        private void loadPermutation() {
            List<T> newPermutation = new ArrayList<>(indices.length);

            for (int i : indices) {
                newPermutation.add(allElements.get(i));
            }

            this.nextPermutation = newPermutation;
        }
    

    private static void swap(int[] array, int a, int b) {
        int tmp = array[a];
        array[a] = array[b];
        array[b] = tmp;
    }

   
}

	public static <X,Y> boolean containsUpToCase(Collection<X> set, Y s) {
		for (X t : set) {
			if (s.toString().toLowerCase().equals(t.toString().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static String[] union(String[] res, String[] res2) {
		String[] ret = new String[res.length + res2.length];
		System.arraycopy(res , 0, ret, 0, res.length);
		System.arraycopy(res2, 0, ret, res.length, res2.length);
		return ret;
	}

	public static void checkClass(String clazz) {
		try {
			if (!clazz.trim().isEmpty()) {
				Class.forName(clazz);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
