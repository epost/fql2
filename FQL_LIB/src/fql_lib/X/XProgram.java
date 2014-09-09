package fql_lib.X;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.decl.FQLProg;
import fql_lib.decl.LineException;

public class XProgram implements FQLProg {
	
	public List<String> order = new LinkedList<>();
	public LinkedHashMap<String, Integer> lines = new LinkedHashMap<>();
	
	public LinkedHashMap<String, XExp> exps = new LinkedHashMap<>();
	//String -> java.lang.String
/*	public LinkedHashMap<String, String> tys = new LinkedHashMap<>(); 
	//Length -> String, String, com.fql.length
	public LinkedHashMap<String, Triple<String, String, String>> fns = new LinkedHashMap<>();
	//ryan -> String, com.fql.ryan 
	public LinkedHashMap<String, Pair<String, String>> consts = new LinkedHashMap<>();
	// a.b.c = p.q.r
	public List<Pair<List<String>, List<String>>> eqs = new LinkedList<>(); */
	
	public XProgram(List<Triple<String, Integer, XExp>> decls) {
/*			List<Pair<String, String>> tys, 
			List<Pair<String, Triple<String, String, String>>> fns,
			List<Pair<String, Pair<String, String>>> consts,
			List<Pair<List<String>, List<String>>> eqs) { */
			Set<String> seen = new HashSet<>();
			for (Triple<String, Integer, XExp> decl : decls) { 
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

}
