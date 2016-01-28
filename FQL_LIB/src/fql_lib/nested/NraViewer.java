package fql_lib.nested;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;

import catdata.algs.Pair;
import catdata.algs.Triple;
import fql_lib.core.CodeTextPanel;
import fql_lib.core.Example;
import fql_lib.core.Language;
import fql_lib.core.Util;

public class NraViewer {

	protected Example[] examples = { new PeopleExample() };

	String help = ""; // "SQL schemas and instances in categorical normal form (CNF) can be treated as FQL instances directly.  To be in CNF, every table must have a primary key column called id.  This column will be treated as a meaningless ID.  Every column in a table must either be a string, an integer, or a foreign key to another table.  Inserted values must be quoted.  See the People example for details.";

	protected String kind() {
		return "NR Schema";
	}

	static class PeopleExample extends Example {
		
		@Override
		public Language lang() {
			throw new RuntimeException();
		}

		@Override
		public String getName() {
			return "People";
		}

		@Override
		public String getText() {
			return extext1;
		}
	}
/*
	public abstract static class Inst {

	}

	public abstract static class Ty {

	}

	public static class Const extends Inst {

		String s;

		public Const(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}

	public static class Dom extends Ty {
		@Override
		public String toString() {
			return "string";
		}
	}

	public static class Rcd extends Ty {
		Map<String, Ty> row;

		public Rcd(Map<String, Ty> row) {
			this.row = row;
		}

		@Override
		public String toString() {
			boolean first = false;
			String ret = "";
			for (Entry<String, Ty> e : row.entrySet()) {
				if (first) {
					ret += ", ";
				}
				ret += e.getKey() + ": " + e.getValue();
				first = true;
			}
			return "(" + ret + ")";
		}
	}

	public static class Tpl extends Inst {
		Map<String, Inst> row;

		public Tpl(Map<String, Inst> row) {
			this.row = row;
		}

		@Override
		public String toString() {
			boolean first = false;
			String ret = "";
			for (Entry<String, Inst> e : row.entrySet()) {
				if (first) {
					ret += ", ";
				}
				ret += e.getKey() + ": " + e.getValue();
				first = true;
			}
			return "(" + ret + ")";
		}
	}

	public static class St extends Inst {
		Set<Tpl> row;

		public St(Set<Tpl> row) {
			this.row = row;
		}

		@Override
		public String toString() {
			boolean first = false;
			String ret = "";
			for (Tpl e : row) {
				if (first) {
					ret += ", ";
				}
				ret += e;
				first = true;
			}
			return "{" + ret + "}";
		}
	}

	public static class Pow extends Ty {
		Rcd row;

		public Pow(Rcd row) {
			this.row = row;
		}

		@Override
		public String toString() {
			return "set " + row;
		}
	} */

