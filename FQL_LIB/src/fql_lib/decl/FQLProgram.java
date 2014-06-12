package fql_lib.decl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class FQLProgram {
	

// public String name0;
	/*
	
	public Map<String, Color> nmap = new HashMap<>();
	private Map<SigExp, Color> smap = new HashMap<>();
	
	public Color smap(SigExp s) {
		if (smap.containsKey(s)) {
			return smap.get(s); 
		}
		Color c = nColor();
		smap.put(s, c);
		return c;
	}
	
	public Graph<String, Object> build;
	public Graph<String, Object> build2;
	public void doColors() {
		for (String k : sigs.keySet()) {
			Color c = nColor();
			nmap.put(k, c);
			smap.put(new SigExp.Var(k), c);
			smap.put(sigs.get(k), c);
			smap.put(sigs.get(k).toConst(this), c);
		}
		for (String k : insts.keySet()) {
			nmap.put(k, nColor());
		}
		build = build();
		build2 = build2();
		//System.out.println("smap " + smap);
	}
	
	public Map<String, Paint> colorMap2 = new HashMap<>();
	public Map<String, Paint> colorMap3 = new HashMap<>();

	public Graph<String, Object> build2() {
		final Graph<String, Object> g2 = new DirectedSparseMultigraph<>();

//		for (final String k : sigs.keySet()) {
//			if (nmap.get(k) )
	//		SigExp.Const c = sigs.get(k).toConst(this);
			//if (smap.get(c) == null) {
		//		smap.put(c, nmap.get(k));
			//}
			//colorMap3.put(k, smap.get(c));
		//	g2.addVertex(k);
		//}
//		for (final String k : sigs.keySet()) {
	//		if (!colorMap3.containsKey(k)) {
	//			colorMap3.put(k, nColor());
	//		}
	//	}
		for (final String k : maps.keySet()) {
			MapExp.Const i = maps.get(k).toConst(this);
			SigExp src = i.src;
			SigExp dst = i.dst;
			String src_k = revLookup(sigs, src);
			String dst_k = revLookup(sigs, dst);
			if (src_k == null || dst_k == null) {
				continue;
			}
//			Paint src_c = colorMap3.get(src_k);
	//		Paint dst_c = colorMap3.get(dst_k);
	//		if (src_c == null || dst_c == null) {
		//		continue;
		//	}
			g2.addEdge(k, src_k, dst_k);
		}
		
		return g2;
	}
	
	public static <K,V> K revLookup(Map<K,V> map, V v) {
		for (K k : map.keySet()) {
			V v0 = map.get(k);
			if (v.equals(v0)) {
				return k;
			}
		}
		return null;
	}
	
	public Graph<String, Object> build() {
		// Graph<V, E> where V is the type of the vertices

		final Graph<String, Object> g2 = new DirectedSparseMultigraph<>();
		final MutableInteger guid = new MutableInteger(0);

		for (final String k : insts.keySet()) {
			InstExp i = insts.get(k);
			SigExp.Const c = i.type(this).toConst(this);
//			if (smap.get(c) == null) {
	//			smap.put(c, nColor());
	//		}
//			SigExp sig = c.toSig(this);
			//String sig_k = revLookup(sigs, c);
	//		colorMap3.put(sig_k, smap.get(c));
	//		colorMap2.put(k, smap.get(c));
			// Paint color = map.get(c);
			g2.addVertex(k);

			i.accept(new Unit(), new InstExpVisitor<Unit, Unit>() {
				public Unit visit(Unit env, Zero e) {
					return null;
				}

				public Unit visit(Unit env, One e) {
					return null;
				}

				public Unit visit(Unit env, Two e) {
					return null;
				}

				public Unit visit(Unit env, Plus e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.a, k);
					g2.addEdge(new Pair<>(guid.pp(), e), e.b, k);
					return null;
				}

				public Unit visit(Unit env, Times e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.a, k);
					g2.addEdge(new Pair<>(guid.pp(), e), e.b, k);
					return null;
				}

				public Unit visit(Unit env, Exp e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.a, k);
					g2.addEdge(new Pair<>(guid.pp(), e), e.b, k);
					return null;
				}

				public Unit visit(Unit env, Const e) {
					return null;
				}

				public Unit visit(Unit env, Delta e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.I, k);
					return null;
				}

				public Unit visit(Unit env, Sigma e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.I, k);
					return null;
				}

				public Unit visit(Unit env, Pi e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.I, k);
					return null;
				}

				public Unit visit(Unit env, FullSigma e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.I, k);
					return null;
				}

				public Unit visit(Unit env, Relationalize e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.I, k);
					return null;
				}

				public Unit visit(Unit env, External e) {
					return null;
				}

				public Unit visit(Unit env, Eval e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.e, k);
					return null;
				}

				public Unit visit(Unit env, FullEval e) {
					g2.addEdge(new Pair<>(guid.pp(), e), e.e, k);
					return null;
				}

				@Override
				public Unit visit(Unit env, Kernel e) {
					TransExp t = transforms.get(e.trans);
					Pair<String, String> p = t.type(FQLProgram.this);
					g2.addEdge(new Pair<>(guid.pp(), e), p.first, k);
					g2.addEdge(new Pair<>(guid.pp(), e), p.second, k);
					return null;
				}
			});
		}

		return g2;
	}

	
	int cindex = 0;
	public static Color[] colors_arr = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.yellow, Color.CYAN, Color.GRAY, Color.ORANGE, Color.PINK, Color.BLACK, Color.white};
	public Color nColor() {
		if (cindex < colors_arr.length) {
			return colors_arr[cindex++];
		} else {
			cindex = 0;
			return nColor();
		}
	}

*/
	@Override
	public boolean equals(Object o) {
		return (this == o);
	}
/*
	public FQLProgram(LinkedHashMap<String, Type> enums,
			LinkedHashMap<String, SigExp> sigs,
			LinkedHashMap<String, MapExp> maps,
			LinkedHashMap<String, InstExp> insts,
			LinkedHashMap<String, FullQueryExp> full_queries,
			LinkedHashMap<String, QueryExp> queries,
			LinkedHashMap<String, TransExp> transforms,
			LinkedHashMap<String, Integer> lines, List<String> drop,
			List<String> order) {
		super();
		this.enums = enums;
		this.sigs = sigs;
		this.maps = maps;
		this.insts = insts;
		this.full_queries = full_queries;
		this.queries = queries;
		this.transforms = transforms;
		this.lines = lines;
		this.drop = drop;
		this.order = order;
	} 

	public FQLProgram() {
	} */

	public static class NewDecl {
		TransExp trans;
		String name;
		SetExp set;
		FnExp fn;
		CatExp cat;
		FunctorExp ftr;		
		Integer line;
		
		public static NewDecl transDecl(String name, Integer line, TransExp trans) {
			NewDecl ret = new NewDecl(name, line);
			ret.trans = trans;
			return ret;
		}

		public static NewDecl setDecl(String name, Integer line, SetExp set) {
			NewDecl ret = new NewDecl(name, line);
			ret.set = set;
			return ret;
		}

		public static NewDecl fnDecl(String name, Integer line, FnExp fn) {
			NewDecl ret = new NewDecl(name, line);
			ret.fn = fn;
			return ret;
		}
		
		public static NewDecl catDecl(String name, Integer line, CatExp cat) {
			NewDecl ret = new NewDecl(name, line);
			ret.cat = cat;
			return ret;
		}
		
		public static NewDecl ftrDecl(String name, Integer line, FunctorExp ftr) {
			NewDecl ret = new NewDecl(name, line);
			ret.ftr = ftr;
			return ret;
		}


		public NewDecl(String name, Integer line) {
			this.name = name;
			this.line = line;
		}
	}
	
	public LinkedHashMap<String, SetExp> sets = new LinkedHashMap<>();
	public LinkedHashMap<String, FnExp> fns = new LinkedHashMap<>();
	public LinkedHashMap<String, CatExp> cats = new LinkedHashMap<>();
	public LinkedHashMap<String, FunctorExp> ftrs = new LinkedHashMap<>();
	public LinkedHashMap<String, TransExp> trans = new LinkedHashMap<>();
	public List<String> order = new LinkedList<>();
	public LinkedHashMap<String, Integer> lines = new LinkedHashMap<>();

	//copies
	public FQLProgram(FQLProgram p) {
		this.sets = new LinkedHashMap<>(p.sets);
		this.fns = new LinkedHashMap<>(p.fns);
		this.cats = new LinkedHashMap<>(p.cats);
		this.ftrs = new LinkedHashMap<>(p.ftrs);
		this.order = new LinkedList<>(p.order);
		this.lines = new LinkedHashMap<>(p.lines);
		this.trans = new LinkedHashMap<>(p.trans);
	}

	public FQLProgram(List<NewDecl> decls) {
		Set<String> seen = new HashSet<>();
		
		for (NewDecl decl : decls) {
			if (decl.set != null) {
				checkDup(seen, decl.name, "set");
				sets.put(decl.name, decl.set);
			} else if (decl.fn != null) {
				checkDup(seen, decl.name, "function");
				fns.put(decl.name, decl.fn);
			} else if (decl.cat != null) {
				checkDup(seen, decl.name, "category");
				cats.put(decl.name, decl.cat);
			} else if (decl.ftr != null) {
				checkDup(seen, decl.name, "functor");
				ftrs.put(decl.name, decl.ftr);
			} else if (decl.trans != null) {
				checkDup(seen, decl.name, "transform");
				trans.put(decl.name, decl.trans);
			} else {
				throw new RuntimeException("decl was " + decl);
			}
			lines.put(decl.name, decl.line);
			order.add(decl.name);				
		}
	}


	@Override
	public String toString() {
		return "FQLProgram [sets=" + sets + ", fns=" + fns + ", order=" + order
				+ ", lines=" + lines + "]";
	}


	private void checkDup(Set<String> seen, String name, String s)
			throws LineException {
		if (seen.contains(name)) {
			throw new RuntimeException("Duplicate name: " + name);
		}
		seen.add(name);
	}

}
