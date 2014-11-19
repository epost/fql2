package fql_lib.examples;

public class Patrick14Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P Flower";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "adom : type"
			+ "\n"
			+ "\n1 : adom"
			+ "\n2 : adom"
			+ "\n3 : adom"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes M, N;"
			+ "\n	edges f : M -> N, g : N -> N, att : N -> adom;"
			+ "\n	equations g.g = g;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables m1 : M;"
			+ "\n	equations m1.f.att = 1, m1.f.g.att = 2;"
			+ "\n} : S "
			+ "\n"
			+ "\nJ:T = flower {"
			+ "\n	select m1.f.g.att as x, n as y;"
			+ "\n	from M as m1, M as m2, N as n;"
			+ "\n	where m1.f = m2.f;"
			+ "\n} I"
			+ "\n"
			+ "\n";



}
