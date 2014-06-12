package fql_lib.cat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import fql_lib.Pair;
import fql_lib.Quad;
import fql_lib.Util;
import fql_lib.cat.categories.FinSet;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.cat.presentation.FPTransform;
import fql_lib.cat.presentation.Instance;
import fql_lib.cat.presentation.Mapping;
import fql_lib.cat.presentation.Signature;

public class LeftKanSigma {
	
	@SuppressWarnings({ "rawtypes" , "unchecked"})
	public static <O1,A1,O2,A2>
	Quad<Functor<O2,A2,Set,Fn>, Transform<O1,A1,Set,Fn>, Transform<O2,A2,Set,Fn>,
	Map<Object, List<Pair<A2, Object>>>> 
	fullSigma(Functor<O1,A1,O2,A2> F, Functor<O1,A1,Set,Fn> I, Transform<O1,A1,Set,Fn> t, Functor<O2,A2,Set,Fn> JJJ)  { 
		
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

		Mapping<O1,A1,O2,A2> G = F.toMapping();
		
		Quad<Instance<O2,A2>, 
	     Map<Signature<O1,A1>.Node, Map<Object, Integer>>, 
	     Map<Signature<O2,A2>.Node, Map<Integer, Object>>, 
	     Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> q = fullSigmaWithAttrs(G, I.toInstance(), m, J, 100);
	
		Functor<Signature<O2, A2>.Node, Signature<O2, A2>.Path, Set, Fn> f2 = q.first.toFunctor();
		
		Functor<O2, A2, Set, Fn> f3 = new Functor<>(F.target, FinSet.FinSet, x -> f2.applyO(G.target.new Node(x)), a -> f2.applyA(G.target.path(G.target.getEdge(a))));
	
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
					rt.add(new Pair<>(fst.first.name, fst.second));
				}
			}
			nq.put(o.getKey(), rt);
		}
		
		return new Quad<>(f3, et, thr, nq);
	}


	public static <O1,A1,O2,A2>
	Quad<Instance<O2,A2>, 
	     Map<Signature<O1,A1>.Node, Map<Object, Integer>>, 
	     Map<Signature<O2,A2>.Node, Map<Integer, Object>>, 
	     Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> 
	fullSigmaWithAttrs(
			 Mapping<O1,A1,O2,A2> F, Instance<O1,A1> I, FPTransform<O1,A1> t, Instance<O2,A2> JJJ, Integer kkk)  {

		LeftKan<O1,A1,O2,A2> D = new LeftKan<>(kkk, F, I, t, JJJ);

		Pair<Instance<O2,A2>, Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> hhh = sigma(D);
		Map<Signature<O1,A1>.Node, Map<Object, Integer>> etables = new HashMap<>();
		for (Entry<Signature<O1, A1>.Node, Set<Pair<Object, Integer>>> n : D.ua.entrySet()) {
			etables.put(n.getKey(), Util.convert(n.getValue()));
		}
		
		Instance<O2,A2> j = hhh.first;
		return new Quad<> (j, etables, D.utables, hhh.second);
	}

	@SuppressWarnings({ "unchecked" })
	private static <O1,A1,O2,A2> Pair<Instance<O2,A2>, Map<Object, List<Pair<Signature<O2,A2>.Edge, Object>>>> sigma(
			LeftKan<O1,A1,O2,A2> lk)  {
		
		if (!lk.compute()) {
			throw new RuntimeException("Too many sigma iterations.");
		}

		Map<Signature<O2,A2>.Node, Set<Object>> nm = new HashMap<>();
		Map<Signature<O2,A2>.Edge, Map<Object, Object>> em = new HashMap<>();

		
		for (Signature<O2,A2>.Node e : lk.Pb.keySet()) {
			Set<Pair<Integer, Integer>> t = lk.Pb.get(e);
			nm.put(e, t.stream().map(x -> x.first).collect(Collectors.toSet()));
		}
		for (Signature<O2,A2>.Edge e : lk.Pg.keySet()) {
			Set<Pair<Integer, Integer>> t = lk.Pg.get(e);
			Map<Object, Object> m = (Map<Object,Object>) ((Object)Util.convert(t));
			em.put(e, m);
		}

		Instance<O2,A2> ret = new Instance<>(nm, em, lk.F.target);

		return new Pair<>(ret, lk.lineage);
	}

}
