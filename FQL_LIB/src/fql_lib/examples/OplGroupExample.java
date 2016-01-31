package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class OplGroupExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	@Override
	public String getName() {
		return "KB";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Group = theory {"
			+ "\n sorts "
			+ "\n	S;"
			+ "\n symbols"
			+ "\n	e : S,"
			+ "\n	I : S -> S,"
			+ "\n	o : S,S -> S;"
			+ "\n equations"
			+ "\n forall x. (e o x) = x,"
			+ "\n forall x. (I(x) o x) = e,"
			+ "\n forall x, y, z. ((x o y) o z) = (x o (y o z));"
			+ "\n}"
			+ "\nAGroupPresentation = presentation {"
			+ "\n	generators g : S;"
			+ "\n	equations o(g, o(g, g)) = g;"
			+ "\n} : Group"
			+ "\nAGroup = saturate AGroupPresentation"
			+ "\nAGroupPresentationAgain = unsaturate AGroup"
            + "\nAGroupAgain = saturate AGroupPresentationAgain" 
			+ "\n"
			+ "\nTrivialGroup = model {"
			+ "\n	sorts S -> {x};"
			+ "\n	symbols e -> { ((),x) },"
			+ "\n		   I -> { ((x),x) },"
			+ "\n		   o -> { ((x,x),x) };"
			+ "\n} : Group"
			+ "\n"
			+ "\nLR = theory {"
			+ "\n sorts "
			+ "\n	S;"
			+ "\n symbols"
			+ "\n	e : S,"
			+ "\n	I : S -> S,"
			+ "\n	o : S,S -> S;"
			+ "\n equations"
			+ "\n	forall x. o(e,x) = x,"
			+ "\n	forall x. o(x,I(x)) = e,"
			+ "\n	forall x, y, z. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\nRL = theory {"
			+ "\n sorts "
			+ "\n	S;"
			+ "\n symbols"
			+ "\n	e : S,"
			+ "\n	I : S -> S,"
			+ "\n	o : S,S -> S;"
			+ "\n equations"
			+ "\n	forall x. o(x,e) = x,"
			+ "\n	forall x. o(I(x),x) = e,"
			+ "\n	forall x, y, z. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\nOddEvenMod4 = theory {"
			+ "\n	sorts "
			+ "\n		Odd, Even;"
			+ "\n	symbols "
			+ "\n		zero@0 : Even,"
			+ "\n		one@1 : Odd,"
			+ "\n		incO@2 : Odd -> Even,"
			+ "\n		incE@3 : Even -> Odd;"
			+ "\n	equations"
			+ "\n		forall x. incE(incO(incE(incO(x)))) = x,"
			+ "\n		forall x. incO(incE(incO(incE(x)))) = x;	"
			+ "\n}"
			+ "\n"
			+ "\nArith = theory {"
			+ "\n	sorts"
			+ "\n		N;"
			+ "\n	symbols"
			+ "\n		zero@1 : N,"
			+ "\n		succ@2 : N -> N,"
			+ "\n		plus@3 : N,N -> N,"
			+ "\n		times@4 : N,N -> N;  "
			+ "\n	equations"
			+ "\n       forall x. plus(zero, x) = x,"
			+ "\n		forall x, y. plus(succ(x),y) = succ(plus(x,y)),"
			+ "\n		forall x, y. times(succ(x),y) = plus(x,times(x,y));"
			+ "\n}"
			+ "\n";




	



}
