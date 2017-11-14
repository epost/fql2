
parser grammar AqlConstraint;
options { tokenVocab=AqlLexerRules; }

constraintId : symbol ;

constraintKindAssignment : CONSTRAINTS constraintId EQUAL constraintDef ;

constraintDef
  : LITERAL COLON schemaId
      (LBRACE constraintLiteralSection? RBRACE)?    #ConstraintExp_Literal
  ;

constraintKind
  : constraintId
  | constraintDef
  | LPAREN constraintDef RPAREN
  ;

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
    (WHERE constraintEquation+)?
  ;

constraintGen : symbol ;

constraintEquation : constraintPath EQUAL constraintPath ;

constraintPath
  : schemaArrowId
  | constraintPath DOT schemaArrowId
  | schemaArrowId LPAREN constraintPath RPAREN
  ;
