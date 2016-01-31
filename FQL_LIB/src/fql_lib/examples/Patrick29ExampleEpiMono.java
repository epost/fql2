package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class Patrick29ExampleEpiMono extends Example {
	
	@Override
	public Language lang() {
		return Language.FPQL;
	}


	@Override
	public String getName() {
		return "Epi/Mono Factor";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//This FPQL program finds the image of any function, i.e., the epi-mono factorization."
+ "\n"
+ "\n//Schema A is where you put your function (as an instance)."
+ "\n"
+ "\nA = schema {"
+ "\n	nodes c, x;"
+ "\n	edges f:c->x;"
+ "\n	equations;"
+ "\n}"
+ "\n"
+ "\n//Schemas B and C are just auxilliary schemas to perform our calculation."
+ "\n"
+ "\nB = schema {"
+ "\n	nodes c1,c2,c,x;"
+ "\n	edges f1:c1->x, f2:c2->x, f:c->x, g1:c1->c, g2:c2->c;"
+ "\n	equations f1=g1.f, f2=g2.f;"
+ "\n}"
+ "\n"
+ "\nC = schema {"
+ "\n	nodes k,c1,c2,c,x;"
+ "\n	edges "
+ "\n		f1:c1->x, f2:c2->x, f:c->x, g1:c1->c, g2:c2->c,"
+ "\n		h1:k->c1, h2:k->c2"
+ "\n		;"
+ "\n	equations f1=g1.f, f2=g2.f, h1.f1=h2.f2;"
+ "\n}"
+ "\n"
+ "\n//Schema D is where the result will show up."
+ "\n"
+ "\nD = schema {"
+ "\n	nodes k,c,image,x;"
+ "\n	edges h1:k->c, h2:k->c, quotient:c->image,f:image->x;"
+ "\n	equations h1.quotient=h2.quotient;"
+ "\n}"
+ "\n"
+ "\nF = mapping {"
+ "\n	nodes c1->c,c2->c,c->c,x->x;"
+ "\n	edges f1->f, f2->f, f->f, g1->c,g2->c;"
+ "\n}"
+ "\n : B -> A"
+ "\n"
+ "\nG= mapping {"
+ "\n	nodes c1->c1,c2->c2,c->c,x->x;"
+ "\n	edges f1->f1, f2->f2,f->f, g1->g1,g2->g2;"
+ "\n}"
+ "\n : B -> C"
+ "\n"
+ "\nH= mapping {"
+ "\n	nodes k->k, c1->c, c2->c,c->c,x->x;"
+ "\n	edges f1->quotient.f, f2->quotient.f,f->quotient.f, g1->c,g2->c,h1->h1,h2->h2;"
+ "\n}"
+ "\n : C -> D"
+ "\n"
+ "\n//Any function can go here: "
+ "\n"
+ "\nfunc = instance {"
+ "\n	variables x11 x12 x13 x21 x22:c, y1 y2 y3:x;"
+ "\n	equations x11.f=y1, x12.f=y1,x13.f=y1,x21.f=y1,x22.f=y2;"
+ "\n}"
+ "\n :  A"
+ "\n"
+ "\nsetup = delta F func"
+ "\n"
+ "\npullback = pi G setup"
+ "\n"
+ "\n//the following instance is the result of our epi-mono factorization."
+ "\n//k contains the kernel, and the functions c->image->x is the epi-mono factorization of f."
+ "\n"
+ "\nepimono = sigma H pullback"
+ "\n";



}
