/**
 *	A lexical grammar for Aql
 *
 *	Modified 2017.10.12
 *	-- initial
 */
lexer grammar AqlLexerRules;

options
   { superClass = 'LexerAdaptor'; }

// Common set of fragments
import LexBasic;

tokens
   { TOKEN_REF , RULE_REF , LEXER_CHAR_SET }

channels {
  HIDDEN_CHANNEL,
  WHITESPACE_CHANNEL,
  BLOCK_COMMMENT_CHANNEL,
  LINE_COMMENT_CHANNEL
}

// ======================================================
// Lexer specification
//
// -------------------------
// Comments

DOC_COMMENT
   : DocComment  -> channel  (DOC_COMMENT_CHANNEL)
   ;


BLOCK_COMMENT
   : BlockComment -> channel (BLOCK_COMMENT_CHANNEL)
   ;


BLOCK_COMMMENT
  : BlockComment -> channel(HIDDEN_CHANNEL)
  ;

LINE_COMMENT
  : LineComment   -> channel(HIDDEN_CHANNEL)
  ;

// -------------------------
// Integer
//

INTEGER
   : DecimalNumeral
   ;

NUMBER
    // 1.35, 1.35E-9, 0.3, -4.5
    :   '-'? DecimalNumeral Dot DecimalNumeral Exponent?
    // 1e10 -3e4
    |   '-'? DecimalNumeral Exponent
    // -3, 45
    |   '-'? DecimalNumeral
    ;

// -------------------------
// Literal string
//
// Aql makes no distinction between a single character
// literal and a multi-character string.
// All literals are single quote delimited and
// may contain unicode escape sequences of the form \uxxxx, where x
// is a valid hexadecimal number (per Unicode standard).

STRING_LITERAL
   : SQuoteLiteral
   ;

UNTERMINATED_STRING_LITERAL
   : USQuoteLiteral
   ;

