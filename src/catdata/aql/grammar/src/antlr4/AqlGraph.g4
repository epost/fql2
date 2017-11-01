parser grammar AqlGraph;
options { tokenVocab=AqlLexerRules; }

graphId : (LOWER_ID | UPPER_ID) ;

graphKindAssignment : GRAPH graphId EQUAL graphDef ;
graphDef
  : LITERAL LBRACE graphLiteralExpr RBRACE      #GraphExp_Literal
  ;
graphKind : graphId | LPAREN graphDef RPAREN ;

graphLiteralExpr
  : (IMPORTS graphId*)?
    NODES graphNodeId*
    EDGES (graphEdgeId COLON graphNodeId RARROW graphNodeId)*
  ;

graphNodeId : (LOWER_ID | UPPER_ID) ;
graphEdgeId : (LOWER_ID | UPPER_ID) ;
