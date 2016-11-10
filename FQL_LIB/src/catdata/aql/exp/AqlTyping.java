package catdata.aql.exp;

import catdata.Pair;
import catdata.ide.Program;

public class AqlTyping {
	
	public AqlTyping(Program<Exp<?>> prog) {
		for (String s : prog.order) {
			Exp<?> e = prog.exps.get(s);
			switch (e.kind()) {
			case INSTANCE:
				defs.insts.put(s, ((InstExp<?,?,?,?,?,?,?,?,?>)e).type(this));
				continue;
			case MAPPING:
				Pair<? extends SchExp<?,?,?,?,?>, ? extends SchExp<?,?,?,?,?>> p = ((MapExp<?,?,?,?,?,?,?,?>)e).type(this);
				defs.maps.put(s, new Pair<>(p.first, p.second));
				continue; 
			case PRAGMA:
				continue;
			case QUERY:
				p = ((QueryExp<?,?,?,?,?,?,?,?>)e).type(this);			
				defs.qs.put(s, new Pair<>(p.first, p.second));
				continue;
			case SCHEMA:
				continue;
			case TRANSFORM:
				Pair<? extends InstExp<?,?,?,?,?,?,?,?,?>, ? extends InstExp<?,?,?,?,?,?,?,?,?>> q = ((TransExp<?,?,?,?,?,?,?,?,?,?,?,?,?>)e).type(this);
				defs.trans.put(s, new Pair<>(q.first, q.second));
				continue;
			case TYPESIDE:
				continue;
			case GRAPH:
				continue;
			}
			throw new RuntimeException("Anomaly: please report");
		}
	}

	public final KindCtx<String, Void, Void, Void, SchExp<?,?,?,?,?>, Pair<InstExp<?,?,?,?,?,?,?,?,?>,InstExp<?,?,?,?,?,?,?,?,?>>, Pair<SchExp<?,?,?,?,?>,SchExp<?,?,?,?,?>>, Pair<SchExp<?,?,?,?,?>,SchExp<?,?,?,?,?>>, Void> defs = new KindCtx<>();
	
}
