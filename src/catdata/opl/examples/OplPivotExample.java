package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplPivotExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Pivot";
	}

	@Override
	public String getText() {
		return s;
	}

	private final String s = "Type = theory {"
			+ "\n	sorts String, Nat;"
			+ "\n	symbols"
			+ "\n		Adam, Bill : String,"
			+ "\n		Four : Nat;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nS = SCHEMA {"
			+ "\n	entities"
			+ "\n		Person, Home;"
			+ "\n	edges"
			+ "\n		livesIn : Person -> Home;"
			+ "\n	attributes"
			+ "\n		name : Person -> String,"
			+ "\n		size : Home -> Nat;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Type"
			+ "\n"
			+ "\nI = INSTANCE {"
			+ "\n	generators"
			+ "\n		p1, p2: Person,"
			+ "\n		h: Home;"
			+ "\n	equations"
			+ "\n		p1.livesIn = h, p1.name = Adam, p2.name = Bill,"
			+ "\n		p2.livesIn = h, h.size = Four;"
			+ "\n} : S"
			+ "\n"
			+ "\nJ = pivot I"
			+ "\n";


}
