parser grammar AqlGraph;
options { tokenVocab=AqlLexerRules; }

graphId : symbol ;

graphKindAssignment : GRAPH graphId EQUAL graphDef ;

graphDef
  : LITERAL
    (LBRACE graphLiteralSection RBRACE)?
  #GraphExp_Literal
  ;

graphKind : graphId | LPAREN graphDef RPAREN ;

graphLiteralSection
  : (IMPORTS graphId*)?
    (NODES graphNodeId*)?
    (EDGES (graphEdgeId+ COLON graphNodeId RARROW graphNodeId)*)?
  ;

graphNodeId : symbol ;
graphEdgeId : symbol ;
