package catdata.fqlpp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import catdata.Chc;
import catdata.Pair;


@SuppressWarnings("serial")
public abstract class TransExp implements Serializable{

	public abstract <R, E> R accept(E env, TransExpVisitor<R, E> v);

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
	
	public static class PeterApply extends TransExp {
		String node;
		TransExp t;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			PeterApply other = (PeterApply) obj;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		public PeterApply(String node, TransExp t) {
			super();
			this.node = node;
			this.t = t;
		}
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class AndOrNotImplies extends TransExp {
		String which;
		CatExp cat;
		
		public AndOrNotImplies(String which, CatExp cat) {
			this.cat = cat;
			this.which = which;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cat == null) ? 0 : cat.hashCode());
			result = prime * result + ((which == null) ? 0 : which.hashCode());
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
			AndOrNotImplies other = (AndOrNotImplies) obj;
			if (cat == null) {
				if (other.cat != null)
					return false;
			} else if (!cat.equals(other.cat))
				return false;
			if (which == null) {
				if (other.which != null)
					return false;
			} else if (!which.equals(other.which))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Chr extends TransExp {
		TransExp t;

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			Chr other = (Chr) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		public Chr(TransExp t) {
			super();
			this.t = t;
		}
	}
	
	public static class Ker extends TransExp {
		TransExp t;

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			Ker other = (Ker) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		public Ker(TransExp t) {
			super();
			this.t = t;
		}
	}
	
