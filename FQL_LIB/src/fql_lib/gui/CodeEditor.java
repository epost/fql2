package fql_lib.gui;

import java.awt.Event;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import fql_lib.DEBUG;
import fql_lib.decl.FQLProg;
import fql_lib.decl.LineException;
//import fql.parse.PrettyPrinter;
import fql_lib.examples.Example;

/**
 * 
 * @author ryan
 * 
 *         The FQL code editor
 */
public abstract class CodeEditor<Prog extends FQLProg, Env, Disp extends FQLDisp> extends JPanel implements Runnable {

	public abstract boolean isPatrick();
	
	final Integer id;

	Disp display;

	private static final long serialVersionUID = 1L;

	public RSyntaxTextArea topArea;

	FQLTextPanel respArea = new FQLTextPanel(BorderFactory.createEtchedBorder(), "Compiler response", "");

	final JTextField searchField = new JTextField();
	final JButton nextButton = new JButton("Find Next");
	final JButton prevButton = new JButton("Find Previous");
	final JCheckBox matchCaseCB = new JCheckBox("Match Case");
	final JCheckBox replaceCB = new JCheckBox("Replace");
	final JCheckBox wholeCB = new JCheckBox("Whole Word");
	final JTextField replaceField = new JTextField();

	JFrame frame;

