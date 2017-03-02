package catdata.ide;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import catdata.Pair;
import catdata.Unit;
import catdata.Util;
import catdata.aql.Kind;
import catdata.aql.gui.AqlCodeEditor;
import catdata.fpql.EnrichViewer;
import catdata.fpql.XEasikToFQL;
import catdata.fpql.XJsonToFQL;
import catdata.fpql.XNeo4jToFQL;
import catdata.fpql.XRaToFpql;
import catdata.fpql.XSqlToFql;
import catdata.fql.Chase;
import catdata.fql.RaToFql;
import catdata.fql.RingToFql;
import catdata.fql.SqlToFql;
import catdata.fql.gui.FqlCodeEditor;
import catdata.fqlpp.KBViewer;
import catdata.ide.IdeOptions.IdeOption;
import catdata.nested.NraViewer;
import catdata.opl.CfgToOpl;
import catdata.opl.OplWarehouse;
import catdata.opl.SqlChecker;
import catdata.opl.SqlToOpl;
import catdata.sql.SqlLoader;
import catdata.sql.SqlMapper;

@SuppressWarnings("serial")
/*

  @author ryan
 *
 *         Top level gui
 */
public class GUI extends JPanel {

	public static final JTabbedPane editors = new JTabbedPane();

	public static JFrame topFrame;

	private static CodeEditor<?, ?, ?> getSelectedEditor() {
		int i = editors.getSelectedIndex();
		if (i == -1) {
			return null;
		}
		return (CodeEditor<?, ?, ?>) editors.getComponentAt(i);
	}

