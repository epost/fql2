package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Eq;
import catdata.aql.Query;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;

//TODO aql add type params to all raws?
public class QueryExpRaw<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> 
extends QueryExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> {
	
	public final SchExp<Ty,En1,Sym,Fk1,Att1> src;
	public final SchExp<Ty,En2,Sym,Fk2,Att2> dst;
	
	public final List<String> imports;
	
	public final Map<String, String> options;
	
	public final List<Pair<En2, Block<En1,Att2>>> blocks;

	public final List<Pair<Fk2, Trans>> fks;
	
	public final List<Pair<Att2, RawTerm>> atts;
	
	public static class Trans {
		public final List<Pair<Var, RawTerm>> gens; 

		public final Map<String, String> options;

		public Trans(List<Pair<String, RawTerm>> gens, List<Pair<String, String>> options) {
			this.gens = new LinkedList<>();
			for (Pair<String, RawTerm> gen : gens) {
				this.gens.add(new Pair<>(new Var(gen.first), gen.second));
			}
			this.options = Util.toMapSafely(options);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
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

		@Override
		public String toString() {
			return "Trans [gens=" + gens + ", options=" + options + "]";
		}
		
		
	}
	
	public static class Block<En1, Att2> {
		public final List<Pair<Var, En1>> gens; 

		public final List<Pair<RawTerm, RawTerm>> eqs;
	
		public final Map<String, String> options;

		@Override
		public int hashCode() {
			final int prime = 31;
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
			Block<?,?> other = (Block<?,?>) obj;
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

	
		public Block(List<Pair<String, En1>> gens, List<Pair<RawTerm, RawTerm>> eqs, List<Pair<String, String>> options) {
			this.gens = new LinkedList<>();
			for (Pair<String, En1> gen : gens) {
				this.gens.add(new Pair<>(new Var(gen.first), gen.second));
			}
			this.eqs = eqs;
			this.options = Util.toMapSafely(options);
		}

		@Override
		public String toString() {
			return "Block [gens=" + gens + ", eqs=" + eqs + ", options=" + options + "]";
		}
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
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
		QueryExpRaw<?,?,?,?,?,?,?,?> other = (QueryExpRaw<?,?,?,?,?,?,?,?>) obj;
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

	@Override
	public String toString() {
		return "QueryExpRaw [src=" + src + ", dst=" + dst + ", imports=" + imports + ", options=" + options + ", blocks=" + blocks + ", fks=" + fks + ", atts=" + atts + "]";
	}


	
	public QueryExpRaw(SchExp<Ty, En1, Sym, Fk1, Att1> src, SchExp<Ty, En2, Sym, Fk2, Att2> dst, List<String> imports, List<Pair<En2, Pair<Block<En1, Att2>, List<Pair<Att2, RawTerm>>>>> blocks,  List<Pair<Fk2, Trans>> fks, List<Pair<String, String>> options) {
		this.src = src;
		this.dst = dst;
		this.imports = imports;
		this.options = Util.toMapSafely(options);
		this.blocks = blocks.stream().map(x -> new Pair<>(x.first, x.second.first)).collect(Collectors.toList());
		this.fks = new LinkedList<>(fks);
		this.atts = new LinkedList<>();
		for (Pair<En2, Pair<Block<En1, Att2>, List<Pair<Att2, RawTerm>>>> block : blocks) {
			atts.addAll(block.second.second);
		}
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Util.union(src.deps(), Util.union(dst.deps(), imports.stream().map(x -> new Pair<>(x, Kind.QUERY)).collect(Collectors.toSet())));
	}

	@Override
	public Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> type(Ctx<String, Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>> ctx) {
		return new Pair<>(src, dst);
	}

	
	@Override
	public Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> eval(AqlEnv env) {
		Schema<Ty, En1, Sym, Fk1, Att1> src0 = src.eval(env);
		Schema<Ty, En2, Sym, Fk2, Att2> dst0 = dst.eval(env);
	
		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, Map<String, String>>> ens0 = new Ctx<>();
		Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts0 = new Ctx<>();
		Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks0 = new Ctx<>();
	
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> v = (Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2>) env.getQuery(k);
			for (En2 en2 : v.ens.keySet()) {
				ens0.put(en2, new Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, Map<String, String>>(v.ens.get(en2).gens, v.ens.get(en2).eqs, v.ens.get(en2).options));
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
			Ctx<Var, En1> ctx = new Ctx<>(p.second.gens);
			Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = new Collage<>(src0.collage());
			Ctx<String, Chc<Ty, En1>> ctx0 = unVar(ctx.inRight());
			col.gens.putAll(ctx.map);
			cols.put(p.first, col);
			Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = new HashSet<>();
			for (Pair<RawTerm, RawTerm> eq : p.second.eqs) {
				Triple<Ctx<String,Chc<Ty,En1>>,Term<Ty,En1,Sym,Fk1,Att1,Var,Void>,Term<Ty,En1,Sym,Fk1,Att1,Var,Void>> x = RawTerm.infer1(ctx0.map, eq.first, eq.second, col);
				eqs.add(new Eq<>(new Ctx<>(), freeze(x.second), freeze(x.third)));
			}
			Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, Map<String, String>> b = new Triple<>(ctx, eqs, p.second.options);
			ens0.put(p.first, b);
		}
		
		for (Pair<Att2, RawTerm> p : atts) {
			Ctx<String, Chc<Ty, En1>> ctx = unVar(ens0.get(dst0.atts.get(p.first).first).first.inRight());
			Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = cols.get(dst0.atts.get(p.first).first);
			Chc<Ty, En1> required = Chc.inLeft(dst0.atts.get(p.first).second);
			Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term = RawTerm.infer0(ctx.map, p.second, required , col, "in attribute " + p.first + ", ");
			atts0.put(p.first, freeze(term));
		}
		
		for (Pair<Fk2, Trans> p : fks) {
			Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>> trans = new Ctx<>();
			for (Pair<Var, RawTerm> v : p.second.gens) {
				Ctx<String, Chc<Ty, En1>> ctx = unVar(ens0.get(dst0.fks.get(p.first).first).first.inRight());
				Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = cols.get(dst0.fks.get(p.first).first);
				Chc<Ty, En1> required = Chc.inRight(ens0.get(dst0.fks.get(p.first).second).first.get(v.first));
				Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term = RawTerm.infer0(ctx.map, v.second, required , col, "in foreign key " + p.first + ", ");
				trans.put(v.first, freeze(term).convert());
			}
			boolean doNotCheckEqs = (Boolean) new AqlOptions(p.second.options, null).getOrDefault(AqlOption.dont_validate_unsafe); 
			fks0.put(p.first, new Pair<>(trans, doNotCheckEqs));
		}
	
		
		boolean doNotCheckEqs = (Boolean) new AqlOptions(options, null).getOrDefault(AqlOption.dont_validate_unsafe); 
		
		return new Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2>(ens0, atts0, fks0, src0, dst0, doNotCheckEqs );
	}

	private Term<Ty, En1, Sym, Fk1, Att1, Var, Void> freeze(Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term) {
		Map<Var,Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> m = new HashMap<>();
		for (Var v : term.vars()) {
			m.put(v, Term.Gen(v));
		}
		return term.subst(m);
	}

	private <X> Ctx<String, X> unVar(Ctx<Var, X> ctx) {
		Ctx<String, X> ret = new Ctx<>();
		for (Var v : ctx.keySet()) {
			ret.put(v.var, ctx.get(v));
		}
		return ret;
	}

//TODO aql identity query
//TODO aql compose query

	
}
