package fql_lib.cat.categories;

import java.util.HashSet;
import java.util.Set;

import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.cat.Category;
import fql_lib.cat.Functor;
import fql_lib.cat.categories.FinSet.Fn;

public class Groth {

	/*
    Objects are pairs (A,a) where A \in \mathop{\rm Ob}(C) and a \in FA.
    An arrow (A,a) \to (B,b) is an arrow f: A \to B in C such that (Ff)a = b.
	 */
	
	public static <O,A,X> Category<Pair<O,X>,Triple<Pair<O,X>,Pair<O,X>,A>> pivot(Functor<O,A,Set<X>,Fn<X,X>> F) {
		
		Set<Pair<O, X>> objects = new HashSet<>();
		Set<Triple<Pair<O,X>, Pair<O,X>, A>> arrows = new HashSet<>();
		
		for (O o : F.source.objects()) {
			for (X x : F.applyO(o)) {
				objects.add(new Pair<>(o,x));
			}
		}
		for (Pair<O, X> s : objects) {
			for (Pair<O, X> t : objects) {
				for (A f : F.source.hom(s.first, t.first)) {
					X rhs = F.applyA(f).apply(s.second);
					X lhs = t.second;
					if (rhs.equals(lhs)) {
						arrows.add(new Triple<>(s, t, f));
					}
				}
			}	
		}
		
		return new Category<Pair<O,X>,Triple<Pair<O,X>,Pair<O,X>,A>>() {

			@Override
			public Set<Pair<O, X>> objects() {
				return objects;
			}

			@Override
			public Set<Triple<Pair<O, X>, Pair<O, X>, A>> arrows() {
				return arrows;
			}

			@Override
			public Pair<O, X> source(Triple<Pair<O, X>, Pair<O, X>, A> a) {
				return a.first;
			}

			@Override
			public Pair<O, X> target(Triple<Pair<O, X>, Pair<O, X>, A> a) {
				return a.second;
			}

			@Override
			public Triple<Pair<O, X>, Pair<O, X>, A> identity(Pair<O, X> o) {
				return new Triple<>(o, o, F.source.identity(o.first));
			}

			@Override
			public Triple<Pair<O, X>, Pair<O, X>, A> compose(Triple<Pair<O, X>, Pair<O, X>, A> a1,
					Triple<Pair<O, X>, Pair<O, X>, A> a2) {
				if (!target(a1).equals(source(a2))) {
					throw new RuntimeException("Dom/Cod mismatch on (groth): " + a1 + " and " + a2);
				}
				return new Triple<>(source(a1), target(a2), F.source.compose(a1.third, a2.third));
			}

			
		};
				
	}
	
}
