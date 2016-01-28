package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class Patrick24ExampleBadUber extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Uber Bad";
	}

	@Override
	public String getText() {
	return s;
	}
	
	String s = "S = schema {"
			+ "\n	nodes A;"
			+ "\n	edges;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nT = schema {"
			+ "\n	nodes A, B;"
			+ "\n	edges f : A -> B, g : A -> B; "
			+ "\n	equations f = g;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables a1 a2:A;"
			+ "\n	equations;"
			+ "\n} : S "
			+ "\n"
			+ "\nQ = polynomial {"
			+ "\n	 q1 = {for v1:A, v2:A; where; attributes; edges f = {v=v1}:q2, g = {v=v2}:q2 ;} : A,"
			+ "\n      q2 = {for v :A;       where; attributes; edges; } : B 	 "
			+ "\n} : S -> T"
			+ "\n";



}
