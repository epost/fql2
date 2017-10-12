parser grammar AqlMapping;
options { tokenVocab=AqlLexerRules; }

mappingId: IDENTIFIER;
mappingKindAssignment: MAPPING mappingId Equal mappingDef ;
mappingDef:
      ID schemaId                       #MapExp_Id
    | LBrack mappingId SEMI mappingId RBrack   #MapExp_Compose
    | LITERAL COLON schemaId RARROW schemaId
            LBrace mappingLiteralExpr RBrace      #MapExp_Literal
    ;
mappingKind: mappingId | LParen mappingDef RParen;

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
  | schemaPath Dot schemaArrowId
  | schemaArrowId LParen schemaPath RParen
  ;

// identity arrows are indicated with entity-names.
mappingArrowId: schemaEntityId | schemaForeignId;

mappingAttributeSig:
  schemaAttributeId RARROW (mappingLambda | schemaPath);

mappingLambda:
  LAMBDA mappingGen (COMMA mappingGen) Dot evalMappingFn ;

mappingGen: IDENTIFIER;
evalMappingFn:
    mappingGen
  | mappingFn LParen evalMappingFn RParen
  ;

mappingFn: typesideFnName | schemaAttributeId | schemaForeignId ;
