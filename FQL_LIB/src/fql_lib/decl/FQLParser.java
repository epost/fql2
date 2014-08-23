package fql_lib.decl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;

import fql_lib.Chc;
import fql_lib.Pair;
import fql_lib.Triple;
import fql_lib.Unit;
import fql_lib.cat.categories.FinSet.Fn;
import fql_lib.decl.FQLProgram.NewDecl;
import fql_lib.decl.FunctorExp.CatConst;
import fql_lib.decl.FunctorExp.FinalConst;
import fql_lib.decl.FunctorExp.InstConst;
import fql_lib.decl.FunctorExp.MapConst;
import fql_lib.decl.FunctorExp.SetSetConst;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FQLParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "?" };

	static String[] res = new String[] {  "set", "function", "category", "functor", "range",
		    "not", "and", "or", "implies", "return", "coreturn", "uncurry", "pushout",
			 "match",  "objects", "cod", "dom", "apply", "on", "object", "arrow", "left", "right", "whisker",
			 "transform",  "arrows", "Set", "Cat", "kleisli", "cokleisli",
			"equations", "id", "delta", "sigma", "pi", "eval", "in", "path", "union",
			"relationalize",  "fst", "forall", "exists", "tt", "ff", "APPLY",
			"snd", "inl", "inr", "curry",  "void", "unit", "CURRY", "pivot", "unpivot",
			"prop", "iso1", "iso2", "true", "false", "char", "kernel" };

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);

	public static final Parser<Void> IGNORED = Parsers.or(Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	public static final Parser<?> TOKENIZER = Parsers.or(
			(Parser<?>) Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			RESERVED.tokenizer(), (Parser<?>) Terminals.Identifier.TOKENIZER,
			(Parser<?>) Terminals.IntegerLiteral.TOKENIZER);

	static Parser<?> term(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<?> ident() {
		return Terminals.Identifier.PARSER;
	}

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	public static final Parser<?> program() {
		return Parsers
				.or(    Parsers.tuple(setDecl().source().peek(), setDecl()),
						Parsers.tuple(fnDecl().source().peek(), fnDecl()),
						Parsers.tuple(catDecl().source().peek(), catDecl()),
						Parsers.tuple(ftrDecl().source().peek(), ftrDecl()),
						Parsers.tuple(transDecl().source().peek(), transDecl())
						).many();
/*
		return Parsers
				.or(    Parsers.tuple(schemaDecl().source().peek(), schemaDecl()),
						Parsers.tuple(instanceDecl().source().peek(),instanceDecl()),
						Parsers.tuple(mappingDecl().source().peek(),
								mappingDecl()),
						Parsers.tuple(enumDecl().source().peek(), enumDecl()),
						Parsers.tuple(fullQueryDecl().source().peek(),
								fullQueryDecl()),
						Parsers.tuple(queryDecl().source().peek(), queryDecl()),
						Parsers.tuple(transDecl().source().peek(), transDecl()),
						Parsers.tuple(dropDecl().source().peek(), dropDecl()))
				.many(); */
	}
	
	public static final Object toValue(Object o) {
		if (o.toString().equals("true")) {
			return true;
		}
		if (o.toString().equals("false")) {
			return false;
		}
		if (o instanceof Tuple5) {
			Tuple5 t = (Tuple5) o;
			if (t.a.toString().equals("(")) {
				return new Pair(toValue(t.b), toValue(t.d));
			}
			List l = (List) t.b;
			Map s = new HashMap();
			for (Object y : l) {
				Pair yy = (Pair) toValue(y);
				if (s.containsKey(yy.first)) {
					throw new RuntimeException("Duplicate domain entry in " + o);
				}
				s.put(yy.first, yy.second); 
			}
			Tuple3 tt= (Tuple3) t.e;
			Set ui = (Set) toValue(tt.a);
			Set uj = (Set) toValue(tt.c);
			return new Fn(ui, uj, s::get);
		}
		if (o instanceof Tuple3) {
			Tuple3 p = (Tuple3) o;
			List l = (List) p.b;
			Set s = new HashSet();
			for (Object y : l) {
				s.add(toValue(y));
			}
			return s;
		}
		if (o instanceof org.codehaus.jparsec.functors.Pair) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			if (p.a.toString().equals("inl")) {
				return Chc.inLeft(toValue(p.b));
			} else if (p.a.toString().equals("inr")) {
				return Chc.inRight(toValue(p.b));
			} else {
				return new Unit();
			}
		}
		return o.toString();
	}
	
	public static final Parser<?> value() {
		Reference ref = Parser.newReference();

		Parser<?> p1 = Parsers.tuple(term("inl"), ref.lazy());
		Parser<?> p2 = Parsers.tuple(term("inr"), ref.lazy());
		Parser<?> p3 = Parsers.tuple(term("("), term(")"));
		Parser<?> p4 = Parsers.tuple(term("("), ref.lazy(), term(","), ref.lazy(), term(")"));
		Parser<?> p5 = term("true");
		Parser<?> p6 = term("false");
		Parser<?> p7 = Parsers.tuple(term("{"), ref.lazy().sepBy(term(",")), term("}"));
		Parser<?> xxx = Parsers.tuple(p7, term("->"), p7);
		Parser<?> p8 = Parsers.tuple(term("{"), p4.sepBy(term(",")), term("}"), term(":"), xxx);
		
//		Parser<?> op = Parsers.tuple(term("opposite"), ref.lazy());

		Parser<?> a = Parsers.or(new Parser<?>[] { string(), p1, p2, p3, p4, p5, p6, p8, p7 });

		ref.set(a);

		return a;
	}

	public static final Parser<?> set() {
		Reference ref = Parser.newReference();

		Parser<?> app = Parsers.tuple(term("apply"), ident(), term("on"), term("object"), ref.lazy());
		
		Parser<?> plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser<?> prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser<?> expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));
		Parser<?> k = Parsers.tuple(term("cod"), ident());
		Parser<?> v = Parsers.tuple(term("dom"), ident());
		Parser<?> u = Parsers.tuple(term("range"), ident());
		Parser<?> a = Parsers.or(new Parser<?>[] { term("void"), term("unit"), term("prop"),
				 plusTy, prodTy, expTy, k, v, u,
				ident(), setConst(), Terminals.IntegerLiteral.PARSER, app  });

		ref.set(a);

		return a;
	}
	
	public static final Parser<?> cat() {
		Reference ref = Parser.newReference();

		Parser<?> plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser<?> prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser<?> expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));
		
		Parser<?> k = Parsers.tuple(term("cod"), ident());
		Parser<?> v = Parsers.tuple(term("dom"), ident());
		
		Parser<?> kleisli  = Parsers.tuple(term("kleisli"), ident(), ident(), ident());
		Parser<?> cokleisli= Parsers.tuple(term("cokleisli"), ident(), ident(), ident());
		
		Parser<?> union = Parsers.tuple(term("union"), ref.lazy(), ref.lazy());
		
		Parser<?> a = Parsers.or(new Parser<?>[] { term("void"), term("unit"), 
				 plusTy, prodTy, expTy, k, v,
				ident() , catConst(), term("Cat"), term("Set"), kleisli, cokleisli, union });

		ref.set(a);

		return a;
	}
	
	public static final Parser<?> catConst() {
		Parser<?> p1 = ident();
		Parser<?> pX = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				ident());
		Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		Parser<?> foo = Parsers.tuple(section("objects", p1), 
				section("arrows", pX),
				section("equations", p3));
		return Parsers.between(term("{"), foo, term("}"));
	}
