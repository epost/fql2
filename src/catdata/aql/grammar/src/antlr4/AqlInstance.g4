parser grammar AqlInstance;
options { tokenVocab=AqlLexerRules; }

instanceId : symbol ;

instanceKindAssignment : INSTANCE instanceId EQUAL instanceDef ;

instanceDef
  : EMPTY COLON schemaKind
  | SRC transformKind
  | DST transformKind
  | DISTINCT instanceKind
  | EVAL queryKind instanceKind
      (LBRACE instanceEvalSection RBRACE)?
  | COEVAL queryKind instanceKind
      (LBRACE instanceCoevalSection RBRACE)?
  | DELTA mappingKind instanceKind
  | SIGMA mappingKind instanceKind
      (LBRACE instanceSigmaSection RBRACE)?
  | COPRODUCT_SIGMA (mappingKind instanceKind)+ COLON schemaKind
      (LBRACE instanceCoprodSigmaSection RBRACE)?
  | COPRODUCT instanceKind (PLUS instanceKind)* COLON schemaKind
      (LBRACE instanceCoprodSection RBRACE)?
  | COPRODUCT_UNRESTRICTED instanceId (PLUS instanceId)* COLON schemaKind
      (LBRACE instanceCoprodUnrestrictSection RBRACE)?
  | COEQUALIZE transformKind transformKind
      (LBRACE instanceCoequalizeSection RBRACE)?
  | COLIMIT graphKind schemaKind
      (LBRACE instanceColimitSection RBRACE)?
  | IMPORT_JDBC jdbcClass jdbcUri COLON schemaKind
      (LBRACE instanceImportJdbcSection RBRACE)?
  | QUOTIENT_JDBC (jdbcClass (jdbcUri)?)? instanceKind
      (LBRACE instanceQuotientJdbcSection RBRACE)?
  | QUOTIENT_CSV schemaDef
      (LBRACE instanceQuotientCsvSection RBRACE)?
  | IMPORT_JDBC_ALL (jdbcClass (jdbcUri)?)?
      (LBRACE instanceImportJdbcAllSection RBRACE)?
  | IMPORT_CSV instanceFile COLON schemaId
      (LBRACE instanceImportCsvSection RBRACE)?
  | LITERAL COLON schemaKind
      (LBRACE instanceLiteralSection RBRACE)?
  | QUOTIENT instanceId
      (LBRACE instanceQuotientSection RBRACE)?
  | CHASE constraintKind+ instanceKind INTEGER?
  | RANDOM COLON schemaId
      (LBRACE instanceRandomSection RBRACE)?
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
