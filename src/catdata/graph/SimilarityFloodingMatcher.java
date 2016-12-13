package catdata.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import catdata.Pair;
import catdata.Quad;
import catdata.Triple;
import catdata.graph.SimilarityFloodingMatcher.SimilarityFloodingParams;

/**
 * @author ryan, serena
 *
 *         Graph matching by similarity flooding
 *
 * @param <N1> type of source nodes
 * @param <N2> type of target nodes
 * @param <E1> type of source edges
 * @param <E2> type of target edges
 */
@SuppressWarnings("unused")
public class SimilarityFloodingMatcher<N1, N2, E1, E2> extends Matcher<N1, E1, N2, E2, SimilarityFloodingParams<N1, N2, E1, E2>> {

	// The four variations of the fixpoint formula from table 3
	public static enum Sigma {
		Basic, A, B, C;
	}


	/**
	 * Parameters to similarity flooding algorithm.
	 */
	
	public static class SimilarityFloodingParams<N1, N2, E1, E2> {
		//TODO note to Serena: add max iterations
		
		public final int max_iterations;
		
		/**
		 * A function assigning pairs of source edges and target edges to
		 * doubles. It's possible this may need a different signature, such as
		 * E1*E1 -> Double or E2*E2->Double
		 */
		public final BiFunction<E1, E2, Double> edgeComparator;

		/**
		 * If two edges compare to a number >= cutoff, they should be considered
		 * the same.
		 */
		public final Double cutoff;

		/**
		 * Which fixpoint formula to use (Table 3)
		 */
		public final Sigma sigma;

		public SimilarityFloodingParams(BiFunction<E1, E2, Double> edgeComparator, Double cutoff, Sigma sigma, int max_its) {
			this.edgeComparator = edgeComparator;
			this.cutoff = cutoff;
			this.sigma = sigma;
			this.max_iterations = max_its;
			//TODO note to serena - validate any options here, like this
			if (max_iterations < 0) {
				throw new RuntimeException("Expected max iterations to be >= 0");
			}
		}
	}

	public SimilarityFloodingMatcher(DMG<N1, E1> src, DMG<N2, E2> dst, Map<String, String> options) {
		super(src, dst, options);
	}

	@Override
	public SimilarityFloodingParams<N1, N2, E1, E2> createParams(Map<String, String> options) {
		if (!options.isEmpty()) {
			throw new RuntimeException("No options allowed for similarity flooding matching - yet");
		}
		return new SimilarityFloodingParams<>((x, y) -> x.toString().trim().equals(y.toString().trim()) ? 1.0 : 0.0, 1.0, Sigma.Basic, 5);  //note the use of trim
		//TODO note to serena, here is where the magic number 6 went.  see how we are collecting all the defaults in one place?
		
	}

	//////////////////////////////////////////////////////////////////

	// TODO note to Serena - do not use Util.similarity here.
	// The params contains the similarity function you are supposed to use (see line 136)
	//private double similarity(E1 e1, E2 e2) {
		//return Util.similarity(s1, s2);
	//}
	
	
	/**
	 * Constructs a pairwise connectivity graph (pcg)
	 * @param A the source graph
	 * @param B the target graph
	 * @return the pcg
	 */
	private DMG<Pair<N1,N2>,Pair<E1,E2>> pcg(DMG<N1, E1> A, DMG<N2, E2> B) {
		
		//initialize set of nodes for pcg
		Set<Pair<N1,N2>> nodes = new HashSet<>();
		for (N1 n1 : A.nodes) {
			for (N2 n2 : B.nodes) {
				nodes.add(new Pair<>(n1, n2));
			}
		}

		//initialize set of edges for pcg
		Set<Triple<Pair<E1,E2>,Pair<N1,N2>,Pair<N1,N2>>> edges = new HashSet<>();
		
		for (E1 e1 : A.edges.keySet()) {
			N1 s1 = A.edges.get(e1).first;
			N1 t1 = A.edges.get(e1).second;
			//e1 : s1 -> t1 is an edge in graph A
			
			for (E2 e2 : B.edges.keySet()) {
				N2 s2 = B.edges.get(e2).first;
				N2 t2 = B.edges.get(e2).second;
				//e2 : s2 -> t2 is an edge in graph B
				
				//if e1 ~ e2 according to the parameters
				if (params.edgeComparator.apply(e1, e2) >= params.cutoff) {
					//add (e1,e2) : (s1,s2) -> (t1,t2) to pcg
					Pair<N1,N2> s1s2 = new Pair<>(s1, s2);
					Pair<N1,N2> t1t2 = new Pair<>(t1, t2);
					Pair<E1,E2> e1e2 = new Pair<>(e1, e2);
					edges.add(new Triple<>(e1e2, s1s2, t1t2));
				}
			}
		}
		
		DMG<Pair<N1,N2>,Pair<E1,E2>> pcg = new DMG<>(nodes, edges);
		return pcg;
	}

