package fql_lib.kb;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;
import fql_lib.cat.categories.FinSet;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;
import fql_lib.opl.OplExp.OplCtx;
import fql_lib.opl.OplExp.OplSig;
import fql_lib.opl.OplExp.OplTerm;

public class OplToKB {
	
	private OplSig sig;
	private KB<String, String> KB;
	
	
	public OplToKB(OplSig sig) {
		this.sig = sig;
		if (DEBUG.debug.opl_require_const) {
			checkEmpty();
		}
		KB = convert(this.sig, "lpo");
		KB.complete();
	}
	
	private void checkEmpty() {
		Set<String> m = new HashSet<>(sig.sorts);
		for (String f : sig.symbols.keySet()) {
			Pair<List<String>, String> a = sig.symbols.get(f);
			if (a.first.isEmpty()) {
				m.remove(a.second);
			}
		}
		
		if (!m.isEmpty()) {
			throw new RuntimeException("Sort " + Util.sep(m, ",") + " has no 0-ary constants");
		}
	}
	
	Map<Pair<List<String>, String>, Set<Pair<OplCtx<String>, OplTerm>>> hom = new HashMap<>();
	public Set<Pair<OplCtx<String>, OplTerm>> hom(List<String> s, String t) {
		Set<Pair<OplCtx<String>, OplTerm>> ret = hom.get(new Pair<>(s, t));
		if (!sig.sorts.contains(t)) {
			throw new RuntimeException("Bad target sort " + t);
		}
		if (!sig.sorts.containsAll(s)) {
			throw new RuntimeException("Bad source sort " + s);		
		}
		
		if (ret == null) {
			List<Pair<String, String>> vars = new LinkedList<>();
			Map<String, Set<String>> vars2 = new HashMap<>();
			int i = 0;
			for (String z : s) {
				vars.add(new Pair<>("v" + (i++), z));
			}
			OplCtx<String> ctx = new OplCtx<String>(vars);
			for (String sort : sig.sorts) {
				vars2.put(sort, new HashSet<>());
			}
			for (Pair<String, String> k : vars) {
				vars2.get(k.second).add(k.first);
			}
			
			int count = 0;
			Map<String, Set<OplTerm>> j = arity0(vars2);
			for (;;) {
				Map<String, Set<OplTerm>> k = inc(j);
				Map<String, Set<OplTerm>> k2= red(k);
				if (j.equals(k2)) {
					break;
				}

				count++;
				if (count > 8) {
					throw new RuntimeException("exceeded");
				}
				j = k2;
			}	
			ret = j.get(t).stream().map(g -> { return new Pair<>(ctx, g); }).collect(Collectors.toSet());
		}
		return ret;
	}
	
	private Map<String, Set<OplTerm>> red(Map<String, Set<OplTerm>> in) {
		Map<String, Set<OplTerm>> ret = new HashMap<>();
		
		for (String k : in.keySet()) {
			Set<OplTerm> v = in.get(k);
			Set<OplTerm> v2 = new HashSet<>();
			for (OplTerm a : v) {
				KBExp<String, String> b = convert(a);
				KBExp<String, String> c = KB.red(b);
				OplTerm d = convert(c);
				v2.add(d);
			}
			ret.put(k, v2);
		}
		
		return ret;
	}

	public KBExp<String,String> red(OplTerm r) {
		return KB.red(convert(r));
	}
	
	
	/*
	Map<String, Set<OplTerm>> upto(Map<String, Set<String>> vars, int n) {
		Map<String, Set<OplTerm>> m = arity0(vars);
		for (int i = 0; i < n; i++) {
			m = inc(m);
		}
		return m;
	}
	*/
	private Map<String, Set<OplTerm>> arity0(Map<String, Set<String>> vars) {
		Map<String, Set<OplTerm>> m = new HashMap<>();
		for (String s : sig.sorts) {
			Set<OplTerm> set = new HashSet<>();
			for (String v : vars.get(s)) {
				set.add(new OplTerm(v));
			}
			m.put(s, set);
		}
		for (String f : sig.symbols.keySet()) {
			Pair<List<String>, String> a = sig.symbols.get(f);
			if (a.first.isEmpty()) {
				m.get(a.second).add(new OplTerm(f, new LinkedList<>()));
			}
		}
		
		return m;
	}
	
