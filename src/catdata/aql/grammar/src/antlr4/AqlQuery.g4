parser grammar AqlQuery;
options { tokenVocab=AqlLexerRules; }

queryId: LOWER_ID;
queryFromSchema: LPAREN ID schemaId RPAREN;

queryKindAssignment: QUERY queryId EQUAL queryDef ;
queryDef:
      ID schemaId                       #QueryExp_Id
    | LITERAL COLON schemaId RARROW schemaId
            LBRACE queryLiteralExpr RBRACE      #QueryExp_Literal
    | SIMPLE COLON schemaId
            LBRACE querySimpleExpr RBRACE       #QueryExp_Simple
    | GET_MAPPING schemaColimitId
            schemaId                      #QueryExp_Get
    | TO_QUERY mappingId
            (LBRACE queryFromMappingExpr RBRACE)?  #QueryExp_FromMapping
    | TO_COQUERY ID schemaId
            (LBRACE queryFromSchemaExpr RBRACE)?  #QueryExp_FromMapping
    | queryCompositionExpr  #QueryExp_Composition
    ;
queryKind: queryId | LPAREN queryDef RPAREN;

queryLiteralExpr:
  (IMPORTS queryId*)?
  (ENTITIES queryEntityExpr*)?
  (FOREIGN_KEYS queryForeignSig*)?
  (OPTIONS (timeoutOption | dontValidateUnsafeOption)*)?
  ;

queryEntityExpr: schemaEntityId RARROW LBRACE queryClauseExpr RBRACE;

querySimpleExpr:
  queryClauseExpr
  (OPTIONS (timeoutOption | dontValidateUnsafeOption | proverOptions)*)?
  ;

queryLiteralValue : STRING | NUMBER | INTEGER | TRUE | FALSE ;

queryClauseExpr:
  FROM (queryGen COLON schemaEntityId)+
  (WHERE (queryPath EQUAL (queryLiteralValue | queryPath))+)?
  RETURN (schemaAttributeId RARROW queryPath)+
  ;

queryForeignSig:
  schemaForeignId RARROW LBRACE queryPathMapping+ RBRACE;

queryPathMapping: queryGen RARROW queryPath;
queryGen: LOWER_ID;
queryPath
   : queryLiteralValue | typesideConstantName
   | queryGen
   | queryGen (DOT schemaArrowId)+
   | queryGen LPAREN queryPath (COMMA queryPath)* RPAREN
   ;

queryFromMappingExpr :
  (OPTIONS (timeoutOption | dontValidateUnsafeOption | proverOptions)*)?
  ;

queryFromSchemaExpr :
  (OPTIONS (timeoutOption | dontValidateUnsafeOption | proverOptions)*)?
  ;

queryCompositionExpr :
  LBRACK ID schemaId SEMI ID schemaId RBRACK ;
