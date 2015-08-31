package fql_lib.kb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Util;

public abstract class KBExp<C,V> {
	
	

	static class KBVar<C,V> extends KBExp<C,V> {
		public V var;
		
		public KBVar(V var) {
			this.var = var;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			KBVar<?,?> other = (KBVar<?,?>) obj;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return var.toString();
		}
		
		@Override
		public <R,E> R accept(E env, KBExpVisitor<C,V,R,E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean occurs(V v) {
			return var.equals(v);
		}

		@Override
		public KBExp<C, V> subst(Map<V, KBExp<C,V>> sigma) {
			KBExp<C,V> ret = sigma.get(var);
			if (ret == null) {
				return this;
			}
			return ret;
		}

		@Override
		public Set<V> vars() {
			return Collections.singleton(var);
		}

		@Override
		public Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Map<V, KBExp<C, V>> inv, List<Integer> l, KBExp<C, V> a, KBExp<C, V> b, KBExp<C,V> g, KBExp<C, V> d, boolean first) {
			return new HashSet<>();
		}

		@Override
		public KBExp<C, V> replace(List<Integer> l, KBExp<C, V> r) {
			if (l.isEmpty()) {
				return r;
			}
			throw new RuntimeException("Cannot replace");
		}

		@Override
		public KBExp<C, V> clone() {
			return new KBVar<>(var);
		}

		@Override
		public KBExp<C, V> freeze() {
			return new KBApp(this, new LinkedList<>());
		}

		@Override
		public KBExp<C, V> unfreeze() {
			throw new RuntimeException();
		}
		
	}
	
	static class KBApp<C,V> extends KBExp<C,V> {
		public C f;
		public List<KBExp<C,V>> args;
		
		
		public KBApp(C f, List<KBExp<C, V>> args) {
			super();
			this.f = f;
			this.args = args.stream().map(x -> { return x.clone(); }).collect(Collectors.toList());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((args == null) ? 0 : args.hashCode());
			result = prime * result + ((f == null) ? 0 : f.hashCode());
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
			KBApp<?,?> other = (KBApp<?,?>) obj;
			if (args == null) {
				if (other.args != null)
					return false;
			} else if (!args.equals(other.args))
				return false;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			return true;
		}

		@Override
		public String toString() {
			if (args.isEmpty()) {
				return f.toString();
			}
			if (args.size() == 2) {
				return "(" + args.get(0) + " " + f + " " + args.get(1) + ")";
			}
			String ret = f + "(" + Util.sep(args, ",") + ")";
		//	ret = ret.replace("((", "(");
		//	ret = ret.replace("))", ")");
			return ret;
		}
		
