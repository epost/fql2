package catdata.fqlpp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import catdata.algs.Chc;
import catdata.algs.Pair;


@SuppressWarnings("serial")
public abstract class FunctorExp implements Serializable{

	public abstract <R, E> R accept(E env, FunctorExpVisitor<R, E> v);

	public abstract boolean equals(Object o);
	
	public static class Pushout extends FunctorExp {
		String l, r;

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
			Pushout other = (Pushout) obj;
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

		public Pushout(String l, String r) {
			super();
			this.l = l;
			this.r = r;
		}
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class Pivot extends FunctorExp {
		FunctorExp F;
		boolean pivot;

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + (pivot ? 1231 : 1237);
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
			Pivot other = (Pivot) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (pivot != other.pivot)
				return false;
			return true;
		}

		public Pivot(FunctorExp f, boolean b) {
			F = f;
			pivot = b;
		}

	}
	
	public static class Dom extends FunctorExp {
		String t;
		boolean dom;
		public Dom(String t, boolean dom) {
			super();
			this.t = t;
			this.dom = dom;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (dom ? 1231 : 1237);
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
			Dom other = (Dom) obj;
			if (dom != other.dom)
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Apply extends FunctorExp {
		FunctorExp F;
		public FunctorExp I;

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

		public Apply(FunctorExp f, FunctorExp i) {
			super();
			F = f;
			I = i;
		}
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Migrate extends FunctorExp {
		public FunctorExp F;
		public String which;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
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
			Migrate other = (Migrate) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (which == null) {
				if (other.which != null)
					return false;
			} else if (!which.equals(other.which))
				return false;
			return true;
		}
		public Migrate(FunctorExp f, String which) {
			super();
			F = f;
			this.which = which;
		}
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class Uncurry extends FunctorExp {
		public FunctorExp F;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
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
			Uncurry other = (Uncurry) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			return true;
		}

		public Uncurry(FunctorExp f) {
			super();
			F = f;
		}
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Prop extends FunctorExp {
		CatExp cat;
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Prop other = (Prop) obj;
			if (cat == null) {
				if (other.cat != null)
					return false;
			} else if (!cat.equals(other.cat))
				return false;
			return true;
		}

		public Prop(CatExp cat) {
			super();
			this.cat = cat;
		}
		
		
		
	}

	public static class One extends FunctorExp {
		public CatExp cat;
		public CatExp ambient;
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public One(CatExp cat, CatExp ambient) {
			super();
			this.cat = cat;
			this.ambient = ambient;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ambient == null) ? 0 : ambient.hashCode());
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
			One other = (One) obj;
			if (ambient == null) {
				if (other.ambient != null)
					return false;
			} else if (!ambient.equals(other.ambient))
				return false;
			if (cat == null) {
				if (other.cat != null)
					return false;
			} else if (!cat.equals(other.cat))
				return false;
			return true;
		}
		
	}
	
