package catdata.aql;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import catdata.Ctx;
import catdata.Program;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.AqlMultiDriver;
import catdata.aql.exp.AqlParser;
import catdata.aql.exp.CombinatorParser;
import catdata.aql.exp.Exp;
import catdata.ide.CodeTextPanel;
import catdata.ide.Example;
import catdata.ide.Examples;
import catdata.ide.Language;

public class AqlTester {
	
	static String message = "This self-test will run all the built-in AQL examples and check for exceptions.  This test cannot be aborted.  This window will disappear for a while. Continue?";
	public static void doSelfTests() {
		int c = JOptionPane.showConfirmDialog(null, message, "Run Self-Test?", JOptionPane.YES_NO_OPTION);
		if (c != JOptionPane.YES_OPTION) {
			return;
		}
		Ctx<String, String> exs = new Ctx<>();
		for (Example e : Examples.getExamples(Language.AQL)) {
			exs.put(e.getName(), e.getText());
		}
		Ctx<String, Throwable> result = runMany(exs);
		if (result.isEmpty()) {
			JOptionPane.showMessageDialog(null, "OK: Tested Passed");
			return;
		}
		JTabbedPane t = new JTabbedPane();
		for (String k : result.keySet()) {
			t.addTab(k, new CodeTextPanel("Error", result.get(k).getMessage()));
		}
		JOptionPane.showMessageDialog(null, t);
	}

	private static Ctx<String, Throwable> runMany(Ctx<String, String> progs) {
		Ctx<String, Throwable> result = new Ctx<>();
		for (String k : progs.keySet()) {
			try {
				System.out.println(k);
				Program<Exp<?>> prog = AqlParser.getParser().parseProgram(progs.get(k));
				String[] toUpdate = new String[] { "" };
				AqlMultiDriver driver = new AqlMultiDriver(prog, toUpdate, null);
				driver.start(); //blocks
				AqlEnv env = driver.env;
				if (env.exn != null) {
					result.put(k, env.exn);
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
				result.put(k, ex);
			}
		}
		return result;
	}
	
}
