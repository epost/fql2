package catdata.aql;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Tuple3;

import catdata.Triple;
import catdata.aql.InstExp.InstExpEmpty;
import catdata.aql.InstExp.InstExpVar;
import catdata.aql.SchExp.SchExpEmpty;
import catdata.aql.SchExp.SchExpInst;
import catdata.aql.SchExp.SchExpVar;
import catdata.aql.TyExp.TyExpEmpty;
import catdata.aql.TyExp.TyExpSch;
import catdata.aql.TyExp.TyExpVar;
import catdata.ide.Program;
import catdata.ide.Util;

//TODO: move from invoking this methods (eg get) many times to having a just one copy of each parser

public class AqlParser {

	 static final Parser<String> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, String>() {
				public String map(String s) {
					Integer.valueOf(s);
					return s;
				}
			}); 

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "?", "@" };	
	
	static String[] res = new String[] {
			"typeside", "schema", "mapping", "instance", "transform", "query", "pragma", "graph",
			
			"literal",
			"id",
			"empty",
			"imports",
			"types",
			"constants",
			"functions",
			"equations",
			"forall", 		
			"java",
			"options",
			"entities",
			"path",
			"observation",
			"generators",
			"labelled nulls",
			"lambda",
			"sigma",
			"delta",
			"pi",
			"unit",
			"counit",
			"eval",
			"coeval",
			"ed",
			"chase",
			"for",
			"where",
			"return",
			"pivot",
			"copivot",
			"colimit",
			"nodes",
			"edges",
			"typesideOf",
			"schemaOf"
			};

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);

	public static final Parser<Void> IGNORED = Parsers.or(
			Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT,
			Scanners.WHITESPACES).skipMany();

	public static final Parser<Object> TOKENIZER = Parsers.or(
			Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			RESERVED.tokenizer(), 
			Terminals.Identifier.TOKENIZER,
			Terminals.IntegerLiteral.TOKENIZER);

	static Parser<Token> token(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<String> ident() {
		return Parsers.or(Terminals.StringLiteral.PARSER,Terminals.IntegerLiteral.PARSER,
				Terminals.Identifier.PARSER);
	}

//	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);
	
//	public static final Parser<?> program() {
//		return Parsers.tuple(decl().source().peek(), decl()).many();
//	}

	
	
	public static final Parser<RawTerm> term() {
		Reference<RawTerm> ref = Parser.newReference();
		
		Parser<RawTerm> ann = Parsers.tuple(ident(), token("@"), ident()).map(x -> new RawTerm(x.a, x.c));
		
		Parser<RawTerm> app = Parsers.tuple(ident(), token("("),
				ref.lazy().sepBy(token(",")), token(")")).map(x -> {
					return new RawTerm(x.a, x.c);
				});
		
		Parser<RawTerm> app2 = Parsers.tuple(token("("), ref.lazy(), ident(),
				ref.lazy(), token(")")).map(x -> new RawTerm(x.c, Util.list(x.b, x.d)));
		
		Parser<RawTerm> sing = ident().map(x -> new RawTerm(x, new LinkedList<>()));
				
		Parser<RawTerm> ret = Parsers.or(ann, app, app2, sing);
		
		ref.set(ret);
		return ret;
	}

	/*
	 * typeside ty = literal {
	imports
		Bool;
	types
		Nat, 
		String;
	constants
		zero : Nat,
	functions	
		append : String -> String;
		succ : Nat -> Nat,
		plus : Nat,Nat -> Nat;
	equations
		forall x, y:Nat, plus(succ(x),y) = succ(plus(x,y));
	java types
		String;
	java constants
		String -> "return input[0]";
	java functions
		append -> "return input[0] + input[1]";		 	
	options
		"prover" = "completion",
		"timeout" = "500",
		"precedence" = "plus,succ,zero";
}
	 */

	public final void tyExp() {
		Parser<TyExp<?, ?>> var = ident().map(TyExpVar::new);
		Parser<TyExp<Void, Void>> empty = token("empty").map(x -> new TyExpEmpty());
		Parser<TyExp<?,?>> sch = Parsers.tuple(token("typesideOf"), sch_ref.lazy()).map(x -> new TyExpSch<>(x.b));
		Parser<TyExp<? extends Object, ? extends Object>> 
		 ret = Parsers.or(sch, empty, tyExpRaw(), var);
		
		ty_ref.set(ret);
		
	//	return ret;
	}
	
	public final void schExp() {		
		Parser<SchExp<?,?,?,?,?>> var = ident().map(SchExpVar::new);
		Parser<SchExp<?,Void,?,Void,Void>> empty = Parsers.tuple(token("empty"), ty_ref.get()).map(x -> new SchExpEmpty<>(x.b));
		Parser<SchExp<?,?,?,?,?>> inst = Parsers.tuple(token("schemaOf"), inst_ref.lazy()).map(x -> new SchExpInst<>(x.b));

		Parser<SchExp<? extends Object,? extends Object,? extends Object,? extends Object,? extends Object>> 
		 ret = Parsers.or(inst, empty, schExpRaw(), var);
		
		sch_ref.set(ret);
//		return ret;
	}

	public final void instExp() {
		Parser<InstExp<? extends Object, ? extends Object, String,String,String,String, String>> schExpRaw = instExpRaw();
		Parser<InstExp<?,?,?,?,?,?,?>> var = ident().map(InstExpVar::new);
		Parser<InstExp<? extends Object, ? extends Object, ? extends Object, ? extends Object, ? extends Object, Void, Void>> 
		 empty = Parsers.tuple(token("empty"), sch_ref.get()).map(x -> new InstExpEmpty<>(x.b));
		
		Parser<InstExp<? extends Object, ? extends Object,? extends Object,? extends Object,? extends Object,? extends Object,? extends Object>> ret
		= Parsers.or(schExpRaw, empty, var);
		
		inst_ref.set(ret);
//		return ret;

	} 

	public static final Parser<TyExp<String, String>> tyExpRaw() {
		return Parsers.fail("ty exp raw parser not implemented yet"); //TODO
	}
	
	private static Parser<SchExp<String, String, String, String, String>> schExpRaw() {
		return Parsers.fail("schema exp raw parser not implemented yet"); //TODO
	}

	private static Parser<InstExp<? extends Object, ? extends Object, String, String, String, String, String>> instExpRaw() {
		return Parsers.fail("inst exp raw parser not implemented yet"); //TODO
	}

	public static final Parser<PragmaExp> pragmaExp() {
		return Parsers.fail("pragma parser not implemented yet"); //TODO
	}
	
	public static final Parser<TransExp> transExp() {
		return Parsers.fail("transform parser not implemented yet"); //TODO
	}
	
	public static final Parser<QueryExp> queryExp() {
		return Parsers.fail("query parser not implemented yet"); //TODO
	}
	
	public static final Parser<MapExp> mapExp() {
		return Parsers.fail("mapping parser not implemented yet"); //TODO
	}
	
/*	public static final Parser<?> pragma() {
		Parser<?> p = Parsers.tuple(Terminals.StringLiteral.PARSER, token("="),
				Terminals.StringLiteral.PARSER);
		Parser<?> foo = section("options", p);
		return Parsers.tuple(token("pragma"), token("{"), foo, (token("}")));
	} */




	public static <X> Parser<List<X>> section(String s, Parser<X> p) {
		Parser<Tuple3<Token,List<X>,Token>> ret = Parsers.tuple(token(s), p.sepBy(token(",")), token(";"));
		return ret.map(x -> x.b);
	}
	
	public static final <Y>  Parser<Triple<String, Integer, Y>> decl(String s, Parser<Y> p) {
		return Parsers.tuple(token(s), ident(), Parsers.INDEX, token("="), p).map(x -> new Triple<>(x.b, x.c, x.e));
	}
	

	Reference<TyExp<? extends Object, ? extends Object>> ty_ref = Parser.newReference();
	Reference<SchExp<? extends Object,? extends Object,? extends Object,? extends Object,? extends Object>> sch_ref = Parser.newReference();
	Reference<InstExp<? extends Object, ? extends Object, ? extends Object,? extends Object,? extends Object,? extends Object,? extends Object>> inst_ref = Parser.newReference();

	public Parser<Program<Exp<? extends Object>>> program() {
		tyExp();
		schExp();
		instExp();
		
		Parser<Triple<String, Integer, ? extends Exp<? extends Object>>> p 
		= Parsers.or(decl("typeside", ty_ref.get()),
					 decl("schema", sch_ref.get()), 
					 decl("instance", inst_ref.get()),
					 decl("transform", transExp()),
					 decl("mapping", mapExp()),
					 decl("query", queryExp()),
					 decl("pragma", pragmaExp()));
		
		return p.many().map(x -> new Program<>(conv(x)));
	}
	
	static <X extends Object> List<Triple<String, Integer,Exp<? extends Object>>> conv(List<Triple<String, Integer, ? extends Exp<? extends Object>>> l) {
		List<Triple<String, Integer,Exp<? extends Object>>> ret = new LinkedList<>();
		for (Triple<String, Integer, ? extends Exp<? extends Object>> k : l) {	
			ret.add(new Triple<>(k.first, k.second, k.third));
		}
		return ret; 
	}
	
	public static final Program<Exp<? extends Object>> parseProgram(String s) {
		return new AqlParser().program().from(TOKENIZER, IGNORED).parse(s);
	}
	


}
