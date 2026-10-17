package nl.abbyberkers.lilypond.run.pdf

/**
 * An immutable copy of the viewer settings, taken when the process starts: the run configuration may
 * be edited while it runs.
 */
data class PdfViewerSettings(
    val mode: PdfViewerMode,
    val customCommand: String?,
)
