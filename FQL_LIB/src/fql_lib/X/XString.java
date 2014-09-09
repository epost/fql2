package fql_lib.X;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import fql_lib.gui.FQLTextPanel;

public class XString implements XObject {

	private String title;
	private String value;


	public XString(String title, String value) {
		this.title = title;
		this.value = value;
	}
	
	
	@Override
	public JComponent display() {
		return new FQLTextPanel(BorderFactory.createEtchedBorder(), title, value);
	}

}
