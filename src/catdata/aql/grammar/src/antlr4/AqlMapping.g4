
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
  mappingForeignId '->' mappingForeignPath;

mappingForeignPath:
    mappingArrowId
  | schemaPath '.' arrowId
  | arrowId '(' schemaPath ')'
  ;

// identity arrows are indicated with entity-names.
mappingArrowId: schemaEntityId | schemaForeignId;

mappingAttributeSig:
  mappingAttributeName '->' (mappingLambda | mappingPath);

mappingLambda:
  'lambda' mappingGen (',' mappingGen) '.' evalMappingFn ;

mappingGen: IDENTIFIER;
evalMappingFn:
    mappingGen
  | mappingFn '(' evalMappingFn ')'
  ;
mappingFn: typesideFnName | schemaAttributeId | schemaForeignId ;
