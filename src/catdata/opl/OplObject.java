package catdata.opl;

import javax.swing.JComponent;

public interface OplObject {

	public JComponent display();

	public default String toHtml() {
		return toString().replace("\n", "<br>").replace("\t", "&nbsp;");
	}
	
	
}
