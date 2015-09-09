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
			+ "\n     first@10 	: Employee -> dom,"
			+ "\n     last@11		: Employee -> dom,"
			+ "\n     name@12 	: Department -> dom,"
			+ "\n	manager@13   : Employee -> Employee,"
			+ "\n	worksIn@14   : Employee -> Department,"
			+ "\n	secretary@15 : Department -> Employee;"
			+ "\n equations  "
			+ "\n  	forall x:Employee. worksIn(manager(x)) = worksIn(x),"
			+ "\n  	forall x:Department. worksIn(secretary(x)) = x,"
			+ "\n  	forall x:Employee. manager(manager(x)) = manager(x);"
			+ "\n}"
			+ "\n"
			+ "\nI = presentation {"
			+ "\n	generators a@1:Employee, b@2:Employee, c@3:Employee,"
			+ "\n	          m@1:Department, s@2:Department,"
			+ "\n	          Al@1:dom, Akin@2:dom, Bob@3:dom, Bo@4:dom, "
			+ "\n	          Carl@5:dom, Cork@6:dom, Math@7:dom, Cs@8:dom;"
			+ "\n	equations forall. first(a()) = Al(),  forall. last(a()) = Akin(),"
			+ "\n			forall. first(b()) = Bob(), forall. last(b()) = Bo(),"
			+ "\n			forall. first(c()) = Carl(),forall. last(c()) = Cork(),"
			+ "\n			forall. name(m())  = Math(),forall. name(s()) = Cs(),"
			+ "\n			forall. worksIn(a()) = m(), forall. worksIn(b()) = m(), forall. worksIn(c()) = s(),"
			+ "\n			forall. secretary(s()) = c(), forall . secretary(m()) = b();"
			+ "\n} : S"
			+ "\n"
			+ "\nJ = saturate I"
			+ "\nK = unsaturate J"
			+ "\nL = saturate K"
			+ "\n";



}
