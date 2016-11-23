package catdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Naive (edge to edge) graph matching based on string distance .
 * 
 * @author ryan
 */
public class NaiveMatcher<N1,N2,E1,E2> extends Matcher<N1,E1,N2,E2,BiFunction<String,String,Double>> {
	
	public NaiveMatcher(DMG<N1, E1> src, DMG<N2, E2> dst, Map<String, String> options) {
		super(src, dst, options);
	}
	
	@Override
	public Match<N1, E1, N2, E2> bestMatch() {
		Map<N1, N2> nodes = new HashMap<>();
		Map<E1, List<E2>> edges = new HashMap<>();
		
		for (N1 s : src.nodes) {
			double max_d = -1;
			N2 max_t = null;
			for (N2 t : dst.nodes) {
				double cur_d = params.apply(s.toString(), t.toString());
				if (cur_d > max_d) {
					max_d = cur_d;
					max_t = t;
				}
			}
			if (max_t == null) {
				throw new RuntimeException("No match from " + s);
			}
			nodes.put(s, max_t);
		}
			
		for (E1 c : src.edges.keySet()) {
			double max_d = -1;
			E2 max_c = null;
			for (E2 d : dst.edges(nodes.get(src.edges.get(c).first), nodes.get(src.edges.get(c).second))) {
				double cur_d = params.apply(c.toString(), d.toString());
				if (cur_d > max_d) {
					max_d = cur_d;
					max_c = d;
				}
			}
			if (max_c == null) {
				throw new RuntimeException("No match from " + c + " under node mapping\n\n" + Util.sep(nodes, " -> ", "\n") );
			}
			edges.put(c, Util.singList(max_c));
		}
		
		return new Match<>(src, dst, nodes, edges);
	}

	@Override
	public BiFunction<String, String, Double> createParams(Map<String, String> options) {
		if (!options.isEmpty()) {
			throw new RuntimeException("No options allowed for naive matching");
		}
		return EditDistance::similarity;
	}

}
