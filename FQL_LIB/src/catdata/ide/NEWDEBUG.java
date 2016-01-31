package catdata.ide;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import catdata.Pair;
import catdata.Unit;
import catdata.fpql.XOptions;
import catdata.fpqlpp.AOptions;
import catdata.fqlpp.FqlppOptions;
import catdata.opl.OplOptions;

/**
 * 
 * @author ryan
 * 
 *         Contains global constants for debugging.
 */
public class NEWDEBUG implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static NEWDEBUG debug = new NEWDEBUG();
	
	public void clear() {
		debug = new NEWDEBUG();
	}

	static int selected_tab = 0;

	public GeneralOptions general = new GeneralOptions(); 
//	public GeneralOptions fql = new GeneralOptions(); 
	public FqlppOptions fqlpp = new FqlppOptions(); 
	public XOptions fpql = new XOptions(); 
	public AOptions fpqlpp = new AOptions(); 
	public OplOptions opl = new OplOptions(); 
	
	private Options[] options = new Options[] { general, /* fql, */ fqlpp, fpql, fpqlpp, opl };
	
	{
		for (Options option : options) {
			Options.biggestSize = Integer.max(Options.biggestSize, option.size());
		}
	}
	
	public void save() {
		try {
			FileOutputStream fileOut = new FileOutputStream("cdide.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} catch (Exception i) {
			i.printStackTrace();
			JOptionPane.showMessageDialog(null, i.getLocalizedMessage());
		}
	}

	public static NEWDEBUG load(boolean silent) {
		try {
			FileInputStream fileIn = new FileInputStream("cdide.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			NEWDEBUG e = (NEWDEBUG) in.readObject();
			if (e != null) {
				if (silent) {
					debug = e;
				} else {
					e.showOptions();
				}
			}
			in.close();
			fileIn.close();
		} catch (Exception i) {
			if (!silent) {
				i.printStackTrace();
				JOptionPane.showMessageDialog(null, i.getLocalizedMessage());
			}
		}
		return null;
	}

				
	public void showOptions() {
		JTabbedPane jtb = new JTabbedPane();

		List<Function<Unit, Unit>> callbacks = new LinkedList<>();
		for (Options option : options) {
			Pair<JComponent, Function<Unit, Unit>> x = option.display();
			jtb.add(option.getName(), x.first);
			callbacks.add(x.second);
		}
		jtb.setSelectedIndex(selected_tab);
		
		/*jtb.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                Window window = SwingUtilities.getWindowAncestor(jtb);
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog)window;
                    if (!dialog.isResizable()) {
                        dialog.setResizable(true);
                    }
                }
            }
        });*/
	
		JOptionPane pane = new JOptionPane(jtb, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null,
				new String[] { "OK", "Cancel", "Reset", "Save", "Load" }, "OK");
		
		JDialog dialog = pane.createDialog(null, "Options");
		dialog.setModal(false);
		dialog.setResizable(true);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowDeactivated(WindowEvent e) {
				Object ret = pane.getValue();
				
				selected_tab = jtb.getSelectedIndex();

				if (ret == "OK" || ret == "Save") {
					for (Function<Unit, Unit> callback : callbacks) {
						callback.apply(new Unit());
					}
				} else if (ret == "Reset") {
					new NEWDEBUG().showOptions();
				}
				if (ret == "Save") { // save
					save();
					showOptions();
				}

				if (ret == "Load") { // load
					load(false);
				}  
			}
		
		});
		
		dialog.setVisible(true);
		
//		pane.getValue()
		
/*		int ret = JOptionPane.showOptionDialog(null, jtb, "Options",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				new String[] { "OK", "Cancel", "Reset", "Save", "Load" }, "OK");

		selected_tab = jtb.getSelectedIndex();

		if (ret == 0 || ret == 3) {
			for (Function<Unit, Unit> callback : callbacks) {
				callback.apply(new Unit());
			}
		} else if (ret == 2) {
			new DEBUG().showOptions();
		}
		if (ret == 3) { // save
			save();
			showOptions();
		}

		if (ret == 4) { // load
			load(false);
		}  */
	
	}
	

	public static void showAbout() {
		JOptionPane.showMessageDialog(null, about, "About",
				JOptionPane.PLAIN_MESSAGE, null);
	}

	static String about = "Categorical Data IDE Copyright (C) 2012-2016 Patrick Schultz, David Spivak, and Ryan Wisnesky"
			+ "\n\nplease see categoricaldata.net for more information";

}
