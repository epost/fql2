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
					+ "\nI = instance {"
					+ "\n	variables e:Employee;"
					+ "\n	equations;"
					+ "\n} : S"
					+ "\n"
					+ "\n//////////////////////////////////////////////"
					+ "\n"
					+ "\nString : type"
					+ "\nInt : type"
					+ "\n"
					+ "\none : Int"
					+ "\ntwo : Int"
					+ "\nfoo : String"
					+ "\n"
					+ "\n//length : String -> Int"
					+ "\nprint : Int -> String"
					+ "\n//succ : Int -> Int"
					+ "\nreverse : String -> String"
					+ "\n"
					+ "\neq1 : reverse.reverse = String "
					+ "\n"
					+ "\nX = schema { "
					+ "\n      nodes n;"
					+ "\n      edges f : n -> n, g : n -> String, h : n -> Int;"
					+ "\n      equations n.f.f = f, f.g = f.g.reverse;"
					+ "\n}"
					+ "\n";



}
