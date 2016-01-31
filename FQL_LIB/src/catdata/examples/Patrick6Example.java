package catdata.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick6Example extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Inconsistent";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s =
			"\nInt : type"
+			"\n"
+			"\none : Int"
+			"\ntwo : Int"
+			"\n"
+			"\n//eq1 : one = two"
+			"\n"
+			"\nX = schema { "
+			"\n      nodes;"
+			"\n      edges;"
+			"\n      equations;"
+			"\n}"
+			"\n"
+			"\nI = instance { "
+			"\n      variables x : Int, y : Int;"
+			"\n      equations x = one, y = two, x = y;"
+			"\n} : X"
+			"\n";

}
