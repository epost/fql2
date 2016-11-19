package catdata.aql.examples;

public class AqlPullbackExample extends AqlExample {

	@Override
	public String getName() {
		return "Pullback";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "typeside Type = literal {"
			+ "\n	java_types	"
			+ "\n		dom = \"java.lang.Object\""
			+ "\n	java_constants"
			+ "\n		dom = \"return input[0]\""
			+ "\n}"
			+ "\n"
			+ "\n///////////////////////////////////////////////////////////////////////////"
			+ "\n"
			+ "\nschema CoSpan = literal : Type {"
			+ "\n	entities"
			+ "\n		B C D"
			+ "\n 	foreign_keys"
			+ "\n		f: B -> D"
			+ "\n		g: C -> D"
			+ "\n	attributes"
			+ "\n		B_att : B -> dom"
			+ "\n		C_att : C -> dom	"
			+ "\n} "
			+ "\n"
			+ "\nschema Square = literal : Type {"
			+ "\n	imports"
			+ "\n		CoSpan"
			+ "\n	entities"
			+ "\n		A"
			+ "\n 	foreign_keys"
			+ "\n		f2: A -> B"
			+ "\n		g2: A -> C"
			+ "\n	path_equations"
			+ "\n		f2.f=g2.g"
			+ "\n	attributes"
			+ "\n		A_attB : A -> dom"
			+ "\n		A_attC : A -> dom	"
			+ "\n} "
			+ "\n"
			+ "\nquery pullback = literal : CoSpan -> Square {"
			+ "\n entities"
			+ "\n "
			+ "\n	D -> {from d:D}"
			+ "\n		"
			+ "\n	B -> {from b:B "
			+ "\n		 return B_att -> b.B_att}"
			+ "\n"
			+ "\n	C -> {from c:C "
			+ "\n		 return C_att -> c.C_att}"
			+ "\n"
			+ "\n	A -> {from b0:B c0:C "
			+ "\n		 where b0.f = c0.g "
			+ "\n		 return A_attB -> b0.B_att "
			+ "\n		 	   A_attC -> c0.C_att} "
			+ "\n"
			+ "\nforeign_keys		 	   "
			+ "\n"
			+ "\n	f -> {d -> b.f}"
			+ "\n	g -> {d -> c.g}"
			+ "\n	f2 -> {b -> b0}"
			+ "\n	g2 -> {c -> c0}"
			+ "\n"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = literal : CoSpan {"
			+ "\n	generators"
			+ "\n		b1 b2 : B"
			+ "\n		c1 c2 : C"
			+ "\n		d1 d2 d3 : D"
			+ "\n	equations"
			+ "\n		b1.B_att = \"b1\"@dom"
			+ "\n		b2.B_att = \"b2\"@dom"
			+ "\n		c1.C_att = \"c1\"@dom"
			+ "\n		c2.C_att = \"c2\"@dom"
			+ "\n		b1.f = d1"
			+ "\n		b2.f = d2"
			+ "\n		c1.g = d1"
			+ "\n		c1.g = d3 "
			+ "\n}"
			+ "\n"
			+ "\ninstance J = eval pullback I"
			+ "\n";



}
