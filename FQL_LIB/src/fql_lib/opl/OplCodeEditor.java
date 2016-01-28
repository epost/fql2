package fql_lib.opl;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;

import fql_lib.core.CodeEditor;
import fql_lib.core.Language;


@SuppressWarnings("serial")
public class OplCodeEditor extends CodeEditor<OplProgram, OplEnvironment, OplDisplay> {

	public OplCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public Language isPatrick() {
		return Language.OPL;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.OPL.name();
	}

	@Override
	protected String getATMFrhs() {
		return "fql_lib.opl.OplTokenMaker";
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
	
		CodeTemplateManager ctm = topArea.getCodeTemplateManager();
		
		provider.addCompletion(new ShorthandCompletion(provider, "theory", "theory {\n\tsorts;\n\tsymbols;\n\tequations;\n}", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "model", "model {\n\tsorts;\n\tsymbols;\n}\n : ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "javascript", "javascript {\n\tsymbols;\n}\n : ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "mapping", "mapping {\n\tsorts;\n\tsymbols;\n}\n :  -> ", ""));
				
		provider.addCompletion(new ShorthandCompletion(provider, "transform", "tranform {\n\tsorts;\n}\n :  ->  ", "")); 
		
		provider.addCompletion(new ShorthandCompletion(provider, "transpres", "transpres {\n\tsorts;\n}\n :  ->  ", "")); 
		
		provider.addCompletion(new ShorthandCompletion(provider, "presentation", "presentation {\n\tgenerators;\n\tequations;\n}\n : ", ""));

		return provider;
		
	}

	
	
	@Override
	protected OplProgram parse(String program) throws ParserException {
		return OplParser.program(program);
	}

	@Override
	protected OplDisplay makeDisplay(String foo, OplProgram init, OplEnvironment env, long start, long middle) {
		return new OplDisplay(foo, init, env, start, middle);
	}

	@Override
	protected OplEnvironment makeEnv(String str, OplProgram init) {
		return OplDriver.makeEnv(str, init);
	}

	@Override
	protected String textFor(OplEnvironment env) {
		return "Done.";
	} 

}
