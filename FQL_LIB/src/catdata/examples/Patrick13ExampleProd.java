package catdata.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick13ExampleProd extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}
 
	
	@Override
	public String getName() {
		return "Prod";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "string : type"
			+ "\ncommon : string"
			+ "\nbaz : string"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes A, B;"
			+ "\n	edges att : A -> string, f : A -> B;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n     variables v1:A, v2:A, v3:B; "
			+ "\n     equations v1.att = common, v2.att=common, "
			+ "\n               v1.f = v3, v2.f = v3; "
			+ "\n} : S"
			+ "\n"
			+ "\nJ = instance {"
			+ "\n	variables a:A, b:A, c:A, d:B, e:B;"
			+ "\n     equations a.att = common, b.att = common, c.att = baz,"
			+ "\n               a.f = d, b.f = e, c.f = e;"
			+ "\n} : S"
			+ "\n"
			+ "\nIJ = (I * J)"
			+ "\n"
			+ "\nK = fst I J"
			+ "\n"
			+ "\nL = snd I J"
			+ "\n"
			+ "\nM = pair K L //is id"
			+ "\n"
			+ "\nN = unit S"
			+ "\n"
			+ "\nO = tt J"						
;


}