		@Override
		public <R,E> R accept(E env, KBExpVisitor<C,V,R,E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean occurs(V v) {
			for (KBExp<C,V> arg : args) {
				if (arg.occurs(v)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public KBExp<C, V> subst(Map<V, KBExp<C,V>> sigma) {
			List<KBExp<C,V>> n = new LinkedList<>();
			for (KBExp<C,V> arg : args) {
				n.add(arg.subst(sigma));
			}
			return new KBApp<>(f, n);
		}

		@Override
		public Set<V> vars() {
			Set<V> ret = new HashSet<>();
			for (KBExp<C,V> e : args) {
				ret.addAll(e.vars());
			}
			return ret;
		}

		@Override
		//this = t
		public Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Map<V, KBExp<C, V>> inv, List<Integer> p, KBExp<C, V> a, KBExp<C, V> b, KBExp<C, V> g, KBExp<C, V> d, boolean first) {
			Set<Pair<KBExp<C, V>, KBExp<C, V>>> ret = new HashSet<>();
			int q = 0;
			for (KBExp<C, V> arg : args) {
				List<Integer> p0 = new LinkedList<>(p);
				p0.add(q++); //TODO
				ret.addAll(arg.cp(inv, p0, a, b, g, d, false));
			}
			if (first) {
				return ret;
			}
		//	if (a == g && p.isEmpty()) {
				//System.out.println("!!!!!!!!"); //TODO
			//	return ret;
			//}
			try {
				
				Map<V, KBExp<C,V>> s = KBUnifier.unify0(this, a);
	//System.out.println("this is " + this);
	//System.out.println("a is " + a);
	//System.out.println("b is " + b);
	//System.out.println("g is " + g);
	//System.out.println("d is " + d);
	
		//		System.out.println("pos is " + p);
			//	System.out.println("trying to unify " + this + " and " + a);
			
				//System.out.println("result " + s);
				//System.out.println(g + " after replacement is " + g.replace(p, b));
				Pair<KBExp<C,V>, KBExp<C,V>> toadd = new Pair<>(d.subst(s), g.replace(p, b).subst(s));
				////z = (I(ee) o (z o z))
				//if (toadd.first.subst(inv).toString().equals("z") && toadd.second.subst(inv).toString().equals("(I(ee) o (z o z))")) {
					//throw new RuntimeException("$$!! this " + this + " a= " + a + " b= " + b + " g= " + g + " d= " + d + " p=" + p);
				//}
			//	System.out.println("adding " + toadd);
			//	System.out.println();
				ret.add(toadd);

			//	System.out.println("after subst " + g.replace(p, b).subst(s));
				//System.out.println("added");
			} catch (RuntimeException ex) { }
			return ret;
		}

		@Override
		public KBExp<C, V> replace(List<Integer> l, KBExp<C, V> r) {
			if (l.isEmpty()) {
				return r;
			}
			Integer x = l.get(0);
			List<KBExp<C,V>> new_args = new LinkedList<>();
			for (int i = 0; i < args.size(); i++) {
				KBExp<C,V> a = args.get(i);
				if (i == x) {
					a = a.replace(l.subList(1, l.size()), r);
				} 
				new_args.add(a);
			}
			return new KBApp<>(f, new_args);
		}

		@Override
		public KBExp<C, V> clone() {
			return new KBApp<C, V>(f, args.stream().map(x -> { return x.clone(); }).collect(Collectors.toList()));
		}

		@Override
		public KBExp<C, V> freeze() {
			return new KBApp<>(f, args.stream().map(x -> { return x.freeze(); }).collect(Collectors.toList()));
		}

		@Override
		public KBExp<C, V> unfreeze() {
			if (f instanceof KBVar) {
				return (KBVar) f;
			}
			return new KBApp<C, V>(f, args.stream().map(x -> { return x.unfreeze(); }).collect(Collectors.toList()));
		}

	}
	
	public static interface KBExpVisitor<C,V,R,E> {
		public R visit (E env, KBVar<C,V> e);
		public R visit (E env, KBApp<C,V> e);
	}
	
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
	public abstract <R,E> R accept(E env, KBExpVisitor<C,V,R,E> e);

	public abstract boolean occurs(V v);
	public abstract KBExp<C, V> subst(Map<V, KBExp<C,V>> sigma);
	public abstract Set<V> vars();
	public abstract Set<Pair<KBExp<C, V>, KBExp<C, V>>> cp(Map<V, KBExp<C, V>> inv, List<Integer> l, KBExp<C, V> a, KBExp<C, V> b, KBExp<C,V> g, KBExp<C, V> d, boolean first);
	public abstract KBExp<C, V> replace(List<Integer> p, KBExp<C, V> r);
	public abstract KBExp<C, V> freeze();
	public abstract KBExp<C, V> unfreeze();
	
	@Override
	public abstract KBExp<C, V> clone();
	//TODO: be sure to consider critical pairs against itself, but renamed
	
/*	public static void main(String[] args) {
		KBExp<String, String> u = new KBVar<>("u");
		KBExp<String, String> e = new KBApp<>("e", new LinkedList<>());
		KBExp<String, String> Iu = new KBApp<>("I", Collections.singletonList(u));
		KBExp<String, String> Iuou = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { Iu, u }) );
		
		KBExp<String, String> a = Iuou;
		KBExp<String, String> b = e;
		
		KBExp<String, String> x = new KBVar<>("x");
		KBExp<String, String> y = new KBVar<>("y");
		KBExp<String, String> z = new KBVar<>("z");
		KBExp<String, String> xy = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { x, y }) );
		KBExp<String, String> yz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { y, z }) );
		
		KBExp<String, String> xyQz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { xy, z }) );
		KBExp<String, String> xQyz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { x, yz }) );
		
		KBExp<String, String> g = xyQz;
		KBExp<String, String> d = xQyz;
		
		Set<Pair<KBExp<String, String>, KBExp<String, String>>> res = g.cp(new LinkedList<>(), a, b, g, d, true);
		System.out.println("critical pairs for " + a + " -> " + b + " and " + g + " -> " + d + " are " + Util.sep(res, "\n"));
	} */
	
/*	public static void main(String[] args) {
		KBExp<String, String> x = new KBVar<>("x");
		KBExp<String, String> y = new KBVar<>("y");
		KBExp<String, String> u = new KBVar<>("u");
		KBExp<String, String> v = new KBVar<>("v");
		
		KBExp<String, String> xPy = new KBApp<String,String>("+", Arrays.asList(new KBExp[] { x, y }) );
		KBExp<String, String> uPv = new KBApp<String,String>("+", Arrays.asList(new KBExp[] { u, v }) );
		
		KBExp<String, String> f_xPy = new KBApp<String,String>("f", Arrays.asList(new KBExp[] { xPy }) );
		KBExp<String, String> g_uPv = new KBApp<String,String>("g", Arrays.asList(new KBExp[] { uPv }) );
		
		KBExp<String, String> fx = new KBApp<String,String>("f", Arrays.asList(new KBExp[] { x }) );
		KBExp<String, String> gv = new KBApp<String,String>("g", Arrays.asList(new KBExp[] { v }) );
		
		KBExp<String, String> fxPy = new KBApp<String,String>("+", Arrays.asList(new KBExp[] { fx, y }) );
		KBExp<String, String> uPgv = new KBApp<String,String>("+", Arrays.asList(new KBExp[] { u, gv }) );
		KBExp<String, String> yPfxPy = new KBApp<String,String>("+", Arrays.asList(new KBExp[] { y, fxPy }) );
		
		KBExp<String, String> a = yPfxPy;
		KBExp<String, String> b = f_xPy;
		KBExp<String, String> g = uPgv;
		KBExp<String, String> d = g_uPv;
		
		Set<Pair<KBExp<String, String>, KBExp<String, String>>> res = g.cp(new LinkedList<>(), a, b, g, d, true);
		System.out.println("critical pairs for " + a + " -> " + b + " and " + g + " -> " + d + " are " + Util.sep(res, "\n"));
		
		System.out.println();
		res = a.cp(new LinkedList<>(), g, d, a, b, true);
		System.out.println("critical pairs for " + g + " -> " + d + " and " + a + " -> " + b + " are " + Util.sep(res, "\n"));
	
		System.out.println();
		res = a.cp(new LinkedList<>(), a, b, a, b, true);
		System.out.println("critical pairs for " + a + " -> " + b + " and " + a + " -> " + b + " are " + Util.sep(res, "\n"));

		System.out.println();
		res = g.cp(new LinkedList<>(), g, b, g, b, true);
		System.out.println("critical pairs for " + g + " -> " + d + " and " + g + " -> " + d + " are " + Util.sep(res, "\n"));

	} */
	//a critical pair of (I((I(x) o (x o z))) o (z o z)) -> z and (I(x2) o x2) -> ee is z = (I(ee) o (z o z))
	public static void main(String[] args) {
		KBExp<String, String> x = new KBVar<>("x");
		KBExp<String, String> y = new KBVar<>("y");
		KBExp<String, String> z = new KBVar<>("z");
		
		KBExp<String, String> x2 = new KBVar<>("x2");

		KBExp<String, String> zz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] {z, z}) );
		KBExp<String, String> xz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] {x, z}) );
		KBExp<String, String> e = new KBApp<String,String>("ee", Arrays.asList(new KBExp[] {}) );
		
		KBExp<String, String> ie = new KBApp<String,String>("I", Arrays.asList(new KBExp[] { e }) );
		KBExp<String, String> ix = new KBApp<String,String>("I", Arrays.asList(new KBExp[] { x }) );
		KBExp<String, String> ix2 = new KBApp<String,String>("I", Arrays.asList(new KBExp[] { x2 }) );
		
		KBExp<String, String> ixxz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { ix, xz }) );
		KBExp<String, String> iixxz = new KBApp<String,String>("I", Arrays.asList(new KBExp[] { ixxz }) );
		KBExp<String, String> iixxzzz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { iixxz, zz }) );
		
		KBExp<String, String> ix2x2 = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { ix2, x2 }) );
		
		KBExp<String, String> iezz = new KBApp<String,String>("o", Arrays.asList(new KBExp[] { ie, zz }) );
		
		KBExp<String, String> a = iixxzzz;
		KBExp<String, String> b = z;
		KBExp<String, String> g = ix2x2;
		KBExp<String, String> d = e;
		
		List<Pair<KBExp<String, String>, KBExp<String, String>>> res0 = new LinkedList<>(g.cp(new HashMap<>(), new LinkedList<>(), a, b, g, d, true));
		//System.out.println("critical pairs for " + a + " -> " + b + " and " + g + " -> " + d + " are " + Util.sep(res0, "\n"));
	
		//System.out.println();
		List<Pair<KBExp<String, String>, KBExp<String, String>>> res2 = new LinkedList<>(a.cp(new HashMap<>(), new LinkedList<>(), g, d, a, b, true));
		System.out.println("critical pairs for " + g + " -> " + d + " and " + a + " -> " + b + " are " + Util.sep(res2, "\n"));

	}
	/*
	public static void main(String[] args) {
		KBExp<String, String> x = new KBVar<>("x");
		KBExp<String, String> u = new KBVar<>("u");
		
		KBExp<String, String> gx = new KBApp<String,String>("g", Arrays.asList(new KBExp[] { x }) );
		KBExp<String, String> gu = new KBApp<String,String>("g", Arrays.asList(new KBExp[] { u }) );
		
		KBExp<String, String> fx = new KBApp<String,String>("f", Arrays.asList(new KBExp[] { x }) );
		KBExp<String, String> fu = new KBApp<String,String>("f", Arrays.asList(new KBExp[] { u }) );

		KBExp<String, String> gfx = new KBApp<String,String>("g", Arrays.asList(new KBExp[] { fx }) );
		KBExp<String, String> gfu = new KBApp<String,String>("g", Arrays.asList(new KBExp[] { fu }) );

		KBExp<String, String> fgfx = new KBApp<String,String>("f", Arrays.asList(new KBExp[] { gfx }) );
		KBExp<String, String> fgfu = new KBApp<String,String>("f", Arrays.asList(new KBExp[] { gfu }) );

		KBExp<String, String> a = fgfx;
		KBExp<String, String> b = gx;
		KBExp<String, String> g = fgfu;
		KBExp<String, String> d = gu;
		
		List<Pair<KBExp<String, String>, KBExp<String, String>>> res0 = new LinkedList<>(g.cp(new LinkedList<>(), a, b, g, d, true));
		System.out.println("critical pairs for " + a + " -> " + b + " and " + g + " -> " + d + " are " + Util.sep(res0, "\n"));
		
		System.out.println();
		List<Pair<KBExp<String, String>, KBExp<String, String>>> res2 = new LinkedList<>(a.cp(new LinkedList<>(), g, d, a, b, true));
		System.out.println("critical pairs for " + g + " -> " + d + " and " + a + " -> " + b + " are " + Util.sep(res2, "\n"));
	
		System.out.println();
		Set<Pair<KBExp<String, String>, KBExp<String, String>>> res = a.cp(new LinkedList<>(), a, b, a, b, true);
		System.out.println("critical pairs for " + a + " -> " + b + " and " + a + " -> " + b + " are " + Util.sep(res, "\n"));

		System.out.println();
		res = g.cp(new LinkedList<>(), g, b, g, b, true);
		System.out.println("critical pairs for " + g + " -> " + d + " and " + g + " -> " + d + " are " + Util.sep(res, "\n"));

		System.out.println("subsumed: " + KB.subsumes(res0.get(0), res2.get(0)));
	}
	*/
}
