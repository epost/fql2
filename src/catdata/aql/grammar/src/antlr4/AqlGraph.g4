parser grammar AqlGraph;
options { tokenVocab=AqlLexerRules; }

graphId: IDENTIFIER;

graphKindAssignment: 'graph' graphId '=' graphDef ;
graphDef:
      'literal' '{' graphLiteralExpr '}'      #GraphExp_Literal
    ;
graphKind: graphId | '(' graphDef ')';

graphLiteralExpr:
  ('imports' graphId*)?
  'nodes' graphNodeId*
  'edges' (graphEdgeId ':' graphNodeId '->' graphNodeId)*
  ;

graphNodeId: IDENTIFIER;
graphEdgeId: IDENTIFIER;
