package catdata.fql.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class EDsToFqlExample extends Example {

	@Override
	public Language lang() {
		return Language.FQL;
	}
	
	@Override
	public String getName() {
		return "EDs to FQL";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "//This FQL program shows how to implement the EDs of Example 2 in \"Functorial Schema Mapping\" paper."
			+ "\n"
			+ "\n/* "
			+ "\n * Begin with an instance I1 on the loop schema. We will produce three instances on the unit schema."
			+ "\n * The instance Managers will be the set of people that manage someone."
			+ "\n * The instance QuidProQuo will be the set of pairs (A,B) such that A manages B and B manages A."
			+ "\n *    Note, this includes those (A,A) where A manages A."
			+ "\n * The instance InhabitedEmp will be empty if Emp is empty, and have one element otherwise."
			+ "\n */"
			+ "\n"
			+ "\nschema Loop = {"
			+ "\n	nodes N;"
			+ "\n	attributes;"
			+ "\n	arrows f:N->N;"
			+ "\n	equations N.f.f.f.f=N.f.f;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I1 = {"
			+ "\n	nodes N->{i1,i2,i3,i4,i5,i6,i7};"
			+ "\n	attributes;"
			+ "\n	arrows f->{(i1,i2),(i2,i3),(i3,i4),(i4,i3),(i5,i5),(i6,i7),(i7,i7)};"
			+ "\n} :  Loop"
			+ "\n"
			+ "\nschema Arrow = {"
			+ "\n	nodes a,b;"
			+ "\n	attributes;"
			+ "\n	arrows f:a->b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping Arr2Loo = {"
			+ "\n	nodes a->N, b->N;"
			+ "\n	attributes;"
			+ "\n	arrows f->N.f;"
			+ "\n} : Arrow -> Loop"
			+ "\n"
			+ "\ninstance I2=delta Arr2Loo I1"
			+ "\n"
			+ "\nschema Cospan = {"
			+ "\n	nodes a1,a2,b;"
			+ "\n	attributes;"
			+ "\n	arrows f1:a1->b, f2:a2->b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping Cos2Arr = {"
			+ "\n	nodes a1->a, a2->a, b->b;"
			+ "\n	attributes;"
			+ "\n	arrows f1->a.f, f2->a.f;"
			+ "\n} : Cospan -> Arrow"
			+ "\n"
			+ "\ninstance I3=delta Cos2Arr I2"
			+ "\n"
			+ "\nschema Square = {"
			+ "\n	nodes NW,NE,SW,SE;"
			+ "\n	attributes;"
			+ "\n	arrows n:NW->NE,w:NW->SW,e:NE->SE,s:SW->SE;"
			+ "\n	equations NW.n.e=NW.w.s;"
			+ "\n}"
			+ "\n"
			+ "\nmapping Cos2Squ = {"
			+ "\n	nodes a1->NE,a2->SW,b->SE;"
			+ "\n	attributes;"
			+ "\n	arrows f1->NE.e,f2->SW.s;"
			+ "\n} : Cospan -> Square"
			+ "\n"
			+ "\ninstance I4 = pi Cos2Squ I3"
			+ "\n"
			+ "\nschema Span = {"
			+ "\n	nodes a,b1,b2;"
			+ "\n	attributes;"
			+ "\n	arrows f1:a->b1,f2:a->b2;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping Spa2Squ = {"
			+ "\n	nodes a->NW,b1->NE,b2->SW;"
			+ "\n	attributes;"
			+ "\n	arrows f1->NW.n,f2->NW.w;"
			+ "\n} : Span -> Square"
			+ "\n"
			+ "\ninstance I5 = delta Spa2Squ I4"
			+ "\n"
			+ "\nmapping USpan=unit {} Span "
			+ "\n"
			+ "\n//Managers"
			+ "\n"
			+ "\ninstance Managers=SIGMA USpan I5"
			+ "\n"
			+ "\nschema TwoNodeLoop = {"
			+ "\n	nodes a,b;"
			+ "\n	attributes;"
			+ "\n	arrows f:a->b,g:b->a;"
			+ "\n	equations a.f.g.f.g=a.f.g,b.g.f.g.f=b.g.f;"
			+ "\n}  "
			+ "\n"
			+ "\nmapping Two2Loo = {"
			+ "\n	nodes a->N,b->N;"
			+ "\n	attributes;"
			+ "\n	arrows f->N.f,g->N.f;"
			+ "\n} : TwoNodeLoop -> Loop"
			+ "\n"
			+ "\ninstance I6=delta Two2Loo I1"
			+ "\n"
			+ "\nmapping UTwo = unit {} TwoNodeLoop"
			+ "\n"
			+ "\n//QuidProQuo"
			+ "\n"
			+ "\ninstance QuidProQuo = pi UTwo I6"
			+ "\n"
			+ "\nschema T = {"
			+ "\n	nodes N;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping T2Loop = {"
			+ "\n	nodes N->N;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n} : T -> Loop"
			+ "\n"
			+ "\ninstance I7=delta T2Loop I1"
			+ "\n"
			+ "\n//InhabitedEmp"
			+ "\n"
			+ "\ninstance InhabitedEmp=relationalize I7";




}