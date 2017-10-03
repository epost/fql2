parser grammar AqlTransform;
options { tokenVocab=AqlLexerRules; }


transformId: IDENTIFIER;
transformKindAssignment: 'transform' transformId '=' transformDef ;
transformDef:
      'id' ':' instanceKind              #Transform_Id
    | '[' transformId ';' transformId ']'    #Transform_Compose
    | 'distinct' transformId                 #Transform_Destination
    | 'delta' mappingKind transformId        #Transform_Delta
    | 'sigma' mappingKind transformId
        transformSigmaSection?
        transformSigmaSection?               #Transform_Sigma
    | 'eval' queryId transformId             #Transform_Eval
    | 'coeval' queryId transformId
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
    | 'import_jdbc' transformJdbcClass transformJdbcUri ':'
        instanceId '->' instanceId
        transformImportJdbcSection?         #Transform_ImportJdbc
    | 'import_csv' transformFile ':'
        instanceId '->' instanceId
        transformImportCsvSection?          #Transform_ImportCsv
    | 'literal' ':' instanceId '->' instanceId
        transformLiteralSection             #Transform_Literal
    ;
transformKind: transformId | '(' transformDef ')';

transformJdbcClass: STRING;
transformJdbcUri: STRING;
transformFile: STRING;
transformSqlExpr: STRING;

transformSigmaSection: '{' ('options' (proverOptions)?)? '}';
transformCoevalSection: '{' ('options' (proverOptions)?)? '}';
transformUnitSection: '{' ('options' (proverOptions)?)? '}';
transformUnitQuerySection: '{' ('options' (proverOptions)?)? '}';
transformCounitQuerySection: '{' ('options' (proverOptions)?)? '}';

transformImportJdbcSection: '{'
  transformSqlEntityExpr*
  ('options' (timeoutOption | alwaysReloadOption)*)?
  '}';

transformImportCsvSection: '{'
  transformFileExpr*
  ('options' (timeoutOption | alwaysReloadOption | csvOptions)*)?
  '}';

transformSqlEntityExpr: schemaEntityId '->' transformSqlExpr;
transformFileExpr: schemaEntityId '->' transformFile;

transformLiteralSection: '{' transformLiteralExpr '}';
transformLiteralExpr:
  ('generators' (transformGen '->' schemaPath)*)?
  ('options' ('transform' '=' boolean | dontValidateUnsafeOption)*)?
  ;

transformGen: STRING;
