package nl.abbyberkers.lilypond.language.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static nl.abbyberkers.lilypond.language.psi.LilypondTypes.*;

%%

%{
  public LilypondLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class LilypondLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

VARIABLE=[a-zA-Z]+
WHITESPACE=[ \t\n\x0B\f\r]+

%%
<YYINITIAL> {
  {WHITE_SPACE}       { return WHITE_SPACE; }

  "\\"                { return BACKSLASH; }
  "="                 { return EQUALS; }

  {VARIABLE}          { return VARIABLE; }
  {WHITESPACE}        { return WHITESPACE; }

}

[^] { return BAD_CHARACTER; }
