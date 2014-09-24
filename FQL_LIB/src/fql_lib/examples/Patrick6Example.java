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
			"\nInt = type \"java.lang.Integer\""
+			"\n"
+			"\none = constant Int \"fql.primitives.One\""
+			"\ntwo = constant Int \"fql.primitives.Two\""
+			"\n"
+			"\n//eq1 = assume one = two"
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
