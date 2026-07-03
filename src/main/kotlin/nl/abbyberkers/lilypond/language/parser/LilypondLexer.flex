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

  // Saved lexer states, so we can nest LilyPond inside Scheme (#{ ... #}) and
  // Scheme inside LilyPond (#, $) arbitrarily. Language injection cannot express
  // this mutual nesting, so the state machine is hand-rolled.
  private final Deque<Integer> stack = new ArrayDeque<>();

  // For each #/$ scheme region still open, the paren depth that was in effect when
  // it started. A region ends once the depth returns to that captured baseline.
  private final Deque<Integer> schemeBracketsOpenStack = new ArrayDeque<>();

  private int schemeBracketsOpen = 0;

  public void yypushState(int newState) {
    stack.push(yystate());
    yybegin(newState);
  }

  public void yypopState() {
    yybegin(stack.isEmpty() ? YYINITIAL : stack.pop());
  }

  // True when the current paren depth has returned to the baseline captured when the
  // most recent #/$ scheme region was entered, i.e. that region is now complete.
  private boolean schemeRegionComplete() {
    return !schemeBracketsOpenStack.isEmpty()
        && schemeBracketsOpen == schemeBracketsOpenStack.peek();
  }
%}

%public
%class LilypondLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WS=\s+
DIGIT=[0-9]

// A LilyPond command/identifier reference: a backslash followed by letters. Command
// names contain no digits, so a lone backslash (\\, \!, ...) falls through to BACKSLASH.
COMMAND_TOKEN=\\[a-zA-Z]+

// A whole string literal, including \" and \\ escapes; may span newlines.
STRING_LITERAL=\"([^\"\\]|\\[^])*\"

// A bare run of "word" characters (note names, durations, etc.). Excludes every char
// that carries its own token. Unchanged from the original lexer to preserve tokenization.
WORD=[^\s\\\{\}%\[\]$\(\)|!\"'=&<>,.#]+

BLOCK_COMMENT=%\{[^]*?%\}
LINE_COMMENT=%[^\r\n]*

SCM_IDENTIFIER=[a-zA-Z\-_:$[\xc0-\xf6\xf8-\xff]]+
SCM_BLOCK_COMMENT=#\!\{[^(\!#)]\!#
SCM_LINE_COMMENT=;.*

%xstates SCHEME

%%

<YYINITIAL> {
  {STRING_LITERAL}       { return STRING_LITERAL; }
  {COMMAND_TOKEN}        { return COMMAND_TOKEN; }

  "<<"                   { return MULTI_VOICE_START; }
  ">>"                   { return MULTI_VOICE_END; }
  // Tokenizer is greedy, so these should come after their double variant.
  "<"                    { return SMALLER; }
  ">"                    { return GREATER; }

  "#}"                   {
          yypushState(SCHEME);
          if (!schemeBracketsOpenStack.isEmpty()) schemeBracketsOpenStack.pop();
          return SCM_CONTINUE;
      }
  "#"                    { yypushState(SCHEME); schemeBracketsOpenStack.push(schemeBracketsOpen); return SCM_START; }
  "$"                    { yypushState(SCHEME); schemeBracketsOpenStack.push(schemeBracketsOpen); return SCM_START_DOLLAR; }

  "|"                    { return BAR; }
  "/"                    { return SLASH; }
  "\\"                   { return BACKSLASH; }
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
  "&"                    { return AMPERSAND; }
  "{"                    { return LEFT_BRACE; }
  "}"                    { return RIGHT_BRACE; }
  "["                    { return LEFT_BRACKET; }
  "]"                    { return RIGHT_BRACKET; }
  "?"                    { return QUESTION_MARK; }
  "!"                    { return EXCLAMATION_MARK; }
  "("                    { return LEFT_PAREN; }
  ")"                    { return RIGHT_PAREN; }

  {DIGIT}                { return DIGIT; }
  {WS}                   { return WHITE_SPACE; }
  {BLOCK_COMMENT}        { return BLOCK_COMMENT; }
  {LINE_COMMENT}         { return LINE_COMMENT; }
  {WORD}                 { return WORD; }
}

<SCHEME> {
  {WS} {
          if (schemeRegionComplete()) { schemeBracketsOpenStack.pop(); yypopState(); }
          return WHITE_SPACE;
      }
  "\\" {
          if (schemeRegionComplete()) { schemeBracketsOpenStack.pop(); yypopState(); }
          return BACKSLASH;
      }
  {STRING_LITERAL}       { return STRING_LITERAL; }
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
          if (schemeRegionComplete()) { schemeBracketsOpenStack.pop(); yypopState(); }
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
  // A '}' inside Scheme closes the enclosing LilyPond block: end the scheme region and
  // re-lex the brace in the outer state so it becomes a RIGHT_BRACE with correct offsets.
  "}"                    {
          if (!schemeBracketsOpenStack.isEmpty()) schemeBracketsOpenStack.pop();
          yypushback(1);
          yypopState();
      }
  {DIGIT}                { return SCM_DIGIT; }
  {SCM_IDENTIFIER}       { return SCM_IDENTIFIER; }
  {SCM_BLOCK_COMMENT}    { return SCM_BLOCK_COMMENT; }
  {SCM_LINE_COMMENT}     { return SCM_LINE_COMMENT; }
  // Total fallback so no input can ever wedge the lexer inside the Scheme state.
  [^]                    { return BAD_CHARACTER; }
}

[^] { return BAD_CHARACTER; }
