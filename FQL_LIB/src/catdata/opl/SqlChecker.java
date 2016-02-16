package catdata.opl;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.collections15.Transformer;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.ide.CodeTextPanel;
import catdata.ide.Example;
import catdata.ide.Util;
import catdata.opl.OplExp.OplInst;
import catdata.opl.OplExp.OplPres;
import catdata.opl.OplExp.OplSchema;
import catdata.opl.OplExp.OplSig;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

public class SqlChecker {

	static Example[] examples = { new EmpExample(), new CompoundExample() };

	static class EmpExample extends Example {

		@Override
		public String getName() {
			return "Employees";
		}

		@Override
		public String getText() {
			return "CREATE TABLE Employee("
					+ "\n id INT PRIMARY KEY,"
					+ "\n first VARCHAR(255),"
					+ "\n last VARCHAR(255),"
					+ "\n manager INT,"
					+ "\n worksIn INT"
					+ "\n);"
					+ "\n"
					+ "\nCREATE TABLE Department("
					+ "\n id INT PRIMARY KEY,"
					+ "\n name VARCHAR(255),"
					+ "\n secretary INT,"
					+ "\n);"
					+ "\n "
					+ "\nINSERT INTO Employee VALUES "
					+ "\n (101, 'Alan', 'Turing', 103, 10), "
					+ "\n (102, 'Camille', 'Jordan', 102, 2), "
					+ "\n (103, 'Andrey', 'Markov', 103, 10);"
					+ "\n"
					+ "\nINSERT INTO Department VALUES"
					+ "\n (10, 'Applied Math', 101),"
					+ "\n (2, 'Pure Math', 102);"
					+ "\n"
					+ "\nALTER TABLE Employee ADD CONSTRAINT e1"
					+ "\n FOREIGN KEY (manager) REFERENCES Employee (id);"
					+ "\n"
					+ "\nALTER TABLE Employee ADD CONSTRAINT e2 "
					+ "\n FOREIGN KEY (worksIn) REFERENCES Department (id);"
					+ "\n"
					+ "\nALTER TABLE Department ADD CONSTRAINT d1"
					+ "\n FOREIGN KEY (secretary) REFERENCES Employee (id);"
					+ "\n"
					+ "\n"
					+ "\nCHECK Employee . Employee,manager->id . Employee,manager->id"
					+ "\n = Employee . Employee,manager->id;"
					+ "\n"
					+ "\nCHECK Employee . Employee,manager->id . Department,worksIn->id"
					+ "\n = Employee . Department,worksIn->id;"
					+ "\n"
					+ "\nCHECK Department . Employee,secretary->id . Department,worksIn->id"
					+ "\n = Department;"
					+ "\n";

		}

	}
	
	static class CompoundExample extends Example {
		@Override
		public String getName() {
			return "Compound";
		}
		
		@Override
		public String getText() {
			return "CREATE TABLE CUSTOMER("
					+ "\nSID integer primary key,"
					+ "\nLast_Name varchar(255),"
					+ "\nFirst_Name varchar(255));"
					+ "\n"
					+ "\nCREATE TABLE ORDERS("
					+ "\nOrder_ID integer primary key,"
					+ "\nOrder_Date date,"
					+ "\nCustomer_SID integer REFERENCES CUSTOMER(SID),"
					+ "\nAmount double);"
					+ "\n"
					+ "\nCREATE TABLE INVOICE("
					+ "\nInvoice_ID integer,"
					+ "\nStore_ID integer,"
					+ "\nCUSTOMER_ID integer,"
					+ "\nFOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER (SID),"
					+ "\nPRIMARY KEY(Invoice_ID, Store_ID));"
					+ "\n"
					+ "\nCREATE TABLE PAYMENT("
					+ "\nPayment_ID integer,"
					+ "\nInvoice_ID integer,"
					+ "\nStore_ID integer,"
					+ "\nPayment_Date datetime,"
					+ "\nPayment_Amount float,"
					+ "\nPRIMARY KEY (Payment_ID),"
					+ "\nFOREIGN KEY (Invoice_ID, Store_ID) REFERENCES INVOICE (Invoice_ID, Store_ID));"
					+ "\n";
		}


	}

	String help = "";

	protected String kind() {
		return "SQL Checker";
	}

	final CodeTextPanel input = new CodeTextPanel(BorderFactory.createEtchedBorder(), "SQL Input",
			"");
	final CodeTextPanel output = new CodeTextPanel(BorderFactory.createEtchedBorder(), "Response",
			"");

