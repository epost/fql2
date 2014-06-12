package fql_lib.decl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fql_lib.cat.Category;
import fql_lib.cat.Functor;
import fql_lib.cat.Transform;
import fql_lib.cat.categories.FinSet;
import fql_lib.cat.categories.FinSet.Fn;

public class Environment {

	public Map<String, Set<?>> sets;
	public Map<String, FinSet.Fn<?,?>> fns;
	public Map<String, Category<?, ?>> cats;
	public Map<String, Functor<?,?,?,?>> ftrs;
	public Map<String, Transform<?,?,?,?>> trans;
	
	public Environment(Environment e) {
		this(new HashMap<>(e.sets), new HashMap<>(e.fns), new HashMap<>(e.cats), new HashMap<>(e.ftrs), new HashMap<>(e.trans));
	}

	public Environment(Map<String, Set<?>> sets, Map<String, Fn<?,?>> fns,
			Map<String, Category<?, ?>> cats, Map<String, Functor<?,?,?,?>> ftrs,
			Map<String, Transform<?,?,?,?>> trans) {
		this.sets = sets;
		this.fns = fns;
		this.cats = cats;
		this.ftrs = ftrs;
		this.trans = trans;
	}

}
