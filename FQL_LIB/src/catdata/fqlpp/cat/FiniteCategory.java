package catdata.fqlpp.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import catdata.algs.Pair;


@SuppressWarnings("serial")
public class FiniteCategory<O, A> extends Category<O, A> {

	public Set<O> objects = new HashSet<>();
	public Set<A> arrows = new HashSet<>();
	public Map<A, O> sources = new HashMap<>();
	public Map<A, O> targets = new HashMap<>();
	public Map<Pair<A, A>, A> composition = new HashMap<>();
	public Map<O, A> identities = new HashMap<>();

	/**
	 * Empty Category
	 */
	public FiniteCategory() {
	}
	
	public FiniteCategory(Set<O> objects, Set<A> arrows, Map<A, O> sources,
			Map<A, O> targets, Map<Pair<A, A>, A> composition,
			Map<O, A> identities) {
		this.objects = objects;
		this.arrows = arrows;
		this.sources = sources;
		this.targets = targets;
		this.composition = composition;
		this.identities = identities;
		//System.out.println(this);
		validate();
	}




	


	/**
	 * Singleton category
	 * 
	 * @param o
	 *            the object
	 * @param a
	 *            the identity arrow
	 */
	public FiniteCategory(O o, A a) {
		objects.add(o);
		arrows.add(a);
		composition.put(new Pair<>(a, a), a);
		sources.put(a, o);
		targets.put(a, o);
		identities.put(o, a);
	}

