package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class MonadExample extends Example {
	
	@Override
	public Language lang() {
		return Language.FQLPP;
	}


	@Override
	public String getName() {
		return "Monads";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "////////////////////////////////////////////////////////////////////"
			+ "\n//functors Set -> Set"
			+ "\n"
			+ "\nfunctor Maybe = { "
			+ "\n objects"
			+ "\n  X -> (unit + X);"
			+ "\n arrows"
			+ "\n  f : A -> B -> (inl unit B + (f ; inr unit B));"
			+ "\n} : Set -> Set"
			+ "\n"
			+ "\nset Maybe3 = apply Maybe on object 3"
			+ "\nset Maybe4 = apply Maybe on object 4"
			+ "\n"
			+ "\nfunction MaybeF = apply Maybe on arrow {(0,1),(1,2),(2,0),(3,0)} : 4 -> 3"
			+ "\n"
			+ "\nfunctor Double = {"
			+ "\n		objects X->(X*X);"
			+ "\n		arrows f:A->B -> ((fst A A; f)*(snd A A; f));"
			+ "\n} : Set -> Set"
			+ "\n"
			+ "\nset dbl=apply Double on object {1,2,3}"
			+ "\n"
			+ "\nfunction f = apply Double on arrow id {1,2,3}"
			+ "\n"
			+ "\ntransform ret = {"
			+ "\n objects"
			+ "\n  X -> inr unit X;"
			+ "\n} : (id Set : Set -> Set) -> (Maybe : Set -> Set)"
			+ "\n"
			+ "\ntransform join = {"
			+ "\n objects"
			+ "\n  X -> (inl unit X + id (unit + X));"
			+ "\n} : ((Maybe ; Maybe) : Set -> Set) -> (Maybe : Set -> Set)"
			+ "\n"
			+ "\nfunction ret1 = apply ret {1,2,3}"
			+ "\nset set1 = apply Maybe on object {1,2,3}"
			+ "\nset set2 = apply Maybe on object set1"
			+ "\nfunction joined = apply join set2"
			+ "\n"
			+ "\ncategory K = kleisli Maybe ret join"
			+ "\n"
			+ "\n////////"
			+ "\n"
			+ "\ncategory C = {"
			+ "\n	objects a,b,c;"
			+ "\n	arrows f:a->b,g:b->c;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor M = {"
			+ "\n	objects a->a, b->c,c->c;"
			+ "\n	arrows f->a.f.g,g->c;"
			+ "\n} : C -> C"
			+ "\n"
			+ "\ntransform eta = {"
			+ "\n	objects a->a, b->b.g, c->c;"
			+ "\n} : (id C:C->C) -> (M: C->C)"
			+ "\n"
			+ "\ntransform mu = {"
			+ "\n	objects a->a, b->c, c->c;"
			+ "\n} : ((M;M):C->C) -> (M: C->C) "
			+ "\n"
			+ "\ncategory M_kleisli = kleisli M eta mu"
			+ "\n"
			+ "\n///////////"
			+ "\n"
			+ "\ncategory D = {"
			+ "\n	objects a,b,c;"
			+ "\n	arrows f:a->b,g:b->c;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor T = {"
			+ "\n	objects a->a, b->a,c->c;"
			+ "\n	arrows f->a,g->a.f.g;"
			+ "\n} : D -> D"
			+ "\n"
			+ "\ntransform epsilon = {"
			+ "\n	objects a->a, b->a.f, c->c;"
			+ "\n} : (T: D->D) -> (id D:D->D)"
			+ "\n"
			+ "\ntransform Delta = {"
			+ "\n	objects a->a, b->a, c->c;"
			+ "\n} : (T:D->D) -> ((T;T): D->D) "
			+ "\n"
			+ "\ncategory CK = cokleisli T epsilon Delta"
			+ "\n"

;

}
