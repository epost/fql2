parser grammar AqlComment;
options { tokenVocab=AqlLexerRules; }

htmlCommentDeclaration: HTML HTML_CHAR* HTML_END;
mdCommentDeclaration: MARKDOWN MD_CHAR* MD_END;
