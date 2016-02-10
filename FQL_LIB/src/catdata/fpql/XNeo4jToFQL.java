package catdata.fpql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import javax.swing.JTextArea;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;

import catdata.Pair;
import catdata.Triple;
import catdata.Utils;
import catdata.fpql.XExp.XInst;
import catdata.fpql.XExp.XSchema;
import catdata.ide.CodeTextPanel;
import catdata.ide.Example;
import catdata.ide.Language;

public class XNeo4jToFQL {

	static String trans0(Map<String, Map<String, Object>> properties, Map<String, Set<Pair<String, String>>> edges) {
		labelForProperty(properties);
		Map<String, Set<String>> pfl = propsForLabels(properties);
		Map<String, Pair<String, String>> sfe = sortsForEges(edges, properties);
		
		XSchema xxx = toSchema(pfl, sfe);
		XInst yyy = toInst(properties, edges);
				
		return "dom : type\n" + Utils.sep(adom(properties), " ") + " : dom\n\nS = " + xxx.toString() + "\n\nI = " + yyy.toString() + " : S\n"; 
	}
	
	static Map<String, String> labelForProperty(Map<String, Map<String, Object>> properties) {
		Map<String, String> ret = new HashMap<>();
		
		for (String n : properties.keySet()) {
			Map<String, Object> p = properties.get(n);
			for (String k : p.keySet()) {
				if (k.equals("label")) {
					continue;
				}
				String v = (String) p.get("label");
				String v0 = ret.get(k);
				if (v0 == null) {
					ret.put(k, (String)p.get("label"));
				} else if (!v0.equals(v)) {
					throw new RuntimeException("Property " + k + " associated with labels " + v + " and " + v0);
				}
			}
		}
		
		return ret;
	}
	
	static XInst toInst(Map<String, Map<String, Object>> properties,
			Map<String, Set<Pair<String, String>>> edges) {

		List<Pair<String, String>> data = new LinkedList<>();
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();

		for (String n : properties.keySet()) {
			Map<String, Object> props = properties.get(n);
			data.add(new Pair<>(n, (String) props.get("label")));
			for (String p : props.keySet()) {
				if (p.equals("label")) {
					continue;
				}
				List<String> l = new LinkedList<>();
				List<String> r = new LinkedList<>();
				l.add(n);
				l.add(p);
				r.add((String) props.get(p));
				eqs.add(new Pair<>(l, r));
			}
		}
		
		for (String e : edges.keySet()) {
			for (Pair<String, String> p : edges.get(e)) {
				List<String> l = new LinkedList<>();
				List<String> r = new LinkedList<>();
				l.add(p.first);
				l.add(e);
				r.add(p.second);
				eqs.add(new Pair<>(l, r));
			}
		}
			
		XInst ret = new XInst(new XExp.Var("S"), data, eqs);
		return ret;
	}

	static XSchema toSchema(Map<String, Set<String>> propsForLabels, Map<String, Pair<String, String>> sortsForEdges) {
		List<String> labels = new LinkedList<>(propsForLabels.keySet());
		List<Triple<String, String, String>> arrows = new LinkedList<>();

		for (String l : labels) {
			for (String p : propsForLabels.get(l)) {
				arrows.add(new Triple<>(p, l, "dom"));
			}
		}
		
		for (String e : sortsForEdges.keySet()) {
			Pair<String, String> p = sortsForEdges.get(e);
			arrows.add(new Triple<>(e, p.first, p.second));
		}
		
		XSchema ret = new XSchema(labels, arrows, new LinkedList<>());
		return ret;
	}
	
