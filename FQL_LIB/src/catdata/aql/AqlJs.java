package catdata.aql;

import java.util.List;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
	
	
}
