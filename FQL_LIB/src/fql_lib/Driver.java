package fql_lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql_lib.cat.Category;
import fql_lib.cat.Functor;
import fql_lib.cat.Transform;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.decl.CatExp;
import fql_lib.decl.CatOps;
import fql_lib.decl.Environment;
import fql_lib.decl.FQLProgram;
import fql_lib.decl.FnExp;
import fql_lib.decl.FunctorExp;
import fql_lib.decl.LineException;
import fql_lib.decl.PreProcessor;
import fql_lib.decl.SetExp;
import fql_lib.decl.SetOps;
import fql_lib.decl.TransExp;

public class Driver {

	public static Environment makeEnv(String str, FQLProgram init) {
	//	System.out.println("Driver input: " + init);
		Map<String, Fn<?,?>> fns = new HashMap<>();
		Map<String, Set<?>> sets = new HashMap<>();
		Map<String, Category<?,?>> cats = new HashMap<>();
		Map<String, Functor<?,?,?,?>> ftrs = new HashMap<>();
		Map<String, Transform<?,?,?,?>> trans = new HashMap<>();
		Environment ret = new Environment(init, str, sets, fns, cats, ftrs, trans);

		for (Entry<String, CatExp> k : init.cats.entrySet()) {
			init.cats.put(k.getKey(), k.getValue().accept(init, new PreProcessor()));
		}
		
		
		for (String k : init.order) {
			SetExp se = init.sets.get(k);
			if (se != null) {
				try {
					Set<?> xxx = se.accept(init, new SetOps(ret));
					if (!(xxx instanceof Set)) {
						throw new RuntimeException("Does not evaluate to a set.");
					}
					sets.put(k, xxx);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new LineException(t.getLocalizedMessage(), k, "set");
				}
			}
			FnExp fe = init.fns.get(k);
			if (fe != null) {
				try {
					Fn<?,?> xxx = fe.accept(init, new SetOps(ret));
					if (!(xxx instanceof Fn)) {
						throw new RuntimeException("Does not evaluate to a function.");
					}
					fns.put(k, xxx);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new LineException(t.getLocalizedMessage(), k, "function");
				}
			}
			CatExp ce = init.cats.get(k);
//			CatExp ce = ce0.accept(init, new PreProcessor());
			if (ce != null) {
				try {
					Category<?,?> xxx = ce.accept(init, new CatOps(ret));
					if (!(xxx instanceof Category)) {
						throw new RuntimeException("Does not evaluate to a category.");
					}
					cats.put(k, xxx);	
				} catch (Throwable t) {
					t.printStackTrace();
					throw new LineException(t.getLocalizedMessage(), k, "category");
				}
			}
			FunctorExp FE = init.ftrs.get(k);
			if (FE != null) {
				try {
					Functor<?,?,?,?> xxx = FE.accept(init, new CatOps(ret));
					if (!(xxx instanceof Functor)) {
						throw new RuntimeException("Does not evaluate to a functor, is " + xxx.getClass() + ": " + xxx);
					}
					ftrs.put(k, xxx);		
				} catch (Throwable t) {
					t.printStackTrace();
					throw new LineException(t.getLocalizedMessage(), k, "functor");
				}
			}
			TransExp te = init.trans.get(k);
			if (te != null) {
				try {
					Transform<?,?,?,?> xxx = te.accept(init, new CatOps(ret));
					if (!(xxx instanceof Transform)) {
						throw new RuntimeException("Does not evaluate to a transform.");
					}
					trans.put(k, xxx);		
				} catch (Throwable t) {
					t.printStackTrace();
					throw new LineException(t.getLocalizedMessage(), k, "transform");
				}
			}
		}
		
		//System.out.println("Driver output: " + ret);
		return ret;
	}

}
