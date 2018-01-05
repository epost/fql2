package catdata.aql;

import java.io.File;
import java.io.FileReader;

import catdata.Program;
import catdata.Util;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.AqlMultiDriver;
import catdata.aql.exp.AqlParser;
import catdata.aql.exp.CombinatorParser;
import catdata.aql.exp.Exp;

/**
 * Program entry point for Cody.  
 */
public class CodyCmdLine {
	
	
	//args[0] = input AQL program
	//args[1] = directory for tptp output
	public static void main(String[] args) {
		try (FileReader r = new FileReader(args[0])) {
			String str = Util.readFile(r);
			Program<Exp<?>> prog = AqlParser.getParser().parseProgram(str);
			
			String t[] = new String[1]; //poll for driver status
			AqlMultiDriver driver = new AqlMultiDriver(prog, t, null);
			driver.start(); //blocks
			AqlEnv env = driver.env; 
			if (env.exn != null) {
				throw env.exn; 
			}
			
			File dir = new File(args[1]);
			dir.mkdirs();
			for (String k : env.defs.tys.keySet()) {
				TypeSide<?,?> ts = env.defs.tys.get(k);
				File f = new File(dir, k + ".tptp");
				String s = ts.collage().toKB().tptp(true); //maedmax is unsound with empty sorts, but true here proceeds anyway
				Util.writeFile(s, f.getAbsolutePath());
			}
			for (String k : env.defs.insts.keySet()) {
				Instance ts = env.defs.insts.get(k);
				File f = new File(dir, k + ".tptp");
				String s = ts.collage().toKB().tptp(true); //maedmax is unsound with empty sorts, but true here proceeds anyway
				Util.writeFile(s, f.getAbsolutePath());
			}
			for (String k : env.defs.schs.keySet()) {
				Schema ts = env.defs.schs.get(k);
				File f = new File(dir, k + ".tptp");
				String s = ts.collage().toKB().tptp(true); //maedmax is unsound with empty sorts, but true here proceeds anyway
				Util.writeFile(s, f.getAbsolutePath());
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
