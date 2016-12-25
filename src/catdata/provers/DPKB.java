package catdata.provers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;

public abstract class DPKB<T,C,V> {

		final Collection<Triple<Map<V,T>, KBExp<C,V>, KBExp<C,V>>> theory;
		final Map<C,Pair<List<T>,T>> signature;
		final Collection<T> sorts;
	
		DPKB() {
			sorts = null;
			theory = null;
			signature = null;
		}
		
		DPKB(Collection<T> sorts, Map<C, Pair<List<T>, T>> signature, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> theory) {
			Util.assertNotNull(sorts, theory, signature);
			this.sorts = sorts;
			this.signature = signature;
			this.theory = theory;		
		}
	
			
		public abstract boolean eq(Map<V, T> ctx, KBExp<C,V> lhs, KBExp<C,V> rhs);
		
		public abstract boolean hasNFs();
		
		public abstract KBExp<C,V> nf(Map<V, T> ctx, KBExp<C,V> term);
				
}
