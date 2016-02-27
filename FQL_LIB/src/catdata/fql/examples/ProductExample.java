package catdata.fql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class ProductExample extends Example {

	@Override
	public Language lang() {
		return Language.FQL;
	}
	
	@Override
	public String getName() {
		return "Products";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "schema S = {"
			+ "\n	nodes a, b;"
			+ "\n	attributes att : a -> string;"
			+ "\n	arrows f : a -> b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\nnodes a -> {1,2}, b -> {3};"
			+ "\nattributes att -> {(1,common),(2,common)};"
			+ "\narrows f -> {(1,3),(2,3)};"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance J = {"
			+ "\nnodes a -> {a,b,c}, b -> {d,e};"
			+ "\nattributes att -> {(a,common),(b,common),(c,baz)};"
			+ "\narrows f -> {(a,d),(b,e),(c,e)};"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance A = (I * J)"
			+ "\n"
			+ "\ntransform K = A.fst"
			+ "\n"
			+ "\ntransform L = A.snd"
			+ "\n"
			+ "\ntransform M = A.(K * L) //is id"
			+ "\n"
			+ "\nschema X = {"
			+ "\n	nodes a;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance N = unit X"
			+ "\n"
			+ "\ntransform O = N.unit N"
			+ "\n"
			+ "\nenum color = {r,g,b}"
			+ "\nenum num = {1,2}"
			+ "\n"
			+ "\nschema C = {"
			+ "\n	nodes a,b,c;"
			+ "\n	attributes attb:b->color, attc:c->num;"
			+ "\n	arrows f:a->b, g:a->c;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance II = unit C"
			+ "\n"
			+ "\ninstance JJ = {"
			+ "\n	nodes a -> {1,2,3}, b -> {1,2}, c -> {1,3};"
			+ "\n	attributes attb -> {(1,r),(2,g)}, attc -> {(1,1),(3,2)};"
			+ "\n	arrows f -> {(1,1),(2,1),(3,2)}, g -> {(1,1),(2,1),(3,3)};"
			+ "\n} : C"
			+ "\n"
			+ "\ntransform KK = II.unit JJ"
			+ "\n"
			+ "\nschema SS = {"
			+ "\n	nodes a, b, c;"
			+ "\n	attributes att:a->string;"
			+ "\n	arrows f:a->b, g:b->c, h:a->c;"
			+ "\n	equations a.h = a.f.g;"
			+ "\n}"
			+ "\n"
			+ "\nschema TT = {"
			+ "\n	nodes x, y;"
			+ "\n	attributes att:x->string;"
			+ "\n	arrows u:x->y, z:x->y;"
			+ "\n	equations x.u = x.z;"
			+ "\n}"
			+ "\n"
			+ "\nmapping F = {"
			+ "\n	nodes x -> a, y -> c;"
			+ "\n	attributes att->att;"
			+ "\n	arrows u -> a.f.g, z->a.f.g;"
			+ "\n} : TT -> SS "
			+ "\n"
			+ "\nschema AA = (SS * TT)"
			+ "\n"
			+ "\nmapping p1 = fst SS TT"
			+ "\nmapping p2 = snd SS TT"
			+ "\n"
			+ "\nmapping p = (p1*p2) //is identity"
			+ "\n"
			+ "\nschema XX = unit {string}"
			+ "\nmapping H = unit {string} TT";




}