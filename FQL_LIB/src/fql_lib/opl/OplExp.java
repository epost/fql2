package fql_lib.opl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.gui.FQLTextPanel;

public abstract class OplExp implements OplObject {
	
	public static class OplString extends OplExp {
		String str;
		public OplString(String str) {
			this.str = str;
		}
		@Override
		public String toString() {
			return str;
		}
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			throw new RuntimeException();
		}
	}
	
	@Override
	public JComponent display() {
		FQLTextPanel p = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
		JTabbedPane ret = new JTabbedPane();
		ret.add(p, "Text");
		return ret;
	}
	
	public abstract <R, E> R accept(E env, OplExpVisitor<R, E> v);

	public static class OplTerm {
		String var;
		
		String head;
		List<OplTerm> args;
		
		public OplTerm(String a) {
			if (a == null) {
				throw new RuntimeException();
			}
			var = a;
		}
		
		public OplTerm(String h, List<OplTerm> a) {
			if (h == null || a == null) {
				throw new RuntimeException();
			}
			head = h;
			args = a;
		}

		
		public Object eval(Invocable invokable) throws NoSuchMethodException, ScriptException {
			if (var != null) {
				throw new RuntimeException("Can only only evaluate closed terms.");
			}
			List<Object> args0 = new LinkedList<>();
			for (OplTerm t : args) {
				args0.add(t.eval(invokable));
			}
			Object ret = invokable.invokeFunction(head, args0);
			if (ret == null) {
				throw new RuntimeException("returned null from " + head + " on " + args0);
			}
			return ret;
		}
		
		public String eval0(Invocable invokable) throws NoSuchMethodException, ScriptException {
			Object o = eval(invokable);
//			return o + " of class " + o.getClass();
			try {
				jdk.nashorn.api.scripting.ScriptObjectMirror m = (jdk.nashorn.api.scripting.ScriptObjectMirror) o;
				if (m.isArray()) {
					List<Object> l = new LinkedList<>();
					int i = 0;
					for (;;) {
						Object ob = m.get(i);
						if (ob == null) {
							break;
						}
						l.add(ob);
						i++;
					}
					return l.toString();
				} /* else if (m.isFunction()) {
					Map l = new HashMap<>();
					for (Object k : m.getOwnKeys(true)) {
						Object v = m.get(k);
						l.put(k, v);
					}
					return o.toString() + "\n\nin environment:\n\n" + l.toString() + "\n\n" + m.getProto() 
							+ "\n" + Arrays.toString(((jdk.nashorn.api.scripting.ScriptObjectMirror)m.getProto()).getOwnKeys(true));
					
				} */
			} catch (Exception ee) {
//				System.out.println(ee.getMessage());
			}
			return o.toString(); 
		}
		
		
		private static String printNicely(Object o) {
			if (o instanceof Bindings) {
				Bindings b = (Bindings) o;
				List<String> p = b.keySet().stream().map(x -> x + "=" + b.get(x)).collect(Collectors.toList());
				return "[" + o + " | " + Util.sep(p, ", ") + "]";
			}
			return o.toString();
		}
		
		public String eval(OplSig sig, OplSetInst inst, OplCtx<String> env) {
			if (var != null) {
				return env.get(var);
			}
			Pair<List<String>, String> t = sig.getSymbol(head);
			List<String> actual = args.stream().map(x -> x.eval(sig, inst, env)).collect(Collectors.toList());
			if (t.first.size() != actual.size()) {
				throw new RuntimeException("Argument length mismatch for " + this + ", expected " + t.first.size() + " but given " + actual.size());
			}
			Map<List<String>, String> m = inst.getSymbol(head);
			String ret = m.get(actual);
			if (ret == null) {
				throw new RuntimeException("Error, model provides no result for " + actual + " on symbol " + head);
			}
			return ret;
		}
		
		public String type(OplSig sig, OplCtx<String> ctx) {
			if (var != null) {
				return ctx.get(var);
			}
			Pair<List<String>, String> t = sig.getSymbol(head);
			
			List<String> actual = args.stream().map(x -> x.type(sig, ctx)).collect(Collectors.toList());
			if (t.first.size() != actual.size()) {
				throw new RuntimeException("Argument length mismatch for " + this + ", expected " + t.first.size() + " but given " + actual.size());
			}
			for (int i = 0; i < actual.size(); i++) {
				if (!t.first.get(i).equals(actual.get(i))) {
					throw new RuntimeException("Argument mismatch for " + this + 
							", expected term of sort " + t.first.get(i) + " but given " + args.get(i) + " of sort " + actual.get(i));					
				}
			}
			
			return t.second;
		}
		
		@Override
		public String toString() {
			if (var != null) {
				return var;
			}
			List<String> x = args.stream().map(z -> z.toString()).collect(Collectors.toList());
			return head + "(" + Util.sep(x, ", ") + ")";
		}

		public OplTerm subst(OplCtx<String> G, List<OplTerm> L) {
			if (var != null) {
				int i = G.indexOf(var);
				if (i == -1) {
					return this;
				}
				return L.get(i);
			}
			List<OplTerm> args0 = args.stream().map(x -> x.subst(G, L)).collect(Collectors.toList());
			return new OplTerm(head, args0);
		}
		
	}
	
	public static class OplCtx<X> {
		private LinkedHashMap<String, X> vars0;
		
		@Override
		public String toString() {
			List<String> l = new LinkedList<>();
			for (String k : vars0.keySet()) {
				l.add(k + ":" + vars0.get(k));
			}
			return Util.sep(l, ", ");
		}
		
		public int indexOf(String var) {
			int i = -1;
			for (String k : vars0.keySet()) {
				i++;
				if (var.equals(k)) {
					break;
				}
			}
			return i;
		}

		public OplCtx(List<Pair<String, X>> vars) {
			vars0 = new LinkedHashMap<>();
			for (Pair<String, X> k : vars) {
				if (vars0.containsKey(k.first)) {
					throw new RuntimeException("Duplicate variable " + k.first);
				}
				vars0.put(k.first, k.second);
			}
		}

		public OplCtx() {
			vars0 = new LinkedHashMap<>();
		}

		public X get(String s) {
			X ret = vars0.get(s);
			if (s == null) {
				throw new RuntimeException("Unbound var " + s);
			}
			return ret;
		}

		public void validate(OplSig oplSig) {
			for (String k : vars0.keySet()) {
				X v = get(k);
				if (!oplSig.sorts.contains(v)) {
					throw new RuntimeException("Context has bad sort " + v);
				}
			}
		}

		public List<X> values() {
			List<X> ret = new LinkedList<>();
			for (String k : vars0.keySet()) {
				X v = vars0.get(k);
				ret.add(v);
			}
			return ret;
		}
		
		public List<String> names() {
			List<String> ret = new LinkedList<>(vars0.keySet());
			return ret;
		}

		public OplCtx<X> makeEnv(List<X> env) {
			List<Pair<String, X>> in = new LinkedList<>();
			int i = 0;
			for (String k : vars0.keySet()) {
				in.add(new Pair<>(k, env.get(i)));
				i++;
			}
			return new OplCtx<>(in);
		}

		
	}
	
	
	public static class OplVar extends OplExp {
		String name;
		
		public OplVar(String c) {
			if (c == null) {
				throw new RuntimeException();
			}
			this.name = c;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override 
		public String toString() {
			return name;
		}
	}
	
	public static class OplSig extends OplExp {
		Set<String> sorts;
		Map<String, Pair<List<String>, String>> symbols;
		List<Triple<OplCtx<String>, OplTerm, OplTerm>> equations;
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			ret += "\t\t" + Util.sep(sorts, ", ") + ";\n";
			
			ret += "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (String k : symbols.keySet()) {
				Pair<List<String>, String> v = symbols.get(k);
				String s = k + " : " + Util.sep(v.first, ", ") + " -> " + v.second;
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist,",\n\t\t") + ";\n";
			
			ret += "\tequations\n";
			List<String> elist = new LinkedList<>();
			for (Triple<OplCtx<String>, OplTerm, OplTerm> k : equations) {
				String s = "forall " + k.first + ". " + k.second + " = " + k.third;
				elist.add(s);
			}

			ret += "\t\t" + Util.sep(elist, ",\n\t\t") + ";\n";
			
			return "theory {\n" + ret + "}";
		}

		public OplSig(Set<String> sorts, Map<String, Pair<List<String>, String>> symbols,
				List<Triple<OplCtx<String>, OplTerm, OplTerm>> equations) {
			super();
			this.sorts = sorts;
			this.symbols = symbols;
			this.equations = equations;
//			validate();
		}
		
		void validate() {
			for (String k : symbols.keySet()) {
				Pair<List<String>, String> v = symbols.get(k);
				if (!sorts.contains(v.second)) {
					throw new RuntimeException("Bad codomain " + v.second + " for " + k);
				}
				for (String a : v.first) {
					if (!sorts.contains(a)) {
						throw new RuntimeException("Bad argument sort " + a + " for " + k);
					}
				}
			}
			for (Triple<OplCtx<String>, OplTerm, OplTerm> eq : equations) {
				eq.first.validate(this);
				String t1 = eq.second.type(this, eq.first);
				String t2 = eq.third.type(this, eq.first);
				if (!t1.equals(t2)) {
					throw new RuntimeException("Domains do not agree in " + eq.second + " = " + eq.third + ", are " + t1 + " and " + t2);
				}
			}
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public Pair<List<String>, String> getSymbol(String var) {
			Pair<List<String>, String> ret = symbols.get(var);
			if (ret == null) {
				throw new RuntimeException("Unknown symbol " + var);
			}
			return ret;
		}

	}
	
	public static class OplMapping extends OplExp {
		Map<String, String> sorts;
		Map<String, Pair<OplCtx<String>, OplTerm>> symbols;
		
		String src0, dst0;
		OplSig src, dst;
		
		public JComponent display() {
			JTabbedPane jtp = new JTabbedPane();
			
			JComponent text = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			jtp.addTab("Text", text);
			
			JComponent tables = makeTables();
			jtp.addTab("Tables", tables);
			
			return jtp;
		}
		
		private JComponent makeTables() {
			List<JComponent> list = new LinkedList<>();
			
			List<Object[]> rs = new LinkedList<>();
			for (String n : sorts.keySet()) {
				String f = sorts.get(n);
				rs.add(new Object[] {n, f});
			}
			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), "Sorts", 
					rs.toArray(new Object[][] { }), new Object[] {src0 + " (in)", dst0 + " (out)"} ));
			
			List<Object[]> rows = new LinkedList<>();
			for (String n : symbols.keySet()) {
					Pair<OplCtx<String>, OplTerm> f = symbols.get(n);
					Object[] row = new Object[] { n, "forall " + f.first + ". " + f.second }; 
					rows.add(row);
				} 
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), "Symbols", 
						rows.toArray(new Object[][] { }), new Object[] {src0 + " (in)", dst0 + " (out)"}));

			 
