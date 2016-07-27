package catdata.opl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import catdata.Chc;
import catdata.Pair;
import catdata.ide.Util;
import catdata.opl.OplExp.OplInst;
import catdata.opl.OplExp.OplPres;
import catdata.opl.OplExp.OplPresTrans;
import catdata.opl.OplExp.OplPushout;
import catdata.opl.OplQuery.Block;

public class OplChase {

	static OplInst chase(OplInst I, List<OplQuery> EDs, int limit) {

		OplInst ret = I;
		for (int i = 0; i < limit; i++) {
			boolean changed = false;
			for (OplQuery ed : EDs) {
				OplInst ret2 = step(ret, ed);
				if (ret2 != null) {
					ret = ret2;
					changed = true;
				}
			}
			if (!changed) {
				return ret;
			}
		}
		
		throw new RuntimeException("Limit exceeded, last instance:\n\n" + ret);
		
	}
	
	
	//TODO this will fail because the schema will not have plain strings as entities, will be of the form Chc<S, String>
	static <S, C, V, X, Z> OplInst<S, C, V, Chc<X, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>> step(OplInst<S, C, V, X> I,
			OplQuery<S, C, V, String, String, V> Q) {
		if (!Q.blocks.containsKey("EXISTS")) {
			throw new RuntimeException("Need a block called EXISTS");
		}
		if (!Q.blocks.get("EXISTS").first.equals("EXISTS")) {
			throw new RuntimeException("EXISTS block must target EXISTS entity");
		}
		Block<S, C, V, String, String, V> EXISTS = Q.blocks.get("EXISTS").second;
		if (!EXISTS.edges.containsKey("THERE")) {
			throw new RuntimeException("EXISTS block must have FK called THERE");
		}
		if (!EXISTS.edges.get("THERE").first.equals("FORALL")) {
			throw new RuntimeException("THERE FK must target FORALL");
		}
		Map<V, OplTerm<C, V>> THERE = EXISTS.edges.get("THERE").second;

		if (!Q.blocks.containsKey("FORALL")) {
			throw new RuntimeException("Need a block called FORALL");
		}
		if (!Q.blocks.get("FORALL").first.equals("FORALL")) {
			throw new RuntimeException("FORALL block must target FORALL entity");
		}
		Block<S, C, V, String, String, V> FORALL = Q.blocks.get("FORALL").second;

		Pair<OplExp.OplInst<String,String,V,Chc<OplTerm<Chc<C,X>,V>,Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>>>,Map<Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>,Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>>>
		temp2 = Q.eval(I);
		OplInst<String,String,V,Chc<OplTerm<Chc<C,X>,V>,Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>>> 
		QI0 = temp2.first;
				
		//temp also contains active domains
		Map<String,Set<Chc<OplTerm<Chc<C,X>,V>,Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>>>> temp = Util.revS(QI0.P.gens);
		for (String s : QI0.S.entities) {
			if (!temp.containsKey(s)) {
				temp.put(s, new HashSet<>());
			}
		}
				
		Set<Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>> QIA = Chc.projIfAllRight(temp.get("FORALL"));
		Set<Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>> QIE = Chc.projIfAllRight(temp.get("EXISTS"));

		Map<Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>, Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>> QIm = new HashMap<>();
		for (Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>> back : QIE) {
			Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>> front = temp2.second.get(back);
			if (front == null) {
				throw new RuntimeException("Report to Ryan, no front for back. front=" + front + " back=" + back + " temp2=" + temp2.second);
			}
			QIm.put(back, front);
		}
		System.out.println("QIm=" + QIm);
		
		Set<Pair<Object,Map<V,OplTerm<Chc<C,X>,V>>>> T = new HashSet<>(QIA);
		T.removeAll(QIm.values());
		System.out.println("T=" + T);
		
		if (T.isEmpty()) {
			return null;
		}
		
		
		Map<Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, Integer> 
		Aprec = new HashMap<>(), Eprec = new HashMap<>();
		Map<Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, S> 
		Agens = new HashMap<>(), Egens = new HashMap<>();
		
		List<Pair<OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V>, OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V>>> 
		 Aeqs = new LinkedList<>(), Eeqs = new LinkedList<>();
		
		//
		
		Map<S, Map<Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, OplTerm<Chc<C, X>, V>>> 
		AImap = new HashMap<>();
		
		Map<S, Map<Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, 
		           OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V>>> 
		AEmap = new HashMap<>();
		
		for (S s : I.S.entities) {
			AImap.put(s, new HashMap<>());
			AEmap.put(s, new HashMap<>());
		}
		
		//are generators at type being lost? if so must change type of Agens, etc
		//should not use QIm here, should use frozen instance for m
		for (Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>> t : T) {
			System.out.println("t=" + t);	
			for (V v : Q.fI.get("FORALL").gens.keySet()) {
				S s = Q.fI.get("FORALL").gens.get(v);
				Agens.put(new Pair<>(t, v), s);
				AImap.get(s).put(new Pair<>(t, v), t.second.get(v));
				
				//TODO: equations, precedence
			}
			
			for (V v : Q.fI.get("EXISTS").gens.keySet()) {
				S s = Q.fI.get("EXISTS").gens.get(v);
				Egens.put(new Pair<>(t, v), s);
				//TODO: equations, precedence
			}
			
			//have trigger in A, need to transform into trigger in E
			
			for (V v : Q.fI.get("FORALL").gens.keySet()) {
				System.out.println("v=" + v);
				S s = Q.fI.get("FORALL").gens.get(v);

				OplTerm<Chc<C, X>, V> inDst = THERE.get(v).inLeft();
				System.out.println("THERE.get(v)=" + THERE.get(v));
			//	OplTerm<Chc<C, X>, V> t2x = inDst.subst(t.second);
			//	System.out.println("t2x=" + t2x + "[subst=" + t.second + "]");
				
				Function<OplTerm<Chc<C, X>, V>,  OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V>>
				 fun = new Function<OplTerm<Chc<C, X>, V>,  OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V>>() {

					@Override
					public OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V> apply(
							OplTerm<Chc<C, X>, V> term) {
						
						if (term.var != null) {
							Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V> p = new Pair<>(t, term.var);
							return new OplTerm<>(Chc.inRight(p), new LinkedList<>());
						}
						
						throw new RuntimeException(); //TODO
					}

					
				};
				
		        OplTerm<Chc<C, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>, V> 
		        	toAdd = fun.apply(inDst);
		        
				AEmap.get(s).put(new Pair<>(t, v), toAdd); //TODO				
			}

			


		}
		
		//
		
		OplPres<S, C, V, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>> 
		A0 = new OplPres<S, C, V, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>(Aprec, "?" , Q.src.sig, Agens, Aeqs), 
		E0 = new OplPres<S, C, V, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>(Eprec, "?" , Q.src.sig, Egens, Eeqs);
		A0.toSig(); E0.toSig();
		
		OplInst<S, C, V, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>>
		A = new OplInst<>("?", "?", "?"), E = new OplInst<>("?", "?", "?");
		A.validate(Q.src, A0, null); E.validate(Q.src, E0, null);
				
		OplPresTrans<S, C, V, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, X> AtoI 
		= new OplExp.OplPresTrans<>(AImap, "?", "?", A.P, I.P);
		AtoI.validateNotReally(A, I);
		AtoI.validateNotReally(A.P, I.P);
		AtoI.src1 = A; AtoI.dst1 = I;
		//
		
		OplPresTrans<S, C, V, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>> 
		AtoE = new OplPresTrans<>(AEmap, "?", "?", A.P, E.P);	
		AtoE.validateNotReally(A, E);
		AtoE.validateNotReally(A.P, E.P);
		AtoE.src1 = A; AtoE.dst1 = E; //why must do this manually?
		
		OplPushout<S,C,V,Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>, X, Pair<Pair<Object, Map<V, OplTerm<Chc<C, X>, V>>>, V>> 
		p = new OplPushout<>("?", "?");
		
		p.validate(AtoI, AtoE);
		return p.pushout().first;
	}

}
