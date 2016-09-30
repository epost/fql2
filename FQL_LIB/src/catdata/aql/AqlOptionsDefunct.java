package catdata.aql;

import java.util.Arrays;
import java.util.function.Function;

import javax.swing.JComponent;

import catdata.Pair;
import catdata.Unit;
import catdata.Util;
import catdata.ide.CodeTextPanel;
import catdata.ide.Language;
import catdata.ide.Options;

public final class AqlOptionsDefunct extends Options {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return Language.AQL.toString();
	}
	
	String msg = "precedence = \"a b c\" means a < b < c";

	@Override
	public Pair<JComponent, Function<Unit, Unit>> display() {
		String str = Util.sep(Arrays.asList(AqlOption.values()), "\n");
		return new Pair<>(new CodeTextPanel("", "Aql options are specified as pragmas in each Aql file.\nHere are the available options: " + str + "\n\n" + msg), x -> x);
	}

	@Override
	public int size() {
		return 1;
	}

}
