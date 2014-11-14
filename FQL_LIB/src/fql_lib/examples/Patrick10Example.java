package fql_lib.examples;

public class Patrick10Example extends Example {
	
	@Override
	public boolean isPatrick() {
		return true;
	}

	@Override
	public String getName() {
		return"P Relationalize";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s ="string : type"
			+"\nc1 : string"
			+"\nc2 : string"
			+"\nc3 : string"
			+"\nc4 : string"
			+"\nc5 : string"
			+"\nc6 : string"
			+"\nc7 : string"
			+"\n"
			+"\nC = schema {"
			+"\n	nodes A;"
			+"\n	edges att:A->string, f:A->A;"
			+"\n	equations A.f.f.f.f=A.f.f;"
			+"\n}"
			+"\n"
			+"\nI = instance {"
			+"\n	variables x1:A,x2:A,x3:A,x4:A,x5:A,x6:A,x7:A;"
			+"\n	equations x1.att=c1,x2.att=c2,x3.att=c3,x4.att=c1,x5.att=c5,x6.att=c3,x7.att=c5,"
			+"\n	 x1.f=x2,x2.f=x3,x3.f=x5,x4.f=x2,x5.f=x3,x6.f=x7,x7.f=x6;"
			+"\n} : C"
			+"\n"
			+"\nRelI = relationalize I"
			+"\n";



}
