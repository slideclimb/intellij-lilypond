package nl.abbyberkers.lilypond.language.parser

import com.intellij.lexer.FlexAdapter

class LilypondLexerAdapter : FlexAdapter(LilypondLexer(null))