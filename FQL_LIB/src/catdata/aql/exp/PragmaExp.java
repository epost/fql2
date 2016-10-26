package catdata.aql.exp;

import catdata.aql.Pragma;

public abstract class PragmaExp extends Exp<Pragma> {
	
	public Kind kind() {
		return Kind.PRAGMA;
	}
	

	
	
}