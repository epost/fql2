package catdata.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplMod4Example extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	} 
	
	@Override
	public String getName() {
		return "Mod4";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Mod4 = theory {"
			+ "\n	sorts "
			+ "\n		Nat;"
			+ "\n	symbols "
			+ "\n		zero : Nat,"
			+ "\n		succ : Nat -> Nat;"
			+ "\n	equations"
			+ "\n		forall x. succ(succ(succ(succ(x)))) = x;"
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
