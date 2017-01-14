package catdata.ide;

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
import java.util.Collection;
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
import javax.swing.JMenuItem;
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

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.jparsec.error.ParserException;

import catdata.LineException;
import catdata.Prog;
import catdata.Util;


/**
 * 
 * @author ryan
 * 
 *         The FQL code editor
 */
public abstract class CodeEditor<Progg extends Prog, Env, DDisp extends Disp> extends JPanel implements Runnable {

	public void copyAsRtf() {
		topArea.copyAsRtf();
	}
	
	public abstract Language lang();
	
	final Integer id;
	protected final String title;
	
	private DDisp display; 

	private static final long serialVersionUID = 1L;

	public final RSyntaxTextArea topArea;

	protected final CodeTextPanel respArea = new CodeTextPanel(BorderFactory.createEtchedBorder(), "Response", "");

	private final JTextField searchField = new JTextField();
	private final JButton nextButton = new JButton("Find Next");
	private final JButton prevButton = new JButton("Find Previous");
	private final JCheckBox matchCaseCB = new JCheckBox("Match Case");
	private final JCheckBox replaceCB = new JCheckBox("Replace");
	private final JCheckBox wholeCB = new JCheckBox("Whole Word");
	private final JTextField replaceField = new JTextField();
	

	private JFrame frame;

	private JPanel makeSearchDialog() {
		JPanel toolBar = new JPanel(new GridLayout(3, 4));
		toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		toolBar.add(new JLabel("Search for:"));
		toolBar.add(searchField);
		nextButton.addActionListener((ActionEvent e) -> doFind(true));
		toolBar.add(nextButton);
		prevButton.addActionListener((ActionEvent e) -> doFind(false));
		toolBar.add(prevButton);
		searchField.addActionListener((ActionEvent e) -> nextButton.doClick(0));

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

	private void doFind(boolean b) {
		SearchContext context = new SearchContext();
		String text = searchField.getText();
		if (text.isEmpty()) {
			return;
		}
		context.setSearchFor(text);
		context.setMatchCase(matchCaseCB.isSelected());
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

	private void makeSearchVisible() {
		SwingUtilities.invokeLater(() -> {
                    if (frame == null) {
                        frame = new JFrame();
                        frame.setContentPane(makeSearchDialog());
                        frame.setTitle("Find and Replace");
                        
                        frame.pack();
                        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        frame.setLocationRelativeTo(null);
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent evt) {
                                SearchContext context = new SearchContext();
                                SearchEngine.markAll(topArea, context);
                            }
                        });
                        
                        ActionListener escListener = (ActionEvent e) -> frame.setVisible(false);
                        
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
                });
	} 
	
	protected abstract String getATMFlhs();
	protected abstract String getATMFrhs();
	protected abstract void doTemplates();
	
	protected CodeEditor(String title, Integer id, String content) {
		super(new GridLayout(1, 1));
		this.id = id;
		this.title = title;
		Util.assertNotNull(id);
		respArea.setWordWrap(true);
		
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory
				.getDefaultInstance();
	
		atmf.putMapping(getATMFlhs(), getATMFrhs());
		FoldParserManager.get().addFoldParserMapping(getATMFlhs(),
				new CurlyFoldParser());

		topArea = new RSyntaxTextArea();
		
		//this is kind of neat
		topArea.setMarkOccurrences(true);
		
		if (getATMFrhs() != null) {
			topArea.setSyntaxEditingStyle(getATMFlhs()); 
		}
		topArea.setText(content);
		topArea.setCaretPosition(0);
		topArea.setAutoscrolls(true);
		

		InputMap inputMap = topArea.getInputMap();

		KeyStroke key2;
        key2 = System.getProperty("os.name").contains("Windows") ? KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
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
		
		if (System.getProperty("os.name").contains("Windows")) { 
			key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK
					| Event.CTRL_MASK);
			inputMap.put(key, alx);
		} else {
			key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK
					| Event.META_MASK);
			inputMap.put(key, alx);
		}

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
     
		JSplitPane xx1 = new Split(.8, JSplitPane.VERTICAL_SPLIT);
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
				IdeOptions.theCurrentOptions.fontSizeUp();
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
				IdeOptions.theCurrentOptions.fontSizeDown();
			}
		};
		topArea.getActionMap().put("DecreaseFont", al);
		inputMap.put(key, "DecreaseFont");
		inputMap.put(key2, "DecreaseFont");
		
		doTemplates();
		
		JMenuItem rtf = new JMenuItem("Copy as RTF");
		rtf.addActionListener(x -> topArea.copyAsRtf());
		topArea.getPopupMenu().add(rtf, 0);

		JMenuItem foldall = new JMenuItem("Fold All");
		foldall.addActionListener(x -> foldAll(true));
		topArea.getPopupMenu().add(foldall, 0);
		
		JMenuItem unfoldall = new JMenuItem("UnFold All");
		unfoldall.addActionListener(x -> foldAll(false));
		topArea.getPopupMenu().add(unfoldall, 0);
		
		//TODO aql real DAWG?
		spc = new SpellChecker(z -> reservedWords());
		topArea.addParser(spc); 
		
		//topArea.setMasetMarginLineEnabled(true);
		
		
		topArea.setOpaque(true);
		respArea.setOpaque(true);
		IdeOptions.theCurrentOptions.apply(this);
		
		//System.out.println(topArea.getSyntaxScheme().getStyle(TokenTypes.COMMENT_EOL).foreground.getRGB());
		
