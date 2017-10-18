parser grammar AqlTransform;
options { tokenVocab=AqlLexerRules; }


transformId: LOWER_ID;
transformKindAssignment: TRANSFORM transformId EQUAL transformDef ;
transformDef:
      ID instanceKind              #Transform_Id
    | LBRACK transformId SEMI transformId RBRACK    #Transform_Compose
    | DISTINCT transformId                 #Transform_Destination
    | DELTA mappingKind transformId        #Transform_Delta
    | SIGMA mappingKind transformId
        transformSigmaSection?
        transformSigmaSection?               #Transform_Sigma
    | EVAL queryKind transformId             #Transform_Eval
    | COEVAL queryKind transformId
        transformCoevalSection?
        transformCoevalSection?              #Transform_Coeval
    | UNIT mappingKind instanceId
        transformUnitSection?                #Transform_Unit
    | COUNIT mappingKind instanceId
        transformUnitSection?                #Transform_Counit
    | UNIT_QUERY queryKind instanceId
        transformUnitQuerySection?           #Transform_UnitQuery
    | COUNIT_QUERY queryKind instanceId
        transformCounitQuerySection?         #Transform_CounitQuery
    | IMPORT_JDBC transformJdbcClass transformJdbcUri COLON
        instanceId RARROW instanceId
        transformImportJdbcSection?         #Transform_ImportJdbc
    | IMPORT_CSV transformFile COLON
        instanceId RARROW instanceId
        transformImportCsvSection?          #Transform_ImportCsv
    | LITERAL COLON instanceId RARROW instanceId
        transformLiteralSection             #Transform_Literal
    ;
transformKind: transformId | LPAREN transformDef RPAREN;

transformJdbcClass: STRING;
transformJdbcUri: STRING;
transformFile: STRING;
transformSqlExpr: STRING;

transformSigmaSection: LBRACE (OPTIONS (proverOptions)?)? RBRACE;
transformCoevalSection: LBRACE (OPTIONS (proverOptions)?)? RBRACE;
transformUnitSection: LBRACE (OPTIONS (proverOptions)?)? RBRACE;
transformUnitQuerySection: LBRACE (OPTIONS (proverOptions)?)? RBRACE;
transformCounitQuerySection: LBRACE (OPTIONS (proverOptions)?)? RBRACE;

transformImportJdbcSection: LBRACE
  transformSqlEntityExpr*
  (OPTIONS (timeoutOption | alwaysReloadOption)*)?
  RBRACE;

transformImportCsvSection: LBRACE
  transformFileExpr*
  (OPTIONS (timeoutOption | alwaysReloadOption | csvOptions)*)?
  RBRACE;

transformSqlEntityExpr: schemaEntityId RARROW transformSqlExpr;
transformFileExpr: schemaEntityId RARROW transformFile;

transformLiteralSection: LBRACE transformLiteralExpr RBRACE;
transformLiteralExpr:
  (GENERATORS (transformGen RARROW schemaPath)*)?
  (OPTIONS (TRANSFORM EQUAL truthy | dontValidateUnsafeOption)*)?
  ;

transformGen: LOWER_ID;
