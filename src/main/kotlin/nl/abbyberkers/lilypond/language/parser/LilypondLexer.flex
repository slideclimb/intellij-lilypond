package nl.abbyberkers.lilypond.language.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static nl.abbyberkers.lilypond.language.psi.LilypondTypes.*;

%%

%{
  public LilypondLexer() {
    this((java.io.Reader)null);
  }

  private Deque<Integer> stack = new ArrayDeque<>();

  public void yypushState(int newState) {
    stack.push(yystate());
    yybegin(newState);
  }

  public void yypopState() {
    yybegin(stack.pop());
  }

  private int schemeBracketsOpen = 0;

  private int lastSchemeBracketsOpen = 0;

%}

%public
%class LilypondLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

IDENTIFIER=[a-zA-Z_]+
DIGIT=[0-9]
STRING_LITERAL=\"[^\"]*\"
WHITESPACE=[ \t\n\x0B\f\r]+
BLOCK_COMMENT=%\{[^(%})]*%}
LINE_COMMENT=%[^{].*

%xstates SCHEME

SCM_IDENTIFIER=[a-zA-Z\-:]+
SCM_BLOCK_COMMENT=#\!\{[^(\!#)]\!#
SCM_LINE_COMMENT=;.*


%%
<YYINITIAL, SCHEME> {
  "|"                    { return BAR; }
  "/"                    { return SLASH; }
  ":"                    { return COLON; }
  "_"                    { return UNDERSCORE; }
  "+"                    { return PLUS; }
  "-"                    { return MINUS; }
  "*"                    { return STAR; }
  "^"                    { return HAT; }
  "="                    { return EQUALS; }
  "."                    { return DOT; }
  ","                    { return COMMA; }
  "'"                    { return SINGLE_QUOTE; }
  "~"                    { return TILDE; }
  "{"                    { return LEFT_BRACE; }
  "}"                    { return RIGHT_BRACE; }
  "["                    { return LEFT_BRACKET; }
  "]"                    { return RIGHT_BRACKET; }
  "?"                    { return QUESTION_MARK; }
  "!"                    { return EXCLAMATION_MARK; }
}

<YYINITIAL> {
  {WHITE_SPACE}          { return WHITE_SPACE; }

  "\\"                   { return BACKSLASH; }
  "("                    { return LEFT_PAREN; }
  ")"                    { return RIGHT_PAREN; }
  "<<"                   { return MULTI_VOICE_START; }
  ">>"                   { return MULTI_VOICE_END; }
  "<"                    { return CHORD_START; }
  ">"                    { return CHORD_END; }
  "#}"                   { yypushState(SCHEME); return SCM_CONTINUE; }
  "#"                    { yypushState(SCHEME); return SCM_START; }
  "$"                    { yypushState(SCHEME); return SCM_START_DOLLAR; }

  {IDENTIFIER}           { return IDENTIFIER; }
  {DIGIT}                { return DIGIT; }
  {STRING_LITERAL}       { return STRING_LITERAL; }
  {WHITESPACE}           { return WHITESPACE; }
  {BLOCK_COMMENT}        { return BLOCK_COMMENT; }
  {LINE_COMMENT}         { return LINE_COMMENT; }
}

<SCHEME> {
  {WHITE_SPACE}          {
          if (schemeBracketsOpen == 0) yypopState();
          return WHITE_SPACE;
  }
  "("                    { schemeBracketsOpen++; return LEFT_PAREN; }
  ")"                    { schemeBracketsOpen--; if (schemeBracketsOpen == lastSchemeBracketsOpen) yypopState(); return RIGHT_PAREN; }
  "#{"                   { yypopState(); lastSchemeBracketsOpen = schemeBracketsOpen; return SCM_LILY_START; }
  "#"                    { return SCM_HASH; }
  {SCM_IDENTIFIER}       { return SCM_IDENTIFIER; }
  {SCM_BLOCK_COMMENT}        { return SCM_BLOCK_COMMENT; }
  {SCM_LINE_COMMENT}         { return SCM_LINE_COMMENT; }
}

[^] { return BAD_CHARACTER; }