	public JPanel makeSearchDialog() {
		JPanel toolBar = new JPanel(new GridLayout(3, 4));
		toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		toolBar.add(new JLabel("Search for:"));
		toolBar.add(searchField);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFind(true);
			}
		});
		toolBar.add(nextButton);
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFind(false);

			}
		});
		toolBar.add(prevButton);
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});

		toolBar.add(new JLabel("Replace with:"));
		toolBar.add(replaceField);
		toolBar.add(replaceCB);
		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
		toolBar.add(wholeCB);
		toolBar.add(matchCaseCB);

		return toolBar;
	}

	protected void doFind(boolean b) {
		// Create an object defining our search parameters.
		SearchContext context = new SearchContext();
		String text = searchField.getText();
		if (text.length() == 0) {
			return;
		}
		context.setSearchFor(text);
		context.setMatchCase(matchCaseCB.isSelected());
		// context.setRegularExpression(regexCB.isSelected());
		context.setSearchForward(b);
		context.setWholeWord(wholeCB.isSelected());

		if (replaceCB.isSelected()) {
			context.setReplaceWith(replaceField.getText());
			SearchEngine.replace(topArea, context);
		} else {
			SearchEngine.find(topArea, context);
		}
	}

	public void setText(String s) {
		topArea.setText(s);
		topArea.setCaretPosition(0);
	}

	public String getText() {
		return topArea.getText();
	}

	public void makeSearchVisible() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame == null) {
					frame = new JFrame();
					frame.setContentPane(makeSearchDialog());
					frame.setTitle("Find and Replace");

					frame.pack();
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					frame.setLocationRelativeTo(null);
					frame.addWindowListener(new WindowAdapter() {
			            public void windowClosing(WindowEvent evt) {
							SearchContext context = new SearchContext();
							SearchEngine.markAll(topArea, context);
				        }
			        });

					ActionListener escListener = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							frame.setVisible(false);
						}
					};

					frame.getRootPane().registerKeyboardAction(escListener,
							KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
							JComponent.WHEN_IN_FOCUSED_WINDOW);
					KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
							InputEvent.CTRL_MASK);
					KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
							InputEvent.META_MASK);
					frame.getRootPane().registerKeyboardAction(escListener,
							ctrlW, JComponent.WHEN_IN_FOCUSED_WINDOW);
					frame.getRootPane().registerKeyboardAction(escListener,
							commandW, JComponent.WHEN_IN_FOCUSED_WINDOW);
				}
				frame.setVisible(true);
			}
		});
	}
	
	protected abstract String getATMFlhs();
	protected abstract String getATMFrhs();
	protected abstract void doTemplates();
	
	protected CodeEditor(Integer id, String content) {
		super(new GridLayout(1, 1));

		this.id = id;
		respArea.setWordWrap(true);
		
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory
				.getDefaultInstance();
		/*
		atmf.putMapping("text/fql", "fql_lib.decl.FqlTokenMaker");
		FoldParserManager.get().addFoldParserMapping("text/fql",
				new CurlyFoldParser());
				*/

		atmf.putMapping(getATMFlhs(), getATMFrhs());
		FoldParserManager.get().addFoldParserMapping(getATMFlhs(),
				new CurlyFoldParser());

		// topArea.setAntiAliasingEnabled(true);
		RSyntaxTextArea.setTemplatesEnabled(true);

		/*
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
		*/
		doTemplates();

		topArea = new RSyntaxTextArea();
//		topArea.setSyntaxEditingStyle("text/fql"); 
		if (getATMFrhs() != null) {
			topArea.setSyntaxEditingStyle(getATMFlhs()); 
		}
		topArea.setText(content);
		topArea.setCaretPosition(0);
		topArea.setAutoscrolls(true);
		
		Font font = new Font(topArea.getFont().getFontName(), topArea.getFont().getStyle(), DEBUG.debug.FONT_SIZE);
		topArea.setFont(font);

		InputMap inputMap = topArea.getInputMap();

		KeyStroke key2 = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);

		inputMap.put(key2, DefaultEditorKit.beginLineAction);

		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.META_MASK);
		key2 = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);

		inputMap.put(key, DefaultEditorKit.endLineAction);
		inputMap.put(key2, DefaultEditorKit.endLineAction);

		Action alx = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				topArea.redoLastAction();
			}
		};
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK
				| Event.CTRL_MASK);
		inputMap.put(key, alx);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK
				| Event.META_MASK);
		inputMap.put(key, alx);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.META_MASK);
		key2 = KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK);

		Action al = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int len = topArea.getLineEndOffsetOfCurrentLine();
				int offs = topArea.getCaretPosition();
				try {
					if (len - offs - 1 > 0) {
						topArea.getDocument().remove(offs, len - offs - 1);
					}
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		};
		topArea.getActionMap().put("RemoveToEndOfLine", al);
		inputMap.put(key, "RemoveToEndOfLine");
		inputMap.put(key2, "RemoveToEndOfLine");

		topArea.setCloseCurlyBraces(true);
		topArea.setCodeFoldingEnabled(true);
		RTextScrollPane sp = new RTextScrollPane(topArea);
		sp.setFoldIndicatorEnabled(true);

		JSplitPane xx1 = new FQLSplit(.8, JSplitPane.VERTICAL_SPLIT);
		xx1.add(sp);
		xx1.setDividerSize(6);
		xx1.setResizeWeight(.8);
		xx1.add(respArea);
		add(xx1);

		topArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				GUI.setDirty(CodeEditor.this.id, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				GUI.setDirty(CodeEditor.this.id, true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				GUI.setDirty(CodeEditor.this.id, true);
			}
		});

		key = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.META_MASK);
		key2 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.CTRL_MASK);

		al = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				DEBUG.debug.FONT_SIZE++;
				GUI.setFontSize(DEBUG.debug.FONT_SIZE);
			}
		};
		topArea.getActionMap().put("IncreaseFont", al);
		inputMap.put(key, "IncreaseFont");
		inputMap.put(key2, "IncreaseFont");
		
		key = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.META_MASK);
		key2 = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.CTRL_MASK);

		al = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (topArea.getFont().getSize() == 1) {
					return;
				}
				DEBUG.debug.FONT_SIZE--;
				GUI.setFontSize(DEBUG.debug.FONT_SIZE);
			}
		};
		topArea.getActionMap().put("DecreaseFont", al);
		inputMap.put(key, "DecreaseFont");
		inputMap.put(key2, "DecreaseFont");
	}
	
	public void setFontSize(int size) {
		if (size < 1) {
			return;
		}
		Font font = new Font(topArea.getFont().getFontName(), topArea.getFont().getStyle(), size);
		topArea.setFont(font);
	}

	protected void findAction() {
		makeSearchVisible();
	}

	protected void doExample(Example e) {
		if (abortBecauseDirty()) {
			return;
		}
		String program = e.getText();
		topArea.setText(program);
		topArea.setCaretPosition(0);
		respArea.setText("");
		GUI.setDirty(CodeEditor.this.id, false);
		if (display != null) {
			display.close();
		}
		display = null;
	}

	@SuppressWarnings("deprecation")
	void abortAction() {
		if (thread != null && thread.isAlive()) {
			thread.stop();
			thread = null;
		}
		if (temp != null && temp.isAlive()) {
			temp.stop();
			temp = null;
		}
		try {
			toDisplay = "Aborted";
			respArea.setText("Aborted");
		} catch (Exception e) {
			respArea.setText(e.getLocalizedMessage());
		}
	}

	String toDisplay = null;
	Thread thread, temp;

	@SuppressWarnings("deprecation")
	public void runAction() {
		toDisplay = null;
		if (temp != null) {
			temp.stop();
		}
		temp = null;
		if (thread != null) {
			thread.stop();
		}
		thread = null;
		thread = new Thread(this);
		temp = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					respArea.setText("");
					for (;;) {
						Thread.sleep(250);
						if (toDisplay != null) {
							respArea.setText(toDisplay);
							//toDisplay = null;
							return;
						} else if (thread != null) {
							respArea.setText(respArea.getText() + ".");
						}
					}
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				} catch (Throwable tt) {
					tt.printStackTrace();
					respArea.setText(tt.getMessage());
				}
			}
		});
		temp.setPriority(Thread.MIN_PRIORITY);
		temp.start();
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@SuppressWarnings("deprecation")
	public void run() {
		String program = topArea.getText();

		Prog init;
		Env env; 
		init = tryParse(program);
		if (init == null) {
			return;
		}

		try {
			env = makeEnv(program, init);
		} catch (LineException e) {
			toDisplay = "Error in " + e.kind + " " + e.decl + ": "
					+ e.getLocalizedMessage();
			e.printStackTrace();
			topArea.requestFocusInWindow();
			Integer theLine = init.getLine(e.decl);
			topArea.setCaretPosition(theLine);
			return;
		} catch (Throwable re) {
			toDisplay = "Error: " + re.getLocalizedMessage();
			re.printStackTrace();
			return;
		}

		try {
			toDisplay = "Computation finished, creating viewer...";

			if (display != null && !DEBUG.debug.MultiView) {
				display.close();
			}
			DateFormat format = DateFormat.getTimeInstance();
			String foo = GUI.getTitle(id);
			if (DEBUG.debug.MultiView) {
				foo += " - "
						+ format.format(new Date(System.currentTimeMillis()));
			}			
			display = makeDisplay(foo, init, env); 
			toDisplay = textFor(env); //"Done";
			respArea.setText(textFor(env)); //"Done");

		} catch (Throwable ee) {
			toDisplay = ee.toString();
			ee.printStackTrace();
			if (thread != null) {
				thread.stop();
			}
			thread = null;
			if (temp != null) {
				temp.stop();
			}
			temp = null;
			return;
		}

		if (thread != null) {
			thread.stop();
		}
		thread = null;
		if (temp != null) {
			temp.stop();
		}
		temp = null;

	}
	
	protected abstract String textFor(Env env);

	protected abstract Disp makeDisplay(String foo, Prog init, Env env);

	protected abstract Env makeEnv(String program, Prog init);

	protected abstract Prog parse(String program) throws ParserException;
	
//	@SuppressWarnings("deprecation")
	private Prog tryParse(String program) {
		try {
			return parse(program);
//			return FQLParser.program(program);
		} catch (ParserException e) {
			int col = e.getLocation().column;
			int line = e.getLocation().line;
			topArea.requestFocusInWindow();
			topArea.setCaretPosition(topArea.getDocument()
					.getDefaultRootElement().getElement(line - 1)
					.getStartOffset()
					+ (col - 1));
			String s = e.getMessage();
			String t = s.substring(s.indexOf(" "));
			t.split("\\s+");

			toDisplay = "Syntax error: " + e.getLocalizedMessage();
			e.printStackTrace();
			return null;
		} catch (Throwable e) {
			toDisplay = "Error: " + e.getLocalizedMessage();
			e.printStackTrace();
			return null;
		}
	}

	public boolean abortBecauseDirty() {
		try {
			if (!GUI.getDirty(id)) {
				return false;
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			return true;
		}

		int choice = JOptionPane.showConfirmDialog(null,
				"Unsaved Changes - Continue?", "Continue?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (choice == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}

}
