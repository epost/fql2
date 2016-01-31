package fql_lib.fqlpp.cat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.algs.Pair;
import catdata.algs.Quad;
import fql_lib.fqlpp.cat.FinSet.Fn;
import fql_lib.ide.Util;

public class LeftKanSigma {
	/*
	public static <O1,A1,O2,A2>
	Quad<Functor<Signature<O2,A2>.Node,Signature<O2,A2>.Path,Set,Fn>, 
	     Transform<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Set,Fn>, 
	     Transform<Signature<O2,A2>.Node,Signature<O2,A2>.Path,Set,Fn>,
	Map<Object, List<Pair<Signature<O2, A2>.Path, Object>>>> 
	fastFullSigma(Functor<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Signature<O2,A2>.Node,Signature<O2,A2>.Path> F, 
			  Functor<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Set,Fn> I, 
			  Transform<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Set,Fn> t, 
			  Functor<Signature<O2,A2>.Node,Signature<O2,A2>.Path,Set,Fn> JJJ)  {
		//TODO: factor out injection
		//Mapping<Signature<O1, A1>.Node, Signature<O1, A1>.Path, Signature<O2, A2>.Node, Signature<O2, A2>.Path> G = F.toMappingX(F.source.origin, F.target.origin);
		Mapping<Signature<O1, A1>.Node, Signature<O1, A1>.Path, Signature<O2, A2>.Node, Signature<O2, A2>.Path> G = F.toMappingZ(F.source.origin.inject(), F.target.origin.inject());

		FPTransform<Signature<O1, A1>.Node, Signature<O1, A1>.Path> m = null;
		if (t != null) {
			m = t.toFPTransform();
		}
		Instance<Signature<O2, A2>.Node, Signature<O2, A2>.Path> J = null;
		if (JJJ != null) {
			J = JJJ.toInstanceX(F.target.origin.inject());
		}
		
	/*	Quad<Instance<O2,A2>, 
	     Map<Signature<O1,A1>.Node, Map<Object, Object>>, 
	     Map<Signature<O2,A2>.Node, Map<Object, Object>>, 
	     Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> */
		/*Quad<Instance<Signature<O2,A2>.Node,Signature<O2,A2>.Path>,
		Map<Signature<Signature<O1,A1>.Node,Signature<O1,A1>.Path>.Node,
		Map<Object,Object>>,
		Map<Signature<Signature<O2,A2>.Node,Signature<O2,A2>.Path>.Node,Map<Object,Object>>,Map<Object,List<Pair<Signature<Signature<O2,A2>.Node,Signature<O2,A2>.Path>.Edge,Object>>>> 
		 q = fullSigmaOnPresentation(G, I.toInstanceX(F.source.origin.inject()), m, J, 100);
		
		Map<Signature<O2,A2>.Node, Set<Object>> map2 = new HashMap<>();
		for (Entry<Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Node, Set<Object>> k : q.first.nm.entrySet()) {
			map2.put(k.getKey().name, k.getValue());
		}	
		Map<Signature<O2,A2>.Edge, Map<Object,Object>> map = new HashMap<>();
		for (Entry<Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Edge, Map<Object, Object>> k : q.first.em.entrySet()) {
			Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Edge key = k.getKey();
			Signature<O2, A2>.Path name = key.name;
			if (name.path.size() != 1) {
				throw new RuntimeException("Fast left kan is breaking things. Report to Ryan.");
			}
			map.put(name.path.get(0), k.getValue());
		}
		System.out.println("map is " + map);
		Instance<O2,A2> III = new Instance<>(map2, map, F.target.origin);
		
	
//		Functor<Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Edge>.Node, Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Edge>.Path, Set, Fn> f2 = q.first.toFunctor();
		
//		Functor<Signature<O2,A2>.Node,Signature<O2,A2>.Edge, Set, Fn> f3 = new Functor<>(F.target, FinSet.FinSet, x -> f2.applyO(G.target.new Node(x)), a -> f2.applyA(G.target.path(G.target.getEdge(a))));
//System.out.println(q.first.nm);
		
		Functor<Signature<O2,A2>.Node,Signature<O2,A2>.Path, Set, Fn> f3 = new Functor<Signature<O2,A2>.Node,Signature<O2,A2>.Path, Set, Fn>
		(F.target, FinSet.FinSet, x -> {
		//	System.out.println("input object " + x);
		//	System.out.println("new node " + G.target.new Node(x));
		//	System.out.println("returning " + q.first.nm.get(G.target.new Node(x)));
			return q.first.nm.get(G.target.new Node(x));
			}, new Function<Signature<O2,A2>.Path, Fn>()  {
				@Override
				public Fn apply(Signature<O2,A2>.Path a) {
					if (a.path.size() == 0) {
						return Fn.id(q.first.nm.get(G.target.new Node(a.source)));
					}
//					return III.evaluate(a);
//					Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Edge k = G.target.getEdge(a);
	//				Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Path v = q.first.thesig.path(k);
		//			Map m = q.first.evaluate(v);
					return new Fn(q.first.nm.get(G.target.new Node(a.source)), 
					              q.first.nm.get(G.target.new Node(a.target)),
					             III.evaluate(a)::get); // map.get(a)::get);
//					Object k = a.path.stream().map(x -> q.first.thesig.getEdge(G.target.getEdge(x))).collect(Collectors.toList());
//					String p = q.first.thesig.path(G.target.new Node(a.source), G.target.getEdge(a));
				//	Signature<Signature<O2,A2>.Node, Signature<O2,A2>.Path>.Path p = q.first.thesig.path(a.source, a.path.stream().map(x -> q.first.thesig.getEdge(G.target.getEdge(x)).collect(Collectors.toList())));
				//	return null; //new Fn(q.first.nm.get(G.target.new Node(a.source)), 
					        //q.first.nm.get(G.target.new Node(a.target)), 
					       // q.first.evaluate(p)::get); //q.first.thesig.path(q.first.thesig.getEdge(a)))::get); 
//					throw new RuntimeException(t.getClass().toString()); 
				} 
				}); 
/*			System.out.println("input arrow is " + a);
			System.out.println("get edge is " + G.target.getEdge(a));
			System.out.println("em says " + q.first.em.get(G.target.getEdge(a)));
			return new Fn<>(q.first.nm.get(G.target.new Node(a.source)), 
					        q.first.nm.get(G.target.new Node(a.target)), 
					        q.first.em.get(G.target.getEdge(a))::get); 
		} */ //);


