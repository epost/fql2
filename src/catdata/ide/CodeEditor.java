package catdata.ide;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;

import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;
import org.jparsec.error.ParserException;

import catdata.LineException;
import catdata.Prog;
import catdata.Unit;
import catdata.Util;

/**
 * 
 * @author ryan
 * 
 *         The FQL code editor
 */
public abstract class CodeEditor<Progg extends Prog, Env, DDisp extends Disp> extends JPanel
		implements SearchListener, Runnable {

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

	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;

	private void initSearchDialogs() {
		findDialog = new FindDialog((Dialog) null, (SearchListener) this);
		replaceDialog = new ReplaceDialog((Dialog) null, (SearchListener) this);

		// This ties the properties of the two dialogs together (match case,
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);

		tweak(findDialog); // indDialog.get
		tweak(replaceDialog);
	}

	private void tweak(JDialog frame) {
		ActionListener escListener = (ActionEvent e) -> frame.setVisible(false);

		frame.getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
		frame.getRootPane().registerKeyboardAction(escListener, ctrlW, JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.getRootPane().registerKeyboardAction(escListener, commandW, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	// private JFrame frame;

	public void setText(String s) {
		topArea.setText(s);
		setCaretPos(0);
	}

	public String getText() {
		return topArea.getText();
	}
	
	protected abstract String getATMFlhs();

	protected abstract String getATMFrhs();

	protected abstract void doTemplates();

	protected final RTextScrollPane sp;

	protected synchronized Outline<Progg, Env, DDisp> getOutline() {
		if (outline == null) {
			outline = new ListOutline<Progg, Env, DDisp>(this);
		}
		return outline;
	}

	volatile List<Integer> history = Collections.synchronizedList(new LinkedList<>());
	int position = 0;
	
	private void hist() {
		if (position < 0) {
			position = 0;
		} else if (position >= history.size()) {
			position = history.size() - 1;
		}
		setCaretPos(history.get(position));
	}
	public void backAction() {
		position++;
		hist();
	}
	
	public void fwdAction() {
		position--;
		hist();
	}
	
	protected CodeEditor(String title, Integer id, String content) {
		super(new GridLayout(1, 1));
		this.id = id;
		this.title = title;
		Util.assertNotNull(id);
		history.add(0);
		last_keystroke = System.currentTimeMillis();
		// respArea.setWordWrap(true);
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();

		atmf.putMapping(getATMFlhs(), getATMFrhs());
		FoldParserManager.get().addFoldParserMapping(getATMFlhs(), new CurlyFoldParser());

		topArea = new RSyntaxTextArea();
		topArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int i = topArea.getCaretPosition();
				history.add(0, i); 
				if (history.size() > 128) {
					history = Collections.synchronizedList(history.subList(0, 32));
				}
			}
		});
		
		topArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				last_keystroke = System.currentTimeMillis();
			}
		});
		
		topArea.setMarkOccurrences(true);

		if (getATMFrhs() != null) {
			topArea.setSyntaxEditingStyle(getATMFlhs());
		}
		topArea.setText(content);
		setCaretPos(0);
		topArea.setAutoscrolls(true);

		InputMap inputMap = topArea.getInputMap();

		topArea.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (KeyEvent.VK_DOWN == e.getKeyCode() && isCaretOnLastLine()) {
					setCaretPos(Integer.max(0, topArea.getText().length()));
				} else if (KeyEvent.VK_UP == e.getKeyCode() && isCaretOnFirstLine()) {
					setCaretPos(0);
				}
			}

			private boolean isCaretOnFirstLine() {
				return topArea.getCaretLineNumber() == 0;
			}

			private boolean isCaretOnLastLine() {
				return topArea.getCaretLineNumber() == Integer.max(0, topArea.getLineCount() - 1);
			}

		});

		KeyStroke key2;
		key2 = System.getProperty("os.name").contains("Windows")
				? KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.META_MASK)
				: KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
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
			key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK | Event.CTRL_MASK);
			inputMap.put(key, alx);
		} else {
			key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK | Event.META_MASK);
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
		sp = new RTextScrollPane(topArea);
		sp.setFoldIndicatorEnabled(true);

		sp.setBorder(BorderFactory.createEtchedBorder());
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

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
		
		
		// TODO aql real DAWG?
		spc = new SpellChecker(z -> reservedWords());
		topArea.addParser(spc);

		respArea.setMinimumSize(new Dimension(0, 0));
		respArea.setPreferredSize(new Dimension(600,200));
		topArea.setOpaque(true);
		respArea.setOpaque(true);
		IdeOptions.theCurrentOptions.apply(this);

		initSearchDialogs();
		
		situate();
	}

	private void situate() {
		if (outline_elongated) {
			situateElongated();
		} else {
			situateNotElongated();
		}
	}
	
	private void situateElongated() {
		JComponent outline = getOutline().p;
		
		JSplitPane xx1 = new Split(.8, JSplitPane.VERTICAL_SPLIT);
		xx1.setDividerSize(6);
		xx1.setResizeWeight(.8);
		xx1.add(sp);
		xx1.add(respArea);
		xx1.setBorder(BorderFactory.createEmptyBorder());
		JComponent newtop = xx1;

		if (enable_outline) {
			JSplitPane xx2 = new Split(.8, JSplitPane.HORIZONTAL_SPLIT);
			xx2.setDividerSize(6);

			if (outline_on_left) {
				xx2.setResizeWeight(.2);
				xx2.add(outline);
				xx2.add(xx1);
			} else {
				xx2.setResizeWeight(.8);
				xx2.add(xx1);
				xx2.add(outline);
			}
			xx2.setBorder(BorderFactory.createEmptyBorder());
			newtop = xx2;
		}

		

		this.removeAll();
		add(newtop);
		//revalidate();
	}
	
	private void situateNotElongated() {
		JComponent outline = getOutline().p;
		
		JComponent newtop = sp;

		if (enable_outline) {
			JSplitPane xx2 = new Split(.8, JSplitPane.HORIZONTAL_SPLIT);
			xx2.setDividerSize(6);

			if (outline_on_left) {
				xx2.setResizeWeight(.2);
				xx2.add(outline);
				xx2.add(sp);
			} else {
				xx2.setResizeWeight(.8);
				xx2.add(sp);
				xx2.add(outline);
			}
			xx2.setBorder(BorderFactory.createEmptyBorder());
			newtop = xx2;
		}

		JSplitPane xx1 = new Split(.8, JSplitPane.VERTICAL_SPLIT);
		xx1.setDividerSize(6);
		xx1.setResizeWeight(.8);
		xx1.add(newtop);
		xx1.add(respArea);
		xx1.setBorder(BorderFactory.createEmptyBorder());
		
		respArea.setMinimumSize(new Dimension(0, 0));

		this.removeAll();
		add(xx1);
		revalidate();
	}

	private final SpellChecker spc;

	public void foldAll(boolean b) {
		int i = topArea.getFoldManager().getFoldCount();
		for (int j = 0; j < i; j++) {
			Fold fold = topArea.getFoldManager().getFold(j);
			fold.setCollapsed(b);
		}
		setCaretPos(topArea.getCaretPosition());
		topArea.revalidate();
		sp.revalidate();
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

	// TODO aql join reordering

	void gotoLine() {
		if (findDialog.isVisible()) {
			findDialog.setVisible(false);
		}
		if (replaceDialog.isVisible()) {
			replaceDialog.setVisible(false);
		}
		GoToDialog dialog = new GoToDialog((Frame) null);
		dialog.setMaxLineNumberAllowed(topArea.getLineCount());
		dialog.setVisible(true);
		int line = dialog.getLineNumber();
		if (line > 0) {
			try {
				setCaretPos(topArea.getLineStartOffset(line - 1));
			} catch (BadLocationException ble) { // Never happens
				UIManager.getLookAndFeel().provideErrorFeedback(topArea);
				ble.printStackTrace();
			}
		}
	}

	void findAction() {
		if (replaceDialog.isVisible()) {
			replaceDialog.setVisible(false);
		}
		findDialog.setVisible(true);
	}

	void replaceAction() {
		if (findDialog.isVisible()) {
			findDialog.setVisible(false);
		}
		replaceDialog.setVisible(true);
	}

	// TODO -> private
	protected void doExample(Example e) {
		if (abortBecauseDirty()) {
			return;
		}
		String program = e.getText();
		topArea.setText(program);
		setCaretPos(0);
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
								if (respArea.getText().length() > 1024 * 16) {
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
	// TODO aql put this on another thread besides the event dispatch thread
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
				toDisplay = textFor(env); // "Done";
				respArea.setText(textFor(env)); // "Done");
			} else {
				throw display.exn();
			}
		} catch (LineException e) {
			toDisplay = "Error in " + e.kind + " " + e.decl + ": " + e.getLocalizedMessage();
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
	
	

	public void setCaretPos(int p) {
		try {
			SwingUtilities.invokeLater(() -> {
				topArea.requestFocusInWindow();
				topArea.setCaretPosition(p);
				Util.centerLineInScrollPane(topArea);
			});
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private void moveTo(int col, int line) {
		topArea.requestFocusInWindow();
		setCaretPos(
				topArea.getDocument().getDefaultRootElement().getElement(line - 1).getStartOffset() + (col - 1));
	}

	protected Progg tryParse(String program) {
		try {
			return parse(program);
		} catch (ParserException e) {
			int col = e.getLocation().column;
			int line = e.getLocation().line;
			moveTo(col, line);

			// String s = e.getMessage();
			// String t = s.substring(s.indexOf(" "));
			// t.split("\\s+");

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
		} catch (NullPointerException npe) { // TODO aql weird
			npe.printStackTrace();
			return true;
		}
		int choice = JOptionPane.showOptionDialog(null, "Unsaved changes - continue to close?", "Close?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Yes", "No" }, "No");
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
			// t.printStackTrace();
		}
	}

	@SuppressWarnings("static-method")
	protected Collection<String> reservedWords() {
		return Collections.emptySet();
	}

	@Override
	public String getSelectedText() {
		return topArea.getSelectedText();
	}

	@Override
	public void searchEvent(SearchEvent e) {

		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result = null;

		switch (type) {
		default: // Prevent FindBugs warning later
		case MARK_ALL:
			result = SearchEngine.markAll(topArea, context);
			break;
		case FIND:
			result = SearchEngine.find(topArea, context);
			if (!result.wasFound()) {
				UIManager.getLookAndFeel().provideErrorFeedback(topArea);
			}
			break;
		case REPLACE:
			result = SearchEngine.replace(topArea, context);
			if (!result.wasFound()) {
				UIManager.getLookAndFeel().provideErrorFeedback(topArea);
			}
			break;
		case REPLACE_ALL:
			result = SearchEngine.replaceAll(topArea, context);
			JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
			break;
		}

	

	}

	protected Outline<Progg, Env, DDisp> outline;

	public volatile boolean isClosed = false;

	public Progg parsed_prog;
	protected String parsed_prog_string;
	protected Unit parsed_prog_lock;

	public void close() {
			isClosed = true;
	} 

	@SuppressWarnings("unused")
	protected boolean omit(String s, Progg p) {
		return false;
	}

	//TODO aql pull these from options rather than cache her?
	private volatile Boolean enable_outline = false;
	 public volatile Boolean outline_alphabetical = true;
	private volatile Boolean outline_on_left = true;
	 public volatile Boolean outline_prefix_kind = true;
	private volatile Boolean outline_elongated = true;
	protected volatile long sleepDelay = 2;
	 public volatile Boolean outline_types = true;
	 public volatile Long last_keystroke = null;
	
	public void set_delay(int i) {
		if (i < 1) {
			i = 1;
		}
		sleepDelay = (long) i * 1000;
	}
	
	public void outline_types(Boolean bool) {
		outline_types = bool;
		//getOutline().build();
	}
	
	public void enable_outline(Boolean bool) {
		enable_outline = bool;
		situate();
	}

	public void outline_alphabetical(Boolean bool) {
		outline_alphabetical = bool;
		//getOutline().build();
	}

	public void outline_on_left(Boolean bool) {
		outline_on_left = bool;
		situate();
	}

	public void outline_prefix_kind(Boolean bool) {
		outline_prefix_kind = bool;
		//getOutline().build();
	}

	public void outline_elongated(Boolean bool) {
		outline_elongated = bool;
		situate();
	}

	
	
}
