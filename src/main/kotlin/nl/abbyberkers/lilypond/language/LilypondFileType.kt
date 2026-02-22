package nl.abbyberkers.lilypond.language

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import nl.abbyberkers.lilypond.LilypondIcons
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class LilypondFileType : LanguageFileType(LilypondLanguage) {
    override fun getName(): @NonNls String = "LilyPond source file"

    override fun getDescription(): @NlsContexts.Label String = "LilyPond source file"

    override fun getDefaultExtension(): @NlsSafe String = "ly"

    override fun getIcon(): Icon = LilypondIcons.FILE
}