		/* Functor<O2, A2, Set, Fn> f3 = new Functor<>(F.target, FinSet.FinSet, x -> f2.applyO(G.target.new Node(x)), 
			a -> { 
			//	q.first.
				//List<A2> l = new LinkedList<>();
				//l.add(a);
				//if (F.target.isId(a)) {
				//	return FinSet.FinSet.identity(f2.applyO(G.target.new Node(F.target.source(a))));
				//} else {
				//	return f2.applyA(G.target.path(G.target.getEdge(a)));
				///	return f2.applyA(G.target.path(F.target.source(a), l));
				//}
				//G.target.
			}
		); */
	//	q.first.
	/*
		//use third
		Transform<Signature<O2,A2>.Node,Signature<O2,A2>.Path,Set,Fn> thr = null;
		if (t != null) {
			thr = new Transform<>(f3, JJJ, x -> new Fn<>(f3.applyO(x), JJJ.applyO(x), q.third.get(G.target.new Node(x))::get));
		}
		
		Transform<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Set,Fn> et = new Transform<>(I, Functor.compose(F,f3), x -> new Fn<>(I.applyO(x), Functor.compose(F,f3).applyO(x), q.second.get(G.source.new Node(x))::get));
		
		Map<Object, List<Pair<Signature<O2, A2>.Path, Object>>> nq = new HashMap<>();
		
		//System.out.println("*********");
		//System.out.println(q.fourth);
		for (Entry<Object, List<Pair<Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Edge, Object>>> o : q.fourth.entrySet()) {
			//System.out.println(o.getValue());
			List<Pair<Signature<O2, A2>.Path, Object>> rt = new LinkedList<>();
		//	 fst = o.getValue().get(0);
		//	rt.add(new Pair<>( F.source.identity(((Signature<O1,A1>.Node)fst.first).name), fst.second));
			for (int j = 0; j < o.getValue().size(); j++) {
				Pair<Signature<Signature<O2, A2>.Node, Signature<O2, A2>.Path>.Edge, Object> fst = o.getValue().get(j);
			//	System.out.println(fst.first);
				if (fst.first == null) {
					rt.add(new Pair<>(null, ((Pair)fst.second).second)); //because no guids, have pairs as first
				} else {
					rt.add(new Pair<>(fst.first.name, fst.second )); //TODO
				}
			}
			nq.put(o.getKey(), rt);
		}
		
		return new Quad<>(f3, et, thr, nq);
		
	}	*/
	/*
	public static Quad<Functor, Transform, Transform, Map> xxx(Mapping G, Instance I, Transform t, Instance J) {
		Quad<Instance<Object,Object>,Map<Signature<Object,Object>.Node,Map<Object,Object>>,Map<Signature<Object,Object>.Node,Map<Object,Object>>,Map<Object,List<Pair<Signature<Object,Object>.Edge,Object>>>>
		q = fullSigmaOnPresentation(G, I, t != null ? t.toFPTransform() : null, J, 100);

		Functor F = G.toFunctor();
		Functor<Signature<Object, Object>.Node, Signature<Object, Object>.Path, Set, Fn> f2 = q.first.toFunctor();
		
		Functor<Object, Object, Set, Fn> f3 = new Functor(F.target, FinSet.FinSet, x -> f2.applyO((Node)x), a -> {
			
		if (F.target.isId(a)) {
			return Fn.id(f2.applyO((Node)F.target.source(a)));
		} else {
			return f2.applyA((Path)a); //G.target.path(G.target.getEdge(a)));
		}
		} );
	
		//use third
		Transform<?,?,Set,Fn> thr = null;
		if (t != null) {
			thr = new Transform<>(f3, J.toFunctor(), x -> new Fn<>((Set)f3.applyO(x), (Set)J.toFunctor().applyO(x), q.third.get(G.target.new Node(x))::get));
		}
		
		Transform<Object,Object,Set,Fn> et = new Transform<>(I.toFunctor(), Functor.compose(F,f3), x -> new Fn((Set)I.toFunctor().applyO(x), (Set)Functor.compose(F,f3).applyO(x), q.second.get((Node)x)::get));
		
		Map<Object, List<Pair<Object, Object>>> nq = new HashMap<>();
		
		//System.out.println("*********");
		//System.out.println(q.fourth);
		for (Entry<Object, List<Pair<Signature<Object, Object>.Edge, Object>>> o : q.fourth.entrySet()) {
			//System.out.println(o.getValue());
			List<Pair<Object, Object>> rt = new LinkedList<>();
		//	 fst = o.getValue().get(0);
		//	rt.add(new Pair<>( F.source.identity(((Signature<O1,A1>.Node)fst.first).name), fst.second));
			for (int j = 0; j < o.getValue().size(); j++) {
				Pair<Signature<Object, Object>.Edge, Object> fst = o.getValue().get(j);
			//	System.out.println(fst.first);
				if (fst.first == null) {
					rt.add(new Pair<>(null, ((Pair)fst.second).second)); //because no guids, have pairs as first
				} else {
					rt.add(new Pair<>(fst.first.name, fst.second )); //TODO
				}
			}
			nq.put(o.getKey(), rt);
		}
		
		return new Quad<>(f3, et, thr, nq);
	} */
	
