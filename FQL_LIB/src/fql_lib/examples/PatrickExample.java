package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class PatrickExample extends Example {
	
	@Override
	public Language lang() {
		return Language.FPQL;
	}


	@Override
	public String getName() {
		return "Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = 
					  "dom : type"
					+ "\nAl Akin Bob Bo Carl Cork Math Cs : dom"
					+ "\n"
					+ "\nS = schema { "
					+ "\n nodes"
					+ "\n 	Employee, Department;"
					+ "\n edges"
					+ "\n     first 	: Employee -> dom,"
					+ "\n     last		: Employee -> dom,"
					+ "\n     name 	: Department -> dom,"
					+ "\n	manager   : Employee -> Employee,"
					+ "\n	worksIn   : Employee -> Department,"
					+ "\n	secretary : Department -> Employee;"
					+ "\n equations  "
					+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
					+ "\n  	Department.secretary.worksIn = Department,"
					+ "\n  	Employee.manager.manager = Employee.manager;"
					+ "\n}"
					+ "\n"
					+ "\nI = instance {"
					+ "\n	variables a b c : Employee, m s : Department;"
					+ "\n	equations a.first = Al, a.last = Akin,"
					+ "\n			b.first = Bob, b.last = Bo,"
					+ "\n			c.first = Carl, c.last = Cork,"
					+ "\n			m.name = Math, s.name = Cs,"
					+ "\n			a.worksIn = m, b.worksIn = m, c.worksIn = s,"
					+ "\n			s.secretary=c, m.secretary=b;"
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
