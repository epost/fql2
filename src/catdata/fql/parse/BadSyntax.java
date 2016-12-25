package catdata.fql.parse;

/**
 * 
 * @author ryan
 * 
 *         Exception for bad syntax
 */
@SuppressWarnings("WeakerAccess")
public class BadSyntax extends Exception {

	private static final long serialVersionUID = 1L;

	private static Tokens furthest;

	// String f = "unknown";

	@SuppressWarnings("unused")
	private BadSyntax() {
	}

	// keep static singleton of furthest match
	// add line numbers to tokens
	public BadSyntax(Tokens t, String s) {
		super(s);
		if (furthest == null) {
			furthest = t;
			// f = getMessage();
		}
		if (furthest.words().size() > t.words().size()) {
			furthest = t;
			// f = getMessage();
		}
	}


}
