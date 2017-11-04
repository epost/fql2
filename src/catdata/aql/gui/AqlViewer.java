package catdata.aql.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.LinkedHashMap;
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
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.AqlJs;
import catdata.aql.ColimitSchema;
import catdata.aql.Collage;
import catdata.aql.Comment;
import catdata.aql.Constraints;
import catdata.aql.DP;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Morphism;
import catdata.aql.Pragma;
import catdata.aql.Query;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Semantics;
import catdata.aql.SemanticsVisitor;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.TypeSide;
import catdata.aql.Var;
import catdata.aql.exp.AqlParser;
import catdata.graph.DMG;
import catdata.ide.CodeTextPanel;
import catdata.ide.Split;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

//TODO: aql quoting

//TODO aql replace literal by constant

public final class AqlViewer implements SemanticsVisitor<Unit, JTabbedPane, RuntimeException> { 

	private int maxrows;
	public AqlViewer(int maxrows) {
		this.maxrows = maxrows;
	}
	
	public static String html(Object obj) {
		return obj.toString().replace("\n", "<br>").replace("\t", "&nbsp;");
	}

	public static JComponent view(float time, Semantics s, int maxrows) {
		JTabbedPane ret = new JTabbedPane();
		new AqlViewer(maxrows).visit(ret, s);
		ret.addTab("Text", new CodeTextPanel("", s.toString()));
		ret.addTab("Performance", new CodeTextPanel("",  "Compute time: " + time + " seconds"));
		return ret;
	}