	private static enum Direction {
		forward, backward;
	}
	/**
	 * Constructs an inducted propagation graph that has the same
	 * nodes as the pcg and for each edge
	 * 
	 * (e1,e2) : s1,s2 -> t1,t2 in the pcg 
	 * 
	 * two edges
	 * 
	 *  (forward, e1, e2, similarity(e1,e2)) : s1 -> s2  
	 *  (backward, e1, e2, similarity(e1,e2)) : s2 -> s1 (note reversal)
	 *  
	 *  in the ipg.
	 *  
	 * @param pcg a pairwise connectivity graph
	 * @return the induced propagation graph
	 */
	private DMG<Pair<N1,N2>,Quad<Direction,E1,E2,Double>> ipg(DMG<Pair<N1,N2>,Pair<E1,E2>> pcg) {
		
		//the nodes of the ipg are the same as the pcg
		Set<Pair<N1, N2>> nodes = new HashSet<>(pcg.nodes);
		
		//initialize the edges of the ipg
		Set<Triple<Quad<Direction,E1,E2,Double>, Pair<N1, N2>, Pair<N1, N2>>> edges = new HashSet<>();
		
		
		
		for (Pair<E1, E2> e : pcg.edges.keySet()) {
			Pair<N1, N2> s = pcg.edges.get(e).first; 
			Pair<N1, N2> t = pcg.edges.get(e).second;
			//e : s -> t is an edge in pcg
			
			E1 e1 = e.first;
			N1 s1 = s.first;
			N1 t1 = t.first;
			//e1 : s1 -> t1 is an edge in the pcg's A graph
			
			E2 e2 = e.second;
			N2 s2 = s.second;
			N2 t2 = t.second;
			//e2 : s2 -> t2 is an edge in the pcg's B graph
				
			Quad<Direction,E1,E2,Double> fwde1e2double  = new Quad<>(Direction.forward,  e1, e2, params.edgeComparator.apply(e1, e2)); //TODO note to serena - we used params again
			Quad<Direction,E1,E2,Double> bkwde1e2double = new Quad<>(Direction.backward, e1, e2, params.edgeComparator.apply(e1, e2)); //TODO note to serena - we used params again
			
			//add (e1, e2, similarity(e1,e2)) : s -> t to ipg
			edges.add(new Triple<>(fwde1e2double, s, t)); 
			
			//add (e1, e2, similarity(e1,e2)) : s -> t to ipg
			edges.add(new Triple<>(bkwde1e2double, t, s));
		}
		
		DMG<Pair<N1,N2>,Quad<Direction,E1,E2,Double>> ipg = new DMG<>(nodes, edges); 
		return ipg;
	}
	
	@Override
	public Match<N1, E1, N2, E2> bestMatch() {
		System.out.println("----------------------------");
		System.out.println("Starting bestMatch for \n " + src + "\n   ---->\n" + dst + "\n");
		
		//to get the best match, start by computing the pcg
		DMG<Pair<N1,N2>,Pair<E1,E2>> pcg = pcg(src, dst);
		System.out.println("pcg is " + pcg + "\n");
		
		//then compute the pig
		DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg = ipg(pcg);
		System.out.println("ipg is " + ipg + "\n");
		
		//TODO note to Serena, let's just do a case-statement of which sigma to use, rather
		//than try to pull out each sigma as a separate function.
		
		/////////////// fixpt computation

		// max iterations //TODO I have added this to the params class - please do this for all params
		//int maxit = 6;
		
		// set up the map holding the sigma values 
		Map<Pair<N1,N2>, Double> sigmap = new HashMap<>();
		
		Map<Integer, Double> sigmapInt = new HashMap<>(); // rep node as int index
		// initialize nodes and 1 value
		for (Pair<N1,N2> n: ipg.nodes) {
			sigmap.put(n, 1.0);
		}
		
		
		Map<Pair<N1,N2>, Double> sigmap_n = sigmap;
		DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n = ipg; 
		for (int i = 0; i < params.max_iterations; i++) { //things start at 0 in java
			sigmap_n = sigmaBasic(ipg, sigmap_n);
			//note to serena: please have this loop break when the algorithm has converged
			//note to serena: if the loop doesn't converge after max iterations, please throw an error
			
			//System.out.println("On iteration " + i + ", ipg_n+1 is " + ipg_np1 + "\n");
			
		}
		
		
		System.out.println("sigma is " + sigmap_n + "\n");

		// Print final sigma values 
		for (Pair<N1,N2> pn : sigmap_n.keySet()) {
			// String value= sigmap.get(pn).toString();  
	         //System.out.println(pn + " " + value);  
			System.out.println(pn.toString());
			System.out.printf("%f \n", sigmap_n.get(pn));
		}
//		Match<N1,E1,N2,E2> best = createMatchFromIpg(ipg_n, sigmap_n);
//		System.out.println("best match is " + best + "\n");
//		System.out.println("----------------------------");
		
//		return best;
		
		throw new RuntimeException("Todo");
	}

