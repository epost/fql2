package catdata.opl;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import catdata.algs.Pair;
import catdata.ide.CodeTextPanel;
import catdata.ide.Disp;
import catdata.opl.OplExp.OplInst;
import catdata.opl.OplExp.OplJavaInst;
import catdata.opl.OplExp.OplMapping;
import catdata.opl.OplExp.OplPres;
import catdata.opl.OplExp.OplPresTrans;
import catdata.opl.OplExp.OplSchema;
import catdata.opl.OplExp.OplSetInst;
import catdata.opl.OplExp.OplSetTrans;
import catdata.opl.OplExp.OplSig;
import catdata.opl.OplExp.OplTyMapping;

public class OplDisplay implements Disp {

	
	@Override
	public void close() {
	}
	
	@SuppressWarnings("rawtypes")
	private static String doLookup(String c, OplObject o) {
		if (o instanceof OplSig) {
			return "theory " + c;
		}
		if (o instanceof OplSetInst) {
			return "model " + c + " : " + ((OplSetInst)o).sig;
		}
		if (o instanceof OplJavaInst) {
			return "javascript " + c + " : " + ((OplJavaInst)o).sig;
		}
		if (o instanceof OplSetTrans) {
			OplSetTrans x = (OplSetTrans) o;
			return "transform " + c + " : " + x.src + " -> " + x.dst;
		}
		if (o instanceof OplMapping) {
			OplMapping x = (OplMapping) o;
			return "mapping " + c + " : " + x.src0 + " -> " + x.dst0;
 		}
		if (o instanceof OplTyMapping) {
			OplTyMapping x = (OplTyMapping) o;
			return "ty mapping " + c + " : " + x.src0 + " -> " + x.dst0;
		}
		if (o instanceof OplPres) {
			return "presentation " + c + " : " + ((OplPres)o).S;
 		}
		if (o instanceof OplSchema) {
			return "schema " + c + " : " + ((OplSchema)o).sig0;
 		}
		if (o instanceof OplQuery) {
			OplQuery q = (OplQuery) o;
			return "query " + c + " : " + q.src_e + " -> " + q.dst_e;
 		}
		if (o instanceof OplPresTrans) {
			OplPresTrans x = (OplPresTrans) o;
			return "transpres " + c + " : " + x.src0 + " -> " + x.dst0;
		}
		if (o instanceof OplInst) {
			OplInst oo = (OplInst) o;
			return "instance " + c + "=" + oo.P0 + " : " + oo.S0 + "," + oo.J0;
 		}
		return c;
	}


	public OplDisplay(String title, OplProgram p, OplEnvironment env, long start, long middle) {
		Map<Object, String> map = new HashMap<>();
		for (String c : p.order) {
			OplObject obj = env.get(c);
			map.put(obj, c); 
			try {
				frames.add(new Pair<>(doLookup(c, obj), obj.display()));
			} catch (Exception ex) {
				ex.printStackTrace();
				frames.add(new Pair<>(doLookup(c, obj), new CodeTextPanel(BorderFactory.createEtchedBorder(), "Exception", ex.getMessage())));
			}
		}
		long end = System.currentTimeMillis();
		int c1 = (int) ((middle - start) / (1000f));
		int c2 = (int) ((end - middle) / (1000f));
		display(title + " | (exec: " + c1 + "s)(gui: " + c2 + "s)", p.order);
	}
	
	JFrame frame = null;
	String name;
	List<Pair<String, JComponent>> frames = new LinkedList<>();

	final CardLayout cl = new CardLayout();
	final JPanel x = new JPanel(cl);
	final JList<String> yyy = new JList<>();
	final Map<String, String> indices = new HashMap<>();

	public void display(String s, List<String> order) {
		frame = new JFrame();
		this.name = s;

		final Vector<String> ooo = new Vector<>();
		int index = 0;
		for (Pair<String, JComponent> p : frames) {
			x.add(p.second, p.first);
			ooo.add(p.first);
			indices.put(order.get(index++), p.first);
		}
		x.add(new JPanel(), "blank");
		cl.show(x, "blank");

		yyy.setListData(ooo);
		JPanel temp1 = new JPanel(new GridLayout(1, 1));
		temp1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
				"Select:"));
		JScrollPane yyy1 = new JScrollPane(yyy);
		temp1.add(yyy1);
	//	temp1.setMinimumSize(new Dimension(200, 600));
	//	yyy.setPreferredSize(new Dimension(200, 600));
		yyy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		yyy.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = yyy.getSelectedIndex();
				if (i == -1) {
					cl.show(x, "blank");
				} else {
					cl.show(x, ooo.get(i).toString());
				}
			}

		});

		JPanel north = new JPanel(new GridLayout(1, 1));
	//	JButton saveButton = new JButton("Save GUI");
	//	north.add(saveButton);
	//	saveButton.setMinimumSize(new Dimension(10,10));
	//	saveButton.addActionListener(x -> GUI.save2(env));
		JSplitPane px = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//px.setResizeWeight(.8);
		px.setDividerLocation(200);
//		FQLSplit px = new FQLSplit(.5, JSplitPane.HORIZONTAL_SPLIT);
		px.setDividerSize(4);
		frame = new JFrame(/* "Viewer for " + */s);

		JSplitPane temp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		temp2.setResizeWeight(1);
		temp2.setDividerSize(0);
		temp2.setBorder(BorderFactory.createEmptyBorder());
		temp2.add(temp1);
		temp2.add(north);

		// px.add(temp1);
		px.add(temp2);

		px.add(x);

		// JPanel bd = new JPanel(new BorderLayout());
		// bd.add(px, BorderLayout.CENTER);
		// bd.add(north, BorderLayout.NORTH);

		// frame.setContentPane(bd);
		frame.setContentPane(px);
		frame.setSize(900, 600);

		ActionListener escListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		};

		frame.getRootPane().registerKeyboardAction(escListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK);
		frame.getRootPane().registerKeyboardAction(escListener, ctrlW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.getRootPane().registerKeyboardAction(escListener, commandW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	


}
