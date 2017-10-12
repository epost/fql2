parser grammar AqlConstraint;
options { tokenVocab=AqlLexerRules; }

constraintId: IDENTIFIER;
constraintKindAssignment: CONSTRAINT constraintId Equal constraintDef ;
constraintDef:
  LITERAL COLON schemaId
            LBrace constraintLiteralExpr RBrace      #constraintExp_Literal
    ;
constraintKind: constraintId | LParen constraintDef RParen;

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

constraintEquation: constraintPath Equal constraintPath;
constraintPath: constraintGen (Dot schemaArrowId)*;
