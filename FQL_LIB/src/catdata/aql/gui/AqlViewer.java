package catdata.aql.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.commons.collections15.Transformer;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.Algebra;
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

//TODO: quoting

//TODO random intances

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
			viewDP(typeSide.semantics, typeSide.collage(), ret);
			break;
		case SCHEMA:
			@SuppressWarnings("unchecked")
			Schema<Object, Object, Object, Object, Object> schema = (Schema<Object, Object, Object, Object, Object>) obj;
			viewDP(schema.dp(), schema.collage(), ret);
			ret.add(viewSchema(schema), "Graph");
			break;
		case INSTANCE:
			@SuppressWarnings("unchecked")
			Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object> instance = (Instance<Object, Object, Object, Object, Object, Object, Object, Object, Object>) obj;
			viewDP(instance.dp(), instance.collage(), ret);
			Tables<Object, Object, Object, Object, Object, Object, Object> tables = new Tables<>(instance.algebra());
			ret.add(tables.makeGui(), "Tables");
			ret.add(new CodeTextPanel("", tables.toString()), "Algebra");
			break;
		case MAPPING:
			@SuppressWarnings("unchecked")
			Mapping<Object, Object, Object, Object, Object, Object, Object, Object> mapping = (Mapping<Object, Object, Object, Object, Object, Object, Object, Object>) obj;
			viewMorphism(mapping.semantics(), ret);
			break;
		case TRANSFORM:
			@SuppressWarnings("unchecked")
			Transform  <Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> transform = (Transform<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) obj;
			TablesTrans<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> tablesTrans = new TablesTrans<>(transform);
			ret.add(tablesTrans.makeGui(), "Tables");
