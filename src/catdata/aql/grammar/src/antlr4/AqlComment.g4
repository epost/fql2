parser grammar AqlComment;
options { tokenVocab=AqlLexerRules; }

htmlCommentDeclaration: HTML;
mdCommentDeclaration: MARKDOWN;
