package fql_lib.opl;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;

import fql_lib.gui.CodeEditor;


@SuppressWarnings("serial")
public class OplCodeEditor extends CodeEditor<OplProgram, OplEnvironment, OplDisplay> {

	public OplCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public String isPatrick() {
		return "opl";
	}

	@Override
	protected String getATMFlhs() {
		return "text/opl";
	}

	@Override
	protected String getATMFrhs() {
		return "fql_lib.opl.OplTokenMaker";
	}

	@Override
	protected void doTemplates() {
		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		
		StaticCodeTemplate ct = new StaticCodeTemplate("theory", "theory ",
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