/*			 for (String n : symbols.keySet()) {
				Pair<OplCtx<String>, OplTerm> f = symbols.get(n);
				List<String> ns = f.first.names();
				List<String> ts = f.first.values();
				ns.add(f.second.toString());
				ts.add(f.second.type(dst, f.first));
				Object[] row = ns.toArray();
				Object[] cols = ts.toArray();
				List<Object[]> rows = new LinkedList<Object[]>();
				rows.add(row);
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), n, 
						rows.toArray(new Object[][] { }), cols));
			}  */
			
			return Util.makeGrid(list);			
		}
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (String k : sorts.keySet()) {
				String v = sorts.get(k);
				sortsX.add(k + " -> " + v);
			}
			ret += "\t\t" + Util.sep(sortsX, ",\n\t\t") + ";\n";
			
			ret += "\tsymbols\n";
			List<String> symbolsX = new LinkedList<>();
			for (String k : symbols.keySet()) {
				Pair<OplCtx<String>, OplTerm> v = symbols.get(k);
				symbolsX.add(k + " -> forall " + v.first + ". " + v.second);
			}
			ret += "\t\t" + Util.sep(symbolsX, ",\n\t\t") + ";\n";

			return "mapping {\n" + ret + "} : " + src0 + " -> " + dst0;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		public <X> Pair<OplCtx<X>, OplTerm> trans(OplCtx<X> G, OplTerm E) {
		
			OplTerm E0;
			if (E.var != null) {
				E0 = E;
			} else {
				List<OplTerm> ta = E.args.stream().map(x -> trans(G, x).second).collect(Collectors.toList());
				Pair<OplCtx<String>, OplTerm> th = symbols.get(E.head);
				E0 = th.second.subst(th.first, ta);				
			}
			
			List<Pair<String, X>> l = G.vars0.keySet().stream().map(x -> new Pair<>(x, (X) sorts.get(G.get(x)))).collect(Collectors.toList());
			OplCtx<X> G0 = new OplCtx<>(l);
			
			return new Pair<>(G0, E0);
		}
		
		public void validate(OplSig src, OplSig dst) {
			this.src = src;
			this.dst = dst;
			for (String k : sorts.keySet()) {
				if (!src.sorts.contains(k)) {
					throw new RuntimeException("Extra sort: " + k);
				}
				String v = sorts.get(k);
				if (!dst.sorts.contains(v)) {
					throw new RuntimeException("Bad target sort " + v + " from " + k);
				}
			}
			for (String k : src.sorts) {
				if (!sorts.keySet().contains(k)) {
					throw new RuntimeException("Missing sort: " + k);
				}
			}
			for (String k : src.symbols.keySet()) {
				if (!symbols.keySet().contains(k)) {
					throw new RuntimeException("Extra symbol: " + k);
				}
			}
			for (String k : symbols.keySet()) {
				if (!src.symbols.containsKey(k)) {
					throw new RuntimeException("Extra symbol: " + k);
				}
				Pair<OplCtx<String>, OplTerm> v = symbols.get(k);
				String t = v.second.type(dst, v.first);
				if (!t.equals(sorts.get(src.symbols.get(k).second))) {
					throw new RuntimeException("Symbol " + k + " returns a " + src.symbols.get(k).second + " but transforms to " + t);
				}
				List<String> trans_t = src.symbols.get(k).first.stream().map(x -> sorts.get(x)).collect(Collectors.toList());
				if (!v.first.values().equals(trans_t)) {
					throw new RuntimeException("Symbol " + k + " inputs a " + v.first.values() + " but transforms to " + trans_t);
				}
			}
			//TODO: equations check
		}

		public OplMapping(Map<String, String> sorts,
				Map<String, Pair<OplCtx<String>, OplTerm>> symbols, String src0, String dst0) {
			this.sorts = sorts;
			this.symbols = symbols;
			this.src0 = src0;
			this.dst0 = dst0;
		}
		
		//F : C -> D
		//h : I (D-Inst) => J (D-Inst)
		//delta F h : delta F A (C-Inst) => delta F B (C-Inst)
		public OplSetTrans delta(OplSetTrans h) {
			if (!h.src0.sig.equals(dst0)) {
				throw new RuntimeException("Source of transform, " + h.src0 + " does not have theory " + dst0);
			}
			if (!h.dst0.sig.equals(dst0)) {
				throw new RuntimeException("Target of transform, " + h.src0 + " does not have theory " + dst0);
			}

			OplSetInst srcX = delta(h.src0);
			OplSetInst dstX = delta(h.dst0);
			
			Map<String, Map<String, String>> sortsX = new HashMap<>();
			for (String s : src.sorts) {
				Map<String, String> m = new HashMap<>();
				for (String v : srcX.sorts.get(s)) {
					m.put(v, h.sorts.get(sorts.get(s)).get(v));
				}
				sortsX.put(s, m);
			}
			
			OplSetTrans ret = new OplSetTrans(sortsX, "?", "?");
			ret.validate(src, srcX, dstX);
			return ret;
		}
		
		public OplSetInst delta(OplSetInst J) {
			if (!dst0.equals(J.sig)) {
				throw new RuntimeException("Target of mapping " + dst + " does not match " + J.sig);
			}
			Map<String, Set<String>> sortsX = new HashMap<>();
			for (String s : src.sorts) {
				String s0 = sorts.get(s);
				sortsX.put(s, J.sorts.get(s0));
			}
			
			Map<String, Map<List<String>, String>> symbolsX = new HashMap<>();
			for (String n : src.symbols.keySet()) {
				Pair<List<String>, String> f_t = src.symbols.get(n);
				List<Set<String>> f_a = f_t.first.stream().map(x -> sortsX.get(x)).collect(Collectors.toList());
				List<List<String>> inputs = prod(f_a);
				Pair<OplCtx<String>, OplTerm> f = symbols.get(n);
				Map<List<String>, String> map = new HashMap<>();
				for (List<String> input : inputs) {
					String output = f.second.eval(dst, J, f.first.makeEnv(input));
					if (map.containsKey(input)) {
						throw new RuntimeException();
					}
					map.put(input, output);
				}
				symbolsX.put(n, map);
			}
			
			OplSetInst I = new OplSetInst(sortsX, symbolsX, src0);
			I.validate(src);
			return I;
		}
		
		/*public OplJavaInst delta(OplJavaInst J) {
			if (!dst0.equals(J.sig)) {
				throw new RuntimeException("Target of mapping " + dst + " does not match " + J.sig);
			}
			
			Map<String, Object> symbolsX = new HashMap<>();
			for (String n : src.symbols.keySet()) {
				Pair<OplCtx<String>, OplTerm> f = symbols.get(n);
				
				Bindings b = J.engine.getBindings(ScriptContext.ENGINE_SCOPE);
				int i = 0;
				for (String var : f.first.names()) {
					b.put(var, "input[ " + i + "]");
					i++;
				}
				Object trans = f.second.eval((Invocable)J.engine);
				
				symbolsX.put(n, trans);
			}
			
			OplJavaInst I = new OplJavaInst(symbolsX, src0);
			I.validate(src);
			return I;
		}
		*/
	} 
	
	public static class OplDelta extends OplExp {
		String F, I;

		public OplDelta(String f, String i) {
			F = f;
			I = i;
		}

		@Override
		public String toString() {
			return "delta " + F + " " + I;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplTransEval extends OplExp {
		String I;
		String F;
		OplTerm e; 
		
		public OplTransEval(String I, String F, OplTerm e) {
			this.I = I;
			this.F = F;
			this.e = e;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class OplEval extends OplExp {
		String I;
		OplTerm e; 
		
		public OplEval(String i, OplTerm e) {
			I = i;
			this.e = e;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class OplSetInst extends OplExp {
		Map<String, Set<String>> sorts;
		String sig;
		private Map<String, Map<List<String>, String>> symbols;
		OplSig sig0;
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (String k : sorts.keySet()) {
				sortsX.add(k + " -> {" + Util.sep(sorts.get(k), ", ") + "}");
			}
			ret += "\t\t" + Util.sep(sortsX, ", ") + ";\n";
			
			ret += "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (String k : symbols.keySet()) {
				Map<List<String>, String> v = symbols.get(k);
				List<String> u = new LinkedList<>();
				for (List<String> i : v.keySet()) {
					String j = v.get(i);
					u.add("((" + Util.sep(i, ",") + "), " + j + ")");
				}
				String s = k + " -> {" + Util.sep(u, ", ") + "}";
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist,",\n\t\t") + ";\n";
						
			return "model {\n" + ret + "} : " + sig;
		}
		
		public JComponent display() {
			JTabbedPane jtp = new JTabbedPane();
			
			JComponent text = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			jtp.addTab("Text", text);
			
			JComponent tables = makeTables();
			jtp.addTab("Tables", tables);
			
			return jtp;
		}
		
		
		private JComponent makeTables() {
			List<JComponent> list = new LinkedList<>();
			
			for (String n : sorts.keySet()) {
				List<Object[]> rows = new LinkedList<>();
				for (String arg : sorts.get(n)) {
					Object[] row = new Object[] {arg};
					rows.add(row);
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), n, 
						rows.toArray(new Object[][] { }), new Object[] {n} ));
			}
			for (String n : symbols.keySet()) {
				Map<List<String>, String> f = symbols.get(n);
				int numcols = -1;
				for (List<String> arg : f.keySet()) {
					numcols = arg.size();
					break;
				}
				if (numcols == -1) {
					continue;
				}
				List<Object[]> rows = new LinkedList<>();
				for (List<String> arg : f.keySet()) {
					List<String> argX = new LinkedList<>(arg);
					argX.add(f.get(arg));
					Object[] row = argX.toArray();
					rows.add(row);
				}
				List<String> l = new LinkedList<>(sig0.symbols.get(n).first);
				l.add(sig0.symbols.get(n).second);
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), n, rows.toArray(new Object[][] { }), l.toArray()));
			}
			
			return Util.makeGrid(list);			
		}


		public void validate(OplSig sig) {
			for (String s : sig.sorts) {
				if (!sorts.containsKey(s)) {
					throw new RuntimeException("No data for " + s + " in " + this);
				}
			}
			for (String s : sorts.keySet()) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Extra data for " + s);
				}
			}
			for (String f : sig.symbols.keySet()) {
				if (!symbols.containsKey(f)) {
					throw new RuntimeException("No data for " + f);
				}
				List<String> arg_ts = sig.symbols.get(f).first;
				List<Set<String>> arg_ds = arg_ts.stream().map(x -> sorts.get(x)).collect(Collectors.toList());
				List<List<String>> args = prod(arg_ds);
				for (List<String> arg : args) {
					String at = symbols.get(f).get(arg);
					if (at == null) {
						throw new RuntimeException("Missing on argument " + arg + " in " + f);
					}
					if (!sorts.get(sig.symbols.get(f).second).contains(at)) {
						throw new RuntimeException("Return value " + at + " not in correct sort in " + f);
					}
				}
				for (List<String> gt : symbols.get(f).keySet()) {
					if (!args.contains(gt)) {
						throw new RuntimeException("Superfluous arg " + gt + " in " + f + " notin " + args);
					}
				}
			}
			for (String f : symbols.keySet()) {
				if (!sig.symbols.keySet().contains(f)) {
					throw new RuntimeException("Extra data for " + f);	
				}
			}
			for (Triple<OplCtx<String>, OplTerm, OplTerm> eq : sig.equations) {
				List<String> arg_ts = eq.first.values();
				List<Set<String>> arg_ds = arg_ts.stream().map(x -> sorts.get(x)).collect(Collectors.toList());
				List<List<String>> args = prod(arg_ds);
				for (List<String> env : args) {
					OplCtx<String> env2 = eq.first.makeEnv(env);
					String x = eq.second.eval(sig, this, env2);
					String y = eq.third.eval(sig, this, env2);
					if (!x.equals(y)) {
						throw new RuntimeException("Equation " + eq.second + " = " + eq.third + " not respected on " + env + ", lhs=" + x + " and rhs=" + y);
					}
				}
			}
			sig0 = sig;
		}
		
		
		

		public OplSetInst(Map<String, Set<String>> sorts,
				Map<String, Map<List<String>, String>> symbols, String sig) {
			this.sorts = sorts;
			this.sig = sig;
			this.symbols = symbols;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public Map<List<String>, String> getSymbol(String head) {
			Map<List<String>, String> ret = symbols.get(head);
			if (ret == null) {
				throw new RuntimeException("Unknown symbol " + head);
			}
			return ret;
		}

	}
	
	public static class OplJavaInst extends OplExp {
		
		Map<String, String> defs;
		
		ScriptEngine engine;
		
		String sig;
		
		OplSig sig0;
		
		public OplJavaInst(Map<String, String> defs, String sig) {
			this.defs = defs;
			this.sig = sig;
		}
		
		public void validate(OplSig sig) {
			sig0 = sig;
			for (String k : defs.keySet()) {
				if (!sig.symbols.containsKey(k)) {
					throw new RuntimeException("Extra symbol " + k);
				}				
			}
			for (String k : sig.symbols.keySet()) {
				if (!defs.keySet().contains(k)) {
					throw new RuntimeException("Missing symbol " + k);
				}
			}
			
			engine = new ScriptEngineManager().getEngineByName("nashorn");
			String ret = "";
			for (String v : defs.values()) {
				ret += v + "\n\n";
			}
			
			try {
				engine.eval(ret);
			} catch (ScriptException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		@Override
		public String toString() {
			String ret = "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (String k : defs.keySet()) {
				String s = k + " -> \"" + defs.get(k) + "\"";
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist,",\n\t\t") + ";\n";
						
			return "javascript {\n" + ret + "} : " + sig;
		}

	}
	
	public static class OplSetTrans extends OplExp {
		Map<String, Map<String, String>> sorts;
		String src, dst;
		
		OplSig sig;
		OplSetInst src0, dst0;
		
		public JComponent display() {
			JTabbedPane jtp = new JTabbedPane();
			
			JComponent text = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			jtp.addTab("Text", text);
			
			JComponent tables = makeTables();
			jtp.addTab("Tables", tables);
			
			return jtp;
		}
		
		private JComponent makeTables() {
			List<JComponent> list = new LinkedList<>();
			for (String n : sorts.keySet()) {
				Map<String, String> f = sorts.get(n);
				List<Object[]> rows = new LinkedList<>();
				for (String arg : f.keySet()) {
					Object[] row = new Object[2]; 
					row[0] = arg;
					row[1] = f.get(arg);
					rows.add(row);
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), n, 
						rows.toArray(new Object[][] { }), new Object[] {src + " (in)", dst + " (out)"}));
			}
			
			return Util.makeGrid(list);			
		}
		
		public OplSetTrans(Map<String, Map<String, String>> sorts, String src, String dst) {
			this.sorts = sorts;
			this.src = src;
			this.dst = dst;
		}

		public void validate(OplSig sig, OplSetInst src0, OplSetInst dst0) {
			this.sig = sig;
			this.src0 = src0;
			this.dst0 = dst0;
			
			for (String s : sig.sorts) {
				if (!sorts.containsKey(s)) {
					throw new RuntimeException("Missing sort: " + s);
				}
			}
			for (String s : sorts.keySet()) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Extra sort: " + s);
				}
			}
			for (String s : sig.sorts) {
				Map<String, String> h = sorts.get(s);
				for (String x : h.keySet()) {
					String y = h.get(x);
					if (!src0.sorts.get(s).contains(x)) {
						throw new RuntimeException("Value " + x + " is not in source for sort " + s);
					}
					if (!dst0.sorts.get(s).contains(y)) {
						throw new RuntimeException("Value " + y + " is not in target for sort " + s);
					}
				}
			}
			for (String f : sig.symbols.keySet()) {
				Pair<List<String>, String> a = sig.symbols.get(f);
				List<String> arg_ts = a.first;
				List<Set<String>> arg_ds = arg_ts.stream().map(x -> src0.sorts.get(x)).collect(Collectors.toList());
				List<List<String>> args = prod(arg_ds);
				
				for (List<String> arg : args) {
					String r = src0.getSymbol(f).get(arg);
					String lhs = sorts.get(a.second).get(r);
					
					List<String> l = new LinkedList<>();
					int i = 0;
					for (String at : arg_ts) {
						String v = arg.get(i);
						String u = sorts.get(at).get(v);
						l.add(u);
						i++;
					}
					String rhs = dst0.getSymbol(f).get(l);
					if (!lhs.equals(rhs)) {
						throw new RuntimeException("Compatibility condition failure for " + f + " on " + arg + ", lhs=" + lhs + " but rhs=" + rhs);
					}					
				}
	
			}
		}
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (String k : sorts.keySet()) {
				Map<String, String> v = sorts.get(k);
				List<String> l = new LinkedList<>();
				for (String x : v.keySet()) {
					String y = v.get(x);
					l.add("(" + x + ", " + y + ")");
				}
				sortsX.add(k + " -> {" + Util.sep(l, ", ") + "}");
			}
			ret += "\t\t" + Util.sep(sortsX, ", ") + ";\n";
						
			return "transform {\n" + ret + "} : " + src + " -> " + dst;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplJavaTrans {
		
	}
		


	public interface OplExpVisitor<R, E> {
		public R visit (E env, OplSig e);
		public R visit (E env, OplSetInst e);
		public R visit (E env, OplTransEval e);	
		public R visit (E env, OplEval e);
		public R visit (E env, OplVar e);
		public R visit (E env, OplSetTrans e);
		public R visit (E env, OplJavaInst e);
		public R visit (E env, OplMapping e);
		public R visit (E env, OplDelta e);
	}

	private static List<List<String>> prod(List<Set<String>> in1) {
		List<List<String>> y = new LinkedList<>();
		List<String> z = new LinkedList<>();
		y.add(z);
		
		for (Set<String> X : in1) {
			List<List<String>> y0 = new LinkedList<>();
			for (List<String> a : y) {					
				for (String x : X) {
					List<String> toadd = new LinkedList<>(a);
					toadd.add(x);
					y0.add(toadd);
				}
			}
			y = y0;
		}
		
		return y;
	}
	
}
