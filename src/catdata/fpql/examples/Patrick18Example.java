package catdata.fpql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick18Example extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Reorder Joins";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "adom : type"
			+ "\nc1 c2 c3 c4 c5 c6 c7 c8 : adom"
			+ "\n"
			+ "\nS = schema {"
			+ "\n     nodes N;"
			+ "\n     edges f : N -> adom;"
			+ "\n     equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n     variables v1 v2 v3 v4 v5 v6 v7 v8 : N;"
			+ "\n     equations v1.f = c1, v2.f = c2, v3.f = c3, v4.f = c4, v5.f = c5, v6.f = c6, v7.f = c7, v8.f = c8; "
			+ "\n} : S"
			+ "\n"
			+ "\n//requires FROM clause reordering to compute"
			+ "\nQ = flower {"
			+ "\n     select n.f as nf;"
			+ "\n     from  N as n1, N as n2, N as n3, N as n4, N as n5, N as n6, N as n7, N as n;"
			+ "\n     where n1.f = n.f, n2.f = n.f, n3.f = n.f, n4.f = n.f, n5.f = n.f, n6.f = n.f, n7.f = n.f;"
			+ "\n} I"
			+ "\n";



}
