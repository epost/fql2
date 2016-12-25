package catdata.fqlpp.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class BenExample extends Example {

	@Override
	public String getName() {
		return "Ben";
	}
	
	@Override
	public Language lang() {
		return Language.FQLPP;
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "/* "
			+ "\nConsider the diagram shape A <-- O --> B in Cat, "
			+ "\nwhere O is the terminal category (x), A and B are free arrows "
			+ "\nA = (a0--->a1) and B = (b0-->b1), "
			+ "\nthe map O-->A picks out the target, a1, and the map O-->B picks out the source, b0. "
			+ "\nThe colimit category has a \"new path\" (a0-->x-->b1) that neither A nor B \"knew about\"."
			+ "\n*/"
			+ "\n"
			+ "\ncategory O = {"
			+ "\n	objects "
			+ "\n		x;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n} "
			+ "\n"
			+ "\ncategory A = {"
			+ "\n	objects "
			+ "\n		a0, a1;"
			+ "\n	arrows"
			+ "\n		f : a0 -> a1;"
			+ "\n	equations;"
			+ "\n} "
			+ "\n"
			+ "\ncategory B = {"
			+ "\n	objects "
			+ "\n		b0, b1;"
			+ "\n	arrows"
			+ "\n		g : b0 -> b1;"
			+ "\n	equations;"
			+ "\n} "
			+ "\n"
			+ "\nfunctor F = {"
			+ "\n	objects "
			+ "\n		x -> a1;"
			+ "\n	arrows;"
			+ "\n} : O -> A "
			+ "\n"
			+ "\nfunctor G = {"
			+ "\n	objects "
			+ "\n		x -> b0;"
			+ "\n	arrows;"
			+ "\n} : O -> B "
			+ "\n"
			+ "\ncategory span = {"
			+ "\n	objects "
			+ "\n		o, a, b;"
			+ "\n	arrows"
			+ "\n		f : o -> a, g : o -> b;	"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor colimFunctor = {"
			+ "\n	objects"
			+ "\n		o -> O, a -> A, b -> B;"
			+ "\n	arrows"
			+ "\n		f -> F, g -> G;"
			+ "\n} : span -> Cat"
			+ "\n"
			+ "\ncategory colimCat = colim colimFunctor"
			+ "\n"
			+ "\n/*   colimCat should be"
			+ "\n category {"
			+ "\n	objects"
			+ "\n		a1, b1, a0;"
			+ "\n	arrows"
			+ "\n		f : a0 -> a1,"
			+ "\n		g : a1 -> b1;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n*/"
			+ "\n";



}
