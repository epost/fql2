package catdata.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import catdata.Pair;
import catdata.Util;
import catdata.graph.SimilarityFloodingMatcher.SimilarityFloodingParams;

/**
 * @author ryan, serena
 *
 * Graph matching by similarity flooding
 */
public class SimilarityFloodingMatcher<N1,N2,E1,E2> extends Matcher<N1,E1,N2,E2,SimilarityFloodingParams<N1,N2,E1,E2>> {
	
	//The four variations of the fixpoint formula from table 3
	public static enum Sigma {
		Basic, A, B, C;
	}
	
	// functions for the Sigma 
	/*
	Function<PCGDMG, double[]> basicSigma(int size) {
			double[] sig= new double[size];
			return sig;
	}
	*/
	
	public double[] basicSigma(int size, int maxit, Map<Pair<N1, N2>, List<WPEdge>> oedges, Map<String, List<WPEdge>> edges, Collection<Pair<N1, N2>> nodes, Map< Pair<Pair<N1, N2>, Pair<N1, N2>>, Double> propmap) {
		double[] sig= new double[size]; // size is size of nodes 
		double[] tempsig = new double[size];
		
		// initialize sig to 1s
		for (int i=1; i<= size; i++) {
			sig[i] = 1; 
		}
		
		
		
		for (int i=1; i<= maxit; i++) {
			// iterate through nodes 
			Iterator it = nodes.iterator();
			//Iterator kit = nodes.keySet().iterator();
			//Iterator eit = nodes.entrySet().iterator();
			
			while (it.hasNext()) {
				Pair<N1, N2> node = (Pair<N1, N2>) it.next(); // 
				//Pair<N1, N2> inc= (Pair<N1, N2>) eit.next(); // entries are inc pair
				
				// look at outgoing edges from this node
				List<WPEdge> olist = oedges.get(node); 
				Iterator olistit = olist.iterator();
				while (olistit.hasNext()) {
					WPEdge nx = (WPEdge) olistit.next();
					Pair<N1, N2> inode = nx.target;
					Pair<Pair<N1, N2>, Pair<N1, N2>> p = new Pair<Pair<N1, N2>,Pair<N1, N2>>(node, inode);
					// sum from outg edges
					double wout = propmap.get(p);
				}
			}
			
			for (int j=1; j<= size; j++ ) {
				double sig0 = sig[j];
				
				// look at edges incoming to j 
				for (int k=1; k<= size; k++) {
					
					 //double k= iedges.get()
					//sig0 = sig0 + sig[k]*gedges.get(key);
				}
				
			}
			
		}
		
		return sig;
	}
	
	public double[] ASigma(int size) {
		double[] sig = new double[size];
		return sig;
	}
		
	
	
	/**
	 * Parameters to similarity flooding algorithm.
	 */
	public static class SimilarityFloodingParams<N1,N2,E1,E2> {
		/**
		 * A function assigning pairs of source edges and target edges to doubles.
		 * It's possible this may need a different signature, such as E1*E1 -> Double or E2*E2->Double
		 */
		public final BiFunction<E1,E2,Double> edgeComparator;
		
		/**
		 * If two edges compare to a number > cutoff, they should be considered the same.
		 */
		public final Double cutoff;
		
		/**
		 * Which fixpoint formula to use (Table 3)
		 */
		public final Sigma sigma;
	
		public SimilarityFloodingParams(BiFunction<E1, E2, Double> edgeComparator, Double cutoff, Sigma sigma) {
			this.edgeComparator = edgeComparator;
			this.cutoff = cutoff;
			this.sigma = sigma;
		}		
	}
	
	public SimilarityFloodingMatcher(DMG<N1, E1> src, DMG<N2, E2> dst, Map<String, String> options) {
		super(src, dst, options);
	}

