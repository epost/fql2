package catdata.aql.fdm;

import java.util.function.Function;

import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Term;
import catdata.aql.Transform;

public class DeltaTransform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2, X1, Y1, X2, Y2>
extends Transform<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2, Pair<En1, X1>, Y1, Pair<En1, X2>, Y2> {

	@SuppressWarnings("unused")
	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	@SuppressWarnings("unused")
	private final Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h;
	
	private final DeltaInstance<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, X1, Y1> src;
	private final DeltaInstance<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2, En2, Fk2, Att2, X2, Y2> dst;

	private final Ctx<Pair<En1, X1>, Term<Void, En1, Void, Fk1, Void, Pair<En1, X2>, Void>> gens = new Ctx<>();
	private final Ctx<Y1, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2>> sks = new Ctx<>();
	
	public DeltaTransform(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h) {
		if (!h.src().schema().equals(f.dst)) {
			throw new RuntimeException("Target of mapping is " + f.dst + " but instances are on " + h.src().schema());
		}
        F = f;
		this.h = h;
		src = new DeltaInstance<>(f, h.src());
		dst = new DeltaInstance<>(f, h.dst());
							
		for (Pair<En1, X1> gen1 : src.gens().keySet()) {
			gens.put(gen1, Term.Gen(new Pair<>(gen1.first, h.repr(gen1.second)))); 
		}
		for (Y1 sk1 : src.sks().keySet()) {
			sks.put(sk1, h.dst().algebra().intoY(h.reprT(sk1)).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity()));
		}
		
		validate(false); //TODO aql allow not to validte transform
	}
	
	/* private Term<Ty, Void, Sym, Void, Void, Void, Y2> trans0(Term<Ty, Void, Sym, Void, Void, Void, Y1> term) {
		if (term.var != null) {
			return Term.Var(term.var); 
		} else if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.fk != null) {
			return Term.Fk(term.fk, trans0(term.arg));
		} else if (term.att != null) {
			return Term.Att(term.att, trans0(term.arg));
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args.stream().map(this::trans0).collect(Collectors.toList()));
		} else if (term.gen != null) {
			return Term.Gen(term.gen);
		} else if (term.sk != null) {
			Term<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1> z = h.src().algebra().reprT(Term.Sk(term.sk));
		}
		throw new RuntimeException("Anomaly: please report");
	
	}*/
	
	@Override
	public Ctx<Pair<En1, X1>, Term<Void, En1, Void, Fk1, Void, Pair<En1, X2>, Void>> gens() {
		return gens;
	}
	
	@Override
	public Ctx<Y1, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2>> sks() {
		return sks;
	}
	
	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Pair<En1, X1>, Y1, Pair<En1, X1>, Y1> src() {
		return src;
	}
	
	@Override
	public Instance<Ty, En1, Sym, Fk1, Att1, Pair<En1, X2>, Y2, Pair<En1, X2>, Y2> dst() {
		return dst;
	}

}
