package fql_lib.examples;

public class OplUnfailingExample extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}

	@Override
	public String getName() {
		return "O Unfailing";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Entropic = theory {"
			+ "\n	 sorts "
			+ "\n		S;"
			+ "\n	 symbols"
			+ "\n		o : S,S -> S;"
			+ "\n	 equations"
			+ "\n	 	forall x:S, y:S, z:S, w:S. o(o(x,y),o(z,w)) = o(o(x,z),o(y,w)),"
			+ "\n		forall x:S, y:S. o(o(x,y),x) = x;"
			+ "\n}"
			+ "\n"
			+ "\n//will fail"
			+ "\nAC = theory {"
			+ "\n	sorts "
			+ "\n		S;"
			+ "\n	symbols"
			+ "\n		o : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x:S, y:S, z:S. o(o(x,y),z) = o(x,o(y,z)),"
			+ "\n		forall x:S, y:S. o(x,y) = o(y,x),"
			+ "\n		forall x:S, y:S, z:S. o(x,o(y,z)) = o(y,o(x,z));"
			+ "\n}"
			+ "\n"
			+ "\n/*BooleanRing = theory {"
			+ "\n	sorts"
			+ "\n		S;"
			+ "\n	symbols"
			+ "\n		plus : S,S -> S,"
			+ "\n		times : S,S -> S,"
			+ "\n		zero : -> S,"
			+ "\n		one : -> S;"
			+ "\n	equations"
			+ "\n		forall x:S, y:S, z:S.  plus(plus(x,y),z) = plus(x,plus(y,z)),"
			+ "\n		forall x:S, y:S, z:S.  times(times(x,y),z) = times(x,times(y,z)),"
			+ "\n		forall x:S, y:S. plus(x,y) = plus(y,x),	"
			+ "\n		forall x:S, y:S. times(x,y) = times(y,x),"
			+ "\n		forall x:S, y:S, z:S. plus(x,plus(y,z)) = plus(y,plus(x,z)),"
			+ "\n		forall x:S, y:S, z:S. times(x,times(y,z)) = times(y,times(x,z)),"
			+ "\n		forall x:S, y:S, z:S. times(x,plus(y,z)) = plus(times(x,y),times(x,z)),"
			+ "\n		forall x:S, y:S, z:S. times(plus(x,y),z) = plus(times(x,z),times(y,z)),"
			+ "\n		forall x:S. plus(zero(),x) = x,"
			+ "\n		forall x:S. plus(x,zero()) = x,"
			+ "\n		forall x:S. times(one(),x) = x,"
			+ "\n		forall x:S. times(x,one()) = x,"
			+ "\n		forall x:S. times(zero(),x) = zero(),"
			+ "\n		forall x:S. times(x,zero()) = zero(),"
			+ "\n		forall x:S. plus(x,x) = zero(),"
			+ "\n		forall x:S. times(x,x) = x,"
			+ "\n		forall x:S, y:S. plus(x,plus(x,y)) = y,"
			+ "\n		forall x:S, y:S. times(x,times(x,y)) = times(x,y);"
			+ "\n}*/"
			+ "\n";


	
}
