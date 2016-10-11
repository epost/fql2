package catdata.aql;

import java.util.function.Function;

import javax.swing.JComponent;

import catdata.Pair;
import catdata.Unit;
import catdata.ide.CodeTextPanel;
import catdata.ide.Language;
import catdata.ide.Options;

public final class AqlOptionsDefunct extends Options {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return Language.AQL.toString();
	}
	
	String msg = "completion_precedence = \"a b c\" means a < b < c";

	@Override
	public Pair<JComponent, Function<Unit, Unit>> display() {
		return new Pair<>(new CodeTextPanel("", "Aql options are specified as pragmas in each Aql file.\nHere are the available options and their defaults:\n\n\t" + AqlOptions.printDefault() + "\n\n" + msg), x -> x);
	}

	@Override
	public int size() {
		return 1;
	}

}
