package catdata.aql.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import catdata.Program;
import catdata.Util;
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
		CodeEditor<Program<Exp<?>>, AqlEnv, AqlDisplay> {

	private final Outline outline;
	
	
        @Override
	public void abortAction() {
		super.abortAction();
		if (driver != null) {
			driver.abort();
		}
		
	}
	
	private final class Outline extends JFrame {
		
		private final JPanel p = new JPanel(new GridLayout(1,1));
		final JList<String> list = new JList<>();
		
		public void build() {
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (last_prog == null) {
				return;
			}
			Set<String> set = last_prog.exps.keySet();		
			Vector<String> listData = new Vector<>();
			for (String s : set) {
                            listData.add(s);
			}
			list.setListData(listData);
			revalidate();
		}
				
		private Outline(String title) {
			super(title);
			p.add(new JScrollPane(list));
			setContentPane(p);
			build();
			list.addListSelectionListener(e -> {
					String s = list.getSelectedValue(); 
					if (s == null) {
						return;
					}
					Integer line = last_prog.lines.get(s);
					if (line == null) {
						toDisplay = "Cannot fine line for " + s + " - try recompiling. ";
						line = 0;
					}
					setCaretPos(line);
							
			});
			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			 ComponentListener listener = new ComponentAdapter() {
			      @Override
				public void componentShown(ComponentEvent evt) {
			    	  build();
			      }
			 };
			addComponentListener(listener);
			setSize(new Dimension(200,300));
			setLocationRelativeTo(null);
		}
	
	}
	
	
	public AqlCodeEditor(String title, int id, String content) {
		super(title, id, content);
		SyntaxScheme scheme = topArea.getSyntaxScheme();
		scheme.getStyle(TokenTypes.RESERVED_WORD).foreground = Color.RED;
		scheme.getStyle(TokenTypes.RESERVED_WORD_2).foreground = Color.BLUE;
		
		JMenuItem im = new JMenuItem("Infer Mapping (using last compiled state)");
		im.addActionListener(x -> infer(Kind.MAPPING));
		topArea.getPopupMenu().add(im, 0);
		JMenuItem iq = new JMenuItem("Infer Query (using last compiled state)");
		iq.addActionListener(x -> infer(Kind.QUERY));
		topArea.getPopupMenu().add(iq, 0);
		JMenuItem it = new JMenuItem("Infer Transform (using last compiled state)");
		it.addActionListener(x -> infer(Kind.TRANSFORM));
		topArea.getPopupMenu().add(it, 0);
		JMenuItem ii = new JMenuItem("Infer Instance (using last compiled state)");
		ii.addActionListener(x -> infer(Kind.INSTANCE));
		topArea.getPopupMenu().add(ii, 0);
			
		outline = new Outline(title);
		
		JMenuItem o = new JMenuItem("Outline (using last compiled state)");
		o.addActionListener(x -> showOutline());
		topArea.getPopupMenu().add(o, 0);
	}

	public void showOutline() {
		outline.setVisible(true);
		outline.toFront();
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

	@Override
	protected void doTemplates() {
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				InputEvent.META_DOWN_MASK
						| InputEvent.SHIFT_DOWN_MASK);
		ac.setTriggerKey(key);
		ac.install(topArea);
	}

	private static CompletionProvider createCompletionProvider() {
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
	protected Program<Exp<?>> parse(String program) throws ParserException {
		return AqlParser.parseProgram(program);
	}

	@Override
	protected AqlDisplay makeDisplay(String foo, Program<Exp<?>> init,
		AqlEnv env, long start, long middle) {
		AqlDisplay ret = new AqlDisplay(foo, init, env, start, middle);
		if (env.exn != null) {
			GUI.topFrame.toFront();
		}
		return ret;
	}

//	private String last_str;
	private Program<Exp<?>> last_prog;
	public AqlEnv last_env;
	private AqlMultiDriver driver;
	
	@Override
	protected AqlEnv makeEnv(String str, Program<Exp<?>> init) {
		driver = new AqlMultiDriver(init, toUpdate, last_prog, last_env);
		driver.start();
		last_env = driver.env; //constructor blocks
		last_prog = init;
		//topArea.forceReparsing(parser);
		clearSpellCheck();
		outline.build();
		if (last_env.exn != null && last_env.defs.keySet().isEmpty()) {
			throw last_env.exn;
		}
		return last_env;
	}

	//TODO aql tokenizer error on multiline quotes (jdbc example)


	@Override
	protected String textFor(AqlEnv env) {
		return "Done.";
	}

	public void infer(Kind k) {
		try {
			Inferrer.infer(this, k);
		} catch (Throwable thr) {
			thr.printStackTrace();
			respArea.setText(thr.getMessage());
		}
	}

	@Override
	protected Collection<String> reservedWords() {
		Collection<String> ret = Util.union(super.reservedWords(), Util.union(Util.list(AqlParser.ops), Util.list(AqlParser.res)));
		if (last_prog != null) {
			ret = Util.union(ret, last_prog.exps.keySet());
		}
		return ret;
	}

}