	private Map<String, Set<OplTerm>> inc(Map<String, Set<OplTerm>> in) {
		Map<String, Set<OplTerm>> out = new HashMap<>();
		for (String k : in.keySet()) {
			out.put(k, new HashSet<>(in.get(k)));
		}
		
		for (String f : sig.symbols.keySet()) {
			Pair<List<String>, String> a = sig.symbols.get(f);
			if (a.first.isEmpty()) {
				continue;
			}
			Map<Integer, List<OplTerm>> arg_ts = new HashMap<>();
			int i = 0;
			for (String t : a.first) {
				arg_ts.put(i++, new LinkedList<>(in.get(t)));
			}
			List<LinkedHashMap<Integer, OplTerm>> cands = FinSet.homomorphs(arg_ts);
			for (LinkedHashMap<Integer, OplTerm> cand : cands) {
				List<OplTerm> actual = new LinkedList<>();
				for (int j = 0; j < i; j++) {
					actual.add(cand.get(j));
				}
				out.get(a.second).add(new OplTerm(f, actual));
			}
		}
		
		return out;
	}
	
	
	public String printKB() {
		Set<Pair<KBExp<String, String>, KBExp<String, String>>> R = KB.R();
		
		List<String> R0 = new LinkedList<>();
		for (Pair<KBExp<String, String>, KBExp<String, String>> r : R) {
			int i = 0;
			Map<String, KBExp<String, String>> m = new HashMap<>();
			for (String v : r.first.vars()) {
				if (v.startsWith("_v") && !m.containsKey(v)) {
					m.put(v, new KBVar<String, String>("v" + i++));
				}
			}
			for (String v : r.second.vars()) {
				if (v.startsWith("_v") && !m.containsKey(v)) {
					m.put(v, new KBVar<String, String>("v" + i++));
				}
			}
			R0.add(r.first.subst(m) + " -> " + r.second.subst(m));
		}
		R0.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		return Util.sep(R0, "\n\n");
	}
	
	public static KBExp<String, String> convert(OplTerm t) {
		if (t.var != null) {
			return new KBVar<>(t.var);
		}
		List<KBExp<String, String>> l = t.args.stream().map(x -> { return convert(x); }).collect(Collectors.toList());
		return new KBApp<>(t.head, l);
	}
	
	public static OplTerm convert(KBExp<String, String> t) {
		if (t.isVar) {
			return new OplTerm(t.getVar().var);
		}
		KBApp<String, String> t2 = t.getApp();
		List<OplTerm> l = new LinkedList<>();
		for (KBExp<String, String> p : t2.args) {
			l.add(convert(p));
		}
		return new OplTerm(t2.f, l);
	}
	
	private static KB<String, String> convert(OplSig s, String which) {
		Function<Pair<String, String>, Boolean> gt = x -> {
			Integer l = s.prec.get(x.first);
			Integer r = s.prec.get(x.second);
			return l > r;
		};

		if (!s.symbols.keySet().equals(s.prec.keySet())) {
			if (DEBUG.debug.opl_alpha) {
				gt = x -> {
					return x.first.toString().compareTo(x.second) > 0;
				};
			} else {
				throw new RuntimeException("Cannot use Knuth-Bendix - not all symbol precedence given.");
			}
		}
		
		Set<Pair<KBExp<String, String>, KBExp<String, String>>> eqs = new HashSet<>();
		for (Triple<OplCtx<String>, OplTerm, OplTerm> eq : s.equations) {
			eqs.add(new Pair<>(convert(eq.second), convert(eq.third)));
		}
		
		Iterator<String> fr = new Iterator<String>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return true;
			}
			@Override
			public String next() {
				return "_v" + (i++);
			}
		};
		
		return new KB<>(eqs, KBOrders.pogt(gt, which), fr);
	}
	
}
