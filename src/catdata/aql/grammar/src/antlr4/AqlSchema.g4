parser grammar AqlSchema;
options { tokenVocab=AqlLexerRules; }

schemaId: IDENTIFIER;
schemaKindAssignment: SCHEMA schemaId Equal schemaDef ;
schemaDef:
      EMPTY COLON typesideId              #Schema_Empty
    | SCHEMA_OF LParen schemaDef RParen        #Schema_OfInstance
    | DST queryId                       #Schema_Destination
    | LITERAL COLON typesideId
            LBrace schemaLiteralExpr RBrace     #Schema_Literal
    | GET_SCHEMA schemaColimitId          #Schema_GetSchemaColimit
    ;
schemaKind: schemaId | LParen schemaDef RParen;

schemaColimitId: IDENTIFIER;

schemaLiteralExpr:
    (IMPORTS typesideId*)?
    (ENTITIES schemaEntityId*)?
    (FOREIGN_KEYS schemaForeignSig*)?
    (PATH_EQUATIONS schemaPathEquation*)?
    (ATTRIBUTES schemaAttributeSig*)?
    (OBSERVATION_EQUATIONS schemaObservationEquationSig*)?
    (OPTIONS (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    ;

schemaEntityId: IDENTIFIER;

schemaForeignSig:
  schemaForeignId COLON schemaEntityId RARROW schemaEntityId;

schemaPathEquation:
  schemaPath Equal schemaPath;

schemaPath:
    schemaArrowId
  | schemaPath Dot schemaArrowId
  | schemaArrowId LParen schemaPath RParen
  ;

// identity arrows are indicated with entity-names.
schemaArrowId: schemaEntityId | schemaForeignId;
schemaTermId: schemaEntityId | schemaForeignId | schemaAttributeId;

schemaAttributeSig:
       schemaAttributeId COLON schemaEntityId RARROW typesideTypeId;

schemaAttributeId: IDENTIFIER;

schemaObservationEquationSig:
  FORALL schemaEquationSig;

schemaEquationSig:
  schemaGen (COMMA schemaGen)* Dot evalSchemaFn Equal evalSchemaFn;

evalSchemaFn:
    schemaGen
  | schemaFn LParen evalSchemaFn RParen
  ;

schemaGen: IDENTIFIER;
schemaFn: typesideFnName | schemaAttributeId | schemaForeignId ;

schemaForeignId: IDENTIFIER;
