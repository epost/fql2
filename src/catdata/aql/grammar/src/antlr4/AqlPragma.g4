parser grammar AqlPragma;
options { tokenVocab=AqlLexerRules; }

pragmaId: IDENTIFIER;
pragmaKindAssignment: 'pragma' pragmaId Equal pragmaDef ;
pragmaDef:
      'exec_cmdline' pragmaCmdLineSection    #Pragma_CmdLine
    | 'exec_js' pragmaExecJsSection          #Pragma_ExecJs
    | 'exec_jdbc' pragmaJdbcClass pragmaJdbcUri
              pragmaExecJdbcSection          #Pragma_ExecJdbc
    | 'check' COLON constraintId instanceId    #Pragma_Check
    | 'assert_consistent' instanceId         #Pragma_AssertConsistent
    | 'export_csv_instance' instanceId pragmaFile
              pragmaExportCsvSection          #Pragma_ExportCsvInstance
    | 'export_csv_transform' transformId pragmaFile
              pragmaExportCsvSection          #Pragma_ExportCsvTransform
    | 'export_jdbc_instance' instanceId
        (pragmaJdbcClass (pragmaJdbcUri
          pragmaPrefixDst?)?)?
          pragmaExportJdbcSection  #Pragma_ExportJdbcInstance
    | 'export_jdbc_query' queryId
        (pragmaJdbcClass (pragmaJdbcUri
          (pragmaPrefixSrc pragmaPrefixDst?)?)?)?
          pragmaExportJdbcSection  #Pragma_ExportJdbcQuery
    | 'export_jdbc_transform' transformId
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
