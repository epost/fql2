parser grammar AqlSchemaColimit;
options { tokenVocab=AqlLexerRules; }

schemaColimitId: symbol ;

schemaColimitKindAssignment: SCHEMA_COLIMIT schemaColimitId EQUAL schemaColimitDef ;

schemaColimitDef
  : QUOTIENT schemaId (PLUS schemaId)* COLON typesideId
      (LBRACE schemaColimitQuotientSection RBRACE)?  #SchemaColimit_Quotient
  | COPRODUCT schemaId (PLUS schemaId)* COLON typesideId
                                                     #SchemaColimit_Coproduct
  | MODIFY schemaColimitId
      (LBRACE schemaColimitModifySection RBRACE)?    #SchemaColimit_Modify
  | WRAP schemaColimitId mappingId mappingId         #SchemaColimit_Wrap
  ;

schemaColimitKind: schemaColimitId | LPAREN schemaColimitDef RPAREN;

schemaColimitQuotientSection
  : (ENTITY_EQUATIONS (scEntityPath EQUAL scEntityPath)*)?
    (PATH_EQUATIONS (scFkPath EQUAL scFkPath)*)?
    (OBSERVATION_EQUATIONS scObsEquation* )?
  ;

scObsEquation
  : FORALL scGen (COMMA scGen)* DOT scEntityPath EQUAL scEntityPath
  ;

scGen : symbol ;

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
  : (RENAME ENTITIES (scEntityPath RARROW scEntityPath)*)?
    (RENAME FOREIGN_KEYS (scFkPath RARROW scFkPath)*)?
    (RENAME ATTRIBUTES (scAttrPath RARROW scAttrPath)*)?
    (REMOVE FOREIGN_KEYS (scFkPath RARROW scFkPath)*)?
    (REMOVE ATTRIBUTES (scAttrPath RARROW scAttrPath)*)?
  ;
