package catdata.ide;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextAreaBase;

import catdata.Util;
import catdata.aql.AqlOptions;

//TODO have AQL add the tab to IdeOptions so that IdeOptions does not depend on AQL
public class IdeOptions {
	
	private static File fname = new File(".fql.properties");

	public static IdeOptions theCurrentOptions = new IdeOptions();

	
	
	static {
		if (fname.exists()) {
			theCurrentOptions = load();
		}
	}

	private String get(String k) {
		String r = prop.getProperty(k);
		if (r == null) {
			throw new RuntimeException("Returned null on " + k);
		}
		return r;
	}

	public void fontSizeUp() {
		Font old = getFont(IdeOption.FONT);
		Font font = new Font(old.getFontName(), old.getStyle(), old.getSize()+1);
		setFont(IdeOption.FONT, font);
		notifyListenersOfChange();
	}
	
	public void fontSizeDown() {
		Font old = getFont(IdeOption.FONT);
		Font font = new Font(old.getFontName(), old.getStyle(), Integer.max(1, old.getSize()-1));
		setFont(IdeOption.FONT, font);
		notifyListenersOfChange();
	}
	

	private final Properties prop; 

	public IdeOptions() {
		prop = makeDefault();
	}

	public IdeOptions(IdeOptions o) {
		prop = new Properties(); //can't pass o.prop bc defaults aren't serialized
		for (Object obj : o.prop.keySet()) {
			String s = (String) obj;
			prop.setProperty(s, o.prop.getProperty(s));
		}
	}

	private static Properties makeDefault() {
		Properties ret = new Properties();
		for (IdeOption o : IdeOption.values()) {
			ret.setProperty(o.name(), toString(o, o.default0));
		}
		return ret;
	}

	private static String toString(IdeOption o, Object obj) {
		switch (o.type) {
		case BOOL:
			return obj.toString();
		case COLOR:
			return Integer.toString(((Color) obj).getRGB());
		case FILE:
			return ((File) obj).toURI().toString();
		case FONT:
			Font f = (Font) obj;
			String s = "";
			if (f.isBold()) {
				s += "BOLD";
			}
			if (f.isItalic()) {
				s += "ITALIC";
			}
			return f.getName() + " " + s + " " + f.getSize();
		case LF:
			return obj.toString();
		case NAT:
			return obj.toString();
		default:
			return Util.anomaly();
		}
	}

	private static <X> X check(X x) {
		if (x == null) {
			throw new RuntimeException();
		}
		return x;
	}

	public String getString(IdeOption o) {
		return check(get(o.name()));
	}
	
	public void setString(IdeOption o, String s) {
		prop.setProperty(o.name(), check(s));
	}

	public Integer getNat(IdeOption o) {
		return check(Integer.parseInt(get(o.name())));
	}

	public void setNat(IdeOption o, Integer i) {
		if (i < 0) {
			Util.anomaly();
		}
		prop.setProperty(o.name(), toString(o, i));
	}

	public Color getColor(IdeOption o) {
		return check(Color.decode(get(o.name())));
	}

	public void setColor(IdeOption o, Color c) {
		prop.setProperty(o.name(), toString(o, c));
	}

