package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class OplFlowerExamle extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Flower";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Mod4 = theory {"
			+ "\n	sorts"
			+ "\n		Nat;"
			+ "\n	symbols"
			+ "\n		O : Nat,"
			+ "\n		S : Nat -> Nat,"
			+ "\n		plus : Nat , Nat -> Nat;"
			+ "\n	equations"
			+ "\n		forall x. S(S(S(S(x)))) = x,"
			+ "\n		forall x, y. plus(S(x),y) = S(plus(x,y));"
			+ "\n}"
			+ "\n"
			+ "\nFour = model {"
			+ "\n	sorts"
			+ "\n		Nat -> {0,1,2,3};"
			+ "\n	symbols"
			+ "\n		O -> { ((),0) },"
			+ "\n		S -> { ((0),1), ((1),2), ((2),3), ((3),0) },"
			+ "\n		plus -> {  ((0,0), 0), ((0,1),1), ((0,2),2), ((0,3),3),"
			+ "\n				 ((1,0), 1), ((1,1),2), ((1,2),3), ((1,3),0),"
			+ "\n		           ((2,0), 2), ((2,1),3), ((2,2),0), ((2,3),1),"
			+ "\n		           ((3,0), 3), ((3,1),0), ((3,2),1), ((3,3),2) };"
			+ "\n} : Mod4"
			+ "\n"
			+ "\nTwo = model {"
			+ "\n	sorts"
			+ "\n		Nat -> {x0,x1};"
			+ "\n	symbols"
			+ "\n		O -> { ((),x0) },"
			+ "\n		S -> { ((x0),x1), ((x1),x0) },"
			+ "\n		plus -> {  ((x0,x0), x0), ((x0,x1),x1),"
			+ "\n				 ((x1,x0), x1), ((x1,x1),x0) };"
			+ "\n} : Mod4"
			+ "\n"
			+ "\nh = transform {"
			+ "\n	sorts"
			+ "\n		Nat -> {(0,x0),(1,x1),(2,x0),(3,x1)};"
			+ "\n} : Four -> Two "
			+ "\n"
			+ "\nQ = flower {"
			+ "\n	select S(n1) as col1, S(n2) as col2;"
			+ "\n	from Nat as n1, Nat as n2;"
			+ "\n	where plus(n1,n2) = O();"
			+ "\n} Four"
			+ "\n"
			+ "\nhQ = flower {"
			+ "\n	select S(n1) as col1, S(n2) as col2;"
			+ "\n	from Nat as n1, Nat as n2;"
			+ "\n	where plus(n1,n2) = O();"
			+ "\n} h"
			+ "\n";





}
