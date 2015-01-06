package fql_lib.examples;

public class Patrick20ExampleUber extends Example {

	@Override 
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P Uber";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "dom : type"
			+ "\n1 2 3 : dom"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes A, B; "
			+ "\n	edges f:A->B, attA:A->dom, attB:B->dom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables a1 a2:A, b:B;"
			+ "\n	equations a1.attA=1, a2.attA=2, b.attB=3, a1.f=b, a2.f=b;"
			+ "\n} : S"
			+ "\n"
			+ "\nQ = polynomial {"
			+ "\n    qA = { for a:A;"
			+ "\n           where a.attA=1;"
			+ "\n           attributes attA = a.attA;"
			+ "\n           edges f = {b=a.f} : qB;"
			+ "\n           } : A,"
			+ "\n    qB = { for b:B;"
			+ "\n           where;"
			+ "\n           attributes attB = b.attB;"
			+ "\n           edges;"
			+ "\n         } : B "
			+ "\n} : S -> S"
			+ "\n"
			+ "\nJ = apply Q I"
			+ "\n"
;


}
