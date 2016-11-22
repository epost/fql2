package catdata.aql.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.commons.collections15.Transformer;

import catdata.Chc;
import catdata.DMG;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.AqlJs;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.DP;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Morphism;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.TypeSide;
import catdata.aql.Var;
import catdata.aql.exp.AqlParser;
import catdata.aql.exp.Kind;
import catdata.ide.CodeTextPanel;
import catdata.ide.Split;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

//TODO: aql quoting

//TODO aql random instances

//TODO aql replace literal by constant

public final class AqlViewer {

	public static String html(Object obj) {
		return obj.toString().replace("\n", "<br>").replace("\t", "&nbsp;");
	}

	public static JComponent view(Kind kind, Object obj) {
		JTabbedPane ret = new JTabbedPane();

		ret.add(new CodeTextPanel("", obj.toString()), "Text");

		switch (kind) {
		case TYPESIDE:
			@SuppressWarnings("unchecked")
			TypeSide<Object, Object> typeSide = (TypeSide<Object, Object>) obj;
			ret.add(viewDP(typeSide.semantics, typeSide.collage(), typeSide.js), "DP");
			break;
		case SCHEMA:
			@SuppressWarnings("unchecked")
			Schema<Object, Object, Object, Object, Object> schema = (Schema<Object, Object, Object, Object, Object>) obj;
			ret.add(viewSchema(schema), "Graph");
	//		ret.add(viewSchema2(schema), "Graph2");
			ret.add(viewDP(schema.dp(), schema.collage(), schema.typeSide.js), "DP");
		//	ret.add(new CodeTextPanel("", schema.collage().toString()), "Temp");
			break;
		case INSTANCE:
			@SuppressWarnings("unchecked")
			Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> instance = (Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object>) obj;
			ret.add(viewAlgebra(instance.algebra()), "Tables");
			ret.add(new CodeTextPanel("", instance.algebra().toString()), "Algebra");
			ret.add(viewDP(instance.dp(), instance.collage(), instance.schema().typeSide.js), "DP");
			break;
		case MAPPING:
			@SuppressWarnings("unchecked")
			Mapping<Object, Object, Object, Object, Object, Object, Object, Object> mapping = (Mapping<Object, Object, Object, Object, Object, Object, Object, Object>) obj;
			ret.add(viewMorphism(mapping.semantics(), mapping.src.typeSide.js), "Translate");
			break;
		case TRANSFORM:
			@SuppressWarnings("unchecked")
			Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> transform = (Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) obj;
			ret.add(viewTransform(transform), "Algebra");
		break;
		case PRAGMA:
			/* Pragma pragma = (Pragma) obj;
			Optional<JComponent> comp = viewPragma(pragma);
			if (comp.isPresent()) {
				ret.add(comp.get(), "Graphical");
			} */
			break;
		case QUERY:
			// viewQuery(obj, ret);
			break;
			
		case GRAPH:
			@SuppressWarnings("unchecked") DMG<Object, Object> dmg = (DMG<Object, Object>) obj;
			ret.add(viewGraph(dmg), "Graph");
			break;
		default:
			throw new RuntimeException("Anomaly: please report");
		}

		return ret;

	}

