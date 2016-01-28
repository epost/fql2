package fql_lib.X;

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
import fql_lib.core.Disp;

public class XDisplay implements Disp {
	
	private static String doLookup(String c, XObject obj, Map<Object, String> map, XProgram prog) {
		String front = obj.kind() + " " + c;
		if (obj instanceof XCtx) {
			XCtx ctx = (XCtx) obj;
			if (ctx.global == null) {
				throw new RuntimeException();
			} else if (ctx.schema == null) {
				return front;
			} else {
				String s = map.get(ctx.schema);
				if (s == null) {
					s = prog.getType(c).first.toString();
				}
				return front + " : " + s;
			}
		} else if (obj instanceof XMapping) {
			XMapping m = (XMapping) obj;
			String s = null;
			String t = null;
			if (map.containsKey(m.src)) {
				s = map.get(m.src);
			}
			if (map.containsKey(m.dst)) {
				t = map.get(m.dst);
			} 
			if (s == null) {
				s = prog.getType(c).first.toString();
			}
			if (t == null) {
				t = prog.getType(c).second.toString();
			}
			return front + " : " + s + " -> " + t;
		} else if (obj instanceof XPoly) {
			XPoly m = (XPoly) obj;
			String s = null;
			String t = null;
			if (map.containsKey(m.src)) {
				s = map.get(m.src);
			}
			if (map.containsKey(m.dst)) {
				t = map.get(m.dst);
			}
			if (s == null) {
				s = prog.getType(c).first.toString();
			}
			if (t == null) {
				t = prog.getType(c).second.toString();
			}
			return front + " : " + s + " -> " + t;
		}
		
		return front; 
		
	}

