package fql_lib.X;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import fql_lib.DEBUG;
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
			if (DEBUG.debug.reorder_joins) {
				this.from = sort(from);
			} else {
				this.from = from;
			}
		}
		
		private void count(List<C> first, Map counts) {
			for (C s : first) {
				Integer i = (Integer) counts.get(s);
				if (i == null) {
					continue;
				}
				counts.put(s, i+1);
			}
		}

		public Map<String, C> sort(Map m) {
			Map count = new HashMap<>();
			for (Object s : m.keySet()) {
				count.put(s, 0);
			}
			for (Pair<List<C>, List<C>> k : where) {
				count(k.first, count);
				count(k.first, count);
			}
			List<String> l = new LinkedList<>(m.keySet());
			l.sort(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return ((Integer)count.get(o2)) - ((Integer)count.get(o1));
				}
			});
			Map ret = new LinkedHashMap<>();
			for (String s : l) {
				ret.put(s, m.get(s));
			}
			return ret;
		}
		

		Map<String, C> from = new HashMap<>(); 
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
		return "XPoly [" + blocks + "]";
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
