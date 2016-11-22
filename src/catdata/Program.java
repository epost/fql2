package catdata;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class Program<X> implements Prog {

	
	public List<String> order = new LinkedList<>();
	public LinkedHashMap<String, Integer> lines = new LinkedHashMap<>();	
	public LinkedHashMap<String, X> exps = new LinkedHashMap<>();
	
	@Override
	public String toString() {
		String ret = "";
		for (String s : order) {
			ret += s + " = " + exps.get(s) + "\n\n";
		}
		return ret;
	}
	
	public Program(List<Triple<String, Integer, X>> decls) {
			List<Triple<String, Integer, X>> seen = new LinkedList<>();
			for (Triple<String, Integer, X> decl : decls) { 
				checkDup(seen, decl);
				exps.put(decl.first, decl.third);
				lines.put(decl.first, decl.second);
				order.add(decl.first);				
			}
	}

	private void checkDup(List<Triple<String, Integer, X>> seen, Triple<String, Integer, X> toAdd) {
		for (Triple<String, Integer, X> other : seen) {
			if (other.first.equals(toAdd.first)) {
				throw new RuntimeException("Duplicate name: " + toAdd.first + " on line " + other.second + " and " + toAdd.second);
			}
		}
		seen.add(toAdd);
	}

	@Override
	public Integer getLine(String s) {
		return lines.get(s);
	}

}