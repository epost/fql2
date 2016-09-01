package catdata.aql;

import java.util.HashSet;
import java.util.Set;

import catdata.ide.LineException;
import catdata.ide.Program;


public final class Driver {

	// TODO: let x be the position of the first change between old and new
	// program.
	// a definition y is 'safe' if definition y+1 begins before x.
	// each code editor's driver should copy over the safe definitions and start
	// the driver
	// at the appropriate spot
	
	

	public static Env makeEnv(String str,
			Program<Exp<? extends Object>> init, String[] toUpdate, String last_str,
			Program<Exp<? extends Object>> last_prog, Env last_env) {
		Env env = new Env();

/*		boolean usesPragma = false;
		for (String k : init.order) {
			Exp se = init.exps.get(k);
			if (se instanceof OplPragma) {
				se.accept(init, new OplOps(ret));
				ret.put(k, se);
				usesPragma = true;
			}
		} */
		
	//	if (NEWDEBUG.debug.opl.opl_lazy_gui && usesPragma) {
	//		throw new RuntimeException("Pragmas and lazy guis are not compatible");
	//	}


		Set<String> unchanged = new HashSet<>();
		int i = 0;
		if (last_str != null /* && NEWDEBUG.debug.opl.opl_cache_gui */) { //TODO
			for (String k : init.order) {
				if (i >= last_prog.order.size()) {
					break;
				}
				String v = last_prog.order.get(i);
				if (!v.equals(k)) {
					break;
				} 
				Exp<? extends Object> a = init.exps.get(k);
				Exp<? extends Object> b = last_prog.exps.get(k);
				if (!a.equals(b)) {
					 System.out.println("not eq: " + k + "\n" + a +
					 "\n\n---------\n\n" + b + "\n\n---- " + a.getClass() );
					break;
				}
				unchanged.add(k);
				i++;
			}
		}

		for (String n : init.order) {
			Exp<? extends Object> exp = init.exps.get(n);
			Kind k = exp.kind();

			/* if (se instanceof OplPragma) {
				continue;
			} */

			if (unchanged.contains(n)) {
				env.put(n, k, last_env.get(n, k));
				continue;
			}
			try {
				Object val = exp.eval(env);
				if (val == null) {
					throw new RuntimeException("null result on " + exp);
				}
				env.put(n, k, val);
				if (toUpdate != null) {
					toUpdate[0] = "Last Processed: " + k;
				}
			} catch (Throwable t) {
				t.printStackTrace();
				throw new LineException(t.getMessage(), n, "");
			}
		}

		return env;
	}
	
}
