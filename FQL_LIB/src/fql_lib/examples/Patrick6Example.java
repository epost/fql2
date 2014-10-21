package fql_lib.examples;

public class Patrick6Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P Inconsistent";
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
