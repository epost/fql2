package catdata.fqlpp.cat;

import java.util.List;
import java.util.Set;

public interface Operad<O,A> {

	static class Arrow<O,A> {
		public A a;
		public List<O> src;
		public O dst;
		
		public Arrow(List<O> src, O dst, A a) {
			this.a = a;
			this.src = src;
			this.dst = dst;
		}
		
		@Override
		public String toString() {
			return "Arrow [a=" + a + ", src=" + src + ", dst=" + dst + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
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
			Arrow<?,?> other = (Arrow<?,?>) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
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
			return true;
		}
		
	}
	
	public Set<O> objects();
	
	public Set<Arrow<O,A>> hom(List<O> src, O dst);
	
	public Arrow<O,A> id(O o);
	
	public Arrow<O,A> comp(Arrow<O,A> F, List<Arrow<O,A>> A);
	
}
