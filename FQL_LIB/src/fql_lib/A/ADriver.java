package fql_lib.A;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fql_lib.core.LineException;
import fql_lib.opl.OplEnvironment;
import fql_lib.opl.OplObject;
import fql_lib.opl.OplOps;
import fql_lib.opl.OplProgram;

public class ADriver {

	public static OplEnvironment makeEnv(String str, AProgram init) {
		OplProgram x = init.convert();
		OplEnvironment ret = new OplEnvironment(x, str);
		Map<String, Integer> extra = new HashMap<>();
		
	//	int i = 0;
		for (String k : init.order) {
			AExp se = init.exps.get(k);
			try {
				OplObject xxx = se.accept(init, new AOps(ret));
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
