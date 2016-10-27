package catdata.aql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;

public class AqlFdm {

	private static class AqlDelta<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y> extends Algebra<Ty, En1, Sym, Fk1, Att1, Pair<En1,Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y, Pair<En1,Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y> {
		
		@Override
		public String toStringProver() {
			return alg.toStringProver();
		}
		
		public AqlDelta(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> alg) {
			this.F = F;
			this.alg = alg;
		}

		private Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
		private Algebra<Ty, En2, Sym, Fk2, Att2, Gen, Sk, X, Y> alg;
//		private Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk> I; 
	
		@Override
		public boolean hasNFs() {
//			alg.
			return false;
		}

		@Override
		public Schema<Ty, En1, Sym, Fk1, Att1> schema() {
			return F.src;
		}

		@Override
		public boolean eq(Ctx<Var, Chc<Ty, En1>> ctx, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y> lhs, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y> rhs) {
			return alg.eq(F.trans(ctx),translate(lhs),translate(rhs));
		}
		
		@Override
		public Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y> nf(Ctx<Var, Chc<Ty, En1>> ctx, Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y> term) {
			throw new RuntimeException("Anomaly: please report"); //TODO aql maybe there is a normal form
		}

		@Override
		public Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>> nf(Term<Void, En1, Void, Fk1, Void, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Void> term) {
			 Term<Ty,En2,Sym,Fk2,Att2,Gen,Sk> term2 = translate(term.convert());
			 X x = alg.nf(term2.convert());
			 En1 en1 = type(term.convert());
			 return new Pair<>(en1, alg.repr(x));	
		}
	

		private Map<En1, Collection<Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>>> en_cache = new HashMap<>();
		@Override
		public Collection<Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>> en(En1 en) {
			if (en_cache.containsKey(en)) {
				return en_cache.get(en);
			}
			Collection<X> in = alg.en(F.ens.get(en));
			Collection<Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>> ret = new ArrayList<>(in.size());
			for (X x : in) {
				ret.add(new Pair<>(en, alg.repr(x)));
			}
			en_cache.put(en, ret);
			return ret;
		}

		@Override
		public Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>> fk(Fk1 fk1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>> e) {
			X x = alg.nf(e.second);
			for (Fk2 fk2 : F.trans(Util.singList(fk1))) {
				x = alg.fk(fk2, x);
			}
			En1 en1 = F.src.fks.get(fk1).second;
			return new Pair<>(en1, alg.repr(x));
		}

		@Override
		public Collage<Ty, Void, Sym, Void, Void, Void, Y> talg() {
			return alg.talg();
		}
		
		private Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> translate(Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Y> e) {
			if (e.var != null) {
				return Term.Var(e.var);
			} else if (e.gen != null) {
				return e.gen.second.convert();
			} else if (e.fk != null) {
				return F.dst.fold(F.fks.get(e.fk).second,  translate(e.arg));
			}  else if (e.att != null) {
				Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk> t = F.atts.get(e.att).third.convert();
				return t.subst(Util.singMapM(F.atts.get(e.att).first, translate(e.arg)));
			}
			throw new RuntimeException("Anomaly: please report");			
		} 
			
		private En1 type(Term<Ty, En1, Sym, Fk1, Att1, Pair<En1, X>, Sk> e) {
			if (e.gen != null) {
				return e.gen.first;
			} else if (e.fk != null) {
				return F.src.fks.get(e.fk).second; //no need to recurse, only use outer one
			}
			throw new RuntimeException("Anomaly: please report");
		}
		
		@Override
		public Term<Void, En1, Void, Fk1, Void, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, Void> repr(Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>> x) {
			return Term.Gen(x);
		}

		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att1 att, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>> x) {
			return attY(F.atts.get(att).third, x.second);
		}

		private Term<Ty, Void, Sym, Void, Void, Void, Y> attY(Term<Ty, En2, Sym, Fk2, Att2, Void, Void> term, Term<Void, En2, Void, Fk2, Void, Gen, Void> x) {
			if (term.att != null) {
				return alg.att(term.att, alg.nf(attX(term.arg.convert(), x)));
			} else if (term.sym != null) {
				return Term.Sym(term.sym, term.args.stream().map(q -> attY(q, x)).collect(Collectors.toList()));
			} 
			throw new RuntimeException("Anomaly: please report");
		}