//		RSyntaxTextArea.
	}
		
	private final SpellChecker spc;
	
	public void foldAll(boolean b) {
		int i = topArea.getFoldManager().getFoldCount();
		for (int j = 0; j < i; j++) {
			Fold fold = topArea.getFoldManager().getFold(j);
			fold.setCollapsed(b);
		}
	}	
	
	public void setFontSize(int size) {
		if (size < 1) {
			return;
		}
		Font font = new Font(topArea.getFont().getFontName(), topArea.getFont().getStyle(), size);
		topArea.setFont(font);

		Font font2 = new Font(respArea.area.getFont().getFontName(), respArea.area.getFont().getStyle(), size + 1);
		respArea.area.setFont(font2);
	}

	//TODO aql join reordering
		
	void findAction() {
		makeSearchVisible();
	}

	//TODO -> private
	protected void doExample(Example e) {
		if (abortBecauseDirty()) {
			return;
		}
		String program = e.getText();
		topArea.setText(program);
		topArea.setCaretPosition(0);
		respArea.setText("");
		GUI.setDirty(id, false);
		if (display != null) {
			display.close();
		}
		display = null;
	}

	public void abortAction() {
		interruptAndNullify();
		respArea.setText("Aborted");
	}
	
	protected final String[] toUpdate = new String[] { null };
	protected String toDisplay = null;
	private Thread thread, temp;

	public void runAction() {
		toDisplay = null;
		interruptAndNullify();
		thread = new Thread(this);
		temp = new Thread(() -> {
                    try {
                        respArea.setText("Begin\n");
                        int count = 0;
                        while (!Thread.currentThread().isInterrupted()) {
                            count++;
                            Thread.sleep(250);
                            if (toDisplay != null) {
                                respArea.setText(toDisplay);
                                return;
                            } else if (thread != null) {
                                synchronized (toUpdate) {
                                    if (toUpdate[0] != null) {
                                        if ((count % 8) == 0) {
                                            respArea.setText(toUpdate[0] + "\n");
                                        } else {
                                            respArea.setText(respArea.getText() + ".");
                                        }
                                    } else {
                                        if (respArea.getText().length() > 1024*16) {
                                            respArea.setText("");
                                        }
                                        respArea.setText(respArea.getText() + ".");
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException ie) {
                    } catch (Exception tt) {
                        tt.printStackTrace();
                        respArea.setText(tt.getMessage());
                    }
                });
		temp.setPriority(Thread.MIN_PRIORITY);
		temp.start();
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public void run() {
		String program = topArea.getText();

		Progg init;
		Env env; 
		init = tryParse(program);
		if (init == null) {
			return;
		}

		long start; 
		long middle; 
		
		try {
			start = System.currentTimeMillis();
			env = makeEnv(program, init);
			middle = System.currentTimeMillis();
			
			toDisplay = "Computation finished, creating viewer...";

			DateFormat format = DateFormat.getTimeInstance();
			String foo = title;
				foo += " - " + format.format(new Date(start));
			display = makeDisplay(foo, init, env, start, middle); 
			if (display.exn() == null) {
				toDisplay = textFor(env); //"Done";
				respArea.setText(textFor(env)); //"Done");
			} else {
				throw display.exn();
			}
		} catch (LineException e) {
			toDisplay = "Error in " + e.kind + " " + e.decl + ": "
					+ e.getLocalizedMessage();
			respArea.setText(toDisplay);
			e.printStackTrace();
			Integer theLine = init.getLine(e.decl);
			setCaretPos(theLine);
		} catch (Throwable re) {
                    toDisplay = "Error: " + re.getLocalizedMessage();
                    respArea.setText(toDisplay);
                    re.printStackTrace();
		} 
		interruptAndNullify();
	}

	private void interruptAndNullify() {
		if (thread != null) {
			thread.interrupt();
		} 	
		thread = null;
		if (temp != null) {
			temp.interrupt();
		}
		temp = null;
	}
	
	protected abstract String textFor(Env env);

	protected abstract DDisp makeDisplay(String foo, Progg init, Env env, long start, long middle);

	protected abstract Env makeEnv(String program, Progg init);

	protected abstract Progg parse(String program) throws ParserException;
	
	protected void setCaretPos(int p) {
		topArea.requestFocusInWindow();
		topArea.setCaretPosition(p);
	}
	
	private void moveTo(int col, int line) {
		topArea.requestFocusInWindow();
		topArea.setCaretPosition(topArea.getDocument()
				.getDefaultRootElement().getElement(line - 1)
				.getStartOffset()
				+ (col - 1));
	}
	
	protected Progg tryParse(String program) {
		try {
			return parse(program);
		} catch (ParserException e) {
			int col = e.getLocation().column;
			int line = e.getLocation().line;
			moveTo(col, line);
			
			//String s = e.getMessage();
			//String t = s.substring(s.indexOf(" "));
			//t.split("\\s+");

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
		} catch (NullPointerException npe) { //TODO aql weird
			npe.printStackTrace();
			return true;
		}
		int choice = JOptionPane.showOptionDialog(null, 
				"Unsaved changes - continue to close?", "Close?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Yes", "No"}, "No");
		return (choice != JOptionPane.YES_OPTION);
	}

	public void optionsHaveChanged() {
		IdeOptions.theCurrentOptions.apply(this);
		clearSpellCheck();
	}
	
	public void clearSpellCheck() {
		try {
			topArea.forceReparsing(spc);
		} catch (Throwable t) {
		//	t.printStackTrace(); 
		}
	}
	
	@SuppressWarnings("static-method")
	protected Collection<String> reservedWords() {
		return Util.list("pushout", "pushouts", "pullback", "pullbacks", "observable", "observables", "validator", "boolean", "booleans", "equational", "axiomatization", "axiomatize", "axiom", "axioms", "functor", "functors", "schema", "schemas", "runtime", "sql", "aql", "fql", "fpql", "opl", "java", "javascript", "colimit");
	}

	
}
