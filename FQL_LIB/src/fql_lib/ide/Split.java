package fql_lib.ide;

import javax.swing.JSplitPane;

@SuppressWarnings("serial")
/**
 * @author ryan
 * A split panel, but doesn't work well.
 */
public class Split extends JSplitPane {

	double bias;

	public Split(double bias, int split) {
		super(split);
		this.bias = bias;
		setDividerSize(2);
	}

	@Override
	public void setVisible(boolean b) {
		setVisible(b);
		setDividerLocation(bias);
		invalidate();
	}

}
