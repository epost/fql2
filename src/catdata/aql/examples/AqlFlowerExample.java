package catdata.aql.examples;

public class AqlFlowerExample extends AqlExample {

	@Override
	public String getName() {
		return "Joinless";
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
			+ "\nschema Schools = literal : Ty {"
			+ "\n	entities "
			+ "\n		Person"
			+ "\n		School"
			+ "\n		Dept"
			+ "\n	foreign_keys"
			+ "\n	     instituteOf : Person -> School "
			+ "\n	     deptOf      : Person -> Dept"
			+ "\n	     biggestDept : School -> Dept"
			+ "\n	attributes"
			+ "\n		lastName    : Person -> String"
			+ "\n	     schoolName  : School -> String"
			+ "\n	     deptName    : Dept   -> String"
			+ "\n}"
			+ "\n"
			+ "\ninstance BostonSchools = literal : Schools {"
			+ "\n	generators "
			+ "\n		ryan david adam greg gregory jason : Person"
			+ "\n	     harvard mit : School"
			+ "\n	     math cs : Dept"
			+ "\n	multi_equations "
			+ "\n		lastName -> {ryan Wisnesky, david Spivak, adam Chlipala, greg Morrisett, gregory Malecha, jason Gross}"
			+ "\n		schoolName -> {harvard Harvard, mit MIT}"
			+ "\n		deptName -> {math Mathematics, cs CompSci}"
			+ "\n		instituteOf -> {ryan harvard, david mit, adam mit, greg harvard, gregory harvard, jason mit}"
			+ "\n		deptOf -> {ryan math, david math, adam cs, greg cs, gregory cs, jason cs}"
			+ "\n		biggestDept -> {harvard math, mit cs}"
			+ "\n}"
			+ "\n"
			+ "\n"
			+ "\nschema Person = literal : Ty {"
			+ "\n	entities "
			+ "\n		Person"
			+ "\n	attributes"
			+ "\n		lastName   : Person -> String"
			+ "\n		schoolName : Person -> String"
			+ "\n}"
			+ "\n"
			+ "\n//Find all the people whose school's biggest department is Mathematics"
			+ "\nquery BiggestDeptIsMathQuery = literal : Schools -> Person {"
			+ "\n	entities"
			+ "\n		Person -> {from   p:Person"
			+ "\n				 where  p.instituteOf.biggestDept.deptName = Mathematics"
			+ "\n				 return lastName -> p.lastName "
			+ "\n			  		   schoolName -> p.instituteOf.schoolName}"
			+ "\n} "
			+ "\n"
			+ "\ninstance BiggestDeptIsMathInBoston = eval BiggestDeptIsMathQuery BostonSchools"
			+ "\n";



}
