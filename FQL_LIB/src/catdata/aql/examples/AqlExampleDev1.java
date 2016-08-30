package catdata.aql.examples;

import catdata.aql.AqlExample;

public class AqlExampleDev1 extends AqlExample {

	@Override
	public String getName() {
		return "Test 1";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "typeside t = empty"
			+ "\n"
			+ "\nschema s = empty t"
			+ "\nmapping sid = id s"
			+ "\n"
			+ "\ninstance i = empty s"
			+ "\ninstance j = empty empty empty"
			+ "\ntransform iid = id i"
			+ "\n"
			+ "\ntypeside t2 = t"
			+ "\nschema s2 = s"
			+ "\ninstance i2 = i"
			+ "\n"
			+ "\ntypeside t3 = typesideOf s "
			+ "\n"
			+ "\nschema s3 = schemaOf i"
			+ "\n";

}
