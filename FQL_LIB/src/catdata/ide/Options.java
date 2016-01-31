package catdata.ide;

import java.io.Serializable;
import java.util.function.Function;

import javax.swing.JComponent;

import catdata.Pair;
import catdata.Unit;

public abstract class Options implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract String getName();
	
	public abstract Pair<JComponent, Function<Unit, Unit>> display(); 	

	public abstract int size();
	
	public static int biggestSize = 0;
}