	@SuppressWarnings({ "unchecked" })
	public static Pair<JPanel, MenuBar> makeGUI(JFrame frame) {
		topFrame = frame;

		JPanel pan = new JPanel();

		MenuBar menuBar = new MenuBar();

		Menu fileMenu = new Menu("File");

		MenuItem openItem = new MenuItem("Open");
		MenuItem saveItem = new MenuItem("Save");
		MenuItem saveAsItem = new MenuItem("Save As");
		MenuItem saveAllItem = new MenuItem("Save All");
		MenuItem closeItem = new MenuItem("Close");
		MenuItem exitItem = new MenuItem("Quit");

		Map<Language, MenuItem> newItems = new HashMap<>();
		for (Language l : Language.values0()) {
			MenuItem newItem = new MenuItem("New " + l);
			fileMenu.add(newItem);
			newItems.put(l, newItem);
			newItem.addActionListener((ActionEvent e) -> newAction(null, "", l));
		}

		fileMenu.add(openItem);
		// fileMenu.add(openItem2);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(saveAllItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);

		closeItem.addActionListener(e -> closeAction());
		saveAsItem.addActionListener(e -> saveAsActionAlternate(getSelectedEditor()));
	
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		MenuShortcut s = new MenuShortcut(ctrlS.getKeyCode());
		saveItem.setShortcut(s);

		//TODO aql why doesn't shift mask work? order?
		MenuShortcut sA = new MenuShortcut(ctrlS.getKeyCode(), true);
		saveAllItem.setShortcut(sA);
		saveAllItem.addActionListener(e -> saveAllAction());

		KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		MenuShortcut q = new MenuShortcut(ctrlQ.getKeyCode());
		exitItem.setShortcut(q);
		
	
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
//		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
		MenuShortcut c = new MenuShortcut(ctrlW.getKeyCode());
		closeItem.setShortcut(c);

	
		KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

		KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		MenuShortcut n = new MenuShortcut(ctrlN.getKeyCode());
		newItems.get(Language.getDefault()).setShortcut(n);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		MenuShortcut o = new MenuShortcut(ctrlO.getKeyCode());
		openItem.setShortcut(o);

		Menu toolsMenu = new Menu("Tools");
		//Menu transMenu = new Menu("Translate");

		Menu editMenu = new Menu("Edit");
		MenuItem findItem = new MenuItem("Find");
		editMenu.add(findItem);

		Menu aqlMenu = new Menu("AQL");
		Menu fqlMenu = new Menu("FQL");
		Menu fqlppMenu = new Menu("FQL++");
		Menu oplMenu = new Menu("OPL");
		Menu fpqlMenu = new Menu("FPQL");

		populateAql(aqlMenu);
		populateFql(fqlMenu);
		populateFqlpp(fqlppMenu);
		populateFpql(fpqlMenu);
		populateOpl(oplMenu);
		
		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		MenuShortcut f = new MenuShortcut(ctrlF.getKeyCode());
		findItem.setShortcut(f);

		MenuItem runItem = new MenuItem("Run");
		toolsMenu.add(runItem);
		runItem.addActionListener(e -> {
			CodeEditor<?, ?, ?> ed = getSelectedEditor();
			if (ed != null) {
				ed.runAction();
			}
		});
		MenuShortcut q2 = new MenuShortcut(ctrlR.getKeyCode());
		runItem.setShortcut(q2);

		MenuItem abortItem = new MenuItem("Abort");
		toolsMenu.add(abortItem);
		abortItem.addActionListener(e -> abortAction());
		
		MenuItem optionsItem = new MenuItem("Options");
		toolsMenu.add(optionsItem);
		optionsItem.addActionListener(e -> IdeOptions.showOptions());
		
		MenuItem optionsItem2 = new MenuItem("Legacy options");
		toolsMenu.add(optionsItem2);
		optionsItem2.addActionListener(e -> DefunctGlobalOptions.showOptions());
		
		MenuItem rtf = new MenuItem("Copy as RTF");
		editMenu.add(rtf);
		rtf.addActionListener(x -> {
			CodeEditor<?, ?, ?> ed = getSelectedEditor();
			if (ed != null) {
				ed.copyAsRtf();
			}
		});
	
		MenuItem fall = new MenuItem("Fold All");
		editMenu.add(fall);
		fall.addActionListener(x -> {
			CodeEditor<?, ?, ?> ed = getSelectedEditor();
			if (ed != null) {
				ed.foldAll(true);
			}
		});

		MenuItem uall = new MenuItem("Unfold All");
		editMenu.add(uall);
		uall.addActionListener(x -> {
			CodeEditor<?, ?, ?> ed = getSelectedEditor();
			if (ed != null) {
				ed.foldAll(false);
			}
		});

		MenuItem chaseItem = new MenuItem("ED Chaser");
		toolsMenu.add(chaseItem);
		chaseItem.addActionListener(x -> Chase.dostuff());

		MenuItem sqlLoaderItem = new MenuItem("SQL Loader");
		toolsMenu.add(sqlLoaderItem);
		sqlLoaderItem.addActionListener(x -> SqlLoader.showLoader());

		MenuItem sqlMapperItem = new MenuItem("SQL Mapper");
		toolsMenu.add(sqlMapperItem);
		sqlMapperItem.addActionListener(x -> SqlMapper.showGuesser());

		MenuItem sqlCheckItem = new MenuItem("SQL Checker");
		toolsMenu.add(sqlCheckItem);
		sqlCheckItem.addActionListener(x -> new SqlChecker());

		MenuItem shredItem = new MenuItem("NR Shredder");
		toolsMenu.add(shredItem);
		shredItem.addActionListener(x -> new NraViewer());

		MenuItem easikItem = new MenuItem("EASIK");
		toolsMenu.add(easikItem);
		easikItem.addActionListener(x -> easik.Easik.main(new String[0]));
		
		Menu helpMenu = new Menu("About");
		MenuItem aboutItem = new MenuItem("About");
		helpMenu.add(aboutItem);
		aboutItem.addActionListener(e -> IdeOptions.showAbout());

		openItem.addActionListener(e -> openActionAlternate());

		saveItem.addActionListener(e -> saveAction());

		exitItem.addActionListener(e -> exitAction());

		findItem.addActionListener(e -> {
			delay();
			CodeEditor<?, ?, ?> ed = getSelectedEditor();
			if (ed != null) {
				ed.findAction();
			}
		});

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(toolsMenu);
		menuBar.add(aqlMenu);
		menuBar.add(fqlMenu);
		menuBar.add(fqlppMenu);
		menuBar.add(fpqlMenu);
		menuBar.add(oplMenu);

		menuBar.add(helpMenu);

		pan.setLayout(new BorderLayout());

		JPanel toolBar = new JPanel(new GridLayout(1, 8));

		JButton compileB = new JButton("Run");
		compileB.addActionListener(e -> {
			CodeEditor<?, ?, ?> ed = (CodeEditor<?, ?, ?>) editors.getComponentAt(editors.getSelectedIndex());
			if (ed != null) {
				ed.runAction();
			}
		});

		JButton abortB = new JButton("Abort");
		abortB.addActionListener(e -> {
			CodeEditor<?, ?, ?> ed = (CodeEditor<?, ?, ?>) editors.getComponentAt(editors.getSelectedIndex());
			if (ed != null) {
				ed.abortAction();
			}
		});

		JButton new_button = new JButton("New " + Language.getDefault());
		new_button.addActionListener(e -> newAction(null, "", Language.getDefault()));

		JButton save_button = new JButton("Save");
		save_button.addActionListener(e -> saveAction());

		JButton open_button = new JButton("Open");
		open_button.addActionListener(e -> openActionAlternate());

		JButton optionsb = new JButton("Options");
		optionsb.addActionListener(e -> IdeOptions.showOptions());

		CardLayout cl = new CardLayout();
		JPanel boxPanel = new JPanel(cl);
		@SuppressWarnings("rawtypes")
		JComboBox allBox = new JComboBox<>(Examples.filterBy(Language.getDefault().toString()));
		allBox.setSelectedIndex(-1);
		allBox.addActionListener(x -> doExample((Example) allBox.getSelectedItem()));
		for (Language l : Language.values()) {
			@SuppressWarnings({ "rawtypes" })
			JComboBox box = new JComboBox(Examples.filterBy(l.toString()));
			box.setSelectedIndex(-1);
			box.addActionListener(x -> doExample((Example) box.getSelectedItem()));
			boxPanel.add(box, l.toString());
		}

		boxPanel.add(allBox, Language.getDefault().prefix());
		cl.show(boxPanel, Language.getDefault().prefix());

		Vector<String> vec = new Vector<>();
	
		for (Language l : Language.values0()) {
			vec.add(l.toString());
		}
		JComboBox<String> modeBox = new JComboBox<>(vec);
		modeBox.setSelectedItem(Language.getDefault().toString());
		modeBox.addActionListener(x -> cl.show(boxPanel, (String) modeBox.getSelectedItem()));

		toolBar.add(compileB);
		toolBar.add(abortB);
		toolBar.add(new_button);
		toolBar.add(open_button);
		toolBar.add(save_button);
		toolBar.add(optionsb);
		toolBar.add(new JLabel("Load Example:", SwingConstants.RIGHT));
		toolBar.add(modeBox);
		toolBar.add(boxPanel);

		pan.add(toolBar, BorderLayout.PAGE_START);
		pan.add(editors, BorderLayout.CENTER);
	
		return new Pair<>(pan, menuBar);
	}

