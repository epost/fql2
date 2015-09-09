package fql_lib.kb;

import java.util.Collections;
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
import fql_lib.cat.categories.Operad;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;
import fql_lib.opl.OplExp.OplCtx;
import fql_lib.opl.OplExp.OplSig;
import fql_lib.opl.OplExp.OplTerm;

public class OplToKB<S,C,V> implements Operad<S, Pair<OplCtx<S,V>, OplTerm<C,V>>> {
	
	@Override
	public Set<S> objects() {
		return sig.sorts;
	}

	@Override
	public Set<Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>>> hom(List<S> src, S dst) {
		Set<Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>>> ret = new HashSet<>();
		for (Pair<OplCtx<S, V>, OplTerm<C, V>> x : hom0(src, dst)) {
			ret.add(new Arrow<>(src, dst, x));
		}
		return ret;
	}
	
	@Override
	public Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>> id(S o) {
		V v = fr.next();
		OplTerm<C,V> t = new OplTerm<>(v);
		OplCtx<S,V> g = new OplCtx<>(Collections.singletonList(new Pair<>(v, o)));
		return new Arrow<>(Collections.singletonList(o), o, new Pair<>(g, t));
	}

	@Override
	public Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>> comp(
			Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>> F,
			List<Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>>> A) {
		if (F.src.size() != A.size()) {
			throw new RuntimeException("Arity mismatch: " + F + " and " + A);
		}
		List<Pair<V,S>> new_vs = new LinkedList<>();
		List<OplTerm<C, V>> new_args = new LinkedList<>();
		List<S> new_sorts = new LinkedList<>();
		for (Arrow<S, Pair<OplCtx<S, V>, OplTerm<C, V>>> a : A) {
			Map<V, OplTerm<C,V>> mm = new HashMap<>();
			for (V v : a.a.first.names()) {
				V u = fr.next();
				mm.put(v, new OplTerm<>(u));
				S s = a.a.first.get(v);
				new_vs.add(new Pair<>(u, s));
				new_sorts.add(s);
			}
			new_args.add(a.a.second.subst(mm));
		}
		
		return new Arrow<>(new_sorts, F.dst, new Pair<>(new OplCtx<>(new_vs), F.a.second.subst(F.a.first, new_args)));
		
	}


	
	private OplSig<S, C, V> sig;
	private KB<C, V> KB;
	private Iterator<V> fr;
	
	public OplToKB(Iterator<V> fr, OplSig<S, C, V> sig) {
		this.sig = sig;
		if (DEBUG.debug.opl_require_const) {
			checkEmpty();
		}
		this.fr = fr;
		KB = convert(this.sig);
		KB.complete();
	}
	
	private void checkEmpty() {
		Set<S> m = new HashSet<>(sig.sorts);
		for (C f : sig.symbols.keySet()) {
			Pair<List<S>, S> a = sig.symbols.get(f);
			if (a.first.isEmpty()) {
				m.remove(a.second);
			}
		}
		
		if (!m.isEmpty()) {
			throw new RuntimeException("Sort " + Util.sep(m, ",") + " has no 0-ary constants");
		}
	}
	