STRING :  '"' (ESC | CHAR)* '"' ;
CHAR : '"' (ESC | ~["\\]) '"' ;

// -------------------------
// Keywords
//
// Keywords may not be used as labels for rules or in any other context where
// they would be ambiguous with the keyword vs some other identifier.
// Some blocks are handled idiomatically
// as island grammars in dedicated lexical modes.

OPTIONS
   : 'options' // -> pushMode (Options)
   ;


HTML
   : 'html' LDocQuote  -> pushMode (Html)
   ;


MARKDOWN
   : 'md' LDocQuote -> pushMode (MarkDown)
   ;

LITERAL : 'literal' ;
CONSTRAINT : 'constraint' ;
IMPORTS : 'imports' ;
FORALL : 'forall' ;
WHERE : 'where' ;
RARROW : RArrow ;
EXISTS : 'exists' ;

GRAPH : 'graph' ;
NODES : 'nodes' ;
EDGES : 'edges' ;

INSTANCE : 'instance' ;
EMPTY : 'empty' ;
SRC : 'src' ;
DST : 'dst' ;
DISTINCT : 'distinct' ;
EVAL : 'eval' ;
COEVAL : 'coeval' ;
DELTA : 'delta' ;
SIGMA : 'sigma' ;
COPRODUCT_SIGMA : 'coproduct_sigma' ;
COPRODUCT : 'coproduct' ;
COPRODUCT_UNRESTRICTED: 'coproduct_unrestricted' ;
COEQUALIZE : 'coequalize' ;
COLIMIT : 'colimit' ;
IMPORT_JDBC : 'import_jdbc' ;
QUOTIENT_JDBC : 'quotient_jdbc' ;
QUOTIENT_CSV : 'quotient_csv' ;
IMPORT_JDBC_ALL : 'import_jdbc_all' ;
IMPORT_CSV : 'import_csv' ;
STATIC_TYPING : 'static_typing' ;
QUOTIENT : 'quotient' ;
CHASE : 'chase' ;
RANDOM : 'random' ;
GENERATORS : 'generators' ;
EQUATIONS : 'equations' ;
MULTI_EQUATIONS : 'multi_equations' ;
RANDOM_SEED : 'random_seed' ;

MAPPING : 'mapping' ;
ID : 'id' ;
ENTITIES : 'entities' ;
FOREIGN_KEYS : 'foreign_keys' ;
ATTRIBUTES : 'attributes' ;
LAMBDA : 'lambda' ;

IMPORT_JOINED : IMPORT_JOINED;
MAP_NULLS_ARBITRARILY_UNSAFE :  MAP_NULLS_ARBITRARILY_UNSAFE;
INTERPRET_AS_ALGEGRA :  INTERPRET_AS_ALGEGRA;
NUM_THREADS :  NUM_THREADS;
TIMEOUT :  TIMEOUT;
REQUIRE_CONSISTENCY :  REQUIRE_CONSISTENCY;
SCHEMA_ONLY :  SCHEMA_ONLY;
ALLOW_JAVA_EQS_UNSAFE :  ALLOW_JAVA_EQS_UNSAFE;
DONT_VALIDATE_UNSAFE :  DONT_VALIDATE_UNSAFE;
ALWAYS_RELOAD :  ALWAYS_RELOAD;
CSV_FIELD_DELIM_CHAR :  CSV_FIELD_DELIM_CHAR;
CSV_ESCAPE_CHAR :  CSV_ESCAPE_CHAR;
CSV_QUOTE_CHAR :  CSV_QUOTE_CHAR;
CSV_FILE_EXTENSION :  CSV_FILE_EXTENSION;
CSV_GENERATE_IDS :  CSV_GENERATE_IDS;
ID_COLUMN_NAME :  ID_COLUMN_NAME;
VARCHAR_LENGTH :  VARCHAR_LENGTH;
START_IDS_AT :  START_IDS_AT;
IMPORT_AS_THEORY :  IMPORT_AS_THEORY;
JDBC_DEFAULT_CLASS :  JDBC_DEFAULT_CLASS;
JDBC_DEFAULT_STRING :  JDBC_DEFAULT_STRING;
DONT_VERIFY_FOR_UNSAFE :  DONT_VERIFY_FOR_UNSAFE;
PROVER :  PROVER;
PROGRAM_ALLOW_NONTERM_UNSAFE :  PROGRAM_ALLOW_NONTERM_UNSAFE;
COMPLETION_PRECEDENCE :  COMPLETION_PRECEDENCE;
COMPLETION_SORT :  COMPLETION_SORT;
COMPLETION_COMPOSE :  COMPLETION_COMPOSE;
COMPLETION_FILTER_SUBSUMED :  COMPLETION_FILTER_SUBSUMED;
COMPLETION_SYNTACTIC_AC :  COMPLETION_SYNTACTIC_AC;

GUI_MAX_TABLE_SIZE : GUI_MAX_TABLE_SIZE;
GUI_MAX_GRAPH_SIZE : GUI_MAX_GRAPH_SIZE;
GUI_MAX_STRING_SIZE : GUI_MAX_STRING_SIZE;
GUI_ROWS_TO_DISPLAY : GUI_ROWS_TO_DISPLAY;

EVAL_MAX_TEMP_SIZE : EVAL_MAX_TEMP_SIZE;
EVAL_REORDER_JOINS : EVAL_REORDER_JOINS;
EVAL_MAX_PLAN_DEPTH : EVAL_MAX_PLAN_DEPTH;
EVAL_JOIN_SELECTIVITY : EVAL_JOIN_SELECTIVITY;
EVAL_USE_INDICES : EVAL_USE_INDICES;
EVAL_USE_SQL_ABOVE : EVAL_USE_SQL_ABOVE;
EVAL_APPROX_SQL_UNSAFE : EVAL_APPROX_SQL_UNSAFE;
EVAL_SQL_PERSISTENT_INDICIES : EVAL_SQL_PERSISTENT_INDICIES;


COPRODUCT_ALLOW_ENTITY : COPRODUCT_ALLOW_ENTITY;
COPRODUCT_ALLOW_TYPE : COPRODUCT_ALLOW_TYPE;
QUERY_REMOVE_REDUNDANCY : QUERY_REMOVE_REDUNDANCY;
TRUE : TRUE;
FALSE : FALSE;

AUTO : AUTO;
FAIL : FAIL;
FREE : FREE;
SATURATED : SATURATED;
CONGRUENCE : CONGRUENCE;
MONOIDAL : MONOIDAL;
PROGRAM : PROGRAM;
COMPLETION : COMPLETION;


PRAGMA : PRAGMA;
EXEC_CMDLINE : EXEC_CMDLINE;
EXEC_JS : EXEC_JS;
EXEC_JDBC : EXEC_JDBC;
CHECK : CHECK;
ASSERT_CONSISTENT : ASSERT_CONSISTENT;
EXPORT_CSV_INSTANCE : EXPORT_CSV_INSTANCE;
EXPORT_CSV_TRANSFORM : EXPORT_CSV_TRANSFORM;
EXPORT_JDBC_INSTANCE : EXPORT_JDBC_INSTANCE;
EXPORT_JDBC_QUERY : EXPORT_JDBC_QUERY;
EXPORT_JDBC_TRANSFORM : EXPORT_JDBC_TRANSFORM ;

QUERY : QUERY;
SIMPLE : SIMPLE;
GET_MAPPING : GET_MAPPING;
FROM : FROM;
RETURN : RETURN ;

SCHEMA : SCHEMA;
SCHEMA_OF : SCHEMA_OF;
GET_SCHEMA : GET_SCHEMA;

SCHEMA_COLIMIT : SCHEMA_COLIMIT;
MODIFY : MODIFY;
WRAP : WRAP;
ENTITY_EQUATIONS : ENTITY_EQUATIONS;
PATH_EQUATIONS : PATH_EQUATIONS;
OBSERVATION_EQUATIONS : OBSERVATION_EQUATIONS;
RENAME : RENAME;
REMOVE : REMOVE;

TRANSFORM : TRANSFORM;
UNIT : UNIT;
COUNIT : COUNIT;
UNIT_QUERY : UNIT_QUERY;
COUNIT_QUERY : COUNIT_QUERY;

TYPESIDE : TYPESIDE;
SQL : SQL;
TYPESIDE_OF : TYPESIDE_OF;
TYPES : TYPES;
CONSTANTS : CONSTANTS;
FUNCTIONS : FUNCTIONS;
JAVA_TYPES : JAVA_TYPES;
JAVA_CONSTANTS : JAVA_CONSTANTS;
JAVA_FUNCTIONS : JAVA_FUNCTIONS;


// -------------------------
// Punctuation

COLON : Colon ;
COLON_COLON : DColon ;
COMMA : Comma ;
SEMI : Semi ;
LPAREN : LParen ;
RPAREN : RParen ;
LBRACE : LBrace ;
RBRACE : RBrace ;
RARROW : RArrow ;
LT : Lt ;
GT : Gt ;
ASSIGN : Equal  ;
QUESTION : Question ;
STAR : Star ;
PLUS_ASSIGN : PlusAssign ;
PLUS : Plus ;
OR : Pipe ;
DOLLAR : Dollar ;
RANGE : Range ;
DOT : Dot ;
AT : At ;
POUND : Pound ;
NOT : Tilde ;

// -------------------------
// Identifiers - allows unicode rule/token names

IDENTIFIER: IdLetter (IdLetter | DecDigit)+ ;


// -------------------------
// Whitespace

WS
   : Ws + -> channel (HIDDEN_CHANNEL)
   ;

// -------------------------
// Illegal Characters
//
// This is an illegal character trap which is always the last rule in the
// lexer specification. It matches a single character of any value and being
// the last rule in the file will match when no other rule knows what to do
// about the character. It is reported as an error but is not passed on to the
// parser. This means that the parser to deal with the gramamr file anyway
// but we will not try to analyse or code generate from a file with lexical
// errors.
//
// Comment this rule out to allow the error to be propagated to the parser

ERRCHAR
   : . -> channel (HIDDEN)
   ;


// ======================================================
// Lexer modes
// ------------------------------------------------------

mode Html ;

HTML_END
   : RDocQuote -> popMode;


mode Markdown ;

MD_END
   : RDocQuote -> popMode;


// ======================================================
// Grammar specific fragments
// ------------------------------------------------------

fragment IdLetter : 'a'..'z'|'A'..'Z'|'_'|'$' ;

fragment LDocQuote : LBrace Ws+ LParen Star Ws+ Dquote ;
fragment RDocQuote : DQuote Ws+ Star RParen Ws+ LBrace ;

fragment Exponent :   [Ee] [+\-]? DecimalNumeral ;
