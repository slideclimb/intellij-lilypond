package nl.abbyberkers.lilypond.language.parser

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import nl.abbyberkers.lilypond.language.psi.LilypondTypes

interface LilypondTokenSets {
    companion object {
        // Registered via LilypondParserDefinition.getCommentTokens(), which makes the
        // PsiBuilder skip these tokens like whitespace. As a result comments may appear
        // anywhere between tokens without any grammar rule needing to mention them.
        // Referenced through LilypondTypes constants (not strings) so a token rename
        // fails to compile.
        val COMMENTS = TokenSet.create(
            LilypondTypes.LINE_COMMENT,
            LilypondTypes.BLOCK_COMMENT,
            LilypondTypes.SCM_LINE_COMMENT,
            LilypondTypes.SCM_BLOCK_COMMENT,
        )

        // Exposed via getStringLiteralElements(); drives string spell-check and language
        // injection. LilyPond and embedded Scheme share the one STRING_LITERAL token.
        val STRING_LITERALS = TokenSet.create(LilypondTypes.STRING_LITERAL)

        // Exposed via getWhitespaceTokens(); the lexer emits the platform WHITE_SPACE token.
        val WHITESPACES = TokenSet.create(TokenType.WHITE_SPACE)
    }
}
