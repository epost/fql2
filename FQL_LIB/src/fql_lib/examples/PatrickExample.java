package fql_lib.examples;

public class PatrickExample extends Example {
	
	@Override
	public boolean isPatrick() {
		return true;
	}

	@Override
	public String getName() {
		return "P Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = 
			"S = schema { "
					+ "\n nodes"
					+ "\n 	Employee, Department;"
					+ "\n edges"
					+ "\n	manager   : Employee -> Employee,"
					+ "\n	worksIn   : Employee -> Department,"
					+ "\n	secretary : Department -> Employee;"
					+ "\n equations  "
					+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
					+ "\n  	Department.secretary.worksIn = Department,"
					+ "\n  	Employee.manager.manager = Employee.manager;"
					+ "\n}"
					+ "\n"
					+ "\nT = S"
					+ "\n"
					+ "\n//////////////////////////////////////////////"
					+ "\n"
					+ "\nString = type \"java.lang.String\""
					+ "\nInt = type \"java.lang.Integer\""
					+ "\n"
					+ "\none = constant Int \"fql.primitives.One\""
					+ "\ntwo = constant Int \"fql.primitives.Two\""
					+ "\nfoo = constant String \"fql.primitives.Foo\""
					+ "\n"
					+ "\n//length = function String -> Int \"fql.primitives.Length\""
					+ "\nprint = function Int -> String \"fql.primitives.Print\""
					+ "\n//succ = function Int -> Int \"fql.primitives.Succ\""
					+ "\nreverse = function String -> String \"fql.primitives.Reverse\""
					+ "\n"
					+ "\neq1 = assume reverse.reverse = String "
					+ "\n"
					+ "\nX = schema { "
					+ "\n      nodes n;"
					+ "\n      edges f : n -> n, g : n -> String, h : n -> Int;"
					+ "\n      equations n.f.f = f, f.g = f.g.reverse;"
					+ "\n}"
					+ "\n";



}
