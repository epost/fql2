package fql_lib.X;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import fql_lib.Pair;
import fql_lib.gui.FQLTextPanel;

public class XPoly<C,D> extends XExp implements XObject {


	public XPoly(XExp src, XExp dst, Map<String, Pair<D, Block<C, D>>> blocks) {
		super();
		this.src_e = src;
		this.dst_e = dst;
		this.blocks = blocks;
	}

	public static class Block<C, D> {
		
		public Block(Map<String, C> from, Set<Pair<List<C>, List<C>>> where, Map<D, List<C>> attrs,
				Map<D, Pair<String, Map<D, List<C>>>> edges) {
			super();
			this.from = from;
			this.where = where;
			this.attrs = attrs;
			this.edges = edges;
		}

		Map<String, C> from = new HashMap<>(); //String should be C
		Set<Pair<List<C>, List<C>>> where = new HashSet<>();
		Map<D, List<C>> attrs = new HashMap<>();
		Map<D, Pair<String, Map<D, List<C>>>> edges = new HashMap<>();
		@Override
		public String toString() {
			return "Block [from=" + from + ", where=" + where + ", attrs=" + attrs + ", edges="
					+ edges + "]";
		}
		
		
	}
	
	XExp src_e, dst_e;
	
	XCtx<C> src;
	XCtx<D> dst;
	Map<String, Pair<D, Block<C,D>>> blocks = new HashMap<>();
	
	@Override
	public String kind() {
		return "polynomial";
	}

	@Override
	public String toString() {
		return "XPoly [src=" + src + ", dst=" + dst + ", blocks=" + blocks + "]";
	}

	@Override
	public JComponent display() {
		return new FQLTextPanel(BorderFactory.createEtchedBorder(), "", toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XPoly other = (XPoly) obj;
		if (blocks == null) {
			if (other.blocks != null)
				return false;
		} else if (!blocks.equals(other.blocks))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (dst_e == null) {
			if (other.dst_e != null)
				return false;
		} else if (!dst_e.equals(other.dst_e))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (src_e == null) {
			if (other.src_e != null)
				return false;
		} else if (!src_e.equals(other.src_e))
			return false;
		return true;
	}

	@Override
	public <R, E> R accept(E env, XExpVisitor<R, E> v) {
		return v.visit(env, this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((dst_e == null) ? 0 : dst_e.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((src_e == null) ? 0 : src_e.hashCode());
		return result;
	}

}