	@SuppressWarnings({ "rawtypes" , "unchecked"})
	public static <O1,A1,O2,A2>
	Quad<Functor<O2,A2,Set,Fn>, Transform<O1,A1,Set,Fn>, Transform<O2,A2,Set,Fn>,
	Map<Object, List<Pair<A2, Object>>>> 
	fullSigma(Functor<O1,A1,O2,A2> F, Functor<O1,A1,Set,Fn> I, Transform<O1,A1,Set,Fn> t, Functor<O2,A2,Set,Fn> JJJ)  {
		Mapping<O1,A1,O2,A2> G = F.toMapping();

	//	if (F.mapping0 != null && I.instance0 != null && t == null && JJJ == null) {
		//	return (Quad<Functor<O2,A2,Set,Fn>, Transform<O1,A1,Set,Fn>, Transform<O2,A2,Set,Fn>,
			//		Map<Object, List<Pair<A2, Object>>>> ) ((Object)xxx(F.mapping0, I.instance0, null, null));
		//}
		
		/*if (F.source.origin != null && F.target.origin != null) {
			return (Quad<Functor<O2, A2, Set, Fn>, Transform<O1, A1, Set, Fn>, Transform<O2, A2, Set, Fn>, Map<Object, List<Pair<A2, Object>>>>) 
					((Object)fastFullSigma((Functor<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Signature<O2,A2>.Node,Signature<O2,A2>.Path>)F, 
					(Functor<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Set,Fn>) I, 
					(Transform<Signature<O1,A1>.Node,Signature<O1,A1>.Path,Set,Fn>) t, 
					(Functor<Signature<O2,A2>.Node,Signature<O2,A2>.Path,Set,Fn>) JJJ));
		}  */
		
		FPTransform<O1,A1> m = null;
		if (t != null) {
			m = t.toFPTransform();
		}
		Instance<O2,A2> J = null;
		if (JJJ != null) {
			J = JJJ.toInstance();
		}
		
//		 System.out.println("Start mapping " + F + " on instance " + I);
	//	 System.out.println("Next mapping" + F.toMapping() + " on instance " + I.toInstance());
		// System.out.println("Final mapping" + F.toMapping().toFunctor() + " on instance " + I.toInstance().toFunctor());

		
		Quad<Instance<O2,A2>, 
	     Map<Signature<O1,A1>.Node, Map<Object, Object>>, 
	     Map<Signature<O2,A2>.Node, Map<Object, Object>>, 
	     Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> q = fullSigmaOnPresentation(G, I.toInstance(), m, J, 100);
	
		Functor<Signature<O2, A2>.Node, Signature<O2, A2>.Path, Set, Fn> f2 = q.first.toFunctor();
		
		Functor<O2, A2, Set, Fn> f3 = new Functor<>(F.target, FinSet.FinSet, x -> f2.applyO(G.target.new Node(x)), a -> {
			
		if (F.target.isId(a)) {
			return Fn.id(f2.applyO(G.target.new Node(F.target.source(a))));
		} else {
			return f2.applyA(G.target.path(G.target.getEdge(a)));
		}
		} );
	
		//use third
		Transform<O2,A2,Set,Fn> thr = null;
		if (t != null) {
			thr = new Transform<>(f3, JJJ, x -> new Fn<>(f3.applyO(x), JJJ.applyO(x), q.third.get(G.target.new Node(x))::get));
		}
		
		Transform<O1,A1,Set,Fn> et = new Transform<>(I, Functor.compose(F,f3), x -> new Fn<>(I.applyO(x), Functor.compose(F,f3).applyO(x), q.second.get(G.source.new Node(x))::get));
		
		Map<Object, List<Pair<A2, Object>>> nq = new HashMap<>();
		
		//System.out.println("*********");
		//System.out.println(q.fourth);
		for (Entry<Object, List<Pair<Signature<O2, A2>.Edge, Object>>> o : q.fourth.entrySet()) {
			//System.out.println(o.getValue());
			List<Pair<A2, Object>> rt = new LinkedList<>();
		//	 fst = o.getValue().get(0);
		//	rt.add(new Pair<>( F.source.identity(((Signature<O1,A1>.Node)fst.first).name), fst.second));
			for (int j = 0; j < o.getValue().size(); j++) {
				Pair<Signature<O2, A2>.Edge, Object> fst = o.getValue().get(j);
			//	System.out.println(fst.first);
				if (fst.first == null) {
					rt.add(new Pair<>(null, ((Pair)fst.second).second)); //because no guids, have pairs as first
				} else {
					rt.add(new Pair<>(fst.first.name, fst.second )); //TODO
				}
			}
			nq.put(o.getKey(), rt);
		}
		
		return new Quad<>(f3, et, thr, nq);
	}


