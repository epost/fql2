package catdata.aql;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import catdata.Util;

public class AqlJs {

	//TODO: do not store classes, functions, etc in typesides 
	//users of js like saturate and query eval can cache local copies of compiled code
	
	//private Map<String, Function<List<Object>, Object>> compiled = new HashMap<>();
	
	public static Function<List<Object>, Object> compile(String s) {
		String ret =  "function aqljs(input) { " + s + " }\n\n";

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		
		try {
			engine.eval(ret);
		} catch (ScriptException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
			
		Function<List<Object>, Object> fun = new Function<List<Object>, Object>() {

			@Override
			public Object apply(List<Object> args) {
				Object ret;
				try {
					ret = ((Invocable)engine).invokeFunction("aqljs", args);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("In javascript execution, " + e.getClass() + " error: "  + e.getMessage());
				}
				if (ret == null) {
					throw new RuntimeException("javascript null from " + args + " on " + s);
				}
				return ret;
			}
			
		};
		
		return fun;
		
		
	
	}

	public static Class<?> load(String clazz) {
		try {
			return Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(clazz + " is not on the java classpath");
		}
		
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> reduce(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term, Collage<Ty, ?, Sym, ?, ?, ?, ?> col) {
		for (;;) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> next = reduce1(term, col);
			if (next.equals(term)) {
				return next;
			}
			term = next;
		}
	}
	
	//TODO don't need all of collage, just java part
	private static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> reduce1(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term,  Collage<Ty, ?, Sym, ?, ?, ?, ?> col) {
		List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args = null; 
		Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg = null; 

		if (term.var != null || term.gen != null || term.sk != null || term.obj != null) {
			return term;
		}

		if (term.args != null) {
			args = new LinkedList<>();
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> x : term.args) {
				args.add(reduce1(x, col));
			}
		} else if (term.arg != null) {
			arg = reduce1(term.arg, col);
		} 
		
		if (term.fk != null) {
			return Term.Fk(term.fk, arg);
		} else if (term.att != null) {
			return Term.Att(term.att, arg);
		} else if (term.sym != null) {
			String java = col.java_fns.get(term.sym);
			if (java == null) {
				return Term.Sym(term.sym, args);
			}
			List<Object> unwrapped_args = new LinkedList<>();
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> t : args) {
				if (t.obj != null) {
					unwrapped_args.add(t.obj);
				}
			}
			if (unwrapped_args.size() != args.size()) {
				return Term.Sym(term.sym, args);
			}
			Function<List<Object>, Object> fn = compile(java);
			Object result = fn.apply(unwrapped_args);
			if (result == null) {
				//TODO: too restrictive?
				throw new RuntimeException("Java applying " + term.sym + " to " + Util.sep(unwrapped_args, ", ") + " gives a null result");
			}
			Ty ty = col.syms.get(term.sym).second;
			String clazz = col.java_tys.get(ty);
			try {
				Class<?> c = Class.forName(clazz);
				if (!c.isInstance(result)) {
					throw new RuntimeException("Java applying " + term.sym + " to " + Util.sep(unwrapped_args, ", ") + " gives a result of class " + result.getClass() + ", not " + clazz + " as expected" );
				}
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
				throw new RuntimeException("Anomaly: please report");
			}
			
			return Term.Obj(result, ty);
		}
		throw new RuntimeException();
	}
	
}
