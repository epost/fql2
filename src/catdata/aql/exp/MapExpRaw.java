package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Kind;
import catdata.aql.Mapping;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.InstExpRaw.Sk;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public final class MapExpRaw extends MapExp<Ty, En, Sym, Fk, Att, En, Fk, Att> implements Raw {

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.MAPPING)).collect(Collectors.toList()));
		return ret;
	}

	public final SchExp<Ty, En, Sym, Fk, Att> src;
	public final SchExp<Ty, En, Sym, Fk, Att> dst;

	public final Set<String> imports;

	public final Set<Pair<String, String>> ens;
	public final Set<Pair<Pair<String, String>, List<String>>> fks;
	public final Set<Pair<Pair<String, String>, Triple<String, String, RawTerm>>> atts;

	public final Map<String, String> options;

	@Override
	public Map<String, String> options() {
		return options;
	}

	//

	
	private Ctx<En, Integer> enPos = new Ctx<>();
	private Ctx<Fk, Integer> fkPos = new Ctx<>();
	private Ctx<Att, Integer> attPos = new Ctx<>();

	public MapExpRaw(SchExp<?, ?, ?, ?, ?> src, SchExp<?, ?, ?, ?, ?> dst, List<LocStr> imports,
			List<Pair<LocStr, Triple<String, List<Pair<LocStr, List<String>>>, List<Pair<LocStr, Triple<String, String, RawTerm>>>>>> list,
			List<Pair<String, String>> options) {
		this.src = (SchExp<Ty, En, Sym, Fk, Att>) src;
		this.dst = (SchExp<Ty, En, Sym, Fk, Att>) dst;
		this.imports = LocStr.set1(imports);

		Map<LocStr, Triple<String, List<Pair<LocStr, List<String>>>, List<Pair<LocStr, Triple<String, String, RawTerm>>>>> list2 = Util
				.toMapSafely(list);

		this.ens = new HashSet<>(); 
		this.fks = new HashSet<>(); 
		this.atts = new HashSet<>(); 
	
		for (LocStr en : list2.keySet()) {
			Triple<String, List<Pair<LocStr, List<String>>>, List<Pair<LocStr, Triple<String, String, RawTerm>>>> v = list2
					.get(en);
			this.ens.add(new Pair<>(en.str, v.first));
			enPos.put(new En(en.str), en.loc);

			for (Pair<LocStr, List<String>> fk : v.second) {
				this.fks.add(new Pair<>(new Pair<>(en.str, fk.first.str), fk.second));
				fkPos.put(new Fk(new En(en.str), fk.first.str), fk.first.loc);
			}
			for (Pair<LocStr, Triple<String, String, RawTerm>> att : v.third) {
				this.atts.add(new Pair<>(new Pair<>(en.str, att.first.str), att.second));
				attPos.put(new Att(new En(en.str), att.first.str), att.first.loc);
			}
		}

		this.options = Util.toMapSafely(options);
		Util.toMapSafely(this.ens);
		Util.toMapSafely(this.fks);
		Util.toMapSafely(this.atts); // do here rather than wait

		List<InteriorLabel<Object>> t = InteriorLabel.imports("imports", imports);
		raw.put("imports", t);

		raw.put("entities", new LinkedList<>());
		raw.put("foreign keys", new LinkedList<>());
		raw.put("attributes", new LinkedList<>());

		for (Pair<String, String> p : ens) {
			List<InteriorLabel<Object>> inner = new LinkedList<>();
			raw.put(p.first, inner);
			
			inner.add(new InteriorLabel<>("entities", new Pair<>(p.first, p.second), enPos.get(new En(p.first)),
					x -> x.first + " -> " + x.second).conv());
		
			for (Pair<Pair<String, String>, List<String>> q : this.fks.stream().filter(x -> x.first.first.equals(p.first)).collect(Collectors.toList())) {
				inner.add(new InteriorLabel<>("foreign keys",
						q /* new Pair<>(p.first.second.str, p.second) */, fkPos.get(new Fk(new En(p.first),q.first.second)),
						x -> x.first + " -> " + Util.sep(x.second, ".")).conv());
			}
	
			for (Pair<Pair<String, String>, Triple<String, String, RawTerm>> q : atts.stream().filter(x -> x.first.first.equals(p.first)).collect(Collectors.toList())) {
				inner.add(new InteriorLabel<>("attributes", new Pair<>(q.first.second, q.second), attPos.get(new Att(new En(p.first),q.first.second)),
						x -> x.first + " -> \\" + x.second.first + ". " + x.second.third).conv());
			}
		}
	}

	Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

	@Override
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}

	private String toString;

	@Override
	public synchronized String toString() {
		if (toString != null) {
			return toString;
		}
		toString = "";

		if (!imports.isEmpty()) {
			toString += "\timports";
			toString += "\n\t\t" + Util.sep(imports, " ") + "\n";
		}

		
		for (Pair<String, String> en : Util.alphabetical(ens)) {
			List<String> temp = new LinkedList<>();

			toString += "\tentity";

			//for (Pair<LocStr, String> x : Util.alphabetical(ens)) {
				temp.add(en.first + " -> " + en.second);
			//}

			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			
			List<Pair<Pair<String, String>, List<String>>> x = fks.stream().filter(z -> z.first.first.equals(en.first)).collect(Collectors.toList());
			if (!x.isEmpty()) {
				toString += "\tforeign keys";
				temp = new LinkedList<>();
				for (Pair<Pair<String, String>, List<String>> sym : Util.alphabetical(x)) {
					temp.add(sym.first.second + " -> " + Util.sep(sym.second, "."));
				}
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}

			List<Pair<Pair<String, String>, Triple<String, String, RawTerm>>> y = atts.stream().filter(z -> z.first.first.equals(en.first)).collect(Collectors.toList());
			
			if (!y.isEmpty()) {
				toString += "\tattributes";
				temp = new LinkedList<>();
				for (Pair<Pair<String, String>, Triple<String, String, RawTerm>> sym : Util.alphabetical(y)) {
					temp.add(sym.first.second + " -> lambda " + sym.second.first + ". " + sym.second.third);
				}
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}
		}

		

		if (!options.isEmpty()) {
			toString += "\toptions";
			List<String> temp = new LinkedList<>();
			for (Entry<String, String> sym : options.entrySet()) {
				temp.add(sym.getKey() + " = " + sym.getValue());
			}

			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		return "literal : " + src + " -> " + dst + " {\n" + toString + "}";
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		MapExpRaw other = (MapExpRaw) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (imports == null) {
			if (other.imports != null)
				return false;
		} else if (!imports.equals(other.imports))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	@Override
	public Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> src0 = src.eval(env);
		Schema<Ty, En, Sym, Fk, Att> dst0 = dst.eval(env);
		// Collage<String, String, String, String, String, Void, Void> scol =
		// new Collage<>(src0);
		Collage<Ty, En, Sym, Fk, Att, Void, Void> dcol = new Collage<>(dst0.collage());

		Map<En, En> ens0 = new HashMap<>();
		// Map<String, Pair<String, List<String>>> fks0 = new HashMap<>();
		Map<Att, Triple<Var, En, Term<Ty, En, Sym, Fk, Att, Void, Void>>> atts0 = new HashMap<>();
		Map<Fk, Pair<En, List<Fk>>> fksX = new HashMap<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att> v = env.defs.maps.get(k);
			Util.putAllSafely(ens0, v.ens.map);
			Util.putAllSafely(fksX, v.fks.map);
			Util.putAllSafely(atts0, v.atts.map);
		}

		Util.putAllSafely(ens0, Util.toMapSafely(
				ens.stream().map(x -> new Pair<>(new En(x.first), new En(x.second))).collect(Collectors.toList())));
		for (En k : ens0.keySet()) {
			if (!dst0.ens.contains(ens0.get(k))) {
				throw new LocException(find("entities", new Pair<>(k, ens0.get(k))),
						"The mapping for " + k + ", namely " + ens0.get(k) + ", does not appear in the target schema");
			} else if (!src0.ens.contains(k)) {
				throw new LocException(find("entities", new Pair<>(k, ens0.get(k))),
						k + " does not appear in the source schema");
			}
		}

		for (Pair<Pair<String, String>, List<String>> p : this.fks) {
			Fk theFk = new Fk(new En(p.first.first), p.first.second);
			if (!src0.fks.containsKey(theFk)) {
				throw new RuntimeException("Not a foreign key in source: " + theFk.en + "." + theFk.str); 
			}
			try {
				En start_en_fixed = ens0.get(new En(p.first.first));

				En start_en = ens0.get(new En(p.first.first));
				List<Fk> r = new LinkedList<>();
				for (String o : p.second) {
					if (ens0.containsValue(new En(o))) {
						if (fksX.containsKey(new Fk(start_en, o))) {
							throw new RuntimeException(
									o + " is both a target foreign key and a target entity, so the path is ambiguous");
						}
						// if (start_en == null) {
						// start_en = new En(p.second.get(0));
						// }
					} else {
						/*
						 * if (start_en == null) { Pair<En, En> j =
						 * dst0.fks.get(new Fk(o)); if (j == null) { throw new
						 * RuntimeException(p.second.get(0) +
						 * " is not a foreign key in the target"); } start_en =
						 * j.first; }
						 */
						// }
						r.add(new Fk(start_en, o));
						start_en = dst0.fks.get(new Fk(start_en, o)).second;

					}
				}
				// if (start_en == null) {
				// throw new RuntimeException("Anomaly: please report");
				// }
				fksX.put(new Fk(new En(p.first.first), p.first.second), new Pair<>(start_en_fixed, r));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(fkPos.get(new Fk(new En(p.first.first), p.first.second)), "In foreign key mapping " + p.first + " -> "
						+ Util.sep(p.second, ".") + ", " + ex.getMessage());
			}
		}
		// Util.putAllSafely(fks0, Util.toMapSafely(fksX));

		for (Pair<Pair<String, String>, Triple<String, String, RawTerm>> att : atts) {
			try {
				String var = att.second.first;
				String var_en = att.second.second;
				RawTerm term = att.second.third;

				Pair<En, Ty> p = src0.atts.map.get(new Att(new En(att.first.first), att.first.second));
				if (p == null) {
					throw new RuntimeException(att.first + " is not a source attribute.");
				}
				En src_att_dom_en = p.first;
				En dst_att_dom_en = ens0.get(src_att_dom_en);
				if (dst_att_dom_en == null) {
					throw new RuntimeException(
							"no entity mapping for " + src_att_dom_en + " , required for domain for " + att.first);
				}

				if (var_en != null && !new En(var_en).equals(dst_att_dom_en)) {
					throw new RuntimeException("the given source entity for the variable, " + var_en + ", is not "
							+ dst_att_dom_en + " as expected.");
				}

				Ty src_att_cod_ty = p.second;
				if (!dst0.typeSide.tys.contains(src_att_cod_ty)) {
					throw new RuntimeException("type " + p.second + " does not exist in target typeside.");
				}
				Chc<Ty, En> proposed_ty2 = Chc.inLeft(src_att_cod_ty);

				Chc<Ty, En> var_en2 = Chc.inRight(dst_att_dom_en);

				Map<String, Chc<Ty, En>> ctx = Util.singMap(var, var_en2);

				Term<Ty, En, Sym, Fk, Att, Gen, Sk> term0 = RawTerm.infer1x(ctx, term, null, proposed_ty2,
						dcol.convert(), "", src0.typeSide.js).second;

				Util.putSafely(atts0, new Att(new En(att.first.first), att.first.second),
						new Triple<>(new Var(var), dst_att_dom_en, term0.convert()));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(attPos.get(new Att(new En(att.first.first), att.first.second)), "in mapping for " + att.first + ", " + ex.getMessage());
			}
		}

		AqlOptions ops = new AqlOptions(options, null, env.defaults);

		Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att> ret = new Mapping<>(ens0, atts0, fksX, src0, dst0,
				(Boolean) ops.getOrDefault(AqlOption.dont_validate_unsafe));
		return ret;
	}

	@Override
	public Pair<SchExp<Ty, En, Sym, Fk, Att>, SchExp<Ty, En, Sym, Fk, Att>> type(AqlTyping G) {
		return new Pair<>(src, dst);
	}
}