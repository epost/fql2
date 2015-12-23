package fql_lib.opl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import catdata.algs.Chc;
import catdata.algs.Pair;
import catdata.algs.Triple;
import catdata.algs.kb.KBExp;
import fql_lib.DEBUG;
import fql_lib.Util;
import fql_lib.cat.categories.FinSet;
import fql_lib.gui.FQLTextPanel;
import fql_lib.gui.MyTableRowSorter;
import fql_lib.opl.OplParser.DoNotIgnore;

public abstract class OplExp implements OplObject {
	
	public static class OplFlower<S,C,V,X,Z> extends OplExp {
		Map<Z, OplTerm<C,V>> select;
		Map<V, S> from;
		List<Pair<OplTerm<C,V>, OplTerm<C,V>>> where;
		OplSetInst<S,C,X> I;
		String I0;
		 
		public OplFlower(Map<Z, OplTerm<C, V>> select, Map<V, S> from,
				List<Pair<OplTerm<C, V>, OplTerm<C, V>>> where,  String i0) {
			super();
			this.select = select;
			this.from = from;
			this.where = where;
			I0 = i0;
		}
		
		public void validate() {
			OplSig<S,C,V> sig = (OplSig<S, C, V>) I.sig0;

			for (V v : from.keySet()) {
				S s = from.get(v);
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Bad sort: " + s);
				}
			}
			OplCtx<S,V> ctx = new OplCtx<>(from);
			for (Pair<OplTerm<C, V>, OplTerm<C, V>> eq : where) {
				eq.first.type(sig, ctx);
				eq.second.type(sig, ctx);
			}
			for (Z z : select.keySet()) {
				OplTerm<C, V> e = select.get(z);
				e.type(sig, ctx);
			}
		}

		public OplSetTrans<String,String,String> eval(OplSetTrans<S,C,X> h) {
			OplSetInst<S,C,X> I = h.src0;
			OplSetInst<S,C,X> J = h.dst0;
			
			Pair<Map<Integer, OplCtx<X, Z>>, OplSetInst<String, String, String>> I0X = eval(I);
			Pair<Map<Integer, OplCtx<X, Z>>, OplSetInst<String, String, String>> J0X = eval(J);
			Map<Integer, OplCtx<X, Z>> Im = I0X.first;
			Map<Integer, OplCtx<X, Z>> Jm = J0X.first;
			OplSetInst<String, String, String> QI = I0X.second;
			OplSetInst<String, String, String> QJ = J0X.second;
			
			OplSig<S,C,V> sig = (OplSig<S, C, V>) I.sig0;
			OplCtx<S,V> ctx = new OplCtx<>(from);
			
			Map<String, Map<String, String>> sorts = new HashMap<>();
			for (S k : I.sig0.sorts) {
				if (k.toString().equals("_Q")) {
					continue;
				}
				Set<X> X = I.sorts.get(k);
				Map<String, String> m = new HashMap<>();
				for (X x : X) {
					m.put(x.toString(), h.sorts.get(k).get(x).toString());
				}		
				sorts.put(k.toString(), m);
			}
			Map<String, String> m = new HashMap<>();
			for (Integer i : Im.keySet()) {
				OplCtx<X, Z> t = Im.get(i);
				Map<Z, X> n = new HashMap<>();
				for (Z z : t.vars0.keySet()) {
					X x = t.vars0.get(z);
					S s = select.get(z).type(sig, ctx);
					X x2 = h.sorts.get(s).get(x);
					n.put(z, x2);
				}
				OplCtx<X,Z> t2 = new OplCtx<>(n);
				Integer j = Util.revLookup(Jm, t2);
				if (j == null) {
					throw new RuntimeException("Not found: " + t2 + " in " + Jm);
				}
				m.put("_" + Integer.toString(i), "_" + Integer.toString(j));
			}
			sorts.put("_Q", m);
			
			OplSetTrans<String, String, String> ret = new OplSetTrans<String, String, String>(sorts , "?", "?");
			ret.validate(QI.sig0, QI, QJ);
			return ret;
		}
		
		public Pair<Map<Integer, OplCtx<X, Z>>, OplSetInst<String,String,String>> eval(OplSetInst<S,C,X> I) {
			this.I = I;
			validate();
			
			OplSig<S,C,V> sig = (OplSig<S, C, V>) I.sig0;
			OplCtx<S,V> ctx = new OplCtx<>(from);

			Set<OplCtx<X,V>> tuples = new HashSet<>();
			tuples.add(new OplCtx<>());
			
			for (V v : from.keySet()) {
				S s = from.get(v);
				Set<X> dom = I.sorts.get(s);
				tuples = extend(tuples, dom, v);
				tuples = filter(tuples, where, I);
			}
			
			Set<String> ret_sorts = new HashSet<>();
			for (S c : sig.sorts) {
				ret_sorts.add(c.toString());
			}
			ret_sorts.add("_Q");
			Map<String, Pair<List<String>, String>> ret_symbols = new HashMap<>();
			for (Z z : select.keySet()) {
				OplTerm<C, V> k = select.get(z);
				ret_symbols.put(z.toString(), new Pair<>(Util.singList("_Q"), k.type(sig, ctx).toString()));
			}
			OplSig<String,String,V> ret_sig = new OplSig<>(sig.fr, new HashMap<>(), ret_sorts, ret_symbols, new LinkedList<>());

			Map<String, Set<String>> ret_sorts2 = new HashMap<>();
			Set<String> projected = new HashSet<>();
			Map<String, Map<List<String>, String>> ret_symbols2 = new HashMap<>();
			Map<Integer, OplCtx<X, Z>> m = new HashMap<>();
			int i = 0;
			for (OplCtx<X, V> env : tuples) {
				Map<Z, X> tuple = new HashMap<>();
				for (Z z : select.keySet()) {
					tuple.put(z, select.get(z).eval(sig, I, env));
				}
				projected.add("_" + Integer.toString(i));
				m.put(i, new OplCtx<X, Z>(tuple));				
				i++;
			}
			ret_sorts2.put("_Q", projected); 
			for (S s : sig.sorts) {
				ret_sorts2.put(s.toString(), I.sorts.get(s).stream().map(x -> { return x.toString(); }).collect(Collectors.toSet()));
			}
			
			for (Z z : select.keySet()) {
				Map<List<String>, String> n = new HashMap<>();
				for (int j = 0; j < i; j++) {
					OplCtx<X, Z> tuple = m.get(j);
					n.put(Util.singList("_" + j), tuple.get(z).toString());
				}
				ret_symbols2.put(z.toString(), n);
			}
			
			OplSetInst<String, String, String> ret = new OplSetInst<>(ret_sorts2, ret_symbols2 , "?");
			ret.validate(ret_sig);
			return new Pair<>(m, ret);
		}

		@SuppressWarnings("unchecked")
		private static <X,C,S,V> Set<OplCtx<X, V>> filter(Set<OplCtx<X, V>> tuples,
				List<Pair<OplTerm<C, V>, OplTerm<C, V>>> where, OplSetInst<S, C, X> I) {
			Set<OplCtx<X, V>> ret = new HashSet<>();
			outer: for (OplCtx<X, V> tuple : tuples) {
				for (Pair<OplTerm<C, V>, OplTerm<C, V>> eq : where) {
					if (eq.first.isGround(tuple) && eq.second.isGround(tuple)) {
						X l = eq.first.eval((OplSig<S,C,V>)I.sig0, I, tuple);
						X r = eq.second.eval((OplSig<S,C,V>)I.sig0, I, tuple);
						if (!l.equals(r)) {
							continue outer;
						}
					}					
				}
				ret.add(tuple);				
			}
			return ret;
		}

		private static <X,V> Set<OplCtx<X, V>> extend(Set<OplCtx<X, V>> tuples, Set<X> dom, V v) {
			Set<OplCtx<X, V>> ret = new HashSet<>();
			
			for (OplCtx<X, V> tuple : tuples) {
				for (X x : dom) {
					Map<V,X> m = new HashMap<>(tuple.vars0);
					m.put(v, x);
					OplCtx<X, V> new_tuple = new OplCtx<>(m);
					ret.add(new_tuple);
				}
			}
			
			return ret;
		}
					

