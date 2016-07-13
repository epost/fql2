package catdata.opl;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

import catdata.ide.CodeEditor;
import catdata.ide.Environment;
import catdata.ide.Language;
import catdata.ide.Program;


@SuppressWarnings("serial")
public class OplCodeEditor extends CodeEditor<Program<OplExp>, Environment<OplObject>, OplDisplay> {

	public OplCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.OPL.name();
	}

	@Override
	protected String getATMFrhs() {
		return "catdata.opl.OplTokenMaker";
	}

	protected void doTemplates() {
		  CompletionProvider provider = createCompletionProvider();
		  AutoCompletion ac = new AutoCompletion(provider);
		  KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, java.awt.event.InputEvent.META_DOWN_MASK
            | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
		  ac.setTriggerKey(key);
	      ac.install(this.topArea);
	}
	
	  private CompletionProvider createCompletionProvider() {
		   DefaultCompletionProvider provider = new DefaultCompletionProvider();
	
		provider.addCompletion(new ShorthandCompletion(provider, "theory", "theory {\n\tsorts;\n\tsymbols;\n\tequations;\n}", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "SCHEMA", "SCHEMA {\n\tentities;\n\tedges;\n\tattributes;\n\tpathEqualities;\n\tobsEqualities;\n} : ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "model", "model {\n\tsorts;\n\tsymbols;\n} : ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "javascript", "javascript {\n\tsymbols;\n} : ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "mapping", "mapping {\n\tsorts;\n\tsymbols;\n} :  -> ", ""));
				
		provider.addCompletion(new ShorthandCompletion(provider, "transform", "tranform {\n\tsorts;\n} :  ->  ", "")); 
		
		provider.addCompletion(new ShorthandCompletion(provider, "transpres", "transpres {\n\tsorts;\n} :  ->  ", "")); 
		
		provider.addCompletion(new ShorthandCompletion(provider, "presentation", "presentation {\n\tgenerators;\n\tequations;\n} : ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "INSTANCE", "INSTANCE {\n\tgenerators;\n\tequations;\n} : ", ""));
		return provider;
		
	}

	@Override
	protected Program<OplExp> parse(String program) throws ParserException {
		return OplParser.program(program);
	}

	 
	@Override
	protected OplDisplay makeDisplay(String foo, Program<OplExp> init, Environment<OplObject> env, long start, long middle) {
		return new OplDisplay(foo, init, env, start, middle);
	}

	String last_str;
	Program<OplExp> last_prog;
	Environment<OplObject> last_env;
	
	@Override
	protected Environment<OplObject> makeEnv(String str, Program<OplExp> init) {
		last_env = OplDriver.makeEnv(str, init, toUpdate, last_str, last_prog, last_env);
		last_prog = init;
		last_str = str;		
		return last_env;
	}

	@Override
	protected String textFor(Environment<OplObject> env) {
		return "Done.";
	} 

}
