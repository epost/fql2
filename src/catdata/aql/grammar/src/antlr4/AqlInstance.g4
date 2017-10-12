parser grammar AqlInstance;
options { tokenVocab=AqlLexerRules; }

instanceId: IDENTIFIER;
instanceKindAssignment: INSTANCE instanceId Equal instanceDef ;
instanceDef:
    EMPTY COLON schemaKind
  | SRC transformKind
  | DST transformKind
  | DISTINCT instanceKind
  | EVAL queryKind instanceKind
          (OPTIONS evalOptions*)?
  | COEVAL queryKind instanceKind
          (OPTIONS (timeoutOption | proverOptions)*)?
  | DELTA mappingKind instanceKind
  | SIGMA mappingKind instanceKind
  | COPRODUCT_SIGMA (mappingKind instanceKind)+ COLON schemaKind
  | COPRODUCT instanceKind (PLUS instanceKind)* COLON schemaKind
  | COPRODUCT_UNRESTRICTED instanceId (PLUS instanceId)* COLON schemaKind
  | COEQUALIZE transformKind transformKind
  | COLIMIT graphKind schemaKind LBrace
      NODES (instanceId RARROW instanceDef)+
      EDGES (schemaArrowId RARROW transformKind)+
      (OPTIONS (timeoutOption | STATIC_TYPING Equal truthy)*)?
      RBrace
  | IMPORT_JDBC jdbcClass jdbcUri COLON schemaDef LBrace instanceImportJdbc RBrace
  | QUOTIENT_JDBC jdbcClass jdbcUri  schemaDef LBrace instanceSql+ RBrace
  | QUOTIENT_CSV schemaDef LBrace instanceFile+ RBrace
  | IMPORT_JDBC_ALL jdbcClass jdbcUri
        (OPTIONS (timeoutOption | proverOptions
                  | alwaysReloadOption
                  | requireConsistencyOption
                  | schemaOnlyOption)*)?
  | IMPORT_CSV COLON schemaDef LBrace instanceEntityFile+ RBrace
        (OPTIONS (timeoutOption | proverOptions
                  | alwaysReloadOption
                  | csvOptions
                  | idColumnNameOption
                  | requireConsistencyOption)*)?
  | LITERAL COLON schemaKind LBrace instanceLiteralExpr RBrace
  | QUOTIENT instanceKind LBrace instanceQuotientExpr RBrace
  | CHASE constraintKind* instanceKind INTEGER?
  | RANDOM COLON schemaId LBrace instanceRandomExpr RBrace
    ;
instanceKind: instanceId | LParen instanceDef RParen;

instanceLiteralExpr:
  (IMPORTS instanceId*)?
  GENERATORS (instanceGen COLON schemaEntityId)+
  (EQUATIONS instanceEquation*)?
  (MULTI_EQUATIONS instanceMultiEquation*)?
  (OPTIONS (timeoutOption | proverOptions
            | requireConsistencyOption
            | interpretAsAlgebraOption)*)?
  ;

instanceImportJdbc:
  (schemaEntityId | schemaAttributeId | schemaForeignId | typesideTypeId)
  instanceSql
  (OPTIONS (
      timeoutOption | proverOptions
    | alwaysReloadOption | requireConsistencyOption)*)?
  ;

jdbcClass: STRING;
jdbcUri: STRING;
instanceSql: STRING;
instanceFile: STRING;
instanceEntityFile: schemaEntityId RARROW instanceFile;

instanceGen: IDENTIFIER;

instanceEquation: instancePath Equal instancePath;

instanceMultiEquation:
  instanceEquationId RARROW LBrace instanceMultiValue (COMMA instanceMultiValue)* RBrace;

instanceEquationId: STRING;

instanceMultiValue:
  instancePath STRING;

instancePath:
  instanceArrowId
  | instancePath Dot instanceArrowId
  | instanceArrowId LParen instancePath RParen
  ;

// identity arrows are indicated with entity-names.
instanceArrowId: schemaEntityId | schemaForeignId;

instanceQuotientExpr:
    EQUATIONS (schemaEntityId Equal schemaEntityId)* ;

instanceRandomExpr:
  GENERATORS (schemaEntityId RARROW INTEGER)*
  | OPTIONS (RANDOM_SEED Equal INTEGER)
  ;
