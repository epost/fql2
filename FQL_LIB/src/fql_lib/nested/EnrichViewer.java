package fql_lib.nested;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.JTextField;

import org.codehaus.jparsec.error.ParserException;

import fql_lib.X.XCtx;
import fql_lib.X.XDriver;
import fql_lib.X.XEnvironment;
import fql_lib.X.XExp;
import fql_lib.X.XExp.XSchema;
import fql_lib.X.XParser;
import fql_lib.X.XProgram;
import fql_lib.decl.LineException;
import fql_lib.examples.Example;
import fql_lib.gui.FQLTextPanel;

public class EnrichViewer {

	abstract class Example2 extends Example {
		public abstract String name();		
	}
	
	class NistExample extends Example2 {

		@Override
		public String name() {
			return "material";
		}

		@Override
		public String getName() {
			return "NIST";
		}

		@Override
		public String getText() {
			return s;
		}
		
		String s = "adom : type"
				+ "\n"
				+ "\nisa_schema = schema {"
				+ "\n	nodes A, B;"
				+ "\n	edges l : A -> B, r : A -> B, name : B -> adom;"
				+ "\n	equations;"
				+ "\n}"
				+ "\n"
				+ "\nS = schema {"
				+ "\n nodes"
				+ "\n  unitcode,"
				+ "\n  productorservicecategory,"
				+ "\n  equipmenttype,"
				+ "\n  industry,"
				+ "\n  material,"
				+ "\n  moldtypes,"
				+ "\n  process,"
				+ "\n  supplier,"
				+ "\n  capability,"
				+ "\n  capabilitycategories,"
				+ "\n  capabilityequipment,"
				+ "\n  capabilityindustry,"
				+ "\n  capabilitymaterials,"
				+ "\n  capabilityprocesses,"
				+ "\n  capabilitytypesofmolds,"
				+ "\n  suppliercapabilities;"
				+ "\n edges"
				+ "\n  productorservicecategory_Parent_id: productorservicecategory -> productorservicecategory,"
				+ "\n  capability_Parent_id: capability -> capability,"
				+ "\n  capability_Production_Volume_Min_Unit: capability -> unitcode,"
				+ "\n  capability_Production_Volume_Max_Unit: capability -> unitcode,"
				+ "\n  capability_Max_Length_Unit: capability -> unitcode,"
				+ "\n  capability_Tolerance_Unit: capability -> unitcode,"
				+ "\n  capabilitycategories_Capability_id: capabilitycategories -> capability,"
				+ "\n  capabilitycategories_ProductOrServiceCategory_id: capabilitycategories -> productorservicecategory,"
				+ "\n  capabilityequipment_Capability_id: capabilityequipment -> capability,"
				+ "\n  capabilityequipment_EquipmentType_id: capabilityequipment -> equipmenttype,"
				+ "\n  capabilityindustry_Capability_id: capabilityindustry -> capability,"
				+ "\n  capabilityindustry_Industry_id: capabilityindustry -> industry,"
				+ "\n  capabilitymaterials_Capability_id: capabilitymaterials -> capability,"
				+ "\n  capabilitymaterials_Material_id: capabilitymaterials -> material,"
				+ "\n  capabilityprocesses_Capability_id: capabilityprocesses -> capability,"
				+ "\n  capabilityprocesses_Process_id: capabilityprocesses -> process,"
				+ "\n  capabilitytypesofmolds_Capability_id: capabilitytypesofmolds -> capability,"
				+ "\n  capabilitytypesofmolds_MoldTypes_id: capabilitytypesofmolds -> moldtypes,"
				+ "\n  suppliercapabilities_Supplier_id: suppliercapabilities -> supplier,"
				+ "\n  suppliercapabilities_Capability_id: suppliercapabilities -> capability,"
				+ "\n  unitcode_Code: unitcode -> adom,"
				+ "\n  unitcode_Description: unitcode -> adom,"
				+ "\n  productorservicecategory_Category_Name: productorservicecategory -> adom,"
				+ "\n  productorservicecategory_isConcrete: productorservicecategory -> adom,"
				+ "\n  equipmenttype_EquipmentType_Name: equipmenttype -> adom,"
				+ "\n  industry_Industry_Name: industry -> adom,"
				+ "\n  material_Material_Name: material -> adom,"
				+ "\n  moldtypes_MoldTypes_Name: moldtypes -> adom,"
				+ "\n  process_Process_Name: process -> adom,"
				+ "\n  supplier_Source: supplier -> adom,"
				+ "\n  supplier_Note: supplier -> adom,"
				+ "\n  capability_Capability_Name: capability -> adom,"
				+ "\n  capability_Production_Volume_Min: capability -> adom,"
				+ "\n  capability_Production_Volume_Max: capability -> adom,"
				+ "\n  capability_Max_Length: capability -> adom,"
				+ "\n  capability_Tolerance: capability -> adom;"
				+ "\n equations"
				+ "\n  productorservicecategory.productorservicecategory_Parent_id.productorservicecategory_Parent_id.productorservicecategory_Parent_id = productorservicecategory.productorservicecategory_Parent_id.productorservicecategory_Parent_id.productorservicecategory_Parent_id.productorservicecategory_Parent_id,"
				+ "\n  capability.capability_Parent_id.capability_Parent_id.capability_Parent_id = capability.capability_Parent_id.capability_Parent_id.capability_Parent_id.capability_Parent_id;"
				+ "\n}"
				+ "\n";


		
	}
	
