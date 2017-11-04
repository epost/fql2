package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
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
import catdata.aql.Instance;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public  final class InstExpRandom
extends InstExp<Ty,String,Sym,String,String,String,String,ID,Chc<String,Pair<ID,String>>> implements Raw {

	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	@Override 
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	public final Map<String, Integer> ens;
			
	public final Map<String, String> options;
	
	public final SchExp<Ty, String, Sym, String, String> sch;
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	@SuppressWarnings("unchecked")
	public InstExpRandom(SchExp<?,?,?,?,?> sch, List<Pair<LocStr, String>> ens, List<Pair<String, String>> options) {
		this.ens = Util.toMapSafely(LocStr.set2y(ens, x -> Integer.parseInt(x)));
		this.options = Util.toMapSafely(options);
		this.sch = (SchExp<Ty, String, Sym, String, String>) sch;
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
			x = "generators\n" + Util.sep(ens, " -> ", "\n");
		}
		
		return s + x + "\n}";
	}

	@Override
	public SchExp<Ty, String, Sym, String, String> type(AqlTyping G) {
		return sch;
	}

	//not exactly the smartest way
	@Override
	public Instance<Ty, String, Sym, String, String, String, String, ID, Chc<String, Pair<ID, String>>> eval(AqlEnv env) {
		int seed = (Integer) new AqlOptions(options, null, env.defaults).getOrDefault(AqlOption.random_seed);
		Random rand = new Random(seed);
		List<Pair<LocStr, String>> gens = new LinkedList<>();
		List<Pair<Integer, Pair<RawTerm, RawTerm>>> eqs = new LinkedList<>();
		Schema<Ty, String, Sym, String, String> schema = sch.eval(env);
		for (String en : ens.keySet()) {
			int size = ens.get(en);
			for (int i = 0; i < size; i++) {
				String src = en + i;
				gens.add(new Pair<>(new LocStr(0, src), en)); 
				for (String fk : schema.fksFrom(en)) {
					Object dst_en = schema.fks.get(fk).second;
					int dst_size = ens.containsKey(dst_en) ? ens.get(dst_en) : 0;
					if (dst_size == 0) {
						continue;
					}
					String dst = dst_en.toString() + rand.nextInt(dst_size);
					eqs.add(new Pair<>(0, new Pair<>(new RawTerm(fk.toString(), Util.singList(new RawTerm(src))), new RawTerm(dst))));
				}
				for (String att : schema.attsFrom(en)) {
					Object dst_ty = schema.atts.get(att).second;
					int dst_size = ens.containsKey(dst_ty) ? ens.get(dst_ty) : 0;

					if (dst_size == 0) {
						continue;
					}
					String dst = dst_ty.toString() + rand.nextInt(dst_size);
					eqs.add(new Pair<>(0, new Pair<>(new RawTerm(att.toString(), Util.singList(new RawTerm(src))), new RawTerm(dst))));
				}
			}
		}
				
		return new InstExpRaw<String, String, String>(sch, Collections.emptyList(), gens, eqs, Util.toList(options)).eval(env);  //inherits options
	}

	
}