parser grammar AqlTransform;
options { tokenVocab=AqlLexerRules; }

transformId : symbol ;

transformKindAssignment : TRANSFORM transformId EQUAL transformDef ;

transformDef
  : ID instanceKind                                 #Transform_Id
  | LBRACK transformId SEMI transformId RBRACK      #Transform_Compose
  | DISTINCT transformId                            #Transform_Destination
  | DELTA mappingKind transformId                   #Transform_Delta
  | SIGMA mappingKind transformId
      (LBRACE transformSigmaSection RBRACE)?
      (LBRACE transformSigmaSection RBRACE)?        #Transform_Sigma
  | EVAL queryKind transformId                      #Transform_Eval
  | COEVAL queryKind transformId
      (LBRACE transformCoevalSection RBRACE)?
      (LBRACE transformCoevalSection RBRACE)?       #Transform_Coeval
  | UNIT mappingKind instanceId
      (LBRACE transformUnitSection RBRACE)?         #Transform_Unit
  | COUNIT mappingKind instanceId
      (LBRACE transformUnitSection RBRACE)?         #Transform_Counit
  | UNIT_QUERY queryKind instanceId
      (LBRACE transformUnitQuerySection RBRACE)?    #Transform_UnitQuery
  | COUNIT_QUERY queryKind instanceId
      (LBRACE transformCounitQuerySection RBRACE)?  #Transform_CounitQuery
  | IMPORT_JDBC transformJdbcClass transformJdbcUri COLON
      instanceId RARROW instanceId
      (LBRACE transformImportJdbcSection RBRACE)?   #Transform_ImportJdbc
  | IMPORT_CSV transformFile COLON
      instanceId RARROW instanceId
      (LBRACE transformImportCsvSection  RBRACE)?   #Transform_ImportCsv
  | LITERAL COLON instanceKind RARROW instanceId
      (LBRACE transformLiteralSection RBRACE)?      #Transform_Literal
  ;

transformKind : transformId | (LPAREN transformDef RPAREN) ;

transformJdbcClass : STRING ;
transformJdbcUri : STRING ;
transformFile : STRING ;
transformSqlExpr : STRING ;

transformSigmaSection : allOptions  ;
transformCoevalSection : allOptions ;
transformUnitSection : allOptions ;
transformUnitQuerySection : allOptions ;
transformCounitQuerySection : allOptions ;

transformImportJdbcSection : transformSqlEntityExpr* allOptions ;

transformImportCsvSection : transformFileExpr* allOptions ;

transformSqlEntityExpr : schemaEntityId RARROW transformSqlExpr ;
transformFileExpr : schemaEntityId RARROW transformFile ;

transformLiteralSection
  : (GENERATORS (transformGen RARROW schemaPath)*)?
    allOptions
  ;

transformGen : symbol ;
