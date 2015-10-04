package fql_lib.examples;

public class OplEmployeesExample extends Example {
	
	@Override
	public String isPatrick() {
		return "OPL";
	}

	@Override
	public String getName() {
		return "O Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S = theory { "
			+ "\n sorts"
			+ "\n 	Employee, Department, dom;"
			+ "\n symbols"
			+ "\n     first 	: Employee -> dom,"
			+ "\n     last 	: Employee -> dom,"
			+ "\n     name 	: Department -> dom,"
			+ "\n	manager   : Employee -> Employee,"
			+ "\n	worksIn   : Employee -> Department,"
			+ "\n	secretary : Department -> Employee;"
			+ "\n equations  "
			+ "\n  	forall x:Employee. worksIn(manager(x)) = worksIn(x),"
			+ "\n  	forall x:Department. worksIn(secretary(x)) = x,"
			+ "\n  	forall x:Employee. manager(manager(x)) = manager(x);"
			+ "\n}"
			+ "\n"
			+ "\nI = presentation {"
			+ "\n	generators a:Employee, b:Employee, c:Employee,"
			+ "\n	          m:Department, s:Department,"
			+ "\n	          Al:dom, Akin:dom, Bob:dom, Bo:dom, "
			+ "\n	          Carl:dom, Cork:dom, Math:dom, Cs:dom;"
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
			+ "\n";



}
