parser grammar AqlInstance;
options { tokenVocab=AqlLexerRules; }

instanceId: LOWER_ID;
instanceKindAssignment: INSTANCE instanceId EQUAL instanceDef ;
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
  | COLIMIT graphKind schemaKind LBRACE
      NODES (instanceId RARROW instanceDef)+
      EDGES (schemaArrowId RARROW transformKind)+
      (OPTIONS (timeoutOption | STATIC_TYPING EQUAL truthy)*)?
      RBRACE
  | IMPORT_JDBC jdbcClass jdbcUri COLON schemaDef LBRACE instanceImportJdbc RBRACE
  | QUOTIENT_JDBC jdbcClass jdbcUri  schemaDef LBRACE instanceSql+ RBRACE
  | QUOTIENT_CSV schemaDef LBRACE instanceFile+ RBRACE
  | IMPORT_JDBC_ALL jdbcClass jdbcUri
        (OPTIONS (timeoutOption | proverOptions
                  | alwaysReloadOption
                  | requireConsistencyOption
                  | schemaOnlyOption)*)?
  | IMPORT_CSV COLON schemaDef LBRACE instanceEntityFile+ RBRACE
        (OPTIONS (timeoutOption | proverOptions
                  | alwaysReloadOption
                  | csvOptions
                  | idColumnNameOption
                  | requireConsistencyOption)*)?
  | LITERAL COLON schemaKind LBRACE instanceLiteralExpr RBRACE
  | QUOTIENT instanceKind LBRACE instanceQuotientExpr RBRACE
  | CHASE constraintKind* instanceKind INTEGER?
  | RANDOM COLON schemaId LBRACE instanceRandomExpr RBRACE
    ;
instanceKind: instanceId | LPAREN instanceDef RPAREN;

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

instanceGen: LOWER_ID;

instanceEquation: instancePath EQUAL instancePath;

instanceMultiEquation:
  instanceEquationId RARROW LBRACE instanceMultiValue (COMMA instanceMultiValue)* RBRACE;

instanceEquationId: STRING;

instanceMultiValue:
  instancePath STRING;

instancePath:
  instanceArrowId
  | instancePath DOT instanceArrowId
  | instanceArrowId LPAREN instancePath RPAREN
  ;

// identity arrows are indicated with entity-names.
instanceArrowId: schemaEntityId | schemaForeignId;

instanceQuotientExpr:
    EQUATIONS (schemaEntityId EQUAL schemaEntityId)* ;

instanceRandomExpr:
  GENERATORS (schemaEntityId RARROW INTEGER)*
  | OPTIONS (RANDOM_SEED EQUAL INTEGER)
  ;
