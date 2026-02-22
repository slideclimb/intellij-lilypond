package nl.abbyberkers.lilypond.language.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.abbyberkers.lilypond.language.LilypondFileType
import nl.abbyberkers.lilypond.language.LilypondLanguage
import org.jetbrains.annotations.NonNls

class LilypondFile(@NonNls viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LilypondLanguage) {

    override fun getFileType() = LilypondFileType

    override fun toString() = "LilyPond File"
}