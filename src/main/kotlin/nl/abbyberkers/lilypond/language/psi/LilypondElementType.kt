package nl.abbyberkers.lilypond.language.psi

import com.intellij.psi.tree.IElementType
import nl.abbyberkers.lilypond.language.LilypondLanguage
import org.jetbrains.annotations.NonNls

class LilypondElementType(@NonNls debugName: String) : IElementType(debugName, LilypondLanguage)