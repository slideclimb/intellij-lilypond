package nl.abbyberkers.lilypond.language.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static nl.abbyberkers.lilypond.language.psi.LilypondTypes.*;

%%

%{
  public LilypondLexer() {
    this((java.io.Reader)null);
  }

  private Deque<Integer> stack = new ArrayDeque<>();

  private Deque<Integer> schemeBracketsOpenStack = new ArrayDeque<>();

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

WHITE_SPACE=\s+

SYMBOL=[^\s\\\{\}%\[\]$\(\)|!\"'=&<>,.#]+
DIGIT=[0-9]
WHITESPACE=[ \t\n\x0B\f\r]+
BLOCK_COMMENT=%\{(.|\n)*?%}
LINE_COMMENT=%[^\r\n]*

%xstates STRING SCHEME

SCM_IDENTIFIER=[a-zA-Z\-_:$[\xc0-\xf6\xf8-\xff]]+
SCM_BLOCK_COMMENT=#\!\{[^(\!#)]\!#
SCM_LINE_COMMENT=;.*


%%

<YYINITIAL> {
  "\""                   { yypushState(STRING); return QUOTE; }
//  "|"                    { return BAR; }
  "/"                    { return SLASH; }
  "\\"                   { return BACKSLASH; }
//  ":"                    { return COLON; }
//  "__"                   { return EXTENDER; }
//  "_"                    { return UNDERSCORE; }
//  "+"                    { return PLUS; }
//  "--"                   { return HYPHEN; }
  "-"                    { return MINUS; }
//  "*"                    { return STAR; }
//  "^"                    { return HAT; }
//  "="                    { return EQUALS; }
  "."                    { return DOT; }
//  ","                    { return COMMA; }
//  "'"                    { return SINGLE_QUOTE; }
//  "~"                    { return TILDE; }
//  "&"                    { return AMPERSAND; }
//  "{"                    { return LEFT_BRACE; }
//  "}"                    { return RIGHT_BRACE; }
//  "["                    { return LEFT_BRACKET; }
//  "]"                    { return RIGHT_BRACKET; }
//  "?"                    { return QUESTION_MARK; }
//  "!"                    { return EXCLAMATION_MARK; }
//  "("                    { return LEFT_PAREN; }
//  ")"                    { return RIGHT_PAREN; }
//  "<<"                   { return MULTI_VOICE_START; }
//  ">>"                   { return MULTI_VOICE_END; }
  // Tokenizer is greedy, so these should come after their double variant.
//  "<"                    { return SMALLER; }
//  ">"                    { return GREATER; }
  "#}"                   { yypushState(SCHEME); schemeBracketsOpenStack.pop(); return SCM_CONTINUE; }
  "#"                    { yypushState(SCHEME); schemeBracketsOpenStack.push(schemeBracketsOpen); return SCM_START; }
  "$"                    { yypushState(SCHEME); schemeBracketsOpenStack.push(schemeBracketsOpen); return SCM_START_DOLLAR; }

  {DIGIT}                { return DIGIT; }
  {WHITESPACE}           { return WHITE_SPACE; }
  {BLOCK_COMMENT}        { return BLOCK_COMMENT; }
  {LINE_COMMENT}         { return LINE_COMMENT; }
  {SYMBOL}               { return SYMBOL; }

}

<STRING> {
  {WHITE_SPACE} { return WHITE_SPACE; }
  "\\\""    { return ESCAPED_QUOTE; }
  "\\\\" { return ESCAPED_BACKSLASH; }
  "\"" { yypopState(); return QUOTE; }
  [^] { return STRING_LITERAL_CHAR; }
}

<SCHEME> {
  {WHITE_SPACE}          {
          if (schemeBracketsOpen == schemeBracketsOpenStack.peek()) {
              yypopState();
              schemeBracketsOpenStack.pop();
          }
          return WHITE_SPACE;
  }
  "\\"                   {
          if (schemeBracketsOpen == schemeBracketsOpenStack.peek()) {
              yypopState();
              schemeBracketsOpenStack.pop();
          }
          return BACKSLASH;
      }
  "\""                   { yypushState(STRING); return QUOTE; }
  "/"                    { return SCM_SLASH; }
  "="                    { return SCM_EQUALS; }
  "+"                    { return SCM_PLUS; }
  "-"                    { return SCM_MINUS; }
  "*"                    { return SCM_STAR; }
  "."                    { return SCM_DOT; }
  ","                    { return SCM_COMMA; }
  "'"                    { return SCM_SINGLE_QUOTE; }
  "?"                    { return SCM_QUESTION_MARK; }
  "!"                    { return SCM_EXCLAMATION_MARK; }
  "("                    { schemeBracketsOpen++; return SCM_LEFT_PAREN; }
  ")"                    {
          schemeBracketsOpen--;
          if (schemeBracketsOpen == schemeBracketsOpenStack.peek()) {
              yypopState();
              schemeBracketsOpenStack.pop();
          }
          return SCM_RIGHT_PAREN;
      }

  "#t"                   { return SCM_TRUE; }
  "#f"                   { return SCM_FALSE; }
  "#("                   { schemeBracketsOpen++; return SCM_VECTOR_OPEN; }

  "#{"                   { yypopState(); schemeBracketsOpenStack.push(schemeBracketsOpen); return SCM_LILY_START; }
  "#\\"                  { return SCM_CHAR_START; }
  "#"                    { return SCM_HASH; }
  "@"                    { return SCM_AT; }
  "<"                    { return SCM_SMALLER; }
  ">"                    { return SCM_GREATER; }
  "`"                    { return SCM_BACKTICK; }
//  "}"                    { yypopState(); return LEFT_BRACE; }
  {DIGIT}                { return SCM_DIGIT; }
  {SCM_IDENTIFIER}       { return SCM_IDENTIFIER; }
  {SCM_BLOCK_COMMENT}        { return SCM_BLOCK_COMMENT; }
  {SCM_LINE_COMMENT}         { return SCM_LINE_COMMENT; }
}

[^] { return BAD_CHARACTER; }
