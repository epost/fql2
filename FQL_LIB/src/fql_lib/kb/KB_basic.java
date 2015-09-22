package fql_lib.kb;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import fql_lib.Pair;

public class KB_basic<C, V> {
	/*
	public KB_basic(Set<Pair<KBExp<C, V>, KBExp<C, V>>> E0,
			Function<Pair<KBExp<C, V>, KBExp<C, V>>, Boolean> gt0, Iterator<V> fresh) {
		super(E0, gt0, fresh);
	}

	@Override
	protected Set<Pair<KBExp<C, V>, KBExp<C, V>>> allcps2(
			Set<Pair<Pair<KBExp<C, V>, KBExp<C, V>>, Pair<KBExp<C, V>, KBExp<C, V>>>> seen,
			Pair<KBExp<C, V>, KBExp<C, V>> ab) {
		return new HashSet<>();
	}

	@Override
	protected KBExp<C, V> step1Es(List<Pair<KBExp<C, V>, KBExp<C, V>>> E, KBExp<C, V> e) {
		return e;
	}

	@Override
	protected void simplify2() { }

	@Override
	protected void collapseBy2(Pair<KBExp<C, V>, KBExp<C, V>> ab) { }

	@Override
	protected void handleUnorientable(Pair<KBExp<C, V>, KBExp<C, V>> st) {
		throw new RuntimeException("Cannot orient " + st.first + " and " + st.second);
	}

	@Override
	protected boolean checkEmpty() {
		if (E.isEmpty()) {
			isComplete = true;
			isCompleteGround = true;
			return true;
		}		
		return false;
	}
	
	public boolean eq(KBExp<C, V> lhs, KBExp<C, V> rhs) {
		KBExp<C, V> lhs0 = nf(lhs);
		KBExp<C, V> rhs0 = nf(rhs);
		if (lhs0.equals(rhs0)) {
			return true;
		}
		
		if (isComplete) {
			return false;
		}
		
		step();
		return eq(lhs, rhs);
	}
*/
}
