package fql_lib.examples;

public class OplTypedExample extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}
	
	@Override
	public String getName() {
		return "O Typed";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//type side"
			+ "\nNat = theory {"
			+ "\n	sorts "
			+ "\n		Nat;"
			+ "\n	symbols "
			+ "\n		zero : Nat,"
			+ "\n		succ : Nat -> Nat;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n//type side implementation"
			+ "\nJSDouble = javascript {"
			+ "\n	symbols"
			+ "\n		zero -> \"function zero(input) { return 1; }\","
			+ "\n		succ -> \"function succ(input) { return (2 * input[0]); }\";"
			+ "\n} : Nat"
			+ "\n"
			+ "\n//type side implementation"
			+ "\nJSCollapse = javascript {"
			+ "\n	symbols"
			+ "\n		zero -> \"function zero(input) { return 0; }\","
			+ "\n		succ -> \"function succ(input) { return 0; }\";"
			+ "\n} : Nat"
			+ "\n"
			+ "\n//instance presentation"
			+ "\nQuotientByFour = presentation {"
			+ "\n	generators"
			+ "\n		infinity: Nat;"
			+ "\n	equations"
			+ "\n		forall x. succ(succ(succ(succ(x)))) = x;"
			+ "\n} : Nat "
			+ "\n"
			+ "\n//term model"
			+ "\nres0 = saturate QuotientByFour"
			+ "\n"
			+ "\n//image of term model under one type side implementation"
			+ "\nres1 = SATURATE JSDouble QuotientByFour "
			+ "\n"
			+ "\n//image of term model under one type side implementation"
			+ "\nres2 = SATURATE JSCollapse QuotientByFour"
			+ "\n"
;

;


}
