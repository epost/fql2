

transformId: IDENTIFIER;
transformKindAssignment: 'transform' transformId '=' transformDef ;
transformDef:
      'id' ':' instanceKind              #Transform_Id
    | '[' transformId ';' transformId ']'    #Transform_Compose
    | 'distinct' transformId                 #Transform_Destination
    | 'delta' mappingKind transformId        #Transform_Delta
    | 'sigma' mappingKind transformId        #Transform_Sigma
        transformSigmaSection? transformSigmaSection?
    | 'eval' queryId transformId             #Transform_Eval
    | 'coeval' queryId transformId           #Transform_Coeval
        transformCoevalSection? transformCoevalSection?
    | 'unit' mappingKind instanceId          #Transform_Unit
        transformUnitSection?
    | 'counit' mappingKind instanceId        #Transform_Counit
        transformUnitSection?
    | 'unit_query' queryKind instanceId      #Transform_UnitQuery
        transformUnitQuerySection?
    | 'counit_query' queryKind instanceId    #Transform_CounitQuery
        transformCounitQuerySection?
    | 'import_jdbc' transformJdbcClass transformJdbcUri ':'
          instanceId '->' instanceId         #Transform_ImportJdbc
        transformImportJdbcSection?
    | 'import_csv' transformCsvFile ':'
          instanceId '->' instanceId         #Transform_ImportCsv
        transformImportCsvSection?
    | 'literal' ':' instanceId '->' instanceId #Transform_Literal
        transformLiteralSection
    ;
transformKind: transformId | '(' transformDef ')';

transformJdbcClass: STRING;
transformJdbcUri: STRING;
transformFile: STRING;
transformSqlExpr: STRING;

transformSigmaSection: '{' ('options' (proverOption)?)? '}';
transformCoevalSection: '{' ('options' (proverOption)?)? '}';
transformUnitSection: '{' ('options' (proverOption)?)? '}';
transformUnitQuerySection: '{' ('options' (proverOption)?)? '}';
transformCounitQuerySection: '{' ('options' (proverOption)?)? '}';

transformImportJdbcSection: '{'
  transformSqlEntityExpr*
  ('options' (timeoutOption | alwaysReloadOption)*)?
  '}';

transformCsvFileSection: '{'
  transformFileExpr*
  ('options' (timeoutOption | alwaysReloadOption | csvOptions)*)?
  '}';

transformSqlEntityExpr: schemaEntityId '->' transformSqlExpr;
transformFileExpr: schemaEntityId '->' transformFile;

transformLiteralSection: '{' transformLiteralExpr '}';
transformLiteralExpr:
  ('generators' (transformGen '->' transformPath)*)?
  ('options' ('transform' '=' boolean | dontValidateUnsafeOption)*)?
  ;
