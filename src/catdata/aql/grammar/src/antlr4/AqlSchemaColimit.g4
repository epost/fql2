parser grammar AqlSchemaColimit;
options { tokenVocab=AqlLexerRules; }

schemaColimitId: IDENTIFIER;
schemaColimitKindAssignment: SCHEMA_COLIMIT schemaColimitId EQUAL schemaColimitDef ;
schemaColimitDef:
      QUOTIENT schemaId (PLUS schemaId)* COLON typesideId
        schemaColimitQuotientSection        #SchemaColimit_Quotient
    | COPRODUCT schemaId (PLUS schemaId)* COLON typesideId
                                            #SchemaColimit_Coproduct
    | MODIFY schemaColimitId
        schemaColimitModifySection          #SchemaColimit_Modify
    | WRAP schemaColimitId mappingId mappingId
                                            #SchemaColimit_Wrap
    ;
schemaColimitKind: schemaColimitId | LPAREN schemaColimitDef RPAREN;

schemaColimitQuotientSection: LBRACE
  (ENTITY_EQUATIONS (scEntityPath EQUAL scEntityPath)*)?
  (PATH_EQUATIONS (scFkPath EQUAL scFkPath)*)?
  (OBSERVATION_EQUATIONS scObsEquation )?
  RBRACE  ;

scObsEquation:
    FORALL scGen (COMMA scGen)* DOT scEntityPath EQUAL scEntityPath
  | ;

scGen: STRING;
scEntityPath: schemaId DOT schemaTermId;
scFkPath: schemaId UNDERSCORE schemaArrowId;

schemaColimitModifySection: LBRACE
  (RENAME ENTITIES (scEntityPath EQUAL scEntityPath)*)?
  (RENAME FOREIGN_KEYS (scFkPath EQUAL scFkPath)*)?
  (RENAME ATTRIBUTES scObsEquation )?
  (REMOVE FOREIGN_KEYS scObsEquation )?
  (REMOVE ATTRIBUTES scObsEquation )?
  RBRACE  ;
