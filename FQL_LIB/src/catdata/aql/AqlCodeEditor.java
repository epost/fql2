package catdata.aql;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

import catdata.ide.CodeEditor;
import catdata.ide.Language;
import catdata.ide.Program;

@SuppressWarnings("serial")
public final class AqlCodeEditor extends
		CodeEditor<Program<Exp<? extends Object>>, AqlEnv, AqlDisplay> {

	public AqlCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
		SyntaxScheme scheme = topArea.getSyntaxScheme();
		scheme.getStyle(Token.RESERVED_WORD).foreground = Color.RED;
		scheme.getStyle(Token.RESERVED_WORD_2).foreground = Color.BLUE;
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
		return "catdata.aql.AqlTokenMaker"; //TODO
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

		provider.addCompletion(new ShorthandCompletion(provider, "typeside",
				"typeside ? = literal {\n\timports\n\ttypes\n\tsconstants\n\tfunctions\n\tequations\n\tjava_types\n\tjava_constants\n\tjava_functions\n\toptions\n} ", ""));

		provider.addCompletion(new ShorthandCompletion(
				provider,
				"schema",
				"schema ? = literal : ? {\n\timports\n\tforeign_keys\n\tpath_equations\n\tattributes\n\tobservation_equations\n\toptions\n} ",
				""));
		
		provider.addCompletion(new ShorthandCompletion(
				provider,
				"instance",
				"instance ? = literal : ? {\n\timports\n\tgenerators\n\tequations\n\toptions\n} ",
				""));

		provider.addCompletion(new ShorthandCompletion(provider, "mapping",
				"mapping ? = literal : ? -> ? {\n\timports\n\tentities\n\tforeign_keys\n\tattributes\n\toptions\n} ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "transform",
				"transform ? = literal : ? -> ? {\n\timports\n\tgenerators\n\toptions\n} ", ""));

			
		return provider;

	}

	@Override
	protected Program<Exp<? extends Object>> parse(String program) throws ParserException {
		return AqlParser.parseProgram(program);
	}

	@Override
	protected AqlDisplay makeDisplay(String foo, Program<Exp<? extends Object>> init,
			AqlEnv env, long start, long middle) {
			AqlDisplay ret = new AqlDisplay(foo, init, env, start, middle);
			return ret;
	}

	String last_str;
	Program<Exp<? extends Object>> last_prog;
	AqlEnv last_env;

	@Override
	protected AqlEnv makeEnv(String str, Program<Exp<? extends Object>> init) {
			last_env = AqlDriver.makeEnv(str, init, toUpdate, last_str,
					last_prog, last_env);
			last_prog = init;
			last_str = str;
			return last_env;
	}



	@Override
	protected String textFor(AqlEnv env) {
		return "Done.";
	}

}
