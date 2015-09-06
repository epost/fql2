package fql_lib.examples;

public class OplSigmaExample extends Example {
	
	@Override 
	public String isPatrick() {
		return "OPL";
	}

	@Override
	public String getName() {
		return "O Sigma";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//this file requires KB on sorts without constants"
			+ "\n"
			+ "\nC = theory {"
			+ "\n	sorts "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal,"
			+ "\n		String;"
			+ "\n	symbols"
			+ "\n		attA@10: Amphibian -> String, "
			+ "\n		attL@11: LandAnimal -> String, "
			+ "\n		attW@12: WaterAnimal -> String,"
			+ "\n		IsAL@13: Amphibian -> LandAnimal,"
			+ "\n		IsAW@14: Amphibian -> WaterAnimal;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nI = presentation {"
			+ "\n	constants "
			+ "\n		a1@1:Amphibian, a2@2:Amphibian,"
			+ "\n		l1@1:LandAnimal, l2@2:LandAnimal, l3@3:LandAnimal, l4@4:LandAnimal, l5@5:LandAnimal,"
			+ "\n		w1@1:WaterAnimal, w2@2:WaterAnimal, w3@3:WaterAnimal, w4@4:WaterAnimal,"
			+ "\n		gecko@1:String, frog@2:String, human@3:String, cow@4:String, "
			+ "\n		horse@5:String, dolphin@6:String, fish@7:String;"
			+ "\n	equations"
			+ "\n		forall. attA(a1()) = gecko(), forall. attA(a2()) = frog(),"
			+ "\n		forall. attL(l1()) = gecko(), forall. attL(l2()) = frog(), "
			+ "\n		forall. attL(l3()) = human(), forall. attL(l4()) = cow(), "
			+ "\n		forall. attL(l5()) = horse(), forall. attW(w1()) = fish(), "
			+ "\n		forall. attW(w2()) = gecko(), forall. attW(w3()) = frog(), "
			+ "\n		forall. attW(w4()) = dolphin(), forall. IsAL(a1()) = l1(), "
			+ "\n		forall. IsAL(a2()) = l2(), forall. IsAW(a1()) = w2(), forall. IsAW(a2()) = w3(); "
			+ "\n} : C"
			+ "\n"
			+ "\nD = theory {"
			+ "\n	sorts "
			+ "\n		yAmphibian,"
			+ "\n		yLandAnimal,"
			+ "\n		yWaterAnimal,"
			+ "\n		yAnimal,"
			+ "\n		String;"
			+ "\n	symbols"
			+ "\n		yattA@10:yAmphibian->String, "
			+ "\n		yattL@11:yLandAnimal->String, "
			+ "\n		yattW@12:yWaterAnimal->String,"
			+ "\n		yIsAL@13:yAmphibian->yLandAnimal,"
			+ "\n		yIsAW@14:yAmphibian->yWaterAnimal,"
			+ "\n		yIsALL@15:yLandAnimal->yAnimal,"
			+ "\n		yIsAWW@16:yWaterAnimal->yAnimal;"
			+ "\n	equations"
			+ "\n		forall x:yAmphibian. yIsALL(yIsAL(x)) = yIsAWW(yIsAW(x));"
			+ "\n}"
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	sorts "
			+ "\n		Amphibian->yAmphibian,"
			+ "\n		LandAnimal->yLandAnimal,"
			+ "\n		WaterAnimal->yWaterAnimal,"
			+ "\n		String -> String;"
			+ "\n	symbols"
			+ "\n		attA -> forall x:yAmphibian. yattA(x), "
			+ "\n		attL -> forall x:yLandAnimal. yattL(x), "
			+ "\n		attW -> forall x:yWaterAnimal. yattW(x),"
			+ "\n		IsAL -> forall x:yAmphibian. yIsAL(x),"
			+ "\n		IsAW -> forall x:yAmphibian. yIsAW(x);"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nJ = sigma F I"
			+ "\nK = saturate J";



}
