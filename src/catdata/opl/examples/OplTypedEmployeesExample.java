package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplTypedEmployeesExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	@Override
	public String getName() {
		return "Ty Emp";
	}

	@Override
	public String getText() {
		return s;
	}

	private final String s = "S0 = theory { "
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
			+ "\n		Al -> \"return \\\"Al\\\"\","
			+ "\n		Akin -> \"return \\\"Akin\\\"\","
			+ "\n		Bob -> \"return \\\"Bob\\\"\","
			+ "\n		Bo -> \"return \\\"Bo\\\"\","
			+ "\n		Carl -> \"return \\\"Carl\\\"\","
			+ "\n		Cork -> \"return \\\"Cork\\\"\","
			+ "\n		Dan -> \"return \\\"Dan\\\"\","
			+ "\n		Dunn -> \"return \\\"Dunn\\\"\","
			+ "\n		Math -> \"return \\\"Math\\\"\","
			+ "\n		CS -> \"return \\\"CS\\\"\","
			+ "\n		zero -> \"return 0\","
			+ "\n		succ -> \"return (input[0] + 1)\","
			+ "\n		plus -> \"return (input[0] + input[1])\","
			+ "\n		length -> \"return input[0].length\","
			+ "\n		reverse -> \"return input[0].split('').reverse().join('')\","
			+ "\n		append -> \"return input[0].concat(input[1])\","
			+ "\n		print -> \"return input[0].toString()\";"
			+ "\n} : T"
			+ "\n"
			+ "\nI = instance S I0 M"
			+ "\n";


}
