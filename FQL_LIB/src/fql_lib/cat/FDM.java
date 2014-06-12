package fql_lib.cat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import fql_lib.Pair;
import fql_lib.Quad;
import fql_lib.Triple;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.cat.categories.Inst;
import fql_lib.cat.categories.Pi;


public class FDM {
	
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <O1, A1, O2, A2> Functor<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> deltaF(
			Functor<O2, A2, O1, A1> F) {

		Category<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> src = Inst.get(F.target);
		Category<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> dst = Inst.get(F.source);

		Function<Functor<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>> o = I -> Functor.compose(F, I);
		Function<Transform<O1, A1, Set, Fn>, Transform<O2, A2, Set, Fn>> a = f -> new Transform<>(
				o.apply(f.source), o.apply(f.target), d -> new Fn<>(o.apply(f.source).applyO(d), o
						.apply(f.target).applyO(d), i -> f.apply(F.applyO(d)).apply(i)));

		return new Functor<>(src, dst, o, a);
	}

	@SuppressWarnings({ "rawtypes" })
	public static <O1, A1, O2, A2> Functor<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> sigmaF(
			Functor<O1, A1, O2, A2> F) {

		Category<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> src = Inst.get(F.source);
		Category<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> dst = Inst.get(F.target);

		Function<Functor<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>> o = I -> LeftKanSigma
				.fullSigma(F, I, null, null).first;

		Function<Transform<O1, A1, Set, Fn>, Transform<O2, A2, Set, Fn>> a = t -> LeftKanSigma
				.fullSigma(F, t.source, Transform.compose(t,
						LeftKanSigma.fullSigma(F, t.target, null, null).second), o.apply(t.target)).third;

		return new Functor<>(src, dst, o, a);
	}

