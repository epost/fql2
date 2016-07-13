package catdata.opl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import catdata.ide.Environment;
import catdata.ide.LineException;
import catdata.ide.NEWDEBUG;
import catdata.ide.Program;

public class OplDriver {

	
	//TODO: let x be the position of the first change between old and new program.
	//a definition y is 'safe' if definition y+1 begins before x.
	//each code editor's driver should copy over the safe definitions and start the driver
	//at the appropriate spot
	
	public static Environment<OplObject> makeEnv(String str, Program<OplExp> init, String[] toUpdate,
			String last_str, Program<OplExp> last_prog, Environment<OplObject> last_env) {
		Environment<OplObject> ret = new Environment<OplObject>();
		Map<String, Integer> extra = new HashMap<>();
		
		Set<String> unchanged = new HashSet<>();
		int i = 0;
		if (last_str != null && NEWDEBUG.debug.opl.cache) {
			for (String k : init.order) {
				String v = last_prog.order.get(i); 
				if (!v.equals(k)) {
					break;
				}
				OplExp a = init.exps.get(k);
				OplExp b = last_prog.exps.get(k);
				if (!a.equals(b)) {
					//System.out.println("not eq: " + k + "\n" + a + "\n\n---------\n\n" + b + "\n\n---- " + a.getClass() );
					break;
				}
				unchanged.add(k);
				i++;
			}
		}
		
	//	int i = 0;
//		toUpdate[0] = "Processed:";
		for (String k : init.order) {
			if (unchanged.contains(k)) {
				ret.put(k, last_env.get(k));
				continue;
			}
			OplExp se = init.exps.get(k);
			try {
				OplObject xxx = se.accept(init, new OplOps(ret));
				ret.put(k, xxx);
				toUpdate[0] = "Last Processed: " + k ; 
			//	i++;
			} catch (Throwable t) {
				t.printStackTrace();
				throw new LineException(t.getLocalizedMessage(), k, "");
			}
		}
		
		//TODO: what is this?
		int j = 0;
		for (Entry<String, Integer> e : extra.entrySet()) {
			init.order.add(e.getValue() + j, e.getKey()); 
			j++;
		}
		
		//TODO: add to order
		return ret;
	}
	
	

}
