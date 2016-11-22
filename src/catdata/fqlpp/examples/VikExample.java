package catdata.fqlpp.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class VikExample extends Example {

	@Override
	public Language lang() {
		return Language.FQLPP;
	}

	@Override
	public String getName() {
		return "SELECT CTDB";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//The query from http://categoricaldata.net/fql/introSlides.pdf#page=45"
+ "\n//It's best to go to options and choose \"Pi ID creation strategy\" to be \"Summary as ID\"."
		+	"\n\ncategory C = {"
			+ "\n	objects "
			+ "\n		RL3, //numbers less than 3."
			+ "\n		R,  //numbers"
			+ "\n		Book,"
			+ "\n		Isbn_num,"
			+ "\n		String;"
			+ "\n	arrows "
			+ "\n		i:RL3->R,"
			+ "\n		price:Book->R,"
			+ "\n		title:Book->String,"
			+ "\n		isbn:Book->Isbn_num;	"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor I = {"
			+ "\n	objects "
			+ "\n		RL3->{1,2},"
			+ "\n		R->{1,2,3,4,5},"
			+ "\n		Book->{1,2,3},"
			+ "\n		Isbn_num->{1,2,3,249,258,4597},"
			+ "\n		String->{\"a\",\"b\",\"GoneWind\",\"Catch22\",\"CatTheory\"};"
			+ "\n	arrows"
			+ "\n		title->{(1,\"GoneWind\"),(2,\"Catch22\"), (3,\"CatTheory\")},"
			+ "\n		isbn->{(1,258),(2,4597),(3,249)}, //Note the last digit of the ISBN is the number of letters in the title."
			+ "\n		i->{(1,1),(2,2)},"
			+ "\n		price->{(1,4),(2,3),(3,1)};"
			+ "\n} :  C->Set"
			+ "\n"
			+ "\n//Note that there is exactly one book whose price is less than 3."
			+ "\n"
			+ "\ncategory D = {"
			+ "\n	objects "
			+ "\n		RL3,"
			+ "\n		R, "
			+ "\n		W,"
			+ "\n		Book,"
			+ "\n		Isbn_num,"
			+ "\n		String;"
			+ "\n	arrows "
			+ "\n		title:Book->String,"
			+ "\n		isbn:Book->Isbn_num,"
			+ "\n		f:W->RL3,"
			+ "\n		g:W->Book,"
			+ "\n		i:RL3->R,"
			+ "\n		price:Book->R;	"
			+ "\n	equations"
			+ "\n		W.f.i=W.g.price;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor F = {"
			+ "\n	objects "
			+ "\n		RL3->RL3, "
			+ "\n		R->R, "
			+ "\n		Book->Book,"
			+ "\n		Isbn_num->Isbn_num,"
			+ "\n		String->String;"
			+ "\n	arrows "
			+ "\n		i->RL3.i, "
			+ "\n		price->Book.price,"
			+ "\n		title->Book.title,"
			+ "\n		isbn->Book.isbn;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\n"
			+ "\nfunctor pi_F_I = apply pi F on object I"
			+ "\n"
			+ "\ncategory E = {"
			+ "\n	objects W, String, Isbn_num;"
			+ "\n	arrows title:W->String, isbn:W->Isbn_num;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor G = {"
			+ "\n	objects W->W, String->String, Isbn_num->Isbn_num;"
			+ "\n	arrows title->W.g.title, isbn->W.g.isbn;"
			+ "\n} : E -> D"
			+ "\n"
			+ "\nfunctor Select = apply delta G on object pi_F_I"
			+ "\n";



}
