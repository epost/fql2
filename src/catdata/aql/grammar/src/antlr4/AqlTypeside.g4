parser grammar AqlTypeside;
options { tokenVocab=AqlLexerRules; }

typesideId: (LOWER_ID | UPPER_ID) ;

typesideKindAssignment
  : TYPESIDE typesideId EQUAL typesideDef ;

typesideDef
  : EMPTY
  | SQL
  | TYPESIDE_OF LPAREN EMPTY COLON (LOWER_ID | UPPER_ID) RPAREN
  | LITERAL (LBRACE typesideLiteralSection RBRACE)?
  ;

typesideLiteralSection
  : (IMPORTS typesideImport*)?
    (TYPES typesideTypeSig*)?
    (CONSTANTS typesideConstantSig*)?
    (FUNCTIONS typesideFunctionSig*)?
    (JAVA_TYPES typesideJavaTypeSig*)?
    (JAVA_CONSTANTS typesideJavaConstantSig*)?
    (JAVA_FUNCTIONS typesideJavaFunctionSig*)?
    (EQUATIONS typesideEquationSig*)?
    allOptions
  ;

typesideImport
  : (LOWER_ID | UPPER_ID)          #Typeside_ImportName
  ;

typesideTypeSig : typesideTypeId ;
typesideJavaTypeSig : (TRUE | FALSE | typesideTypeId) EQUAL STRING ;
typesideTypeId : (LOWER_ID | UPPER_ID) ;

typesideConstantSig
  : typesideConstantLiteral+ COLON typesideConstantValue
;

typesideConstantValue : (LOWER_ID | UPPER_ID) ;

typesideJavaConstantSig
  : (TRUE | FALSE | typesideConstantLiteral) EQUAL STRING ;

typesideConstantLiteral : (STRING | LOWER_ID | UPPER_ID) ;

typesideFunctionSig
  : typesideFnName COLON typesideFnLocal
        (COMMA typesideFnLocal)*
        RARROW typesideFnLocal ;

typesideFnLocal : (LOWER_ID | UPPER_ID) ;

typesideJavaFunctionSig
  : (TRUE | FALSE | typesideFnName) COLON typesideFnLocal
        (COMMA typesideFnLocal)*
        (RARROW typesideFnLocal)?
        EQUAL STRING
  ;

typesideFnName : (LOWER_ID | UPPER_ID) ;

typesideEquationSig : FORALL typesideEqFnSig ;

typesideEqFnSig
  : LOWER_ID (COMMA LOWER_ID)* DOT typesideEval EQUAL typesideEval ;

typesideEval
  : NUMBER                       #Typeside_EvalNumber
  | LOWER_ID                   #Typeside_EvalGen
  | LOWER_ID LPAREN typesideEval
      (COMMA typesideEval)* RPAREN      #Typeside_EvalFunction
  ;
