package catdata.aql.exp;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jparsec.Parser;
import org.jparsec.Parser.Reference;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Terminals.Identifier;
import org.jparsec.Terminals.IntegerLiteral;
import org.jparsec.Terminals.StringLiteral;
import org.jparsec.Token;
import org.jparsec.functors.Pair;
import org.jparsec.functors.Tuple3;
import org.jparsec.functors.Tuple4;
import org.jparsec.functors.Tuple5;

import catdata.Program;
import catdata.Quad;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.RawTerm;
import catdata.aql.exp.ColimSchExp.ColimSchExpModify;
import catdata.aql.exp.ColimSchExp.ColimSchExpQuotient;
import catdata.aql.exp.ColimSchExp.ColimSchExpRaw;
import catdata.aql.exp.ColimSchExp.ColimSchExpVar;
import catdata.aql.exp.ColimSchExp.ColimSchExpWrap;
import catdata.aql.exp.EdsExp.EdExpRaw;
import catdata.aql.exp.EdsExp.EdsExpRaw;
import catdata.aql.exp.EdsExp.EdsExpVar;
import catdata.aql.exp.GraphExp.GraphExpRaw;
import catdata.aql.exp.GraphExp.GraphExpVar;
import catdata.aql.exp.InstExp.InstExpChase;
import catdata.aql.exp.InstExp.InstExpCoEq;
import catdata.aql.exp.InstExp.InstExpCoEval;
import catdata.aql.exp.InstExp.InstExpCoProdFull;
import catdata.aql.exp.InstExp.InstExpCoProdSigma;
import catdata.aql.exp.InstExp.InstExpCod;
import catdata.aql.exp.InstExp.InstExpColim;
import catdata.aql.exp.InstExp.InstExpDelta;
import catdata.aql.exp.InstExp.InstExpDistinct;
import catdata.aql.exp.InstExp.InstExpDom;
import catdata.aql.exp.InstExp.InstExpEmpty;
import catdata.aql.exp.InstExp.InstExpEval;
import catdata.aql.exp.InstExp.InstExpRandom;
import catdata.aql.exp.InstExp.InstExpSigma;
import catdata.aql.exp.InstExp.InstExpVar;
import catdata.aql.exp.MapExp.MapExpColim;
import catdata.aql.exp.MapExp.MapExpComp;
import catdata.aql.exp.MapExp.MapExpId;
import catdata.aql.exp.MapExp.MapExpVar;
import catdata.aql.exp.PragmaExp.PragmaExpCheck;
import catdata.aql.exp.PragmaExp.PragmaExpConsistent;
import catdata.aql.exp.PragmaExp.PragmaExpJs;
import catdata.aql.exp.PragmaExp.PragmaExpLoadJars;
import catdata.aql.exp.PragmaExp.PragmaExpMatch;
import catdata.aql.exp.PragmaExp.PragmaExpProc;
import catdata.aql.exp.PragmaExp.PragmaExpSql;
import catdata.aql.exp.PragmaExp.PragmaExpToCsvInst;
import catdata.aql.exp.PragmaExp.PragmaExpToCsvTrans;
import catdata.aql.exp.PragmaExp.PragmaExpToJdbcInst;
import catdata.aql.exp.PragmaExp.PragmaExpToJdbcQuery;
import catdata.aql.exp.PragmaExp.PragmaExpToJdbcTrans;
import catdata.aql.exp.PragmaExp.PragmaExpVar;
import catdata.aql.exp.QueryExp.QueryExpDeltaCoEval;
import catdata.aql.exp.QueryExp.QueryExpDeltaEval;
import catdata.aql.exp.QueryExp.QueryExpId;
import catdata.aql.exp.QueryExp.QueryExpVar;
import catdata.aql.exp.QueryExpRaw.Block;
import catdata.aql.exp.QueryExpRaw.Trans;
import catdata.aql.exp.SchExp.SchExpCod;
import catdata.aql.exp.SchExp.SchExpColim;
import catdata.aql.exp.SchExp.SchExpEmpty;
import catdata.aql.exp.SchExp.SchExpInst;
import catdata.aql.exp.SchExp.SchExpVar;
import catdata.aql.exp.TransExp.TransExpCoEval;
import catdata.aql.exp.TransExp.TransExpCoEvalEvalCoUnit;
import catdata.aql.exp.TransExp.TransExpCoEvalEvalUnit;
import catdata.aql.exp.TransExp.TransExpDelta;
import catdata.aql.exp.TransExp.TransExpDistinct;
import catdata.aql.exp.TransExp.TransExpEval;
import catdata.aql.exp.TransExp.TransExpId;
import catdata.aql.exp.TransExp.TransExpSigma;
import catdata.aql.exp.TransExp.TransExpSigmaDeltaCounit;
import catdata.aql.exp.TransExp.TransExpSigmaDeltaUnit;
import catdata.aql.exp.TransExp.TransExpVar;
import catdata.aql.exp.TyExp.TyExpEmpty;
import catdata.aql.exp.TyExp.TyExpSch;
import catdata.aql.exp.TyExp.TyExpSql;
import catdata.aql.exp.TyExp.TyExpVar;

@SuppressWarnings("deprecation")
public class AqlParser {

	public static final String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(", ")", "=", "->", "@", "(*",
			"*)", "+", "[", "]", "<-" };

	public static final String[] res = new String[] { "md", "quotient_jdbc", "random", "sql", "chase", "check",
			"import_csv", "quotient_csv", "coproduct_unrestricted",
			"simple", "assert_consistent", "coproduct_sigma", "coequalize", "html", "quotient", "entity_equations",
			"schema_colimit", "exists", "constraints", "getMapping", "getSchema", "typeside", "schema", "mapping",
			"instance", "transform", "query", "pragma", "graph", "exec_jdbc", "exec_js", "exec_cmdline", "literal",
			"add_to_classpath", // TODO aql not officially supported
			"id", "match", "attributes", "empty", "imports", "types", "constants", "functions", "equations", "forall",
			"java_types", "multi_equations", // TODO aql colorize multi
			"toQuery",
			"toCoQuery",									// equations
			"java_constants", "java_functions", "options", "entities", "src", "unique", "dst", "path_equations",
			"observation_equations", "generators", "rename", "remove", "modify",
			// "labelled nulls",
			"foreign_keys", "lambda", "sigma", "delta", "pi", "unit", "counit", "eval", "coeval", "ed", "chase", "from",
			"where", "return", "pivot", "copivot", "colimit", "nodes", "edges", "typesideOf", "schemaOf", "distinct",
			"import_csv", "export_csv_instance", "export_csv_transform", "import_jdbc", "import_jdbc_all",
			"export_jdbc_transform", "export_jdbc_instance", "export_jdbc_query", "unit_query", "counit_query",
			"coproduct", "wrap" };

