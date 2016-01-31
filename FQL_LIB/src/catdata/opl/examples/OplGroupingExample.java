package catdata.opl.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class OplGroupingExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Grouping";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S0 = theory { "
			+ "\n sorts"
			+ "\n 	Dept, Emp, Nat;"
			+ "\n symbols"
			+ "\n 	zero@0 : Nat, "
			+ "\n 	succ@1 : Nat -> Nat,"
			+ "\n 	deptID@10 : Dept -> Nat,"
			+ "\n	worksIn@8 : Emp -> Dept;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	entities Dept, Emp;	"
			+ "\n} : S0"
			+ "\n"
			+ "\nTy1 = types S "
			+ "\n"
			+ "\nJS1 = javascript {"
			+ "\n	symbols zero -> \"return 0\","
			+ "\n		   succ -> \"return input[0]+1\";"
			+ "\n} : Ty1"
			+ "\n"
			+ "\nI0 = presentation {"
			+ "\n	generators math, cs : Dept, david, patrick, ryan : Emp;"
			+ "\n	equations"
			+ "\n		worksIn(david)=math, worksIn(patrick)=math, worksIn(ryan)=cs,"
			+ "\n		deptID(math)=0, deptID(cs)=1;"
			+ "\n} : S0"
			+ "\nI = instance S I0 JS1"
			+ "\n"
			+ "\nS1x = theory { "
			+ "\n sorts"
			+ "\n 	Dept, Emp, Nat, SetOfEmp;"
			+ "\n symbols"
			+ "\n 	zero@0 : Nat, "
			+ "\n 	succ@1 : Nat -> Nat,"
			+ "\n 	deptID@10 : Dept -> Nat,"
			+ "\n	worksIn@8 : Emp -> Dept,"
			+ "\n	worksInID@9 : Nat -> SetOfEmp,"
			+ "\n	emps@11 : Dept -> SetOfEmp;"
			+ "\n equations"
			+ "\n  	forall x:Dept. emps(x) = worksInID(deptID(x));"
			+ "\n}"
			+ "\nS1 = schema {"
			+ "\n		entities Dept, Emp;"
			+ "\n} : S1x"
			+ "\nTy2 = types S1"
			+ "\n"
			+ "\n//does not use knuth-bendix, but should?"
			+ "\nJS2 = javascript {"
			+ "\n	symbols zero -> \"return 0\","
			+ "\n		   succ -> \"return input[0]+1\","
			+ "\n	        worksInID -> \"var util=Java.type('catdata.ide.Util'); "
			+ "\n	                      var chc=Java.type('catdata.Chc');"
			+ "\n	                      var term=Java.type('catdata.opl.OplTerm');"
			+ "\n	                      var wrap=Java.type('catdata.opl.JSWrapper');"
			+ "\n	                      var revd = util.rev0(I.saturate().second.symbols.get('deptID'));"
			+ "\n	                      var cand =  new term(chc.inRight(new wrap(input[0])), []);"
			+ "\n	          	       var dept =  revd.get(cand)[0];    "
			+ "\n	          	       var w = util.revS(I.saturate().second.symbols.get('worksIn'));"
			+ "\n	          	       var u = w.get(dept); "
			+ "\n	          	       var list=Java.type('java.util.LinkedList');"
			+ "\n	          	       var ret = new list();"
			+ "\n	          	       for (var i = 0; i < u.size(); i++) {"
			+ "\n	          	       	ret.add(new list(u)[i][0]);"
			+ "\n	          	       }"
			+ "\n	          	       return ret; \";"
			+ "\n} : Ty2 "
			+ "\n"
			+ "\n//requires to duplicate the instance"
			+ "\nI1 = presentation {"
			+ "\n	generators math, cs : Dept, david, patrick, ryan : Emp;"
			+ "\n	equations"
			+ "\n		worksIn(david)=math, worksIn(patrick)=math, worksIn(ryan)=cs,"
			+ "\n		deptID(math)=0, deptID(cs)=1;"
			+ "\n} : S1x"
			+ "\n"
			+ "\nJ = instance S1 I1 JS2"
			+ "\n";




}
