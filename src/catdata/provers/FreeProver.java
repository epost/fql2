package catdata.provers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import catdata.Pair;
import catdata.Triple;

public class FreeProver<T,C,V> extends DPKB<T,C,V> {

	public FreeProver(Collection<T> sorts, Map<C,Pair<List<T>,T>> signature, Collection<Triple<Map<V,T>, KBExp<C,V>, KBExp<C,V>>> theory) { 
		super(sorts, signature, theory);
		if (!theory.isEmpty()) {
			throw new RuntimeException("not an empty theory, as required by free proving strategy");
		}
	}

	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		return lhs.equals(rhs);
	}

	@Override
	public boolean hasNFs() {
		return true;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		return term;
	}
	
	@Override
	public String toString() {
		return "Free prover";
	}

	
}
