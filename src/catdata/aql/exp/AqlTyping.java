package catdata.aql.exp;

import catdata.LineException;
import catdata.Pair;
import catdata.Program;
import catdata.aql.AqlOptions;
import catdata.graph.DMG;

public class AqlTyping {

	public <Ty, Sym> boolean eq(TyExp<Ty, Sym> t1, TyExp<Ty, Sym> t2) {
		return t1.resolve(prog).equals(t2.resolve(prog));
	}

	public <Ty, En, Sym, Fk, Att> boolean eq(SchExp<Ty, En, Sym, Fk, Att> s1, SchExp<Ty, En, Sym, Fk, Att> s2) {
		return s1.resolve(this, prog).equals(s2.resolve(this, prog));
	}

	// TODO aql turn into visitor
	public final Program<Exp<?>> prog;

	public AqlTyping(Program<Exp<?>> prog, AqlOptions defaults, boolean continue0) {
		this.defaults = defaults;
		this.prog = prog;
		for (String s : prog.order) {
			try {
				Exp<?> e = prog.exps.get(s);
				switch (e.kind()) {
				case INSTANCE:
					defs.insts.put(s, ((InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>) e).type(this));
					continue;
				case MAPPING:
					Pair<? extends SchExp<?, ?, ?, ?, ?>, ? extends SchExp<?, ?, ?, ?, ?>> p = ((MapExp<?, ?, ?, ?, ?, ?, ?, ?>) e)
							.type(this);
					defs.maps.put(s, new Pair<>(p.first, p.second));
					continue;
				case PRAGMA:
					continue;
				case QUERY:
					p = ((QueryExp<?, ?, ?, ?, ?, ?, ?, ?>) e).type(this);
					defs.qs.put(s, new Pair<>(p.first, p.second));
					continue;
				case SCHEMA:
					continue;
				case TRANSFORM:
					Pair<? extends InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>, ? extends InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>> q = ((TransExp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) e)
							.type(this);
					defs.trans.put(s, new Pair<>(q.first, q.second));
					continue;
				case TYPESIDE:
					continue;
				case GRAPH:
					defs.gs.put(s, ((GraphExp<?, ?>) e).eval(this).dmg);
					continue;
				case COMMENT:
					continue;
				case SCHEMA_COLIMIT:
					defs.scs.put(s, ((ColimSchExp<?>) e).type(this));
					continue;
				case CONSTRAINTS:
					defs.eds.put(s, ((EdsExp<?, ?, ?, ?, ?>) e).type(this));
					continue;
				default:
					throw new RuntimeException("Anomaly: please report");
				}
			} catch (Exception ex) {
				if (!continue0) {
					throw new LineException(ex.getMessage(), s, prog.exps.get(s).kind().toString());
				}
			}
		}
	}

	// TODO aql
	public final KindCtx<String, DMG<?, ?>, Void, Void, SchExp<?, ?, ?, ?, ?>, Pair<InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>>, Pair<SchExp<?, ?, ?, ?, ?>, SchExp<?, ?, ?, ?, ?>>, Pair<SchExp<?, ?, ?, ?, ?>, SchExp<?, ?, ?, ?, ?>>, Void, Void, ColimSchExp<?>, SchExp<?, ?, ?, ?, ?>> defs = new KindCtx<>();

	public final AqlOptions defaults;
}
