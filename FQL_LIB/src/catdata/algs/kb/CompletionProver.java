package catdata.algs.kb;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Triple;

//herbrandization should morally be handled here.  but it's easier to do it in the helper 
public class CompletionProver<T, C, V> extends DPKB<T, C, V> {

	public KB<C, V> kb;
	
	public CompletionProver(Function<Pair<C,C>, Boolean> gt, KBOptions op,Iterator<V> fresh, Collection<T> sorts, Map<C, Pair<List<T>, T>> sig, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> eqs) throws InterruptedException {
		super(sorts, sig, eqs);
		
		kb = new KB<>(eqs.stream().map(x -> new Pair<>(x.second, x.third)).collect(Collectors.toSet()), KBOrders.lpogt(false, gt), fresh, Collections.emptySet(), op);
		kb.complete();
	}
	
	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		if (!ctx.isEmpty() && !hasNFs()) {
			throw new RuntimeException("Cannot find normal forms for non-ground terms using system that is only ground complete.");
		}
		return nf(ctx, lhs).equals(nf(ctx, rhs));
	}

	@Override
	public boolean hasNFs() {
		if (kb.isComplete) {
			return true;
		}
		return false;
	}

	@Override
	public KBExp<C, V> nf(Map<V, T> ctx, KBExp<C, V> term) {
		return kb.nf(term);
	}
	
	@Override
	public String toString() {
		return kb.toString();
	}

}
