package fql_lib.opl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import fql_lib.X.XExp;
import fql_lib.opl.OplExp.OplCtx;
import fql_lib.opl.OplExp.OplDelta;
import fql_lib.opl.OplExp.OplEval;
import fql_lib.opl.OplExp.OplJavaInst;
import fql_lib.opl.OplExp.OplMapping;
import fql_lib.opl.OplExp.OplSetInst;
import fql_lib.opl.OplExp.OplSetTrans;
import fql_lib.opl.OplExp.OplTerm;
import fql_lib.opl.OplExp.OplTransEval;
import fql_lib.opl.OplExp.OplVar;

public class OplParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "?", "@" };

	static String[] res = new String[] { 
		"transeval", "mapping", "delta", "eval", "theory", "model", "sorts", "symbols", "equations", "forall", "transform", "javascript"
	};

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
		return string(); //Terminals.Identifier.PARSER;
	}

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	public static final Parser<?> program() {
		return Parsers.tuple(decl().source().peek(), decl()).many();
	}
	
	public static final Parser<?> oplTerm() {
		Reference ref = Parser.newReference();
		Parser<?> app = Parsers.tuple(ident(), term("("), ref.lazy().sepBy(term(",")), term(")"));
		Parser<?> a = Parsers.or(new Parser<?>[] { app, ident()});
		ref.set(a);
		return a;
	}
	
	public static final Parser<?> oplSequent() {
		Parser<?> p = Parsers.tuple(ident(), term(":"), ident()).sepBy(term(","));
		return Parsers.tuple(term("forall"), p, term("."), oplTerm());
	}
	
	public static final Parser<?> oplEq() {
		Parser<?> p = Parsers.tuple(ident(), term(":"), ident()).sepBy(term(","));
		Parser<?> q = Parsers.tuple(oplTerm(), term("="), oplTerm());
		return Parsers.tuple(term("forall"), p, term("."), q);
	}
	
	public static final Parser<?> exp() {
		Reference ref = Parser.newReference();

		Parser<?> theory = theory();
		Parser<?> model = model();
		Parser<?> trans = trans();
		Parser<?> eval = Parsers.tuple(term("eval"), ident(), oplTerm());
		Parser<?> java = java();
		Parser<?> mapping = mapping();
		Parser<?> delta = Parsers.tuple(term("delta"), ident(), ident());
		Parser<?> evaltrans = Parsers.tuple(term("transeval"), ident(), ident(), oplTerm());
		
		Parser<?> a = Parsers.or(new Parser<?>[] { delta, mapping, theory, model, eval, trans, java, evaltrans });
		ref.set(a);

		return a;
	}	
	
	public static final Parser<?> trans() {
		Parser<?> q = Parsers.tuple(term("("), ident(), term(","), ident(), term(")")).sepBy(term(","));
		Parser<?> p = Parsers.tuple(ident(), term("->"), term("{"), q, term("}"));
		Parser<?> foo = section("sorts", p);
		return Parsers.tuple(term("transform").followedBy(term("{")), foo, 
				Parsers.tuple(term("}").followedBy(term(":")), ident(), term("->"), ident()));
	}
	
	public static final Parser<?> mapping() {
		Parser<?> q = Parsers.tuple(ident(), term("->"), oplSequent());
		Parser<?> p = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> foo = Parsers.tuple(section("sorts", p), section("symbols", q));
		return Parsers.tuple(term("mapping").followedBy(term("{")), foo, 
				Parsers.tuple(term("}").followedBy(term(":")), ident(), term("->"), ident()));
	}
	
	public static final Parser<?> theory() {
		Parser<?> q = Parsers.tuple(ident(), Parsers.tuple(term("@"), NUMBER).optional());
		Parser<?> p = Parsers.tuple(q, term(":"), ident().sepBy(term(",")), term("->"),
				ident());
		Parser<?> foo = Parsers.tuple(section("sorts", ident()), 
				section("symbols", p),
				section("equations", oplEq()));
		return Parsers.between(term("theory").followedBy(term("{")), foo, term("}"));
	}
	
	public static final Parser<?> java() {
		Parser<?> q = Parsers.tuple(ident(), term("->"), string());
		Parser<?> foo = 
				section("symbols", q);
		return Parsers.tuple(term("javascript").followedBy(term("{")), foo, Parsers.tuple(term("}").followedBy(term(":")), ident()));
	}

	public static final Parser<?> model() {
		Parser<?> p = Parsers.tuple(ident(), term("->"), Parsers.between(term("{"),ident().sepBy(term(",")), term("}")));
		Parser<?> y = Parsers.between(term("("), ident().sepBy(term(",")), term(")"));
		Parser<?> z = Parsers.tuple(term("("), y, term(","), ident(), term(")"));
		Parser<?> q = Parsers.tuple(ident(), term("->"), Parsers.between(term("{"), z.sepBy(term(",")), term("}")));
		Parser<?> foo = Parsers.tuple(section("sorts", p), 
				section("symbols", q));
		return Parsers.tuple(term("model").followedBy(term("{")), foo, Parsers.tuple(term("}").followedBy(term(":")), ident()));
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
	//	Parser e = Parsers.or(new Parser[] { exp()  });
		
		Parser p1 = Parsers.tuple(ident(), term("="), exp());
		
		return Parsers.or(new Parser[] { p1 });
		
//		return Parsers.tuple(ident(), Parsers.or(term("="), term(":")), e);
	}
	
	
	
	public static Parser<?> section2(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p, term(";"));
	}
		
	

	public static final OplTerm parse_term(String s) {
		Object o = oplTerm().from(TOKENIZER, IGNORED).parse(s);
		return toTerm(o);
	} 
	
	public static final OplProgram program(String s) {
		List<Triple<String, Integer, OplExp>> ret = new LinkedList<>();
		List decls = (List) program.parse(s);

		for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Tuple3 decl = (Tuple3) pr.b;
			
			toProgHelper(pr.a.toString(), s, ret, decl);
		}

		return new OplProgram(ret); 
	}
	
	private static void toProgHelper(String txt, String s, List<Triple<String, Integer, OplExp>> ret, Tuple3 decl) {
		int idx = s.indexOf(txt);
		if (idx < 0) {
			throw new RuntimeException();
		}

		String name = decl.a.toString();
		ret.add(new Triple<>(name, idx, toExp(decl.c)));
	}

	private static OplExp toTheory(Object o) {
		Tuple3 t = (Tuple3) o;
		
		Tuple3 a = (Tuple3) t.a;
		Tuple3 b = (Tuple3) t.b;
		Tuple3 c = (Tuple3) t.c;
		
		if (!(a.a.toString().equals("sorts") && b.a.toString().equals("symbols") && c.a.toString().equals("equations"))) {
			throw new RuntimeException();
		}
		
		Set<String> sorts = new HashSet<>((List<String>) a.b);
		if (sorts.size() != ((List)a.b).size()) {
			throw new DoNotIgnore("Duplicate sort");
		}
		
		List<Tuple5> symbols0 = (List<Tuple5>) b.b;
		List<Tuple4> equations0 = (List<Tuple4>) c.b;
		
		Map<String, Pair<List<String>, String>> symbols = new HashMap<>();
		Map<String, Integer> prec = new HashMap<>();
		for (Tuple5 x : symbols0) {
			org.codehaus.jparsec.functors.Pair name0 = (org.codehaus.jparsec.functors.Pair) x.a;
			String name = (String) name0.a;
			
			if (name0.b != null) {
				org.codehaus.jparsec.functors.Pair zzz = (org.codehaus.jparsec.functors.Pair) name0.b;
				Integer i = (Integer) zzz.b;
				prec.put(name, i);
			}

			List<String> args = (List<String>) x.c;
			String dom = (String) x.e;
			if (symbols.containsKey(name)) {
				throw new DoNotIgnore("Duplicate symbol " + name);
			}
			symbols.put(name, new Pair<>(args, dom));
		}
		
		List<Triple<OplCtx<String>, OplTerm, OplTerm>> equations = new LinkedList<>();
		for (Tuple4 x : equations0) {
			List<Tuple3> fa = (List<Tuple3>) x.b;
			OplCtx<String> ctx = toCtx(fa);
			Tuple3 eq = (Tuple3) x.d;
			OplTerm lhs = toTerm(eq.a);
			OplTerm rhs = toTerm(eq.c);
			equations.add(new Triple<>(ctx, lhs, rhs));
		}
		
		return new OplExp.OplSig(prec, sorts, symbols, equations);
	}

	private static OplExp toModel(Object o) {
		Tuple3 t = (Tuple3) o;
		
		//org.codehaus.jparsec.functors.Pair a = (org.codehaus.jparsec.functors.Pair) t.a;
		org.codehaus.jparsec.functors.Pair b = (org.codehaus.jparsec.functors.Pair) t.b;
		org.codehaus.jparsec.functors.Pair c = (org.codehaus.jparsec.functors.Pair) t.c;
		
		Tuple3 y = (Tuple3) b.a;
		List<Tuple3> sorts = (List<Tuple3>) y.b;
		Map<String, Set<String>> sorts0 = new HashMap<>();
		for (Tuple3 x : sorts) {
			String s = x.a.toString();
			List<String> s0 = (List<String>) x.c;
			if (sorts0.containsKey(s)) {
				throw new DoNotIgnore("Duplicate sort: " + s);
			}
			Set<String> s1 = new HashSet<>(s0);
			if (s1.size() != s0.size()) {
				throw new DoNotIgnore("Duplicate member: " + s0);
			}
			sorts0.put(s, s1);
		}
		
		Map<String, Map<List<String>, String>> symbols0 = new HashMap<>();
		Tuple3 z = (Tuple3) b.b;
		List<Tuple3> q = (List<Tuple3>) z.b;
		for (Tuple3 r : q) {
			List<Tuple5> u = (List<Tuple5>) r.c;
			String fname = (String) r.a;
			if (symbols0.containsKey(fname)) {
				throw new DoNotIgnore("Duplicte symbol " + fname);
			}
			Map<List<String>, String> toadd = new HashMap<>();
			for (Tuple5 e : u) {
				List<String> args = (List<String>) e.b;
				String ret = (String) e.d;
				if (toadd.containsKey(args)) {
					throw new DoNotIgnore("Duplicate argument at " + args);
				}
				toadd.put(args, ret);
			}
			symbols0.put(fname, toadd);
		}
		return new OplSetInst(sorts0 , symbols0 , c.b.toString());
	}
	
	private static OplTerm toTerm(Object a) {
		if (a instanceof String) {
			return new OplTerm((String)a);
		}
		Tuple4 t = (Tuple4) a;
		String f = (String) t.a;
		List<Object> l = (List<Object>) t.c;
		List<OplTerm> l0 = l.stream().map(x -> toTerm(x)).collect(Collectors.toList());
		return new OplTerm(f, l0);
	}

	private static OplCtx<String> toCtx(List<Tuple3> fa) {
		List<Pair<String, String>> ret = new LinkedList<>();
		for (Tuple3 t : fa) {
			ret.add(new Pair<>(t.a.toString(), t.c.toString()));
		}
		return new OplCtx<String>(ret);
	}

	private static OplExp toExp(Object c) {
		if (c instanceof String) {
			return new OplVar((String) c);
		}
		try {
			return toTheory(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}
		catch (Exception ee) {
		}
		
		try {
			return toModel(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) { 
		}
		try {
			return toTrans(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) {
		}
		
		try {
			return toEval(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) { 
		}
		
		try {
			return toTransEval(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) { 
		}
		
		try {
			return toDelta(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) { 
		}
		
		try {
			return toJava(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) {
		}
		
		try {
			return toMapping(c);
		} catch (DoNotIgnore de) {
			de.printStackTrace();
			throw new RuntimeException(de.getMessage());
		}catch (Exception ee) {
			//ee.printStackTrace();
		}
		
		
		throw new RuntimeException("Report this error to Ryan.");
	}
	
	private static OplExp toMapping(Object c) {
		Tuple3 t = (Tuple3) c;

		org.codehaus.jparsec.functors.Pair aa = (org.codehaus.jparsec.functors.Pair) t.b;
		Tuple3 a = (Tuple3) aa.a;
		Tuple3 b = (Tuple3) aa.b;
		//Tuple3 b = (Tuple3) t.c;
		
		List<Tuple3> sorts = (List<Tuple3>) a.b;
		List<Tuple3> symbols = (List<Tuple3>) b.b;
		
		Map<String, String> sorts0 = new HashMap<>();
		Map<String, Pair<OplCtx<String>, OplTerm>> symbols0 = new HashMap<>();
		
		for (Tuple3 z : sorts) {
			String p = (String) z.a;
			if (sorts0.containsKey(p)) {
				throw new DoNotIgnore("Duplicate sort: " + p);
			}
			String q = (String) z.c;
			sorts0.put(p, q);
		}
		
		for (Tuple3 z : symbols) {
			String p = (String) z.a;
			if (sorts0.containsKey(p)) {
				throw new DoNotIgnore("Duplicate symbol: " + p);
			}
			Tuple4 q = (Tuple4) z.c;
			List<Tuple3> ctx = (List<Tuple3>) q.b;
			List<Pair<String, String>> ctx0 = new LinkedList<>();
			Set<String> seen = new HashSet<>();
			for (Tuple3 u : ctx) {
				String name = (String) u.a;
				String type = (String) u.c;
				if (seen.contains(name)) {
					throw new DoNotIgnore("Duplicate var: " + name);
				}
				seen.add(name);
				ctx0.add(new Pair<>(name, type));
			}
			symbols0.put(p, new Pair<>(new OplCtx<>(ctx0), toTerm(q.d)));
		}
		
		Tuple4 x = (Tuple4) t.c;
		String src0 = (String) x.b;
		String dst0 = (String) x.d;
		return new OplMapping(sorts0, symbols0, src0, dst0);
	}

	static class DoNotIgnore extends RuntimeException {

		public DoNotIgnore(String string) {
			super(string);
		}
		
	}
	
	public static OplExp toJava(Object x) {
	//	System.out.println(x);
		Tuple3 t = (Tuple3) x;
		
		Tuple3 b = (Tuple3) t.b;
		List<Tuple3> l = (List<Tuple3>) b.b;
		Map<String, String> defs = new HashMap<>();
		for (Tuple3 k : l) {
			String f = (String) k.a;
			String body = (String) k.c;
			if (defs.containsKey(f)) {
				throw new DoNotIgnore("Duplicate symbol: " + f);
			}
			defs.put(f, body);
		}
		
		org.codehaus.jparsec.functors.Pair c = (org.codehaus.jparsec.functors.Pair) t.c;		
		return new OplJavaInst(defs, (String) c.b);
	}
	
	public static OplExp toTrans(Object c) {
		Tuple3 t = (Tuple3) c;
		
		Map<String, Map<String, String>> map = new HashMap<>();
		Tuple3 tb = (Tuple3) t.b;
		List<Tuple5> l = (List<Tuple5>) tb.b;
		
		for (Tuple5 x : l) {
			String name = (String) x.a;
			List<Tuple5> y = (List<Tuple5>) x.d;
			Map<String, String> m = new HashMap<>();
			for (Tuple5 z : y) {
				String xx = (String) z.b;
				String yy = (String) z.d;
				if (m.containsKey(xx)) {
					throw new DoNotIgnore("Duplicate argument: " + xx);
				}
				m.put(xx,  yy);
			}
			if (map.containsKey(name)) {
				throw new DoNotIgnore("Duplicate sort: " + name);
			}
			map.put(name, m);
		}
		
		Tuple4 tc = (Tuple4) t.c;		
		return new OplSetTrans(map , (String) tc.b, (String) tc.d);
	}
	
	
	private static OplExp toEval(Object c) {
		Tuple3 t = (Tuple3) c;
		if (!t.a.toString().equals("eval")) {
			throw new RuntimeException();
		}
		String i = (String) t.b;
		OplTerm r = toTerm(t.c);
		return new OplEval(i, r);
	}
	
	private static OplExp toTransEval(Object c) {
		Tuple4 t = (Tuple4) c;
		if (!t.a.toString().equals("transeval")) {
			throw new RuntimeException();
		}
		String f = (String) t.b;
		OplTerm r = toTerm(t.d);
		String i = (String) t.c;
		return new OplTransEval(i, f, r);
	}
	
	private static OplExp toDelta(Object c) {
		Tuple3 t = (Tuple3) c;
		if (!t.a.toString().equals("delta")) {
			throw new RuntimeException();
		}
		String i = (String) t.b;
		String r = (String) t.c;
		return new OplDelta(i, r);
	}

	public static Parser<?> section(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p.sepBy(term(",")), term(";"));
	}

	 private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	} 
	 
	
	
	

}
