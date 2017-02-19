package catdata.aql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.It.ID;
import catdata.aql.fdm.ComposeTransform;
import catdata.aql.fdm.IdentityTransform;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralTransform; //TODO aql why depend fdm

public final class Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> implements Semantics {

	@Override
	public Kind kind() {
		return Kind.QUERY;
	}
	
	public final Ctx<En2, Frozen<Ty,En1,Sym,Fk1,Att1>> ens = new Ctx<>();
	public final Ctx<Att2, Term<Ty,En1,Sym,Fk1,Att1,Var,Void>> atts; 
	
	public final Ctx<Fk2, Transform<Ty,En1,Sym,Fk1,Att1,Var,Void,Var,Void,ID,Chc<Void, Pair<ID, Att1>>,ID,Chc<Void, Pair<ID, Att1>>>> fks = new Ctx<>();
	public final Ctx<Fk2, Boolean> doNotValidate = new Ctx<>();
	
	public final Schema<Ty,En1,Sym,Fk1,Att1> src;
	public final Schema<Ty,En2,Sym,Fk2,Att2> dst;

	public Query(
			Ctx<En2, Triple<Ctx<Var,En1>,Collection<Eq<Ty,En1,Sym,Fk1,Att1,Var,Void>>,Map<String,String>>> ens,
			Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts, 
			Ctx<Fk2, Pair<Ctx<Var, Term<Void,En1,Void,Fk1,Void,Var,Void>>, Boolean>> fks,
			Schema<Ty, En1, Sym, Fk1, Att1> src, Schema<Ty, En2, Sym, Fk2, Att2> dst,
			boolean doNotCheckPathEqs) {
		Util.assertNotNull(ens, atts, fks, src, dst);
		this.src = src;
		this.dst = dst;
		totalityCheck(ens, atts, fks);
		
		for (En2 en2 : ens.keySet()) {
			try {
				this.ens.put(en2, new Frozen<>(ens.get(en2).first, ens.get(en2).second, src, ens.get(en2).third));
			} catch (Throwable thr) {
				throw new RuntimeException("In block for entity " + en2 + ", " + thr.getMessage());
			}
		}
		
		for (Fk2 fk2 : fks.keySet()) {
			try {
				this.fks.put(fk2, new LiteralTransform<>(fks.get(fk2).first.map, new HashMap<>(), this.ens.get(dst.fks.get(fk2).second), this.ens.get(dst.fks.get(fk2).first), fks.get(fk2).second));
                doNotValidate.put(fk2, fks.get(fk2).second);
			} catch (Throwable thr) {
				throw new RuntimeException("In transform for foreign key " + fk2 + ", " + thr.getMessage());
			}
		}
		this.atts = new Ctx<>(atts.map);
		if (!doNotCheckPathEqs) {
			validate();
		}
	}

	private void totalityCheck(Ctx<En2, ?> ens2, Ctx<Att2, ?> atts, Ctx<Fk2, ?> fks2) {
		for (En2 en2 : dst.ens) {
			if (!ens2.containsKey(en2)) {
				throw new RuntimeException("no query for " + en2);
			}
		}
		for (En2 en2 : ens2.keySet()) {
			if (!dst.ens.contains(en2)) {
				throw new RuntimeException("there is a query for " + en2 + ", which is not an entity in the target");
			}
		}
		for (Att2 att2 : dst.atts.keySet()) {
			if (!atts.containsKey(att2)) {
				throw new RuntimeException("no return clause for attribute " + att2);
			}
		}
		for (Att2 att2 : atts.keySet()) {
			if (!dst.atts.containsKey(att2)) {
				throw new RuntimeException("there is a return clause for " + att2 + ", which is not an attribute in the target");
			}
		}
		for (Fk2 fk2 : dst.fks.keySet()) {
			if (!fks2.containsKey(fk2)) {
				throw new RuntimeException("no transform for foreign key " + fk2);
			}
		}
		for (Fk2 fk2 : fks2.keySet()) {
			if (!dst.fks.containsKey(fk2)) {
				throw new RuntimeException("there is a transform for " + fk2 + ", which is not a foreign key in the target");
			}
		}
	}
	