	public static class Zero extends FunctorExp {
		public CatExp cat;
		public CatExp ambient;
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ambient == null) ? 0 : ambient.hashCode());
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
			Zero other = (Zero) obj;
			if (ambient == null) {
				if (other.ambient != null)
					return false;
			} else if (!ambient.equals(other.ambient))
				return false;
			if (cat == null) {
				if (other.cat != null)
					return false;
			} else if (!cat.equals(other.cat))
				return false;
			return true;
		}

		public Zero(CatExp cat, CatExp ambient) {
			super();
			this.cat = cat;
			this.ambient = ambient;
		}
		

		
	}
	
	
	public static class Iso extends FunctorExp {
		
		public Iso(boolean lToR, CatExp l, CatExp r) {
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
		CatExp l, r;
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
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}	
	}
	
	public static class Id extends FunctorExp {
		public CatExp t;

		public Id(CatExp t) {
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
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			return "id " + t;
		}

	}
	
	//TODO toString for exps
	
	public static class InstConst extends FunctorExp {
		
		public CatExp sig;
		
		public Map<String, SetExp> nm;
		public Map<String, Chc<FnExp,SetExp>> em;

		public InstConst(CatExp sig, Map<String, SetExp> nm,
				Map<String, Chc<FnExp,SetExp>> em) {
			super();
			this.sig = sig;
			this.nm = nm;
			this.em = em;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InstConst other = (InstConst) obj;
			if (em == null) {
				if (other.em != null)
					return false;
			} else if (!em.equals(other.em))
				return false;
			if (nm == null) {
				if (other.nm != null)
					return false;
			} else if (!nm.equals(other.nm))
				return false;
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((em == null) ? 0 : em.hashCode());
			result = prime * result + ((nm == null) ? 0 : nm.hashCode());
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
			return result;
		}
	}
	
	public static class MapConst extends FunctorExp {
		
		public CatExp src, dst;
		
		public Map<String, String> nm;
		public Map<String, Pair<String,List<String>>> em;
		
		public MapConst(CatExp src, CatExp dst, Map<String, String> nm,
				Map<String, Pair<String, List<String>>> em) {
			this.src = src;
			this.dst = dst;
			this.nm = nm;
			this.em = em;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MapConst other = (MapConst) obj;
			if (em == null) {
				if (other.em != null)
					return false;
			} else if (!em.equals(other.em))
				return false;
			if (nm == null) {
				if (other.nm != null)
					return false;
			} else if (!nm.equals(other.nm))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((em == null) ? 0 : em.hashCode());
			result = prime * result + ((nm == null) ? 0 : nm.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			return result;
		}
	}
	
	public static class SetSetConst extends FunctorExp {
		String ob;
		SetExp set;
		
		String f, src, dst;
		FnExp fun;
		
		public SetSetConst(String ob, SetExp set, String f, String src,
				String dst, FnExp fun) {
			this.ob = ob;
			this.set = set;
			this.f = f;
			this.src = src;
			this.dst = dst;
			this.fun = fun;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((f == null) ? 0 : f.hashCode());
			result = prime * result + ((fun == null) ? 0 : fun.hashCode());
			result = prime * result + ((ob == null) ? 0 : ob.hashCode());
			result = prime * result + ((set == null) ? 0 : set.hashCode());
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
			SetSetConst other = (SetSetConst) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
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
			if (set == null) {
				if (other.set != null)
					return false;
			} else if (!set.equals(other.set))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class FinalConst extends FunctorExp {
		
		public CatExp src, C;
		
		public Map<String, FunctorExp> nm;
		public Map<String, TransExp> em;
		
		public FinalConst(CatExp src, CatExp c, Map<String, FunctorExp> nm, Map<String, TransExp> em) {
			super();
			this.src = src;
			C = c;
			this.nm = nm;
			this.em = em;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((C == null) ? 0 : C.hashCode());
			result = prime * result + ((em == null) ? 0 : em.hashCode());
			result = prime * result + ((nm == null) ? 0 : nm.hashCode());
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
			FinalConst other = (FinalConst) obj;
			if (C == null) {
				if (other.C != null)
					return false;
			} else if (!C.equals(other.C))
				return false;
			if (em == null) {
				if (other.em != null)
					return false;
			} else if (!em.equals(other.em))
				return false;
			if (nm == null) {
				if (other.nm != null)
					return false;
			} else if (!nm.equals(other.nm))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
		
		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class CatConst extends FunctorExp {
		
		public CatExp sig;
		
		public Map<String, CatExp> nm;
		public Map<String, FunctorExp> em;

		public CatConst(CatExp sig, Map<String, CatExp> nm,
				Map<String, FunctorExp> em) {
			this.sig = sig;
			this.nm = nm;
			this.em = em;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CatConst other = (CatConst) obj;
			if (em == null) {
				if (other.em != null)
					return false;
			} else if (!em.equals(other.em))
				return false;
			if (nm == null) {
				if (other.nm != null)
					return false;
			} else if (!nm.equals(other.nm))
				return false;
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((em == null) ? 0 : em.hashCode());
			result = prime * result + ((nm == null) ? 0 : nm.hashCode());
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
			return result;
		}
	}

	public static class Var extends FunctorExp {
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
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class TT extends FunctorExp {
		CatExp t;

		public TT(CatExp t) {
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
			TT other = (TT) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "tt " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class FF extends FunctorExp {
		CatExp t;

		public FF(CatExp t) {
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
			FF other = (FF) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "void " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Fst extends FunctorExp {
		CatExp s, t;

		public Fst(CatExp s, CatExp t) {
			this.s = s;
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Fst other = (Fst) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "fst " + s + " " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Snd extends FunctorExp {
		CatExp s, t;

		public Snd(CatExp s, CatExp t) {
			this.s = s;
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Snd other = (Snd) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "snd " + s + " " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Inl extends FunctorExp {
		CatExp s, t;

		public Inl(CatExp s, CatExp t) {
			this.s = s;
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Inl other = (Inl) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "inl " + s + " " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Inr extends FunctorExp {
		CatExp s, t;

		public Inr(CatExp s, CatExp t) {
			this.s = s;
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Inr other = (Inr) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "inr " + s + " " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Eval extends FunctorExp {
		CatExp s, t;

		public Eval(CatExp s, CatExp t) {
			this.s = s;
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Eval other = (Eval) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "eval " + s + " " + t;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Curry extends FunctorExp {
		FunctorExp f;

		public Curry(FunctorExp f) {
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
			Curry other = (Curry) obj;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			return true;
		}

		public String toString() {
			return "curry " + f;
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Prod extends FunctorExp {
		FunctorExp l, r;

		public Prod(FunctorExp l, FunctorExp r) {
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

		public String toString() {
			return "(" + l + " * " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Exp extends FunctorExp {
		FunctorExp l, r;

		public Exp(FunctorExp l, FunctorExp r) {
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
			Exp other = (Exp) obj;
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

		public String toString() {
			return "(" + l + " ^ " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	

	public static class Comp extends FunctorExp {
		FunctorExp l, r;

		public Comp(FunctorExp FunctorExp, FunctorExp r) {
			this.l = FunctorExp;
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

		public String toString() {
			return "(" + l + " ; " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Case extends FunctorExp {
		FunctorExp l, r;

		public Case(FunctorExp l, FunctorExp r) {
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
			Case other = (Case) obj;
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

		public String toString() {
			return "(" + l + " + " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, FunctorExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public abstract int hashCode();

	public interface FunctorExpVisitor<R, E> {
		public R visit(E env, Id e);
		public R visit(E env, Comp e);
		public R visit(E env, Var e);
		public R visit(E env, InstConst e);
		public R visit(E env, MapConst e);
		public R visit(E env, CatConst e);
		public R visit(E env, TT e);
		public R visit(E env, FF e);
		public R visit(E env, Fst e);
		public R visit(E env, Snd e);
		public R visit(E env, Inl e);
		public R visit(E env, Inr e);
		public R visit(E env, Eval e);
		public R visit(E env, Curry e);
		public R visit(E env, Case e);
		public R visit(E env, Prod e);
		public R visit(E env, Iso e);
		public R visit(E env, SetSetConst e);
		public R visit(E env, One e);
		public R visit(E env, Zero e);
		public R visit(E env, FinalConst e);
		public R visit(E env, Uncurry e);
		public R visit(E env, Migrate e);
		public R visit(E env, Apply e);
		public R visit(E env, Exp e);
		public R visit(E env, Prop e);
		public R visit(E env, Dom e);
		public R visit(E env, Pivot e);
		public R visit(E env, Pushout e);
	}

}
