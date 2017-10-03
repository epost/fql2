parser grammar AqlSchema;
options { tokenVocab=AqlLexerRules; }

schemaId: IDENTIFIER;
schemaKindAssignment: 'schema' schemaId '=' schemaDef ;
schemaDef:
      'empty' ':' typesideId              #Schema_Empty
    | 'schemaOf' '(' schemaDef ')'        #Schema_OfInstance
    | 'dst' queryId                       #Schema_Destination
    | 'literal' ':' typesideId
            '{' schemaLiteralExpr '}'     #Schema_Literal
    | 'getSchema' schemaColimitId          #Schema_GetSchemaColimit
    ;
schemaKind: schemaId | '(' schemaDef ')';

schemaColimitId: IDENTIFIER;

schemaLiteralExpr:
    ('imports' typesideId*)?
    ('entities' schemaEntityId*)?
    ('foreign_keys' schemaForeignSig*)?
    ('path_equations' schemaPathEquation*)?
    ('attributes' schemaAttributeSig*)?
    ('observation_equations' schemaObservationEquationSig*)?
    ('options' (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    ;

schemaEntityId: IDENTIFIER;

schemaForeignSig:
  schemaForeignId ':' schemaEntityId '->' schemaEntityId;

schemaPathEquation:
  schemaPath '=' schemaPath;

schemaPath:
    schemaArrowId
  | schemaPath '.' schemaArrowId
  | schemaArrowId '(' schemaPath ')'
  ;

// identity arrows are indicated with entity-names.
schemaArrowId: schemaEntityId | schemaForeignId;
schemaTermId: schemaEntityId | schemaForeignId | schemaAttributeId;

schemaAttributeSig:
       schemaAttributeId ':' schemaEntityId '->' typesideTypeId;

schemaAttributeId: IDENTIFIER;

schemaObservationEquationSig:
  'forall' schemaEquationSig;

schemaEquationSig:
  schemaGen (',' schemaGen)* '.' evalSchemaFn '=' evalSchemaFn;

evalSchemaFn:
    schemaGen
  | schemaFn '(' evalSchemaFn ')'
  ;

schemaGen: IDENTIFIER;
schemaFn: typesideFnName | schemaAttributeId | schemaForeignId ;

schemaForeignId: IDENTIFIER;
