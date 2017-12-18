package catdata.ide;

import java.awt.HeadlessException;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import catdata.Pair;
import catdata.ide.IdeOptions.IdeOption;

/**
 * 
 * @author ryan
 * 
 *         Program entry point.
 */
public class IDE {
/*
	public static final class OldEntryPoint {
		public static void main(String... args) {
			if (args.length == 1) {
				try {
					FQLProgram init = FQLParser.program(args[0]);
					Triple<FqlEnvironment, String, List<Throwable>> envX = Driver.makeEnv(init);
					if (!envX.third.isEmpty()) {
						throw new RuntimeException("Errors: " + envX.third);
					}
					System.out.println("OK");
					System.out.println(envX.second);
					return;
				} catch (RuntimeException err) {
					err.printStackTrace(System.err);
					System.out.println(err.getLocalizedMessage());
					return;
				}
			}
		}
	} */

	public static void main(String... args) {
		System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
		//apple.awt.application.name

		SwingUtilities.invokeLater(() -> {
			try {
				DefunctGlobalOptions.load();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {

				UIManager.setLookAndFeel(IdeOptions.theCurrentOptions.getString(IdeOption.LOOK_AND_FEEL));

				JFrame f = new JFrame("AQL IDE");

				Pair<JPanel, MenuBar> gui = GUI.makeGUI(f);

				f.setContentPane(gui.first);
				f.setMenuBar(gui.second);
				f.pack();
				f.setSize(1024, 640);
				f.setLocationRelativeTo(null);
				f.setVisible(true);
				
				f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent windowEvent) {
						GUI.exitAction();
					}
				});
				
				if (args.length == 0) {
					GUI.newAction(null, "", Language.getDefault());
				} else {
					File[] fs = new File[args.length];
					int i = 0;
					for (String s : args) {
						fs[i++] = new File(s);
					}
					GUI.openAction(fs);
				}
				
				((CodeEditor<?, ?, ?>) GUI.editors.getComponentAt(0)).topArea.requestFocusInWindow();		
				
			} catch (HeadlessException | ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unrecoverable error, restart IDE: " + e.getMessage());
			}
		});
	}

}