	public File getFile(IdeOption o) {
		try {
			return new File(new URI(get(o.name())));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setFile(IdeOption o, File f) {
		prop.setProperty(o.name(), toString(o, f));
	}

	public Boolean getBool(IdeOption o) {
		return Boolean.parseBoolean(get(o.name()));
	}
	
	public void setBool(IdeOption o, Boolean b) {
		prop.put(o.name(), toString(o, b));
	}

	public Font getFont(IdeOption o) {
		return check(Font.decode(get(o.name())));
	}

	public void setFont(IdeOption o, Font f) {
		prop.put(o.name(), toString(o, f));
		Font font = UIManager.getFont("Table.font");
		font = new Font(font.getFontName(), font.getStyle(), f.getSize());
		UIManager.put("Table.font", font);

		font = UIManager.getFont("TableHeader.font");
		font = new Font(font.getFontName(), font.getStyle(), f.getSize());
		UIManager.put("TableHeader.font", font);
		

		font = UIManager.getFont("List.font");
		font = new Font(font.getFontName(), font.getStyle(), f.getSize());
		UIManager.put("List.font", font);

	}

	private static int size(boolean onlyColors) {
		int i = 0;
		for (IdeOption o : IdeOption.values()) {
			if ( (onlyColors && o.type == IdeOptionType.COLOR) || (!onlyColors && o.type != IdeOptionType.COLOR) ) {
				i++;
			}		
		}
		return i;
	}
	
	private static int size() {
		return Integer.max(size(true), size(false));
	}
	

	private JComponent display(boolean onlyColors) {
		JPanel p1 = new JPanel(new GridLayout(size(), 1));
		JPanel p2 = new JPanel(new GridLayout(size(), 1));

		JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		p.add(p1);
		p.add(p2);

		for (IdeOption o : IdeOption.values()) {
			if ( (onlyColors && o.type == IdeOptionType.COLOR) || (!onlyColors && o.type != IdeOptionType.COLOR) ) {
				p1.add(new JLabel(o.toString()));
				p2.add(viewerFor(o));
			}		
		}

		return p;
	}

	private static JComponent pair(JComponent l, JComponent r) {
		JPanel pan = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.anchor = GridBagConstraints.LINE_START;
		c2.weightx = 1.0;

		GridBagConstraints c1 = new GridBagConstraints();
		c1.anchor = GridBagConstraints.LINE_END;
		c1.fill = GridBagConstraints.NONE;

		JPanel ppp = new JPanel(new GridLayout(1, 1));
		ppp.add(pan);
		pan.add(l, c2);
		pan.add(r, c1);
		return ppp;
	}

	private JComponent viewerFor(IdeOption o) {
		switch (o.type) {
		case BOOL:
			JCheckBox b = new JCheckBox("", getBool(o));
			b.addActionListener(x -> setBool(o, b.isSelected()));
			return b;
		case COLOR:
			JButton button = new JButton("Set Color");
			JLabel l = new JLabel("   ");
			l.setOpaque(true);
			l.setBackground(getColor(o));

			// l.revalidate();
			button.addActionListener(x -> {
				Color c = JColorChooser.showDialog(null, "Set " + o.toString(), getColor(o));
				if (c != null) {
					setColor(o, c);
					l.setBackground(getColor(o));
					l.revalidate();
				}
			});

			return pair(l, button);
		case FILE:
			button = new JButton("Set Dir");
			JTextField ll = new JTextField(toString(o, getFile(o)));
			ll.setEditable(false);
			button.addActionListener(x -> {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setSelectedFile(getFile(o));
				jfc.showOpenDialog(null);
				File f = jfc.getSelectedFile();
				if (f == null) {
					return;
				}
				setFile(o, f);
				ll.setText(toString(o, getFile(o)));
				ll.revalidate();
			});

			return pair(ll, button);
		case FONT:
			button = new JButton("Set Font");
			l = new JLabel(toString(o, getFont(o)));
			button.addActionListener(x -> {
				JFontChooser c = new JFontChooser();
				c.setSelectedFont(getFont(o));
				int ret = c.showDialog(null);
				if (ret == JFontChooser.OK_OPTION) {
					setFont(o, c.getSelectedFont());
					l.setText(toString(o, getFont(o)));
					l.revalidate();
					// System.out.println("Set to " + c.getSelectedFont());
				}
			});

			return pair(l, button);
		case LF:
			String[] items = new String[UIManager.getInstalledLookAndFeels().length];
			int i = 0;
			for (LookAndFeelInfo k : UIManager.getInstalledLookAndFeels()) {
				items[i++] = k.getClassName();
			}
			JComboBox<String> lfb = new JComboBox<>(items);
			lfb.setSelectedItem(getString(o));
			lfb.addActionListener(x -> {
				setString(o, (String)lfb.getSelectedItem());
			});
			return lfb;
		case NAT:
			SpinnerModel model = new SpinnerNumberModel(getNat(o).intValue(), 0, 1000, 1);
			JSpinner spinner = new JSpinner(model);
			spinner.addChangeListener(x -> {
				setNat(o, (Integer)spinner.getValue());
			});
			JPanel pan = new JPanel(new GridLayout(1,6));
			pan.add(spinner);
			pan.add(new JLabel());
			pan.add(new JLabel());
			pan.add(new JLabel());
			pan.add(new JLabel());
			pan.add(new JLabel());
			return pan;
		default:
			return Util.anomaly();
		}
	}

	public static enum IdeOptionType {
		NAT, COLOR, FILE, BOOL, LF, FONT
	}

	private static String defaultLF() {
	       return System.getProperty("os.name").toLowerCase().contains("mac") 
         		  ? UIManager.getSystemLookAndFeelClassName() :
         			  "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	}
	
	public void apply(JTextArea a) {
		for (IdeOption o : IdeOption.values()) {
			apply(o, a);
		}
		a.repaint();
		a.revalidate();
	}
	
	public void apply(CodeEditor<?,?,?> a) {
		for (IdeOption o : IdeOption.values()) {
			apply(o, a.topArea);
			apply(o, a.respArea.area);
		}
		a.clearSpellCheck();
		a.topArea.repaint();
		a.topArea.revalidate();
		a.respArea.area.revalidate();
	}
	
	public void apply(IdeOption o, JTextArea a) {
	
		switch (o) {
		case FILE_PATH:
		case LOOK_AND_FEEL:
		case SPELL_CHECK:
		case COMMENT_COLOR:
		case KEYWORD_1_COLOR:
		case KEYWORD_2_COLOR:
		case QUOTE_COLOR:
		case BRACKET_MATCH_BG_COLOR:
		case BRACKET_MATCH_BORDER_COLOR:
		case CURRENT_LINE_HIGHLIGHT_COLOR:
		case MARGIN_LINE_COLOR:
		case MARK_ALL_HIGHLIGHT_COLOR:
		case AUTO_CLOSE_BRACES:
		case AUTO_INDENT:
		case LINE_HIGHLIGHT:
		case MARK_OCCURANCES:
		case SHOW_MARGIN:
		case FADE_CURRENT_LINE:
		case FOLDING:
		case MATCH_BRACKET:
		case ROUNDED_EDGES:
		case SHOW_MATCHED_POPUP:
		case TABS_EMULATED:
		case ANIMATE_MATCH:
		case MARGIN_COLS:
			return;
	
		case BACKGROUND_COLOR:
			a.setBackground(getColor(o));
			return;
	
		case CARET_COLOR:
			a.setCaretColor(getColor(o));
			return;
	
		case FONT:
			a.setFont(getFont(o));
			return;
		case FOREGROUND_COLOR:
			a.setForeground(getColor(o));
			return;	
		case LINE_WRAP:
			a.setLineWrap(getBool(o));
			return;
		case SELECTION_COLOR:
			a.setSelectionColor(getColor(o));
			return;
		case TAB_SIZE:
			a.setTabSize(getNat(o));
			return;
			
		default:
			Util.anomaly();
		}
	}
	
	public static void notifyListenersOfChange() {
		GUI.optionsHaveChanged();
	}
	
	public void apply(IdeOption o, RSyntaxTextArea a) {
		SyntaxScheme scheme = a.getSyntaxScheme();
	
		switch (o) {
		case FILE_PATH:
			return;
		case LOOK_AND_FEEL:
			return;
		case SPELL_CHECK:
			return;

		case COMMENT_COLOR:
			//new org.fife.ui.rsyntaxtextarea. 
			scheme.getStyle(TokenTypes.COMMENT_EOL).foreground = getColor(o);
			/*scheme.getStyle(TokenTypes.COMMENT_DOCUMENTATION).foreground = getColor(o);
			scheme.getStyle(TokenTypes.COMMENT_MARKUP).foreground = getColor(o);
			scheme.getStyle(TokenTypes.COMMENT_MULTILINE).foreground = getColor(o);
			scheme.getStyle(TokenTypes.COMMENT_KEYWORD).foreground = getColor(o);*/
			return;
		case KEYWORD_1_COLOR:
			scheme.getStyle(TokenTypes.RESERVED_WORD).foreground = getColor(o);
			return;
		case KEYWORD_2_COLOR:
			scheme.getStyle(TokenTypes.RESERVED_WORD_2).foreground = getColor(o);
			return;
		case QUOTE_COLOR:
			scheme.getStyle(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE).foreground = getColor(o);
			return;

		case BACKGROUND_COLOR:
			a.setBackground(getColor(o));
			return;
		case BRACKET_MATCH_BG_COLOR:
			a.setMatchedBracketBGColor(getColor(o));
			return;
		case BRACKET_MATCH_BORDER_COLOR:
			a.setMatchedBracketBorderColor(getColor(o));
			return;
		case CARET_COLOR:
			a.setCaretColor(getColor(o));
			return;
		case CURRENT_LINE_HIGHLIGHT_COLOR:
			a.setCurrentLineHighlightColor(getColor(o));
			return;
		case FONT:
			a.setFont(getFont(o));
			return;
		case FOREGROUND_COLOR:
			a.setForeground(getColor(o));
			return;	
		case LINE_WRAP:
			a.setLineWrap(getBool(o));
			return;
		case MARGIN_LINE_COLOR:
			a.setMarginLineColor(getColor(o));
			return;
		case MARK_ALL_HIGHLIGHT_COLOR:
			a.setMarkAllHighlightColor(getColor(o));
			return;
		case SELECTION_COLOR:
			a.setSelectionColor(getColor(o));
			return;
		case AUTO_CLOSE_BRACES:
			a.setCloseCurlyBraces(getBool(o));
			return;
		case AUTO_INDENT:
			a.setAutoIndentEnabled(getBool(o));
			return;
		case LINE_HIGHLIGHT:
			a.setHighlightCurrentLine(getBool(o));
			return;
		case MARK_OCCURANCES:
			a.setMarkOccurrences(getBool(o));
			return;
		case SHOW_MARGIN:
			a.setMarginLineEnabled(getBool(o));
			return;
		case FADE_CURRENT_LINE:
			a.setFadeCurrentLineHighlight(getBool(o));
			return;
		case FOLDING:
			a.setCodeFoldingEnabled(getBool(o));
			return;
		case MATCH_BRACKET:
			a.setBracketMatchingEnabled(getBool(o));
			return;
		case ROUNDED_EDGES:
			a.setRoundedSelectionEdges(getBool(o));
			return;
		case SHOW_MATCHED_POPUP:
			a.setShowMatchedBracketPopup(getBool(o));
			return;
		case TABS_EMULATED:
			a.setTabsEmulated(getBool(o));
			return;
		case ANIMATE_MATCH:
			a.setAnimateBracketMatching(getBool(o));
			return;
		case MARGIN_COLS:
			a.setMarginLinePosition(getNat(o));
			return;
		case TAB_SIZE:
			a.setTabSize(getNat(o));
			return;
		default:
			Util.anomaly();
		}
	}
	
	public static enum IdeOption {
		LOOK_AND_FEEL(IdeOptionType.LF, defaultLF()), 
		FILE_PATH(IdeOptionType.FILE, new File("")), 
		FONT(IdeOptionType.FONT, RTextAreaBase.getDefaultFont()),
		LINE_WRAP(IdeOptionType.BOOL, false),
		SPELL_CHECK(IdeOptionType.BOOL, true),
		AUTO_CLOSE_BRACES(IdeOptionType.BOOL, true),
		AUTO_INDENT(IdeOptionType.BOOL, true),
		SHOW_MARGIN(IdeOptionType.BOOL, true),
		LINE_HIGHLIGHT(IdeOptionType.BOOL, true),
		MARK_OCCURANCES(IdeOptionType.BOOL, true),
		MATCH_BRACKET(IdeOptionType.BOOL, true),
		FOLDING(IdeOptionType.BOOL, true),
		FADE_CURRENT_LINE(IdeOptionType.BOOL, false),
		ROUNDED_EDGES(IdeOptionType.BOOL, false),
		SHOW_MATCHED_POPUP(IdeOptionType.BOOL, true),
		TABS_EMULATED(IdeOptionType.BOOL, false),
		ANIMATE_MATCH(IdeOptionType.BOOL, true),
		
		BACKGROUND_COLOR(IdeOptionType.COLOR, Color.WHITE), 
		FOREGROUND_COLOR(IdeOptionType.COLOR, RTextAreaBase.getDefaultForeground()), 
		KEYWORD_1_COLOR(IdeOptionType.COLOR, Color.RED), 
		KEYWORD_2_COLOR(IdeOptionType.COLOR, Color.BLUE),
		COMMENT_COLOR(IdeOptionType.COLOR, new Color(-16744448)),
		QUOTE_COLOR(IdeOptionType.COLOR, Color.gray),
		CURRENT_LINE_HIGHLIGHT_COLOR(IdeOptionType.COLOR, RTextAreaBase.getDefaultCurrentLineHighlightColor()),
		MARK_ALL_HIGHLIGHT_COLOR(IdeOptionType.COLOR, RTextArea.getDefaultMarkAllHighlightColor()),
		CARET_COLOR(IdeOptionType.COLOR, Color.black),
		SELECTION_COLOR(IdeOptionType.COLOR, RSyntaxTextArea.getDefaultSelectionColor()),
		MARGIN_LINE_COLOR(IdeOptionType.COLOR, RTextAreaBase.getDefaultMarginLineColor()),
		BRACKET_MATCH_BG_COLOR(IdeOptionType.COLOR, RSyntaxTextArea.getDefaultBracketMatchBGColor()),
		BRACKET_MATCH_BORDER_COLOR(IdeOptionType.COLOR, RSyntaxTextArea.getDefaultBracketMatchBorderColor()),
		
		MARGIN_COLS(IdeOptionType.NAT, 100),
		TAB_SIZE(IdeOptionType.NAT, 4)
		
		;

		public final IdeOptionType type;
		public final Object default0;

		private IdeOption(IdeOptionType type, Object default0) {
			this.type = type;
			this.default0 = default0;
		}

		@Override
		public String toString() {
			switch (this) {
			case FILE_PATH:
				return "Initial file chooser path";
			case FONT:
				return "Font";
			case LINE_WRAP:
				return "Line wrap";
			case LOOK_AND_FEEL:
				return "Look and feel";
			case SPELL_CHECK:
				return "Spell check comments";
			case BACKGROUND_COLOR:
				return "Background";
			case BRACKET_MATCH_BG_COLOR:
				return "Bracket match background";
			case BRACKET_MATCH_BORDER_COLOR:
				return "Bracket match border";
			case CARET_COLOR:
				return "Caret";
			case COMMENT_COLOR:
				return "Comments";
			case CURRENT_LINE_HIGHLIGHT_COLOR:
				return "Current line highlight";
			case FOREGROUND_COLOR:
				return "Foreground";
			case KEYWORD_1_COLOR:
				return "Keyword 1";
			case KEYWORD_2_COLOR:
				return "Keyword 2";
			case MARGIN_LINE_COLOR:
				return "Margin line";
			case MARK_ALL_HIGHLIGHT_COLOR:
				return "Mark all highlight";
			case QUOTE_COLOR:
				return "Quote";
			case SELECTION_COLOR:
				return "Selection";
			case AUTO_CLOSE_BRACES:
				return "Auto close braces";
			case AUTO_INDENT:
				return "Auto indent";
			case LINE_HIGHLIGHT:
				return "Highlight current line";
			case MARK_OCCURANCES:
				return "Mark occurances";
			case SHOW_MARGIN:
				return "Show Margin";
			case FADE_CURRENT_LINE:
				return "Fade line highlight";
			case FOLDING:
				return "Code folding";
			case MATCH_BRACKET:
				return "Match brackets";
			case ROUNDED_EDGES:
				return "Rounded selection edges";
			case SHOW_MATCHED_POPUP:
				return "Matched bracket popup";
			case TABS_EMULATED:
				return "Tabs emulated";
			case ANIMATE_MATCH:
				return "Animate bracket match";
			case MARGIN_COLS:
				return "Columns before margin";
			case TAB_SIZE:
				return "Spaces per tab";
			default:
				return Util.anomaly();
			}
		}

	}

	/////////////////////////////

	// private static final long serialVersionUID = 1L;

	
	public static void clear() {
		theCurrentOptions = new IdeOptions();
	}

	private static int selected_tab = 0;


	//does not mutate
	private static void save(IdeOptions o) {
		try (FileWriter writer = new FileWriter(fname))	{
			o.prop.store(writer, "Categorical Data IDE Properties");
		} catch (IOException i) {
			i.printStackTrace();
			JOptionPane.showMessageDialog(null, i.getLocalizedMessage());
		}

	}

	private static void delete() {
		if (!fname.exists()) {
			return;
		}
		if (!fname.delete()) {
			throw new RuntimeException("Could not delete " + fname);
		}
	}

	public static IdeOptions load() {
		IdeOptions ret = new IdeOptions();
		try (FileReader writer = new FileReader(fname))	{
			ret.prop.load(writer);
		} catch (IOException i) {
			i.printStackTrace();
			JOptionPane.showMessageDialog(null, i.getLocalizedMessage());
		}
		return ret;
	}

	public static void showOptions() {
		IdeOptions.theCurrentOptions.showOptions0();
	}
	public void showOptions0() {
		IdeOptions o = this; //new IdeOptions(IdeOptions.theCurrentOptions);

		JTabbedPane jtb = new JTabbedPane();
		jtb.add("General", o.display(false));
		jtb.add("Colors", o.display(true));
		jtb.addTab("AQL", new CodeTextPanel("", AqlOptions.getMsg()));

		jtb.setSelectedIndex(selected_tab);

		JOptionPane pane = new JOptionPane(new JScrollPane(jtb), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new String[] { "OK", "Cancel", "Reset", "Save", "Load", "Delete" }, "OK");

		JDialog dialog = pane.createDialog(null, "Options");
		dialog.setModal(false);
		dialog.setResizable(true);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowDeactivated(WindowEvent e) {
				Object ret = pane.getValue();

				selected_tab = jtb.getSelectedIndex();

				if (ret == "OK") {
					theCurrentOptions = o;
					notifyListenersOfChange();
				} else if (ret == "Reset") {
					//theCurrentOptions = new IdeOptions();
					//notifyListenersOfChange();
					new IdeOptions().showOptions0();
				} else if (ret == "Save") { // save
					//theCurrentOptions = o;
					//notifyListenersOfChange();
					save(o);
					o.showOptions0();
				} else if (ret == "Load") { // load
					//theCurrentOptions = load();
					//notifyListenersOfChange();
					load().showOptions0();
				} else if (ret == "Delete") {
					delete();
					o.showOptions0();
				}
			}

		});

		dialog.pack();
		dialog.setVisible(true);

	}

	public static void showAbout() {
		JOptionPane.showMessageDialog(null, new CodeTextPanel("", about()), "About", JOptionPane.PLAIN_MESSAGE, null);
	}

	private static final String aboutErr = "No cdide.properties found.  If you are building from source, make sure cdide.properties is on the classpath.";

	private static String aboutString = null;

	private static String about() {
		if (aboutString != null) {
			return aboutString;
		}
		try (InputStream in = Object.class.getResourceAsStream("/cdide.properties")) {
			Properties prop = new Properties();
			prop.load(in);

			aboutString = in == null ? aboutErr : Util.sep(prop, ": ", "\n\n");
		} catch (IOException e) {
			e.printStackTrace();
			aboutString = aboutErr;
		}
		return aboutString;
	}

}
