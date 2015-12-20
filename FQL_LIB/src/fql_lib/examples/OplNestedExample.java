package fql_lib.examples;

public class OplNestedExample extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}
	
	@Override
	public String getName() {
		return "O Nested";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "Ax = theory {"
			+ "\n	sorts A1, A2;"
			+ "\n	symbols a : A1 -> A2, a1 : A1, a2 : A2;"
			+ "\n	equations;"
			+ "\n}"
			+ "\nA = saturate Ax"
			+ "\n"
			+ "\nBx = theory {"
			+ "\n	sorts B;"
			+ "\n	symbols b : B -> B, b1 : B;"
			+ "\n	equations forall x. b(x) = x;"
			+ "\n}"
			+ "\nB = saturate Bx"
			+ "\n"
			+ "\nS0 = theory { "
			+ "\n sorts"
			+ "\n 	M, Model, Int;"
			+ "\n symbols"
			+ "\n 	modelOf : M -> Model, "
			+ "\n 	size1 : M -> Int,"
			+ "\n 	size2 : Model -> Int,"
			+ "\n 	m1, m2 : Model;"
			+ "\n equations"
			+ "\n 	forall m. size1(m) = size2(modelOf(m));"
			+ "\n}"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	entities M;	"
			+ "\n} : S0"
			+ "\n"
			+ "\nT = types S"
			+ "\n"
			+ "\nI0 = presentation {"
			+ "\n	generators mA, mB : M;"
			+ "\n	equations modelOf(mA)=m1, modelOf(mB)=m2;"
			+ "\n} : S0"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols"
			+ "\n		size2 -> \"return input[0].size()\","
			+ "\n		m1 -> \"return A\","
			+ "\n		m2 -> \"return B\";			"
			+ "\n} : T"
			+ "\n"
			+ "\nI = instance S I0 M"
			+ "\n";


}
