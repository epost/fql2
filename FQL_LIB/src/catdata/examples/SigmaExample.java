package catdata.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class SigmaExample extends Example {

	@Override
	public Language lang() {
		return Language.FQLPP;
	}

	@Override
	public String getName() {
		return "Sigma";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "category C = {"
			+ "\n	objects "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal,"
			+ "\n		string;"
			+ "\n	arrows"
			+ "\n		attA : Amphibian -> string, "
			+ "\n		attL:LandAnimal-> string, "
			+ "\n		attW:WaterAnimal->string,"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor I = {"
			+ "\n	objects "
			+ "\n		Amphibian->{1,2},"
			+ "\n		LandAnimal->{1,2,3,4,5},"
			+ "\n		WaterAnimal->{1,2,3,4},"
			+ "\n		string->{gecko,frog,human,cow,horse,dolphin,fish};"
			+ "\n	arrows"
			+ "\n		attA -> {(1,gecko),(2, frog)}, "
			+ "\n		attL ->{(1,gecko),(2,frog),(3,human),(4,cow),(5,horse)},"
			+ "\n		attW -> {(1,fish),(2,gecko),(3,frog),(4,dolphin)},"
			+ "\n		IsAL->{(1,1),(2,2)},"
			+ "\n		IsAW->{(1,2),(2,3)};"
			+ "\n} : C -> Set"
			+ "\n"
			+ "\nfunctor I0 = {"
			+ "\n	objects "
			+ "\n		Amphibian->{x1},"
			+ "\n		LandAnimal->{x1,x2,x3,x4},"
			+ "\n		WaterAnimal->{x1,x2,x3},"
			+ "\n		string->{xgecko,xfrog,xhuman,xcow,xfish};"
			+ "\n	arrows "
			+ "\n		attA -> {(x1,xgecko)}, "
			+ "\n		attL ->{(x1,xgecko),(x2,xfrog),(x3,xhuman),(x4,xcow)},"
			+ "\n		attW -> {(x1,xfish),(x2,xgecko),(x3,xfrog)},"
			+ "\n		IsAL->{(x1,x1)},"
			+ "\n		IsAW->{(x1,x2)};"
			+ "\n} : C -> Set"
			+ "\n"
			+ "\ntransform t = {"
			+ "\n	objects "
			+ "\n		Amphibian->{(x1,1)},"
			+ "\n		LandAnimal->{(x1,1),(x2,2),(x3,3),(x4,4)},"
			+ "\n		WaterAnimal->{(x1,1),(x2,2),(x3,3)},"
			+ "\n		string -> {(xgecko,gecko),(xfrog,frog),(xhuman,human),(xcow,cow),(xfish,fish)};"
			+ "\n} : (I0:C->Set) -> (I:C->Set)"
			+ "\n"
			+ "\ncategory D ={"
			+ "\n	objects "
			+ "\n		yAmphibian,"
			+ "\n		yLandAnimal,"
			+ "\n		yWaterAnimal,"
			+ "\n		yAnimal,"
			+ "\n		ystring;"
			+ "\n	arrows"
			+ "\n		yattA:yAmphibian -> ystring, "
			+ "\n		yattL:yLandAnimal-> ystring, "
			+ "\n		yattW:yWaterAnimal->ystring,"
			+ "\n		yIsAL:yAmphibian->yLandAnimal,"
			+ "\n		yIsAW:yAmphibian->yWaterAnimal,"
			+ "\n		yIsALL:yLandAnimal->yAnimal,"
			+ "\n		yIsAWW:yWaterAnimal->yAnimal;"
			+ "\n	equations"
			+ "\n		yAmphibian.yIsAL.yIsALL=yAmphibian.yIsAW.yIsAWW;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor F = {"
			+ "\n	objects "
			+ "\n		Amphibian->yAmphibian,"
			+ "\n		LandAnimal->yLandAnimal,"
			+ "\n		WaterAnimal->yWaterAnimal,"
			+ "\n		string->ystring;"
			+ "\n	arrows"
			+ "\n		attA -> yAmphibian.yattA, "
			+ "\n		attL -> yLandAnimal.yattL, "
			+ "\n		attW -> yWaterAnimal.yattW,"
			+ "\n		IsAL -> yAmphibian.yIsAL,"
			+ "\n		IsAW -> yAmphibian.yIsAW;"
			+ "\n} : C -> D"
			+ "\n"
			+ "\nfunctor sigma_FI = apply sigma F on object I"
			+ "\n"
			+ "\nfunctor sigma_FI0 = apply sigma F on object I0"
			+ "\n	"
			+ "\ntransform t0 = apply sigma F on arrow t"
			+ "\n"
			+ "\n"
			+ "\nfunctor deltasigmaI = apply delta F on object sigma_FI"
			+ "\ntransform monad_unit = apply return sigma delta F on deltasigmaI"
			+ "\nfunctor sigmadeltasigmaI = apply sigma F on object deltasigmaI"
			+ "\ntransform monad_counit = apply coreturn sigma delta F on sigmadeltasigmaI"
			+ "\n"
;

}
