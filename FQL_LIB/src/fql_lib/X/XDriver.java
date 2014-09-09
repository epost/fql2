package fql_lib.X;

import java.util.HashMap;
import java.util.Map;

import fql_lib.decl.LineException;

public class XDriver {

	public static XEnvironment makeEnv(String str, XProgram init) {
		XEnvironment ret = new XEnvironment(init, str);

		for (String k : init.order) {
			XExp se = init.exps.get(k);
			try {
				XObject xxx = se.accept(init, new XOps(ret));
				if (xxx == null) {
					throw new RuntimeException();
				}
				ret.objs.put(k, xxx);
			} catch (Throwable t) {
				t.printStackTrace();
				throw new LineException(t.getLocalizedMessage(), k, "");
			}
		}
		return ret;
	}

}
