parser grammar AqlSchema;
options { tokenVocab=AqlLexerRules; }

schemaId : symbol ;

schemaKindAssignment : SCHEMA schemaId EQUAL schemaDef ;

schemaDef
  : EMPTY COLON typesideKind                 #Schema_Empty
  | SCHEMA_OF (INSTANCE_ALL | instanceKind)  #Schema_OfInstance
  | DST queryId                              #Schema_Destination
  | LITERAL COLON typesideKind
      (LBRACE schemaLiteralSection RBRACE)? #Schema_Literal
  | GET_SCHEMA schemaColimitId               #Schema_GetSchemaColimit
  ;

schemaKind : schemaId | schemaDef | (LPAREN schemaDef RPAREN) ;

schemaColimitId : symbol ;

schemaLiteralSection
  : (IMPORTS typesideId*)?
    (ENTITIES schemaEntityId*)?
    (FOREIGN_KEYS schemaForeignSig*)?
    (PATH_EQUATIONS schemaPathEquation*)?
    (ATTRIBUTES schemaAttributeSig*)?
    (OBSERVATION_EQUATIONS schemaObservationEquationSig*)?
    allOptions
  ;

schemaEntityId : symbol ;

schemaForeignSig
  : schemaForeignId+ COLON schemaEntityId RARROW schemaEntityId ;

schemaPathEquation : schemaPath EQUAL schemaPath ;

schemaPath
  : schemaArrowId
  | schemaPath DOT schemaArrowId
  | schemaArrowId LPAREN schemaPath RPAREN
  ;

// identity arrows are indicated with entity-names.
schemaArrowId : schemaEntityId | schemaForeignId ;

schemaTermId : schemaEntityId | schemaForeignId | schemaAttributeId ;

schemaAttributeSig
  : schemaAttributeId+ COLON schemaEntityId RARROW typesideTypeId ;

schemaAttributeId : symbol ;

schemaObservationEquationSig
  : FORALL schemaEquationSig
  | schemaPath EQUAL schemaPath
  ;

schemaEquationSig
  : schemaGen (COMMA schemaGen)* DOT evalSchemaFn EQUAL evalSchemaFn ;

evalSchemaFn
  : schemaLiteralValue
  | schemaGen
  | schemaFn LPAREN evalSchemaFn (COMMA evalSchemaFn)* RPAREN
  | schemaFn DOT evalSchemaFn
  ;

schemaGen : symbol (COLON schemaGenType)? ;
schemaGenType : symbol ;

schemaFn
  : typesideFnName
  | schemaAttributeId
  | schemaForeignId
  ;

schemaForeignId : symbol ;

schemaLiteralValue
  : INTEGER
  | NUMBER
  | truthy
  | STRING
  ;
