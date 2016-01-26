package fql_lib.A;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import catdata.algs.Triple;
import fql_lib.decl.FQLProg;
import fql_lib.decl.LineException;
import fql_lib.opl.OplProgram;


public class AProgram implements FQLProg {

	
	public List<String> order = new LinkedList<>();
	public LinkedHashMap<String, Integer> lines = new LinkedHashMap<>();	
	public LinkedHashMap<String, AExp> exps = new LinkedHashMap<>();
	
	public AProgram(List<Triple<String, Integer, AExp>> decls) {
			Set<String> seen = new HashSet<>();
			for (Triple<String, Integer, AExp> decl : decls) { 
				checkDup(seen, decl.first);
				exps.put(decl.first, decl.third);
				lines.put(decl.first, decl.second);
				order.add(decl.first);				
			}
	}

	private void checkDup(Set<String> seen, String name)
			throws LineException {
		if (seen.contains(name)) {
			throw new RuntimeException("Duplicate name: " + name);
		}
		seen.add(name);
	}

	@Override
	public Integer getLine(String s) {
		return lines.get(s);
	}

	public OplProgram convert() {
		throw new RuntimeException();
	}

}
