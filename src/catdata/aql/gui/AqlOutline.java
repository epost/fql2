package catdata.aql.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import catdata.Program;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Kind;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.AqlTyping;
import catdata.aql.exp.Exp;
import catdata.aql.exp.InteriorLabel;
import catdata.aql.exp.Raw;
import catdata.ide.CodeEditor;
import catdata.ide.Outline;

public class AqlOutline extends Outline<Program<Exp<?>>, AqlEnv, AqlDisplay> {

	private class TreeLabel {
		private final String s;

		private final boolean prefix;

		private final AqlTyping G;

		private final boolean useTypes;

		public TreeLabel(String s, boolean prefix, AqlTyping G, boolean useTypes) {
			super();
			this.s = s;
			this.prefix = prefix;
			this.G = G;
			this.useTypes = useTypes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			TreeLabel other = (TreeLabel) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			return true;
		}

		private AqlOutline getOuterType() {
			return AqlOutline.this;
		}

		public String toString() {
			Kind k = G.prog.exps.get(s).kind();
			if (useTypes) {
				return AqlDisplay.doLookup(prefix, s, k, G);
			}
			return prefix ? k + s : s;
		}

	}

	private DefaultMutableTreeNode makeTree(List<String> set, Program<Exp<?>> prog, boolean prefix, boolean alpha,
			boolean useTypes) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		AqlTyping G = new AqlTyping(prog, new AqlOptions(prog.options, null, AqlOptions.initialOptions), true);
		for (String k : set) {
			Exp<?> e = prog.exps.get(k);
			if (e.kind().equals(Kind.COMMENT)) {
				continue;
			}
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject(new TreeLabel(k, prefix, G, useTypes));
			asTree(n, alpha, e);
			root.add(n);
		}
		return root;
	}

	private void asTree(DefaultMutableTreeNode root, boolean alpha, Exp<?> e) {
		if (e instanceof Raw) {
			Raw T = (Raw) e;
			for (String k : T.raw().keySet()) {
				List<InteriorLabel<Object>> v = T.raw().get(k);
				add(root, v, k, t -> t, alpha);
			}
		}

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
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				if (node == null) {
					return;
				}
				Object o = node.getUserObject();
				if (o instanceof TreeLabel) {
					TreeLabel l = (TreeLabel) o;
					if (codeEditor.parsed_prog.exps.containsKey(l.s)) {
						Integer line = codeEditor.parsed_prog.getLine(l.s);
						codeEditor.setCaretPos(line);
						codeEditor.addToHistory(line);
					}
				} else if (o instanceof InteriorLabel) {
					InteriorLabel<?> l = (InteriorLabel<?>) o;
					codeEditor.setCaretPos(l.loc);
					codeEditor.addToHistory(l.loc);
				} /* else if (node.getChildCount() != 0) {
					DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(0);
					if (n.getUserObject() instanceof InteriorLabel) {
						InteriorLabel<?> lx = (InteriorLabel<?>) n.getUserObject();
						codeEditor.setCaretPos(lx.loc);
						codeEditor.addToHistory(lx.loc);
					}
				} */

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

	@SuppressWarnings("unchecked")
	TreePath conv(TreePath path) {
		TreePath parent = path.getParentPath();
		if (parent == null) {
			return new TreePath(getComp().getModel().getRoot());
		}
		TreePath rest = conv(parent);
		DefaultMutableTreeNode last = (DefaultMutableTreeNode) rest.getLastPathComponent();
		DefaultMutableTreeNode us = (DefaultMutableTreeNode) path.getLastPathComponent();
		Enumeration<TreeNode> cs = last.children();
		if (cs == null) {
			return null;
		}
		while (cs.hasMoreElements()) {
			DefaultMutableTreeNode m = (DefaultMutableTreeNode) cs.nextElement();
			if (nodeEq(m, us)) {
				return rest.pathByAddingChild(m);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean nodeEq(DefaultMutableTreeNode m, DefaultMutableTreeNode n) {
		if (!n.getUserObject().equals(m.getUserObject())) {
			return false;
		} 
		if (m.getChildCount() != n.getChildCount()) {
			return false;
		}
		Enumeration<TreeNode> e1 = m.children();
		Enumeration<TreeNode> e2 = m.children();
		if (e1 == null && e2 == null) {
			return true;
		}
		if (e1 == null || e2 == null) {
			return false;
		}
		while (e1.hasMoreElements()) {
			boolean b = nodeEq((DefaultMutableTreeNode)e1.nextElement(), (DefaultMutableTreeNode)e2.nextElement());
			if (!b) {
				return false;
			}
		}
		return true;
	}

	protected void setComp(List<String> set) {
		TreePath t1 = getComp().getSelectionPath();

		Enumeration<TreePath> p = getComp().getExpandedDescendants(new TreePath(getComp().getModel().getRoot()));

		getComp().setModel(new DefaultTreeModel(makeTree(set, codeEditor.parsed_prog, codeEditor.outline_prefix_kind,
				codeEditor.outline_alphabetical, codeEditor.outline_types)));
		tree.setCellRenderer(makeRenderer());
		if (p == null) {
			return;
		}
		while (p.hasMoreElements()) {
			try {
				TreePath path = p.nextElement();
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

	@Override
	protected boolean equiv(Program<Exp<?>> now, Program<Exp<?>> then) {
		return false; //heavy, but positions in the text file change all the time
		/* if (then == null) {
			return false;
		}
		Set<String> nows = now.keySet().stream().filter(x -> now.exps.get(x).kind() != Kind.COMMENT)
				.collect(Collectors.toSet());
		Set<String> thens = then.keySet().stream().filter(x -> then.exps.get(x).kind() != Kind.COMMENT)
				.collect(Collectors.toSet());
		if (!nows.equals(thens)) {
			return false;
		}
		for (String k : nows) {
			Exp<?> e1 = now.exps.get(k);
			Exp<?> e2 = then.exps.get(k);
			if (!e1.equals(e2)) {
				return false;
			}
		}
		return true; */
	}

	public AqlOutline(CodeEditor<Program<Exp<?>>, AqlEnv, AqlDisplay> codeEditor) {
		super(codeEditor);

	}

	private <X, Y, Z> void add(DefaultMutableTreeNode root, Collection<X> x, Y y, Function<X, Z> f, boolean alpha) {
		if (x.size() > 0) {
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject(y);
			for (X t : Util.alphaMaybe(alpha, x)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(f.apply(t));
				if (t instanceof Exp) {
					asTree(m, alpha, (Exp<?>) t);
				} else if (t instanceof InteriorLabel) {
					InteriorLabel<?> l = (InteriorLabel<?>) t;
					if (l.s instanceof Exp) {
						asTree(m, alpha, (Exp<?>) l.s);
					} 
				}
				n.add(m);
			}
			root.add(n);
		}
	}

	
}