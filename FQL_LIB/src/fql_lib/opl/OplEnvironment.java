package fql_lib.opl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OplEnvironment {

	private OplProgram prog;
	private String str;
	private Map<String, OplObject> objs;
	
	public Set<String> keys() {
		return objs.keySet();
	}
	
	public OplEnvironment(OplProgram prog, String str) {
		this.prog = prog;
		this.str = str;	
		objs = new HashMap<>();
	}
	
	public void put(String k, OplObject v) {
		if (k == null || v == null) {
			throw new RuntimeException();
		}
		if (objs.containsKey(k)) {
			throw new RuntimeException("Duplicate name: " + k);
		}
		objs.put(k, v);
	}

	public OplObject get(String name) {
		OplObject ret = objs.get(name);
		if (ret == null) {
			throw new RuntimeException("Unbound variable: " + name);
		}
		return ret;

	}

	
}
