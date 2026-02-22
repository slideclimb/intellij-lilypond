package nl.abbyberkers.lilypond.language.parser

import com.intellij.psi.tree.TokenSet

interface LilypondTokenSets {
    companion object {
        val COMMENTS = TokenSet.create()
        val STRING_LITERALS = TokenSet.create()
    }
}