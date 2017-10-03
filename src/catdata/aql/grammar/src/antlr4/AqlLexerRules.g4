lexer grammar AqlLexerRules;

channels {
  WHITESPACE_CHANNEL,
  BLOCK_COMMMENT_CHANNEL,
  LINE_COMMENT_CHANNEL
}

/*
Island grammars

 'md' '{' '(*' '"' -> pushMode(MARKDOWN)
 '"' '*)' '}' -> popMode;
*/
HtmlCommentDeclaration:
  'html' '{' '(*' STRING '*)' '}';

MdCommentDeclaration:
  'md' '{' '(*' STRING '*)' '}';

WS: [ \t\r\n]+ -> channel(HIDDEN) ;

BLOCK_COMMMENT:
    '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT:
    '//' .*? '\r'? '\n' -> channel(HIDDEN) ;

STRING :  '"' (ESC | CHAR)* '"' ;
CHAR : '"' (ESC | ~["\\]) '"' ;

fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

NUMBER
    :   '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP            // 1e10 -3e4
    |   '-'? INT                // -3, 45
    ;
INTEGER: INT;
fragment INT :   '0' | [1-9] DIGIT* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ; // \- since - means "range" inside [...]

IDENTIFIER: ID_LETTER (ID_LETTER | DIGIT)+ ;
fragment ID_LETTER : 'a'..'z'|'A'..'Z'|'_'|'$' ;
fragment DIGIT : [0-9] ;
