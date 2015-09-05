package fql_lib.examples;

public class Mod4Example extends Example {

	@Override
	public String isPatrick() {
		return "opl";
	}
	
	@Override
	public String getName() {
		return "O Mod4";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Mod4 = theory {"
			+ "\n	sorts "
			+ "\n		Nat;"
			+ "\n	symbols "
			+ "\n		zero@0 : -> Nat,"
			+ "\n		succ@1 : Nat -> Nat;"
			+ "\n	equations"
			+ "\n		forall x:Nat. succ(succ(succ(succ(x)))) = x;"
			+ "\n}"
			+ "\n"
			+ "\nFour = model {"
			+ "\n	sorts"
			+ "\n		Nat -> {0,1,2,3};"
			+ "\n	symbols"
			+ "\n		zero -> { ((),0) },"
			+ "\n		succ -> { ((0),1), ((1),2), ((2),3), ((3),0) };"
			+ "\n} : Mod4"
			+ "\n"
			+ "\nTwo = model {"
			+ "\n	sorts"
			+ "\n		Nat -> {t, f};"
			+ "\n	symbols"
			+ "\n		zero -> { ((),t) },"
			+ "\n		succ -> { ((t),f), ((f),t) };"
			+ "\n} : Mod4"
			+ "\n"
			+ "\nh = transform {"
			+ "\n	sorts"
			+ "\n		Nat -> {(0,t),(1,f),(2,t),(3,f)};"
			+ "\n} : Four -> Two"
			+ "\n"
			+ "\nzero_nat = eval Four zero()"
			+ "\none_nat = eval Four succ(zero())"
			+ "\ntwo_nat = eval Four succ(succ(zero()))"
			+ "\nthree_nat = eval Four succ(succ(succ(zero())))"
			+ "\n"
			+ "\nzero_bool = eval Two zero()"
			+ "\none_bool = eval Two succ(zero())"
			+ "\ntwo_bool = eval Two succ(succ(zero()))"
			+ "\nthree_bool = eval Two succ(succ(succ(zero())))"
			+ "\n";



}
