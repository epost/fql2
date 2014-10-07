package fql_lib.examples;

public class Patrick8Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//for now, the strings associated with types, constants, and fns are ignored, but eventually"
			+ "\n//they will bind to java classes."
			+ "\nstring = type \"java.lang.String\""
			+ "\nbill = constant string \"fql.primitives.BillString\""
			+ "\nreverse = fn string -> string \"fql.primitives.Reverse\""
			+ "\n"
			+ "\neq1 = assume reverse.reverse = string"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes N;"
			+ "\n	edges f : N -> N, att : N -> string;"
			+ "\n	equations f.f = f;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables v:N;"
			+ "\n	equations v.att = bill.reverse;"
			+ "\n} : S"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	nodes N -> N;"
			+ "\n	edges f -> f.f, att -> att;"
			+ "\n} : S -> S"
			+ "\n"
			+ "\nh = homomorphism {"
			+ "\n	variables v -> v;"
			+ "\n} : I -> I"
			+ "\n"
			+ "\nunit = return F I"
			+ "\ncounit = coreturn F I"
			+ "\n";



}
