package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplMLExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
	@Override
	public String getName() {
		return "ML";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "//requires the classes and jars from the fqlml project to run"
			+ "\nS0 = theory {"
			+ "\n	sorts Person, Nat, String;"
			+ "\n	symbols zero@0 : Nat, "
			+ "\n		   succ@1 : Nat -> Nat,"
			+ "\n		   old@2, young@3 : String,		   "
			+ "\n		   classify@98 : Nat -> String,"
			+ "\n   "
			+ "\n		   age@99  : Person -> Nat,"
			+ "\n		   inputClass@97 : Person -> String,"
			+ "\n		   outputClass@100 : Person -> String;"
			+ "\n	equations"
			+ "\n		forall x. outputClass(x) = classify(age(x));"
			+ "\n}"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	entities Person;"
			+ "\n} : S0"
			+ "\n"
			+ "\nI0 = presentation {"
			+ "\n	generators p0, p1, p2, p3, p4, p5, p6, p7 : Person;"
			+ "\n	equations age(p0) = 0, age(p1) = 1, age(p2) = 2, age(p3) = 3, "
			+ "\n	          age(p4) = 4, age(p5) = 5, age(p6) = 6, age(p7) = 7,"
			+ "\n		     inputClass(p0) = young, inputClass(p1) = young, "
			+ "\n		     inputClass(p6) = old, inputClass(p7) = old;"
			+ "\n} : S0"
			+ "\nI = instance S I0 none"
			+ "\n"
			+ "\nT = types S"
			+ "\n"
			+ "\n"
			+ "\nML = javascript {"
			+ "\n      symbols"
			+ "\n		_preamble -> \"java.lang.Class.forName('catdata.opl.ml.MLUtil'); "
			+ "\n                        var MLUtils = Java.type('catdata.opl.ml.MLUtil'); "
			+ "\n                        var KNNClass = Java.type('net.sf.javaml.classification.KNearestNeighbors'); "
			+ "\n                        var data = MLUtils.toDataset(I, 'Person', ['age'], 'inputClass'); "
			+ "\n                        knn = new KNNClass(1); "
			+ "\n                        knn.buildClassifier(data);\","
			+ "\n		zero -> \"return 0.0\","
			+ "\n		succ -> \"return input[0]+1.0\","
			+ "\n		old -> \"return \\\"old\\\"\","
			+ "\n		young -> \"return \\\"young\\\"\","
			+ "\n		classify -> \"return knn.classify(MLUtils.wrap(input))\";"
			+ "\n} : T"
			+ "\n"
			+ "\nJ = instance S I0 ML"
			+ "\n";






}
