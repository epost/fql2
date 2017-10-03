parser grammar AqlTypeside;
options { tokenVocab=AqlLexerRules; }

typesideId: IDENTIFIER;

typesideKindAssignment:
  'typeside' typesideId '=' typesideInstance;

typesideInstance:
    'empty' | 'sql'
  | 'typesideOf' '(' 'empty' ':' IDENTIFIER ')'
  | typesideLiteralExpr;

typesideLiteralExpr:
  'literal' '{'
    ('imports' typesideImport*)?
    ('types' typesideTypeSig*)?
    ('constants' typesideConstantSig*)?
    ('functions' typesideFunctionSig*)?
    ('java_types' (typesideTypeSig '=' STRING)*)?
    ('java_constants' (typesideConstantSig '=' STRING)*)?
    ('java_functions' (typesideFunctionSig '=' STRING)*)?
    ('equations' typesideEquations*)?
    ('options' (timeoutOption | proverOptions | allowJavaEqsUnsafeOption)*)?
    '}';

typesideImport:
  IDENTIFIER                #Typeside_ImportName
  ;

typesideTypeSig:
  typesideTypeId ':' IDENTIFIER
  ;

typesideTypeId: IDENTIFIER;

typesideConstantSig:
  typesideConstantName ':' IDENTIFIER;

typesideConstantName: IDENTIFIER;

typesideFunctionSig:
  typesideFnName ':' IDENTIFIER (',' IDENTIFIER)* '->' IDENTIFIER;

typesideFnName: IDENTIFIER;

typesideEquations:
  'forall' typesideLambdaSig;

typesideLambdaSig:
  IDENTIFIER (',' IDENTIFIER) '.' typesideEval '=' typesideEval;

typesideEval:
    NUMBER                       #Typeside_EvalNumber
  | IDENTIFIER                   #Typeside_EvalGen
  | IDENTIFIER '(' typesideEval (',' typesideEval)* ')'      #Typeside_EvalFunction
  ;
