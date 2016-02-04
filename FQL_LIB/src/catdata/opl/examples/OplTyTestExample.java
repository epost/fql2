package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplTyTestExample extends Example {

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
			+ "\n 	parent@4 : Person -> Person,"
			+ "\n 	age@2 : Person -> Nat,"
			+ "\n	zero@0 : Nat,"
			+ "\n	succ@1 : Nat -> Nat,"
			+ "\n	plus@3 : Nat,Nat -> Nat;"
			+ "\n equations  "
			+ "\n  	forall x. plus(zero,x) = x,"
			+ "\n  	forall x, y. plus(succ(x),y) = succ(plus(x,y));"
			+ "\n}"
			+ "\n"
			+ "\nI = presentation {"
			+ "\n	generators infinity : Nat, "
			+ "\n		      bill, george, sue, william, alice : Person;"
			+ "\n	equations bill = william, "
			+ "\n	          age(alice)=succ(zero), parent(alice)=bill,"
			+ "\n	          age(bill) = infinity, parent(bill)=george,"
			+ "\n	          age(george) = succ(infinity), parent(george)=sue,"
			+ "\n	          age(sue)=zero, parent(sue) = william;"
			+ "\n} : S"
			+ "\n"
			+ "\nC = schema {"
			+ "\n	entities Person;"
			+ "\n} : S"
			+ "\n"
			+ "\nI0 = instance C I none"
			+ "\n"
			+ "\nQ = query {"
			+ "\n	 q = {for p1:Person, p2:Person; "
			+ "\n	      where p1 = parent(parent(parent(p1))); "
			+ "\n	      return age = plus(age(p1),age(p2)); "
			+ "\n	      keys parent = {p1=parent(p2), p2=parent(p1)} : q;} : Person"
			+ "\n} : C -> C"
			+ "\n "
			+ "\nJ0 = apply Q I0"
			+ "\n";




}
