
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
importJoinedOption:  'import_joined' '=' boolean;
mapNullsArbitrarilyUnsafeOption:
  'map_nulls_arbitrarily_unsafe' '=' boolean;
interpretAsAlgebraOption:  'interpret_as_algebra' '=' boolean;

numThreadsOption: 'num_threads' '=' INTEGER;
randomSeedOption:  'random_seed' '=' INTEGER;
timeoutOption:  'timeout' '=' INTEGER;
requireConsistencyOption: 'require_consistency' '=' boolean;
schemaOnlyOption:  'schema_only' '=' boolean;
allowJavaEqsUnsafeOption: 'allow_java_eqs_unsafe' '=' boolean;
dontValidateUnsafeOption: 'dont_validate_unsafe' '=' boolean;
alwaysReloadOption:  'always_reload' '=' boolean;

// docs/aqlmanual/aqlmanual.tex ch 13.10 Import Options
csvOptions:
    'csv_field_delim_char' '=' CHAR
  | 'csv_escape_char' '=' CHAR
  | 'csv_quote_char' '=' CHAR
  | 'csv_file_extension' '=' STRING
  | 'csv_generate_ids' '=' boolean
  ;

idColumnNameOption: 'id_column_name' '=' STRING;
varcharLengthOption: 'varchar_length' '=' NUMBER;
startIdsAtOption: 'start_ids_at' '=' INTEGER;
importAsTheoryOption: 'import_as_theory' '=' boolean;
jdbcDefaultClassOption: 'jdbc_default_class' '=' STRING;
jdbDefaultStringOption: 'jdbc_default_string' '=' STRING;
dVIAFProverUnsafeOption:
  'dont_verify_is_appropriate_for_prover_unsafe' '=' boolean;

// provers and their options
proverOptions:
    'prover' '='  provers
  | 'program_allow_nontermination_unsafe' '=' boolean
  | 'completion_precedence' '=' '[' STRING+ ']'
  | 'completion_sort' '=' boolean
  | 'completion_compose' '=' boolean
  | 'completion_filter_subsumed' '=' boolean
  | 'completion_syntactic_ac' '=' boolean
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
  | 'eval_reorder_joins' '=' boolean
  | 'eval_max_plan_depth' '=' INTEGER
  | 'eval_join_selectivity' '=' boolean
  | 'eval_use_indices' '=' boolean
  | 'eval_use_sql_above' '=' boolean
  | 'eval_approx_sql_unsafe' '=' boolean
  | 'eval_sql_persistent_indices' '=' boolean
  ;


// docs/aqlmanual/aqlmanual.tex ch 13.22 Coproduct Options
coproductOptions:
    'coproduct_allow_entity_collisions_unsafe' '=' boolean
  | 'coproduct_allow_type_collisions_unsafe' '=' boolean
  ;

queryRemoveRedundancyOption: 'query_remove_redundancy' '=' boolean;

boolean: 'true' | 'false';

provers:
    'auto'
  | 'fail'
  |  'free'
  | 'saturated'
  | 'congruence'
  | 'monoidal'
  | 'program'
  | 'completion'
