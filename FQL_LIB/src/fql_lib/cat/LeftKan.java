package fql_lib.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql_lib.DEBUG;
import fql_lib.Pair;
import fql_lib.Util;
import fql_lib.cat.presentation.FPTransform;
import fql_lib.cat.presentation.Instance;
import fql_lib.cat.presentation.Mapping;
import fql_lib.cat.presentation.Signature;


public class LeftKan<O1,A1,O2,A2> {

	private Signature<O1,A1> A;
	private Signature<O2,A2> B;
	public  Mapping<O1,A1,O2,A2> F;
	private Instance<O1,A1> X;
	//Map<Object, Integer> rank = new HashMap<>(); //assumes all IDs globally unique //TODO
	public int fresh; 	
	
	private boolean gamma() {
		boolean ret = false;
		
		for (;;) {
			Pair<Signature<O2,A2>.Node, Pair<Integer, Integer>> k = gamma0();
			if (k == null) {
				return ret;
			}
			ret = true;
			gamma1(k.first, k.second);			
		}	
	}
	
	private void filter(Set<Pair<Integer, Integer>> set, Integer d) {
		Iterator<Pair<Integer, Integer>> it = set.iterator();
		while (it.hasNext()) {
			Pair<Integer, Integer> p = it.next();
			if (p.first.equals(d) || p.second.equals(d)) {
				it.remove();
			}
		}
	}
	
	private void gamma1(Signature<O2,A2>.Node b1, Pair<Integer, Integer> xy) {
		//System.out.println("remove " + xy);
		//System.out.println("before " + this);
		if (xy.first.equals(xy.second)) {
			Sb.get(b1).remove(xy);
			return;
		}
		Integer x, y;
		if (xy.first > xy.second) {
			x = xy.second;
			y = xy.first;
		} else {
			x = xy.first;
			y = xy.second;
		}

		Pb.get(b1).remove(new Pair<>(y,y));
		
		replace(x, y);

		Set<Pair<Integer, Integer>> set0 = new HashSet<>(Sb.get(b1));
		for (Pair<Integer, Integer> k : Sb.get(b1)) {
			if (k.first.equals(y)) {
				set0.add(new Pair<>(x, k.second));
			}
			if (k.second.equals(y)) {
				set0.add(new Pair<>(k.first, x));
			}
		}
		filter(set0, y); 		
		Sb.put(b1, set0);
		
		for (Signature<O2,A2>.Edge g : Pg.keySet()) {
			Set<Pair<Integer, Integer>> set = Pg.get(g);
			Set<Pair<Integer, Integer>> a = new HashSet<>();
			if (g.source.equals(b1) && g.target.equals(b1)) {
				for (Pair<Integer, Integer> k : set) {
					if (k.first.equals(y) && k.second.equals(y)) {
						a.add(new Pair<>(x,x));
					} 
					if (k.first.equals(y) && !k.second.equals(y)) {
						a.add(new Pair<>(x, k.second));
					}
					if (k.second.equals(y) && !k.first.equals(y)) {
						a.add(new Pair<>(k.first, x));
					}
				}
			} else if (g.source.equals(b1)) {
				for (Pair<Integer, Integer> k : set) {
					if (k.first.equals(y) && !k.second.equals(y)) {
						a.add(new Pair<>(x, k.second));
					}
				}
			} else if (g.target.equals(b1)) {
				for (Pair<Integer, Integer> k : set) {
					if (k.second.equals(y) && !k.first.equals(y)) {
						a.add(new Pair<>(k.first, x));
					}
				}
			}
			set.addAll(a);
			filter(set, y);
		}
		
		lineage.remove(y);
		for (Object k : lineage.keySet()) {
			List<Pair<Signature<O2, A2>.Edge, Object>> v = lineage.get(k);
			for (Pair<Signature<O2, A2>.Edge, Object> p : v) {
				if (p.second.equals(y)) {
					// System.out.println("replace " + uv);
					p.second = x;
				}
			}
		}
//		System.out.println("after " + this);
	}

	private void replace(Integer x, Integer y) {
		for (Set<Pair<Object, Integer>> a : ua.values()) {
			for (Pair<Object, Integer> s : a) {
				if (s.second.equals(y)) {
					s.second = x;
				}
			}
		}
		if (alpha != null) {
			for (Signature<O2, A2>.Node k : utables.keySet()) {
				Map<Integer, Object> v = utables.get(k);
				v.remove(y);
			}
		}

	}
	
	private Pair<Signature<O2,A2>.Node, Pair<Integer, Integer>> gamma0() {
		for (Entry<Signature<O2,A2>.Node, Set<Pair<Integer, Integer>>> c : Sb.entrySet()) {
			if (c.getValue().size() == 0) {
				continue;
			}
			for (Pair<Integer, Integer> p0 : c.getValue()) {
				return new Pair<>(c.getKey(), p0);
			}
		}
		
		return null;
	}
	
