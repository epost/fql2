package catdata.fqlpp.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class SetsExample extends Example {
	
	@Override
	public Language lang() {
		return Language.FQLPP;
	}


	@Override
	public String getName() {
		return "Sets";
	}

	@Override
	public String getText() {
		return text;
	}
	
	String text = "/*"
			+ "\n * In FQL++, a *value* is either:"
			+ "\n *"
			+ "\n *  - a string, that must be quoted if it contains spaces or special symbols, like \"-\"."
			+ "\n *        Numbers are treated as strings in FQL++."
			+ "\n *  - a unit, written ()."
			+ "\n *  - a boolean, written true or false."
			+ "\n *  - a pair of two values v1 and v2, written (v1, v2)."
			+ "\n *  - a left or right injection of a value v, written inl v or inr v."
			+ "\n *  - a (possibly empty) set of values, written {v1, ..., vn}."
			+ "\n *  - a function, written as a set of pairs with explicit domain and co-domain:"
			+ "\n *        {(x1,y1), ..., (xN,yN)} : {x1, ..., xN} -> {y1, ..., yN}."
			+ "\n */"
			+ "\n"
			+ "\n//A *set literal* is a set of values, for example: "
			+ "\nset s1 = {foo, bar, \"b a z\", 7, true, false}"
			+ "\nset s2 = {(), inl (), inr (), inl inr 7, (hello,world), {a,b,{c,d},{}}}"
			+ "\nset s3 = { {(1,1),(2,4),(3,9)} : {1,2,3} -> {1,4,9}, {(1,1),(2,4),(3,9)} : {1,2,3} -> {1,4,9,10,11,12} }"
			+ "\n"
			+ "\n//A *set* is either a set literal, as above, or a set constructed from"
			+ "\n//set literals and +,*,^,void,unit,prop, for example:"
			+ "\nset s4 = void // = {}"
			+ "\nset s5 = unit // = { () }"
			+ "\nset s6 = prop // = { true, false }"
			+ "\nset s7 = (unit + unit) // = { inl (), inr () }"
			+ "\nset s8 = (prop * prop) // = { (true,true),(true,false),(false,true),(false,false) }"
			+ "\nset s9 = (prop ^ unit) // = { ((), true) : {()} -> {true,false}, ((), false) : {()} -> {true,false} }"
			+ "\n"
			+ "\n//a *function literal* is a function value, for example"
			+ "\nfunction f1 = { (1,2),(2,3) } : {1,2} -> {2,3}"
			+ "\n"
			+ "\n//functions can refer to named sets, so we could write instead"
			+ "\nset sf = {1,2}"
			+ "\nfunction f2 = { (1,2),(2,3) } : sf -> {2,3}"
			+ "\n"
			+ "\n//a *function* is either a function literal, as above, or a function constructed from"
			+ "\n//function literals and tt,ff,fst,snd,(+),(*),(;),eval,curry,char,kernel,iso1,iso2,id"
			+ "\n//for example"
			+ "\nfunction f3 = fst {a,b} {c,d} // (a,c) -> a  (a,d) -> a  (b,c) -> b  (b,d) -> d"
			+ "\nfunction f4 = inl {a,b} {c,d} // a -> inl a, b -> inl b"
			+ "\nfunction f5 = id {a,b} // a -> a, b -> b"
			+ "\nfunction f6 = (f5 ; f5) // = f5"
			+ "\nfunction pair_eta = (fst {a,b} {c,d} * snd {a,b} {c,d}) // = id"
			+ "\nfunction sum_eta = (inl {a,b} {c,d} + inr {a,b} {c,d}) // = id"
			+ "\nfunction exp_eta = curry eval {a} {b} // = id"
			+ "\nfunction f7 = char {(1,a),(2,b)} : {1,2} -> {a,b,c} // = a -> true, b -> true"
			+ "\nfunction f8 = kernel f7 // = {(a, a),  (b, b)} : {a, b} -> {a, b, c}"
			+ "\nfunction f9 = iso1 {a,b} {1,2} // = a -> 1, b -> 2"
			+ "\nfunction f10 = tt {a}"
			+ "\nfunction f11 = ff {a}"
			+ "\nfunction f12 = true"
			+ "\nfunction f13 = false"
			+ "\nfunction f14 = and"
			+ "\nfunction f15 = or"
			+ "\nfunction f16 = not"
			+ "\nfunction f17 = implies"
			+ "\n"
			+ "\n//Domains, co-domains, and range require that you must name the function, i.e., as f7"
			+ "\nset x = dom f7 // = {a,b}"
			+ "\nset y = cod f7 // = {true,false}"
			+ "\nset z = range f8 // = {a,b}"
			+ "\n"
			+ "\n//the number n denotes the set {0, ..., n-1}"
			+ "\nset two = 2"
			+ "\nset three = 3"
			+ "\nset five = 5"
			+ "\nset two_plus_three = (2+3)"
			+ "\nfunction two_plus_three_equals_five = iso1 two_plus_three five"
			;




}
