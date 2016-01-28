package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class Patrick2Example extends Example {
	
	@Override
	public Language lang() {
		return Language.FPQL;
	}


	@Override
	public String getName() {
		return "Sigma";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "String : type"
			+ "\n"
			+ "\ngecko frog human cow horse dolphin fish : String"
			+ "\n"
			+ "\nC = schema {"
			+ "\n	nodes "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal;"
			+ "\n	edges"
			+ "\n		attA: Amphibian -> String, "
			+ "\n		attL: LandAnimal -> String, "
			+ "\n		attW: WaterAnimal -> String,"
			+ "\n		IsAL: Amphibian -> LandAnimal,"
			+ "\n		IsAW: Amphibian -> WaterAnimal;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables "
			+ "\n		a1 a2: Amphibian,"
			+ "\n		l1 l2 l3 l4 l5: LandAnimal,"
			+ "\n		w1 w2 w3 w4: WaterAnimal;"
			+ "\n	equations"
			+ "\n		a1.attA = gecko, a2.attA = frog,"
			+ "\n		l1.attL = gecko, l2.attL = frog, l3.attL = human, l4.attL = cow, l5.attL = horse,"
			+ "\n		w1.attW = fish, w2.attW = gecko, w3.attW = frog, w4.attW = dolphin,"
			+ "\n		a1.IsAL = l1, a2.IsAL = l2,"
			+ "\n		a1.IsAW = w2, a2.IsAW = w3; "
			+ "\n} : C"
			+ "\n"
			+ "\n"
			+ "\nD = schema {"
			+ "\n	nodes "
			+ "\n		yAmphibian,"
			+ "\n		yLandAnimal,"
			+ "\n		yWaterAnimal,"
			+ "\n		yAnimal;"
			+ "\n	edges"
			+ "\n		yattA:yAmphibian->String, "
			+ "\n		yattL:yLandAnimal->String, "
			+ "\n		yattW:yWaterAnimal->String,"
			+ "\n		yIsAL:yAmphibian->yLandAnimal,"
			+ "\n		yIsAW:yAmphibian->yWaterAnimal,"
			+ "\n		yIsALL:yLandAnimal->yAnimal,"
			+ "\n		yIsAWW:yWaterAnimal->yAnimal;"
			+ "\n	equations"
			+ "\n		yAmphibian.yIsAL.yIsALL=yAmphibian.yIsAW.yIsAWW;"
			+ "\n}"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	nodes "
			+ "\n		Amphibian->yAmphibian,"
			+ "\n		LandAnimal->yLandAnimal,"
			+ "\n		WaterAnimal->yWaterAnimal;"
			+ "\n	edges"
			+ "\n		attA -> yAmphibian.yattA, "
			+ "\n		attL -> yLandAnimal.yattL, "
			+ "\n		attW -> yWaterAnimal.yattW,"
			+ "\n		IsAL -> yAmphibian.yIsAL,"
			+ "\n		IsAW -> yAmphibian.yIsAW;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nJ = sigma F I"
			+ "\n"
			+ "\nI0 = instance {"
			+ "\n	variables "
			+ "\n		xa1: Amphibian," 
			+ "\n		xl1 xl2 xl3 xl4: LandAnimal,"
			+ "\n		xw1 xw2 xw3: WaterAnimal;"
			+ "\n	equations"
			+ "\n		xa1.attA = gecko, "
			+ "\n		xl1.attL = gecko, xl2.attL = frog, xl3.attL = human, xl4.attL = cow, "
			+ "\n		xw1.attW = fish, xw2.attW = gecko, xw3.attW = frog, "
			+ "\n		xa1.IsAL = xl1, "
			+ "\n		xa1.IsAW = xw2; "
			+ "\n} : C"
+ "\n"
			+ "\nt = homomorphism {"
			+ "\n	variables "
			+ "\n		xa1 -> a1, xl1 -> l1, xl2 -> l2, xl3 -> l3, xl4 -> l4, xw1 -> w1, xw2 -> w2, xw3 -> w3;"
			+ "\n} : I0 -> I"
+ "\n"
			+"\nt0 = sigma F t"
			+"\n";



}
