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
importJoinedOption:  'import_joined' Equal truthy;
mapNullsArbitrarilyUnsafeOption:
  'map_nulls_arbitrarily_unsafe' Equal truthy;
interpretAsAlgebraOption:  'interpret_as_algebra' Equal truthy;

numThreadsOption: 'num_threads' Equal INTEGER;
randomSeedOption:  RANDOM_SEED Equal INTEGER;
timeoutOption:  'timeout' Equal INTEGER;
requireConsistencyOption: 'require_consistency' Equal truthy;
schemaOnlyOption:  'schema_only' Equal truthy;
allowJavaEqsUnsafeOption: 'allow_java_eqs_unsafe' Equal truthy;
dontValidateUnsafeOption: 'dont_validate_unsafe' Equal truthy;
alwaysReloadOption:  'always_reload' Equal truthy;

// docs/aqlmanual/aqlmanual.tex ch 13.10 Import Options
csvOptions:
    'csv_field_delim_char' Equal CHAR
  | 'csv_escape_char' Equal CHAR
  | 'csv_quote_char' Equal CHAR
  | 'csv_file_extension' Equal STRING
  | 'csv_generate_ids' Equal truthy
  ;

idColumnNameOption: 'id_column_name' Equal STRING;
varcharLengthOption: 'varchar_length' Equal NUMBER;
startIdsAtOption: 'start_ids_at' Equal INTEGER;
importAsTheoryOption: 'import_as_theory' Equal truthy;
jdbcDefaultClassOption: 'jdbc_default_class' Equal STRING;
jdbDefaultStringOption: 'jdbc_default_string' Equal STRING;
dVIAFProverUnsafeOption:
  'dont_verify_is_appropriate_for_prover_unsafe' Equal truthy;

// provers and their options
proverOptions:
    'prover' Equal  provers
  | 'program_allow_nontermination_unsafe' Equal truthy
  | 'completion_precedence' Equal LBrack STRING+ RBrack
  | 'completion_sort' Equal truthy
  | 'completion_compose' Equal truthy
  | 'completion_filter_subsumed' Equal truthy
  | 'completion_syntactic_ac' Equal truthy
  ;

// docs/aqlmanual/aqlmanual.tex ch 13.19 GUI Options
guiOptions:
    'gui_max_table_size' Equal INTEGER
  | 'gui_max_graph_size' Equal INTEGER
  | 'gui_max_string_size' Equal INTEGER
  | 'gui_rows_to_display' Equal INTEGER
  ;

// docs/aqlmanual/aqlmanual.tex ch 13.20 Evaluation Options
evalOptions:
    'eval_max_temp_size' Equal INTEGER
  | 'eval_reorder_joins' Equal truthy
  | 'eval_max_plan_depth' Equal INTEGER
  | 'eval_join_selectivity' Equal truthy
  | 'eval_use_indices' Equal truthy
  | 'eval_use_sql_above' Equal truthy
  | 'eval_approx_sql_unsafe' Equal truthy
  | 'eval_sql_persistent_indices' Equal truthy
  ;


// docs/aqlmanual/aqlmanual.tex ch 13.22 Coproduct Options
coproductOptions
  : 'coproduct_allow_entity_collisions_unsafe' Equal truthy
  | 'coproduct_allow_type_collisions_unsafe' Equal truthy
  ;

queryRemoveRedundancyOption: 'query_remove_redundancy' Equal truthy;

truthy : 'true' | 'false';

provers
  : 'auto'
  | 'fail'
  | 'free'
  | 'saturated'
  | 'congruence'
  | 'monoidal'
  | 'program'
  | 'completion'
  ;
