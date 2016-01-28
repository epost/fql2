package fql_lib.opl;

import org.codehaus.jparsec.error.ParserException;
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

	@Override
	protected void doTemplates() {
		CodeTemplateManager ctm = topArea.getCodeTemplateManager();
		
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
		
		ct = new StaticCodeTemplate("transpres", "transpres ",
				"{\n\tsorts;\n}\n :  ->  "); 
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("presentation", "presentation ",
				"{\n\tgenerators;\n\tequations;\n}\n : ");
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
