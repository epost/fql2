parser grammar AqlPragma;
options { tokenVocab=AqlLexerRules; }

pragmaId: IDENTIFIER;
pragmaKindAssignment: PRAGMA pragmaId Equal pragmaDef ;
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
pragmaKind: pragmaId | LParen pragmaDef RParen;

pragmaCmdLineSection: LBrace
  STRING
  (OPTIONS (timeoutOption|alwaysReloadOption)*)?
  RBrace  ;

pragmaExecJsSection: LBrace
  STRING
  (OPTIONS (timeoutOption|alwaysReloadOption)*)?
  RBrace  ;

pragmaExecJdbcSection: LBrace
  STRING
  (OPTIONS (timeoutOption|alwaysReloadOption)*)?
  RBrace  ;

pragmaExportCsvSection: LBrace
  STRING
  (OPTIONS (timeoutOption
    | alwaysReloadOption
    | csvOptions | idColumnNameOption
    | startIdsAtOption)*)?
  RBrace  ;

pragmaExportJdbcSection: LBrace
  STRING
  (OPTIONS (timeoutOption
    | alwaysReloadOption
    | idColumnNameOption
    | varcharLengthOption)*)?
  RBrace  ;

pragmaFile: STRING;
pragmaJdbcClass: STRING;
pragmaJdbcUri: STRING;
pragmaPrefix: STRING;
pragmaPrefixSrc: STRING;
pragmaPrefixDst: STRING;
