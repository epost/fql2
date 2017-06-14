package catdata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jparsec.error.Location;
import org.jparsec.error.ParseErrorDetails;
import org.jparsec.error.ParserException;

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
	
	public Program(List<Triple<String, Integer, X>> decls, String text) {
		this(decls, text, Collections.emptyList());
	}
	
	public Program(List<Triple<String, Integer, X>> decls, String text, List<Pair<String, String>> options) {
		this.text = text;
		List<Triple<String, Integer, X>> seen = new LinkedList<>();
		for (Triple<String, Integer, X> decl : decls) { 
			checkDup(seen, decl);
			exps.put(decl.first, decl.third);
			lines.put(decl.first, decl.second);
			order.add(decl.first);				
		}
		this.options = Util.toMapSafely(options);
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

}