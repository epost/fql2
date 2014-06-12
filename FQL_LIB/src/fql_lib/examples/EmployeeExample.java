package fql_lib.examples;

public class EmployeeExample extends Example {

	@Override
	public String getName() {
		return "Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "category S = { "
			+ "\n objects"
			+ "\n 	Employee, Department;"
			+ "\n arrows"
			+ "\n	manager   : Employee -> Employee,"
			+ "\n	worksIn   : Employee -> Department,"
			+ "\n	secretary : Department -> Employee;"
			+ "\n equations  "
			+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
			+ "\n  	Department.secretary.worksIn = Department,"
			+ "\n  	Employee.manager.manager = Employee.manager; "
			+ "\n}"
			+ "\n"
			+ "\nfunctor I = {"
			+ "\n objects"
			+ "\n	Employee -> { 101, 102, 103 },"
			+ "\n	Department -> { q10, x02 };"
			+ "\n arrows"
			+ "\n	manager -> { (101, 103), (102, 102), (103, 103) } : {101,102,103} -> {101,102,103},"
			+ "\n	worksIn -> { (101, q10), (102, x02), (103, q10) } : {101,102,103} -> {q10,x02},"
			+ "\n	secretary -> { (q10, 101), (x02, 102) } : {q10,x02} -> {101,102} ;"
			+ "\n} : S -> Set"
			+ "\n"
			+ "\nset emps = { 101, 102, 103 }"
			+ "\nset depts = { q10, x02 }"
			+ "\nfunction foo = { (101,(101,q10)),(102,(102,x02)),(103,(103,q10)) } : emps -> (emps * depts)"
			+ "\n"
			+ "\nfunctor J = {"
			+ "\n objects"
			+ "\n	Employee -> emps,"
			+ "\n	Department -> depts;"
			+ "\n arrows"
			+ "\n	manager -> id emps,"
			+ "\n	worksIn -> (foo ; snd emps depts),"
			+ "\n	secretary -> { (q10, 101), (x02, 102) } : depts -> emps ;"
			+ "\n} : S -> Set";






}