	//TODO serena notice how sigma(which) has type X -> X - this is how we know we can take its fixed point
	private Collection<Pair<Pair<N1,N2>, Double>> sigma(Sigma which, DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n) {
		switch (which) {
		case A:
			//return sigmaA(ipg_n);
		case B:
			//return sigmaB(ipg_n);
		case Basic:
			//return sigmaBasic(ipg_n);
		case C:
			//return sigmaC(ipg_n);
		default:
			throw new RuntimeException();
		}
	}

	//TODO serena each one of these should construct a new graph from its input graph
	private DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> sigmaC(DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n) {
		// TODO serena Auto-generated method stub
		throw new RuntimeException("Todo - serena");
	}

	/*
	private DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> sigmaBasic(DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n) {
		// TODO serena Auto-generated method stub
		throw new RuntimeException("Todo - serena");
	}
	*/

	private DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> sigmaB(DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n) {
		// TODO serena Auto-generated method stub
		throw new RuntimeException("Todo - serena");
	}

	private DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> sigmaA(DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Todo - serena");
	}

	
	// vector sigma functions 
	// also input map from nodes to an index 
	// save sigma values in a map
	private Map<Pair<N1,N2>, Double> sigmaBasic(DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n, Map<Pair<N1,N2>, Double> sigmap) {
		
		//note to serena: please use generics.  If you don't, nothing will ever compile or run correctly.
		Iterator<Entry<Pair<N1, N2>, Double>> sigIt = sigmap.entrySet().iterator();

		// temp sigma values 
		Map<Pair<N1, N2>, Double> tempsig = new HashMap<>();
		
		// max 
		Double maxsig = 0.0; 
		
		while (sigIt.hasNext()) {
			Entry<Pair<N1, N2>, Double> nex = sigIt.next();
			Double dnex = nex.getValue();
			Pair<N1,N2> pn1n2 =  nex.getKey();
			
			
			
			//note to serena: you should never need to cast anything.  If you are, 
			//it's a sign that something is wrong.
			//Pair<Pair<N1,N2>, Double> nex = (Pair<Pair<N1, N2>, Double>) sigIt.next();
			 //			Double dnex = nex.second;
			 //			Pair<N1,N2> pn1n2 =  nex.first;
			// note to serena: you can only call next() once per iteration through the loop.
			// more seriously, here you casting the next value in the iterator to a double,
			// but directly above here you casting the next value in the iterator to a pair.
			// the next value in the iterator can't be both a double and a pair, so this
			//code would not have worked.
			 //			double sig0 = (double) sigIt.next();
			
			// sum outgoing edges from this node by searching all forward n1n2 first entry or backward n1n2 sec entry
			// iterate over edges 
			double sig0 = dnex; //note to Serena: please read https://docs.oracle.com/javase/tutorial/java/data/autoboxing.html
			for (Quad<Direction, E1, E2, Double> e: ipg_n.edges.keySet() ) {
				Pair<N1, N2> p1 = ipg_n.edges.get(e).first;

				Pair<N1, N2> p2 = ipg_n.edges.get(e).second;
				
				Direction d = e.first;
				// forward with first entry matching
				if (d.equals(Direction.forward) && p1.equals(pn1n2)) {
					// add 
					sig0 = sig0 + sigmap.get(pn1n2)*e.fourth;
				} else if (d.equals(Direction.backward) && p2.equals(pn1n2)) {
					//add 
				
				
					//note to serena: if forward and backward are handled the same way, 
					//you don't to do an if/else
					sig0 = sig0 + sigmap.get(pn1n2)*e.fourth;
				}
				
			}
			tempsig.put(pn1n2, sig0);
			// save max double
			if (sig0 > maxsig) {
				maxsig = sig0;
			}
			
		}
		
		// must normalize sigma values 
		Iterator<Entry<Pair<N1, N2>, Double>> tempsigIt = tempsig.entrySet().iterator();
		
		Map<Pair<N1, N2>, Double> tempsig2 = new HashMap<>();

		
		while (tempsigIt.hasNext()) {
			Entry<Pair<N1, N2>, Double> nex = tempsigIt.next();
			Double dnex = nex.getValue();
			Pair<N1,N2> pn1n2 =  nex.getKey();
			tempsig2.put(pn1n2, dnex/maxsig);
		}

		
		return tempsig2;
	}
	
	////////////
	
	/**
	 * Constructs a graph morphism from an induced propaation graph.
	 * 
	 * @param ipg_n
	 * @return
	 */
	//also sigma val
	private Match<N1, E1, N2, E2> createMatchFromIpg(DMG<Pair<N1, N2>, Quad<Direction, E1, E2, Double>> ipg_n, Map<Pair<N1, N2>, Double> sig) {
		
		
		//return bestMatch;
		throw new RuntimeException("Todo - serena");
		
	}
	
	

}
