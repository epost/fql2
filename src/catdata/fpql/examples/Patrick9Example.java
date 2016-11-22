package catdata.fpql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class Patrick9Example extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Pi";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "string : type"
			+ "\nRyan Wisnesky David Spivak Harvard MIT Leslie : string"
			+ "\n"
			+ "\nC = schema {"
			+ "\n nodes "
			+ "\n 	c1, "
			+ "\n 	c2;"
			+ "\n edges"
			+ "\n	att1 : c1 -> string,"
			+ "\n	att2 : c1 -> string, "
			+ "\n	att3 : c2 -> string;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n variables"
			+ "\n	v11 v12 : c1,"
			+ "\n	v21 v22 v23 : c2;"
			+ "\n equations"
			+ "\n	v11.att1 = David, v12.att1 = Ryan,"
			+ "\n	v11.att2 = Spivak, v12.att2 = Wisnesky,"
			+ "\n	v21.att3 = MIT, v22.att3 = Harvard, v23.att3 = Leslie;"
			+ "\n} : C "
			+ "\n"
			+ "\nD = schema {"
			+ "\n nodes"
			+ "\n 	d;"
			+ "\n edges"
			+ "\n 	a1 : d -> string, "
			+ "\n 	a2 : d -> string, "
			+ "\n 	a3 : d -> string;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n nodes "
			+ "\n 	c1 -> d,"
			+ "\n 	c2 -> d;"
			+ "\n edges"
			+ "\n	att1 -> d.a1, "
			+ "\n	att2 -> d.a2,"
			+ "\n	att3 -> d.a3;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nJ = pi F I"
			+ "\n"
			+ "\n"
			+ "\nI0 = instance {"
			+ "\n variables"
			+ "\n	u11:c1,"
			+ "\n	u21 u22:c2;"
			+ "\n equations"
			+ "\n	u11.att1 = David,"
			+ "\n	u11.att2 = Spivak, "
			+ "\n	u21.att3 = MIT, u22.att3 = Harvard;"
			+ "\n} : C "
			+ "\n"
			+ "\nt = homomorphism {"
			+ "\n     variables u11 -> v11, u21 -> v21, u22 -> v22;"
			+ "\n} : I0 -> I"
			+ "\n"
			+ "\ntx = pi F t"
			+ "\n"
			+ "\nXXX = instance {"
			+ "\n variables a b c : d;"
			+ "\n equations "
			+ "\n  a.a1 = Harvard, b.a1 = Harvard, c.a1 = Harvard,"
			+ "\n  a.a2 = MIT, b.a2 = MIT, c.a2 = MIT,"
			+ "\n  a.a3 = Leslie, b.a3 = Leslie, c.a3 = Leslie;"
			+ "\n} : D"
			+ "\n"
			+ "\nret = return delta pi F XXX"
			+ "\n"
			+ "\ncoret = coreturn delta pi F I"
			+ "\n";





}
