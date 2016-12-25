package catdata.fql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class TriangleExample extends Example {

	@Override
	public Language lang() {
		return Language.FQL;
	}
	
	@Override
	public String getName() {
		return "Triangle";
	}

	@Override
	public String getText() {
		return s;
	}

	private final String s =
			"schema C = {"
					+ "\n nodes "
					+ "\n 	A, B, C;"
					+ "\n attributes;"
					+ "\n arrows "
					+ "\n 	ax : A -> B, bx : B -> C, cx : C -> A; "
					+ "\n equations"
					+ "\n	A.ax.bx.cx = A,"
					+ "\n	B.bx.cx.ax = B,"
					+ "\n	C.cx.ax.bx = C;"
					+ "\n}\n"
					+ "\nschema D = opposite C\n"
					+ "\nmapping F = id C"
					+ "\nmapping Fop = opposite F"
					;
}
