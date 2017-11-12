package catdata.aql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;

public final class Schema<Ty, En, Sym, Fk, Att> implements Semantics {

	@Override
	public int size() {
		return ens.size() + atts.size() + fks.size() + eqs.size();
	}

	@Override
	public Kind kind() {
		return Kind.SCHEMA;
	}

	public final TypeSide<Ty, Sym> typeSide;

	public final Set<En> ens;
	public final Ctx<Att, Pair<En, Ty>> atts;
	public final Ctx<Fk, Pair<En, En>> fks;

	public final Set<Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> eqs;

	// TODO: aql who is calling isTypeSide and isSchema?

	private void validate(boolean checkJava) {
		// check that each att/fk is in tys/ens
		for (Att att : atts.keySet()) {
			Pair<En, Ty> ty = atts.get(att);
			if (!typeSide.tys.contains(ty.second)) {
				throw new RuntimeException(
						"On attribute " + att + ", the target type " + ty.second + " is not declared.");
			} else if (!ens.contains(ty.first)) {
				throw new RuntimeException(
						"On attribute " + att + ", the source entity " + ty.first + " is not declared.");
			}
		}
		for (Fk fk : fks.keySet()) {
			Pair<En, En> ty = fks.get(fk);
			if (!ens.contains(ty.second)) {
				throw new RuntimeException(
						"On foreign key " + fk + ", the target entity " + ty.second + " is not declared.");
			} else if (!ens.contains(ty.first)) {
				throw new RuntimeException(
						"On foreign key " + fk + ", the source entity " + ty.first + " is not declared.");
			}
		}
		for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq : eqs) {
			// check that the context is valid for each eq
			if (!ens.contains(eq.first.second)) {
				throw new RuntimeException("In schema equation " + toString(eq) + ", context sort " + eq.first.second
						+ " is not a declared entity.");
			}
			// check lhs and rhs types match in all eqs
			Chc<Ty, En> lhs = type(eq.first, eq.second);
			Chc<Ty, En> rhs = type(eq.first, eq.third);
			if (!lhs.equals(rhs)) {
				throw new RuntimeException("In schema equation " + toString(eq) + ", lhs sort is " + lhs.toStringMash()
						+ " but rhs sort is " + rhs.toStringMash());
			}

		}

