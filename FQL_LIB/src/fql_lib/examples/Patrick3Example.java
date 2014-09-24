package fql_lib.examples;

public class Patrick3Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P Cyclic";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "CM53 = schema {"
			+ "\n nodes M;"
			+ "\n edges arr : M -> M;"
			+ "\n equations M.arr.arr.arr.arr.arr = M.arr.arr;"
			+ "\n}"
			+ "\n"
			+ "\nCM33 = schema {"
			+ "\n nodes J;"
			+ "\n edges a : J -> J;"
			+ "\n equations J.a.a.a = J;"
			+ "\n}"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n nodes M -> J;"
			+ "\n edges arr -> J.a;"
			+ "\n} : CM53 -> CM33"
			+ "\n"

;

}
