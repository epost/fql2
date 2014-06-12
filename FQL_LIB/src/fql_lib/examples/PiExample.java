package fql_lib.examples;

public class PiExample extends Example {

	@Override
	public String getName() {
		return "Pi";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "category C = {"
			+ "\n objects "
			+ "\n 	c1, "
			+ "\n 	c2,"
			+ "\n 	string;"
			+ "\n arrows"
			+ "\n	att1 : c1 -> string,"
			+ "\n	att2 : c1 -> string, "
			+ "\n	att3 : c2 -> string;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor I = {"
			+ "\n objects"
			+ "\n	c1   -> { 1,2 },"
			+ "\n	c2   -> { 1,2,3 },"
			+ "\n	string->{ Ryan, Wisnesky, David, Spivak, Harvard, MIT, Leslie };"
			+ "\n arrows"
			+ "\n	att1 -> { (1,David), (2,Ryan) },"
			+ "\n	att2 -> { (1,Spivak), (2,Wisnesky) },"
			+ "\n	att3 -> { (1,MIT), (2,Harvard),(3,Leslie) };"
			+ "\n} : C -> Set"
			+ "\n"
			+ "\nfunctor I0 = {"
			+ "\n objects"
			+ "\n	c1   -> { x1 },"
			+ "\n	c2   -> { x1,x2 },"
			+ "\n	string->{ Davidx, Spivakx, MITx, Harvardx };"
			+ "\n arrows"
			+ "\n	att1 -> { (x1,Davidx) },"
			+ "\n	att2 -> { (x1,Spivakx) },"
			+ "\n	att3 -> { (x1,MITx), (x2,Harvardx) };"
			+ "\n} : C -> Set"
			+ "\n"
			+ "\ntransform t = {"
			+ "\n	objects c1 -> {(x1,1)}, c2 -> {(x1,1),(x2,2)}, "
			+ "\n	        string -> {(Davidx,David),(Spivakx, Spivak),(MITx,MIT),(Harvardx,Harvard)} ;"
			+ "\n} : (I0:C->Set) -> (I:C->Set) "
			+ "\n"
			+ "\ncategory D = {"
			+ "\n objects "
			+ "\n 	d,"
			+ "\n 	string;"
			+ "\n arrows"
			+ "\n 	a1 : d -> string, "
			+ "\n 	a2 : d -> string, "
			+ "\n 	a3 : d -> string;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor F = {"
			+ "\n objects "
			+ "\n 	c1 -> d,"
			+ "\n 	c2 -> d,"
			+ "\n 	string -> string;"
			+ "\n arrows"
			+ "\n	att1 -> d.a1, "
			+ "\n	att2 -> d.a2,"
			+ "\n	att3 -> d.a3;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nfunctor J = apply pi F on object I"
			+ "\ntransform tx = apply pi F on arrow t"
			+ "\n"
			+ "\nfunctor XXX = {"
			+ "\n objects "
			+ "\n 	d -> {a,b,c},"
			+ "\n 	string -> {foo,bar,baz};"
			+ "\n arrows"
			+ "\n 	a1 -> {(a,foo),(b,foo),(c,foo)}, "
			+ "\n 	a2  ->{(a,bar),(b,bar),(c,bar)},"
			+ "\n 	a3 ->{(a,baz),(b,baz),(c,baz)};"
			+ "\n } : D -> Set"
			+ "\n"
			+ "\ntransform coret = apply coreturn delta pi F on I"
			+ "\ntransform ret = apply return delta pi F on XXX"
			+ "\n";





}
