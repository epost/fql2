package catdata.aql.examples;

public class AqlFkExample extends AqlExample {

	@Override
	public String getName() {
		return "Foreign Keys";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "typeside Ty = empty"
			+ "\n"
			+ "\nschema University = literal : Ty {"
			+ "\n	entities"
			+ "\n		Professor Student Department"
			+ "\n 	foreign_keys"
			+ "\n		worksIn    : Professor -> Department"
			+ "\n		majoringIn : Student -> Department	"
			+ "\n} "
			+ "\n"
			+ "\nschema AdvisorMatches = literal : Ty {"
			+ "\n	imports"
			+ "\n		University"
			+ "\n	entities"
			+ "\n		Match"
			+ "\n 	foreign_keys"
			+ "\n		studentOf : Match -> Student"
			+ "\n		professorOf : Match -> Professor"
			+ "\n	path_equations"
			+ "\n		studentOf.majoringIn = professorOf.worksIn"
			+ "\n} "
			+ "\n"
			+ "\nquery findMatches = literal : University -> AdvisorMatches {"
			+ "\n entities"
			+ "\n	Department -> {from dept:Department}"
			+ "\n		"
			+ "\n	Professor -> {from prof:Professor}"
			+ "\n"
			+ "\n	Student -> {from stu:Student}"
			+ "\n"
			+ "\n/* When "
			+ "\n * "
			+ "\n *   where p.worksIn = s.majoringIn "
			+ "\n *   "
			+ "\n * is commented out the query is rejected: "
			+ "\n *   "
			+ "\n *   Error in query findMatches: "
			+ "\n *     Target equation v.studentOf.majoringIn = v.professorOf.worksIn not respected: "
			+ "\n *       transforms to s.majoringIn = p.worksIn, which is not provable in the sub-query for Match."
			+ "\n */  Match -> {from p:Professor s:Student "
			+ "\n		     where p.worksIn = s.majoringIn} "
			+ "\n"
			+ "\nforeign_keys		 	   "
			+ "\n"
			+ "\n	majoringIn -> {dept -> stu.majoringIn}"
			+ "\n	"
			+ "\n	worksIn -> {dept -> prof.worksIn}"
			+ "\n	"
			+ "\n	professorOf -> {prof -> p}"
			+ "\n	"
			+ "\n	studentOf -> {stu -> s}"
			+ "\n"
			+ "\n/* Disabling the equation validator will cause a runtime error when this query is executed:"
			+ "\n * "
			+ "\n *   Error in instance MatchesForUnivX : Algebra does not satisfy equation  "
			+ "\n *     forall v. v.studentOf.majoringIn = v.professorOf.worksIn "
			+ "\n *       on ID <Match p:[Euler],s:[Kleene]>"
			+ "\n */ /* options"
			+ "\n	    dont_validate_unsafe=true */"
			+ "\n"
			+ "\n} "
			+ "\n"
			+ "\n "
			+ "\ninstance UniversityOfX = literal : University {"
			+ "\n	generators"
			+ "\n		Gauss Church Euler : Professor"
			+ "\n		Riemann Turing Kleene : Student"
			+ "\n		math cs : Department"
			+ "\n	multi_equations"
			+ "\n		worksIn -> {Gauss math, Church cs, Euler math}"
			+ "\n		majoringIn -> {Riemann math, Turing cs, Kleene cs}		"
			+ "\n}"
			+ "\n"
			+ "\ninstance MatchesForUnivX = eval findMatches UniversityOfX"
			+ "\n";



}
