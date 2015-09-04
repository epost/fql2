package fql_lib.kb;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;

public class KBUnifier<C,V> {
	
	public static <C,V> Map<V, KBExp<C,V>> findSubst(KBExp<C,V> s, KBExp<C,V> t) {
	//	if (!Collections.disjoint(s.vars_fast(), t.vars_fast())) { 
		//	throw new RuntimeException("not disjoint in findsubst");
		//}
			Map<V, KBExp<C,V>> m = unify0(s, t.freeze());
			if (m == null) {
				return null;
			}
			Map<V, KBExp<C,V>> ret = new HashMap<>();
			for (V v : m.keySet()) {
				ret.put(v, m.get(v).unfreeze());
			}
			return ret;
	}
	/*
	public static <C,V> Map<V, KBExp<C,V>> findSubst(KBExp<C,V> s, KBExp<C,V> t) {
		if (s instanceof KBVar<?,?>) {
			return singleton(((KBVar<C,V>)s).var, t);
		}
		KBApp<C,V> s0 = (KBApp<C,V>) s;
		if (t instanceof KBVar<?,?>) {
			return null;
		}
		KBApp<C,V> t0 = (KBApp<C,V>) t;
		if (!s0.f.equals(t0.f)) {
			return null;
		}
		if (s0.args.size() != t0.args.size()) {
			return null;
		}
		Map<V, KBExp<C,V>> ret = new HashMap<>();
		for (int i = 0; i < s0.args.size(); i++) {
			Map<V, KBExp<C,V>> m = findSubst(s0.args.get(i), t0.args.get(i));
			if (m == null) {
				return null;
			}
			ret = compose(m, ret);
//			ret = compose(ret, m);
		}
		if (ret != null && !s.subst(ret).equals(t)) {
			throw new RuntimeException("trying to map " + s + " into " + t + " yields " + ret + " and " + s.subst(ret));
		}
		return ret;
	} */

	
	public static <C,V> Map<V, KBExp<C,V>> unify0(KBExp<C,V> s, KBExp<C,V> t) {
		if (s.isVar) {
			V v = s.getVar().var;
			if (!t.isVar && t.vars().contains(v)) {
				return null; //occurs check failed
			}
			return singleton(v, t);
		}
		KBApp<C,V> s0 = (KBApp<C,V>) s;
		if (t.isVar) {
			V v = t.getVar().var;
			if (s.vars().contains(v)) {
				return null; //occurs check failed
			}
			return singleton(v, s);
		}
		KBApp<C,V> t0 = t.getApp();
		if (!s0.f.equals(t0.f)) {
			return null;
		}
		if (s0.args.size() != t0.args.size()) {
			return null;
		}
		Map<V, KBExp<C,V>> ret = new HashMap<>();
		for (int i = 0; i < s0.args.size(); i++) {
			Map<V, KBExp<C,V>> m = unify0(s0.args.get(i).subst(ret), t0.args.get(i).subst(ret));
			if (m == null) {
				return null;
			}
			ret = andThen(ret, m);
		}
		//if (ret != null && !s.subst(ret).equals(t.subst(ret))) {
		//	throw new RuntimeException("trying to unify " + s + " and " + t + " yields " + ret + " but " + s.subst(ret) + " and " + t.subst(ret));
		//}
		return ret;
	}
	
	/* //works fine
	public static <C,V>  Map<V, KBExp<C,V>> unifyXXX(KBExp<C,V> s, KBExp<C,V> t) {
		KBUnifier<C,V> x = new KBUnifier<C,V>();
		x.unify(s, t);
		if (!s.subst(x.sigma).equals(t.subst(x.sigma))) {
			System.out.println("!!!!!!!!!!unified " + s + " and " + t + " using " + x.sigma + " but bad" );
		}
		return x.sigma;
	} */

	//private Map<V, KBExp<C,V>> sigma;
	
//	private KBUnifier() { 
	//	sigma = new HashMap<>();
	//}
	/*
	private void unify(KBExp<C,V> s, KBExp<C,V> t) {
		if (s instanceof KBVar<?,?>) {
			s = s.subst(sigma); 
		}
		if (t instanceof KBVar<?,?>) {
			t = t.subst(sigma);
		}
		if (s instanceof KBVar<?,?> && s.equals(t)) {
			//do nothing
		}
		else if (s instanceof KBApp<?,?> && t instanceof KBApp<?,?>) {
			KBApp<C,V> s0 = (KBApp<C,V>) s;
			KBApp<C,V> t0 = (KBApp<C,V>) t;
			C f = s0.f;
			C g = t0.f;
			if (!f.equals(g)) {
				throw new RuntimeException("Cannot unify: " + s + " and " + t);
			}
			List<KBExp<C,V>> ss = s0.args;
			List<KBExp<C,V>> tt = t0.args;
			if (ss.size() != tt.size()) {
				throw new RuntimeException("Argument length mismatch: " + s + " and " + t);
			}
			for (int i = 0; i < ss.size(); i++) {
				unify(ss.get(i), tt.get(i));
			}
		}
		else if (s instanceof KBApp<?,?>) {
			unify(t, s);
		}
		else if (t.occurs(((KBVar<C,V>)s).var)) {
			throw new RuntimeException("Occurs check failed: " + s + " in " + t);
		}
		else {
//			sigma = compose(singleton(((KBVar<C,V>)s).var, t), sigma); 
			sigma = andThen(sigma, singleton(((KBVar<C,V>)s).var, t)); 
		}
	} */

	private static <C,V> Map<V, KBExp<C, V>> singleton(V v, KBExp<C, V> t) {
		Map<V, KBExp<C, V>> ret = new HashMap<>();
		ret.put(v, t);
		return ret;
	}


	private static <C,V> Map<V, KBExp<C, V>> andThen(Map<V, KBExp<C, V>> s, Map<V, KBExp<C, V>> t) {
		 Map<V, KBExp<C, V>> ret = new HashMap<>();

		 for (V k : s.keySet()) {
			 ret.put(k, s.get(k).subst(t));
		 }
		 for (V k : t.keySet()) {
			 if (!s.containsKey(k)) {
				 ret.put(k, t.get(k));
			 }
		 }
		 
		 return ret;
	}


	
	
}
