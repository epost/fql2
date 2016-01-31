package fql_lib.examples;

import fql_lib.ide.Example;
import fql_lib.ide.Language;

public class PropExample extends Example {

	@Override
	public Language lang() {
		return Language.FQLPP;
	}

	@Override
	public String getName() {
		return "Prop";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//for better performance, disable validation"
			+ "\n"
			+ "\ncategory S = { "
			+ "\n objects"
			+ "\n 	Employee, Department, dname, fname;"
			+ "\n arrows"
			+ "\n 	name  : Department -> dname,"
			+ "\n 	first : Employee -> fname,"
			+ "\n	manager   : Employee -> Employee,"
			+ "\n	worksIn   : Employee -> Department,"
			+ "\n	secretary : Department -> Employee;"
			+ "\n equations  "
			+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
			+ "\n  	Department.secretary.worksIn = Department,"
			+ "\n  	Employee.manager.manager = Employee.manager;"
			+ "\n}"
			+ "\n"
			+ "\nfunctor J = {"
			+ "\n objects"
			+ "\n	Employee -> { 101, 102, 103 },"
			+ "\n	Department -> { q10, x02 },"
			+ "\n	dname -> {AppliedMath, PureMath},"
			+ "\n	fname -> {Alan, Camille, Andrey};"
			+ "\narrows"
			+ "\n	first -> { (101, Alan), (102, Camille), (103, Andrey) },"
			+ "\n	name  -> { (q10, AppliedMath), (x02, PureMath) },"
			+ "\n	manager -> { (101, 101), (102, 102), (103, 103) },"
			+ "\n	worksIn -> { (101, q10), (102, x02), (103, q10) },"
			+ "\n	secretary -> { (q10, 101), (x02, 102) };"
			+ "\n} : S -> Set"
			+ "\n"
			+ "\nfunctor I = {"
			+ "\n objects"
			+ "\n	Employee -> { x101, x102 },"
			+ "\n	Department -> { xq10, xx02 },"
			+ "\n	dname -> {xAppliedMath, xPureMath},"
			+ "\n	fname -> {xAlan, xCamille, xAndrey};"
			+ "\n arrows"
			+ "\n	first -> { (x101, xAlan), (x102, xCamille) },"
			+ "\n	name  -> { (xq10, xAppliedMath), (xx02, xPureMath) },"
			+ "\n	manager -> { (x101, x101), (x102, x102) },"
			+ "\n	worksIn -> { (x101, xq10), (x102, xx02) },"
			+ "\n	secretary -> { (xq10, x101), (xx02, x102) };"
			+ "\n} : S -> Set"
			+ "\n"
			+ "\ntransform t = {"
			+ "\n objects "
			+ "\n	Employee -> { (x101,101), (x102,102) },"
			+ "\n	Department -> { (xq10,q10), (xx02,x02) },"
			+ "\n	dname -> {(xAppliedMath,AppliedMath), (xPureMath,PureMath)},"
			+ "\n	fname -> {(xAlan,Alan), (xCamille,Camille), (xAndrey,Andrey)};"
			+ "\n} : (I:S->Set) -> (J:S->Set)"
			+ "\n"
			+ "\nfunctor prp = prop S"
			+ "\nfunctor one = unit S Set"
			+ "\n"
			+ "\ntransform tru = true S // true"
			+ "\ntransform fals = false S // false"
			+ "\n"
			+ "\ntransform char_t = char t"
			+ "\n"
			+ "\n//these two transforms are equal"
			+ "\ntransform lhs = (t ; char_t)"
			+ "\ntransform rhs = (tt I ; tru)"
			+ "\n"
			+ "\ntransform char_t2 = kernel char_t"
			+ "\nfunctor ker = dom char_t2"
			+ "\n//I and ker are isomorphic"
			+ "\ntransform iso = iso1 I ker //not the isomorphism people would pick"
			+ "\ntransform should_equal_t = (iso ; char_t2) //= t (mod iso)"
			+ "\n"
			+ "\n// Intuitionistic Propositional logic ///////////////////////////////////////"
			+ "\n"
			+ "\ncategory X = {"
			+ "\n	objects n, e;"
			+ "\n	arrows att : n -> e;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ntransform NOT = not X"
			+ "\ntransform AND = and X"
			+ "\ntransform OR  = or  X"
			+ "\ntransform IMP = implies X"
			+ "\n";



}
