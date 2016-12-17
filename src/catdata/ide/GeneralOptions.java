package catdata.ide;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import catdata.Pair;
import catdata.Unit;


public class GeneralOptions extends Options {

	private static final long serialVersionUID = 2L;

	@Override
	public String getName() {
		return "General";
	}
	
	public String file_path = "";
	public String look_and_feel 
	= System.getProperty("os.name").toLowerCase().contains("mac") 
	  ? UIManager.getSystemLookAndFeelClassName() : "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	public int font_size = 12;
	public boolean spellcheck = true;

	//@Override
	@Override
	public Pair<JComponent, Function<Unit, Unit>> display() {
		JPanel general1 = new JPanel(new GridLayout(Options.biggestSize, 1));
		JPanel general2 = new JPanel(new GridLayout(Options.biggestSize, 1));

		JSplitPane generalsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		generalsplit.add(general1);
		generalsplit.add(general2);

		JTextField fileArea = new JTextField();
		fileArea.setText(file_path);
		JLabel fileLabel = new JLabel("Default File Chooser Path:");
		general1.add(fileLabel);
		JButton choose = new JButton("Choose");
		choose.addActionListener(x -> {
			JFileChooser jfc = new JFileChooser();
			File f = new File(fileArea.getText());
			jfc.setSelectedFile(f);
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = jfc.showOpenDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				fileArea.setText(jfc.getSelectedFile().getAbsolutePath());
			}
		});
		JPanel pan = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.anchor = GridBagConstraints.LINE_START;
		c2.weightx = 1.0;
		
		GridBagConstraints c1 = new GridBagConstraints();
		c1.anchor = GridBagConstraints.LINE_END;
		c1.fill = GridBagConstraints.NONE;
		
		JPanel ppp = new JPanel(new GridLayout(1,1));
		ppp.add(fileArea);
		pan.add(ppp, c2);
		pan.add(choose, c1);
		general2.add(pan);
	
		general1.add(new JLabel("Look and feel:"));
		String[] items = new String[UIManager.getInstalledLookAndFeels().length];
		int i = 0;
		for (LookAndFeelInfo k : UIManager.getInstalledLookAndFeels()) {
			items[i++] = k.getClassName(); 
		}
		JComboBox<String> lfb = new JComboBox<>(items);
		lfb.setSelectedItem(look_and_feel);
		general2.add(lfb);
	
		JTextField font_field = new JTextField(3);
		font_field.setText(Integer.toString(font_size));
		JLabel font_label = new JLabel("Editor font size:");
		font_label.setToolTipText("Sets the size of the font used in all editors.");
		general1.add(font_label);
		general2.add(wrap(font_field));
			
		JCheckBox spellbox = new JCheckBox("");
		spellbox.setSelected(spellcheck);
		JLabel slabel = new JLabel("Spell check comments:");
		general1.add(slabel);
		general2.add(spellbox);
		
		
		for (int j = 0; j < Options.biggestSize - size(); j++) {
			general1.add(new JLabel());
			general2.add(new JLabel());
		}
		
		
		
		Function<Unit, Unit> fn = new Function<Unit, Unit>() {

			@Override
			public Unit apply(Unit t) {
				try {
					Integer new_font_size = Integer.parseInt(font_field.getText().trim());
					if (new_font_size < 1) {
						new_font_size = font_size;
					}
					font_size = new_font_size;
					GUI.setFontSize(font_size);
				} catch (NumberFormatException nfe) {
				}
				
				File file = new File(fileArea.getText());
				if (!fileArea.getText().trim().equals("") && (!file.exists() || !file.isDirectory())) {
					JOptionPane.showMessageDialog(null, "Bad file-system path");				
				} else {
					file_path = fileArea.getText();
				}

				spellcheck = spellbox.isSelected();
				GUI.keys.values().forEach(x -> x.clearSpellCheck());
				
				if (!lfb.getSelectedItem().equals(look_and_feel)) {
					try {
						UIManager.setLookAndFeel(lfb.getSelectedItem().toString());
						SwingUtilities.updateComponentTreeUI(GUI.topFrame);
						look_and_feel = lfb.getSelectedItem().toString();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e);
					}
				}
				
				return new Unit();
			}
			
		};

		return new Pair<>(generalsplit, fn);
	}

	

	@Override
	public int size() {
		return 4;
	} 

}
