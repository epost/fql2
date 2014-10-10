package fql_lib.examples;

public class Patrick11Example extends Example {

	@Override
	public boolean isPatrick() {
		return true;
	}
	
	@Override
	public String getName() {
		return "P SELECT CTDB";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "String = type \"\""
			+ "\na = constant String \"\""
			+ "\nb = constant String \"\""
			+ "\nGoneWind = constant String \"\""
			+ "\nCatch22 = constant String \"\""
			+ "\nCatTheory = constant String \"\""
			+ "\n"
			+ "\nIsbn_num = type \"\""
			+ "\nisbn1 = constant Isbn_num \"\""
			+ "\nisbn2 = constant Isbn_num \"\""
			+ "\nisbn3 = constant Isbn_num \"\""
			+ "\nisbn249 = constant Isbn_num \"\""
			+ "\nisbn258 = constant Isbn_num \"\""
			+ "\nisbn4597 = constant Isbn_num \"\""
			+ "\n"
			+ "\nR = type \"\""
			+ "\nzero = constant R \"\""
			+ "\nsucc = fn R -> R \"\""
			+ "\neq0 = assume succ.succ.succ.succ.succ = succ //otherwise, pi doesn't terminate"
			+ "\n"
			+ "\nRL3 = type \"\""
			+ "\none = constant RL3 \"\""
			+ "\ntwo = constant RL3 \"\""
			+ "\n"
			+ "\ni = fn RL3 -> R \"\""
			+ "\neq1 = assume one.i = zero.succ"
			+ "\neq2 = assume two.i = zero.succ.succ"
			+ "\n"
			+ "\nC = schema {"
			+ "\n	nodes "
			+ "\n		Book;"
			+ "\n	edges "
			+ "\n		price:Book->R,"
			+ "\n		title:Book->String,"
			+ "\n		isbn:Book->Isbn_num;	"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n//Note the last digit of the ISBN is the number of letters in the title."
			+ "\nI = instance {"
			+ "\n	variables "
			+ "\n	 book1:Book, book2:Book, book3:Book;"
			+ "\n	equations"
			+ "\n      book1.title = GoneWind, book2.title=Catch22, book3.title=CatTheory,"
			+ "\n      book1.isbn = isbn258, book2.isbn = isbn4597, book3.isbn = isbn249,"
			+ "\n      book1.price = zero.succ.succ.succ.succ, book2.price = zero.succ.succ.succ, book3.price = zero.succ;"
			+ "\n} : C"
			+ "\n"
			+ "\n//Note that there is exactly one book whose price is less than 3."
			+ "\nD = schema {"
			+ "\n	nodes"
			+ "\n		W,"
			+ "\n		Book;"
			+ "\n	edges "
			+ "\n		title:Book->String,"
			+ "\n		isbn:Book->Isbn_num,"
			+ "\n		f:W->RL3,"
			+ "\n		g:W->Book,"
			+ "\n		price:Book->R;	"
			+ "\n	equations"
			+ "\n		W.f.i=W.g.price;"
			+ "\n}"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	nodes "
			+ "\n		Book->Book;"
			+ "\n	edges "
			+ "\n		price->Book.price,"
			+ "\n		title->Book.title,"
			+ "\n		isbn->Book.isbn;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\n"
			+ "\npi_F_I = pi F I"
			+ "\n"
			+ "\n"
			+ "\nE = schema {"
			+ "\n	nodes W;"
			+ "\n	edges title:W->String, isbn:W->Isbn_num;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nG = mapping {"
			+ "\n	nodes W->W;"
			+ "\n	edges title->W.g.title, isbn->W.g.isbn;"
			+ "\n} : E -> D"
			+ "\n"
			+ "\nSelect = delta G pi_F_I"
			+ "\n";



}
