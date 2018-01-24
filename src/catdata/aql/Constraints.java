package catdata.aql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Unit;
import catdata.Util;
import catdata.aql.ED.WHICH;
import catdata.aql.It.ID;
import catdata.aql.Query.Frozen;
import catdata.aql.fdm.ColimitInstance;
import catdata.aql.fdm.EvalAlgebra.Row;
import catdata.aql.fdm.EvalInstance;
import catdata.aql.fdm.LiteralTransform;
import catdata.graph.DMG;

public class Constraints<Ty, En, Sym, Fk, Att> implements Semantics {

	@Override
	public int size() {
		return eds.size();
	}
	
	public final Schema<Ty, En, Sym, Fk, Att> schema;

	public final Collection<ED<Ty, En, Sym, Fk, Att>> eds;

	@Override
	public String toString() {
		return Util.sep(eds, "\n=====================================================================\n\n");
	}

	// TODO aql equals

	public Constraints(Schema<Ty, En, Sym, Fk, Att> schema, Collection<ED<Ty, En, Sym, Fk, Att>> eds, AqlOptions options) {
		this.eds = new HashSet<>(desugar(eds, options));
		this.schema = schema;
		for (ED<Ty, En, Sym, Fk, Att> ed : eds) {
			if (!ed.schema.equals(schema)) {
				throw new RuntimeException("The ED " + ed + "\n is on schema " + ed.schema + "\n\n, not " + schema + " as expected.");
			}
		}
	}

	private static <Ty, En, Sym, Fk, Att> Collection<ED<Ty, En, Sym, Fk, Att>> desugar(Collection<ED<Ty, En, Sym, Fk, Att>> eds, AqlOptions options) {
		List<ED<Ty, En, Sym, Fk, Att>> l = new LinkedList<>();
		for (ED<Ty, En, Sym, Fk, Att> x : eds) {
			l.add(new ED<>(x.schema, x.As, x.Es, x.Awh, x.Ewh, false, options));
			
			if (x.isUnique) {
				Ctx<Var, En> es2 = x.Es.map((v,t) -> new Pair<>(new Var(v + "0"), t));	
				Map<Var, Term<Ty, En, Sym, Fk, Att, Void, Void>> subst = new HashMap<>();
				Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> ewh = new HashSet<>();
				
				for (Var v : x.Es.keySet()) {
					subst.put(v, Term.Var(new Var(v + "0")));
					ewh.add(new Pair<>(Term.Var(v), subst.get(v)));
				}
				Ctx<Var, En> as = new Ctx<>();
				as.putAll(x.As.map);
				as.putAll(x.Es.map);
				as.putAll(es2.map);
				Set<Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> awh = new HashSet<>();
				awh.addAll(x.Awh);
				awh.addAll(x.Ewh);
				for (Pair<Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> p : x.Ewh) {
					awh.add(new Pair<>(p.first.subst(subst), p.second.subst(subst)));
				}
				l.add(new ED<>(x.schema, as, new Ctx<>(), awh, ewh, false, options));
			}
			
		}
		return l;
	}

	@Override
	public Kind kind() {
		return Kind.CONSTRAINTS;
	}
	
	public <Gen, Sk, X, Y> Instance<Ty, En, Sym, Fk, Att, ?, ?, ?, ?> chase(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I, AqlOptions options) {	
		for (ED<Ty, En, Sym, Fk, Att> ed : eds) {
			Frozen<Ty, En, Sym, Fk, Att> f = ed.Q.ens.get(ED.WHICH.FRONT);
			if (!f.algebra().hasFreeTypeAlgebraOnJava()) {
				throw new RuntimeException("Cannot chase, unsafe use of java in front of\n" + ed);
			}
			f = ed.Q.ens.get(ED.WHICH.BACK);
			if (!f.algebra().hasFreeTypeAlgebraOnJava()) {
				throw new RuntimeException("Cannot chase, unsafe use of java in back of\n" + ed);
			}
		}
		Instance<Ty, En, Sym, Fk, Att, ?, ?, ?, ?> ret = I;
		for (;;) {
			Instance<Ty, En, Sym, Fk, Att, ?, ?, ?, ?> ret2 = step(ret, options);
			if (ret2 == null) {
				return ret;
			}
			ret = ret2;
		}		
	}

