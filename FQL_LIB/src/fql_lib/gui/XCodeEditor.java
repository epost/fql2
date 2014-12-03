package fql_lib.gui;

import java.util.List;
import java.util.Map.Entry;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;

import fql_lib.Pair;
import fql_lib.Util;
import fql_lib.X.XDriver;
import fql_lib.X.XEnvironment;
import fql_lib.X.XMapping;
import fql_lib.X.XObject;
import fql_lib.X.XParser;
import fql_lib.X.XProgram;
import fql_lib.X.XDisplay;

@SuppressWarnings("serial")
public class XCodeEditor extends CodeEditor<XProgram, XEnvironment, XDisplay> {

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
		return "fql_lib.X.FpqlTokenMaker";
	}

	@Override
	protected void doTemplates() {
		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		CodeTemplate ct = new StaticCodeTemplate("type", "type ",
				" \"\"");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("query", "query ",
				"{\n\tpi;\n\tdelta;\n\tsigma;\n} ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("flower", "flower ",
				"{\n\tselect;\n\tfrom;\n\twhere;\n} ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("FLOWER", "FLOWER ",
				"{\n\tselect;\n\tfrom;\n\twhere;\n} ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("schema", "schema ",
				"{\n\tnodes;\n\tedges;\n\tequations;\n}");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("mapping", "mapping ",
				"{\n\tnodes;\n\tedges;\n} :  -> ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("instance", "instance ",
				"{\n\tvariables;\n\tequations;\n} : ");
		ctm.addTemplate(ct);
		
		ct = new StaticCodeTemplate("INSTANCE", "INSTANCE ",
				"{\n\tvariables;\n\tequations;\n} : ");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("homomorphism", "homomorphism ",
				"{\n\tvariables;\n} :  ->  "); 
		ctm.addTemplate(ct);
	}

	
	
	@Override
	protected XProgram parse(String program) throws ParserException {
		return XParser.program(program);
	}

	@Override
	protected XDisplay makeDisplay(String foo, XProgram init, XEnvironment env) {
		return new XDisplay(foo, init, env);
	}

	@Override
	protected XEnvironment makeEnv(String str, XProgram init) {
		return XDriver.makeEnv(str, init);
	}

	@Override
	protected String textFor(XEnvironment env) {
		String ret = "";
		for (Entry<String, XObject> o : env.objs.entrySet()) {
			if (o.getValue() instanceof XMapping) {
				XMapping<String, String> m = (XMapping<String, String>) o.getValue();
				for (Entry<Pair<List<String>, List<String>>, String> k : m.unprovable.entrySet()) {
					if (!k.getValue().equals("true")) {
						ret += "\nWarning: in " + o.getKey() + ", could not prove " + Util.sep(k.getKey().first, ".") + " = " + Util.sep(k.getKey().second, ".");
					}
				}
			}
			ret += "\n";
		}
		return ret.trim();
	}

}
