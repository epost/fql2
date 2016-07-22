package catdata.ide;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import catdata.Pair;
import catdata.Unit;
import catdata.fpql.XOptions;
import catdata.fql.FqlOptions;
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
	public FqlOptions fql = new FqlOptions(); 
	public FqlppOptions fqlpp = new FqlppOptions(); 
	public XOptions fpql = new XOptions(); 
	public OplOptions opl = new OplOptions(); 
	
	private Options[] options() {
		return new Options[] { general, fql, fqlpp, fpql, opl };
	}
	
	{
		for (Options option : options()) {
			Options.biggestSize = Integer.max(Options.biggestSize, option.size());
		}
	}
	
	public static void save() {
		try {
			FileOutputStream fileOut = new FileOutputStream("cdide.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(debug);
			out.close();
			fileOut.close();
		} catch (Exception i) {
			i.printStackTrace();
			JOptionPane.showMessageDialog(null, i.getLocalizedMessage());
		}
	}
	
	public static void delete() {
		File f = new File("cdide.ser");
		if (!f.exists()) {
			return;
		}
		f.delete();
	}

	public static void load() {
		try {
			if (!new File("cdide.ser").exists()) {
				return;
			}
			FileInputStream fileIn = new FileInputStream("cdide.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			NEWDEBUG e = (NEWDEBUG) in.readObject();
			in.close();
			fileIn.close();

			if (e != null) {
				debug = e;
			} else {
				throw new RuntimeException("Cannot restore options, file corrupt");
			}
		} catch (Exception i) {
				i.printStackTrace();
				JOptionPane.showMessageDialog(null, i.getLocalizedMessage());
		}
		return;
	}

				
	public static void showOptions() {
		JTabbedPane jtb = new JTabbedPane();

		List<Function<Unit, Unit>> callbacks = new LinkedList<>();
		for (Options option : debug.options()) {
			Pair<JComponent, Function<Unit, Unit>> x = option.display();
			jtb.add(option.getName(), x.first);
			callbacks.add(x.second);
		}
		jtb.setSelectedIndex(selected_tab);
		
		JOptionPane pane = new JOptionPane(new JScrollPane(jtb), JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null,
				new String[] { "OK", "Cancel", "Reset", "Save", "Load", "Delete" }, "OK");
		
		JDialog dialog = pane.createDialog(null, "Options");
		dialog.setModal(false);
		dialog.setResizable(true);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowDeactivated(WindowEvent e) {
				Object ret = pane.getValue();
				
				selected_tab = jtb.getSelectedIndex();

				if (ret == "OK") {
					for (Function<Unit, Unit> callback : callbacks) {
						callback.apply(new Unit());
					}
				//	System.out.println("OPL options set to : " + debug.opl);
				} else if (ret == "Reset") {
					debug = new NEWDEBUG();
					showOptions();
				} else if (ret == "Save") { // save
					for (Function<Unit, Unit> callback : callbacks) {
						callback.apply(new Unit());
					}
					save();
					showOptions();
				} else if (ret == "Load") { // load
					load();
					showOptions();
				} else if (ret == "Cancel") {
					
				} else if (ret == "Delete") {
					delete();
					showOptions();
				} //else if (ret != null && ret != uninitializedValue") {
				//	throw new RuntimeException("xx " + ret);
				//}
			}
			
		
		});
		
		dialog.pack();
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
