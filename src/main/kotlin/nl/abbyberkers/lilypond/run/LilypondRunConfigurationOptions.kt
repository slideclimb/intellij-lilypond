package nl.abbyberkers.lilypond.run

import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import nl.abbyberkers.lilypond.run.core.LilypondPaths
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode

/**
 * Persisted state of a LilyPond run configuration.
 *
 * The property names are the serialized `<option name="...">` attributes, so renaming one silently
 * discards the corresponding value in every configuration users have already saved.
 */
class LilypondRunConfigurationOptions : LocatableRunConfigurationOptions() {
    var mainFilePath by string(null)

    /**
     * Blank means: look `lilypond` up on PATH when the configuration runs.
     */
    var executablePath by string(null)

    var outputDirectory by string(LilypondPaths.DEFAULT_OUTPUT_DIRECTORY)

    var extraArguments by string(null)

    var pdfViewer by enum(PdfViewerMode.BUILT_IN)

    var customViewerCommand by string(null)
}
