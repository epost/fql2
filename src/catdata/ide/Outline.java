package catdata.ide;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import catdata.Prog;
import catdata.Unit;
import catdata.Util;

public abstract class Outline<Progg extends Prog, Env, DDisp extends Disp> {

	protected final CodeEditor<Progg, Env, DDisp> codeEditor;
	
	final JPanel p = new JPanel(new GridLayout(1, 1));
	
	protected abstract JComponent getComp();
	
	protected abstract void setComp(List<String> order);
	

	public void build() {
		synchronized (this.codeEditor.parsed_prog_lock) {
			if (this.codeEditor.parsed_prog == null) {
				return;
			}
			List<String> set = new LinkedList<>(this.codeEditor.parsed_prog.keySet());
			if (this.codeEditor.outline_alphabetical) {
				set.sort(Util.AlphabeticalComparator);
			}
			setComp(set);
		}
		this.codeEditor.revalidate();
	}

	 protected Outline(CodeEditor<Progg, Env, DDisp> codeEditor) {
		this.codeEditor = codeEditor;
		JScrollPane jsp = new JScrollPane(getComp());
		jsp.setBorder(BorderFactory.createEmptyBorder());
		p.add(jsp);
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Outline"));
		
		this.codeEditor.parsed_prog_lock = new Unit();
		this.codeEditor.parsed_prog_string = "";
//		build();
		
		p.setMinimumSize(new Dimension(0, 0));
	
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						Thread.sleep(codeEditor.sleepDelay);
					} catch (Throwable e1) {
						return;
					}
					if (Outline.this.codeEditor.isClosed) {
						return;
					}
					
					String s = Outline.this.codeEditor.topArea.getText();
					try {
						if (!s.equals(Outline.this.codeEditor.parsed_prog_string)) {
							Progg e = Outline.this.codeEditor.parse(s);
							if (!equiv(e, Outline.this.codeEditor.parsed_prog)) {
								if (System.currentTimeMillis() - Outline.this.codeEditor.last_keystroke > codeEditor.sleepDelay) {
									synchronized (Outline.this.codeEditor.parsed_prog_lock) {
										Outline.this.codeEditor.parsed_prog = e;
										Outline.this.codeEditor.parsed_prog_string = s;
									}
									build();
									Outline.this.codeEditor.clearSpellCheck(); //morally, should have its own thread, but meh
								}
							}
						}
					} catch (Exception ex) {
					}
					
					
				}
			}

			
		});
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();

		
	}
	 
	 @SuppressWarnings("unused")
	protected  boolean equiv(Progg now, Progg then) {
			return false;
		}

	public void setFont(Font font) {
		getComp().setFont(font);
	}

}