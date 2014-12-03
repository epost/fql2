package fql_lib.X;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

public class XQuery implements XObject {
	
	public XCtx apply(XCtx I) {
		return sigma.apply0(delta.delta(pi.pi(I)));
	}
	
	public XQuery(XMapping pi, XMapping delta, XMapping sigma) {
		this.pi = pi;
		this.delta = delta;
		this.sigma = sigma;
		if (!pi.dst.equals(delta.dst)) {
			throw new RuntimeException("Pi target, Delta target mismatch");
		}
		if (!delta.src.equals(sigma.src)) {
			throw new RuntimeException("Delta source, sigma source mismatch");
		}
		//TODO: check discrete op-fib, surjection here
	}

	XMapping pi, delta, sigma;

	@Override
	public String kind() {
		return "query";
	}

	@Override
	public JComponent display() {
		JTabbedPane ret = new JTabbedPane();
		ret.addTab("Pi", pi.display());
		ret.addTab("Delta", delta.display());
		ret.addTab("Sigma", sigma.display());
		return ret;
	}

}