//			ret.add(new CodeTextPanel("", tablesTrans.toString()), "Algebra");

			break;
		case PRAGMA:
			// viewPragma(obj, ret);
			break;
		case QUERY:
			// viewQuery(obj, ret);
			break;
		default:
			throw new RuntimeException("Anomaly: please report");
		}

		return ret;

	}

	private static <Ty, En1, Sym1, Fk1, Att1, Gen1, Sk1, En2, Sym2, Fk2, Att2, Gen2, Sk2> void viewMorphism(Morphism<Ty, En1, Sym1, Fk1, Att1, Gen1, Sk1, En2, Sym2, Fk2, Att2, Gen2, Sk2> m, JTabbedPane ret) {
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
				Pair<List<Pair<String, String>>, RawTerm> y = AqlParser.parseTerm(input.getText());
				Triple<Ctx<Var, Chc<Ty, En1>>, Term<Ty, En1, Sym1, Fk1, Att1, Gen1, Sk1>, Term<Ty, En1, Sym1, Fk1, Att1, Gen1, Sk1>> z = RawTerm.infer2(y.first, y.second, y.second, m.src());
				Pair<Ctx<Var, Chc<Ty, En2>>, Term<Ty, En2, Sym2, Fk2, Att2, Gen2, Sk2>> a = m.translate(z.first, z.second);
				output.setText(a.first.toString() + a.second);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});

		ret.addTab("Trans", main);
	}
	
	private static <Ty, En, Sym, Fk, Att> Component viewSchema(Schema<Ty, En, Sym, Fk, Att> schema) {
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
		Layout<Chc<Ty, En>, Chc<Fk, Att>> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Chc<Ty, En>, Chc<Fk, Att>> vv = new VisualizationViewer<>(layout);
		Transformer<Chc<Ty, En>, Paint> vertexPaint = x -> x.left ? null : Color.black;
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
		return ret;
	}	

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> void viewDP(DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, JTabbedPane ret) {
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
				Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> z = RawTerm.infer2(y.first, y.second, y.third, col);
				boolean isEq = dp.eq(z.first, z.second, z.third);
				output.setText(Boolean.toString(isEq));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});
		nf.addActionListener(x -> {
			try {
				Pair<List<Pair<String, String>>, RawTerm> y = AqlParser.parseTerm(input.getText());
				Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> z = RawTerm.infer2(y.first, y.second, y.second, col);
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> w = dp.nf(z.first, z.second);
				output.setText(w.toString());
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});

		ret.addTab("DP", main);
	}
	
	public static class TablesTrans<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> {
	
		public final Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t;
		
		public Tables<Ty, En, Sym, Fk, Att, Gen1, Sk1> src; 
		public Tables<Ty, En, Sym, Fk, Att, Gen2, Sk2> dst;
		
		public TablesTrans(Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
			this.t = t;
			src = new Tables<>(t.src().algebra());
			dst = new Tables<>(t.dst().algebra()); //TODO aql recomputes
		}
	
		public Component makeGui() {
			List<JComponent> list = new LinkedList<>();

			List<En> ens = Util.alphabetical(src.carriers.keySet());
			List<Ty> tys = Util.alphabetical(src.alg.schema().typeSide.tys);
			
			for (En en : ens) {
				List<String> header = new LinkedList<>();
				header.add("Input");
				header.add("Output");
				int n = src.carriers.get(en).size();
				Object[][] data = new Object[n][2];
				int i = 0;
				for (Term<Void, En, Void, Fk, Void, Gen1, Void> x : Util.alphabetical(src.carriers.get(en))) {
					Object[] row = new Object[2];
					row[0] = x.toString(Util.voidFn(), src.alg::printGen);
					Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> y = t.trans(x.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn()));
					row[1] = y.toString(dst.alg::printSk, dst.alg::printGen);
					data[i] = row;
					i++;
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), en + " (" + n + ") rows", data, header.toArray())); // TODO: aql boldify attributes
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
				for (Y1 sk : z.get(ty)) {
					Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> x = t.src().algebra().reprT(Term.Sk(sk));
					Object[] row = new Object[2];
					row[0] = x.toString(src.alg::printSk, src.alg::printGen);
					Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> y = t.trans(x); 
					row[1] = y.toString(dst.alg::printSk, dst.alg::printGen);
					data[i] = row;
					i++;
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), ty + " (" + n + ") rows", data, header.toArray())); // TODO: aql boldify attributes
			}

			return Util.makeGrid(list);
		}
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

	public static class Tables<Ty, En, Sym, Fk, Att, Gen, Sk> {
		public Map<En, Collection<Term<Void, En, Void, Fk, Void, Gen, Void>>> carriers = new HashMap<>();
		public Map<Fk, Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Void, En, Void, Fk, Void, Gen, Void>>> fks = new HashMap<>();
		public Map<Att, Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> atts = new HashMap<>();

		public Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, ?, ?> alg;

		public <X, Y> Tables(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg) {
			this.alg = alg;
			for (En en : alg.schema().ens) {
				carriers.put(en, alg.en(en).stream().map(alg::repr).collect(Collectors.toList()));
			}

			for (Fk fk : alg.schema().fks.keySet()) {
				Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Void, En, Void, Fk, Void, Gen, Void>> m = new HashMap<>();
				for (X x : alg.en(alg.schema().fks.get(fk).first)) {
					m.put(alg.repr(x), alg.repr(alg.fk(fk, x)));
				}
				fks.put(fk, m);
			}

			for (Att att : alg.schema().atts.keySet()) {
				Map<Term<Void, En, Void, Fk, Void, Gen, Void>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> m = new HashMap<>();
				for (X x : alg.en(alg.schema().atts.get(att).first)) {
					m.put(alg.repr(x), alg.reprT(alg.att(att, x)));
				}
				atts.put(att, m);
			}
		}

		@Override
		public String toString() {
			String ret = "----- entity algebra\n\n";

			ret = "carriers\n\t";
			ret += Util.sep(alg.schema().ens.stream().map(x -> x + " -> {" + Util.sep(carriers.get(x), ", ") + "}").collect(Collectors.toList()), "\n\t");

			ret += "\n\nforeign keys";
			for (Fk fk : alg.schema().fks.keySet()) {
				ret += "\n\t" + fk + " -> {" + Util.sep(fks.get(fk).keySet().stream().map(x -> "(" + x + ", " + fks.get(fk).get(x) + ")").collect(Collectors.toList()), ", ") + "}";
			}

			ret += "\n\nattributes";
			for (Att att : alg.schema().atts.keySet()) {
				ret += "\n\t" + att + " -> {" + Util.sep(atts.get(att).keySet().stream().map(x -> "(" + x + ", " + atts.get(att).get(x) + ")").collect(Collectors.toList()), ", ") + "}";
			}

			ret += "\n\n----- type algebra\n\n";
			ret += alg.talg().toString();

			ret += "\n\n----- prover\n\n";
			ret += alg.toStringProver();

			return ret;
		}

		public Component makeGui() {
			List<JComponent> list = new LinkedList<>();

			List<En> ens = Util.alphabetical(carriers.keySet());

			for (En en : ens) {
				List<Att> atts0 = Util.alphabetical(alg.schema().attsFrom(en));
				List<Fk> fks0 = Util.alphabetical(alg.schema().fksFrom(en));

				List<String> header = Util.append(Util.toString(atts0), Util.toString(fks0));
				header.add(0, "ID");
				int n = carriers.get(en).size();
				Object[][] data = new Object[n][];
				int i = 0;
				for (Term<Void, En, Void, Fk, Void, Gen, Void> x : Util.alphabetical(carriers.get(en))) {
					List<String> row = new LinkedList<>();
					row.add(x.toString(Util.voidFn(), alg::printGen));
					for (Att att0 : atts0) {
						row.add(atts.get(att0).get(x).toString(alg::printSk, alg::printGen));
					}
					for (Fk fk0 : fks0) {
						row.add(fks.get(fk0).get(x).toString(Util.voidFn(), alg::printGen));
					}
					data[i] = row.toArray();
					i++;
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), en + " (" + n + ") rows", data, header.toArray())); // TODO: aql boldify attributes
			}

			return Util.makeGrid(list);
		}
	}

}