	@Override
	//TODO note to Serena: eventually, this is where we will convert options entered into the IDE
	//into parameters such as the cutoff, etc.  For the initial version, these will be
	//defaulted to the parameters defined here.  Note that similarity is of the form 1/n, so cutoff
	//must be small 
	public SimilarityFloodingParams<N1, N2, E1, E2> createParams(Map<String, String> options) {
		if (!options.isEmpty()) {
			throw new RuntimeException("No options allowed for similarity flooding matching - yet");
		}

		//return new SimilarityFloodingParams<>((x,y) -> Util.similarity(x.toString(),y.toString()), .25, Sigma.Basic);
		return new SimilarityFloodingParams<>((x,y) -> { if (x.equals(y)) { return 1.0; } else { return 0.0; } }, .9, Sigma.C);
		
	}

	//////////////////////////////////////////////////////////////////
	  
	// function similarity between edges
	private double similarity(String s1, String s2) {
		return Util.similarity(s1, s2);
	}
	
	
	// function for pairwise connect. graph
	private void PCGraph(DMG<N1, E1> src, DMG<N2, E2> dst) {
		// for each label matching (two edges compare to number > cutoff ) 
		// DMG<N, E> G = new DMG<N,E>(); 
		for (E1 e : src.edges.keySet()) {
			
			// name of the edge is get(e).first
	//		for (E2 t : dst.edges) {
				// compare e and t
	//			double score = similarity(e.toString(),t.toString());
	//		}
		}
	}
	
	
	@Override
	public Match<N1, E1, N2, E2> bestMatch() {
		//this is a test.  yes, yes it is
		//this
		//throw new RuntimeException("TODO - serena"); //TODO: serena
		int e1 = 10;
		int e2 = 10;
	    int es = e1*e2; // size of edges
	
	    double[] weights = new double[es]; 
	    
	    // nodes are a collection 
	    Collection<Pair<N1, N2>> pnodes = (Collection<Pair<N1, N2>>) new Pair<N1, N2>(null, null);
	    
		Map< Pair<N1, N2>, Pair<N1, N2>> pcgnodes = new HashMap<>(); // out pair A,B and inc pair A,B
		
		Map<String, List<WPEdge>> pcgedges = new HashMap<>();
		
		Map<Pair<N1, N2>, List<WPEdge>> outedges = new HashMap<>(); // map where key is the node pair connected to outg edge

		Map<Pair<N1, N2> , List<WPEdge>> incedges = new HashMap<>(); // map where key is node pair connected to inc edge 
		

		//DMG<Pair<N1,N2>, Pair<E1, E2>> pwcg = new DMG<Pair<N1,N2>, Pair<E1, E2>>();
		PCGDMG pwcg = new PCGDMG((Collection) pcgnodes, pcgedges);
		
		// new hash map that hold the amount of edges outgoing from certain node 
		Map<Pair<N1,N2>, Integer > outedgenum = new HashMap<>();
	
		
		// hash map for the weights in the propagation graph
		Map< Pair<Pair<N1, N2>, Pair<N1, N2>>, Double> propmap = new HashMap<>(); 
		
		 
		for (E1 c : src.edges.keySet()) { // c in keySet is the edge label  
			//int min_d = Integer.MAX_VALUE;
			//E2 min_c = null;
			N1 n2_s = this.src.edges.get(c).first;
			N1 n2_t = this.src.edges.get(c).second;
	
			
			for (E2 d : dst.edges.keySet()) { // iterate through labels in the dest graph 
				//int cur_d = params.apply(c.toString(), d.toString());
				double score = similarity(c.toString(), d.toString());
				
				N2 n3_s = this.dst.edges.get(d).first;
				N2 n3_t = this.dst.edges.get(d).second;
				
				if (score > this.params.cutoff) {
					// create new pair of edges from outg , incoming
					Pair np1 = new Pair<>(n2_s, n3_s);
					Pair np2 = new Pair<>(n2_t, n3_t);
					
					// check if nodes contains these, and if not add
					if (pnodes.contains(np1)) {
					
					} else {
						pnodes.add(np1);
					}
					
					if (pnodes.contains(np2)) {
						
					} else {
						pnodes.add(np2);
					}
					
					pcgnodes.put(np1,np2); // outg , inc 
					
					Pair<Pair<N1, N2>, Pair<N1, N2>> nn = new Pair<>(np1, np2); // outg , inc 
						
					WPEdge wp = new WPEdge(np1, np2, c.toString(), 0); 
					
					// add to edges 
					if (pcgedges.containsKey(c.toString())) {
						// add to linked list of WPEdges
						List<WPEdge> list = pcgedges.get(c.toString());
						list.add(wp);
						pcgedges.put(c.toString(), list);
						incedges.put(np2, list); 
						// not sure if this is needed
						outedges.put(np1, list);
						
					} else {
						// is this correct way to initialize linked list with entry 
						List<WPEdge> lis = new LinkedList<WPEdge>();
						lis.add(wp);
						pcgedges.put(c.toString(), lis);
						incedges.put(np2, lis);
						// not sure if this is needed
						outedges.put(np1, lis);
					}
					
					
					// add to map outedgenum, check if there was the node 
					if (outedgenum.containsKey(np1)) {
						// increment the weight
						int g = outedgenum.get(np1);
						outedgenum.put(np1, g+1);
						
					} else {
						outedgenum.put(np1, 1);
					}
					
					if (outedgenum.containsKey(np2)) {
						// increment the weight
						int g = outedgenum.get(np2);
						outedgenum.put(np2, g+1);
					} else {
						outedgenum.put(np2, 1);
					}
					
						
					
				}
				
			}
			
			
		}
		
		
		
		
		// create the propagation graph with outedgenum
		Iterator it = pcgedges.entrySet().iterator();
		while (it.hasNext()) {
			WPEdge w = (WPEdge) it.next(); 
			
			// look for source node, and locate the number of outgoing edges
			Pair<N1, N2> sn = w.source;
			int wgt = outedgenum.get(sn.first);
			// set the weight
			double wt = 1/wgt;
			w.weight = wt; // set the weight of current edge 
			
			Pair<N1, N2> dn = w.target;
			// add to the propmap
			
			Pair< Pair<N1, N2>, Pair<N1, N2>> vpair1 = new Pair< Pair<N1, N2>, Pair<N1, N2>>(sn,dn);
			
			propmap.put(vpair1, wt);
			
			// check the reverse edge and see if there is an edge , if not, add reverse edge with weight 1
			
			
			
			// reverse edge
			Pair< Pair<N1, N2>, Pair<N1, N2>> vpair = new Pair< Pair<N1, N2>, Pair<N1, N2>>(dn,sn);
			
			//propmap.put(vpair, wt);
			
			WPEdge wp = new WPEdge(dn, sn, w.name, 1);
			
			if (pcgedges.containsKey(dn)) {
				List<WPEdge> list = pcgedges.get(dn);
				if (list.contains(sn)) {
					// don't need to add edge with wt 1 
				} else {
					// add edge to list with wt 1 
 					list.add(wp);
 					propmap.put(vpair, 1.0);
					pcgedges.put(w.name, list);
					// also add to out/ inc maps
					outedges.put(sn, list); 
					incedges.put(dn, list);
				}
			} else { // doesnt have edge with reverse edge 
				List<WPEdge> lis = new LinkedList<WPEdge>();
				lis.add(wp);
				pcgedges.put(w.name, lis);
				propmap.put(vpair, 1.0);
				// also add to out/ inc maps
				outedges.put(sn, lis);
				incedges.put(dn, lis); 
			}
			
			
		}
		
		// fixpt computation 
		 
		// max iterations 
		int maxit = 6; 
		
		Sigma sig = Sigma.Basic;
		for (int i=1; i<= maxit; i++) {
			// for each of nodes 
			
		}
		
		
		
		return bestMatch;
}
	
}
