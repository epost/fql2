parser grammar AqlMapping;
options { tokenVocab=AqlLexerRules; }

mappingId : symbol ;

mappingKindAssignment : MAPPING mappingId EQUAL mappingDef ;

mappingDef
  : ID schemaId                       #MapExp_Id
  | LBRACK mappingId SEMI mappingId RBRACK   #MapExp_Compose
  | LITERAL COLON schemaId RARROW schemaId
            LBRACE mappingLiteralSection RBRACE      #MapExp_Literal
  | GET_MAPPING schemaColimitId schemaId #MapExp_Get
  ;

mappingKind
  : mappingId
  | mappingDef
  | LPAREN mappingDef RPAREN
  ;

mappingLiteralSection
  : (IMPORTS mappingId*)?
    (ENTITIES mappingEntitySig*)?
    (FOREIGN_KEYS mappingForeignSig*)?
    (ATTRIBUTES mappingAttributeSig*)?
    allOptions
  ;

mappingEntitySig : schemaEntityId RARROW schemaEntityId ;

mappingForeignSig
  : schemaForeignId RARROW mappingForeignPath ;

mappingForeignPath
  : mappingArrowId
  | schemaPath DOT schemaArrowId
  | schemaArrowId LPAREN schemaPath RPAREN
  ;

// identity arrows are indicated with entity-names.
mappingArrowId : schemaEntityId | schemaForeignId ;

mappingAttributeSig
  : schemaAttributeId RARROW (mappingLambda | schemaPath) ;

mappingLambda
  : LAMBDA mappingGen (COMMA mappingGen)* DOT evalMappingFn ;

mappingGen : symbol (COLON mappingGenType)? ;
mappingGenType : symbol ;

evalMappingFn
  : mappingGen
  | mappingFn LPAREN evalMappingFn (COMMA evalMappingFn)* RPAREN
  | LPAREN evalMappingFn (typesideFnName evalMappingFn)* RPAREN
  ;

mappingFn : typesideFnName | schemaAttributeId | schemaForeignId ;
