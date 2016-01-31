package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplSKExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	} 
	
	@Override
	public String getName() {
		return "SK";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "SK = theory {"
			+ "\n	sorts "
			+ "\n		U;"
			+ "\n	symbols "
			+ "\n		o : U,U -> U,"
			+ "\n		S, K, a, b, c : U;"
			+ "\n	equations	"
			+ "\n		forall x, y. o(o(K, x), y) = x,"
			+ "\n		forall x, y, z. o(o(o(S, x), y), z) = o(o(x, z), o(y, z));"
			+ "\n}"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols"
			+ "\n		a -> \"return \\\"a\\\"\","
			+ "\n		b -> \"return \\\"b\\\"\","
			+ "\n		c -> \"return \\\"c\\\"\","
			+ "\n		K -> \"return function(x) { return function(y) { return x } }\","
			+ "\n		S -> \"return function(x) { return function(y) { return function(z) { return (x(z))(y(z)) } } }\","
			+ "\n		o -> \"return input[0](input[1])\";"
			+ "\n} : SK"
			+ "\n"
			+ "\na = eval M a()"
			+ "\nb = eval M b()"
			+ "\nKa = eval M o(K(), a()) "
			+ "\nKab = eval M o(o(K(), a()), b()) // = a()"
			+ "\n"
			+ "\nSK0 = eval M o(S(), K())"
			+ "\nSKK0 = eval M o(o(S(), K()), K()) "
			+ "\nSKKa = eval M o(o(o(S(), K()), K()), a()) // = a()"
			+ "\n";



}
