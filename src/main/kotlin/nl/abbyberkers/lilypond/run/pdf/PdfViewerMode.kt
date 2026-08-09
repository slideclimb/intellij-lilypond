package nl.abbyberkers.lilypond.run.pdf

import nl.abbyberkers.lilypond.LilypondBundle

enum class PdfViewerMode {
    BUILT_IN,
    SYSTEM_DEFAULT,
    CUSTOM_COMMAND,
    NONE,
    ;

    val displayName: String
        get() = when (this) {
            BUILT_IN -> LilypondBundle.message("run.settings.pdf.viewer.builtin")
            SYSTEM_DEFAULT -> LilypondBundle.message("run.settings.pdf.viewer.system")
            CUSTOM_COMMAND -> LilypondBundle.message("run.settings.pdf.viewer.custom")
            NONE -> LilypondBundle.message("run.settings.pdf.viewer.none")
        }
}
