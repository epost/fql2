package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
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
import catdata.aql.Eq;
import catdata.aql.Kind;
import catdata.aql.Query;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.InstExpRaw.Sk;
import catdata.aql.exp.QueryExpRaw.Trans;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

//TODO aql add type params to all raws?
public class QueryExpRaw extends QueryExp<Ty, En, Sym, Fk, Att, En, Fk, Att> implements Raw {

	private final SchExp<Ty, En, Sym, Fk, Att> src;
	private final SchExp<Ty, En, Sym, Fk, Att> dst;

	private final Set<String> imports;

	private final Map<String, String> options;

	private final Set<Block> blocks;

	public final Ctx<String, String> params;
	public final Ctx<String, RawTerm>  consts;

	private final Ctx<En, Integer> b1 = new Ctx<>();
	private final Ctx<Fk, Integer> b2 = new Ctx<>();
	private final Ctx<Att, Integer> b3 = new Ctx<>();

	@Override
	public Map<String, String> options() {
		return options;
	}

	public static class Trans extends Exp<Void> implements Raw {

		private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

		@Override
		public Ctx<String, List<InteriorLabel<Object>>> raw() {
			return raw;
		}

		@Override
		protected Map<String, String> options() {
			return null;
		}

		@Override
		public Kind kind() {
			return null;
		}

		@Override
		public Void eval(AqlEnv env) {
			return null;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return null;
		}

		public final Set<Pair<Var, RawTerm>> gens;

		public final Map<String, String> options;

		public Trans(List<Pair<LocStr, RawTerm>> gens, List<Pair<String, String>> options) {
			this.gens = new HashSet<>();
			for (Pair<LocStr, RawTerm> gen : gens) {
				this.gens.add(new Pair<>(new Var(gen.first.str), gen.second));
			}
			this.options = Util.toMapSafely(options);

			List<InteriorLabel<Object>> f = new LinkedList<>();
			for (Pair<LocStr, RawTerm> p : gens) {
				f.add(new InteriorLabel<>("generators", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " -> " + x.second).conv());
			}
			raw.put("generators", f);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			Trans other = (Trans) obj;
			if (gens == null) {
				if (other.gens != null)
					return false;
			} else if (!gens.equals(other.gens))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		private String toString;

		@Override
		public synchronized String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";

			List<String> temp = new LinkedList<>();

			if (!gens.isEmpty()) {
				for (Pair<Var, RawTerm> En : gens) {
					temp.add(En.first + " -> " + En.second);
				}

				toString += Util.sep(temp, "\n\t\t\t\t");
			}

			if (!options.isEmpty()) {
				toString += "\n\toptions";
				temp = new LinkedList<>();
				for (Entry<String, String> sym : options.entrySet()) {
					temp.add(sym.getKey() + " = " + sym.getValue());
				}

				toString += "\n\t\t\t" + Util.sep(temp, "\n\t\t\t");
			}

			return "\t{" + toString + "}";
		}

	}

	public static class PreBlock {
		public final List<Pair<LocStr, String>> gens;
		public final List<Pair<Integer, Pair<RawTerm, RawTerm>>> eqs;
		public final List<Pair<String, String>> options;
		public final List<Pair<LocStr, RawTerm>> atts;
		public final List<Pair<LocStr, Trans>> fks;

		public PreBlock(List<Pair<LocStr, String>> gens, List<Pair<Integer, Pair<RawTerm, RawTerm>>> eqs,
				List<Pair<LocStr, RawTerm>> atts, List<Pair<LocStr, Trans>> fks, List<Pair<String, String>> options) {
			this.gens = gens;
			this.eqs = eqs;
			this.atts = atts;
			this.fks = fks;
			this.options = options;
		}

		

	}

	public static class Block extends Exp<Void> implements Raw {

		public Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

		@Override
		public Kind kind() {
			return null;
		}

		@Override
		public Void eval(AqlEnv env) {
			return null;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return null;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((en == null) ? 0 : en.hashCode());
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
			Block other = (Block) obj;
			if (eqs == null) {
				if (other.eqs != null)
					return false;
			} else if (!eqs.equals(other.eqs))
				return false;
			if (gens == null) {
				if (other.gens != null)
					return false;
			} else if (!gens.equals(other.gens))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (en == null) {
				if (other.en != null)
					return false;
			} else if (!en.equals(other.en))
				return false;
			return true;
		}

