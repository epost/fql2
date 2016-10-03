package catdata.algs.kb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;

public class CompletionProver<T, C, V> extends DPKB<T, C, V> {

	private KB<C, V> kb;
	
	public CompletionProver(KBOptions op, List<C> prec, Iterator<V> fresh, Collection<T> sorts, Map<C, Pair<List<T>, T>> sig, Collection<Triple<Map<V, T>, KBExp<C, V>, KBExp<C, V>>> eqs) {
		super(sorts, sig, eqs);
		
		Util.assertNoDups(prec);
		if (!new HashSet<>(prec).equals(sig.keySet())) {
			Set<C> precMinusSig = new HashSet<>(prec);
			precMinusSig.removeAll(sig.keySet());
			Set<C> sigMinusPrec = new HashSet<>(sig.keySet());
			sigMinusPrec.removeAll(prec);
			throw new RuntimeException("Incorrect precedence. Symbols in precedence but not signature: " + precMinusSig + " and symbols in signature but not precedence: " + sigMinusPrec);
		}
		Function<Pair<C,C>, Boolean> gt = x -> {
			int i = prec.indexOf(x.first);
			int j = prec.indexOf(x.second);
			if (i == -1 || j == -1) {
				throw new RuntimeException("Anomaly: please report");
			}
			return i > j;
		};
		kb = new KB<>(eqs.stream().map(x -> new Pair<>(x.second, x.third)).collect(Collectors.toSet()), KBOrders.lpogt(false, gt), fresh, Collections.emptySet(), op);
		kb.complete(Thread.currentThread());
	}
	
	@Override
	public boolean eq(Map<V, T> ctx, KBExp<C, V> lhs, KBExp<C, V> rhs) {
		return nf(ctx, lhs).equals(nf(ctx, rhs));
	}

	@Override
	public boolean hasNFs() {
		return true;
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
