package catdata.provers;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Ctx;
import catdata.Triple;
import catdata.Util;

public class MaedmaxProver<T, C, V> extends DPKB<T, C, V>  {
	
	@Override 
	public void finalize() {
		try {
			writer.println("exit");
			writer.close();
			reader.close();
			proc.destroyForcibly();
		} catch (Exception ex) {
			try {
	
			} catch (Exception ex2) {
				
			}
		}
	}

	private final Process proc;
	private final BufferedReader reader;
	private final PrintWriter writer;
	private final KBTheory<T,C,V> th;
	
	//done elsewhere for convenience
	//TODO AQL empty sorts check
	public MaedmaxProver(String exePath, KBTheory<T,C,V> th, boolean allowEmptySorts, long seconds) {
		super(th.tys, th.syms, th.eqs);
		this.th = th;
		
		File f = new File(exePath);
		if (!f.exists()) {
			throw new RuntimeException("File does not exist: " + exePath);
		}
		
		
		
		try {
			File g = File.createTempFile("AqlMaedmax", ".tptp");
			Util.writeFile(th.tptp(allowEmptySorts), g.getAbsolutePath());
			System.out.println(g.getAbsolutePath());
			
			String str = exePath + "-T " + seconds + " --interactive --aql " + g.getAbsolutePath();
			proc = Runtime.getRuntime().exec(str);
			
			
			reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			writer = new PrintWriter(proc.getOutputStream());
			
			
			String line = reader.readLine();
			if (line == null) {
				throw new RuntimeException("Call to maedmax yields null, process is alive: " + proc.isAlive() + ".  Command: " + str);
			} else if (!line.equals("OK")) {
				throw new RuntimeException("Maedmax error: " + line);
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public synchronized boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		if (!ctx.keySet().equals(Util.union(lhs.vars(), rhs.vars()))) {
			throw new RuntimeException("Maedmax does not currently support contexts.");
		}
		writer.println(th.convert(lhs) + " = " + th.convert(rhs));
		writer.flush();
		try {
			reader.readLine(); //enter ... :
			String line = reader.readLine();
			if (line == null) {
				Util.anomaly();
			} else if (line.equals("YES")) {
				return true;
			} else if (line.equals("NO")) {
				return false;
			}
			throw new RuntimeException("Maedmax error on " + lhs + " = " + rhs + ", " + line + " is not YES or NO.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		return Util.anomaly();
	}
	
	@Override
	public String toString() {
		return "Maedmax prover";
	}

}
