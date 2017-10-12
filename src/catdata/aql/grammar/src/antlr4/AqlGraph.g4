parser grammar AqlGraph;
options { tokenVocab=AqlLexerRules; }

graphId: IDENTIFIER;

graphKindAssignment: GRAPH graphId Equal graphDef ;
graphDef:
      LITERAL LBrace graphLiteralExpr RBrace      #GraphExp_Literal
    ;
graphKind: graphId | LParen graphDef RParen;

graphLiteralExpr:
  (IMPORTS graphId*)?
  NODES graphNodeId*
  EDGES (graphEdgeId COLON graphNodeId RARROW graphNodeId)*
  ;

graphNodeId: IDENTIFIER;
graphEdgeId: IDENTIFIER;
