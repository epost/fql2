parser grammar AqlInstance;
options { tokenVocab=AqlLexerRules; }

instanceId : symbol ;

instanceKindAssignment : INSTANCE instanceId EQUAL instanceDef ;

instanceDef
  : EMPTY COLON schemaKind
  #Instance_Empty

  | SRC transformKind
  #Instance_Src

  | DST transformKind
  #Instance_Dst

  | DISTINCT instanceKind
  #Instance_Distinct

  | EVAL queryKind instanceKind
    (LBRACE instanceEvalSection RBRACE)?
  #Instance_Eval

  | COEVAL queryKind instanceKind
    (LBRACE instanceCoevalSection RBRACE)?
  #Instance_Coeval

  | DELTA mappingKind instanceKind
  #Instance_Delta

  | SIGMA mappingKind instanceKind
    (LBRACE instanceSigmaSection RBRACE)?
  #Instance_Sigma

  | COPRODUCT_SIGMA (mappingKind instanceKind)+ COLON schemaKind
    (LBRACE instanceCoprodSigmaSection RBRACE)?
  #Instance_CoSigma

  | COPRODUCT instanceKind (PLUS instanceKind)* COLON schemaKind
    (LBRACE instanceCoprodSection RBRACE)?
  #Instance_Coprod

  | COPRODUCT_UNRESTRICTED instanceId (PLUS instanceId)* COLON schemaKind
    (LBRACE instanceCoprodUnrestrictSection RBRACE)?
  #Instance_CoprodUn

  | COEQUALIZE transformKind transformKind
    (LBRACE instanceCoequalizeSection RBRACE)?
  #Instance_CoEqual

  | COLIMIT graphKind schemaKind
    (LBRACE instanceColimitSection RBRACE)?
  #Instance_CoLimit

  | IMPORT_JDBC jdbcClass jdbcUri COLON schemaKind
    (LBRACE instanceImportJdbcSection RBRACE)?
  #Instance_ImportJdbc

  | QUOTIENT_JDBC (jdbcClass (jdbcUri)?)? instanceKind
    (LBRACE instanceQuotientJdbcSection RBRACE)?
  #Instance_QuotientJdbc

  | QUOTIENT_CSV schemaDef
    (LBRACE instanceQuotientCsvSection RBRACE)?
  #Instance_QuotientCsv

  | IMPORT_JDBC_ALL (jdbcClass (jdbcUri)?)?
    (LBRACE instanceImportJdbcAllSection RBRACE)?
  #Instance_ImportJdbcAll

  | IMPORT_CSV instanceFile COLON schemaId
    (LBRACE instanceImportCsvSection RBRACE)?
  #Instance_ImportCsv

  | LITERAL COLON schemaKind
    (LBRACE instanceLiteralSection RBRACE)?
  #Instance_Literal

  | QUOTIENT instanceId
    (LBRACE instanceQuotientSection RBRACE)?
  #Instance_Quotient

  | CHASE constraintKind+ instanceKind INTEGER
  #Instance_Chase

  | RANDOM COLON schemaId
    (LBRACE instanceRandomSection RBRACE)?
  #Instance_Random
  ;

instanceKind: instanceId | instanceDef | (LPAREN instanceKind RPAREN);

instanceImportJdbcAllSection : allOptions ;

instanceColimitSection
  : NODES (instanceId RARROW instanceKind)+
    EDGES (schemaArrowId RARROW transformKind)+
    allOptions
  ;

instanceLiteralSection
  : (IMPORTS instanceId*)?
    (GENERATORS (instanceGen+ COLON schemaEntityId)+)?
    (EQUATIONS instanceEquation*)?
    (MULTI_EQUATIONS instanceMultiEquation*)?
    allOptions
  ;

instanceImportJdbcSection
  : ((schemaEntityId | schemaAttributeId | schemaForeignId | typesideTypeId)
      RARROW
      instanceSql)+
    allOptions
  ;

jdbcClass : STRING ;
jdbcUri : STRING ;
instanceSql : STRING | MULTI_STRING ;

instanceQuotientCsvSection : instanceFile+ ;
instanceFile : STRING ;

instanceGen
  : symbol
  | instanceLiteralValue
  ;

instanceEquation : instancePath EQUAL (instanceLiteral | instancePath) ;

instanceMultiEquation
  : instanceEquationId RARROW
    LBRACE instanceMultiBind (COMMA instanceMultiBind)* RBRACE
  ;

instanceEquationId : symbol ;

instanceMultiBind
  : instancePath (instanceSymbol | instanceLiteral) ;

instanceSymbol : symbol ;

instanceLiteral :  instanceLiteralValue (AT instanceSymbol)? ;

instanceLiteralValue
  : truthy
  | INTEGER
  | NUMBER
  | STRING
  ;

instancePath
  : instanceArrowId
  | instanceLiteralValue
  | instancePath DOT instanceArrowId
  | instanceArrowId LPAREN instancePath RPAREN
  ;

// identity arrows are indicated with entity-names.
instanceArrowId : schemaEntityId | schemaForeignId;

instanceQuotientJdbcSection
  : instanceSql+
    allOptions
  ;

instanceQuotientSection
  : EQUATIONS (instancePath EQUAL instancePath)*
    allOptions
  ;

instanceRandomSection
  : GENERATORS (schemaEntityId RARROW INTEGER)*
  | OPTIONS (RANDOM_SEED EQUAL INTEGER)
  ;

instanceEvalSection : allOptions ;
instanceCoevalSection : allOptions  ;
instanceSigmaSection : allOptions ;
instanceCoprodSection : allOptions ;
instanceCoprodSigmaSection : allOptions ;
instanceCoprodUnrestrictSection : allOptions ;
instanceCoequalizeSection : allOptions ;

instanceImportCsvSection
  : (schemaEntityId RARROW instanceCsvId)*
    allOptions
  ;

instanceCsvId : symbol ;
