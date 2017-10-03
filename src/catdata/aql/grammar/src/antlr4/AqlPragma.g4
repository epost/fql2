parser grammar AqlPragma;
options { tokenVocab=AqlLexerRules; }

pragmaId: IDENTIFIER;
pragmaKindAssignment: 'pragma' pragmaId '=' pragmaDef ;
pragmaDef:
      'exec_cmdline' pragmaCmdLineSection    #Pragma_CmdLine
    | 'exec_js' pragmaExecJsSection          #Pragma_ExecJs
    | 'exec_jdbc' pragmaJdbcClass pragmaJdbcUri
              pragmaExecJdbcSection          #Pragma_ExecJdbc
    | 'check' ':' constraintId instanceId    #Pragma_Check
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
pragmaKind: pragmaId | '(' pragmaDef ')';

pragmaCmdLineSection: '{'
  STRING
  ('options' (timeoutOption|alwaysReloadOption)*)?
  '}'  ;

pragmaExecJsSection: '{'
  STRING
  ('options' (timeoutOption|alwaysReloadOption)*)?
  '}'  ;

pragmaExecJdbcSection: '{'
  STRING
  ('options' (timeoutOption|alwaysReloadOption)*)?
  '}'  ;

pragmaExportCsvSection: '{'
  STRING
  ('options' (timeoutOption
    | alwaysReloadOption
    | csvOptions | idColumnNameOption
    | startIdsAtOption)*)?
  '}'  ;

pragmaExportJdbcSection: '{'
  STRING
  ('options' (timeoutOption
    | alwaysReloadOption
    | idColumnNameOption
    | varcharLengthOption)*)?
  '}'  ;

pragmaFile: STRING;
pragmaJdbcClass: STRING;
pragmaJdbcUri: STRING;
pragmaPrefix: STRING;
pragmaPrefixSrc: STRING;
pragmaPrefixDst: STRING;
