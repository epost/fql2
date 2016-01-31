package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class OplJSExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "JS";
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
			+ "\n		succ, pred : Int -> Int,"
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
			+ "\n		_preamble -> \"javax.swing.JOptionPane.showMessageDialog(null, \\\"hello\\\")\","
			+ "\n		zero -> \"return 0\","
			+ "\n		succ -> \"return (input[0] + 1)\","
			+ "\n		pred -> \"return (input[0] - 1)\","
			+ "\n		length -> \"return input[0].length\","
			+ "\n		reverse -> \"return input[0].split('').reverse().join('')\","
			+ "\n		append -> \"return input[0].concat(input[1])\","
			+ "\n		print -> \"return input[0].toString()\";"
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
