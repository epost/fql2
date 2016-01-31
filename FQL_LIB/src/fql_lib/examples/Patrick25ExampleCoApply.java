package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class Patrick25ExampleCoApply extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Coapply";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "dom : type"
			+ "\n1 2 3 : dom"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes S1, S2;"
			+ "\n	edges a:S1->dom, b:S1->dom, c:S2->dom, d:S2->dom, e:S2->dom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nT = schema {"
			+ "\n	nodes T;"
			+ "\n	edges x:T->dom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nQ = polynomial {"
			+ "\n	 q = {for v:S1, u:S2; where v.b=u.c; attributes x=v.b; edges;} : T"
			+ "\n} : S -> T"
			+ "\n"
			+ "\nJ = instance {"
			+ "\n	variables j1 j2 j3 j4 : T;"
			+ "\n	equations j1.x=1, j2.x=2, j3.x=3;"
			+ "\n} : T"
			+ "\n"
			+ "\nI = coapply Q J"
			+ "\n"
			+ "\n///////////////////////////////////////////////////////"
			+ "\n/*"
			+ "\nSuppose S := T1(a,b) and T2(c,d,e)"
			+ "\nSuppose T := T(x)"
			+ "\n"
			+ "\nSuppose Q := "
			+ "\nSELECT b "
			+ "\nFROM T1(a,b) and T2(c,d,e)"
			+ "\nWHERE b=c"
			+ "\n"
			+ "\nSuppose J(1), J(2), J(3), J(y) [variables y, constants 1,2,3] "
			+ "\n"
			+ "\nThen frozen_Q = T1(a,b), T2(c,d,e), b=c [variables b, constants a c d e] "
			+ "\n"
			+ "\nThen coeval_Q(J) = chase ["
			+ "\n//additional EDs in S, T would go here"
			+ "\n_ => exists a c d e, T1(a,b), T2(c,d,e), b=c, b=1"
			+ "\n_ => exists a c d e, T1(a,b), T2(c,d,e), b=c, b=2"
			+ "\n_ => exists a c d e, T1(a,b), T2(c,d,e), b=c, b=3"
			+ "\n_ => exists a c d e, T1(a,b), T2(c,d,e), b=c, b=y"
			+ "\n] "
			+ "\nthe above is obtained by \"running\" frozen_Q on J. it is also an"
			+ "\ninstance "
			+ "\n{ "
			+ "\n   T1 (a, 1), T2 (1, d, e), "
			+ "\n   T1 (a, 2), T2 (2, d, e), "
			+ "\n   T1 (a, 3), T2 (3, d, e), "
			+ "\n   T1 (a, y), T2 (y, d, e)"
			+ "\n }"
			+ "\n"
			+ "\nIf eval_Q(I) = {2, 3, x}, the possible homomorphisms J => eval_Q(I) are"
			+ "\n"
			+ "\nh1 := y |-> 2"
			+ "\nh2 := y |-> 3"
			+ "\nh3 := y |-> x"
			+ "\n"
			+ "\nEach of these is also a homomorphism coeval_Q(J) => I; i.e.,  is the same thing as a valuation in I for coeval_Q(J)'s variables."
			+ "\n*/"
			+ "\n";



}
