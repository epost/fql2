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

//TODO aql add type params to all raws?
public class QueryExpRaw<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2>
		extends QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> implements Raw {

	

	private final SchExp<Ty, En1, Sym, Fk1, Att1> src;
	private final SchExp<Ty, En2, Sym, Fk2, Att2> dst;

	private final Set<String> imports;

	private final Map<String, String> options;

	private final Set<Pair<En2, Block<En1, Att2>>> blocks;

	private final Set<Pair<Fk2, Trans>> fks;

	private final Set<Pair<Att2, RawTerm>> atts;

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

		/*
		 * public void asTree(DefaultMutableTreeNode root) { if (gens.size() >
		 * 0) { DefaultMutableTreeNode n = new DefaultMutableTreeNode();
		 * n.setUserObject("entities"); for (Pair<Var, RawTerm> t : gens) {
		 * DefaultMutableTreeNode m = new DefaultMutableTreeNode();
		 * m.setUserObject(t.first + " -> " + t.second); n.add(m); }
		 * root.add(n); }
		 * 
		 * 
		 * }
		 */
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
				for (Pair<Var, RawTerm> en1 : gens) {
					temp.add(en1.first + " -> " + en1.second);
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

	public static class Block<En1, Att2> extends Exp<Void> implements Raw {

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

		public final Set<Pair<Var, En1>> gens;

		public final Set<Pair<RawTerm, RawTerm>> eqs;

		public final Map<String, String> options;

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
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
			Block<?, ?> other = (Block<?, ?>) obj;
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
			return true;
		}

		public final Set<Pair<Att2, RawTerm>> atts;

		public Block(List<Pair<LocStr, String>> gens, List<Pair<Integer, Pair<RawTerm, RawTerm>>> eqs,
				List<Pair<String, String>> options, List<Pair<LocStr, RawTerm>> atts) {
			this.gens = new HashSet<>();
			this.atts = LocStr.set2(atts).stream().map(x -> new Pair<>((Att2) x.first, x.second))
					.collect(Collectors.toSet());
			for (Pair<LocStr, String> gen : gens) {
				this.gens.add(new Pair<>(new Var(gen.first.str), (En1) gen.second));
			}
			this.eqs = LocStr.proj2(eqs);
			this.options = Util.toMapSafely(options);

			List<InteriorLabel<Object>> e = new LinkedList<>();
			for (Pair<LocStr, String> p : gens) {
				e.add(new InteriorLabel<>("from", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " : " + x.second).conv());
			}
			raw.put("from", e);

			List<InteriorLabel<Object>> xx = new LinkedList<>();
			for (Pair<Integer, Pair<RawTerm, RawTerm>> p : eqs) {
				xx.add(new InteriorLabel<>("where", p.second, p.first, x -> x.first + " = " + x.second).conv());
			}
			raw.put("where", xx);

			xx = new LinkedList<>();
			for (Pair<LocStr, RawTerm> p : atts) {
				xx.add(new InteriorLabel<>("return", new Pair<>(p.first.str, p.second), p.first.loc,
						x -> x.first + " -> " + x.second).conv());
			}
			raw.put("return", xx);
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
				toString += "from\t";

				Map<En1, Set<Var>> x = Util.revS(Util.toMapSafely(gens));
				temp = new LinkedList<>();
				for (En1 en1 : Util.alphabetical(x.keySet())) {
					temp.add(Util.sep(x.get(en1), " ") + " : " + en1);
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
				toString += "\n\t\t\t\treturn\t";
				temp = new LinkedList<>();
				for (Pair<Att2, RawTerm> sym : Util.alphabetical(atts)) {
					temp.add(sym.first + " -> " + sym.second);
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

			return "\t" + toString ;
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
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
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
		QueryExpRaw<?, ?, ?, ?, ?, ?, ?, ?> other = (QueryExpRaw<?, ?, ?, ?, ?, ?, ?, ?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
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
			toString += "\tentities";

			for (Pair<En2, Block<En1, Att2>> x : Util.alphabetical(blocks)) {
				temp.add(x.first + " -> {" + x.second.toString() + "}");
			}

			toString += "\n\t\t" + Util.sep(temp, "\n\n\t\t") + "\n";
		}

		if (!fks.isEmpty()) {
			toString += "\tforeign_keys";
			temp = new LinkedList<>();
			for (Pair<Fk2, Trans> sym : Util.alphabetical(fks)) {
				temp.add(sym.first + " -> " + sym.second + "");
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

	/*
	 * public static class QueryExpRawHelper<En1, Att2> extends Exp<Void>
	 * implements Raw {
	 * 
	 * private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	 * 
	 * 
	 * 
	 * Block<En1, Att2> b; List<Pair<LocStr, RawTerm>> atts;
	 * 
	 * public QueryExpRawHelper(Block<En1, Att2> b, List<Pair<LocStr, RawTerm>>
	 * atts) { this.b = b; this.atts = atts;
	 * 
	 * raw.putAll(b.raw.map);
	 * 
	 * List<InteriorLabel<Object>> xx = new LinkedList<>(); for (Pair<Att2,
	 * RawTerm> p : b.atts) { xx.add(new InteriorLabel<>("return", new
	 * Pair<>(p.first.str, p.second), p.first.loc, x -> x.first + " -> " +
	 * x.second).conv()); } raw.put("return", xx); }
	 * 
	 * @Override public int hashCode() { final int prime = 31; int result = 1;
	 * result = prime * result + ((atts == null) ? 0 : atts.hashCode()); result
	 * = prime * result + ((b == null) ? 0 : b.hashCode()); return result; }
	 * 
	 * @Override public boolean equals(Object obj) { if (this == obj) return
	 * true; if (obj == null) return false; if (getClass() != obj.getClass())
	 * return false; QueryExpRawHelper other = (QueryExpRawHelper) obj; if (atts
	 * == null) { if (other.atts != null) return false; } else if
	 * (!atts.equals(other.atts)) return false; if (b == null) { if (other.b !=
	 * null) return false; } else if (!b.equals(other.b)) return false; return
	 * true; }
	 * 
	 * @Override public Ctx<String, List<InteriorLabel<Object>>> raw() { return
	 * null; }
	 * 
	 * @Override protected Map<String, String> options() { return null; }
	 * 
	 * @Override public Kind kind() { return null; }
	 * 
	 * @Override public Void eval(AqlEnv env) { return null; }
	 * 
	 * @Override public String toString() { return null; }
	 * 
	 * @Override public Collection<Pair<String, Kind>> deps() { return null; }
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

	@SuppressWarnings("unchecked")
	public QueryExpRaw(SchExp<?, ?, ?, ?, ?> c, SchExp<?, ?, ?, ?, ?> d, List<LocStr> imports,
			List<Pair<LocStr, Pair<Block<En1, Att2>, List<Pair<LocStr, RawTerm>>>>> list, List<Pair<LocStr, Trans>> fks,
			List<Pair<String, String>> options) {
		this.src = (SchExp<Ty, En1, Sym, Fk1, Att1>) c;
		this.dst = (SchExp<Ty, En2, Sym, Fk2, Att2>) d;
		this.imports = LocStr.set1(imports);
		this.options = Util.toMapSafely(options);
		this.blocks = list.stream().map(x -> new Pair<>((En2) x.first.str, x.second.first)).collect(Collectors.toSet());
		this.fks = LocStr.set2(fks).stream().map(x -> new Pair<>((Fk2) x.first, x.second)).collect(Collectors.toSet());
		atts = new HashSet<>();
		for (Pair<LocStr, Pair<Block<En1, Att2>, List<Pair<LocStr, RawTerm>>>> block : list) {
			atts.addAll(block.second.second.stream().map(x -> new Pair<>((Att2) x.first.str, x.second))
					.collect(Collectors.toList()));
		}

		raw.put("imports", InteriorLabel.imports("imports", imports));

		List<InteriorLabel<Object>> f = new LinkedList<>();
		List<InteriorLabel<Object>> g = new LinkedList<>();
		for (Pair<LocStr, Pair<Block<En1, Att2>, List<Pair<LocStr, RawTerm>>>> p : list) {
			f.add(new InteriorLabel<>("entities", p.second.first, p.first.loc, x -> p.first.str).conv());
			
			for (Pair<LocStr, RawTerm> q : p.second.second) {
				g.add(new InteriorLabel<>("attributes", new Pair<>(q.first.str, q.second), q.first.loc, x -> x.first + " -> " + x.second).conv());
			}
			
		}
		raw.put("entities", f);
		raw.put("attributes", g);

		f = new LinkedList<>();
		for (Pair<LocStr, Trans> p : fks) {
			f.add(new InteriorLabel<>("foreign keys", p.second, p.first.loc, x -> p.first.str).conv());
		}
		raw.put("foreign keys", f);
	}

	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

	@Override
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}

	public QueryExpRaw(SchExp<Ty, En1, Sym, Fk1, Att1> src, SchExp<Ty, En2, Sym, Fk2, Att2> dst, List<String> imports,
			List<Pair<En2, Pair<Block<En1, Att2>, List<Pair<Att2, RawTerm>>>>> blocks, List<Pair<Fk2, Trans>> fks,
			List<Pair<String, String>> options, @SuppressWarnings("unused") Object o) {
		this.src = src;
		this.dst = dst;
		this.imports = new HashSet<>(imports);
		this.options = Util.toMapSafely(options);
		this.blocks = blocks.stream().map(x -> new Pair<>(x.first, x.second.first)).collect(Collectors.toSet());
		this.fks = new HashSet<>(fks);
		atts = Collections.emptySet();
		for (Pair<En2, Pair<Block<En1, Att2>, List<Pair<Att2, RawTerm>>>> block : blocks) {
			atts.addAll(block.second.second);
		}
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Util.union(src.deps(), Util.union(dst.deps(),
				imports.stream().map(x -> new Pair<>(x, Kind.QUERY)).collect(Collectors.toSet())));
	}

	@Override
	public Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> type(AqlTyping G) {
		return new Pair<>(src, dst);
	}

	@Override
	public Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> eval(AqlEnv env) {
		Schema<Ty, En1, Sym, Fk1, Att1> src0 = src.eval(env);
		Schema<Ty, En2, Sym, Fk2, Att2> dst0 = dst.eval(env);

		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens0 = new Ctx<>();
		Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts0 = new Ctx<>();
		Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks0 = new Ctx<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> v = env.defs.qs.get(k);
			for (En2 en2 : v.ens.keySet()) {
				ens0.put(en2, new Triple<>(v.ens.get(en2).gens, v.ens.get(en2).eqs, v.ens.get(en2).options));
			}
			for (Att2 att2 : v.atts.keySet()) {
				atts0.put(att2, v.atts.get(att2));
			}
			for (Fk2 fk2 : v.fks.keySet()) {
				fks0.put(fk2, new Pair<>(v.fks.get(fk2).gens(), v.doNotValidate.get(fk2)));
			}
		}

		Ctx<En2, Collage<Ty, En1, Sym, Fk1, Att1, Var, Void>> cols = new Ctx<>();
		for (Pair<En2, Block<En1, Att2>> p : blocks) {
			
			try {
					if (!dst0.ens.contains(p.first)) {
						throw new RuntimeException(
								"the proposed target entity " + p.first + " does not actually appear in the target schema");
					}
				processBlock(options, env, src0, ens0, cols, p);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("entities", p.second),
						"In block for target entity " + p.first + ", " + ex.getMessage());
			}
		}

		for (Pair<Att2, RawTerm> p : atts) {
			try {
			
				processAtt(src0, dst0, ens0, atts0, cols, p);

			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("attributes", p),
						"In return clause for " + p.first + ", " + ex.getMessage());
			}
		}

		for (Pair<Fk2, Trans> p : fks) {
			try {
				Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>> trans = new Ctx<>();
				for (Pair<Var, RawTerm> v : p.second.gens) {
					Ctx<String, Chc<Ty, En1>> ctx = unVar(ens0.get(dst0.fks.get(p.first).first).first.inRight());
					Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = cols.get(dst0.fks.get(p.first).first);
					Chc<Ty, En1> required = Chc.inRight(ens0.get(dst0.fks.get(p.first).second).first.get(v.first));
					Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term = RawTerm.infer1x(ctx.map, v.second, null, required, col,
							"in foreign key " + p.first + ", ", src0.typeSide.js).second;
					trans.put(v.first, freeze(term).convert());
				}
				boolean doNotCheckEqs = (Boolean) new AqlOptions(p.second.options, null, env.defaults)
						.getOrDefault(AqlOption.dont_validate_unsafe);
				fks0.put(p.first, new Pair<>(trans, doNotCheckEqs));

			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("foreign keys", p.second), ex.getMessage());
			}
		}

		boolean doNotCheckEqs = (Boolean) new AqlOptions(options, null, env.defaults)
				.getOrDefault(AqlOption.dont_validate_unsafe);

		boolean elimRed = (Boolean) new AqlOptions(options, null, env.defaults)
				.getOrDefault(AqlOption.query_remove_redundancy);

		return Query.makeQuery(ens0, atts0, fks0, src0, dst0, doNotCheckEqs, elimRed);
	}

	public static <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> void processAtt(Schema<Ty, En1, Sym, Fk1, Att1> src0, Schema<Ty, En2, Sym, Fk2, Att2> dst0,
			Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens0,
			Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts0,
			Ctx<En2, Collage<Ty, En1, Sym, Fk1, Att1, Var, Void>> cols, Pair<Att2, RawTerm> p) {
		Ctx<String, Chc<Ty, En1>> ctx = unVar(ens0.get(dst0.atts.get(p.first).first).first.inRight());
		Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = cols.get(dst0.atts.get(p.first).first);
		Chc<Ty, En1> required = Chc.inLeft(dst0.atts.get(p.first).second);
		Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term = RawTerm.infer1x(ctx.map, p.second, null, required, col, "",
				src0.typeSide.js).second;
		atts0.put(p.first, freeze(term));
	}

	public static <Ty, En1, Sym, Fk1, Att1, En2, Att2> void processBlock(Map<String, String> options, AqlEnv env, Schema<Ty, En1, Sym, Fk1, Att1> src0,
			Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens0,
			Ctx<En2, Collage<Ty, En1, Sym, Fk1, Att1, Var, Void>> cols, Pair<En2, Block<En1, Att2>> p) {

		Ctx<Var, En1> ctx = new Ctx<Var, En1>(Util.toMapSafely(p.second.gens)); // p.second.gens);
		for (Var v : ctx.map.keySet()) {
			En1 en = ctx.get(v);
			if (!src0.ens.contains(en)) {
				throw new RuntimeException(
						"from clause contains " + v + ":" + en + ", but " + en + " is not a source entity");
			}
		}
		Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = new Collage<>(src0.collage());
		Ctx<String, Chc<Ty, En1>> ctx0 = unVar(ctx.inRight());
		col.gens.putAll(ctx.map);
		cols.put(p.first, col);
		Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = new HashSet<>();
		for (Pair<RawTerm, RawTerm> eq : p.second.eqs) {
				Triple<Ctx<Var, Chc<Ty, En1>>, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> x = RawTerm
						.infer1x(ctx0.map, eq.first, eq.second, null, col, "In equation " + eq.first + " = " + eq.second + ", ", src0.typeSide.js).first3();
				eqs.add(new Eq<>(new Ctx<>(), freeze(x.second), freeze(x.third)));
		}
		Map<String, String> uu = new HashMap<>(options);
		uu.putAll(p.second.options);
		AqlOptions theops = new AqlOptions(uu, null, env.defaults);
		Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions> b = new Triple<>(
				ctx, eqs, theops);
		ens0.put(p.first, b);
	}

	public static <Ty, En1, Sym, Fk1, Att1> Term<Ty, En1, Sym, Fk1, Att1, Var, Void> freeze(Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term) {
		Map<Var, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> m = new HashMap<>();
		for (Var v : term.vars()) {
			m.put(v, Term.Gen(v));
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
