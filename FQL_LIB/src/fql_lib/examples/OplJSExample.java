package fql_lib.examples;

public class OplJSExample extends Example {
	
	@Override
	public String isPatrick() {
		return "opl";
	}

	@Override
	public String getName() {
		return "O JS";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "T = theory {"
			+ "\n	sorts "
			+ "\n		Int, String;"
			+ "\n	symbols "
			+ "\n		zero : Int,"
			+ "\n		succ : Int -> Int,"
			+ "\n		pred : Int -> Int,"
			+ "\n		length : String -> Int,"
			+ "\n		reverse : String -> String,"
			+ "\n		append : String, String -> String,"
			+ "\n		print : Int -> String;"
			+ "\n	equations	"
			+ "\n		forall x. pred(succ(x)) = x,"
			+ "\n		forall x. succ(pred(x)) = x,"
			+ "\n		forall x. length(x) = length(reverse(x));"
			+ "\n}"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols"
			+ "\n		zero -> \"function zero(input) { return 0; }\","
			+ "\n		succ -> \"function succ(input) { return (input[0] + 1); }\","
			+ "\n		pred -> \"function pred(input) { return (input[0] - 1); }\","
			+ "\n		length -> \"function length(input) { return input[0].length; }\","
			+ "\n		reverse -> \"function reverse(input) { return input[0].split('').reverse().join(''); }\","
			+ "\n		append -> \"function append(input) { return input[0].concat(input[1]); }\","
			+ "\n		print -> \"function print(input) { return input[0].toString(); }\";"
			+ "\n} : T"
			+ "\n"
			+ "\nz = eval M zero()"
			+ "\nsz = eval M succ(zero())"
			+ "\nssz = eval M succ(succ(zero()))"
			+ "\npssz = eval M pred(succ(succ(zero())))"
			+ "\n"
			+ "\ntest1 = eval M append(print(succ(zero())), print(zero()))"
			+ "\nrev_test1 = eval M reverse(append(print(succ(zero())), print(zero())))"
			+ "\nlen_test1 = eval M length(reverse(append(print(succ(zero())), print(zero()))))"
			+ "\n";



}
