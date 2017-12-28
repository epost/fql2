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
		} catch (Exception ex) {
			try {
				proc.destroyForcibly();
			} catch (Exception ex2) {
				
			}
		}
	}

	private final Process proc;
	private final BufferedReader reader;
	private final PrintWriter writer;
	
	private final Ctx<C, String> iso1 = new Ctx<>();
	private final Ctx<String, C> iso2 = new Ctx<>();
	
	private final String convert(KBExp<C,V> e) {
		if (e.isVar) {
			return convertV(e.getVar().var);
		}
		List<String> l = new LinkedList<>();
		for (KBExp<C,V> arg : e.getApp().args) {
			l.add(convert(arg));
		}
		if (l.isEmpty()) {
			return convertC(e.getApp().f);
		} 
		return convertC(e.getApp().f) + "(" + Util.sep(l, ",") + ")";
	}
	
	private final String convertV(V e) {
		return "V" + e.toString();
	}
	
	private final String convertC(C e) {
		return iso1.get(e);
	}
	
	//done elsewhere for convenience
	//TODO AQL empty sorts check
	public MaedmaxProver(String exePath, KBTheory<T,C,V> th, boolean allowEmptySorts) {
		super(th.tys, th.syms, th.eqs);
		
		File f = new File(exePath);
		if (!f.exists()) {
			throw new RuntimeException("File does not exist: " + exePath);
		}
		
		if (!allowEmptySorts) {
	 		Set<T> es = new HashSet<>();
			th.inhabGen(es);
			if (!es.equals(th.tys)) {
				throw new RuntimeException("Sorts " + Util.sep(Util.diff(th.tys, es), ", ") + " have no ground terms (required by maedmax; consider maedmax_allow_empty_sorts_unsafe = true).");
			}
		}
		
		int i = 0;
		for (C c : th.syms.keySet()) {
			iso1.put(c, "s" + i);
			iso2.put("s" + i, c);			
			i++;
		}
		
		int j = 0;
		StringBuilder sb = new StringBuilder();
		for (Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>> eq : th.eqs) {
			if (!eq.first.keySet().equals(Util.union(eq.second.vars(), eq.third.vars()))) {
				throw new RuntimeException("Maedmax does not currently support contexts.");
			}
			sb.append("cnf(eq" + j + ",axiom,(" + convert(eq.second) + " = " + convert(eq.third) + ")).");			
			sb.append(System.lineSeparator());
			j++;
		}
		
		try {
			File g = File.createTempFile("AqlMaedmax", ".tptp");
			Util.writeFile(sb.toString(), g.getAbsolutePath());
			System.out.println(g.getAbsolutePath());
			
			String str = exePath + " --interactive --aql " + g.getAbsolutePath();
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
		writer.println(convert(lhs) + " = " + convert(rhs));
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
