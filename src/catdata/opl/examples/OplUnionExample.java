package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplUnionExample extends Example {

	@Override
	public String getName() {
		return "Union";
	}

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "Type = theory {"
			+ "\n	sorts"
			+ "\n		String;"
			+ "\n	symbols"
			+ "\n		;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nA = SCHEMA {"
			+ "\n	entities"
			+ "\n		Person, Food, X;"
			+ "\n	edges"
			+ "\n		favorite1 : Person -> Food,"
			+ "\n		favorite2 : Person -> Food;"
			+ "\n	attributes"
			+ "\n		foodname : Food -> String;"
			+ "\n	pathEqualities"
			+ "\n		forall x. x.favorite1 = x.favorite2;"
			+ "\n	obsEqualities"
			+ "\n		forall x. x.favorite1.foodname = x.favorite2.foodname;"
			+ "\n} : Type"
			+ "\n"
			+ "\nB = SCHEMA {"
			+ "\n	entities"
			+ "\n		Department, Employee, X;"
			+ "\n	edges"
			+ "\n		worksIn1 : Employee -> Department,"
			+ "\n		worksIn2 : Employee -> Department;"
			+ "\n	attributes"
			+ "\n		deptname : Department -> String;"
			+ "\n	pathEqualities"
			+ "\n		forall x. x.worksIn1 = x.worksIn2;"
			+ "\n	obsEqualities"
			+ "\n		forall x. x.worksIn1.deptname = x.worksIn1.deptname;"
			+ "\n} : Type"
			+ "\n"
			+ "\nC = union Type {A B}"
			+ "\n"
			+ "\n"
			+ "\nI = INSTANCE {"
			+ "\n	generators"
			+ "\n		p1, p2 : Person,"
			+ "\n		f1 : Food,"
			+ "\n		x : X;"
			+ "\n	equations"
			+ "\n		p1.favorite1 = f1;"
			+ "\n} : A"
			+ "\n"
			+ "\nJ = INSTANCE {"
			+ "\n	generators"
			+ "\n		d1, d2 : Person,"
			+ "\n		e1 : Food,"
			+ "\n		e : X;"
			+ "\n	equations"
			+ "\n		d1.favorite1 = e1;"
			+ "\n} : A"
			+ "\n"
			+ "\nK = union A {I J}"
			+ "\n";



}
