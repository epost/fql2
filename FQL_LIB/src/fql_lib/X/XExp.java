package fql_lib.X;

import java.util.List;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Util;

public abstract class XExp {
	
	public static class XRel extends XExp {
		XExp I;
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public XRel(XExp i) {
			super();
			I = i;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			XRel other = (XRel) obj;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}
		
		
	}
	
	public static class XUnit extends XExp {
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		String kind;
		XExp F, I;
		public XUnit(String kind, XExp f, XExp i) {
			super();
			this.kind = kind;
			F = f;
			I = i;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
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
			XUnit other = (XUnit) obj;
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
			if (kind == null) {
				if (other.kind != null)
					return false;
			} else if (!kind.equals(other.kind))
				return false;
			return true;
		}
		
		
	}
	
	public static class XCounit extends XExp {
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		String kind;
		XExp F, I;
		public XCounit(String kind, XExp f, XExp i) {
			super();
			this.kind = kind;
			F = f;
			I = i;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
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
			XCounit other = (XCounit) obj;
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
			if (kind == null) {
				if (other.kind != null)
					return false;
			} else if (!kind.equals(other.kind))
				return false;
			return true;
		}		
		
		
	}
	
	
	@Override
	public abstract boolean equals(Object o);

	public abstract <R, E> R accept(E env, XExpVisitor<R, E> v);

	@Override
	public abstract int hashCode();
	
	public interface XExpVisitor<R, E> {
		public R visit (E env, XSchema e);
		public R visit (E env, XMapConst e);
		public R visit (E env, XTransConst e);
		public R visit (E env, XSigma e);
		public R visit (E env, XDelta e);
		public R visit (E env, XInst e);
		public R visit (E env, Var e);
		public R visit (E env, XTy e);
		public R visit (E env, XFn e);
		public R visit (E env, XConst e);
		public R visit (E env, XEq e);
		public R visit (E env, XUnit e);
		public R visit (E env, XCounit e);
		public R visit (E env, XPi e);
		public R visit (E env, XRel e);
	}
	
	public static class XTy extends XExp {
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((javaName == null) ? 0 : javaName.hashCode());
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
			XTy other = (XTy) obj;
			if (javaName == null) {
				if (other.javaName != null)
					return false;
			} else if (!javaName.equals(other.javaName))
				return false;
			return true;
		}

		public XTy(String javaName) {
			super();
			this.javaName = javaName;
		}