		private Term<Void, En2, Void, Fk2, Void, Gen, Void> attX(Term<Void, En2, Void, Fk2, Void, Gen, Void> term, Term<Void, En2, Void, Fk2, Void, Gen, Void> x) {
			if (term.var != null) {
				return x;
			} else if (term.gen != null) {
				return alg.repr(alg.nf(term));
			} else if (term.fk != null) {
				return alg.repr(alg.fk(term.fk, alg.nf(term.arg)));
			}
			throw new RuntimeException("Anomaly: please report");
		}
		
		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Y sk) {
			return Term.Sk(sk);
			//return alg.sk(sk);
		}
		
		@Override
		public String printSk(Y y) {
			return "[" + alg.printSk(y) + "]";
		}

		@Override
		public String printGen(Pair<En1,Term<Void, En2, Void, Fk2, Void, Gen, Void>> x) {
			return x.second.toString();
		}

		@Override
		public Term<Ty, Void, Sym, Void, Void, Void, Y> reprT(Y y) {
			return Term.Sk(y);
		}
				
		
	}	
	
	public static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2> Instance<Ty, En1, Sym, Fk1, Att1, ?, ?> 
	delta(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk> I) {
		Instance<Ty, En1, Sym, Fk1, Att1, Pair<En1, Term<Void, En2, Void, Fk2, Void, Gen, Void>>, ?> ret = new AqlDelta<>(F, I.semantics()).toInstance();
		
		return ret;
	}
	
	public static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2> 
	Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2> delta(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2> h) {
		Instance<Ty,En1,Sym,Fk1,Att1,Gen1,Sk1> src = null; //delta(f, h.src);
		Instance<Ty,En1,Sym,Fk1,Att1,Gen2,Sk2> dst = null ; //delta(f, h.dst);
		
		Map<Gen1, Term<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2>> gens = new HashMap<>();
		for (Object gen1 : src.gens.keySet()) {
			//gens.put(gen1, f.trans(h.gens.get(gen1)));
		}
		Map<Sk1, Term<Ty, En1, Sym, Fk1, Att1, Gen2, Sk2>> sks = new HashMap<>();
		for (Object sk1 : src.sks.keySet()) {
			//sks.put(sk1, f.trans(h.sks.get(sk1)));
		}
		
		return new Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2>(gens, sks, src, dst);
	}
	
	public static <Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2> Instance<Ty, En2, Sym, Fk2, Att2, Gen, Sk> 
	sigma(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F, Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk> I, List<Pair<String, String>> options) {
		Collage<Ty, En2, Sym, Fk2, Att2, Gen, Sk> col = new Collage<>(F.dst);
		
		col.sks.putAll(I.sks);
		for (Gen gen : I.gens.keySet()) {
			col.gens.put(gen, F.ens.get(I.gens.get(gen)));
		}
		
		Set<Pair<Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>, Term<Ty, En2, Sym, Fk2, Att2, Gen, Sk>>> eqs = new HashSet<>();
		for (Pair<Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>, Term<Ty, En1, Sym, Fk1, Att1, Gen, Sk>> eq : I.eqs) {
			eqs.add(new Pair<>(F.trans(eq.first), F.trans(eq.second)));
		}
		AqlOptions strategy = new AqlOptions(Util.toMapSafely(options), col); 
		
		return new Instance<>(F.dst, col.gens, col.sks, eqs, strategy);
	}

	//TODO: this recomputes src and dst
	public static <Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, En2, Fk2, Att2, Gen2, Sk2> Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2> sigma(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f, Transform<Ty, En1, Sym, Fk1, Att1, Gen1, Sk1, Gen2, Sk2> h,  List<Pair<String, String>> options1,  List<Pair<String, String>> options2) {
		Instance<Ty,En2,Sym,Fk2,Att2,Gen1,Sk1> src = sigma(f, h.src, options1);
		Instance<Ty,En2,Sym,Fk2,Att2,Gen2,Sk2> dst = sigma(f, h.dst, options2);
		
		Map<Gen1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> gens = new HashMap<>();
		for (Gen1 gen1 : src.gens.keySet()) {
			gens.put(gen1, f.trans(h.gens.get(gen1)));
		}
		Map<Sk1, Term<Ty, En2, Sym, Fk2, Att2, Gen2, Sk2>> sks = new HashMap<>();
		for (Sk1 sk1 : src.sks.keySet()) {
			sks.put(sk1, f.trans(h.sks.get(sk1)));
		}
		
		return new Transform<Ty, En2, Sym, Fk2, Att2, Gen1, Sk1, Gen2, Sk2>(gens, sks, src, dst);
	}

}
