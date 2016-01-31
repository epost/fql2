package catdata.fqlpp.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class DeltaExample extends Example {

	@Override
	public Language lang() {
		return Language.FQLPP;
	}

	@Override
	public String getName() {
		return "Delta";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "category C = {"
			+ "\n objects "
			+ "\n	T1, T2, string, int;"
			+ "\n arrows"
			+ "\n	t1_ssn    : T1 -> string,"
			+ "\n	t1_first  : T1 -> string,"
			+ "\n	t1_last   : T1 -> string,"
			+ "\n	t2_first  : T2 -> string,"
			+ "\n	t2_last   : T2 -> string,"
			+ "\n	t2_salary : T2 -> int;"
			+ "\n equations; "
			+ "\n}"
			+ "\n"
			+ "\ncategory D = {"
			+ "\n objects "
			+ "\n	T, string, int;"
			+ "\n arrows"
			+ "\n	ssn0    : T -> string,"
			+ "\n	first0  : T -> string,"
			+ "\n	last0   : T -> string,"
			+ "\n	salary0 : T -> int;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor F = {"
			+ "\n objects "
			+ "\n	T1 -> T,"
			+ "\n	T2 -> T,"
			+ "\n	string -> string,"
			+ "\n	int -> int;"
			+ "\n arrows"
			+ "\n	t1_ssn    -> T.ssn0,"
			+ "\n	t1_first  -> T.first0,"
			+ "\n	t2_first  -> T.first0,"
			+ "\n	t1_last   -> T.last0,"
			+ "\n	t2_last   -> T.last0,"
			+ "\n	t2_salary -> T.salary0;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nfunctor J = {"
			+ "\n objects "
			+ "\n	T -> { XF667, XF891, XF221 },"
			+ "\n	string -> { \"115-234\", \"112-988\", \"198-887\", Bob, Sue, Alice, Smith, Jones },"
			+ "\n	int -> { 250, 100, 300 };"
			+ "\n arrows"
			+ "\n	ssn0    -> { (XF667, \"115-234\"),(XF891,\"112-988\"),(XF221,\"198-887\") },"
			+ "\n	first0  -> { (XF667,Bob),(XF891,Sue),(XF221,Alice) },"
			+ "\n	last0   -> { (XF667,Smith),(XF891,Smith),(XF221,Jones) },"
			+ "\n	salary0 -> { (XF667,250),(XF891,300),(XF221,100) };"
			+ "\n} : D -> Set"
			+ "\n"
			+ "\nfunctor df = delta F"
			+ "\n"
			+ "\nfunctor I = apply df on object J"
			+ "\n"
			+ "\nfunctor J0 = {"
			+ "\n objects "
			+ "\n	T -> { XF66,XF89,XF22, xxx },"
			+ "\n	string -> { \"115-23\", \"112-98\", \"198-88\", Bo, Su, Alic, Smit, Jone, xxx },"
			+ "\n	int -> { 25, 10, 30, 0 };"
			+ "\n arrows"
			+ "\n	ssn0    -> { (XF66, \"115-23\"),(XF89,\"112-98\"),(XF22,\"198-88\"), (xxx,\"xxx\") },"
			+ "\n	first0  -> { (XF66,Bo),(XF89,Su),(XF22,Alic),(xxx, \"xxx\" )},"
			+ "\n	last0   -> { (XF66,Smit),(XF89,Smit),(XF22,Jone), (xxx, \"xxx\") },"
			+ "\n	salary0 -> { (XF66,25),(XF89,30),(XF22,10), (xxx, 0) };"
			+ "\n} : D -> Set"
			+ "\n"
			+ "\ntransform trans = {"
			+ "\n objects "
			+ "\n 	T -> {(XF667,XF66),(XF891,XF89),(XF221,XF22)},"
			+ "\n 	string -> { (\"115-234\",\"115-23\"), (\"112-988\",\"112-98\"), (\"198-887\",\"198-88\"), "
			+ "\n 	            (Bob,Bo), (Sue,Su), (Alice,Alic), (Smith,Smit), (Jones,Jone) },"
			+ "\n 	int -> {(250,25),(100,10),(300,30)};"
			+ "\n} : (J: D -> Set) -> (J0: D -> Set)  "
			+ "\n"
			+ "\nfunctor I0 = apply df on object J0"
			+ "\n"
			+ "\ntransform trans0 = apply df on arrow trans"
			+ "\n";



}
