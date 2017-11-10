package catdata.aql.exp;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Ctx;
import catdata.Pair;
import catdata.Quad;
import catdata.Triple;
import catdata.aql.RawTerm;

public class LocStr {
	
	@Override
	public String toString() {
		return str;
	}

	public final Integer loc;
	public final String str;
	
	public LocStr(Integer loc, String str) {
		this.loc = loc;
		this.str = str;
	}

	public static Set<String> set1(Collection<LocStr> l) {
		return l.stream().map(x -> x.str).collect(Collectors.toSet());
	}
	
	public static Ctx<Pair<String, Object>, Pair<Integer, Function<Pair<String, Object>, String>>>
	imports(String section, Collection<LocStr> l, Function<Pair<String, Object>, String> f) {
		Ctx<Pair<String, Object>, Pair<Integer, Function<Pair<String, Object>, String>>> ret = new Ctx<>();
		for (LocStr s : l) {
			ret.put(new Pair<>(section,  s.str), new Pair<>(s.loc, f));
		}
		return ret;
	}
	
	public static Set<Pair<String, Pair<List<String>, String>>> functions1(
			Collection<Pair<LocStr, Pair<List<String>, String>>> f) {
		return f.stream().map(x -> new Pair<>(x.first.str, x.second)).collect(Collectors.toSet());
	}

	public static Set<Triple<List<Pair<String, String>>, RawTerm, RawTerm>> eqs1(
			Collection<Pair<Integer, Triple<List<Pair<String, String>>, RawTerm, RawTerm>>> eqs) {
		return eqs.stream().map(x -> x.second).collect(Collectors.toSet());
	}

	public static <X> Set<Pair<String, X>> set2(Collection<Pair<LocStr, X>> l) {
		return set2y(l, Function.identity()); //l.stream().map(x -> new Pair<>(x.first.str, x.second)).collect(Collectors.toSet());
	}
	
	public static <X> Set<Pair<String,Pair<String, X>>> set2x(Collection<Pair<LocStr, Pair<String, String>>> atts, Function<String, X> f) {
		return atts.stream().map(x -> new Pair<>(x.first.str, new Pair<>(x.second.first, f.apply(x.second.second)))).collect(Collectors.toSet());
	}
	
	public static <En,Y> Set<Pair<String, En>> set2y(Collection<Pair<LocStr, Y>> gens, Function<Y, En> f) {
		return gens.stream().map(x -> new Pair<>(x.first.str, f.apply(x.second))).collect(Collectors.toSet());
	}

	public static Set<Pair<String, Triple<List<String>, String, String>>> functions2(
			Collection<Pair<LocStr, Triple<List<String>, String, String>>> f) {
		return f.stream().map(x -> new Pair<>(x.first.str, x.second)).collect(Collectors.toSet());
	}
/*
	public static Set<Pair<String, Pair<String, String>>> functions3(List<Pair<LocStr, Pair<String, String>>> fks) {
		return fks.stream().map(x -> new Pair<>(x.first, x.second))
	}
	*//*

	public static Set<Pair<List<String>, List<String>>> eqs3(
			List<Pair<LocStr, Pair<List<String>, List<String>>>> p_eqs) {
		return p_eqs.stream().map(x -> x.second).collect(Collectors.toSet());
	}
*/
	public static Set<Quad<String, String, RawTerm, RawTerm>> eqs2(
			List<Pair<LocStr, Quad<String, String, RawTerm, RawTerm>>> t_eqs) {
		return t_eqs.stream().map(x -> x.second).collect(Collectors.toSet());
	}

	public static <X> Set<X> proj2(Collection<Pair<Integer, X>> eqs) {
		return eqs.stream().map(x -> x.second).collect(Collectors.toSet());
	}
	
	public static <X,Y> List<Pair<Pair<Y, String>, X>> list2x(List<Pair<Pair<Y,LocStr>, X>> as) {
		return as.stream().map(x -> new Pair<>(new Pair<>(x.first.first, x.first.second.str),x.second)).collect(Collectors.toList());
	}


	public static <X> List<Pair<String, X>> list2(List<Pair<LocStr, X>> as) {
		return as.stream().map(x -> new Pair<>(x.first.str, x.second)).collect(Collectors.toList());
	}
	
	public static <X,Y> List<Pair<Y, X>> list2(List<Pair<LocStr, X>> as, Function<String,Y> f) {
		return as.stream().map(x -> new Pair<>(f.apply(x.first.str), x.second)).collect(Collectors.toList());
	}
/*
	public static <X> List<Pair<String, X>> map2(List<Pair<LocStr, X>> nodes) {
		// TODO Auto-generated method stub
		return null;
	}
*/

	
	

}