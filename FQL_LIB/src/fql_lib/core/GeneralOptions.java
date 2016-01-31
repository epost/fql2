package fql_lib.core;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.function.Function;

import javax.swing.JButton;
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

import catdata.algs.Pair;
import catdata.algs.Unit;


public class GeneralOptions extends Options {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "General";
	}
	
	public String file_path = "";
	public String look_and_feel = UIManager.getSystemLookAndFeelClassName();
	public int font_size = 12;

	//@Override
	public Pair<JComponent, Function<Unit, Unit>> display() {
		JPanel general1 = new JPanel(new GridLayout(Options.biggestSize, 1));
		JPanel general2 = new JPanel(new GridLayout(Options.biggestSize, 1));

		JSplitPane generalsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		generalsplit.add(general1);
		generalsplit.add(general2);

		JTextField fileArea = new JTextField(24);
		fileArea.setText(file_path);
		JLabel fileLabel = new JLabel("Default File Chooser Path:");
		general1.add(fileLabel);
		JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
		pan.add(choose);
		pan.add(fileArea);
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
		JPanel w = new JPanel(new FlowLayout(FlowLayout.LEFT));
		w.add(font_field);
		general2.add(w);
		
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
		return 3;
	} 

}
