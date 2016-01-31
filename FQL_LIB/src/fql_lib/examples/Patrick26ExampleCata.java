package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class Patrick26ExampleCata extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Nat";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "/*"
			+ "\n * Nat(X) := 1+X"
			+ "\n * Nat(f) := 1+f"
			+ "\n * Nat := lfix X. Nat(X)"
			+ "\n * "
			+ "\n * in : 1 + Nat -> Nat"
			+ "\n * out: Nat -> 1 + Nat"
			+ "\n * in o out = id"
			+ "\n * out o id  = id"
			+ "\n * "
			+ "\n * fold(f) : Nat -> Y for f : 1+Y -> Y "
			+ "\n * in o fold(f) = Nat(fold(f)) o f  "
			+ "\n */"
			+ "\nS = schema {"
			+ "\n	nodes  One"
			+ "\n	      ,Nat"
			+ "\n	      ,Nat_mod_2 "
			+ "\n	      ,One_Plus_Nat"
			+ "\n	      ,One_Plus_Nat_mod_2"
			+ "\n	      ;"
			+ "\n	edges zero : One -> Nat_mod_2"
			+ "\n		,succ : Nat_mod_2 -> Nat_mod_2"
			+ "\n		,in : One_Plus_Nat -> Nat"
			+ "\n	     ,out : Nat -> One_Plus_Nat"
			+ "\n	     ,inj1 : Nat -> One_Plus_Nat"
			+ "\n	     ,inj2 : One -> One_Plus_Nat"
			+ "\n	     ,inj3 : Nat_mod_2 -> One_Plus_Nat_mod_2"
			+ "\n	     ,inj4 : One -> One_Plus_Nat_mod_2"
			+ "\n	     ,f : One_Plus_Nat_mod_2 -> Nat_mod_2"
			+ "\n	     ,fold_f : Nat -> Nat_mod_2"
			+ "\n	     ,Natfold_f : One_Plus_Nat -> One_Plus_Nat_mod_2 "
			+ "\n		;"
			+ "\n	equations  succ . succ = Nat_mod_2"
			+ "\n			,in . out = One_Plus_Nat "
			+ "\n	          ,out . in = Nat"
			+ "\n	          ,inj4 . f = zero"
			+ "\n	          ,inj3 . f = succ"
			+ "\n	          ,inj1 . Natfold_f = fold_f . inj3 "
			+ "\n	          ,inj2 . Natfold_f = inj4 "
			+ "\n	          ,in . fold_f = Natfold_f . f "
			+ "\n	          ; //missing equations about coproducts and terminal objects"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables;"
			+ "\n	equations;"
			+ "\n} : S"
			+ "\n"
			+ "\n// One.inj2.in.fold_f  ----> zero"
			+ "\n// One.inj2.in.inj1.in.fold_f ----> zero.succ"
			+ "\n// One.inj2.in.inj1.in.inj1.in.fold_f ---> zero"
			+ "\n"

;

}
