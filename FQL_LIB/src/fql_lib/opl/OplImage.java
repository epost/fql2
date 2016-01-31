package fql_lib.opl;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class OplImage implements OplObject {
	
	String msg;
	JLabel label;
	String url;
	
	public OplImage(String url) {
		try {
			this.url = url;
			label = new javax.swing.JLabel(new javax.swing.ImageIcon(new java.net.URL(url)));
		} catch (Exception ex) {
			ex.printStackTrace();
			msg = ex.getMessage();
		}
	}

	@Override
	public JComponent display() {
		if (label != null) {
			return label;
		}
		return new JLabel(msg);
	}
	
	@Override
	public String toString() {
		if (msg != null) {
			return msg;
		}
		return url;
	}
	
	@Override
	public boolean equals(Object o) {
		//System.out.println("OPLIMAGE comparing " + this + " against " + o);
		if (!(o instanceof OplImage)) {
			return false;
		}
		OplImage other = (OplImage) o;
		boolean ret = (other.url.equals(url));
		//System.out.println("result: " + ret);
		return ret; 
	}
	
	@Override
	public int hashCode() {
		return 0;
	}

}
