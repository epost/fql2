package catdata.aql;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import catdata.Ctx;
import catdata.Pair;
import catdata.Util;

public class AqlJs<Ty, Sym> {
	
	private static final String postfix = "\n\nPossibly helpful info: 32-bit Java integers cannot exceed 2 billion; if you need larger numbers please use strings \n\nPossibly helpful info: javascript arguments are accessed as input[0], input[1], etc.\n\nPossibly useful links: http://docs.oracle.com/javase/8/docs/api/ and http://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/intro.html .";

	//private Map<String, String> binding = new HashMap<>();
	
	private final Ctx<Ty, String> iso1 = new Ctx<>();
	private final Ctx<Sym, String> iso2 = new Ctx<>();
	//Map<Integer, String> iso2 = new HashMap<>();
	
	private final Ctx<Sym, Pair<List<Ty>, Ty>> syms; //TODO aql duplicates
	public final Ctx<Ty, String> java_tys;
	public final Ctx<Ty, String> java_parsers;
	public final Ctx<Sym, String> java_fns;

	private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	
	
	public AqlJs(Ctx<Sym, Pair<List<Ty>, Ty>> syms, Ctx<Ty, String> java_tys, Ctx<Ty, String> java_parsers, Ctx<Sym, String> java_fns) {
		this.syms = syms;
		this.java_fns = java_fns;
		this.java_parsers = java_parsers;
		this.java_tys = java_tys;
		String last = "";
		try {
			int i = 0;
			for (Ty k : java_parsers.keySet()) {
				String ret = "function aqljsparser_" + i + "(input) { " + java_parsers.get(k) + " }\n\n";
				iso1.put(k, "aqljsparser_" + i);
				i++;
				engine.eval(ret);
				last = k.toString();
			}
			i = 0;
			for (Sym k : java_fns.keySet()) {
				String ret = "function aqljsfn_" + i + "(input) { " + java_fns.get(k) + " }\n\n";
				iso2.put(k, "aqljsfn_" + i);
				i++;
				engine.eval(ret);
				last = k.toString();
			}
		} catch (Throwable e) {
			throw new RuntimeException("In javascript execution, " + e.getMessage() + postfix + "\n\nlast binding evaluated: " + last);
		}
	}
	//private final TypeSide<Ty, Sym> ts;
	/*public AqlJs(TypeSide<Ty, Sym> ts) {
		this(ts.syms, ts.java_tys, ts.java_parsers, ts.java_fns);
	}*/
	
	private Object apply(Sym name, List<Object> args) {
		try {
			//TODO aql check inputs and outputs here?
			Object ret = ((Invocable)engine).invokeFunction(iso2.get(name), args);			
			check(syms.get(name).second, ret);
			return ret;
		} catch (Throwable e) {
			throw new RuntimeException("In javascript execution of " + name + " on arguments " + args + ", " + Util.sep(args.stream().map(x->x.getClass()).collect(Collectors.toList()), ",") + " , " + e.getClass() + " error: "  + e.getMessage() + postfix);
		}
	}
	
	//TODO does this fix Fred's Heisenbug?
	public synchronized Object parse(Ty name, String o) {
		if (!iso1.containsKey(name)) {
			throw new RuntimeException("In javascript execution of " + o + " no javascript definition for " + name);
		}
		try {
			Object ret = ((Invocable)engine).invokeFunction(iso1.get(name), Util.singList(o));
			check(name, ret);
			return ret;
		} catch (Throwable e) {
			if (e.getMessage() != null && e.getMessage().contains("jdk.nashorn.internal.codegen.TypeMap")) {
				throw new RuntimeException("The Java Runtime has suffered an internal error and the IDE must be restarted.\n\n" + e.getMessage());
			}
			//e.printStackTrace();
			throw new RuntimeException("In javascript execution of " + o + " (of " + o.getClass() + ") cannot convert to " + name + " error: "  + e.getMessage() + postfix + "\n\nPossible fix: check the java_constants of the typeside for type conversion errors.");
		}
	}
	
	private void check(Ty ty, Object o) {
		if (o == null) {
			throw new RuntimeException("evaluation return null." + postfix);
		}
		String clazz = java_tys.get(ty);
		Class<?> c = Util.load(clazz);
		if (!c.isInstance(o)) {
			throw new RuntimeException(o + " does not have type " + c + ", has type " + o.getClass());
		}
	}
	

	public <En, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> reduce(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (java_tys.isEmpty()) {
			return term;
		}
		while (true) {
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> next = reduce1(term);
			if (next.equals(term)) {
				return next;
			}
			term = next;
		}
	}
	
	private <En, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> reduce1(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.var != null || term.gen != null || term.sk != null || term.obj != null) {
			return term;
		}
		
		Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg = null; 
		if (term.arg != null) {
			arg = reduce1(term.arg);
		} 
		
		if (term.fk != null) {
			return Term.Fk(term.fk, arg);
		} else if (term.att != null) {
			return Term.Att(term.att, arg);
		} else if (term.args != null) {
			List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args = new LinkedList<>();
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> x : term.args) {
				args.add(reduce1(x));
			}
			if (!java_fns.containsKey(term.sym)) {
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
			Object result = apply(term.sym, unwrapped_args);
			Ty ty = syms.get(term.sym).second;
			return Term.Obj(result, ty);
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	@Deprecated
	public static synchronized Object exec(String s, Map<String, Object> m) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");	
		try {
			Bindings b = engine.createBindings();
			b.putAll(m);
			return engine.eval(s, b);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error executing " + s + ": " + e.getMessage() + postfix, e);
		}	
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((java_fns == null) ? 0 : java_fns.hashCode());
		result = prime * result + ((java_parsers == null) ? 0 : java_parsers.hashCode());
		result = prime * result + ((java_tys == null) ? 0 : java_tys.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		AqlJs<?,?> other = (AqlJs<?,?>) obj;
		if (java_fns == null) {
			if (other.java_fns != null)
				return false;
		} else if (!java_fns.equals(other.java_fns))
			return false;
		if (java_parsers == null) {
			if (other.java_parsers != null)
				return false;
		} else if (!java_parsers.equals(other.java_parsers))
			return false;
		if (java_tys == null) {
			if (other.java_tys != null)
				return false;
		} else if (!java_tys.equals(other.java_tys))
			return false;
		return true;
	}
	
	
}