	/*	private void count(OplTerm l, Map counts) {
			for (Object s : l) {
				Integer i = (Integer) counts.get(s);
				if (i == null) {
					continue;
				}
				counts.put(s, i+1);
			}
		}

		public Map sort() {
			Map count = new HashMap<>();
			for (Object s : from.keySet()) {
				count.put(s, 0);
			}
			for (Pair<OplTerm<C, V>, OplTerm<C, V>> k : where) {
				count(k.first, count);
				count(k.first, count);
			}
			List l = new LinkedList(from.keySet());
			l.sort(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((Integer)count.get(o2)) - ((Integer)count.get(o1));
				}
			});
			Map ret = new LinkedHashMap<>();
			for (Object s : l) {
				ret.put(s, from.get(s));
			}
			return ret;
		}  */

		

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		
		
	}
	
	public static class JSWrapper {
		public Object o;
		public JSWrapper(Object o) {
			this.o = o;
		}
		public String toString() {
			String s = "JS<" + o + ">";
			if (!DEBUG.debug.opl_pretty) {
				return s;
			}
			return o.toString();
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((o == null) ? 0 : o.hashCode());
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
			JSWrapper other = (JSWrapper) obj;
			if (o == null) {
				if (other.o != null)
					return false;
			} else if (!o.equals(other.o))
				return false;
			return true;
		}
		
	}
	
	@Override
	public JComponent display() {
		FQLTextPanel p = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
		JTabbedPane ret = new JTabbedPane();
		ret.add(p, "Text");
		return ret;
	}
	
	/*@Override 
	   public Component getTableCellRendererComponent(
               JTable table, Object color,
               boolean isSelected, boolean hasFocus,
               int row, int column) {
		return display();
	}
	*/
	public static class OplString extends OplExp {
		String str;
		public OplString(String str) {
			this.str = str;
		}
		@Override
		public String toString() {
			return strip(str);
		}
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			throw new RuntimeException();
		}
	}
	
		
	public abstract <R, E> R accept(E env, OplExpVisitor<R, E> v);
	
	public static class OplTerm<C, V> implements Comparable<OplTerm<C,V>>  {
		
		public V var;
		
		public C head;
		public List<OplTerm<C, V>> args;
		
		public OplTerm(V a) {
			if (a == null) {
				throw new RuntimeException();
			}
			var = a;
		}
		
		public boolean isGround(OplCtx<?, V> ctx) {
			if (var != null) {
				return ctx.vars0.containsKey(var);
			}
			for (OplTerm<C, V> t : args) {
				if (!t.isGround(ctx)) {
					return false;
				}
			}
			return true;
		}

		public OplTerm(C h, List<OplTerm<C, V>> a) {
			if (h == null || a == null) {
				throw new RuntimeException();
			}
			head = h;
			args = a;
		}

		
		public JSWrapper eval(Invocable invokable) throws NoSuchMethodException, ScriptException {
			if (var != null) {
				throw new RuntimeException("Can only only evaluate closed terms.");
			}
			List<Object> args0 = new LinkedList<>();
			for (OplTerm t : args) {
				args0.add(t.eval(invokable).o);
			}
			Object ret = invokable.invokeFunction((String)head, args0);
			if (ret == null) {
				throw new RuntimeException("returned null from " + head + " on " + args0);
			}
			//System.out.println(ret.getClass());
			return new JSWrapper(ret);
		}
		
/*		public String eval0(Invocable invokable) throws NoSuchMethodException, ScriptException {
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
					
				} */ /*
			} catch (Exception ee) {
//				System.out.println(ee.getMessage());
			}
			return o.toString(); 
			
			
			
		}*/
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((args == null) ? 0 : args.hashCode());
			result = prime * result + ((head == null) ? 0 : head.hashCode());
			result = prime * result + ((var == null) ? 0 : var.hashCode());
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
			OplTerm other = (OplTerm) obj;
			if (args == null) {
				if (other.args != null)
					return false;
			} else if (!args.equals(other.args))
				return false;
			if (head == null) {
				if (other.head != null)
					return false;
			} else if (!head.equals(other.head))
				return false;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

/*		private static String printNicely(Object o) {
			if (o instanceof Bindings) {
				Bindings b = (Bindings) o;
				List<String> p = b.keySet().stream().map(x -> x + "=" + b.get(x)).collect(Collectors.toList());
				return "[" + o + " | " + Util.sep(p, ", ") + "]";
			}
			return o.toString();
		} */
		
		public <S,X> X eval(OplSig<S,C,V> sig, OplSetInst<S,C,X> inst, OplCtx<X,V> env) {
			if (var != null) {
				return env.get(var);
			}
			Pair<List<S>, S> t = sig.getSymbol(head);
			List<X> actual = args.stream().map(x -> x.eval(sig, inst, env)).collect(Collectors.toList());
			if (t.first.size() != actual.size()) {
				throw new RuntimeException("Argument length mismatch for " + this + ", expected " + t.first.size() + " but given " + actual.size());
			}
			Map<List<X>, X> m = inst.getSymbol(head);
			X ret = m.get(actual);
			if (ret == null) {
				throw new RuntimeException("Error, model provides no result for " + actual + " on symbol " + head);
			}
			return ret;
		}
		
		public <S> S type(OplSig<S,C,V> sig, OplCtx<S, V> ctx) {
			if (var != null) {
				return ctx.get(var);
			}
			Pair<List<S>, S> t = sig.getSymbol(head);
			
			List<S> actual = args.stream().map(x -> x.type(sig, ctx)).collect(Collectors.toList());
			if (t.first.size() != actual.size()) {
				throw new DoNotIgnore("Argument length mismatch for " + this + ", expected " + t.first.size() + " but given " + actual.size());
			}
			for (int i = 0; i < actual.size(); i++) {
				if (!t.first.get(i).equals(actual.get(i))) {
					S z = actual.get(i);
					String y = z == null ? " (possible forgotten ())" : "";
					throw new DoNotIgnore("Argument mismatch for " + this + 
							", expected term of sort " + t.first.get(i) + " but given " + args.get(i) + " of sort " + z + y);					
				}
			}
			
			return t.second;
		}
		
		
		@Override
		public String toString() {
			if (var != null) {
				return var.toString();
			}
			try {
				int i = Util.termToNat(this);
				return Integer.toString(i);
			} catch (Exception ex) { 
				//System.out.println("converting " + this);
			//	ex.printStackTrace();
			}
			List<String> x = args.stream().map(z -> z.toString()).collect(Collectors.toList());
			String ret = head + "(" + Util.sep(x, ", ") + ")";
			return strip(ret);
		}

		public <S> OplTerm<C,V> subst(OplCtx<S,V> G, List<OplTerm<C,V>> L) {
			if (var != null) {
				int i = G.indexOf(var);
				if (i == -1) {
					return this;
				}
				return L.get(i);
			}
			List<OplTerm<C,V>> args0 = args.stream().map(x -> x.subst(G, L)).collect(Collectors.toList());
			return new OplTerm<>(head, args0);
		}

		public OplTerm<C,V> subst(Map<V, OplTerm<C,V>> s) {
			if (var != null) {
				OplTerm<C,V> t = s.get(var);
				if (t != null) {
					return t;
				} 
				return this;
			}
			List<OplTerm<C,V>> l = new LinkedList<>();
			for (OplTerm<C,V> a : args) {
				l.add(a.subst(s));
			}
			return new OplTerm<>(head, l);
		}

		public Set<C> symbols() {
			if (var != null) {
				return new HashSet<>();
			}
			Set<C> ret = new HashSet<>();
			ret.add(head);
			for (OplTerm<C,V> arg : args) {
				ret.addAll(arg.symbols());
			}
			return ret;
		}

		//needed for machine learning library for some reason
		@Override
		public int compareTo(OplTerm<C, V> o) {
			return o.toString().compareTo(toString());
		}
		
	}
	
	public static class OplCtx<S,V> {
		private LinkedHashMap<V, S> vars0;
		
		public OplCtx(Map<V,S> m) {
			vars0 = new LinkedHashMap<>(m);
		}
		
		@Override
		public String toString() {
			List<String> l = new LinkedList<>();
			for (V k : vars0.keySet()) {
				if (vars0.get(k) != null) {
					l.add(k + ":" + vars0.get(k));
				} else {
					l.add(k.toString());
				}
			}
			String ret = Util.sep(l, ", ");
			return strip(ret);
		}
		
		public int indexOf(V var) {
			int i = -1;
			for (V k : vars0.keySet()) {
				i++;
				if (var.equals(k)) {
					break;
				}
			}
			return i;
		}

		public OplCtx(List<Pair<V, S>> vars) {
			vars0 = new LinkedHashMap<>();
			for (Pair<V, S> k : vars) {
				if (vars0.containsKey(k.first)) {
					throw new DoNotIgnore("Duplicate variable " + k.first);
				}
				vars0.put(k.first, k.second);
			}
		}

		public OplCtx() {
			vars0 = new LinkedHashMap<>();
		}

		public S get(V s) {
			S ret = vars0.get(s);
			if (s == null) {
				throw new DoNotIgnore("Unbound var " + s);
			}
			return ret;
		}

		public void validate(OplSig<S,?,V> oplSig) {
			for (V k : vars0.keySet()) {
				S v = get(k);
				if (v == null) {
					throw new RuntimeException("Cannot infer type for " + k);
				}
				if (!oplSig.sorts.contains(v)) {
					throw new DoNotIgnore("Context has bad sort " + v);
				}
			}
		}

		public List<S> values() {
			List<S> ret = new LinkedList<>();
			for (V k : vars0.keySet()) {
				S v = vars0.get(k);
				ret.add(v);
			}
			return ret;
		}
		
		public List<Pair<V, S>> values2() {
			List<Pair<V, S>> ret = new LinkedList<>();
			for (V k : vars0.keySet()) {
				S v = vars0.get(k);
				ret.add(new Pair<>(k, v));
			}
			return ret;
		}
		
		public List<V> names() {
			List<V> ret = new LinkedList<>(vars0.keySet());
			return ret;
		}

		public <SS> OplCtx<SS, V> makeEnv(List<SS> env) {
			List<Pair<V, SS>> in = new LinkedList<>();
			int i = 0;
			for (V k : vars0.keySet()) {
				in.add(new Pair<>(k, env.get(i)));
				i++;
			}
			return new OplCtx<>(in);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((vars0 == null) ? 0 : vars0.hashCode());
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
			OplCtx other = (OplCtx) obj;
			if (vars0 == null) {
				if (other.vars0 != null)
					return false;
			} else if (!vars0.equals(other.vars0))
				return false;
			return true;
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
	
	public static class OplUnSat extends OplExp {
		
		String I;
		
		public OplUnSat(String I) {
			this.I = I;
		}
		
		//TODO to use with saturate_easy, should turn back into theory
		//TODO this type signature is totally wrong, is not inverse of saturate_easy
		//TODO not sure what this is, but can only desaturate things that were first saturted, not arbitrary models
		public static <S,C,V,X> OplPres<S,C,V,X> desaturate(String S, OplSetInst<S,C,X> I) {
			OplSig<S,C,V> sig = (OplSig<S, C, V>) I.sig0; //TODO
			
			Map<X, S> gens = new HashMap<>();
			for (S s : sig.sorts) {
				for (X c : I.sorts.get(s)) {
					gens.put(c, s);
				}
			}
			
			List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations = new LinkedList<>();
			for (C f : sig.symbols.keySet()) {
				List<S> arg_ts = sig.symbols.get(f).first;
				if (arg_ts.isEmpty()) {
					continue;
				}
				Map<Integer, List<X>> l = new HashMap<>();
				int i = 0;
				for (S t : arg_ts) {
					l.put(i, new LinkedList<>(I.sorts.get(t)));
					i++;
				}
				List<LinkedHashMap<Integer, X>> m = FinSet.homomorphs(l);
				for (LinkedHashMap<Integer, X> a : m) {
					List<X> arg1 = new LinkedList<>();
					List<OplTerm<Chc<C,X>,V>> arg2 = new LinkedList<>();
					for (int j = 0; j < i; j++) {
						arg1.add(a.get(j));
						arg2.add(new OplTerm<>(Chc.inRight(a.get(j)), new LinkedList<>()));
					}
					OplTerm<Chc<C,X>,V> termA = new OplTerm<>(Chc.inLeft(f), arg2);
					OplTerm<Chc<C,X>,V> termB = new OplTerm<>(Chc.inRight(I.symbols.get(f).get(arg1)), new LinkedList<>());
					equations.add(new Pair<>(termA, termB));
				} 
			}
			
			OplPres<S,C,V,X> ret = new OplPres<S,C,V,X>(new HashMap<>(), S, sig, gens, equations);
		//	System.out.println(sig.inject());
			//System.out.println(ret);
		//	ret.convert(fr, sig.inject()); //needed
			return ret; 
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplUberSat extends OplExp {
		
		String I, P;
		
		public OplUberSat(String I, String P) {
			this.I = I;
			this.P = P;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		private static <S,C,V,X,Y> OplSetInst<S, C, KBExp<Chc<Chc<C,X>, JSWrapper>, V>> 
		inject(OplSetInst<S, C, OplTerm<Chc<C,X>, V>> I, OplJavaInst I0) {
			Map<S, Set<KBExp<Chc<Chc<C, X>, JSWrapper>, V>>> sorts = new HashMap<>();
			for (S s : I.sorts.keySet()) {
				Set<KBExp<Chc<Chc<C,X>, JSWrapper>,V>> set = new HashSet<>();
				for (OplTerm<Chc<C, X>, V> e : I.sorts.get(s)) {
					OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(e);
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
					set.add(z);
				}
				sorts.put(s, set);
			}
			
			Map<C, Map<List<KBExp<Chc<Chc<C, X>, JSWrapper>, V>>, KBExp<Chc<Chc<C, X>, JSWrapper>, V>>> symbols = new HashMap<>();
			for (C c : I.symbols.keySet()) {
				Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>> m = I.symbols.get(c);
				Map<List<KBExp<Chc<Chc<C, X>, JSWrapper>, V>>, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> n = new HashMap<>();
				for (List<OplTerm<Chc<C, X>, V>> a : m.keySet()) {
					List<KBExp<Chc<Chc<C, X>, JSWrapper>, V>> l = new LinkedList<>();
					for (OplTerm<Chc<C, X>, V> e : a) {
						OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(e);
						KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
						KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
						l.add(z);
					}
					OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(m.get(a));
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
					n.put(l, z);
				}
				symbols.put(c, n);
			}
			
			 OplSetInst<S, C, KBExp<Chc<Chc<C,X>, JSWrapper>, V>> ret = new OplSetInst<>(sorts, symbols, I.sig);
			 ret.validate(I.sig0);
			 return ret;
		}
		
		public static <S,C,V,X> OplSetInst<S, C, KBExp<Chc<Chc<C, X>, JSWrapper>, V>> 
		saturate(OplJavaInst I0, OplPres<S,C,V,X> P0) {
			OplSetInst<S, C, OplTerm<Chc<C,X>, V>> I = OplSat.saturate(P0);
			OplSetInst<S, C, KBExp<Chc<Chc<C,X>, JSWrapper>, V>> J = inject(I, I0);
			
			return J;
		}
		
	}
	
	
	public static class OplSat extends OplExp {
		
		String I;
		
		public OplSat(String I) {
			this.I = I;
		}
		
		//works fine
		public static <S,C,V,X> OplSetInst<S, Chc<C, X>, OplTerm<Chc<C, X>, V>> 
		saturateEasy(OplPres<S,C,V,X> P)  {
			OplSig<S,Chc<C,X>,V> sig = P.toSig();
			return sig.saturate(P.S);
		}
		
		public static <S,C,V,X> OplSetInst<S,C,OplTerm<Chc<C,X>,V>> 
		saturate(OplPres<S,C,V,X> P)  {
			OplSig<S,Chc<C,X>,V> sig = P.toSig();
			OplToKB<S,Chc<C,X>,V> kb = sig.getKB();
			
			Map<S, Set<OplTerm<Chc<C,X>,V>>> sorts = kb.doHoms();
			
			
			Map<C, Map<List<OplTerm<Chc<C,X>,V>>, OplTerm<Chc<C,X>,V>>> symbols = new HashMap<>();
			for (C f : P.sig.symbols.keySet()) {
				Pair<List<S>, S> ty = P.sig.symbols.get(f);
				Map<Integer, List<OplTerm<Chc<C,X>,V>>> args = new HashMap<>();
				int i = 0;
				for (S t : ty.first) {
					args.put(i++, new LinkedList<>(sorts.get(t)));
				}
				List<LinkedHashMap<Integer, OplTerm<Chc<C, X>, V>>> cands = FinSet.homomorphs(args);
				Map<List<OplTerm<Chc<C,X>, V>>, OplTerm<Chc<C,X>, V>> out = new HashMap<>();
				for (LinkedHashMap<Integer, OplTerm<Chc<C, X>, V>> cand : cands) {
					List<OplTerm<Chc<C,X>, V>> actual = new LinkedList<>();
					for (int j = 0; j < i; j++) {
						actual.add(cand.get(j));
					}
					OplTerm<Chc<C,X>, V> to_red = new OplTerm<>(Chc.inLeft(f), actual);
					OplTerm<Chc<C,X>, V> red = OplToKB.convert(kb.nf(OplToKB.convert(to_red)));
					out.put(actual, red);
				}
				symbols.put(f, out);
			}
						
			OplSetInst<S,C,OplTerm<Chc<C,X>,V>> ret = new OplSetInst<>(sorts, symbols, P.S);
			ret.validate(P.sig);
			return ret; 
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplSig<S, C, V> extends OplExp {
		
		private OplToKB<S,C,V> kb;
		public OplToKB<S,C,V> getKB() {
			if (kb != null) {
				return kb;
			}
			kb = new OplToKB<S,C, V>(fr, this);
			return kb;
		}
		
/*		public OplSig<S, C, V> attachKB(OplJavaInst I0) {
			OplSig<S, C, V> ret = new OplSig<>(fr, prec, sorts, symbols, equations);
			ret.kb = new OplToKB<S,C, V>(fr, this, I0);
			return ret;
		}
		*/

		//public OplToKB<S,C,V> getKB(OplJavaInst I) {
		//	return new OplToKB<S,C, V>(fr, this, I);
		//}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((equations == null) ? 0 : equations.hashCode());
			result = prime * result + ((sorts == null) ? 0 : sorts.hashCode());
			result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
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
			OplSig other = (OplSig) obj;
			if (equations == null) {
				if (other.equations != null)
					return false;
			} else if (!equations.equals(other.equations))
				return false;
			if (sorts == null) {
				if (other.sorts != null)
					return false;
			} else if (!sorts.equals(other.sorts))
				return false;
			if (symbols == null) {
				if (other.symbols != null)
					return false;
			} else if (!symbols.equals(other.symbols))
				return false;
			return true;
		}

		public static <X,C,V> OplTerm<Chc<C,X>, V> inject(OplTerm<C,V> e) {
			if (e.var != null) {
				return new OplTerm<>(e.var);
			}
			List<OplTerm<Chc<C,X>,V>> args0 = new LinkedList<>();
			for (OplTerm<C, V> a : e.args) {
				args0.add(inject(a));
			}
			return new OplTerm<Chc<C,X>, V>(Chc.inLeft(e.head), args0);
		}
		
		public <X> OplSig<S, Chc<C,X>, V> inject() {
			Map<Chc<C, X>, Pair<List<S>, S>> symbols0 = new HashMap<>();
			for (C f : symbols.keySet()) {
				Pair<List<S>, S> s = symbols.get(f);
				symbols0.put(Chc.inLeft(f), s);
				
			}
			List<Triple<OplCtx<S, V>, OplTerm<Chc<C,X>, V>, OplTerm<Chc<C,X>, V>>> equations0 = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : equations) {
				equations0.add(new Triple<>(eq.first, inject(eq.second), inject(eq.third)));
			}
			return new OplSig<S, Chc<C,X>, V>(fr, new HashMap<>(), sorts, symbols0, equations0); 
		}
		
		@Override
		public JComponent display() {
			return display(true);
		}
		public JComponent display(Boolean b) {
			JTabbedPane ret = new JTabbedPane();

			FQLTextPanel p = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			ret.add(p, "Text");
			
			try {
				JPanel pp = makeTiny2(b);
				ret.add(pp, "KB");				
				try {
					JPanel qq = makeTiny();
					ret.add(qq, "Hom");				
				} catch (Exception ex) {
					ex.printStackTrace();
						p = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "exception: " + ex.getMessage());
						ret.add(p, "Hom");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("made it");
				p = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", "exception: " + ex.getMessage());
				ret.add(p, "KB");
			}

			return ret;
		}
		
		JPanel makeTiny2(boolean b) {
			OplToKB kb = getKB();
			JPanel ret = new JPanel(new GridLayout(1,1));
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			ret.add(pane);
			
			JPanel top = new JPanel();
			
			JTextField src = new JTextField(16);
			JTextField dst = new JTextField(16);
			
			JButton go = new JButton("Reduce");
			go.addActionListener(x -> {   
				try {
					OplTerm t = OplParser.parse_term(symbols, src.getText());
					dst.setText(kb.nf(OplToKB.convert(t)).toString());
				} catch (Exception ex) {
					dst.setText(ex.getMessage());
				}
			});

			top.add(new JLabel("Input term:"));
			top.add(src);
			top.add(go);
			top.add(new JLabel("Result:"));
			top.add(dst);

			FQLTextPanel bot = new FQLTextPanel(BorderFactory.createEtchedBorder(), "Re-write rules", strip(kb.printKB()));
			
			pane.add(top);
			pane.add(bot);
			
			if (b) {
				return ret;
			} else {
				return bot;
			}
			
		}
		
		JPanel makeTiny() {
			OplToKB kb = getKB();
			JPanel ret = new JPanel(new GridLayout(1,1));
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			ret.add(pane);
			
			JPanel top = new JPanel(new GridLayout(3,1));
			JPanel p1 = new JPanel();
			JPanel p2 = new JPanel();
			JPanel p3 = new JPanel();
			
			JTextField src = new JTextField(32);
			JTextField dst = new JTextField(32);
			p1.add(new JLabel("source (sep by ,):"));
			p1.add(src);
			p2.add(new JLabel("target:"));
			p2.add(dst);

			FQLTextPanel bot = new FQLTextPanel(BorderFactory.createEtchedBorder(), "Result", "");

			JButton go = new JButton("Compute hom set");
			go.addActionListener(x -> {   
				String[] l = src.getText().split(",");
				String r = dst.getText();
				List<String> l0 = new LinkedList<>();
				for (String j : l) {
					String j2 = j.trim();
					if (j2.length() > 0) {
						l0.add(j2);
					}
				}			
				//boolean finished = false;
					Runnable runnable = new Runnable() {
						public void run() {
							try {
							Collection<Pair<OplCtx<S,V>, OplTerm<C,V>>> z = kb.hom0(l0, r);
							List<String> u = z.stream().map(o -> { return strip( o.first + " |- " + OplToKB.convert(o.second).toString() ); }).collect(Collectors.toList());
							if (u.isEmpty()) {
								bot.setText("empty");
							} else {
								bot.setText(Util.sep(u, "\n\n"));
							}
							} catch (Exception ex) {
								ex.printStackTrace();
								bot.setText(ex.getMessage());
							}
							//finished = true;
						}
					};
					Thread t = new Thread(runnable);
					try {
						t.start();
			//		try {
						t.join(DEBUG.debug.opl_hom_its);
						
						t.stop();
						if (bot.getText().equals("")) {
							bot.setText("Timeout");
						}

					} catch (Exception ex) {
						ex.printStackTrace();
						throw new RuntimeException("Timout");
					}
					
		//		} catch (Exception ex) {
			//		ex.printStackTrace();
			//		bot.setText(ex.getMessage());
			//	}
			});
			p3.add(go);
			top.add(p1);
			top.add(p2);
			top.add(p3);
			
			pane.add(top);
			pane.add(bot);
			
			return ret;
		}
		
		public Map<C, Integer> prec;
		public Set<S> sorts;
		public Map<C, Pair<List<S>, S>> symbols;
		public List<Triple<OplCtx<S,V>, OplTerm<C,V>, OplTerm<C,V>>> equations;
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			ret += "\t\t" + Util.sep(sorts, ", ") + ";\n";
			
			ret += "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (C k : symbols.keySet()) {
				Pair<List<S>, S> v = symbols.get(k);
				String s;
				if (v.first.isEmpty()) {
					s = strip(k.toString())+ " : " + v.second;
								
				} else {
					s = strip(k.toString())+ " : " + Util.sep(v.first, ", ") + " -> " + v.second;
				}
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist,",\n\t\t") + ";\n";
			
			ret += "\tequations\n";
			List<String> elist = new LinkedList<>();
			for (Triple<OplCtx<S,V>, OplTerm<C,V>, OplTerm<C,V>> k : equations) {
				String z = k.first.vars0.size() == 0 ? "" : "forall ";
				String y = k.first.vars0.size() == 0 ? "" : ". ";
				String s = z + k.first + y + strip(k.second.toString()) + " = " + strip(k.third.toString());
				elist.add(s);
			}

			ret += "\t\t" + Util.sep(elist, ",\n\t\t") + ";\n";
			
			return "theory {\n" + ret + "}";
		}

		public OplSig(Iterator<V> fr, Map<C, Integer> prec, Set<S> sorts, 
				Map<C, Pair<List<S>, S>> symbols,
				List<Triple<OplCtx<S,V>, OplTerm<C,V>, OplTerm<C,V>>> equations) {
			this.sorts = sorts;
			this.symbols = symbols;
			this.equations = equations;
			this.prec = prec;
			this.fr = fr;
			validate(); 
		}
		private Iterator<V> fr;
		void validate() {
			for (C k : symbols.keySet()) {
				Pair<List<S>, S> v = symbols.get(k);
				if (!sorts.contains(v.second)) {
 					throw new DoNotIgnore("Bad codomain " + v.second + " for " + k );
				}
				for (S a : v.first) {
					if (!sorts.contains(a)) {
						throw new DoNotIgnore("Bad argument sort " + a + " for " + k);
					}
				}
			}
			for (Triple<OplCtx<S,V>, OplTerm<C,V>, OplTerm<C,V>> eq0 : equations) {
				Triple<OplCtx<S,V>, OplTerm<C,V>, OplTerm<C,V>> eq = new Triple<>(inf(eq0), eq0.second, eq0.third);
				eq0.first = eq.first;
				eq.first.validate(this);
				S t1 = eq.second.type(this, eq.first);
				S t2 = eq.third.type(this, eq.first);
				if (!t1.equals(t2)) {
					throw new DoNotIgnore("Domains do not agree in " + eq.second + " = " + eq.third + ", are " + t1 + " and " + t2);
				}
			}
		}

		private OplCtx<S,V> inf(Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq0) {
			try {
				KBExp<C,V> lhs = OplToKB.convert(eq0.second);
				KBExp<C,V> rhs = OplToKB.convert(eq0.third);
				Map<V, S> m = new HashMap<>(eq0.first.vars0);
				S l = lhs.typeInf(symbols, m);
				S r = rhs.typeInf(symbols, m);
				if (l == null && r == null) {
					throw new DoNotIgnore("Cannot infer types for " + lhs + " and " + rhs);
				}
				if (l == null && lhs.isVar && rhs != null) {
					m.put(lhs.getVar().var, r);
				}
				if (r == null && rhs.isVar && lhs != null) {
					m.put(rhs.getVar().var, l);
				}
				return new OplCtx<>(m);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new DoNotIgnore(ex.getMessage() + "\n\n in " + this);
					
			}
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public Pair<List<S>, S> getSymbol(C var) {
			Pair<List<S>, S> ret = symbols.get(var);
			if (ret == null) {
				throw new DoNotIgnore("Unknown symbol " + var);
			}
			return ret;
		}
		
		public OplSetInst<S,C,OplTerm<C,V>> saturate(String name)  {
			
			Map<S, Set<OplTerm<C, V>>> sorts0 = getKB().doHoms();
			
			Map<C, Map<List<OplTerm<C,V>>, OplTerm<C,V>>> symbols0 = new HashMap<>();
			for (C f : symbols.keySet()) {
				Pair<List<S>, S> ty = symbols.get(f);
				Map<Integer, List<OplTerm<C, V>>> args = new HashMap<>();
				int i = 0;
				for (S t : ty.first) {
					args.put(i++, new LinkedList<>(sorts0.get(t)));
				}
				List<LinkedHashMap<Integer, OplTerm<C, V>>> cands = FinSet.homomorphs(args);
				Map<List<OplTerm<C, V>>, OplTerm<C, V>> out = new HashMap<>();
				for (LinkedHashMap<Integer, OplTerm<C, V>> cand : cands) {
					List<OplTerm<C, V>> actual = new LinkedList<>();
					for (int j = 0; j < i; j++) {
						actual.add(cand.get(j));
					}
					OplTerm<C, V> to_red = new OplTerm<>(f, actual);
					OplTerm<C, V> red = OplToKB.convert(kb.nf(OplToKB.convert(to_red)));
					out.put(actual, red);
				}
				symbols0.put(f, out);
			}
						
			OplSetInst<S,C,OplTerm<C,V>> ret = new OplSetInst<>(sorts0, symbols0, name);
			ret.validate(this);
			return ret; 
		}

		
		
	}
	
	public static class OplPres<S,C,V,X> extends OplExp {
		
		public String S;
		public Map<X, Integer> prec;
		public Map<X, S> gens;
		public List<Pair<OplTerm<Chc<C,X>,V>, OplTerm<Chc<C,X>,V>>> equations;
		public OplSig<S,C,V> sig;
		public OplSig<S,Chc<C,X>,V> toSig; 

		private static <S,C,X,V> OplTerm<Chc<C,X>, V> conv(Map<X, S> gens, OplTerm<Object, V> e) {
			if (e.var != null) {
				return new OplTerm<>(e.var);
			}
			List<OplTerm<Chc<C,X>, V>> args0 = new LinkedList<>();
			for (OplTerm<Object, V> arg : e.args) {
				args0.add(conv(gens, arg));
			}
			if (gens.get(e.head) != null) {
				return new OplTerm<>(Chc.inRight((X) e.head), args0);
			} else {
				return new OplTerm<>(Chc.inLeft((C) e.head), args0);
			}
		}
		
		public static <S,C,V,X> OplPres<S,C,V,X> OplPres(Map<X, Integer> prec, String S, OplSig<S,C,V> sig, Map<X, S> gens,
				List<Pair<OplTerm<Object,V>, OplTerm<Object,V>>> equations) {
			
			List<Pair<OplTerm<Chc<C,X>,V>, OplTerm<Chc<C,X>,V>>> eqs = new LinkedList<>();
			for (Pair<OplTerm<Object, V>, OplTerm<Object, V>> eq : equations) {
				eqs.add(new Pair<>(conv(gens, eq.first), conv(gens, eq.second)));
			}
			
			return new OplPres<>(prec, S, sig, gens, eqs);
		}
		
		public OplSig<S,Chc<C,X>,V> toSig() {
			if (toSig != null) {
				return toSig;
			}
			OplSig<S, Chc<C,X>, V> sig0 = sig.inject();
			
			Map<Chc<C,X>, Pair<List<S>, S>> symbols0 = new HashMap<>(sig0.symbols);
			for (X k : gens.keySet()) {
				symbols0.put(Chc.inRight(k), new Pair<>(new LinkedList<>(), gens.get(k)));
			}

			List<Triple<OplCtx<S,V>, OplTerm<Chc<C,X>,V>, OplTerm<Chc<C,X>,V>>> equations0 = new LinkedList<>(sig0.equations);
			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> eq : equations) {
				equations0.add(new Triple<>(new OplCtx<>(), eq.first, eq.second));
			}
			
			Map<Chc<C,X>, Integer> m = new HashMap<>();
			for (C c : sig.symbols.keySet()) {
				Integer i = sig.prec.get(c);
				if (i != null) {
					m.put(Chc.inLeft(c), i);
				}
			}
			for (X x : gens.keySet()) {
				Integer i = prec.get(x);
				if (i != null) {
					m.put(Chc.inRight(x), i);
				}
			}
			
			toSig = new OplSig<S,Chc<C,X>,V>(sig.fr, m, sig.sorts, symbols0, equations0);
			return toSig;
		}
		
		@Override
		public JComponent display() {
			return toSig().display(false);
		}
		
		public OplPres(Map<X, Integer> prec, String S, OplSig<S,C,V> sig, Map<X, S> gens,
				List<Pair<OplTerm<Chc<C,X>,V>, OplTerm<Chc<C,X>,V>>> equations) {
			this.S = S;
			this.gens = gens;
			this.equations = equations;
			this.sig = sig;
			this.prec = prec;
		}

		@Override
		public String toString() {
			String ret = "";
			ret += "\tgenerators\n";
			List<String> slist = new LinkedList<>();
			for (X k : gens.keySet()) {
				String v = gens.get(k).toString();
				String s = strip(k.toString()) + " : " + v;
				slist.add(s);
			}
			ret += "\t\t" + Util.sep(slist,",\n\t\t") + ";\n";
			
			ret += "\tequations\n";
			List<String> elist = new LinkedList<>();
			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> k : equations) {
				String s = strip(k.first + " = " + k.second);
				elist.add(s);
			}

			ret += "\t\t" + Util.sep(elist, ",\n\t\t") + ";\n";
			
			return "presentation {\n" + ret + "} : S";
		}
		
	

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	private static String strip(String s) {
		if (!DEBUG.debug.opl_pretty) {
			return s;
		}
		String ret = s.replace("inl ", "").replace("inr ", "").replace("()", "").replace("forall . ", "").trim();
		if (ret.startsWith("|- ")) {
			ret = ret.substring(3);
		}
		return ret;
		//return ret.replace("JS[", "").replace("]", "");
	}
	
	public static class OplMapping<S1,C1,V,S2,C2> extends OplExp {
		Map<S1, S2> sorts;
		Map<C1, Pair<OplCtx<S2, V>, OplTerm<C2,V>>> symbols;
		
		String src0, dst0;
		OplSig<S1,C1,V> src;
		OplSig<S2,C2,V> dst;
				
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
			for (S1 n : sorts.keySet()) {
				S2 f = sorts.get(n);
				rs.add(new Object[] {n, f});
			}
			list.add(Util.makeTable(BorderFactory.createEmptyBorder(), "Sorts", 
					rs.toArray(new Object[][] { }), new Object[] {src0 + " (in)", dst0 + " (out)"} ));
			
			List<Object[]> rows = new LinkedList<>();
			for (C1 n : symbols.keySet()) {
					Pair<OplCtx<S2, V>, OplTerm<C2, V>> f = symbols.get(n);
					Object[] row = new Object[] { strip(n.toString()), strip("forall " + f.first + ". " + f.second) }; 
					rows.add(row);
				} 
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), "Symbols", 
						rows.toArray(new Object[][] { }), new Object[] {src0 + " (in)", dst0 + " (out)"}));
			
			return Util.makeGrid(list);			
		}
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (S1 k : sorts.keySet()) {
				S2 v = sorts.get(k);
				sortsX.add(k + " -> " + v);
			}
			ret += "\t\t" + Util.sep(sortsX, ",\n\t\t") + ";\n";
			
			ret += "\tsymbols\n";
			List<String> symbolsX = new LinkedList<>();
			for (C1 k : symbols.keySet()) {
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> v = symbols.get(k);
				String z = v.first.vars0.size() == 0 ? "" : "forall ";
				String y = v.first.vars0.size() == 0 ? "" : " . ";
				symbolsX.add(strip(k.toString()) + " -> " + z + v.first + y + v.second.toString());
			}
			ret += "\t\t" + Util.sep(symbolsX, ",\n\t\t") + ";\n";

			return "mapping {\n" + ret + "} : " + src0 + " -> " + dst0;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		public void validate(OplSig<S1,C1,V> src, OplSig<S2,C2,V> dst) {
			this.src = src;
			this.dst = dst;
			for (S1 k : sorts.keySet()) {
				if (!src.sorts.contains(k)) {
					throw new RuntimeException("Extra sort: " + k);
				}
				S2 v = sorts.get(k);
				if (!dst.sorts.contains(v)) {
					throw new RuntimeException("Bad target sort " + v + " from " + k);
				}
			}
			for (S1 k : src.sorts) {
				if (!sorts.keySet().contains(k)) {
					throw new RuntimeException("Missing sort: " + k);
				}
			}
			for (C1 k : src.symbols.keySet()) {
				if (!symbols.keySet().contains(k)) {
					throw new RuntimeException("Extra symbol: " + k);
				}
			}
			for (C1 k : symbols.keySet()) {
				if (!src.symbols.containsKey(k)) {
					throw new RuntimeException("Extra symbol: " + k);
				}
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> v = symbols.get(k);
				v.second = replaceVarsByConsts(v.first, v.second);
				v.first = inf(v);
				S2 t = v.second.type(dst, v.first);
				if (t == null) {
					throw new RuntimeException("Cannot type " + v.second + " in context [" + v.first  + "]. ");
				}
			
				if (!t.equals(sorts.get(src.symbols.get(k).second))) {
					throw new RuntimeException("Symbol " + k + " returns a " + src.symbols.get(k).second + " but transforms to " + t);
				}
				List<S2> trans_t = src.symbols.get(k).first.stream().map(x -> sorts.get(x)).collect(Collectors.toList());
				if (!v.first.values().equals(trans_t)) {
					throw new RuntimeException("Symbol " + k + " inputs a " + v.first.values() + " but transforms to " + trans_t + " in \n\n " + this);
				}
			}
			
			for (Triple<OplCtx<S1, V>, OplTerm<C1, V>, OplTerm<C1, V>> eq : src.equations) {
				OplTerm<C2, V> l = subst(eq.second);
				OplTerm<C2, V> r = subst(eq.second);
				if (DEBUG.debug.opl_validate) {

				KBExp<C2, V> l0 = dst.getKB().nf(OplToKB.convert(l));
				KBExp<C2, V> r0 = dst.getKB().nf(OplToKB.convert(r));
				if (!l0.equals(r0)) {
					throw new RuntimeException("Not preserved: " + l + " = " + r + " transforms to " + l0 + " = " + r0);
				}
				}

			} 
		}
		
		private OplCtx<S2,V> inf(Pair<OplCtx<S2, V>, OplTerm<C2, V>> eq0) {
			KBExp<C2,V> lhs = OplToKB.convert(eq0.second);
			Map<V, S2> m = new HashMap<>(eq0.first.vars0);
			lhs.typeInf(dst.symbols, m);
			return new OplCtx<>(m);
		}

		
		private OplTerm<C2, V> replaceVarsByConsts(OplCtx<S2, V> g, OplTerm<C2, V> e) {
			if (e.var != null) {
				if (dst.symbols.containsKey(e.var)) {
					if (g.vars0.containsKey(e.var)) {
						throw new RuntimeException("Attempt to shadow " + e.var);
					}
					return new OplTerm<C2, V>((C2) e.var, new LinkedList<>());
				}
				return e;
			}
			return new OplTerm<C2, V>(e.head, e.args.stream().map(x -> replaceVarsByConsts(g, x)).collect(Collectors.toList()));
		}

		public OplMapping(Map<S1, S2> sorts,
				Map<C1, Pair<OplCtx<S2,V>, OplTerm<C2,V>>> symbols, String src0, String dst0) {
			this.sorts = sorts;
			this.symbols = symbols;
			this.src0 = src0;
			this.dst0 = dst0;
		}
		
		public <X,Y> OplPresTrans<S2,C2,V,X,Y> sigma(OplPresTrans<S1,C1,V,X,Y> h) {
			OplPres<S1,C1,V,X> I = h.src;
			
			OplPres<S2,C2,V,X> I0 = sigma(h.src);
			OplPres<S2,C2,V,Y> J0 = sigma(h.dst);
			
			Map<S2, Map<X, OplTerm<Chc<C2, Y>, V>>> map = new HashMap<>();
			for (S2 s : I0.sig.sorts) {
				map.put(s, new HashMap<>());	
			}
			for (S1 s : I.sig.sorts) {
				for (X x : I.gens.keySet()) {
					S1 t = I.gens.get(x);
					if (!s.equals(t)) {
						continue;
					}
					map.get(sorts.get(s)).put(x, sigma(h.map.get(s).get(x)));
				}
			}

			//validates
			OplPresTrans<S2,C2,V,X,Y> ret = new OplPresTrans<S2,C2,V,X,Y>(map, "?", "?", I0, J0);
			return ret;
		}
		
		public <X> OplSetTrans<S1,C1,X> delta(OplSetTrans<S2,C2,X> h) {
			if (!h.src0.sig.equals(dst0)) {
				throw new RuntimeException("Source of transform, " + h.src0 + " does not have theory " + dst0);
			}
			if (!h.dst0.sig.equals(dst0)) {
				throw new RuntimeException("Target of transform, " + h.src0 + " does not have theory " + dst0);
			}

			OplSetInst<S1,C1,X> srcX = delta(h.src0);
			OplSetInst<S1,C1,X> dstX = delta(h.dst0);
			
			Map<S1, Map<X, X>> sortsX = new HashMap<>();
			for (S1 s : src.sorts) {
				Map<X, X> m = new HashMap<>();
				for (X v : srcX.sorts.get(s)) {
					m.put(v, h.sorts.get(sorts.get(s)).get(v));
				}
				sortsX.put(s, m);
			}
			
			OplSetTrans<S1,C1,X> ret = new OplSetTrans<>(sortsX, "?", "?");
			ret.validate(src, srcX, dstX);
			return ret;
		}
		
		public <X> OplPres<S2,C2,V,X> sigma(OplPres<S1,C1,V,X> I) {
			if (!src.equals(I.sig)) {
				throw new RuntimeException("Source of mapping " + src0 + " does not match " + I.S);
			}
			
			Map<X, S2> sym = new HashMap<>();
			for (X c : I.gens.keySet()) {
				S1 t = I.gens.get(c);
				sym.put(c, sorts.get(t));
			}
			
			List<Pair<OplTerm<Chc<C2, X>, V>, OplTerm<Chc<C2, X>, V>>> eqs = new LinkedList<>();
			for (Pair<OplTerm<Chc<C1, X>, V>, OplTerm<Chc<C1, X>, V>> eq : I.equations) {
				eqs.add(new Pair<>(sigma(eq.first), sigma(eq.second)));
			}
			
			OplPres<S2,C2,V,X> ret = new OplPres<S2,C2,V,X>(I.prec, dst0, dst, sym, eqs);
			//ret.convert(fr, dst); //needed
			return ret;
		}
		
		private OplCtx<S2,V> sigma(OplCtx<S1,V> t) {
			List<Pair<V, S2>> l = t.values2().stream().map(x -> { return new Pair<>(x.first, sorts.get(x.second)); }).collect(Collectors.toList());
			return new OplCtx<>(l);				
		}
		
		private OplTerm<C2,V> subst(OplTerm<C1,V> t) {
			if (t.var != null) {
				return new OplTerm<>(t.var);
			} else {
				List<OplTerm<C2,V>> l = new LinkedList<>();
				for (OplTerm<C1, V> a : t.args) {
					l.add(subst(a));
				}
				
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> h = symbols.get(t.head);
				if (h == null) {
					throw new RuntimeException();
				}
				
				Map<V, OplTerm<C2,V>> s = new HashMap<>();
				List<Pair<V, S2>> r = h.first.values2();
				int i = 0;
				for (Pair<V, S2> p : r) {
					s.put(p.first, l.get(i++));
				}
				
				OplTerm<C2,V> ret = h.second;
				return ret.subst(s);
			}
		}
		
		private <X> OplTerm<Chc<C2,X>,V> sigma(OplTerm<Chc<C1,X>,V> t) {
			if (t.var != null) {
				return new OplTerm<>(t.var);
			} else {
				List<OplTerm<Chc<C2, X>,V>> l = new LinkedList<>();
				for (OplTerm<Chc<C1, X>, V> a : t.args) {
					l.add(sigma(a));
				}
				
				if (!t.head.left) {
					return new OplTerm<>(Chc.inRight(t.head.r), l);
				}
				
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> h = symbols.get(t.head.l);				
				
				Map<V, OplTerm<Chc<C2,X>,V>> s = new HashMap<>();
				List<Pair<V, S2>> r = h.first.values2();
				int i = 0;
				for (Pair<V, S2> p : r) {
					s.put(p.first, l.get(i++));
				}
				
				OplTerm<Chc<C2,X>,V> ret = dst.inject(h.second);
				return ret.subst(s);
			}
		}
		
		public <X> OplSetInst<S1,C1,X> delta(OplSetInst<S2,C2,X> J) {
			if (!dst0.equals(J.sig)) {
				throw new RuntimeException("Target of mapping " + dst + " does not match " + J.sig);
			}
			Map<S1, Set<X>> sortsX = new HashMap<>();
			for (S1 s : src.sorts) {
				S2 s0 = sorts.get(s);
				sortsX.put(s, J.sorts.get(s0));
			}
			
			Map<C1, Map<List<X>, X>> symbolsX = new HashMap<>();
			for (C1 n : src.symbols.keySet()) {
				Pair<List<S1>, S1> f_t = src.symbols.get(n);
				List<Set<X>> f_a = f_t.first.stream().map(x -> sortsX.get(x)).collect(Collectors.toList());
				List<List<X>> inputs = prod(f_a);
				Pair<OplCtx<S2, V>, OplTerm<C2, V>> f = symbols.get(n);
				Map<List<X>, X> map = new HashMap<>();
				for (List<X> input : inputs) {
					X output = f.second.eval(dst, J, f.first.makeEnv(input));
					if (map.containsKey(input)) {
						throw new RuntimeException();
					}
					map.put(input, output);
				}
				symbolsX.put(n, map);
			}
			
			OplSetInst<S1,C1,X> I = new OplSetInst<>(sortsX, symbolsX, src0);
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
	
	public static class OplSigma extends OplExp {
		String F, I;

		public OplSigma(String f, String i) {
			F = f;
			I = i;
		}

		@Override
		public String toString() {
			return "sigma " + F + " " + I;
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
	
	public static class OplSetInst<S, C, X> extends OplExp {
		
		public int size() {
			int ret = 0;
			for (S s : sorts.keySet()) {
				ret += sorts.get(s).size();
			}
			return ret;
		}
		public Map<S, Set<X>> sorts;
		String sig;
		public Map<C, Map<List<X>, X>> symbols;
		public OplSig<S, C, ?> sig0;
		
		@Override
		public String toString() {
			String ret = "\tsorts\n";
			List<String> sortsX = new LinkedList<>();
			for (S k : sorts.keySet()) {
				sortsX.add(k + " -> {" + Util.sep(sorts.get(k).stream().map(x -> strip(x.toString())).collect(Collectors.toList()), ", ") + "}");
			}
			ret += "\t\t" + Util.sep(sortsX, ", ") + ";\n";
			
			ret += "\tsymbols\n";
			List<String> slist = new LinkedList<>();
			for (C k : symbols.keySet()) {
				Map<List<X>, X> v = symbols.get(k);
				List<String> u = new LinkedList<>();
				for (List<X> i : v.keySet()) {
					X j = v.get(i);
					u.add("((" + Util.sep(i.stream().map(x -> strip(x.toString())).collect(Collectors.toList()), ",") + "), " + strip(j.toString()) + ")");
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
			
			JComponent tables = makeTables(new HashSet());
			jtp.addTab("Tables", tables);
			
			return jtp;
		}
		
//		private static String strip(String s) {
	//		return s.replace("inl ", "").replace("inr ", "").replace("()", "");
//		}
		
		public JComponent makeTables(Set skip) {
			//System.out.println(this);
			List<JComponent> list = new LinkedList<>();
			
			Map<String, JComponent> all = new HashMap<>();
			
		//	List<S> keys = new LinkedList<>(sorts.keySet());
			List<C> keys2 = new LinkedList<>(symbols.keySet());

			Comparator comp = new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return o1.toString().compareTo(o2.toString());
				}
			};
			
	//		Map<S, Set<C>> unary = new HashMap<>();
			for (S n : sorts.keySet()) {
				List<S> t0 = Collections.singletonList(n);
				List<C> set = new LinkedList<>();
				for (C f : symbols.keySet()) {
					Pair<List<S>, S> t = sig0.symbols.get(f);
					if (t.first.equals(t0)) {
						set.add(f);
						keys2.remove(f);
					}
				}
				set.sort(comp);
				
				List<Object[]> rows = new LinkedList<>();
				List<String> cols = new LinkedList<>();
				
				
				for (X arg : sorts.get(n)) {
					List<Object> row = new LinkedList<>();
					cols = new LinkedList<>();
					cols.add(n.toString());
					row.add(arg);
					for (C f : set) {
						row.add(symbols.get(f).get(Collections.singletonList(arg)));
						cols.add(f.toString());
					}
//					String[] row2 = new String[] { row };
					rows.add(row.toArray(new Object[] { }));
				}
				all.put(n.toString(), makeTable2(BorderFactory.createEmptyBorder(), n + " (" + rows.size() + ")", 
						rows.toArray(new Object[][] { }), cols.toArray(new String[] { })));
			}
			
			for (C n : keys2) {
				if (sig0.symbols.get(n).first.size() == 0) {
					continue;
				}
				Map<List<X>, X> f = symbols.get(n);
				List<Object[]> rows = new LinkedList<>();
				for (List<X> arg : f.keySet()) {
					List<Object> argX = (List<Object>) arg; //arg.stream().map(x -> x).collect(Collectors.toList());
					argX.add(f.get(arg));
					Object[] row = argX.toArray(new Object[] {});
					rows.add(row);
				}
				List<String> l = new LinkedList<String>((List<String>)sig0.symbols.get(n).first);
				l.add(sig0.symbols.get(n).second.toString());
				all.put(n.toString(), makeTable2(BorderFactory.createEmptyBorder(), n + " (" + rows.size() + ")", rows.toArray(new Object[][] { }), l.toArray(new String[] {})));
			}
			List<String> xxx = new LinkedList<>(all.keySet());
			xxx.sort(comp);
			for (String n : xxx) {
				//if (skip.contains(n)) {
				//	continue;
				//}
				list.add(all.get(n));
			}
			return Util.makeGrid(list);			
		}

		 static class NonEditableModel extends DefaultTableModel {

		    NonEditableModel(Object[][] data, String[] columnNames) {
		        super(data, columnNames);
		    }

		    @Override
		    public boolean isCellEditable(int row, int column) {
		        return true;
		    }
		} 
		
		private static JPanel makeTable2(Border b, String border,
				Object[][] rowData, String[] colNames) {
			

			JTable t = new JTable() {
				public Dimension getPreferredScrollableViewportSize() {
					Dimension d = getPreferredSize();
					return new Dimension(d.width, d.height);
				}
			};
			PlusMinusCellRenderer r = new PlusMinusCellRenderer();
			t.setDefaultRenderer(Object.class, r);
			t.setDefaultEditor(Object.class, r);
			t.setModel(new NonEditableModel(rowData, colNames));
		//	ListSelectionModel.
			t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //(JTable.)
			
			JPanel p = new JPanel(new GridLayout(1, 1));
			TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());
			if (colNames.length > 0) {
				sorter.toggleSortOrder(0);
			}
			t.setRowSorter(sorter);
			sorter.allRowsChanged();
			p.add(new JScrollPane(t));
			
			for (int row = 0; row < t.getRowCount(); row++)
		    {
		        int rowHeight = t.getRowHeight();

		        for (int column = 0; column < t.getColumnCount(); column++)
		        {
		            Component comp = t.prepareRenderer(t.getCellRenderer(row, column), row, column);
		            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
		        }

		        t.setRowHeight(row, rowHeight);
		    }

			// p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(b, border));
			return p;
			
		}
	
		static class PlusMinusCellRenderer extends DefaultCellEditor implements TableCellRenderer {
		    private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
			
		    public PlusMinusCellRenderer() {
		    	super(new JTextField());
		    }
		    
		    Map<Pair<Integer, Integer>, Component> cache = new HashMap<>();
	        public Component getTableCellRendererComponent(
	                            final JTable table, Object value,
	                            boolean isSelected, boolean hasFocus,
	                            int row, int column) {
	        	Pair<Integer, Integer> p = new Pair<>(row, column);
	        	Component ret = cache.get(p);
	        	if (ret != null) {
	        		return ret;
	        	}
	        	if (ret == null) {
	        		ret = extract(value);
	        		if (ret != null) {
	        			cache.put(p, ret);
	        			return ret;
	        		}
	        	}
	        
	        	ret = new DefaultTableCellRenderer().getTableCellRendererComponent(table, strip(value.toString()), false, hasFocus, row, column);
	        	//cache.put(p, ret);
	        	return ret;
	        }
	        
	        @Override
	        public Component getTableCellEditorComponent(JTable table, Object value,
					boolean isSelected, int row, int column) {
	        	
	        	Pair<Integer, Integer> p = new Pair<>(row, column);
	        	Component ret = cache.get(p);
	        	if (ret != null) {
	        		return ret;
	        	}
	        	if (ret == null) {
	        		ret = extract(value);
	        		if (ret != null) {
	        			cache.put(p, ret);
	        			return ret;
	        		}
	        	}
	        	
	        	return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		    }

		
	       /* private void adjustRowHeight(JTable table, int row, int column) {
     		       int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
     		      setSize(new Dimension(cWidth, 1000));
     		      int prefH = getPreferredSize().height;
     		      while (rowColHeight.size() <= row) {
     		        rowColHeight.add(new ArrayList<Integer>(column));
     		      }
     		      List<Integer> colHeights = rowColHeight.get(row);
     		      while (colHeights.size() <= column) {
     		        colHeights.add(0);
     		      }
     		      colHeights.set(column, prefH);
     		      int maxH = prefH;
     		      for (Integer colHeight : colHeights) {
     		        if (colHeight > maxH) {
     		          maxH = colHeight;
     		        }
     		      }
     		      if (table.getRowHeight(row) != maxH) {
     		        table.setRowHeight(row, maxH);
     		      }
     		    } */
	}
	

		public <V> void validate(OplSig<S,C,V> sig) {
			for (S s : sig.sorts) {
				if (!sorts.containsKey(s)) {
					throw new RuntimeException("No data for " + s + " in " + this);
				}
			}
			for (S s : sorts.keySet()) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Extra data for " + s);
				}
			}
			for (C f : sig.symbols.keySet()) {
				if (!symbols.containsKey(f)) {
					throw new RuntimeException("No data for " + f);
				}
				List<S> arg_ts = sig.symbols.get(f).first;
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> sorts.get(x)).collect(Collectors.toList());
				List<List<X>> args = prod(arg_ds);
				for (List<X> arg : args) {
					X at = symbols.get(f).get(arg);
					if (at == null) {
						throw new RuntimeException("Missing on argument " + arg + " in " + f);
					}
					if (!sorts.get(sig.symbols.get(f).second).contains(at)) {
						throw new RuntimeException("In " + f + ", return value " + at + " not in correct sort " + sorts.get(sig.symbols.get(f).second));
					}
				}
				for (List<X> gt : symbols.get(f).keySet()) {
					if (!args.contains(gt)) {
						throw new RuntimeException("Superfluous arg " + gt + " in " + f + " notin " + args);
					}
				}
			}
			for (C f : symbols.keySet()) {
				if (!sig.symbols.keySet().contains(f)) {
					throw new RuntimeException("Extra data for " + f);	
				}
			}
			for (Triple<OplCtx<S,V>, OplTerm<C,V>, OplTerm<C,V>> eq : sig.equations) {
				List<S> arg_ts = eq.first.values();
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> sorts.get(x)).collect(Collectors.toList());
				List<List<X>> args = prod(arg_ds);
				for (List<X> env : args) {
					OplCtx<X,V> env2 = eq.first.makeEnv(env);
					X x = eq.second.eval(sig, this, env2);
					X y = eq.third.eval(sig, this, env2);
					if (!x.equals(y)) {
						throw new RuntimeException("Equation " + eq.second + " = " + eq.third + " not respected on " + env + ", lhs=" + x + " and rhs=" + y);
					}
				}
			}
			sig0 = sig;
		}
		

		public OplSetInst(Map<S, Set<X>> sorts,
				Map<C, Map<List<X>, X>> symbols, String sig) {
			this.sorts = sorts;
			this.sig = sig;
			this.symbols = symbols;
		}

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public Map<List<X>, X> getSymbol(C head) {
			Map<List<X>, X> ret = symbols.get(head);
			if (ret == null) {
				throw new RuntimeException("Unknown symbol " + head);
			}
			return ret;
		}

	}
	
	public static class OplJavaInst extends OplExp {
		
		public Map<String, String> defs;
		
		public ScriptEngine engine;
		
		String sig;
		
		OplSig sig0;
		
		OplEnvironment ENV;
				
		public OplJavaInst(Map<String, String> defs, String sig) {
			this.defs = defs;
			this.sig = sig;
		}
		
		public void validate(OplSig sig, OplEnvironment ENV) {
			this.ENV = ENV;
			sig0 = sig;
			for (String k : defs.keySet()) {
				if (k.equals("_preamble")) {
					continue;
				}
				if (!sig.symbols.containsKey(k)) {
					throw new RuntimeException("Extra symbol " + k);
				}				
			}
			for (Object k : sig.symbols.keySet()) {
				if (!defs.keySet().contains(k)) {
					throw new RuntimeException("Missing symbol " + k);
				}
			}
			
			engine = new ScriptEngineManager().getEngineByName("nashorn");
			for (String key : ENV.keys()) {
				engine.put(key, ENV.get(key));
			}
			String ret = "";
			if (defs.containsKey("_preamble")) {
				ret += defs.get("_preamble") + "\n\n";
 			}
			for (String k : defs.keySet()) {
				if (k.equals("_preamble")) {
					continue;
				}
				String v = defs.get(k);
				ret += "function " + k + "(input) { " + v + " }\n\n";
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

	//TODO: pretty print
	public static class OplPresTrans<S,C,V,X,Y> extends OplExp {
		Map<S, Map<X, OplTerm<Chc<C,Y>, V>>> map = new HashMap<>();
		Map<S, Map<X, OplTerm<Object, V>>> pre_map;
		String src0, dst0;
		
		OplPres<S,C,V,X> src;
		OplPres<S,C,V,Y> dst;
		
		
		
		@Override
		public String toString() {
			return "OplSetTranGens [map=" + map + ", pre_map=" + pre_map + ", src0=" + src0
					+ ", dst0=" + dst0 + ", src=" + src + ", dst=" + dst + ", mapping=" + mapping
					+ "]";
		}

		@Override
		public JComponent display() {
			return toMapping().display();
		}
				
		private OplTerm<Chc<C,Y>, V> conv(OplTerm<Object, V> e) {
			if (e.var != null) {
				return new OplTerm<>(e.var);
			}
			List<OplTerm<Chc<C,Y>, V>> args0 = new LinkedList<>();
			for (OplTerm<Object, V> arg : e.args) {
				args0.add(conv(arg));
			}
			if (dst.gens.get(e.head) != null) {
				return new OplTerm<>(Chc.inRight((Y) e.head), args0);
			} else {
				return new OplTerm<>(Chc.inLeft((C) e.head), args0);
			}
		}
		
		OplMapping<S, Chc<C,X>, V, S, Chc<C,Y>> mapping;
		public OplMapping<S, Chc<C,X>, V, S, Chc<C,Y>> toMapping() {
			if (mapping != null) {
				return mapping;
			}
			Map<S, S> sorts = Util.id(src.sig.sorts);
			
			Map<Chc<C,X>, Pair<OplCtx<S,V>, OplTerm<Chc<C,Y>, V>>> symbols = new HashMap<>();
			for (C c : src.sig.symbols.keySet()) {
				Pair<List<S>, S> t = src.sig.symbols.get(c);
				
				List<Pair<V,S>> l = new LinkedList<>();
				List<OplTerm<Chc<C,Y>, V>> r = new LinkedList<>();
				for (S s : t.first) {
					V v = src.sig.fr.next();
					l.add(new Pair<>(v, s));
					r.add(new OplTerm<>(v));
				}
				
				OplCtx<S,V> g = new OplCtx<>(l);
				OplTerm<Chc<C,Y>, V> e = new OplTerm<>(Chc.inLeft(c), r);
				
				Pair<OplCtx<S,V>, OplTerm<Chc<C,Y>, V>> p = new Pair<>(g, e);
				symbols.put(Chc.inLeft(c), p);
			}
			
			for (X x : src.gens.keySet()) {
				S s = src.gens.get(x);
				if (!map.containsKey(s)) {
					throw new RuntimeException(s + " not a key in " + map);
				}
				if (!map.get(s).containsKey(x)) {
					throw new RuntimeException(x + " not a key in " + map.get(s));
				}
				OplTerm<Chc<C, Y>, V> y = map.get(s).get(x);
				symbols.put(Chc.inRight(x), new Pair<>(new OplCtx<>(), y));
			}

			mapping = new OplMapping<S,Chc<C,X>,V,S,Chc<C,Y>>(sorts, symbols, src0, dst0);
			mapping.validate(src.toSig(), dst.toSig());
			return mapping;
		}
		
		public void validateNotReally(OplPres<S,C,V,X> src, OplPres<S,C,V,Y> dst) {
			if (pre_map == null) {
				return;
			}
			this.src = src;
			this.dst = dst;
			
			if (!src.sig.equals(dst.sig)) {
				throw new RuntimeException("Signatures do not match");
			}
			
			for (Object s : src.sig.sorts) {
				if (!pre_map.containsKey(s)) {
					throw new RuntimeException("Missing sort: " + s);
				}
			}
			for (S s : pre_map.keySet()) {
				if (!src.sig.sorts.contains(s)) {
					throw new RuntimeException("Extra sort: " + s);
				}
			}
			
			for (S s : src.sig.sorts) {
				Map<X, OplTerm<Chc<C,Y>, V>> m = new HashMap<>();
				for (X x : pre_map.get(s).keySet()) {
					OplTerm<Object, V> n = pre_map.get(s).get(x);
					OplTerm<Chc<C, Y>, V> u = conv(n);
					if (!src.gens.keySet().contains(x)) {
						throw new RuntimeException("Not a generator: " + x);
					}
					u.type(dst.toSig(), new OplCtx<>());
					m.put(x, u);
				}		
				map.put(s, m);
			}
			
			toMapping();
			
			///////////
/*			
			
			for (Object s : src.sig.sorts) {
				Map<X, OplTerm<Chc<C, Y>, V>> h = map.get(s);
				for (X x : h.keySet()) {
					if (!src.gens.get(x).equals(s)) {
						throw new RuntimeException("Value " + x + " not of sort " + s);
					}
					OplTerm<Chc<C, Y>, V> y = h.get(x);
					if (!s.equals(y.type(dst.toSig(), new OplCtx<>()))) {
						throw new RuntimeException("Value " + y + " is not of sort " + s);
					}
				}
			}
			//h : F => G
			//f : X -> Y
			//h_Y o F(f) = G(f) o h_X  
			for (C f : src.sig.symbols.keySet()) {
				//Pair<List<S>, S> t = src.sig.symbols.get(f);
				//v : s   in F
				//v : t2
				//
				
			} */
			
		}
		
		
		public OplPresTrans(Map<S, Map<X, OplTerm<Object, V>>> m, String src0, String dst0) {
			this.pre_map = m;
			this.src0 = src0;
			this.dst0 = dst0;
		}

		
		//validates
		 public OplPresTrans(Map<S, Map<X, OplTerm<Chc<C, Y>, V>>> map, String src0, String dst0, 
				 OplPres<S, C, V, X> src, OplPres<S, C, V, Y> dst ) {
			this.map = map;
			this.src0 = src0;
			this.dst0 = dst0;
			this.src = src;
			this.dst = dst;
			toMapping();
		} 

		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		
	}
	
	public static class OplSetTrans<S,C,X> extends OplExp {
		Map<S, Map<X, X>> sorts;
		String src, dst;
		
		OplSig<S,C,?> sig;
		OplSetInst<S,C,X> src0, dst0;
		
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
			for (S n : sorts.keySet()) {
				Map<X, X> f = sorts.get(n);
				List<Object[]> rows = new LinkedList<>();
				for (X arg : f.keySet()) {
					Object[] row = new Object[2]; 
					row[0] = arg;
					row[1] = f.get(arg);
					rows.add(row);
				}
				list.add(Util.makeTable(BorderFactory.createEmptyBorder(), n.toString() + " (" + rows.size() + ")", 
						rows.toArray(new Object[][] { }), new Object[] {src + " (in)", dst + " (out)"}));
			}
			
			return Util.makeGrid(list);			
		}
		
		public OplSetTrans(Map<S, Map<X, X>> sorts, String src, String dst) {
			this.sorts = sorts;
			this.src = src;
			this.dst = dst;
		}

		public void validate(OplSig<S,C,?> sig, OplSetInst<S,C,X> src0, OplSetInst<S,C,X> dst0) {
			this.sig = sig;
			this.src0 = src0;
			this.dst0 = dst0;
			
			for (Object s : sig.sorts) {
				if (!sorts.containsKey(s)) {
					throw new RuntimeException("Missing sort: " + s);
				}
			}
			for (S s : sorts.keySet()) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Extra sort: " + s);
				}
			}
			for (Object s : sig.sorts) {
				Map<X, X> h = sorts.get(s);
				for (X x : h.keySet()) {
					X y = h.get(x);
					if (!src0.sorts.get(s).contains(x)) {
						throw new RuntimeException("Value " + x + " is not in source for sort " + s);
					}
					if (!dst0.sorts.get(s).contains(y)) {
						throw new RuntimeException("Value " + y + " is not in target for sort " + s);
					}
				}
			}
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> a = sig.symbols.get(f);
				List<S> arg_ts = a.first;
				List<Set<X>> arg_ds = arg_ts.stream().map(x -> src0.sorts.get(x)).collect(Collectors.toList());
				List<List<X>> args = prod(arg_ds);
				
				for (List<X> arg : args) {
					X r = src0.getSymbol(f).get(arg);
					X lhs = sorts.get(a.second).get(r);
					
					List<X> l = new LinkedList<>();
					int i = 0;
					for (S at : arg_ts) {
						X v = arg.get(i);
						X u = sorts.get(at).get(v);
						l.add(u);
						i++;
					}
					X rhs = dst0.getSymbol(f).get(l);
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
			for (S k : sorts.keySet()) {
				Map<X, X> v = sorts.get(k);
				List<String> l = new LinkedList<>();
				for (X x : v.keySet()) {
					X y = v.get(x);
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
		
	public static class OplSchema<S, C, V> extends OplExp {
		String sig0;
		OplSig<S, C, V> sig;
		Set<S> entities;
		
		public OplSchema(String sig0, Set<S> entities) {
			this.sig0 = sig0;
			this.entities = entities;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	/*	@Override
		public JComponent display() {
			return sig.display();
		} */
		
		public void validate(OplSig<S, C, V> sig) {
			this.sig = sig;
			for (S s : entities) {
				if (!sig.sorts.contains(s)) {
					throw new RuntimeException("Not a sort: " + s);
				}
			}
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);
				
				if (t.first.size() == 1 && entities.contains(t.first.get(0)) && entities.contains(t.second)) {
					continue; //is foreign key
				}
				if (t.first.size() == 1 && entities.contains(t.first.get(0)) && !entities.contains(t.second)) {
					continue; //is attribute
				}
				boolean hitEntity = false;
				for (S k : t.first) {
					if (entities.contains(k)) {
						hitEntity =  true;
						break;
					}
				}
				if (!hitEntity && !entities.contains(t.second)) {
					continue; //is typeside function
				}				
				throw new RuntimeException("Does not pass entity/typeside check: " + f);
			}
		}
		
		OplSig<S, C, V> cache_E, cache_A, cache_T;
		public OplSig<S, C, V> projE() {
			if (cache_E != null) {
				return cache_E;
			}
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);
				
				if (t.first.size() == 1 && entities.contains(t.first.get(0)) && entities.contains(t.second)) {
					symbols.put(f, t);
				}
			}
						
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				if (eq.first.vars0.size() != 1) {
					continue;
				}
				if (!entities.contains(new LinkedList<>(eq.first.vars0.values()).get(0))) {
					continue;
				}
				if (!symbols.keySet().containsAll(eq.second.symbols()) || !symbols.keySet().containsAll(eq.third.symbols())) {
					continue;
				}
				equations.add(eq);
			}
			
			cache_E = new OplSig<S,C,V>(sig.fr, sig.prec, new HashSet<>(entities), symbols, equations);
			return cache_E;
		}
		
		public OplSig<S, C, V> projT() {
			if (cache_T != null) {
				return cache_T;
			}
			Set<S> types = new HashSet<>(sig.sorts);
			types.removeAll(entities);
			
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			outer: for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);
				
				if (entities.contains(t.second)) {
					continue;
				}
				for (S s : t.first) {
					if (entities.contains(s)) {
						continue outer;
					}
				}
				
				symbols.put(f, t);
			}
						
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				if (!types.containsAll(eq.first.vars0.values())) {
					continue;
				}
				if (!symbols.keySet().containsAll(eq.second.symbols()) || !symbols.keySet().containsAll(eq.third.symbols())) {
					continue;
				}
				equations.add(eq);
			}
			
			cache_T = new OplSig<S,C,V>(sig.fr, sig.prec, types, symbols, equations);
			return cache_T;
		}
		
		OplSig<S, C, V> cache_EA;
		public OplSig<S, C, V> projEA() {
			if (cache_EA != null) {
				return cache_EA;
			}
			
			Set<S> types = new HashSet<>();
			types.addAll(projA().sorts);
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			symbols.putAll(projA().symbols);
			symbols.putAll(projE().symbols);
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			//don't want these, causes problems with uber flower eval?
			//TODO
				equations.addAll(projA().equations);
			equations.addAll(projE().equations);
			cache_EA = new OplSig<S,C,V>(sig.fr, sig.prec, types, symbols, equations);
			return cache_EA;
		}
		
		OplSig<S, C, V> cache_EdiscreteT;
		public OplSig<S, C, V> projEdiscreteT() {
			if (cache_EdiscreteT != null) {
				return cache_EdiscreteT;
			}
			
			Set<S> types = new HashSet<>();
			types.addAll(projA().sorts);
			types.addAll(projT().sorts);
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
	//		symbols.putAll(projA().symbols);
			symbols.putAll(projE().symbols);
			for (C symbol : projT().symbols.keySet()) {
				Pair<List<S>, S> t = projT().symbols.get(symbol);
				if (t.first.isEmpty()) {
					symbols.put(symbol, t);
				}
			}
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
				equations.addAll(projA().equations);
			equations.addAll(projE().equations);
			cache_EdiscreteT = new OplSig<S,C,V>(sig.fr, sig.prec, types, symbols, equations);
			//System.out.println(cache_EdiscreteT);
			return cache_EdiscreteT;
		}
		
		OplSig<S, C, V> cache_EAdiscreteT;
		public OplSig<S, C, V> projEAdiscreteT() {
			if (cache_EAdiscreteT != null) {
				return cache_EAdiscreteT;
			}
			
			Set<S> types = new HashSet<>();
			types.addAll(projA().sorts);
			types.addAll(projT().sorts);
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			symbols.putAll(projA().symbols);
			symbols.putAll(projE().symbols);
			for (C symbol : projT().symbols.keySet()) {
				Pair<List<S>, S> t = projT().symbols.get(symbol);
				if (t.first.isEmpty()) {
					symbols.put(symbol, t);
				}
			}
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			equations.addAll(projA().equations);
			equations.addAll(projE().equations);
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : projT().equations) {
				if (symbols.keySet().containsAll(eq.second.symbols()) && symbols.keySet().containsAll(eq.third.symbols())) {
					equations.add(eq);
				}
			}
			
			cache_EAdiscreteT = new OplSig<S,C,V>(sig.fr, sig.prec, types, symbols, equations);
			//System.out.println(cache_EAdiscreteT);
			return cache_EAdiscreteT;
		}
		
		
		public OplSig<S, C, V> projA() {
			if (cache_A != null) {
				return cache_A;
			}
			Map<C, Pair<List<S>, S>> symbols = new HashMap<>();
			outer: for (C f : sig.symbols.keySet()) {
				Pair<List<S>, S> t = sig.symbols.get(f);
				
				if (entities.contains(t.second)) {
					continue;
				}
				if (t.first.size() != 1) {
					continue;
				}
				if (!entities.contains(t.first.get(0))) {
					continue;
				}
				
				symbols.put(f, t);
			}
						
			List<Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>>> equations = new LinkedList<>();
			for (Triple<OplCtx<S, V>, OplTerm<C, V>, OplTerm<C, V>> eq : sig.equations) {
				if (!symbols.keySet().containsAll(eq.second.symbols()) || !symbols.keySet().containsAll(eq.third.symbols())) {
					continue;
				}
				equations.add(eq);
			}
			
			cache_A = new OplSig<S,C,V>(sig.fr, sig.prec, new HashSet<>(sig.sorts), symbols, equations);
			return cache_A;
		}
		
		@Override 
		public String toString() {
			return "schema {\n entities\n  " + Util.sep(entities, ", ") + ";\n}";
		}
	}
	
	public static class OplSchemaProj<S, C, V> extends OplExp {
		String which;
		String sch0;
		
		public OplSchemaProj(String sch0, String which) {
			this.sch0 = sch0;
			this.which = which;
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		public OplSig<S, C, V> proj(OplSchema<S, C, V> sig) {
			if (which.equals("E")) {
				return sig.projE();
			} else if (which.equals("A")) {
				return sig.projA();
			} else if (which.equals("T")) {
				return sig.projT();
			} else if (which.equals("EA")) {
				return sig.projEA();
			}
			throw new RuntimeException();
		}
	}
	
	public static class OplInst<S,C,V,X> extends OplExp {
		String P0, J0, S0;
		OplPres<S,C,V,X> P;
		OplJavaInst J;
		OplSchema<S,C,V> S;
		
		public OplPres<S,C,V,X> projE() {
			return proj(S.projE());
		}
		public OplPres<S,C,V,X> projEA() {
			return proj(S.projEA());
		}
		public OplPres<S,C,V,X> projEdiscreteT() {
			return proj(S.projEdiscreteT());
		}
		public OplPres<S,C,V,X> projEAdiscreteT() {
			return proj(S.projEAdiscreteT());
		}
		public OplPres<S,C,V,X> proj(OplSig<S, C, V> sig) {
		//	OplSig<S, C, V> sig = S.projE();
			
			Map<X, S> gens = new HashMap<>();
			for (X x : P.gens.keySet()) {
				S s = P.gens.get(x);
				//if (S.entities.contains(s)) {
					gens.put(x, s);
				//}
			}
			
			List<Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>>> equations = new LinkedList<>();
			for (Pair<OplTerm<Chc<C, X>, V>, OplTerm<Chc<C, X>, V>> eq : P.equations) {
				Set<Chc<C, X>> set = new HashSet<>();
				set.addAll(eq.first.symbols());
				set.addAll(eq.second.symbols());
				
				Set<C> set1 = new HashSet<>();
				Set<X> set2 = new HashSet<>();
				for (Chc<C, X> cx : set) {
					if (cx.left) {
						set1.add(cx.l);
					} else {
						set2.add(cx.r);
					}
				}
				
				if (!gens.keySet().containsAll(set2)) {
					continue;
				}
				if (!sig.symbols.keySet().containsAll(set1)) {
					continue;
				}
				equations.add(eq);
			}
			
			OplPres<S,C,V,X> ret = new OplPres<>(P.prec, "?", sig, gens, equations);
			
			return ret;
		}
		
		public OplInst(String S0, String P0, String J0) {
			this.P0 = P0;
			this.J0 = J0;
			this.S0 = S0;
		}
		
		void validate(OplSchema<S,C,V> S, OplPres<S,C,V,X> P, OplJavaInst J) {
			if (!S.sig.equals(P.sig)) {
				throw new RuntimeException("Presentation not on expected theory.");
			}
			if (J != null && !J.sig0.equals(S.projT())) {
				throw new RuntimeException("JS model not on expected theory");
			}
			
			this.P = P;
			this.J = J;
			this.S = S;	
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		@Override
		public String toString() {
			return P.toString();
		}
		
		public JComponent display() {
			JTabbedPane ret = new JTabbedPane();
			
			FQLTextPanel p = new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
			ret.add(p, "Presentation");
			
//			FQLTextPanel q = new FQLTex/tPanel(BorderFactory.createEtchedBorder(), "",projEA().display();
//			ret.add(OplSat.saturate(projEA()).display(), "Attribute Graph");
			
			try {
				ret.add(saturate().makeTables(S.projT().sorts), "Saturation");
//				ret.add(saturate(J, projE(), S, P).makeTables(S.projT().sorts), "Saturation");
			} catch (Exception ex) {
				ex.printStackTrace();
				ret.add(new FQLTextPanel(BorderFactory.createEtchedBorder(), "Exception", ex.getMessage()), "Saturation");
			}
			return ret;

		}
		
		public OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> saturate() {
			return saturate(J, projEdiscreteT(), S, P);
		}
		
		
		
		
		private static <S,C,V,X,Y> OplSetInst<S, C, OplTerm<Chc<Chc<C,X>, JSWrapper>, V>> 
		inject(OplSetInst<S, C, OplTerm<Chc<C,X>, V>> I, OplJavaInst I0, OplSchema<S,C,V> S, OplPres<S,C,V,X> P) {
			Map<S, Set<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>> sorts = new HashMap<>();
			for (S s : I.sorts.keySet()) {
				Set<OplTerm<Chc<Chc<C,X>, JSWrapper>,V>> set = new HashSet<>();
				for (OplTerm<Chc<C, X>, V> e : I.sorts.get(s)) {
					OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(e);
				//	KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
				//	KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj); entities can't reduce
					set.add(kkk);
				}
				sorts.put(s, set);
			} 
			for (S s : S.sig.sorts) {
				if (!sorts.containsKey(s)) {
					sorts.put(s, new HashSet<>());
				}
			}
			
			
			Map<C, Map<List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>> symbols = new HashMap<>();
			for (C c : S.projA().symbols.keySet()) {
				Map<List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> n = new HashMap<>();
				Pair<List<S>, S> t = S.sig.symbols.get(c);
				S t0 = t.first.get(0);
				Set<List<OplTerm<Chc<C, X>, V>>> dom = new HashSet<>();
				for (OplTerm<Chc<C, X>, V> x : I.sorts.get(t0)) {
					dom.add(Util.singList(x));
				}
				Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>> m = new HashMap<>(); 
				for (List<OplTerm<Chc<C, X>, V>> x : dom) {
					OplTerm<Chc<C, X>, V> y = new OplTerm<>(Chc.inLeft(c), x);
					m.put(x, P.toSig().getKB().nf(y));
				}
				
				for (List<OplTerm<Chc<C, X>, V>> a : m.keySet()) {
					List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> l = new LinkedList<>();
					for (OplTerm<Chc<C, X>, V> e : a) {
						OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(e);
						KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
						KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
						l.add(OplToKB.convert(z));
					}
					OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(m.get(a));
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
					n.put(l, OplToKB.convert(z));
					sorts.get(t.second).add(OplToKB.convert(z)); //added to build sets for types
				}
				symbols.put(c, n);
			}
			for (C c : S.projE().symbols.keySet()) {
				Map<List<OplTerm<Chc<C, X>, V>>, OplTerm<Chc<C, X>, V>> m = I.symbols.get(c); 
				Map<List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>>, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> n = new HashMap<>();
				for (List<OplTerm<Chc<C, X>, V>> a : m.keySet()) {
					List<OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> l = new LinkedList<>();
					for (OplTerm<Chc<C, X>, V> e : a) {
						OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(e);
						KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
						KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
						l.add(OplToKB.convert(z));
					}
					OplTerm<Chc<Chc<C,X>, JSWrapper>,V> kkk = OplSig.inject(m.get(a));
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> jjj = OplToKB.convert(kkk);
					KBExp<Chc<Chc<C,X>, JSWrapper>,V> z = OplToKB.redBy(I0, jjj);
					n.put(l, OplToKB.convert(z));
				}
				symbols.put(c, n);
			}
			
			 OplSetInst<S, C, OplTerm<Chc<Chc<C,X>, JSWrapper>, V>> ret = new OplSetInst<S, C, OplTerm<Chc<Chc<C,X>, JSWrapper>, V>>(sorts, symbols, "?");
			 ret.validate(S.projEA()); 
			 return ret;
		}
		
		public static <S,C,V,X> OplSetInst<S, C, OplTerm<Chc<Chc<C, X>, JSWrapper>, V>> 
		saturate(OplJavaInst I0, OplPres<S,C,V,X> P0, OplSchema<S,C,V> S, OplPres<S,C,V,X> P) {
			OplSetInst<S, C, OplTerm<Chc<C,X>, V>> I = OplSat.saturate(P0);
			OplSetInst<S, C, OplTerm<Chc<Chc<C,X>, JSWrapper>, V>> J = inject(I, I0, S, P);
			
			return J;
		}
		
	}
	
	public static class OplApply extends OplExp {
		String Q0, I0;
		OplQuery Q;
		OplInst I;
		public OplApply(String Q0, String I0) {
			this.Q0 = Q0;
			this.I0 = I0;
		}
		void validate(OplQuery Q, OplInst I) {
			this.Q = Q;
			this.I = I;
			//TODO 
		}
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplId extends OplExp {
		String s;
		public OplId(String s) {
			this.s = s;
		}
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class OplTyMapping<S1,C1,V,S2,C2> extends OplExp {
		String src0, dst0;
		OplSchema<S1,C1,V> src;
		OplSchema<S2,C2,V> dst;
		OplMapping<S1,C1,V,S2,C2> m;
		
		public OplTyMapping(String src0, String dst0, OplSchema<S1, C1, V> src,
				OplSchema<S2, C2, V> dst, OplMapping<S1, C1, V, S2, C2> m) {
			this.src0 = src0;
			this.dst0 = dst0;
			this.src = src;
			this.dst = dst;
			this.m = m;
			validate();
		}
		
		private void validate() {
			if (!src.projT().equals(dst.projT())) {
				throw new RuntimeException("Differing type sides");
			}
			extend().validate(src.sig, dst.sig);
		}
		
		private OplMapping<S1,C1,V,S2,C2> cache;
		public OplMapping<S1,C1,V,S2,C2> extend() {
			if (cache != null) {
				return cache;
			}
			
			Map<S1, S2> sorts = new HashMap<>(m.sorts);
			Map<C1, Pair<OplCtx<S2, V>, OplTerm<C2, V>>> symbols = new HashMap<>(m.symbols);
			
			for (S1 s1 : src.projT().sorts) {
				sorts.put(s1, (S2)s1);
			}
			
			for (C1 c1 : src.projT().symbols.keySet()) {
				Pair<List<S1>, S1> t = src.projT().symbols.get(c1);
				List<Pair<V, S2>> l = new LinkedList<>();
				List<OplTerm<C2,V>> vs = new LinkedList<>();
				for (S1 s1 : t.first) {
					V v = src.sig.fr.next();
					vs.add(new OplTerm<>(v));
					l.add(new Pair<>(v, (S2)s1));
				}
				OplCtx<S2, V> ctx = new OplCtx<>(l);
				OplTerm<C2, V> value = new OplTerm<>((C2)c1, vs);
				symbols.put(c1, new Pair<>(ctx, value));
			}
			
			cache = new OplMapping<S1,C1,V,S2,C2>(sorts, symbols, "?", "?");
			return cache;
		}
		
		@Override
		public JComponent display() {
			return extend().display();
		}
		
		@Override
		public <R, E> R accept(E env, OplExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public interface OplExpVisitor<R, E> {
		public R visit (E env, OplTyMapping e);
		public R visit (E env, OplId e);
		public R visit (E env, OplApply e);
		public R visit (E env, OplSig e);
		public R visit (E env, OplPres e);
		public R visit (E env, OplSetInst e);
		public R visit (E env, OplTransEval e);	
		public R visit (E env, OplEval e);
		public R visit (E env, OplVar e);
		public R visit (E env, OplSetTrans e);
		public R visit (E env, OplJavaInst e);
		public R visit (E env, OplMapping e);
		public R visit (E env, OplDelta e);
		public R visit (E env, OplSigma e);
		public R visit (E env, OplSat e);		
		public R visit (E env, OplUnSat e);		
		public R visit (E env, OplPresTrans e);	
		public R visit (E env, OplUberSat e);
		public R visit (E env, OplFlower e);
		public R visit (E env, OplSchema e);
		public R visit (E env, OplSchemaProj e);
		public R visit (E env, OplInst e);
		public R visit (E env, OplQuery e);
	}

	private static <X> List<List<X>> prod(List<Set<X>> in1) {
		List<List<X>> y = new LinkedList<>();
		List<X> z = new LinkedList<>();
		y.add(z);
		
		for (Set<X> X : in1) {
			List<List<X>> y0 = new LinkedList<>();
			for (List<X> a : y) {					
				for (X x : X) {
					List<X> toadd = new LinkedList<>(a);
					toadd.add(x);
					y0.add(toadd);
				}
			}
			y = y0;
		}
		
		return y;
	}
	
	public static JComponent extract(Object t0) {
		if (t0 instanceof OplTerm) {
	    	OplTerm t = (OplTerm) t0;
			if (t.args.size() == 0) {
	    		Object o = Util.stripChcs(t.head).second;
	    		if (o instanceof JSWrapper) {
	    			JSWrapper w = (JSWrapper) o;
	    			if (w.o instanceof JComponent) {
	    				return (JComponent) w.o;
	    			} else if (w.o instanceof OplObject) {
	    				return ((OplObject)w.o).display();
	    			}
	    		}
	    	}
		}
		if (t0 instanceof JComponent) {
			return (JComponent) t0;
		}
    	return null;
	}
    	
/*	static class OplWrapper {
		JComponent comp;
		public OplWrapper(OplObject obj) {
			comp = obj.display();
		}
	} */
   
}
