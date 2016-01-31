package fql_lib.opl;

import fql_lib.ide.CodeTextPanel;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class OplString implements OplObject {
	String str;

	public OplString(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return OplExp.strip(str);
	}

	@Override
	public JComponent display() {
		return new CodeTextPanel(BorderFactory.createEmptyBorder(), "", str);
	}

}