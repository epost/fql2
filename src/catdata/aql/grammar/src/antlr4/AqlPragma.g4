parser grammar AqlPragma;
options { tokenVocab=AqlLexerRules; }

pragmaId: IDENTIFIER;
pragmaKindAssignment: PRAGMA pragmaId EQUAL pragmaDef ;
pragmaDef:
      EXEC_CMDLINE pragmaCmdLineSection    #Pragma_CmdLine
    | EXEC_JS pragmaExecJsSection          #Pragma_ExecJs
    | EXEC_JDBC pragmaJdbcClass pragmaJdbcUri
              pragmaExecJdbcSection          #Pragma_ExecJdbc
    | CHECK COLON constraintId instanceId    #Pragma_Check
    | ASSERT_CONSISTENT instanceId         #Pragma_AssertConsistent
    | EXPORT_CSV_INSTANCE instanceId pragmaFile
              pragmaExportCsvSection          #Pragma_ExportCsvInstance
    | EXPORT_CSV_TRANSFORM transformId pragmaFile
              pragmaExportCsvSection          #Pragma_ExportCsvTransform
    | EXPORT_JDBC_INSTANCE instanceId
        (pragmaJdbcClass (pragmaJdbcUri
          pragmaPrefixDst?)?)?
          pragmaExportJdbcSection  #Pragma_ExportJdbcInstance
    | EXPORT_JDBC_QUERY queryId
        (pragmaJdbcClass (pragmaJdbcUri
          (pragmaPrefixSrc pragmaPrefixDst?)?)?)?
          pragmaExportJdbcSection  #Pragma_ExportJdbcQuery
    | EXPORT_JDBC_TRANSFORM transformId
        (pragmaJdbcClass (pragmaJdbcUri pragmaPrefix?)?)?
          pragmaExportJdbcSection  #Pragma_ExportJdbcTransform
    ;
pragmaKind: pragmaId | LPAREN pragmaDef RPAREN;

pragmaCmdLineSection: LBRACE
  STRING
  (OPTIONS (timeoutOption|alwaysReloadOption)*)?
  RBRACE  ;

pragmaExecJsSection: LBRACE
  STRING
  (OPTIONS (timeoutOption|alwaysReloadOption)*)?
  RBRACE  ;

pragmaExecJdbcSection: LBRACE
  STRING
  (OPTIONS (timeoutOption|alwaysReloadOption)*)?
  RBRACE  ;

pragmaExportCsvSection: LBRACE
  STRING
  (OPTIONS (timeoutOption
    | alwaysReloadOption
    | csvOptions | idColumnNameOption
    | startIdsAtOption)*)?
  RBRACE  ;

pragmaExportJdbcSection: LBRACE
  STRING
  (OPTIONS (timeoutOption
    | alwaysReloadOption
    | idColumnNameOption
    | varcharLengthOption)*)?
  RBRACE  ;

pragmaFile: STRING;
pragmaJdbcClass: STRING;
pragmaJdbcUri: STRING;
pragmaPrefix: STRING;
pragmaPrefixSrc: STRING;
pragmaPrefixDst: STRING;
