package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class OplHornExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	} 

	@Override
	public String getName() {
		return "Horn";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S1 = theory {"
			+ "\n	sorts X, dom;"
			+ "\n	symbols r1 : X -> dom, r2 : X -> dom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\nI1 = presentation {"
			+ "\n	generators x1, x2 : X;"
			+ "\n	equations r1(x1) = r1(x2);"
			+ "\n} : S1 "
			+ "\nJ1 = saturate I1"
			+ "\n"
			+ "\n//////////"
			+ "\n"
			+ "\nS2 = theory {"
			+ "\n	sorts X, dom;"
			+ "\n	symbols r1 : X -> dom, r2 : X -> dom;"
			+ "\n	equations;"
			+ "\n	implications "
			+ "\n		forall x1, x2. r1(x1) = r1(x2) -> r2(x1) = r2(x2); "
			+ "\n}"
			+ "\nI2 = presentation {"
			+ "\n	generators x1, x2 : X;"
			+ "\n	equations r1(x1) = r1(x2);"
			+ "\n} : S2"
			+ "\nJ2 = saturate I2"
			+ "\n";



}
