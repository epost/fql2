package catdata.aql;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.fdm.TransformLiteral; //TODO aql why depend fdm

public final class Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> {

	public final Ctx<En2, Frozen<Ty,En1,Sym,Fk1,Att1>> ens = new Ctx<>();
	public final Ctx<Att2, Term<Ty,En1,Sym,Fk1,Att1,Var,Void>> atts = new Ctx<>(); 
	
	public final Ctx<Fk2, Transform<Ty,En1,Sym,Fk1,Att1,Var,Void,Var,Void,Void,Void,Void,Void>> fks = new Ctx<>();
		
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
			this.ens.put(en2, new Frozen<>(ens.get(en2).first, ens.get(en2).second, src, ens.get(en2).third));
		}
		for (Fk2 fk2 : fks.keySet()) {
			this.fks.put(fk2, new TransformLiteral<>(fks.get(fk2).first.map, new HashMap<>(), this.ens.get(dst.fks.get(fk2).second), this.ens.get(dst.fks.get(fk2).first), fks.get(fk2).second));
		}
		if (!doNotCheckPathEqs) {
			validate();
		}
	}

	public void totalityCheck(Ctx<En2, ?> ens2, Ctx<Att2, ?> atts, Ctx<Fk2, ?> fks2) {
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
				throw new RuntimeException("no term for attribute " + att2);
			}
		}
		for (Att2 att2 : atts.keySet()) {
			if (!dst.atts.containsKey(att2)) {
				throw new RuntimeException("there is a term for " + att2 + ", which is not an attribute in the target");
			}
		}
		for (Fk2 fk2 : dst.fks.keySet()) {
			if (!fks2.containsKey(fk2)) {
				throw new RuntimeException("no transform for foreign key " + fk2);
			}
		}
		for (Fk2 fk2 : fks2.keySet()) {
			if (!dst.fks.containsKey(fk2)) {
				throw new RuntimeException("there is a transform for " + fk2 + ", which is not a foreign kety in the target");
			}
		}
	}
	
	public void validate() {
		//TODO aql validate query
	}
	
	public static class Frozen<Ty,En1,Sym,Fk1,Att1> extends Instance<Ty,En1,Sym,Fk1,Att1,Var,Void,Void,Void> {

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
		
		public Frozen(Ctx<Var, En1> gens, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs, Schema<Ty, En1, Sym, Fk1, Att1> schema, Map<String, String> options) {
			this.gens = gens;
			this.eqs = eqs;
			this.schema = schema;
			dp = AqlProver.create(new AqlOptions(options, collage()), collage());			
			validate();
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

		@Override
		public Algebra<Ty, En1, Sym, Fk1, Att1, Var, Void, Void, Void> algebra() {
			throw new RuntimeException("Anomaly: please report");
		}
		
	}
	
	
}
