
parser grammar AqlConstraint;
options { tokenVocab=AqlLexerRules; }

constraintId: IDENTIFIER;
constraintKindAssignment: CONSTRAINT constraintId EQUAL constraintDef ;
constraintDef:
  LITERAL COLON schemaId
            LBRACE constraintLiteralExpr RBRACE      #constraintExp_Literal
    ;
constraintKind: constraintId | LPAREN constraintDef RPAREN;

constraintLiteralExpr:
  (IMPORTS constraintId*)?
  (constraintExpr)+
  (OPTIONS (timeoutOption | dontValidateUnsafeOption)*)?
  ;

constraintExpr:
  FORALL (constraintGen COLON schemaEntityId)+
  WHERE constraintEquation+
  RARROW
  (EXISTS (constraintGen COLON schemaEntityId)+)?
  WHERE constraintEquation+
  ;

constraintGen: IDENTIFIER;

constraintEquation: constraintPath EQUAL constraintPath;
constraintPath: constraintGen (DOT schemaArrowId)*;
