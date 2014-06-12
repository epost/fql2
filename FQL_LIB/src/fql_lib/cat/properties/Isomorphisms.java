package fql_lib.cat.properties;

import java.util.Optional;

import fql_lib.Pair;

public interface Isomorphisms<O,A> {

	public Optional<Pair<A,A>> iso(O o1, O o2);	

}

