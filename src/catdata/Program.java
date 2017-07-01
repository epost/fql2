package catdata;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jparsec.error.Location;
import org.jparsec.error.ParseErrorDetails;
import org.jparsec.error.ParserException;

import catdata.aql.exp.Exp;

public class Program<X> implements Prog {

	
	public final List<String> order = new LinkedList<>();
	public final LinkedHashMap<String, Integer> lines = new LinkedHashMap<>();
	public final LinkedHashMap<String, X> exps = new LinkedHashMap<>();
	public final Map<String, String> options;
	private final String text;
	
	@Override
	public String toString() {
		String ret = "";
		for (String s : order) {
			ret += s + " = " + exps.get(s) + "\n\n";
		}
		return ret;
	}
	
	public Function<X, String> kindOf;
	
	@Override 
	public String kind(String s) {
		return kindOf.apply(exps.get(s));
	}
	
	public Program(List<Triple<String, Integer, X>> decls, String text) {
		this(decls, text, Collections.emptyList(), x -> "");
	}
	
	public Program(List<Triple<String, Integer, X>> decls, String text, List<Pair<String, String>> options, Function<X, String> k) {
		this.text = text;
		List<Triple<String, Integer, X>> seen = new LinkedList<>();
		for (Triple<String, Integer, X> decl : decls) { 
			checkDup(seen, decl);
			exps.put(decl.first, decl.third);
			lines.put(decl.first, decl.second);
			if (decl.second == null || decl.third == null) {
				Util.anomaly();
			}
			order.add(decl.first);				
		}
		this.options = Util.toMapSafely(options);
		this.kindOf = k;
	}

	private Location conv(int i) {
		int c = 1;
		int line = 1, col = 1;
		while (c++ <= i) {
		  if (text.charAt(c) == '\n') {
		    ++line;
		    col = 1;
		  } else {
		    ++col;
		  }
		}
		return new Location(line, col);
	}
	
	@SuppressWarnings("deprecation")
	private void checkDup(List<Triple<String, Integer, X>> seen, Triple<String, Integer, X> toAdd) {
		for (Triple<String, Integer, X> other : seen) {
			if (other.first.equals(toAdd.first)) {
				if (text == null) {
					throw new RuntimeException("Duplicate name: " + toAdd.first); //TODO AQL + " on line " + other.second + " and " + toAdd.second);
				}
				throw new ParserException(new ParseErrorDetails() {

					@Override
					public String getEncountered() {
						return other.first;
					}

					@Override
					public List<String> getExpected() {
						return new LinkedList<>();
					}

					@Override
					public String getFailureMessage() {
						return "Other occurance: " + conv(other.second);
					}

					@Override
					public int getIndex() {
						return other.second;
					}

					@Override
					public String getUnexpected() {
						return "";
					}}, "Duplicate name: " + toAdd.first, conv(toAdd.second)); //TODO AQL );

			}
		}
		seen.add(toAdd);
	}

	@Override
	public Integer getLine(String s) {
		return lines.get(s);
	}

	@Override
	public Collection<String> keySet() {
		return order;
	}


}