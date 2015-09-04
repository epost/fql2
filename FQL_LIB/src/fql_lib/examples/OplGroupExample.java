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
			+ "\n	e : -> S,"
			+ "\n	I : S -> S,"
			+ "\n	o : S,S -> S;"
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
			+ "\n	e : -> S,"
			+ "\n	I : S -> S,"
			+ "\n	o : S,S -> S;"
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
			+ "\n	e : -> S,"
			+ "\n	I : S -> S,"
			+ "\n	o : S,S -> S;"
			+ "\n equations"
			+ "\n	forall x:S. o(x,e()) = x,"
			+ "\n	forall x:S. o(I(x),x) = e(),"
			+ "\n	forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n";



	



}
