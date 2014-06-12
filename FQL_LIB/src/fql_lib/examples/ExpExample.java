package fql_lib.examples;

public class ExpExample extends Example {

	@Override
	public String getName() {
		return "Exponentials";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s ="//Exponentials in C-Set"
			+ "\n"
			+ "\ncategory C = {"
			+ "\n         objects a, b;"
			+ "\n         arrows  f : a -> b;"
			+ "\n         equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor I = {"
			+ "\n         objects a -> {1,2,3}, b -> {4,5};"
			+ "\n         arrows f -> {(1,4),(2,5),(3,5)};"
			+ "\n} : C -> Set"
			+ "\n"
			+ "\nfunctor J = {"
			+ "\n         objects a -> {1,2}, b -> {4};"
			+ "\n         arrows f -> {(1,4),(2,4)};"
			+ "\n} : C -> Set"
			+ "\n"
			+ "\n//eta says these two are equal"
			+ "\nfunctor K = (J^I)"
			+ "\ntransform eta_lhs = curry eval J I "
			+ "\ntransform eta_rhs = id K"
			+ "\n"
			+ "\n //beta says that for any G, G = tr"
			+ "\nfunctor IJ = (I * J)"
			+ "\nfunctor one= unit C Set "
			+ "\ntransform G = tt IJ"
			+ "\ntransform tr = (((fst I J ; curry G) * snd I J) ; eval unit C Set J)"
			+ "\n"
;


}
