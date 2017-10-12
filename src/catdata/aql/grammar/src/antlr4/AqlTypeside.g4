parser grammar AqlTypeside;
options { tokenVocab=AqlLexerRules; }

typesideId: IDENTIFIER;

typesideKindAssignment:
  TYPESIDE typesideId EQUAL typesideInstance;

typesideInstance:
    EMPTY | SQL
  | TYPESIDE_OF LPAREN EMPTY COLON IDENTIFIER RPAREN
  | typesideLiteralExpr;

typesideLiteralExpr:
  LITERAL LBRACE
    (IMPORTS typesideImport*)?
    (TYPES typesideTypeSig*)?
    (CONSTANTS typesideConstantSig*)?
    (FUNCTIONS typesideFunctionSig*)?
    (JAVA_TYPES (typesideTypeSig EQUAL STRING)*)?
    (JAVA_CONSTANTS (typesideConstantSig EQUAL STRING)*)?
    (JAVA_FUNCTIONS (typesideFunctionSig EQUAL STRING)*)?
    (EQUATIONS typesideEquations*)?
    (OPTIONS (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    RBRACE;

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
  IDENTIFIER (COMMA IDENTIFIER) DOT typesideEval EQUAL typesideEval;

typesideEval:
    NUMBER                       #Typeside_EvalNumber
  | IDENTIFIER                   #Typeside_EvalGen
  | IDENTIFIER LPAREN typesideEval (COMMA typesideEval)* RPAREN      #Typeside_EvalFunction
  ;
