package fql_lib.examples;

public class OplTyTest2Example extends Example {

	@Override
	public String isPatrick() {
		return "OPL";
	}
	
	@Override
	public String getName() {
		return "O Ty Test";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S = theory { "
			+ "\n sorts"
			+ "\n 	Person, Nat;"
			+ "\n symbols"
			+ "\n 	parent@4 : Person -> Person,"
			+ "\n 	age@2 : Person -> Nat,"
			+ "\n	zero@0 : Nat,"
			+ "\n	infinity@0 : Nat,"
			+ "\n	succ@1 : Nat -> Nat,"
			+ "\n	plus@3 : Nat,Nat -> Nat;"
			+ "\n equations  "
			+ "\n   forall x. parent(x) = x,"
			+ "\n  	forall x. plus(zero,x) = x,"
			+ "\n  	forall x, y. plus(succ(x),y) = succ(plus(x,y));"
			+ "\n}"
			+ "\n"
			+ "\nI = presentation {"
			+ "\n	generators undef : Nat, "
			+ "\n		      bill, george, sue, william, alice : Person;"
			+ "\n	equations bill = william, "
			+ "\n	          age(alice)=succ(zero),"
			+ "\n	          age(bill) = succ(infinity),"
			+ "\n// keep commented for testing	age(george) = succ(infinity), "
			+ "\n	          age(sue)=succ(undef);"
			+ "\n} : S"
			+ "\nC = schema {"
			+ "\n	entities Person;"
			+ "\n} : S"
			+ "\n"
			+ "\nQ = query {"
			+ "\n	 qP = {for pA:Person, pB:Person; "
			+ "\n	 	  where age(pA) = age(pB); "
			+ "\n	 	  attributes age=succ(age(pA)); "
			+ "\n	 	  edges parent={pA=pA, pB=pB} : qP;} : Person"
			+ "\n} : C -> C"
			+ "\n"
			+ "\nI0 = instance C I none"
			+ "\n"
			+ "\nJ0 = apply Q I0"
			+ "\n"
			+ "\nT = types C"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols zero -> \"return 0;\","
			+ "\n			succ -> \"return input[0]+1;\","
			+ "\n			plus -> \"return input[0]+input[1];\","
			+ "\n		    infinity -> \"return 0;\";"
			+ "\n} : T"
			+ "\n"
			+ "\nI1 = instance C I M"
			+ "\n"
			+ "\nJ1 = apply Q I1"
			+ "\n";



}
