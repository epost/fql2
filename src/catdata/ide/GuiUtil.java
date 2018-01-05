package catdata.ide;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import catdata.Util;

public class GuiUtil {

	public static void show(JComponent p, int w, int h, String title) {
		JFrame f = new JFrame(title);
		f.setContentPane(p);
		f.pack();
		if (w > 0 && h > 0) {
			f.setSize(new Dimension(w, h));
		}
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	

	private static JPanel makeRowOrCol(List<JComponent> list, int orientation) {
		if (list.isEmpty()) {
			JPanel ret = new JPanel();
			ret.setBorder(BorderFactory.createEmptyBorder());
			return ret;
		}
		JPanel ret = new JPanel(new GridLayout(1, 1));
		if (list.size() == 1) {
			ret.add(list.get(0));
			return ret;
		}
		Iterator<JComponent> it = list.iterator();
		JComponent sofar = it.next();
		double n = 2;
		while (it.hasNext()) {
			JSplitPane jsp = new JSplitPane(orientation);
			jsp.setResizeWeight(1.0d / n);
			jsp.add(sofar);
			jsp.add(it.next());
			jsp.setDividerSize(2);
			jsp.setBorder(BorderFactory.createEmptyBorder());
			sofar = jsp;
			n++;
		}
		ret.add(sofar);
		return ret;
	}
	
	public static JPanel makeGrid(List<JComponent> list) {
		JPanel ret = new JPanel(new GridLayout(list.size(), 1));
		
		for (JComponent x : list) {
			JScrollPane jsp = new JScrollPane(x);
			/*
			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill=GridBagConstraints.VERTICAL;
			c.gridx = 0;
			c.gridy = 0;
			p.add(jsp, c); */
			JPanel p = new JPanel(new GridLayout(1,1));
			p.add(jsp);
			ret.add(p);
		}

		JScrollPane jsp = new JScrollPane(ret);
		JPanel p = new JPanel(new GridLayout(1,1));
		p.add(jsp);
		return p;
	} 
/*
	public static JPanel makeGrid(List<JComponent> list) {
		int n = (int) Math.ceil(Math.sqrt(list.size()));

		List<JComponent> list2 = new LinkedList<>();
		for (int i = 0; i < list.size(); i += n) {
			int end = Math.min(list.size(), i + n);
			list2.add(makeRowOrCol(list.subList(i, end), JSplitPane.HORIZONTAL_SPLIT));
		}

		JScrollPane jsp = new JScrollPane(makeRowOrCol(list2, JSplitPane.VERTICAL_SPLIT));
		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.add(jsp);
		return ret;
	}  */

	/*public static JPanel makeTable(Border b, String border, Object[][] rowData, Object... colNames) {
		return makeTable(null, b, border, rowData, colNames);
	}*/

	@SuppressWarnings("serial")
	public static JPanel makeTable(Border b, String border, Object[][] rowData, Object... colNames) {
		JTable t = new JTable(rowData, colNames) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		/*if (f != null) {
			t.setFont(f);
		}*/
		JPanel p = new JPanel(new GridLayout(1, 1));
		TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());
		if (colNames.length > 0) {
			sorter.toggleSortOrder(0);
		}
		t.setRowSorter(sorter);
		sorter.allRowsChanged();
		p.add(new JScrollPane(t));
		
		for (int row = 0; row < t.getRowCount(); row++) {
			int rowHeight = t.getRowHeight();

			for (int column = 0; column < t.getColumnCount(); column++) {
				Component comp = t.prepareRenderer(t.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			t.setRowHeight(row, rowHeight);
		}

		Font font = UIManager.getFont("TableHeader.font");
		p.setBorder(BorderFactory.createTitledBorder(b, border, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
		return p;

	}

	@SuppressWarnings("serial")
	//TODO aql merge with other makeTable method
	public static JPanel makeBoldHeaderTable(Collection<String> atts, Border b, String border, Object[][] rowData, String... colNames) {
		JTable t = new JTable(rowData, colNames) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		/*if (f != null) {
			t.setFont(f);
		}*/
		// PlusMinusCellRenderer r = new PlusMinusCellRenderer();
		// t.setDefaultRenderer(Object.class, r);
		// t.setDefaultEditor(Object.class, r);
		// t.setModel(new NonEditableModel(rowData, colNames));
		// t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel p = new JPanel(new GridLayout(1, 1));
		TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());
		if (colNames.length > 0) {
			sorter.toggleSortOrder(0);
		}
		t.setRowSorter(sorter);
		sorter.allRowsChanged();
		p.add(new JScrollPane(t));

		for (int row = 0; row < t.getRowCount(); row++) {
			int rowHeight = t.getRowHeight();

			for (int column = 0; column < t.getColumnCount(); column++) {
				Component comp = t.prepareRenderer(t.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			t.setRowHeight(row, rowHeight);
		}

		p.setBorder(BorderFactory.createTitledBorder(b, border));
		// t.getTableHeader().set
		for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
			TableColumn col = t.getColumnModel().getColumn(i);

			col.setHeaderRenderer(new BoldifyingColumnHeaderRenderer(atts, t.getTableHeader().getDefaultRenderer()));
		}
		
		Font font = UIManager.getFont("TableHeader.font");
		p.setBorder(BorderFactory.createTitledBorder(b, border, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));


		return p;

	}
	
	public static void centerLineInScrollPane(JTextComponent component)
	{
		Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

		if (container == null) return;

		try
		{
			Rectangle r = component.modelToView(component.getCaretPosition());
			JViewport viewport = (JViewport)container;
			int extentHeight = viewport.getExtentSize().height;
			int viewHeight = viewport.getViewSize().height;
			if (r == null || viewport == null) {
				return;
			}
			int y = Math.max(0, r.y - ((extentHeight - r.height) / 2));
			y = Math.min(y, viewHeight - extentHeight);

			viewport.setViewPosition(new Point(0, y));
		}
		catch(BadLocationException ble) {}
	}
	
	public static String readFile(InputStream file) {
		Util.assertNotNull(file);
		try (InputStreamReader r = new InputStreamReader(file)) {

			return Util.readFile(r);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	}

	public static String readFile(String file) {
		try (FileReader r = new FileReader(file)) {
			return Util.readFile(r);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	}

	public static String readFile(File file) {
		try (FileReader r = new FileReader(file)) {
			return Util.readFile(r);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	} 
	

	public static JList<String> makeList() {
		JList<String> list = new JList<String>() {
			private static final long serialVersionUID = 1L;
	
			@Override
			public int locationToIndex(Point location) {
				int index = super.locationToIndex(location);
				if (index != -1 && !getCellBounds(index, index).contains(location)) {
					return -1;
				} else {
					return index;
				}
			}
		};
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return list;
	}

}
