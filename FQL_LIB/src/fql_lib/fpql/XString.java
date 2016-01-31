package fql_lib.fpql;

import fql_lib.ide.CodeTextPanel;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class XString implements XObject {

	private String title;
	private String value;


	public XString(String title, String value) {
		this.title = title;
		this.value = value;
	}
	
	
	@Override
	public JComponent display() {
		return new CodeTextPanel(BorderFactory.createEtchedBorder(), title, value);
	}


	@Override
	public String kind() {
		return "black-box";
	}

}
