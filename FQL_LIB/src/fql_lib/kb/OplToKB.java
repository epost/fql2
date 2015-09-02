package fql_lib.kb;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.kb.KBExp.KBApp;
import fql_lib.kb.KBExp.KBVar;
import fql_lib.opl.OplExp.OplCtx;
import fql_lib.opl.OplExp.OplSig;
import fql_lib.opl.OplExp.OplTerm;

public class OplToKB {

	
	
	private static KBExp<String, String> convert(OplTerm t) {
		if (t.var != null) {
			return new KBVar<>(t.var);
		}
		List<KBExp<String, String>> l = t.args.stream().map(x -> { return convert(x); }).collect(Collectors.toList());
		return new KBApp<>(t.head, l);
	}
	
	public static KB<String, String> convert(OplSig s, String which) {
		Set<Pair<KBExp<String, String>, KBExp<String, String>>> eqs = new HashSet<>();
		for (Triple<OplCtx<String>, OplTerm, OplTerm> eq : s.equations) {
			eqs.add(new Pair<>(convert(eq.second), convert(eq.third)));
		}
		
		Function<Pair<String, String>, Boolean> gt = x -> {
			String l = x.first;
			String r = x.second;
		/*	int la = s.symbols.get(l).first.size();
			int ra = s.symbols.get(r).first.size();
			if (la > ra) {
				return true;
			} */
			
			return l.compareTo(r) < 0;
		};
		
		Iterator<String> fr = new Iterator<String>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return true;
			}
			@Override
			public String next() {
				return "v" + (i++);
			}
		};
		
			return new KB<>(eqs, KBOrders.pogt(gt, which), fr);
	}
	
}
