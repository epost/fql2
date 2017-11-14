parser grammar AqlPragma;
options { tokenVocab=AqlLexerRules; }

pragmaId : symbol ;

pragmaKindAssignment : PRAGMA pragmaId EQUAL pragmaDef ;

pragmaDef
  : EXEC_CMDLINE
      (LBRACE pragmaCmdLineSection RBRACE)?
    #Pragma_CmdLine

  | EXEC_JS
      (LBRACE pragmaExecJsSection RBRACE)?
    #Pragma_ExecJs

  | EXEC_JDBC pragmaJdbcClass pragmaJdbcUri
      (LBRACE pragmaExecJdbcSection RBRACE)?
    #Pragma_ExecJdbc

  | CHECK constraintId instanceId
    #Pragma_Check

  | ASSERT_CONSISTENT instanceId
    #Pragma_AssertConsistent

  | EXPORT_CSV_INSTANCE instanceId pragmaFile
      (LBRACE pragmaExportCsvSection RBRACE)?
    #Pragma_ExportCsvInstance

  | EXPORT_CSV_TRANSFORM transformId pragmaFile
      (LBRACE pragmaExportCsvSection RBRACE)?
    #Pragma_ExportCsvTransform

  | EXPORT_JDBC_INSTANCE instanceId
      (pragmaJdbcClass (pragmaJdbcUri pragmaPrefixDst?)?)?
      (LBRACE pragmaExportJdbcSection RBRACE)?
    #Pragma_ExportJdbcInstance

  | EXPORT_JDBC_QUERY queryId
      (pragmaJdbcClass (pragmaJdbcUri (pragmaPrefixSrc pragmaPrefixDst?)?)?)?
      (LBRACE pragmaExportJdbcSection RBRACE)?
    #Pragma_ExportJdbcQuery

  | EXPORT_JDBC_TRANSFORM transformId
      (pragmaJdbcClass (pragmaJdbcUri pragmaPrefix?)?)?
      (LBRACE pragmaExportJdbcSection RBRACE)?
      (LBRACE pragmaExportJdbcSection RBRACE)?
    #Pragma_ExportJdbcTransform

  | ADD_TO_CLASSPATH
      (LBRACE pragmaAddClasspathSection RBRACE)?
    #Pragma_AddToClasspath
  ;

pragmaKind: pragmaId | LPAREN pragmaDef RPAREN;

pragmaAddClasspathSection : STRING+ ;

pragmaCmdLineSection : STRING+ allOptions ;

pragmaExecJsSection : STRING+ allOptions ;

pragmaExecJdbcSection : (STRING | MULTI_STRING)+ allOptions ;

pragmaExportCsvSection : STRING* allOptions ;

pragmaExportJdbcSection : STRING* allOptions ;

pragmaFile : STRING ;
pragmaJdbcClass : STRING ;
pragmaJdbcUri : STRING ;
pragmaPrefix : STRING ;
pragmaPrefixSrc : STRING ;
pragmaPrefixDst : STRING ;
