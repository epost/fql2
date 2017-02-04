package catdata.aql.exp;

import catdata.Pair;
import catdata.Program;
import catdata.aql.exp.ColimSchExp.ColimSchExpLit;
import catdata.graph.DMG;

public class AqlTyping {
	
	//TODO aql turn into visitor
	
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
				defs.gs.put(s, ((GraphExp<?,?>)e).eval(this).dmg);
				continue;
			case COMMENT:
				continue;
			case SCHEMA_COLIMIT:
				defs.scs.put(s, ((ColimSchExp<?,?,?,?,?,?,?>)e).type(this));
				continue;
			case CONSTRAINTS:
				defs.eds.put(s, ((EdsExp<?,?,?,?,?>)e).type(this));
				continue;
			default:
				break;
			}
			throw new RuntimeException("Anomaly: please report");
		}
	}

	//TODO aql
	public final KindCtx<String, DMG<?,?>, Void, Void, SchExp<?,?,?,?,?>, Pair<InstExp<?,?,?,?,?,?,?,?,?>,InstExp<?,?,?,?,?,?,?,?,?>>, Pair<SchExp<?,?,?,?,?>,SchExp<?,?,?,?,?>>, Pair<SchExp<?,?,?,?,?>,SchExp<?,?,?,?,?>>, Void, Void, ColimSchExpLit<?,?,?,?,?,?,?>,SchExp<?,?,?,?,?>> defs = new KindCtx<>();
	
}
