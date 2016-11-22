package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplTyTest2Example extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	@Override
	public String getName() {
		return "Ty Test";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S = theory { "
			+ "\n sorts"
			+ "\n 	Person, Nat;"
			+ "\n symbols"
			+ "\n 	parent@5 : Person -> Person,"
			+ "\n 	age@3 : Person -> Nat,"
			+ "\n	zero@0 : Nat,"
			+ "\n	infinity@1 : Nat,"
			+ "\n	succ@2 : Nat -> Nat,"
			+ "\n	plus@4 : Nat,Nat -> Nat;"
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
			+ "\n	 	  return age=succ(age(pA)); "
			+ "\n	 	  keys parent={pA=pA, pB=pB} : qP;} : Person //use pA=pB and pB=pA to fail equality check"
			+ "\n} : C -> C"
			+ "\n"
			+ "\nI0 = instance C I none"
			+ "\n"
			+ "\nJ0 = apply Q I0"
			+ "\n"
			+ "\nT = types C"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols zero -> \"return 0\","
			+ "\n			succ -> \"return input[0]+1\","
			+ "\n			plus -> \"return input[0]+input[1]\","
			+ "\n		    infinity -> \"return 0\";"
			+ "\n} : T"
			+ "\n"
			+ "\nI1 = instance C I M"
			+ "\n"
			+ "\nJ1 = apply Q I1"
			+ "\n"
			+ "\n//////////////////////////////////////////////////////////////////////////////////////////////////"
			+ "\nK = presentation {"
			+ "\n	generators undef2:Nat, billy, george, sue, alice : Person;"
			+ "\n	equations"
			+ "\nage(sue)=succ(succ(billy.age)),"
			+ "\nage(billy) = succ(infinity),"
			+ "\nage(alice)=succ(zero); "
			+ "\n} : S"
			+ "\nK0 = instance C K none"
			+ "\n"
			+ "\nh = transpres {"
			+ "\n	sorts Nat -> {(undef,succ(billy.age))}, "
			+ "\n		 Person -> {(bill,billy), (george,george.parent), (sue,sue), (william,billy), (alice,alice)};"
			+ "\n} : I0 -> K0  "
			+ "\n"
			+ "\noplquh0 = apply Q h"
			+ "\n";





}
