package fql_lib.nested;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import catdata.algs.Pair;
import catdata.algs.kb.KB_Thue;
import fql_lib.fqlpp.CatExp;
import fql_lib.fqlpp.PPParser;
import fql_lib.fqlpp.cat.Signature;
import fql_lib.ide.CodeTextPanel;
import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class KBViewer {

	protected Example[] examples = { new Cat() };

	String help = ""; // "SQL schemas and instances in categorical normal form (CNF) can be treated as FQL instances directly.  To be in CNF, every table must have a primary key column called id.  This column will be treated as a meaningless ID.  Every column in a table must either be a string, an integer, or a foreign key to another table.  Inserted values must be quoted.  See the People example for details.";

	protected String kind() {
		return "Knuth Bendix";
	}

	static class Cat extends Example {
		
		@Override
		public Language lang() {
			throw new RuntimeException();
		}

		@Override
		public String getName() {
			return "Category";
		}

		@Override
		public String getText() {
			return catstr;
		}
	} 


	
	public KBViewer() {
		final CodeTextPanel input = new CodeTextPanel(BorderFactory.createEtchedBorder(),
				"Input Category", "");
		final CodeTextPanel output = new CodeTextPanel(BorderFactory.createEtchedBorder(),
				"Output Re-writes", "");

		// JButton jdbcButton = new JButton("Load using JDBC");
		// JButton runButton = new JButton("Run " + kind());
		JButton transButton = new JButton("Complete");
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
					String p = translate(input.getText());
					output.setText(p);
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
				JDialog dialog = pane.createDialog(null, "Help on Knuth-Bendix Completion");
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
		JFrame f = new JFrame("Knuth-Bendix Completion");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	private String translate(String s) {
		Object o = PPParser.catConst().from(PPParser.TOKENIZER, PPParser.IGNORED).parse(s);
		CatExp.Const c = PPParser.toCatConst(o);
		Signature<String, String> sig = new Signature<>(c.nodes, c.arrows, c.eqs);
		
		Set<Pair<List<String>, List<String>>> rules = new HashSet<>();
		for (Signature<String,String>.Eq eq : sig.eqs) {
			rules.add(new Pair<>(eq.lhs.path.stream().map(x -> x.name).collect(Collectors.toList()), 
					             eq.rhs.path.stream().map(x -> x.name).collect(Collectors.toList())));
		}
		KB_Thue<String> kb = new KB_Thue<>(rules, 16);
		kb.complete();
		return kb.toString();
	}
	
	/* @SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Signature<String, String> program(String s) {
		//Object o = program.parse(s);
		//return toProg(o);
		// return to(o);
		return null;
	} */

	static String catstr = "{"
+ "\n	objects "
+ "\n		Ob, "
+ "\n		Hom, "
+ "\n		Comp;"
+ "\n	arrows"
+ "\n		Dom:Hom->Ob,"
+ "\n		Cod:Hom->Ob,"
+ "\n		Id:Ob->Hom,"
+ "\n		LeftId:Hom->Comp, //f:a-->b  mapped to id_a ; f"
+ "\n		RightId:Hom->Comp, //f:a-->b  mapped to f ; id_b"
+ "\n		First:Comp->Hom,"
+ "\n		Second:Comp->Hom,"
+ "\n		Compose:Comp->Hom;"
+ "\n	equations"
+ "\n		Ob.Id.Dom=Ob,"
+ "\n		Ob.Id.Cod=Ob,"
+ "\n		Hom.LeftId.Second=Hom,"
+ "\n		Hom.RightId.First=Hom,"
+ "\n		Hom.LeftId.Compose=Hom,"
+ "\n		Hom.RightId.Compose=Hom,"
+ "\n		Hom.LeftId.First=Hom.Dom.Id,"
+ "\n		Hom.RightId.Second=Hom.Cod.Id,"
+ "\n		Comp.First.Cod=Comp.Second.Dom,"
+ "\n		Comp.First.Dom=Comp.Compose.Dom,"
+ "\n		Comp.Second.Cod=Comp.Compose.Cod;"
+ "\n}"
+ "\n";

}
