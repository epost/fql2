package catdata.ide;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import catdata.Pair;
import catdata.aql.exp.Kind;
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
import catdata.nested.NraViewer;
import catdata.opl.CfgToOpl;
import catdata.opl.OplWarehouse;
import catdata.opl.SqlChecker;
import catdata.opl.SqlToOpl;
import catdata.sql.SqlLoader;
import catdata.sql.SqlMapper;


@SuppressWarnings("serial")
/**
 * 
 * @author ryan
 *
 * Top level gui
 */
public class GUI extends JPanel {

	public static JTabbedPane editors = new JTabbedPane();

	public static JFrame topFrame;

	@SuppressWarnings("unchecked")
	public static Pair<JPanel, MenuBar> makeGUI(JFrame frame) {
		topFrame = frame;

		JPanel pan = new JPanel();

		MenuBar menuBar = new MenuBar();

		// MenuItem m = new MenuItem()

		Menu fileMenu = new Menu("File");

		MenuItem openItem = new MenuItem("Open");
		// MenuItem openItem2 = new MenuItem("Open GUI");
		MenuItem saveItem = new MenuItem("Save");
		MenuItem saveAsItem = new MenuItem("Save As");
		MenuItem closeItem = new MenuItem("Close");
		MenuItem exitItem = new MenuItem("Exit");

		Map<Language, MenuItem> newItems = new HashMap<>();
		for (Language l : Language.values()) {
			MenuItem newItem = new MenuItem("New " + l.toString());
			fileMenu.add(newItem);
			newItems.put(l, newItem);
			newItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newAction(null, "", l);
				}
			});
		}

		fileMenu.add(openItem);
		// fileMenu.add(openItem2);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);

		closeItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeAction();
			}

		});

		saveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAsAction();
			}
		});

		// respArea.setWrapStyleWord();

		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
		MenuShortcut s = new MenuShortcut(ctrlS.getKeyCode());
		saveItem.setShortcut(s);

		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
		MenuShortcut c = new MenuShortcut(ctrlW.getKeyCode());
		closeItem.setShortcut(c);

		// Get the appropriate input map using the JComponent constants.
		// This one works well when the component is a container.
		// InputMap inputMap =
		// editors.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		InputMap inputMap = editors.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		// Add the key binding for the keystroke to the action name
		inputMap.put(ctrlW, "closeTab");
		inputMap.put(commandW, "closeTab");

		KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK);
		KeyStroke commandR = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_MASK);
		AbstractAction runAction = new AbstractAction() {
			@SuppressWarnings("rawtypes")
			@Override
			public void actionPerformed(ActionEvent e) {
				((CodeEditor) editors.getComponentAt(editors.getSelectedIndex())).runAction();
			}
		};

		inputMap.put(ctrlR, "run");
		inputMap.put(commandR, "run");
		editors.getActionMap().put("run", runAction);

		// Now add a single binding for the action name to the anonymous action
		AbstractAction closeTabAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeAction();
			}
		};

		editors.getActionMap().put("closeTab", closeTabAction);

		KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
		MenuShortcut n = new MenuShortcut(ctrlN.getKeyCode());
		newItems.get(Language.getDefault()).setShortcut(n);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK);
		MenuShortcut o = new MenuShortcut(ctrlO.getKeyCode());
		openItem.setShortcut(o);

		Menu toolsMenu = new Menu("Tools");
		Menu transMenu = new Menu("Translate");

		final Menu editMenu = new Menu("Edit");
		MenuItem findItem = new MenuItem("Find");
		editMenu.add(findItem);
		
		Menu aqlMenu = new Menu("AQL");
		populateAql(aqlMenu);
		

		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK);
		MenuShortcut f = new MenuShortcut(ctrlF.getKeyCode());
		findItem.setShortcut(f);

		KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK);
		MenuShortcut q = new MenuShortcut(ctrlQ.getKeyCode());
		exitItem.setShortcut(q);

		MenuItem runItem = new MenuItem("Run");
		toolsMenu.add(runItem);
		runItem.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void actionPerformed(ActionEvent e) {
				((CodeEditor) editors.getComponentAt(editors.getSelectedIndex())).runAction();
			}
		});
		MenuShortcut q2 = new MenuShortcut(ctrlR.getKeyCode());
		runItem.setShortcut(q2);

		MenuItem abortItem = new MenuItem("Abort");
		toolsMenu.add(abortItem);
		abortItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abortAction();
			}
		});

		MenuItem formatItem = new MenuItem("FQL Code Format");
		editMenu.add(formatItem);
		formatItem.addActionListener(x -> {
			formatAction();
		});

		MenuItem visItem = new MenuItem("FQL Visual Edit");
		editMenu.add(visItem);
		visItem.addActionListener(x -> {
			veditAction();
		});
		
		MenuItem rtf = new MenuItem("Copy as RTF");
		editMenu.add(rtf);
		rtf.addActionListener(x -> {
			int i = editors.getSelectedIndex();
			CodeEditor<?,?,?> ed = (CodeEditor<?,?,?>) editors.getComponentAt(i);
			if (ed == null) {
				return;
			}
			ed.copyAsRtf();
		});

		MenuItem chaseItem = new MenuItem("ED Chaser");
		toolsMenu.add(chaseItem);
		chaseItem.addActionListener(x -> {
			Chase.dostuff();
		});

		MenuItem checkItem = new MenuItem("FQL Type Checker");
		toolsMenu.add(checkItem);
		checkItem.addActionListener(x -> {
			checkAction();
		});
		
		MenuItem sqlLoaderItem = new MenuItem("SQL Loader");
		toolsMenu.add(sqlLoaderItem);
		sqlLoaderItem.addActionListener(x -> {
			SqlLoader.showLoader();
		});
		
		MenuItem sqlMapperItem = new MenuItem("SQL Mapper");
		toolsMenu.add(sqlMapperItem);
		sqlMapperItem.addActionListener(x -> {
			SqlMapper.showGuesser();
		});
		
		MenuItem sqlToOplItem = new MenuItem("SQL to OPL");
		transMenu.add(sqlToOplItem);
		sqlToOplItem.addActionListener(x -> {
			SqlToOpl.showPanel();
		});
		
		MenuItem sqlCheckItem = new MenuItem("SQL Checker");
		toolsMenu.add(sqlCheckItem);
		sqlCheckItem.addActionListener(x -> {
			new SqlChecker();
		}); 
		
		MenuItem wizardItem = new MenuItem("Warehouse Wizard");
		toolsMenu.add(wizardItem);
		
		wizardItem.addActionListener(x -> {
			new Wizard<>(new OplWarehouse(), y -> { 
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
			}).startWizard(); }); 
	

		MenuItem raToFqlItem = new MenuItem("SPCU to FQL");
		transMenu.add(raToFqlItem);
		raToFqlItem.addActionListener(x -> {
			new RaToFql();
		});

		MenuItem sqlToFqlItem = new MenuItem("SQL Schema to FQL");
		transMenu.add(sqlToFqlItem);
		sqlToFqlItem.addActionListener(x -> {
			new SqlToFql();
		});

		MenuItem ringToFqlItem = new MenuItem("Polynomials to FQL");
		transMenu.add(ringToFqlItem);
		ringToFqlItem.addActionListener(x -> {
			new RingToFql();
		});

		MenuItem shredItem = new MenuItem("NR Shredder");
		toolsMenu.add(shredItem);
		shredItem.addActionListener(x -> {
			new NraViewer();
		});

		MenuItem kbItem = new MenuItem("FQL++ Knuth-Bendix");
		toolsMenu.add(kbItem);
		kbItem.addActionListener(x -> {
			new KBViewer();
		});

		MenuItem enrichItem = new MenuItem("FPQL Enrich");
		toolsMenu.add(enrichItem);
		enrichItem.addActionListener(x -> {
			new EnrichViewer();
		});

		MenuItem raItem = new MenuItem("RA to FPQL");
		transMenu.add(raItem);
		raItem.addActionListener(x -> {
			new XRaToFpql();
		});

		MenuItem sqlItem = new MenuItem("SQL to FPQL");
		transMenu.add(sqlItem);
		sqlItem.addActionListener(x -> {
			new XSqlToFql();
		});

		MenuItem jsonItem = new MenuItem("JSON to FPQL");
		transMenu.add(jsonItem);
		jsonItem.addActionListener(x -> {
			new XJsonToFQL();
		});

		MenuItem neo4j = new MenuItem("Neo4j to FPQL");
		transMenu.add(neo4j);
		neo4j.addActionListener(x -> {
			new XNeo4jToFQL();
		});

		MenuItem easik = new MenuItem("EASIK to FPQL");
		transMenu.add(easik);
		easik.addActionListener(x -> {
			new XEasikToFQL();
		});

		MenuItem cfgItem = new MenuItem("CFG to OPL");
		transMenu.add(cfgItem);
		cfgItem.addActionListener(x -> {
			new CfgToOpl();
		});

		Menu helpMenu = new Menu("About");
		MenuItem aboutItem = new MenuItem("About");
		helpMenu.add(aboutItem);
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlobalOptions.showAbout();
			}
		});

		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitAction();
			}
		});

		findItem.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent e) {
				delay();
				((CodeEditor) editors.getComponentAt(editors.getSelectedIndex())).findAction();

			}
		});


		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(toolsMenu);
		menuBar.add(transMenu);
		menuBar.add(aqlMenu);

		menuBar.add(helpMenu);

	
		pan.setLayout(new BorderLayout());

		JPanel toolBar = new JPanel(new GridLayout(1, 7));
	
		JButton compileB = new JButton("Run");
		compileB.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void actionPerformed(ActionEvent e) {
				((CodeEditor) editors.getComponentAt(editors.getSelectedIndex())).runAction();
			}
		});

		JButton new_button = new JButton("New " + Language.getDefault());
		new_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newAction(null, "", Language.getDefault());
			}
		});

		JButton save_button = new JButton("Save");
		save_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

		JButton open_button = new JButton("Open");
		open_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});

		JButton optionsb = new JButton("Options");
		optionsb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlobalOptions.showOptions();
			}
		});

		CardLayout cl = new CardLayout();
		JPanel boxPanel = new JPanel(cl);
		@SuppressWarnings("rawtypes")
		JComboBox allBox = new JComboBox<>(Examples.getAllExamples());
		allBox.setSelectedIndex(-1);
		allBox.addActionListener(x -> doExample((Example) allBox.getSelectedItem()));
		for (Language l : Language.values()) {
			@SuppressWarnings({  "rawtypes" })
			JComboBox box = new JComboBox(Examples.filterBy(l.toString()));
			box.setSelectedIndex(-1);
			box.addActionListener(x -> doExample((Example) box.getSelectedItem()));
			boxPanel.add(box, l.toString());
		}

		boxPanel.add(allBox, "All");
		cl.show(boxPanel, "All");

		Vector<String> vec = new Vector<>();
		vec.add("All");
		for (Language l : Language.values()) {
			vec.add(l.toString());
		}
		JComboBox<String> modeBox = new JComboBox<>(vec);
		modeBox.addActionListener(x -> {
			cl.show(boxPanel, (String) modeBox.getSelectedItem());
		});

		toolBar.add(compileB);
		toolBar.add(new_button);
		toolBar.add(open_button);
		toolBar.add(save_button);
		toolBar.add(optionsb);
		toolBar.add(new JLabel("Load Example:", JLabel.RIGHT));
		toolBar.add(modeBox);
		toolBar.add(boxPanel);

		pan.add(toolBar, BorderLayout.PAGE_START);
		pan.add(editors, BorderLayout.CENTER);

		newAction(null, "", Language.getDefault());

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
		MenuItem im = new MenuItem("Infer Mapping (using last compiled state)");
		im.addActionListener(x -> infer(Kind.MAPPING));
		menu.add(im);
		MenuItem iq = new MenuItem("Infer Query (using last compiled state)");
		iq.addActionListener(x -> infer(Kind.QUERY));
		menu.add(iq);
		MenuItem it = new MenuItem("Infer Transform (using last compiled state)");
		it.addActionListener(x -> infer(Kind.TRANSFORM));
		menu.add(it);
	}

	protected static void doExample(Example e) {
		// int i = untitled_count;
		if (e == null) {
			return;
		}
		newAction(e.toString(), e.getText(), e.lang());
	}



	private static void abortAction() {
		int i = editors.getSelectedIndex();
		@SuppressWarnings("rawtypes")
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		c.abortAction();
	}

	@SuppressWarnings("rawtypes")
	private static void closeAction() {
		delay();
		int i = editors.getSelectedIndex();
		if (i < 1 || i >= editors.getTabCount()) {
			return;
		}
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		if (c.abortBecauseDirty()) {
			return;
		}
		editors.remove(i);
		dirty.remove(c.id);
		keys.remove(c.id);
		files.remove(c.id);
		titles.remove(c.id);
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
		int choice = JOptionPane.showOptionDialog(null, i
				+ " documents have unsaved changes - exit?", "Exit?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Yes", "No"}, "No");
		if (choice == JOptionPane.NO_OPTION) {
			return;
		}
		System.exit(0);
	}

	protected static void saveAction() {
		@SuppressWarnings("rawtypes")
		CodeEditor e = (CodeEditor) editors.getComponentAt(editors.getSelectedIndex());
		File f = files.get(e.id);
		if (f == null) {
			saveAsAction();
		} else {
			delay();
			doSave(f, e.getText());
			setDirty(e.id, false);
		}
	}

	static void doSave(File f, String s) {
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(s);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not save to " + f);
		}
	}

	public static class Filter extends FileFilter {

		Language l;

		public Filter(Language l) {
			this.l = l;
		}

		@Override
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith("." + l.fileExtension()) || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return l + " files (*." + l.fileExtension() + ")";
		}
	}

	public static class AllFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			for (Language l : Language.values()) {
				if (f.getName().toLowerCase().endsWith("." + l.fileExtension())) {
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

	protected static void saveAsAction() {
		delay();
		JFileChooser jfc = new JFileChooser(GlobalOptions.debug.general.file_path);
		@SuppressWarnings("rawtypes")
		CodeEditor e = (CodeEditor) editors.getComponentAt(editors.getSelectedIndex());

		jfc.setFileFilter(new Filter(e.lang()));

		jfc.showSaveDialog(null);
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		if (!jfc.getSelectedFile().getAbsolutePath().endsWith("." + e.lang().fileExtension())) {
			f = new File(jfc.getSelectedFile() + "." + e.lang().fileExtension());
		}

		doSave(f, e.getText());
		// change for david
		dirty.put(e.id, false);
		closeAction();
		doOpen(f, e.lang());
	}

	static void doOpen(File f, Language isPatrick) {
		String s = readFile(f.getAbsolutePath());
		if (s == null) {
			return;
		}
		Integer i = newAction(f.getName(), s, isPatrick);
		files.put(i, f);
	}

	static protected void openAction() {
		delay();
		JFileChooser jfc = new JFileChooser(GlobalOptions.debug.general.file_path);
		jfc.setFileFilter(new AllFilter());
		jfc.showOpenDialog(null);
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		for (Language l : Language.values()) {
			if (f.getAbsolutePath().endsWith("." + l.fileExtension())) {
				doOpen(f, l);
				return;
			}
		}
		throw new RuntimeException("Unknown file extension on " + f.getAbsolutePath());
	}

	static void setDirty(Integer i, boolean b) {
		dirty.put(i, b);
	}

	static Boolean getDirty(Integer i) {
		Boolean ret = dirty.get(i);
		if (ret == null) {
			throw new RuntimeException();
		}
		return dirty.get(i);
	}

	public static void setFontSize(int size) {
		for (@SuppressWarnings("rawtypes")
		CodeEditor c : keys.values()) {
			c.setFontSize(size);
		}
	}

	static Map<Integer, Boolean> dirty = new HashMap<>();
	@SuppressWarnings("rawtypes")
	static Map<Integer, CodeEditor> keys = new HashMap<>();
	static Map<Integer, File> files = new HashMap<>();
	static Map<Integer, String> titles = new HashMap<>();
	// static Map<Integer, Integer> position = new HashMap<>();
	static int untitled_count = 0;

	public static String getTitle(Integer i) {
		return titles.get(i);
	}

	static Integer newAction(String title, String content, Language isPatrick) {
		untitled_count++;
		@SuppressWarnings("rawtypes")
		CodeEditor c = isPatrick.createEditor(untitled_count, content);
		int i = editors.getTabCount();
		keys.put(untitled_count, c);
		dirty.put(untitled_count, false);
		// position.put(untitled_count, i);

		if (title == null) {
			title = "Untitled " + untitled_count;
		}
		titles.put(c.id, title);
		editors.addTab(title, c);
		editors.setTabComponentAt(i, new ButtonTabComponent(editors));
		editors.setSelectedIndex(i);
		// editors.requestFocus();
		// editors.requestFocus(false); //requestDefaultFocus();
		// c.dis
		return c.id;

	}

	static private String readFile(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			reader.close();
			return stringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	}

	private static void delay() {
		try {
			// Thread.currentThread();
			Thread.sleep(100); // hack for enough time to unhighlight menu
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	

	private static void formatAction() {
		int i = editors.getSelectedIndex();
		CodeEditor<?,?,?> c = (CodeEditor<?,?,?>) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		if (!c.lang().equals(Language.FQL)) {
			return;
		}
		FqlCodeEditor cc = (FqlCodeEditor) c;
		cc.format();
	}

	private static void veditAction() {
		int i = editors.getSelectedIndex();
		CodeEditor<?,?,?> c = (CodeEditor<?,?,?>) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		if (!c.lang().equals(Language.FQL)) {
			return;
		}
		FqlCodeEditor cc = (FqlCodeEditor) c;
		cc.format();
		cc.vedit();
	}

	private static void checkAction() {
		int i = editors.getSelectedIndex();
		CodeEditor<?,?,?> c = (CodeEditor<?,?,?>) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		if (!c.lang().equals(Language.FQL)) {
			return;
		}
		FqlCodeEditor cc = (FqlCodeEditor) c;
		cc.format();
		cc.check();
	}

}
