parser grammar AqlMapping;
options { tokenVocab=AqlLexerRules; }

mappingId: LOWER_ID;
mappingKindAssignment: MAPPING mappingId EQUAL mappingDef ;
mappingDef:
      ID schemaId                       #MapExp_Id
    | LBRACK mappingId SEMI mappingId RBRACK   #MapExp_Compose
    | LITERAL COLON schemaId RARROW schemaId
            LBRACE mappingLiteralExpr RBRACE      #MapExp_Literal
    ;
mappingKind: mappingId | LPAREN mappingDef RPAREN;

mappingLiteralExpr:
  (IMPORTS mappingId*)?
  (ENTITIES mappingEntitySig*)?
  (FOREIGN_KEYS mappingForeignSig*)?
  (ATTRIBUTES mappingAttributeSig*)?
  (OPTIONS (timeoutOption | dontValidateUnsafeOption)*)?
  ;

mappingEntitySig: schemaEntityId RARROW schemaEntityId;

mappingForeignSig:
  schemaForeignId RARROW mappingForeignPath;

mappingForeignPath:
    mappingArrowId
  | schemaPath DOT schemaArrowId
  | schemaArrowId LPAREN schemaPath RPAREN
  ;

// identity arrows are indicated with entity-names.
mappingArrowId: schemaEntityId | schemaForeignId;

mappingAttributeSig:
  schemaAttributeId RARROW (mappingLambda | schemaPath);

mappingLambda:
  LAMBDA mappingGen (COMMA mappingGen)* DOT evalMappingFn ;

mappingGen: LOWER_ID;
evalMappingFn:
    mappingGen
  | mappingFn LPAREN evalMappingFn (COMMA evalMappingFn)* RPAREN
  | LPAREN evalMappingFn (typesideFnName evalMappingFn)* RPAREN
  ;

mappingFn: typesideFnName | schemaAttributeId | schemaForeignId ;
