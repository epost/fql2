package catdata.aql.gui;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import catdata.LineException;
import catdata.Pair;
import catdata.Program;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Semantics;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.AqlTyping;
import catdata.aql.exp.Exp;
import catdata.aql.exp.PragmaExp.PragmaExpCheck;
import catdata.aql.exp.PragmaExp.PragmaExpConsistent;
import catdata.ide.CodeTextPanel;
import catdata.ide.Disp;
import catdata.ide.GuiUtil;

//TODO aql suppress instance equations - do not compute/display if not required - maybe make instance an interface
public final class AqlDisplay implements Disp {

	private final Throwable exn;

	@Override
	public Throwable exn() {
		return exn;
	}

	@Override
	public void close() {
	}

	public static String doLookup(boolean prefix, String c, Kind k, AqlTyping typing) {
		String s = prefix ? k + " " + c : c ;
		if (!typing.defs.keySet().contains(c)) {
			return s;
		}
		switch (k) {
		case INSTANCE:
			return s + " : " + typing.defs.insts.get(c);
		case MAPPING:
			return s + " : " + typing.defs.maps.get(c).first + " -> "
					+ typing.defs.maps.get(c).second;
		case PRAGMA:
			return s;
		case QUERY:
			return s + " : " + typing.defs.qs.get(c).first + " -> "
					+ typing.defs.qs.get(c).second;
		case SCHEMA:
			return s;
		case TRANSFORM:
			return s + " : " + typing.defs.trans.get(c).first + " -> "
					+ typing.defs.trans.get(c).second;
		case TYPESIDE:
			return s;
		case GRAPH:
			return s;
		case COMMENT:
			return s;
		case SCHEMA_COLIMIT:
			return s;
		case CONSTRAINTS:
			return s + " : " + typing.defs.eds.get(c);
		default:
			throw new RuntimeException("Anomaly: please report");
		}

	}

	private static int getMaxSize(Exp<?> exp, AqlEnv env) {
		switch (exp.kind()) {
		case INSTANCE:
		case TRANSFORM:
			return (Integer) exp.getOrDefault(env, AqlOption.gui_max_table_size);

		case PRAGMA:
		case CONSTRAINTS:
			return (Integer) exp.getOrDefault(env, AqlOption.gui_max_string_size);

		case MAPPING:
		case QUERY:
		case SCHEMA:
		case SCHEMA_COLIMIT:
		case GRAPH:
		case TYPESIDE:
			return (Integer) exp.getOrDefault(env, AqlOption.gui_max_graph_size);

		case COMMENT:
			return 0;

		default:
			throw new RuntimeException("Anomaly: please report");
		}
	}

	private static JComponent wrapDisplay(String c, Exp<?> exp, Semantics obj, AqlEnv env, float time) {
		int maxSize = getMaxSize(exp, env);
		int sampleSize = (int) exp.getOrDefault(env, AqlOption.gui_sample_size);
		boolean doSample = (boolean) exp.getOrDefault(env, AqlOption.gui_sample);
		if (obj.size() > maxSize) {
			String s = doSample ? obj.sample(sampleSize) : null;
			s = s == null ? "" : "\n\nSample (may not include all tables, columns, or rows):\n\n" + s;
			return new CodeTextPanel("",
					"Display supressed, size > " + maxSize
							+ ".\n\nSee manual for a description of size, or try options gui_max_Z_size = X for X > "
							+ obj.size()
							+ " (the size of this object) and Z one of table, graph, string.  \n\nWarning: sizes that are too large will hang the viewer.\n\nCompute time: "
							+ env.performance.get(c)
							+ s);
		}
		int max_rows = (int) exp.getOrDefault(env, AqlOption.gui_rows_to_display);
		return AqlViewer.view(time, obj, max_rows, exp, env);
	}

	public AqlDisplay(String title, Program<Exp<?>> p, AqlEnv env, long start, long middle) {
		// Map<Object, String> map = new HashMap<>();
		
		yyy.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                JList<?> list = (JList<?>) e.getSource();
                if (list.locationToIndex(e.getPoint()) == -1 && !e.isShiftDown()
                        && !isMenuShortcutKeyDown(e)) {
                    list.clearSelection();
                }
            }

