package catdata.aql;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import catdata.ide.CodeEditor;
import catdata.ide.Language;
import catdata.ide.Program;

@SuppressWarnings("serial")
public class AqlCodeEditor extends
		CodeEditor<Program<Exp>, Env, Display> {

	public AqlCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public Language lang() {
		return Language.AQL;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.AQL.name();
	}

	@Override
	protected String getATMFrhs() {
		return "catdata.aql.AqlTokenMaker";
	}

	protected void doTemplates() {
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				java.awt.event.InputEvent.META_DOWN_MASK
						| java.awt.event.InputEvent.SHIFT_DOWN_MASK);
		ac.setTriggerKey(key);
		ac.install(this.topArea);
	}

	private CompletionProvider createCompletionProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();

		
		return provider;

	}

	@Override
	protected Program<Exp> parse(String program) throws ParserException {
		//return OplParser.program(program);
		throw new RuntimeException();
	}

	@Override
	protected Display makeDisplay(String foo, Program<Exp> init,
			Env env, long start, long middle) {
			Display ret = new Display(foo, init, env, start, middle);
			return ret;
	}

	String last_str;
	Program<Exp> last_prog;
	Env last_env;

	@Override
	protected Env makeEnv(String str, Program<Exp> init) {
			last_env = Driver.makeEnv(str, init, toUpdate, last_str,
					last_prog, last_env);
			last_prog = init;
			last_str = str;
			return last_env;
	}



	@Override
	protected String textFor(Env env) {
		return "Done.";
	}

}
