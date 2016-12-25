package catdata.fpql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick17Example extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Tableaux";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "adom:type"
			+ "\n1 2 3 4 5 6:adom"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes A, B;"
			+ "\n	edges f : A -> B, A_att : A -> adom, B_att : B -> adom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables a1 a2 a3 : A, b1 b2 b3 : B;"
			+ "\n	equations a1.f = b1, a2.f = b1, a3.f = b1, "
			+ "\n	          a1.A_att=1, a2.A_att=2, a3.A_att=3, "
			+ "\n	          b1.B_att=4, b2.B_att=5, b3.B_att=6;"
			+ "\n} : S"
			+ "\n"
			+ "\n//we will do this flower by hand"
			+ "\n//J = flower {"
			+ "\n//	select a.A_att as p, b.B_att as q;"
			+ "\n//	from A as a, B as b;"
			+ "\n//	where;"
			+ "\n//} I"
			+ "\n"
			+ "\n//schema for from clause"
			+ "\nS2 = schema {"
			+ "\n	nodes A, B, Q;"
			+ "\n	edges f : A -> B, A_att : A -> adom, B_att : B -> adom, a:Q->A, b:Q->B;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nM2 = mapping {"
			+ "\n	nodes A->A, B->B;"
			+ "\n	edges f->f, A_att->A_att, B_att->B_att;"
			+ "\n} : S -> S2 "
			+ "\n"
			+ "\nJ2 = pi M2 I //from part"
			+ "\n"
			+ "\nS3 = schema { //schema for select part"
			+ "\n	nodes Q;"
			+ "\n	edges p:Q->adom, q:Q->adom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nM3 = mapping {"
			+ "\n	nodes Q->Q;"
			+ "\n	edges p->a.A_att, q->b.B_att;"
			+ "\n} : S3 -> S2 "
			+ "\n"
			+ "\n//final result"
			+ "\nJ3 = delta M3 J2 "
			+ "\n"
			+ "\n////////////////////"
			+ "\n"
			+ "\nPatrick1 = instance {"
			+ "\n	variables var : Q;"
			+ "\n	equations;"
			+ "\n} : S3"
			+ "\n"
			+ "\nPatrick2 = delta M2 sigma M3 Patrick1  //on S"
			+ "\n"
			+ "\n//This is the \"canonical\" or \"frozen\" instance for a non-relational conjunctive query!"
			+ "\n";



}
