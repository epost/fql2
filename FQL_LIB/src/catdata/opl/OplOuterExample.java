package catdata.opl;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplOuterExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Outer Join";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "preamble = pragma {"
			+ "\n	options"
			+ "\n		\"opl_prover_force_prec\" = \"true\";"
			+ "\n}"
			+ "\n"
			+ "\nTy = theory {"
			+ "\n	sorts"
			+ "\n		Nat, Bool, NullableNat, String;"
			+ "\n	symbols"
			+ "\n		alice, bob, charlie, dave, evan : String,"
			+ "\n		\"1\", \"2\", \"3\" : Nat,"
			+ "\n		nullNat : NullableNat,"
			+ "\n		inNat : Nat -> NullableNat,"
			+ "\n		tru, fals : Bool,"
			+ "\n		eqNat : Nat,Nat -> Bool,"
			+ "\n		ifNat : Bool, NullableNat, NullableNat -> NullableNat;"
			+ "\n	equations"
			+ "\n		eqNat(\"1\", \"2\") = fals,"
			+ "\n		eqNat(\"1\", \"3\") = fals,"
			+ "\n		eqNat(\"2\", \"1\") = fals,"
			+ "\n		eqNat(\"2\", \"3\") = fals,"
			+ "\n		eqNat(\"3\", \"1\") = fals,"
			+ "\n		eqNat(\"3\", \"2\") = fals,"
			+ "\n		eqNat(\"1\", \"1\") = tru,"
			+ "\n		eqNat(\"2\", \"2\") = tru,"
			+ "\n		eqNat(\"3\", \"3\") = tru,"
			+ "\n		forall x, y. ifNat(tru, x, y) = x,"
			+ "\n		forall x, y. ifNat(fals, x, y) = y;"
			+ "\n}"
			+ "\n"
			+ "\nS = SCHEMA {"
			+ "\n	entities"
			+ "\n		A, B;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		Aname : A -> String,"
			+ "\n		Bname : B -> String,"
			+ "\n		Aid : A -> Nat, "
			+ "\n		Bid : B -> Nat;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\nT= SCHEMA {"
			+ "\n	entities"
			+ "\n		C;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		CAname : C -> String,"
			+ "\n		CBname : C -> String,"
			+ "\n		Cid : C -> NullableNat;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\nOuterJoin = query {"
			+ "\n 	CQ = {for a:A, b:B;"
			+ "\n 		 where;"
			+ "\n 		 return Cid = ifNat(eqNat(a.Aid, b.Bid), inNat(a.Aid), nullNat),"
			+ "\n 		        CAname = a.Aname,"
			+ "\n 		        CBname = b.Bname;"
			+ "\n 		 keys;} : C"
			+ "\n} : S -> T "
			+ "\n"
			+ "\nI = INSTANCE {"
			+ "\n	generators"
			+ "\n		a1, a2 : A,"
			+ "\n		    b2, b3 : B;"
			+ "\n	equations"
			+ "\n		a1.Aname = alice, a1.Aid = \"1\","
			+ "\n		a2.Aname = bob, a2.Aid = \"2\","
			+ "\n		b2.Bname = charlie, b2.Bid = \"2\","
			+ "\n		b3.Bname = dave, b3.Bid = \"3\";"
			+ "\n} : S"
			+ "\n"
			+ "\nJ = apply OuterJoin I"
			+ "\n"
			+ "\n"
			+ "\n////////////////////////////////////////////"
			+ "\n"
			+ "\nTy2 = theory {"
			+ "\n	sorts"
			+ "\n		Nat, Bool, String;"
			+ "\n	symbols"
			+ "\n		alice, bob, charlie, dave, evan : String,"
			+ "\n		\"1\", \"2\", \"3\" : Nat,"
			+ "\n		null : Nat,"
			+ "\n		tru, fals : Bool,"
			+ "\n		eq : Nat,Nat -> Bool,"
			+ "\n		if : Bool, Nat, Nat -> Nat;"
			+ "\n	equations"
			+ "\n		eq(\"1\", \"2\") = fals,"
			+ "\n		eq(\"1\", \"3\") = fals,"
			+ "\n		eq(\"1\", null) = fals,"
			+ "\n		eq(\"2\", \"1\") = fals,"
			+ "\n		eq(\"2\", \"3\") = fals,"
			+ "\n		eq(\"2\", null) = fals,"
			+ "\n		eq(\"3\", \"1\") = fals,"
			+ "\n		eq(\"3\", \"2\") = fals,"
			+ "\n		eq(\"3\", null) = fals,"
			+ "\n		eq(\"1\", \"1\") = tru,"
			+ "\n		eq(\"2\", \"2\") = tru,"
			+ "\n		eq(\"3\", \"3\") = tru,"
			+ "\n		eq(null, null) = tru,"
			+ "\n		forall x, y. if(tru, x, y) = x,"
			+ "\n		forall x, y. if(fals, x, y) = y;"
			+ "\n}"
			+ "\n"
			+ "\nS2 = SCHEMA {"
			+ "\n	entities"
			+ "\n		A, B;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		Aname : A -> String,"
			+ "\n		Bname : B -> String,"
			+ "\n		Aid : A -> Nat, "
			+ "\n		Bid : B -> Nat;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty2"
			+ "\n"
			+ "\nT2 = SCHEMA {"
			+ "\n	entities"
			+ "\n		C;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		CAname : C -> String,"
			+ "\n		CBname : C -> String,"
			+ "\n		Cid : C -> Nat;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty2"
			+ "\n"
			+ "\nOuterJoin2 = query {"
			+ "\n 	CQ = {for a:A, b:B;"
			+ "\n 		 where;"
			+ "\n 		 return Cid = if(eq(a.Aid, b.Bid), a.Aid, null),"
			+ "\n 		        CAname = a.Aname,"
			+ "\n 		        CBname = b.Bname;"
			+ "\n 		 keys;} : C"
			+ "\n} : S2 -> T2 "
			+ "\n"
			+ "\nI2 = INSTANCE {"
			+ "\n	generators"
			+ "\n		a1, a2 : A,"
			+ "\n		    b2, b3 : B;"
			+ "\n	equations"
			+ "\n		a1.Aname = alice, a1.Aid = \"1\","
			+ "\n		a2.Aname = bob, a2.Aid = \"2\","
			+ "\n		b2.Bname = charlie, b2.Bid = \"2\","
			+ "\n		b3.Bname = dave, b3.Bid = \"3\";"
			+ "\n} : S2"
			+ "\n"
			+ "\nJ2 = apply OuterJoin2 I2"
			+ "\n";


}
