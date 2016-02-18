package catdata.mpl;

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

import catdata.Pair;
import catdata.Triple;
import catdata.ide.Program;
import catdata.mpl.Mpl.MplExp;
import catdata.mpl.Mpl.MplExp.MplEval;
import catdata.mpl.Mpl.MplExp.MplSch;
import catdata.mpl.Mpl.MplExp.MplVar;
import catdata.mpl.Mpl.MplTerm;
import catdata.mpl.Mpl.MplTerm.MplAlpha;
import catdata.mpl.Mpl.MplTerm.MplComp;
import catdata.mpl.Mpl.MplTerm.MplConst;
import catdata.mpl.Mpl.MplTerm.MplId;
import catdata.mpl.Mpl.MplTerm.MplLambda;
import catdata.mpl.Mpl.MplTerm.MplPair;
import catdata.mpl.Mpl.MplTerm.MplRho;
import catdata.mpl.Mpl.MplTerm.MplTr;
import catdata.mpl.Mpl.MplType;
import catdata.mpl.Mpl.MplType.MplBase;
import catdata.mpl.Mpl.MplType.MplProd;
import catdata.mpl.Mpl.MplType.MplUnit;

public class MplParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "?", "@" };

	static String[] res = new String[] { 
		"tr", "id", "I", "theory", "eval", "sorts", "symbols", "equations", "lambda1", "lambda2", "rho1", "rho2", "alpha1", "alpha2"
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
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.Identifier.PARSER);
	}

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	public static final Parser<?> program() {
		return Parsers.tuple(decl().source().peek(), decl()).many();
	}
	
	
	


	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> exp() {
		Reference ref = Parser.newReference();

		Parser<?> eval = Parsers.tuple(term("eval"), ident(), term());
		
		Parser<?> a = Parsers.or(new Parser<?>[] { theory(), eval });
		
		ref.set(a);

		return a;
	}	
	
	 
	public static final Parser<?> eq() {
		return Parsers.tuple(term(), term("="), term());
	}


	public static final Parser<?> term() {
		Reference ref = Parser.newReference();

		Parser<?> prod = Parsers.tuple(term("("), ref.lazy(), term("*"), ref.lazy(), term(")"));
		Parser<?> comp = Parsers.tuple(term("("), ref.lazy(), term(";"), ref.lazy(), term(")"));
		Parser<?> alpha1 = Parsers.tuple(term("alpha1"), type(), type(), type());
		Parser<?> alpha2 = Parsers.tuple(term("alpha2"), type(), type(), type());
		Parser<?> lambda1 = Parsers.tuple(term("lambda1"), type() );
		Parser<?> lambda2 = Parsers.tuple(term("lambda2"), type() );
		Parser<?> rho1 = Parsers.tuple(term("rho1"), type() );
		Parser<?> rho2 = Parsers.tuple(term("rho2"), type() );
		Parser<?> id = Parsers.tuple(term("id"), type());
		Parser<?> tr = Parsers.tuple(term("tr"), ref.lazy());

		Parser<?> rho = Parsers.tuple(term("("), ref.lazy(), term("*"), ref.lazy(), term(")"));
		
		Parser<?> a = Parsers.or(new Parser<?>[] { tr, id, ident(), prod, comp, alpha1, alpha2, lambda1, lambda2, rho1, rho2 });
		
		ref.set(a);

		return a;
		
	}

	public static final Parser<?> type() {
		Reference ref = Parser.newReference();

		Parser<?> prod = Parsers.tuple(term("("), ref.lazy(), term("*"), ref.lazy(), term(")"));
		
		Parser<?> a = Parsers.or(new Parser<?>[] { term("I"), ident(), prod });
		
		ref.set(a);

		return a;
	}
	
	public static final Parser<?> theory() {
		Parser<?> z1 = Parsers.tuple(type(), term("->"), type());
		
		Parser<?> p = Parsers.tuple(ident().sepBy1(term(",")), term(":"), z1);
		Parser<?> foo = Parsers.tuple(section("sorts", ident()), 
				section("symbols", p),
				section("equations", eq()));
		return Parsers.tuple(Parsers.constant("theory"), Parsers.between(term("theory").followedBy(term("{")), foo, term("}")));
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> decl() {		
		Parser p1 = Parsers.tuple(ident(), term("="), exp());
		
		return Parsers.or(new Parser[] { p1 });
	}	
	
	
	public static Parser<?> section2(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p, term(";"));
	}
		
	
	@SuppressWarnings({ "rawtypes" })
	public static final Program<MplExp<String, String>> program(String s) {
		List<Triple<String, Integer, MplExp<String, String>>> ret = new LinkedList<>();
		List decls = (List) program.parse(s);

		 for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Tuple3 decl = (Tuple3) pr.b;
			
			toProgHelper(pr.a.toString(), s, ret, decl);
		}

		return new Program<MplExp<String,String>>(ret);  
	}
	
	static MplExp<String,String> toExp(Object o) {
		if (o instanceof String) {
			return new MplVar<>((String)o);
		}
		if (o instanceof Tuple3) {
			return toTheory(o);
		}
		if (o instanceof org.codehaus.jparsec.functors.Pair) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			if (p.a.toString().equals("theory")) {
				return toTheory(p.b);
			}
			return new MplEval<>(new MplVar<>((String)p.a), toTerm(p.b));
		}
			throw new RuntimeException();
		//}
		//return toTheory(o);
	}

	private static void toProgHelper(String txt, String s, List<Triple<String, Integer, MplExp<String,String>>> ret, Tuple3 decl) {
		int idx = s.indexOf(txt);
		if (idx < 0) {
			throw new RuntimeException();
		}

		String name = decl.a.toString();
		ret.add(new Triple<>(name, idx, toExp(decl.c)));
	}
	
	private static MplTerm<String,String> toTerm(Object o) {
		if (o instanceof Tuple5) {
			Tuple5 t = (Tuple5) o;
			if (t.c.toString().equals(";")) {
				return new MplComp<>(toTerm(t.b), toTerm(t.d));
			}
			if (t.c.toString().equals("*")) {
				return new MplPair<>(toTerm(t.b), toTerm(t.d));
			}
		}
		if (o instanceof Tuple4) {
			Tuple4 t = (Tuple4) o;
			if (t.a.toString().equals("alpha1")) {
				return new MplAlpha<>(toType(t.b), toType(t.c), toType(t.d), true);
			}
			if (t.a.toString().equals("alpha2")) {
				return new MplAlpha<>(toType(t.b), toType(t.c), toType(t.d), false);
			}
		}
		if (o instanceof org.codehaus.jparsec.functors.Pair) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			if (p.a.toString().equals("id")) {
				return new MplId(toType(p.b));
			}
			if (p.a.toString().equals("lambda1")) {
				return new MplLambda<>(toType(p.b), true);
			}
			if (p.a.toString().equals("lambda2")) {
				return new MplLambda<>(toType(p.b), false);
			}
			if (p.a.toString().equals("rho1")) {
				return new MplRho<>(toType(p.b), true);
			}
			if (p.a.toString().equals("rho2")) {
				return new MplRho<>(toType(p.b), false);
			}
			if (p.a.toString().equals("tr")) {
				return new MplTr<String, String>(toTerm(p.b));
			}
		}
		if (o instanceof String) {
			return new MplConst<String,String>((String)o);
		}
		
		throw new RuntimeException(o.toString());
		
	}
	
	private static MplType<String> toType(Object o) {
		if (o instanceof String) {
			return new MplBase<>((String)o);
		}
		if (o instanceof Token) {
			return new MplUnit<>();
		}
		Tuple5 t = (Tuple5) o;
		return new MplProd<>(toType(t.b), toType(t.d));
	}
	private static MplExp toTheory(Object o) {
		Tuple3 t = (Tuple3) o;
		
		Tuple3 a = (Tuple3) t.a;
		Tuple3 b = (Tuple3) t.b;
		Tuple3 c = (Tuple3) t.c;
				
		Set<String> sorts = new HashSet<>((List<String>) a.b);
		
		List<Tuple3> symbols0 = (List<Tuple3>) b.b;
		List<Tuple3> equations0 = (List<Tuple3>) c.b;

		Map<String, Pair<MplType<String>, MplType<String>>> symbols = new HashMap<>();
		for (Tuple3 x : symbols0) {
			MplType<String> dom;
			MplType<String> args;
				Tuple3 zzz = (Tuple3) x.c;
				args = toType( zzz.a );
				dom = toType( zzz.c );
			
			List<String> name0s = (List<String>) x.a;
			for (String name : name0s) {
			
			if (symbols.containsKey(name)) {
				throw new DoNotIgnore("Duplicate symbol " + name);
			}
			symbols.put(name, new Pair<>(args, dom));
			}		
		}
		
		Set<Pair<MplTerm<String,String>, MplTerm<String,String>>> equations = new HashSet<>();
		for (Tuple3 eq : equations0) {
			MplTerm<String,String> lhs = toTerm(eq.a);
			MplTerm<String,String> rhs = toTerm(eq.c);
			equations.add(new Pair<>(lhs, rhs));
		}
		
		
		return new MplSch<String, String>(sorts, symbols, equations);
	}

	
	
	public static class DoNotIgnore extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public DoNotIgnore(String string) {
			super(string);
		}
		
	}
	
	

	public static Parser<?> section(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p.sepBy(term(",")), term(";"));
	}

	 private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	} 
	 
	


}
