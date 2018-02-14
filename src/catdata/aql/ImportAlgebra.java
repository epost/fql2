package catdata.aql;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Triple;
import catdata.Util;

public class ImportAlgebra<Ty,En,Sym,Fk,Att,X,Y> extends Algebra<Ty,En,Sym,Fk,Att,X,Y,X,Y>  implements DP<Ty,En,Sym,Fk,Att,X,Y>  {

	private final Schema<Ty, En, Sym, Fk, Att> schema;
	private final Ctx<En, Collection<X>> ens;
	private final Ctx<Ty, Collection<Y>> tys;
	private final Ctx<X, Ctx<Fk, X>> fks;
	private final Ctx<X, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Y>>> atts;
	
	private final Function<X, String> printX;
	private final Function<Y, String> printY;
	
	private final Collage<Ty, Void, Sym, Void, Void, Void, Y> talg = new Collage<>();
	
//	private final Ctx<Y, Term<Ty, En, Sym, Fk, Att, X, Y>> reprT_extra;
	 
	public ImportAlgebra(Schema<Ty, En, Sym, Fk, Att> schema, Ctx<En, Collection<X>> ens, Ctx<Ty, Collection<Y>> tys, Ctx<X, Ctx<Fk, X>> fks, Ctx<X, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Y>>> atts, Function<X, String> printX, Function<Y, String> printY, boolean dontCheckClosure) {
		this.schema = schema;
		this.ens = ens;
		this.tys = tys;
		this.fks = fks;
		this.atts = atts;
		this.printX = printX;
		this.printY = printY; 
		initTalg(); 
		if (!dontCheckClosure) {
			checkClosure();
		}
	}

	private void checkClosure() {
		for (En en : schema.ens) {
			for (X x : ens.get(en)) {
				for (Fk fk : schema.fksFrom(en)) {
					if (!fks.containsKey(x)) {
						//moved inside fk loop because don't have Ctx if no fks/atts out
						throw new RuntimeException("Incomplete import: no foreign key values specified for ID " + x + " in entity " + en);
					}
					if (!fks.get(x).containsKey(fk)) {
						throw new RuntimeException("Incomplete import: no value for foreign key " + fk + " specified for  ID " + x+ " in entity " + en);
					}
					X y = fk(fk, x);
					if (!ens.get(schema.fks.get(fk).second).contains(y)) {
						throw new RuntimeException("Incomplete import: value for " + x + "'s foreign key " + fk + " is " + y + " which is not an ID imported into " + schema.fks.get(fk).second) ;
					}
				}
				for (Att att : schema.attsFrom(en)) {
					if (!atts.containsKey(x)) {
						throw new RuntimeException("Incomplete import: no attribute " + att + " specified for ID " + x + " in entity " + en);
					}
					if (!atts.get(x).containsKey(att)) {
						throw new RuntimeException("Incomplete import: no value for attribute " + att + " specified for  ID " + x + " in entity " + en);
							}
					Term<Ty, Void, Sym, Void, Void, Void, Y> y = att(att, x);
					if (y.sk != null && !tys.get(schema.atts.get(att).second).contains(y.sk)) {
						throw new RuntimeException("Incomplete import: value for " + x + "'s attribute " + att + " is " + y.sk + " which is not an ID imported into " + schema.atts.get(att).second) ;
					}
				}
			}
		}
		for (Ty ty : schema.typeSide.tys) {
			if (!tys.containsKey(ty)) {
				throw new RuntimeException("Incomplete import: no skolem values for " + ty);
			}
		}
		
		/*
		 * for (Fk fk : schema.fksFrom(en)) {
					if (!fks.get(x).containsKey(fk)) {
						throw new RuntimeException("Incomplete import: no value for " + x + " along fk " + fk);
					}
					if (!ens.get)
				}
		 */
	}

	private void initTalg() {
		talg.syms.putAll(schema.typeSide.syms.map);
		talg.tys.addAll(schema.typeSide.tys);
		talg.java_fns.putAll(schema.typeSide.js.java_fns.map);
		talg.java_parsers.putAll(schema.typeSide.js.java_parsers.map);
		talg.java_tys.putAll(schema.typeSide.js.java_tys.map);
		for (Ty ty : tys.keySet()) {
			for (Y y : tys.get(ty)) {
				talg.sks.put(y, ty);
			}
		}
		for (Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>> x : schema.typeSide.eqs) {
			talg.eqs.add(new Eq<>(x.first.inLeft(), x.second.mapGenSk(Util.voidFn(), Util.voidFn()), x.third.mapGenSk(Util.voidFn(),Util.voidFn())));
		}
	}

	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, X, Y> lhs, Term<Ty, En, Sym, Fk, Att, X, Y> rhs) {
		if (!ctx.isEmpty()) {
			Util.anomaly();
		} else if (lhs.hasTypeType()) {
			return intoY(schema.typeSide.js.reduce(lhs)).equals(intoY(schema.typeSide.js.reduce(rhs)));
		} 
		return intoX(lhs).equals(intoX(rhs));
	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return schema;
	}

	@Override
	public Collection<X> en(En en) {
		return ens.get(en);
	}

	@Override
	public X gen(X gen) {
		return gen;
	}

	@Override
	public X fk(Fk fk, X x) {
		return fks.get(x).get(fk);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att att, X x) {
		return atts.get(x).get(att);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Y sk) {
		return Term.Sk(sk);
	}

	@Override
	public Term<Void, En, Void, Fk, Void, X, Void> repr(X x) {
		return Term.Gen(x);
	}

	@Override
	public Term<Ty, En, Sym, Fk, Att, X, Y> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Y> y) {
		if (schema.typeSide.js.java_tys.isEmpty()) {
			return y.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
		}
		return schema.typeSide.js.reduce(y.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity()));
	}

	@Override
	public String toStringProver() {
		return "Import algebra prover";
	}

	@Override
	public String printX(X x) {
		return printX.apply(x);
	}

	@Override
	public String printY(Y y) {
		return printY.apply(y);
	}

	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
		return talg;
	}

	@Override
	public boolean hasFreeTypeAlgebra() {
		return talg.eqs.isEmpty();
	}

	@Override
	public boolean hasFreeTypeAlgebraOnJava() {
		return talg.eqs.isEmpty(); //TODO aql guess
	}


}
