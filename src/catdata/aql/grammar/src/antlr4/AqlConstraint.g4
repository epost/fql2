constraintId: IDENTIFIER;
constraintKindAssignment: 'constraint' constraintId '=' constraint ;
constraint:
  'literal' ':' schemaId
            '{' constraintLiteralExpr '}'      #constraintExp_Literal
    ;

constraintLiteralExpr:
  ('imports' constraintId*)?
  (constraintExpr)+
  ('options' (timeoutOption | dontValidateUnsafeOption)*)?
  ;

constraintExpr:
  'forall' (constraintGen ':' schemaEntityId)+
  'where' constraintEquation+
  '->'
  ('exists' (constraintGen ':' schemaEntityId)+)?
  'where' constraintEquation+
  ;

constraintGen: IDENTIFIER;

constraintEquation: constraintPath '=' constraintPath;
constraintPath: constraintGen ('.' arrowId)*;
