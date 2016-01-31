package fql_lib.X;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

import catdata.algs.Pair;
import fql_lib.core.CodeEditor;
import fql_lib.core.Language;
import fql_lib.core.Util;

@SuppressWarnings("serial")
public class XCodeEditor extends CodeEditor<XProgram, XEnvironment, XDisplay> {

	public XCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
	}

	@Override
	public Language isPatrick() {
		return Language.FPQL;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.FPQL.name();
	}

	@Override
	protected String getATMFrhs() {
		return "fql_lib.X.FpqlTokenMaker";
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
	
		   provider.addCompletion(new ShorthandCompletion(provider, "type",
	            "type \"\"", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "polynomial", "polynomial {\n\t q = {for v:t; where v=v; attributes l=v.l; edges f = {e=v.l} : q;} : t\n}\n : s -> s", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "query", "query {\n\tpi;\n\tdelta;\n\tsigma;\n} ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "flower", "flower {\n\tselect;\n\tfrom;\n\twhere;\n} ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "FLOWER", "FLOWER {\n\tselect;\n\tfrom;\n\twhere;\n} ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "schema", "schema {\n\tnodes;\n\tedges;\n\tequations;\n}", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "mapping", "mapping {\n\tnodes;\n\tedges;\n}\n :  -> ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "instance", "instance {\n\tvariables;\n\tequations;\n}\n : ", ""));
		
		provider.addCompletion(new ShorthandCompletion(provider, "INSTANCE", "INSTANCE {\n\tvariables;\n\tequations;\n}\n : ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "homomorphism", "homomorphism {\n\tvariables;\n}\n :  ->  ", ""));
		
		return provider;
		
	  }
	
	
	@Override
	protected XProgram parse(String program) throws ParserException {
		return XParser.program(program);
	}

	@Override
	protected XDisplay makeDisplay(String foo, XProgram init, XEnvironment env, long start, long middle) {
		return new XDisplay(foo, init, env, start, middle);
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
				@SuppressWarnings("unchecked")
				XMapping<String, String> m = (XMapping<String, String>) o.getValue();
				for (Entry<Pair<List<String>, List<String>>, String> k : m.unprovable.entrySet()) {
					if (!k.getValue().equals("true")) {
						ret += "\nWarning: in " + o.getKey() + ", could not prove " + Util.sep(k.getKey().first, ".") + " = " + Util.sep(k.getKey().second, ".");
					}
				}
			}
			ret += "\n";
		}
		
		//ret += "\n\n" + env.prog.typeReport(); 
		
		return ret.trim();
	}

}
