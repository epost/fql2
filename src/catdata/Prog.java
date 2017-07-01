package catdata;

import java.util.Collection;

public interface Prog {

	Integer getLine(String s);
	
	Collection<String> keySet();
	
	default String kind(String s) {
		return "";
	}
	
}
