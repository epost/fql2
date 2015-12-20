package fql_lib.examples;

public class OplNested2Example extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}
	
	@Override
	public String getName() {
		return "O Nested 2"; 
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "Ax = theory {"
			+ "\n	sorts A1, A2;"
			+ "\n	symbols;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nBx = theory {"
			+ "\n	sorts B;"
			+ "\n	symbols b : B;"
			+ "\n	equations;"
			+ "\n}"
			+ "\nB = saturate Bx"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	sorts A1 -> B, A2 -> B;"
			+ "\n	symbols;"
			+ "\n} : Ax -> Bx "
			+ "\n"
			+ "\nS0 = theory { "
			+ "\n sorts"
			+ "\n 	M, T1, T2;"
			+ "\n symbols"
			+ "\n 	modelOf1@4 : M -> T1, "
			+ "\n 	modelOf2@3 : M -> T2,"
			+ "\n 	Q@2 : T2 -> T1,"
			+ "\n 	t2@1 : T2;"
			+ "\n equations"
			+ "\n 	forall m. modelOf1(m) = Q(modelOf2(m));"
			+ "\n}"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	entities M;	"
			+ "\n} : S0"
			+ "\n"
			+ "\nT = types S"
			+ "\n"
			+ "\nI0 = presentation {"
			+ "\n	generators m : M;"
			+ "\n	equations modelOf2(m)=t2;"
			+ "\n} : S0"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols"
			+ "\n		Q -> \"return F.delta(input[0])\","
			+ "\n		t2 -> \"return B\";			"
			+ "\n} : T"
			+ "\n"
			+ "\nI = instance S I0 M"
			+ "\n";


}
