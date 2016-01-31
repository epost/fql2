package catdata.fpqlpp;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

import catdata.ide.CodeEditor;
import catdata.ide.Language;
import catdata.opl.OplEnvironment;


@SuppressWarnings("serial")
public class ACodeEditor extends CodeEditor<AProgram, OplEnvironment, ADisplay> {

	public ACodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public Language isPatrick() {
		return Language.FPQLPP;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.FPQLPP.name();
	}

	@Override
	protected String getATMFrhs() {
		return "fql_lib.A.ATokenMaker";
	}

	@Override
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
	
		   provider.addCompletion(new ShorthandCompletion(provider, "query",
	            "newstuff1", "newstuff2"));

		   return provider;

	   }
	      
		//CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		//ctm.
		
		/*StaticCodeTemplate ct = new StaticCodeTemplate("theory", "theory ",
				"{\n\tsorts;\n\tsymbols;\n\tequations;\n}");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("model", "model ",
				"{\n\tsorts;\n\tsymbols;\n}\n : ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("javascript", "javascript ",
				"{\n\tsymbols;\n}\n : ");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("mapping", "mapping ",
				"{\n\tsorts;\n\tsymbols;\n}\n :  -> ");
		ctm.addTemplate(ct);
				
		ct = new StaticCodeTemplate("transform", "tranform ",
				"{\n\tsorts;\n}\n :  ->  "); 
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("transpres", "transpres ",
				"{\n\tsorts;\n}\n :  ->  "); 
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("presentation", "presentation ",
				"{\n\tgenerators;\n\tequations;\n}\n : ");
		ctm.addTemplate(ct); */
		
	//}

	
	
	@Override
	protected AProgram parse(String program) throws ParserException {
		return AParser.program(program);
	}

	@Override
	protected ADisplay makeDisplay(String foo, AProgram init, OplEnvironment env, long start, long middle) {
		return new ADisplay(foo, init, env, start, middle);
	}

	@Override
	protected OplEnvironment makeEnv(String str, AProgram init) {
		return ADriver.makeEnv(str, init);
	}

	@Override
	protected String textFor(OplEnvironment env) {
		return "Done.";
	} 

}
