package fql_lib.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Quad;
import fql_lib.Util;

public class KB<Y> {
	
	@Override
	public String toString() {
		Set<String> ret = rules.stream().map(x -> {
			String s1 = Util.sep(x.first, ".");
			String s2 = Util.sep(x.second, ".");
			if (x.first.size() == x.second.size()) {
				return s1 + " = " + s2;
			} 
			return s1 + " -> " + s2;
		}).collect(Collectors.toSet());
		return Util.sep(ret, "\n");
	}
	
	Set<Pair<List<Y>, List<Y>>> rules;
	boolean finished = false;
	Map<Pair<List<Y>, List<Y>>, Boolean> equivs = new HashMap<>();
	int max_iterations;
	int iteration = 0;
	
	//does not copy, and mutates the pairs inside of rules
	public KB(Set<Pair<List<Y>, List<Y>>> rules, int max_iterations) {
		this.rules = rules;
		this.max_iterations = max_iterations;
	}
	
	public void complete() {
		go(rules, iteration, max_iterations);
		finished = true;
	}
	
	public boolean equiv(List<Y> a, List<Y> b) {
		Pair<List<Y>, List<Y>> pair = new Pair<>(a,b);
		if (equivs.containsKey(pair)) {
			return equivs.get(pair);
		}
		boolean eq = almost_joinable(a, b, rules);
		if (finished) {
			equivs.put(pair, eq);
			return eq;
		}
		if (eq) {
			equivs.put(pair, true);
			return true;
		}
		iteration++;
		if (iteration > max_iterations) {
			throw new RuntimeException("Max iterations exceeded: " + max_iterations);
		}
		finished = step(rules);
		return equiv(a,b);
	}
	

	 private static <X> void orient(Pair<List<X>, List<X>> rule) {
				if (rule.second.size() > rule.first.size()) {
					List<X> old_first  = rule.first;
					List<X> old_second = rule.second;
					rule.first  = old_second;
					rule.second = old_first;
				}
		}
	
	 private static <X> void orient(Set<Pair<List<X>, List<X>>> t) {
		for (Pair<List<X>, List<X>> rule : t) {
			orient(rule);
		}
	}
	 
	/* public static <X> void go(Set<Pair<List<X>, List<X>>> t, int iteration, int max_iterations) {
		int i = iteration;
		orient(t);
		normalize(t);
		Set<Pair<List<X>, List<X>>> ce = cp(t);
		while (!ce.isEmpty()) {
			t.addAll(ce);
			normalize(t);
			ce = cp(t);
			i++;
			if (iteration > max_iterations) {
				throw new RuntimeException("Too many iterations.");
			}
		}
		simplify(t);
	} */
	
	 private static <X> void go(Set<Pair<List<X>, List<X>>> t, int iteration, int max_iterations) {
		while (!step(t)) {
			if (iteration++ > max_iterations) {
				throw new RuntimeException("Max iterations exceeded: " + max_iterations);
			}
		}
	}
	
	//true = finished
	 private static <X> boolean step(Set<Pair<List<X>, List<X>>> t) {
		orient(t);
		normalize(t);
		Set<Pair<List<X>, List<X>>> ce = cp(t);
		if (!ce.isEmpty()) {
			t.addAll(ce);
			normalize(t);
			ce = cp(t);
			return false;
		} else {
			simplify(t);
			return true;
		}
	}
	
	
	 private static <X> Pair<List<X>, List<X>> getUnmarked(Set<Pair<List<X>, List<X>>> marked, Set<Pair<List<X>, List<X>>> t) {
		for (Pair<List<X>, List<X>> rule : t) {
			if (!marked.contains(rule)) {
				return rule;
			}
		}
		return null;
	}
	
	 private static <X> void simplify(Set<Pair<List<X>, List<X>>> t) {
		Iterator<Pair<List<X>, List<X>>> it = t.iterator();
		while (it.hasNext()) {
			Pair<List<X>, List<X>> rule = it.next();
			Set<Pair<List<X>, List<X>>> t0 = new HashSet<>(t);
			t0.remove(rule);
			if (!normal_form(rule.first, t0).equals(rule.first)) {
				it.remove();
				continue;
			}
			//TODO sanity check
//			if (!normal_form(rule.second, t0).equals(rule.second)) {
	//			throw new RuntimeException();
		//	}
		}
	}
	