	private static void infer(Kind k) {
		int i = editors.getSelectedIndex();
		Object o = editors.getComponentAt(i);
		if (o instanceof AqlCodeEditor) {
			AqlCodeEditor a = (AqlCodeEditor) o;
			a.infer(k);
		}
	}

	private static void populateAql(Menu menu) {
		MenuItem m = new MenuItem("Outline (using last state)");
		m.addActionListener(x -> {
			int i = editors.getSelectedIndex();
			if (i == -1) {
				return;
			}
			Object o = editors.getComponentAt(i);
			if (o instanceof AqlCodeEditor) {
				AqlCodeEditor a = (AqlCodeEditor) o;
				a.showOutline();
			}
		});
		menu.add(m);

		MenuItem im = new MenuItem("Infer Mapping (using last state)");
		im.addActionListener(x -> infer(Kind.MAPPING));
		menu.add(im);
		MenuItem iq = new MenuItem("Infer Query (using last state)");
		iq.addActionListener(x -> infer(Kind.QUERY));
		menu.add(iq);
		MenuItem it = new MenuItem("Infer Transform (using last state)");
		it.addActionListener(x -> infer(Kind.TRANSFORM));
		menu.add(it);
		MenuItem ii = new MenuItem("Infer Instance (using last state)");
		it.addActionListener(x -> infer(Kind.INSTANCE));
		menu.add(ii);

		MenuItem ih = new MenuItem("Emit HTML (using last compiled state)");
		ih.addActionListener(x -> {
			CodeEditor<?,?,?> c = getSelectedEditor();
			if (c == null) {
				return;
			}
			if (c instanceof AqlCodeEditor) {
				AqlCodeEditor a = (AqlCodeEditor) c;
				a.emitDoc();
			}
		});
		menu.add(ih);
	}

