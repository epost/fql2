package catdata.graph;

import java.util.Map;
import java.util.function.BiFunction;

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
		return new SimilarityFloodingParams<>((x,y) -> Util.similarity(x.toString(),y.toString()), .25, Sigma.Basic);
	}

	//////////////////////////////////////////////////////////////////
	  
	@Override
	public Match<N1, E1, N2, E2> bestMatch() {
		//this is a test.  yes, yes it is
		
		throw new RuntimeException("TODO - serena"); //TODO: serena
	}
	
	
}
