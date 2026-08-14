package nl.abbyberkers.lilypond.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import nl.abbyberkers.lilypond.run.core.LilypondCompileRequest
import nl.abbyberkers.lilypond.run.core.LilypondOutputs
import nl.abbyberkers.lilypond.run.pdf.PdfOpener
import nl.abbyberkers.lilypond.run.pdf.PdfViewerAvailability
import nl.abbyberkers.lilypond.run.pdf.PdfViewerMode
import nl.abbyberkers.lilypond.run.pdf.PdfViewerSettings
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

/**
 * Refreshes the output directory once LilyPond is done, and opens the score it produced.
 *
 * Every callback here runs on a process thread: off the EDT, and outside any read or write action.
 */
class LilypondPostRunListener(
    private val project: Project,
    private val request: LilypondCompileRequest,
    private val viewerSettings: PdfViewerSettings,
) : ProcessListener {
    private val startedAt = System.currentTimeMillis()

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode != 0) return

        val outputDirectory = Path.of(request.outputDirectory)
        // Synchronous: we hold no locks here, and an asynchronous refresh would race the open below.
        val outputDirectoryFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(outputDirectory)
        outputDirectoryFile?.refresh(false, true)

        if (viewerSettings.mode == PdfViewerMode.NONE) return

        val pdf = selectPdf(outputDirectory) ?: return
        val virtualFile = LocalFileSystem.getInstance().findFileByNioFile(pdf)
        val builtInAvailable = virtualFile != null && viewerSettings.mode == PdfViewerMode.BUILT_IN &&
            ReadAction.compute<Boolean, RuntimeException> {
                PdfViewerAvailability.isBuiltInViewerAvailable(project, virtualFile)
            }

        ApplicationManager.getApplication().invokeLater(
            { PdfOpener.open(project, pdf, virtualFile, viewerSettings, builtInAvailable) },
            ModalityState.nonModal(),
            project.disposed,
        )
    }

    /**
     * The PDFs are selected from the directory rather than predicted, because a score with several
     * `\book` blocks produces `song-1.pdf`, `song-2.pdf`, … next to `song.pdf`. Only the first is opened.
     */
    private fun selectPdf(outputDirectory: Path): Path? {
        val names = runCatching { outputDirectory.listDirectoryEntries().map { it.fileName.toString() } }
            .getOrElse { return null }
        val candidates = LilypondOutputs.selectProducedPdfs(request.outputBasename, names)
            .map(outputDirectory::resolve)
        // Keep only what this run wrote, so a stale song-2.pdf is not opened when this run produced
        // only song.pdf. The tolerance covers clock skew on network filesystems.
        val fresh = candidates.filter {
            runCatching { Files.getLastModifiedTime(it).toMillis() >= startedAt - TIMESTAMP_TOLERANCE_MS }
                .getOrDefault(true)
        }
        val produced = fresh.ifEmpty { candidates }

        if (produced.size > 1) log.info("LilyPond produced $produced, opening the first")
        return produced.firstOrNull()
    }

    companion object {
        private const val TIMESTAMP_TOLERANCE_MS = 2_000L

        private val log = logger<LilypondPostRunListener>()
    }
}