	private Map<Pair<List<S>, S>, Set<Pair<OplCtx<S, V>, OplTerm<C, V>>>> hom = new HashMap<>();
	public Set<Pair<OplCtx<S,V>, OplTerm<C,V>>> hom0(List<S> s, S t) {
		Set<Pair<OplCtx<S,V>, OplTerm<C,V>>> ret = hom.get(new Pair<>(s, t));
		if (!sig.sorts.contains(t)) {
			throw new RuntimeException("Bad target sort " + t);
		}
		if (!sig.sorts.containsAll(s)) {
			throw new RuntimeException("Bad source sort " + s);		
		}
		
		if (ret == null) {
			List<Pair<V, S>> vars = new LinkedList<>();
			Map<S, Set<V>> vars2 = new HashMap<>();
			int i = 0;
			for (S z : s) {
				vars.add(new Pair<>(fr.next(), z));
				i++;
			}
			OplCtx<S,V> ctx = new OplCtx<>(vars);
			for (S sort : sig.sorts) {
				vars2.put(sort, new HashSet<>());
			}
			for (Pair<V, S> k : vars) {
				vars2.get(k.second).add(k.first);
			}
			
			int count = 0;
			Map<S, Set<OplTerm<C, V>>> j = arity0(vars2);
			for (;;) {
				Map<S, Set<OplTerm<C, V>>> k = inc(j);
				Map<S, Set<OplTerm<C, V>>> k2= red(k);
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
	
	private Map<S, Set<OplTerm<C,V>>> red(Map<S, Set<OplTerm<C,V>>> in) {
		Map<S, Set<OplTerm<C,V>>> ret = new HashMap<>();
		
		for (S k : in.keySet()) {
			Set<OplTerm<C,V>> v = in.get(k);
			Set<OplTerm<C,V>> v2 = new HashSet<>();
			for (OplTerm<C,V> a : v) {
				KBExp<C, V> b = convert(a);
				KBExp<C, V> c = KB.nf(b);
				OplTerm<C, V> d = convert(c);
				v2.add(d);
			}
			ret.put(k, v2);
		}
		
		return ret;
	}

	public KBExp<C,V> nf(KBExp<C,V> r) {
		return KB.nf(r);
	}
	public OplTerm<C,V> nf(OplTerm<C,V> r) {
		return convert(KB.nf(convert(r)));
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
	private Map<S, Set<OplTerm<C,V>>> arity0(Map<S, Set<V>> vars) {
		Map<S, Set<OplTerm<C,V>>> m = new HashMap<>();
		for (S s : sig.sorts) {
			Set<OplTerm<C,V>> set = new HashSet<>();
			for (V v : vars.get(s)) {
				set.add(new OplTerm<>(v));
			}
			m.put(s, set);
		}
		for (C f : sig.symbols.keySet()) {
			Pair<List<S>, S> a = sig.symbols.get(f);
			if (a.first.isEmpty()) {
				m.get(a.second).add(new OplTerm<>(f, new LinkedList<>()));
			}
		}
		
		return m;
	}
	
	private Map<S, Set<OplTerm<C,V>>> inc(Map<S, Set<OplTerm<C,V>>> in) {
		Map<S, Set<OplTerm<C,V>>> out = new HashMap<>();
		for (S k : in.keySet()) {
			out.put(k, new HashSet<>(in.get(k)));
		}
		
		for (C f : sig.symbols.keySet()) {
			Pair<List<S>, S> a = sig.symbols.get(f);
			if (a.first.isEmpty()) {
				continue;
			}
			Map<Integer, List<OplTerm<C,V>>> arg_ts = new HashMap<>();
			int i = 0;
			for (S t : a.first) {
				arg_ts.put(i++, new LinkedList<>(in.get(t)));
			}
			List<LinkedHashMap<Integer, OplTerm<C,V>>> cands = FinSet.homomorphs(arg_ts);
			for (LinkedHashMap<Integer, OplTerm<C,V>> cand : cands) {
				List<OplTerm<C,V>> actual = new LinkedList<>();
				for (int j = 0; j < i; j++) {
					actual.add(cand.get(j));
				}
				out.get(a.second).add(new OplTerm<>(f, actual));
			}
		}
		
		return out;
	}
	
	
	public String printKB() {
		return this.KB.printKB();
	}
	
	public static <C,V> KBExp<C, V> convert(OplTerm<C,V> t) {
		if (t.var != null) {
			return new KBVar<>(t.var);
		}
		List<KBExp<C, V>> l = t.args.stream().map(x -> { return convert(x); }).collect(Collectors.toList());
		return new KBApp<>(t.head, l);
	}
	
	public static <C,V> OplTerm<C,V> convert(KBExp<C, V> t) {
		if (t.isVar) {
			return new OplTerm<>(t.getVar().var);
		}
		KBApp<C, V> t2 = t.getApp();
		List<OplTerm<C,V>> l = new LinkedList<>();
		for (KBExp<C, V> p : t2.args) {
			l.add(convert(p));
		}
		return new OplTerm<>(t2.f, l);
	}
	
	private KB<C, V> convert(OplSig<?,C,V> s) {
		Function<Pair<C, C>, Boolean> gt = x -> {
			Integer l = s.prec.get(x.first);
			Integer r = s.prec.get(x.second);
			return l > r;
		};

		if (!s.symbols.keySet().equals(s.prec.keySet())) {
			if (DEBUG.debug.opl_alpha) {
				gt = x -> {
					return x.first.toString().compareTo(x.second.toString()) > 0;
				};
			} else {
				throw new RuntimeException("Cannot use Knuth-Bendix - not all symbol precedence given.");
			}
		}
		
		Set<Pair<KBExp<C, V>, KBExp<C, V>>> eqs = new HashSet<>();
		for (Triple<?, OplTerm<C, V>, OplTerm<C, V>> eq : s.equations) {
			eqs.add(new Pair<>(convert(eq.second), convert(eq.third)));
		}
		
		return new KB<>(eqs, KBOrders.pogt(gt), fr);
	}


	
	
}
