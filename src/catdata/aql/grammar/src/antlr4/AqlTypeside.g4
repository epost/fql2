parser grammar AqlTypeside;
options { tokenVocab=AqlLexerRules; }

typesideId: symbol ;

typesideKindAssignment
  : TYPESIDE typesideId EQUAL typesideDef ;

typesideDef
  : EMPTY
  | SQL
  | TYPESIDE_OF schemaKind
  | LITERAL (LBRACE typesideLiteralSection RBRACE)?
  ;

typesideKind : typesideId | typesideDef | (LPAREN typesideDef RPAREN) ;

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
  : symbol          #Typeside_ImportName
  ;

typesideTypeSig : typesideTypeId ;
typesideJavaTypeSig : (TRUE | FALSE | typesideTypeId) EQUAL STRING ;
typesideTypeId : symbol ;

typesideConstantSig
  : typesideConstantLiteral+ COLON typesideConstantValue
;

typesideConstantValue : symbol ;

typesideJavaConstantSig
  : (truthy | typesideConstantLiteral) EQUAL STRING ;

typesideConstantLiteral : (STRING | LOWER_ID | UPPER_ID) ;

typesideFunctionSig
  : typesideFnName COLON typesideFnLocal
        (COMMA typesideFnLocal)*
        RARROW typesideFnLocal ;

typesideFnLocal : symbol ;

typesideJavaFunctionSig
  : (truthy | typesideFnName) COLON
        (typesideFnLocal (COMMA typesideFnLocal)*)?
        (RARROW typesideFnLocal)?
        EQUAL STRING
  ;

typesideFnName : symbol ;

typesideEquationSig
  : FORALL
    (typesideLocal (COMMA typesideLocal)*
      | typesideLocal+)
    DOT typesideEval EQUAL typesideEval
  ;

typesideLocal : symbol (COLON typesideLocalType)? ;
typesideLocalType : symbol ;

typesideEval
  : NUMBER
  #Typeside_EvalNumber

  | typesideLiteral
  #Typeside_EvalGen

  | LPAREN typesideEval typesideFnName typesideEval RPAREN
  #Typeside_InfixFunction

  | typesideFnName LPAREN typesideEval (COMMA typesideEval)* RPAREN
  #Typeside_EvalFunction
  ;

typesideLiteral
  : LOWER_ID
  | UPPER_ID
  ;
