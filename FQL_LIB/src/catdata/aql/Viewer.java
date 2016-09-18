package catdata.aql;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.ide.CodeTextPanel;
import catdata.ide.Split;

//TODO: quoting

public final class Viewer {

	public static String html(Object obj) {
		return obj.toString().replace("\n", "<br>").replace("\t", "&nbsp;");
	} 
	
	public static JComponent view(Kind kind, Object obj) {
		JTabbedPane ret = new JTabbedPane();
		
		ret.add(new CodeTextPanel("", obj.toString()), "Text");
				
		switch (kind) {
		case INSTANCE:
			Instance<Object,Object,Object,Object,Object,Object,Object> instance = (Instance<Object,Object,Object,Object,Object,Object,Object>) obj;
			viewDP(instance.semantics(), instance.collage(), ret);

			break;
		case MAPPING:
		//	viewMapping(obj, ret);
			break;
		case PRAGMA:
		//	viewPragma(obj, ret);
			break;
		case QUERY:
		//	viewQuery(obj, ret);
			break;
		case SCHEMA:
			@SuppressWarnings("unchecked") Schema<Object,Object,Object,Object,Object> schema = (Schema<Object,Object,Object,Object,Object>) obj;
			viewDP(schema.semantics(), schema.collage(), ret);
			break;
		case TRANSFORM:
		//	viewTransform(obj, ret);
			break;
		case TYPESIDE:
			@SuppressWarnings("unchecked") TypeSide<Object,Object> typeSide = (TypeSide<Object,Object>) obj;
			viewDP(typeSide.semantics(), typeSide.collage(), ret);
			break;
		default:
			throw new RuntimeException("Anomaly: please report");			
		} 
		
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
