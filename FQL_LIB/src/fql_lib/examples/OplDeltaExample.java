package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class OplDeltaExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Delta";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "C = theory {"
			+ "\n 	sorts "
			+ "\n		T1, T2, string, int;"
			+ "\n 	symbols"
			+ "\n		t1_ssn, t1_first, t1_last : T1 -> string,"
			+ "\n		t2_first, t2_last : T2 -> string,"
			+ "\n		t2_salary : T2 -> int;"
			+ "\n 	equations; "
			+ "\n}"
			+ "\n"
			+ "\nD = theory {"
			+ "\n 	sorts "
			+ "\n		T, string, int;"
			+ "\n 	symbols"
			+ "\n		ssn0, first0, last0    : T -> string,"
			+ "\n		salary0 : T -> int;"
			+ "\n 	equations;"
			+ "\n}"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n 	sorts "
			+ "\n		T1 -> T,"
			+ "\n		T2 -> T,"
			+ "\n		string -> string,"
			+ "\n		int -> int;"
			+ "\n 	symbols"
			+ "\n		t1_ssn    -> forall x:T. ssn0(x),"
			+ "\n		t1_first  -> forall x:T. first0(x),"
			+ "\n		t2_first  -> forall x:T. first0(x),"
			+ "\n		t1_last   -> forall x:T. last0(x),"
			+ "\n		t2_last   -> forall x:T. last0(x),"
			+ "\n		t2_salary -> forall x:T. salary0(x);"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nJ = model {"
			+ "\n	sorts "
			+ "\n		T -> { XF667, XF891, XF221 },"
			+ "\n		string -> { \"115-234\", \"112-988\", \"198-887\", Bob, Sue, Alice, Smith, Jones },"
			+ "\n		int -> { 250, 100, 300 };"
			+ "\n 	symbols"
			+ "\n		ssn0    -> { ((XF667), \"115-234\"),((XF891),\"112-988\"),((XF221),\"198-887\") },"
			+ "\n		first0  -> { ((XF667),Bob),((XF891),Sue),((XF221),Alice) },"
			+ "\n		last0   -> { ((XF667),Smith),((XF891),Smith),((XF221),Jones) },"
			+ "\n		salary0 -> { ((XF667),250),((XF891),300),((XF221),100) };"
			+ "\n} : D "
			+ "\n"
			+ "\ndeltaFJ = delta F J"
			+ "\n"
			+ "\nJ0 = model {"
			+ "\n sorts "
			+ "\n	T -> { XF66,XF89,XF22, xxx },"
			+ "\n	string -> { \"115-23\", \"112-98\", \"198-88\", Bo, Su, Alic, Smit, Jone, xxx },"
			+ "\n	int -> { 25, 10, 30, 0 };"
			+ "\n symbols"
			+ "\n	ssn0    -> { ((XF66), \"115-23\"),((XF89),\"112-98\"),((XF22),\"198-88\"), ((xxx),\"xxx\") },"
			+ "\n	first0  -> { ((XF66),Bo),((XF89),Su),((XF22),Alic),((xxx), \"xxx\" )},"
			+ "\n	last0   -> { ((XF66),Smit),((XF89),Smit),((XF22),Jone), ((xxx), \"xxx\") },"
			+ "\n	salary0 -> { ((XF66),25),((XF89),30),((XF22),10), ((xxx), 0) };"
			+ "\n} : D"
			+ "\n"
			+ "\nh = transform {"
			+ "\n sorts "
			+ "\n 	T -> {(XF667,XF66),(XF891,XF89),(XF221,XF22)},"
			+ "\n 	string -> { (\"115-234\",\"115-23\"), (\"112-988\",\"112-98\"), (\"198-887\",\"198-88\"), "
			+ "\n 	            (Bob,Bo), (Sue,Su), (Alice,Alic), (Smith,Smit), (Jones,Jone) },"
			+ "\n 	int -> {(250,25),(100,10),(300,30)};"
			+ "\n} : J -> J0"
			+ "\n"
			+ "\nh0 = delta F h"
			+ "\n";



}
