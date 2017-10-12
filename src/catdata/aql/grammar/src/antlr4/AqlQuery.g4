parser grammar AqlQuery;
options { tokenVocab=AqlLexerRules; }

queryId: IDENTIFIER;
queryFromSchema: LParen ID schemaId RParen;

queryKindAssignment: 'query' queryId Equal queryDef ;
queryDef:
      ID schemaId                       #QueryExp_Id
    | LITERAL COLON schemaId RARROW schemaId
            LBrace queryLiteralExpr RBrace      #QueryExp_Literal
    | 'simple' COLON schemaId
            LBrace queryEntityExpr RBrace       #QueryExp_Simple
    | 'getMapping' schemaColimitId
            schemaId                      #QueryExp_Get
    ;
queryKind: queryId | LParen queryDef RParen;

queryLiteralExpr:
  (IMPORTS queryId*)?
  (ENTITIES (schemaEntityId RARROW LBrace queryEntityExpr RBrace)*)?
  (FOREIGN_KEYS queryForeignSig*)?
  (OPTIONS (timeoutOption | dontValidateUnsafeOption)*)?
  ;

queryEntityExpr: schemaEntityId RARROW LBrace selectClause RBrace;
selectClause:
  'from' (queryGen COLON schemaEntityId)+
  (WHERE (queryPath Equal queryPath)+)
  'return' (schemaEntityId RARROW queryPath)+
  ;

queryForeignSig:
  schemaForeignId RARROW LBrace queryPathMapping+ RBrace;

queryPathMapping: queryGen RARROW queryPath;
queryGen: IDENTIFIER;
queryPath: queryGen (Dot schemaArrowId)*;
