package fql_lib.examples;

public class OplTypedEmployeesExample extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}
	
	@Override
	public String getName() {
		return "O Ty Emp";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "S0 = theory { "
			+ "\n sorts"
			+ "\n 	Employee, Department, string, nat;"
			+ "\n symbols"
			+ "\n 	Al, Akin, Bob, Bo, Carl, Cork, Dan, Dunn, Math, CS : string,"
			+ "\n 	zero 	: nat,"
			+ "\n 	succ		: nat -> nat,"
			+ "\n 	plus		: nat, nat -> nat,"
			+ "\n 	print	: nat -> string,"
			+ "\n 	length 	: string -> nat,"
			+ "\n 	reverse 	: string -> string,"
			+ "\n 	append	: string, string -> string,"
			+ "\n     first, last, middle 	: Employee -> string,"
			+ "\n     age		: Employee -> nat,"
			+ "\n     name 	: Department -> string,"
			+ "\n	manager   : Employee -> Employee,"
			+ "\n	worksIn   : Employee -> Department,"
			+ "\n	secretary : Department -> Employee;"
			+ "\n equations  "
			+ "\n 	forall x. plus(x,zero) = x,"
			+ "\n 	forall x, y. plus(succ(x),y) = succ(plus(x,y)),"
			+ "\n 	forall x. reverse(reverse(x)) = x,"
			+ "\n 	forall x. length(x) = length(reverse(x)),"
			+ "\n 	forall x, y. length(append(x,y)) = plus(length(x),length(y)),"
			+ "\n  	forall x. worksIn(manager(x)) = worksIn(x),"
			+ "\n  	forall x. worksIn(secretary(x)) = x;"
			+ "\n  //	forall x. manager(manager(x)) = manager(x); can finitize at instance level "
			+ "\n}"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	entities"
			+ "\n		Employee, Department;	"
			+ "\n} : S0"
			+ "\n"
			+ "\nE = entities S"
			+ "\nA = attributes S"
			+ "\nT = types S"
			+ "\nEA = entitiesAndAttributes S"
			+ "\n"
			+ "\nI0 = presentation {"
			+ "\n	generators a, b, c : Employee, "
			+ "\n	           m, s : Department;"
			+ "\n	equations first(a) = Al, "
			+ "\n			first(b) = Bob,  last(b) = Bo,"
			+ "\n			first(c) = Carl, "
			+ "\n			name(m)  = Math, name(s) = CS,"
			+ "\n			age(a) = age(c), //eq of 2 skolems"
			+ "\n			last(a) = last(b), //eq 1 skolem, 1 not"
			+ "\n			middle(a) = append(first(a),first(b)), //computation"
			+ "\n			middle(b) = append(first(a),middle(c)), //partial computation"
			+ "\n			manager(a) = b, manager(b) = b, manager(c) = c,"
			+ "\n			worksIn(a) = m,  worksIn(b) = m,  worksIn(c) = s,"
			+ "\n			secretary(s) = c, secretary(m) = b;"
			+ "\n} : S0"
			+ "\n"			
			+ "\nM = javascript {"
			+ "\n	symbols"
			+ "\n		Al -> \"function Al(input) { return \\\"Al\\\" }\","
			+ "\n		Akin -> \"function Akin(input) { return \\\"Akin\\\" }\","
			+ "\n		Bob -> \"function Bob(input) { return \\\"Bob\\\" }\","
			+ "\n		Bo -> \"function Bo(input) { return \\\"Bo\\\" }\","
			+ "\n		Carl -> \"function Carl(input) { return \\\"Carl\\\" }\",		"
			+ "\n		Cork -> \"function Cork(input) { return \\\"Cork\\\" }\","
			+ "\n		Dan -> \"function Dan(input) { return \\\"Dan\\\" }\",	"
			+ "\n		Dunn -> \"function Dunn(input) { return \\\"Dunn\\\" }\",	"
			+ "\n		Math -> \"function Math(input) { return \\\"Math\\\" }\","
			+ "\n		CS -> \"function CS(input) { return \\\"CS\\\" }\",						"
			+ "\n		zero -> \"function zero(input) { return 0; }\","
			+ "\n		succ -> \"function succ(input) { return (input[0] + 1); }\","
			+ "\n		plus -> \"function plus(input) { return (input[0] + input[1]); }\","
			+ "\n		length -> \"function length(input) { return input[0].length; }\","
			+ "\n		reverse -> \"function reverse(input) { return input[0].split('').reverse().join(''); }\","
			+ "\n		append -> \"function append(input) { return input[0].concat(input[1]); }\","
			+ "\n		print -> \"function print(input) { return input[0].toString(); }\";"
			+ "\n} : T"
			+ "\n"
			+ "\nI = instance S I0 M"
			+ "\n";


}
