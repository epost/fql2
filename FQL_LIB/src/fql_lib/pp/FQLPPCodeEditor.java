package fql_lib.pp;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;

import fql_lib.core.CodeEditor;
import fql_lib.core.Display;
import fql_lib.core.Language;

@SuppressWarnings("serial")
public class FQLPPCodeEditor extends CodeEditor<FQLPPProgram, FQLPPEnvironment, Display> {

	public FQLPPCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public Language isPatrick() {
		return Language.FQLPP;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.FQLPP.name();
	}

	@Override
	protected String getATMFrhs() {
		return "fql_lib.pp.FqlPPTokenMaker";
	}

	@Override
	protected void doTemplates() {
		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		CodeTemplate ct = new StaticCodeTemplate("set", "set ",
				" = { }");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("function", "function ",
				" = { } :  -> ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("category", "category ",
				" = {\n\tobjects;\n\tarrows;\n\tequations;\n}");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("functor", "functor ",
				" = {\n\tobjects;\n\tarrows;\n} :  -> ");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("transform", "transform ",
				" = {\n\tobjects;\n} : ( :  -> ) -> ( :  -> ) "); 
		ctm.addTemplate(ct);
	}

	@Override
	protected FQLPPEnvironment makeEnv(String program, FQLPPProgram init) {
		return FQLPPDriver.makeEnv(program, init);
	}

	@Override
	protected FQLPPProgram parse(String program) throws ParserException {
		return PPParser.program(program);
	}

	@Override
	protected Display makeDisplay(String foo, FQLPPProgram init, FQLPPEnvironment env, long start, long middle) {
		return new Display(foo, init, env);
	}

	@Override
	protected String textFor(FQLPPEnvironment env) {
		return "Done";
	}

}
