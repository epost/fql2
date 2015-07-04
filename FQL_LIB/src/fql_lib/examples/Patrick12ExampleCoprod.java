package fql_lib.examples;

public class Patrick12ExampleCoprod extends Example {

	@Override
	public String isPatrick() {
		return "true";
	}
	
	@Override
	public String getName() {
		return "P Coprod";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s ="S = schema {"
			+ "\n	nodes A, B;"
			+ "\n	edges f : A -> B;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables v1:A, v2:A, v3:B;"
			+ "\n	equations v1.f=v3, v2.f=v3;"
			+ "\n} : S "
			+ "\n"
			+ "\nJ = instance {"
			+ "\n	variables a:A,b:A,c:A, d:B,e:B;"
			+ "\n	equations a.f=d, b.f=e, c.f=e;"
			+ "\n} : S"
			+ "\n"
			+ "\nA = (I + J) "
			+ "\n"
			+ "\nk = inl I J"
			+ "\n"
			+ "\nl = inr I J"
			+ "\n"
			+ "\nm = case k l //is id"
			+ "\n"
			+ "\nN = void S "
			+ "\n"
			+ "\nO = ff J"
			+ "\n";



}