	// TODO: lineage assumes all IDs in output of sigma are unique

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <O1, A1, O2, A2> Adjunction<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> sigmaDelta(
			Functor<O2, A2, O1, A1> F) {

		Category<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> D = Inst.get(F.source);
		Category<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> C = Inst.get(F.target);
		Function<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> g0 = I -> {
			Functor<O2, A2, Set, Fn> deltad = deltaF(F).applyO(I);
			Quad<Functor<O1, A1, Set, Fn>, Transform<O2, A2, Set, Fn>, Transform<O1, A1, Set, Fn>, Map<Object, List<Pair<A1, Object>>>> q = LeftKanSigma
					.fullSigma(F, deltad, null, null);
			Functor<O1, A1, Set, Fn> sigmad = q.first;
			Map<Object, List<Pair<A1, Object>>> lineage = q.fourth;
			// System.out.println("lineage " + lineage);
			return new Transform<>(sigmad, I, n -> {
				Function<Object, Object> h = i -> {
					List<Pair<A1, Object>> l = lineage.get(i);
					Object ret = l.get(0).second;
					for (int e = 1; e < l.size(); e++) {
						Pair<A1, Object> le = l.get(e);
						A1 edge = le.first;
						ret = I.applyA(edge).apply(ret);
					}
					return ret;
				};

				return new Fn<>(sigmad.applyO(n), I.applyO(n), h);
			});
		};
		Transform<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>, Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> f = new Transform<>(
				Functor.compose(deltaF(F), sigmaF(F)), Functor.identity(C), g0);

		Function<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> f0 = I -> LeftKanSigma
				.fullSigma(F, I, null, null).second;
		Transform<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>, Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> g = new Transform<>(
				Functor.identity(D), Functor.compose(sigmaF(F), deltaF(F)), f0);

		return new Adjunction<>(sigmaF(F), deltaF(F), f, g);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static <O1, A1, O2, A2> Functor<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> piF(
			Functor<O1, A1, O2, A2> F) {

		Category<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> src = Inst.get(F.source);
		Category<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> dst = Inst.get(F.target);

		Function<Functor<O1, A1, Set, Fn>, Functor<O2, A2, Set, Fn>> o = I -> Pi.pi(F, I).first;

		Function<Transform<O1, A1, Set, Fn>, Transform<O2, A2, Set, Fn>> a = t -> Pi.pi(F, t);

		return new Functor<>(src, dst, o, a);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <O1, A1, O2, A2> Adjunction<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>, Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> deltaPi(
			Functor<O2, A2, O1, A1> F) {
		Category<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> D = Inst.get(F.source);
		Category<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> C = Inst.get(F.target);

		
		Function<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> f = I -> {
			Triple<Functor<O1,A1,Set,Fn>,Map<O1,Set<Map>>,Map<O1, Triple<O1,O2,A1>[]>> xxx = Pi.pi(F, Functor.compose(F, I));
			Function<O1, Fn> j = n -> {
				return new Fn<>(I.applyO(n), xxx.first.applyO(n), i -> {
					outer: for (Map m : xxx.second.get(n)) {
						for (int p = 1; p < m.size(); p++) {
							if (xxx.third.get(n)[p-1].third.equals(F.target.identity(n))) {
								if (!m.get(p).equals(i)) {
									continue outer;
								}
							}
						}
						return m.get(0);
					}
				throw new RuntimeException("Cannot find diagonal of " + i + " in " + xxx.second.get(n));
				}); 
			};
			return new Transform<>(I, xxx.first, j);
		};
		Transform<Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>, Functor<O1, A1, Set, Fn>, Transform<O1, A1, Set, Fn>> unit 
		 = new Transform<>(Functor.identity(C), Functor.compose(deltaF(F), piF(F)), f);

		Function<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> g =  I -> {
			Triple<Functor<O1,A1,Set,Fn>,Map<O1,Set<Map>>,Map<O1, Triple<O1,O2,A1>[]>> xxx = Pi.pi(F, I);
			Functor<O2,A2,Set,Fn> deltad = Functor.compose(F, xxx.first);
			Function<O2, Fn> j = m -> {
				O1 n = F.applyO(m);
				Triple<O1,O2,A1>[] col = xxx.third.get(n);
				Triple<O1,O2,A1> tofind = new Triple<>(n, m, F.target.identity(n));
				Set<Map> lim = xxx.second.get(n);
				int[] i = new int[] { 0 };
				for (Triple<O1,O2,A1> cand : col) {
					if (!cand.equals(tofind)) {
						i[0]++;
						continue;
					}
					Function h = id -> {
						for (Map row : lim) {
							if (row.get(0).equals(id)) {
								return row.get(i[0]+1);
							}
						}
						throw new RuntimeException("Report this error to Ryan.");
					};
					return new Fn<>(deltad.applyO(m), I.applyO(m), h);
				}
				throw new RuntimeException("Report this error to Ryan.");
			};
			return new Transform<>(deltad, I, j);
		};

		Transform<Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>, Functor<O2, A2, Set, Fn>, Transform<O2, A2, Set, Fn>> counit 
		 = new Transform<>(Functor.compose(piF(F), deltaF(F)), Functor.identity(D), g);

		return new Adjunction<>(deltaF(F), sigmaF(F), counit, unit);
	}
/*
 * 	for (Node m : f.source.nodes) {
						Node n = f.nm.get(m);
						Triple<Node, Node, Arr<Node, Path>>[] col = colmap
								.get(n.string);
						// System.out.println("n " + n);
						// System.out.println("m " + m);
						// System.out.println(Arrays.toString(col));

						Triple<Node, Node, Arr<Node, Path>> toFind = new Triple<>(
								n, m, new Arr<Node, Path>(
										new Path(f.target, n), n, n));
						int i = 0;
						boolean found = false;
						for (Triple<Node, Node, Arr<Node, Path>> cand : col) {
							if (cand.equals(toFind)) {
								found = true;
								Map<String, String> from = new HashMap<>();
								from.put("lim", middle + "_" + n + "_limit");
								LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
								select.put("c0", new Pair<>("lim", "guid"));
								select.put("c1", new Pair<>("lim", "c" + i));
								List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
								Flower flower = new Flower(select, from, where);
								ret.add(new SimpleCreateTable(xxx, PSM
										.VARCHAR(), false));
								ret.add(new InsertSQL(xxx, flower, "c0", "c1"));

								ret.add(new InsertSQL(
										env + "_" + m,
										PSMGen.compose(new String[] {
												e.inst + "_" + m + "_subst_inv",
												xxx }), "c0", "c1"));
								ret.add(new DropTable(xxx));
								break;
							}
							i++;
						}
						if (!found) {
							throw new RuntimeException();
						}
					}
 */
	
}