	static Map<String, Pair<String, String>> sortsForEges(Map<String, Set<Pair<String, String>>> edges, Map<String, Map<String, Object>> nodes) {
		Map<String, Pair<String, String>> ret = new HashMap<>();
		
		for (String e : edges.keySet()) {
			for (Pair<String, String> p : edges.get(e)) {
				String s = p.first;
				String t = p.second;
				if (!nodes.containsKey(s)) {
					throw new RuntimeException("Unknown node: " + s);
				}
				if (!nodes.containsKey(t)) {
					throw new RuntimeException("Unknown node: " + t);
				}
				String sl = (String) nodes.get(s).get("label");
				String tl = (String) nodes.get(t).get("label");
				Pair<String, String> prev = ret.get(e);
				if (prev == null) {
					ret.put(e, new Pair<>(sl, tl));
				} else {
					if (!prev.equals(new Pair<>(sl, tl))) {
						throw new RuntimeException("Not well sorted on " + e + ", needs both " + prev + " and (" + sl + ", " + tl + ")");
					}
				}
			}
		}
		
		return ret;
	}
	
	static Set<Object> adom(Map<String, Map<String, Object>> nodes) {
		Set<Object> ret = new HashSet<>();
		
		for (String n : nodes.keySet()) {
			Map<String, Object> props = nodes.get(n);
			for (String p : props.keySet()) {
				if (p.equals("label")) {
					continue;
				}
				Object v = props.get(p);
				ret.add(v);
			}
		}

		return ret;
	}
	
	static Map<String, Set<String>> propsForLabels(Map<String, Map<String, Object>> nodes) {
		Map<String, Set<String>> ret = new HashMap<>();
		
		for (String n : nodes.keySet()) {
			Map<String, Object> props = nodes.get(n);
			String l = (String) props.get("label");
			Set<String> s = ret.get(l);
			if (s == null) {
				s = new HashSet<>();
				ret.put(l, s);
			}
			s.addAll(props.keySet());
			s.remove("label");
		}
		
		return ret;
	}
	
	
	protected Example[] examples = { new PeopleEx() };

	static class PeopleEx extends Example {
		
		@Override
		public Language lang() {
			return null;
		}

		@Override
		public String getName() {
			return "People";
		}

		@Override
		public String getText() {
			return people_example;
		}
		
	}
	
	String help = "Translates Neo4J Cypher into FPQL.  Graphs must have exactly one label per node and no properties on edges.";

	protected String kind() {
		return "Neo4j";
	}

	String translate(String in) {
		Pair<Map<String, Map<String, Object>>, Map<String, Set<Pair<String, String>>>> ne = program(in);
		
		String l = trans0(ne.first, ne.second);
		
		return l;
	}