	static int count = 0;
	public static String next() {
		return "v" + count++;
	}
	
	private static String pr(Pair<String, List<Pair<String, String>>> y) {
		List<String> l = y.second.stream().map(x -> x.first + "->" + x.second).collect(Collectors.toList());
		return y.first + "," + Util.sep(l, ",");
	}
	
	 Triple<String, Set<String>, String> path(String start, List<Pair<String, List<Pair<String,String>>>> path, DBInfo info) {
		String init = start;
		String v = next();
		String init_v = v;
		Set<String> from = new HashSet();
		Set<String> where = new HashSet();
		
		Set<String> ret = new HashSet<>();
		
		from.add(start + " AS " + v);
		
		if (!info.rawtypes.containsKey(start)) {
			throw new RuntimeException("Not a table: ");
		}
		
		for (Pair<String, List<Pair<String, String>>> edge : path) {
			String target = edge.first;
			if (!info.rawtypes.containsKey(target)) {
				throw new RuntimeException("Not a table: " + target);
			}
			if (!match(target, edge.second, info.fkeys.get(start))) {
				String exn = pr(edge) + " is a not declared as a foreign key from " + start + " to " + target;
				ret.add(exn);
				if (haltOnErrors.isSelected()) {
					throw new RuntimeException(exn);
				}
			}
			ret.addAll(typeCheck(start, target, edge, info));
			if (!targetIsPK(start, target, edge, info)) {
				String exn = pr(edge) + " does not target the primary key of " + target;
				ret.add(exn);
				if (haltOnErrors.isSelected()) {
					throw new RuntimeException(exn);
				}
			}
			String v2 = next();
			from.add(target + " AS " + v2);
			for (Pair<String, String> p : edge.second) {
				where.add(v + "." + p.first + " = " + v2 + "." + p.second);
			}	
			v = v2;
			start = target;
		}
		
		Set<String> select = new HashSet<>();
		for (String col : info.rawtypes.get(init).keySet()) {
			select.add(init_v + "." + col + " AS " + "I_" + col );
		}
		for (String col : info.rawtypes.get(start).keySet()) {
			select.add(v + "." + col  + " AS " + "O_" + col );
		}
		
		String str = "SELECT " + Util.sep(select, ", ") + "\nFROM " + Util.sep(from, ", ") 
			+ (where.isEmpty() ? "" : "\nWHERE " + Util.sep(where, " AND "));
		
		return new Triple<>(str, ret, start);
	}
	
	Set<String> typeCheck(String source, String target, Pair<String, List<Pair<String, String>>> edge, DBInfo info) {
		Set<String> ret = new HashSet<>();
		for (Pair<String, String> p : edge.second) {
			String src_t = info.rawtypes.get(source).get(p.first);
			if (src_t == null) {
				throw new RuntimeException("In " + edge + ", " + p.first + " is not a column in " + source);
			}
			String dst_t = info.rawtypes.get(target).get(p.second);
			if (dst_t == null) {
				throw new RuntimeException("In " + edge + ", " + p.second + " is not a column in " + target);
			}
			if (!src_t.equals(dst_t)) {
				ret.add("In " + pr(edge) + ", types do not agree for " + p.first + "->" + p.second + ", is " + src_t + "->" + dst_t);
			}
		}
		return ret;
	}
	private boolean targetIsPK(String source, String target, Pair<String, List<Pair<String, String>>> edge, DBInfo info) {
		Set<String> cand = new HashSet<>();
		for (Pair<String, String> p : edge.second) {
			cand.add(p.second);
		}
		if (!info.pkeys.get(target).equals(cand)) {
			return false;
		}
		return true;
	}

	static Pair<List<String>, List<Pair<String, String>>> conv(String target, List<Pair<String, String>> edge) {
		List<Pair<String, String>> l = new LinkedList<>();
		List<String> r = new LinkedList<>();
		for (Pair<String, String> p : edge) {
			l.add(new Pair<>(target, p.second));
			r.add(p.first);
		}
		
		Pair<List<String>, List<Pair<String, String>>> tofind = new Pair<>(r, l);
		return tofind;
	}
	
	private static boolean match(String target, List<Pair<String, String>> edge,
			List<Pair<List<String>, List<Pair<String, String>>>> fks) {

		return fks.contains(conv(target, edge));
	}

