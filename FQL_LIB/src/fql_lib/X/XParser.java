package fql_lib.X;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;

import fql_lib.Pair;
import fql_lib.Triple;

@SuppressWarnings({"rawtypes", "unchecked"})
public class XParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "?" };

	static String[] res = new String[] { "type", "constant", "function", "assume", "nodes", "edges", "equations", "schema", "mapping", "instance", "transform", "delta", "sigma", "pi" };

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
		return Parsers.tuple(decl().source().peek(), decl()).many();
	}
	
	public static final Parser<?> exp() {
		Reference ref = Parser.newReference();

		Parser<?> sigma = Parsers.tuple(term("sigma"), ref.lazy(), ref.lazy());
		
		Parser<?> a = Parsers.or(new Parser<?>[] { ident(), schema(), mapping(ref), instance(ref), sigma});

		ref.set(a);

		return a;
	}

	public static final Parser<?> type() {
		return Parsers.tuple(term("type"), string());
	}
	
	public static final Parser<?> fn() {
		return Parsers.tuple(term("function"), ident(), term("->"), ident(), string());
	}
	
	public static final Parser<?> constx() {
		return Parsers.tuple(term("constant"), ident(), string());
	}
	
	public static final Parser<?> assume() {
		return Parsers.tuple(term("assume"), path(), term("="), path());
	}
	
	public static final Parser<?> schema() {
		Parser<?> p1 = ident();
		Parser<?> pX = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				ident());
		Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		Parser<?> foo = Parsers.tuple(section("nodes", p1), 
				section("edges", pX),
				section("equations", p3));
		return Parsers.between(term("schema").followedBy(term("{")), foo, term("}"));
	}
	
	
	public static final XExp.XSchema toCatConst(Object y) {
		List<String> nodes = new LinkedList<>();
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();

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
			eqs.add(new Pair<>(l1, l2));
		}
		XExp.XSchema c = new XExp.XSchema(nodes, arrows, eqs);
		return c;
	}

	
	public static final Parser<?> decl() {
		Parser e = Parsers.or(new Parser[] { exp(), type(), fn(), constx(), assume() });
		
		return Parsers.tuple(ident(), term("="), e);
	}
	
	public static final Parser<?> instance(Reference ref) {
		Parser<?> node = Parsers.tuple(ident(), term(":"), ident());

		Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		
		Parser<?> xxx = Parsers.tuple(section("nodes", node), 
				section("equations", p3));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("instance").followedBy(term("{")), xxx, term("}")), term(":"),
						ref.lazy());
		return constant;
	} 
	
	public static final Parser<?> mapping(Reference ref) {
		Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				path());

		Parser<?> xxx = Parsers.tuple(section("nodes", node), 
				section("edges", arrow));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("mapping").followedBy(term("{")), xxx, term("}")), term(":"),
						ref.lazy(), term("->"), ref.lazy());
		return constant;
	} 
	
	public static Parser<?> section2(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p, term(";"));
	}
		
	
/*
	
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
	*/
	/*
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
	*/

	/*public static FunctorExp toMapConst(Object decl) {
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
	} */

	public static final XProgram program(String s) {
		List<Triple<String, Integer, XExp>> ret = new LinkedList<>();
		List decls = (List) program.parse(s);

		for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Tuple3 decl = (Tuple3) pr.b;
			String txt = pr.a.toString();
			int idx = s.indexOf(txt);
			if (idx < 0) {
				throw new RuntimeException();
			}

			String name = decl.a.toString();
			ret.add(new Triple<>(name, idx, toExp(decl.c)));
		}

		return new XProgram(ret); 
	}
	
	private static XExp toExp(Object c) {
		if (c instanceof String) {
			return new XExp.Var((String) c);
		}
		
		try {
			return toCatConst(c);
		} catch (Exception e) { }
		
		/*	
		return Parsers.tuple(term("type"), string());
		return Parsers.tuple(term("function"), ident(), term("->"), ident(), string());
		return Parsers.tuple(term("constant"), ident(), string());
		return Parsers.tuple(term("assume"), path(), term("="), path());
        */

		if (c instanceof Tuple5) {
			Tuple5 p = (Tuple5) c;
			return new XExp.XFn((String) p.b, (String) p.d, (String) p.e);
		}
		if (c instanceof Tuple4) {
			Tuple4 p = (Tuple4) c;
			return new XExp.XEq((List<String>) p.b, (List<String>) p.d);
		} 
		if (c instanceof Tuple3) {
			Tuple3 p = (Tuple3) c;
			return new XExp.XConst((String) p.b, (String) p.c);
		}
		if (c instanceof org.codehaus.jparsec.functors.Pair) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) c;
			return new XExp.XTy((String)p.b);
		}
		
		throw new RuntimeException("x: " + c.getClass());
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