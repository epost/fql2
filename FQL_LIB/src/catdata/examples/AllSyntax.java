package catdata.examples;

import catdata.ide.Example;
import catdata.ide.Language;

public class AllSyntax extends Example {

	@Override
	public Language lang() {
		return Language.FQLPP;
	}
	
	@Override
	public String getName() {
		return "All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "//Syntax for the FQL fragment of FQL++"
			+ "\n"
			+ "\ncategory c1 = void"
			+ "\ncategory c2 = unit"
			+ "\ncategory c3 = (c2 + c2)"
			+ "\ncategory c4 = (c2 * c2)"
			+ "\ncategory c5 = (unit ^ void)"
			+ "\ncategory c6 = Set"
			+ "\ncategory C  = {objects; arrows; equations;}"
			+ "\ncategory CC = union C C"
			+ "\n"
			+ "\nfunctor f1 = id unit"
			+ "\nfunctor f2 = (f1 ; f1)"
			+ "\nfunctor f4 = iso1 c4 c5"
			+ "\nfunctor f6 = iso2 c4 c5"
			+ "\nfunctor f7 = (fst c3 c3 * snd c3 c3) "
			+ "\nfunctor f8  = (inl c3 c3 + inr c3 c3) "
			+ "\nfunctor f9  = curry eval c3 c3 "
			+ "\nfunctor f10 = {objects; arrows;} : C -> C //mapping"
			+ "\nfunctor f20 = {objects; arrows;} : C -> Set //instance"
			+ "\nfunctor f13 = (f20 ^ f20) "
			+ "\nfunctor f14 = delta f10"
			+ "\nfunctor f15 = sigma f10"
			+ "\nfunctor f16 = pi f10"
			+ "\nfunctor f17 = apply f14 on object f20"
			+ "\nfunctor f18 = prop C"
			+ "\nfunctor t21 = void C Set //need Set here"
			+ "\nfunctor t22 = unit C Set //need Set here"
			+ "\nfunctor f3 = ff unit"
			+ "\nfunctor f5 = tt unit"
			+ "\n"
			+ "\ncategory c6x = dom f1"
			+ "\ncategory c7x = cod f2"
			+ "\n"
			+ "\ntransform t0 = {objects;} : (f20:C->Set) -> (f20:C->Set) //must have dom and cod of functors  "
			+ "\ntransform t1 = id f20"
			+ "\ntransform t2 = (t1 ; t1) "
			+ "\ntransform t3x= (inl f20 f20 + inr f20 f20) "
			+ "\ntransform t4x= (fst f20 f20 * snd f20 f20) "
			+ "\ntransform t5 = curry eval f20 f20"
			+ "\ntransform t5x = tt f20"
			+ "\ntransform t5y = ff f20"
			+ "\ntransform t6 = apply f14 on arrow t1"
			+ "\ntransform t7 = true C"
			+ "\ntransform t8 = false C"
			+ "\ntransform t9 = and C"
			+ "\ntransform t7y= or C"
			+ "\ntransform t7x= not C"
			+ "\ntransform t8z= implies C"
			+ "\ntransform t8x= return sigma delta f10"
			+ "\ntransform t9x= coreturn sigma delta f10"
			+ "\ntransform t10= return delta pi f10"
			+ "\ntransform t11= coreturn delta pi f10"
			+ "\ntransform t12= iso1 f20 f20"
			+ "\ntransform t13= iso2 f20 f20"
			+ "\n"
			+ "\nfunctor f19x = dom t1"
			+ "\nfunctor t20x = cod t1"
			+ "\n";


}
