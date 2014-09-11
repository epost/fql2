package fql_lib.examples;

public class Patrick2Example extends Example {
	
	@Override
	public boolean isPatrick() {
		return true;
	}

	@Override
	public String getName() {
		return "Patrick 2";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "String = type \"java.lang.String\""
			+ "\n"
			+ "\ngecko = constant String \"fql.primitives.Gecko\""
			+ "\nfrog = constant String \"fql.primitives.Frog\""
			+ "\nhuman = constant String \"fql.primitives.Human\""
			+ "\ncow = constant String \"fql.primitives.Cow\""
			+ "\nhorse = constant String \"fql.primitives.Horse\""
			+ "\ndolphin = constant String \"fql.primitives.Dolphin\""
			+ "\nfish = constant String \"fql.primitives.Fish\""
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
			+ "\n		a1: Amphibian, a2: Amphibian,"
			+ "\n		l1: LandAnimal, l2: LandAnimal, l3: LandAnimal, l4: LandAnimal, l5: LandAnimal,"
			+ "\n		w1: WaterAnimal, w2: WaterAnimal, w3: WaterAnimal, w4: WaterAnimal;"
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
;



}
