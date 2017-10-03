
parser grammar AqlCommentTest;
options { tokenVocab=AqlLexerRules; }
import AqlComment;

program:
  // optionsDeclarationSection
  commentDeclaration+;


commentDeclaration:
    htmlCommentDeclaration     #Comment_HTML
  | mdCommentDeclaration       #Comment_MD;
