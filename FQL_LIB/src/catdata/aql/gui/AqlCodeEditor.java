package catdata.aql.gui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

import catdata.Pair;
import catdata.Program;
import catdata.Util;
import catdata.aql.Instance;
import catdata.aql.Schema;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.AqlMultiDriver;
import catdata.aql.exp.AqlParser;
import catdata.aql.exp.Exp;
import catdata.aql.exp.Kind;
import catdata.ide.CodeEditor;
import catdata.ide.GUI;
import catdata.ide.Language;

@SuppressWarnings("serial")
public final class AqlCodeEditor extends
		CodeEditor<Program<Exp<? extends Object>>, AqlEnv, AqlDisplay> {

	public AqlCodeEditor(int untitled_count, String content) {
		super(untitled_count, content);
		SyntaxScheme scheme = topArea.getSyntaxScheme();
		scheme.getStyle(Token.RESERVED_WORD).foreground = Color.RED;
		scheme.getStyle(Token.RESERVED_WORD_2).foreground = Color.BLUE;
		
		JMenuItem im = new JMenuItem("Infer Mapping (using last compiled state)");
		im.addActionListener(x -> infer(Kind.MAPPING));
		topArea.getPopupMenu().add(im, 0);
		JMenuItem iq = new JMenuItem("Infer Query (using last compiled state)");
		iq.addActionListener(x -> infer(Kind.QUERY));
		topArea.getPopupMenu().add(iq, 0);
		JMenuItem it = new JMenuItem("Infer Transform (using last compiled state)");
		it.addActionListener(x -> infer(Kind.TRANSFORM));
		topArea.getPopupMenu().add(it, 0);
	}
	
	@SuppressWarnings("unchecked")
	public void infer(Kind k) { 
		if (last_env == null) {
			respArea.setText("Must compile before using inference.");
			return;
		}
		try {
			String in = topArea.getSelectedText();
			Pair<String, String> s = AqlParser.parseInfer(in);
			String a = s.first;
			String b = s.second;
			String repl = in + " {\n";
			if (k.equals(Kind.MAPPING)) {
				repl += inferMapping(last_env.defs.schs.map.get(a), last_env.defs.schs.map.get(b));
			} else if (k.equals(Kind.QUERY)) {
				repl += inferQuery(last_env.defs.schs.map.get(a), last_env.defs.schs.map.get(b));
			} else if (k.equals(Kind.TRANSFORM)) {
				repl += inferTransform(last_env.defs.insts.map.get(a), last_env.defs.insts.map.get(b));
			} 
			repl += "\n}";
			topArea.replaceSelection(repl);
		} catch (Throwable e) {
			e.printStackTrace();
			respArea.setText("Error in inference: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	private <X> String pr(Collection<X>... cs) {
		Collection<X> ret = new HashSet<>();
		for (Collection<X> col : cs) {
			ret.addAll(col);
		}
		return Util.sep(Util.alphabetical(ret), " ");
	}

	@SuppressWarnings("unchecked")
	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0, Gen, Sk, Gen0, Sk0> String inferTransform(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ?, ?> a, Instance<Ty0, En0, Sym0, Fk0, Att0, Gen0, Sk0, ?, ?> b) {
		if (a == null || b == null) {
			throw new RuntimeException("Compiled instances(s) not found - try compiling before using inference.");
		}
		if (!a.schema().equals(b.schema())) { //TODO aql schema equality
			throw new RuntimeException("Source instance is on schema " + a.schema() + "\n\nand target instance is on schema " + b.schema());
		}
			
		String sks = Util.sep(a.gens().keySet().stream().
				map(z -> "\t\t" + z + " -> some type side symbols [" + pr(b.schema().typeSide.syms.keySet()) 
				+ "] applied to Attributes [" + pr(b.schema().atts.keySet()) 
				+ "] applied to paths of foreign keys [" + pr(b.schema().fks.keySet()) 
				+ "] applied to Generators [" + pr(b.gens().keySet()) 
				+ "] and labelled nulls [" + pr(b.sks().keySet()) + "]").collect(Collectors.toList()), "\n");
				
		String gens = Util.sep(Util.alphabetical(a.gens().keySet()).stream().map(z -> "\t\t" + z + " -> a generator [" 
				+ pr(a.gens().keySet()) + "] ." + " a path of foreign keys [" + pr(b.schema().fks.keySet()) 
						+ "] ending at " + a.gens().get(z)).collect(Collectors.toList()), "\n");
						
		return "\tgenerators\n" + gens + "\n" + sks;

	}

	@SuppressWarnings("unchecked")
	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0> String inferMapping(Schema<Ty, En, Sym, Fk, Att> a, Schema<Ty0, En0, Sym0, Fk0, Att0> b) {
		if (a == null || b == null) {
			throw new RuntimeException("Compiled schema(s) not found - try compiling before using inference.");
		}
		String ens = "\tentities\n" + Util.sep(Util.alphabetical(a.ens).stream().map(z -> "\t\t" + z 
				+ " -> an entity [" + pr(b.ens) + "]").collect(Collectors.toList()), "\n");
		String fks = "\tforeign_keys\n" + Util.sep(Util.alphabetical(a.fks.keySet()).stream().map(z -> "\t\t" + z 
				+ " -> a path of foreign keys [" + pr(b.fks.keySet()) + "]").collect(Collectors.toList()), "\n");
		String atts = "\tattributes\n" + Util.sep(Util.alphabetical(a.atts.keySet()).stream().map(z -> "\t\t" + z 
				+ " -> lambda v. some type side symbols [" + pr(b.typeSide.syms.keySet()) + "] applied to Attributes [" + pr(b.atts.keySet()) + "] " + "applied to paths of foreign keys [" + pr(b.fks.keySet()) + "] ending on variable [v]").collect(Collectors.toList()), "\n");
		return ens + "\n" + fks + "\n" + atts;
	}

	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0> String inferQuery(Schema<Ty, En, Sym, Fk, Att> a, Schema<Ty0, En0, Sym0, Fk0, Att0> b) {
		if (a == null || b == null) {
			throw new RuntimeException("Compiled schema(s) not found - try compiling before using inference.");
		}
		String ens = "\tentities\n" + Util.sep(Util.alphabetical(b.ens).stream().map(z -> "\t\t" + z 
				+ " -> " + inferBlock(z, a, b)).collect(Collectors.toList()), "\n");
		String fks = "\tforeign_keys\n" + Util.sep(Util.alphabetical(b.fks.keySet()).stream().map(z -> "\t\t" + z 
				+ " -> " + inferTrans(z, a, b)).collect(Collectors.toList()), "\n");
		return ens + "\n\n" + fks;

	}
	
	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0> List<String> varsColon(En0 en, Schema<Ty, En, Sym, Fk, Att> a, Schema<Ty0, En0, Sym0, Fk0, Att0> b) {
		return a.ens.stream().map(x -> "v_" + en + "_" + x + ":" + x).collect(Collectors.toList());
	}
	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0> List<String> vars(En0 en, Schema<Ty, En, Sym, Fk, Att> a, Schema<Ty0, En0, Sym0, Fk0, Att0> b) {
		return a.ens.stream().map(x -> "v_" + en + "_" + x).collect(Collectors.toList());
	}
	
	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0> String inferTrans(Fk0 fk, Schema<Ty, En, Sym, Fk, Att> a, Schema<Ty0, En0, Sym0, Fk0, Att0> b) {
		List<String> dom = vars(b.fks.get(fk).second, a, b);
		List<String> cod = varsColon(b.fks.get(fk).first, a, b);
		
		String gens = Util.sep(Util.alphabetical(dom).stream().map(z -> z + " -> a generator [" 
				+ pr(cod) + "] ." + " a path of foreign keys [" + pr(a.fks.keySet()) 
						+ "] ending at " + b.fks.get(fk).first).collect(Collectors.toList()), "\n\t\t\t");
		return "{" + gens + "}";
	
	}
		
	
	private <Ty, En, Sym, Fk, Att,Ty0, En0, Sym0, Fk0, Att0> String inferBlock(En0 en, Schema<Ty, En, Sym, Fk, Att> a, Schema<Ty0, En0, Sym0, Fk0, Att0> b) {
		String s = "some type side symbols [" + pr(b.typeSide.syms.keySet()) + "] applied to Attributes [" + pr(b.atts.keySet()) + "] applied to paths of foreign keys [" + pr(a.fks.keySet()) + "] ending on variables [" + pr(vars(en, a, b)) + "]";
		
		List<String> as = Util.alphabetical(b.attsFrom(en)).stream().map(x -> x + " -> " + s).collect(Collectors.toList());
		
		return "{from " + pr(varsColon(en, a, b)) + "\n\t\t\twhere equality of terms using " + s + "\n\t\t\treturn " + Util.sep(as, "\n\t\t\t\t") + "}";
	}


	@Override
	public Language lang() {
		return Language.AQL;
	}

	@Override
	protected String getATMFlhs() {
		return "text/" + Language.AQL.name();
	}

	@Override
	protected String getATMFrhs() {
		return "catdata.aql.gui.AqlTokenMaker"; 
	}

	protected void doTemplates() {
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				java.awt.event.InputEvent.META_DOWN_MASK
						| java.awt.event.InputEvent.SHIFT_DOWN_MASK);
		ac.setTriggerKey(key);
		ac.install(this.topArea);
	}

	private CompletionProvider createCompletionProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();

		provider.addCompletion(new ShorthandCompletion(provider, "typeside",
				"typeside ? = literal {\n\timports\n\ttypes\n\tsconstants\n\tfunctions\n\tequations\n\tjava_types\n\tjava_constants\n\tjava_functions\n\toptions\n} ", ""));

		provider.addCompletion(new ShorthandCompletion(
				provider,
				"schema",
				"schema ? = literal : ? {\n\timports\n\tforeign_keys\n\tpath_equations\n\tattributes\n\tobservation_equations\n\toptions\n} ",
				""));
		
		provider.addCompletion(new ShorthandCompletion(
				provider,
				"instance",
				"instance ? = literal : ? {\n\timports\n\tgenerators\n\tequations\n\toptions\n} ",
				"")); 

		provider.addCompletion(new ShorthandCompletion(
				provider,
				"graph",
				"graph ? = literal : ? {\n\timports\n\tnodes\n\tedges\n} ",
				""));

		provider.addCompletion(new ShorthandCompletion(provider, "mapping",
				"mapping ? = literal : ? -> ? {\n\timports\n\tentities\n\tforeign_keys\n\tattributes\n\toptions\n} ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "transform",
				"transform ? = literal : ? -> ? {\n\timports\n\tgenerators\n\toptions\n} ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "query",
				"query ? = literal : ? -> ? {\n"
				+ "\n entities"
				+ "\n  e -> {for x:X y:Y "
				+ "\n        where f(x)=f(x) g(y)=f(y) "
				+ "\n        return att -> at(a) att2 -> at(a) "
				+ "\n        options"
				+ "\n  }"
				+ "\n"
				+ "\n foreign_keys"
				+ "\n  f -> {x -> a.g y -> f(y) "
				+ "\n        options"
				+ "\n  }"
				+ "\n options"
				+ "\n}", ""));
	
		provider.addCompletion(new ShorthandCompletion(provider, "import_csv",
				"import_csv path : schema (resp. inst -> inst) {imports options} ", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "export_csv",
				"export_csv_instance (resp. export_csv_transform) inst (resp. trans) path {options} ", ""));
			
		provider.addCompletion(new ShorthandCompletion(provider, "import_csv",
				"import_jdbc classname url prefix : schema (resp. inst -> inst) {\nen -> sql ty -> sql (resp + att -> sql fk -> sql) ...}", ""));

		provider.addCompletion(new ShorthandCompletion(provider, "export_csv",
				"export_jdbc_instance (resp export_jdbc_transform) classname url prefix {options} ", ""));
	
		return provider;

	}

	@Override
	protected Program<Exp<? extends Object>> parse(String program) throws ParserException {
		return AqlParser.parseProgram(program);
	}

	@Override
	protected AqlDisplay makeDisplay(String foo, Program<Exp<? extends Object>> init,
			AqlEnv env, long start, long middle) {
			AqlDisplay ret = new AqlDisplay(foo, init, env, start, middle);
			if (env.exn != null) {
				GUI.topFrame.toFront();
			}
			return ret;
	}

	String last_str;
	Program<Exp<? extends Object>> last_prog;
	AqlEnv last_env;

	@Override
	protected AqlEnv makeEnv(String str, Program<Exp<? extends Object>> init) {
		last_env = new AqlMultiDriver(init, toUpdate, last_prog, last_env).env; //constructor blocks
		last_prog = init;
		last_str = str;
		if (last_env.exn != null && last_env.defs.keySet().isEmpty()) {
			throw last_env.exn;
		}
		return last_env;
	}



	@Override
	protected String textFor(AqlEnv env) {
		return "Done.";
	}

}
