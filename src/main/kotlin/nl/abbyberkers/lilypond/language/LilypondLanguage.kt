package nl.abbyberkers.lilypond.language

import com.intellij.lang.Language

object LilypondLanguage : Language("LilyPond") {
    @Suppress("unused")
    private fun readResolve(): Any = LilypondLanguage
}