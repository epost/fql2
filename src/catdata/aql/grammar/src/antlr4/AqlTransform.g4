parser grammar AqlTransform;
options { tokenVocab=AqlLexerRules; }


transformId: IDENTIFIER;
transformKindAssignment: 'transform' transformId Equal transformDef ;
transformDef:
      ID COLON instanceKind              #Transform_Id
    | LBrack transformId SEMI transformId RBrack    #Transform_Compose
    | DISTINCT transformId                 #Transform_Destination
    | DELTA mappingKind transformId        #Transform_Delta
    | SIGMA mappingKind transformId
        transformSigmaSection?
        transformSigmaSection?               #Transform_Sigma
    | EVAL queryId transformId             #Transform_Eval
    | COEVAL queryId transformId
        transformCoevalSection?
        transformCoevalSection?              #Transform_Coeval
    | 'unit' mappingKind instanceId
        transformUnitSection?                #Transform_Unit
    | 'counit' mappingKind instanceId
        transformUnitSection?                #Transform_Counit
    | 'unit_query' queryKind instanceId
        transformUnitQuerySection?           #Transform_UnitQuery
    | 'counit_query' queryKind instanceId
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
transformKind: transformId | LParen transformDef RParen;

transformJdbcClass: STRING;
transformJdbcUri: STRING;
transformFile: STRING;
transformSqlExpr: STRING;

transformSigmaSection: LBrace (OPTIONS (proverOptions)?)? RBrace;
transformCoevalSection: LBrace (OPTIONS (proverOptions)?)? RBrace;
transformUnitSection: LBrace (OPTIONS (proverOptions)?)? RBrace;
transformUnitQuerySection: LBrace (OPTIONS (proverOptions)?)? RBrace;
transformCounitQuerySection: LBrace (OPTIONS (proverOptions)?)? RBrace;

transformImportJdbcSection: LBrace
  transformSqlEntityExpr*
  (OPTIONS (timeoutOption | alwaysReloadOption)*)?
  RBrace;

transformImportCsvSection: LBrace
  transformFileExpr*
  (OPTIONS (timeoutOption | alwaysReloadOption | csvOptions)*)?
  RBrace;

transformSqlEntityExpr: schemaEntityId RARROW transformSqlExpr;
transformFileExpr: schemaEntityId RARROW transformFile;

transformLiteralSection: LBrace transformLiteralExpr RBrace;
transformLiteralExpr:
  (GENERATORS (transformGen RARROW schemaPath)*)?
  (OPTIONS ('transform' Equal truthy | dontValidateUnsafeOption)*)?
  ;

transformGen: STRING;
