package fql_lib.examples;

public class Patrick14Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P Flower";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S = schema {"
			+ "\n	nodes two, three;"
			+ "\n	edges;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nTwoThree = instance {"
			+ "\n	variables a:two, b:two, c:three, d:three, e:three;"
			+ "\n	equations;"
			+ "\n} : S"
			+ "\n"
			+ "\nSix = flower {"
			+ "\n	select;"
			+ "\n	from two as x, three as y;"
			+ "\n	where;"
			+ "\n} TwoThree"
			+ "\n";





}