	 private static <X> void normalize(Set<Pair<List<X>, List<X>>> t) {
	//	System.out.println("normalize " + t);
		Set<Pair<List<X>, List<X>>> marked = new HashSet<>();
		Pair<List<X>, List<X>> lr = null;
		while ((lr = getUnmarked(marked, t)) != null) {
		//	System.out.println("lr " + lr + " marked " + marked + " t " + t);
			t.remove(lr);
			List<X> l0 = normal_form(lr.first , t);
			List<X> r0 = normal_form(lr.second, t);
			if (!almost_joinable(l0, r0, t)) {
				Pair<List<X>, List<X>> l0r0 = new Pair<>(l0, r0);
				orient(l0r0);
				t.add(l0r0);
				marked.add(l0r0);
			}
		}
	}
	
	 private static <X> List<X> normal_form(List<X> e, Set<Pair<List<X>, List<X>>> t) {
	//	System.out.println("normalizing " + e + " on " + t);
		if (e.size() > 20) {
			throw new RuntimeException(e.toString());
		}
		List<X> ret = new LinkedList<>(e);
		for (Pair<List<X>, List<X>> rule : t) {
			if (rule.first.size() == rule.second.size()) {
				continue;
			}
			int i = occurs(ret, rule.first);
			if (i == -1) {
				continue;
			}
			//System.out.println("deleting at " + i + " size " + rule.first.size());
			delete(ret, i, rule.first.size());
			add(ret, i, rule.second);
			if (!e.equals(ret)) {
				return normal_form(ret, t);
			}
		}
		
		return ret;
	}
	
	 private static <X> List<X> apply(List<X> e, Pair<List<X>, List<X>> rule) {
		List<X> ret = new LinkedList<>(e);
		int i = occurs(ret, rule.first);
		if (i == -1) {
			return ret;
		}
		//System.out.println("deleting at " + i + " size " + rule.first.size());
		delete(ret, i, rule.first.size());
		add(ret, i, rule.second);
		return ret;
	}
	
	 private static <X> void delete(List<X> l, int i, int size) {
		for (int j = 0; j < size; j++) {
			l.remove(i);
		}
	}
	
	 private static <X> void add(List<X> l, int i, List<X> add) {
		for (int j = add.size() - 1; j >= 0; j--) {
			l.add(i, add.get(j));
		}
	}
	
	 private static <X> int occurs(List<X> l, List<X> find) {
		for (int i = 0; i <= l.size() - find.size(); i++) {
		//	System.out.println("checking " + i);
			if (occursAt(l, find, i)) {
				return i;
			}
		}
		return -1;
	}
	
	//assumes does not 'run out the end' (prevented by occurs)
	 private static <X> boolean occursAt(List<X> l, List<X> find, int i) {
		for (int j = i, k = 0; j < i + find.size(); j++, k++) {
			if (!l.get(j).equals(find.get(k))) {
				return false;
			}
		}
		return true;
	}
	
	 private static <X> boolean almost_joinable(List<X> e, List<X> f, Set<Pair<List<X>, List<X>>> t) {
	//	System.out.println("almost_joinable " + e + " and " + f + " and " + t);
		List<X> e0 = normal_form(e, t);
		List<X> f0 = normal_form(f, t);
		
//		if (e0.size() != f0.size()) {
	//		return false;
		//}
		
		Set<List<X>> e0_closed = close(e0, t);
		return e0_closed.contains(f0);
	}
	
	 private static <X> Set<List<X>> close(List<X> e, Set<Pair<List<X>, List<X>>> t) {
		//System.out.println("Closing: " + e + " under " + t);
		Set<List<X>> init = new HashSet<>();
		init.add(e);
		
		for (;;) {
			Set<List<X>> next = close1(init, t);
			//System.out.println("Stepped to " + next);
			if (init.equals(next)) {
				return init;
			}
			init = next;
		}
	}
	
	 private static <X> Set<List<X>> close1(Set<List<X>> set, Set<Pair<List<X>, List<X>>> t) {
		Set<List<X>> ret = new HashSet<>(set);
		
		for (List<X> e : set) {
			for (Pair<List<X>, List<X>> rule : t) {
				if (rule.first.size() != rule.second.size()) {
					continue;
				}
				ret.add(apply(e, rule));
				Pair<List<X>, List<X>> rule2 = rule.reverse();
				ret.add(apply(e, rule2));
			}
		}
		
		return ret;
	}
	
