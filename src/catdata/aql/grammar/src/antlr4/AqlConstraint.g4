
parser grammar AqlConstraint;
options { tokenVocab=AqlLexerRules; }

constraintId : (LOWER_ID | UPPER_ID) ;

constraintKindAssignment : CONSTRAINTS constraintId EQUAL constraintDef ;

constraintDef
  : LITERAL COLON schemaId
      LBRACE constraintLiteralSection RBRACE      #ConstraintExp_Literal
  ;

constraintKind : constraintId | LPAREN constraintDef RPAREN ;

constraintLiteralSection
  : (IMPORTS constraintId*)?
    (constraintExpr)+
    allOptions
  ;

constraintExpr
  : FORALL (constraintGen+ COLON schemaEntityId)+
    (WHERE constraintEquation+)?
    RARROW
    (EXISTS (constraintGen COLON schemaEntityId)+)?
    WHERE constraintEquation+
  ;

constraintGen : (LOWER_ID | UPPER_ID) ;

constraintEquation : constraintPath EQUAL constraintPath ;
constraintPath : constraintGen (DOT schemaArrowId)* ;
