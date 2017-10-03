parser grammar AqlMapping;
options { tokenVocab=AqlLexerRules; }

mappingId: IDENTIFIER;
mappingKindAssignment: 'mapping' mappingId '=' mappingDef ;
mappingDef:
      'id' schemaId                       #MapExp_Id
    | '[' mappingId ';' mappingId ']'   #MapExp_Compose
    | 'literal' ':' schemaId '->' schemaId
            '{' mappingLiteralExpr '}'      #MapExp_Literal
    ;
mappingKind: mappingId | '(' mappingDef ')';

mappingLiteralExpr:
  ('imports' mappingId*)?
  ('entities' mappingEntitySig*)?
  ('foreign_keys' mappingForeignSig*)?
  ('attributes' mappingAttributeSig*)?
  ('options' (timeoutOption | dontValidateUnsafeOption)*)?
  ;

mappingEntitySig: schemaEntityId '->' schemaEntityId;

mappingForeignSig:
  schemaForeignId '->' mappingForeignPath;

mappingForeignPath:
    mappingArrowId
  | schemaPath '.' schemaArrowId
  | schemaArrowId '(' schemaPath ')'
  ;

// identity arrows are indicated with entity-names.
mappingArrowId: schemaEntityId | schemaForeignId;

mappingAttributeSig:
  schemaAttributeId '->' (mappingLambda | schemaPath);

mappingLambda:
  'lambda' mappingGen (',' mappingGen) '.' evalMappingFn ;

mappingGen: IDENTIFIER;
evalMappingFn:
    mappingGen
  | mappingFn '(' evalMappingFn ')'
  ;

mappingFn: typesideFnName | schemaAttributeId | schemaForeignId ;