/*
	public static final Parser<?> enumDecl() {
		return Parsers.tuple(term("enum"), ident(), term("="), Parsers.between(
				term("{"), string().sepBy(term(",")), term("}")));
	}

	public static final Parser<?> queryDecl() {
		return Parsers.tuple(term("query"), ident(), term("="), query());
	}

	public static final Parser<?> fullQueryDecl() {
		return Parsers.tuple(term("QUERY"), ident(), term("="), fullQuery());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> query() {
		Reference ref = Parser.newReference();

		Parser p1 = Parsers.tuple(term("delta"), mapping());
		Parser p2 = Parsers.tuple(term("pi"), mapping());
		Parser p3 = Parsers.tuple(term("sigma"), mapping());
		Parser comp = Parsers.tuple(term("("), ref.lazy(), term("then"),
				ref.lazy(), term(")"));
		Parser zzz = Parsers.tuple(ident(), term(","), ident());
		Parser yyy = Parsers.between(term("("), zzz, term(")"));
		Parser xxx = Parsers
				.between(term("{"), yyy.sepBy(term(",")), term("}"));
		Parser mtch = Parsers.tuple(term("match"), xxx, schema(), schema(),
				Terminals.StringLiteral.PARSER);
		Parser ret = Parsers.or(Parsers.tuple(p1, p2, p3), comp, ident(), mtch);

		ref.set(ret);

		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> fullQuery() {
		Reference ref = Parser.newReference();

		Parser p1 = Parsers.tuple(term("delta"), mapping());
		Parser p2 = Parsers.tuple(term("pi"), mapping());
		Parser p3 = Parsers.tuple(term("SIGMA"), mapping());
		Parser comp = Parsers.tuple(term("("), ref.lazy(), term("then"),
				ref.lazy(), term(")"));
		Parser zzz = Parsers.tuple(ident(), term(","), ident());
		Parser yyy = Parsers.between(term("("), zzz, term(")"));
		Parser xxx = Parsers
				.between(term("{"), yyy.sepBy(term(",")), term("}"));
		Parser mtch = Parsers.tuple(term("match"), xxx, schema(), schema(),
				Terminals.StringLiteral.PARSER);
		Parser ret = Parsers.or(p1, p2, p3, comp, ident(), mtch);

		ref.set(ret);

		return ret;
	}
*/
	public static final Parser<?> setDecl() {
		return Parsers.tuple(term("set"), ident(), term("="), set());
	}

	public static final Parser<?> setConst() {
		return Parsers.tuple(term("{"), value().sepBy(term(",")), term("}"));
	}