            private boolean isMenuShortcutKeyDown(InputEvent event) {
                return (event.getModifiers() & Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()) != 0;
            }
        });
		
		exn = env.exn;
		for (String c : p.order) {
			Exp<?> exp = p.exps.get(c);
			if (exp.kind() == Kind.COMMENT || exp instanceof PragmaExpCheck || exp instanceof PragmaExpConsistent) {
				continue;
			}
			if (env.defs.keySet().contains(c)) {
				Semantics obj = (Semantics) env.defs.get(c, exp.kind());

				try {
					frames.add(
							new Pair<>(doLookup(true, c, exp.kind(), env.typing), wrapDisplay(c, exp, obj, env, env.performance.get(c))));
				} catch (RuntimeException ex) {
					ex.printStackTrace();
					throw new LineException(ex.getMessage(), c, exp.kind().toString());
				}
			}
		}
		long end = System.currentTimeMillis();
		float c1 = ((middle - start) / (1000f));
		float c2 = ((end - middle) / (1000f));
		String pre = exn == null ? "" : "(ERROR, PARTIAL RESULT) | ";
		JComponent report = report(p, env, p.order, c1, c2, pre + title);
		frames.add(0, new Pair<>("Summary", report));

		display(pre + title, p.order, report);
	}

	private JComponent report(Program<Exp<?>> prog, AqlEnv env, List<String> order, float c1, float c2, String pre) {
		DecimalFormat df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.CEILING);
		List<String> l = new LinkedList<>();
		Object[][] rowData = new Object[env.defs.insts.size()][3];
		int i = 0;
		List<String> missing = new LinkedList<>();
		for (String k : order) {
			if (env.defs.insts.containsKey(k)) {
				Instance<?, ?, ?, ?, ?, ?, ?, ?, ?> I = (Instance<?, ?, ?, ?, ?, ?, ?, ?, ?>) env.get(Kind.INSTANCE, k);
				String s = k + "\t" + I.size() + "\t" + env.performance.get(k);
				l.add(s);
				rowData[i][0] = k;
				rowData[i][1] = I.size();
				rowData[i][2] = env.performance.get(k);
				i++;
			} else if (prog.exps.get(k).kind().equals(Kind.INSTANCE)) {
				missing.add(k);
			}
		}

		JPanel t = GuiUtil.makeTable(BorderFactory.createEmptyBorder(), "", rowData, "instance", "rows", "seconds");
		JPanel pan = new JPanel(new GridLayout(1, 1));
		pan.add(new JScrollPane(t));
		String tsv = "instance\trows\tseconds\n" + Util.sep(l, "\n");
		JTabbedPane jtb = new JTabbedPane();
		String text = pre;
		if (!missing.isEmpty()) {
			text += "\n\nInstances not computed: " + Util.sep(Util.alphabetical(missing), ", ");
		}
		text += "\n\nComputation wall-clock time: " + df.format(c1) + "s\nGUI building time: " + df.format(c2) + "s\n";
		Map<Kind, Float> perfs = new HashMap<>();
		for (Kind k : Kind.values()) {
			perfs.put(k, 0f);
		}
		for (String s : env.performance.keySet()) {
			Kind k = prog.exps.get(s).kind();
			perfs.put(k, perfs.get(k) + env.performance.get(s));
		}
		for (Kind k : Kind.values()) {
			if (perfs.get(k) < .05f) {
				continue;
			}
			text += "\n" + k + " computation total time: " + df.format(perfs.get(k)) + "s";
		}

		if (!prog.options.isEmpty()) {
			text += "\n\nGlobal options:\n";
			text += Util.sep(prog.options, " = ", "\n");
		}

		jtb.addTab("Text", new CodeTextPanel("", text));
		jtb.addTab("Performance", pan);
		jtb.addTab("TSV", new CodeTextPanel("", tsv));
		//jtb.addTab("Tree", viewTree(prog));
		return jtb;
	}

	
	

	private JFrame frame = null;
	private final List<Pair<String, JComponent>> frames = new LinkedList<>();

	private final CardLayout cl = new CardLayout();
	private final JPanel x = new JPanel(cl);
	private final JList<String> yyy = new JList<String>() {
		private static final long serialVersionUID = 1L;

		@Override
		public int locationToIndex(Point location) {
			int index = super.locationToIndex(location);
			if (index != -1 && !getCellBounds(index, index).contains(location)) {
				return -1;
			} else {
				return index;
			}
		}
	};

	private String current;

	private final JComponent lookup(String s) {
		for (Pair<String, JComponent> p : frames) {
			if (p.first.equals(s)) {
				return p.second;
			}
		}
		return null;
	}

	private void display(String s, @SuppressWarnings("unused") List<String> order, JComponent report) {
		frame = new JFrame();

		Vector<String> ooo = new Vector<>();
		for (Pair<String, JComponent> p : frames) {
			x.add(p.second, p.first);
			ooo.add(p.first);
		}
		x.add(report, "Summary");
		cl.show(x, "Summary");
		current = "Summary";

		yyy.setListData(ooo);
		yyy.setSelectedIndex(0);
		JPanel temp1 = new JPanel(new GridLayout(1, 1));
		temp1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Select:"));
		JScrollPane yyy1 = new JScrollPane(yyy);
		temp1.add(yyy1);
		yyy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		yyy.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				JComponent c = lookup(current);
				if (!(c instanceof JTabbedPane) || c == null) {
					return;
				}
				JTabbedPane p = (JTabbedPane) c;
				int i = p.getSelectedIndex();
				int j = -1;
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					j = i - 1;
					if (j < 0) {
						j = p.getTabCount() - 1;
					}
					p.setSelectedIndex(j);
					p.revalidate();
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					j = i + 1;
					if (j >= p.getTabCount()) {
						j = 0;
					}
					p.setSelectedIndex(j);
					p.revalidate();
				}

			}

		});

		yyy.addListSelectionListener(e -> {
			int i = yyy.getSelectedIndex();
			if (i == -1) {
				cl.show(x, "Summary");
				current = "Summary";
			} else {
				cl.show(x, ooo.get(i));
				current = ooo.get(i);
			}

		});

		JPanel north = new JPanel(new GridLayout(1, 1));
		JSplitPane px = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		px.setDividerLocation(200);
		px.setDividerSize(4);
		frame = new JFrame(/* "Viewer for " + */s);

		// TODO AQL what is other split pane?
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

		ActionListener escListener = e -> frame.dispose();

		frame.getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
		frame.getRootPane().registerKeyboardAction(escListener, ctrlW, JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.getRootPane().registerKeyboardAction(escListener, commandW, JComponent.WHEN_IN_FOCUSED_WINDOW);

		frame.setLocationRelativeTo(null);
		if (exn != null) {
			frame.setLocation(frame.getLocation().x + 400, frame.getLocation().y);
		}
		frame.setVisible(true);

	}

}
