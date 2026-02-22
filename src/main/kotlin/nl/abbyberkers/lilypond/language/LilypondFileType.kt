package nl.abbyberkers.lilypond.language

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.abbyberkers.lilypond.LilypondIcons

object LilypondFileType : LanguageFileType(LilypondLanguage) {

    override fun getName() = "LilyPond source file"

    override fun getDescription() = "LilyPond source file"

    override fun getDefaultExtension() = "ly"

    override fun getIcon() = LilypondIcons.FILE
}