	// TODO aql needs to be over all eds
	public <Gen, Sk, X, Y> Instance<Ty, En, Sym, Fk, Att, ?, ?, ?, ?> step(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I, AqlOptions options) {
		Collection<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>> T = triggers(I, options);

		if (T.isEmpty()) {
			 return null;
		}

		DMG<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Void> shape = new DMG<>(T, new HashMap<>());
		Ctx<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Instance<Ty, En, Sym, Fk, Att, Var, Var, ID, Chc<Var, Pair<ID, Att>>>> nodesA = new Ctx<>();
		Ctx<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Instance<Ty, En, Sym, Fk, Att, Var, Var, ID, Chc<Var, Pair<ID, Att>>>> nodesE = new Ctx<>();
	
		Map<Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Term<Void, En, Void, Fk, Void, Gen, Void>> aaa = new HashMap<>();
		Map<Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Term<Void, En, Void, Fk, Void, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Void>> xxx = new HashMap<>();
	

		for (Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>> t : T) {
			Query<Ty, En, Sym, Fk, Att, WHICH, Unit, Void> Q = t.first.Q;
			Instance<Ty, En, Sym, Fk, Att, Var, Var, ID, Chc<Var, Pair<ID, Att>>> A = Q.ens.get(WHICH.FRONT);
			Instance<Ty, En, Sym, Fk, Att, Var, Var, ID, Chc<Var, Pair<ID, Att>>> E = Q.ens.get(WHICH.BACK);

			Transform<Ty, En, Sym, Fk, Att, Var, Var, Var, Var, ID, Chc<Var, Pair<ID, Att>>, ID, Chc<Var, Pair<ID, Att>>> AE = Q.fks.get(new Unit());

			nodesA.put(t, A);
			nodesE.put(t, E);
			for (Var v : AE.src().gens().keySet()) {
				xxx.put(new Pair<>(t,v), Term.Gen(new Pair<>(t, v))); // revisit after colimit fixed
				aaa.put(new Pair<>(t,v), I.algebra().repr(t.second.get(v)));
			}
			/*for (Void v : AE.src().sks().keySet()) {
				yyy.put(v, Util.abort(v)); // revisit after colimit fixed
				bbb.put(v, Util.abort(v));
			}*/
		}

		ColimitInstance<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Void, Ty, En, Sym, Fk, Att, Var, Var, ID, Chc<Var, Pair<ID, Att>>> 
		A0 = new ColimitInstance<>(schema, shape, nodesA, new Ctx<>(), options);

		ColimitInstance<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Void, Ty, En, Sym, Fk, Att, Var, Var, ID, Chc<Var, Pair<ID, Att>>> 
		E0 = new ColimitInstance<>(schema, shape, nodesE, new Ctx<>(), options);

		LiteralTransform<Ty, En, Sym, Fk, Att, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, ID, Chc<Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<ID, Att>>, ID, Chc<Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<ID, Att>>>
		A0E0 = new LiteralTransform<>(xxx, new HashMap<>(), A0, E0, false);

		LiteralTransform<Ty, En, Sym, Fk, Att, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Gen, Sk, ID, Chc<Pair<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>, Var>, Pair<ID, Att>>, X, Y> A0I 
		= 
		new LiteralTransform<>(aaa, new HashMap<>(), A0, I, false);
		
		return pushout(A0E0, A0I, options);
		// TODO aql disable checking for speed

	}

	public <X, Y, Gen, Sk> Collection<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>> triggers(Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I, AqlOptions options) {
		Collection<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>> T = new LinkedList<>();

		for (ED<Ty, En, Sym, Fk, Att> ed : eds) {
			Query<Ty, En, Sym, Fk, Att, WHICH, Unit, Void> Q = ed.Q;
			EvalInstance<Ty, En, Sym, Fk, Att, Gen, Sk, WHICH, Unit, Void, X, Y> QI = new EvalInstance<>(Q, I, options);
			outer: for (Row<WHICH, X> e : QI.algebra().en(WHICH.FRONT)) {
				for (Row<WHICH, X> a : QI.algebra().en(WHICH.BACK)) {
					if (QI.algebra().fk(new Unit(), a).equals(e)) {
						continue outer;
					}
				}
				T.add(new Pair<>(ed, e));
			}
		}
		return T;
	}
	
	
	public static enum THREE {A, B, C}
	public static enum TWO {A, B}
	
	public static <Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2,Gen3,Sk3,X3,Y3> 
	ColimitInstance<THREE, TWO, Ty, En, Sym, Fk, Att, ?, ?, ?, ?> pushout(Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> j, Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen3,Sk3,X1,Y1,X3,Y3> k, AqlOptions options) {
		if (!j.src().equals(k.src())) {
			throw new RuntimeException("Source of \n" + j + "\nnamely \n" + j.src() + "\n is not equal to source of\n" + k + "\nnamely \n" + k.src() );
		}
		Set<THREE> ns = new HashSet<>();
		ns.add(THREE.A);
		ns.add(THREE.B);
		ns.add(THREE.C);
		Map<TWO, Pair<THREE, THREE>> es = new HashMap<>();
		es.put(TWO.A, new Pair<>(THREE.A,THREE.B));
		es.put(TWO.B, new Pair<>(THREE.A,THREE.C));
		DMG<THREE, TWO> shape = new DMG<>(ns, es);
				
		Ctx<THREE, Instance<Ty, En, Sym, Fk, Att, ?, ?, ?, ?>> 
		nodes = new Ctx<>();
		nodes.put(THREE.A, j.src());
		nodes.put(THREE.B, j.dst());
		nodes.put(THREE.C, k.dst());
	
		Ctx<TWO, Transform<Ty, En, Sym, Fk, Att, ?, ?, ?, ?, ?, ?, ?, ?>> 
		edges = new Ctx<>();
		edges.put(TWO.A, j);
		edges.put(TWO.B, k);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ColimitInstance<THREE, TWO, Ty, En, Sym, Fk, Att, ?, ?, ?, ?> 
		ret = new ColimitInstance(j.src().schema(), shape, nodes, edges, options);
		return ret;
	}

}
