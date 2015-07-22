package fql_lib.opl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;

import fql_lib.Pair;
import fql_lib.Util;
import fql_lib.examples.Example;
import fql_lib.gui.FQLTextPanel;


public class CfgToOpl {
	
	static class STLCExample extends Example {

		@Override
		public String getName() {
			return "STLC";
		}

		@Override
		public String getText() {
			return s;
		}
		
		String s = "t ::= 1 | t \"*\" t | t \"^\" t"
				+ "\n\n, //separate productions by ,\n"
				+ "\ne ::= id t | e \";\" e | \"!\" t | \"(\" e \",\" e \")\" | fst t t | snd t t | curry e | eval t t";
		
	}

	protected Example[] examples = { new STLCExample() } ; //{ new PeopleExample() /* new GlobalSpec(), new Thomas() */, new A(), new B() };

	String help = ""; //"SQL schemas and instances in categorical normal form (CNF) can be treated as FQL instances directly.  To be in CNF, every table must have a primary key column called id.  This column will be treated as a meaningless ID.  Every column in a table must either be a string, an integer, or a foreign key to another table.  Inserted values must be quoted.  See the People example for details.";

	protected String kind() {
		return "CFG";
	}
	
	
	String translate(String in) {
		return program(in).toString();
	}

	public CfgToOpl() {
		final FQLTextPanel input = new FQLTextPanel(BorderFactory.createEtchedBorder(), kind() + " Input", "");
		final FQLTextPanel output = new FQLTextPanel(BorderFactory.createEtchedBorder(), "OPL Output", "");

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
					output.setText(translate(input.getText()).toString());
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
				//jta.setEditable(false);
				jta.setLineWrap(true);
				JScrollPane p = new JScrollPane(jta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				p.setPreferredSize(new Dimension(300,200));

				JOptionPane pane = new JOptionPane(p);
				 // Configure via set methods
				 JDialog dialog = pane.createDialog(null, "Help on CFG to OPL");
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
		JFrame f = new JFrame(kind() + " to OPL");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	
//	public static Object transSQLSchema(String in) {
	//	return in;
	//}
	
	
	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { "|" , "::=", "," };

	static String[] res = new String[] { };

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Object program(String s) {
		return toCfg( program.parse(s) );

	}

	static OplExp toCfg(Object o) {
		HashMap<String, List<List<String>>> ret = new HashMap<String, List<List<String>>>();
		
		List<Tuple3> l = (List<Tuple3>) o;
		for (Tuple3 p : l) {
			String x = (String) p.a;
			if (ret.containsKey(x)) {
				throw new RuntimeException("Duplicate production name: " + x);
			}
			ret.put(x, (List<List<String>>) p.c);
		}
		
		java.util.Map<String, Pair<List<String>, String>> symbols = new HashMap<>();
		int i = 0;
		for (String k : ret.keySet()) {
			List<List<String>> v = ret.get(k);
			for (List<String> u : v) {
				List<String> pre = new LinkedList<>();
				List<String> tys = new LinkedList<>();
				for (String z : u) {
					if (ret.keySet().contains(z)) {
						tys.add(z);
					} else {
						pre.add(z);
					}
				}
				String name0 = Util.sep(pre, "_");
				String xxx = symbols.keySet().contains(name0) ? "_" + (i++) : ""; 
				String name = "\"" + name0 +  xxx + "\"";
				symbols.put(name, new Pair<>(tys, k));
				i++;
			}
		}
		return new OplExp.OplSig(ret.keySet(), symbols, new LinkedList<>());
	}
	
	static Parser program() {
		Parser q = string().many().sepBy(term("|"));
		Parser p = Parsers.tuple(ident(), term("::="), q).sepBy(term(","));
		return p;
	}
	

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	
	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}
	

}

