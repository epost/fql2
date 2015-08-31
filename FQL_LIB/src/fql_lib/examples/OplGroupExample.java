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
	
	String s = "//try with e vs ee" 
			+ "\nGroup = theory {"
			+ "\n	sorts "
			+ "\n		S;"
			+ "\n	symbols "
			+ "\n		ee : -> S,"
			+ "\n		I : S -> S,"
			+ "\n		o : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x:S. o(ee(),x) = x,"
			+ "\n		forall x:S. o(I(x),x) = ee(),"
			+ "\n		forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\n/*LR = theory {"
			+ "\n	sorts "
			+ "\n		S;"
			+ "\n	symbols "
			+ "\n		e : -> S,"
			+ "\n		I : S -> S,"
			+ "\n		o : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x:S. o(e(),x) = x,"
			+ "\n		forall x:S. o(x,I(x)) = e(),"
			+ "\n		forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\n"
			+ "\nRL = theory {"
			+ "\n	sorts "
			+ "\n		S;"
			+ "\n	symbols "
			+ "\n		e : -> S,"
			+ "\n		I : S -> S,"
			+ "\n		o : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x:S. o(x,e()) = x,"
			+ "\n		forall x:S. o(I(x),x) = e(),"
			+ "\n		forall x:S, y:S, z:S. o(o(x,y),z)=o(x,o(y,z));"
			+ "\n}"
			+ "\nDist = theory {"
			+ "\n	sorts"
			+ "\n		S;"
			+ "\n	symbols"
			+ "\n		p : S,S -> S,"
			+ "\n		q : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x:S, y:S, z:S. p(x,q(y,z)) = q(p(x,y),p(x,z));"
			+ "\n}*/"
			+ "\n";



}
