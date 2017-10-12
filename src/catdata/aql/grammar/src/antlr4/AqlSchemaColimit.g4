parser grammar AqlSchemaColimit;
options { tokenVocab=AqlLexerRules; }

schemaColimitId: IDENTIFIER;
schemaColimitKindAssignment: 'schema_colimit' schemaColimitId Equal schemaColimitDef ;
schemaColimitDef:
      QUOTIENT schemaId ('+' schemaId)* COLON typesideId
        schemaColimitQuotientSection        #SchemaColimit_Quotient
    | COPRODUCT schemaId ('+' schemaId)* COLON typesideId
                                            #SchemaColimit_Coproduct
    | 'modify' schemaColimitId
        schemaColimitModifySection          #SchemaColimit_Modify
    | 'wrap' schemaColimitId mappingId mappingId
                                            #SchemaColimit_Wrap
    ;
schemaColimitKind: schemaColimitId | LParen schemaColimitDef RParen;

schemaColimitQuotientSection: LBrace
  ('entity_equations' (scEntityPath Equal scEntityPath)*)?
  ('path_equations' (scFkPath Equal scFkPath)*)?
  ('observation_equations' scObsEquation )?
  RBrace  ;

scObsEquation:
    FORALL scGen (COMMA scGen)* Dot scEntityPath Equal scEntityPath
  | ;

scGen: STRING;
scEntityPath: schemaId Dot schemaTermId;
scFkPath: schemaId '_' schemaArrowId;

schemaColimitModifySection: LBrace
  ('rename' ENTITIES (scEntityPath Equal scEntityPath)*)?
  ('rename' FOREIGN_KEYS (scFkPath Equal scFkPath)*)?
  ('rename' ATTRIBUTES scObsEquation )?
  ('remove' FOREIGN_KEYS scObsEquation )?
  ('remove' ATTRIBUTES scObsEquation )?
  RBrace  ;
