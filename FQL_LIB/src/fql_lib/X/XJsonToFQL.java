package fql_lib.X;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
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

import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.examples.Example;
import fql_lib.gui.FQLTextPanel;

/**
 * 
 * @author ryan
 *
 */
public class XJsonToFQL {

	protected Example[] examples = { new EmpEx() };

	static class EmpEx extends Example {

		@Override
		public String getName() {
			return "Employees";
		}

		@Override
		public String getText() {
			return emp_str;
		}
		
	}
	
	String help = "Transles Idea Flow JSON into an FPQL schema";

	protected String kind() {
		return "JSON";
	}

	String translate(String in) {
		try {
			JsonReader rdr = Json.createReader(new StringReader(in));
			JsonObject obj = rdr.readObject();
			JsonObject graph = obj.getJsonObject("graph");
			JsonArray nodes = graph.getJsonArray("nodes");
			JsonArray edges = graph.getJsonArray("edges");
			Set<String> types = new HashSet<>();
			Set<String> entities = new HashSet<>();
			Set<Triple<String, String, String>> consts = new HashSet<>();
			Set<Triple<String, String, String>> fks = new HashSet<>();
			 for (JsonObject o : nodes.getValuesAs(JsonObject.class)) {
				 String id = o.getJsonString("id").toString();
				 String ty = o.getJsonString("type").toString();
				 if (ty.equals("\"type\"")) {
					 types.add(id);
				 } else {
					 entities.add(id);
				 }
			 }
			 for (JsonObject o : edges.getValuesAs(JsonObject.class)) {
				 String id = o.getJsonString("id").toString();
				 String src = o.getJsonString("source").toString();
				 String dst = o.getJsonString("target").toString();
				 if (dst.equals("\"_1\"")) {
					 continue;
				 }
				 if (types.contains(src)) {
					 consts.add(new Triple<>(id, src, dst));
				 } else {
					 fks.add(new Triple<>(id, src, dst));
				 }
			 }
			 
			 String ret = "";
			 for (String ty : types) {
				 if (ty.equals("\"_1\"")) {
					 continue;
				 }
				 ret += ty + " : type\n"; 
			 }
			 ret += "\n";
			 for (Triple<String, String, String> c : consts) {
				 if (c.third.equals("\"_1\"")) {
					 continue;
				 }
				 if (c.second.equals("\"_1\"")) {
					 ret += c.first + " : " + c.third + "\n";
				 } else {
					 ret += c.first + " : " +  c.second + " -> " + c.third + "\n";
				 }
			 }
			 ret += "\n";
			 ret += "S = schema {\n";
			 ret += "nodes\n";
			 ret += Util.sep(entities, ",\n");
			 
			 ret += ";\n";
			 ret += "edges\n";
			 ret += Util.sep(fks.stream().map(x -> x.first + " : " + x.second + " -> " + x.third).collect(Collectors.toList()), ",\n");
			 
			 ret += ";\n";
			 ret += "equations;\n";
			 ret += "}\n";
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public XJsonToFQL() {
		final FQLTextPanel input = new FQLTextPanel(BorderFactory.createEtchedBorder(), kind()
				+ " Input", "");
		final FQLTextPanel output = new FQLTextPanel(BorderFactory.createEtchedBorder(),
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
				JDialog dialog = pane.createDialog(null, "Help on JSON to FPQL");
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
	
	static String emp_str = "{\"graph\": { \"directed\":true,"
			+ "\n\"nodes\":["
			+ "\n{\"id\": \"dom\", \"type\":\"type\", \"label\":\"dom\"},"
			+ "\n{\"id\": \"Employee\", \"type\":\"entity\", \"label\":\"Employee\"},"
			+ "\n{\"id\": \"_1\", \"type\":\"type\", \"label\":\"_1\"},"
			+ "\n{\"id\": \"String\", \"type\":\"type\", \"label\":\"String\"},"
			+ "\n{\"id\": \"Department\", \"type\":\"entity\", \"label\":\"Department\"},"
			+ "\n{\"id\": \"Int\", \"type\":\"type\", \"label\":\"Int\"}], "
			+ "\n\"edges\":["
			+ "\n{\"id\": \"last\", \"directed\":\"true\", \"label\":\"last\", \"source\":\"Employee\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"Bob\", \"directed\":\"true\", \"label\":\"Bob\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"!_Employee\", \"directed\":\"true\", \"label\":\"!_Employee\", \"source\":\"Employee\", \"target\":\"_1\"},"
			+ "\n{\"id\": \"!_dom\", \"directed\":\"true\", \"label\":\"!_dom\", \"source\":\"dom\", \"target\":\"_1\"},"
			+ "\n{\"id\": \"worksIn\", \"directed\":\"true\", \"label\":\"worksIn\", \"source\":\"Employee\", \"target\":\"Department\"},"
			+ "\n{\"id\": \"first\", \"directed\":\"true\", \"label\":\"first\", \"source\":\"Employee\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"Al\", \"directed\":\"true\", \"label\":\"Al\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"Akin\", \"directed\":\"true\", \"label\":\"Akin\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"Cork\", \"directed\":\"true\", \"label\":\"Cork\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"!_Department\", \"directed\":\"true\", \"label\":\"!_Department\", \"source\":\"Department\", \"target\":\"_1\"},"
			+ "\n{\"id\": \"!__1\", \"directed\":\"true\", \"label\":\"!__1\", \"source\":\"_1\", \"target\":\"_1\"},"
			+ "\n{\"id\": \"!_Int\", \"directed\":\"true\", \"label\":\"!_Int\", \"source\":\"Int\", \"target\":\"_1\"},"
			+ "\n{\"id\": \"one\", \"directed\":\"true\", \"label\":\"one\", \"source\":\"_1\", \"target\":\"Int\"},"
			+ "\n{\"id\": \"Carl\", \"directed\":\"true\", \"label\":\"Carl\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"reverse\", \"directed\":\"true\", \"label\":\"reverse\", \"source\":\"String\", \"target\":\"String\"},"
			+ "\n{\"id\": \"manager\", \"directed\":\"true\", \"label\":\"manager\", \"source\":\"Employee\", \"target\":\"Employee\"},"
			+ "\n{\"id\": \"Bo\", \"directed\":\"true\", \"label\":\"Bo\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"two\", \"directed\":\"true\", \"label\":\"two\", \"source\":\"_1\", \"target\":\"Int\"},"
			+ "\n{\"id\": \"!_String\", \"directed\":\"true\", \"label\":\"!_String\", \"source\":\"String\", \"target\":\"_1\"},"
			+ "\n{\"id\": \"name\", \"directed\":\"true\", \"label\":\"name\", \"source\":\"Department\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"print\", \"directed\":\"true\", \"label\":\"print\", \"source\":\"Int\", \"target\":\"String\"},"
			+ "\n{\"id\": \"secretary\", \"directed\":\"true\", \"label\":\"secretary\", \"source\":\"Department\", \"target\":\"Employee\"},"
			+ "\n{\"id\": \"Cs\", \"directed\":\"true\", \"label\":\"Cs\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"Math\", \"directed\":\"true\", \"label\":\"Math\", \"source\":\"_1\", \"target\":\"dom\"},"
			+ "\n{\"id\": \"foo\", \"directed\":\"true\", \"label\":\"foo\", \"source\":\"_1\", \"target\":\"String\"}"
			+ "\n]}}"
			+ "\n";



}
