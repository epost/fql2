package catdata.aql;

import catdata.ide.Example;
import catdata.ide.Language;

public abstract class AqlExample extends Example {

	@Override
	public Language lang() {
		return Language.AQL;
	}
	
}