	public XDisplay(String title, XProgram p, XEnvironment env, long start, long middle) {
		Map<Object, String> map = new HashMap<>();
		for (String c : p.order) {
			XObject obj = env.objs.get(c);
			map.put(obj, c);
			if (obj instanceof XString) {
				continue;
			}
			if (obj instanceof XPoly) {
				//TODO
//				((XPoly)obj).validate();
			continue;
			}
			frames.add(new Pair<>(doLookup(c, obj, map, p), obj.display()));
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

	@Override
	public void close() {
	}
/*	
	public void showInstanceFlow(Graph<String, Object> g) {
		final JFrame f = new JFrame();

		ActionListener escListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		};
		f.getRootPane().registerKeyboardAction(escListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.META_MASK);
		f.getRootPane().registerKeyboardAction(escListener, ctrlW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		f.getRootPane().registerKeyboardAction(escListener, commandW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		if (g.getVertexCount() == 0) {
			f.add(new JPanel());
		} else {
			f.add(doView(g));
		}
		f.setSize(600, 540);
		f.setTitle("Instance Dependence Graph for " + name);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public void showSchemaFlow(Graph<String, Object> g) {
		final JFrame f = new JFrame();

		ActionListener escListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		};
		f.getRootPane().registerKeyboardAction(escListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.META_MASK);
		f.getRootPane().registerKeyboardAction(escListener, ctrlW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		f.getRootPane().registerKeyboardAction(escListener, commandW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		if (g.getVertexCount() == 0) {
			f.add(new JPanel());
		} else {
			f.add(doView2(g));
		}
		f.setSize(600, 540);
		f.setTitle("Schema Mapping Graph for " + name);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
**/	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public JComponent doView(
	/* final Environment env, *///Graph<String, Object> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new FRLayout(sgv);
/*
		try {
//			 Class<?> c = Class.forName(DEBUG.layout_prefix
//			 + DEBUG.debug.instFlow_graph);
//			 Constructor<?> x = c.getConstructor(Graph.class);
//			 Layout<String, Object> layout = (Layout<String, Object>) x
//			 .newInstance(sgv);
			Layout<String, Object> layout = new ISOMLayout<>(sgv);

			// Layout<String, String> layout = new CircleLayout(sgv);
			layout.setSize(new Dimension(600, 540));
			final VisualizationViewer<String, Object> vv = new VisualizationViewer<>(
					layout);
			Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
				public Paint transform(String i) {
					return prog.nmap.get(i);
				}
			};
			DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
			gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			vv.setGraphMouse(gm);
			gm.setMode(Mode.PICKING);
			// Set up a new stroke Transformer for the edges
			// float dash[] = { 1.0f };
			// final Stroke edgeStroke = new BasicStroke(0.5f,
			// BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
			// 10.0f);
			// Transformer<String, Stroke> edgeStrokeTransformer = new
			// Transformer<String, Stroke>() {
			// public Stroke transform(String s) {
			// return edgeStroke;
			// }
			// };
			// final Stroke bs = new BasicStroke();
			// Transformer<String, Stroke> edgeStrokeTransformer = new
			// Transformer<String, Stroke>() {
			// public Stroke transform(String s) {
			// if (isAttribute(s)) {
			// return edgeStroke;
			// }
			// return bs;
			// }
			// };
			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
			// vv.getRenderContext().setEdgeStrokeTransformer(
			// edgeStrokeTransformer);
			// vv.getRenderContext().setVertexLabelTransformer(
			// new ToStringLabeller<String>());
		//	vv.getRenderContext().setVertexLabelRenderer(new MyVertexT());
			//vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.black));

			vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
					
			vv.getRenderContext().setEdgeLabelTransformer(
					new Transformer() {

						@Override
						public Object transform(Object arg0) {
							return ((Pair<?,?>)arg0).second.toString();
						} 
						
					});
			
			vv.getPickedVertexState().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() != ItemEvent.SELECTED) {
						return;
					}
					vv.getPickedEdgeState().clear();
					String str = ((String) e.getItem());
					yyy.setSelectedValue(indices.get(str), true);
				}
				
			});
			
			vv.getPickedEdgeState().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() != ItemEvent.SELECTED) {
						return;
					}
					vv.getPickedVertexState().clear();
					Object o = ((Pair<?,?>) e.getItem()).second;
			//		handleInstanceFlowEdge(o);
				}
				
			});
			
			// new ToStringLabeller<String>());
			// vv.getRenderer().getVertexRenderer().
			 vv.getRenderContext().setLabelOffset(20);
			//vv.getRenderer().getEdgeLabelRenderer().setPosition(Position.CNTR);
			// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

			/*
			 * vv.getRenderContext().setVertexLabelTransformer( new
			 * ToStringLabeller<String>() {
			 * 
			 * @Override public String transform(String t) { // if
			 * (isAttribute(t)) { // return getTypeLabel(t); // } return t; }
			 * 
			 * });
			 */
/*
			GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
			JPanel ret = new JPanel(new GridLayout(1, 1));
//			ret.add(zzz);
	//		ret.setBorder(BorderFactory.createEtchedBorder());
			return ret;
		} catch (Throwable cnf) {
		//	cnf.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	@SuppressWarnings("unchecked")
	public JComponent doView2(
			/* final Environment env, */ //Graph<String, Object> sgv) {
				// Layout<V, E>, BasicVisualizationServer<V,E>
				// Layout<String, String> layout = new FRLayout(sgv);

	/*			try {
//					 Class<?> c = Class.forName(DEBUG.layout_prefix
//					 + DEBUG.debug.schFlow_graph);
//					 Constructor<?> x = c.getConstructor(Graph.class);
//					 Layout<String, Object> layout = (Layout<String, Object>) x
//					 .newInstance(sgv);
					Layout<String, Object> layout = new ISOMLayout<>(sgv);

					// Layout<String, String> layout = new CircleLayout(sgv);
					layout.setSize(new Dimension(600, 540));
					final VisualizationViewer<String, Object> vv = new VisualizationViewer<>(
							layout);
					Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
						public Paint transform(String i) {
							return prog.smap(new SigExp.Var(i));
						}
					};
					DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
					gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
					vv.setGraphMouse(gm);
					gm.setMode(Mode.PICKING);
					// Set up a new stroke Transformer for the edges
					// float dash[] = { 1.0f };
					// final Stroke edgeStroke = new BasicStroke(0.5f,
					// BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
					// 10.0f);
					// Transformer<String, Stroke> edgeStrokeTransformer = new
					// Transformer<String, Stroke>() {
					// public Stroke transform(String s) {
					// return edgeStroke;
					// }
					// };
					// final Stroke bs = new BasicStroke();
					// Transformer<String, Stroke> edgeStrokeTransformer = new
					// Transformer<String, Stroke>() {
					// public Stroke transform(String s) {
					// if (isAttribute(s)) {
					// return edgeStroke;
					// }
					// return bs;
					// }
					// };
					vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
					// vv.getRenderContext().setEdgeStrokeTransformer(
					// edgeStrokeTransformer);
					 vv.getRenderContext().setEdgeLabelTransformer(
					 new ToStringLabeller<Object>());
					 vv.getRenderContext().setVertexLabelTransformer(
					 new ToStringLabeller<String>());
				//	vv.getRenderContext().setVertexLabelRenderer(new MyVertexT());
					//vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.black));

						vv.getPickedVertexState().addItemListener(new ItemListener() {

							@Override
							public void itemStateChanged(ItemEvent e) {
								if (e.getStateChange() != ItemEvent.SELECTED) {
									return;
								}
								vv.getPickedEdgeState().clear();
								String str = ((String) e.getItem());
								yyy.setSelectedValue(indices.get(str), true);
							}
							
						});
						
						vv.getPickedEdgeState().addItemListener(new ItemListener() {

							@Override
							public void itemStateChanged(ItemEvent e) {
								if (e.getStateChange() != ItemEvent.SELECTED) {
									return;
								}
								vv.getPickedVertexState().clear();
								String str = ((String) e.getItem());
								yyy.setSelectedValue(indices.get(str), true);

							}
							
						});
					
					// new ToStringLabeller<String>());
					// vv.getRenderer().getVertexRenderer().
					 vv.getRenderContext().setLabelOffset(20);
					//vv.getRenderer().getEdgeLabelRenderer().setPosition(Position.CNTR);
					// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

					/*
					 * vv.getRenderContext().setVertexLabelTransformer( new
					 * ToStringLabeller<String>() {
					 * 
					 * @Override public String transform(String t) { // if
					 * (isAttribute(t)) { // return getTypeLabel(t); // } return t; }
					 * 
					 * });
					 */
/*
					GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
					JPanel ret = new JPanel(new GridLayout(1, 1));
					ret.add(zzz);
					ret.setBorder(BorderFactory.createEtchedBorder());
					return ret;
				} catch (Throwable cnf) {
					cnf.printStackTrace();
					throw new RuntimeException();
				}

			} */

			
}
