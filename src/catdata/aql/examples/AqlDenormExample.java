package catdata.aql.examples;

public class AqlDenormExample extends AqlExample {

	@Override
	public String getName() {
		return "Denormalize";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "typeside Ty = literal { "
			+ "\n	java_types"
			+ "\n		String = \"java.lang.String\""
			+ "\n	java_constants"
			+ "\n		String = \"return input[0]\""
			+ "\n}"
			+ "\n"
			+ "\nschema NormalizedSchema = literal : Ty {"
			+ "\n	entities"
			+ "\n		Male "
			+ "\n		Female"
			+ "\n	foreign_keys"
			+ "\n		mother : Male -> Female"
			+ "\n  	attributes"
			+ "\n  		female_name : Female -> String"
			+ "\n  		male_name : Male -> String "
			+ "\n}"
			+ "\n"
			+ "\ninstance NormalizedData = literal : NormalizedSchema {"
			+ "\n	generators"
			+ "\n		Al Bob Charlie : Male"
			+ "\n		Ellie Fran : Female"
			+ "\n	equations"
			+ "\n		Al.male_name = Albert "
			+ "\n		Al.mother = Ellie"
			+ "\n		"
			+ "\n		Bob.male_name = George"
			+ "\n		Bob.mother = Ellie"
			+ "\n		"
			+ "\n		Charlie.male_name = Charles	"
			+ "\n		Charlie.mother = Fran"
			+ "\n"
			+ "\n		Ellie.female_name = Elaine"
			+ "\n		Fran.female_name = Francine"
			+ "\n}"
			+ "\n"
			+ "\nschema DeNormalizedSchema = literal : Ty {"
			+ "\n	imports"
			+ "\n		NormalizedSchema"
			+ "\n	attributes"
			+ "\n		mother_name : Male -> String"
			+ "\n  	observation_equations"
			+ "\n  		forall m:Male. mother_name(m) = female_name(mother(m))"
			+ "\n}"
			+ "\n"
			+ "\ninstance DeNormalizedData = literal : DeNormalizedSchema {"
			+ "\n	imports"
			+ "\n		NormalizedData"
			+ "\n}"
			+ "\n";



}