	/**
	 * Creates a new category, does not copy inputs.
	 */ /*
	public FinCat(
			List<Obj> objects,
			List<Arr<Obj, Arrow>> arrows,
			Map<Pair<Arr<Obj, Arrow>, Arr<Obj, Arrow>>, Arr<Obj, Arrow>> composition,
			Map<Obj, Arr<Obj, Arrow>> identities) {
		noDupes(objects);
		noDupes(arrows);
		this.objects = objects;
		this.arrows = arrows;
		this.composition = composition;
		this.identities = identities;
		if (DEBUG.debug.VALIDATE) {
			validate();
		}
	} */

/*	private <X> void noDupes(List<X> X) {
		Set<X> x = new HashSet<X>(X);
		if (x.size() != X.size()) {
			throw new RuntimeException("duplicates " + X);
		}
	} */



/*	public Arr<Obj, Arrow> id(Obj o) {
		return identities.get(o);
	} */
/*
	Map<Pair<Obj,Obj>, Set<Arr<Obj, Arrow>>> cached = new HashMap<>();
	public Set<Arr<Obj, Arrow>> hom(Obj A, Obj B) {
		Pair<Obj,Obj> p = new Pair<>(A,B);
		Set<Arr<Obj, Arrow>> retX = cached.get(p);
		if (retX != null) {
			return retX;
		}
		if (!objects.contains(A)) {
			throw new RuntimeException(A.toString() + " not in " + objects);
		}
		if (!objects.contains(B)) {
			throw new RuntimeException(B.toString() + " not in " + objects);
		}
		Set<Arr<Obj, Arrow>> ret = new HashSet<>();
		for (Arr<Obj, Arrow> a : arrows) {
			if (a.src.equals(A) && a.dst.equals(B)) {
				ret.add(a);
			}
		}
		cached.put(p, ret);
		return ret;
	} */
/*
	public Arr<Obj, Arrow> compose(Arr<Obj, Arrow> a, Arr<Obj, Arrow> b) {
		return composition.get(new Pair<>(a, b));
	}

	public boolean isId(Arr<Obj, Arrow> a) {
		return identities.containsValue(a);
	} */

	
	/*
	public Quad<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>, Pair<Map<Attribute<Obj>, String>, Map<String, Attribute<Obj>>>> toSig(Map<String, Type> types)
			throws FQLException {

		Map<Attribute<Obj>, String> attM = new HashMap<>();
		Map<String, Attribute<Obj>> attM2 = new HashMap<>();
		int ax = 0;

		List<String> objs = new LinkedList<>();
		List<Triple<String, String, String>> attrs0 = new LinkedList<>();

		int i = 0;
		Map<String, Obj> objM = new HashMap<>();
		Map<Obj, String> objM2 = new HashMap<>();
		for (Obj o : objects) {
			objM2.put(o, "obj" + i);
			objM.put("obj" + i, o);
			objs.add("obj" + i);
			i++;
		}

		if (attrs != null) {
			for (Attribute<Obj> att : attrs) {
				attM.put(att, "attrib" + ax);
				attM2.put("attrib" + ax, att);
				attrs0.add(new Triple<>("attrib" + ax++, objM2.get(att.source),
						att.target.toString()));
			}
		}

		List<Triple<String, String, String>> arrs = new LinkedList<>();
		int j = 0;
		Map<String, Arr<Obj, Arrow>> arrM = new HashMap<>();
		Map<Arr<Obj, Arrow>, String> arrM2 = new HashMap<>();
		for (Arr<Obj, Arrow> a : arrows) {
			if (isId(a)) {
				continue;
			}
			arrM.put("arrow" + j, a);
			arrM2.put(a, "arrow" + j);
			arrs.add(new Triple<>(arrM2.get(a), objM2.get(a.src), objM2
					.get(a.dst)));
			j++;
		}

		// System.out.println(objM);
		// System.out.println(arrM);

		// for (Arr<Obj, Arrow> a : this.arrows) {
		// // System.out.println("arrow a is " + a);
		//
		// }

		// System.out.println("arrows are " + arrows);
		LinkedList<Pair<List<String>, List<String>>> eqs = new LinkedList<>();
		for (Pair<Arr<Obj, Arrow>, Arr<Obj, Arrow>> k : composition.keySet()) {
			Arr<Obj, Arrow> v = composition.get(k);

			String s = arrM2.get(k.first);
			String t = arrM2.get(k.second);
			String u = arrM2.get(v);

			String ob = objM2.get(v.src);

			List<String> lhs = new LinkedList<>();
			List<String> rhs = new LinkedList<>();
			lhs.add(ob);
			rhs.add(ob);
			if (s != null) {
				lhs.add(s);
			}
			if (t != null) {
				lhs.add(t);
			}
			if (u != null) {
				rhs.add(u);
			}
			if (!lhs.equals(rhs)) {
				eqs.add(new Pair<>(lhs, rhs));
			}
		}

		// System.out.println("$$$$$$$$$$$$$$$$$$$$$");
		// System.out.println(this);
		// System.out.println(objM);
		// System.out.println(objM2);
		// System.out.println(arrM);
		// System.out.println(arrM2);
		// System.out.println(attM2);
		// System.out.println(attM);
		// System.out.println(eqs);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$");

		Signature ret2 = new Signature(types, objs, attrs0, arrs, eqs);

		// System.out.println(ret2);

		Quad<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>, Pair<Map<Attribute<Obj>, String>, Map<String, Attribute<Obj>>>> retret = new Quad<>(
				ret2, new Pair<>(objM2, objM), new Pair<>(arrM2, arrM),
				new Pair<>(attM, attM2));
		return retret;
	}
*/


	@Override
	public Set<O> objects() {
		return objects;
	}

	@Override
	public Set<A> arrows() {
		return arrows;
	}

	@Override
	public O source(A a) {
		return sources.get(a);
	}

	@Override
	public O target(A a) {
		return targets.get(a);
	}

	@Override
	public A identity(O o) {
		return identities.get(o);
	}

	@Override
	public A compose(A a1, A a2) {
		A ret = composition.get(new Pair<>(a1, a2));
		if (ret == null) {
			throw new RuntimeException("Cannot compose " + a1 + " and " + a2 + " in " + this);
		}
		return ret;
	}

}
