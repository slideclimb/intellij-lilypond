package nl.abbyberkers.lilypond.language.psi

import com.intellij.psi.tree.IElementType
import nl.abbyberkers.lilypond.language.LilypondLanguage
import org.jetbrains.annotations.NonNls

class LilypondTokenType(@NonNls debugName: String) : IElementType(debugName, LilypondLanguage) {
    override fun toString(): String = "LilypondTokenType." + super.toString()
}