	public static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2> JComponent viewMorphism(Morphism<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Sym, Fk2, Att2, Gen2, Sk2> m, AqlJs<Ty,Sym> js) {
		CodeTextPanel input = new CodeTextPanel("Input", "");
		CodeTextPanel output = new CodeTextPanel("Output", "");

		JButton nf = new JButton("Normalize Term-in-ctx");
		JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
		buttonPanel.add(nf);

		Split split = new Split(.5, JSplitPane.VERTICAL_SPLIT); // TODO: aql does not position correctly
		split.add(input);
		split.add(output);

		JPanel main = new JPanel(new BorderLayout());
		main.add(split, BorderLayout.CENTER);
		main.add(buttonPanel, BorderLayout.NORTH);

		nf.addActionListener(x -> {
			try {
				Pair<List<Pair<String, String>>, RawTerm> y = AqlParser.parseTermInCtx(input.getText());
				Triple<Ctx<Var, Chc<Ty, En1>>, Term<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1>, Term<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1>> z = RawTerm.infer2(y.first, y.second, y.second, m.src(), js);
				Pair<Ctx<Var, Chc<Ty, En2>>, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> a = m.translate(z.first, z.second);
				output.setText(a.first.toString() + a.second);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});

		return main;
	}
	/*
	public static Optional<JComponent> viewPragma(Pragma p) {
		if (p instanceof ToCsvPragmaTransform) {
			return Optional.empty();
		} else if (p instanceof ToCsvPragmaInstance) {
			return Optional.of(viewPragmaToCsvInstance((ToCsvPragmaInstance)p));
		}
		throw new RuntimeException("Anomaly: please report");
	} 
	
	private static JComponent viewPragmaToCsvInstance(ToCsvPragmaInstance p) {
		return new JPanel();
	} */

	
	public static <N,E> JComponent viewGraph(DMG<N,E> g) {
		Graph<N, E> sgv = new DirectedSparseMultigraph<>();

		for (N n : g.nodes) {
			sgv.addVertex(n);
		}
		for (E e : g.edges.keySet()) {
			sgv.addEdge(e, g.edges.get(e).first, g.edges.get(e).second);
		}
		
		if (sgv.getVertexCount() == 0) {
			return new JPanel();
		}
		Layout<N,E> layout = new FRLayout<>(sgv);
		
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<N, E> vv = new VisualizationViewer<>(layout);
		Transformer<N, Paint> vertexPaint = x -> Color.black;
		DefaultModalGraphMouse<N, E> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<E, String> et = x -> x.toString();
		Transformer<N, String> vt = x -> x.toString();
		vv.getRenderContext().setEdgeLabelTransformer(et);
		vv.getRenderContext().setVertexLabelTransformer(vt);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		
		vv.getRenderContext().setLabelOffset(16);
		vv.setBackground(Color.white);
		
		return ret;
	}	

	
	public static <Ty, En, Sym, Fk, Att> JComponent viewSchema(Schema<Ty, En, Sym, Fk, Att> schema) {
		Graph<Chc<Ty, En>, Chc<Fk, Att>> sgv = new DirectedSparseMultigraph<>();

		for (En en : schema.ens) {
			sgv.addVertex(Chc.inRight(en));
		}
		for (Ty ty : schema.typeSide.tys) {
			sgv.addVertex(Chc.inLeft(ty));
		}
		for (Att att : schema.atts.keySet()) {
			sgv.addEdge(Chc.inRight(att), Chc.inRight(schema.atts.get(att).first), Chc.inLeft(schema.atts.get(att).second));
		}
		for (Fk fk : schema.fks.keySet()) {
			sgv.addEdge(Chc.inLeft(fk), Chc.inRight(schema.fks.get(fk).first), Chc.inRight(schema.fks.get(fk).second));
		}

		if (sgv.getVertexCount() == 0) {
			return new JPanel();
		}
		//Layout<Chc<Ty, En>, Chc<Fk, Att>> layout = new KKLayout<>(sgv);
		Layout<Chc<Ty, En>, Chc<Fk, Att>> layout = new FRLayout<>(sgv);
		
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Chc<Ty, En>, Chc<Fk, Att>> vv = new VisualizationViewer<>(layout);
		Transformer<Chc<Ty, En>, Paint> vertexPaint = x -> x.left ? Color.gray : Color.black;
		DefaultModalGraphMouse<Chc<Ty, En>, Chc<Fk, Att>> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<Chc<Fk, Att>, String> et = x -> x.toStringMash();
		Transformer<Chc<Ty, En>, String> vt = x -> x.toStringMash();
		vv.getRenderContext().setEdgeLabelTransformer(et);
		vv.getRenderContext().setVertexLabelTransformer(vt);

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		
		vv.getRenderContext().setLabelOffset(16);
		//vv.getRenderContext().set
		vv.setBackground(Color.white);
		
		//vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		//AbstractEdgeShapeTransformer aesf = (AbstractEdgeShapeTransformer)
			//	vv.getRenderContext().getEdgeShapeTransformer();
			//	aesf.		
		//		aesf.setControlOffsetIncrement(0);
		
		return ret;
	}	
	
	/*public static <Ty, En, Sym, Fk, Att> JComponent viewSchema2(Schema<Ty, En, Sym, Fk, Att> schema) {
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		Ctx<Chc<En,Ty>, Object> nodes = new Ctx<>();
		for (En en : schema.ens) {
			Object v1 = graph.insertVertex(parent, null, en.toString(), 20, 20, 80, 30);
			nodes.put(Chc.inLeft(en), v1);
		}
		for (Ty ty : schema.typeSide.tys) {
			Object v1 = graph.insertVertex(parent, null, ty.toString(), 20, 20, 80, 30);
			nodes.put(Chc.inRight(ty), v1);
		}
		for (Att att : schema.atts.keySet()) {
			graph.insertEdge(parent, null, att.toString(), nodes.get(Chc.inLeft(schema.atts.get(att).first)), nodes.get(Chc.inRight(schema.atts.get(att).second)));
		}
		for (Fk fk : schema.fks.keySet()) {
			graph.insertEdge(parent, null, fk.toString(), nodes.get(Chc.inLeft(schema.fks.get(fk).first)), nodes.get(Chc.inLeft(schema.fks.get(fk).second)));
		}
		mxGraphLayout layout = new mxOrganicLayout(graph); // or whatever layouting algorithm
		layout.execute(parent);
		
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		
		//  Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
		 // style.put(mxConstants.STYLE_ROUNDED, true);
		//  style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION);
		
		return graphComponent;
	}	*/

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> JComponent viewDP(DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlJs<Ty,Sym> js) {
		CodeTextPanel input = new CodeTextPanel("Input", "");
		CodeTextPanel output = new CodeTextPanel("Output", "");

		JButton eq = new JButton("Decide Equation-in-ctx");
		JButton nf = new JButton("Normalize Term-in-ctx");
		/*
		 * if (!dp.hasNFs()) { nf.setEnabled(false); }
		 */
		JButton print = new JButton("Show Info");
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		buttonPanel.add(eq);
		buttonPanel.add(nf);
		buttonPanel.add(print);

		Split split = new Split(.5, JSplitPane.VERTICAL_SPLIT); // TODO: aql does not position correctly
		split.add(input);
		split.add(output);

		JPanel main = new JPanel(new BorderLayout());
		main.add(split, BorderLayout.CENTER);
		main.add(buttonPanel, BorderLayout.NORTH);

		print.addActionListener(x -> {
			output.setText(dp.toString());
		});
		eq.addActionListener(x -> {
			try {
				Triple<List<Pair<String, String>>, RawTerm, RawTerm> y = AqlParser.parseEq(input.getText());
				Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> z = RawTerm.infer2(y.first, y.second, y.third, col, js);
				boolean isEq = dp.eq(z.first, z.second, z.third);
				output.setText(Boolean.toString(isEq));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});
		nf.addActionListener(x -> {
			try {
				Pair<List<Pair<String, String>>, RawTerm> y = AqlParser.parseTermInCtx(input.getText());
				Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> z = RawTerm.infer2(y.first, y.second, y.second, col, js);
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> w = dp.nf(z.first, z.second);
				output.setText(w.toString());
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});

		return main;
	}
	
	
		public static <Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> JComponent viewTransform(Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			List<JComponent> list = new LinkedList<>();

			List<En> ens = Util.alphabetical(t.src().schema().ens);
			List<Ty> tys = Util.alphabetical(t.src().schema().typeSide.tys);
			
			for (En en : ens) {
				List<String> header = new LinkedList<>();
				header.add("Input");
				header.add("Output");
				int n = t.src().algebra().en(en).size();
				Object[][] data = new Object[n][2];
				int i = 0;
				for (X1 x1 : Util.alphabetical(t.src().algebra().en(en))) {
					Object[] row = new Object[2];
					row[0] = t.src().algebra().printX(x1);
					X2 x2 = t.repr(x1);
					row[1] = t.dst().algebra().printX(x2); 
					data[i] = row;
					i++;
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), en + " (" + n + ")", data, header.toArray())); // TODO: aql boldify attributes
			}
			Map<Ty,Set<Y1>> z = Util.revS(t.src().algebra().talg().sks.map);
			for (Ty ty : tys) {
				List<String> header = new LinkedList<>();
				header.add("Input");
				header.add("Output");
				if (!z.containsKey(ty)) {
					continue;
				}
				int n = z.get(ty).size();
				Object[][] data = new Object[n][2];
				int i = 0;
				for (Y1 y1 : z.get(ty)) {
					Object[] row = new Object[2];
					row[0] = t.src().algebra().printY(y1); 
					Term<Ty, Void, Sym, Void, Void, Void, Y2> y0 = t.dst().algebra().intoY(t.reprT(y1));
					row[1] = y0.toString(t.dst().algebra()::printY, Util.voidFn());
					data[i] = row;
					i++;
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), ty + " (" + n + ")", data, header.toArray())); // TODO: aql boldify attributes
			}