	public static final String[] res2 = AqlOptions.optionNames().toArray(new String[0]);

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, Util.union(res, res2));

	private static final Parser<Void> IGNORED = Parsers
			.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	private static final Parser<Object> TOKENIZER = Parsers.or(StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			RESERVED.tokenizer(), Identifier.TOKENIZER, IntegerLiteral.TOKENIZER);

	private static Parser<Token> token(String... names) {
		return RESERVED.token(names);
	}

	private static final Parser<String> ident = Parsers.or(StringLiteral.PARSER, IntegerLiteral.PARSER,
			Identifier.PARSER);

	private static final Parser<LocStr> locstr = Parsers.tuple(Parsers.INDEX, ident).map(x -> new LocStr(x.a, x.b));

	// public static final Parser<?> program = program().from(TOKENIZER,
	// IGNORED);

	// public static final Parser<?> program() {
	// return Parsers.tuple(decl().source().peek(), decl()).many();
	// }

	private static Parser<RawTerm> term() {
		Reference<RawTerm> ref = Parser.newReference();

		Parser<RawTerm> ann = Parsers.tuple(ident, token("@"), ident).map(x -> new RawTerm(x.a, x.c));

		Parser<RawTerm> app = Parsers.tuple(ident, token("("), ref.lazy().sepBy(token(",")), token(")"))
				.map(x -> new RawTerm(x.a, x.c));

		/*
		 * Parser<RawTerm> appH = Parsers.tuple(ident, ref.lazy().many()).map(x
		 * -> { return new RawTerm(x.a, x.b); });
		 */ // appH probs won't work

		Parser<RawTerm> app2 = Parsers.tuple(token("("), ref.lazy(), ident, ref.lazy(), token(")"))
				.map(x -> new RawTerm(x.c, Util.list(x.b, x.d)));

		Parser<RawTerm> sing = ident.map(x -> new RawTerm(x, new LinkedList<>()));

		// use of ref.lazy for first argument leads to left recursion
		Parser<RawTerm> dot = Parsers.tuple(
				ident.label(
						"\n\n **** Possible problem: only identifiers allowed in . notation (lest left-recusion ensue)\n\n"),
				(Parsers.tuple(token("."), ident).map(x -> x.b)).many1()).map(x -> {
					RawTerm r = new RawTerm(x.a, Collections.emptyList());
					for (String s : x.b) {
						r = new RawTerm(s, Util.singList(r));
					}
					return r;
				});

		Parser<RawTerm> ret = Parsers.or(ann, app, app2, dot,
				/* appH, */ sing /* ,parens(ref) */); // conflicts with infix?
														// TODO aql

		ref.set(ret);
		return ret;
	}

	private static <X> Parser<X> parens(Reference<X> p) {
		Parser<X> q = Parsers.tuple(token("("), p.lazy(), token(")")).map(x -> x.b);
		return q;
	}

	private static void tyExp() {
		Parser<TyExp<?, ?>> var = ident.map(TyExpVar::new), sql = token("sql").map(x -> new TyExpSql()),
				empty = token("empty").map(x -> new TyExpEmpty()),
				sch = Parsers.tuple(token("typesideOf"), sch_ref.lazy()).map(x -> new TyExpSch<>(x.b)),
				ret = Parsers.or(sch, empty, sql, tyExpRaw(), var, parens(ty_ref));

		ty_ref.set(ret);
	}

	private static void schExp() {
		Parser<SchExp<?, ?, ?, ?, ?>> var = ident.map(SchExpVar::new),
				empty = Parsers.tuple(token("empty"), token(":"), ty_ref.get()).map(x -> new SchExpEmpty<>(x.c)),
				inst = Parsers.tuple(token("schemaOf"), inst_ref.lazy()).map(x -> new SchExpInst<>(x.b)),
				colim = Parsers.tuple(token("getSchema"), colim_ref.lazy()).map(x -> new SchExpColim<>(x.b)),
				cod = Parsers.tuple(token("dst"), query_ref.lazy()).map(x -> new SchExpCod<>(x.b)),
				ret = Parsers.or(inst, empty, schExpRaw(), var, colim, parens(sch_ref), cod);

		sch_ref.set(ret);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void pragmaExp() {
		Parser<Pair<List<String>, List<catdata.Pair<String, String>>>> p = Parsers.tuple(ident.many(), options)
				.between(token("{"), token("}"));

		Parser<PragmaExp> var = ident.map(PragmaExpVar::new), 
				
				csvInst = Parsers
				.tuple(token("export_csv_instance"), inst_ref.lazy(), ident,
						options.between(token("{"), token("}")).optional())
				.map(x -> new PragmaExpToCsvInst(x.b, x.c, x.d == null ? new LinkedList<>() : x.d)),

				csvTrans = Parsers
						.tuple(token("export_csv_transform"), trans_ref.lazy(), ident,
								options.between(token("{"), token("}")).optional(), options.between(token("{"), token("}")).optional())
						.map(x -> new PragmaExpToCsvTrans(x.b, x.c, x.d == null ? new LinkedList<>() : x.d, x.e == null ? new LinkedList<>() : x.e)),

				sql = Parsers.tuple(token("exec_jdbc"), ident, ident, p)
						.map(x -> new PragmaExpSql(x.b, x.c, x.d.a, x.d.b)),

				js = Parsers.tuple(token("exec_js"), p).map(x -> new PragmaExpJs(x.b.a, x.b.b)),
				proc = Parsers.tuple(token("exec_cmdline"), p).map(x -> new PragmaExpProc(x.b.a, x.b.b)),

				jdbcInst = Parsers
						.tuple(Parsers.tuple(token("export_jdbc_instance"), inst_ref.lazy()), ident, ident, ident,
								options.between(token("{"), token("}")).optional())
						.map(x -> new PragmaExpToJdbcInst(x.a.b, x.b, x.c, x.d,
								x.e == null ? new LinkedList<>() : x.e)),

				jdbcQuery = Parsers.tuple(Parsers.tuple(token("export_jdbc_query"), query_ref.lazy()),
						Parsers.tuple(ident, ident, ident, ident), options.between(token("{"), token("}")).optional())
						.map(x -> new PragmaExpToJdbcQuery(x.a.b, x.b.a, x.b.b, x.b.c, x.b.d,
								x.c == null ? new LinkedList<>() : x.c)),

				jdbcTrans = Parsers
						.tuple(Parsers.tuple(token("export_jdbc_transform"), trans_ref.lazy()), ident, ident, ident,
								Parsers.tuple(options.between(token("{"), token("}")).optional(), options.between(token("{"), token("}")).optional()))
						.map(x -> new PragmaExpToJdbcTrans(x.a.b, x.b, x.c, x.d,
								x.e.a == null ? new LinkedList<>() : x.e.a, x.e.b == null ? new LinkedList<>() : x.e.b)),

				match = Parsers
						.tuple(token("match"), ident.followedBy(token(":")), graph_ref.lazy().followedBy(token("->")),
								graph_ref.lazy(), options.between(token("{"), token("}")).optional())
						.map(x -> new PragmaExpMatch(x.b, x.c, x.d, x.e == null ? new LinkedList<>() : x.e)),

				load = Parsers.tuple(token("add_to_classpath"), token("{"), ident.many(), token("}"))
						.map(x -> new PragmaExpLoadJars(x.c)),

				check = Parsers.tuple(token("check"), edsExp(), inst_ref.lazy()).map(x -> new PragmaExpCheck(x.c, x.b)),

				cons = Parsers.tuple(token("assert_consistent"), inst_ref.lazy())
						.map(x -> new PragmaExpConsistent(x.b)),

				ret = Parsers.or(jdbcQuery, check, csvInst, cons, csvTrans, var, sql, js, proc, jdbcInst, jdbcTrans,
						match, load, parens(pragma_ref));

		pragma_ref.set(ret);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void instExp() {
		
		Parser<InstExpCoProdFull> l2 = Parsers.tuple(token("coproduct_unrestricted"),
				ident.sepBy(token("+")), token(":"), sch_ref.lazy(), options.between(token("{"), token("}")).optional()).
				map(x -> new InstExpCoProdFull(x.b, x.d, Util.newIfNull(x.e)));

		
		Parser<InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>> var = ident.map(InstExpVar::new),
				empty = Parsers.tuple(token("empty"), token(":"), sch_ref.get()).map(x -> new InstExpEmpty<>(x.c)),
				sigma = Parsers
						.tuple(token("sigma"), map_ref.lazy(), inst_ref.lazy(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new InstExpSigma(x.b, x.c, x.d == null ? new HashMap<>() : Util.toMapSafely(x.d))),
				delta = Parsers.tuple(token("delta"), map_ref.lazy(), inst_ref.lazy())
						.map(x -> new InstExpDelta(x.b, x.c)),
				distinct = Parsers.tuple(token("distinct"), inst_ref.lazy()).map(x -> new InstExpDistinct(x.b)),
				eval = Parsers
						.tuple(token("eval"), query_ref.lazy(), inst_ref.lazy(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new InstExpEval(x.b, x.c, x.d == null ? new LinkedList<>() : x.d)),
				dom = Parsers.tuple(token("src"), trans_ref.lazy()).map(x -> new InstExpDom(x.b)),
				cod = Parsers.tuple(token("dst"), trans_ref.lazy()).map(x -> new InstExpCod(x.b)),
				chase = Parsers
						.tuple(token("chase"), edsExp(), inst_ref.lazy(), IntegerLiteral.PARSER,
								options.between(token("{"), token("}")).optional())
						.map(x -> new InstExpChase(x.b, x.c, Integer.parseInt(x.d),
								x.e == null ? new LinkedList<>() : x.e)),
			
				coeval = Parsers
						.tuple(token("coeval"), query_ref.lazy(), inst_ref.lazy(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new InstExpCoEval(x.b, x.c, x.d == null ? new LinkedList<>() : x.d));

		Parser ret = Parsers.or(l2, instExpCsvQuot(), instExpJdbcQuot(), instExpCoProd(), instExpRand(), instExpCoEq(), instExpJdbcAll(),
				chase, instExpJdbc(), empty, instExpRaw(), var, sigma, delta, distinct, eval, colimInstExp(), dom, cod,
				instExpCsv(), coeval, parens(inst_ref), instExpQuotient());

		inst_ref.set(ret);
	}

	// @SuppressWarnings({"unchecked"})
	private static void graphExp() {
		Parser<GraphExp<?, ?>> var = ident.map(GraphExpVar::new),

				ret = Parsers.or(var, graphExpRaw(), parens(graph_ref));

		graph_ref.set(ret);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void mapExp() {
		Parser<MapExp<?, ?, ?, ?, ?, ?, ?, ?>> var = ident.map(MapExpVar::new),
				id = Parsers.tuple(token("id"), sch_ref.lazy()).map(x -> new MapExpId<>(x.b)),
				colim = Parsers.tuple(token("getMapping"), colim_ref.lazy(), ident).map(x -> new MapExpColim(x.c, x.b)),
				comp = Parsers.tuple(token("["), map_ref.lazy(), token(";"), map_ref.lazy(), token("]"))
						.map(x -> new MapExpComp(x.b, x.d)),

				ret = Parsers.or(id, mapExpRaw(), var, colim, comp, parens(map_ref));

		map_ref.set(ret);
	}

	// TODO aql revisit parser type safety
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void transExp() {
		Parser<TransExp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> var = ident.map(TransExpVar::new),
				id = Parsers.tuple(token("id"), inst_ref.lazy()).map(x -> new TransExpId<>(x.b)), sigma = Parsers
						.tuple(token("sigma"), map_ref.lazy(), trans_ref.lazy(), options
								.between(token("{"), token("}")).optional(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new TransExpSigma(x.b, x.c, x.d == null ? new HashMap<>() : Util.toMapSafely(x.d),
								x.e == null ? new HashMap<>() : Util.toMapSafely(x.e))),
				delta = Parsers.tuple(token("delta"), map_ref.lazy(), trans_ref.lazy())
						.map(x -> new TransExpDelta(x.b, x.c)),
				unit = Parsers
						.tuple(token("unit"), map_ref.lazy(), inst_ref.lazy(),
								options.between(token("{"), token("}")).optional(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new TransExpSigmaDeltaUnit(x.b, x.c,
								x.d == null ? new HashMap<>() : Util.toMapSafely(x.d))),
				counit = Parsers
						.tuple(token("counit"), map_ref.lazy(), inst_ref.lazy(),
								options.between(token("{"), token("}")).optional(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new TransExpSigmaDeltaCounit(x.b, x.c,
								x.d == null ? new HashMap<>() : Util.toMapSafely(x.d))),
				distinct = Parsers.tuple(token("distinct"), trans_ref.lazy()).map(x -> new TransExpDistinct(x.b)),
				eval = Parsers.tuple(token("eval"), query_ref.lazy(), trans_ref.lazy())
						.map(x -> new TransExpEval(x.b, x.c)),

				coeval = Parsers
						.tuple(token("coeval"), query_ref.lazy(), trans_ref.lazy(),
								options.between(token("{"), token("}")).optional(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new TransExpCoEval(x.b, x.c, x.d == null ? new LinkedList<>() : x.d,
								x.e == null ? new LinkedList<>() : x.e)),

				unitq = Parsers
						.tuple(token("unit_query"), query_ref.lazy(), inst_ref.lazy(),
						//		options.between(token("{"), token("}")).optional(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new TransExpCoEvalEvalUnit(x.b, x.c,
								x.d == null ? new HashMap<>() : Util.toMapSafely(x.d))),
				counitq = Parsers
						.tuple(token("counit_query"), query_ref.lazy(), inst_ref.lazy(),
					//			options.between(token("{"), token("}")).optional(),
								options.between(token("{"), token("}")).optional())
						.map(x -> new TransExpCoEvalEvalCoUnit(x.b, x.c,
								x.d == null ? new HashMap<>() : Util.toMapSafely(x.d))),

				comp = Parsers.tuple(token("["), trans_ref.lazy(), token(";"), trans_ref.lazy(), token("]"))
						.map(x -> new TransExpCompose(x.b, x.d));

		Parser ret = Parsers.or(id, transExpRaw(), var, sigma, delta, unit, counit, distinct, eval, coeval,
				transExpCsv(), unitq, counitq, transExpJdbc(), comp, parens(trans_ref));

		trans_ref.set(ret);
	}

	private static final Parser<String> option = Parsers.or(AqlOptions.optionNames().stream()
			.map(x -> token(x).map(y -> y.value().toString())).collect(Collectors.toList()));

	private static final Parser<List<LocStr>> imports = Parsers.tuple(token("imports"), locstr.many()).optional()
			.map(x -> x == null ? new LinkedList<>() : x.b);
	private static final Parser<List<catdata.Pair<String, String>>> options = Parsers
			.tuple(token("options"), Parsers.tuple(option, token("="), ident).many()).optional().map(x -> {
				List<catdata.Pair<String, String>> ret = new LinkedList<>();
				if (x != null) {
					for (Tuple3<String, Token, String> y : x.b) {
						ret.add(new catdata.Pair<>(y.a, y.c));
					}
				}
				return ret;
			});
	private static final Parser<List<catdata.Pair<String, String>>> ctx = Parsers
			.tuple(ident.many1(), Parsers.tuple(token(":"), ident).optional()).sepBy(token(",")).map(x -> {
				List<catdata.Pair<String, String>> ret = new LinkedList<>();
				for (Pair<List<String>, Pair<Token, String>> y : x) {
					for (String z : y.a) {
						ret.add(new catdata.Pair<>(z, y.b == null ? null : y.b.b));
					}
				}
				return ret;
			});
	/*
	private static <X> Parser<List<catdata.Pair<LocStr, X>>> env_backwards(Parser<X> p, String t) {
		return Parsers.tuple(Parsers.tuple(p, token(t)), locstr.many1()).many().map(x -> {
			List<catdata.Pair<LocStr, X>> ret = new LinkedList<>();
			for (Pair<Pair<X, Token>, List<LocStr>> y : x) {
				for (LocStr z : y.b) {
					ret.add(new catdata.Pair<>(z, y.a.a));
				}
			}
			return ret;
		});
	} */

	private static <X> Parser<List<catdata.Pair<LocStr, X>>> env(Parser<X> p, String t) {
		return Parsers.tuple(locstr.many1(), Parsers.tuple(token(t), p)).many().map(x -> {
			List<catdata.Pair<LocStr, X>> ret = new LinkedList<>();
			for (Pair<List<LocStr>, Pair<Token, X>> y : x) {
				for (LocStr z : y.a) {
					ret.add(new catdata.Pair<>(z, y.b.b));
				}
			}
			return ret;
		});
	}

	private static Parser<GraphExpRaw> graphExpRaw() {
		Parser<List<LocStr>> nodes = Parsers.tuple(token("nodes"), locstr.many()).map(x -> x.b);

		Parser<Pair<Token, List<Tuple5<List<LocStr>, Token, String, Token, String>>>> edges = Parsers
				.tuple(token("edges"), Parsers.tuple(locstr.many1(), token(":"), ident, token("->"), ident).many());
		Parser<List<catdata.Pair<LocStr, catdata.Pair<String, String>>>> edges0 = edges.map(x -> {
			List<catdata.Pair<LocStr, catdata.Pair<String, String>>> ret = new LinkedList<>();
			for (Tuple5<List<LocStr>, Token, String, Token, String> a : x.b) {
				for (LocStr b : a.a) {
					ret.add(new catdata.Pair<>(b, new catdata.Pair<>(a.c, a.e)));
				}
			}
			return ret;
		});

		Parser<Tuple3<List<LocStr>, List<LocStr>, List<catdata.Pair<LocStr, catdata.Pair<String, String>>>>> pa = Parsers
				.tuple(imports, nodes.optional(), edges0.optional());

		Parser<GraphExpRaw> ret = pa.map(x -> new GraphExpRaw(Util.newIfNull(x.b), Util.newIfNull(x.c), x.a));
		return ret.between(token("literal").followedBy(token("{")), token("}"));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<InstExpCsv> instExpCsv() {
		Parser<Pair<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<String, String>>>> qs = Parsers
				.tuple(env(ident, "->"), options).between(token("{"), token("}"));

		Parser<InstExpCsv> ret = Parsers.tuple(token("import_csv").followedBy(token(":")), sch_ref.lazy(), qs)
				.map(x -> new InstExpCsv(x.b, x.c.a, x.c.b));
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<TransExpCsv> transExpCsv() {
		Parser<Pair<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<String, String>>>> qs = Parsers
				.tuple(env(ident, "->"), options).between(token("{"), token("}"));

		Parser<TransExpCsv> ret = Parsers.tuple(token("import_csv").followedBy(token(":")),
				inst_ref.lazy().followedBy(token("->")), inst_ref.lazy(), qs)
				.map(x -> new TransExpCsv(x.b, x.c, x.d.a, x.d.b));
		return ret;
	}
	// public TransExpJdbc(InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> src,
	// InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> dst, List<String>
	// imports, List<Pair<String, String>> options, String clazz, String
	// jdbcString, List<Pair<String, String>> map) {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<TransExpJdbc> transExpJdbc() {
		Parser<Pair<InstExp, InstExp>> st = Parsers.tuple(inst_ref.lazy(), token("->"), inst_ref.lazy())
				.map(x -> new Pair<>(x.a, x.c));

		Parser<Pair<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<String, String>>>> qs = Parsers
				.tuple(env(ident, "->"), options).between(token("{"), token("}"));

		Parser<TransExpJdbc> ret = Parsers.tuple(token("import_jdbc"), ident, ident.followedBy(token(":")), st, qs)
				.map(x -> new TransExpJdbc(x.b, x.c, x.d.a, x.d.b, x.e.a, x.e.b)); // too
																					// hard
		return ret;
	}

	private static Parser<TyExpRaw> tyExpRaw() {
		Parser<List<LocStr>> types = Parsers.tuple(token("types"), locstr.many()).map(x -> x.b);

		Parser<Pair<Token, List<Tuple3<List<LocStr>, Token, String>>>> consts = Parsers.tuple(token("constants"),
				Parsers.tuple(locstr.many1(), token(":"), ident).many());
		Parser<List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>>> consts0 = consts.map(x -> {
			List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>> ret = new LinkedList<>();
			for (Tuple3<List<LocStr>, Token, String> a : x.b) {
				for (LocStr b : a.a) {
					ret.add(new catdata.Pair<>(b, new catdata.Pair<>(Collections.emptyList(), a.c)));
				}
			}
			return ret;
		});

		Parser<Pair<Token, List<Tuple5<List<LocStr>, Token, List<String>, Token, String>>>> fns = Parsers.tuple(
				token("functions"),
				Parsers.tuple(locstr.many1(), token(":"), ident.sepBy(token(",")), token("->"), ident).many());
		Parser<List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>>> fns0 = fns.map(x -> {
			List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>> ret = new LinkedList<>();
			for (Tuple5<List<LocStr>, Token, List<String>, Token, String> a : x.b) {
				for (LocStr b : a.a) {
					ret.add(new catdata.Pair<>(b, new catdata.Pair<>(a.c, a.e)));
				}
			}
			return ret;
		});

		Parser<Pair<Token, List<catdata.Pair<Integer, Triple<List<catdata.Pair<String, String>>, RawTerm, RawTerm>>>>> eqs = Parsers
				.tuple(token("equations"), Parsers.tuple(Parsers.INDEX, Parsers.or(eq1, eq2))
						.map(x -> new catdata.Pair<>(x.a, x.b)).many());
		Parser<List<catdata.Pair<Integer, Triple<List<catdata.Pair<String, String>>, RawTerm, RawTerm>>>> eqs0 = eqs
				.map(x -> x.b);

		Parser<Pair<Token, List<Tuple3<LocStr, Token, String>>>> java_typs = Parsers.tuple(token("java_types"),
				Parsers.tuple(locstr, token("="), ident).many());
		Parser<List<catdata.Pair<LocStr, String>>> java_typs0 = java_typs.map(x -> {
			List<catdata.Pair<LocStr, String>> ret = new LinkedList<>();
			for (Tuple3<LocStr, Token, String> p : x.b) {
				ret.add(new catdata.Pair<>(p.a, p.c));
			}
			return ret;
		});

		Parser<Pair<Token, List<Tuple3<LocStr, Token, String>>>> java_consts = Parsers.tuple(token("java_constants"),
				Parsers.tuple(locstr, token("="), ident).many());
		Parser<List<catdata.Pair<LocStr, String>>> java_consts0 = java_consts.map(x -> {
			List<catdata.Pair<LocStr, String>> ret = new LinkedList<>();
			for (Tuple3<LocStr, Token, String> p : x.b) {
				ret.add(new catdata.Pair<>(p.a, p.c));
			}
			return ret;
		});

		Parser<List<String>> lll = ident.sepBy(token(",")).followedBy(token("->"));
		Parser<List<String>> jjj = Parsers.constant(new LinkedList<>());
		Parser<List<String>> uuu = Parsers.longer(lll, jjj);

		Parser<Pair<Token, List<Tuple5<LocStr, List<String>, String, Token, String>>>> java_fns = Parsers.tuple(
				token("java_functions"),
				Parsers.tuple(locstr.followedBy(token(":")), uuu, ident, token("="), ident).many());
		Parser<List<catdata.Pair<LocStr, Triple<List<String>, String, String>>>> java_fns0 = java_fns.map(x -> {
			List<catdata.Pair<LocStr, Triple<List<String>, String, String>>> ret = new LinkedList<>();
			for (Tuple5<LocStr, List<String>, String, Token, String> p : x.b) {
				ret.add(new catdata.Pair<>(p.a, new Triple<>(p.b, p.c, p.e)));
			}
			return ret;
		});

		Parser<Tuple5<List<LocStr>, List<LocStr>, List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>>, List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>>, List<catdata.Pair<LocStr, String>>>> pa = Parsers
				.tuple(imports, types.optional(), consts0.optional(), fns0.optional(), java_typs0.optional());
		Parser<Tuple4<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<LocStr, Triple<List<String>, String, String>>>, List<catdata.Pair<Integer, Triple<List<catdata.Pair<String, String>>, RawTerm, RawTerm>>>, List<catdata.Pair<String, String>>>> pb = Parsers
				.tuple(java_consts0.optional(), java_fns0.optional(), eqs0.optional(), options);

		Parser<TyExpRaw> ret = Parsers.tuple(pa, pb).map(x -> {

			List<catdata.Pair<LocStr, catdata.Pair<List<String>, String>>> l = new LinkedList<>();
			if (x.a.c != null) {
				l.addAll(x.a.c);
			}
			if (x.a.d != null) {
				l.addAll(x.a.d);
			}

			return new TyExpRaw(x.a.a, Util.newIfNull(x.a.b), l, Util.newIfNull(x.b.c), Util.newIfNull(x.a.e),
					Util.newIfNull(x.b.a), Util.newIfNull(x.b.b), Util.newIfNull(x.b.d));
		});
		return ret.between(token("literal").followedBy(token("{")), token("}"));
	}

	private static Parser<SchExpRaw<String, String>> schExpRaw() {
		Parser<List<LocStr>> entities = Parsers.tuple(token("entities"), locstr.many()).map(x -> x.b);

		Parser<Pair<Token, List<Tuple5<List<LocStr>, Token, String, Token, String>>>> fks = Parsers.tuple(
				token("foreign_keys"), Parsers.tuple(locstr.many1(), token(":"), ident, token("->"), ident).many());
		Parser<List<catdata.Pair<LocStr, catdata.Pair<String, String>>>> fks0 = fks.map(x -> {
			List<catdata.Pair<LocStr, catdata.Pair<String, String>>> ret = new LinkedList<>();
			for (Tuple5<List<LocStr>, Token, String, Token, String> a : x.b) {
				for (LocStr b : a.a) {
					ret.add(new catdata.Pair<>(b, new catdata.Pair<>(a.c, a.e)));
				}
			}
			return ret;
		});

		Parser<Pair<Token, List<Tuple5<List<LocStr>, Token, String, Token, String>>>> atts = Parsers.tuple(
				token("attributes"), Parsers.tuple(locstr.many1(), token(":"), ident, token("->"), ident).many());
		Parser<List<catdata.Pair<LocStr, catdata.Pair<String, String>>>> atts0 = atts.map(x -> {
			List<catdata.Pair<LocStr, catdata.Pair<String, String>>> ret = new LinkedList<>();
			for (Tuple5<List<LocStr>, Token, String, Token, String> a : x.b) {
				for (LocStr b : a.a) {
					ret.add(new catdata.Pair<>(b, new catdata.Pair<>(a.c, a.e)));
				}
			}
			return ret;
		});

		Parser<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>> p_eq = Parsers
				.tuple(Parsers.INDEX, Parsers.tuple(ident.sepBy(token(".")), token("="), ident.sepBy(token("."))))
				.map(x -> new catdata.Pair<>(x.a, new catdata.Pair<>(x.b.a, x.b.c)));

		Parser<Pair<Token, List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>>> p_eqs = Parsers
				.tuple(token("path_equations"), p_eq.many());
		Parser<List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>> p_eqs0 = p_eqs.map(x -> x.b);

		Parser<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> o_eq_from_p_eq = p_eq
				.map(x -> new catdata.Pair<>(x.first, new Quad<>("_x", null, RawTerm.fold(x.second.first, "_x"),
						RawTerm.fold(x.second.second, "_x"))));

		Parser<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> o_eq_old = Parsers
				.tuple(Parsers.INDEX,
						Parsers.tuple(token("forall"), ident,
								Parsers.tuple(token(":"), ident).optional().followedBy(token(".")),
								term().followedBy(token("=")), term()))
				.map(x -> new catdata.Pair<>(x.a, new Quad<>(x.b.b, x.b.c == null ? null : x.b.c.b, x.b.d, x.b.e)));

		Parser<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> o_eq = Parsers.or(o_eq_old,
				o_eq_from_p_eq);

		Parser<Pair<Token, List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>>> o_eqs = Parsers
				.tuple(token("observation_equations"), o_eq.many());
		Parser<List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>> o_eqs0 = o_eqs.map(x -> x.b);

		Parser<Tuple4<List<LocStr>, List<LocStr>, List<catdata.Pair<LocStr, catdata.Pair<String, String>>>, List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>>> pa = Parsers
				.tuple(imports, entities.optional(), fks0.optional(), p_eqs0.optional());
		Parser<Tuple3<List<catdata.Pair<LocStr, catdata.Pair<String, String>>>, List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>, List<catdata.Pair<String, String>>>> pb = Parsers
				.tuple(atts0.optional(), o_eqs0.optional(), options);

		Parser<Tuple4<Token, Token, TyExp<?, ?>, Token>> l = Parsers.tuple(token("literal"), token(":"), ty_ref.lazy(),
				token("{")); // .map(x -> x.c);

		// needs tyexp
		Parser<SchExpRaw<String, String>> ret = Parsers.tuple(l, pa, pb, token("}"))
				.map(x -> new SchExpRaw<String, String>(x.a.c, x.b.a, Util.newIfNull(x.b.b), Util.newIfNull(x.b.c),
						Util.newIfNull(x.b.d), Util.newIfNull(x.c.a), Util.newIfNull(x.c.b), x.c.c));

		return ret;
	}

	@SuppressWarnings("rawtypes")
	private static Parser<ColimSchExpQuotient<?, ?, ?, ?, ?, ?>> colimSchExpQuotient() {
		Parser<catdata.Pair<Integer, Quad<String, String, String, String>>> q = Parsers.tuple(Parsers.INDEX,
				Parsers.tuple(ident.followedBy(token(".")), ident, token("="), ident.followedBy(token(".")), ident)
						.map(x -> new Quad<>(x.a, x.b, x.d, x.e)))
				.map(x -> new catdata.Pair<>(x.a, x.b));

		Parser<List<catdata.Pair<Integer, Quad<String, String, String, String>>>> entities = Parsers
				.tuple(token("entity_equations"), q.many()).map(x -> x.b);

		Parser<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>> p_eq = Parsers
				.tuple(Parsers.INDEX, Parsers.tuple(ident.sepBy(token(".")), token("="), ident.sepBy(token(".")))
						.map(x -> new catdata.Pair<>(x.a, x.c)))
				.map(x -> new catdata.Pair<>(x.a, x.b));

		Parser<catdata.Pair<Token, List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>>> p_eqs = Parsers
				.tuple(token("path_equations"), p_eq.many()).map(x -> new catdata.Pair<>(x.a, x.b));
		Parser<List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>> p_eqs0 = p_eqs.map(x -> x.second);

		Parser<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> o_eq_old = Parsers
				.tuple(Parsers.INDEX,
						Parsers.tuple(token("forall"), ident,
								Parsers.tuple(token(":"), ident).optional().followedBy(token(".")),
								term().followedBy(token("=")), term())
								.map(x -> new Quad<>(x.b, x.c == null ? null : x.c.b, x.d, x.e)))
				.map(x -> new catdata.Pair<>(x.a, x.b));

		Parser<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> o_eq_from_p_eq = p_eq
				.map(x -> new catdata.Pair<>(x.first, new Quad<>("_x", null, RawTerm.fold(x.second.first, "_x"),
						RawTerm.fold(x.second.second, "_x"))));

		Parser<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> o_eq = Parsers.or(o_eq_old,
				o_eq_from_p_eq);

		Parser<Pair<Token, List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>>> o_eqs = Parsers
				.tuple(token("observation_equations"), o_eq.many());
		Parser<List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>> o_eqs0 = o_eqs.map(x -> x.b);

		Parser<Tuple4<List<catdata.Pair<Integer, Quad<String, String, String, String>>>, List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>, List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(entities.optional(), p_eqs0.optional(), o_eqs0.optional(), options);

		Parser<Tuple4<Token, List<LocStr>, Token, TyExp<?, ?>>> l = Parsers.tuple(Parsers.or(token("coproduct"), token("quotient")),
				locstr.sepBy(token("+")), token(":"), ty_ref.lazy());

		Parser<Tuple4<List<catdata.Pair<Integer, Quad<String, String, String, String>>>, List<catdata.Pair<Integer, catdata.Pair<List<String>, List<String>>>>, List<catdata.Pair<Integer, Quad<String, String, RawTerm, RawTerm>>>, List<catdata.Pair<String, String>>>> 
		ppp = Parsers.tuple(token("{"), pa, token("}")).map(x -> x.b).optional();

		@SuppressWarnings("unchecked")
		Parser<ColimSchExpQuotient<?, ?, ?, ?, ?, ?>> ret = Parsers.tuple(l, ppp).map(x -> {
			TyExp<?, ?> ty = x.a.d;
			List<LocStr> nodes = x.a.b;
			
			if (x.b == null) {
				return new ColimSchExpQuotient(ty, nodes, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
			} else {
				return new ColimSchExpQuotient(ty, nodes, Util.newIfNull(x.b.a), Util.newIfNull(x.b.c), Util.newIfNull(x.b.b), x.b.d);
			}
		});

		return ret;
	}

	private static Parser<EdsExp<?, ?, ?, ?, ?>> edsExpRaw() {
		Parser<SchExp<?, ?, ?, ?, ?>> l = Parsers.tuple(token("literal"), token(":"), sch_ref.lazy(), token("{"))
				.map(x -> x.c);

		return Parsers.tuple(l, imports,
				Parsers.tuple(Parsers.INDEX, edExpRaw()).map(x -> new catdata.Pair<>(x.a, x.b)).many(), options, token("}"))
				.map(x -> new EdsExpRaw(x.a, x.b, x.c, x.d));
	}

	private static Parser<EdsExp<?, ?, ?, ?, ?>> edsExp() {
		Parser<EdsExp<?, ?, ?, ?, ?>> var = ident.map(EdsExpVar::new), raw = edsExpRaw();
		return Parsers.or(var, raw); // TODO aql no parens
	}

	private static Parser<EdExpRaw> edExpRaw() {
		Parser<List<catdata.Pair<LocStr, String>>> as = Parsers.tuple(token("forall"), env(ident, ":")).map(x -> x.b)
				.optional();

		Parser<catdata.Pair<List<catdata.Pair<LocStr, String>>, Boolean>> es = Parsers
				.tuple(token("exists"), token("unique").optional(), env(ident, ":"))
				.map(x -> new catdata.Pair<>(x.c, x.b != null)).optional();

		Parser<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>> eq = Parsers
				.tuple(Parsers.INDEX, Parsers.tuple(term(), token("="), term()).map(x -> new catdata.Pair<>(x.a, x.c)))
				.map(x -> new catdata.Pair<>(x.a, x.b));

		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> eqs = Parsers
				.tuple(token("where"), eq.many()).map(x -> x.b).optional();

		Parser<EdExpRaw> ret = Parsers.tuple(as, eqs, token("->"), es, eqs)
				.map(x -> new EdExpRaw(Util.newIfNull(x.a), Util.newIfNull(x.b),
						x.d == null ? new LinkedList<>() : x.d.first, Util.newIfNull(x.e),
						x.d == null ? false : x.d.second));
		return ret;
	}

	private static Parser<InstExpRandom> instExpRand() {
		Parser<List<catdata.Pair<LocStr, String>>> generators = Parsers
				.tuple(token("generators"), env(IntegerLiteral.PARSER, "->")).map(x -> x.b);

		Parser<Tuple4<Token, Token, SchExp<?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(token("random"), token(":"),
				sch_ref.lazy(), token("{"));

		Parser<InstExpRandom> ret = Parsers.tuple(l, generators, options, token("}"))
				.map(x -> new InstExpRandom(x.a.c, x.b, x.c));

		return ret;
	}
	
	private static Parser<InstExpQuotient<?, ?, ?, ?, ?, ?, ?, ?, ?>> instExpQuotient() {

		Parser<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>> eq = Parsers
				.tuple(Parsers.INDEX, Parsers.tuple(term(), token("="), term()))
				.map(x -> new catdata.Pair<>(x.a, new catdata.Pair<>(x.b.a, x.b.c)));

		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> eqs = Parsers
				.tuple(token("equations"), eq.many()).map(x -> x.b);

		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> table = Parsers.tuple(Parsers.INDEX, Parsers
				.tuple(ident, token("->"), token("{"), Parsers.tuple(term(), term()).sepBy(token(",")), token("}")))
				.map(x -> {
					List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>> ret = new LinkedList<>();
					for (Pair<RawTerm, RawTerm> y : x.b.d) {
						ret.add(new catdata.Pair<>(x.a,
								new catdata.Pair<>(new RawTerm(x.b.a, Util.singList(y.a)), y.b)));
					}
					return ret;
				});
		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> tables = Parsers
				.tuple(token("multi_equations"), table.many()).map(x -> Util.concat(x.b));

		Parser<Tuple3<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>, List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(eqs.optional(), tables.optional(), options);

		Parser<Tuple3<Token, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(token("quotient"), 
				inst_ref.lazy(), token("{")); // .map(x -> x.c);

		Parser<InstExpQuotient<?, ?, ?, ?, ?, ?, ?, ?, ?>> ret = Parsers.tuple(l, pa, token("}"))
				.map(x -> new InstExpQuotient<>
				(x.a.b, new LinkedList<>(Util.append(Util.newIfNull(x.b.a), Util.newIfNull(x.b.b))), x.b.c));

		return ret;
	}

	private static Parser<InstExpRaw<?, ?, ?, ?, ?>> instExpRaw() {
		Parser<List<catdata.Pair<LocStr, String>>> generators = Parsers.tuple(token("generators"), env(ident, ":"))
				.map(x -> x.b);

		Parser<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>> eq = Parsers
				.tuple(Parsers.INDEX, Parsers.tuple(term(), token("="), term()))
				.map(x -> new catdata.Pair<>(x.a, new catdata.Pair<>(x.b.a, x.b.c)));

		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> eqs = Parsers
				.tuple(token("equations"), eq.many()).map(x -> x.b);

		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> table = Parsers.tuple(Parsers.INDEX, Parsers
				.tuple(ident, token("->"), token("{"), Parsers.tuple(term(), term()).sepBy(token(",")), token("}")))
				.map(x -> {
					List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>> ret = new LinkedList<>();
					for (Pair<RawTerm, RawTerm> y : x.b.d) {
						ret.add(new catdata.Pair<>(x.a,
								new catdata.Pair<>(new RawTerm(x.b.a, Util.singList(y.a)), y.b)));
					}
					return ret;
				});
		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> tables = Parsers
				.tuple(token("multi_equations"), table.many()).map(x -> Util.concat(x.b));

		Parser<Tuple5<List<LocStr>, List<catdata.Pair<LocStr, String>>, List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>, List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(imports, generators.optional(), eqs.optional(), tables.optional(), options);

		Parser<Tuple4<Token, Token, SchExp<?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(token("literal"), token(":"),
				sch_ref.lazy(), token("{")); // .map(x -> x.c);

		Parser<InstExpRaw<?, ?, ?, ?, ?>> ret = Parsers.tuple(l, pa, token("}"))
				.map(x -> new InstExpRaw<String, String, String, String, String>(x.a.c, Util.newIfNull(x.b.a),
						Util.newIfNull(x.b.b),
						new LinkedList<>(Util.append(Util.newIfNull(x.b.c), Util.newIfNull(x.b.d))), x.b.e));

		return ret;
	}

	private static Parser<catdata.Pair<Block<String, String>, List<catdata.Pair<LocStr, RawTerm>>>> block() {
		Parser<List<catdata.Pair<LocStr, String>>> generators = Parsers.tuple(token("from"), env(ident, ":"))
				.map(x -> x.b);

		Parser<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>> eq = Parsers
				.tuple(Parsers.INDEX, Parsers.tuple(term(), token("="), term()).map(x -> new catdata.Pair<>(x.a, x.c)))
				.map(x -> new catdata.Pair<>(x.a, x.b));

		Parser<List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>> eqs = Parsers
				.tuple(token("where"), eq.many()).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, RawTerm>>> atts = Parsers
				.tuple(token("return"),
						Parsers.tuple(locstr, token("->"), term()).map(x -> new catdata.Pair<>(x.a, x.c)).many())
				.map(x -> x.b);

		Parser<Tuple4<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<Integer, catdata.Pair<RawTerm, RawTerm>>>, List<catdata.Pair<LocStr, RawTerm>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(generators.optional(), eqs.optional(), atts.optional(), options);

		Parser<catdata.Pair<Block<String, String>, List<catdata.Pair<LocStr, RawTerm>>>> ret = Parsers
				.tuple(token("{"), pa, token("}"))
				.map(x -> new catdata.Pair<Block<String, String>, List<catdata.Pair<LocStr, RawTerm>>>(
						new Block<String, String>(x.b.a, Util.newIfNull(x.b.b), x.b.d, Util.newIfNull(x.b.c)),

						Util.newIfNull(x.b.c)));

		return ret;
	}

	// TODO aql add example illustrating multi equations

	/*
	 * private static final Parser<PragmaExp> pragmaExp() { return
	 * Parsers.fail("pragma parser not implemented yet"); //TODO aql pragma }
	 */

	private static void queryExp() {
		Parser<QueryExp<?, ?, ?, ?, ?, ?, ?, ?>> var = ident.map(QueryExpVar::new),
				deltaQueryEval = Parsers.tuple(token("toQuery"), map_ref.lazy(), options.between(token("{"), token("}")).optional()).map(x -> new QueryExpDeltaEval<>(x.b, Util.newIfNull(x.c))),
				deltaQueryCoEval = Parsers.tuple(token("toCoQuery"), map_ref.lazy(), options.between(token("{"), token("}")).optional()).map(x -> new QueryExpDeltaCoEval<>(x.b, Util.newIfNull(x.c))),

				id = Parsers.tuple(token("id"), sch_ref.lazy()).map(x -> new QueryExpId<>(x.b)),
				ret = Parsers.or(id, queryExpRaw(), queryExpRawSimple(), var, deltaQueryEval, deltaQueryCoEval, parens(query_ref));

		query_ref.set(ret);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void colimSchExp() {
		Parser<List<catdata.Pair<LocStr, SchExp<?, ?, ?, ?, ?>>>> nodes = Parsers
				.tuple(token("nodes"), env(sch_ref.lazy(), "->")).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, MapExp<?, ?, ?, ?, ?, ?, ?, ?>>>> edges = Parsers
				.tuple(token("edges"), env(map_ref.lazy(), "->")).map(x -> x.b);

		Parser<Tuple4<Object, List<catdata.Pair<LocStr, SchExp<?, ?, ?, ?, ?>>>, List<catdata.Pair<LocStr, MapExp<?, ?, ?, ?, ?, ?, ?, ?>>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(Parsers.always(), nodes.optional(), edges.optional(), options);

		Parser<Tuple5<Token, GraphExp<?, ?>, Token, TyExp<?, ?>, Token>> l = Parsers.tuple(token("literal"),
				graph_ref.lazy(), token(":"), ty_ref.lazy(), token("{")); 

		Parser<ColimSchExp<?, ?, ?, ?, ?, ?, ?>> ret = Parsers
				.tuple(l, pa, token("}")).map(x -> {

					// schema graph nodes edges options imports
					return new ColimSchExpRaw(x.a.b, x.a.d, Util.newIfNull(x.b.b), Util.newIfNull(x.b.c), x.b.d);
				});

		Parser<ColimSchExp<?, ?, ?, ?, ?, ?, ?>> ret2 = ident
				.map(x -> new ColimSchExpVar(x)),
				ret3 = Parsers.tuple(token("wrap"), colim_ref.lazy(), map_ref.lazy(), map_ref.lazy())
						.map(x -> new ColimSchExpWrap(x.b, x.c, x.d));

		Parser<ColimSchExp<?, ?, ?, ?, ?, ?, ?>> retX = Parsers.or(ret, ret2, ret3
			 , colimExpModify(), colimSchExpQuotient(), parens(colim_ref));

		colim_ref.set(retX);

	}

	private static Parser<InstExpColim<String, String, String, String, String, String, String, String, String, String, String>> colimInstExp() {
		Parser<List<catdata.Pair<LocStr, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>>>> nodes = Parsers
				.tuple(token("nodes"), env(inst_ref.lazy(), "->")).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, TransExp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>>>> edges = Parsers
				.tuple(token("edges"), env(trans_ref.lazy(), "->")).map(x -> x.b);

		Parser<Tuple4<String, List<catdata.Pair<LocStr, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>>>, List<catdata.Pair<LocStr, TransExp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(Parsers.always(), nodes.optional(), edges.optional(), options);

		Parser<Tuple4<Token, GraphExp<?, ?>, SchExp<?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(token("colimit"),
				graph_ref.lazy(), sch_ref.lazy(), token("{")); // .map(x ->
																// x.c);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Parser<InstExpColim<String, String, String, String, String, String, String, String, String, String, String>> ret = Parsers
				.tuple(l, pa, token("}")).map(x -> {

					// schema graph nodes edges options imports
					return new InstExpColim(x.a.b, x.a.c,

							Util.newIfNull(x.b.b), Util.newIfNull(x.b.c), x.b.d);
				});

		return ret;
	}

	private static Parser<QueryExpRawSimple<String, String, String, String, String, String, String, String>> queryExpRawSimple() {
		Parser<Tuple4<Token, Token, SchExp<?, ?, ?, ?, ?>, catdata.Pair<Block<String, String>, List<catdata.Pair<LocStr, RawTerm>>>>> l = Parsers
				.tuple(token("simple"), token(":"), sch_ref.lazy(), block());

		Parser<QueryExpRawSimple<String, String, String, String, String, String, String, String>> ret = l
				.map(x -> new QueryExpRawSimple<String, String, String, String, String, String, String, String>(x.c,
						x.d.first));

		return ret;
	}

	private static Parser<QueryExpRaw<String, String, String, String, String, String, String, String>> queryExpRaw() {
		Parser<List<catdata.Pair<LocStr, catdata.Pair<Block<String, String>, List<catdata.Pair<LocStr, RawTerm>>>>>> ens = Parsers
				.tuple(token("entities"), env(block(), "->")).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, Trans>>> trans = Parsers.tuple(token("foreign_keys"), env(trans(), "->"))
				.map(x -> x.b);

		Parser<Tuple4<List<LocStr>, List<catdata.Pair<LocStr, catdata.Pair<Block<String, String>, List<catdata.Pair<LocStr, RawTerm>>>>>, List<catdata.Pair<LocStr, Trans>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(imports, ens.optional(), trans.optional(), options);

		Parser<Tuple5<Token, Token, SchExp<?, ?, ?, ?, ?>, SchExp<?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(
				token("literal"), token(":"), sch_ref.lazy().followedBy(token("->")), sch_ref.lazy(), token("{")); // .map(x
																													// ->
																													// x.c);

		Parser<QueryExpRaw<String, String, String, String, String, String, String, String>> ret = Parsers
				.tuple(l, pa, token("}"))
				.map(x -> new QueryExpRaw<String, String, String, String, String, String, String, String>(x.a.c, x.a.d,
						x.b.a, Util.newIfNull(x.b.b), Util.newIfNull(x.b.c), x.b.d));

		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<InstExpCoProdSigma> instExpCoProd() {
		Parser<catdata.Pair<MapExp, InstExp>> p = Parsers.tuple(map_ref.lazy(), inst_ref.lazy())
				.map(x -> new catdata.Pair<>(x.a, x.b));

		Parser<InstExpCoProdSigma> ret = Parsers
				.tuple(token("coproduct_sigma"), p.many(), token(":"), sch_ref.lazy(),
						options.between(token("{"), token("}")).optional())
				.map(x -> new InstExpCoProdSigma(x.b, x.d, Util.newIfNull(x.e)));

		//
		Parser<InstExpCoProdSigma> ret2 = Parsers
				.tuple(token("coproduct"),
						inst_ref.lazy()
								.sepBy(token("+")),
						token(":"), sch_ref
								.lazy(),
						options.between(token("{"), token("}")).optional())
				.map(x -> new InstExpCoProdSigma(
						x.b.stream().map(y -> new catdata.Pair<>(new MapExpId(x.d), y)).collect(Collectors.toList()),
						x.d, Util.newIfNull(x.e)));

		return Parsers.or(ret, ret2);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<InstExpCoEq> instExpCoEq() {
		Parser<InstExpCoEq> ret = Parsers
				.tuple(token("coequalize"), trans_ref.lazy(), trans_ref.lazy(),
						options.between(token("{"), token("}")).optional())
				.map(x -> new InstExpCoEq(x.b, x.c, Util.newIfNull(x.d)));

		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<InstExpJdbcQuotient> instExpJdbcQuot() {
		Parser<Pair<List<String>, List<catdata.Pair<String, String>>>> p = Parsers.tuple(ident.many(), options)
				.between(token("{"), token("}"));

		Parser<InstExpJdbcQuotient> ret = Parsers.tuple(token("quotient_jdbc"), ident, ident, inst_ref.lazy(), p)
				.map(x -> new InstExpJdbcQuotient(x.b, x.c, x.d, x.e.a, x.e.b));

		return ret;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Parser<InstExpCsvQuotient> instExpCsvQuot() {
		Parser<Pair<List<String>, List<catdata.Pair<String, String>>>> p = Parsers.tuple(ident.many(), options)
				.between(token("{"), token("}"));

		Parser<InstExpCsvQuotient> ret = Parsers.tuple(token("quotient_csv"), inst_ref.lazy(), p)
				.map(x -> new InstExpCsvQuotient(x.b, x.c.a, x.c.b));

		return ret;
	}

	@SuppressWarnings("rawtypes")
	private static Parser<InstExpJdbc> instExpJdbc() {
		Parser<Pair<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<String, String>>>> qs = Parsers
				.tuple(env(ident, "->"), options).between(token("{"), token("}"));

		@SuppressWarnings("unchecked")
		Parser<InstExpJdbc> ret = Parsers
				.tuple(token("import_jdbc"), ident, ident.followedBy(token(":")), sch_ref.lazy(), qs)
				.map(x -> new InstExpJdbc(x.d, x.e.b, x.b, x.c, x.e.a));
		return ret;
	}

	private static Parser<InstExpJdbcAll> instExpJdbcAll() {
		Parser<InstExpJdbcAll> ret = Parsers
				.tuple(token("import_jdbc_all"), ident, ident, options.between(token("{"), token("}")).optional())
				.map(x -> new InstExpJdbcAll(x.b, x.c, Util.newIfNull(x.d)));

		return ret;
	}

	// TODO: aql reverse order on arguments env
	private static Parser<MapExpRaw> mapExpRaw() {
		Parser<List<catdata.Pair<LocStr, String>>> ens = Parsers.tuple(token("entities"), env(ident, "->"))
				.map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, List<String>>>> fks = Parsers
				.tuple(token("foreign_keys"), env(ident.sepBy1(token(".")), "->")).map(x -> x.b);

		Parser<Tuple5<Token, String, Pair<Token, String>, Token, RawTerm>> lp0 = Parsers.tuple(token("lambda"), ident,
				Parsers.tuple(token(":"), ident).optional(), token("."), term());

		Parser<Tuple5<Token, String, Pair<Token, String>, Token, RawTerm>> lq = ident.sepBy1(token(".")).map(x -> {
			RawTerm term = RawTerm.fold(x, "_x"); // TODO aql
			return new Tuple5<>(null, "_x", new Pair<>(null, null), null, term);
		});

		Parser<Tuple5<Token, String, Pair<Token, String>, Token, RawTerm>> lp = Parsers.or(lp0, lq);

		Parser<List<catdata.Pair<LocStr, Triple<String, String, RawTerm>>>> envp = env(
				lp.map(x -> new Triple<>(x.b, x.c == null ? null : x.c.b, x.e)), "->");

		Parser<List<catdata.Pair<LocStr, Triple<String, String, RawTerm>>>> atts = Parsers
				.tuple(token("attributes"), envp).map(x -> x.b);

		Parser<Tuple5<List<LocStr>, List<catdata.Pair<LocStr, String>>, List<catdata.Pair<LocStr, List<String>>>, List<catdata.Pair<LocStr, Triple<String, String, RawTerm>>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(imports, ens.optional(), fks.optional(), atts.optional(), options);

		Parser<Tuple5<Token, Token, SchExp<?, ?, ?, ?, ?>, SchExp<?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(
				token("literal"), token(":"), sch_ref.lazy().followedBy(token("->")), sch_ref.lazy(), token("{")); // .map(x
																													// ->
																													// x.c);

		Parser<MapExpRaw> ret = Parsers.tuple(l, pa, token("}")).map(x -> new MapExpRaw(x.a.c, x.a.d, x.b.a,
				Util.newIfNull(x.b.b), Util.newIfNull(x.b.c), Util.newIfNull(x.b.d), x.b.e));

		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Parser<ColimSchExpModify<?, ?, ?, ?, ?, ?, ?>> colimExpModify() {
		Parser<List<catdata.Pair<LocStr, String>>> ens = Parsers
				.tuple(token("rename").followedBy(token("entities")), env(ident, "->")).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, String>>> fks0 = Parsers
				.tuple(token("rename").followedBy(token("foreign_keys")), env(ident, "->")).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, String>>> atts0 = Parsers
				.tuple(token("rename").followedBy(token("attributes")), env(ident, "->")).map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, List<String>>>> fks = Parsers
				.tuple(token("remove").followedBy(token("foreign_keys")), env(ident.sepBy1(token(".")), "->"))
				.map(x -> x.b);

		Parser<List<catdata.Pair<LocStr, Triple<String, String, RawTerm>>>> envp = env(
				Parsers.tuple(token("lambda"), ident, Parsers.tuple(token(":"), ident).optional(), token("."), term())
						.map(x -> new Triple<>(x.b, x.c == null ? null : x.c.b, x.e)),
				"->");

		Parser<List<catdata.Pair<LocStr, Triple<String, String, RawTerm>>>> atts = Parsers
				.tuple(token("remove").followedBy(token("attributes")), envp).map(x -> x.b);

		Parser<Tuple3<List<catdata.Pair<LocStr, String>>, List<catdata.Pair<LocStr, String>>, List<catdata.Pair<LocStr, String>>>> pa = Parsers
				.tuple(ens.optional(), fks0.optional(), atts0.optional());

		Parser<Tuple3<List<catdata.Pair<LocStr, List<String>>>, List<catdata.Pair<LocStr, Triple<String, String, RawTerm>>>, List<catdata.Pair<String, String>>>> pb = Parsers
				.tuple(fks.optional(), atts.optional(), options);

		Parser<Tuple3<Token, ColimSchExp<?, ?, ?, ?, ?, ?, ?>, Token>> l = Parsers.tuple(token("modify"),
				colim_ref.lazy(), token("{")); // .map(x -> x.c);

		Parser<ColimSchExpModify<?, ?, ?, ?, ?, ?, ?>> ret = Parsers.tuple(l, pa, pb, token("}"))
				.map(x -> new ColimSchExpModify(x.a.b, Util.newIfNull(x.b.a), Util.newIfNull(x.b.b),
						Util.newIfNull(x.b.c), Util.newIfNull(x.c.a), Util.newIfNull(x.c.b), x.c.c));

		return ret;
	}

	private static Parser<TransExpRaw> transExpRaw() {
		Parser<List<catdata.Pair<LocStr, RawTerm>>> gens1 = Parsers.tuple(token("generators"), env(term(), "->"))
				.map(x -> x.b);
	//	Parser<List<catdata.Pair<LocStr, RawTerm>>> gens2 = Parsers.tuple(token("generators"), env_backwards(term(), "<-"))
		//		.map(x -> x.b);
		//Parser<List<catdata.Pair<LocStr, RawTerm>>> gens = Parsers.or(gens1, gens2);
	

		Parser<Tuple3<List<LocStr>, List<catdata.Pair<LocStr, RawTerm>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(imports, gens1.optional(), options);

		Parser<Tuple5<Token, Token, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>, InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>, Token>> l = Parsers
				.tuple(token("literal"), token(":"), inst_ref.lazy().followedBy(token("->")), inst_ref.lazy(),
						token("{")); // .map(x -> x.c);

		Parser<TransExpRaw> ret = Parsers.tuple(l, pa, token("}"))
				.map(x -> new TransExpRaw(x.a.c, x.a.d, x.b.a, Util.newIfNull(x.b.b), x.b.c));

		return ret;
	}

	private static Parser<Trans> trans() {
		Parser<List<catdata.Pair<LocStr, RawTerm>>> gens = env(term(), "->");
		//Parser<List<catdata.Pair<LocStr, RawTerm>>> gens2 = env_backwards(term(), "<-");
		
		//Parser<List<catdata.Pair<LocStr, RawTerm>>> gens = Parsers.or(gens2, gens1);
		
		Parser<Pair<List<catdata.Pair<LocStr, RawTerm>>, List<catdata.Pair<String, String>>>> pa = Parsers
				.tuple(gens.optional(), options);

		Parser<Trans> ret = Parsers.tuple(token("{"), pa, token("}")).map(x -> new Trans(Util.newIfNull(x.b.a), x.b.b));

		return ret;
	}

	private static <Y> Parser<Triple<String, Integer, Y>> decl(String s, Parser<Y> p) {
		return Parsers.tuple(token(s), Parsers.INDEX, ident, token("="), p).map(x -> new Triple<>(x.c, x.b, x.e));
	}

	private static final Reference<GraphExp<?, ?>> graph_ref = Parser.newReference();
	private static final Reference<TyExp<?, ?>> ty_ref = Parser.newReference();
	private static final Reference<SchExp<?, ?, ?, ?, ?>> sch_ref = Parser.newReference();
	private static final Reference<ColimSchExp<?, ?, ?, ?, ?, ?, ?>> colim_ref = Parser.newReference();
	private static final Reference<PragmaExp> pragma_ref = Parser.newReference();
	private static final Reference<InstExp<?, ?, ?, ?, ?, ?, ?, ?, ?>> inst_ref = Parser.newReference();
	private static final Reference<MapExp<?, ?, ?, ?, ?, ?, ?, ?>> map_ref = Parser.newReference();
	private static final Reference<TransExp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> trans_ref = Parser.newReference();
	private static final Reference<QueryExp<?, ?, ?, ?, ?, ?, ?, ?>> query_ref = Parser.newReference();

	private static Parser<Program<Exp<?>>> program(String s) { // TODO: aql
																// should create
																// single
																// instance of
																// this rather
																// than fn
		tyExp();
		schExp();
		instExp();
		mapExp();
		transExp();
		graphExp();
		queryExp();
		pragmaExp();
		colimSchExp();

		@SuppressWarnings("unchecked")
		Parser<Triple<String, Integer, ? extends Exp<?>>> p = Parsers.or(comment(), decl("typeside", ty_ref.get()),
				decl("schema", sch_ref.get()), decl("instance", inst_ref.get()), decl("mapping", map_ref.get()),
				decl("transform", trans_ref.get()), decl("graph", graph_ref.get()), decl("query", query_ref.get()),
				decl("pragma", pragma_ref.get()), decl("schema_colimit", colim_ref.get()),
				decl("constraints", edsExp()));

		return Parsers.tuple(options, p.many()).map(x -> new Program<>(conv(x.b), s, x.a, q -> q.kind().toString()));
	}

	private static Parser<Triple<String, Integer, ? extends Exp<?>>> comment() {
		Parser<Triple<String, Integer, ? extends Exp<?>>> p1 = Parsers
				.tuple(token("html"), token("{").followedBy(token("(*")), StringLiteral.PARSER, Parsers.INDEX,
						token("*)").followedBy(token("}")))
				.map(x -> new Triple<>("html" + x.d, x.d, new CommentExp(x.c, false)));

		Parser<Triple<String, Integer, ? extends Exp<?>>> p2 = Parsers
				.tuple(token("md"), token("{").followedBy(token("(*")), StringLiteral.PARSER, Parsers.INDEX,
						token("*)").followedBy(token("}")))
				.map(x -> new Triple<>("md" + x.d, x.d, new CommentExp(x.c, true)));

		return p1.or(p2);
	}

	private static List<Triple<String, Integer, Exp<?>>> conv(List<Triple<String, Integer, ? extends Exp<?>>> l) {
		List<Triple<String, Integer, Exp<?>>> ret = new LinkedList<>();
		for (Triple<String, Integer, ? extends Exp<?>> k : l) {
			ret.add(new Triple<>(k.first, k.second, k.third));
		}
		return ret;
	}

	public static Program<Exp<?>> parseProgram(String s) {
		return program(s).from(TOKENIZER, IGNORED).parse(s);
	}

	public static List<String> parseManyIdent(String s) {
		return ident.many().from(TOKENIZER, IGNORED).parse(s);
	}

	public static Triple<List<catdata.Pair<String, String>>, RawTerm, RawTerm> parseEq(String s) {
		return Parsers.or(eq1, eq2).from(TOKENIZER, IGNORED).parse(s);
	}

	public static catdata.Pair<String, String> parseInfer(String s) {
		Parser<catdata.Pair<String, String>> p = Parsers
				.tuple(token("literal").followedBy(token(":")), ident.followedBy(token("->")), ident)
				.map(x -> new catdata.Pair<>(x.b, x.c));
		return p.from(TOKENIZER, IGNORED).parse(s);
	}

	public static String parseInfer1(String s) {
		Parser<String> p = Parsers.tuple(token("literal"), token(":"), ident).map(x -> x.c);
		return p.from(TOKENIZER, IGNORED).parse(s);
	}

	public static catdata.Pair<List<catdata.Pair<String, String>>, RawTerm> parseTermInCtx(String s) {
		return Parsers.or(term1, term2).from(TOKENIZER, IGNORED).parse(s);
	}

	public static RawTerm parseTermNoCtx(String s) {
		return term().from(TOKENIZER, IGNORED).parse(s);
	}

	private static final Parser<Triple<List<catdata.Pair<String, String>>, RawTerm, RawTerm>> eq1 = Parsers
			.tuple(token("forall"), ctx.followedBy(token(".")), term(), token("="), term())
			.map(x -> new Triple<>(x.b, x.c, x.e));

	private static final Parser<Triple<List<catdata.Pair<String, String>>, RawTerm, RawTerm>> eq2 = Parsers
			.tuple(term(), token("="), term()).map(x -> new Triple<>(new LinkedList<>(), x.a, x.c));

	private static final Parser<catdata.Pair<List<catdata.Pair<String, String>>, RawTerm>> term1 = Parsers
			.tuple(token("lambda"), ctx.followedBy(token(".")), term()).map(x -> new catdata.Pair<>(x.b, x.c));

	private static final Parser<catdata.Pair<List<catdata.Pair<String, String>>, RawTerm>> term2 = term()
			.map(x -> new catdata.Pair<>(new LinkedList<>(), x));

	// TODO: aql visitor for aql exps?

}
