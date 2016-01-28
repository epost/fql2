package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class Patrick7Example extends Example {

	@Override
	public Language lang() {
		return Language.FPQL;
	}

	
	@Override
	public String getName() {
		return "Monad";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "Graph = schema {"
			+ "\n	nodes arrow,vertex;"
			+ "\n	edges src:arrow->vertex,tgt:arrow->vertex;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n//has 4 connected components"
			+ "\nG = instance {"
			+ "\n	variables "
			+ "\n		a b c d e : arrow,"
			+ "\n		t u v w x y z : vertex;"
			+ "\n	equations"
			+ "\n		a.src = t, b.src = t, c.src = x, d.src = y, e.src = z,"
			+ "\n		a.tgt = u, b.tgt = v, c.tgt = x, d.tgt = z, e.tgt = y;"
			+ "\n} : Graph"
			+ "\n"
			+ "\nTerminal = schema {"
			+ "\n	nodes X;"
			+ "\n	edges;"
			+ "\n	equations;"
			+ "\n} "
			+ "\n"
			+ "\nF = mapping {"
			+ "\n	nodes arrow->X, vertex->X;"
			+ "\n	edges src->X, tgt->X;"
			+ "\n} : Graph -> Terminal"
			+ "\n"
			+ "\n//has 4 rows"
			+ "\nComponents = sigma F G"
			+ "\n"
			+ "\n//puts 4 rows into vertex, 4 rows into arrow, corresponding to the connected components"
			+ "\nI = delta F Components"
			+ "\n"
			+ "\n//gives the transform from the original graph to the connected components"
			+ "\nt = return sigma delta F G"
			+ "\n"
			+ "\nu = coreturn sigma delta F Components"
			+ "\n";



}
