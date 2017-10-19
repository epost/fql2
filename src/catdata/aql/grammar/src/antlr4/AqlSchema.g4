parser grammar AqlSchema;
options { tokenVocab=AqlLexerRules; }

schemaId : LOWER_ID | UPPER_ID ;

schemaKindAssignment : SCHEMA schemaId EQUAL schemaDef ;

schemaDef
  : EMPTY COLON typesideId              #Schema_Empty
  | SCHEMA_OF LPAREN schemaDef RPAREN        #Schema_OfInstance
  | DST queryId                       #Schema_Destination
  | LITERAL COLON typesideId
            LBRACE schemaLiteralExpr RBRACE     #Schema_Literal
  | GET_SCHEMA schemaColimitId          #Schema_GetSchemaColimit
  ;

schemaKind : schemaId | LPAREN schemaDef RPAREN ;

schemaColimitId : LOWER_ID | UPPER_ID ;

schemaLiteralExpr:
    (IMPORTS typesideId*)?
    (ENTITIES schemaEntityId*)?
    (FOREIGN_KEYS schemaForeignSig*)?
    (PATH_EQUATIONS schemaPathEquation*)?
    (ATTRIBUTES schemaAttributeSig*)?
    (OBSERVATION_EQUATIONS schemaObservationEquationSig*)?
    (OPTIONS (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    ;

schemaEntityId: LOWER_ID | UPPER_ID;

schemaForeignSig:
  schemaForeignId COLON schemaEntityId RARROW schemaEntityId;

schemaPathEquation:
  schemaPath EQUAL schemaPath;

schemaPath:
    schemaArrowId
  | schemaPath DOT schemaArrowId
  | schemaArrowId LPAREN schemaPath RPAREN
  ;

// identity arrows are indicated with entity-names.
schemaArrowId : schemaEntityId | schemaForeignId;

schemaTermId : schemaEntityId | schemaForeignId | schemaAttributeId;

schemaAttributeSig:
  schemaAttributeId+ COLON schemaEntityId RARROW typesideTypeId;

schemaAttributeId: LOWER_ID | UPPER_ID ;

schemaObservationEquationSig
  : FORALL schemaEquationSig
  | schemaPath EQUAL schemaPath
  ;

schemaEquationSig:
  schemaGen (COMMA schemaGen)* DOT evalSchemaFn EQUAL evalSchemaFn;

evalSchemaFn:
    schemaGen
  | schemaFn LPAREN evalSchemaFn (COMMA evalSchemaFn)* RPAREN
  ;

schemaGen: LOWER_ID;
schemaFn: typesideFnName | schemaAttributeId | schemaForeignId ;

schemaForeignId: LOWER_ID;
