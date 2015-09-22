package fql_lib.examples;

public class OplGroupExample extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}
	
	@Override
	public String getName() {
		return "O KB";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Group = theory {"
			+ "\n sorts "
			+ "\n	S;"
			+ "\n symbols"
			+ "\n	e@1 : -> S,"
			+ "\n	I@3 : S -> S,"
			+ "\n	o@2 : S,S -> S;"
			+ "\n equations"
			+ "\n	forall x:S. o(e(),x) = x,"
			+ "\n	forall x:S. o(I(x),x) = e(),"
			+ "\n	forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\nLR = theory {"
			+ "\n sorts "
			+ "\n	S;"
			+ "\n symbols"
			+ "\n	e@1 : -> S,"
			+ "\n	I@3 : S -> S,"
			+ "\n	o@2 : S,S -> S;"
			+ "\n equations"
			+ "\n	forall x:S. o(e(),x) = x,"
			+ "\n	forall x:S. o(x,I(x)) = e(),"
			+ "\n	forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\nRL = theory {"
			+ "\n sorts "
			+ "\n	S;"
			+ "\n symbols"
			+ "\n	e@1 : -> S,"
			+ "\n	I@3 : S -> S,"
			+ "\n	o@2 : S,S -> S;"
			+ "\n equations"
			+ "\n	forall x:S. o(x,e()) = x,"
			+ "\n	forall x:S. o(I(x),x) = e(),"
			+ "\n	forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\nOddEvenMod4 = theory {"
			+ "\n	sorts "
			+ "\n		Odd, Even;"
			+ "\n	symbols "
			+ "\n		zero@0 : -> Even,"
			+ "\n		one@1 : -> Odd,"
			+ "\n		incO@2 : Odd -> Even,"
			+ "\n		incE@3 : Even -> Odd;"
			+ "\n	equations"
			+ "\n		forall x:Odd. incE(incO(incE(incO(x)))) = x,"
			+ "\n		forall x:Even. incO(incE(incO(incE(x)))) = x;	"
			+ "\n}"
			+ "\n"
			+ "\nArith = theory {"
			+ "\n	sorts"
			+ "\n		N;"
			+ "\n	symbols"
			+ "\n		zero@1 : -> N,"
			+ "\n		succ@2 : N -> N,"
			+ "\n		plus@3 : N,N -> N,"
			+ "\n		times@4 : N,N -> N;  "
			+ "\n	equations"
			+ "\n		forall x:N, y:N. plus(succ(x),y) = succ(plus(x,y)),"
			+ "\n		forall x:N, y:N. times(succ(x),y) = plus(x,times(x,y));"
			+ "\n}"
/*			+ "\n"
			+ "\nEntropic = theory {"
			+ "\n	 sorts "
			+ "\n		S;"
			+ "\n	 symbols"
			+ "\n		o : S,S -> S;"
			+ "\n	 equations"
			+ "\n	 	forall x:S, y:S, z:S, w:S. o(o(x,y),o(z,w)) = o(o(x,z),o(y,w)),"
			+ "\n		forall x:S, y:S. o(o(x,y),x) = x;"
			+ "\n	}"
			*/
			+ "\n";




	



}
