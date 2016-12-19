package catdata.aql.examples;

public class AqlOuterjoinExample extends AqlExample {

	@Override
	public String getName() {
		return "Outer Join";
	}

	@Override
	public String getText() {
		return s;
	}

	private final String s = "//Outer joins using java's Optional class"
			+ "\n"
			+ "\ntypeside Ty = literal {"
			+ "\n	java_types"
			+ "\n		String = \"java.lang.String\""
			+ "\n		Nat = \"java.lang.Integer\""
			+ "\n		Bool = \"java.lang.Boolean\""
			+ "\n		NullableNat = \"java.util.Optional\""
			+ "\n	java_constants"
			+ "\n		String = \"return input[0]\""
			+ "\n		Nat = \"return java.lang.Integer.parseInt(input[0])\""
			+ "\n		Bool = \"return java.lang.Boolean.parseBool(input[0])\""
			+ "\n		NullableNat = \"return java.lang.Integer.parseInt(input[0])\""
			+ "\n	java_functions"
			+ "\n		null : -> NullableNat = \"return java.util.Optional.empty()\""
			+ "\n		inNat : Nat -> NullableNat = \"return java.util.Optional.of(input[0])\""
			+ "\n		eqNat : Nat, Nat -> Bool = \"return (input[0].equals(input[1]))\""
			+ "\n		ifNat : Bool, NullableNat, NullableNat -> NullableNat = "
			+ "\n		  \"if (input[0]) { return input[1]; } else { return input[2]; }\""
			+ "\n}		  "
			+ "\n"
			+ "\nschema S = literal : Ty {"
			+ "\n	entities"
			+ "\n		A B"
			+ "\n	attributes"
			+ "\n		Aname : A -> String"
			+ "\n		Bname : B -> String"
			+ "\n		Aid : A -> Nat"
			+ "\n		Bid : B -> Nat"
			+ "\n}"
			+ "\n"
			+ "\nschema T = literal : Ty {"
			+ "\n	entities"
			+ "\n		C"
			+ "\n	attributes"
			+ "\n		CAname : C -> String"
			+ "\n		CBname : C -> String"
			+ "\n		Cid : C -> NullableNat"
			+ "\n} "
			+ "\n"
			+ "\nquery OuterJoin = literal : S -> T {"
			+ "\n 	entities "
			+ "\n	 	C  -> {from a:A b:B"
			+ "\n 			  return Cid -> ifNat(eqNat(a.Aid, b.Bid), inNat(a.Aid), null)"
			+ "\n 			         CAname -> a.Aname"
			+ "\n 		     	    CBname -> b.Bname"
			+ "\n 		  	} "
			+ "\n}  "
			+ "\n"
			+ "\ninstance I = literal : S {"
			+ "\n	generators"
			+ "\n		a1 a2 : A"
			+ "\n		b2 b3 : B"
			+ "\n	equations"
			+ "\n		a1.Aname = alice a1.Aid = \"1\""
			+ "\n		a2.Aname = bob a2.Aid = \"2\""
			+ "\n		b2.Bname = charlie b2.Bid = \"2\""
			+ "\n		b3.Bname = dave b3.Bid = \"3\""
			+ "\n} "
			+ "\n"
			+ "\ninstance J = eval OuterJoin I"
			+ "\n";


}
