package catdata.aql.fdm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.Algebra;
import catdata.aql.AqlOptions;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.It.ID;
import catdata.graph.DMG;

//has to be gen rather than (N,gen) in order to use explicit prover
public class ColimitInstance<N, E, Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> 
 extends Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> {
	
	private final Schema<Ty, En, Sym, Fk, Att> schema;
	
	@SuppressWarnings("unused")
	private final DMG<N, E> shape;
	
	@SuppressWarnings("unused")
	private final Ctx<N, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>> nodes;
	@SuppressWarnings("unused")
	private final Ctx<E, Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk, X, Y, X, Y>> edges;

	private final Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> J;
	
	public ColimitInstance(Schema<Ty, En, Sym, Fk, Att> schema, DMG<N, E> shape, Ctx<N, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>> nodes, Ctx<E, Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk, X, Y, X, Y>> edges, Map<String, String> options) {
		for (N n : nodes.keySet()) {
			if (!nodes.get(n).schema().equals(schema)) {
				throw new RuntimeException("The instance for " + n + " has schema " + nodes.get(n).schema() + ", not " + schema + " as expected");
			}
		}
		for (E e : shape.edges.keySet()) {
			if (!edges.get(e).src().schema().equals(schema)) {
				throw new RuntimeException("On " + e + ", it is on schema \n\n" + edges.get(e).src().schema() + "\n\n, not " + schema + "\n\nas expected");
			}
			
			Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> reqdSrc = nodes.get(shape.edges.get(e).first);
			Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> reqdDst = nodes.get(shape.edges.get(e).second);
			
			Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> givenSrc = edges.get(e).src();
			Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> givenDst = edges.get(e).dst();
			
			//TODO aql in general, this will be too strong - want isomorphism
			//if ((Boolean)new AqlOptions(options, null).getOrDefault(AqlOption.static_typing)) {
				if (!reqdSrc.equals(givenSrc)) {
					throw new RuntimeException("On " + e + ", its source is \n\n" + givenSrc + " \n\n but should be \n\n " + reqdSrc);
				} 
				if (!reqdDst.equals(givenDst)) {
					throw new RuntimeException("On " + e + ", its target is \n\n " + givenDst + " \n\n but should be \n\n " + reqdDst);
				} 
			//}
		}
		
		this.schema = schema;
		this.shape = shape;
		this.nodes = nodes;
		this.edges = edges;
		
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col = new Collage<>(schema.collage());
		Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs = new HashSet<>();
		
		Ctx<Gen, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>> genInv = new Ctx<>();
		Ctx<Sk, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y>> skInv = new Ctx<>();
		
		for (N n : nodes.keySet()) {
			for (Gen gen : nodes.get(n).gens().keySet()) {
				col.gens.put(gen, nodes.get(n).gens().get(gen));
				genInv.put(gen, nodes.get(n));
			}
			for (Sk sk : nodes.get(n).sks().keySet()) {
				col.sks.put(sk, nodes.get(n).sks().get(sk));
				skInv.put(sk, nodes.get(n));
			}
			for (Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : nodes.get(n).eqs()) {
				col.eqs.add(new Eq<>(new Ctx<>(), eq.first, eq.second));
				eqs.add(eq);
			}
		}
		
		for (E e : shape.edges.keySet()) {
			Transform<Ty, En, Sym, Fk, Att, Gen, Sk, Gen, Sk, X, Y, X, Y> h = edges.get(e);
			for (Gen gen : h.src().gens().keySet()) {
				Term<Void, En, Void, Fk, Void, Gen, Void> rhs = h.gens().get(gen);
				eqs.add(new Pair<>(Term.Gen(gen), rhs.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())));
				col.eqs.add(new Eq<>(new Ctx<>(), Term.Gen(gen), rhs.map(Util.voidFn(), Util.voidFn(), Function.identity(), Util.voidFn(), Function.identity(), Util.voidFn())));
			}
			for (Sk sk : h.src().sks().keySet()) {
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs = h.sks().get(sk);
				eqs.add(new Pair<>(Term.Sk(sk), rhs));
				col.eqs.add(new Eq<>(new Ctx<>(), Term.Sk(sk), rhs));
			}
		}
		
		AqlOptions strat = new AqlOptions(options, col);  
		
		Function<Gen,String> printGen = x -> genInv.get(x).algebra().printX(genInv.get(x).algebra().nf(Term.Gen(x)));
		Function<Sk, String> printSk = x -> skInv.get(x).algebra().sk(x).toString(skInv.get(x).algebra()::printY, Util.voidFn());
		
		InitialAlgebra<Ty, En, Sym, Fk, Att, Gen, Sk, ID> initial 
		= new InitialAlgebra<>(strat, schema(), col, new It(), printGen, printSk);
				
		J = new LiteralInstance<>(schema(), col.gens.map, col.sks.map, eqs, initial.dp(), initial); 

	}

	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return schema;
	}

	@Override
	public Ctx<Gen, En> gens() {
		return J.gens();
	}

	@Override
	public Ctx<Sk, Ty> sks() {
		return J.sks();
	}

	@Override
	public Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs() {
		return J.eqs();
	}

	@Override
	public DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp() {
		return J.dp();
	}

	@Override
	public Algebra<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> algebra() {
		return J.algebra();
	}
	
	
}
