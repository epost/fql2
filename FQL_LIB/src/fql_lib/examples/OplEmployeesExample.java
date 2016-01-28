package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class OplEmployeesExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S = theory { "
			+ "\n sorts"
			+ "\n 	Employee, Department, dom;"
			+ "\n symbols"
			+ "\n     first,last: Employee -> dom,"
			+ "\n     name 	: Department -> dom,"
			+ "\n	manager   : Employee -> Employee,"
			+ "\n	worksIn   : Employee -> Department,"
			+ "\n	secretary : Department -> Employee;"
			+ "\n equations  "
			+ "\n  	forall x. worksIn(manager(x)) = worksIn(x),"
			+ "\n  	forall x. worksIn(secretary(x)) = x,"
			+ "\n  	forall x. manager(manager(x)) = manager(x);"
			+ "\n}"
			+ "\n"
			+ "\nI = presentation {"
			+ "\n	generators a, b, c : Employee, "
			+ "\n	          m, s : Department,"
			+ "\n	          Al, Akin, Bob, Bo, Carl, Cork, Math, Cs : dom;"
			+ "\n	equations first(a) = Al,   last(a) = Akin,"
			+ "\n			first(b) = Bob,  last(b) = Bo,"
			+ "\n			first(c) = Carl, last(c) = Cork,"
			+ "\n			name(m)  = Math, name(s) = Cs,"
			+ "\n			worksIn(a) = m,  worksIn(b) = m,  worksIn(c) = s,"
			+ "\n			secretary(s) = c, secretary(m) = b;"
			+ "\n} : S"
			+ "\n"
			+ "\nJ = saturate I"
			+ "\nK = unsaturate J"
			+ "\nL = saturate K"
			+ "\n"
			+ "\nC = schema {"
			+ "\n	entities Employee, Department;"
			+ "\n} : S"
			+ "\n"
			+ "\nQ = query {"
			+ "\n	 qE = {for e:Employee; "
			+ "\n	 	  where; "
			+ "\n	 	  attributes first=first(e), last=last(e); "
			+ "\n	 	  edges manager = {e=manager(e)} : qE, worksIn = {d=worksIn(e)} : qD;} : Employee,"
			+ "\n	 qD = {for d:Department; "
			+ "\n	 	  where; "
			+ "\n	 	  attributes name=name(d); "
			+ "\n	 	  edges secretary = {e=secretary(d)} : qE;} : Department"
			+ "\n} : C -> C"
			+ "\n"
			+ "\nQ0 = id C"
			+ "\n";



}