		String javaName;
	}
	
	public static class XFn extends XExp {
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((javaFn == null) ? 0 : javaFn.hashCode());
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
			XFn other = (XFn) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (javaFn == null) {
				if (other.javaFn != null)
					return false;
			} else if (!javaFn.equals(other.javaFn))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		public XFn(String src, String dst, String javaFn) {
			super();
			this.src = src;
			this.dst = dst;
			this.javaFn = javaFn;
		}

		String src, dst, javaFn;
	}
	
	public static class XConst extends XExp {
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		String dst, javaFn;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((javaFn == null) ? 0 : javaFn.hashCode());
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
			XConst other = (XConst) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (javaFn == null) {
				if (other.javaFn != null)
					return false;
			} else if (!javaFn.equals(other.javaFn))
				return false;
			return true;
		}

		public XConst(String dst, String javaFn) {
			super();
			this.dst = dst;
			this.javaFn = javaFn;
		}
	}
	
	public static class XEq extends XExp {
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
			result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
			XEq other = (XEq) obj;
			if (lhs == null) {
				if (other.lhs != null)
					return false;
			} else if (!lhs.equals(other.lhs))
				return false;
			if (rhs == null) {
				if (other.rhs != null)
					return false;
			} else if (!rhs.equals(other.rhs))
				return false;
			return true;
		}

		public XEq(List<String> lhs, List<String> rhs) {
			super();
			this.lhs = lhs;
			this.rhs = rhs;
		}

		List<String> lhs, rhs;
	}
	
	public static class XInst extends XExp {
		public XExp schema;
		public XInst(XExp schema, List<Pair<String, String>> nodes,
				List<Pair<List<String>, List<String>>> eqs) {
			super();
			this.schema = schema;
			this.nodes = nodes;
			this.eqs = eqs;
		}


		public List<Pair<String, String>> nodes;
		public List<Pair<List<String>, List<String>>> eqs;
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
			XInst other = (XInst) obj;
			if (eqs == null) {
				if (other.eqs != null)
					return false;
			} else if (!eqs.equals(other.eqs))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		@Override
		public String toString() {
			String x = "\n variables\n";
			boolean b = false;
			
			List<String> nodes2 = nodes.stream().map(z -> "  " + z.first + ": " + z.second).collect(Collectors.toList());
			x += Util.sep(nodes2, ",\n");
			x = x.trim();
			x += ";\n";
			x += " equations\n";
			List<String> eqs2 = eqs.stream().map(z -> "  " + Util.sep(z.first, ".") + " = " + Util.sep(z.second, ".")).collect(Collectors.toList());
			x += Util.sep(eqs2, ",\n");
			x = x.trim();
			return "instance {\n " + x + ";\n}";
		}
	}
	
	public static class XSchema extends XExp {
		public List<String> nodes;
		public List<Triple<String, String, String>> arrows;
		public List<Pair<List<String>, List<String>>> eqs;

		public XSchema(
				List<String> nodes,
				List<Triple<String, String, String>> arrows,
				List<Pair<List<String>, List<String>>> eqs) {
			this.nodes = nodes;
			this.arrows = arrows;
			this.eqs = eqs;
		}

		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((arrows == null) ? 0 : arrows.hashCode());
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
			XSchema other = (XSchema) obj;
			if (arrows == null) {
				if (other.arrows != null)
					return false;
			} else if (!arrows.equals(other.arrows))
				return false;
			if (eqs == null) {
				if (other.eqs != null)
					return false;
			} else if (!eqs.equals(other.eqs))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			String x = "\n nodes\n";
			boolean b = false;
			for (String n : nodes) {
				if (b) {
					x += ",\n";
				}
				x += "  " + n;
				b = true;
			}
			
			x = x.trim();
			x += ";\n";
			x += " edges\n";

			b = false;
			for (Triple<String, String, String> a : arrows) {
				if (b) {
					x += ",\n";
				}
				x += "  " + a.first + ": " + a.second + " -> " + a.third;
				b = true;
			}

			x = x.trim();
			x += ";\n";
			x += " equations\n";

			b = false;
			for (Pair<List<String>, List<String>> a : eqs) {
				if (b) {
					x += ",\n";
				}
				x += "  " + printOneEq(a.first) + " = " + printOneEq(a.second);
				b = true;
			}
			x = x.trim();
			return "schema {\n " + x + ";\n}";
		}
		
		private String printOneEq(List<String> l) {
			return Util.sep(l, ".");
		}
		
	}
	
	public static class Var extends XExp {
		public String v;

		public Var(String v) {
			if (v.contains(" ") || v.equals("void") || v.equals("unit")) {
				throw new RuntimeException("Cannot var " + v);
			}
			this.v = v;
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
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class XTransConst extends XExp {
		XExp src, dst;
		public List<Pair<String, List<String>>> vm;
		

		public XTransConst(XExp src, XExp dst, List<Pair<String, List<String>>> vm) {
			super();
			this.src = src;
			this.dst = dst;
			this.vm = vm;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			result = prime * result + ((vm == null) ? 0 : vm.hashCode());
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
			XTransConst other = (XTransConst) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			if (vm == null) {
				if (other.vm != null)
					return false;
			} else if (!vm.equals(other.vm))
				return false;
			return true;
		}



		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class XMapConst extends XExp {
		
		public XExp src, dst;
		
		public List<Pair<String, String>> nm;
		public List<Pair<String, List<String>>> em;
		

		public XMapConst(XExp src, XExp dst, List<Pair<String, String>> nm,
				List<Pair<String, List<String>>> em) {
			super();
			this.src = src;
			this.dst = dst;
			this.nm = nm;
			this.em = em;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			XMapConst other = (XMapConst) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((em == null) ? 0 : em.hashCode());
			result = prime * result + ((nm == null) ? 0 : nm.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			return result;
		}
		
		@Override
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		@Override
		public String toString() {	
			String nm0 = "\n nodes\n";
			boolean b = false;
			for (Pair<String, String> k : nm) {
				if (b) {
					nm0 += ",\n";
				}
				b = true;
				nm0 += "  " + k.first + " -> " + k.second;
			}
			nm0 = nm0.trim();
			nm0 += ";\n";

			nm0 += " edges\n";
			b = false;
			for (Pair<String, List<String>> k : em) {
				if (b) {
					nm0 += ",\n";
				}
				b = true;
				nm0 += "  " + k.first + " -> " + Util.sep(k.second, ".");
			}
			nm0 = nm0.trim();
			nm0 += ";\n";

			return "mapping {\n " + nm0 + "}"; // : " + src + " -> " + dst;
		}

	}
	
	
	
	public static class XSigma extends XExp {
		XExp F, I;
		

		public XSigma(XExp f, XExp i) {
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
			XSigma other = (XSigma) obj;
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
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	
	public static class XPi extends XExp {
		XExp F, I;
		

		public XPi(XExp f, XExp i) {
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
			XPi other = (XPi) obj;
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
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}

	
	public static class XDelta extends XExp {
		XExp F, I;
		

		public XDelta(XExp f, XExp i) {
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
			XDelta other = (XDelta) obj;
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
		public <R, E> R accept(E env, XExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
	}
	

}
