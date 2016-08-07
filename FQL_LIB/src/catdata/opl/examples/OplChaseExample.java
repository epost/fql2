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

	String s = "//In a relational data exchange setting, the type side will be discrete (implies finite), or free "
			+ "\nTy = theory {"
			+ "\n	sorts"
			+ "\n		dom;"
			+ "\n	symbols"
			+ "\n		math, cs, \"1\", \"2\", \"3\", \"4\", \"5\", alice, bob, charlie, david, evan : dom;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n//The ED schema is always as follows"
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
			+ "\n//////////////////////////////////////////////////////////////////////////"
			+ "\n"
			+ "\n//source relational schema.  Note that in a relational data exchange setting, source constraints are irrelevant"
			+ "\nS = SCHEMA {"
			+ "\n	entities"
			+ "\n		SDeptEmp;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		SdeptId : SDeptEmp -> dom,"
			+ "\n		SmgrName : SDeptEmp -> dom,"
			+ "\n		SempId : SDeptEmp -> dom;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\n//target relational schema.  It has two data integrity consraints, TT_tgd1 (full) and TT_tgd2"
			+ "\nT = SCHEMA {"
			+ "\n	entities"
			+ "\n		TDept, TEmp;"
			+ "\n	edges;"
			+ "\n	attributes"
			+ "\n		TempId : TEmp -> dom,"
			+ "\n		TwrksIn : TEmp -> dom,"
			+ "\n		TdeptId : TDept -> dom,"
			+ "\n		TmgrId : TDept -> dom,"
			+ "\n		TmgrName : TDept -> dom;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\n//To encode a relational data exchange setting in OPL, we must union the source and target schemas together."
			+ "\nST = SCHEMA {"
			+ "\n	imports "
			+ "\n		S, T;"
			+ "\n	entities;"
			+ "\n	edges;"
			+ "\n	attributes;"
			+ "\n	pathEqualities;"
			+ "\n	obsEqualities;"
			+ "\n} : Ty"
			+ "\n"
			+ "\n//all EDs are expressed on the combined schema ST.  "
			+ "\n//Target constraints are target-to-target TGDs and EGDs. "
			+ "\n//Here are the two target constraints: "
			+ "\n"
			+ "\n//managers work in the departments they manage (stronger than FK)"
			+ "\n//Dept(d,m,n) -> Emp(m,d)"
			+ "\nTT_tgd1 = query {"
			+ "\n FORALL = {for d0:TDept; where; return; keys;} : FORALL,"
			+ "\n EXISTS = {for d :TDept, e:TEmp; "
			+ "\n 		 where d.TdeptId = e.TwrksIn,  d.TmgrId = e.TempId;"
			+ "\n 		 return; keys THERE={d0=d} : FORALL;} : EXISTS  "
			+ "\n} : ST -> ED "
			+ "\n"
			+ "\n//every employee works in some department (FK)"
			+ "\n//Emp(e,d) -> exists M,N. Dept(d,M,N)"
			+ "\nTT_tgd2 = query {"
			+ "\n FORALL = {for e0:TEmp; where; return; keys;} : FORALL,"
			+ "\n EXISTS = {for e :TEmp, d:TDept; "
			+ "\n 		 where d.TdeptId = e.TwrksIn;"
			+ "\n 		 return; keys THERE={e0=e} : FORALL;} : EXISTS  "
			+ "\n} : ST -> ED "
			+ "\n"
			+ "\n/////////////////////////////////////////////////////////////////////////"
			+ "\n"
			+ "\n//A data exchange  is expressed as source-to-target TGDs"
			+ "\n"
			+ "\n//every DeptEmp is a department and an employee"
			+ "\n//DeptEmp(d,n,e) -> exists M. Dept(d, M, n) , Emp(e, d)"
			+ "\nST_tgd1 = query {"
			+ "\n FORALL = {for de0:SDeptEmp; where; return; keys;} : FORALL,"
			+ "\n EXISTS = {for de :SDeptEmp, d:TDept, e:TEmp; "
			+ "\n 		 where de.SdeptId = d.TdeptId, d.TdeptId = e.TwrksIn, de.SdeptId = d.TdeptId, de.SempId = e.TempId;"
			+ "\n 		 return; keys THERE={de0=de} : FORALL;} : EXISTS  "
			+ "\n} : ST -> ED "
			+ "\n"
			+ "\n//test instance (on ST) DeptEmp(cs, alice, 1)"
			+ "\nI = INSTANCE {"
			+ "\n	generators"
			+ "\n		de:SDeptEmp;"
			+ "\n	equations"
			+ "\n		de.SdeptId = cs, de.SmgrName = alice, de.SempId = \"1\";"
			+ "\n} : ST"
			+ "\n"
			+ "\n//The three EDs are 'weakly acyclic'.  Thus we know any chase sequence will terminate in polynomial time."
			+ "\n//J = Dept(cs, null1, null2), Emp(1, cs), Emp(null2, cs)"
			+ "\n//intuitively, null2 = alice's manager id, null3 = alice's manager's name "
			+ "\nJ = chase I with {ST_tgd1, TT_tgd1, TT_tgd2} max 2"
			+ "\n";



}
