package catdata.aql.examples;

public class AqlDeltaExample extends AqlExample {

	@Override
	public String getName() {
		return "Delta";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "typeside Ty = literal {"
			+ "\n	types"
			+ "\n		int"
			+ "\n	constants"
			+ "\n		\"100\" \"150\" \"200\" \"250\" \"300\" : int"
			+ "\n	java_types"
			+ "\n		string = \"java.lang.String\""
			+ "\n	java_constants"
			+ "\n		string = \"return input[0]\""
			+ "\n}"
			+ "\n"
			+ "\nschema C = literal : Ty {"
			+ "\n 	entities "
			+ "\n		T1 T2"
			+ "\n 	attributes"
			+ "\n		t1_ssn t1_first t1_last : T1 -> string"
			+ "\n		t2_first t2_last : T2 -> string"
			+ "\n		t2_salary : T2 -> int"
			+ "\n}"
			+ "\n"
			+ "\nschema D = literal : Ty {"
			+ "\n 	entities "
			+ "\n		T"
			+ "\n 	attributes"
			+ "\n		ssn0 first0 last0 : T -> string"
			+ "\n		salary0 : T -> int"
			+ "\n}"
			+ "\n"
			+ "\nmapping F = literal : C -> D {"
			+ "\n 	entities "
			+ "\n		T1 -> T"
			+ "\n		T2 -> T"
			+ "\n 	attributes"
			+ "\n		t1_ssn    -> lambda x:T. ssn0(x)"
			+ "\n		t1_first  -> lambda x:T. first0(x)"
			+ "\n		t2_first  -> lambda x:T. first0(x)"
			+ "\n		t1_last   -> lambda x:T. last0(x)"
			+ "\n		t2_last   -> lambda x:T. last0(x)"
			+ "\n		t2_salary -> lambda x:T. salary0(x)"
			+ "\n} "
			+ "\n"
			+ "\ninstance J = literal : D {"
			+ "\n	generators "
			+ "\n		XF667 XF891 XF221 : T"
			+ "\n	equations"
			+ "\n		XF667.ssn0 = \"115-234\" XF891.ssn0 = \"112-988\" XF221.ssn0 = \"198-887\""
			+ "\n//		XF667.first0 = Bob XF891.first0 = Sue XF221.first0 = Alice"
			+ "\n		XF667.last0 = Smith XF891.last0 = Smith XF221.last0 = Jones"
			+ "\n		XF667.salary0 = 250 XF891.salary0 = 300 XF221.salary0 = 100"
			+ "\n}  "
			+ "\n"
			+ "\ninstance deltaFJ = delta F J"
			+ "\n"
			+ "\ninstance J0 = literal : D {"
			+ "\n generators "
			+ "\n	XF22 aXF66 XF89  xxx : T "
			+ "\n equations"
			+ "\n	aXF66.ssn0 = \"115-234\" XF89.ssn0 = \"112-988\" XF22.ssn0 = \"198-887\""
			+ "\n	/* aXF66.first0 = Bob XF89.first0 = Sue */ XF22.first0 = Alice"
			+ "\n	aXF66.last0 = Smith XF89.last0 = Smith XF22.last0 = Jones"
			+ "\n	aXF66.salary0 = 250 XF89.salary0 = 300 XF22.salary0 = 100"
			+ "\n}"
			+ "\n"
			+ "\ntransform h = literal : J -> J0 {"
			+ "\n generators"
			+ "\n	 XF667 -> aXF66 "
			+ "\n	 XF891 -> XF89 "
			+ "\n	 XF221 -> XF22"
			+ "\n}"
			+ "\n"
			+ "\ntransform h0 = delta F h"
			+ "\n"
			+ "\ninstance sigmadeltaFJ = sigma F deltaFJ"
			+ "\n"
			+ "\ntransform u = counit F J"
			+ "\n";



}
