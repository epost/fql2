package fql_lib.gui;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;

import fql_lib.Driver;
import fql_lib.decl.Environment;
import fql_lib.decl.FQLParser;
import fql_lib.decl.FQLProgram;

@SuppressWarnings("serial")
public class FQLCodeEditor extends CodeEditor<FQLProgram, Environment, Display> {

	public FQLCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public String isPatrick() {
		return "false";
	}

	@Override
	protected String getATMFlhs() {
		return "text/fql";
	}

	@Override
	protected String getATMFrhs() {
		return "fql_lib.decl.FqlTokenMaker";
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
	protected Environment makeEnv(String program, FQLProgram init) {
		return Driver.makeEnv(program, init);
	}

	@Override
	protected FQLProgram parse(String program) throws ParserException {
		return FQLParser.program(program);
	}

	@Override
	protected Display makeDisplay(String foo, FQLProgram init, Environment env, long start, long middle) {
		return new Display(foo, init, env);
	}

	@Override
	protected String textFor(Environment env) {
		return "Done";
	}

}