	private void validate() {
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : dst.eqs) {
			Chc<Ty, En2> ty = dst.type(eq.first, eq.second);
			Frozen<Ty, En1, Sym, Fk1, Att1> I = ens.get(eq.first.second);
            if (ty.left) {
                Term<Ty, En1, Sym, Fk1, Att1, Var, Void> lhs = transT(eq.second);
                Term<Ty, En1, Sym, Fk1, Att1, Var, Void> rhs = transT(eq.third);
                if (!I.dp.eq(new Ctx<>(), lhs, rhs)) {
                    throw new RuntimeException("Target equation " + eq.second + " = " + eq.third + " not respected: transforms to " + lhs + " = " + rhs + ", which is not provable in the sub-query for " + eq.first.second);
                }
            } else { //entity
                for (Var u : ens.get(ty.r).gens.keySet()) {
                    Term<Ty, En1, Sym, Fk1, Att1, Var, Void> lhs = transP(eq.second, Term.Gen(u), ty.r);
                    Term<Ty, En1, Sym, Fk1, Att1, Var, Void> rhs = transP(eq.third, Term.Gen(u), ty.r);
                    if (!I.dp.eq(new Ctx<>(), lhs, rhs)) {
                        throw new RuntimeException("Target equation " + eq.second + " = " + eq.third + " not respected: transforms to " + lhs + " = " + rhs + ", which is not provable in the sub-query for " + eq.first.second);
                    }
                }
            }
		}
	}
	
	//TODO aql pass just for better error messages in uber flowers
	public static class Frozen<Ty,En1,Sym,Fk1,Att1> extends Instance<Ty,En1,Sym,Fk1,Att1,Var,Void,ID,Chc<Void, Pair<ID, Att1>>> {

		public List<Var> order() {
			Map<Var, Integer> counts = new HashMap<>();
			List<Var> ret = new LinkedList<>();
			for (Var v1 : gens.keySet()) {
				counts.put(v1, 0);
				ret.add(v1);
			}
			for (Eq<Ty, En1, Sym, Fk1, Att1, Var, Void> eq : eqs) {
				inc(eq.lhs, counts);
				inc(eq.rhs, counts);
			}
			
			ret.sort((o1,o2) -> counts.get(o2).compareTo(counts.get(o1)));

			return ret;
		}
		
		private void inc(Term<Ty, En1, Sym, Fk1, Att1, Var, Void> t, Map<Var, Integer> counts) {
			if (t.gen != null) {
				counts.put(t.gen, counts.get(t.gen) + 1);
				return;
			} 
			for (Term<Ty, En1, Sym, Fk1, Att1, Var, Void> arg : t.args()) {
				inc(arg, counts);
			}
		}
		
		public final Ctx<Var,En1> gens;
		public final Collection<Eq<Ty,En1,Sym,Fk1,Att1,Var,Void>> eqs;
		public final Schema<Ty, En1, Sym, Fk1, Att1> schema;
		private final DP<Ty, En1, Sym, Fk1, Att1, Var, Void> dp;
		public final Map<String, String> options;
		
		public Frozen(Ctx<Var, En1> gens, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs, Schema<Ty, En1, Sym, Fk1, Att1> schema, Map<String, String> options) {
			this.gens = gens;
			this.eqs = eqs;
			this.schema = schema;
			this.options = options;
			dp = AqlProver.create(new AqlOptions(options, collage()), collage(), schema.typeSide.js);			
			validateNoTalg(); 
		}

		@Override
		public Schema<Ty, En1, Sym, Fk1, Att1> schema() {
			return schema;
		}

		@Override
		public Ctx<Var, En1> gens() {
			return gens;
		}

		@Override
		public Ctx<Void, Ty> sks() {
			return new Ctx<>();
		}

		@Override
		public Set<Pair<Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>>> eqs() {
			return eqs.stream().map(x -> new Pair<>(x.lhs, x.rhs)).collect(Collectors.toSet());
		}

		@Override
		public DP<Ty, En1, Sym, Fk1, Att1, Var, Void> dp() {
			return dp;
		}
	
		private InitialAlgebra<Ty, En1, Sym, Fk1, Att1, Var, Void, ID> hidden;
		@Override
		public Algebra<Ty, En1, Sym, Fk1, Att1, Var, Void, ID, Chc<Void, Pair<ID, Att1>>> algebra() {	
			if (hidden!=null) {
				return hidden;
			}
			hidden = new InitialAlgebra<>(dp, schema, collage(), new It(), x->x.toString(), x->x.toString());
			return hidden;
		}

		@Override
		public boolean requireConsistency() {
			return false;
		}

		@Override
		public boolean allowUnsafeJava() {
			return true;
		}
		
	}
	
	@Override
	public final String toString() {
		String ret = "";
		
		ret += "---- entities ---------------------------\n\n";
		ret += Util.sep(ens.map, "\n\n", "\n----\n\n");
		ret += "\n\n---- foreign keys------------------------\n\n";
		ret += Util.sep(fks.map, "\n\n", "\n----\n\n");
		ret += "\n\n---- attributes---------------------\n\n";
		ret += Util.sep(atts.map, "\n\n", "\n----\n\n");
		
		return ret;
	}
	
	private Term<Ty,En1,Sym,Fk1,Att1,Var,Void> transT(Term<Ty,En2,Sym,Fk2,Att2,Void,Void> term) {
		if (term.obj != null) {
			return term.asObj();
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::transT).collect(Collectors.toList()));
		} else if (term.att != null) {
			return transP(term.arg, atts.get(term.att), dst.atts.get(term.att).first);
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	private List<Fk2> transP(Term<Ty,En2,Sym,Fk2,Att2,Void,Void> term) {
		if (term.var != null) {
			return Collections.emptyList();
		} else if (term.fk != null) {
			return Util.append(Util.singList(term.fk), transP(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	private Transform<Ty,En1,Sym,Fk1,Att1,Var,Void,Var,Void,ID,Chc<Void, Pair<ID, Att1>>,ID,Chc<Void, Pair<ID, Att1>>> compose(List<Fk2> l, En2 en2) {
		if (l.isEmpty()) {
			return new IdentityTransform<>(ens.get(en2));
		} else {
			Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> t = fks.get(l.get(0));
			Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> u = compose(l.subList(1, l.size()), dst.fks.get(l.get(0)).first);
			return new ComposeTransform<>(t, u);
		}
	}
	
	private Term<Ty,En1,Sym,Fk1,Att1,Var,Void> transP(Term<Ty,En2,Sym,Fk2,Att2,Void,Void> term, Term<Ty,En1,Sym,Fk1,Att1,Var,Void> u, En2 en2) {
		List<Fk2> l = transP(term);
		Transform<Ty, En1, Sym, Fk1, Att1, Var, Void, Var, Void, ID, Chc<Void, Pair<ID, Att1>>, ID, Chc<Void, Pair<ID, Att1>>> t = compose(l, en2);
		return t.trans(u);
	}
	
	
}
