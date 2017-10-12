parser grammar AqlSchemaColimit;
options { tokenVocab=AqlLexerRules; }

schemaColimitId: IDENTIFIER;
schemaColimitKindAssignment: SCHEMA_COLIMIT schemaColimitId Equal schemaColimitDef ;
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
schemaColimitKind: schemaColimitId | LParen schemaColimitDef RParen;

schemaColimitQuotientSection: LBrace
  (ENTITY_EQUATIONS (scEntityPath Equal scEntityPath)*)?
  (PATH_EQUATIONS (scFkPath Equal scFkPath)*)?
  (OBSERVATION_EQUATIONS scObsEquation )?
  RBrace  ;

scObsEquation:
    FORALL scGen (COMMA scGen)* Dot scEntityPath Equal scEntityPath
  | ;

scGen: STRING;
scEntityPath: schemaId Dot schemaTermId;
scFkPath: schemaId '_' schemaArrowId;

schemaColimitModifySection: LBrace
  (RENAME ENTITIES (scEntityPath Equal scEntityPath)*)?
  (RENAME FOREIGN_KEYS (scFkPath Equal scFkPath)*)?
  (RENAME ATTRIBUTES scObsEquation )?
  (REMOVE FOREIGN_KEYS scObsEquation )?
  (REMOVE ATTRIBUTES scObsEquation )?
  RBrace  ;