//	private static int unknown_idx = 0;
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final SetExp toSet(Object o) {
		try {
			Tuple5 t = (Tuple5) o;
//		Parser<?> app = Parsers.tuple(term("apply"), ident(), term("on"), term("object"), ref.lazy());
			String f = t.b.toString();
			SetExp e = toSet(t.e);
			return new SetExp.Apply(f, e);
		} catch (Exception e) { }
		
		try {
			int i = Integer.parseInt(o.toString());
			return new SetExp.Numeral(i);
		} catch (Exception e) { }
		
		try {
			Tuple3<?, ?, ?> t = (Tuple3<?, ?, ?>) o;
			String y = t.b.toString();
			if (y.equals("+")) {
				return new SetExp.Plus(toSet(t.a), toSet(t.c));
			} else if (y.equals("*")) {
				return new SetExp.Times(toSet(t.a), toSet(t.c));
			} else if (y.equals("^")) {
				return new SetExp.Exp(toSet(t.a), toSet(t.c));
			} else if (t.a.toString().equals("{")) {
				List tb = (List) t.b;
				Set x = new HashSet();
				for (Object uu : tb) {
					x.add(toValue(uu));
				}
				return new SetExp.Const(x);
			}
		} catch (RuntimeException cce) {
		}

		try {
			org.codehaus.jparsec.functors.Pair<?, ?> p = (org.codehaus.jparsec.functors.Pair<?, ?>) o;
			if (p.a.toString().equals("dom")) {
				return new SetExp.Dom(toFn(p.b));
			} else if (p.a.toString().equals("cod")) {
				return new SetExp.Cod(toFn(p.b));
			} else if (p.a.toString().equals("range")) {
				return new SetExp.Range(toFn(p.b));
			}
		} catch (RuntimeException cce) {
		}
		
		try {
			if (o.toString().equals("void")) {
				return new SetExp.Zero();
			} else if (o.toString().equals("unit")) {
				return new SetExp.One();
			} else if (o.toString().equals("prop")) {
				return new SetExp.Prop();
			}
			
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		return new SetExp.Var(o.toString()); 
	}

	public static final CatExp.Const toCatConst(Object y) {
		Set<String> nodes = new HashSet<>();
		Set<Triple<String, String, String>> arrows = new HashSet<>();
		Set<Pair<Pair<String, List<String>>, Pair<String, List<String>>>> eqs = new HashSet<>();

		Tuple3 s = (Tuple3) y;

		Tuple3 nodes0 = (Tuple3) s.a;
		Tuple3 arrows0 = (Tuple3) s.b;
		Tuple3 eqs0 = (Tuple3) s.c;

		List nodes1 = (List) nodes0.b;
		List arrows1 = (List) arrows0.b;
		List eqs1 = (List) eqs0.b;

		for (Object o : nodes1) {
			nodes.add((String) o);
		}

		for (Object o : arrows1) {
			Tuple5 x = (Tuple5) o;
			arrows.add(new Triple<>((String) x.a, (String) x.c, (String) x.e));
		}
		for (Object o : eqs1) {
			Tuple3 x = (Tuple3) o;
			List<String> l1 = (List<String>) x.a;
			List<String> l2 = (List<String>) x.c;
			String xxx = l1.remove(0);
			String yyy = l2.remove(0);
			eqs.add(new Pair<>(new Pair<>(xxx, l1), new Pair<>(yyy, l2)));
		}
		CatExp.Const c = new CatExp.Const(nodes, arrows, eqs);
		return c;
	}

	public static final CatExp toCat(Object o) {
		 if (o.toString().equals("Set")) {
			return new CatExp.Named("Set");
		} 
		if (o.toString().equals("Cat")) {
			return new CatExp.Named("Cat");
		} 
		
		if (o instanceof Tuple4) {
			Tuple4 t = (Tuple4) o;
			return new CatExp.Kleisli(t.b.toString(), t.c.toString(), t.d.toString(), t.a.toString().equals("cokleisli"));
		}
		
		try {
			Tuple3<?, ?, ?> t = (Tuple3<?, ?, ?>) o;
			String y = t.b.toString();
			if (y.equals("+")) {
				return new CatExp.Plus(toCat(t.a), toCat(t.c));
			} else if (y.equals("*")) {
				return new CatExp.Times(toCat(t.a), toCat(t.c));
			} else if (y.equals("^")) {
				return new CatExp.Exp(toCat(t.a), toCat(t.c));
			} else if (t.a.toString().equals("union")) {
				return new CatExp.Union(toCat(t.b), toCat(t.c));
			}
			else {
				return toCatConst(o);
			}
		} catch (RuntimeException cce) {
		}
		
		try {
			org.codehaus.jparsec.functors.Pair<?, ?> p = (org.codehaus.jparsec.functors.Pair<?, ?>) o;
			if (p.a.toString().equals("dom")) {
				return new CatExp.Dom(toFtr(p.b));
			} else if (p.a.toString().equals("cod")) {
				return new CatExp.Cod(toFtr(p.b));
			} 
		} catch (RuntimeException cce) {
		}

		try {
			if (o.toString().equals("void")) {
				return new CatExp.Zero();
			} else if (o.toString().equals("unit")) {
				return new CatExp.One();
			} 		
		} catch (RuntimeException cce) {
		}

		return new CatExp.Var(o.toString()); 
	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final SetExp toSetConst(Object y) {
		return new SetExp.Const((Set)toValue(y));
	}


	public static final Parser<?> transDecl() {
		return Parsers.tuple(term("transform"), ident(), term("="), trans());
	}
	
	public static final Parser<?> fnDecl() {
		return Parsers.tuple(term("function"), ident(), term("="), fn());
	}
	
	public static final Parser<?> ftrDecl() {
		return Parsers.tuple(term("functor"), ident(), term("="), ftr());
	}
	
	public static final Parser<?> catDecl() {
		return Parsers.tuple(term("category"), ident(), term("="), cat());
	}
	
	public static final Parser<?> trans() {
		Reference ref = Parser.newReference();

		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term(";"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser sumTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		
		//////
		Parser<?> node = Parsers.tuple(ident(), term("->"), fn());
		Parser s2 = Parsers.tuple(ftr(), term(":"), term("Set"), term("->"), term("Set"));
		Parser s1 = Parsers.between(term("("), s2, term(")"));
		Parser<?> setset = Parsers
				.tuple(Parsers.between(term("{"), section2("objects", node), term("}")), term(":"),
						s1, term("->"), s1);
		/////
		Parser<?> node2 = Parsers.tuple(ident(), term("->"), fn().or(setOfPairs()));

		Parser x2 = Parsers.tuple(ftr(), term(":"), cat(), term("->"), term("Set"));
		Parser x1 = Parsers.between(term("("), x2, term(")"));
		Parser<?> toset = Parsers
				.tuple(Parsers.between(term("{"), section("objects", node2), term("}")), term(":"),
						x1, term("->"), x1);
		/////
		Parser nd =  Parsers.tuple(ident(), term("->"), ftr());
		Parser t2 = Parsers.tuple(ftr(), term(":"), cat(), term("->"), term("Cat"));
		Parser t1 = Parsers.between(term("("), t2, term(")"));
		Parser<?> tocat = Parsers
				.tuple(Parsers.between(term("{"), section("objects", nd), term("}")), term(":"),
						t1, term("->"), t1);
		/////
		
		Parser edX =  Parsers.tuple(ident(), term("->"), ref.lazy());
		Parser uip = term("Cat");
		Parser piu = term("Set");
		Parser u2X = Parsers.tuple(ftr(), term(":"), cat(), term("->"), Parsers.between(term("("), Parsers.tuple(uip.or(piu), term("^"), cat()), term(")")));
		Parser u1X = Parsers.between(term("("), u2X, term(")"));
		Parser<?> tofinal = Parsers
				.tuple(Parsers.between(term("{"), section("objects", edX), term("}")), term(":"),
						u1X, term("->"), u1X); 
		/////
		Parser ed =  Parsers.tuple(ident(), term("->"), path());
		Parser u2 = Parsers.tuple(ftr(), term(":"), cat(), term("->"), cat());
		Parser u1 = Parsers.between(term("("), u2, term(")"));
		Parser<?> tomap = Parsers
				.tuple(Parsers.between(term("{"), section("objects", ed), term("}")), term(":"),
						u1, term("->"), u1);
		

		
		Parser a = Parsers.or(new Parser[] {   
			ident(),
			Parsers.tuple(term("id"), ftr()), tofinal,
			compTy, setset, toset, tocat, tomap,
			Parsers.tuple(term("tt"), ref.lazy()),
			Parsers.tuple(term("not"), cat()),
			Parsers.tuple(term("and"), cat()),
			Parsers.tuple(term("or"), cat()),
			Parsers.tuple(term("implies"), cat()),
			Parsers.tuple(term("char"), ref.lazy()),
			Parsers.tuple(term("kernel"), ref.lazy()),
			Parsers.tuple(term("fst"), ftr(), ftr()),
			Parsers.tuple(term("snd"), ftr(), ftr()),
			Parsers.tuple(term("ff"), ref.lazy()),
			Parsers.tuple(term("curry"), ref.lazy()),
			Parsers.tuple(term("CURRY"), ref.lazy()),
			Parsers.tuple(term("return"), term("sigma"), term("delta"), ftr()),
			Parsers.tuple(term("coreturn"), term("sigma"), term("delta"), ftr()),
			Parsers.tuple(term("return"), term("delta"), term("pi"), ftr()),
			Parsers.tuple(term("coreturn"), term("delta"), term("pi"), ftr()),
			Parsers.tuple(term("apply"), ftr(), term("on"), term("arrow"), ref.lazy()),
			Parsers.tuple(term("apply"), ftr(), term("on"), term("path"), Parsers.tuple(path(), term("in"), cat())),
			Parsers.tuple(term("APPLY"), ref.lazy(), term("on"), ident()),
			Parsers.tuple(term("apply"), ref.lazy(), term("on"), ftr()),
			Parsers.tuple(term("left"), term("whisker"), ftr(), ref.lazy()),
			Parsers.tuple(term("right"), term("whisker"), ftr(), ref.lazy()),
			Parsers.tuple(term("inl"), ftr(), ftr()),
			Parsers.tuple(term("inr"), ftr(), ftr()),
			Parsers.tuple(term("eval"), ftr(), ftr()),
			Parsers.tuple(term("iso1"), ftr(), ftr()),
			Parsers.tuple(term("iso2"), ftr(), ftr()),
			Parsers.tuple(term("true"), cat()),
			Parsers.tuple(term("false"), cat()),
			prodTy, sumTy
		});
		
		ref.set(a);
		return a;
	}
	

	public static final Parser<?> fn() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term(";"), ref.lazy()), term(")"));

		Parser<?> app2 = Parsers.tuple(term("apply"), ident(), set());
		Parser<?> app = Parsers.tuple(term("apply"), ident(), term("on"), term("arrow"), ref.lazy());
		
		Parser a = Parsers.or(new Parser[] { 
				term("true"), 
				term("false"),
				term("and"),
				term("or"),
				term("not"),
				term("implies"),
				Parsers.tuple(term("tt"), set()),
				Parsers.tuple(term("ff"), set()),
				Parsers.tuple(term("iso1"), set(), set()),
				Parsers.tuple(term("iso2"), set(), set()),
				Parsers.tuple(term("fst"), set(), set()),
				Parsers.tuple(term("snd"), set(), set()),
				Parsers.tuple(term("inl"), set(), set()),
				Parsers.tuple(term("inr"), set(), set()),
				Parsers.tuple(term("eval"), set(), set()),
				Parsers.tuple(term("curry"), ref.lazy()),
				Parsers.tuple(term("char"), ref.lazy()),
				Parsers.tuple(term("kernel"), ref.lazy()),
				Parsers.tuple(term("id"), set()),
				compTy, plusTy, prodTy, ident(), fnConst(), app, app2 });

		ref.set(a);
		return a;
	}
	
	public static final Parser<?> ftr() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term(";"), ref.lazy()), term(")"));
		
		////
		Parser<?> node = Parsers.tuple(ident(), term("->"), cat());
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				ref.lazy());
		Parser<?> xxx = Parsers.tuple(section("objects", node), 
				section("arrows", arrow));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("{"), xxx, term("}")), term(":"),
						cat(), term("->"), term("Cat"));
		////
		
		////
				Parser<?> node2 = Parsers.tuple(ident(), term("->"), ref.lazy());
				Parser<?> arrow2 = Parsers.tuple(
						ident(),
						term("->"),
						ident());
				Parser<?> xxx2 = Parsers.tuple(section("objects", node2), 
						section("arrows", arrow2));
				Parser t1 = Parsers.between(term("("),
						Parsers.tuple(term("Set"), term("^"), cat()), term(")"));
				Parser t2 = Parsers.between(term("("),
						Parsers.tuple(term("Cat"), term("^"), cat()), term(")"));
				Parser<?> constant2 = Parsers
						.tuple(Parsers.between(term("{"), xxx2, term("}")), term(":"),
								cat(), term("->"), t1.or(t2));
		////
		Parser catTerm = term("Cat");
		Parser setTerm = term("Set");
		Parser a = Parsers.or(new Parser[] {
				Parsers.tuple(term("unit"), cat(), catTerm.or(setTerm)),
				Parsers.tuple(term("void"), cat(), catTerm.or(setTerm)),
				Parsers.tuple(term("tt"), cat()),
				Parsers.tuple(term("ff"), cat()),
				Parsers.tuple(term("dom"), ident()),
				Parsers.tuple(term("cod"), ident()),
				Parsers.tuple(term("iso1"), cat(), cat()),
				Parsers.tuple(term("iso2"), cat(), cat()),
				Parsers.tuple(term("fst"), cat(), cat()),
				Parsers.tuple(term("snd"), cat(), cat()),
				Parsers.tuple(term("inl"), cat(), cat()),
				Parsers.tuple(term("inr"), cat(), cat()),
				Parsers.tuple(term("eval"), cat(), cat()),
				Parsers.tuple(term("curry"), ref.lazy()),
				Parsers.tuple(term("uncurry"), ref.lazy()),
				Parsers.tuple(term("delta"), ref.lazy()),
				Parsers.tuple(term("sigma"), ref.lazy()),
				Parsers.tuple(term("pi"), ref.lazy()),
				Parsers.tuple(term("pivot"), ref.lazy()),
				Parsers.tuple(term("unpivot"), ref.lazy()),
				Parsers.tuple(term("pushout"), ident(), ident()),
				Parsers.tuple(term("apply"), ref.lazy(), term("on"), term("object"), ref.lazy()),
				Parsers.tuple(term("id"), cat()),
				Parsers.tuple(term("prop"), cat()),
				compTy, plusTy, prodTy, expTy, ident(), 
				instanceConst(), constant, setsetConst(), constant2, 
				mappingConst(),  }); 

		ref.set(a);
		return a;
	}
	
	public static final Parser<?> instanceConst() {
		Parser<?> node = Parsers.tuple(ident(), term("->"), set());
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				fn().or(setOfPairs()));

		Parser<?> xxx = Parsers.tuple(section("objects", node), 
				section("arrows", arrow));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("{"), xxx, term("}")), term(":"),
						cat(), term("->"), term("Set"));
		return constant;
	}
	
	public static final Parser<?> mappingConst() {
		Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				path());

		Parser<?> xxx = Parsers.tuple(section("objects", node), 
				section("arrows", arrow));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("{"), xxx, term("}")), term(":"),
						cat(), term("->"), cat());
		return constant;
	}
	
	public static Parser<?> section2(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p, term(";"));
	}
	
	public static final Parser<?> setsetConst() {
		Parser<?> node = Parsers.tuple(ident(), term("->"), set());
		
		Parser<?> arrow = Parsers.tuple(
				Parsers.tuple(ident(), term(":"), ident(), term("->"), ident()),
				term("->"),
				fn());

		Parser<?> xxx = Parsers.tuple(section2("objects", node), 
				                      section2("arrows", arrow));
		
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("{"), xxx, term("}")), term(":"),
						term("Set"), term("->"), term("Set"));
		return constant;
	}
	
	
	public static final Parser setOfPairs() {
		Parser<?> q = Parsers.tuple(term("("), value(), term(","), value(), term(")"));
		return Parsers.tuple(term("{"), q.sepBy(term(",")), term("}"));		
	}

	public static final Parser<?> fnConst() { 
		Parser<?> p = Parsers.tuple(set(), term("->"), set());
		Parser<?> q = Parsers.tuple(term("("), value(), term(","), value(), term(")"));
		return Parsers.tuple(term("{"), q.sepBy(term(",")), term("}"), term(":"), p);
	}
	
	public static final FnExp toFn(Object o) { 
		try {
			Tuple5 t5 = (Tuple5) o;
			
			if (t5.a.toString().equals("apply")) {
				Tuple5 t = (Tuple5) o;
				String f = t.b.toString();
				FnExp e = toFn(t.e);
				return new FnExp.Apply(f, e);
			} 
			
			Tuple3 t3 = (Tuple3) t5.e;
			Map s = new HashMap<>();
			List l = (List) t5.b;
			for (Object y : l) {
				Pair yy = (Pair) toValue(y);
				if (s.containsKey(yy.first)) {
					throw new RuntimeException("Duplicate domain entry in " + o);
				}
				s.put(yy.first, yy.second); 
			}
			SetExp t3a = toSet(t3.a);
			SetExp t3c = toSet(t3.c);
			return new FnExp.Const(s::get, t3a, t3c);
		} catch (Exception e) {
			
		}
		try {
			Tuple3 p = (Tuple3) o;

			Object p2 = p.b;
			Object p3 = p.c;
			Object o1 = p.a;
			String p1 = p.a.toString();

			if (p1.equals("fst")) {
				return new FnExp.Fst(toSet(p2), toSet(p3));
			} else if (p1.equals("snd")) {
				return new FnExp.Snd(toSet(p2), toSet(p3));
			}  else if (p1.equals("inl")) {
				return new FnExp.Inl(toSet(p2), toSet(p3));
			} else if (p1.equals("inr")) {
				return new FnExp.Inr(toSet(p2), toSet(p3));
			} else if (p1.equals("iso1")) {
				return new FnExp.Iso(true, toSet(p2), toSet(p3));
			} else if (p1.equals("iso2")) {
				return new FnExp.Iso(false, toSet(p2), toSet(p3));
			} else if (p1.equals("eval")) {
				return new FnExp.Eval(toSet(p2), toSet(p3));
			} else if (p2.toString().equals(";")) {
				return new FnExp.Comp(toFn(o1), toFn(p3));
			} else if (p2.toString().equals("*")) {
				return new FnExp.Prod(toFn(o1), toFn(p3));
			} else if (p2.toString().equals("+")) {
				return new FnExp.Case(toFn(o1), toFn(p3));
			} if (p1.toString().equals("apply")) {
				return new FnExp.ApplyTrans(p2.toString(), toSet(p3));
			}

		} catch (RuntimeException re) {
		}


		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			String p1 = p.a.toString();
			Object p2 = p.b; 
			if (p1.equals("id")) {
				return new FnExp.Id(toSet(p2));
			} else if (p1.equals("curry")) {
				return new FnExp.Curry(toFn(p2));
			} else if (p1.equals("ff")) {
				return new FnExp.FF(toSet(p2));
			} else if (p1.equals("tt")) {
				return new FnExp.TT(toSet(p2));
			} else if (p1.equals("char")) {
				return new FnExp.Chr(toFn(p2));
			} else if (p1.equals("kernel")) {
				return new FnExp.Krnl(toFn(p2));
			}
		} catch (RuntimeException re) {

		}

		if (o instanceof String) {
			return new FnExp.Var(o.toString());
		}
		if (o.toString().equals("true")) {
			return new FnExp.Tru("true");
		} else if (o.toString().equals("false")) {
			return new FnExp.Tru("false");
		} else if (o.toString().equals("and")) {
			return new FnExp.Tru("and");
		} else if (o.toString().equals("or")) {
			return new FnExp.Tru("or");
		} else if (o.toString().equals("not")) {
			return new FnExp.Tru("not");
		} else if (o.toString().equals("implies")) {
			return new FnExp.Tru("implies");
		} 

		throw new RuntimeException(); 
	}
	
	//TODO: ** notation
	
	public static final TransExp toTrans(Object o) { 
		try {
			Tuple5 t = (Tuple5) o;
			
			if (t.a.toString().equals("apply")) {
				FunctorExp f = toFtr(t.b);
				if (t.d.toString().equals("arrow")) {
					TransExp e = toTrans(t.e);
					return new TransExp.Apply(f, e);
				} else {
					Tuple3 t3 = (Tuple3) t.e;
					List<String> l = (List<String>) t3.a;
					return new TransExp.ApplyPath(f, l.remove(0), l, toCat(t3.c));
				}
			}
			

			Tuple5 a = (Tuple5) t.c;
			Tuple5 b = (Tuple5) t.e;

			if (a.c.toString().equals("Set") && a.e.toString().equals("Set") &&
				b.c.toString().equals("Set") && b.e.toString().equals("Set")) {
				Tuple3 z = (Tuple3) ((Tuple3) t.a).b;
				FunctorExp src = toFtr(a.a);
				FunctorExp dst = toFtr(b.a);
				String ob = z.a.toString();
				FnExp fun = toFn(z.c);
				return new TransExp.SetSet(ob, fun, src, dst);
			} else if (a.e.toString().equals("Set") && b.e.toString().equals("Set")) {
				FunctorExp src = toFtr(a.a);
				FunctorExp dst = toFtr(b.a);
				List<Object> ob = (List<Object>) ((Tuple3) t.a).b;
				Map<String, Chc<FnExp, SetExp>> fun = new HashMap<>();
		
				for (Object ttt : ob) {
					if (fun.containsKey(o)) {
						throw new RuntimeException("Duplicate arrow: " + ttt + " in " + o);
					}						
					Tuple3 u = (Tuple3) ttt;
					String n = (String) u.a;
					try {
						fun.put(n, Chc.inLeft(toFn(u.c)));
					} catch (Exception yyy) {
						fun.put(n, Chc.inRight(toSet(u.c)));						
					}
				}
				return new TransExp.ToSet(fun , src, dst);
			} else if (a.e.toString().equals("Cat") && b.e.toString().equals("Cat")) {
				FunctorExp src = toFtr(a.a);
				FunctorExp dst = toFtr(b.a);
				List<Object> ob = (List<Object>) ((Tuple3) t.a).b;
				Map<String, FunctorExp> fun = new HashMap<>();
				
				for (Object ttt : ob) {
					if (fun.containsKey(o)) {
						throw new RuntimeException("Duplicate arrow: " + ttt + " in " + o);
					}
					Tuple3 u = (Tuple3) ttt;
					String n = (String) u.a;
					fun.put(n, toFtr(u.c));
				}
				return new TransExp.ToCat(fun , src, dst);
			} else {
				try {
					FunctorExp src = toFtr(a.a);
					FunctorExp dst = toFtr(b.a);
					List<Object> ob = (List<Object>) ((Tuple3) t.a).b;
					Map<String, TransExp> fun = new HashMap<>();
					
					for (Object ttt : ob) {
						if (fun.containsKey(o)) {
							throw new RuntimeException("Duplicate arrow: " + ttt + " in " + o);
						}
						Tuple3 u = (Tuple3) ttt;
						String n = (String) u.a;
						fun.put(n, toTrans(u.c));
					}
					return new TransExp.ToInst(fun , src, dst);	
				}catch(Exception ex) { }
				
				FunctorExp src = toFtr(a.a);
				FunctorExp dst = toFtr(b.a);
				List<Object> ob = (List<Object>) ((Tuple3) t.a).b;
				Map<String, Pair<String, List<String>>> fun = new HashMap<>();		
				for (Object ttt : ob) {
					if (fun.containsKey(o)) {
						throw new RuntimeException("Duplicate arrow: " + ttt + " in " + o);
					}
					Tuple3 u = (Tuple3) ttt;
					String n = (String) u.a;
					List<String> l = (List<String>) u.c;
					String ll = l.remove(0);
					fun.put(n, new Pair<>(ll, l));
				}
				return new TransExp.ToMap(fun , src, dst, toCat(a.c), toCat(a.e));
			}
		} catch (Exception re) { }
		
		try {
			Tuple4 t = (Tuple4) o;
			if (t.a.toString().equals("return") || t.a.toString().equals("coreturn")) {
				return new TransExp.Adj(t.a.toString(), t.b.toString(), t.c.toString(), toFtr(t.d));
			} else if (t.a.toString().toString().equals("left") ) {
				return new TransExp.Whisker(true, toFtr(t.c), toTrans(t.d));
			} else if (t.a.toString().toString().equals("right") ) {
				return new TransExp.Whisker(false, toFtr(t.c), toTrans(t.d));
			} else if (t.a.toString().equals("APPLY")) {
				return new TransExp.PeterApply(t.d.toString(),toTrans(t.b));
			} else {
				return new TransExp.ApplyTrans(toTrans(t.b), toFtr(t.d));
			}
		} catch (Exception re) { }

		try {
			Tuple3 p = (Tuple3) o;

			Object p2 = p.b;
			Object p3 = p.c;
			Object o1 = p.a;
			//String p1 = p.a.toString();

			if (p2.toString().equals(";")) {
				return new TransExp.Comp(toTrans(o1), toTrans(p3));
			} else if (o1.toString().equals("fst")) {
				return new TransExp.Proj(toFtr(p2), toFtr(p3), true);
			} else if (o1.toString().equals("snd")) {
				return new TransExp.Proj(toFtr(p2), toFtr(p3), false);
			} else if (o1.toString().equals("inl")) {
				return new TransExp.Inj(toFtr(p2), toFtr(p3), true);
			} else if (o1.toString().equals("inr")) {
				return new TransExp.Inj(toFtr(p2), toFtr(p3), false);
			}  else if (o1.toString().equals("eval")) {
				return new TransExp.Eval(toFtr(p2), toFtr(p3));
			} else if (p2.toString().equals("*")) {
				return new TransExp.Prod(toTrans(o1), toTrans(p3));
			} else if (p2.toString().equals("+")) {
				return new TransExp.CoProd(toTrans(o1), toTrans(p3));
			} else if (o1.toString().equals("iso1")) {
				return new TransExp.Iso(true, toFtr(p2), toFtr(p3));
			} else if (o1.toString().equals("iso2")) {
				return new TransExp.Iso(false, toFtr(p2), toFtr(p3));
			}

		} catch (RuntimeException re) { }

		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			String p1 = p.a.toString();
			Object p2 = p.b; 
			if (p1.equals("id")) {
				return new TransExp.Id(toFtr(p2));
			} else if (p1.equals("tt")) {
				return new TransExp.One(toFtr(p2));
			} else if (p1.equals("ff")) {
				return new TransExp.Zero(toFtr(p2));
			} else if (p1.equals("curry")) {
				return new TransExp.Curry(toTrans(p2), true);
			} else if (p1.equals("CURRY")) {
				return new TransExp.Curry(toTrans(p2), false);
			} else if (p1.equals("true")) {
				return new TransExp.Bool(true, toCat(p2));
			} else if (p1.equals("false")) {
				return new TransExp.Bool(false,toCat(p2));
			} else if (p1.equals("char")) {
				return new TransExp.Chr(toTrans(p2));
			} else if (p1.equals("kernel")) {
				return new TransExp.Ker(toTrans(p2));
			} else if (p1.equals("not") || p1.equals("and") || p1.equals("implies") || p1.equals("or")) {
				return new TransExp.AndOrNotImplies(p1, toCat(p2));
			}
		} catch (RuntimeException re) { }

		if (o instanceof String) {
			return new TransExp.Var(o.toString());
		}
		
		throw new RuntimeException("Could not create transform from " + o); 
	}
	
	public static FunctorExp toSetSet(Object decl) {
		Tuple3 y = (Tuple3) decl;
		org.codehaus.jparsec.functors.Pair x = (org.codehaus.jparsec.functors.Pair) y.a;
		
		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.b;
		
		Tuple3 nodes0  = (Tuple3) nodes.b;
		Tuple3 arrows0 = (Tuple3) arrows.b;

		Tuple5 arrows1 = (Tuple5) arrows0.a;
		
		SetSetConst ret = new SetSetConst(nodes0.a.toString(), toSet(nodes0.c), arrows1.a.toString(), arrows1.c.toString(), arrows1.e.toString(), toFn(arrows0.c));
		return ret;
	}
	
