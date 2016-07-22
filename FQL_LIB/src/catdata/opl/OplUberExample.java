package catdata.opl;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplUberExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	@Override
	public String getName() {
		return "Uber";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//configure OPL prover"
			+ "\npreamble = pragma {"
			+ "\n	options"
			+ "\n		\"opl_prover_force_prec\" = \"true\", "
			+ "\n		\"opl_prover_sort\" = \"false\", "
			+ "\n		\"opl_prover_compose\" = \"false\", "
			+ "\n		\"opl_prover_filter_subsumed\" = \"false\";"
			+ "\n} "
			+ "\n"
			+ "\nTypeSide = theory {"
			+ "\n	sorts"
			+ "\n		String, Bool;"
			+ "\n	symbols"
			+ "\n		tru, fals : Bool,"
			+ "\n		alice, bob, charlie, doris, ellie, frank, gina, henry,"
			+ "\n		book1, book2, book3, book4, book5, book6, book7 : String;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nSource = SCHEMA {"
			+ "\n	entities"
			+ "\n		Man, Woman;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		fav_book_m : Man -> String,"
			+ "\n		fav_book_w : Woman -> String,"
			+ "\n		name_m : Man -> String,"
			+ "\n		name_w : Woman -> String,"
			+ "\n		paying : Man -> Bool;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : TypeSide"
			+ "\n"
			+ "\nTarget = SCHEMA {"
			+ "\n	entities"
			+ "\n		Male, GoodMatch, PayingGoodMatch;"
			+ "\n	edges"
			+ "\n		is_a : PayingGoodMatch -> GoodMatch,"
			+ "\n		for_man : GoodMatch -> Male;"
			+ "\n	attributes"
			+ "\n		man_name : Male -> String,"
			+ "\n		woman_name : GoodMatch -> String;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : TypeSide"
			+ "\n"
			+ "\nQ = query {"
			+ "\n	"
			+ "\n	GoodMatchQ = {"
			+ "\n		for m:Man, w:Woman;"
			+ "\n		where fav_book_m(m) = fav_book_w(w);"
			+ "\n		return woman_name = name_w(w);"
			+ "\n		keys for_man = {man = m} : MaleQ; "
			+ "\n	} : GoodMatch,"
			+ "\n"
			+ "\n	MaleQ = {"
			+ "\n		for man:Man;"
			+ "\n		where;"
			+ "\n		return man_name = name_m(man);"
			+ "\n		keys; "
			+ "\n	} : Male,"
			+ "\n"
			+ "\n	PayingGoodMatchQ = {"
			+ "\n		for man:Man, woman:Woman;"
			+ "\n		where fav_book_m(man) = fav_book_w(woman),"
			+ "\n			 paying(man) = tru;"
			+ "\n		return;"
			+ "\n		keys is_a = {m = man, w = woman} : GoodMatchQ;	 "
			+ "\n	} : PayingGoodMatch"
			+ "\n	"
			+ "\n} : Source -> Target"
			+ "\n"
			+ "\n"
			+ "\nI = INSTANCE {"
			+ "\n	generators"
			+ "\n		a, d, e, g : Woman,"
			+ "\n		b, c, f, h : Man;"
			+ "\n	equations"
			+ "\n		name_m(b) = bob, paying(b) = tru, fav_book_m(b) = book1,"
			+ "\n		name_m(c) = charlie, paying(c) = fals, fav_book_m(c) = book1,"
			+ "\n		name_m(f) = frank, paying(f) = tru, fav_book_m(f) = book2,"
			+ "\n		name_m(h) = henry, paying(h) = tru, fav_book_m(h) = book3,"
			+ "\n"
			+ "\n		name_w(a) = alice, fav_book_w(a) = book1,"
			+ "\n		name_w(d) = doris, fav_book_w(d) = book2,"
			+ "\n		name_w(e) = ellie, fav_book_w(e) = book2,"
			+ "\n		name_w(g) = gina, fav_book_w(g) = book4;		"
			+ "\n} : Source"
			+ "\n"
			+ "\nJ = apply Q I"
			+ "\n";



}
