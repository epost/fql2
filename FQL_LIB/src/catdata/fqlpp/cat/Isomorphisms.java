package catdata.fqlpp.cat;

import java.util.Optional;

import catdata.Pair;

public interface Isomorphisms<O,A> {

	public Optional<Pair<A,A>> iso(O o1, O o2);	

}