	String translate(String in) {
		try {
			output.setText("");
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection("jdbc:h2:mem:");

			List<Pair<String, Pair<Pair<String, List<Pair<String, List<Pair<String, String>>>>>, 
					               Pair<String, List<Pair<String, List<Pair<String, String>>>>>>>> tocheck = new LinkedList<>();
			String[] strings = in.split(";");
			for (String string0 : strings) {
				String string = string0.trim();
				if (string.equals(";")) {
					continue;
				}
				if (string.trim().startsWith("CHECK ")) {
					tocheck.add(new Pair<>(string.substring(6), eq(string.substring(6))));
					continue;
				}
				Statement stmt = conn.createStatement();
				stmt.execute(string);
			}
			List<Pair<String, JComponent>> frames = new LinkedList<>();

			DBInfo info = new DBInfo(conn.getMetaData());
			Triple<Graph, Set, OplInst<String, String, String, String>> gr = build(info, tocheck, conn);			
			frames.add(new Pair<>("schema", doSchemaView(Color.RED, gr.first, gr.second)));
			doChecks(conn, info, frames, tocheck);
			new DisplayThingy(frames);
			if (haltOnErrors.isSelected()) {
				return "S0 = " + gr.third.S.sig + "\nS = " + gr.third.S + "\nI0 = " + gr.third.P + "\nI = instance S I0 none";
			} else {
				return "OK";
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			return "H2 driver not found";
		} catch (SQLException ex) {
			ex.printStackTrace();
			return "JDBC error: " + ex.getMessage();
		}

	}
	
	private void doChecks(Connection conn, DBInfo info,
			List<Pair<String, JComponent>> frames,
			List<Pair<String, Pair<Pair<String, List<Pair<String, List<Pair<String, String>>>>>, Pair<String, List<Pair<String, List<Pair<String, String>>>>>>>> tocheck) 
	 throws SQLException {
		
		for (Pair<String, Pair<Pair<String, List<Pair<String, List<Pair<String, String>>>>>, Pair<String, List<Pair<String, List<Pair<String, String>>>>>>> eq : tocheck) {
			JTabbedPane ret = new JTabbedPane();

			Pair<String, List<Pair<String, List<Pair<String, String>>>>> lhs = eq.second.first;
			Pair<String, List<Pair<String, List<Pair<String, String>>>>> rhs = eq.second.second;
			
			if (!lhs.first.equals(rhs.first)) {
				throw new RuntimeException(eq.first + " starts at two different tables, " + lhs.first + " and " + rhs.first);
			}
			
			Triple<String, Set<String>, String> q1 = path(lhs.first, lhs.second, info);			
			Triple<String, Set<String>, String> q2 = path(rhs.first, rhs.second, info);
			
			if (!q1.third.equals(q2.third)) {
				throw new RuntimeException(eq.first + " ends on two different tables, " + q1.third + " and " + q2.third);
			}
			
			if (!q1.second.isEmpty() || !q2.second.isEmpty()) {
				String exns = "LHS warnings:\n\n" + Util.sep(q1.second, "\n") + "\n\nRHS warnings:\n\n" + Util.sep(q2.second, "\n");
				CodeTextPanel p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", exns);
				ret.add("Warnings", p);
			}
			
			CodeTextPanel p = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", q1.first + "\n\n = \n\n" + q2.first);
			ret.add(p, "Text");

			Statement stmt = conn.createStatement();
			stmt.execute(q1.first);
			ResultSet q1r = stmt.getResultSet();
			
			stmt = conn.createStatement();
			stmt.execute(q2.first);
			ResultSet q2r = stmt.getResultSet();
			
			Set<Map<String, String>> tuples1 = toTuples(q1r);
			Set<Map<String, String>> tuples2 = toTuples(q2r);
			
			boolean b = tuples1.equals(tuples2);
			if (b) {
				CodeTextPanel p2 = new CodeTextPanel(BorderFactory.createEtchedBorder(), "", "OK");
				ret.add(p2, "Result");
			} else {
				ret.add(showDiff(lhs.first, q1.third, tuples1,tuples2, info), "Result");				
			}
			
			frames.add(new Pair<>(eq.first, ret));			
		}
		
	}
	
	JComponent showDiff(String src, String dst, Set<Map<String, String>> lhs, Set<Map<String, String>> rhs, DBInfo info) {
		List<String> sCols = new LinkedList<>(info.rawtypes.get(src).keySet());
		List<String> tCols = new LinkedList<>(info.rawtypes.get(dst).keySet());
		
		List<JPanel> tbls = new LinkedList<>();
		for (Map<String, String> row : lhs) {
			List<String> lhs_out = new LinkedList<>();
			List<String> rhs_out = new LinkedList<>();
			
			Map<String, String> lhsM = row; //match(row, lhs, sCols);
			Map<String, String> rhsM = matchRow(row, rhs, sCols);

			if (lhsM.equals(rhsM)) {
				continue;
			}
			
			List<String> inRow = new LinkedList<>();
			for (String sCol : sCols) {
				inRow.add(row.get("I_" + sCol));
			}

			for (String tCol : tCols) {
				lhs_out.add(lhsM.get("O_" + tCol));
				rhs_out.add(rhsM.get("O_" + tCol));
			}
			
			JPanel inTable = Util.makeTable(BorderFactory.createEmptyBorder(), "Input " + src, new Object[][] { inRow.toArray() }, sCols.toArray());
			JPanel diffTable = Util.makeTable(BorderFactory.createEmptyBorder(), "Output " + dst, new Object[][] { lhs_out.toArray(), rhs_out.toArray() }, tCols.toArray());
			JPanel p = new JPanel(new GridLayout(2, 1));
			p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Mismatch"));
			p.add(inTable);
			p.add(diffTable);
			tbls.add(p);
		}
		
		JPanel ret = new JPanel(new GridLayout(tbls.size(), 1, 0, 6));
		for (JPanel p : tbls) {
			ret.add(p);
		}
		JComponent xxx = new JScrollPane(ret);
		xxx.setBorder(BorderFactory.createEmptyBorder());
		return xxx;
	}
	
	private Map<String, String> matchRow(Map<String, String> row, Set<Map<String, String>> rows,
			List<String> cols) {
		outer: for (Map<String, String> row0 : rows) {
			for (String col : cols) {
				if (!row0.get("I_" + col).equals(row.get("I_" + col))) {
					continue outer;
				}
			}
			return row0;
		}
		throw new RuntimeException("No partner for " + row + " in " + rows);
	}

	static String printNicely(Set<Map<String, String>> map) {
		List<String> l = map.stream().map(x -> printNicely(x)).collect(Collectors.toList());
		Collections.sort(l);
		return Util.sep(l, "\n");
	}
	static String printNicely(Map<String, String> map) {
		List<String> l = new LinkedList<>(map.keySet());
		Collections.sort(l);
		boolean first = true;
		String ret = "";
		for (String key : l) {
			if (!first) {
				ret += ", ";
			}
			ret += key + "=" + map.get(key);
			first = false;
		}
		return ret;
	}
	
	static Set<Map<String, String>> toTuples(ResultSet resultSet) throws SQLException {
		Set<Map<String, String>> rows = new HashSet<>();
		
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		
		while (resultSet.next()) {
			Map<String, String> row = new HashMap<>();
		    for (int i = 1; i <= columnsNumber; i++) {
		        String columnValue = resultSet.getString(i);
		        String columnName = rsmd.getColumnLabel(i);
		        row.put(columnName, columnValue);
		    }
			rows.add(row);
		}
		
		return rows;
	}

	static String print(ResultSet resultSet) throws SQLException {
		String ret = "";
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
		    for (int i = 1; i <= columnsNumber; i++) {
		        if (i > 1) {
		        	ret += ", ";
		        }
		        String columnValue = resultSet.getString(i);
		        ret += (columnValue + " " + rsmd.getColumnName(i));
		    }
		    ret += "\n";
		}
		return ret;
	}
	
