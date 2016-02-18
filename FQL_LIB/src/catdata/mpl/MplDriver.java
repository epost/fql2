package catdata.mpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import catdata.Unit;
import catdata.ide.Environment;
import catdata.ide.LineException;
import catdata.ide.Program;
import catdata.mpl.Mpl.MplExp;

public class MplDriver {


	
	public static Environment<MplObject> makeEnv(String str, Program<MplExp<String,String>> init) {
		Environment<MplObject> ret = new Environment<MplObject>();
		Map<String, Integer> extra = new HashMap<>();
		
		for (String k : init.order) {
			MplExp se = init.exps.get(k);
			try {
				MplObject xxx = (MplObject) se.accept(new Unit(), new MplOps(ret));
				ret.put(k, xxx);
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
		
		return ret;
	}
	
	

}
