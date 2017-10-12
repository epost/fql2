parser grammar AqlTypeside;
options { tokenVocab=AqlLexerRules; }

typesideId: IDENTIFIER;

typesideKindAssignment:
  TYPESIDE typesideId Equal typesideInstance;

typesideInstance:
    EMPTY | SQL
  | TYPESIDE_OF LParen EMPTY COLON IDENTIFIER RParen
  | typesideLiteralExpr;

typesideLiteralExpr:
  LITERAL LBrace
    (IMPORTS typesideImport*)?
    (TYPES typesideTypeSig*)?
    (CONSTANTS typesideConstantSig*)?
    (FUNCTIONS typesideFunctionSig*)?
    (JAVA_TYPES (typesideTypeSig Equal STRING)*)?
    (JAVA_CONSTANTS (typesideConstantSig Equal STRING)*)?
    (JAVA_FUNCTIONS (typesideFunctionSig Equal STRING)*)?
    (EQUATIONS typesideEquations*)?
    (OPTIONS (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    RBrace;

typesideImport:
  IDENTIFIER                #Typeside_ImportName
  ;

typesideTypeSig:
  typesideTypeId COLON IDENTIFIER
  ;

typesideTypeId: IDENTIFIER;

typesideConstantSig:
  typesideConstantName COLON IDENTIFIER;

typesideConstantName: IDENTIFIER;

typesideFunctionSig:
  typesideFnName COLON IDENTIFIER (COMMA IDENTIFIER)* RARROW IDENTIFIER;

typesideFnName: IDENTIFIER;

typesideEquations:
  FORALL typesideLambdaSig;

typesideLambdaSig:
  IDENTIFIER (COMMA IDENTIFIER) Dot typesideEval Equal typesideEval;

typesideEval:
    NUMBER                       #Typeside_EvalNumber
  | IDENTIFIER                   #Typeside_EvalGen
  | IDENTIFIER LParen typesideEval (COMMA typesideEval)* RParen      #Typeside_EvalFunction
  ;