//	@SuppressWarnings("rawtypes")
	public static FunctorExp toInstConst(Object decl) {
		Tuple3 y = (Tuple3) decl;
		org.codehaus.jparsec.functors.Pair x = (org.codehaus.jparsec.functors.Pair) y.a;
		
		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.b;
		
		List nodes0 = (List) nodes.b;
		List arrows0 = (List) arrows.b;


		Map<String, SetExp> nodesX = new HashMap<>();
		for (Object o : nodes0) {
			if (nodesX.containsKey(o)) {
				throw new RuntimeException("Duplicate object: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			SetExp l = toSet(u.c);
			nodesX.put(n, l);
		}
		
		Map<String, Chc<FnExp,SetExp>> arrowsX = new HashMap<>();
		for (Object o : arrows0) {
			if (arrowsX.containsKey(o)) {
				throw new RuntimeException("Duplicate arrow: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			try {
				FnExp l = toFn(u.c);
				arrowsX.put(n, Chc.inLeft(l));
			} catch (Exception eee) {
				SetExp l = toSet(u.c);
				arrowsX.put(n, Chc.inRight(l));				
			}
		}
		InstConst ret = new InstConst(toCat(y.c), nodesX, arrowsX);
		return ret;
	}
	
	public static FunctorExp toCatFtrConst(Object decl) {
		Tuple5 y = (Tuple5) decl;
		org.codehaus.jparsec.functors.Pair x = (org.codehaus.jparsec.functors.Pair) y.a;
		
		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.b;
		
		List nodes0 = (List) nodes.b;
		List arrows0 = (List) arrows.b;

		Map<String, CatExp> nodesX = new HashMap<>();
		for (Object o : nodes0) {
			if (nodesX.containsKey(o)) {
				throw new RuntimeException("Duplicate object: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			CatExp l = toCat(u.c);
			nodesX.put(n, l);
		}

		Map<String, FunctorExp> arrowsX = new HashMap<>();
		for (Object o : arrows0) {
			if (arrowsX.containsKey(o)) {
				throw new RuntimeException("Duplicate arrow: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			FunctorExp l = toFtr(u.c);
			arrowsX.put(n, l);
		}
		CatConst ret = new CatConst(toCat(y.c), nodesX, arrowsX);
		return ret;
	}
	
	public static FunctorExp toFinalConst(Object decl) {
		Tuple5 y = (Tuple5) decl;
		org.codehaus.jparsec.functors.Pair x = (org.codehaus.jparsec.functors.Pair) y.a;
		
		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.b;
		
		List nodes0 = (List) nodes.b;
		List arrows0 = (List) arrows.b;

		Map<String, FunctorExp> nodesX = new HashMap<>();
		for (Object o : nodes0) {
			if (nodesX.containsKey(o)) {
				throw new RuntimeException("Duplicate object: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			FunctorExp l = toFtr(u.c);
			nodesX.put(n, l);
		}

		Map<String, TransExp> arrowsX = new HashMap<>();
		for (Object o : arrows0) {
			if (arrowsX.containsKey(o)) {
				throw new RuntimeException("Duplicate arrow: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			TransExp l = toTrans(u.c);
			arrowsX.put(n, l);
		}
		FinalConst ret = new FinalConst(toCat(y.c), toCat(y.e), nodesX, arrowsX);
		return ret;
	}

	public static FunctorExp toMapConst(Object decl) {
		Tuple5 y = (Tuple5) decl;
		org.codehaus.jparsec.functors.Pair x = (org.codehaus.jparsec.functors.Pair) y.a;
		
		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.b;
		
		List nodes0 = (List) nodes.b;
		List arrows0 = (List) arrows.b;


		Map<String, String> nodesX = new HashMap<>();
		for (Object o : nodes0) {
			if (nodesX.containsKey(o)) {
				throw new RuntimeException("Duplicate object: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			String l = u.c.toString();
			nodesX.put(n, l);
		}
		
		Map<String, Pair<String, List<String>>> arrowsX = new HashMap<>();
		for (Object o : arrows0) {
			if (arrowsX.containsKey(o)) {
				throw new RuntimeException("Duplicate arrow: " + o + " in " + decl);
			}
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			List<String> l = (List<String>) u.c;
			String ll = l.remove(0);
			arrowsX.put(n, new Pair<>(ll, l));
		}
		MapConst ret = new MapConst(toCat(y.c), toCat(y.e), nodesX, arrowsX);
		return ret;
	}

	
	public static final FunctorExp toFtr(Object o) { 
		try {
			
			Tuple5 p = (Tuple5) o;
			
			if (p.a.toString().equals("apply")) {
				FunctorExp f = toFtr(p.b);
				FunctorExp e = toFtr(p.e);
				return new FunctorExp.Apply(f, e);
			} 
			
			if (p.e.toString().equals("Set")) {
				if (p.c.toString().equals("Set")) {
					return toSetSet(o);
				} else {
					return toInstConst(o);
				}
			} else if (p.e.toString().equals("Cat")) {
				return toCatFtrConst(o);
			} else {
				if (p.e instanceof Tuple3) {
					Tuple3 t = (Tuple3) p.e;
					if ((t.a.toString().equals("Cat") || t.a.toString().equals("Set")) && t.b.toString().equals("^")) {
						return toFinalConst(o);
					} else {
						throw new RuntimeException();
					}
				}
				return toMapConst(o);
			}
		} catch (Exception e) { }
		
		try {
			Tuple3 p = (Tuple3) o;

			Object p2 = p.b;
			Object p3 = p.c;
			Object o1 = p.a;
			String p1 = p.a.toString();

			if (p1.equals("fst")) {
				return new FunctorExp.Fst(toCat(p2), toCat(p3));
			} else if (p1.equals("snd")) {
				return new FunctorExp.Snd(toCat(p2), toCat(p3));
			}  else if (p1.equals("inl")) {
				return new FunctorExp.Inl(toCat(p2), toCat(p3));
			} else if (p1.equals("inr")) {
				return new FunctorExp.Inr(toCat(p2), toCat(p3));
			} else if (p1.equals("iso1")) {
				return new FunctorExp.Iso(true, toCat(p2), toCat(p3));
			} else if (p1.equals("iso2")) {
				return new FunctorExp.Iso(false, toCat(p2), toCat(p3));
			} else if (p1.equals("eval")) {
				return new FunctorExp.Eval(toCat(p2), toCat(p3));
			} else if (p2.toString().equals(";")) {
				return new FunctorExp.Comp(toFtr(o1), toFtr(p3));
			} else if (p2.toString().equals("*")) {
				return new FunctorExp.Prod(toFtr(o1), toFtr(p3));
			} else if (p2.toString().equals("+")) {
				return new FunctorExp.Case(toFtr(o1), toFtr(p3));
			} else if (p2.toString().equals("^")) {
				return new FunctorExp.Exp(toFtr(o1), toFtr(p3));
			} else if (p1.equals("unit")) {
				return new FunctorExp.One(toCat(p.b), toCat(p.c));	
			} else if (p1.equals("void")) {
				return new FunctorExp.Zero(toCat(p.b), toCat(p.c));	
			} else if (p1.equals("pushout")) {
				return new FunctorExp.Pushout(p.b.toString(), p.c.toString());					
			}

		} catch (RuntimeException re) {
		}


		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			String p1 = p.a.toString();
			Object p2 = p.b; 
			if (p1.equals("id")) {
				return new FunctorExp.Id(toCat(p2));
			} else if (p1.equals("prop")) {
				return new FunctorExp.Prop(toCat(p2));
			} else if (p1.equals("curry")) {
				return new FunctorExp.Curry(toFtr(p2));
			} else if (p1.equals("uncurry")) {
				return new FunctorExp.Uncurry(toFtr(p2));
			} else if (p1.equals("delta") || p1.equals("sigma") || p1.equals("pi")) {
				return new FunctorExp.Migrate(toFtr(p2), p1.toString());
			} else if (p1.equals("ff")) {
				return new FunctorExp.FF(toCat(p2));
			} else if (p1.equals("tt")) {
				return new FunctorExp.TT(toCat(p2));	
			} else if (p1.equals("dom")) {
				return new FunctorExp.Dom(p2.toString(), true);
			} else if (p1.equals("cod")) {
				return new FunctorExp.Dom(p2.toString(), false);
			} else if (p1.equals("pivot")) {
				return new FunctorExp.Pivot(toFtr(p2), true);
			} else if (p1.equals("unpivot")) {
				return new FunctorExp.Pivot(toFtr(p2), false);
			} 
		} catch (RuntimeException re) {

		}

		if (o instanceof String) {
			return new FunctorExp.Var(o.toString());
		}

		throw new RuntimeException("Bad: " + o); 
	}
/*
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> instance() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));

		Parser<?> external = Parsers.tuple(term("external"), ident(), schema());
		Parser<?> delta = Parsers.tuple(term("delta"), mapping(), ref.lazy());
		Parser<?> sigma = Parsers.tuple(term("sigma"), mapping(), ref.lazy());
		Parser<?> pi = Parsers.tuple(term("pi"), mapping(), ref.lazy());
		Parser<?> SIGMA = Parsers.tuple(term("SIGMA"), mapping(), ref.lazy());
		Parser<?> relationalize = Parsers.tuple(term("relationalize"), ident());
		Parser<?> eval = Parsers.tuple(term("eval"), query(), ref.lazy());
		Parser<?> fullEval = Parsers.tuple(term("EVAL"), fullQuery(),
				ref.lazy());

		Parser a = Parsers.or(new Parser[] {
				Parsers.tuple(term("kernel"), ident()),
				Parsers.tuple(term("prop"), schema()),
				Parsers.tuple(term("void"), schema()),
				Parsers.tuple(term("unit"), schema()), plusTy, prodTy, expTy,
				ident(), instanceConst(), delta, sigma, pi, SIGMA, external,
				relationalize, eval, fullEval });

		ref.set(a);

		return a;
	}

	public static final Parser<?> instanceDecl() {
		return Parsers.tuple(term("instance"), ident(), term("="), instance());
	}

	public static final Parser<?> instanceConst() {
		Parser<?> node = Parsers.tuple(ident(), term("->"), Parsers.between(
				term("{"), string().sepBy(term(",")), term("}")));
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				Parsers.between(
						term("{"),
						Parsers.between(term("("),
								Parsers.tuple(string(), term(","), string()),
								term(")")).sepBy(term(",")), term("}")));

		Parser<?> xxx = Parsers.tuple(section("nodes", node), Parsers.or(
				(Parser<?>) section("attributes", arrow),
				(Parser<?>) Parsers.tuple(term("attributes"),
						term("ASWRITTEN"), term(";"))),
				section("arrows", arrow));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("{"), xxx, term("}")), term(":"),
						schema());
		return constant;
	}

	@SuppressWarnings("rawtypes")
	public static InstExp toInstConst(Object decl) {
		Tuple3 y = (Tuple3) decl;
		Tuple3 x = (Tuple3) y.a;

		// List<Pair<String, List<Pair<Object, Object>>>> data = new
		// LinkedList<>();

		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.c;
		Tuple3 attrs = (Tuple3) x.b;

		List nodes0 = (List) nodes.b;
		List arrows0 = (List) arrows.b;

		// List<Object> seen = new LinkedList<>();

		List<Pair<String, List<Pair<Object, Object>>>> nodesX = new LinkedList<>();
		for (Object o : nodes0) {
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			List m = (List) u.c;
			List<Pair<Object, Object>> l = new LinkedList<>();
			for (Object h : m) {
				l.add(new Pair<>(h, h));
			}
			// if (seen.contains(n)) {
			// throw new RuntimeException("duplicate field: " + o);
			// }
			// seen.add(n);
			nodesX.add(new Pair<>(n, l));
		}

		// RuntimeException toThrow = null;

		List<Pair<String, List<Pair<Object, Object>>>> attrsX = new LinkedList<>();
		if (attrs.b.toString().equals("ASWRITTEN")) {
			for (Pair<String, List<Pair<Object, Object>>> k : nodesX) {
				attrsX.add(new Pair<>(k.first + "_att", k.second));
			}
		} else {
			List attrs0 = (List) attrs.b;

			for (Object o : attrs0) {

				Tuple3 u = (Tuple3) o;
				String n = (String) u.a;
				List m = (List) u.c;
				List<Pair<Object, Object>> l = new LinkedList<>();
				for (Object h : m) {
					Tuple3 k = (Tuple3) h;
					l.add(new Pair<>(k.a, k.c));
				}
				// if (seen.contains(n)) {
				// toThrow = new RuntimeException("duplicate field: " + n );
				// throw toThrow;
				// }
				// seen.add(n);
				attrsX.add(new Pair<>(n, l));
			}
		}
		List<Pair<String, List<Pair<Object, Object>>>> arrowsX = new LinkedList<>();
		for (Object o : arrows0) {
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			List m = (List) u.c;
			List<Pair<Object, Object>> l = new LinkedList<>();
			for (Object h : m) {
				Tuple3 k = (Tuple3) h;
				l.add(new Pair<>(k.a, k.c));
			}
			// if (seen.contains(n)) {
			// throw new RuntimeException("duplicate field: " + o);
			// }
			// seen.add(n);
			arrowsX.add(new Pair<>(n, l));
		}
		fql.decl.InstExp.Const ret = new InstExp.Const(nodesX, attrsX, arrowsX,
				toSchema(y.c));
		return ret;
	}

	@SuppressWarnings("rawtypes")
	public static final InstExp toInst(Object o) {
		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.a;
			String y = z.toString();
			if (y.equals("delta")) {
				return new InstExp.Delta(toMapping(t.b), t.c.toString());
			} else if (y.equals("sigma")) {
				return new InstExp.Sigma(toMapping(t.b), t.c.toString());
			} else if (y.equals("SIGMA")) {
				return new InstExp.FullSigma(toMapping(t.b), t.c.toString());
			} else if (y.equals("pi")) {
				return new InstExp.Pi(toMapping(t.b), t.c.toString());
			} else if (y.equals("external")) {
				return new InstExp.External(toSchema(t.b), t.c.toString());
			} else if (y.equals("eval")) {
				return new InstExp.Eval(toQuery(t.b), t.c.toString());
			} else if (y.equals("EVAL")) {
				return new InstExp.FullEval(toFullQuery(t.b), t.c.toString());
			}
		} catch (RuntimeException cce) {
		}

		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.b;
			String y = z.toString();
			if (y.equals("+")) {
				return new InstExp.Plus(t.a.toString(), t.c.toString());
			} else if (y.equals("*")) {
				return new InstExp.Times(t.a.toString(), t.c.toString());
			}
			if (y.equals("^")) {
				return new InstExp.Exp(t.a.toString(), (t.c).toString());
			}
		} catch (RuntimeException cce) {
		}

		try {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) o;

			if (pr.a.toString().equals("unit")) {
				return new InstExp.One(toSchema(pr.b));
			} else if (pr.a.toString().equals("void")) {
				return new InstExp.Zero(toSchema(pr.b));
			} else if (pr.a.toString().equals("prop")) {
				return new InstExp.Two(toSchema(pr.b));
			} else if (pr.a.toString().equals("relationalize")) {
				return new InstExp.Relationalize(pr.b.toString());
			} else if (pr.a.toString().equals("kernel")) {
				return new InstExp.Kernel(pr.b.toString());
			}
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		return toInstConst(o);
	}

	@SuppressWarnings("rawtypes")
	public static QueryExp toQuery(Object o) {
		if (o instanceof Tuple5) {
			Tuple5 t = (Tuple5) o;
			return new QueryExp.Comp(toQuery(t.b), toQuery(t.d));

		} else if (o instanceof Tuple3) {
			Tuple3 x = (Tuple3) o;
			org.codehaus.jparsec.functors.Pair p1 = (org.codehaus.jparsec.functors.Pair) x.a;
			org.codehaus.jparsec.functors.Pair p2 = (org.codehaus.jparsec.functors.Pair) x.b;
			org.codehaus.jparsec.functors.Pair p3 = (org.codehaus.jparsec.functors.Pair) x.c;
			return new QueryExp.Const(toMapping(p1.b), toMapping(p2.b),
					toMapping(p3.b));
		} else {
			return new QueryExp.Var(o.toString());
		}
	}

	@SuppressWarnings("rawtypes")
	public static FullQueryExp toFullQuery(Object o) {
		if (o instanceof Tuple5) {
			Tuple5 t = (Tuple5) o;
			if (t.a.toString().equals("match")) {
				return new FullQueryExp.Match(toMatch(t.b), toSchema(t.c),
						toSchema(t.d), t.e.toString());
			}
			return new FullQueryExp.Comp(toFullQuery(t.b), toFullQuery(t.d));
		} else if (o instanceof org.codehaus.jparsec.functors.Pair) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			if (p.a.toString().equals("delta")) {
				return new FullQueryExp.Delta(toMapping(p.b));
			} else if (p.a.toString().equals("SIGMA")) {
				return new FullQueryExp.Sigma(toMapping(p.b));
			} else if (p.a.toString().equals("pi")) {
				return new FullQueryExp.Pi(toMapping(p.b));
			}
		} else {
			return new FullQueryExp.Var(o.toString());
		}
		throw new RuntimeException();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Set<Pair<String, String>> toMatch(Object b) {
		List<Tuple3> l = (List<Tuple3>) b;
		Set<Pair<String, String>> ret = new HashSet<>();
		for (Tuple3 k : l) {
			ret.add(new Pair<>(k.a.toString(), k.c.toString()));
		}
		return ret;
	}
*/
	public static final FQLProgram program(String s) {
		List<NewDecl> ret = new LinkedList<>();
		List decls = (List) FQLParser.program.parse(s);

		for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Object decl = pr.b;
			String txt = pr.a.toString();
			int idx = s.indexOf(txt);
			if (idx < 0) {
				throw new RuntimeException();
			}

			Tuple3 t = (Tuple3) decl;
			String kind = ((Token) t.a).toString();
			String name = t.b.toString();
			switch (kind) {
			case "set":
				Tuple4 tt = (Tuple4) decl;
		//		name = (String) t.b;
				ret.add(FQLProgram.NewDecl.setDecl(name, idx, toSet(tt.d)));
				break;
			case "function":
				Tuple4 t0 = (Tuple4) decl;
			//	name = (String) t.b;
				ret.add(FQLProgram.NewDecl.fnDecl(name, idx, toFn(t0.d)));
				break;
			case "functor":
				Tuple4 ti = (Tuple4) decl;
			//	name = (String) t.b;
				ret.add(FQLProgram.NewDecl.ftrDecl(name, idx, toFtr(ti.d)));
				break;
			case "category":
				Tuple4 tx = (Tuple4) decl;
			//	name = (String) t.b;
				ret.add(FQLProgram.NewDecl.catDecl(name, idx, toCat(tx.d)));
				break;
			case "transform":
				Tuple4 te = (Tuple4) decl;
			//	name = (String) t.b;
				ret.add(FQLProgram.NewDecl.transDecl(name, idx, toTrans(te.d)));
				break;
			
			default:
				throw new RuntimeException("Unknown decl: " + kind);
			}
		}

		return new FQLProgram(ret); 
	}

		private static Parser<List<String>> path() {
			return Terminals.Identifier.PARSER.sepBy1(term("."));
		}

	public static Parser<?> section(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p.sepBy(term(",")), term(";"));
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}

}