		public final Set<Pair<Att, RawTerm>> atts;
		public final Set<Pair<Fk, Trans>> fks;
		public final En en;
		public final Set<Pair<Var, En>> gens;
		public final Set<Pair<RawTerm, RawTerm>> eqs;
		public final Map<String, String> options;
		public final Integer enLoc;

		public Block(PreBlock b, LocStr en) {
			this.en = new En(en.str);
			this.enLoc = en.loc;
			this.gens = new HashSet<>();
			this.atts = LocStr.set2(b.atts).stream().map(x -> new Pair<>(new Att(new En(en.str), x.first), x.second))
					.collect(Collectors.toSet());
			this.fks = LocStr.set2(b.fks).stream().map(x -> new Pair<>(new Fk(new En(en.str), x.first), x.second))
					.collect(Collectors.toSet());

			for (Pair<LocStr, String> gen : b.gens) {
				this.gens.add(new Pair<>(new Var(gen.first.str), new En(gen.second)));
			}
			this.eqs = LocStr.proj2(b.eqs);
			this.options = Util.toMapSafely(b.options);

			List<InteriorLabel<Object>> e = new LinkedList<>();
			for (Pair<LocStr, String> p : b.gens) {
				e.add(new InteriorLabel<>("from", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " : " + x.second).conv());
			}
			raw.put("from", e);

			List<InteriorLabel<Object>> xx = new LinkedList<>();
			for (Pair<Integer, Pair<RawTerm, RawTerm>> p : b.eqs) {
				xx.add(new InteriorLabel<>("where", p.second, p.first, x -> x.first + " = " + x.second).conv());
			}
			raw.put("where", xx);

			xx = new LinkedList<>();
			for (Pair<LocStr, RawTerm> p : b.atts) {
				xx.add(new InteriorLabel<>("attributes", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " -> " + x.second).conv());
			}
			raw.put("attributes", xx);

			xx = new LinkedList<>();
			for (Pair<LocStr, Trans> p : b.fks) {
				xx.add(new InteriorLabel<>("foreign_keys", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " -> " + x.second).conv());
			}
			raw.put("foreign_keys", xx);
		}

		private String toString;

		public synchronized String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";

			List<String> temp = new LinkedList<>();

			if (!gens.isEmpty()) {
				toString += "from\t";

				Map<En, Set<Var>> x = Util.revS(Util.toMapSafely(gens));
				temp = new LinkedList<>();
				for (En En : Util.alphabetical(x.keySet())) {
					temp.add(Util.sep(x.get(En), " ") + " : " + En);
				}

				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}

			if (!eqs.isEmpty()) {
				toString += "\n\t\t\t\twhere\t";
				temp = new LinkedList<>();
				for (Pair<RawTerm, RawTerm> sym : Util.alphabetical(eqs)) {
					temp.add(sym.first + " = " + sym.second);
				}
				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}

			if (!atts.isEmpty()) {
				toString += "\n\t\t\t\tattributes\t";
				temp = new LinkedList<>();
				for (Pair<Att, RawTerm> sym : Util.alphabetical(atts)) {
					temp.add(sym.first + " -> " + sym.second);
				}
				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}

			if (!fks.isEmpty()) {
				toString += "\n\t\t\t\tforeign_keys\t";
				temp = new LinkedList<>();
				for (Pair<catdata.aql.exp.SchExpRaw.Fk, Trans> sym : Util.alphabetical(fks)) {
					temp.add(sym.first.str + " -> {" + sym.second + "}");
				}
				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}

			if (!options.isEmpty()) {
				toString += "\n\t\t\t\toptions";
				temp = new LinkedList<>();
				for (Entry<String, String> sym : options.entrySet()) {
					temp.add(sym.getKey() + " = " + sym.getValue());
				}

				toString += "\n\t\t\t\t" + Util.sep(temp, "\n\t\t\t\t\t");
			}

			return "\t" + toString;
		}

		@Override
		public Ctx<String, List<InteriorLabel<Object>>> raw() {
			return raw;
		}

		@Override
		protected Map<String, String> options() {
			return options;
		}

	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((consts == null) ? 0 : consts.hashCode());
		result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
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
		QueryExpRaw other = (QueryExpRaw) obj;
		if (blocks == null) {
			if (other.blocks != null)
				return false;
		} else if (!blocks.equals(other.blocks))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
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
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (consts == null) {
			if (other.consts != null)
				return false;
		} else if (!consts.equals(other.consts))
			return false;
		return true;
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

