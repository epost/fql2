package catdata.aql;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;

import catdata.Triple;
import catdata.aql.InstExp.InstExpEmpty;
import catdata.aql.InstExp.InstExpVar;
import catdata.aql.MapExp.MapExpId;
import catdata.aql.MapExp.MapExpVar;
import catdata.aql.SchExp.SchExpEmpty;
import catdata.aql.SchExp.SchExpInst;
import catdata.aql.SchExp.SchExpVar;
import catdata.aql.TransExp.TransExpId;
import catdata.aql.TransExp.TransExpVar;
import catdata.aql.TyExp.TyExpEmpty;
import catdata.aql.TyExp.TyExpSch;
import catdata.aql.TyExp.TyExpVar;
import catdata.ide.Program;
import catdata.ide.Util;

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

	private static final Parser<Void> IGNORED = Parsers.or(
			Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT,
			Scanners.WHITESPACES).skipMany();

	private static final Parser<Object> TOKENIZER = Parsers.or(
			Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			RESERVED.tokenizer(), 
			Terminals.Identifier.TOKENIZER,
			Terminals.IntegerLiteral.TOKENIZER);

	private static Parser<Token> token(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<String> ident = 
		 Parsers.or(Terminals.StringLiteral.PARSER,Terminals.IntegerLiteral.PARSER,
				Terminals.Identifier.PARSER);
	

//	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);
	
//	public static final Parser<?> program() {
//		return Parsers.tuple(decl().source().peek(), decl()).many();
//	}

	
	
	public static final Parser<RawTerm> term() {
		Reference<RawTerm> ref = Parser.newReference();
		
		Parser<RawTerm> ann = Parsers.tuple(ident, token("@"), ident).map(x -> new RawTerm(x.a, x.c));
		
		Parser<RawTerm> app = Parsers.tuple(ident, token("("),
				ref.lazy().sepBy(token(",")), token(")")).map(x -> {
					return new RawTerm(x.a, x.c);
				});
		
		Parser<RawTerm> app2 = Parsers.tuple(token("("), ref.lazy(), ident,
				ref.lazy(), token(")")).map(x -> new RawTerm(x.c, Util.list(x.b, x.d)));
		
		Parser<RawTerm> sing = ident.map(x -> new RawTerm(x, new LinkedList<>()));
				
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

	private static final void tyExp() {
		Parser<TyExp<?, ?>> 
			var = ident.map(TyExpVar::new),
			empty = token("empty").map(x -> new TyExpEmpty()),
			sch = Parsers.tuple(token("typesideOf"), sch_ref.lazy()).map(x -> new TyExpSch<>(x.b)),
			ret = Parsers.or(sch, empty, tyExpRaw(), var);
		
		ty_ref.set(ret);
	}
	
	private static final void schExp() {		
		Parser<SchExp<?,?,?,?,?>> 
			var = ident.map(SchExpVar::new),
			empty = Parsers.tuple(token("empty"), ty_ref.get()).map(x -> new SchExpEmpty<>(x.b)),
			inst = Parsers.tuple(token("schemaOf"), inst_ref.lazy()).map(x -> new SchExpInst<>(x.b)),
			ret = Parsers.or(inst, empty, schExpRaw(), var);
		
		sch_ref.set(ret);
	}

	private static final void instExp() {
		Parser<InstExp<?,?,?,?,?,?,?>> 
//			schExpRaw = instExpRaw(),
			var = ident.map(InstExpVar::new),
			empty = Parsers.tuple(token("empty"), sch_ref.get()).map(x -> new InstExpEmpty<>(x.b)),
			ret = Parsers.or(empty, var);
		
		inst_ref.set(ret);
	}
	
	private static final void mapExp() {
		Parser<MapExp<?,?,?,?,?,?,?,?,?>> 
			var = ident.map(MapExpVar::new),
			id = Parsers.tuple(token("id"), sch_ref.lazy()).map(x -> new MapExpId<>(x.b)),
			ret = Parsers.or(id, var);
		
		map_ref.set(ret);
	}

	private static final void transExp() {
		Parser<TransExp<?,?,?,?,?,?,?,?,?>> 
			var = ident.map(TransExpVar::new),
			id = Parsers.tuple(token("id"), inst_ref.lazy()).map(x -> new TransExpId<>(x.b)),
			ret = Parsers.or(id, var);
		
		trans_ref.set(ret);
	}
	
	private  static final Parser<TyExp<String, String>> tyExpRaw() {
		return Parsers.fail("ty exp raw parser not implemented yet"); //TODO
	}
	
	private static Parser<SchExp<String, String, String, String, String>> schExpRaw() {
		return Parsers.fail("schema exp raw parser not implemented yet"); //TODO
	}

	/* private static Parser<InstExp<?, ?, String, String, String, String, String>> instExpRaw() {
		return Parsers.fail("inst exp raw parser not implemented yet"); //TODO
	}*/

/*	private static final Parser<PragmaExp> pragmaExp() {
		return Parsers.fail("pragma parser not implemented yet"); //TODO
	} */
/*	
	public static final Parser<TransExp> transExp() {
		return Parsers.fail("transform parser not implemented yet"); //TODO
	}
	
	public static final Parser<QueryExp> queryExp() {
		return Parsers.fail("query parser not implemented yet"); //TODO
	} */
	
	/*public static final Parser<MapExp> mapExpRaw() {
		return Parsers.fail("map exp raw parser not implemented yet"); //TODO
	}*/
	
/*	public static final Parser<?> pragma() {
		Parser<?> p = Parsers.tuple(Terminals.StringLiteral.PARSER, token("="),
				Terminals.StringLiteral.PARSER);
		Parser<?> foo = section("options", p);
		return Parsers.tuple(token("pragma"), token("{"), foo, (token("}")));
	} */




/*	private static <X> Parser<List<X>> section(String s, Parser<X> p) {
		Parser<Tuple3<Token,List<X>,Token>> ret = Parsers.tuple(token(s), p.sepBy(token(",")), token(";"));
		return ret.map(x -> x.b);
	} */
	
	private static final <Y>  Parser<Triple<String, Integer, Y>> decl(String s, Parser<Y> p) {
		return Parsers.tuple(token(s), ident, Parsers.INDEX, token("="), p).map(x -> new Triple<>(x.b, x.c, x.e));
	}
	
	private static final Reference<TyExp<?, ?>> ty_ref = Parser.newReference();
	private static final Reference<SchExp<?,?,?,?,?>> sch_ref = Parser.newReference();
	private static final Reference<InstExp<?, ?, ?,?,?,?,?>> inst_ref = Parser.newReference();
	private static final Reference<MapExp<?,?,?,?,?,?,?,?,?>> map_ref = Parser.newReference();
	private static final Reference<TransExp<?,?,?,?,?,?,?,?,?>> trans_ref = Parser.newReference();
	
	public static Parser<Program<Exp<?>>> program() { //TODO: should create single instance of this rather than fn
		tyExp();
		schExp();
		instExp();
		mapExp();
		transExp();
		
		Parser<Triple<String, Integer, ? extends Exp<?>>> p 
		= Parsers.or(decl("typeside", ty_ref.get()),
					 decl("schema", sch_ref.get()), 
					 decl("instance", inst_ref.get()),
					 decl("mapping", map_ref.get()),
					 decl("transform", trans_ref.get())
					 //decl("query", queryExp()),
					 //decl("pragma", pragmaExp())
					 );
		
		return p.many().map(x -> new Program<>(conv(x)));
	}
	
	private static <X > List<Triple<String, Integer,Exp<?>>> conv(List<Triple<String, Integer, ? extends Exp<?>>> l) {
		List<Triple<String, Integer,Exp<?>>> ret = new LinkedList<>();
		for (Triple<String, Integer, ?extends Exp<?>> k : l) {	
			ret.add(new Triple<>(k.first, k.second, k.third));
		}
		return ret; 
	}
	
	public static final Program<Exp<?>> parseProgram(String s) {
		return AqlParser.program().from(TOKENIZER, IGNORED).parse(s);
	}
	


}
