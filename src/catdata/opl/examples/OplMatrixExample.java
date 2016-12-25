package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplMatrixExample extends Example {

	@Override
	public String getName() {
		return "Matrix";
	}

	@Override
	public String getText() {
		return s;
	}

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	private final String s = "//algebraic theory for boxes of wiring diagram 2.27"
			+ "\nT = theory {"
			+ "\n	sorts "
			+ "\n		A, B, C;"
			+ "\n	symbols "
			+ "\n		X1 : A -> B, "
			+ "\n		X2 : B -> C,"
			+ "\n		a : A;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n//a functor T -> \"Matrix\"? . I got this from section 2.4"
			+ "\nImpl = javascript {"
			+ "\n	symbols"
			+ "\n	_preamble -> \"java.lang.Class.forName('catdata.ide.Util'); "
			+ "\n                  var Util = Java.type('catdata.ide.Util');\","
			+ "\n	_compose -> \"return Util.multiply_many(input[0](), input[1]);\", //comose is matrix multiply"
			+ "\n	X1 -> \"return Util.mat_conv2([[1,2],[3,0]])\","
			+ "\n	X2 -> \"return Util.mat_conv2([[2,2,0],[3,1,1]])\","
			+ "\n	a -> \"return Util.mat_conv2([[1,0],[0,1]])\";"
			+ "\n} : T"
			+ "\n"
			+ "\n//prints [[8, 4, 2], [6, 6, 0]]"
			+ "\nJ = eval Impl X2(X1(a()))"
			+ "\n";

}