	static TableCellRenderer jTableCellRenderer = new TableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = (Component) value;
			int h = table.getRowHeight(row);
			int j = 20 + c.getPreferredSize().height;
			if (j > h) {
			table.setRowHeight(row, j);
			}
			return c;
		}
	};
	
	public static class NRel {
		Map<String, Optional<NRel>> t;
		
		public NRel(Map<String, Optional<NRel>> t) {
			this.t = t;
		}
		
		@Override
		public String toString() {
			String ret = "";
			boolean first = true;
			for (Entry<String, Optional<NRel>> e : t.entrySet()) {
				if (!first) {
					ret += ", ";
				}
				first = false;
				ret += e.getKey() + ": " + (e.getValue().isPresent() ? e.getValue().get().toString() : "string");
			}
			return "set (" + ret + ")"; //t.toString();
		}
	}

	Triple<String, Component, Component> translate(String in) {
		Pair<NRel, Object> p = program(in);
		
		return new Triple<>("OK", conv(p.first, p.second), disp(shred(p.first, (Set<Map>) p.second)));
	}

	
	private Component disp(Map<NRel, Set<Map>> O){
		int n = (int) Math.ceil(Math.sqrt(O.size()));
		
		JPanel ret = new JPanel(new GridLayout(n,n));
		
		for (Entry<NRel, Set<Map>> e : O.entrySet()) {
			Set<Map> s = e.getValue();
			NRel t = e.getKey();
			Object[] colNames = new Object[t.t.size()];
			Object[][] rowData = new Object[s.size()][t.t.size()];

			int col = 0;
			for (Entry<String, Optional<NRel>> k : t.t.entrySet()) {
				colNames[col++] = k.getKey();
			}

			int r = 0;
			for (Map<String, Object> o : s) {
				col = 0;
				for (Entry<String, Optional<NRel>> k : t.t.entrySet()) {
					if (!k.getValue().isPresent()) {
						rowData[r][col++] = (String)o.get(k.getKey());
					} else {
						throw new RuntimeException("Not totally shredded");
					}
				}
				r++;
			}
			JPanel tbl = Util.makeTable(BorderFactory.createEtchedBorder(), e.getKey().toString(), rowData, colNames);
			ret.add(tbl);
		}
		
		return ret;
	}


	public Map<NRel, Set<Map>> shred(NRel t, Set<Map> s) {
		Map<NRel, Set<Map>> ret = new HashMap<>();
		ret.put(t, s);
		
		int[] ref = new int[] { 0 };
		for (;;) {
		//	System.out.println("Starting: " + ret);
			Map<NRel, Set<Map>> ret2 = new HashMap<>();
			for (Entry<NRel, Set<Map>> e : ret.entrySet()) {
				ret2.putAll(unnest(e.getKey(), e.getValue(), ref));
			}
			if (ret2.size() == ret.size()) {
				return ret2;
			}
			ret = ret2;
		//	System.out.println("------");
		}
	}
	
	public <X,Y> Set<Pair<X, Y>> unnest1(Map<X, Set<Y>> s) {
		Set<Pair<X, Y>> ret = new HashSet<>();
		
		for (Entry<X, Set<Y>> x : s.entrySet()) {
			for (Y y : x.getValue()) {
				ret.add(new Pair<>(x.getKey(), y));
			}
		}

		return ret;
	}
	
	
	public Map<NRel, Set<Map>> unnest(NRel t, Set<Map> s, int[] ref) {
	//	System.out.println("Unnesting " + s + " at type " + t);
		Set<Map> ret = new HashSet<>();
		Map<NRel, Map<Integer, Set<Map>>> temp = new HashMap<>();
		Map<String, Optional<NRel>> nw = new HashMap<>();
		for (Map m : s) {
			Map n = new HashMap<>();
			for (Entry<String, Optional<NRel>> k : t.t.entrySet()) {
				if (!k.getValue().isPresent()) {
					n.put(k.getKey(), m.get(k.getKey()));
					nw.put(k.getKey(), Optional.empty());
				}
				else {
					Map<Integer, Set<Map>> u = temp.get(k.getValue().get());
					if (u == null) {
						u = new HashMap<>();
						temp.put(k.getValue().get(), u);
					}
					int z = ref[0]++;
				//	tempX.put(m, z);
					u.put(z, (Set<Map>) m.get(k.getKey()));
					n.put(k.getKey(), Integer.toString(z));
					nw.put(k.getKey(), Optional.empty());
				}
			}
			ret.add(n);
		}	
		
//		System.out.println("temp " + temp);
		
		Map<NRel, Set<Pair<Integer, Map>>> temp2 = new HashMap<>();
		for (Entry<NRel, Map<Integer, Set<Map>>> k : temp.entrySet()) {
			Set<Pair<Integer, Map>> w = unnest1(k.getValue());
			temp2.put(k.getKey(), w);
		}
		
		Map<NRel, Set<Map>> retX = new HashMap<>();
		retX.put(new NRel(nw), ret);
		for (Entry<NRel, Set<Pair<Integer, Map>>> e : temp2.entrySet()) {
			Map vvv = new HashMap<>(e.getKey().t);
			vvv.put("ID", Optional.empty());
			retX.put(new NRel(vvv), conv1(e.getValue()));
		}
		
//		System.out.println("Result: " + retX);
		return retX;
	}
	
	
	
	private Set<Map> conv1(Set<Pair<Integer, Map>> set) {
		Set<Map> ret = new HashSet<>();
		for (Pair<Integer, Map> e : set) {
			Map n = new HashMap<>();
			n.put("ID", e.first.toString());
			n.putAll(e.second);
			ret.add(n);
		}
		return ret;
	}


	public Component conv(NRel t, Object i) {
		Set<Map<String, Object>> s = (Set<Map<String, Object>>) i;

		Object[] colNames = new Object[t.t.size()];
		Object[][] rowData = new Object[s.size()][t.t.size()];

		int col = 0;
		for (Entry<String, Optional<NRel>> k : t.t.entrySet()) {
			colNames[col++] = k.getKey();
		}

		int r = 0;
		for (Map<String, Object> o : s) {
			col = 0;
			for (Entry<String, Optional<NRel>> k : t.t.entrySet()) {
				if (!k.getValue().isPresent()) {
					rowData[r][col++] = new JLabel((String)o.get(k.getKey()));
				} else {
					rowData[r][col++] =	conv(k.getValue().get(), o.get(k.getKey()));
				}
			}
			r++;
		}

		JTable tbl = new JTable(rowData, colNames) {
			public boolean isCellEditable(int row, int column) {                
	                return false;               
	        };
	        public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		
//		JTable tbl = new JTable(rowData, colNames);
	//	tbl.set
		//tbl.setDefaultRenderer(JLabel.class, jTableCellRenderer);
	//	tbl.setDefaultRenderer(JTable.class, jTableCellRenderer);
		
		TableColumnModel tcm = tbl.getColumnModel();
		for(int it = 0; it < tcm.getColumnCount(); it++){
		tcm.getColumn(it).setCellRenderer(jTableCellRenderer);
		} 
		
	//	JPanel ret = new JPanel(new GridLayout(1,1));
	//	ret.add(tbl);
		
		return new JScrollPane(tbl);
	}

	public NraViewer() {
		final CodeTextPanel input = new CodeTextPanel(BorderFactory.createEtchedBorder(),
				"Input schema and instance", "");
		final CodeTextPanel output = new CodeTextPanel(BorderFactory.createEtchedBorder(),
				"Response", "");

		// JButton jdbcButton = new JButton("Load using JDBC");
		// JButton runButton = new JButton("Run " + kind());
		JButton transButton = new JButton("Shred");
		JButton helpButton = new JButton("Help");
		// JButton runButton2 = new JButton("Run FQL");
		// JCheckBox jdbcBox = new JCheckBox("Run using JDBC");
		// JLabel lbl = new JLabel("Suffix (optional):", JLabel.RIGHT);
		// lbl.setToolTipText("FQL will translate table T to T_suffix, and generate SQL to load T into T_suffix");
		// final JTextField field = new JTextField(8);
		// field.setText("fql");

		final JComboBox<Example> box = new JComboBox<>(examples);
		box.setSelectedIndex(-1);
		box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				input.setText(((Example) box.getSelectedItem()).getText());
			}
		});

		transButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Triple<String, Component, Component> p = translate(input.getText());
					output.setText(p.first);
					
					JPanel pan = new JPanel(new GridLayout(1,1));
					pan.add(p.second);
					JFrame f = new JFrame("NR Shredder Input");
					f.setContentPane(pan);
					f.pack();
					f.setSize(new Dimension(600, 500));
					f.setLocationRelativeTo(null);
					f.setVisible(true);
					
					pan = new JPanel(new GridLayout(1,1));
					pan.add(p.third);
					f = new JFrame("NR Shredder Output");
					f.setContentPane(pan);
					f.pack();
					f.setSize(new Dimension(650, 450));
					f.setLocationRelativeTo(null);
					f.setVisible(true);
		
				} catch (Exception ex) {
					ex.printStackTrace();
					output.setText(ex.getLocalizedMessage());
				}
			}
		});

		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextArea jta = new JTextArea(help);
				jta.setWrapStyleWord(true);
				// jta.setEditable(false);
				jta.setLineWrap(true);
				JScrollPane p = new JScrollPane(jta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				p.setPreferredSize(new Dimension(300, 200));

				JOptionPane pane = new JOptionPane(p);
				// Configure via set methods
				JDialog dialog = pane.createDialog(null, "Help on NR shredder");
				dialog.setModal(false);
				dialog.setVisible(true);
				dialog.setResizable(true);

			}
		});

		JPanel p = new JPanel(new BorderLayout());

		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		jsp.setDividerSize(4);
		jsp.setResizeWeight(0.5d);
		jsp.add(input);
		jsp.add(output);

		// JPanel bp = new JPanel(new GridLayout(1, 5));
		JPanel tp = new JPanel(new GridLayout(1, 5));

		// bp.add(field);

		tp.add(transButton);
		tp.add(helpButton);
		// tp.add(jdbcButton);
		// tp.add(helpButton);
		tp.add(new JLabel());
		tp.add(new JLabel("Load Example", JLabel.RIGHT));
		tp.add(box);

		// bp.add(runButton);
		// bp.add(runButton2);
		// bp.add(lbl);
		// bp.add(field);
		// bp.add(jdbcBox);

		// p.add(bp, BorderLayout.SOUTH);
		p.add(jsp, BorderLayout.CENTER);
		p.add(tp, BorderLayout.NORTH);
		JFrame f = new JFrame("NR Shredder");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	static String extext1 = "set (name:string, age:string, kids:set (name:string, gender:string, friends: set (name:string)))"
	+ "\n"
	+ "\n{(name:bill, age:30, kids:{(name:alice, gender:F, friends: {(name: kid1), (name: kid2), (name: kid3)}),"
	+ "\n                           (name:sue,   gender:F, friends: {(name: kid1), (name: kid3)}),"
	+ "\n                           (name:joe,   gender:M, friends: {(name: kid3)})}),"
	+ "\n (name:bob , age:40, kids:{(name:chuck, gender:M, friends: {}),  "
	+ "\n                           (name:kim,   gender:F, friends: {(name: kid1), (name: kid2), (name: kid3)})})"
	+ "\n}"
	+ "\n";




	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(", ")", "=", "->", "+",
			"*", "^", "|" };

	static String[] res = new String[] { "set", "string" };

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);

	static final Parser<Void> IGNORED = Parsers.or(Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	static final Parser<?> TOKENIZER = Parsers.or(
			(Parser<?>) Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, RESERVED.tokenizer(),
			(Parser<?>) Terminals.Identifier.TOKENIZER,
			(Parser<?>) Terminals.IntegerLiteral.TOKENIZER);

	static Parser<?> term(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<?> ident() {
		return Terminals.Identifier.PARSER;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Pair<NRel, Object> program(String s) {
		Object o = program.parse(s);
		return toProg(o);
		// return to(o);
	}

	public static final Parser<?> program = Parsers.tuple(ty(), inst()).from(TOKENIZER, IGNORED);

	static Pair<NRel, Object> toProg(Object o) {
		org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
		return new Pair<>(toTy(p.a), toInst(p.b));
	}

	static NRel toTy(Object o) {
		Tuple4 e = (Tuple4) o;
		
		List l = (List) e.c;
		
		Map<String, Optional<NRel>> m = new HashMap();
		
		for (Object x : l) {
			Tuple3 u = (Tuple3) x;
			if (u.c.toString().equals("string")) {
				m.put(u.a.toString(), Optional.empty());
			} else {
				m.put(u.a.toString(), Optional.of(toTy(u.c)));
			}
		}

		return new NRel(m);
	}

	static Object toInst(Object o) {
		if (o instanceof String) {
			return o;
		}
		Tuple3 t = (Tuple3) o;
		List l = (List) t.b;
		if (t.a.toString().equals("(")) {
			java.util.Map<String, Object> m = new HashMap();
			for (Object lx : l) {
				Tuple3 u = (Tuple3) lx;
				m.put(u.a.toString(), toInst(u.c));
			}
			return m;
		} else {
			Set m = new HashSet<>();
			for (Object lx : l) {
				m.add(toInst(lx));
			}
			return m;
		}
	}

	public static final Parser<?> inst() {
		Reference ref = Parser.newReference();

		Parser rcd = Parsers.tuple(term("("),
				Parsers.tuple(ident(), term(":"), ref.lazy()).sepBy(term(",")), term(")"));
		Parser set = Parsers.tuple(term("{"), rcd.sepBy(term(",")), term("}"));
		Parser p = Parsers.or(string(), rcd, set);

		ref.set(p);

		return p;
	}

	public static final Parser<?> ty() {
		Reference ref = Parser.newReference();

		Parser p = Parsers.tuple(term("set"), term("("),
				Parsers.tuple(ident(), term(":"), ref.lazy().or(term("string"))).sepBy(term(",")), term(")"));
		
		ref.set(p);

		return p;
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER, Terminals.IntegerLiteral.PARSER,
				Terminals.Identifier.PARSER);
	}
	

}