	public SqlChecker() {

		JButton transButton = new JButton("Run");
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
				JDialog dialog = pane.createDialog(null, "Help on SQL Checker");
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

		JPanel tp = new JPanel(new GridLayout(1, 5));

		tp.add(transButton);
		tp.add(helpButton);
		tp.add(haltOnErrors);
		tp.add(new JLabel("Load Example", JLabel.RIGHT));
		tp.add(box);

		p.add(jsp, BorderLayout.CENTER);
		p.add(tp, BorderLayout.NORTH);
		JFrame f = new JFrame("SQL Checker, SQL to OPL");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	JCheckBox haltOnErrors = new JCheckBox("Require FK Decls", true);
	
	public JComponent doSchemaView(Color clr, Graph<String, Chc<Pair<String,String>, Pair<String, Pair>>> sgv, Set entities) {
		if (sgv.getVertexCount() == 0) {
			return new JPanel();
		}
		Layout layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer vv = new VisualizationViewer<>(layout);
		Transformer vertexPaint = x -> {
			if (entities.contains(x)) {
				return clr;
			} else {
				return null;
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer ttt = arg0 -> arg0.toString();
		Transformer<Chc<Pair<String,String>, Pair<String, Pair>>, String> et = arg0 -> {
			if (arg0.left) {
				return arg0.l.second;
			} else {
				return arg0.r.second.first.toString();
			}
		};
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(et);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	static class DBInfo {
		
		public DBInfo(DatabaseMetaData meta) throws SQLException {
			ResultSet result = meta.getTables(null, null, null, new String[] { "TABLE" });
			while (result.next()) {
				String tableName = result.getString(3);
				
				//name, local cols, foreign cols
				List<Pair<List<String>, List<Pair<String, String>>>> fks0 = new LinkedList<>(); 
				ResultSet fks = meta.getImportedKeys(null, null, tableName);
				int lastSeen = 0;
				List<String> l = new LinkedList<>();
				List<Pair<String, String>> r = new LinkedList<>();
				String foreignTable = null;
				String foreignColumn = null; 
				//String localTable = fks.getString(7);
				String localColumn = null;
		//		String fkname = null;
				while (fks.next()) {
					
				//	fkname = fks.getString(12); //13 is pkey name, should be irrelevent
					foreignTable = fks.getString(3);
					foreignColumn = fks.getString(4); 
					//String localTable = fks.getString(7);
					localColumn = fks.getString(8);
					String seq = fks.getString(9);
					if (Integer.parseInt(seq) <= lastSeen) {
						fks0.add(new Pair<>(l, r));
						l = new LinkedList<>();
						r = new LinkedList<>();
					}
					lastSeen = Integer.parseInt(seq);
					
					l.add(localColumn);
					r.add(new Pair<>(foreignTable, foreignColumn));
				}
				if (foreignTable != null || foreignColumn != null || localColumn != null) {
					fks0.add(new Pair<>(l, r));
				}
				fkeys.put(tableName, fks0);
				
				ResultSet columns = meta.getColumns(null, null, tableName, null);
				Map<String, String> colMap = new HashMap<>();
				while (columns.next()) {
					String columnName = columns.getString(4);
					String columnType = columns.getString(5); 
					String resolvedType = resolveType(columnType);
					colMap.put(columnName, resolvedType);
					types.add(resolvedType);
				}
				rawtypes.put(tableName, colMap);
				
				//only 1 primary ket per table for some reason
				Set<String> pks0 = new HashSet<>();
				ResultSet pks = meta.getPrimaryKeys(null, null, tableName);
				while (pks.next()) {
					String colName = pks.getString(4);
					pks0.add(colName);
				}
				if (pks0.isEmpty()) {
					pks0 = colMap.keySet();
				}
				pkeys.put(tableName, pks0);
				
			}
		}
		
		public Set<String> types = new HashSet<>();
		public Map<String, Map<String, String>> rawtypes = new HashMap<>();
		public Map<String, Set<String>> pkeys = new HashMap<>();
		public Map<String, List<Pair<List<String>, List<Pair<String, String>>>>> fkeys = new HashMap<>();
		
		@Override
		public String toString() {
			String ret = "";
			for (String table : rawtypes.keySet()) {
				ret += "Table " + table;
				ret += "\nColumns (" + Util.sep(rawtypes.get(table), " ", ", ") + ")";
				if (pkeys.containsKey(table)) {
					ret += "\nPrimary key (" + Util.sep(pkeys.get(table), ", ") + ")";
				}
				for (Pair<List<String>, List<Pair<String, String>>> t : fkeys.get(table)) {
					List<String> str = t.second.stream().map(x -> { return x.first + "." + x.second; }).collect(Collectors.toList());	
					ret += "\nForeign key (" + Util.sep(t.first, ", ") + ") references (" + Util.sep(str, ", ") + ")"; 
				}
				ret += "\n\n";
			}
			return ret;
		}
		
	}
	
	static String fkToSch(String src, String dst, Pair<List<String>, List<Pair<String, String>>> fk) {
		String str = src + "->" + dst + " by ";
		String str2 = Util.sep(fk.first, ",");
		String str3 = Util.sep(fk.second.stream().map(x -> x.second).collect(Collectors.toList()),",");
		return "\"" + str + str2 + "->" + str3 +"\"";
	}
	
	public  Triple<Graph, Set, OplInst<String,String, String, String>> build(DBInfo info, List<Pair<String, Pair<Pair<String, List<Pair<String, List<Pair<String, String>>>>>, 
            Pair<String, List<Pair<String, List<Pair<String, String>>>>>>>> tocheck, Connection conn) throws SQLException {
		Graph g2 = new DirectedSparseMultigraph<>();

		int i = 0;
		Set<String> sorts = new HashSet<>();
		Map<String, Pair<List<String>, String>> symbols = new HashMap<>();
		for (String type : info.types) {
			g2.addVertex(type);
			sorts.add(type);
		}
		for (String table : info.pkeys.keySet()) {
			g2.addVertex(table);
			sorts.add(table);
			for (String col : info.rawtypes.get(table).keySet()) {
				g2.addEdge(Chc.inLeft(new Pair<>(table, col)), table, info.rawtypes.get(table).get(col));
				symbols.put("\"" + table + "." + col + "\"", new Pair<>(Util.singList(table), info.rawtypes.get(table).get(col)));
			}
			for (Pair<List<String>, List<Pair<String, String>>> fk : info.fkeys.get(table)) {
				String target = fk.second.get(0).first;
				g2.addEdge(Chc.inRight(new Pair<>(table, fk)), table, target);
				symbols.put(fkToSch(table, target, fk), new Pair<>(Util.singList(table), target));
			}
		}
		
		if (!haltOnErrors.isSelected()) {
			return new Triple<>(g2, info.rawtypes.keySet(), null);
		}
		
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> equations = buildEqs(info, tocheck);

		Map<String, String> gens = new HashMap<>();
		List<Pair<OplTerm<Chc<String, String>, String>, OplTerm<Chc<String, String>, String>>> eqs = new LinkedList<>();

		populate(conn, info, symbols, gens, eqs);

		OplSig<String, String, String> sig = new OplSig<>(OplParser.VIt.vit, new HashMap<>(), sorts, symbols, equations);
		OplSchema<String, String, String> sch = new OplSchema<>("S0", info.rawtypes.keySet());
		sch.validate(sig);
		
		OplInst<String, String, String, String> I = new OplInst<String, String, String, String>("S", "I0", "none");
		OplPres<String, String, String, String> P = new OplPres<String, String, String, String>(new HashMap<>(), "S0", sig, gens, eqs);
		System.out.println(sig);
		System.out.println(P);
		P.toSig();
		I.validate(sch, P, null);
		
		return new Triple<>(g2, info.rawtypes.keySet(), I);
	}

	private void populate(Connection conn,
			DBInfo info,
			Map<String, Pair<List<String>, String>> symbols,
			Map<String, String> gens,
			List<Pair<OplTerm<Chc<String, String>, String>, OplTerm<Chc<String, String>, String>>> eqs) throws SQLException {
		
		Map<String, Map<Map<String, String>, String>> iso1 = new HashMap<>();
		Map<String, Map<String, Map<String, String>>> iso2 = new HashMap<>();
		
		for (String table : info.rawtypes.keySet()) {
			Triple<String, Set<String>, String> Q = path(table, new LinkedList<>(), info);
			Statement stmt = conn.createStatement();
			stmt.execute(Q.first);
			ResultSet r = stmt.getResultSet();
			Set<Map<String, String>> tuples = toTuples(r);
			
			Map<Map<String, String>, String> i1 = new HashMap<>();
			Map<String, Map<String, String>> i2 = new HashMap<>();
			for (Map<String, String> tuple0 : tuples) {
				Map<String, String> tuple = projTuple("I_", tuple0, info.rawtypes.get(table).keySet());
				String i = next();
				i1.put(tuple, i);
				i2.put(i, tuple);
				gens.put(i, table);
				for (String col : info.rawtypes.get(table).keySet()) {
					String ty = info.rawtypes.get(table).get(col);
					String val = tuple.get(col);
					if (val == null) {
						continue;
					}
					symbols.put("\" " + val + "\"", new Pair<>(new LinkedList<>(), ty));
					OplTerm<Chc<String, String>, String> rhs = new OplTerm<>(Chc.inLeft("\" " + val + "\""), new LinkedList<>());
					String att = "\"" + table + "." + col + "\"";
					OplTerm<Chc<String, String>, String> lhs = new OplTerm<Chc<String, String>, String>(Chc.inLeft(att), Util.singList(new OplTerm<>(Chc.inRight(i), new LinkedList<>())));
					eqs.add(new Pair<>(lhs, rhs));
				}
			}
			iso1.put(table, i1);
			iso2.put(table, i2);
		}
		for (String table : info.rawtypes.keySet()) {
			for (Pair<List<String>, List<Pair<String, String>>> fk : info.fkeys.get(table)) {
				String target = fk.second.get(0).first;
				Pair<String,List<Pair<String,String>>> fk0 = antiConv(fk);
				Triple<String, Set<String>, String> Q = path(table, Util.singList(fk0), info);
				Statement stmt = conn.createStatement();
				stmt.execute(Q.first);
				ResultSet r = stmt.getResultSet();
				Set<Map<String, String>> tuples = toTuples(r);
				
				for (Map<String, String> tuple0 : tuples) {
					Map<String, String> in = projTuple("I_", tuple0, info.rawtypes.get(table).keySet());
					Map<String, String> out = projTuple("O_", tuple0, info.rawtypes.get(target).keySet());
			
					String tgen = iso1.get(target).get(out);
					String sgen = iso1.get(table).get(in);
					String edge = fkToSch(table, target, fk);
					OplTerm<Chc<String, String>, String> rhs = new OplTerm<>(Chc.inRight(tgen), new LinkedList<>());
					OplTerm<Chc<String, String>, String> lhs = new OplTerm<Chc<String, String>, String>(Chc.inLeft(edge), Util.singList(new OplTerm<>(Chc.inRight(sgen), new LinkedList<>())));
					eqs.add(new Pair<>(lhs, rhs));
				}
			}
		}
	}

	private Map<String, String> projTuple(String pre, Map<String, String> tuple,
			Set<String> cols) {
		Map<String, String> ret = new HashMap<>();
		for (String col : cols) {
			ret.put(col, tuple.get(pre + col));
		}
		return ret;
	}

	private Pair<String, List<Pair<String, String>>> antiConv(
			Pair<List<String>, List<Pair<String, String>>> fk) {
		String target = fk.second.get(0).first;
		List<Pair<String, String>> l = new LinkedList<>();
		for (int i = 0; i < fk.first.size(); i++) {
			l.add(new Pair<>(fk.first.get(i), fk.second.get(i).second));
		}
		return new Pair<>(target, l);
	}

	private static List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> buildEqs(
			DBInfo info,
			List<Pair<String, Pair<Pair<String, List<Pair<String, List<Pair<String, String>>>>>, Pair<String, List<Pair<String, List<Pair<String, String>>>>>>>> eqs) {
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> ret = new LinkedList<>();
		
		for (Pair<String, Pair<Pair<String, List<Pair<String, List<Pair<String, String>>>>>, Pair<String, List<Pair<String, List<Pair<String, String>>>>>>> eq : eqs) {
			Pair<String, List<Pair<String, List<Pair<String, String>>>>> lhs = eq.second.first;
			Pair<String, List<Pair<String, List<Pair<String, String>>>>> rhs = eq.second.second;
			
			OplTerm<String, String> head = new OplTerm<>("x");
			OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>("x", lhs.first)));
			OplTerm<String,String> lhs0 = oplPath(lhs.first, head, lhs.second);
			OplTerm<String,String> rhs0 = oplPath(lhs.first, head, rhs.second);
			ret.add(new Triple<>(ctx, lhs0, rhs0));
		}
		
		return ret;
	}

	private static OplTerm<String, String> oplPath(String src, 
			OplTerm<String, String> head, 
			List<Pair<String, List<Pair<String, String>>>> edges) {

		for (Pair<String, List<Pair<String, String>>> edge : edges) {
			Pair<List<String>, List<Pair<String, String>>> t = conv(edge.first, edge.second);
			String e = fkToSch(src, edge.first, t);
			head = new OplTerm<>(e, Util.singList(head));
			src = edge.first;
		}
		
		return head;
	}

	public static String resolveType(String t) {
		Class<Types> c = Types.class;
		try {
			Field[] fields = c.getFields();
			for (Field field : fields) {
				Object o = field.get(null);
				if (t.toString().equals(o.toString())) {
					return field.getName();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		throw new RuntimeException("Couldn't find type " + t);
	}
	
	public static class DisplayThingy {

		public DisplayThingy(List<Pair<String, JComponent>> frames) {
			this.frames = frames;
			display("SQL Checker Result");
		}

		JFrame frame = null;
		String name;
		List<Pair<String, JComponent>> frames = new LinkedList<>();

		final CardLayout cl = new CardLayout();
		final JPanel x = new JPanel(cl);
		final JList<String> yyy = new JList<>();
		final Map<String, String> indices = new HashMap<>();

		public void display(String s) {
			frame = new JFrame();
			this.name = s;

			final Vector<String> ooo = new Vector<>();
			int index = 0;
			for (Pair<String, JComponent> p : frames) {
				x.add(p.second, p.first);
				ooo.add(p.first);
				indices.put(Integer.toString(index++), p.first);
			}

			yyy.setListData(ooo);
			JPanel temp1 = new JPanel(new GridLayout(1, 1));
			temp1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
					"Select:"));
			JScrollPane yyy1 = new JScrollPane(yyy);
			temp1.add(yyy1);
			yyy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			yyy.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					int i = yyy.getSelectedIndex();
					if (i == -1) {
						cl.show(x, "schema");
					} else {
						cl.show(x, ooo.get(i).toString());
					}
				}

			});

			JPanel north = new JPanel(new GridLayout(1, 1));
			JSplitPane px = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			px.setDividerLocation(200);
			px.setDividerSize(4);
			frame = new JFrame(/* "Viewer for " + */s);

			JSplitPane temp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			temp2.setResizeWeight(1);
			temp2.setDividerSize(0);
			temp2.setBorder(BorderFactory.createEmptyBorder());
			temp2.add(temp1);
			temp2.add(north);

			px.add(temp2);

			px.add(x);

			frame.setContentPane(px);
			frame.setSize(900, 600);

			ActionListener escListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
				}
			};