		if (!blocks.isEmpty()) {
			toString += "\tentity";

			for (Block x : blocks) {
				temp.add(x.toString());
			}

			toString += "\n\t\t" + Util.sep(temp, "\n\n\t\t") + "\n";
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

	public QueryExpRaw(List<Pair<LocStr, String>> params, List<Pair<LocStr, RawTerm>> consts, SchExp<?, ?, ?, ?, ?> c, SchExp<?, ?, ?, ?, ?> d, List<LocStr> imports,
			List<Pair<LocStr, PreBlock>> list, List<Pair<String, String>> options) {
		this.src = (SchExp<Ty, En, Sym, Fk, Att>) c;
		this.dst = (SchExp<Ty, En, Sym, Fk, Att>) d;
		this.imports = LocStr.set1(imports);
		this.options = Util.toMapSafely(options);
		this.consts = new Ctx<>(Util.toMapSafely(LocStr.set2(consts)));
		this.params = new Ctx<>(Util.toMapSafely(LocStr.set2(params)));
		this.blocks = Util.toSetSafely(list).stream().map(x -> new Block(x.second, x.first))
				.collect(Collectors.toSet());
		
		for (Pair<LocStr, PreBlock> x : list) {
			b1.put(new En(x.first.str), x.first.loc);

			for (Pair<LocStr, Trans> y : x.second.fks) {
				b2.put(new Fk(new En(x.first.str), y.first.str), y.first.loc);
			}

			for (Pair<LocStr, RawTerm> y : x.second.atts) {
				b3.put(new Att(new En(x.first.str), y.first.str), y.first.loc);
			}
		}

		raw.put("imports", InteriorLabel.imports("imports", imports));

		for (Pair<LocStr, PreBlock> p : list) {
			List<InteriorLabel<Object>> f = new LinkedList<>();

			f.add(new InteriorLabel<>("entities", p.second, p.first.loc, x -> p.first.str).conv());

			raw.put(p.first.str, f);
		}

	}

	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

	@Override
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}

