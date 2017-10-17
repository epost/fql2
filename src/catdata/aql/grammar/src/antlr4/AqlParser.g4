/**
    A grammar for the  Algebreic Query Language (AQL)
    See http://categoricaldata.net/

    This grammar follows the grammar as outlined
    in 'All Syntax.aql'.
 */
parser grammar AqlParser;
options { tokenVocab=AqlLexerRules; }

import
  AqlComment,
  AqlOptions,
  AqlTypeside,
  AqlSchema,
  AqlInstance,
  AqlMapping,
  AqlTransform,
  AqlQuery,
  AqlGraph,
  AqlPragma,
  AqlSchemaColimit,
  AqlConstraint;

file : program  EOF ;

program
  : optionsDeclarationSection?
    (commentDeclarationSection | kindDeclaration)* 
  ;

optionsDeclarationSection
  : OPTIONS optionsDeclaration* ;

commentDeclarationSection
  : htmlCommentDeclaration     #Comment_HTML
  | mdCommentDeclaration       #Comment_MD;

kindDeclaration
  : typesideKindAssignment
  | schemaKindAssignment
  | instanceKindAssignment
  | mappingKindAssignment
  | transformKindAssignment
  | queryKindAssignment
  | graphKindAssignment
  | pragmaKindAssignment
  | schemaColimitKindAssignment
  | constraintKindAssignment
  ;


path : LOWER_ID (DOT LOWER_ID)* ;

value
  : STRING
  | NUMBER
  | LOWER_ID
  ;
