package fql_lib.examples;

public class OplStackExample extends Example {
	
	@Override
	public String isPatrick() {
		return "opl";
	}

	@Override
	public String getName() {
		return "O Stack";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//requires disabling mapping check"
			+ "\nC = theory {"
			+ "\n 	sorts "
			+ "\n		E;"
			+ "\n 	symbols"
			+ "\n		z : E,"
			+ "\n		s : E -> E,"
			+ "\n		plus : E,E -> E;"
			+ "\n 	equations"
			+ "\n 		forall x. plus(z(), x) = x,"
			+ "\n 		forall x, y. plus(s(x), y) = s(plus(x,y));"
			+ "\n //		forall x, y. plus(x,y) = plus(y,x);"
			+ "\n}"
			+ "\n"
			+ "\nCSem = javascript {"
			+ "\n	symbols"
			+ "\n		z -> \"function z(input) { return 0; }\","
			+ "\n		s -> \"function s(input) { return (input[0] + 1); }\","
			+ "\n		plus -> \"function plus(input) { return (input[0] + input[1]); }\";"
			+ "\n} : C "
			+ "\n"
			+ "\nplus_sz_sz = eval CSem plus(s(z()), s(z())) //2"
			+ "\n"
			+ "\nD = theory {"
			+ "\n 	sorts "
			+ "\n		N, S;"
			+ "\n 	symbols"
			+ "\n		z : N,"
			+ "\n		i : N,"
			+ "\n		empty : S,"
			+ "\n		push : S,N -> S,"
			+ "\n		plus : S -> S,"
			+ "\n		append : S,S -> S;"
			+ "\n	equations"
			+ "\n		forall x. append(empty(), x) = x,"
			+ "\n		forall x. append(x, empty()) = x,"
			+ "\n		//etc"
			+ "\n"
			+ "\n		/* equations below should be provable: */"
			+ "\n		"
			+ "\n		//forall x. plus(z(), x) = x"
			+ "\n		forall x. plus(append(push(empty(), z()), x)) = x, "
			+ "\n		"
			+ "\n 		//forall x, y. plus(s(x), y) = s(plus(x,y))"
			+ "\n 		forall x, y. plus(append(plus(append(x, push(empty(), i()))), y)) "
			+ "\n 		               = plus(append(plus(append(x,y)), push(empty(), i()))); "
			+ "\n}"
			+ "\n"
			+ "\nDSem = javascript {"
			+ "\n	symbols"
			+ "\n		z -> \"function z(input) { return 0; }\","
			+ "\n		i -> \"function i(input) { return 1; }\","
			+ "\n		empty -> \"function empty(input) { return []; }\","
			+ "\n		plus -> \"function plus(input) { var x = input[0].slice(0); var a = x.pop(); var b = x.pop(); x.push(a+b); return x; }\","
			+ "\n		push -> \"function push(input) { return [ input[1] ].concat(input[0]); }\","
			+ "\n		append -> \"function append(input) { return input[0].concat(input[1]); }\";"
			+ "\n} : D "
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	sorts "
			+ "\n		E -> S;"
			+ "\n	symbols"
			+ "\n		z -> forall. push(empty(), z()),"
			+ "\n		s -> forall x0:S. plus(append(x0, push(empty(), i()))),"
			+ "\n		plus -> forall x0:S, y0:S. plus(append(x0,y0));		"
			+ "\n} : C -> D "
			+ "\n";



}
