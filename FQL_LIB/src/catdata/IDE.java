package catdata;

import java.awt.MenuBar;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import catdata.algs.Pair;
import catdata.ide.CodeEditor;
import catdata.ide.Examples;
import catdata.ide.GUI;
import catdata.ide.NEWDEBUG;

/**
 * 
 * @author ryan
 * 
 *         Program entry point.
 */
public class IDE {
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Arrays.sort(Examples.examples);

					NEWDEBUG.load(true);
					
					UIManager.setLookAndFeel(NEWDEBUG.debug.general.look_and_feel);			

					final JFrame f = new JFrame("Categorical Data IDE");
					final Pair<JPanel, MenuBar> gui = GUI.makeGUI(f);

					f.setContentPane(gui.first);
					f.setMenuBar(gui.second);
					f.pack();
					f.setSize(840, 630);
					((CodeEditor<?,?,?>) GUI.editors.getComponentAt(0)).topArea
							.requestFocusInWindow();
					f.setLocationRelativeTo(null);
					f.setVisible(true);

					f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					f.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(
								java.awt.event.WindowEvent windowEvent) {
							GUI.exitAction();

						}
					});
				} catch (Throwable e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							null,
							"Unrecoverable error, restart IDE: "
									+ e.getMessage());
				}
			}
		});
	}


}