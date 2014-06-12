package fql_lib.gui;

import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Quad;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.cat.Category;
import fql_lib.cat.Functor;
import fql_lib.cat.Transform;
import fql_lib.cat.categories.FinCat;
import fql_lib.cat.categories.FinSet;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.cat.categories.FunCat;
import fql_lib.cat.categories.Inst;
import fql_lib.cat.presentation.Signature;
import fql_lib.decl.CatExp;
import fql_lib.decl.CatOps;
import fql_lib.decl.Environment;
import fql_lib.decl.FQLProgram;
import fql_lib.decl.FunctorExp;
//import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.layout.FRLayout;

/**
 * 
 * @author ryan
 * 
 *         Class for showing all the viewers.
 */
@SuppressWarnings("rawtypes")
public class Display {

	Map<Object, Color> colors = new HashMap<>();

	Color getColor(Object o) {
		if (FinSet.FinSet.equals(o) || FinCat.FinCat.equals(o)) {
			return null;
		}
		Color c = colors.get(o);
		if (c == null) {
			colors.put(o, nColor());
		}
		return colors.get(o);
	}

	int cindex = 0;
	public static Color[] colors_arr = new Color[] { Color.RED, Color.GREEN, Color.BLUE,
			Color.MAGENTA, Color.yellow, Color.CYAN, Color.GRAY, Color.ORANGE, Color.PINK,
			Color.BLACK, Color.white };

	public Color nColor() {
		if (cindex < colors_arr.length) {
			return colors_arr[cindex++];
		} else {
			cindex = 0;
			return nColor();
		}
	}

	List<Pair<String, JComponent>> frames = new LinkedList<>();