		if (typeSide.js.java_tys.isEmpty()) {
			return;
		}
		for (Eq<Ty, En, Sym, Fk, Att, Object, Object> eq : collage().simplify().first.eqs) {
			if (checkJava) {
				Chc<Ty, En> lhs = collage().type(eq.ctx, eq.lhs);

				if (lhs.left && typeSide.js.java_tys.containsKey(lhs.l)) {
					throw new RuntimeException("In schema equation " + eq.lhs + " = " + eq.rhs + ", the return type is "
							+ lhs.l
							+ " which is a java type.  \n\nPossible solution: add options allow_java_eqs_unsafe=true ");
				}
				typeSide.assertNoJava(eq.lhs);
				typeSide.assertNoJava(eq.rhs);
			}
		}

	}

	private String toString(
			Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq) {
		return "forall " + eq.first.first + ":" + eq.first.second + ", " + eq.second + " = " + eq.third;
	}

	public final Chc<Ty, En> type(Pair<Var, En> p, Term<Ty, En, Sym, Fk, Att, ?, ?> term) {
		return term.type(new Ctx<>(), new Ctx<>(p), typeSide.tys, typeSide.syms.map, typeSide.js.java_tys.map, ens,
				atts.map, fks.map, new HashMap<>(), new HashMap<>());
	}

	public static <Ty, Sym> Schema<Ty, Void, Sym, Void, Void> terminal(TypeSide<Ty, Sym> t) {
		return new Schema<>(t, Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap(),
				Collections.emptySet(), t.semantics, false);
	}

	public Schema(TypeSide<Ty, Sym> typeSide, Set<En> ens, Map<Att, Pair<En, Ty>> atts, Map<Fk, Pair<En, En>> fks,
			Set<Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>>> eqs,
			DP<Ty, En, Sym, Fk, Att, Void, Void> semantics, boolean checkJava) {
		Util.assertNotNull(typeSide, ens, fks, atts, eqs, semantics);
		this.typeSide = typeSide;
		this.atts = new Ctx<>(atts);
		this.fks = new Ctx<>(fks);
		this.eqs = new HashSet<>(eqs);
		this.ens = new HashSet<>(ens); // TODO aql arraylist
		dp = semantics;
		validate(checkJava);
	}

	public final DP<Ty, En, Sym, Fk, Att, Void, Void> dp;

	// this could take a while, so make sure two threads don't accidentally do
	// it at the same time
	@SuppressWarnings("unchecked")
	public <Gen, Sk> DP<Ty, En, Sym, Fk, Att, Gen, Sk> dp() {
		return (DP<Ty, En, Sym, Fk, Att, Gen, Sk>) dp;
	}

	private Collage<Ty, En, Sym, Fk, Att, Void, Void> collage;

	@SuppressWarnings("unchecked")
	public final synchronized<Gen, Sk> Collage<Ty, En, Sym, Fk, Att, Gen, Sk> collage() {
		if (collage != null) {
			if (!collage.gens.isEmpty() || !collage.sks.isEmpty()) {
				throw new RuntimeException("Anomaly: please report");
			}
			return (Collage<Ty, En, Sym, Fk, Att, Gen, Sk>) collage; // TODO aql
																		// typesafety
		}
		collage = new Collage<>(typeSide.collage());
		collage.ens.addAll(ens);
		collage.atts.putAll(atts.map);
		collage.fks.putAll(fks.map);
		for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> x : eqs) {
			collage.eqs.add(new Eq<>(new Ctx<>(x.first.first, Chc.inRight(x.first.second)), upgrade(x.second),
					upgrade(x.third)));
		}
		return (Collage<Ty, En, Sym, Fk, Att, Gen, Sk>) collage;
	}

	private <Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> upgrade(Term<Ty, En, Sym, Fk, Att, Void, Void> term) {
		return term.map(Function.identity(), Function.identity(), Function.identity(), Function.identity(),
				Util.voidFn(), Util.voidFn());
	}

	@Override
	public final int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Schema<?, ?, ?, ?, ?> other = (Schema<?, ?, ?, ?, ?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (typeSide == null) {
			if (other.typeSide != null)
				return false;
		} else if (!typeSide.equals(other.typeSide))
			return false;
		return true;
	}

	private String toString = null;

	@Override
	public final String toString() {
		if (toString != null) {
			return toString;
		}
		List<En> ens0 = Util.alphabetical(ens);
		
		List<String> obsEqs = eqs.stream().filter(x -> type(x.first, x.second).left)
				.map(x -> "forall " + x.first.first + ":" + x.first.second + ". " + x.second + " = " + x.third)
				.collect(Collectors.toList());

		List<String> pathEqs = eqs.stream().filter(x -> !type(x.first, x.second).left)
				.map(x -> { 
					List<Fk> l = new LinkedList<>(), r = new LinkedList<>();
					x.second.toFkList(l);
					x.third.toFkList(r);
					
					return x.first.second + "." + Util.sep(l, ".") + " = " + 
					x.first.second + "." + Util.sep(r, ".");
					})
				.collect(Collectors.toList());

		List<String> fks0 = new LinkedList<>();
		for (Fk fk : fks.keySet()) {
			fks0.add(fk + " : " + fks.get(fk).first + " -> " + fks.get(fk).second);
		}
		List<String> atts0 = new LinkedList<>();
		for (Att att : atts.keySet()) {
			atts0.add(att + " : " + atts.get(att).first + " -> " + atts.get(att).second);
		}
		toString = "entities";
		toString += "\n\t" + Util.sep(ens0, " ");

		toString += "\nforeign_keys";
		toString += "\n\t" + Util.sep(fks0, "\n\t");

		toString += "\npath_equations";
		toString += "\n\t" + Util.sep(pathEqs, "\n\t");

		toString += "\nattributes";
		toString += "\n\t" + Util.sep(atts0, "\n\t");

		toString += "\nobservation_equations";
		toString += "\n\t" + Util.sep(obsEqs, "\n\t");

		return toString;
	}
	// TODO aql alphabetical?

	private Map<En, List<Att>> attsFrom = new HashMap<>(); 
	public synchronized final Collection<Att> attsFrom(En en) {
		if (attsFrom.containsKey(en)) {
			return attsFrom.get(en);
		}
		List<Att> l = new LinkedList<>();
		for (Att att : atts.keySet()) {
			if (atts.get(att).first.equals(en)) {
				l.add(att);
			}
		}
		attsFrom.put(en, l);
		return l;
	}
	
	private Map<En, List<Fk>> fksFrom = new HashMap<>(); 
	public synchronized final Collection<Fk> fksFrom(En en) {
		if (fksFrom.containsKey(en)) {
			return fksFrom.get(en);
		}
		List<Fk> l = new LinkedList<>();
		for (Fk fk : fks.keySet()) {
			if (fks.get(fk).first.equals(en)) {
				l.add(fk);
			}
		}
		fksFrom.put(en, l);
		return l;
	}
	

	

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> fold(List<Fk> fks,
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> head) {
		for (Fk fk : fks) {
			head = Term.Fk(fk, head);
		}
		return head;
	}
	
	public static String truncate(String x, int truncate) {
		if (truncate == -1) {
			return x;
		}
		if (x.length() > truncate) {
			return x.substring(x.length() - truncate, x.length());
		}
		return x;	
	}

	static int constraint_static = 0;
	public Map<En, Triple<List<Chc<Fk, Att>>, List<String>, List<String>>> toSQL_srcSchemas(String prefix, String idTy, String idCol, int truncate, Function<Fk, String> fun) {
		Map<En, Triple<List<Chc<Fk, Att>>, List<String>, List<String>>> sqlSrcSchs = new HashMap<>();
		
	

		for (En en1 : ens) {
			List<String> l = new LinkedList<>();
			List<Chc<Fk, Att>> k = new LinkedList<>();
			l.add(idCol + " " + idTy + " primary key");
			List<String> f = new LinkedList<>();
			for (Fk fk1 : fksFrom(en1)) {
				l.add(		truncate(fun.apply(fk1), truncate) + " " + idTy + " not null ");
				k.add(Chc.inLeft(fk1));
				f.add("alter table " + 		truncate(prefix + en1, truncate) + " add constraint " + 		truncate(prefix + en1 + fk1 + constraint_static++, truncate) + 
						" foreign key (" + 		truncate(fun.apply(fk1), truncate) + ") references " + 		truncate(prefix + fks.get(fk1).second, truncate) + "(" + idCol + ")");
			}
			for (Att att1 : attsFrom(en1)) {
				//System.out.println("Doing att " + att1);
				l.add(		truncate(att1.toString(), truncate) + " " + SqlTypeSide.mediate(atts.get(att1).second.toString())); 
				k.add(Chc.inRight(att1));
			}
			String str = "create table " + prefix + en1 + "(" + Util.sep(l, ", ") + ")";
			List<String> q = new LinkedList<>();
			q.add("drop table if exists " + prefix + en1);
			q.add(str);
			sqlSrcSchs.put(en1, new Triple<>(k, q, f));
		}
		return sqlSrcSchs;
	}

	public Map<En, List<String>> toSQL_srcIdxs(Pair<Collection<Fk>, Collection<Att>> indices) {
		Map<En, List<String>> sqlSrcSchsIdxs = new HashMap<>();
		for (En en1 : ens) {
			List<String> x = new LinkedList<>();
			for (Fk fk1 : fksFrom(en1)) {
				if (indices.first.contains(fk1)) {
					x.add("create index " + en1 + fk1 + " on " + en1 + "(" + fk1 + ")");
				}
			}
			for (Att att1 : attsFrom(en1)) {
				// TODO skip those that can't be indexed - other, text, blob,
				if (indices.second.contains(att1)) {
					if (!cannotBeIndexed(atts.get(att1).second)) {
						x.add("create index " + en1 + att1 + " on " + en1 + "(" + att1 + ")");
					}
				}
			}
			sqlSrcSchsIdxs.put(en1, x);
		}
		return sqlSrcSchsIdxs;
	}

	private boolean cannotBeIndexed(Ty t) {
		String s = t.toString().toLowerCase();
		return s.equals("custom") || s.equals("text");
	}

}
