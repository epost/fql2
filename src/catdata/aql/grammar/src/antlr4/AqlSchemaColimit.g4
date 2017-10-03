schemaColimitId: IDENTIFIER;
schemaColimitKindAssignment: 'schema_colimit' schemaColimitId '=' schemaColimitDef ;
schemaColimitDef:
      'quotient' schemaId ('+' schemaId)* ':' typesideId
        schemaColimitQuotientSection        #SchemaColimit_Quotient
    | 'coproduct' schemaId ('+' schemaId)* ':' typesideId
                                            #SchemaColimit_Coproduct
    | 'modify' schemaColimitId
        schemaColimitModifySection          #SchemaColimit_Modify
    | 'wrap' schemaColimitId mappingId mappingId
                                            #SchemaColimit_Wrap
    ;
schemaColimitKind: schemaColimitId | '(' schemaColimitDef ')';

schemaColimitQuotientSection: '{'
  ('entity_equations' (scEntityPath '=' scEntityPath)*)?
  ('path_equations' (scFkPath '=' scFkPath)*)?
  ('observation_equations' scObsEquation ))
  '}'  ;

scObsEquation:
    'forall' scGen (',' scGen)* '.' scEntityPath '=' scEntityPath
  | ;

scEntityPath: schemaId '.' schemaTermId;
scFkPath: schemaId '_' schemaArrowId;

schemaColimitModifySection: '{'
  ('rename' 'entities' (scEntityPath '=' scEntityPath)*)?
  ('rename' 'foreign_keys' (scFkPath '=' scFkPath)*)?
  ('rename' 'attributes' scObsEquation ))
  ('remove' 'foreign_keys' scObsEquation ))
  ('remove' 'attributes' scObsEquation ))
  '}'  ;