	public static class Bool extends TransExp {
		boolean b;
		CatExp cat;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (b ? 1231 : 1237);
			result = prime * result + ((cat == null) ? 0 : cat.hashCode());
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
			Bool other = (Bool) obj;
			if (b != other.b)
				return false;
			if (cat == null) {
				if (other.cat != null)
					return false;
			} else if (!cat.equals(other.cat))
				return false;
			return true;
		}
		public Bool(boolean b, CatExp cat) {
			super();
			this.b = b;
			this.cat = cat;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class Whisker extends TransExp {
		boolean left;
		FunctorExp func;
		TransExp trans;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((func == null) ? 0 : func.hashCode());
			result = prime * result + (left ? 1231 : 1237);
			result = prime * result + ((trans == null) ? 0 : trans.hashCode());
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
			Whisker other = (Whisker) obj;
			if (func == null) {
				if (other.func != null)
					return false;
			} else if (!func.equals(other.func))
				return false;
			if (left != other.left)
				return false;
			if (trans == null) {
				if (other.trans != null)
					return false;
			} else if (!trans.equals(other.trans))
				return false;
			return true;
		}
		public Whisker(boolean left, FunctorExp func, TransExp trans) {
			super();
			this.left = left;
			this.func = func;
			this.trans = trans;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class Eval extends TransExp {
		FunctorExp a, b;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
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
			Eval other = (Eval) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}

		public Eval(FunctorExp a, FunctorExp b) {
			super();
			this.a = a;
			this.b = b;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class Curry extends TransExp {
		TransExp f;
		Boolean useInst;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((f == null) ? 0 : f.hashCode());
			result = prime * result + ((useInst == null) ? 0 : useInst.hashCode());
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
			Curry other = (Curry) obj;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			if (useInst == null) {
				if (other.useInst != null)
					return false;
			} else if (!useInst.equals(other.useInst))
				return false;
			return true;
		}

		public Curry(TransExp f, Boolean useInst) {
			super();
			this.f = f;
			this.useInst = useInst;
		}
		
	}
	
	public static class Adj extends TransExp {
		String which, L, R;
		FunctorExp F;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((L == null) ? 0 : L.hashCode());
			result = prime * result + ((R == null) ? 0 : R.hashCode());
			result = prime * result + ((which == null) ? 0 : which.hashCode());
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
			Adj other = (Adj) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (L == null) {
				if (other.L != null)
					return false;
			} else if (!L.equals(other.L))
				return false;
			if (R == null) {
				if (other.R != null)
					return false;
			} else if (!R.equals(other.R))
				return false;
			if (which == null) {
				if (other.which != null)
					return false;
			} else if (!which.equals(other.which))
				return false;
			return true;
		}
		public Adj(String which, String l, String r, FunctorExp f) {
			super();
			this.which = which;
			L = l;
			R = r;
			F = f;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	
	public static class ApplyPath extends TransExp {
		FunctorExp F;
		String node;
		List<String> edges;
		CatExp cat;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((cat == null) ? 0 : cat.hashCode());
			result = prime * result + ((edges == null) ? 0 : edges.hashCode());
			result = prime * result + ((node == null) ? 0 : node.hashCode());
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
			ApplyPath other = (ApplyPath) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (cat == null) {
				if (other.cat != null)
					return false;
			} else if (!cat.equals(other.cat))
				return false;
			if (edges == null) {
				if (other.edges != null)
					return false;
			} else if (!edges.equals(other.edges))
				return false;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			return true;
		}
		public ApplyPath(FunctorExp f, String node, List<String> edges, CatExp cat) {
			super();
			F = f;
			this.node = node;
			this.edges = edges;
			this.cat = cat;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class ApplyTrans extends TransExp {
		TransExp F;
		FunctorExp I;
		public ApplyTrans(TransExp f, FunctorExp i) {
			super();
			F = f;
			I = i;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			ApplyTrans other = (ApplyTrans) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Apply extends TransExp {
		FunctorExp F;
		TransExp I;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			Apply other = (Apply) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}
		public Apply(FunctorExp f, TransExp i) {
			super();
			F = f;
			I = i;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class Proj extends TransExp {
		FunctorExp l, r;
		Boolean proj1;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((proj1 == null) ? 0 : proj1.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Proj other = (Proj) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (proj1 == null) {
				if (other.proj1 != null)
					return false;
			} else if (!proj1.equals(other.proj1))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}

		public Proj(FunctorExp l, FunctorExp r, Boolean proj1) {
			super();
			this.l = l;
			this.r = r;
			this.proj1 = proj1;
		}
		
		
	}
	
	public static class Inj extends TransExp {
		FunctorExp l, r;
		Boolean inj1;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((inj1 == null) ? 0 : inj1.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Inj other = (Inj) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (inj1 == null) {
				if (other.inj1 != null)
					return false;
			} else if (!inj1.equals(other.inj1))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}

		public Inj(FunctorExp l, FunctorExp r, Boolean inj1) {
			super();
			this.l = l;
			this.r = r;
			this.inj1 = inj1;
		}
		
		
	}
	
	
	public static class Zero extends TransExp {
		FunctorExp f;
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		public Zero(FunctorExp f) {
			super();
			this.f = f;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Zero other = (Zero) obj;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			return true;
		}
	}
	
	public static class One extends TransExp {
		FunctorExp f;
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		public One(FunctorExp f) {
			super();
			this.f = f;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			One other = (One) obj;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			return true;
		}
	}
	
	
	public static class Prod extends TransExp {
		TransExp l, r;
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		public Prod(TransExp l, TransExp r) {
			super();
			this.l = l;
			this.r = r;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Prod other = (Prod) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}
	}
	
	public static class CoProd extends TransExp {
		TransExp l, r;
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		public CoProd(TransExp l, TransExp r) {
			super();
			this.l = l;
			this.r = r;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			CoProd other = (CoProd) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}
	}

	
	public static class ToMap extends TransExp {
		
		public Map<String, Pair<String, List<String>>> fun;
		
		public FunctorExp src, dst;
		
		public CatExp s, t;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public ToMap(Map<String, Pair<String, List<String>>> fun,
				FunctorExp src, FunctorExp dst, CatExp s, CatExp t) {
			super();
			this.fun = fun;
			this.src = src;
			this.dst = dst;
			this.s = s;
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((fun == null) ? 0 : fun.hashCode());
			result = prime * result + ((s == null) ? 0 : s.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			ToMap other = (ToMap) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (fun == null) {
				if (other.fun != null)
					return false;
			} else if (!fun.equals(other.fun))
				return false;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		
	}
	
	public static class ToSet extends TransExp {
		
		public Map<String, Chc<FnExp,SetExp>> fun;
		
		public FunctorExp src, dst;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public ToSet(Map<String, Chc<FnExp,SetExp>> fun, FunctorExp src, FunctorExp dst) {
			super();
			this.fun = fun;
			this.src = src;
			this.dst = dst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((fun == null) ? 0 : fun.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			ToSet other = (ToSet) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (fun == null) {
				if (other.fun != null)
					return false;
			} else if (!fun.equals(other.fun))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
	
		
	}
	
	public static class ToInst extends TransExp {
		
		public Map<String, TransExp> fun;
		
		public FunctorExp src, dst;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((fun == null) ? 0 : fun.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			ToCat other = (ToCat) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (fun == null) {
				if (other.fun != null)
					return false;
			} else if (!fun.equals(other.fun))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		public ToInst(Map<String, TransExp> fun, FunctorExp src, FunctorExp dst) {
			super();
			this.fun = fun;
			this.src = src;
			this.dst = dst;
		}
		
	}
	
	public static class ToCat extends TransExp {
		
		public Map<String, FunctorExp> fun;
		
		public FunctorExp src, dst;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((fun == null) ? 0 : fun.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			ToCat other = (ToCat) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (fun == null) {
				if (other.fun != null)
					return false;
			} else if (!fun.equals(other.fun))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		public ToCat(Map<String, FunctorExp> fun, FunctorExp src, FunctorExp dst) {
			super();
			this.fun = fun;
			this.src = src;
			this.dst = dst;
		}
		
	}
	
	public static class SetSet extends TransExp {
		String ob;
		FnExp fun;
		FunctorExp src, dst;
		
		public SetSet(String ob, FnExp fun, FunctorExp src, FunctorExp dst) {
			this.ob = ob;
			this.fun = fun;
			this.src = src;
			this.dst = dst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((fun == null) ? 0 : fun.hashCode());
			result = prime * result + ((ob == null) ? 0 : ob.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			SetSet other = (SetSet) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (fun == null) {
				if (other.fun != null)
					return false;
			} else if (!fun.equals(other.fun))
				return false;
			if (ob == null) {
				if (other.ob != null)
					return false;
			} else if (!ob.equals(other.ob))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	
	public static class Id extends TransExp {
		public FunctorExp t;

		public Id(FunctorExp t) {
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			Id other = (Id) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			return "id " + t;
		}
	}

	public static class Var extends TransExp {
		public String v;

		public Var(String v) {
			this.v = v;
			if (v.contains(" ")) {
				throw new RuntimeException();
			}
		}

		@Override
		public String toString() {
			return v;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((v == null) ? 0 : v.hashCode());
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
			Var other = (Var) obj;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Comp extends TransExp {
		TransExp l, r;

		public Comp(TransExp TransExp, TransExp r) {
			this.l = TransExp;
			this.r = r;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Comp other = (Comp) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "(" + l + " ; " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
public static class Iso extends TransExp {
		
		public Iso(boolean lToR, FunctorExp l, FunctorExp r) {
			this.lToR = lToR;
			this.l = l;
			this.r = r;
		}
		@Override
		public String toString() {
			if (lToR) {
				return "iso1 " + l + " " + r;
			} else {
				return "iso2 " + l + " " + r;
			}
		}
		
		boolean lToR;
		FunctorExp l, r;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + (lToR ? 1231 : 1237);
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Iso other = (Iso) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (lToR != other.lToR)
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}	
	}

	public interface TransExpVisitor<R, E> {
		public R visit(E env, Id e);
		public R visit(E env, Comp e);
		public R visit(E env, Var e);
		public R visit(E env, SetSet e);
		public R visit(E env, ToMap e);
		public R visit(E env, ToSet e);
		public R visit(E env, ToCat e);
		public R visit(E env, ToInst e);
		public R visit(E env, One e);
		public R visit(E env, Proj e);
		public R visit(E env, Prod e);
		public R visit(E env, CoProd e);
		public R visit(E env, Inj e);
		public R visit(E env, Zero e);
		public R visit(E env, Apply e);
		public R visit(E env, ApplyPath e);
		public R visit(E env, Adj e);
		public R visit(E env, Iso e);
		public R visit(E env, ApplyTrans e);
		public R visit(E env, Curry e);
		public R visit(E env, Eval e);
		public R visit(E env, Whisker e);
		public R visit(E env, Bool e);
		public R visit(E env, Chr e);
		public R visit(E env, Ker e);
		public R visit(E env, AndOrNotImplies e);
		public R visit(E env, PeterApply e);
	} 
}
