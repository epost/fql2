package fql_lib.opl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fql_lib.core.LineException;

public class OplDriver {

	public static OplEnvironment makeEnv(String str, OplProgram init) {
		OplEnvironment ret = new OplEnvironment(init, str);
		Map<String, Integer> extra = new HashMap<>();
		
	//	int i = 0;
		for (String k : init.order) {
			OplExp se = init.exps.get(k);
			try {
				OplObject xxx = se.accept(init, new OplOps(ret));
				ret.put(k, xxx);
			//	i++;
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
