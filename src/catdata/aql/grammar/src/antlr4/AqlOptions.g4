parser grammar AqlOptions;
options { tokenVocab=AqlLexerRules; }

optionsDeclaration:
    numThreadsOption
  | randomSeedOption
  | timeoutOption
  | requireConsistencyOption
  | schemaOnlyOption
  | allowJavaEqsUnsafeOption
  | dontValidateUnsafeOption
  | alwaysReloadOption
  | csvOptions
  | idColumnNameOption
  | varcharLengthOption
  | startIdsAtOption
  | importAsTheoryOption
  | jdbcDefaultClassOption
  | jdbDefaultStringOption
  | dVIAFProverUnsafeOption
  | proverOptions
  | guiOptions
  | evalOptions
  | queryRemoveRedundancyOption
  | coproductOptions
  ;

// options not mentioned in the manual
importJoinedOption:  IMPORT_JOINED Equal truthy;
mapNullsArbitrarilyUnsafeOption:
  MAP_NULLS_ARBITRARILY_UNSAFE Equal truthy;
interpretAsAlgebraOption:  INTERPRET_AS_ALGEGRA Equal truthy;

numThreadsOption: NUM_THREADS Equal INTEGER;
randomSeedOption:  RANDOM_SEED Equal INTEGER;
timeoutOption:  TIMEOUT Equal INTEGER;
requireConsistencyOption: REQUIRE_CONSISTENCY Equal truthy;
schemaOnlyOption:  SCHEMA_ONLY Equal truthy;
allowJavaEqsUnsafeOption: ALLOW_JAVA_EQS_UNSAFE Equal truthy;
dontValidateUnsafeOption: DONT_VALIDATE_UNSAFE Equal truthy;
alwaysReloadOption:  ALWAYS_RELOAD Equal truthy;

// docs/aqlmanual/aqlmanual.tex ch 13.10 Import Options
csvOptions:
    CSV_FIELD_DELIM_CHAR Equal CHAR
  | CSV_ESCAPE_CHAR Equal CHAR
  | CSV_QUOTE_CHAR Equal CHAR
  | CSV_FILE_EXTENSION Equal STRING
  | CSV_GENERATE_IDS Equal truthy
  ;

idColumnNameOption: ID_COLUMN_NAME Equal STRING;
varcharLengthOption: VARCHAR_LENGTH Equal NUMBER;
startIdsAtOption: START_IDS_AT Equal INTEGER;
importAsTheoryOption: IMPORT_AS_THEORY Equal truthy;
jdbcDefaultClassOption: JDBC_DEFAULT_CLASS Equal STRING;
jdbDefaultStringOption: JDBC_DEFAULT_STRING Equal STRING;
dVIAFProverUnsafeOption:
  DONT_VERIFY_FOR_UNSAFE Equal truthy;

// provers and their options
proverOptions:
    PROVER Equal  provers
  | PROGRAM_ALLOW_NONTERM_UNSAFE Equal truthy
  | COMPLETION_PRECEDENCE Equal LBrack STRING+ RBrack
  | COMPLETION_SORT Equal truthy
  | COMPLETION_COMPOSE Equal truthy
  | COMPLETION_FILTER_SUBSUMED Equal truthy
  | COMPLETION_SYNTACTIC_AC Equal truthy
  ;

// docs/aqlmanual/aqlmanual.tex ch 13.19 GUI Options
guiOptions:
    GUI_MAX_TABLE_SIZE Equal INTEGER
  | GUI_MAX_GRAPH_SIZE Equal INTEGER
  | GUI_MAX_STRING_SIZE Equal INTEGER
  | GUI_ROWS_TO_DISPLAY Equal INTEGER
  ;

// docs/aqlmanual/aqlmanual.tex ch 13.20 Evaluation Options
evalOptions:
    EVAL_MAX_TEMP_SIZE Equal INTEGER
  | EVAL_REORDER_JOINS Equal truthy
  | EVAL_MAX_PLAN_DEPTH Equal INTEGER
  | EVAL_JOIN_SELECTIVITY Equal truthy
  | EVAL_USE_INDICES Equal truthy
  | EVAL_USE_SQL_ABOVE Equal truthy
  | EVAL_APPROX_SQL_UNSAFE Equal truthy
  | EVAL_SQL_PERSISTENT_INDICIES Equal truthy
  ;


// docs/aqlmanual/aqlmanual.tex ch 13.22 Coproduct Options
coproductOptions
  : COPRODUCT_ALLOW_ENTITY Equal truthy
  | COPRODUCT_ALLOW_TYPE Equal truthy
  ;

queryRemoveRedundancyOption: QUERY_REMOVE_REDUNDANCY Equal truthy;

truthy : TRUE | FALSE;

provers
  : AUTO
  | FAIL
  | FREE
  | SATURATED
  | CONGRUENCE
  | MONOIDAL
  | PROGRAM
  | COMPLETION
  ;
