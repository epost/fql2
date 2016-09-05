package catdata.aql;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import catdata.ide.CodeTextPanel;

//TODO: quoting

public final class Viewer {

	public static String html(Object obj) {
		return obj.toString().replace("\n", "<br>").replace("\t", "&nbsp;");
	} 
	
	public static JComponent view(Kind kind, Object obj) {
		JTabbedPane ret = new JTabbedPane();
		
		ret.add(new CodeTextPanel("", obj.toString()), "Text");
		
	//	JEditorPane pane = new JEditorPane();
	//	pane.setEditable(false);
	//	pane.setEditorKit(new HTMLEditorKit());
	//	pane.setText("<html><a href=\"http://categoricaldata.net/fql.html\">click me</a><br><br>" + html(obj) + "</html>");
	//	pane.addHyperlinkListener(new Hyperactive());
	
		//ret.add(new JScrollPane(pane), "HTML");
		
		return ret;
		
		/* switch (kind) {
		case INSTANCE:
			return viewInstance((Instance<?,?,?,?,?,?,?>)obj);
		case MAPPING:
			return viewMapping((Mapping)obj);
		case PRAGMA:
			return viewPragma((Pragma)obj);
		case QUERY:
			return viewQuery((Query)obj);
		case SCHEMA:
			return viewSchema((Schema)obj);
		case TRANSFORM:
			return viewTransform((Transform)obj);
		case TYPESIDE:
			return viewTypeSide((TypeSide)obj);
		default:
			throw new RuntimeException();			
		} */
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
