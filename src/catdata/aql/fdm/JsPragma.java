package catdata.aql.fdm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Util;
import catdata.aql.AqlJs;
import catdata.aql.Pragma;

public class JsPragma extends Pragma {

	private List<String> jss = new LinkedList<>();
	
	private List<String> responses = new LinkedList<>();
		
	@SuppressWarnings("unused")
	private final Map<String, String> options; 
	
	public JsPragma(List<String> jss, Map<String, String> options) {
		this.jss = jss;
		this.options = options;
	}

	@Override
	public void execute() {
		List<String> ret = new LinkedList<>();
		for (String js : jss) {
			@SuppressWarnings("deprecation")
			Object o = AqlJs.exec(js);
			ret.add(js + ": " + o);
		}
		responses.add(Util.sep(ret, "\n"));
	}
	
	@Override
	public String toString() {
		return Util.sep(responses, "\n\n--------------\n\n");
	}
		
}