	private boolean bgd() {
		return beta1() || beta2() || delta() || gamma();
	}
	
	private boolean step() {
		boolean ret = false;
		while (bgd()) {
			ret = true;
		}
		return ret || alpha();
	}
	
	//true = success
	public boolean compute() {
		for (int i = 0; i < DEBUG.debug.MAX_DENOTE_ITERATIONS; i++) {
		//	System.out.println(this);
			if (!step()) {
			//	System.out.println("done");
				return true;
			}
		}
		return false;
	}
	
	//beta, delta, gamma
	
	private boolean beta2() {
		boolean ret = false;
		
		for (Signature<O1,A1>.Edge e : A.edges) {
			Signature<O2,A2>.Path g = F.apply(A.path(e));
//			Set<Pair<Object, Integer>> lhs = compose(X.evaluate(A.new Path(e)), ua.get(e.target));
			if (X.em.get(e) == null) {
				System.out.println("can't find " + e + " in " + X.nm);
			}
			Set<Pair<Object, Integer>> lhs = Util.compose(Util.convert(X.em.get(e)), ua.get(e.target));
			//			Set<Pair<Object, Integer>> lhs = Util.compose(Util.refl(X.m.get(e)), ua.get(e.target));
			Set<Pair<Object, Integer>> rhs = Util.compose(ua.get(e.source), eval(g));
			Signature<O2,A2>.Node n = g.target;
			ret = ret || addCoincidences(lhs, rhs, n);
		}
		
		return ret;
	}
	
	
	
	private boolean beta1() {
		//System.out.println("start beta: " + this);
		boolean ret = false;
		for (Signature<O2,A2>.Eq eq : B.eqs) {
			Set<Pair<Integer, Integer>> lhs = eval(eq.lhs);
			Set<Pair<Integer, Integer>> rhs = eval(eq.rhs);
			Signature<O2,A2>.Node n = eq.lhs.target;
			ret = ret || addCoincidences(lhs, rhs, n);
		}		
//		System.out.println("end beta: " + this);
		return ret;
	}
	
	private <X> boolean addCoincidences(
			Set<Pair<X, Integer>> lhs, Set<Pair<X, Integer>> rhs,
			Signature<O2,A2>.Node n) {
		boolean ret = false;
		for (Pair<?, Integer> l : lhs) {
			for (Pair<?, Integer> r : rhs) {
				if (!l.first.equals(r.first)) {
					continue;
				}
				if (l.second.equals(r.second)) {
					continue;
				}
//				System.out.println("coincidence: " + l.second + " and " + r.second);
				ret = Sb.get(n).add(new Pair<>(l.second, r.second)) || ret;
				ret = Sb.get(n).add(new Pair<>(r.second, l.second)) || ret;
			}
		}
		return ret;
	}
	
	
	public Set<Pair<Integer, Integer>> eval(Signature<O2,A2>.Path p) {
		Set<Pair<Integer, Integer>> ret = Pb.get(p.source);
		for (Signature<O2,A2>.Edge e : p.path) {
			ret = Util.compose(ret, Pg.get(e));
		}
//		System.out.println("eval " + p + " result: " + ret);

		return ret;
	}
	
	private Integer fresh() {
		return ++fresh;
	}
	
	private Pair<Integer, Signature<O2,A2>.Edge> smallest() {
		Pair<Integer, Signature<O2,A2>.Edge> ret = null;
		for (Signature<O2,A2>.Edge g : Pg.keySet()) {
			Set<Pair<Integer, Integer>> pg = Pg.get(g);
			outer: for (Pair<Integer, Integer> xx : Pb.get(g.source)) {
				Integer x = xx.first;
				for (Pair<Integer, Integer> p : pg) {
					if (p.first.equals(x)) {
						continue outer;
					}
				}
				if (ret == null || x < ret.first) {
					ret = new Pair<>(x, g);
				}
			}
		}
		return ret;
	}
	
	private boolean alpha() {
		Pair<Integer, Signature<O2,A2>.Edge> p = smallest();
		if (p == null) {
			return false;
		}
		Integer x = p.first;
		
		Signature<O2,A2>.Edge g = p.second;
		Signature<O2,A2>.Node b2 = g.target;
		Integer y = fresh();
		
		Pb.get(b2).add(new Pair<>(y,y));
		Pg.get(g ).add(new Pair<>(x,y));
		
		updateLineage(g, x, y);
		
		if (alpha != null) {
			Object xxx = J.em.get(p.second).get(
				utables.get(p.second.source).get(p.first));
			utables.get(p.second.target).put(y, xxx);
		}
		
//		System.out.println("add " + y + " for " + p);
		
		return true;
	}
	
