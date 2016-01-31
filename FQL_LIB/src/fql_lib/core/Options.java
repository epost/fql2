package fql_lib.core;

import java.io.Serializable;
import java.util.function.Function;

import javax.swing.JComponent;

import catdata.algs.Pair;
import catdata.algs.Unit;

public abstract class Options implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract String getName();
	
	public abstract Pair<JComponent, Function<Unit, Unit>> display(); 	

	public abstract int size();
	
	public static int biggestSize = 0;
}
