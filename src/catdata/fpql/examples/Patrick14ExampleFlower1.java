package catdata.fpql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick14ExampleFlower1 extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Flower";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "adom : type"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes two, three;"
			+ "\n	edges two_att : two -> adom, three_att : three -> adom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nTwoThree = instance {"
			+ "\n	variables a b : two, c d e : three;"
			+ "\n	equations;"
			+ "\n} : S"
			+ "\n"
			+ "\n(Six:T) = flower {"
			+ "\n	select x.two_att as c1, y.three_att as c2;"
			+ "\n	from two as x, three as y;"
			+ "\n	where;"
			+ "\n} TwoThree"
			+ "\n";







}
