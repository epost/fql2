parser grammar AqlQuery;
options { tokenVocab=AqlLexerRules; }

queryId: IDENTIFIER;
queryFromSchema: LPAREN ID schemaId RPAREN;

queryKindAssignment: QUERY queryId EQUAL queryDef ;
queryDef:
      ID schemaId                       #QueryExp_Id
    | LITERAL COLON schemaId RARROW schemaId
            LBRACE queryLiteralExpr RBRACE      #QueryExp_Literal
    | SIMPLE COLON schemaId
            LBRACE queryEntityExpr RBRACE       #QueryExp_Simple
    | GET_MAPPING schemaColimitId
            schemaId                      #QueryExp_Get
    ;
queryKind: queryId | LPAREN queryDef RPAREN;

queryLiteralExpr:
  (IMPORTS queryId*)?
  (ENTITIES (schemaEntityId RARROW LBRACE queryEntityExpr RBRACE)*)?
  (FOREIGN_KEYS queryForeignSig*)?
  (OPTIONS (timeoutOption | dontValidateUnsafeOption)*)?
  ;

queryEntityExpr: schemaEntityId RARROW LBRACE selectClause RBRACE;
selectClause:
  FROM (queryGen COLON schemaEntityId)+
  (WHERE (queryPath EQUAL queryPath)+)
  RETURN (schemaEntityId RARROW queryPath)+
  ;

queryForeignSig:
  schemaForeignId RARROW LBRACE queryPathMapping+ RBRACE;

queryPathMapping: queryGen RARROW queryPath;
queryGen: IDENTIFIER;
queryPath: queryGen (DOT schemaArrowId)*;
