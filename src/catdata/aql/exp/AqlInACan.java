package catdata.aql.exp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Pair;
import catdata.Program;
import catdata.Util;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.InstExpRaw.Sk;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.gui.AqlViewer;
import catdata.ide.Example;
import catdata.ide.Examples;
import catdata.ide.Language;

//TODO aql merge aqldoc with aqlinacan
//TODO: aql have this execute pragmas?
class AqlInACan {

	private static String quote(String s) {
		s = s.replace("\\", "\\" + "\\"); //  \ --> \\
		s = s.replace("\"", "\\\""); // " --> \"
		s = s.replace("\n", "\\n"); // <LF> --> \n
		return s;
	}
	
	//TODO: AQL skipping feature will have to change
	private static List<Example> skip() {
		return new LinkedList<>(); // Util.list(AqlExamples.aqlKb, AqlExamples.aqlJdbc, AqlExamples.aqlMatch);
	}
	
	//
	
	@SuppressWarnings("unused")
	private static String makeHtml() {
		String s = "";
		String t = "";
		for (Example ex : Util.alphabetical(Examples.getExamples(Language.AQL))) {
			if (skip().contains(ex)) {
				continue;
			}
			s += "\nif (v == \"" + ex.getName() + "\") { document.getElementById('code').innerHTML = \"" + quote(ex.getText()) + "\" }"; 
			t += "\n<option value = \"" + ex.getName() + "\">" + ex.getName() + "</option>";
		}
		String html = 
				  "<html>"
				+ "\n<head>"
				+ "\n<title>Try AQL</title>"
				+ "\n</head>"
				+ "\n<script>"
		        + "\nfunction setExample(t) {"
		        + "\n    var v = t.value;"
		        + "\n" + s
		        + "\n}; "
		        + "\n</script>"
				+ "\n<body>"
				+ "Choose example:"
				+ "\n<select name=\"example\" onChange = \"setExample(this);\">"
				+ "\n<option disabled selected value> -- select an option -- </option>"
				+ "\n" + t
				+ "\n</select>"
				+ "\n<br>"
				+ "\nEnter AQL code here:"
				+ "\n<form action=\"cgi-bin/try.cgi\""
				+ "\n      method=\"POST\">"
				+ "\n<textarea name=\"code\" id=\"code\" cols=80 rows=40>"
				+ "\n</textarea>"
				+ "\n<br>"
				+ "\n<input type=\"submit\" value=\"Run\">"
				+ "\n</form>"
				+ "\n</body>"
				+ "\n</html>"
				+ "\n";
		return html;
	}

	public static void main(String... args) {
//		System.out.println(makeHtml());
		System.out.println(openCan(args[0]));
	}
	
	private static String openCan(String can) {
		try {
			Program<Exp<?>> program = AqlParser.getParser().parseProgram(can);
			String html = "<html><head><script src=\"http://categoricaldata.net/js/simple.js\"></script><title>Result</title></head><body>\n\n";
			AqlEnv env = new AqlEnv(program);
			env.typing = new AqlTyping(program, env.defaults, false);	
			for (String n : program.order) {
				Exp<?> exp = program.exps.get(n);
				if (exp.kind() == Kind.PRAGMA) {
					throw new RuntimeException("Pragmas disabled in web-AQL");
				}
				Object o = Util.timeout(() -> exp.eval(env), 10 * 1000); //hardcode timeout, do not exec pragmas
				env.defs.put(n, exp.kind(), o);
				if (exp.kind().equals(Kind.INSTANCE)) {
					html += "<p><h2>" + n + " =\n</h2>" + toHtml(env, (Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ?, ?>) o) 
						+ "\n</p><br><hr>\n";
				} 
				//TODO aql revisit if this should print html or javascript graphs
				//for example, could actually call HTML maker thingy, although security implication
			}
			return html + "\n\n</body></html>";
		} catch (Throwable ex) {
			ex.printStackTrace();
			return "ERROR " + ex.getMessage();
		}
	}
	
	private static int i = 0;

	public static <X,Y> String toHtml(AqlEnv env, Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I) {
		String ret = "<div>";
		
		Map<En, Pair<List<String>,Object[][]>> tables = new AqlViewer(256, env).makeEnTables(I.algebra()); //TODO aql hardcoded
		
		for (En t : Util.alphabetical(tables.keySet())) {
			ret += "<table id=\"table" + i + "\" style=\"float: left; border: 1px solid black; padding: 5px; border-collapse: collapse; margin-right:10px\" border=\"1\"  cellpadding=\"3\">";
			ret += "<caption><b>" + t.toString() + "</b></caption>";
			List<String> cols = tables.get(t).first;
			cols.remove(0);
			cols.add(0, "ID");
			Object[][] rows = tables.get(t).second;
			ret += "<tr>";
			int j = 0;
			for (String col : cols) {
				ret += "<th onclick=\"sortTable('table" + i + "', " + j + ")\">" + col + "</th>";
				j++;
			}
			ret += "</tr>";
			for (Object[] row : rows) {
				ret += "<tr>";
				for (Object col : row) {
				//	System.ou
					ret += "<td>" + strip(col.toString()) + "</td>";
				}
				ret += "</tr>";
			}
			ret += "</table>";
			i++;
		} 
		return ret + "</div><br style=\"clear:both;\"/>"; 
	}

	private static String strip(String s) {
		s = s.replace("<", "");
		s = s.replace(">", "");
		s = s.replace("[", "");
		s = s.replace("]", "");
		s = s.replace("|", "");
		return s;
	}
	

}
