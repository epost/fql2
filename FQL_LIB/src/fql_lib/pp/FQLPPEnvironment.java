package fql_lib.pp;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fql_lib.pp.cat.Category;
import fql_lib.pp.cat.FinSet;
import fql_lib.pp.cat.Functor;
import fql_lib.pp.cat.Transform;
import fql_lib.pp.cat.FinSet.Fn;

@SuppressWarnings("serial")
public class FQLPPEnvironment implements Serializable {

	public FQLPPProgram prog;
	public String text;
	public Map<String, Set<?>> sets;
	public Map<String, FinSet.Fn<?,?>> fns;
	public Map<String, Category<?, ?>> cats;
	public Map<String, Functor<?,?,?,?>> ftrs;
	public Map<String, Transform<?,?,?,?>> trans;
	
	public FQLPPEnvironment(FQLPPEnvironment e) {
		this(e.prog, e.text, new HashMap<>(e.sets), new HashMap<>(e.fns), new HashMap<>(e.cats), new HashMap<>(e.ftrs), new HashMap<>(e.trans));
	}

	public FQLPPEnvironment(FQLPPProgram prog, String text, Map<String, Set<?>> sets, Map<String, Fn<?,?>> fns,
			Map<String, Category<?, ?>> cats, Map<String, Functor<?,?,?,?>> ftrs,
			Map<String, Transform<?,?,?,?>> trans) {
		this.sets = sets;
		this.fns = fns;
		this.cats = cats;
		this.ftrs = ftrs;
		this.trans = trans;
		this.text = text;
		this.prog = prog;
	}
	
	public static FQLPPEnvironment load(File path) throws Exception {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			FQLPPEnvironment e = (FQLPPEnvironment) in.readObject();
			in.close();
			fileIn.close();
			if (e == null) {
				throw new RuntimeException();
			}
		return e;
	}

}
