package fql_lib.examples;

public class Patrick16Example extends Example {
	
	@Override
	public boolean isPatrick() {
		return true;
	}

	@Override
	public String getName() {
		return "P Flower 2";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "adom : type"
			+ "\n"
			+ "\nWisnesky Spivak Chlipala Morrisett Malecha Gross Harvard MIT Math CS : adom"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	nodes Person, School, Dept;"
			+ "\n	edges advisor : Person -> Person, "
			+ "\n	      instituteOf : Person -> School, "
			+ "\n	      deptOf : Person -> Dept,"
			+ "\n	      biggestDept : School -> Dept,"
			+ "\n	      lastName : Person -> adom,"
			+ "\n	      schoolName : School -> adom,"
			+ "\n	      deptName : Dept -> adom;"
			+ "\n	equations advisor . advisor = advisor;"
			+ "\n}"
			+ "\n"
			+ "\nI = instance {"
			+ "\n	variables ryan david adam greg gregory jason : Person,"
			+ "\n	          harvard mit : School, math cs : Dept;"
			+ "\n	equations ryan.lastName = Wisnesky, ryan.advisor = david, ryan.instituteOf = harvard, ryan.deptOf = math,"
			+ "\n	          gregory.lastName = Malecha, gregory.advisor = greg,  gregory.instituteOf = harvard, gregory.deptOf = cs,"
			+ "\n	          jason.lastName = Gross, jason.advisor = adam, jason.instituteOf = mit, jason.deptOf = math,"
			+ "\n	          adam.lastName = Chlipala, adam.instituteOf = mit, adam.deptOf = cs,"
			+ "\n	          greg.lastName = Morrisett, greg.instituteOf = harvard, greg.deptOf = cs,"
			+ "\n	          david.lastName = Spivak, david.instituteOf = mit, david.deptOf = math,"
			+ "\n	          mit.schoolName = MIT, harvard.schoolName = Harvard,"
			+ "\n	          math.deptName = Math, cs.deptName = CS,"
			+ "\n	          harvard.biggestDept = math, mit.biggestDept = cs;"
			+ "\n} : S "
			+ "\n"
			+ "\n//Find all the people advised by someone whose school's biggest department is math"
			+ "\nBiggestDeptIsMath = flower {"
			+ "\n	select p.lastName as name, p.instituteOf.schoolName as school;"
			+ "\n	from Person as p;"
			+ "\n	where p.advisor.instituteOf.biggestDept.deptName = \"!__Q\".Math;"
			+ "\n} I "
			+ "\n"
			+ "\nAllPeople = FLOWER {"
			+ "\n	select p.lastName as name, p.instituteOf.schoolName as school;"
			+ "\n	from Person as p;"
			+ "\n	where (p.advisor.instituteOf.biggestDept.deptName = \"!__Q\".Math or"
			+ "\n	       p.advisor.instituteOf.biggestDept.deptName = \"!__Q\".CS);"
			+ "\n} I "
			+ "\n"
			+ "\n//Find all people sharing the same school, and the name of their schools"
			+ "\nSameSchool = flower {"
			+ "\n	select p1.lastName as name1, p2.lastName as name2, p1.instituteOf.schoolName as school;"
			+ "\n	from Person as p1, Person as p2;"
			+ "\n	where p1.instituteOf = p2.instituteOf;"
			+ "\n} I "
			+ "\n"
			+ "\n///////////"
			+ "\n";



}
