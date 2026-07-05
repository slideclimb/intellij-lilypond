package nl.abbyberkers.lilypond.language.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import nl.abbyberkers.lilypond.language.parser.LilypondLexerAdapter
import nl.abbyberkers.lilypond.language.psi.LilypondTypes

class LilypondSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = LilypondLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        when (tokenType) {
            LilypondTypes.COMMAND_TOKEN -> COMMAND_KEYS
            LilypondTypes.STRING_LITERAL -> STRING_KEYS
            LilypondTypes.LINE_COMMENT, LilypondTypes.SCM_LINE_COMMENT -> LINE_COMMENT_KEYS
            LilypondTypes.BLOCK_COMMENT, LilypondTypes.SCM_BLOCK_COMMENT -> BLOCK_COMMENT_KEYS
            LilypondTypes.DIGIT, LilypondTypes.SCM_DIGIT -> NUMBER_KEYS
            LilypondTypes.LEFT_BRACE, LilypondTypes.RIGHT_BRACE,
            LilypondTypes.MULTI_VOICE_START, LilypondTypes.MULTI_VOICE_END,
            -> BRACES_KEYS
            LilypondTypes.LEFT_BRACKET, LilypondTypes.RIGHT_BRACKET -> BRACKETS_KEYS
            LilypondTypes.LEFT_PAREN, LilypondTypes.RIGHT_PAREN,
            LilypondTypes.SCM_LEFT_PAREN, LilypondTypes.SCM_RIGHT_PAREN,
            LilypondTypes.SCM_VECTOR_OPEN,
            -> PARENTHESES_KEYS
            LilypondTypes.SCM_START, LilypondTypes.SCM_START_DOLLAR,
            LilypondTypes.SCM_LILY_START, LilypondTypes.SCM_CONTINUE,
            -> SCHEME_MARKER_KEYS
            LilypondTypes.SCM_TRUE, LilypondTypes.SCM_FALSE -> CONSTANT_KEYS
            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
            else -> EMPTY_KEYS
        }

    companion object {
        val COMMAND = createTextAttributesKey("LILYPOND_COMMAND", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING = createTextAttributesKey("LILYPOND_STRING", DefaultLanguageHighlighterColors.STRING)
        val LINE_COMMENT = createTextAttributesKey("LILYPOND_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT = createTextAttributesKey("LILYPOND_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val NUMBER = createTextAttributesKey("LILYPOND_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val BRACES = createTextAttributesKey("LILYPOND_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS = createTextAttributesKey("LILYPOND_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val PARENTHESES = createTextAttributesKey("LILYPOND_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val SCHEME_MARKER = createTextAttributesKey("LILYPOND_SCHEME_MARKER", DefaultLanguageHighlighterColors.METADATA)
        val CONSTANT = createTextAttributesKey("LILYPOND_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)
        val BAD_CHARACTER = createTextAttributesKey("LILYPOND_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val COMMAND_KEYS = arrayOf(COMMAND)
        private val STRING_KEYS = arrayOf(STRING)
        private val LINE_COMMENT_KEYS = arrayOf(LINE_COMMENT)
        private val BLOCK_COMMENT_KEYS = arrayOf(BLOCK_COMMENT)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val PARENTHESES_KEYS = arrayOf(PARENTHESES)
        private val SCHEME_MARKER_KEYS = arrayOf(SCHEME_MARKER)
        private val CONSTANT_KEYS = arrayOf(CONSTANT)
        private val BAD_CHARACTER_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}