	public static <O1,A1,O2,A2>
	Quad<Instance<O2,A2>, 
	     Map<Signature<O1,A1>.Node, Map<Object, Object>>, 
	     Map<Signature<O2,A2>.Node, Map<Object, Object>>, 
	     Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> 
	fullSigmaOnPresentation(
			 Mapping<O1,A1,O2,A2> F, Instance<O1,A1> I, FPTransform<O1,A1> t, Instance<O2,A2> JJJ, Integer kkk)  {

		LeftKan<O1,A1,O2,A2> D = new LeftKan<>(kkk, F, I, t, JJJ);

		Pair<Instance<O2,A2>, Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> hhh = sigma(D);
		Map<Signature<O1,A1>.Node, Map<Object, Object>> etables = new HashMap<>();
		for (Entry<Signature<O1, A1>.Node, Set<Pair<Object, Object>>> n : D.ua2.entrySet()) {
			etables.put(n.getKey(), Util.convert(n.getValue()));
		}
		
		Instance<O2,A2> j = hhh.first;
		return new Quad<> (j, etables, D.utables2, hhh.second);
	} 

	@SuppressWarnings({ "unchecked" })
	private static <O1,A1,O2,A2> Pair<Instance<O2,A2>, Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> sigma(
			LeftKan<O1,A1,O2,A2> lk)  {
		
		if (!lk.compute()) {
			throw new RuntimeException("Too many sigma iterations.");
		}

		Map<Signature<O2,A2>.Node, Set<Object>> nm = new HashMap<>();
		Map<Signature<O2,A2>.Edge, Map<Object, Object>> em = new HashMap<>();

		
		for (Signature<O2,A2>.Node e : lk.Pb2.keySet()) {
			Set<Pair<Object, Object>> t = lk.Pb2.get(e);
			nm.put(e, t.stream().map(x -> x.first).collect(Collectors.toSet()));
		}
		for (Signature<O2,A2>.Edge e : lk.Pg2.keySet()) {
			Set<Pair<Object, Object>> t = lk.Pg2.get(e);
			Map<Object, Object> m = (Map<Object,Object>) ((Object)Util.convert(t));
			em.put(e, m);
		}

		Instance<O2,A2> ret = new Instance<>(nm, em, lk.F.target);

		return new Pair<>(ret, lk.lineage2);
	}

}
