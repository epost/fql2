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
importJoinedOption:  'import_joined' '=' truthy;
mapNullsArbitrarilyUnsafeOption:
  'map_nulls_arbitrarily_unsafe' '=' truthy;
interpretAsAlgebraOption:  'interpret_as_algebra' '=' truthy;

numThreadsOption: 'num_threads' '=' INTEGER;
randomSeedOption:  'random_seed' '=' INTEGER;
timeoutOption:  'timeout' '=' INTEGER;
requireConsistencyOption: 'require_consistency' '=' truthy;
schemaOnlyOption:  'schema_only' '=' truthy;
allowJavaEqsUnsafeOption: 'allow_java_eqs_unsafe' '=' truthy;
dontValidateUnsafeOption: 'dont_validate_unsafe' '=' truthy;
alwaysReloadOption:  'always_reload' '=' truthy;

// docs/aqlmanual/aqlmanual.tex ch 13.10 Import Options
csvOptions:
    'csv_field_delim_char' '=' CHAR
  | 'csv_escape_char' '=' CHAR
  | 'csv_quote_char' '=' CHAR
  | 'csv_file_extension' '=' STRING
  | 'csv_generate_ids' '=' truthy
  ;

idColumnNameOption: 'id_column_name' '=' STRING;
varcharLengthOption: 'varchar_length' '=' NUMBER;
startIdsAtOption: 'start_ids_at' '=' INTEGER;
importAsTheoryOption: 'import_as_theory' '=' truthy;
jdbcDefaultClassOption: 'jdbc_default_class' '=' STRING;
jdbDefaultStringOption: 'jdbc_default_string' '=' STRING;
dVIAFProverUnsafeOption:
  'dont_verify_is_appropriate_for_prover_unsafe' '=' truthy;

// provers and their options
proverOptions:
    'prover' '='  provers
  | 'program_allow_nontermination_unsafe' '=' truthy
  | 'completion_precedence' '=' '[' STRING+ ']'
  | 'completion_sort' '=' truthy
  | 'completion_compose' '=' truthy
  | 'completion_filter_subsumed' '=' truthy
  | 'completion_syntactic_ac' '=' truthy
  ;

// docs/aqlmanual/aqlmanual.tex ch 13.19 GUI Options
guiOptions:
    'gui_max_table_size' '=' INTEGER
  | 'gui_max_graph_size' '=' INTEGER
  | 'gui_max_string_size' '=' INTEGER
  | 'gui_rows_to_display' '=' INTEGER
  ;

// docs/aqlmanual/aqlmanual.tex ch 13.20 Evaluation Options
evalOptions:
    'eval_max_temp_size' '=' INTEGER
  | 'eval_reorder_joins' '=' truthy
  | 'eval_max_plan_depth' '=' INTEGER
  | 'eval_join_selectivity' '=' truthy
  | 'eval_use_indices' '=' truthy
  | 'eval_use_sql_above' '=' truthy
  | 'eval_approx_sql_unsafe' '=' truthy
  | 'eval_sql_persistent_indices' '=' truthy
  ;


// docs/aqlmanual/aqlmanual.tex ch 13.22 Coproduct Options
coproductOptions:
    'coproduct_allow_entity_collisions_unsafe' '=' truthy
  | 'coproduct_allow_type_collisions_unsafe' '=' truthy
  ;

queryRemoveRedundancyOption: 'query_remove_redundancy' '=' truthy;

truthy: 'true' | 'false';

provers:
    'auto'
  | 'fail'
  |  'free'
  | 'saturated'
  | 'congruence'
  | 'monoidal'
  | 'program'
  | 'completion'
  ;
