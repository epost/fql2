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

'import_joined';
  'map_nulls_arbitrarily_unsafe';
  'interpret_as_algebra';
  'num_threads';
  'timeout';
  'require_consistency';
  'schema_only';
  'allow_java_eqs_unsafe';
  'dont_validate_unsafe';
  'always_reload';
  'csv_field_delim_char';
  'csv_escape_char';
  'csv_quote_char';
  'csv_file_extension';
  'csv_generate_ids';
  'id_column_name';
  'varchar_length';
  'start_ids_at';
  'import_as_theory';
  'jdbc_default_class';
  'jdbc_default_string';
  'dont_verify_is_appropriate_for_prover_unsafe';
  'prover';
  'program_allow_nontermination_unsafe';
  'completion_precedence';
  'completion_sort';
  'completion_compose';
  'completion_filter_subsumed';
  'completion_syntactic_ac';


'gui_max_table_size';
'gui_max_graph_size';
'gui_max_string_size';
'gui_rows_to_display';

'eval_max_temp_size';
'eval_reorder_joins';
'eval_max_plan_depth';
'eval_join_selectivity';
'eval_use_indices';
'eval_use_sql_above';
'eval_approx_sql_unsafe';
 'eval_sql_persistent_indices';


'coproduct_allow_entity_collisions_unsafe';
'coproduct_allow_type_collisions_unsafe';
'query_remove_redundancy';
'true';
'false';

'auto';
'fail';
'free';
'saturated';
'congruence';
'monoidal';
'program';
'completion';

 : '' ;
 : '' ;
 : '' ;
 : '' ;
 : '' ;
 : '' ;
 : '' ;


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