	private boolean delta() {
		boolean ret = false;
		for (Signature<O2,A2>.Edge g : B.edges) {
			for (Pair<Integer, Integer> x : Pb.get(g.source)) {
				Integer y = null;
				Iterator<Pair<Integer, Integer>> it = Pg.get(g).iterator();
				while (it.hasNext()) {
					Pair<Integer, Integer> z = it.next();
					if (!x.first.equals(z.first)) {
						continue;
					}
					if (y == null) {
						y = z.second;
						continue;
					}
//					if (z.second.equals(y)) {
						ret = true;
						it.remove();
//						System.out.println("non-determinism: " + x.first + " mapsto " + y + " and " + z.second);
						Sb.get(g.target).add(new Pair<>(y, z.second));
						Sb.get(g.target).add(new Pair<>(z.second, y));
	//				}
				}
			}
		}
		return ret;
	}
	
	//TODO: wrong.  lineage maps each *new* ID to a list of target edges and a source ID and node
	public Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>> lineage = new HashMap<>();
	
	void updateLineage(Signature<O2,A2>.Edge col, Object old, Object nw) {
		if (!lineage.containsKey(old)) {
			lineage.put(old, new LinkedList<Pair<Signature<O2,A2>.Edge, Object>>());
		}
		List<Pair<Signature<O2,A2>.Edge, Object>> l = new LinkedList<>(lineage.get(old));
		l.add(new Pair<>(col, old));
		lineage.put(nw, l);
	}

	private Instance<O2,A2> J;
	private FPTransform<O1,A1> alpha;
	
	public LeftKan(int fresh, Mapping<O1,A1,O2,A2> f, Instance<O1,A1> x) {
		this(fresh, f, x, null, null);
	}
	public LeftKan(int fresh, Mapping<O1,A1,O2,A2> f, Instance<O1,A1> x, FPTransform<O1,A1> alpha,
			Instance<O2,A2> J) {
		A = f.source;
		B = f.target;
		F = f;
		X = x;
		this.fresh = fresh;
		this.J = J;
		this.alpha = alpha;
		
		for (Signature<O2,A2>.Node n : B.nodes) {
			Pb.put(n, new HashSet<Pair<Integer, Integer>>());
			Sb.put(n, new HashSet<Pair<Integer, Integer>>());
			if (alpha != null) {
				utables.put(n, new HashMap<Integer, Object>());
			}
		}
		for (Signature<O2,A2>.Edge e : B.edges) {
			Pg.put(e, new HashSet<Pair<Integer, Integer>>());
		}
//		Set<Object> rank = new HashSet<>();

		for (Signature<O1,A1>.Node n : A.nodes) {
			Set<Pair<Object, Integer>> j = new HashSet<>();
			Set<Pair<Integer, Integer>> i = Pb.get(F.nm.get(n));
			Set<Object> k = X.nm.get(n);
			for (Object v : k) {
				//TODO be careful: in fql++ input IDs are not unique.  
				//if (rank.contains(v)) {
				//	throw new RuntimeException("Contains non-unique ID " + v + ": " + x);
				//}
				int id = fresh();
				//rank.add(v);
				j.add(new Pair<>(v, id));
				i.add(new Pair<>(id, id));
				updateLineage(null, new Pair<>(n,v), new Integer(id)); //v is not globally unique here.
				
				if (alpha != null) {
					utables.get(F.nm.get(n)).put(id,
						alpha.data.get(n).get(v));
				}
			}
			ua.put(n, j);
		}
//		System.out.println("initial: " + this);
	}
	/*
	private Object lookup(Set<Pair<Object, Object>> set, Object i) {
		if (i == null) {
			throw new RuntimeException();
		}
		if (set == null) {
			throw new RuntimeException();
		}
		for (Pair<Object, Object> k : set) {
			if (k.first.equals(i.toString())) {
				return k.second;
			}
		}
		throw new RuntimeException("Cannot find " + i + " in " + set);
	} */
	
	@Override
	public String toString() {
		return "LeftKan [lineage=" + lineage + ", Pb=" + Pb + ", Pg=" + Pg
				+ ", ua=" + ua + ", Sb=" + Sb + "]";
	}

	public Map<Signature<O2,A2>.Node, Set<Pair<Integer, Integer>>> Pb = new HashMap<>(); 
	public Map<Signature<O2,A2>.Edge, Set<Pair<Integer, Integer>>> Pg = new HashMap<>();
	public Map<Signature<O1,A1>.Node, Set<Pair<Object , Integer>>> ua = new HashMap<>();
	public Map<Signature<O2,A2>.Node, Map<Integer, Object>> utables = new HashMap<>();

	private Map<Signature<O2,A2>.Node, Set<Pair<Integer, Integer>>> Sb = new HashMap<>();
}