			frame.getRootPane().registerKeyboardAction(escListener,
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
			KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
			KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
			frame.getRootPane().registerKeyboardAction(escListener, ctrlW,
					JComponent.WHEN_IN_FOCUSED_WINDOW);
			frame.getRootPane().registerKeyboardAction(escListener, commandW,
					JComponent.WHEN_IN_FOCUSED_WINDOW);

			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

		}
	}

	////////////////////////////
	
	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|" };


	static String[] res = new String[] {  };

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

	static Parser<?> path() {
		Parser<?> p = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> edge = Parsers.tuple(term("."), ident(), term(","), p.sepBy1(term(",")));
		return Parsers.tuple(ident(), edge.many());
	}
	
	static Parser<?> program() {
		return Parsers.tuple(path(), term("="), path());
	}
	
	static Pair<String, List<Pair<String,String>>> toEdge(Object a) {
			Tuple4 t = (Tuple4) a;
			String n = (String) t.b;
			List z = (List) t.d;
			List<Pair<String,String>> y = new LinkedList<>();
			for (Object q : z) {
				Tuple3 q2 = (Tuple3) q;
				Pair<String, String> pair = new Pair<>(((String)q2.a).toUpperCase(), ((String)q2.c).toUpperCase());
				y.add(pair);
			}
			Pair<String, List<Pair<String,String>>> u = new Pair<>(n.toUpperCase(), y);
			return u;
	}
	
	static Pair<String, List<Pair<String, List<Pair<String,String>>>>> toPath(Object ox) {
		org.codehaus.jparsec.functors.Pair  o = (org.codehaus.jparsec.functors.Pair ) ox;
		String start = (String) o.a;
		List l = (List) o.b;
		List<Pair<String, List<Pair<String,String>>>> x = new LinkedList<>();
		for (Object a : l) {
			x.add(toEdge(a));
		}
		return new Pair<>(start.toUpperCase(), x);
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Pair<Pair<String, List<Pair<String, List<Pair<String,String>>>>>,
						     Pair<String, List<Pair<String, List<Pair<String,String>>>>>> eq(String s) {
		Tuple3 decl = (Tuple3) program().from(TOKENIZER, IGNORED).parse(s);

		List<Pair<Pair<String, List<Pair<String, List<Pair<String,String>>>>>,
	     Pair<String, List<Pair<String, List<Pair<String,String>>>>>>> ret = new LinkedList<>();
		
		return new Pair<>(toPath(decl.a), toPath(decl.c));
		
	}

}
