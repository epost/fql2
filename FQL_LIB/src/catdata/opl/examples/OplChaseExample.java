package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplChaseExample extends Example {

	@Override
	public Language lang() {
		return Language.OPL;
	}
	
 	@Override
	public String getName() {
		return "Chase";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "Ty = theory {"
			+ "\n	sorts"
			+ "\n		dom;"
			+ "\n	symbols"
			+ "\n		math, cs, \"1\", \"2\", \"3\", \"4\", \"5\", alice, bob, charlie, david, evan : dom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nED = SCHEMA {"
			+ "\n	entities"
			+ "\n		FORALL, EXISTS;"
			+ "\n	edges"
			+ "\n		THERE : EXISTS -> FORALL;"
			+ "\n	attributes;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\nS = SCHEMA {"
			+ "\n	entities"
			+ "\n		Emp, Dept;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		empId : Emp -> dom,"
			+ "\n		empName : Emp -> dom,"
			+ "\n		empWorksInDeptId : Emp -> dom,"
			+ "\n		deptId : Dept -> dom,"
			+ "\n		deptName : Dept -> dom,"
			+ "\n		deptSecrEmpId : Dept -> dom;	"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\n"
			+ "\n//todo: manager"
			+ "\n"
			+ "\n//deptSecrEmpId is a foreign key to Emp "
			+ "\n//Dept(deptId, deptName, deptSecrEmpId) -> exists empName, empWorksInDeptId. Emp(deptSecrEmpId, empName, empWorksInDeptId)"
			+ "\nED1 = query {"
			+ "\n FORALL = {for d0:Dept; where; return; keys;} : FORALL,"
			+ "\n EXISTS = {for d :Dept, e:Emp; where d.deptSecrEmpId = e.empId; return; keys THERE={d0=d} : FORALL;} : EXISTS  "
			+ "\n} : S -> ED "
			+ "\n"
			+ "\n//deptSecrEmpId is a foreign key in Dept "
			+ "\n//Emp(empId, empName, empWorksInDeptId) -> exists deptId, deptName, deptSecrEmpId. Dept(empWorksInDeptId, deptId, deptName, deptSecrEmpId) "
			+ "\nED2 = query {"
			+ "\n FORALL = {for e0:Emp; where; return; keys;} : FORALL,"
			+ "\n EXISTS = {for e :Emp, d:Dept; where e.empWorksInDeptId = d.deptId; return; keys THERE={e0=e} : FORALL;} : EXISTS  "
			+ "\n} : S -> ED"
			+ "\n"
			+ "\n//every secretary works in their own department "
			+ "\n//Dept(empWorksInDeptId, deptId, deptName, deptSecrEmpId), Emp(empId, empName, empWorksInDeptId) -> deptSecrEmpId = empWorksInDeptId  "
			+ "\nED3 = query {"
			+ "\n FORALL = {for e0:Emp, d0:Dept; where d0.deptSecrEmpId = e0.empId; return; keys;} : FORALL,"
			+ "\n EXISTS = {for e :Emp, d :Dept; where  d.deptSecrEmpId =  e.empId, e.empWorksInDeptId = d.deptId; return; keys THERE={e0=e, d0=d} : FORALL;} : EXISTS  "
			+ "\n} : S -> ED "
			+ "\n"
			+ "\n//empId is a primary key for Emp"
			+ "\n//Emp(empId, empNameX, empWorksInDeptIdX), Emp(empId, empName, empWorksInDeptId) -> empNameX = empName, empWorksInDeptIdX = empWorksInDeptId "
			+ "\nED4 = query {"
			+ "\n FORALL = {for e1x:Emp, e2x:Emp; where e1x.empId = e2x.empId; return; keys;} : FORALL,"
			+ "\n EXISTS = {for e1 :Emp, e2 :Emp; where  e1.empId =  e2.empId, e1.empWorksInDeptId = e2.empWorksInDeptId, e1.empName = e2.empName; return; keys THERE={e1x=e1,e2x=e2} : FORALL;} : EXISTS  "
			+ "\n} : S -> ED "
			+ "\n"
			+ "\n//deptId is a primary key for Dept"
			+ "\n//Dept(deptId, deptName, deptSecrEmpId), Dept(deptId, deptNameX, deptSecrEmpIdX) -> deptName = deptNameX, deptSecrEmpId = deptSecrEmpIdX"
			+ "\nED5 = query {"
			+ "\n FORALL = {for d1x:Dept, d2x:Dept; where d1x.deptId = d2x.deptId; return; keys;} : FORALL,"
			+ "\n EXISTS = {for d1 :Dept, d2 :Dept; where  d1.deptId =  d2.deptId, d1.deptName = d2.deptName, d1.deptSecrEmpId = d2.deptSecrEmpId; return; keys THERE={d1x=d1, d2x=d2} : FORALL;} : EXISTS  "
			+ "\n} : S -> ED"
			+ "\n"
			+ "\nI = INSTANCE {"
			+ "\ngenerators;"
			+ "\nequations;"
			+ "\n} : S"
			+ "\n"
			+ "\nJ = chase I with {ED1, ED2, ED3, ED4, ED5} max 10"
			+ "\n";

}
