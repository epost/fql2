package catdata.aql.examples;

public class AqlPushoutExample extends AqlExample {

	@Override
	public String getName() {
		return "Pushout";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "typeside Type = literal {"
			+ "\n	java_types	"
			+ "\n		dom = \"java.lang.Object\""
			+ "\n	java_constants"
			+ "\n		dom = \"return input[0]\""
			+ "\n}"
			+ "\n"
			+ "\n///////////////////////////////////////////////////////////////////////////"
			+ "\n"
			+ "\nschema Span = literal : Type {"
			+ "\n	entities"
			+ "\n		A B C"
			+ "\n 	foreign_keys"
			+ "\n		f: A -> B"
			+ "\n		g: A -> C"
			+ "\n	attributes"
			+ "\n		C_att : C -> dom"
			+ "\n		B_att : B -> dom	"
			+ "\n} "
			+ "\n"
			+ "\nschema Square = literal : Type {"
			+ "\n	imports"
			+ "\n		Span"
			+ "\n	entities"
			+ "\n		D"
			+ "\n 	foreign_keys"
			+ "\n		f2: B -> D"
			+ "\n		g2: C -> D"
			+ "\n	path_equations"
			+ "\n		f.f2=g.g2"
			+ "\n	attributes"
			+ "\n		D_attB : D -> dom"
			+ "\n		D_attC : D -> dom	"
			+ "\n} "
			+ "\n"
			+ "\nquery pushout = literal : Square -> Span {"
			+ "\n entities"
			+ "\n 		"
			+ "\n	B -> {from b:B "
			+ "\n		 return B_att -> b.f2.D_attB}"
			+ "\n"
			+ "\n	C -> {from c:C "
			+ "\n		 return C_att -> c.g2.D_attC}"
			+ "\n"
			+ "\n	A -> {from a:A} "
			+ "\n"
			+ "\nforeign_keys		 	   "
			+ "\n"
			+ "\n	f -> {b -> a.f}"
			+ "\n	g -> {c -> a.g}"
			+ "\n"
			+ "\n}"
			+ "\n"
			+ "\ninstance J = literal : Span {"
			+ "\n	generators"
			+ "\n		a : A"
			+ "\n		b1 b2 : B"
			+ "\n		c1 c2 : C"
			+ "\n	equations"
			+ "\n		b1.B_att = \"b1\"@dom"
			+ "\n		b2.B_att = \"b2\"@dom"
			+ "\n		c1.C_att = \"c1\"@dom"
			+ "\n		c2.C_att = \"c2\"@dom"
			+ "\n		a.f = b1 "
			+ "\n		a.g = c1"
			+ "\n} "
			+ "\n"
			+ "\ninstance I = coeval pushout J"
			+ "\n"
			+ "\ntransform t = unit_query pushout J"
			;



}
