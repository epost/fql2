parser grammar AqlConstraint;
options { tokenVocab=AqlLexerRules; }

constraintId: IDENTIFIER;
constraintKindAssignment: 'constraint' constraintId '=' constraintDef ;
constraintDef:
  'literal' ':' schemaId
            '{' constraintLiteralExpr '}'      #constraintExp_Literal
    ;
constraintKind: constraintId | '(' constraintDef ')';

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
constraintPath: constraintGen ('.' schemaArrowId)*;
