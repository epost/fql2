parser grammar AqlQuery;
options { tokenVocab=AqlLexerRules; }

queryId : symbol ;

queryFromSchema : LPAREN ID schemaId RPAREN ;

queryKindAssignment : QUERY queryId EQUAL queryDef ;

queryDef
  : ID schemaId
  #QueryExp_Id

  | LITERAL COLON schemaKind RARROW schemaId
      (LBRACE queryLiteralSection RBRACE)?
  #QueryExp_Literal

  | SIMPLE COLON schemaKind
      (LBRACE querySimpleSection RBRACE)?
  #QueryExp_Simple

  | GET_MAPPING schemaColimitId schemaKind
  #QueryExp_Get

  | TO_QUERY mappingKind
      (LBRACE queryFromMappingSection RBRACE)?
  #QueryExp_FromMapping

  | TO_COQUERY mappingKind
      (LBRACE queryFromSchemaSection RBRACE)?
  #QueryExp_FromMapping

  | queryCompositionExpr
  #QueryExp_Composition
  ;

queryKind
  : queryId
  | queryDef
  | LPAREN queryDef RPAREN
  ;

queryLiteralSection
  : (IMPORTS queryId*)?
    (ENTITIES queryEntityExpr*)?
    (FOREIGN_KEYS queryForeignSig*)?
    allOptions
  ;

queryEntityExpr : schemaEntityId RARROW LBRACE queryClauseExpr RBRACE ;

querySimpleSection : queryClauseExpr allOptions ;

queryLiteralValue : STRING | NUMBER | INTEGER | TRUE | FALSE ;

queryClauseExpr
  : FROM (queryGen COLON schemaEntityId)+
    (WHERE (queryPath EQUAL (queryLiteralValue | queryPath))+)?
    (RETURN (schemaAttributeId RARROW queryPath)+)?
  ;

queryForeignSig
  : schemaForeignId RARROW LBRACE queryPathMapping+ RBRACE ;

queryPathMapping : queryGen RARROW queryPath ;

queryGen : symbol ;

queryPath
   : queryLiteralValue
   | typesideConstantLiteral
   | queryGen
   | queryGen (DOT schemaArrowId)+
   | queryGen LPAREN queryPath (COMMA queryPath)* RPAREN
   ;

queryFromMappingSection : allOptions ;

queryFromSchemaSection : allOptions ;

queryCompositionExpr
  : LBRACK queryKind SEMI queryKind RBRACK ;