	protected Example2[] examples = { new NistExample() };

	String help = "Is_a schema first, then actual schema."; // "SQL schemas and instances in categorical normal form (CNF) can be treated as FQL instances directly.  To be in CNF, every table must have a primary key column called id.  This column will be treated as a meaningless ID.  Every column in a table must either be a string, an integer, or a foreign key to another table.  Inserted values must be quoted.  See the People example for details.";

	protected String kind() {
		return "Enrich";
	}

	
	final FQLTextPanel topArea = new FQLTextPanel(BorderFactory.createEtchedBorder(),
			"Input FQL", "");

	public JTextField name = new JTextField("");

	public EnrichViewer() {
		final FQLTextPanel output = new FQLTextPanel(BorderFactory.createEtchedBorder(),
				"Output FQL", "");

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
				topArea.setText(((Example) box.getSelectedItem()).getText());
				name.setText(((Example2) box.getSelectedItem()).name());
			}
		});

		transButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String p = translate(topArea.getText());
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
		jsp.add(topArea);
		jsp.add(output);

		// JPanel bp = new JPanel(new GridLayout(1, 5));
		JPanel tp = new JPanel(new GridLayout(1, 6));

		// bp.add(field);

		tp.add(transButton);
		tp.add(helpButton);
		// tp.add(jdbcButton);
		// tp.add(helpButton);
//		tp.add(new JLabel());
		tp.add(new JLabel("Name:", JLabel.RIGHT));
		tp.add(name);
		
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
		JFrame f = new JFrame("Enrich");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	private String translate(String program) {
		XProgram init;
		try {
			init = XParser.program(program);
	} catch (ParserException e) {
		int col = e.getLocation().column;
		int line = e.getLocation().line;
		topArea.requestFocusInWindow();
		topArea.area.setCaretPosition(topArea.area.getDocument()
				.getDefaultRootElement().getElement(line - 1)
				.getStartOffset()
				+ (col - 1));
		String s = e.getMessage();
		String t = s.substring(s.indexOf(" "));
		t.split("\\s+");
		e.printStackTrace();
		return "Syntax error: " + e.getLocalizedMessage();
	} catch (Throwable e) {
		e.printStackTrace();
		return "Error: " + e.getLocalizedMessage();
	}
		if (init == null) {
			return "";
		}

		String isaX = null, matX = null;
		XExp.XSchema isa = null, mat = null;
		for (String line : init.order) {
			XExp exp = init.exps.get(line);
			if (exp instanceof XExp.XSchema) {
				if (isaX == null) {
					isaX = line;
					isa = (XSchema) exp;
					continue;
				}
				if (matX == null) {
					matX = line;
					mat = (XSchema) exp;
					continue;
				}
				throw new RuntimeException("More than two schemas");
			}
		}
		if (isaX == null || matX == null) {
			throw new RuntimeException("Fewer than two schemas");
		}

		XEnvironment env; 
		try {
			env = XDriver.makeEnv(program, init);
		} catch (LineException e) {
			String toDisplay = "Error in " + e.kind + " " + e.decl + ": "
					+ e.getLocalizedMessage();
			e.printStackTrace();
			topArea.requestFocusInWindow();
			Integer theLine = init.getLine(e.decl);
			topArea.area.setCaretPosition(theLine);
			return toDisplay;
		} catch (Throwable re) {
			return "Error: " + re.getLocalizedMessage();
		}
		
		XCtx<String> isa0 = (XCtx<String>) env.objs.get(isaX);
		XCtx<String> mat0 = (XCtx<String>) env.objs.get(matX);
		
		
		
		return env.toString();
	}
	
	/* @SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Signature<String, String> program(String s) {
		//Object o = program.parse(s);
		//return toProg(o);
		// return to(o);
		return null;
	} */

	
}
