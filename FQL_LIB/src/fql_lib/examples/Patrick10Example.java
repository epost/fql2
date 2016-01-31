package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class Patrick10Example extends Example {
	
	@Override
	public Language lang() {
		return Language.FPQL;
	}

	@Override
	public String getName() {
		return "Relationalize";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s ="string : type"
			+"\nc1 c2 c3 c4 c5 c6 c7 : string"
			+"\n"
			+"\nC = schema {"
			+"\n	nodes A;"
			+"\n	edges att:A->string, f:A->A;"
			+"\n	equations A.f.f.f.f=A.f.f;"
			+"\n}"
			+"\n"
			+"\nI = instance {"
			+"\n	variables x1 x2 x3 x4 x5 x6 x7 : A;"
			+"\n	equations x1.att=c1,x2.att=c2,x3.att=c3,x4.att=c1,x5.att=c5,x6.att=c3,x7.att=c5,"
			+"\n	 x1.f=x2,x2.f=x3,x3.f=x5,x4.f=x2,x5.f=x3,x6.f=x7,x7.f=x6;"
			+"\n} : C"
			+"\n"
			+"\nRelI = relationalize I"
			+"\n";



}
