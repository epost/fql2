package fql_lib.opl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import catdata.algs.Pair;
import fql_lib.core.DEBUG;
import fql_lib.core.MyTableRowSorter;
import fql_lib.core.Util;
import fql_lib.opl.OplExp.NonEditableModel;

public class JSWrapper {
	public Object o;

	public JSWrapper(Object o) {
		this.o = o;
	}

	public String toString() {
		String s = "JS<" + o + ">";
		if (!DEBUG.debug.opl_pretty) {
			return s;
		}
		return o.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((o == null) ? 0 : o.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JSWrapper other = (JSWrapper) obj;
		if (o == null) {
			if (other.o != null)
				return false;
		} else if (!o.equals(other.o))
			return false;
		return true;
	}

	public static JComponent extract(Object t0) {
		if (t0 instanceof OplTerm) {
			OplTerm<?, ?> t = (OplTerm<?, ?>) t0;
			if (t.args.size() == 0) {
				Object o = Util.stripChcs(t.head).second;
				if (o instanceof JSWrapper) {
					JSWrapper w = (JSWrapper) o;
					if (w.o instanceof JComponent) {
						return (JComponent) w.o;
					} else if (w.o instanceof OplObject) {
						return ((OplObject) w.o).display();
					}
				}
			}
		}
		if (t0 instanceof JComponent) {
			return (JComponent) t0;
		}
		return null;
	}
	
	static JPanel makePrettyTables(Border b, String border, Object[][] rowData, String[] colNames) {

		@SuppressWarnings("serial")
		JTable t = new JTable() {
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		PlusMinusCellRenderer r = new PlusMinusCellRenderer();
		t.setDefaultRenderer(Object.class, r);
		t.setDefaultEditor(Object.class, r);
		t.setModel(new NonEditableModel(rowData, colNames));
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
		return p;

	} 

	@SuppressWarnings("serial")
	static class PlusMinusCellRenderer extends DefaultCellEditor implements TableCellRenderer {

		public PlusMinusCellRenderer() {
			super(new JTextField());
		}

		Map<Pair<Integer, Integer>, Component> cache = new HashMap<>();

		public Component getTableCellRendererComponent(final JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Pair<Integer, Integer> p = new Pair<>(row, column);
			Component ret = cache.get(p);
			if (ret != null) {
				return ret;
			}
			if (ret == null) {
				ret = JSWrapper.extract(value);
				if (ret != null) {
					cache.put(p, ret);
					return ret;
				}
			}

			ret = new DefaultTableCellRenderer().getTableCellRendererComponent(table,
					OplExp.strip(value.toString()), false, hasFocus, row, column);
			return ret;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {

			Pair<Integer, Integer> p = new Pair<>(row, column);
			Component ret = cache.get(p);
			if (ret != null) {
				return ret;
			}
			if (ret == null) {
				ret = JSWrapper.extract(value);
				if (ret != null) {
					cache.put(p, ret);
					return ret;
				}
			}

			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}

}