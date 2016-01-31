package fql_lib.fpql;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fql_lib.fpql.XExp.Flower;
import fql_lib.ide.LineException;
import fql_lib.ide.NEWDEBUG;


public class XDriver {

	@SuppressWarnings({ "rawtypes" })
	public static XEnvironment makeEnv(String str, XProgram init) {
		if (NEWDEBUG.debug.fpql.x_typing) {
			init.doTypes();
		}
		XEnvironment ret = new XEnvironment(init, str);
		Map<String, Integer> extra = new HashMap<>();
		
		int i = 0;
		for (String k : init.order) {
			XExp se = init.exps.get(k);
			try {
				XObject xxx = se.accept(init, new XOps(ret));
				if (xxx == null) {
					throw new RuntimeException();
				}
				if (se instanceof Flower) {
					Flower f = (Flower) se;
					if (ret.objs.containsKey(f.ty)) {
						throw new RuntimeException("Duplicate: " + f.ty);
					}
					XCtx c = (XCtx) xxx;
					if (f.ty != null) {
						ret.objs.put(f.ty, c.schema);
						extra.put(f.ty, i);
					}
				} 
				if (ret.objs.containsKey(k)) {
					throw new RuntimeException("Duplicate: " + k);
				}
				ret.objs.put(k, xxx);
				i++;
			} catch (Throwable t) {
				t.printStackTrace();
				throw new LineException(t.getLocalizedMessage(), k, "");
			}
		}
		
		int j = 0;
		for (Entry<String, Integer> e : extra.entrySet()) {
			init.order.add(e.getValue() + j, e.getKey()); 
			j++;
		}
		
		//TODO: add to order
		return ret;
	}

}