	private static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2> JComponent viewMorphism(Morphism /*<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Sym, Fk2, Att2, Gen2, Sk2>*/ m, AqlJs /*<Ty, Sym>*/ js) {
		CodeTextPanel input = new CodeTextPanel("Input term-in-ctx", "");
		CodeTextPanel output = new CodeTextPanel("Output term-in-ctx", "");

		JButton nf = new JButton("Translate");
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
				Triple<Ctx<Var, Chc<Ty, En1>>, Term<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1>, Term<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1>> 
				z = RawTerm.infer2(y.first, y.second, y.second, m.src(), js);
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

	
	private static <N,E> JComponent viewGraph(DMG<N, E> g) {
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
		gm.setMode(Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<E, String> et = Object::toString;
		Transformer<N, String> vt = Object::toString;
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

	
	private  <Ty, En, Sym, Fk, Att> JComponent viewSchema(Schema<Ty, En, Sym, Fk, Att> schema) {
		Graph<Chc<Ty, En>, Chc<Fk, Att>> sgv = new DirectedSparseMultigraph<>();

		//int i = 0;
		for (En en : schema.ens) {
			///if (i >= maxrows) {
			//	break;
			//}
			sgv.addVertex(Chc.inRight(en));
			//i++;
		}
//		if (i <= maxrows) {
			for (Ty ty : schema.typeSide.tys) {
				sgv.addVertex(Chc.inLeft(ty));
			}
			for (Att att : schema.atts.keySet()) {
				sgv.addEdge(Chc.inRight(att), Chc.inRight(schema.atts.get(att).first), Chc.inLeft(schema.atts.get(att).second));
			}
			for (Fk fk : schema.fks.keySet()) {
				sgv.addEdge(Chc.inLeft(fk), Chc.inRight(schema.fks.get(fk).first), Chc.inRight(schema.fks.get(fk).second));
			}
	//	}

		if (sgv.getVertexCount() == 0) {
			return new JPanel();
		}
		//Layout<Chc<Ty, En>, Chc<Fk, Att>> layout = new KKLayout<>(sgv);
		Layout<Chc<Ty, En>, Chc<Fk, Att>> layout = new FRLayout<>(sgv);
		
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Chc<Ty, En>, Chc<Fk, Att>> vv = new VisualizationViewer<>(layout);
		Transformer<Chc<Ty, En>, Paint> vertexPaint = x -> x.left ? Color.gray : Color.black;
		DefaultModalGraphMouse<Chc<Ty, En>, Chc<Fk, Att>> gm = new DefaultModalGraphMouse<>();
		gm.setMode(Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<Chc<Fk, Att>, String> et = Chc::toStringMash;
		Transformer<Chc<Ty, En>, String> vt = Chc::toStringMash;
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

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> JComponent viewDP(DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp, Collage /*<Ty, En, Sym, Fk, Att, Gen, Sk>*/ col, AqlJs /*<Ty, Sym>*/ js) {
		CodeTextPanel input = new CodeTextPanel("Input (either equation-in-ctx or term-in-ctx)", "");
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

		print.addActionListener(x -> output.setText(dp.toStringProver()));
		eq.addActionListener(x -> {
			try {
				Triple<List<Pair<String, String>>, RawTerm, RawTerm> y = AqlParser.parseEq(input.getText());
				Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> z 
				= RawTerm.infer2(y.first, y.second, y.third, col, js);
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
	
	
		private static <Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> JComponent viewTransform(Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t) {
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
					Term<Ty, En, Sym, Fk, Att, Gen1, Sk1> a = t.src().algebra().reprT_protected(Term.Sk(y1));
					row[0] = t.src().algebra().printY(y1); 
					Term<Ty, En, Sym, Fk, Att, Gen2, Sk2> y0 = t.trans(a); //t.dst().algebra().intoY(t.reprT(y1));
					row[1] = y0.toString(); //t.dst().algebra().pr, Util.voidFn()); //TODO aql viewer printing revisit
					data[i] = row;
					i++;
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), ty + " (" + n + ")", data, header.toArray())); // TODO: aql boldify attributes
			}

			return Util.makeGrid(list);
		}
	
	
	

		private <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> Map<Ty, Object[][]> makeTyTables(Map<Ty, Set<Y>> m, Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg) {
			Map<Ty, Object[][]> ret = new LinkedHashMap<>();

			List<Ty> tys = Util.alphabetical(alg.schema().typeSide.tys);

			
			for (Ty ty : tys) {
				if (!m.containsKey(ty)) {
					continue;
				}
				int n = Integer.min(maxrows, m.get(ty).size());

				Object[][] data = new Object[n][1];
				int i = 0;
				for (Y y : Util.alphabetical(m.get(ty))) {
					Object[] row = new Object[1];
					row[0] = alg.printY(y);
					data[i] = row;
					i++;
					if (i == n) {
						break;
					}
				}
				ret.put(ty,  data);
			}
			return ret;
		}

		public  <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>  Map<En, Pair<List<String>, Object[][]>> makeEnTables(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg) {
			Map<En, Pair<List<String>, Object[][]>> ret = new LinkedHashMap<>();

			List<En> ens = Util.alphabetical(alg.schema().ens);
		
			for (En en : ens) {
				List<Att> atts0 = Util.alphabetical(alg.schema().attsFrom(en));
				List<Fk> fks0 = Util.alphabetical(alg.schema().fksFrom(en));

				List<String> header = Util.append(Util.toString(atts0), Util.toString(fks0));
				header.add(0, "ID");
				int n = Integer.min(maxrows, alg.en(en).size());
				Object[][] data = new Object[n][];
				int i = 0;
				List<X> lll = new LinkedList<>(alg.en(en));
				lll.sort((x, y) -> Util.AlphabeticalComparator.compare(alg.printX(x), alg.printX(y)));
				for (X x : lll) {
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
					if (i == n) {
						break;
					}
				}
				
				ret.put(en, new Pair<>(header, data));
			}
					
			return ret;
		}
		
	private <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>  Component viewAlgebra(Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> alg) {
		List<JComponent> list = new LinkedList<>();

		Map<En,Pair<List<String>,Object[][]>> entables = makeEnTables(alg);
		Map<Ty, Set<Y>> m = Util.revS(alg.talg().sks.map);
		Map<Ty,Object[][]> tytables = makeTyTables(m, alg);
		
		for (En en : entables.keySet()) {
			Pair<List<String>,Object[][]> x = entables.get(en);
			String str;
			if (x.second.length < alg.en(en).size()) {
				str = en + " (" + x.second.length + " of " + alg.en(en).size() + ")";
			} else {
				str = en + " (" + x.second.length + ")";
			}
			JPanel p = Util.makeBoldHeaderTable(Util.toString(alg.schema().attsFrom(en)), BorderFactory.createEmptyBorder(), str, x.second, x.first.toArray(new String[x.first.size()]));
			list.add(p);
		}
		
		List<String> header = Util.singList("ID")	;	
		
		for (Ty ty : tytables.keySet()) {
			if (!m.containsKey(ty)) {
				continue;
			}
			Object[][] arr = tytables.get(ty);
			String str;
			if (arr.length < m.get(ty).size()) {
				str = ty + " (" + arr.length + " of " + m.get(ty).size() + ")";
			} else {
				str = ty + " (" + arr.length + ")";
			}

			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), str, arr, header.toArray())); // TODO: aql boldify attributes
		}

		return Util.makeGrid(list);
	}

	@Override
	public <T, C> Unit visit(JTabbedPane ret, TypeSide<T, C> T)  {
		ret.addTab("DP", viewDP(T.semantics, T.collage(), T.js));
		return new Unit();
	}

	@Override
	public <Ty, En, Sym, Fk, Att> Unit visit(JTabbedPane ret, Schema<Ty, En, Sym, Fk, Att> S)  {
		ret.addTab("Graph", viewSchema(S));
//		ret.add(viewSchema2(schema), "Graph2");
		ret.addTab("DP", viewDP(S.dp(), S.collage(), S.typeSide.js));
	//	ret.add(new CodeTextPanel("", schema.collage().toString()), "Temp");
		return new Unit();
	}

	@Override
	public <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> Unit visit(JTabbedPane ret, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I)  {
		ret.addTab("Tables", viewAlgebra(I.algebra()));
		ret.addTab("Type Algebra", new CodeTextPanel("", I.algebra().talg().toString()));
		ret.addTab("DP", viewDP(I.dp(), I.collage(), I.schema().typeSide.js));
		return new Unit();
	}

	@Override
	public <Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> Unit visit(JTabbedPane ret, Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h)  {
		ret.addTab("Tables", viewTransform(h));
		return new Unit();
	}

	@Override
	public Unit visit(JTabbedPane ret, Pragma P) {
		return new Unit();
	}

	@Override
	public Unit visit(JTabbedPane ret, Comment P) {
		return new Unit();
	}

	@Override
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Unit visit(JTabbedPane ret, Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q) {
		/* try {
			Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q = Q.unnest();
			JComponent comp = makeQueryPanel(q);
			ret.add("SQL", comp); 
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.add("SQL", new CodeTextPanel("Exception", ex.getMessage()));
		} */
		return new Unit();
	}
/*
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> JComponent makeQueryPanel(Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> q) {
		try {
			List<String> l = q.unnest().toSQLViews("input", "output", "id");
			return new CodeTextPanel("", Util.sep(l, ";\n\n"));
		} catch (Exception ex) {
			return new CodeTextPanel("", ex.getMessage());
		}
	} */
	
	@Override
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Unit visit(JTabbedPane ret, Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> M) {
		ret.addTab("Translate", viewMorphism(M.semantics(), M.src.typeSide.js));
		return new Unit();
	}

	@Override
	public <N, e> Unit visit(JTabbedPane ret, catdata.aql.Graph<N, e> G) {
		ret.add("Graph", viewGraph(G.dmg));
		return new Unit();
	}

	@SuppressWarnings("unused")
	@Override
	public <N, E0, Ty, En, Sym, Fk, Att> Unit visit(JTabbedPane arg, ColimitSchema<N, Ty, En, Sym, Fk, Att> S) throws RuntimeException {
		return new Unit(); //TODO aql
	}

	@Override
	public <Ty, En, Sym, Fk, Att> Unit visit(JTabbedPane arg, Constraints<Ty, En, Sym, Fk, Att> S) throws RuntimeException {
		return new Unit(); 
	}

	///////////////////////////////////////////////////////
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
