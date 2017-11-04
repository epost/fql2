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
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public final class MapExpRaw extends MapExp<Ty, String, Sym, String, String, String, String, String>
		implements Raw {

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.MAPPING)).collect(Collectors.toList()));
		return ret;
	}

	public final SchExp<Ty, String, Sym, String, String> src;
	public final SchExp<Ty, String, Sym, String, String> dst;

	public final Set<String> imports;

	public final Set<Pair<String, String>> ens;
	public final Set<Pair<String, List<String>>> fks;
	public final Set<Pair<String, Triple<String, String, RawTerm>>> atts;

	public final Map<String, String> options;

	@Override
	public Map<String, String> options() {
		return options;
	}

	// typesafe by covariance of read only collections
	@SuppressWarnings("unchecked")
	public MapExpRaw(SchExp<?, ?, ?, ?, ?> src, SchExp<?, ?, ?, ?, ?> dst, List<LocStr> imports,
			List<Pair<LocStr, String>> ens, List<Pair<LocStr, List<String>>> fks,
			List<Pair<LocStr, Triple<String, String, RawTerm>>> atts, List<Pair<String, String>> options) {
		this.src = (SchExp<Ty, String, Sym, String, String>) src;
		this.dst = (SchExp<Ty, String, Sym, String, String>) dst;
		this.imports = LocStr.set1(imports);
		this.ens = LocStr.set2(ens);
		this.fks = LocStr.set2(fks);
		this.atts = LocStr.set2(atts);
		this.options = Util.toMapSafely(options);
		Util.toMapSafely(this.ens);
		Util.toMapSafely(this.fks);
		Util.toMapSafely(this.atts); // do here rather than wait

		List<InteriorLabel<Object>> t = InteriorLabel.imports("imports", imports);
		raw.put("imports", t);

		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, String> p : ens) {
			f.add(new InteriorLabel<>("entities", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("entities", f);

		f = new LinkedList<>();
		for (Pair<LocStr, List<String>> p : fks) {
			f.add(new InteriorLabel<>("foreign keys", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> " + Util.sep(x.second, ".")).conv());
		}
		raw.put("foreign keys", f);

		f = new LinkedList<>();
		for (Pair<LocStr, Triple<String, String, RawTerm>> p : atts) {
			f.add(new InteriorLabel<>("attributes", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> \\" + x.second.first + ". " + x.second.third).conv());
		}
		raw.put("attributes", f);
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

		List<String> temp = new LinkedList<>();

		if (!ens.isEmpty()) {
			toString += "\tentities";

			for (Pair<String, String> x : Util.alphabetical(ens)) {
				temp.add(x.first + " -> " + x.second);
			}

			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!fks.isEmpty()) {
			toString += "\tforeign_keys";
			temp = new LinkedList<>();
			for (Pair<String, List<String>> sym : Util.alphabetical(fks)) {
				temp.add(sym.first + " -> " + Util.sep(sym.second, "."));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!fks.isEmpty()) {
			toString += "\tattributes";
			temp = new LinkedList<>();
			for (Pair<String, Triple<String, String, RawTerm>> sym : Util.alphabetical(atts)) {
				temp.add(sym.first + " -> lambda " + sym.second.first + ". " + sym.second.third);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}

		if (!options.isEmpty()) {
			toString += "\toptions";
			temp = new LinkedList<>();
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
	public Mapping<Ty, String, Sym, String, String, String, String, String> eval(AqlEnv env) {
		Schema<Ty, String, Sym, String, String> src0 = src.eval(env);
		Schema<Ty, String, Sym, String, String> dst0 = dst.eval(env);
		// Collage<String, String, String, String, String, Void, Void> scol =
		// new Collage<>(src0);
		Collage<Ty, String, Sym, String, String, Void, Void> dcol = new Collage<>(dst0.collage());

		Map<String, String> ens0 = new HashMap<>();
		Map<String, Pair<String, List<String>>> fks0 = new HashMap<>();
		Map<String, Triple<Var, String, Term<Ty, String, Sym, String, String, Void, Void>>> atts0 = new HashMap<>();
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Mapping<Ty, String, Sym, String, String, String, String, String> v = env.defs.maps.get(k);
			Util.putAllSafely(ens0, v.ens.map);
			Util.putAllSafely(fks0, v.fks.map);
			Util.putAllSafely(atts0, v.atts.map);
		}

		Util.putAllSafely(ens0, Util.toMapSafely(ens));
		for (String k : ens0.keySet()) {
			if (!dst0.ens.contains(ens0.get(k))) {
				throw new LocException(find("entities", new Pair<>(k, ens0.get(k))), "The mapping for " + k + ", namely " + ens0.get(k) + ", does not appear in the target schema");
			} else if (!src0.ens.contains(k)) {
				throw new LocException(find("entities", new Pair<>(k, ens0.get(k))), k + " does not appear in the source schema");				
			}
		}
		
		List<Pair<String, Pair<String, List<String>>>> fksX = new LinkedList<>();
		for (Pair<String, List<String>> p : fks) {
			try {
				String start_en = null;
				List<String> r = new LinkedList<>();
				for (String o : p.second) {
					if (ens0.containsValue(o)) {
						if (fks0.containsKey(o)) {
							throw new RuntimeException(
									o + " is both a target foreign key and a target entity, so the path is ambiguous");
						}
						if (start_en == null) {
							start_en = p.second.get(0);
						}
					} else {
						if (start_en == null) {
							Pair<String, String> j = dst0.fks.get(o);
							if (j == null) {
								throw new RuntimeException(p.second.get(0) + " is not a foreign key in the target");
							}
							start_en = j.first;
						}
						r.add(o);
					}
				}
				if (start_en == null) {
					throw new RuntimeException("Anomaly: please report");
				}
				fksX.add(new Pair<>(p.first, new Pair<>(start_en, r)));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("foreign keys", p), "In foreign key mapping " + p.first + " -> "
						+ Util.sep(p.second, ".") + ", " + ex.getMessage());
			}
		}
		Util.putAllSafely(fks0, Util.toMapSafely(fksX));

		for (Pair<String, Triple<String, String, RawTerm>> att : atts) {
			try {
				String var = att.second.first;
				String var_en = att.second.second;
				RawTerm term = att.second.third;

				Pair<String, Ty> p = src0.atts.map.get(att.first);
				if (p == null) {
					throw new RuntimeException(att.first + " is not a source attribute.");
				}
				String src_att_dom_en = p.first;
				String dst_att_dom_en = ens0.get(src_att_dom_en);
				if (dst_att_dom_en == null) {
					throw new RuntimeException(
							"no entity mapping for " + src_att_dom_en + " , required for domain for " + att.first);
				}

				if (var_en != null && !var_en.equals(dst_att_dom_en)) {
					throw new RuntimeException("the given source entity for the variable, " + var_en + ", is not "
							+ dst_att_dom_en + " as expected.");
				}

				Ty src_att_cod_ty = p.second;
				if (!dst0.typeSide.tys.contains(src_att_cod_ty)) {
					throw new RuntimeException("type " + p.second + " does not exist in target typeside.");
				}
				Chc<Ty, String> proposed_ty2 = Chc.inLeft(src_att_cod_ty);

				Chc<Ty, String> var_en2 = Chc.inRight(dst_att_dom_en);

				Map<String, Chc<Ty, String>> ctx = Util.singMap(var, var_en2);

				Term<Ty, String, Sym, String, String, Void, Void> term0 = RawTerm.infer1x(ctx, term, null, proposed_ty2,
						dcol, "", src0.typeSide.js).second;

				Util.putSafely(atts0, att.first, new Triple<>(new Var(var), dst_att_dom_en, term0));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("attributes", att), "in mapping for " + att.first + ", " + ex.getMessage());
			}
		}

		AqlOptions ops = new AqlOptions(options, null, env.defaults);

		Mapping<Ty, String, Sym, String, String, String, String, String> ret = new Mapping<>(ens0, atts0, fks0,
				src0, dst0, (Boolean) ops.getOrDefault(AqlOption.dont_validate_unsafe));
		return ret;
	}

	@Override
	public Pair<SchExp<Ty, String, Sym, String, String>, SchExp<Ty, String, Sym, String, String>> type(
			AqlTyping G) {
		return new Pair<>(src, dst);
	}
}