	//assumes size e = size f
/*	public static <X> boolean congruent(List<X> e, List<X> f, Set<Pair<List<X>, List<X>>> t) {
		if (e.size() != f.size()) {
			throw new RuntimeException();
		}
		Set<List<X>> terms = new HashSet<>();
		terms.add(e);
		terms.add(f);
		for (Pair<List<X>, List<X>> k : t) {
			if (k.first.size() == k.second.size() && k.first.size() == e.size()) {
				terms.add(k.first);
				terms.add(k.second);
			}
		}
		Set<Set<List<X>>> eqcs = new HashSet<>();
		for (List<X> term : terms) {
			Set<List<X>> eqc = new HashSet<>();
			eqc.add(term);
			eqcs.add(eqc);
		}
		
		union(eqcs, t, e.size());
		
		for (Set<List<X>> eqc : eqcs) {
			if (eqc.contains(e)) {
				return eqc.contains(f);
			}
		}
		
		throw new RuntimeException();		
	}
	
	public static <X> void union(Set<Set<List<X>>> eqcs, Set<Pair<List<X>, List<X>>> t, int i) {
	//	System.out.println("eqcs " + eqcs);
		for (Pair<List<X>, List<X>> rule : t) {
			if (rule.first.size() != i || rule.second.size() != i) {
				continue;
			}
			Set<List<X>> lhs = null;
			Set<List<X>> rhs = null;
			for (Set<List<X>> eqc : eqcs) {
				if (eqc.contains(rule.first)) {
					lhs = eqc;
				}
				if (eqc.contains(rule.second)) {
					rhs = eqc;
				}
			}
			if (lhs == null || rhs == null) {
				throw new RuntimeException();
			}
			if (lhs.equals(rhs)) {
				continue;
			}
			//System.out.println("rule is " + rule);
			eqcs.remove(rhs);
			eqcs.remove(lhs);
			lhs.addAll(rhs);
			eqcs.add(lhs);
			union(eqcs, t, i);
			return;
		}
	} */
		
	 private static <X> Set<Pair<List<X>, List<X>>> cp(Set<Pair<List<X>, List<X>>> t) {
		//System.out.println("cp " + t);
		Set<Pair<List<X>, List<X>>> ret = new HashSet<>();
		for (Pair<List<X>, List<X>> rule1 : t) {
			for (Pair<List<X>, List<X>> rule2 : t) {
				if (rule1.first.size() == rule1.second.size() && rule2.first.size() == rule2.second.size()) {
					continue;
				}
				Set<Quad<List<X>, List<X>, List<X>, List<X>>> todo = new HashSet<>();
				if (rule1.first.size() == rule1.second.size()) {
					if (rule2.first.size() == rule2.second.size()) {
						throw new RuntimeException();
					} else {
						todo.add(new Quad<>(rule1.first, rule1.second, rule2.first, rule2.second));
						todo.add(new Quad<>(rule1.second, rule1.first, rule2.first, rule2.second));
					}
				} else {
					if (rule2.first.size() == rule2.second.size()) {
						todo.add(new Quad<>(rule1.first, rule1.second, rule2.first, rule2.second));
						todo.add(new Quad<>(rule1.first, rule1.second, rule2.second, rule2.first));
					} else {
						todo.add(new Quad<>(rule1.first, rule1.second, rule2.first, rule2.second));
					}
				}
				for (Quad<List<X>, List<X>, List<X>, List<X>> rule : todo) {
					addCP1(rule.first, rule.second, rule.third, rule.fourth, t, ret);
					addCP2(rule.first, rule.second, rule.third, rule.fourth, t, ret);
				}
			}
		}
		orient(ret);
		return ret;
	}
	
	//does not include 0 length lists
	 private static <X> Set<Pair<List<X>, List<X>>> split(List<X> l) {
		Set<Pair<List<X>, List<X>>> ret = new HashSet<>();
		for (int i = 1; i < l.size(); i++) {
			ret.add(new Pair<>(l.subList(0, i), l.subList(i, l.size())));
		}
		return ret;
	}
	
	 private static <X> void addCP1(List<X> li, List<X> ri, List<X> lj, List<X> rj, Set<Pair<List<X>, List<X>>> t, Set<Pair<List<X>, List<X>>> ret) {
		for (Pair<List<X>, List<X>> uv : split(li)) {
			for (Pair<List<X>, List<X>> vw : split(lj)) {
				if (!uv.second.equals(vw.first)) {
					continue;
				}
				List<X> urj = new LinkedList<>(uv.first);
				urj.addAll(rj);
				List<X> riw = new LinkedList<>(ri); 
				riw.addAll(vw.second);
				if (!almost_joinable(urj, riw, t)) {
					ret.add(new Pair<>(urj, riw));
				}
			}
		}
	}
	 
