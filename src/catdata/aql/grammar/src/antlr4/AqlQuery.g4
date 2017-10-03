queryId: IDENTIFIER;
queryFromSchema: '(' 'id' schemaId ')';

queryKindAssignment: 'query' queryId '=' queryDef ;
queryDef:
      'id' schemaId                       #QueryExp_Id
    | 'literal' ':' schemaId '->' schemaId
            '{' queryLiteralExpr '}'      #QueryExp_Literal
    | 'simple' ':' schemaId
            '{' queryEntityExpr '}'      #QueryExp_Simple
    | 'getMapping' schemaColimitId schemaId
    ;
queryKind: queryId | '(' queryDef ')';

queryLiteralExpr:
  ('imports' queryId*)?
  ('entities' (schemaEntityId '->' '{' queryEntityExpr '}')*)?
  ('foreign_keys' queryForeignSig*)?
  ('options' (timeoutOption | dontValidateUnsafeOption)*)?
  ;

queryEntityExpr: schemaEntityId '->' '{' selectClause '}';
selectClause:
  'from' (queryGen ':' schemaEntityId)+
  ('where' (queryPath '=' queryPath)+)
  'return' (schemaEntityId '->' queryPath)+
  ;

queryForeignSig:
  schemaForeignId '->' '{' queryPathMapping+ '}';

queryPathMapping: queryGen '->' queryPath;
queryGen: IDENTIFIER;
queryPath: queryGen ('.' arrowId)*;