	public XNeo4jToFQL() {
		final CodeTextPanel input = new CodeTextPanel(BorderFactory.createEtchedBorder(), kind()
				+ " Input", "");
		final CodeTextPanel output = new CodeTextPanel(BorderFactory.createEtchedBorder(),
				"FPQL Output", "");

		JButton transButton = new JButton("Translate");
		JButton helpButton = new JButton("Help");

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
					output.setText(translate(input.getText()));
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
				JDialog dialog = pane.createDialog(null, "Help on Neo4j to FPQL");
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
		JPanel tp = new JPanel(new GridLayout(1, 4));

		// bp.add(field);

		tp.add(transButton);
		tp.add(helpButton);
		// tp.add(jdbcButton);
		// tp.add(helpButton);
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
		JFrame f = new JFrame(kind() + " to FPQL");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}


	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "[", "]", "-" };

	static String[] res = new String[] { "CREATE" };

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);

	static final Parser<Void> IGNORED = Parsers.or(Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	static final Parser<?> TOKENIZER = Parsers.or(
			(Parser<?>) Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			RESERVED.tokenizer(), (Parser<?>) Terminals.Identifier.TOKENIZER,
			(Parser<?>) Terminals.IntegerLiteral.TOKENIZER);

	static Parser<?> term(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<?> ident() {
		return Terminals.Identifier.PARSER;
	}

	/*CREATE (n:label { a="b", c="d" }), ( ),  ; */
	static Parser<?> createNodes() {
		Parser<?> q = Parsers.tuple(ident(), term("="), string());
		Parser<?> p = Parsers.tuple(ident(), term(":"), ident(), Parsers.tuple(term("{"), q.sepBy(term(",")), term("}")));
		return Parsers.tuple(term("CREATE"), Parsers.tuple(term("("), p, term(")")).sepBy(term(",")));
	}
	
	/*CREATE (n) -[:label] ->(m)        */

	static Parser<?> createEdges() {
		Parser<?> n = ident().between(term("("), term(")"));
		Parser<?> e = Parsers.tuple(term("["), term(":"), ident(), term("]"));
		Parser<?> p = Parsers.tuple(n, term("-"), e, term("->"), n);
		return Parsers.tuple(term("CREATE"), p.sepBy(term(",")));
	}

	
	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);
	public static final Parser<?> program() {
		return Parsers.tuple(createNodes(), createEdges(), Parsers.always() /*, query().many() */);
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}
	
	@SuppressWarnings({  "rawtypes" })
	public static final Pair<Map<String, Map<String, Object>>, Map<String, Set<Pair<String, String>>>> program(String s) {
		Tuple3 o = (Tuple3) program.parse(s);
		
		Map<String, Map<String, Object>> ret1 = fromNodes(o.a);
		Map<String, Set<Pair<String, String>>> ret2 = fromEdges(o.b);

		return new Pair<>(ret1, ret2);
	}
/*		Parser<?> n = ident().between(term("("), term(")"));
		Parser<?> e = Parsers.tuple(term("["), term(":"), ident(), term("]"));
		Parser<?> p = Parsers.tuple(n, term("-"), e, term("->"), n);
		return Parsers.tuple(term("CREATE"), p.sepBy(term(","))); */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Set<Pair<String, String>>> fromEdges(Object oo) {
		Map<String, Set<Pair<String, String>>> ret = new HashMap<>();
		
		org.codehaus.jparsec.functors.Pair o = (org.codehaus.jparsec.functors.Pair) oo;
		List<Tuple5> xx = (List<Tuple5>) o.b;
		
		for (Tuple5 tt : xx) {
			String s = (String) tt.a;
			String t = (String) tt.e;
			Tuple4 e0 = (Tuple4) tt.c;
			String e = (String) e0.c;
			 Set<Pair<String, String>> set = ret.get(e);
			 if (set == null) {
				 set = new HashSet<>();
				 ret.put(e, set);
			 }
			 set.add(new Pair<>(s,t));
		}
		
		return ret;
	}
	
/*			Parser<?> q = Parsers.tuple(ident(), term("="), string());
		Parser<?> p = Parsers.tuple(ident(), term(":"), ident(), Parsers.tuple(term("{"), q.sepBy(term(",")), term("}")));
		return Parsers.tuple(term("CREATE"), Parsers.tuple(term("("), p, term(")")).sepBy(term(",")));
	*/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Map<String, Object>> fromNodes(Object oo) {
		Map<String, Map<String, Object>> ret = new HashMap<>();
		
		org.codehaus.jparsec.functors.Pair o = (org.codehaus.jparsec.functors.Pair) oo;
		List<Tuple3> xx = (List<Tuple3>) o.b;
		
		for (Tuple3 tt : xx) {
			Tuple4 t = (Tuple4) tt.b;
			
			String n = (String) t.a;
			String l = (String) t.c;
			if (ret.containsKey(n)) {
				throw new RuntimeException("Duplicate node: " + n);
			}
			Map<String, Object> m = new HashMap<>();
			m.put("label", l);
			
			Tuple3 pp = (Tuple3) t.d;
			List<Tuple3> p = (List<Tuple3>) pp.b;
			
			for (Tuple3 q : p) {
				String k = (String) q.a;
				String v = (String) q.c;
				if (m.containsKey(k)) {
					throw new RuntimeException("Duplicate property: " + k);
				}
				m.put(k, v);
			}
			ret.put(n, m);
		} 
		
		return ret; 
	}

	
	static String people_example = 
			"CREATE (n:person { age=61, name=bill }), (m:person { name=Mary }), (w:woman { })"
			+ "\n"
			+ "\nCREATE (n)-[:mother]->(w)";
}
