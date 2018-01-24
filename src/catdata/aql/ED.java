package catdata.aql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Unit;
import catdata.Util;

public class ED<Ty, En, Sym, Fk, Att> {
	
	public static enum WHICH { FRONT, BACK };
	
	public final boolean isUnique;
	
	public static <Ty,Sym> Schema<Ty,WHICH,Sym,Unit,Void> getEDSchema(TypeSide<Ty,Sym> ty, AqlOptions ops) {
		Collage<Ty, WHICH, Sym, Unit, Void, Void, Void> col = new Collage<>(ty.collage());
		col.ens.add(WHICH.FRONT);
		col.ens.add(WHICH.BACK);
		col.fks.put(new Unit(), new Pair<>(WHICH.BACK, WHICH.FRONT));
		
		Schema<Ty,WHICH,Sym,Unit,Void> ret = new Schema<>(ty, col.ens, col.atts.map, col.fks.map, new HashSet<>(), AqlProver.create(ops, col, ty.js), true); //TODO aq;
		return ret;
	}
	
	public final Query<Ty,En,Sym,Fk,Att,WHICH,Unit,Void> Q;

	@Override
	public String toString() {
		String toString = "";
		
		if (!As.isEmpty()) {
			toString += "\tforall";
			List<String> temp = new LinkedList<>();		
			for (Entry<Var, En> p : As.map.entrySet()) {
				temp.add(p.getKey() + ":" + p.getValue());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		if (!Awh.isEmpty()) {
			toString += "\twhere";
			List<String> temp = new LinkedList<>();
			for (Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> p : Awh) {
				temp.add(p.first + " = " + p.second);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		toString += "->\n";
		if (!Es.isEmpty()) {
			toString += "\texists";
			if (isUnique) {
				toString += " unique";
			}
			List<String> temp = new LinkedList<>();		
			for (Entry<Var, En> p : Es.map.entrySet()) {
				temp.add(p.getKey() + ":" + p.getValue());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		if (!Ewh.isEmpty()) {
			toString += "\twhere";
			List<String> temp = new LinkedList<>();
			for (Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> p : Ewh) {
				temp.add(p.first + " = " + p.second);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		return toString;
	}

	public final Schema<Ty, En, Sym, Fk, Att> schema;
	
	public final Ctx<Var, En> As;

	public final Ctx<Var, En> Es;

	public final Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> Awh;
	
	public final Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> Ewh;

	private final Term<Ty, En, Sym, Fk, Att, Var, Var> freeze(Term<Ty, En, Sym, Fk, Att, Void, Void> t) {
		Term<Ty, En, Sym, Fk, Att, Var, Var> ret = t.mapGenSk(Util.voidFn(), Util.voidFn());
		Map<Var, Term<Ty, En, Sym, Fk, Att, Var, Var>> m = new HashMap<>();
		for (Var v : Util.union(As.keySet(), Es.keySet())) {
			m.put(v, Term.Gen(v));
		}
		return ret.subst(m);
	}
	private final Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>> freeze(Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> eqs) {
		Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>> ret = new LinkedList<>();
		for (Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq : eqs) {
			ret.add(new Eq<>(new Ctx<>(), freeze(eq.first), freeze(eq.second)));
		}
		return ret;
	}
	
	public ED(Schema<Ty, En, Sym, Fk, Att> schema, Ctx<Var, En> as, Ctx<Var, En> es, Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> awh, Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> ewh, boolean isUnique, AqlOptions options) {
		this.schema = schema;
		As = new Ctx<>(as.map);
		Es = new Ctx<>(es.map);
		Awh = new HashSet<>(awh);
		Ewh = new HashSet<>(ewh);
		this.isUnique = isUnique;
		if (!Collections.disjoint(As.keySet(), Es.keySet())) {
			throw new RuntimeException("The forall and exists clauses do not use disjoint variables.");
		}
		Ctx<WHICH, Triple<Ctx<Var, En>, Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>>, AqlOptions>> 
		is = new Ctx<>();
	
		is.put(WHICH.FRONT, new Triple<>(As, freeze(Awh), options));
		Ctx<Var, En> AsEs = new Ctx<>();
		AsEs.putAll(As.map);
		AsEs.putAll(Es.map);
		is.put(WHICH.BACK, new Triple<>(AsEs, freeze(Util.union(Awh, Ewh)), options));

		Ctx<Var, Term<Void, En, Void, Fk, Void, Var, Void>> ctx = new Ctx<>();
		for (Var v : As.keySet()) {
			ctx.put(v, Term.Gen(v));
		}
		Ctx<Unit, Pair<Ctx<Var, Term<Void, En, Void, Fk, Void, Var, Void>>, Boolean>> 
		fks = new Ctx<>();
		fks.put(new Unit(), new Pair<>(ctx, true));
		
		Schema<Ty, WHICH, Sym, Unit, Void> zzz = getEDSchema(schema.typeSide, options);
		Q = Query.makeQuery(is, new Ctx<>(), fks, schema, zzz, false, false); //TODO AQL speed these can be set to true
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((As == null) ? 0 : As.hashCode());
		result = prime * result + ((Awh == null) ? 0 : Awh.hashCode());
		result = prime * result + ((Es == null) ? 0 : Es.hashCode());
		result = prime * result + ((Ewh == null) ? 0 : Ewh.hashCode());
		result = prime * result + (isUnique ? 1231 : 1237);
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ED<?, ?, ?, ?, ?> other = (ED<?, ?, ?, ?, ?>) obj;
		if (As == null) {
			if (other.As != null)
				return false;
		} else if (!As.equals(other.As))
			return false;
		if (Awh == null) {
			if (other.Awh != null)
				return false;
		} else if (!Awh.equals(other.Awh))
			return false;
		if (Es == null) {
			if (other.Es != null)
				return false;
		} else if (!Es.equals(other.Es))
			return false;
		if (Ewh == null) {
			if (other.Ewh != null)
				return false;
		} else if (!Ewh.equals(other.Ewh))
			return false;
		if (isUnique != other.isUnique)
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}
	
	
	
}
