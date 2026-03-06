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
  "\\+"                  { return ESCAPED_PLUS; }
  "\\!"                  { return ESCAPED_EXCLAMATION; }
  "\\\\"                 { return ESCAPED_BACKSLASH; }
  "\\version"            { return VERSION_COMMAND; }
  "\\new"                { return NEW_COMMAND; }
  "\\header"             { return HEADER_COMMAND; }
  "\\context"                { return CONTEXT_COMMAND; }
  "\\with"                { return WITH_COMMAND; }
  "\\drums"             { return DRUMS_COMMAND; }
  "\\figures"             { return FIGURES_COMMAND; }
  "\\chords"             { return CHORDS_COMMAND; }
  "\\lyrics"             { return LYRICSS_COMMAND; }
  "\\lyricsto"           { return LYRICSTO_COMMAND; }
  "\\relative"           { return RELATIVE_COMMAND; }
  "\\repeat"           { return REPEAT_COMMAND; }
  "\\alternative"           { return ALTERNATIVE_COMMAND; }
  "\\simultaneous"           { return SIMULTANEOUS_COMMAND; }
  "\\sequential"         { return SEQUENTIAL_COMMAND; }
  "\\tempo"              { return TEMPO_COMMAND; }
  "\\addlyrics"              { return ADDLYRICS_COMMAND; }
  "\\notemode"              { return  NOTEMODE_COMMAND; }
  "\\drummode"              { return  DRUMMODE_COMMAND; }
  "\\figuremode"              { return  FIGUREMODE_COMMAND; }
  "\\chordmode"              { return  CHORDMODE_COMMAND; }
  "\\lyricmode"              { return  LYRICMODE_COMMAND; }
  "\\change"              { return      CHANGE_COMMAND; }
  "\\unset"                 { return      UNSET_COMMAND; }
  "\\set"                 { return      SET_COMMAND; }
  "\\override"              { return      OVERRIDE_COMMAND; }
  "\\revert"                { return      REVERT_COMMAND; }
  "\\consists"              { return CONSISTS_COMMAND; }
  "\\remove"                { return REMOVE_COMMAND; }
  "\\accepts"               { return ACCEPTS_COMMAND; }
  "\\defaultchild"          { return DEFAULTCHILD_COMMAND; }
  "\\denies"                { return DENIES_COMMAND; }
  "\\alias"                 { return ALIAS_COMMAND; }
  "\\type"                  { return TYPE_COMMAND; }
  "\\description"           { return DESCRIPTION_COMMAND; }
  "\\name"                  { return NAME_COMMAND; }
  "\\etc"                  { return ETC_COMMAND; }
  "\\score"                  { return SCORE_COMMAND; }
  "\\book"                  { return BOOK_COMMAND; }
  "\\bookpart"                  { return BOOKPART_COMMAND; }
  "\\paper"                 { return PAPER_COMMAND; }
  "\\midi"                  { return MIDI_COMMAND; }
  "\\layout"                { return LAYOUT_COMMAND; }
  "\\rest"                { return REST_COMMAND; }
  "\\markuplist"                { return MARKUPLIST_COMMAND; }
  "\\markup"                { return MARKUP_COMMAND; }
  "\\score-lines"                { return SCORELINES_COMMAND; }
  "\\include"            { return INCLUDE_COMMAND; }
  "|"                    { return BAR; }
  "/"                    { return SLASH; }
  "\\"                   { return BACKSLASH; }
  ":"                    { return COLON; }
  "__"                   { return EXTENDER; }
  "_"                    { return UNDERSCORE; }
  "+"                    { return PLUS; }
  "--"                   { return HYPHEN; }
  "-"                    { return MINUS; }
  "*"                    { return STAR; }
  "^"                    { return HAT; }
  "="                    { return EQUALS; }
  "."                    { return DOT; }
  ","                    { return COMMA; }
  "'"                    { return SINGLE_QUOTE; }
//  "~"                    { return TILDE; }
//  "&"                    { return AMPERSAND; }
  "{"                    { return LEFT_BRACE; }
  "}"                    { return RIGHT_BRACE; }
  "["                    { return LEFT_BRACKET; }
  "]"                    { return RIGHT_BRACKET; }
  "?"                    { return QUESTION_MARK; }
  "!"                    { return EXCLAMATION_MARK; }
  "("                    { return LEFT_PAREN; }
  ")"                    { return RIGHT_PAREN; }
  "<<"                   { return MULTI_VOICE_START; }
  ">>"                   { return MULTI_VOICE_END; }
  // Tokenizer is greedy, so these should come after their double variant.
  "<"                    { return SMALLER; }
  ">"                    { return GREATER; }
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
