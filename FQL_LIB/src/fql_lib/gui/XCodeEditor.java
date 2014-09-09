package fql_lib.gui;

import org.codehaus.jparsec.error.ParserException;

import fql_lib.X.XDriver;
import fql_lib.X.XEnvironment;
import fql_lib.X.XParser;
import fql_lib.X.XProgram;
import fql_lib.X.XViewer;

@SuppressWarnings("serial")
public class XCodeEditor extends CodeEditor<XProgram, XEnvironment, XViewer> {

	public XCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public boolean isPatrick() {
		return true;
	}

	@Override
	protected String getATMFlhs() {
		return "text/patrick";
	}

	@Override
	protected String getATMFrhs() {
		return null;
	}

	@Override
	protected void doTemplates() {
	}

	
	
	@Override
	protected XProgram parse(String program) throws ParserException {
		return XParser.program(program);
	}

	@Override
	protected XViewer makeDisplay(String foo, XProgram init, XEnvironment env) {
		return new XViewer(foo, init, env);
	}

	@Override
	protected XEnvironment makeEnv(String str, XProgram init) {
		return XDriver.makeEnv(str, init);
	}

}
