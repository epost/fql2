package catdata.ide;

import java.awt.MenuBar;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import catdata.Pair;
import catdata.Triple;
import catdata.fql.decl.Driver;
import catdata.fql.decl.FQLProgram;
import catdata.fql.decl.FqlEnvironment;
import catdata.fql.parse.FQLParser;

/**
 * 
 * @author ryan
 * 
 *         Program entry point.
 */
public class IDE {
	
	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				FQLProgram init = FQLParser.program(args[0]);
				Triple<FqlEnvironment, String, List<Throwable>> envX = Driver
						.makeEnv(init, new String[0]);
				if (envX.third.size() > 0) {
					throw new RuntimeException("Errors: " + envX.third);
				}
				System.out.println("OK");
				System.out.println(envX.second);
				return;
			} catch (Throwable err) {
				err.printStackTrace(System.err);
				System.out.println(err.getLocalizedMessage());
				return;
			}
		} 
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
 					GlobalOptions.load();
					
					UIManager.setLookAndFeel(GlobalOptions.debug.general.look_and_feel);			

					final JFrame f = new JFrame("Categorical Data IDE");
					
					/* try {
						f.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("ci.png")));
					} catch (Exception ex) {
					} */
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