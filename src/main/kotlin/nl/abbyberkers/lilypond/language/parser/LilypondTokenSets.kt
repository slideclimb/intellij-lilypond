package nl.abbyberkers.lilypond.language.parser

import com.intellij.psi.tree.TokenSet
import nl.abbyberkers.lilypond.language.psi.LilypondTypes

interface LilypondTokenSets {
    companion object {
        val COMMENTS = TokenSet.create(LilypondTypes.BLOCK_COMMENT, LilypondTypes.LINE_COMMENT, LilypondTypes.SCM_BLOCK_COMMENT, LilypondTypes.SCM_LINE_COMMENT)
        val STRING_LITERALS = TokenSet.create()
    }
}