			return Util.makeGrid(list);
		}
	
	
	

	/*
	 * static class Hyperactive implements HyperlinkListener {
	 * 
	 * public void hyperlinkUpdate(HyperlinkEvent e) { if (e.getEventType() ==
	 * HyperlinkEvent.EventType.ACTIVATED) { JEditorPane pane = (JEditorPane)
	 * e.getSource(); if (e instanceof HTMLFrameHyperlinkEvent) {
	 * HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e; HTMLDocument
	 * doc = (HTMLDocument)pane.getDocument();
	 * doc.processHTMLFrameHyperlinkEvent(evt); } else { try {
	 * pane.setPage(e.getURL()); } catch (Throwable t) { t.printStackTrace(); }
	 * } } } }
	 */

	public static <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>  Component viewAlgebra(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg) {
		List<JComponent> list = new LinkedList<>();

		List<En> ens = Util.alphabetical(alg.schema().ens);
		List<Ty> tys = Util.alphabetical(alg.schema().typeSide.tys);

		for (En en : ens) {
			List<Att> atts0 = Util.alphabetical(alg.schema().attsFrom(en));
			List<Fk> fks0 = Util.alphabetical(alg.schema().fksFrom(en));

			List<String> header = Util.append(Util.toString(atts0), Util.toString(fks0));
			header.add(0, "ID");
			int n = alg.en(en).size();
			Object[][] data = new Object[n][];
			int i = 0;
			for (X x : Util.alphabetical(alg.en(en))) {
				List<Object> row = new LinkedList<>();
				row.add(alg.printX(x));
				for (Att att0 : atts0) {
					row.add(alg.att(att0, x).toString(alg::printY, Util.voidFn()));
				}
				for (Fk fk0 : fks0) {
					row.add(alg.printX(alg.fk(fk0, x)));
				}
				data[i] = row.toArray();
				i++;
			}
		
				
			JPanel p = Util.makeBoldHeaderTable(Util.toString(atts0), BorderFactory.createEmptyBorder(), en + " (" + n + ")", data, header.toArray(new String[header.size()]));
			list.add(p);
		}
				
		Map<Ty, Set<Y>> m = Util.revS(alg.talg().sks.map);
		for (Ty ty : tys) {
			if (!m.containsKey(ty)) {
				continue;
			}
			List<String> header = Util.singList("ID");
			int n = m.get(ty).size();
			Object[][] data = new Object[n][1];
			int i = 0;
			for (Y x : Util.alphabetical(m.get(ty))) {
				Object[] row = new Object[1];
				row[0] = alg.printY(x);				
				data[i] = row;
				i++;
			}
			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), ty + " (" + n + ")", data, header.toArray())); // TODO: aql boldify attributes
		}

		return Util.makeGrid(list);
	}


}