	 private static <X> void addCP2(List<X> li, List<X> ri, List<X> lj, List<X> rj, Set<Pair<List<X>, List<X>>> t, Set<Pair<List<X>, List<X>>> ret) {
		Set<Integer> uws = middle(li, lj);
		for (Integer i : uws) {
			List<X> u = li.subList(0, i);
			List<X> w = li.subList(i+lj.size(), li.size());

			//TODO: sanity check
			//List<X> split = new LinkedList<>(u);
			//split.addAll(lj);
			//split.addAll(w);
			//if (!split.equals(li)) {
			//	throw new RuntimeException();
			//}
			
			List<X> urjw = new LinkedList<>(u);
			urjw.addAll(rj);
			urjw.addAll(w);
			if (!almost_joinable(ri, urjw, t)) {
//				System.out.println("add2 " + new Pair<>(ri, urjw));
				ret.add(new Pair<>(ri, urjw));
			}
		}
	}
	
	 private static <X> Set<Integer> middle(List<X> l, List<X> find) {
		Set<Integer> ret = new HashSet<>();
		for (int i = 0; i <= l.size() - find.size(); i++) {
		//	System.out.println("checking " + i);
			if (occursAt(l, find, i)) {
				ret.add(i);
			}
		}
		return ret;
	}
	 
		
	 public static void main(String[] args) {
		//add, delete, occurs
	/*	List<String> l = new LinkedList<>();
		l.add("a");
		l.add("b");
		l.add("c");
		List<String> r = new LinkedList<>();
		r.add("x");
		r.add("y");
		add(l, 1, r);
		System.out.println(l);

		List<String> yb = new LinkedList<>(l);
		yb.add("q");
//		yb.add("b");
	//	yb.add("c");
		System.out.println(occurs(l, yb));
		
		delete(l, 2, 2);
		System.out.println(l); */
		
		/* List<String> a = new LinkedList<>();
		a.add("a");
		List<String> b = new LinkedList<>();
		b.add("b");
		List<String> c = new LinkedList<>();
		c.add("c");
		List<String> d = new LinkedList<>();
		d.add("d");
		Set<Pair<List<String>, List<String>>> t = new HashSet<>();
		t.add(new Pair<>(a,b));
		t.add(new Pair<>(b,c));
		//t.add(new Pair<>(d,c));
		System.out.println(congruent(a, d, t)); */
		 
/*			List<String> l = new LinkedList<>();
			l.add("a");
			l.add("a");
			l.add("a");
			l.add("b");
			System.out.println(split(l));  */
		 
		/*	List<String> l = new LinkedList<>();
			l.add("b");
			l.add("b");
			l.add("a");
			l.add("b");
			l.add("b");
			l.add("b");			
			l.add("c");
			l.add("b");
			l.add("b");
			List<String> r = new LinkedList<>();
			r.add("b");
			r.add("b");
			System.out.println(middle(l, r)); */
		 
//		 List<String> l = new LinkedList<>();
	//		l.add("a");
	//		l.add("b");
	//		l.add("c");
//			delete(l, 0, 3);
	//		delete(l, 1, 2);
	//		delete(l, 2, 1);
		//	System.out.println(l);
		 
		/* 	List<String> a = new LinkedList<>();
		 	a.add("a");
		 	List<String> b = new LinkedList<>();
		 	b.add("b");
		 	List<String> bab = new LinkedList<>();
		 	bab.add("b");
		 	bab.add("a");
		 	bab.add("b");
		 	Set<Pair<List<String>, List<String>>> t = new HashSet<>();
		 	t.add(new Pair<>(a, b));
		 	t.add(new Pair<>(bab, b));
//		 	System.out.println(go(t)); */
		 
		/*    List<String> a = new LinkedList<>();
		 	a.add("a");
		 	List<String> bb = new LinkedList<>();
		 	bb.add("b");
		 	bb.add("b");
		 	List<String> bab = new LinkedList<>();
		 	bab.add("b");
		 	bab.add("a");
		 	bab.add("b");
		 	List<String> aab = new LinkedList<>();
		 	aab.add("a");
		 	aab.add("a");
		 	aab.add("b");
		 	List<String> baa = new LinkedList<>();
		 	baa.add("b");
		 	baa.add("a");
		 	baa.add("a");
		 	Set<Pair<List<String>, List<String>>> t = new HashSet<>();
		 	t.add(new Pair<>(baa, aab));
		 	t.add(new Pair<>(bab, a));
		 	t.add(new Pair<>(bb, a));
		 	KB<String> kb = new KB<>(t, 100);
		    List<String> b = new LinkedList<>();
		 	b.add("b");
		 	System.out.println(kb.equiv(a, b));
*/
//		 	System.out.println(go(t));  
	} 
	 
	
}
