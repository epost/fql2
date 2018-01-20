package catdata.aql.exp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.DP;
import catdata.aql.ImportAlgebra;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.fdm.SaturatedInstance;

public  final class InstExpRandom
extends InstExp<Ty,En,Sym,Fk,Att,Pair<Integer,En>, Pair<Integer, Att>,Pair<Integer,En>,Pair<Integer,Att>> implements Raw {

	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	@Override 
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	public final Ctx<String, Integer> ens;
			
	public final Map<String, String> options;
	
	public final SchExp<Ty, En, Sym, Fk, Att> sch;
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	public InstExpRandom(SchExp<?,?,?,?,?> sch, List<Pair<LocStr, String>> ens, List<Pair<String, String>> options) {
		this.ens = new Ctx<>(Util.toMapSafely(LocStr.set2y(ens, x -> Integer.parseInt(x))));
		this.options = Util.toMapSafely(options);
		this.sch = (SchExp<Ty, En, Sym, Fk, Att>) sch;
		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, String> p : ens) {
			f.add(new InteriorLabel<>("generators", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("generators", f);
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return sch.deps();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((sch == null) ? 0 : sch.hashCode());
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
		InstExpRandom other = (InstExpRandom) obj;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (sch == null) {
			if (other.sch != null)
				return false;
		} else if (!sch.equals(other.sch))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String s = "random : " + sch + " {\n";
		String x = "";
		if (ens.size() > 0) {
			x = "generators\n" + Util.sep(ens.map, " -> ", "\n");
		}
		
		return s + x + "\n}";
	}

	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return sch;
	}

	//not exactly the smartest way
	@Override
	public SaturatedInstance<Ty, En, Sym, Fk, Att, Pair<Integer, En>, Pair<Integer, Att>, Pair<Integer, En>, Pair<Integer, Att>> eval(AqlEnv env) {
		int seed = (Integer) new AqlOptions(options, null, env.defaults).getOrDefault(AqlOption.random_seed);
		Random rand = new Random(seed);
	
		Schema<Ty, En, Sym, Fk, Att> schema = sch.eval(env);

		Ctx<En, Collection<Pair<Integer,En>>> ens0 = new Ctx<>();
		Ctx<Ty, Collection<Pair<Integer,Att>>> tys = new Ctx<>();
		Ctx<Pair<Integer, En>, Ctx<Fk, Pair<Integer, En>>> fks = new Ctx<>();
		Ctx<Pair<Integer,En>, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Pair<Integer, Att>>>> atts = new Ctx<>();
		for (Ty ty : schema.typeSide.tys) {
			tys.put(ty, new LinkedList<>());
		}
		for (String en : ens.keySet()) {
			List<Pair<Integer,En>> l = new LinkedList<>();
			int size = ens.get(en);
			for (int i = 0; i < size; i++) {
				l.add(new Pair<>(i,new En(en)));
				Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Pair<Integer, Att>>> ctx = new Ctx<>();
				for (Att att : schema.attsFrom(new En(en))) {
					ctx.put(att, Term.Sk(new Pair<>(i, att)));
					tys.get(schema.atts.get(att).second).add(new Pair<>(i, att));
				}
				atts.put(new Pair<>(i,new En(en)), ctx);
				
				Ctx<Fk, Pair<Integer, En>> ctx0 = new Ctx<>();
				for (Fk fk : schema.fksFrom(new En(en))) {
					int size0 = ens.get(schema.fks.get(fk).second.str);
					Integer k = rand.nextInt(size0);
					ctx0.put(fk, new Pair<>(k, schema.fks.get(fk).second));
				}
				fks.put(new Pair<>(i,new En(en)), ctx0);
			}
			ens0.put(new En(en), l);	
		}
				
		ImportAlgebra<Ty, En, Sym, Fk, Att, Pair<Integer, En>, Pair<Integer, Att>> 
		alg = new ImportAlgebra<Ty, En, Sym, Fk, Att, Pair<Integer, En>, Pair<Integer, Att>> 
		(schema, ens0, tys, fks, atts, x->x.toString(), x->x.toString() , true); 
		
		
		DP<Ty, En, Sym, Fk, Att, Pair<Integer,En>, Pair<Integer, Att>> dp = new DP<Ty, En, Sym, Fk, Att, Pair<Integer,En>, Pair<Integer, Att>>() {

			@Override
			public String toStringProver() {
				return "Random";
			}

			@Override
			public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, Pair<Integer,En>, Pair<Integer, Att>> lhs,
					Term<Ty, En, Sym, Fk, Att, Pair<Integer,En>, Pair<Integer, Att>> rhs) {
				if (!ctx.isEmpty()) {
					Util.anomaly();
				}
				return lhs.equals(rhs);
			}
			
		};
		
		return new SaturatedInstance
				<Ty, En, Sym, Fk, Att, Pair<Integer,En>, Pair<Integer, Att>, Pair<Integer,En>, Pair<Integer, Att>> 
		(alg, dp, false, true, false, new Ctx<>());
	}

	
}