	private static void doExample(Example e) {
		// int i = untitled_count;
		if (e == null) {
			return;
		}
		newAction(e.toString(), e.getText(), e.lang());
	}

	private static void abortAction() {
		CodeEditor<?, ?, ?> c = getSelectedEditor();
		if (c != null) {
			c.abortAction();
		}
	}
	
	private static Unit deInc(CodeEditor<?,?,?> c) {
		dirty.remove(c.id);
		keys.remove(c.id);
		files.remove(c.id);
		titles.remove(c.id);
		editors.remove(c);
		return new Unit();
	}

	private static void closeAction() {
		delay();
		CodeEditor<?, ?, ?> c = getSelectedEditor();
		if (c == null || c.abortBecauseDirty()) {
			return;
		}
		deInc(c);		
	}

	public static void exitAction() {
		delay();
		int i = 0;
		for (Integer x : keys.keySet()) {
			if (dirty.get(x).equals(true)) {
				i++;
			}
		}
		if (i == 0) {
			System.exit(0);
		}
		int choice = JOptionPane.showOptionDialog(null, i + " documents have unsaved changes - exit?", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Yes", "No" }, "No");
		if (choice == JOptionPane.NO_OPTION) {
			return;
		}
		System.exit(0);
	}

	private static void saveAllAction() {
		int select = editors.getSelectedIndex();
		for (int index = 0; index < editors.getTabCount(); index++) {
			CodeEditor<?,?,?> e = (CodeEditor<?, ?, ?>) editors.getComponentAt(index);
			editors.setSelectedIndex(index);
			int id = e.id;
			File f = files.get(id);
			if (f != null) {
				String s = e.getText();
				doSave(f, s, id);
			} else {
				saveAsActionAlternate(e); 
			}
		}
		if (select >= 0 && select < editors.getTabCount()) {
			editors.setSelectedIndex(select);
		}
	}
	
	private static void saveAction() {
		CodeEditor<?, ?, ?> e = getSelectedEditor();
		if (e == null) {
			return;
		}
		File f = files.get(e.id);
		if (f == null) {
			saveAsActionAlternate(e);
		} else {
			delay();
			doSave(f, e.getText(), e.id);		
		}
	}

	private static void doSave(File f, String s, Integer id) {
		s = s.replace("\r", "");
		try (FileWriter fw = new FileWriter(f)) {
			fw.write(s);
			setDirty(id, false);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not save to " + f);
		}
	}

	public static class Filter extends FileFilter {

		final Language l;

		public Filter(Language l) {
			Util.assertNotNull(l);
			this.l = l;
		}

		@Override
		public boolean accept(File f) {
			return f.getName().endsWith("." + l.fileExtension()) || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return l + " files (*." + l.fileExtension() + ")";
		}
	}
/*
	private static class AllFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			for (Language l : Language.values()) {
				if (f.getName().endsWith("." + l.fileExtension())) {
					return true;
				}
			}

			return f.isDirectory();
		}

		@Override
		public String getDescription() {
			return "All CatData Files";
		}
	}
	*/
	public static class AllNameFilter implements FilenameFilter {
	
		@Override
		public boolean accept(File dir, String name) {
			for (Language l : Language.values()) {
				if (name.endsWith("." + l.fileExtension())) {
					return true;
				}
			}
			return false;
		}

		public static String getAllString() {
			List<String> l = new LinkedList<>();
			for (Language lang : Language.values()) {
				l.add("*." + lang.fileExtension());
			}
			return Util.sep(l, ";");
		}
	}

	// TODO aql file chooser does not bold the selectable files on mac see
	// http://stackoverflow.com/questions/15016176/jfilechooser-showsavedialog-all-files-greyed-out
	/*
	protected static void saveAsAction(CodeEditor<?, ?, ?> e) {
		delay();

		if (e == null) {
			return;
		}

		JFileChooser jfc = new JFileChooser(GlobalOptions.debug.general.file_path) {
			@Override
			public void approveSelection() {
				File selectedFile = getSelectedFile();
				if (selectedFile == null) {
					return;
				}
				
				if (selectedFile.exists() && new Filter(e.lang()).accept(getSelectedFile())) {
					int response = JOptionPane.showOptionDialog(this, "The file " + selectedFile.getName() + " already exists. Replace?", "Ovewrite file", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Yes", "No" }, "No");
					if (response == JOptionPane.NO_OPTION) {
						return;
					}
				}

				super.approveSelection();

			}
		};
		jfc.setAcceptAllFileFilterUsed(false);

		jfc.addChoosableFileFilter(new Filter(e.lang()));

		int j = jfc.showSaveDialog(null);
		if (j == JFileChooser.CANCEL_OPTION) {
			return;
		}
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		if (!jfc.getSelectedFile().getAbsolutePath().endsWith("." + e.lang().fileExtension())) {
			f = new File(jfc.getSelectedFile() + "." + e.lang().fileExtension());
		}

		files.put(e.id, f);
		titles.put(e.id, f.getName());
		doSave(f, e.getText(), e.id);

	}
*/
	private static void doOpen(File f, Language lang) {
		String s = Util.readFile(f.getAbsolutePath());
		if (s == null) {
			return;
		}
		s = s.replace("\r", "");
		Integer i = newAction(f.getName(), s, lang);
		files.put(i, f);
	}

	private static FileDialog openDialog;
	private static FileDialog getOpenDialog() {
		if (openDialog != null) {
			openDialog.setFile(AllNameFilter.getAllString());
			return openDialog;
		}
		openDialog = new FileDialog((Dialog)null, "Open", FileDialog.LOAD);
		openDialog.setFile(AllNameFilter.getAllString());
		openDialog.setFilenameFilter(new AllNameFilter());
		//if (!GlobalOptions.debug.general.file_path.isEmpty()) {
		openDialog.setDirectory(IdeOptions.theCurrentOptions.getFile(IdeOption.FILE_PATH).getAbsolutePath());
		//}
		openDialog.setMultipleMode(true);
		return openDialog;
	}
	
	private static FileDialog saveDialog;
	private static FileDialog getSaveDialog(Language lang) {
		if (saveDialog != null) {
			saveDialog.setFile("*." + lang.fileExtension());
			return saveDialog;
		}
		saveDialog = new FileDialog((Dialog)null, "Save", FileDialog.SAVE);
		saveDialog.setFile("*." + lang.fileExtension());
		//openDialog.setFilenameFilter(new AllNameFilter());
		//if (!GlobalOptions.debug.general.file_path.isEmpty()) {
			saveDialog.setDirectory(IdeOptions.theCurrentOptions.getFile(IdeOption.FILE_PATH).getAbsolutePath());
		//}
		saveDialog.setMultipleMode(false);
		return saveDialog;
	}
	
	public static void openAction(File... fs) {
		for (File f : fs) {
			for (Language l : Language.values()) {
				if (f.getAbsolutePath().endsWith("." + l.fileExtension())) {
					doOpen(f, l);
				}
			}
		}
	}
	private static void openActionAlternate() {
		//delay();
		FileDialog jfc = getOpenDialog();
		jfc.setVisible(true);
		openAction(jfc.getFiles());
	}
	
	private static void saveAsActionAlternate(CodeEditor<?, ?, ?> e) {
		//delay();

		if (e == null) {
			return;
		}

		FileDialog jfc = getSaveDialog(e.lang());
		
		jfc.setVisible(true);

		String f = jfc.getFile();
		if (f == null) {
			return;
		}
		String d = jfc.getDirectory();
		if (d == null) {
			throw new RuntimeException("Could not save file");
		}
		if (!f.endsWith("." + e.lang().fileExtension())) {
			f = f + "." + e.lang().fileExtension();
		}
		File file = new File(d, f);
		files.put(e.id, file);
		titles.put(e.id, file.getName());
		doSave(file, e.getText(), e.id);

	}

	/*
	private static void openAction() {
		delay();
		JFileChooser jfc = new JFileChooser(GlobalOptions.debug.general.file_path) {
			@Override
			public void approveSelection() {
				if (new AllFilter().accept(getSelectedFile())) {
					super.approveSelection();
				}

			}
		};
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(new AllFilter());
		jfc.setMultiSelectionEnabled(true);
		int j = jfc.showOpenDialog(null);
		if (j == JFileChooser.CANCEL_OPTION) {
			return;
		}
		for (File f : jfc.getSelectedFiles()) {
			if (f == null) {
				return;
			}
			for (Language l : Language.values()) {
				if (f.getAbsolutePath().endsWith("." + l.fileExtension())) {
					doOpen(f, l);
				}
			}
		}
	} */

	public static void setDirty(Integer i, boolean b) {
		dirty.put(i, b);
		CodeEditor<?,?,?> wanted = keys.get(i);
		String title = (b ? "*" : "  ") + titles.get(i);
		for (int tab = 0; tab < editors.getTabCount(); tab++) {
			CodeEditor<?,?,?> ed = (CodeEditor<?,?,?>) editors.getComponentAt(tab);
			if (Objects.equals(wanted, ed)) {
				editors.setTitleAt(tab, title);				
				editors.setTabComponentAt(tab, new ButtonTabComponent(editors, x -> GUI.deInc(x)));
			}
		}
	}

	public static Boolean getDirty(Integer i) {
		Boolean ret = dirty.get(i);
		if (ret == null) {
			throw new RuntimeException();
		}
		return ret;
	}

	public static void optionsHaveChanged() {
		for (CodeEditor<?, ?, ?> c : keys.values()) {
			c.optionsHaveChanged();
		}
		String prev = UIManager.getLookAndFeel().getClass().getName();
		String next = IdeOptions.theCurrentOptions.getString(IdeOption.LOOK_AND_FEEL);
	    if (!prev.equals(next)) {
            try {
                UIManager.setLookAndFeel(next);
                SwingUtilities.updateComponentTreeUI(GUI.topFrame);
             } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
	}

	private static final Map<Integer, Boolean> dirty = new HashMap<>();
	public static final Map<Integer, CodeEditor<?, ?, ?>> keys = new HashMap<>();
	private static final Map<Integer, File> files = new HashMap<>();
	private static final Map<Integer, String> titles = new HashMap<>();
	private static int untitled_count = 0;

	public static Integer newAction(String title, String content, Language lang) {
		untitled_count++;
		if (title == null) {
			title = "Untitled " + untitled_count + "." + lang.fileExtension();
		}
		CodeEditor<?, ?, ?> c = lang.createEditor(title, untitled_count, content);
	
		int i = editors.getTabCount();
		keys.put(untitled_count, c);
		setDirty(untitled_count, false);

		titles.put(c.id, title);
		editors.addTab("  " + title, c);
		editors.setTabComponentAt(i, new ButtonTabComponent(editors, x -> GUI.deInc(x)));
		editors.setSelectedIndex(i);
		
		c.topArea.setCaretPosition(0);
		c.topArea.requestFocusInWindow();
		
		return c.id;
	}

	private static void delay() {
/*		try {
			Thread.sleep(100); // hack for enough time to unhighlight menu
		} catch (InterruptedException e) {
			e.printStackTrace();
		} */
	}
 
	private static void formatAction() {
		CodeEditor<?, ?, ?> c = getSelectedEditor();
		if (c == null || !c.lang().equals(Language.FQL)) {
			return;
		}
		FqlCodeEditor cc = (FqlCodeEditor) c;
		cc.format();
	}

	private static void veditAction() {
		CodeEditor<?, ?, ?> c = getSelectedEditor();
		if (c == null || !c.lang().equals(Language.FQL)) {
			return;
		}
		FqlCodeEditor cc = (FqlCodeEditor) c;
		cc.format();
		cc.vedit();
	}

	private static void checkAction() {
		CodeEditor<?, ?, ?> c = getSelectedEditor();
		if (c == null || !c.lang().equals(Language.FQL)) {
			return;
		}
		FqlCodeEditor cc = (FqlCodeEditor) c;
		cc.format();
		cc.check();
	}
	
	private static void populateFql(Menu menu) {
		MenuItem formatItem = new MenuItem("FQL Code Format");
		menu.add(formatItem);
		formatItem.addActionListener(x -> formatAction());

		MenuItem visItem = new MenuItem("FQL Visual Edit");
		menu.add(visItem);
		visItem.addActionListener(x -> veditAction());
		
			MenuItem checkItem = new MenuItem("FQL Type Checker");
			menu.add(checkItem);
			checkItem.addActionListener(x -> checkAction());

		MenuItem raToFqlItem = new MenuItem("SPCU to FQL");
			menu.add(raToFqlItem);
			raToFqlItem.addActionListener(x -> new RaToFql());

			MenuItem sqlToFqlItem = new MenuItem("SQL Schema to FQL");
			menu.add(sqlToFqlItem);
			sqlToFqlItem.addActionListener(x -> new SqlToFql());

			MenuItem ringToFqlItem = new MenuItem("Polynomials to FQL");
			menu.add(ringToFqlItem);
			ringToFqlItem.addActionListener(x -> new RingToFql());


	}

	private static void  populateFqlpp(Menu menu) {

		MenuItem kbItem = new MenuItem("FQL++ Knuth-Bendix");
			menu.add(kbItem);
			kbItem.addActionListener(x -> new KBViewer());

	}

	private static void populateFpql(Menu menu) {

		MenuItem enrichItem = new MenuItem("FPQL Enrich");
			menu.add(enrichItem);
			enrichItem.addActionListener(x -> new EnrichViewer());


		MenuItem raItem = new MenuItem("RA to FPQL");
			menu.add(raItem);
			raItem.addActionListener(x -> new XRaToFpql());

			MenuItem sqlItem = new MenuItem("SQL to FPQL");
			menu.add(sqlItem);
			sqlItem.addActionListener(x -> new XSqlToFql());

			MenuItem jsonItem = new MenuItem("JSON to FPQL");
			menu.add(jsonItem);
			jsonItem.addActionListener(x -> new XJsonToFQL());

			MenuItem neo4j = new MenuItem("Neo4j to FPQL");
			menu.add(neo4j);
			neo4j.addActionListener(x -> new XNeo4jToFQL());

			MenuItem easik = new MenuItem("EASIK to FPQL");
			menu.add(easik);
			easik.addActionListener(x -> new XEasikToFQL());

	}

	private static void populateOpl(Menu menu) {

		MenuItem cfgItem = new MenuItem("CFG to OPL");
			menu.add(cfgItem);
			cfgItem.addActionListener(x -> new CfgToOpl());


		MenuItem sqlToOplItem = new MenuItem("SQL to OPL");
			menu.add(sqlToOplItem);
			sqlToOplItem.addActionListener(x -> SqlToOpl.showPanel());

		MenuItem wizardItem = new MenuItem("Warehouse Wizard");
			menu.add(wizardItem);

			wizardItem.addActionListener(x -> new Wizard<>(new OplWarehouse(), y -> {
                Example ex = new Example() {

                    @Override
                    public Language lang() {
                        return Language.OPL;
                    }

                    @Override
                    public String getName() {
                        return "Wizard";
                    }

                    @Override
                    public String getText() {
                        return y.toString();
                    }

                };
                doExample(ex);
            }).startWizard());
	}
	

}
