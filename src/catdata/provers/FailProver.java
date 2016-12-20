package catdata.provers;

import java.util.Map;

public class FailProver<T,C,V> extends DPKB<T,C,V> {

	public FailProver() { 
		
	}

	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		throw new RuntimeException();
	}

	@Override
	public boolean hasNFs() {
		return false;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		throw new RuntimeException();
	}
	
	@Override
	public String toString() {
		throw new RuntimeException();
	}


}
