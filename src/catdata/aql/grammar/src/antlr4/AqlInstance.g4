parser grammar AqlInstance;
options { tokenVocab=AqlLexerRules; }

instanceId: IDENTIFIER;
instanceKindAssignment: 'instance' instanceId '=' instanceDef ;
instanceDef:
    'empty' ':' schemaKind
  | 'src' transformKind
  | 'dst' transformKind
  | 'distinct' instanceKind
  | 'eval' queryKind instanceKind
          ('options' evalOptions*)?
  | 'coeval' queryKind instanceKind
          ('options' (timeoutOption | proverOptions)*)?
  | 'delta' mappingKind instanceKind
  | 'sigma' mappingKind instanceKind
  | 'coproduct_sigma' (mappingKind instanceKind)+ ':' schemaKind
  | 'coproduct' instanceKind ('+' instanceKind)* ':' schemaKind
  | 'coproduct_unrestricted' instanceId ('+' instanceId)* ':' schemaKind
  | 'coequalize' transformKind transformKind
  | 'colimit' graphKind schemaKind '{'
      'nodes' (instanceId '->' instanceDef)+
      'edges' (schemaArrowId '->' transformKind)+
      ('options' (timeoutOption | 'static_typing' '=' truthy)*)?
      '}'
  | 'import_jdbc' jdbcClass jdbcUri ':' schemaDef '{' instanceImportJdbc '}'
  | 'quotient_jdbc' jdbcClass jdbcUri  schemaDef '{' instanceSql+ '}'
  | 'quotient_csv' schemaDef '{' instanceFile+ '}'
  | 'import_jdbc_all' jdbcClass jdbcUri
        ('options' (timeoutOption | proverOptions
                  | alwaysReloadOption
                  | requireConsistencyOption
                  | schemaOnlyOption)*)?
  | 'import_csv' ':' schemaDef '{' instanceEntityFile+ '}'
        ('options' (timeoutOption | proverOptions
                  | alwaysReloadOption
                  | csvOptions
                  | idColumnNameOption
                  | requireConsistencyOption)*)?
  | 'literal' ':' schemaKind '{' instanceLiteralExpr '}'
  | 'quotient' instanceKind '{' instanceQuotientExpr '}'
  | 'chase' constraintKind* instanceKind INTEGER?
  | 'random' ':' schemaId '{' instanceRandomExpr '}'
    ;
instanceKind: instanceId | '(' instanceDef ')';

instanceLiteralExpr:
  ('imports' instanceId*)?
  'generators' (instanceGen ':' schemaEntityId)+ 
  ('equations' instanceEquation*)?
  ('multi_equations' instanceMultiEquation*)?
  ('options' (timeoutOption | proverOptions
            | requireConsistencyOption
            | interpretAsAlgebraOption)*)?
  ;

instanceImportJdbc:
  (schemaEntityId | schemaAttributeId | schemaForeignId | typesideTypeId)
  instanceSql
  ('options' (
      timeoutOption | proverOptions
    | alwaysReloadOption | requireConsistencyOption)*)?
  ;

jdbcClass: STRING;
jdbcUri: STRING;
instanceSql: STRING;
instanceFile: STRING;
instanceEntityFile: schemaEntityId '->' instanceFile;

instanceGen: IDENTIFIER;

instanceEquation: instancePath '=' instancePath;

instanceMultiEquation:
  instanceEquationId '->' '{' instanceMultiValue (',' instanceMultiValue)* '}';

instanceEquationId: STRING;

instanceMultiValue:
  instancePath STRING;

instancePath:
  instanceArrowId
  | instancePath '.' instanceArrowId
  | instanceArrowId '(' instancePath ')'
  ;

// identity arrows are indicated with entity-names.
instanceArrowId: schemaEntityId | schemaForeignId;

instanceQuotientExpr:
    'equations' (schemaEntityId '=' schemaEntityId)* ;

instanceRandomExpr:
  'generators' (schemaEntityId '->' INTEGER)*
  | 'options' ('random_seed' '=' INTEGER)
  ;
