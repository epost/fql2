parser grammar AqlTypeside;
options { tokenVocab=AqlLexerRules; }

typesideId: LOWER_ID ;

typesideKindAssignment:
  TYPESIDE typesideId EQUAL typesideInstance;

typesideInstance:
    EMPTY | SQL
  | TYPESIDE_OF LPAREN EMPTY COLON LOWER_ID RPAREN
  | typesideLiteralExpr;

typesideLiteralExpr:
  LITERAL LBRACE
    (IMPORTS typesideImport*)?
    (TYPES typesideTypeSig*)?
    (CONSTANTS typesideConstantSig*)?
    (FUNCTIONS typesideFunctionSig*)?
    (JAVA_TYPES typesideJavaTypeSig*)?
    (JAVA_CONSTANTS typesideJavaConstantSig*)?
    (JAVA_FUNCTIONS typesideJavaFunctionSig*)?
    (EQUATIONS typesideEquationSig*)?
    (OPTIONS (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    RBRACE;

typesideImport:
  LOWER_ID                #Typeside_ImportName
  ;

typesideTypeSig : typesideTypeId ;
typesideJavaTypeSig : (TRUE | FALSE | typesideTypeId) EQUAL STRING;
typesideTypeId : UPPER_ID ;

typesideConstantSig:
  typesideConstantName COLON UPPER_ID;

typesideJavaConstantSig:
  (TRUE | FALSE | typesideConstantName) EQUAL STRING;

typesideConstantName: (LOWER_ID | UPPER_ID);

typesideFunctionSig:
  typesideFnName COLON UPPER_ID (COMMA UPPER_ID)* RARROW UPPER_ID;

typesideJavaFunctionSig:
  typesideFnName COLON UPPER_ID (COMMA UPPER_ID)*
  RARROW UPPER_ID
  EQUAL STRING ;

typesideFnName: LOWER_ID;

typesideEquationSig:
  FORALL typesideEqFnSig ;

typesideEqFnSig:
  LOWER_ID (COMMA LOWER_ID)* DOT typesideEval EQUAL typesideEval;

typesideEval:
    NUMBER                       #Typeside_EvalNumber
  | LOWER_ID                   #Typeside_EvalGen
  | LOWER_ID LPAREN typesideEval (COMMA typesideEval)* RPAREN      #Typeside_EvalFunction
  ;
