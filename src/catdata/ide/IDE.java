package catdata.ide;

import java.awt.HeadlessException;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

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
class IDE {
	
	public static void main(String... args) {
		if (args.length == 1) {
			try {
				FQLProgram init = FQLParser.program(args[0]);
				Triple<FqlEnvironment, String, List<Throwable>> envX = Driver
						.makeEnv(init);
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
		
		//TODO AQL get two prompts for some reason
		System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
		
		SwingUtilities.invokeLater(() -> {
                    try {
                        GlobalOptions.load();
                        
                       String lf = System.getProperty("os.name").toLowerCase().contains("mac") 
                        		  ? UIManager.getSystemLookAndFeelClassName() : "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

                        UIManager.setLookAndFeel(lf);
                        
                        JFrame f = new JFrame("Categorical Data IDE");
                       
                        Pair<JPanel, MenuBar> gui = GUI.makeGUI(f);
                        
                        f.setContentPane(gui.first);
                        f.setMenuBar(gui.second);
                        f.pack();
                        f.setSize(840, 630);
                        ((CodeEditor<?,?,?>) GUI.editors.getComponentAt(0)).topArea
                                .requestFocusInWindow();
                        f.setLocationRelativeTo(null);
                        f.setVisible(true);
                        /*
                        ((Graphics2D)f.getGraphics()).setRenderingHint(
                                RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
           */
                        
                        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        f.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(
                                    WindowEvent windowEvent) {
                                GUI.exitAction();
                            }
                        });
                    } catch (HeadlessException | ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                null,
                                "Unrecoverable error, restart IDE: "
                                        + e.getMessage());
                    }
                });
	}


}