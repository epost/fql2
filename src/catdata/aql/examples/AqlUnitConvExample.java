package catdata.aql.examples;

public class AqlUnitConvExample extends AqlExample {

	@Override
	public String getName() {
		return "Unit Conv";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "typeside Ty = literal { "
			+ "\n	java_types"
			+ "\n		in = \"java.lang.Double\""
			+ "\n		cm = \"java.lang.Double\"		"
			+ "\n	java_constants"
			+ "\n		in = \"return java.lang.Double.parseDouble(input[0])\""
			+ "\n		cm = \"return java.lang.Double.parseDouble(input[0])\""
			+ "\n	java_functions"
			+ "\n		inToCm : in -> cm = \"return (2.54 * input[0])\"		"
			+ "\n}"
			+ "\n"
			+ "\nschema AmericanAirplane = literal : Ty {"
			+ "\n	entities "
			+ "\n		Wing"
			+ "\n	attributes"
			+ "\n		wingLength : Wing -> in"
			+ "\n}"
			+ "\n"
			+ "\nschema EuropeanAirplane = literal : Ty {"
			+ "\n	entities "
			+ "\n		Wing"
			+ "\n	attributes"
			+ "\n		wingLength : Wing -> cm"
			+ "\n}"
			+ "\n"
			+ "\nquery AmericanToEuropean = literal : AmericanAirplane -> EuropeanAirplane {"
			+ "\n	entities "
			+ "\n		Wing -> {from w:Wing"
			+ "\n			    return wingLength -> inToCm(w.wingLength)}"
			+ "\n}"
			+ "\n"
			+ "\ninstance Boeing747 = literal : AmericanAirplane {"
			+ "\n	generators"
			+ "\n		left right : Wing"
			+ "\n	equations"
			+ "\n		left.wingLength = right.wingLength"
			+ "\n		left.wingLength = 500	"
			+ "\n}"
			+ "\n"
			+ "\ninstance Boeing747Metric = eval AmericanToEuropean Boeing747"
			+ "\n"
			+ "\n/*"
			+ "\n  Error in query AmericanToEuropean_disastrous_conversion: in attribute wingLength, expected sort of wingLength(w) has sort cm but wingLength(w) actually has sort in"
			+ "\n"
			+ "\nquery AmericanToEuropean_disastrous_conversion = literal : AmericanAirplane -> EuropeanAirplane {"
			+ "\n	entities "
			+ "\n		Wing -> {from w:Wing"
			+ "\n			    return wingLength -> w.wingLength}"
			+ "\n}"
			+ "\n*/"
			+ "\n";



}
