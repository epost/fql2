parser grammar AqlComment;
options { tokenVocab=AqlLexerRules; }

htmlCommentDeclaration: HtmlCommentDeclaration;
mdCommentDeclaration: MdCommentDeclaration;