	public JPanel showSet(Set<?> view, Color c) {
		JTabbedPane px = new JTabbedPane();

		if (DEBUG.debug.set_graph) {
			JComponent gp = makeCatViewer(Category.fromSet(view), c);
			px.add("Graph", gp);
		}

		if (DEBUG.debug.set_tabular) {
			Object[][] rowData = new Object[view.size()][1];
			int i = 0;
			for (Object o : view) {
				rowData[i++][0] = Util.nice(o.toString());
			}
			Object[] colNames = new Object[] { "Element" };
			JPanel gp = Util.makeTable(BorderFactory.createEtchedBorder(), view.size()
					+ " elements", rowData, colNames);
			px.add("Table", gp);
		}

		if (DEBUG.debug.set_textual) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(), view.size()
					+ " elements", Util.nice(view.toString()));
			px.add("Text", gp);
		}

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}

	public JPanel showCat(Category<?, ?> view, Color c) {
		JTabbedPane px = new JTabbedPane();

		Signature<String, String> sig = null;
		String key = unr(env.cats, view, null);
		if (key != null) {
			CatExp r = CatOps.resolve(prog, prog.cats.get(key));
			if (r instanceof CatExp.Const) {
				CatExp.Const sig0 = (CatExp.Const) r;
				sig = new Signature<>(sig0.nodes, sig0.arrows, sig0.eqs);
			}
		}
		if (sig != null && !view.isInfinite()) {
			if (DEBUG.debug.cat_schema) {
				Graph g = buildFromSig(sig);
				if (g.getVertexCount() == 0) {
					px.add("Schema", new JPanel());
				} else if (g.getVertexCount() > DEBUG.debug.MAX_NODES) {
					FQLTextPanel xxx = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "Graph has " + g.getVertexCount() + " nodes, which exceeds limit of " + DEBUG.debug.MAX_NODES);
					JPanel ret = new JPanel(new GridLayout(1,1));
					ret.add(xxx);
					return ret;
				} else {
					JComponent yyy = doSchemaView(c, g);
					px.add("Schema", yyy);
				}	
			}
		}
		
		if (DEBUG.debug.cat_graph && !view.isInfinite()) {
			JComponent gp = makeCatViewer(view, c);
			px.add("Graph", gp);
		}

		if (DEBUG.debug.cat_tabular && !view.isInfinite()) {
			JPanel gp = catTable(view);
			px.add("Table", gp);
		}

		if (DEBUG.debug.cat_textual) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(),null,
					Util.nice(view.toString()));
			px.add("Text", gp);
		}
		
		/*
		if (!view.isInfinite()) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(),null,
					view.repX().toString());
			px.add("Representables", gp);
		}
		*/
		

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}

	@SuppressWarnings({ "unchecked" })
	private JPanel catTable(Category view) {
		JPanel gp = new JPanel(new GridLayout(2, 3));

		Object[][] rowData1 = new Object[view.objects().size()][1];
		int i = 0;
		for (Object o : view.objects()) {
			rowData1[i++][0] = Util.nice(o.toString());
		}
		Object[] colNames1 = new Object[] { "Object" };
		JPanel gp1 = Util.makeTable(BorderFactory.createEtchedBorder(), "Objects ("
				+ view.objects().size() + ")", rowData1, colNames1);

		Object[][] rowData2 = new Object[view.arrows().size()][3];
		i = 0;
		for (Object o : view.arrows()) {
			rowData2[i][0] = Util.nice(o.toString());
			rowData2[i][1] = Util.nice(view.source(o).toString());
			rowData2[i][2] = Util.nice(view.target(o).toString());
			i++;
		}
		Object[] colNames2 = new Object[] { "Arrow", "Source", "Target" };
		JPanel gp2 = Util.makeTable(BorderFactory.createEtchedBorder(), "Arrows ("
				+ view.arrows().size() + ")", rowData2, colNames2);

		Object[][] rowData3 = new Object[view.objects().size()][2];
		i = 0;
		for (Object o : view.objects()) {
			rowData3[i][0] = Util.nice(o.toString());
			rowData3[i][1] = Util.nice(view.identity(o).toString());
			i++;
		}
		Object[] colNames3 = new Object[] { "Object", "Arrow" };
		JPanel gp3 = Util.makeTable(BorderFactory.createEtchedBorder(), "Identities ("
				+ view.arrows().size() + ")", rowData3, colNames3);

		Object[][] rowData4 = new Object[view.arrows().size()][2];
		i = 0;
		for (Object o : view.arrows()) {
			rowData4[i][0] = Util.nice(o.toString());
			rowData4[i][1] = Util.nice(view.source(o).toString());
			i++;
		}
		Object[] colNames4 = new Object[] { "Arrow", "Object" };
		JPanel gp4 = Util.makeTable(BorderFactory.createEtchedBorder(), "Sources ("
				+ view.arrows().size() + ")", rowData4, colNames4);

		Object[][] rowData5 = new Object[view.arrows().size()][2];
		i = 0;
		for (Object o : view.arrows()) {
			rowData5[i][0] = Util.nice(o.toString());
			rowData5[i][1] = Util.nice(view.target(o).toString());
			i++;
		}
		Object[] colNames5 = new Object[] { "Arrow", "Object" };
		JPanel gp5 = Util.makeTable(BorderFactory.createEtchedBorder(), "Targets ("
				+ view.arrows().size() + ")", rowData5, colNames5);

		Object[][] rowData6 = new Object[view.compositionSize()][3];
		i = 0;
		for (Object o1 : view.arrows()) {
			for (Object o2 : view.arrows()) {
				if (!view.target(o1).equals(view.source(o2))) {
					continue;
				}
				rowData6[i][0] = Util.nice(o1.toString());
				rowData6[i][1] = Util.nice(o2.toString());
				rowData6[i][2] = Util.nice(view.compose(o1, o2).toString());
				i++;
			}
		}
		Object[] colNames6 = new Object[] { "A1", "A2", "A1 ; A2" };
		JPanel gp6 = Util.makeTable(BorderFactory.createEtchedBorder(), "Composition (" + i + ")",
				rowData6, colNames6);

		gp.add(gp1);
		gp.add(gp4);
		gp.add(gp3);
		gp.add(gp2);
		gp.add(gp5);
		gp.add(gp6);
		return gp;
	}

	@SuppressWarnings("unchecked")
	public JPanel showFn(Fn view, Color src, Color dst) {
		JTabbedPane px = new JTabbedPane();

		if (DEBUG.debug.fn_graph) {
			JComponent gp = makeFnViewer(view, src, dst);
			px.add("Graph", gp);
		}

		if (DEBUG.debug.fn_tabular) {
			Object[][] rowData = new Object[view.source.size()][2];
			int i = 0;
			for (Object o : view.source) {
				rowData[i][0] = Util.nice(o.toString());
				rowData[i][1] = Util.nice(view.apply(o).toString());
				i++;
			}
			Object[] colNames = new Object[] { unr(env.sets, view.source, "..."),
					unr(env.sets, view.target, "...") };
			JPanel gp = Util.makeTable(BorderFactory.createEtchedBorder(), view.source.size()
					+ " elements in domain, " + view.target.size() + " elements in codomain",
					rowData, colNames);
			px.add("Table", gp);
		}

		if (DEBUG.debug.fn_textual) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(),
					view.source.size() + " elements in domain, " + view.target.size()
							+ " elements in codomain", Util.nice(view.toStringLong()));
			px.add("Text", gp);
		}

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}

	@SuppressWarnings("unchecked")
	public JPanel showFtr(Functor view, Color c, FunctorExp e) {
		JTabbedPane px = new JTabbedPane();

		if (view.source.isInfinite()) {
			FQLTextPanel p = new FQLTextPanel(BorderFactory.createEtchedBorder(), null,
					"Cannot display functors from " + view.source);
			px.add("Text", p);
			JPanel top = new JPanel(new GridLayout(1, 1));
			top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			top.add(px);
			return top;
		}

		Signature<String, String> src_sig = null;
		Signature<String, String> dst_sig = null;
		String src_key = unr(env.cats, view.source, null);
		if (src_key != null) {
			CatExp r = CatOps.resolve(prog, prog.cats.get(src_key));
			if (r instanceof CatExp.Const) {
				CatExp.Const sig0 = (CatExp.Const) r;
				src_sig = new Signature<>(sig0.nodes, sig0.arrows, sig0.eqs);
			}
		}
		String dst_key = unr(env.cats, view.target, null);
		if (dst_key != null) {
			CatExp r = CatOps.resolve(prog, prog.cats.get(dst_key));
			if (r instanceof CatExp.Const) {
				CatExp.Const sig0 = (CatExp.Const) r;
				dst_sig = new Signature<>(sig0.nodes, sig0.arrows, sig0.eqs);
			}
		}
		if (src_sig != null && FinSet.FinSet.equals(view.target)) {
			if (DEBUG.debug.ftr_instance) {
			JPanel vwr = new JPanel(new GridLayout(1, 1));
			if (view.source.objects().size() == 0) {
				px.add("Instance", vwr);
			} else {
				JComponent zzz = doFNView2(view, vwr, c, buildFromSig(src_sig), src_sig);
				JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				newthing.setResizeWeight(.5d);
				newthing.add(zzz);
				newthing.add(vwr);
				JPanel xxx = new JPanel(new GridLayout(1, 1));
				xxx.add(newthing);
				px.add("Instance", xxx);
			}
			}

			if (DEBUG.debug.ftr_joined) {
			px.add("Joined", makeJoined(src_sig, view).first);
			}

			if (DEBUG.debug.ftr_elements) {
			Graph g = buildElements(src_sig, view);
			if (g.getVertexCount() == 0) {
				px.add("Elements", new JPanel());
			} else if (g.getVertexCount() > DEBUG.debug.MAX_NODES) {
				FQLTextPanel xxx = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "Graph has " + g.getVertexCount() + " nodes, which exceeds limit of " + DEBUG.debug.MAX_NODES);
				JPanel ret = new JPanel(new GridLayout(1,1));
				ret.add(xxx);
				return ret;
			} else {
				JComponent yyy = doElementsView(c, g);
				px.add("Elements", yyy);
			}
			}
		}
		if (src_sig != null && dst_sig != null && !view.source.isInfinite() && !view.target.isInfinite()) {
			//JPanel vwr = new JPanel(new GridLayout(1, 1));
			if (DEBUG.debug.ftr_mapping) {
			Graph g = buildMapping(src_sig, dst_sig, view);
			if (g.getVertexCount() == 0) {
				px.add("Mapping", new JPanel());
			} else if (g.getVertexCount() > DEBUG.debug.MAX_NODES) {
				FQLTextPanel xxx = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "Graph has " + g.getVertexCount() + " nodes, which exceeds limit of " + DEBUG.debug.MAX_NODES);
				JPanel ret = new JPanel(new GridLayout(1,1));
				ret.add(xxx);
				return ret;
			} else {
				JComponent zzz = doMappingView(c, getColor(view.target), g);
				JPanel xxx = new JPanel(new GridLayout(1, 1));
				xxx.add(zzz);
				px.add("Mapping", xxx);
			}
			}
		}

		if (DEBUG.debug.ftr_graph) {
			JPanel vwr = new JPanel(new GridLayout(1, 1));
			if (view.source.objects().size() == 0) {
				px.add("Graph", vwr);
			} else {
				JComponent zzz = doFNView(view, vwr, c, buildFromCat(view.source));
				JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				newthing.setResizeWeight(.5d);
				newthing.add(zzz);
				newthing.add(vwr);
				JPanel xxx = new JPanel(new GridLayout(1, 1));
				xxx.add(newthing);
				px.add("Graph", xxx);
			}
		}

		if (DEBUG.debug.ftr_tabular) {
			JPanel gp = new JPanel(new GridLayout(2, 1));

			Object[][] rowData = new Object[view.source.objects().size()][2];
			int i = 0;
			for (Object o : view.source.objects()) {
				rowData[i][0] = Util.nice(o.toString());
				rowData[i][1] = Util.nice(view.applyO(o).toString());
				i++;
			}
			Object[] colNames = new Object[] { "Input", "Output" };
			JPanel gp1 = Util.makeTable(BorderFactory.createEtchedBorder(), "On Objects ("
					+ view.source.objects().size() + ")", rowData, colNames);

			Object[][] rowData2 = new Object[view.source.arrows().size()][6];
			i = 0;
			for (Object o : view.source.arrows()) {
				rowData2[i][0] = Util.nice(o.toString());
				rowData2[i][1] = Util.nice(view.source.source(o).toString());
				rowData2[i][2] = Util.nice(view.source.target(o).toString());
				rowData2[i][3] = Util.nice(view.applyA(o).toString());
				rowData2[i][4] = Util.nice(view.target.source(view.applyA(o)).toString());
				rowData2[i][5] = Util.nice(view.target.target(view.applyA(o)).toString());
				i++;
			}
			Object[] colNames2 = new Object[] { "Input", "Source", "Target", "Output", "Source",
					"Target" };
			JPanel gp2 = Util.makeTable(BorderFactory.createEtchedBorder(), "On Arrows ("
					+ view.source.arrows().size() + ")", rowData2, colNames2);

			gp.add(gp1);
			gp.add(gp2);

			px.add("Table", gp);
		}

		if (DEBUG.debug.ftr_textual) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "",
					Util.nice(view.toString()));
			px.add("Text", gp);
		}
		/*
		if (!view.source.isInfinite() && view.target.equals(FinSet.FinSet)) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "",
					view.subInstances().toString());
			px.add("Subinstances", gp);
		} */


		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}
	
	@SuppressWarnings("unchecked")
	public JPanel showTrans(Transform view, Color c) {
		JTabbedPane px = new JTabbedPane();

		if (view.source.source.isInfinite()) {
			FQLTextPanel p = new FQLTextPanel(BorderFactory.createEtchedBorder(), null,
					"Cannot display transforms from " + view.source.source);
			px.add("Text", p);
			JPanel top = new JPanel(new GridLayout(1, 1));
			top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			top.add(px);
			return top;
		}

		Signature<Object, Object> src_sig = null;
	//	Signature<Object, Object> dst_sig = null;
		String src_key = unr(env.cats, view.source.source, null);
		if (src_key == null) {
			src_key = unr(env.cats, view.target.source, null);
		}
		if (src_key != null) {
			CatExp r = CatOps.resolve(prog, prog.cats.get(src_key));
			if (r instanceof CatExp.Const) {
				CatExp.Const sig0 = (CatExp.Const) r;
				src_sig = new Signature(sig0.nodes, sig0.arrows, sig0.eqs);
			}
		}
	//	String dst_key = unr(env.cats, view.source.target, null);
	//	if (dst_key == null) {
	//		dst_key = unr(env.cats, view.target.target, null);
	//	}
	/*	if (dst_key != null) {
			CatExp r = CatOps.resolve(prog, prog.cats.get(dst_key));
			if (r instanceof CatExp.Const) {
				CatExp.Const sig0 = (CatExp.Const) r;
				dst_sig = new Signature(sig0.nodes, sig0.arrows, sig0.eqs);
			}
		} */

		if (src_sig != null && FinSet.FinSet.equals(view.target.target)) {
		//	JPanel vwr = new JPanel(new GridLayout(1, 1));
			if (DEBUG.debug.trans_elements) {
			Graph g = build2Elements(src_sig, view);
			if (g.getVertexCount() == 0) {
				px.add("Elements", new JPanel());
			} else if (g.getVertexCount() > DEBUG.debug.MAX_NODES) {
				FQLTextPanel xxx = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "Graph has " + g.getVertexCount() + " nodes, which exceeds limit of " + DEBUG.debug.MAX_NODES);
				JPanel ret = new JPanel(new GridLayout(1,1));
				ret.add(xxx);
				return ret;
			} else {
				JComponent zzz = doElements2View(c, g);
				JPanel xxx = new JPanel(new GridLayout(1, 1));
				xxx.add(zzz);
				px.add("Elements", xxx);
			}
			}
		}

		if (DEBUG.debug.trans_graph) {
			JPanel vwr = new JPanel(new GridLayout(1, 1));
			if (view.source.source.objects().size() == 0) {
				px.add("Graph", vwr);

			} else {
				JComponent zzz = doNTView(view, vwr, c, buildFromCat(view.source.source));
				JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				newthing.setResizeWeight(.5d);
				newthing.add(zzz);
				newthing.add(vwr);
				JPanel xxx = new JPanel(new GridLayout(1, 1));
				xxx.add(newthing);
				px.add("Graph", xxx);
			}
		}

		if (DEBUG.debug.trans_tabular) {
			JPanel gp = new JPanel(new GridLayout(1, 1));

			Object[][] rowData = new Object[view.source.source.objects().size()][2];
			int i = 0;
			for (Object o : view.source.source.objects()) {
				rowData[i][0] = Util.nice(o.toString());
				rowData[i][1] = Util.nice(view.apply(o).toString());
				i++;
			}
			Object[] colNames = new Object[] { "Input", "Output" };
			JPanel gp1 = Util.makeTable(BorderFactory.createEtchedBorder(), "On Objects ("
					+ view.source.source.objects().size() + ")", rowData, colNames);

			gp.add(gp1);

			px.add("Table", gp);
		}

		if (DEBUG.debug.trans_textual) {
			FQLTextPanel gp = new FQLTextPanel(BorderFactory.createEtchedBorder(), "",
					Util.nice(view.toString()));
			px.add("Text", gp);
		}

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}

	/*
	 * public JPanel showInst(String c, Color clr, /* Color color Environment
	 * environment, String c, Instance view) throws FQLException { JTabbedPane
	 * px = new JTabbedPane();
	 * 
	 * if (DEBUG.debug.inst_graphical) { JPanel gp = view.pretty(clr); // JPanel
	 * gp0 = new JPanel(new GridLayout(1, 1)); //
	 * gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); //
	 * gp0.add(gp); px.add("Graphical", gp); // gp.setSize(600, 600); }
	 * 
	 * if (DEBUG.debug.inst_textual) { JPanel ta = view.text();
	 * px.add("Textual", ta); }
	 * 
	 * if (DEBUG.debug.inst_tabular) { JPanel tp = view.view();
	 * px.add("Tabular", tp); }
	 * 
	 * if (DEBUG.debug.inst_joined) { JPanel joined = view.join();
	 * px.add("Joined", joined); }
	 * 
	 * if (DEBUG.debug.inst_gr) { JPanel groth = view.groth(clr);
	 * px.add("Elements", groth); }
	 * 
	 * if (DEBUG.debug.inst_obs) { JPanel rel = view.observables2();
	 * px.add("Observables", rel); }
	 * 
	 * if (DEBUG.debug.inst_rdf) { JPanel rel = view.rdf(c); px.add("RDF", rel);
	 * }
	 * 
	 * JPanel top = new JPanel(new GridLayout(1, 1));
	 * top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); top.add(px);
	 * /* System.out.println("start"); long start = System.currentTimeMillis();
	 * System.out.println("fast " + view.subInstances_fast().size()); long x1 =
	 * System.currentTimeMillis(); System.out.println("old " +
	 * view.subInstances().size()); long x2 = System.currentTimeMillis();
	 * System.out.println("fast time " + (x1 - start));
	 * System.out.println("old time " + (x2 - x1));
	 *//*
		 * return top;
		 * 
		 * }
		 * 
		 * public JPanel showMapping(Environment environment, Color scolor,
		 * Color tcolor, Mapping view) throws FQLException {
		 * 
		 * JTabbedPane px = new JTabbedPane();
		 * 
		 * if (DEBUG.debug.mapping_graphical) { JPanel gp = view.pretty(scolor,
		 * tcolor, environment); // JPanel gp0 = new JPanel(new GridLayout(1,
		 * 1)); // gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		 * // gp0.add(gp); px.add("Graphical", gp); // new JScrollPane(gp0, //
		 * JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, //
		 * JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)); // gp0.setSize(600, 600);
		 * }
		 * 
		 * if (DEBUG.debug.mapping_textual) { JPanel ta = view.text();
		 * px.add("Textual", ta); }
		 * 
		 * if (DEBUG.debug.mapping_tabular) { JPanel tp = view.view();
		 * px.add("Tabular", tp); }
		 * 
		 * if (DEBUG.debug.mapping_ed) { JPanel map = view.constraint();
		 * px.add("ED", map); }
		 * 
		 * JPanel top = new JPanel(new GridLayout(1, 1));
		 * top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		 * top.add(px); return top; }
		 * 
		 * public JPanel showTransform(Color scolor, Color tcolor, Environment
		 * environment, String src_n, String dst_n, Transform view) throws
		 * FQLException { JTabbedPane px = new JTabbedPane();
		 * 
		 * if (DEBUG.debug.transform_graphical) { JPanel gp =
		 * view.graphical(scolor, tcolor, src_n, dst_n); // JPanel gp0 = new
		 * JPanel(new GridLayout(1, 1)); //
		 * gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); //
		 * gp0.add(gp); px.add("Graphical", gp); // new JScrollPane(gp0, //
		 * JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, //
		 * JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)); // gp0.setSize(600, 600);
		 * }
		 * 
		 * if (DEBUG.debug.transform_textual) { JPanel ta = view.text();
		 * px.add("Textual", ta); }
		 * 
		 * if (DEBUG.debug.transform_tabular) { JPanel tp = view.view(src_n,
		 * dst_n); px.add("Tabular", tp); }
		 * 
		 * JPanel top = new JPanel(new GridLayout(1, 1));
		 * top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		 * top.add(px); return top; }
		 * 
		 * // FQLProgram prog;
		 * 
		 * public JPanel showSchema(Environment environment, Color clr,
		 * Signature view) throws FQLException { JTabbedPane px = new
		 * JTabbedPane();
		 * 
		 * if (DEBUG.debug.schema_graphical) { JComponent gp = view.pretty(clr);
		 * // JPanel gp0 = new JPanel(new GridLayout(1, 1)); //
		 * gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); //
		 * gp0.add(gp); px.add("Graphical", gp); // gp0.setSize(600, 600); }
		 * 
		 * if (DEBUG.debug.schema_textual) { JPanel ta = view.text();
		 * px.add("Textual", ta); }
		 * 
		 * if (DEBUG.debug.schema_tabular) { JPanel tp = view.view();
		 * px.add("Tabular", tp); }
		 * 
		 * if (DEBUG.debug.schema_ed) { JPanel map = view.constraint();
		 * px.add("ED", map); }
		 * 
		 * if (DEBUG.debug.schema_denotation) { JPanel den = view.denotation();
		 * px.add("Denotation", den); }
		 * 
		 * if (DEBUG.debug.schema_rdf) { JPanel rel = view.rdf(); px.add("OWL",
		 * rel); }
		 * 
		 * JPanel top = new JPanel(new GridLayout(1, 1));
		 * top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		 * top.add(px); return top; }
		 * 
		 * public JPanel showFullQuery(FQLProgram p, Environment env, FullQuery
		 * view, FullQueryExp x) throws FQLException { JTabbedPane px = new
		 * JTabbedPane();
		 * 
		 * 
		 * JTextArea area = new JTextArea(x.printNicely(p));
		 * 
		 * if (DEBUG.debug.query_graphical) { px.add("Graphical",
		 * view.pretty()); } if (DEBUG.debug.query_textual) { px.add("Text", new
		 * JScrollPane(area)); }
		 * 
		 * JPanel top = new JPanel(new GridLayout(1, 1));
		 * top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		 * top.add(px); return top;
		 * 
		 * }
		 * 
		 * public JPanel showQuery(FQLProgram prog, Environment environment /* ,
		 * String c
		 *//*
			 * , Query view) throws FQLException { JTabbedPane px = new
			 * JTabbedPane();
			 * 
			 * Mapping d = view.project; Mapping p = view.join; Mapping u =
			 * view.union; Signature s = d.target; Signature i1 = d.source;
			 * Signature i2 = p.target; Signature t = u.target;
			 * 
			 * px.add("Source", showSchema(environment, prog.smap(s.toConst()),
			 * s)); px.add("Delta", showMapping(environment,
			 * prog.smap(i1.toConst()), prog.smap(s.toConst()), d));
			 * px.add("Intermediate 1", showSchema(environment,
			 * prog.smap(i1.toConst()), i1)); px.add("Pi",
			 * showMapping(environment, prog.smap(i1.toConst()),
			 * prog.smap(i2.toConst()), p)); px.add("Intermediate 2",
			 * showSchema(environment, prog.smap(i2.toConst()), i2));
			 * px.add("Sigma", showMapping(environment, prog.smap(i2.toConst()),
			 * prog.smap(t.toConst()), u)); px.add("Target",
			 * showSchema(environment, prog.smap(t.toConst()), t));
			 * 
			 * JPanel top = new JPanel(new GridLayout(1, 1));
			 * top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			 * top.add(px); return top; }
			 */
	FQLProgram prog;
	Environment env;

	// private Map<String, Color> cmap = new HashMap<>();
	public Display(String title, FQLProgram p, Environment env) {
		this.prog = p;
		this.env = env;

		for (String c : p.order) {
			if (env.sets.containsKey(c)) {
				Set<?> exp = env.sets.get(c);
				frames.add(new Pair<>("set " + c, showSet(exp, getColor(exp))));
			} else if (env.fns.containsKey(c)) {
				Fn o = env.fns.get(c);
				frames.add(new Pair<>("function " + c + " : " + unr(env.sets, o.source, "...")
						+ " -> " + unr(env.sets, o.target, "..."), showFn(o, getColor(o.source),
						getColor(o.target))));
			} else if (env.cats.containsKey(c)) {
				Category<?, ?> cat = env.cats.get(c);
				frames.add(new Pair<>("category " + c, showCat(cat, getColor(cat))));
			} else if (env.ftrs.containsKey(c)) {
				Functor o = env.ftrs.get(c);
				String ddd = "...";
				if (FinSet.FinSet.equals(o.target)) {
					ddd = "Set";
				} else if (FinCat.FinCat.equals(o.target)) {
					ddd = "Cat";
				} else if (o.target instanceof Inst) {
					ddd = "Set^" + unr(env.cats, ((Inst)o.target).cat, ddd);
				} else if (o.target instanceof FunCat) {
					ddd = "Cat^" + unr(env.cats, ((FunCat)o.target).cat, ddd);
				} else {
					ddd = unr(env.cats, o.target, ddd);
				}
				String eee = "...";
				if (FinSet.FinSet.equals(o.source)) {
					eee = "Set";
				} else if (FinCat.FinCat.equals(o.source)) {
					eee = "Cat";
				} else if (o.source instanceof Inst) {
					eee = "Set^" + unr(env.cats, ((Inst)o.source).cat, ddd);
				} else if (o.source
						instanceof FunCat) {
					eee = "Cat^" + unr(env.cats, ((FunCat)o.source).cat, ddd);
				} else {
					eee = unr(env.cats, o.source, eee);
				}
				frames.add(new Pair<>("functor " + c + " : " + eee + " -> " + ddd, showFtr(o,
						getColor(o.source), p.ftrs.get(c))));
			} else if (env.trans.containsKey(c)) {
				Transform o = env.trans.get(c);
				String ddd = unr(env.ftrs, o.target, "...");
				String eee = unr(env.ftrs, o.source, "...");
				frames.add(new Pair<>("transform " + c + " : " + eee + " -> " + ddd, showTrans(o,
						getColor(o.source.source))));
			} else {
				throw new RuntimeException();
			}
		}

		display(title, prog.order);
	}

	private static <X> String unr(Map<String, X> set, X s, String xxx) {
		for (Entry<String, X> k : set.entrySet()) {
			if (k.getValue().equals(s)) {
				return k.getKey();
			}
		}
		return xxx;
	}

	JFrame frame = null;
	String name;

	final CardLayout cl = new CardLayout();
	final JPanel x = new JPanel(cl);
	final JList<String> yyy = new JList<>();
	final Map<String, String> indices = new HashMap<>();

	public void display(String s, List<String> order) {
		// System.out.println(order);
		frame = new JFrame();
		this.name = s;

		final Vector<String> ooo = new Vector<>();
		int index = 0;
		for (Pair<String, JComponent> p : frames) {
			x.add(p.second, p.first);
			ooo.add(p.first);
			indices.put(order.get(index++), p.first);
		}
		x.add(new JPanel(), "blank");
		cl.show(x, "blank");

		yyy.setListData(ooo);
		JPanel temp1 = new JPanel(new GridLayout(1, 1));
		temp1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
				"Select:"));
		JScrollPane yyy1 = new JScrollPane(yyy);
		temp1.add(yyy1);
		temp1.setMinimumSize(new Dimension(200, 600));
		yyy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		yyy.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = yyy.getSelectedIndex();
				if (i == -1) {
					cl.show(x, "blank");
				} else {
					cl.show(x, ooo.get(i).toString());
				}
			}

		});

		JPanel north = new JPanel(new GridLayout(2, 1));
		FQLSplit px = new FQLSplit(.5, JSplitPane.HORIZONTAL_SPLIT);
		px.setDividerSize(6);
		frame = new JFrame(/* "Viewer for " + */s);

		JSplitPane temp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		temp2.setResizeWeight(1);
		temp2.setDividerSize(0);
		temp2.setBorder(BorderFactory.createEmptyBorder());
		temp2.add(temp1);
		temp2.add(north);

		// px.add(temp1);
		px.add(temp2);

		px.add(x);

		// JPanel bd = new JPanel(new BorderLayout());
		// bd.add(px, BorderLayout.CENTER);
		// bd.add(north, BorderLayout.NORTH);

		// frame.setContentPane(bd);
		frame.setContentPane(px);
		frame.setSize(850, 600);

		ActionListener escListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		};

		frame.getRootPane().registerKeyboardAction(escListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
		frame.getRootPane().registerKeyboardAction(escListener, ctrlW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.getRootPane().registerKeyboardAction(escListener, commandW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	public static class MutableInteger {
		int i;

		public MutableInteger(int i) {
			this.i = i;
		}

		public String pp() {
			return Integer.toString(i++);
		}
	}

	public void close() {
		if (frame == null) {
			return;
		}
		frame.setVisible(false);
		frame.dispose();
		frame = null;
	}

	public static <X, Y> Graph<Pair<X, Object>, Triple<Y, Pair<X, Object>, Pair<X, Object>>> buildElements(
			Signature<X, Y> c,
			Functor<Signature<X, Y>.Node, Signature<X, Y>.Path, Set<Object>, Fn<Object, Object>> I) {
		Graph<Pair<X, Object>, Triple<Y, Pair<X, Object>, Pair<X, Object>>> ret = new DirectedSparseMultigraph<>();

		for (Signature<X, Y>.Node n : c.nodes) {
			for (Object o : I.applyO(n)) {
				ret.addVertex(new Pair<>(n.name, o));
			}
		}
		for (Signature<X, Y>.Edge e : c.edges) {
			for (Object o : I.applyO(e.source)) {
				Object fo = I.applyA(c.path(e)).apply(o);
				Pair<X, Object> s = new Pair<>(e.source.name, o);
				Pair<X, Object> t = new Pair<>(e.target.name, fo);
				ret.addEdge(new Triple<>(e.name, s, t), s, t);
			}
		}

		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static Graph buildMapping(Signature<String,String> src, Signature<String,String> dst, Functor F) {
	
		Graph<Object, Object> ret = new DirectedSparseMultigraph<>();

		for (Signature.Node n : src.nodes) {
			ret.addVertex(new Pair<>(n.name, "src"));
		}
		for (Signature.Edge e : src.edges) {
			Pair s = new Pair<>(e.source.name, "src");
			Pair t = new Pair<>(e.target.name, "src");
			ret.addEdge(new Quad<>(e.name, s, t, "src"), s, t);
		}

		for (Signature.Node n : dst.nodes) {
			ret.addVertex(new Pair<>(n.name, "dst"));
		}
		for (Signature.Edge e : dst.edges) {
			Pair s = new Pair<>(e.source.name, "dst");
			Pair t = new Pair<>(e.target.name, "dst");
			ret.addEdge(new Quad<>(e.name, s, t, "dst"), s, t);
		}

		int i = 0;
		for (Signature.Node n : src.nodes) {
			Signature.Node fo = (Signature.Node) F.applyO(n);
			Pair s = new Pair<>(n.name, "src");
			Pair t = new Pair<>(fo.name, "dst");
			ret.addEdge(new Quad<>("", s, t, i++), s, t);
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	public static Graph build2Elements(Signature<Object, Object> sig,
			Transform<Object, Object, Set, Fn> trans) {
		Functor<Object, Object, Set, Fn> I = trans.source;
		Functor<Object, Object, Set, Fn> J = trans.target;

		Graph<Object, Object> ret = new DirectedSparseMultigraph<>();

		for (Signature.Node n : sig.nodes) {
			for (Object o : I.applyO(n)) {
				ret.addVertex(new Triple<>(o, n.name, "src"));
			}
		}
		for (Signature.Edge e : sig.edges) {
			for (Object o : I.applyO(e.source)) {
				Object fo = I.applyA(sig.path(e)).apply(o);
				Triple s = new Triple<>(o, e.source.name, "src");
				Triple t = new Triple<>(fo, e.target.name, "dst");
				ret.addEdge(new Quad<>(e.name, s, t, "src"), s, t);
			}
		}

		for (Signature.Node n : sig.nodes) {
			for (Object o : J.applyO(n)) {
				ret.addVertex(new Triple<>(o, n.name, "dst"));
			}
		}
		for (Signature.Edge e : sig.edges) {
			for (Object o : J.applyO(e.source)) {
				Object fo = J.applyA(sig.path(e)).apply(o);
				Triple s = new Triple<>(o, e.source.name, "dst");
				Triple t = new Triple<>(fo, e.target.name, "dst");
				ret.addEdge(new Quad<>(e.name, s, t, "dst"), s, t);
			}
		}

		int i = 0;
		for (Signature.Node n : sig.nodes) {
			for (Object o : I.applyO(n)) {
				Object fo = trans.apply(n).apply(o);
				Triple s = new Triple<>(o, n.name, "src");
				Triple t = new Triple<>(fo, n.name, "dst");
				ret.addEdge(new Quad<>("", s, t, i++), s, t);
			}
		}

		return ret;
	}

	public static Graph<Signature<String, String>.Node, String> buildFromSig(
			Signature<String, String> c) {
		Graph<Signature<String, String>.Node, String> g2 = new DirectedSparseMultigraph<>();
		for (Signature<String, String>.Node n : c.nodes) {
			g2.addVertex(n);
		}
		for (Signature<String, String>.Edge e : c.edges) {
			g2.addEdge(e.name, e.source, e.target);
		}
		return g2;
	}

	public static <X, Y> Graph<X, Y> buildFromCat(Category<X, Y> c) {
		Graph<X, Y> g2 = new DirectedSparseMultigraph<>();
		for (X n : c.objects()) {
			g2.addVertex(n);
		}
		for (Y e : c.arrows()) {
			if (c.identity(c.source(e)).equals(e)) {
				continue;
			}
			g2.addEdge(e, c.source(e), c.target(e));
		}
		return g2;
	}

	@SuppressWarnings("unchecked")
	public static Graph<Pair<String, Color>, Integer> buildFromFn(Fn f, Color src, Color dst) {

		Graph<Pair<String, Color>, Integer> g2 = new DirectedSparseMultigraph<>();
		for (Object n : f.source) {
			g2.addVertex(new Pair<>(Util.nice(n.toString()), src));
		}
		for (Object n : f.target) {
			g2.addVertex(new Pair<>(Util.nice(n.toString()), dst));
		}
		int i = 0;
		for (Object n : f.source) {
			Pair<String, Color> p1 = new Pair<>(Util.nice(n.toString()), src);
			Pair<String, Color> p2 = new Pair<>(Util.nice(f.apply(n).toString()), dst);

			g2.addEdge(new Integer(i++), p1, p2);
		}

		return g2;
	}

	public static <X, Y> JComponent makeCatViewer(Category<X, Y> cat, Color clr) {
		Graph<X, Y> g = buildFromCat(cat);
		if (g.getVertexCount() == 0) {
			return new JPanel();
		} else if (g.getVertexCount() > DEBUG.debug.MAX_NODES) {
			FQLTextPanel xxx = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "Graph has " + g.getVertexCount() + " nodes, which exceeds limit of " + DEBUG.debug.MAX_NODES);
			JPanel ret = new JPanel(new GridLayout(1,1));
			ret.add(xxx);
			return ret;
		}
		return doCatView(clr, g);
	}

	public static JComponent makeFnViewer(Fn cat, Color src, Color dst) {
		Graph<Pair<String, Color>, Integer> g = buildFromFn(cat, src, dst);
		if (g.getVertexCount() == 0) {
			return new JPanel();
		} else if (g.getVertexCount() > DEBUG.debug.MAX_NODES) {
			FQLTextPanel xxx = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "Graph has " + g.getVertexCount() + " nodes, which exceeds limit of " + DEBUG.debug.MAX_NODES);
			JPanel ret = new JPanel(new GridLayout(1,1));
			ret.add(xxx);
			return ret;
		}
		return doFnView(g);
	}

	public static JComponent doFnView(Graph<Pair<String, Color>, Integer> sgv) {
		try {
			Layout<Pair<String, Color>, Integer> layout = new FRLayout<>(sgv);
			// layout.setSize(new Dimension(600, 200));
			VisualizationViewer<Pair<String, Color>, Integer> vv = new VisualizationViewer<>(layout);
			Transformer<Pair<String, Color>, Paint> vertexPaint = x -> x.second;
			DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
			gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			vv.setGraphMouse(gm);
			gm.setMode(Mode.PICKING);
			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

			vv.getRenderContext().setVertexLabelTransformer(x -> x.first);
			vv.getRenderContext().setEdgeLabelTransformer(x -> "");

			GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
			JPanel ret = new JPanel(new GridLayout(1, 1));
			ret.add(zzz);
			ret.setBorder(BorderFactory.createEtchedBorder());
			return ret;
		} catch (Throwable cnf) {
			cnf.printStackTrace();
			throw new RuntimeException();
		}
	}

	@SuppressWarnings("unchecked")
	public static <X, Y> JComponent doCatView(final Color clr, Graph<X, Y> sgv) {
		Layout<X, Y> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<X, Y> vv = new VisualizationViewer<>(layout);
		Transformer<X, Paint> vertexPaint = x -> clr;
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer ttt = arg0 -> Util.nice(arg0.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(ttt);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <X, Y> JComponent doFNView(Functor fn, JPanel p, final Color clr, Graph<X, Y> sgv) {
		Layout<X, Y> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<X, Y> vv = new VisualizationViewer<>(layout);
		Transformer<X, Paint> vertexPaint = x -> clr;
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer ttt = arg0 -> Util.nice(arg0.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(ttt);

		vv.getPickedVertexState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				vv.getPickedEdgeState().clear();
				X str = ((X) e.getItem());
				Object y = fn.applyO(str);
				p.removeAll();
				if (y instanceof Category) {
					Category ttt = (Category) y;
					JPanel sss = showCat(ttt, getColor(ttt));
					p.add(sss);
				} else if (y instanceof Set) {
					Set ttt = (Set) y;
					JPanel sss = showSet(ttt, getColor(ttt));
					p.add(sss);
				} else if (y instanceof Functor) {
					Functor ttt = (Functor) y;
					JPanel sss = showFtr(ttt, getColor(ttt), null);
					p.add(sss);
				} else {
					String sss = Util.nice(y.toString());
					p.add(new FQLTextPanel(BorderFactory.createEtchedBorder(), null, sss));
				}
				p.revalidate();
			}
		});

		vv.getPickedEdgeState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				vv.getPickedVertexState().clear();
				X str = ((X) e.getItem());
				Object y = fn.applyA(str);
				p.removeAll();
				if (y instanceof Functor) {
					Functor ttt = (Functor) y;
					JPanel sss = showFtr(ttt, getColor(ttt.source), null);
					p.add(sss);
				} else if (y instanceof Fn) {
					Fn ttt = (Fn) y;
					JPanel sss = showFn(ttt, getColor(ttt.source), getColor(ttt.target));
					p.add(sss);
				} else if (y instanceof Transform) {
					Transform ttt = (Transform) y;
					JPanel sss = showTrans(ttt, getColor(ttt.source));
					p.add(sss);
				} else {
					String sss = Util.nice(y.toString());
					p.add(new FQLTextPanel(BorderFactory.createEtchedBorder(), null, sss));
				}
				p.revalidate();
			}
		});

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <X, Y> JComponent doFNView2(Functor fn, JPanel p, final Color clr, Graph<X, Y> sgv,
			Signature sig) {
		Layout<X, Y> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<X, Y> vv = new VisualizationViewer<>(layout);
		Transformer<X, Paint> vertexPaint = x -> clr;
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer ttt = arg0 -> Util.nice(arg0.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(ttt);

		Map<Object, JPanel> map = makeJoined(sig, fn).second;
		vv.getPickedVertexState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				vv.getPickedEdgeState().clear();
				X str = ((X) e.getItem());
				// Object y = fn.applyO(str);
				p.removeAll();
				p.add(map.get(str));
				p.revalidate();
			}
		});

		vv.getPickedEdgeState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				vv.getPickedVertexState().clear();
				X str = ((X) e.getItem());
				// Object y = fn.applyA(str);
				p.removeAll();
				p.add(map.get(str));
				p.revalidate();
			}
		});

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	@SuppressWarnings("unchecked")
	public  JComponent doSchemaView(Color clr, Graph sgv) {
		Layout layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer vv = new VisualizationViewer<>(layout);
		Transformer vertexPaint = x -> clr;
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer ttt = arg0 -> Util.nice(arg0.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(ttt);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}


	public JComponent doElementsView(Color clr, Graph<Pair, Triple> sgv) {
		Layout<Pair, Triple> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Pair, Triple> vv = new VisualizationViewer<>(layout);
		Transformer<Pair, Paint> vertexPaint = x -> clr;
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<Pair, String> ttt1 = arg0 -> Util.nice(arg0.second.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt1);

		Transformer<Triple, String> ttt2 = arg0 -> Util.nice(arg0.first.toString());
		vv.getRenderContext().setEdgeLabelTransformer(ttt2);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}
	
	public JComponent doMappingView(Color clr1, Color clr2, Graph<Pair, Quad> sgv) {
		Layout<Pair, Quad> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Pair, Quad> vv = new VisualizationViewer<>(layout);
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);

		Transformer<Pair, Paint> vertexPaint = x -> x.second.equals("src") ? clr1 : clr2;
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<Pair, String> ttt1 = arg0 -> Util.nice(arg0.first.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt1);

		Transformer<Quad, String> ttt2 = arg0 -> Util.nice(arg0.first.toString());
		vv.getRenderContext().setEdgeLabelTransformer(ttt2);

		float dash[] = { 1.0f };
		Stroke edgeStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				10.0f, dash, 10.0f);
		Stroke bs = new BasicStroke();
		Transformer<Quad, Stroke> edgeStrokeTransformer = x -> x.fourth instanceof Integer ? edgeStroke
				: bs;
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	public JComponent doElements2View(Color clr, Graph<Triple, Quad> sgv) {
		Layout<Triple, Quad> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Triple, Quad> vv = new VisualizationViewer<>(layout);
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);

		Color clr1 = clr.brighter().brighter();
		Color clr2 = clr.darker().darker();

		Transformer<Triple, Paint> vertexPaint = x -> x.third.equals("src") ? clr1 : clr2;
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<Triple, String> ttt1 = arg0 -> Util.nice(arg0.first.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt1);

		Transformer<Quad, String> ttt2 = arg0 -> Util.nice(arg0.first.toString());
		vv.getRenderContext().setEdgeLabelTransformer(ttt2);

		float dash[] = { 1.0f };
		Stroke edgeStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				10.0f, dash, 10.0f);
		Stroke bs = new BasicStroke();
		Transformer<Quad, Stroke> edgeStrokeTransformer = x -> x.fourth instanceof Integer ? edgeStroke
				: bs;
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <X, Y> JComponent doNTView(Transform fn, JPanel p, final Color clr, Graph<X, Y> sgv) {
		//Layout<X, Y> layout = new FRLayout<>(sgv);
		Layout<X, Y> layout = new FRLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<X, Y> vv = new VisualizationViewer<>(layout);
		Transformer<X, Paint> vertexPaint = x -> clr;
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer ttt = arg0 -> Util.nice(arg0.toString());
		vv.getRenderContext().setVertexLabelTransformer(ttt);
		vv.getRenderContext().setEdgeLabelTransformer(ttt);

		vv.getPickedVertexState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				vv.getPickedEdgeState().clear();
				X str = ((X) e.getItem());
				Object y = fn.apply(str);
				p.removeAll();
				if (y instanceof Functor) {
					Functor ttt = (Functor) y;
					JPanel sss = showFtr(ttt, getColor(ttt.source), null);
					p.add(sss);
				} else if (y instanceof Fn) {
					Fn ttt = (Fn) y;
					JPanel sss = showFn(ttt, getColor(ttt.source), getColor(ttt.target));
					p.add(sss);
				} else if (y instanceof Transform) {
					Transform ttt = (Transform) y;
					JPanel sss = showTrans(ttt, getColor(ttt.source));
					p.add(sss);
				} else {
					String sss = Util.nice(y.toString());
					p.add(new FQLTextPanel(BorderFactory.createEtchedBorder(), null, sss));
				}
				p.revalidate();
			}
		});

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	@SuppressWarnings("unchecked")
	Pair<JPanel, Map<Object, JPanel>> makeJoined(Signature<String, String> sig,
			Functor<Object, Object, Set, Fn<Object, Object>> F) {
		Map<Signature.Node, List<Signature<String, String>.Edge>> map = new HashMap<>();
		Map<Object, JPanel> mapX = new HashMap<>();
		for (Signature.Node n : sig.nodes) {
			map.put(n, new LinkedList<>());
		}
		for (Signature.Edge t : sig.edges) {
			map.get(t.source).add(t);
		}

		int x = (int) Math.ceil(Math.sqrt(sig.nodes.size()));
		if (x == 0) {
			return new Pair<>(new JPanel(), new HashMap<>());
		}
		JPanel ret = new JPanel(new GridLayout(x, x));

		for (Signature.Node n : sig.nodes) {
			List<Signature<String, String>.Edge> cols = map.get(n);
			Object[] colNames = new Object[cols.size() + 1];
			colNames[0] = "ID";
			Set set = F.applyO(n);
			Object[][] rowData = new Object[set.size()][cols.size() + 1];
			int j = 0;
			for (Object o : set) {
				rowData[j][0] = o;
				j++;
			}
			int i = 1;
			for (Signature<String, String>.Edge t : cols) {
				colNames[i] = t.name;
				Fn<Object, Object> fn = F.applyA(sig.path(t));
				j = 0;
				for (Object o : set) {
					rowData[j][i] = fn.apply(o);
					j++;
				}
				i++;
			}
			JPanel p = Util.makeTable(BorderFactory.createEtchedBorder(), n + " (" + set.size()
					+ " rows)", rowData, colNames);
			ret.add(p);
			mapX.put(n, p);
		}

		for (Signature<String, String>.Edge t : sig.edges) {
			Object[] colNames = new Object[2];
			colNames[0] = t.source;
			colNames[1] = t.target;
			Set set = F.applyO(t.source);
			Object[][] rowData = new Object[set.size()][2];
			Fn<Object, Object> fn = F.applyA(sig.path(t));
			int j = 0;
			for (Object o : set) {
				rowData[j][0] = o;
				rowData[j][1] = fn.apply(o);
				j++;
			}

			JPanel p = Util.makeTable(BorderFactory.createEtchedBorder(),
					t.name + " (" + set.size() + " rows)", rowData, colNames);
			mapX.put(t.name, p);
		}

		return new Pair<>(ret, mapX);
	}



}
