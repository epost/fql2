package catdata.algs.kb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.algs.kb.KBExp;

public abstract class DPKB<T,C,V> {

		protected final Map<String, Object> options;
		protected final Collection<Triple<Map<V,T>, KBExp<C,V>, KBExp<C,V>>> theory;
		protected final Map<C,Pair<List<T>,T>> signature;
		
		protected DPKB() { 
			options = null;
			theory = null;
			signature = null;
		}
		
		protected DPKB(Map<String, Object> options, Map<C,Pair<List<T>,T>> signature, Collection<Triple<Map<V,T>, KBExp<C,V>, KBExp<C,V>>> theory) {
			Util.assertNotNull(options);
			Util.assertNotNull(theory);
			Util.assertNotNull(signature);
			this.options = options;
			this.signature = signature;
			this.theory = theory;
		}
	
			
		public abstract boolean eq(Map<V, T> ctx, KBExp<C,V> lhs, KBExp<C,V> rhs);
		
		public abstract boolean hasNFs();
		
		public abstract KBExp<C,V> nf(Map<V, T> ctx, KBExp<C,V> term);
				
}
