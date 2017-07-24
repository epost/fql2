package catdata.aql.exp;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Eq;
import catdata.aql.Kind;
import catdata.aql.Query;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;
import catdata.aql.exp.QueryExpRaw.Block;
import catdata.aql.exp.QueryExpRaw.Trans;
import catdata.aql.exp.SchExp.SchExpCod;

public class QueryExpRawSimple<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> extends QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> implements Raw {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
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
		if (!(obj instanceof QueryExpRawSimple))
			return false;
		QueryExpRawSimple<?, ?, ?, ?, ?, ?, ?, ?> other = (QueryExpRawSimple<?, ?, ?, ?, ?, ?, ?, ?>) obj;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
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

	private final SchExp<Ty, En1, Sym, Fk1, Att1> src;

	private final Map<String, String> options;

	private final Block<En1, Att2> block;
	
	public QueryExpRawSimple(SchExp<?, ?, ?, ?, ?> src,
			Block<En1, Att2> block,
			List<Pair<String, String>> options) {
		this.src = (SchExp<Ty, En1, Sym, Fk1, Att1>) src;
		this.options = Util.toMapSafely(options);
		this.block = block;
	}

	@Override
	public Map<String, String> options() {
		return options;
	}

	@Override
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return block.raw;
	}

	@Override
	public Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> type(AqlTyping G) {
		return new Pair<>(src, new SchExpCod<>(this));
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return src.deps();
	}
	
	//TODO aql merge with queryexpraw
	@Override
	public Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> eval(AqlEnv env) {
		Schema<Ty, En1, Sym, Fk1, Att1> src0 = src.eval(env);
		Collage<Ty, En1, Sym, Fk1, Att1, Void, Void> srcCol = src0.collage();
		En2 en2 = (En2) "Q";
		
		AqlOptions ops =  new AqlOptions(options, null, env.defaults);
		
		boolean doNotCheckEqs = (Boolean)
				ops.getOrDefault(AqlOption.dont_validate_unsafe);

		boolean elimRed = (Boolean) 
				ops.getOrDefault(AqlOption.query_remove_redundancy);
		
		boolean checkJava = ! (Boolean) 
				ops.getOrDefault(AqlOption.allow_java_eqs_unsafe);


		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens0 = new Ctx<>();
		Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts0 = new Ctx<>();
		Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks0 = new Ctx<>();

		Ctx<En2, Collage<Ty, En1, Sym, Fk1, Att1, Var, Void>> cols = new Ctx<>();
			
		QueryExpRaw.processBlock(options, env, src0, ens0, cols, new Pair<>(en2, block));
		
		Collage<Ty, En2, Sym, Fk2, Att2, Void, Void> colForDst = new Collage<>(src0.typeSide.collage());
		colForDst.ens.add(en2);
		for (Pair<Att2, RawTerm> p : block.atts) {
			Map<String, Chc<Ty, En1>> s = QueryExpRaw.unVar(cols.get(en2).gens).<Ty>inRight().map;
			Term<Ty, En1, Sym, Fk1, Att1, Void, Void> term 
			= RawTerm.infer1(s, p.second, p.second, srcCol, 
					src0.typeSide.js).second;
			Chc<Ty, En1> ty = srcCol.type(new Ctx<>(s).map((k,v) -> new Pair<>(new Var(k), v)), term);
			if (!ty.left) {
				throw new LocException(find("attributes", p),
						"In return clause for " + p.first + ", the type is " + ty.r + ", which is an entity.");
			}
			colForDst.atts.put(p.first, new Pair<>(en2, ty.l));
		}
		
		DP<Ty, En2, Sym, Fk2, Att2, Void, Void> dp = AqlProver.create(ops, colForDst, src0.typeSide.js);
		Schema<Ty, En2, Sym, Fk2, Att2> dst0 = new Schema<Ty, En2, Sym, Fk2, Att2>
		(src0.typeSide, colForDst.ens, colForDst.atts.map, colForDst.fks.map, new HashSet<>(), dp, checkJava);

		for (Pair<Att2, RawTerm> p : block.atts) {
			try {
				QueryExpRaw.processAtt(src0, dst0, ens0, atts0, cols, p);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("attributes", p),
						"In return clause for " + p.first + ", " + ex.getMessage());
			}
		}


		return Query.makeQuery(ens0, atts0, fks0, src0, dst0, doNotCheckEqs, elimRed);
	}

	private String toString;
	@Override
	public synchronized String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";

		
			List<String> temp = new LinkedList<>();

			temp.add(block.toString());
			
				toString += "\t\t" + Util.sep(temp, "\n\n\t\t") + "\n";
			
			if (!options.isEmpty()) {
				toString += "\toptions";
				temp = new LinkedList<>();
				for (Entry<String, String> sym : options.entrySet()) {
					temp.add(sym.getKey() + " = " + sym.getValue());
				}

				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			}

			return "simple : " + src + " {\n" + toString + "\n}";
		}

	

	
	
	

}
