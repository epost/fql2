package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplUnfailingExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	} 

	@Override
	public String getName() {
		return "Unfailing";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "Entropic = theory {"
			+ "\n	 sorts "
			+ "\n		S;"
			+ "\n	 symbols"
			+ "\n		o : S,S -> S;"
			+ "\n	 equations"
			+ "\n	 	forall x, y, z, w. o(o(x,y),o(z,w)) = o(o(x,z),o(y,w)),"
			+ "\n		forall x, y. o(o(x,y),x) = x;"
			+ "\n}"
			+ "\n"
			+ "\n//will fail without semantic AC"
			+ "\nAC = theory {"
			+ "\n	sorts "
			+ "\n		S;"
			+ "\n	symbols"
			+ "\n		o : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x, y, z. o(o(x,y),z) = o(x,o(y,z)),"
			+ "\n		forall x, y. o(x,y) = o(y,x),"
			+ "\n		forall x, y, z. o(x,o(y,z)) = o(y,o(x,z));"
			+ "\n}"
			+ "\n"
			+ "\n/*BooleanRing = theory {"
			+ "\n	sorts"
			+ "\n		S;"
			+ "\n	symbols"
			+ "\n		plus : S,S -> S,"
			+ "\n		times : S,S -> S,"
			+ "\n		zero :  S,"
			+ "\n		one : S;"
			+ "\n	equations"
			+ "\n		forall x, y, z.  plus(plus(x,y),z) = plus(x,plus(y,z)),"
			+ "\n		forall x, y, z.  times(times(x,y),z) = times(x,times(y,z)),"
			+ "\n		forall x, y. plus(x,y) = plus(y,x),	"
			+ "\n		forall x, y. times(x,y) = times(y,x),"
			+ "\n		forall x, y, z. plus(x,plus(y,z)) = plus(y,plus(x,z)),"
			+ "\n		forall x, y, z. times(x,times(y,z)) = times(y,times(x,z)),"
			+ "\n		forall x, y, z. times(x,plus(y,z)) = plus(times(x,y),times(x,z)),"
			+ "\n		forall x, y, z. times(plus(x,y),z) = plus(times(x,z),times(y,z)),"
			+ "\n		forall x. plus(zero,x) = x,"
			+ "\n		forall x. plus(x,zero) = x,"
			+ "\n		forall x. times(one,x) = x,"
			+ "\n		forall x. times(x,one) = x,"
			+ "\n		forall x. times(zero,x) = zero,"
			+ "\n		forall x. times(x,zero) = zero,"
			+ "\n		forall x. plus(x,x) = zero,"
			+ "\n		forall x. times(x,x) = x,"
			+ "\n		forall x, y. plus(x,plus(x,y)) = y,"
			+ "\n		forall x, y. times(x,times(x,y)) = times(x,y);"
			+ "\n}*/"
			+ "\n";


	
}
