parser grammar AqlSchemaColimit;
options { tokenVocab=AqlLexerRules; }

schemaColimitId: LOWER_ID | UPPER_ID ;

schemaColimitKindAssignment: SCHEMA_COLIMIT schemaColimitId EQUAL schemaColimitDef ;

schemaColimitDef
  : QUOTIENT schemaId (PLUS schemaId)* COLON typesideId
        schemaColimitQuotientSection        #SchemaColimit_Quotient
  | COPRODUCT schemaId (PLUS schemaId)* COLON typesideId
                                            #SchemaColimit_Coproduct
  | MODIFY schemaColimitId schemaColimitModifySection   #SchemaColimit_Modify
  | WRAP schemaColimitId mappingId mappingId #SchemaColimit_Wrap
  ;

schemaColimitKind: schemaColimitId | LPAREN schemaColimitDef RPAREN;

schemaColimitQuotientSection: LBRACE
  (ENTITY_EQUATIONS (scEntityPath EQUAL scEntityPath)*)?
  (PATH_EQUATIONS (scFkPath EQUAL scFkPath)*)?
  (OBSERVATION_EQUATIONS scObsEquation* )?
  RBRACE  ;

scObsEquation
  : FORALL scGen (COMMA scGen)* DOT scEntityPath EQUAL scEntityPath
  ;

scGen : STRING;
scEntityPath
  : schemaId DOT schemaTermId
  | schemaTermId
  ;

scFkPath
: schemaId DOT schemaTermId
| schemaTermId
;

scAttrPath
: schemaId DOT schemaTermId
| schemaTermId
;

schemaColimitModifySection
  : LBRACE
  (RENAME ENTITIES (scEntityPath RARROW scEntityPath)*)?
  (RENAME FOREIGN_KEYS (scFkPath RARROW scFkPath)*)?
  (RENAME ATTRIBUTES (scAttrPath RARROW scAttrPath)*)?
  (REMOVE FOREIGN_KEYS (scFkPath RARROW scFkPath)*)?
  (REMOVE ATTRIBUTES (scAttrPath RARROW scAttrPath)*)?
  RBRACE  ;
