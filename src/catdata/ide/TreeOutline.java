package catdata.ide;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import catdata.Program;
import catdata.aql.Kind;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.Exp;
import catdata.aql.gui.AqlDisplay;

public class TreeOutline extends Outline<Program<Exp<?>>, AqlEnv, AqlDisplay> {

	private DefaultMutableTreeNode makeTree(List<String> set, Program<Exp<?>> prog, boolean prefix) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		for (String k : set) {
			Exp<?> e = prog.exps.get(k);
			if (e.kind().equals(Kind.COMMENT)) {
				continue;
			}
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject(prefix ? e.kind() + " " + k : k);
			e.asTree(n);
			root.add(n);
		}
		return root;
	}

	JTree tree;

	protected synchronized JTree getComp() {
		if (tree != null) {
			return tree;
		}
		tree = new JTree(new DefaultMutableTreeNode());
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(makeRenderer());
		tree.setEditable(true);

		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

			if (node == null) {
				return;
			}
			String s = node.getUserObject().toString();
			if (this.codeEditor.outline_prefix_kind) {
				s = s.substring(s.indexOf(" ") + 1, s.length());
			}
			if (codeEditor.parsed_prog.exps.containsKey(s)) {
				Integer line = codeEditor.parsed_prog.getLine(s);
				codeEditor.setCaretPos(line);
			}

		});

		tree.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				//tree.clearSelection();
			}
		});

		return tree;
	}

	private DefaultTreeCellRenderer makeRenderer() {
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		return renderer;
	}

	TreePath conv(TreePath path) {
		TreePath parent = path.getParentPath();
		if (parent == null) {
			return new TreePath(getComp().getModel().getRoot());
		}
		TreePath rest = conv(parent);
		DefaultMutableTreeNode last = (DefaultMutableTreeNode) rest.getLastPathComponent();
		DefaultMutableTreeNode us = (DefaultMutableTreeNode) path.getLastPathComponent();
		Enumeration<DefaultMutableTreeNode> cs = last.children();
		while (cs.hasMoreElements()) {
			DefaultMutableTreeNode m = cs.nextElement();
			if (nodeEq(m, us)) {
				return rest.pathByAddingChild(m);
			}
		}
		return null;
	}

	private boolean nodeEq(DefaultMutableTreeNode m, DefaultMutableTreeNode n) {
		if (!n.getUserObject().equals(m.getUserObject())) {
			return false;
		}
		if (m.getChildCount() != n.getChildCount()) {
			return false;
		}
		Enumeration<DefaultMutableTreeNode> e1 = m.children();
		Enumeration<DefaultMutableTreeNode> e2 = m.children();
		while (e1.hasMoreElements()) {
			boolean b = nodeEq(e1.nextElement(), e2.nextElement());
			if (!b) {
				return false;
			}
		}
		return true;
	}

	protected void setComp(List<String> set) {
		TreePath t1 = getComp().getSelectionPath();

		Enumeration<TreePath> p = getComp().getExpandedDescendants(new TreePath(getComp().getModel().getRoot()));

		getComp().setModel(new DefaultTreeModel(makeTree(set, codeEditor.parsed_prog, codeEditor.outline_prefix_kind)));
		tree.setCellRenderer(makeRenderer());

		while (p.hasMoreElements()) {
			TreePath path = p.nextElement();

			try {
				if (conv(path) != null) {
					getComp().expandPath(conv(path));
				}
			} catch (Exception ex) {
			}
		}

		if (t1 != null) {
			TreePath t2 = conv(t1);
			if (t2 != null) {
				getComp().setSelectionPath(t2);
				getComp().scrollPathToVisible(t2);
			}
		}

	}

	public TreeOutline(CodeEditor<Program<Exp<?>>, AqlEnv, AqlDisplay> codeEditor) {
		super(codeEditor);

	}

}