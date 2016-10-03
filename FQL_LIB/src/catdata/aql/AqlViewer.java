package catdata.aql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
			@SuppressWarnings("unchecked") TypeSide<Object,Object> typeSide = (TypeSide<Object,Object>) obj;
			viewDP(typeSide.semantics(), typeSide.collage(), ret);
			break;
		case SCHEMA:
			@SuppressWarnings("unchecked") Schema<Object,Object,Object,Object,Object> schema = (Schema<Object,Object,Object,Object,Object>) obj;
			viewDP(schema.semantics(), schema.collage(), ret);
			ret.add(viewSchema(schema), "Graph");
			break;
		case INSTANCE:
			@SuppressWarnings("unchecked") Instance<Object,Object,Object,Object,Object,Object,Object> instance = (Instance<Object,Object,Object,Object,Object,Object,Object>) obj;
			viewDP(instance.semantics(), instance.collage(), ret);
			ret.add(tables(instance.semantics()), "Tables");			
			ret.add(new CodeTextPanel("", instance.semantics().talgToString()), "TAlg");
			break;
		case MAPPING:
			@SuppressWarnings("unchecked") Mapping<Object, Object,Object,Object,Object,Object,Object,Object> mapping = (Mapping<Object,Object,Object,Object,Object,Object,Object,Object>) obj;
			viewMorphism(mapping.semantics(), ret);
			break;
		case TRANSFORM:
			@SuppressWarnings("unchecked") Transform<Object, Object, Object,Object,Object,Object,Object,Object,Object> transform = (Transform<Object, Object,Object,Object,Object,Object,Object,Object,Object>) obj;
			viewMorphism(transform.semantics(), ret);
			break;
		case PRAGMA:
		//	viewPragma(obj, ret);
			break;
		case QUERY:
		//	viewQuery(obj, ret);
			break;
		default:
			throw new RuntimeException("Anomaly: please report");			
		} 
		
		return ret;

	}

	
	
	private static <Ty,En,Sym,Fk,Att> Component viewSchema(Schema<Ty,En,Sym,Fk,Att> schema) {
		Graph<Chc<Ty,En>, Chc<Fk,Att>> sgv = new DirectedSparseMultigraph<>();

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
		Layout<Chc<Ty,En>, Chc<Fk,Att>> layout = new FRLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<Chc<Ty,En>, Chc<Fk,Att>> vv = new VisualizationViewer<>(layout);
		Transformer<Chc<Ty,En>, Paint> vertexPaint = x -> x.left ? null : Color.black;
		DefaultModalGraphMouse<Chc<Ty,En>, Chc<Fk,Att>> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Transformer<Chc<Fk,Att>, String> et = x -> x.toStringMash();
		Transformer<Chc<Ty,En>, String> vt = x -> x.toStringMash();
		vv.getRenderContext().setEdgeLabelTransformer(et);
		vv.getRenderContext().setVertexLabelTransformer(vt);

	
		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	private static <Ty,En,Sym,Fk,Att,Gen,Sk,X> Component tables(Algebra<Ty,En,Sym,Fk,Att,Gen,Sk,X> alg) {
		List<JComponent> list = new LinkedList<>();
		
//		System.out.println(alg.toString());
		List<En> ens = alphabetical(alg.getTables().carriers.keySet());
		
		for (En en : ens) {
			List<Att> atts = alphabetical(alg.schema().attsFrom(en));
			List<Fk> fks = alphabetical(alg.schema().fksFrom(en));

			List<String> header = Util.append(Util.toString(atts), Util.toString(fks)); 
			header.add(0, "ID");
			int n = alg.getTables().carriers.get(en).size();
			Object[][] data = new Object[n][];
			int i = 0;
			for (Term<Ty,En,Sym,Fk,Att,Gen,Sk> x : alphabetical(alg.getTables().carriers.get(en))) {
				List<String> row = new LinkedList<>();
				row.add(x.toString());
				for (Att att0 : atts) {
					row.add(alg.toString(alg.getTables().atts.get(att0).get(x)));
				}
				for (Fk fk0 : fks) {
					row.add(alg.getTables().fks.get(fk0).get(x).toString());
				}
				data[i] = row.toArray();
				i++;
//				System.out.`(Arrays.toString(data[i]));
//				System.out.println(Arrays.toString(header.toArray()));
			}
			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), en + " (" + n + ") rows", data, header.toArray())); //TODO: boldify attributes
		}
		
		
		return Util.makeGrid(list);
	}

	private static <X> List<X> alphabetical(Collection<X> xs) {
		List<X> ret = new LinkedList<>(xs);
		ret.sort((x,y) -> x.toString().compareTo(y.toString()));
		return ret;
	}

	private static <Ty,En,Sym,Fk,Att,Gen,Sk> void viewDP(DP<Ty,En,Sym,Fk,Att,Gen,Sk> dp, Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col, JTabbedPane ret) {
		CodeTextPanel input = new CodeTextPanel("Input", "");
		CodeTextPanel output = new CodeTextPanel("Output", "");

		JButton eq = new JButton("Decide Equation-in-ctx");
		JButton nf = new JButton("Normalize Term-in-ctx");
		/* if (!dp.hasNFs()) {
			nf.setEnabled(false);
		} */
		JButton print = new JButton("Show Info");
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		buttonPanel.add(eq); buttonPanel.add(nf); buttonPanel.add(print);
		
		Split split = new Split(.5, JSplitPane.VERTICAL_SPLIT); //TODO: does not position correctly
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

	private static <Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1,En2,Sym2,Fk2,Att2,Gen2,Sk2> void viewMorphism(Morphism<Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1,En2,Sym2,Fk2,Att2,Gen2,Sk2> m, JTabbedPane ret) {
		CodeTextPanel input = new CodeTextPanel("Input", "");
		CodeTextPanel output = new CodeTextPanel("Output", "");

		JButton nf = new JButton("Normalize Term-in-ctx");
		JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
		buttonPanel.add(nf); 
		
		Split split = new Split(.5, JSplitPane.VERTICAL_SPLIT); //TODO: does not position correctly
		split.add(input);
		split.add(output);
		
		JPanel main = new JPanel(new BorderLayout());
		main.add(split, BorderLayout.CENTER);
		main.add(buttonPanel, BorderLayout.NORTH);
		
		nf.addActionListener(x -> {
			try {
				Pair<List<Pair<String, String>>, RawTerm> y = AqlParser.parseTerm(input.getText());
				Triple<Ctx<Var,Chc<Ty,En1>>,Term<Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1>,Term<Ty,En1,Sym1,Fk1,Att1,Gen1,Sk1>> z = RawTerm.infer2(y.first, y.second, y.second, m.src());			
				Pair<Ctx<Var,Chc<Ty,En2>>,Term<Ty,En2,Sym2,Fk2,Att2,Gen2,Sk2>> a = m.translate(z.first, z.second);
				output.setText(a.first.toString() + a.second);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				output.setText(ex.getMessage());
			}
		});
		
		ret.addTab("Trans", main);
	}

	
/*
	static class Hyperactive implements HyperlinkListener {
		 
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                    HTMLDocument doc = (HTMLDocument)pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    try {
                        pane.setPage(e.getURL());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    } */
	
}
