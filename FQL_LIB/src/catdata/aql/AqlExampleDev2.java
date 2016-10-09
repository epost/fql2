package catdata.aql;

public class AqlExampleDev2 extends AqlExample {

	@Override
	public String getName() {
		return "Test 2";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "typeside T = literal {"
			+ "\n	types"
			+ "\n		Color Bool "
			+ "\n	constants"
			+ "\n		tru fals : Bool"
			+ "\n		red green blue cyan magenta yellow : Color"
			+ "\n		z : String"
			+ "\n	functions"
			+ "\n		isRGB : Color -> Bool"
			+ "\n	equations"
			+ "\n		isRGB(red) = tru"
			+ "\n		isRGB(green) = tru"
			+ "\n		isRGB(blue) = tru"
			+ "\n		isRGB(cyan) = fals"
			+ "\n		isRGB(magenta) = fals"
			+ "\n		isRGB(yellow) = fals"
			+ "\n	java_types"
			+ "\n		Integer = \"java.lang.Integer\""
			+ "\n		String = \"java.lang.String\""
			+ "\n	java_constants"
			+ "\n		Integer = \"return java.lang.Integer.parseInt(input[0])\""
			+ "\n		String = \"return input[0]\""
			+ "\n	java_functions"
			+ "\n		concat : String,String -> String = \"return input[0].concat(input[1])\"	"
			+ "\n	options"
			+ "\n		prover = saturated				"
			+ "\n}"
			+ "\n"
			+ "\nschema S = literal : T {"
			+ "\n	entities"
			+ "\n		Emp Dept"
			+ "\n	foreign_keys"
			+ "\n		worksIn : Emp -> Dept"
			+ "\n		secr : Dept -> Emp"
			+ "\n		mgr : Emp -> Emp"
			+ "\n	path_equations"
			+ "\n//		mgr . worksIn = worksIn"
			+ "\n//		worksIn . secr =  Emp //or literally blank instead of Emp"
			+ "\n	attributes"
			+ "\n		name : Emp -> String"
			+ "\n		salary : Emp -> Integer"
			+ "\n		color : Dept -> Color"
			+ "\n	observation_equations"
			+ "\n//		forall x. color(x) = color(x)"
			+ "\n	options"
			+ "\n		prover = saturated	"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = literal : S {"
			+ "\n	generators"
			+ "\n		e1 e2 e3 : Emp"
			+ "\n		d1 d2 : Dept"
			+ "\n		ultraviolet : Color"
			+ "\n		str : String"
			+ "\n	equations"
			+ "\n		e1.worksIn = d1 e2.worksIn = d1 e3.worksIn = d2"
			+ "\n		d1.secr = e1 d2.secr = e3"
			+ "\n		e1.salary = \"100000\" e2.salary = \"200000\"@Integer"
			+ "\n		e1.mgr = e1 e2.mgr = e2 e3.mgr = e3"
			+ "\n		e1.name = \"ad\" e2.name = \"sdf\" e3.name = \"efe\""
			+ "\n		d1.color = red d2.color = red e3.salary = \"333\""
			+ "\n		isRGB(ultraviolet) = tru"
			+ "\n	options"
			+ "\n		prover = saturated	"
			+ "\n}"
			+ "\n"
			+ "\ninstance J = literal : S {"
			+ "\n	generators"
			+ "\n		e1 e2 e3 : Emp"
			+ "\n		d1 d2 : Dept"
			+ "\n		ultraviolet : Color"
			+ "\n		str : String"
			+ "\n	equations"
			+ "\n		e1.worksIn = d1 e2.worksIn = d1 e3.worksIn = d2"
			+ "\n		d1.secr = e1 e3.name = \"efe\""
			+ "\n		d1.color = red d2.color = red e3.salary = \"333\""
			+ "\n		isRGB(ultraviolet) = tru"
			+ "\n	options"
			+ "\n		prover = saturated"
			+ "\n		dont_verify_is_appropriate_for_prover_unsafe = true "
			+ "\n		//relaxes totality requirement.  this does have a legit semantics, but the congruence"
			+ "\n		//prover should probably be used for ground but not saturated instances"
			+ "\n}"
			+ "\n"
			+ "\nmapping M = literal : S -> S {"
			+ "\n	entities"
			+ "\n		Emp -> Emp"
			+ "\n		Dept -> Dept"
			+ "\n	foreign_keys"
			+ "\n		worksIn -> worksIn"
			+ "\n		secr -> secr"
			+ "\n		mgr -> Emp"
			+ "\n	attributes	"
			+ "\n		name -> lambda x:Emp. whasabi@String"
			+ "\n		salary -> lambda x. salary(x)"
			+ "\n		color -> lambda x:Dept. color(x)"
			+ "\n}"
			+ "\n"
			+ "\ntransform h = literal : I -> I {"
			+ "\n	generators"
			+ "\n		e1 -> e1 "
			+ "\n		e2 -> e1"
			+ "\n		e3 -> e3.mgr"
			+ "\n		d1 -> d1"
			+ "\n		d2 -> d2"
			+ "\n		str -> asfkd //infers at String"
			+ "\n		ultraviolet -> ultraviolet"
			+ "\n}"
			+ "\n";




}