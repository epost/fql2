package catdata.provers;

import java.util.Map;

public class FreeProver<T,C,V> extends DPKB<T,C,V> {

	public FreeProver(KBTheory<T,C,V> th) { 
		super(th.tys, th.syms, th.eqs);
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
