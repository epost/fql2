parser grammar AqlInstance;
options { tokenVocab=AqlLexerRules; }

instanceId: LOWER_ID;
instanceKindAssignment: INSTANCE instanceId EQUAL instanceDef ;
instanceDef:
    EMPTY COLON schemaKind
  | SRC transformKind
  | DST transformKind
  | DISTINCT instanceKind
  | EVAL queryKind instanceKind (LBRACE instanceEvalSection RBRACE)?
  | COEVAL queryKind instanceKind (LBRACE instanceCoevalSection RBRACE)?
  | DELTA mappingKind instanceKind
  | SIGMA mappingKind instanceKind (LBRACE instanceSigmaSection RBRACE)?
  | COPRODUCT_SIGMA (mappingKind instanceKind)+ COLON schemaKind
      (LBRACE instanceCoprodSigmaSection RBRACE)?
  | COPRODUCT instanceKind (PLUS instanceKind)* COLON schemaKind
      (LBRACE instanceCoprodSection RBRACE)?
  | COPRODUCT_UNRESTRICTED instanceId (PLUS instanceId)* COLON schemaKind
      (LBRACE instanceCoprodUnrestrictSection RBRACE)?
  | COEQUALIZE transformKind transformKind
      (LBRACE instanceCoequalizeSection RBRACE)?
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
  | CHASE LITERAL COLON instanceConstraint* instanceKind INTEGER?
  | RANDOM COLON schemaId LBRACE instanceRandomExpr RBRACE
  ;
instanceKind: instanceId | LPAREN instanceDef RPAREN;

// the documentation for the chase constrants is unclear
instanceConstraint
  : schemaId
  | LBRACE RBRACE
  ;

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
  instanceEquationId RARROW LBRACE instanceMultiBind (COMMA instanceMultiBind)* RBRACE;

instanceEquationId: LOWER_ID;

instanceMultiBind:
  instancePath UPPER_ID;

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

instanceEvalSection : (OPTIONS evalOptions*)? ;
instanceCoevalSection : (OPTIONS (timeoutOption | proverOptions)*)?  ;
instanceSigmaSection : (OPTIONS (timeoutOption | proverOptions)*)? ;
instanceCoprodSection : (OPTIONS (timeoutOption | proverOptions)*)? ;
instanceCoprodSigmaSection : (OPTIONS (timeoutOption | proverOptions)*)? ;
instanceCoprodUnrestrictSection : (OPTIONS (timeoutOption | proverOptions)*)? ;
instanceCoequalizeSection : (OPTIONS (timeoutOption | proverOptions)*)? ;