	/*
	 * public QueryExpRaw(SchExp<Ty, En, Sym, Fk, Att> src, SchExp<Ty, En, Sym,
	 * Fk, Att> dst, List<String> imports, List<Pair<En, Pair<Block,
	 * List<Pair<Att, RawTerm>>>>> blocks, List<Pair<Fk, Trans>> fks,
	 * List<Pair<String, String>> options, @SuppressWarnings("unused") Object o)
	 * { this.src = src; this.dst = dst; this.imports = new HashSet<>(imports);
	 * this.options = Util.toMapSafely(options); this.blocks =
	 * blocks.stream().map(x -> new Pair<>(x.first,
	 * x.second.first)).collect(Collectors.toSet()); this.fks = new
	 * HashSet<>(fks); atts = Collections.emptySet(); for (Pair<En, Pair<Block,
	 * List<Pair<Att, RawTerm>>>> block : blocks) {
	 * atts.addAll(block.second.second); } }
	 */
	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Util.union(src.deps(), Util.union(dst.deps(),
				imports.stream().map(x -> new Pair<>(x, Kind.QUERY)).collect(Collectors.toSet())));
	}

	@Override
	public Pair<SchExp<Ty, En, Sym, Fk, Att>, SchExp<Ty, En, Sym, Fk, Att>> type(AqlTyping G) {
		return new Pair<>(src, dst);
	}

	@Override
	public Query<Ty, En, Sym, Fk, Att, En, Fk, Att> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> src0 = src.eval(env);
		Schema<Ty, En, Sym, Fk, Att> dst0 = dst.eval(env);

		Ctx<En, Triple<Ctx<Var, En>, Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>>, AqlOptions>> ens0 = new Ctx<>();
		Ctx<Att, Term<Ty, En, Sym, Fk, Att, Var, Var>> atts0 = new Ctx<>();
		Ctx<Fk, Pair<Ctx<Var, Term<Void, En, Void, Fk, Void, Var, Void>>, Boolean>> fks0 = new Ctx<>();

		Ctx<Var, Ty> xxx = new Ctx<>();
		Ctx<Var, Term<Ty, Void, Sym, Void, Void, Void, Void>> yyy = new Ctx<>();
		
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Query<Ty, En, Sym, Fk, Att, En, Fk, Att> v = env.defs.qs.get(k);
			
			for (Var var : v.params.keySet()) {
				xxx.map.put(var, v.params.get(var)); //allow benign collisions
			}
			for (Var var : v.consts.keySet()) {
				yyy.map.put(var, v.consts.get(var));
			}
			for (En En : v.ens.keySet()) {
				ens0.put(En, new Triple<>(v.ens.get(En).gens, v.ens.get(En).eqs, v.ens.get(En).options));
			}
			for (Att Att : v.atts.keySet()) {
				atts0.put(Att, v.atts.get(Att));
			}
			for (Fk Fk : v.fks.keySet()) {
				fks0.put(Fk, new Pair<>(v.fks.get(Fk).gens(), v.doNotValidate.get(Fk)));
			}
		}

		Ctx<En, Collage<Ty, En, Sym, Fk, Att, Var, Var>> cols = new Ctx<>();
		for (Block p : blocks) {
			try {
				if (!dst0.ens.contains(p.en)) {
					throw new RuntimeException(
							"the proposed target entity " + p.en + " does not actually appear in the target schema");
				}
				processBlock(options, env, src0, ens0, cols, p, params);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(b1.get(p.en), "In block for target entity " + p.en + ", " + ex.getMessage());
			}

			for (Pair<catdata.aql.exp.SchExpRaw.Att, RawTerm> pp : p.atts) {
				try {

					processAtt(src0, dst0, ens0, atts0, cols, pp, params);

				} catch (Exception ex) {
					ex.printStackTrace();
					throw new LocException(b3.get(pp.first),
							"In return clause for " + pp.first + ", " + ex.getMessage());
				}
			}
		}
		
		//two loops bc need stuff in en to do this part
		for (Block p : blocks) {
			
			for (Pair<catdata.aql.exp.SchExpRaw.Fk, Trans> pp : p.fks) {
				try {
					Ctx<Var, Term<Void, En, Void, Fk, Void, Var, Void>> trans = new Ctx<>();
					for (Pair<Var, RawTerm> v : pp.second.gens) {
						Ctx<String, Chc<Ty, En>> ctx = unVar(ens0.get(dst0.fks.get(pp.first).first).first.inRight());
						Collage<Ty, En, Sym, Fk, Att, Var, Var> col = cols.get(dst0.fks.get(pp.first).first);
						Chc<Ty, En> required = Chc.inRight(ens0.get(dst0.fks.get(pp.first).second).first.get(v.first));
						Term<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En, catdata.aql.exp.TyExpRaw.Sym, catdata.aql.exp.SchExpRaw.Fk, catdata.aql.exp.SchExpRaw.Att, Gen, Sk> term = RawTerm
								.infer1x(ctx.map, v.second, null, required, col.convert(),
										"in foreign key " + pp.first.str + ", ", src0.typeSide.js).second;
						trans.put(v.first, freeze(term.convert(), params).convert());
					}
					boolean doNotCheckEqs = (Boolean) new AqlOptions(pp.second.options, null, env.defaults)
							.getOrDefault(AqlOption.dont_validate_unsafe);
					fks0.put(pp.first, new Pair<>(trans, doNotCheckEqs));

				} catch (RuntimeException ex) {
					ex.printStackTrace();
					throw new LocException(b2.get(pp.first), ex.getMessage());
				}
			}
		}

		boolean doNotCheckEqs = (Boolean) new AqlOptions(options, null, env.defaults)
				.getOrDefault(AqlOption.dont_validate_unsafe);

		boolean elimRed = (Boolean) new AqlOptions(options, null, env.defaults)
				.getOrDefault(AqlOption.query_remove_redundancy);

		for (String s : params.keySet()) {
			xxx.put(new Var(s), new Ty(params.get(s)));
		}
		for (String s : consts.keySet()) {
			Chc<Ty, En> required = Chc.inLeft(xxx.get(new Var(s)));
			Term<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En, catdata.aql.exp.TyExpRaw.Sym, catdata.aql.exp.SchExpRaw.Fk, catdata.aql.exp.SchExpRaw.Att, Gen, Sk> term = RawTerm
					.infer1x(new HashMap<>(), consts.get(s), null, required, src0.collage().convert(), "", src0.typeSide.js).second;
			
			yyy.put(new Var(s), term.convert());
		}

		return Query.makeQuery2(xxx, yyy, ens0, atts0, fks0, src0, dst0, doNotCheckEqs, elimRed);
	}

	public static void processAtt(Schema<Ty, En, Sym, Fk, Att> src0, Schema<Ty, En, Sym, Fk, Att> dst0,
			Ctx<En, Triple<Ctx<Var, En>, Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>>, AqlOptions>> ens0,
			Ctx<Att, Term<Ty, En, Sym, Fk, Att, Var, Var>> atts0,
			Ctx<En, Collage<Ty, En, Sym, Fk, Att, Var, Var>> cols, Pair<Att, RawTerm> p, Ctx<String, String> params) {
		Ctx<String, Chc<Ty, En>> ctx = unVar(ens0.get(dst0.atts.get(p.first).first).first.inRight());
		Collage<Ty, En, Sym, Fk, Att, Var, Var> col = cols.get(dst0.atts.get(p.first).first);
		Chc<Ty, En> required = Chc.inLeft(dst0.atts.get(p.first).second);
		 for (String q : params.keySet()) {
			ctx.put(q, Chc.inLeft(new Ty(params.get(q))));
			//col.sks.put(new Var(q), new Ty(params.get(q)));
		} 
		Term<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En, catdata.aql.exp.TyExpRaw.Sym, catdata.aql.exp.SchExpRaw.Fk, catdata.aql.exp.SchExpRaw.Att, Gen, Sk> term = RawTerm
				.infer1x(ctx.map, p.second, null, required, col.convert(), "", src0.typeSide.js).second;
		atts0.put(p.first, freeze(term.convert(), params));
	}

	public static void processBlock(Map<String, String> options, AqlEnv env, Schema<Ty, En, Sym, Fk, Att> src0,
			Ctx<En, Triple<Ctx<Var, En>, Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>>, AqlOptions>> ens0,
			Ctx<En, Collage<Ty, En, Sym, Fk, Att, Var, Var>> cols, Block p, Ctx<String, String> params) {

		Ctx<Var, En> ctx = new Ctx<Var, En>(Util.toMapSafely(p.gens)); // p.second.gens);
		for (Var v : ctx.map.keySet()) {
			En en = ctx.get(v);
			if (!src0.ens.contains(en)) {
				throw new RuntimeException(
						"from clause contains " + v + ":" + en + ", but " + en + " is not a source entity");
			}
		}
		Collage<Ty, En, Sym, Fk, Att, Var, Var> col = new Collage<>(src0.collage());
		Ctx<String, Chc<Ty, En>> ctx0 = unVar(ctx.inRight());
		col.gens.putAll(ctx.map);
		
		for (String q : params.keySet()) {
			ctx0.put(q, Chc.inLeft(new Ty(params.get(q))));
			col.sks.put(new Var(q), new Ty(params.get(q)));
		}
	
		cols.put(p.en, col);
		Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>> eqs = new HashSet<>();
	
		for (Pair<RawTerm, RawTerm> eq : p.eqs) {
			Triple<Ctx<Var, Chc<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En>>, Term<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En, catdata.aql.exp.TyExpRaw.Sym, catdata.aql.exp.SchExpRaw.Fk, catdata.aql.exp.SchExpRaw.Att, Gen, Sk>, Term<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En, catdata.aql.exp.TyExpRaw.Sym, catdata.aql.exp.SchExpRaw.Fk, catdata.aql.exp.SchExpRaw.Att, Gen, Sk>> x = RawTerm
					.infer1x(ctx0.map, eq.first, eq.second, null, col.convert(),
							"In equation " + eq.first + " = " + eq.second + ", ", src0.typeSide.js)
					.first3();
			eqs.add(new Eq<>(new Ctx<>(), freeze(x.second.convert(), params), freeze(x.third.convert(), params)));
		}
		Map<String, String> uu = new HashMap<>(options);
		uu.putAll(p.options);
		AqlOptions theops = new AqlOptions(uu, null, env.defaults);
		Triple<Ctx<Var, En>, Collection<Eq<Ty, En, Sym, Fk, Att, Var, Var>>, AqlOptions> b = new Triple<>(ctx, eqs,
				theops);
		ens0.put(p.en, b);
	}

	public static <Ty, En, Sym, Fk, Att> Term<Ty, En, Sym, Fk, Att, Var, Var> freeze(
			Term<Ty, En, Sym, Fk, Att, Var, Var> term, Ctx<String, String> params) {
		Map<Var, Term<Ty, En, Sym, Fk, Att, Var, Var>> m = new HashMap<>();
		for (Var v : term.vars()) {
			if (params.keySet().contains(v.var)) {
				m.put(v, Term.Sk(v));
			} else {
				m.put(v, Term.Gen(v));
			}
		}
		return term.subst(m);
	}

	public static <X> Ctx<String, X> unVar(Ctx<Var, X> ctx) {
		Ctx<String, X> ret = new Ctx<>();
		for (Var v : ctx.keySet()) {
			ret.put(v.var, ctx.get(v));
		}
		return ret;
	}

	// TODO aql identity query
	// TODO aql compose query

}
